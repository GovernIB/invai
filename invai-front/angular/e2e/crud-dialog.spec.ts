import { expect, test, type Page, type Route } from '@playwright/test';

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
import { selectOption, selectRowAction } from './support/ui';

test.use({ storageState: { cookies: [], origins: [] } });

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
  body: unknown;
}

const INTRANET: WebContext = { id: 7, name: 'Intranet', nameEs: 'Intranet', deletedAt: null };
const LONG_NAME: WebContext = {
  id: 8,
  name: `Context ${'x'.repeat(143)}`,
  nameEs: 'Contexto largo',
  deletedAt: null,
};
const RETIRED: WebContext = {
  id: 9,
  name: 'Extranet retirada',
  nameEs: 'Extranet retirada',
  deletedAt: '2026-01-15T10:00:00',
};

test.describe('shared CRUD entity dialog', () => {
  test('Escape on a clean edit closes at once; on a dirty edit Descartar drops the changes', async ({
    page,
  }) => {
    const { requests } = await mockWebContexts(page);
    const panel = await openWebContexts(page);
    const dialog = editDialog(page);

    await findRow(panel, INTRANET.name).dblclick();
    await expect(dialog.getByRole('heading', { name: 'Consultar context web' })).toBeVisible();
    await dialog.getByRole('button', { name: ACTIONS.edit, exact: true }).click();
    await expect(dialog.getByRole('heading', { name: 'Editar context web' })).toBeVisible();
    await expect(page.locator('#security-web-context-dialog-name')).toBeFocused();
    await page.keyboard.press('Escape');
    await expect(dialog).toHaveCount(0);
    await expect(unsavedDialog(page)).toHaveCount(0);

    await selectRowAction(page, findRow(panel, INTRANET.name), 'Edita');
    await expect(dialog.getByRole('heading', { name: 'Editar context web' })).toBeVisible();
    await page.locator('#security-web-context-dialog-name').fill('Intranet modificada');
    await page.keyboard.press('Escape');

    const prompt = unsavedDialog(page);
    await expect(prompt.getByText(UNSAVED.message, { exact: true })).toBeVisible();
    await expect(prompt.getByRole('button', { name: UNSAVED.discard })).toHaveText('Descartar');
    await expect(prompt.getByRole('button', { name: UNSAVED.save })).toHaveText('Desar');
    await prompt.getByRole('button', { name: UNSAVED.discard }).click();
    await expect(prompt).toHaveCount(0);
    await expect(dialog).toHaveCount(0);

    await findRow(panel, INTRANET.name).dblclick();
    await expect(page.locator('#security-web-context-dialog-name')).toHaveValue(INTRANET.name);
    expect(requests.filter(({ method }) => method === 'PUT')).toEqual([]);
  });

  test('the close button on a dirty edit can be dismissed or saved with Desar', async ({
    page,
  }) => {
    const { requests } = await mockWebContexts(page);
    const panel = await openWebContexts(page);
    const dialog = editDialog(page);
    const prompt = unsavedDialog(page);

    await selectRowAction(page, findRow(panel, INTRANET.name), 'Edita');
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
    await prompt.getByRole('button', { name: UNSAVED.save }).click();
    await expect.poll(() => requests.filter(({ method }) => method === 'PUT')).toEqual([
      {
        method: 'PUT',
        path: `${WEB_CONTEXTS_API}/${INTRANET.id}`,
        params: new URLSearchParams(),
        body: { name: INTRANET.name, nameEs: 'Intranet corporativa' },
      },
    ]);
    await expectToast(page, "El registre s'ha actualitzat correctament.");
    await expect(dialog).toHaveCount(0);
    await expect(prompt).toHaveCount(0);
  });

  test('required and max-length messages block create and edit submissions', async ({
    page,
  }) => {
    const { requests } = await mockWebContexts(page);
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

    await expect(name).toHaveAttribute('maxlength', '150');
    await expect(nameEs).toHaveAttribute('maxlength', '100');
    await name.fill('Nou context');
    await expect(page.locator('#security-web-context-dialog-name-error')).toHaveCount(0);
    await expect(name).toHaveAttribute('aria-invalid', 'false');
    await dialog.getByRole('button', { name: 'Cancel·la els canvis del context web' }).click();
    await expect(dialog).toHaveCount(0);

    // Values longer than the limit can only come from the backend.
    await selectRowAction(page, findRow(panel, 'Context xxx'), 'Edita');
    await expect(name).toHaveValue(LONG_NAME.name);
    await dialog.getByRole('button', { name: ACTIONS.save, exact: true }).click();
    await expect(page.locator('#security-web-context-dialog-name-error')).toHaveText(
      'El nom ha de tenir com a màxim 150 caràcters',
    );
    await expect(dialog.getByRole('heading', { name: 'Editar context web' })).toBeVisible();
    expect(requests.filter(({ method }) => method === 'POST' || method === 'PUT')).toEqual([]);
  });

  test('Neteja resets the filters to the active default and searches again', async ({ page }) => {
    const { requests } = await mockWebContexts(page);
    const panel = await openWebContexts(page);
    const listRequests = () =>
      requests.filter(({ method, path }) => method === 'GET' && path === WEB_CONTEXTS_API);

    await showFilters(panel, FILTERS_BUTTON);
    await page.locator('#security-web-context-filter-name').fill('  Extranet ');
    await selectOption(page, 'security-web-context-filter-status', 'Inactiu');
    await panel.getByRole('button', { name: SEARCH_BUTTON }).click();
    await expect.poll(() => listRequests().at(-1)?.params.toString()).toBe(
      'page=0&size=10&name=Extranet&statusId=2',
    );
    await expect(findRow(panel, RETIRED.name)).toBeVisible();
    await expect(
      panel.getByRole('button', { name: `${FILTERS_BUTTON} (2 filtres aplicats)`, exact: true }),
    ).toBeVisible();

    const before = listRequests().length;
    await panel.getByRole('button', { name: RESET_BUTTON }).click();
    await expect.poll(() => listRequests().length).toBe(before + 1);
    expect(listRequests().at(-1)?.params.toString()).toBe('page=0&size=10&statusId=1');
    await expect(page.locator('#security-web-context-filter-name')).toHaveValue('');
    await expect(page.locator('#security-web-context-filter-status')).toHaveText('Actiu');
    await expect(
      panel.getByRole('button', { name: `${FILTERS_BUTTON} (1 filtre aplicat)`, exact: true }),
    ).toBeVisible();
    await expect(findRow(panel, INTRANET.name)).toBeVisible();
    await expect(findRow(panel, RETIRED.name)).toHaveCount(0);
  });

  test('the deactivation prompt can be cancelled and inactive rows only offer Restaura', async ({
    page,
  }) => {
    const { requests } = await mockWebContexts(page);
    const panel = await openWebContexts(page);
    const confirmation = page.getByRole('dialog');

    await selectRowAction(page, findRow(panel, INTRANET.name), 'Dona de baixa');
    await expect(
      confirmation.getByRole('heading', { name: 'Donar de baixa el registre?' }),
    ).toBeVisible();
    await confirmation
      .getByRole('button', { name: 'Cancel·la la baixa del registre', exact: true })
      .click();
    await expect(confirmation).toHaveCount(0);

    await selectRowAction(page, findRow(panel, INTRANET.name), 'Dona de baixa');
    await expect(
      confirmation.getByRole('button', { name: 'Cancel·la la baixa del registre', exact: true }),
    ).toBeFocused();
    await page.keyboard.press('Escape');
    await expect(confirmation).toHaveCount(0);
    expect(requests.filter(({ method }) => method === 'DELETE')).toEqual([]);

    await showFilters(panel, FILTERS_BUTTON);
    await selectOption(page, 'security-web-context-filter-status', 'Inactiu');
    await panel.getByRole('button', { name: SEARCH_BUTTON }).click();
    const row = findRow(panel, RETIRED.name);
    await expect(
      row.getByRole('button', { name: 'Obrir les accions del registre', exact: true }),
    ).toHaveCount(0);
    await row.getByRole('button', { name: INACTIVE_ROW_ACTIONS_BUTTON }).click();
    await expect(page.locator('.p-menu-overlay:visible').getByRole('menuitem')).toHaveText([
      'Restaura',
    ]);
    await page.getByRole('menuitem', { name: 'Restaura' }).click();
    await expect.poll(() => requests.filter(({ method }) => method === 'PUT')).toEqual([
      {
        method: 'PUT',
        path: `${WEB_CONTEXTS_API}/reactivate/${RETIRED.id}`,
        params: new URLSearchParams(),
        body: null,
      },
    ]);
    await expectToast(page, "El registre s'ha restaurat correctament.");
  });

  test('the database vendor port is required and kept within 1-65535', async ({ page }) => {
    const posts: unknown[] = [];
    await page.route('**/invaiback/**', async (route: Route) => {
      const request = route.request();
      const path = new URL(request.url()).pathname;
      if (path.endsWith('/auth/me')) return fulfillMe(route);
      if (path === '/invaiback/database-vendor' && request.method() === 'POST') {
        posts.push(request.postDataJSON());
        return route.fulfill({
          status: 201,
          json: { id: 4, ...request.postDataJSON(), deletedAt: null },
        });
      }
      return route.fulfill({ json: pageOf([]) });
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
    await page.locator('#database-vendor-name').fill('PostgreSQL E2E');
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
    await add.click();
    await expect.poll(() => posts).toEqual([{ name: 'PostgreSQL E2E', defaultPort: 5432 }]);
    await expectToast(page, "El proveïdor s'ha afegit correctament.");
  });
});

async function mockWebContexts(page: Page): Promise<{ requests: CapturedRequest[] }> {
  const records = [INTRANET, LONG_NAME, RETIRED].map((record) => ({ ...record }));
  const requests: CapturedRequest[] = [];
  await page.route('**/invaiback/**', async (route: Route) => {
    const request = route.request();
    const url = new URL(request.url());
    const path = url.pathname;
    const method = request.method();
    if (path.endsWith('/auth/me')) return fulfillMe(route);
    if (!path.startsWith(WEB_CONTEXTS_API)) return route.fulfill({ json: pageOf([]) });

    requests.push({ method, path, params: url.searchParams, body: request.postDataJSON() });
    const id = Number(path.split('/').pop());
    const record = records.find((item) => item.id === id);
    if (method === 'GET' && path === WEB_CONTEXTS_API) {
      const status = url.searchParams.get('statusId');
      const name = url.searchParams.get('name')?.toLowerCase();
      const content = records.filter(
        (item) =>
          (status === null || Boolean(item.deletedAt) === (status === '2')) &&
          (!name || item.name.toLowerCase().includes(name)),
      );
      return route.fulfill({ json: pageOf(content) });
    }
    if (method === 'GET' && record) return route.fulfill({ json: record });
    if (method === 'PUT' && record) {
      if (path.includes('/reactivate/')) record.deletedAt = null;
      else Object.assign(record, request.postDataJSON());
      return route.fulfill({ json: record });
    }
    return route.fulfill({ status: 404, json: {} });
  });
  return { requests };
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

function fulfillMe(route: Route) {
  return route.fulfill({ json: { authenticated: true, username: 'crud-dialog-test' } });
}

function pageOf(content: unknown[]) {
  return { content, totalElements: content.length, totalPages: 1, last: true, number: 0 };
}
