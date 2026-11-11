import type { Locator, Page } from '@playwright/test';

import { expect, test } from './support/application.fixture';
import type { CleanupRegistry } from './support/cleanup-registry';
import { uniqueToken } from './support/data';
import { apiUrl, expectApiJsonResponse, waitForApiResponse } from './support/http';
import {
  expectToast,
  findRow,
  INACTIVE_ROW_ACTIONS_BUTTON,
  openMaintenancePanel,
  RESET_BUTTON,
  SEARCH_BUTTON,
  showFilters,
  waitForInitialLoader,
} from './support/maintenance-resources';
import { type DatabaseVendorRecord, registerSoftDeleteCleanup, SYSTEMS_API } from './support/systems';
import { selectOption, selectRowAction } from './support/ui';

const WEB_CONTEXTS_API = '/invaiback/web-context';
const FILTERS_BUTTON = 'Mostra o amaga els filtres de contexts web';
const ACTIONS = {
  accept: 'Accepta la consulta del context web',
  add: 'Afegeix el context web',
  close: 'Tanca el formulari del context web',
  edit: 'Edita el context web',
  save: 'Desa els canvis del context web',
};
const UNSAVED = {
  title: 'Canvis sense desar',
  message: 'Hi ha canvis sense desar. Vols descartar-los o desar-los abans de sortir?',
  discard: 'Descarta els canvis i tanca el formulari',
  save: 'Desa els canvis i tanca el formulari',
};

interface WebContext {
  id: number;
  name: string;
  nameEs: string;
  deletedAt: string | null;
}

interface CapturedRequest {
  method: string;
  path: string;
  params: URLSearchParams;
}

test.describe('shared CRUD entity dialog', () => {
  test('Escape on a clean edit closes at once; on a dirty edit Descartar drops the changes', async ({
    page,
    cleanupRegistry,
  }, testInfo) => {
    const requests = recordWebContextRequests(page);
    const panel = await openWebContexts(page);
    const token = uniqueToken(testInfo);
    const intranet = await createWebContext(page, cleanupRegistry, `E2E ${token} Intranet`);
    await quickSearch(panel, token);
    const dialog = editDialog(page);

    await findRow(panel, intranet.name).dblclick();
    await expect(dialog.getByRole('heading', { name: 'Consultar context web' })).toBeVisible();
    await dialog.getByRole('button', { name: ACTIONS.edit, exact: true }).click();
    await expect(dialog.getByRole('heading', { name: 'Editar context web' })).toBeVisible();
    await expect(page.locator('#security-web-context-dialog-name')).toBeFocused();
    await page.keyboard.press('Escape');
    await expect(dialog).toHaveCount(0);
    await expect(unsavedDialog(page)).toHaveCount(0);

    await selectRowAction(page, findRow(panel, intranet.name), 'Edita');
    await expect(dialog.getByRole('heading', { name: 'Editar context web' })).toBeVisible();
    await page.locator('#security-web-context-dialog-name').fill(`${intranet.name} modificada`);
    await page.keyboard.press('Escape');

    const prompt = unsavedDialog(page);
    await expect(prompt.getByText(UNSAVED.message, { exact: true })).toBeVisible();
    await expect(prompt.getByRole('button', { name: UNSAVED.discard })).toHaveText('Descartar');
    await expect(prompt.getByRole('button', { name: UNSAVED.save })).toHaveText('Desar');
    await prompt.getByRole('button', { name: UNSAVED.discard }).click();
    await expect(prompt).toHaveCount(0);
    await expect(dialog).toHaveCount(0);

    await findRow(panel, intranet.name).dblclick();
    await expect(page.locator('#security-web-context-dialog-name')).toHaveValue(intranet.name);
    expect(requests.filter(({ method }) => method === 'PUT')).toEqual([]);
  });

  test('the close button on a dirty edit can be dismissed or saved with Desar', async ({
    page,
    cleanupRegistry,
  }, testInfo) => {
    const requests = recordWebContextRequests(page);
    const panel = await openWebContexts(page);
    const token = uniqueToken(testInfo);
    const intranet = await createWebContext(page, cleanupRegistry, `E2E ${token} Intranet`);
    await quickSearch(panel, token);
    const dialog = editDialog(page);
    const prompt = unsavedDialog(page);

    await selectRowAction(page, findRow(panel, intranet.name), 'Edita');
    await page.locator('#security-web-context-dialog-name-es').fill('Intranet corporativa');
    await dialog.getByRole('button', { name: ACTIONS.close, exact: true }).click();
    await expect(prompt.getByRole('button', { name: UNSAVED.discard })).toBeFocused();

    // Dismissing the prompt keeps editing with the pending change.
    await page.keyboard.press('Escape');
    await expect(prompt).toHaveCount(0);
    await expect(dialog.getByRole('heading', { name: 'Editar context web' })).toBeVisible();
    await expect(page.locator('#security-web-context-dialog-name-es')).toHaveValue(
      'Intranet corporativa',
    );

    await dialog.getByRole('button', { name: ACTIONS.close, exact: true }).click();
    const savePromise = waitForApiResponse(page, 'PUT', `${WEB_CONTEXTS_API}/${intranet.id}`);
    await prompt.getByRole('button', { name: UNSAVED.save }).click();
    const saved = await savePromise;
    expect(saved.ok(), await saved.text()).toBe(true);
    expect(saved.request().postDataJSON()).toEqual({
      name: intranet.name,
      nameEs: 'Intranet corporativa',
    });
    expect(requests.filter(({ method }) => method === 'PUT')).toHaveLength(1);
    await expectToast(page, "El registre s'ha actualitzat correctament.");
    await expect(dialog).toHaveCount(0);
    await expect(prompt).toHaveCount(0);
  });

  test('required messages and length limits block a create submission', async ({ page }) => {
    const requests = recordWebContextRequests(page);
    const panel = await openWebContexts(page);
    const dialog = editDialog(page);
    const name = page.locator('#security-web-context-dialog-name');
    const nameEs = page.locator('#security-web-context-dialog-name-es');

    await panel.getByRole('button', { name: 'Afegeix un context web', exact: true }).click();
    await dialog.getByRole('button', { name: ACTIONS.add, exact: true }).click();
    await expect(page.locator('#security-web-context-dialog-name-error')).toHaveText(
      'Camp obligatori',
    );
    await expect(page.locator('#security-web-context-dialog-name-es-error')).toHaveText(
      'Camp obligatori',
    );
    await expect(name).toHaveAttribute('aria-invalid', 'true');
    await expect(name).toHaveAttribute(
      'aria-describedby',
      'security-web-context-dialog-name-error',
    );

    // The back-end caps the names at the same lengths, so a longer value can never be loaded
    // for editing; the maxlength validators are covered by security-resource-forms.factory.spec.ts.
    await expect(name).toHaveAttribute('maxlength', '150');
    await expect(nameEs).toHaveAttribute('maxlength', '100');
    await name.fill('Nou context');
    await expect(page.locator('#security-web-context-dialog-name-error')).toHaveCount(0);
    await expect(name).toHaveAttribute('aria-invalid', 'false');
    await dialog.getByRole('button', { name: 'Cancel·la els canvis del context web' }).click();
    await expect(dialog).toHaveCount(0);
    expect(requests.filter(({ method }) => method === 'POST' || method === 'PUT')).toEqual([]);
  });

  test('Neteja resets the filters to the active default and searches again', async ({
    page,
    cleanupRegistry,
  }, testInfo) => {
    const requests = recordWebContextRequests(page);
    const panel = await openWebContexts(page);
    const token = uniqueToken(testInfo);
    const retired = await createWebContext(page, cleanupRegistry, `E2E ${token} Extranet`, {
      inactive: true,
    });
    const listRequests = () =>
      requests.filter(({ method, path }) => method === 'GET' && path === WEB_CONTEXTS_API);

    await showFilters(panel, FILTERS_BUTTON);
    await page.locator('#security-web-context-filter-name').fill(`  ${retired.name} `);
    await selectOption(page, 'security-web-context-filter-status', 'Inactiu');
    await panel.getByRole('button', { name: SEARCH_BUTTON }).click();
    await expect
      .poll(() => paramsOf(listRequests().at(-1)))
      .toEqual({ page: '0', size: '10', name: retired.name, statusId: '2' });
    await expect(findRow(panel, retired.name)).toBeVisible();
    await expect(
      panel.getByRole('button', { name: `${FILTERS_BUTTON} (2 filtres aplicats)`, exact: true }),
    ).toBeVisible();

    const before = listRequests().length;
    await panel.getByRole('button', { name: RESET_BUTTON }).click();
    await expect.poll(() => listRequests().length).toBe(before + 1);
    expect(paramsOf(listRequests().at(-1))).toEqual({ page: '0', size: '10', statusId: '1' });
    await expect(page.locator('#security-web-context-filter-name')).toHaveValue('');
    await expect(page.locator('#security-web-context-filter-status')).toHaveText('Actiu');
    await expect(
      panel.getByRole('button', { name: `${FILTERS_BUTTON} (1 filtre aplicat)`, exact: true }),
    ).toBeVisible();
    await expect(findRow(panel, retired.name)).toHaveCount(0);
  });

  test('the deactivation prompt can be cancelled and inactive rows only offer Restaura', async ({
    page,
    cleanupRegistry,
  }, testInfo) => {
    const requests = recordWebContextRequests(page);
    const panel = await openWebContexts(page);
    const token = uniqueToken(testInfo);
    const intranet = await createWebContext(page, cleanupRegistry, `E2E ${token} Intranet`);
    const retired = await createWebContext(page, cleanupRegistry, `E2E ${token} Extranet`, {
      inactive: true,
    });
    await quickSearch(panel, token);
    const confirmation = page.getByRole('dialog');

    await selectRowAction(page, findRow(panel, intranet.name), 'Dona de baixa');
    await expect(
      confirmation.getByRole('heading', { name: 'Donar de baixa el registre?' }),
    ).toBeVisible();
    await confirmation
      .getByRole('button', { name: 'Cancel·la la baixa del registre', exact: true })
      .click();
    await expect(confirmation).toHaveCount(0);

    await selectRowAction(page, findRow(panel, intranet.name), 'Dona de baixa');
    await expect(
      confirmation.getByRole('button', { name: 'Cancel·la la baixa del registre', exact: true }),
    ).toBeFocused();
    await page.keyboard.press('Escape');
    await expect(confirmation).toHaveCount(0);
    expect(requests.filter(({ method }) => method === 'DELETE')).toEqual([]);

    await showFilters(panel, FILTERS_BUTTON);
    await selectOption(page, 'security-web-context-filter-status', 'Inactiu');
    await panel.getByRole('button', { name: SEARCH_BUTTON }).click();
    const row = findRow(panel, retired.name);
    await expect(
      row.getByRole('button', { name: 'Obrir les accions del registre', exact: true }),
    ).toHaveCount(0);
    await row.getByRole('button', { name: INACTIVE_ROW_ACTIONS_BUTTON }).click();
    await expect(page.locator('.p-menu-overlay:visible').getByRole('menuitem')).toHaveText([
      'Restaura',
    ]);
    const restorePromise = waitForApiResponse(
      page,
      'PUT',
      `${WEB_CONTEXTS_API}/reactivate/${retired.id}`,
    );
    await page.getByRole('menuitem', { name: 'Restaura' }).click();
    const restored = await restorePromise;
    expect(restored.ok(), await restored.text()).toBe(true);
    expect(restored.request().postDataJSON()).toBeNull();
    await expectToast(page, "El registre s'ha restaurat correctament.");
  });

  test('the database vendor port is required and kept within 1-65535', async ({
    page,
    cleanupRegistry,
  }, testInfo) => {
    const vendorName = `E2E PostgreSQL ${uniqueToken(testInfo)}`;
    registerSoftDeleteCleanup<DatabaseVendorRecord>({
      page,
      cleanup: cleanupRegistry,
      label: `database vendor ${vendorName}`,
      pathname: SYSTEMS_API.databaseVendors,
      target: { matches: (item) => item.name === vendorName },
    });
    const posts: unknown[] = [];
    page.on('request', (request) => {
      if (
        request.method() === 'POST' &&
        new URL(request.url()).pathname === SYSTEMS_API.databaseVendors
      ) {
        posts.push(request.postDataJSON());
      }
    });
    await page.goto('/manteniments/sistemes#database-vendors');
    await waitForInitialLoader(page);
    const panel = await openMaintenancePanel(page, 'Proveïdors de bases de dades');
    const dialog = editDialog(page);
    const port = page.locator('#database-vendor-port');
    const portError = page.locator('#database-vendor-port-error');
    const add = dialog.getByRole('button', { name: 'Afegeix el proveïdor', exact: true });

    await panel
      .getByRole('button', { name: 'Afegir proveïdor de base de dades', exact: true })
      .click();
    await add.click();
    await expect(page.locator('#database-vendor-name-error')).toHaveText(
      'Aquest camp és obligatori.',
    );
    await expect(portError).toHaveText('El port ha de ser un nombre enter entre 1 i 65535.');
    expect(posts).toEqual([]);

    // The input number clamps out-of-range ports on blur instead of showing the message.
    await page.locator('#database-vendor-name').fill(vendorName);
    for (const [typed, clamped] of [
      ['0', '1'],
      ['70000', '65535'],
    ]) {
      await port.fill(typed);
      await port.blur();
      await expect(port).toHaveValue(clamped);
      await expect(portError).toHaveCount(0);
    }

    await port.fill('5432');
    await port.blur();
    const createPromise = waitForApiResponse(page, 'POST', SYSTEMS_API.databaseVendors);
    await add.click();
    const created = await createPromise;
    expect(created.ok(), await created.text()).toBe(true);
    expect(posts).toEqual([{ name: vendorName, defaultPort: 5432 }]);
    await expectToast(page, "El proveïdor s'ha afegit correctament.");
  });
});

function recordWebContextRequests(page: Page): CapturedRequest[] {
  const requests: CapturedRequest[] = [];
  page.on('request', (request) => {
    const url = new URL(request.url());
    if (!url.pathname.startsWith(WEB_CONTEXTS_API)) return;
    requests.push({ method: request.method(), path: url.pathname, params: url.searchParams });
  });
  return requests;
}

async function createWebContext(
  page: Page,
  cleanup: CleanupRegistry,
  name: string,
  { inactive = false }: { inactive?: boolean } = {},
): Promise<WebContext> {
  const created = await expectApiJsonResponse<WebContext>(
    await page.request.post(apiUrl(page, WEB_CONTEXTS_API), {
      data: { name, nameEs: `${name} ES` },
    }),
    `POST ${WEB_CONTEXTS_API}`,
  );
  registerSoftDeleteCleanup<WebContext>({
    page,
    cleanup,
    label: `web context ${name}`,
    pathname: WEB_CONTEXTS_API,
    target: { id: created.id, matches: (item) => item.id === created.id },
  });
  if (inactive) {
    const response = await page.request.delete(apiUrl(page, `${WEB_CONTEXTS_API}/${created.id}`));
    expect(response.ok(), await response.text()).toBeTruthy();
  }
  return created;
}

// Narrows the list to the records of the test, which may not be on the first page otherwise.
async function quickSearch(panel: Locator, token: string): Promise<void> {
  const listPromise = panel.page().waitForResponse(
    (response) =>
      new URL(response.url()).pathname === WEB_CONTEXTS_API &&
      new URL(response.url()).searchParams.get('search') === token,
  );
  await panel.getByRole('textbox', { name: 'Cerca ràpida de contexts web' }).fill(token);
  await listPromise;
}

function paramsOf(request: CapturedRequest | undefined): Record<string, string> | undefined {
  return request && Object.fromEntries(request.params);
}

async function openWebContexts(page: Page) {
  await page.goto('/manteniments/seguretat#web-contexts');
  await waitForInitialLoader(page);
  return openMaintenancePanel(page, 'Contexts web');
}

function editDialog(page: Page) {
  return page.getByRole('dialog').filter({ hasNot: page.getByText(UNSAVED.title) });
}

function unsavedDialog(page: Page) {
  return page.getByRole('dialog').filter({ has: page.getByText(UNSAVED.title, { exact: true }) });
}
