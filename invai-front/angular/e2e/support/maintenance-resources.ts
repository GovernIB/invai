import { expect, test } from '@playwright/test';
import type { Locator, Page, Request, TestInfo } from '@playwright/test';

import { CleanupRegistry } from './cleanup-registry';
import { uniqueToken } from './data';
import { expectJsonResponse, expectSuccessfulResponse, waitForApiResponse } from './http';
import { ACTIVE_STATUS_ID, INACTIVE_STATUS_ID, registerSoftDeleteCleanup } from './systems';
import { selectOption, selectRowAction } from './ui';

export const EMPTY_RESULTS = "No s'han trobat resultats";
export const INACTIVE_ROW_ACTIONS_BUTTON = 'Obrir les accions del registre inactiu';
export const REQUIRED_ERROR = 'Camp obligatori';
// Filter action names start with an icon glyph.
export const SEARCH_BUTTON = /Cerca$/;
export const RESET_BUTTON = /Neteja$/;

export interface NamedResourceRecord {
  id: number;
  name: string | null;
  nameEs?: string | null;
  deletedAt: string | null;
}

export interface NamedResourceDialogActions {
  accept: string;
  add: string;
  cancel: string;
  close: string;
  deactivate: string;
  edit: string;
  restore: string;
  save: string;
}

export interface NamedResourceCase {
  route: string;
  panelId: string;
  title: string;
  apiPath: string;
  idPrefix: string;
  bilingual: boolean;
  singular: string;
  quickSearchLabel: string;
  filtersButtonLabel: string;
  addButtonLabel: string;
  actions: NamedResourceDialogActions;
}

export async function waitForInitialLoader(page: Page): Promise<void> {
  await expect(page.locator('.app-initial-loader')).toBeHidden({ timeout: 30_000 });
}

export function maintenancePanelHeader(page: Page, title: string): Locator {
  return page.getByRole('button', { name: new RegExp(`^${escapeRegExp(title)}`) });
}

export async function openMaintenancePanel(page: Page, title: string): Promise<Locator> {
  const header = maintenancePanelHeader(page, title);
  const panel = page.locator('p-accordion-panel').filter({ has: header });
  if ((await header.getAttribute('aria-expanded')) !== 'true') await header.click();
  await expect(header).toHaveAttribute('aria-expanded', 'true');
  await expect(panel.locator('.invai-table-loading-container')).toHaveAttribute(
    'aria-busy',
    'false',
  );
  return panel;
}

export async function expectPanelFragments(
  page: Page,
  route: string,
  panels: Array<{ title: string; fragment: string }>,
): Promise<void> {
  await page.goto(route);
  await waitForInitialLoader(page);
  for (const panel of panels) {
    await expect(maintenancePanelHeader(page, panel.title)).toHaveAttribute(
      'aria-expanded',
      'false',
    );
  }

  for (const panel of panels) {
    const header = maintenancePanelHeader(page, panel.title);
    await header.click();
    await expect(page).toHaveURL(`${route}#${panel.fragment}`);
    await expect(header).toHaveAttribute('aria-expanded', 'true');
  }

  for (const panel of panels) {
    await page.goto(`${route}#${panel.fragment}`);
    await waitForInitialLoader(page);
    for (const other of panels) {
      await expect(maintenancePanelHeader(page, other.title)).toHaveAttribute(
        'aria-expanded',
        String(other === panel),
      );
    }
  }

  await page.goto(`${route}#unknown-panel`);
  await waitForInitialLoader(page);
  for (const panel of panels) {
    await expect(maintenancePanelHeader(page, panel.title)).toHaveAttribute(
      'aria-expanded',
      'false',
    );
  }
}

export function findRow(panel: Locator, value: string): Locator {
  return panel.locator('.invai-table-consultable-row').filter({ hasText: value });
}

export async function showFilters(panel: Locator, toggleName: string): Promise<void> {
  const toggle = panel.getByRole('button', { name: toggleName });
  const search = panel.getByRole('button', { name: SEARCH_BUTTON });
  if (!(await search.isVisible())) await toggle.click();
  await expect(search).toBeVisible();
}

export function hasQuery(url: URL, expected: Record<string, string | number>): boolean {
  return Object.entries(expected).every(
    ([key, value]) => url.searchParams.get(key) === String(value),
  );
}

export function recordRequests(page: Page, method: string, pathname: string): Request[] {
  const requests: Request[] = [];
  page.on('request', (request) => {
    if (request.method() === method && new URL(request.url()).pathname === pathname) {
      requests.push(request);
    }
  });
  return requests;
}

export async function expectToast(page: Page, detail: string): Promise<void> {
  await expect(page.locator('.p-toast').getByText(detail, { exact: true }).first()).toBeVisible();
}

// Create -> consult -> edit -> deactivate -> inactive filter -> restore, through the UI.
export async function runNamedResourceLifecycle(
  page: Page,
  testInfo: TestInfo,
  resource: NamedResourceCase,
): Promise<void> {
  const cleanup = new CleanupRegistry(testInfo);
  const token = uniqueToken(testInfo);
  const initial = withLocale(resource, `E2E ${token} inicial`, `E2E ${token} inicial ES`);
  const updated = withLocale(resource, `E2E ${token} actualitzat`, `E2E ${token} actualizado ES`);
  const target: { id?: number; matches: (item: NamedResourceRecord) => boolean } = {
    matches: (item) => item.name === initial.name || item.name === updated.name,
  };
  const dialog = page.getByRole('dialog');
  const nameInput = page.locator(`#${resource.idPrefix}-dialog-name`);
  const nameEsInput = page.locator(`#${resource.idPrefix}-dialog-name-es`);

  try {
    await page.goto(`${resource.route}#${resource.panelId}`);
    await waitForInitialLoader(page);
    registerSoftDeleteCleanup<NamedResourceRecord>({
      page,
      cleanup,
      label: `${resource.apiPath} ${token}`,
      pathname: resource.apiPath,
      target,
      findParams: { statusId: ACTIVE_STATUS_ID, search: token },
    });
    const panel = await openMaintenancePanel(page, resource.title);

    await test.step('quick search narrows the list to the unique token', async () => {
      const searchPromise = waitForApiResponse(page, 'GET', resource.apiPath, (url) =>
        hasQuery(url, { search: token, statusId: ACTIVE_STATUS_ID }),
      );
      await panel.getByRole('textbox', { name: resource.quickSearchLabel }).fill(token);
      await expectSuccessfulResponse(await searchPromise);
      await expect(panel.getByText(EMPTY_RESULTS, { exact: true })).toBeVisible();
    });

    await test.step('create validates required fields and POSTs the trimmed values', async () => {
      const posts = recordRequests(page, 'POST', resource.apiPath);
      await panel.getByRole('button', { name: resource.addButtonLabel, exact: true }).click();
      await expect(
        dialog.getByRole('heading', { name: `Afegir ${resource.singular}`, exact: true }),
      ).toBeVisible();

      await dialog.getByRole('button', { name: resource.actions.add, exact: true }).click();
      await expect(dialog.getByText(REQUIRED_ERROR, { exact: true })).toHaveCount(
        resource.bilingual ? 2 : 1,
      );
      await expect(nameInput).toHaveAttribute('aria-invalid', 'true');
      await expect(nameEsInput).toHaveCount(resource.bilingual ? 1 : 0);

      await nameInput.fill('   ');
      if (resource.bilingual) await nameEsInput.fill(initial.nameEs!);
      await dialog.getByRole('button', { name: resource.actions.add, exact: true }).click();
      await expect(dialog.getByText(REQUIRED_ERROR, { exact: true })).toHaveCount(1);
      expect(posts).toHaveLength(0);

      await nameInput.fill(`  ${initial.name}  `);
      const createPromise = waitForApiResponse(page, 'POST', resource.apiPath);
      const refreshPromise = waitForApiResponse(page, 'GET', resource.apiPath, (url) =>
        hasQuery(url, { search: token, statusId: ACTIVE_STATUS_ID }),
      );
      await dialog.getByRole('button', { name: resource.actions.add, exact: true }).click();
      const response = await createPromise;
      expect(response.request().postDataJSON()).toEqual(initial);
      const created = await expectJsonResponse<NamedResourceRecord>(response);
      target.id = created.id;
      expect(created).toMatchObject({ id: expect.any(Number), ...initial, deletedAt: null });
      await expectSuccessfulResponse(await refreshPromise);
      await expectToast(page, "El registre s'ha afegit correctament.");
      await expect(dialog).toHaveCount(0);
      await expect(findRow(panel, initial.name)).toBeVisible();
    });

    await test.step('double click consults the record in read-only mode', async () => {
      const detailPromise = waitForApiResponse(page, 'GET', `${resource.apiPath}/${target.id}`);
      await findRow(panel, initial.name).dblclick();
      await expectJsonResponse<NamedResourceRecord>(await detailPromise);
      await expect(
        dialog.getByRole('heading', { name: `Consultar ${resource.singular}`, exact: true }),
      ).toBeVisible();
      await expect(nameInput).toHaveValue(initial.name);
      await expect(nameInput).not.toBeEditable();
      if (resource.bilingual) {
        await expect(nameEsInput).toHaveValue(initial.nameEs!);
        await expect(nameEsInput).not.toBeEditable();
      }
      await expect(
        dialog.getByRole('button', { name: resource.actions.edit, exact: true }),
      ).toBeVisible();
      await expect(dialog.getByRole('button', { name: resource.actions.restore })).toHaveCount(0);
      await dialog.getByRole('button', { name: resource.actions.accept, exact: true }).click();
      await expect(dialog).toHaveCount(0);
    });

    await test.step('row action Edita PUTs the new values', async () => {
      const detailPromise = waitForApiResponse(page, 'GET', `${resource.apiPath}/${target.id}`);
      await selectRowAction(page, findRow(panel, initial.name), 'Edita');
      await expectJsonResponse<NamedResourceRecord>(await detailPromise);
      await expect(
        dialog.getByRole('heading', { name: `Editar ${resource.singular}`, exact: true }),
      ).toBeVisible();
      await expect(nameInput).toBeEditable();
      await nameInput.fill(updated.name);
      if (resource.bilingual) await nameEsInput.fill(updated.nameEs!);

      const updatePromise = waitForApiResponse(page, 'PUT', `${resource.apiPath}/${target.id}`);
      await dialog.getByRole('button', { name: resource.actions.save, exact: true }).click();
      const response = await updatePromise;
      expect(response.request().postDataJSON()).toEqual(updated);
      expect(await expectJsonResponse<NamedResourceRecord>(response)).toMatchObject({
        id: target.id,
        ...updated,
      });
      await expectToast(page, "El registre s'ha actualitzat correctament.");
      await expect(findRow(panel, updated.name)).toBeVisible();
      await expect(findRow(panel, initial.name)).toHaveCount(0);
    });

    await test.step('row action Dona de baixa confirms and DELETEs the record', async () => {
      await selectRowAction(page, findRow(panel, updated.name), 'Dona de baixa');
      await expect(
        dialog.getByRole('heading', { name: 'Donar de baixa el registre?', exact: true }),
      ).toBeVisible();
      await expect(
        dialog.getByText(`Estàs a punt de donar de baixa «${updated.name}».`, { exact: true }),
      ).toBeVisible();
      const deletePromise = waitForApiResponse(
        page,
        'DELETE',
        `${resource.apiPath}/${target.id}`,
      );
      await dialog
        .getByRole('button', { name: 'Confirma la baixa del registre', exact: true })
        .click();
      await expectSuccessfulResponse(await deletePromise);
      await expectToast(page, "El registre s'ha donat de baixa correctament.");
      await expect(findRow(panel, updated.name)).toHaveCount(0);
      await expect(panel.getByText(EMPTY_RESULTS, { exact: true })).toBeVisible();
    });

    await test.step('the inactive status filter finds the record and consult offers restore', async () => {
      await showFilters(panel, resource.filtersButtonLabel);
      await page.locator(`#${resource.idPrefix}-filter-name`).fill(updated.name);
      await selectOption(page, `${resource.idPrefix}-filter-status`, 'Inactiu');
      const inactivePromise = waitForApiResponse(page, 'GET', resource.apiPath, (url) =>
        hasQuery(url, { name: updated.name, statusId: INACTIVE_STATUS_ID, search: token }),
      );
      await panel.getByRole('button', { name: SEARCH_BUTTON }).click();
      const inactive = await expectJsonResponse<{ content: NamedResourceRecord[] }>(
        await inactivePromise,
      );
      expect(inactive.content.map(({ id }) => id)).toEqual([target.id]);
      await expect(
        panel.getByRole('button', {
          name: `${resource.filtersButtonLabel} (2 filtres aplicats)`,
          exact: true,
        }),
      ).toBeVisible();

      const row = findRow(panel, updated.name);
      await expect(row).toBeVisible();
      await expect(row.getByRole('button', { name: INACTIVE_ROW_ACTIONS_BUTTON })).toBeVisible();

      await row.dblclick();
      await expect(
        dialog.getByRole('heading', { name: `Consultar ${resource.singular}`, exact: true }),
      ).toBeVisible();
      await expect(
        dialog.getByRole('button', { name: resource.actions.restore, exact: true }),
      ).toBeVisible();
      await expect(dialog.getByRole('button', { name: resource.actions.edit })).toHaveCount(0);
      await dialog.getByRole('button', { name: resource.actions.accept, exact: true }).click();
      await expect(dialog).toHaveCount(0);
    });

    await test.step('Restaura reactivates the record and the active filter finds it again', async () => {
      const restorePromise = waitForApiResponse(
        page,
        'PUT',
        `${resource.apiPath}/reactivate/${target.id}`,
      );
      await selectRowAction(
        page,
        findRow(panel, updated.name),
        'Restaura',
        INACTIVE_ROW_ACTIONS_BUTTON,
      );
      const response = await restorePromise;
      expect(response.request().postDataJSON()).toBeNull();
      expect(await expectJsonResponse<NamedResourceRecord>(response)).toMatchObject({
        id: target.id,
        deletedAt: null,
      });
      await expectToast(page, "El registre s'ha restaurat correctament.");
      await expect(findRow(panel, updated.name)).toHaveCount(0);

      await selectOption(page, `${resource.idPrefix}-filter-status`, 'Actiu');
      const activePromise = waitForApiResponse(page, 'GET', resource.apiPath, (url) =>
        hasQuery(url, { name: updated.name, statusId: ACTIVE_STATUS_ID }),
      );
      await panel.getByRole('button', { name: SEARCH_BUTTON }).click();
      await expectSuccessfulResponse(await activePromise);
      await expect(findRow(panel, updated.name)).toBeVisible();
    });
  } finally {
    await cleanup.runAll();
  }
}

function withLocale(
  resource: NamedResourceCase,
  name: string,
  nameEs: string,
): { name: string; nameEs?: string } {
  return resource.bilingual ? { name, nameEs } : { name };
}

export function escapeRegExp(value: string): string {
  return value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}
