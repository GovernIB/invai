import type { Page } from '@playwright/test';

import { expect, test } from './support/auth.fixture';
import { findApiCatalogItem } from './support/catalog';
import { CleanupRegistry } from './support/cleanup-registry';
import { uniqueToken } from './support/data';
import { expectJsonResponse, expectSuccessfulResponse, waitForApiResponse } from './support/http';
import {
  expectToast,
  findRow,
  openMaintenancePanel,
  REQUIRED_ERROR,
  waitForInitialLoader,
} from './support/maintenance-resources';
import { ACTIVE_STATUS_ID, registerSoftDeleteCleanup } from './support/systems';
import { selectFilteredOption, selectRowAction } from './support/ui';

const API = {
  externalSystems: '/invaiback/external-system',
  companies: '/invaiback/company',
} as const;
const PANEL_TITLE = 'Sistemes externs';

interface CompanyRecord {
  id: number;
  name: string;
  deletedAt: string | null;
}

interface ExternalSystemRecord {
  id: number;
  name: string | null;
  company: CompanyRecord | null;
  deletedAt: string | null;
}

async function openExternalSystems(page: Page) {
  await page.goto('/manteniments/integracions#external-systems');
  await waitForInitialLoader(page);
  return openMaintenancePanel(page, PANEL_TITLE);
}

test('adds the Integracions maintenance tab with the external systems panel', async ({ page }) => {
  const panel = await openExternalSystems(page);

  await expect(page.locator('.maintenances-tabs a', { hasText: 'Integracions' })).toHaveAttribute(
    'aria-current',
    'page',
  );
  await expect(page.getByRole('button', { name: /Sistemes externs/ })).toHaveAttribute(
    'aria-expanded',
    'true',
  );
  // Soffid is part of the reference data of every environment.
  const soffid = findRow(panel, 'Soffid');
  await expect(soffid).toBeVisible();
  await expect(soffid.locator('td').nth(1)).not.toBeEmpty();
});

test('creates and deactivates an external system', async ({ page }, testInfo) => {
  test.setTimeout(120_000);
  const cleanup = new CleanupRegistry(testInfo);
  const name = `E2E DIR3CAIB ${uniqueToken(testInfo)}`;
  const created: { id?: number; matches: (item: ExternalSystemRecord) => boolean } = {
    matches: (item) => item.name === name,
  };
  registerSoftDeleteCleanup<ExternalSystemRecord>({
    page,
    cleanup,
    label: `external system ${name}`,
    pathname: API.externalSystems,
    target: created,
  });

  try {
    const panel = await openExternalSystems(page);
    const company = await findApiCatalogItem<CompanyRecord>({
      page,
      pathname: API.companies,
      params: { statusId: ACTIVE_STATUS_ID },
      matches: (item) => !item.deletedAt && !item.name.startsWith('E2E'),
    });
    if (!company) throw new Error('An active non-E2E company is required');

    const posts: unknown[] = [];
    page.on('request', (request) => {
      if (request.method() === 'POST' && new URL(request.url()).pathname === API.externalSystems) {
        posts.push(request.postDataJSON());
      }
    });
    await panel.getByRole('button', { name: 'Afegeix un sistema extern' }).click();
    await page.getByRole('button', { name: 'Afegeix el sistema extern' }).click();
    await expect(page.getByText(REQUIRED_ERROR).first()).toBeVisible();
    expect(posts).toHaveLength(0);

    await page.locator('#external-system-dialog-name').fill(`  ${name}  `);
    await selectFilteredOption(page, 'external-system-dialog-company', company.name, company.name);
    await page.screenshot({
      path: testInfo.outputPath('external-systems-create.png'),
      fullPage: true,
    });
    const createPromise = waitForApiResponse(page, 'POST', API.externalSystems);
    await page.getByRole('button', { name: 'Afegeix el sistema extern' }).click();

    const response = await createPromise;
    expect(response.request().postDataJSON()).toEqual({ name, companyId: company.id });
    created.id = (await expectJsonResponse<ExternalSystemRecord>(response)).id;
    await expectToast(page, "El sistema extern s'ha afegit correctament.");

    await panel.getByRole('textbox', { name: 'Cerca ràpida de sistemes externs' }).fill(name);
    const row = findRow(panel, name);
    await expect(row).toBeVisible();
    await expect(row).toContainText(company.name);

    const deletePromise = waitForApiResponse(
      page,
      'DELETE',
      `${API.externalSystems}/${created.id}`,
    );
    await selectRowAction(page, row, 'Dona de baixa', 'Obrir les accions del sistema extern');
    await page.getByRole('button', { name: 'Confirma la baixa del sistema extern' }).click();
    await expectSuccessfulResponse(await deletePromise);
    await expect(row).toHaveCount(0);
  } finally {
    await cleanup.runAll();
  }
});
