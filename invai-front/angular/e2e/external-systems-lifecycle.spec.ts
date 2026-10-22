import { expect, test } from './support/auth.fixture';
import type { Locator, Page } from '@playwright/test';

import { findApiCatalogItem } from './support/catalog';
import { CleanupRegistry } from './support/cleanup-registry';
import { uniqueToken } from './support/data';
import {
  apiUrl,
  expectApiJsonResponse,
  expectJsonResponse,
  expectSuccessfulResponse,
  waitForApiResponse,
} from './support/http';
import {
  expectToast,
  findRow,
  hasQuery,
  INACTIVE_ROW_ACTIONS_BUTTON,
  openMaintenancePanel,
  SEARCH_BUTTON,
  showFilters,
  waitForInitialLoader,
} from './support/maintenance-resources';
import { ACTIVE_STATUS_ID, INACTIVE_STATUS_ID, registerSoftDeleteCleanup } from './support/systems';
import { selectFilteredOption, selectOption, selectRowAction } from './support/ui';

const INTEGRATIONS_ROUTE = '/manteniments/integracions';
const PANEL_TITLE = 'Sistemes externs';
const FILTERS_BUTTON = 'Mostra o amaga els filtres';
const ROW_ACTIONS_BUTTON = 'Obrir les accions del sistema extern';
const API = {
  externalSystems: '/invaiback/external-system',
  companies: '/invaiback/company',
} as const;

interface CompanyRecord {
  id: number;
  name: string;
  nif: string | null;
  deletedAt: string | null;
}

interface ExternalSystemRecord {
  id: number;
  name: string | null;
  company: CompanyRecord | null;
  deletedAt: string | null;
}

test.describe('external systems lifecycle', () => {
  test('consult, edit, company filter with inactive companies, deactivate and restore', async ({
    page,
  }, testInfo) => {
    test.setTimeout(300_000);
    const cleanup = new CleanupRegistry(testInfo);
    const token = uniqueToken(testInfo);
    const companyName = `E2E Empresa ${token}`;
    const initialName = `E2E Sistema extern ${token}`;
    const updatedName = `E2E Sistema extern actualitzat ${token}`;
    const dialog = page.getByRole('dialog');
    const nameInput = page.locator('#external-system-dialog-name');

    try {
      await page.goto(`${INTEGRATIONS_ROUTE}#external-systems`);
      await waitForInitialLoader(page);

      const activeCompany = await findApiCatalogItem<CompanyRecord>({
        page,
        pathname: API.companies,
        params: { statusId: ACTIVE_STATUS_ID },
        matches: (item) => !item.deletedAt && !item.name.startsWith('E2E'),
      });
      if (!activeCompany) throw new Error('An active non-E2E company is required');

      const company = await expectApiJsonResponse<CompanyRecord>(
        await page.request.post(apiUrl(page, API.companies), {
          data: { nif: null, name: companyName },
        }),
        `POST ${API.companies}`,
      );
      registerSoftDeleteCleanup<CompanyRecord>({
        page,
        cleanup,
        label: `company ${companyName}`,
        pathname: API.companies,
        target: { id: company.id, matches: (item) => item.name === companyName },
      });
      const system = await expectApiJsonResponse<ExternalSystemRecord>(
        await page.request.post(apiUrl(page, API.externalSystems), {
          data: { name: initialName, companyId: company.id },
        }),
        `POST ${API.externalSystems}`,
      );
      registerSoftDeleteCleanup<ExternalSystemRecord>({
        page,
        cleanup,
        label: `external system ${initialName}`,
        pathname: API.externalSystems,
        target: {
          id: system.id,
          matches: (item) => item.name === initialName || item.name === updatedName,
        },
      });
      const companyDelete = await page.request.delete(
        apiUrl(page, `${API.companies}/${company.id}`),
      );
      expect(companyDelete.ok(), await companyDelete.text()).toBeTruthy();

      // Company options are resolved on navigation, after the fixtures exist.
      await page.reload();
      await waitForInitialLoader(page);
      const panel = await openMaintenancePanel(page, PANEL_TITLE);

      await test.step('the company filter lists inactive companies and sends companyId', async () => {
        await showFilters(panel, FILTERS_BUTTON);
        await selectFilteredOption(page, 'external-systems-filter-company', token, companyName);
        const filteredPromise = waitForApiResponse(page, 'GET', API.externalSystems, (url) =>
          hasQuery(url, { companyId: company.id, statusId: ACTIVE_STATUS_ID }),
        );
        await panel.getByRole('button', { name: SEARCH_BUTTON }).click();
        const filtered = await expectJsonResponse<{ content: ExternalSystemRecord[] }>(
          await filteredPromise,
        );
        expect(filtered.content.map(({ id }) => id)).toEqual([system.id]);
        const row = findRow(panel, initialName);
        await expect(row).toBeVisible();
        await expect(row).toContainText(companyName);
      });

      await test.step('double click consults name and company as read-only values', async () => {
        const detailPromise = waitForApiResponse(
          page,
          'GET',
          `${API.externalSystems}/${system.id}`,
        );
        await findRow(panel, initialName).dblclick();
        await expectJsonResponse<ExternalSystemRecord>(await detailPromise);
        await expect(
          dialog.getByRole('heading', { name: 'Consultar sistema extern', exact: true }),
        ).toBeVisible();
        await expect(nameInput).toHaveValue(initialName);
        await expect(nameInput).not.toBeEditable();
        await expect(
          dialog.locator('[aria-labelledby="external-system-dialog-company-view-label"]'),
        ).toHaveText(companyName);
        await expect(dialog.locator('#external-system-dialog-company')).toHaveCount(0);
        await dialog
          .getByRole('button', { name: 'Accepta la consulta del sistema extern', exact: true })
          .click();
        await expect(dialog).toHaveCount(0);
      });

      await test.step('edit keeps the inactive current company selectable and PUTs a new one', async () => {
        const detailPromise = waitForApiResponse(
          page,
          'GET',
          `${API.externalSystems}/${system.id}`,
        );
        await selectRowAction(page, findRow(panel, initialName), 'Edita', ROW_ACTIONS_BUTTON);
        await expectJsonResponse<ExternalSystemRecord>(await detailPromise);
        await expect(
          dialog.getByRole('heading', { name: 'Editar sistema extern', exact: true }),
        ).toBeVisible();
        await expect(page.locator('#external-system-dialog-company').locator('..')).toContainText(
          companyName,
        );

        await nameInput.fill(updatedName);
        await selectFilteredOption(
          page,
          'external-system-dialog-company',
          activeCompany.name,
          activeCompany.name,
        );
        const updatePromise = waitForApiResponse(
          page,
          'PUT',
          `${API.externalSystems}/${system.id}`,
        );
        await dialog
          .getByRole('button', { name: 'Desa els canvis del sistema extern', exact: true })
          .click();
        const response = await updatePromise;
        expect(response.request().postDataJSON()).toEqual({
          name: updatedName,
          companyId: activeCompany.id,
        });
        expect(await expectJsonResponse<ExternalSystemRecord>(response)).toMatchObject({
          id: system.id,
          name: updatedName,
          company: { id: activeCompany.id },
        });
        await expectToast(page, "El sistema extern s'ha actualitzat correctament.");
        await expect(dialog).toHaveCount(0);
      });

      await test.step('the create dialog only offers active companies', async () => {
        await panel
          .getByRole('button', { name: 'Afegeix un sistema extern', exact: true })
          .click();
        await expect(
          dialog.getByRole('heading', { name: 'Afegir sistema extern', exact: true }),
        ).toBeVisible();
        await page.locator('#external-system-dialog-company').click();
        const overlay = page.locator('.p-select-overlay:visible');
        await overlay.locator('input.p-select-filter').fill(token);
        await expect(overlay.getByRole('option', { name: companyName })).toHaveCount(0);
        await overlay.locator('input.p-select-filter').fill(activeCompany.name);
        await expect(
          overlay.getByRole('option', { name: activeCompany.name, exact: true }),
        ).toBeVisible();
        await page.keyboard.press('Escape');
        await dialog
          .getByRole('button', { name: 'Cancel·la els canvis del sistema extern', exact: true })
          .click();
        await expect(dialog).toHaveCount(0);
      });

      await test.step('Dona de baixa confirms and DELETEs the external system', async () => {
        await searchByName(page, panel, updatedName, 'Actiu', ACTIVE_STATUS_ID);
        await selectRowAction(
          page,
          findRow(panel, updatedName),
          'Dona de baixa',
          ROW_ACTIONS_BUTTON,
        );
        await expect(
          dialog.getByRole('heading', { name: 'Donar de baixa el sistema extern?', exact: true }),
        ).toBeVisible();
        await expect(
          dialog.getByText(`Estàs a punt de donar de baixa «${updatedName}».`, { exact: true }),
        ).toBeVisible();
        const deletePromise = waitForApiResponse(
          page,
          'DELETE',
          `${API.externalSystems}/${system.id}`,
        );
        await dialog
          .getByRole('button', { name: 'Confirma la baixa del sistema extern', exact: true })
          .click();
        await expectSuccessfulResponse(await deletePromise);
        await expectToast(page, "El sistema extern s'ha donat de baixa correctament.");
        await expect(findRow(panel, updatedName)).toHaveCount(0);
      });

      await test.step('the inactive filter finds it and Restaura reactivates it', async () => {
        await searchByName(page, panel, updatedName, 'Inactiu', INACTIVE_STATUS_ID);
        const row = findRow(panel, updatedName);
        await expect(row).toBeVisible();
        await expect(row.getByRole('button', { name: ROW_ACTIONS_BUTTON })).toHaveCount(0);

        const restorePromise = waitForApiResponse(
          page,
          'PUT',
          `${API.externalSystems}/reactivate/${system.id}`,
        );
        await selectRowAction(page, row, 'Restaura', INACTIVE_ROW_ACTIONS_BUTTON);
        const response = await restorePromise;
        expect(response.request().postDataJSON()).toBeNull();
        expect(await expectJsonResponse<ExternalSystemRecord>(response)).toMatchObject({
          id: system.id,
          deletedAt: null,
        });
        await expectToast(page, "El sistema extern s'ha restaurat correctament.");

        await searchByName(page, panel, updatedName, 'Actiu', ACTIVE_STATUS_ID);
        await expect(findRow(panel, updatedName)).toBeVisible();
      });
    } finally {
      await cleanup.runAll();
    }
  });
});

async function searchByName(
  page: Page,
  panel: Locator,
  name: string,
  status: 'Actiu' | 'Inactiu',
  statusId: number,
): Promise<void> {
  await showFilters(panel, FILTERS_BUTTON);
  await page.locator('#external-systems-filter-name').fill(name);
  await selectOption(page, 'external-systems-filter-status', status);
  const responsePromise = waitForApiResponse(page, 'GET', API.externalSystems, (url) =>
    hasQuery(url, { name, statusId }),
  );
  await panel.getByRole('button', { name: SEARCH_BUTTON }).click();
  await expectSuccessfulResponse(await responsePromise);
}
