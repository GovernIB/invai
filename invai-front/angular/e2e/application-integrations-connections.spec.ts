import type { Locator, Page } from '@playwright/test';

import {
  type ConnectionRecord,
  CONNECTIONS_API,
  createConnection,
  findSoffidSystem,
  findTechnology,
  GRANTED_ROLE,
  INTEGRATION_API,
  MISSING_ROLE,
  OTHER_GRANTED_ROLE,
  pickRoles,
  pickSoffidPerson,
  registerConnectionCleanup,
  SOFFID_PERSON,
  type SoffidRole,
  typeSoffidQuery,
} from './support/application-integrations';
import { type DisposableApplication, expect, test } from './support/application.fixture';
import { findApiCatalogItem } from './support/catalog';
import type { CleanupRegistry } from './support/cleanup-registry';
import {
  apiUrl,
  expectApiJsonResponse,
  expectJsonResponse,
  failApiRequest,
  waitForApiResponse,
} from './support/http';
import { ACTIVE_STATUS_ID } from './support/systems';
import { selectOption } from './support/ui';

const EDIT_SECTION = 'Editar Integracions';
const SAVE_SECTION = 'Desar els canvis de Integracions';
const ADD_CONNECTION = 'Afegeix una connexió';
const ROW_ACTIONS = 'Obre les accions del registre';
const RETRY = 'Torna a carregar les integracions';
const UNAVAILABLE_ROLES = 'No disponible';
const UNAVAILABLE_DETAIL =
  "No s'han pogut consultar els rols a Soffid. Torna a carregar la pestanya per poder editar la connexió.";
const MISMATCH_DETAIL = 'Els rols atorgats a Soffid no coincideixen amb els rols requerits.';
const CONNECTION_SAVED = "La connexió s'ha desat correctament.";
const FORBIDDEN = "El teu perfil no té permisos per fer aquesta operació d'integracions.";
const DELETED = "El registre d'integració està donat de baixa i no es pot modificar.";
const REQUIRED = 'Camp obligatori';

interface IntegrationsTab {
  connections: ConnectionRecord[];
  listRequests: URL[];
  applicationSearches: URL[];
  roleSearches: URL[];
  catalogRequests: string[];
  connectionPosts: unknown[];
  connectionPuts: { path: string; body: unknown }[];
}

interface ApplicationRecord {
  id: number;
  code: string | null;
  name: string | null;
}

// Adds one connection of the Soffid person per list of required roles, then opens the tab.
async function openIntegrations(
  page: Page,
  cleanup: CleanupRegistry,
  application: DisposableApplication,
  connectionRoles: SoffidRole[][] = [],
): Promise<IntegrationsTab> {
  const connections: ConnectionRecord[] = [];
  for (const roles of connectionRoles) {
    connections.push(await createConnection(page, cleanup, application, roles));
  }
  const tab: IntegrationsTab = {
    connections,
    listRequests: [],
    applicationSearches: [],
    roleSearches: [],
    catalogRequests: [],
    connectionPosts: [],
    connectionPuts: [],
  };
  page.on('request', (request) => {
    const url = new URL(request.url());
    const resource = url.pathname.replace(/^\/invaiback\//, '');
    const method = request.method();
    if (resource === 'application' && method === 'GET') tab.applicationSearches.push(url);
    else if (resource === 'security-role/soffid-search') tab.roleSearches.push(url);
    else if ((resource === 'technology' || resource === 'external-system') && method === 'GET') {
      tab.catalogRequests.push(resource);
    } else if (url.pathname === CONNECTIONS_API && method === 'GET') tab.listRequests.push(url);
    else if (url.pathname === CONNECTIONS_API && method === 'POST') {
      tab.connectionPosts.push(request.postDataJSON());
    } else if (url.pathname.startsWith(`${CONNECTIONS_API}/`) && method === 'PUT') {
      tab.connectionPuts.push({ path: resource, body: request.postDataJSON() });
    }
  });
  await page.goto(`/aplicacions/${application.id}/integrations`);
  await expect(page.getByRole('heading', { name: 'Integracions', exact: true })).toBeVisible();
  return tab;
}

const section = (page: Page) => page.locator('app-application-integrations-section');
const rows = (page: Page) => page.locator('app-application-integrations-table tbody tr');
const toast = (page: Page) => page.locator('p-toast');
const dialogField = (page: Page, suffix: string) =>
  page.locator(`#application-integration-dialog-${suffix}`);
// The shared CRUD shell does not name its dialog yet, so it is located by its visible heading.
const dialogTitled = (page: Page, title: string) =>
  page.getByRole('dialog').filter({ has: page.getByRole('heading', { name: title }) });
// A connection requiring the missing role has a mismatch; one requiring exactly the granted roles
// has not.
const mismatchRow = (page: Page) => rows(page).filter({ hasText: MISSING_ROLE.name });
const matchingRow = (page: Page) => rows(page).filter({ hasNotText: MISSING_ROLE.name });

async function startEditing(page: Page): Promise<void> {
  await page.getByRole('button', { name: EDIT_SECTION, exact: true }).click();
  await expect(page.getByRole('button', { name: SAVE_SECTION })).toBeVisible();
}

const rowMenu = (page: Page) =>
  page.getByRole('menu').filter({ has: page.getByRole('menuitem', { name: 'Consulta' }) });

async function openRowMenu(page: Page, row: Locator): Promise<void> {
  await row.getByRole('button', { name: ROW_ACTIONS }).click();
  await expect(rowMenu(page)).toBeVisible();
}

async function closeRowMenu(page: Page): Promise<void> {
  await page.keyboard.press('Escape');
  await expect(rowMenu(page)).toBeHidden();
}

// Lets Angular render whatever the previous input could have opened before asserting it did not.
async function settle(page: Page): Promise<void> {
  await page.evaluate(
    () =>
      new Promise<void>((resolve) =>
        requestAnimationFrame(() => requestAnimationFrame(() => resolve())),
      ),
  );
}

async function submitNewConnection(
  page: Page,
  cleanup: CleanupRegistry,
  submit: Locator,
): Promise<void> {
  const createPromise = waitForApiResponse(page, 'POST', CONNECTIONS_API);
  await submit.click();
  const created = await expectJsonResponse<{ id: number }>(await createPromise);
  registerConnectionCleanup(page, cleanup, created.id);
}

// A Soffid outage is the only way the back-end answers rolesUnavailable: the real listing is
// answered with the roles of one connection marked as unavailable.
async function simulateSoffidOutageFor(page: Page, connection: ConnectionRecord): Promise<void> {
  await page.route(
    (url) => url.pathname === CONNECTIONS_API,
    async (route) => {
      if (route.request().method() !== 'GET') return route.fallback();
      const response = await route.fetch();
      const body = (await response.json()) as { content: Array<{ id: number }> };
      body.content = body.content.map((row) =>
        row.id === connection.id
          ? {
              ...row,
              requiredRoles: null,
              grantedRoles: null,
              rolesMismatch: null,
              rolesUnavailable: true,
            }
          : row,
      );
      return route.fulfill({ response, json: body });
    },
  );
}

test('keeps a connection whose Soffid roles are unavailable consultable but not editable', async ({
  page,
  cleanupRegistry,
  disposableApplication,
}, testInfo) => {
  const unavailableConnection = await createConnection(
    page,
    cleanupRegistry,
    disposableApplication,
    [GRANTED_ROLE, MISSING_ROLE],
  );
  await simulateSoffidOutageFor(page, unavailableConnection);
  const tab = await openIntegrations(page, cleanupRegistry, disposableApplication, [
    [GRANTED_ROLE],
  ]);

  await expect(rows(page)).toHaveCount(2);
  const unavailable = rows(page).filter({ hasText: UNAVAILABLE_ROLES });
  // Required and granted roles are the fourth and fifth columns.
  await expect(unavailable.getByRole('cell').nth(3)).toHaveText(UNAVAILABLE_ROLES);
  await expect(unavailable.getByRole('cell').nth(4)).toHaveText(UNAVAILABLE_ROLES);
  await expect(unavailable.getByRole('img')).toHaveCount(0);
  await expect(section(page).getByRole('alert')).toHaveCount(0);

  await startEditing(page);
  await openRowMenu(page, unavailable);
  await expect(rowMenu(page).getByRole('menuitem', { name: 'Consulta' })).toBeEnabled();
  await expect(rowMenu(page).getByRole('menuitem', { name: 'Edita' })).toBeDisabled();
  await expect(rowMenu(page).getByRole('menuitem', { name: 'Dona de baixa' })).toBeEnabled();
  await page.screenshot({ path: testInfo.outputPath('integrations-roles-unavailable-menu.png') });
  await closeRowMenu(page);

  await openRowMenu(page, rows(page).filter({ hasText: GRANTED_ROLE.name }));
  await expect(rowMenu(page).getByRole('menuitem', { name: 'Edita' })).toBeEnabled();
  await closeRowMenu(page);

  await openRowMenu(page, unavailable);
  await rowMenu(page).getByRole('menuitem', { name: 'Consulta' }).click();
  const dialog = dialogTitled(page, 'Consultar connexió');
  await expect(dialog).toBeVisible();
  await expect(dialog.getByLabel("Usuari d'integració")).toHaveValue(SOFFID_PERSON.userName);
  const requiredRoles = dialog.getByLabel('Rols requerits', { exact: true });
  await expect(requiredRoles).toHaveText(UNAVAILABLE_ROLES);
  await expect(requiredRoles).toHaveAccessibleDescription(UNAVAILABLE_DETAIL);
  await expect(dialog.getByText(UNAVAILABLE_DETAIL)).toBeVisible();
  await expect(dialog.getByLabel('Rols atorgats', { exact: true })).toHaveText(UNAVAILABLE_ROLES);
  await expect(dialog.getByLabel('Coincidència de rols')).toHaveValue("No s'ha pogut comprovar");
  await expect(dialog.getByRole('button', { name: 'Edita la connexió' })).toHaveCount(0);
  await dialog.screenshot({ path: testInfo.outputPath('integrations-roles-unavailable-view.png') });

  await dialog.getByRole('button', { name: 'Accepta la consulta de la connexió' }).click();
  await expect(dialog).toBeHidden();
  expect(tab.roleSearches).toHaveLength(0);
  expect(tab.connectionPuts).toHaveLength(0);
});

test('flags a role mismatch next to the system name without opening the row', async ({
  page,
  cleanupRegistry,
  disposableApplication,
}, testInfo) => {
  await openIntegrations(page, cleanupRegistry, disposableApplication, [
    [GRANTED_ROLE, MISSING_ROLE],
    [GRANTED_ROLE, OTHER_GRANTED_ROLE],
  ]);

  await expect(rows(page)).toHaveCount(2);
  const warning = mismatchRow(page)
    .getByRole('cell')
    .first()
    .getByRole('img', { name: MISMATCH_DETAIL });
  await expect(warning).toBeVisible();
  await expect(matchingRow(page).getByRole('img')).toHaveCount(0);

  await mismatchRow(page).focus();
  await page.keyboard.press('Tab');
  await expect(warning).toBeFocused();
  await expect(page.getByRole('tooltip')).toHaveText(MISMATCH_DETAIL);
  await page.screenshot({ path: testInfo.outputPath('integrations-roles-mismatch.png') });

  await page.keyboard.press('Enter');
  await warning.dblclick();
  await settle(page);
  await expect(page.getByRole('dialog')).toHaveCount(0);

  await mismatchRow(page).focus();
  await page.keyboard.press('Enter');
  const dialog = dialogTitled(page, 'Consultar connexió');
  await expect(dialog).toBeVisible();
  await expect(dialog.getByLabel('Coincidència de rols')).toHaveValue('No coincideixen');
});

test('edits a connection from the pickers loaded with the edit form', async ({
  page,
  cleanupRegistry,
  disposableApplication,
}) => {
  test.setTimeout(90_000);
  const tab = await openIntegrations(page, cleanupRegistry, disposableApplication, [
    [GRANTED_ROLE, MISSING_ROLE],
  ]);
  const [connection] = tab.connections;
  const [soffid, jboss] = await Promise.all([
    findSoffidSystem(page),
    findTechnology(page, 'JBoss'),
  ]);
  await expect(rows(page)).toHaveCount(1);
  await startEditing(page);

  await rows(page).first().dblclick();
  const view = dialogTitled(page, 'Consultar connexió');
  await expect(view).toBeVisible();
  tab.roleSearches.length = 0;
  tab.applicationSearches.length = 0;
  tab.catalogRequests.length = 0;
  await view.getByRole('button', { name: 'Edita la connexió' }).click();

  const dialog = dialogTitled(page, 'Editar connexió');
  await expect(dialog).toBeVisible();
  // The unfiltered picker lists are requested with the edit form, before any panel opens.
  await expect.poll(() => tab.roleSearches.length).toBe(1);
  await expect.poll(() => tab.applicationSearches.length).toBe(1);
  await expect
    .poll(() => [...tab.catalogRequests].sort())
    .toEqual(['external-system', 'technology']);
  expect(Object.fromEntries(tab.roleSearches[0].searchParams)).toEqual({ page: '0', size: '20' });
  expect(Object.fromEntries(tab.applicationSearches[0].searchParams)).toEqual({
    page: '0',
    size: '20',
    sort: 'name,asc',
    statusId: '1',
  });

  await expect(dialog.getByRole('radio', { name: 'Sistema extern' })).toBeChecked();
  await expect(dialogField(page, 'external-system')).toHaveText('Soffid');
  await expect(dialogField(page, 'technology')).toHaveText('Spring Boot');
  await expect(dialogField(page, 'soffid-person')).toHaveValue(SOFFID_PERSON.userName);
  await expect(dialog.locator('.p-multiselect-chip')).toHaveText([
    MISSING_ROLE.label,
    GRANTED_ROLE.label,
  ]);

  await selectOption(page, 'application-integration-dialog-technology', 'JBoss');
  await pickRoles(page, MISSING_ROLE, OTHER_GRANTED_ROLE);
  await expect(dialog.locator('.p-multiselect-chip')).toHaveText([
    GRANTED_ROLE.label,
    OTHER_GRANTED_ROLE.label,
  ]);
  await dialog.getByRole('button', { name: 'Desa els canvis de la connexió' }).click();

  await expect(dialog).toBeHidden();
  expect(tab.connectionPuts).toEqual([
    {
      path: `application/integration-connection/${connection.id}`,
      body: {
        appIntegrationId: disposableApplication.appIntegrationId,
        applicationId: null,
        externalSystemId: soffid.id,
        technologyId: jboss.id,
        username: SOFFID_PERSON.userName,
        requiredRoleIds: [GRANTED_ROLE.roleId, OTHER_GRANTED_ROLE.roleId],
      },
    },
  ]);
  await expect(toast(page).getByText(CONNECTION_SAVED)).toBeVisible();
  await expect.poll(() => tab.listRequests.length).toBe(2);
  await expect(rows(page).first()).toContainText('JBoss');
  // The back-end lists the required roles by their Soffid identifier.
  await expect(rows(page).first()).toContainText(
    `${GRANTED_ROLE.name}, ${OTHER_GRANTED_ROLE.name}`,
  );
  // Opening the roles panel reused the unfiltered list requested with the form.
  expect(tab.roleSearches.filter((url) => !url.searchParams.has('name'))).toHaveLength(1);
});

test('adds a connection to another active application of the inventory', async ({
  page,
  cleanupRegistry,
  disposableApplication,
}, testInfo) => {
  test.setTimeout(90_000);
  const tab = await openIntegrations(page, cleanupRegistry, disposableApplication);
  // INVAI itself is part of the reference data of every environment.
  const invai = await findApiCatalogItem<ApplicationRecord>({
    page,
    pathname: '/invaiback/application',
    params: { statusId: ACTIVE_STATUS_ID, quickSearch: 'INVAI' },
    matches: (application) => application.code === 'INVAI',
  });
  if (!invai) throw new Error('The INVAI application is required');
  const invaiOption = `INVAI — ${invai.name}`;
  await startEditing(page);

  await page.getByRole('button', { name: ADD_CONNECTION }).click();
  const dialog = dialogTitled(page, 'Afegir connexió');
  await expect(dialog).toBeVisible();
  await expect(dialog.getByRole('radio', { name: "Aplicació de l'inventari" })).toBeChecked();
  await expect.poll(() => tab.applicationSearches.length).toBe(1);

  await dialogField(page, 'application').click();
  const search = page.getByRole('searchbox', { name: 'Aplicació' });
  // The application being edited is never offered as the other end of its own connection.
  const ownSearch = waitForApiResponse(
    page,
    'GET',
    '/invaiback/application',
    (url) => url.searchParams.get('quickSearch') === disposableApplication.code,
  );
  await search.fill(disposableApplication.code);
  await ownSearch;
  await expect(
    page.getByRole('option', {
      name: `${disposableApplication.code} — ${disposableApplication.name}`,
    }),
  ).toHaveCount(0);
  const invaiSearch = waitForApiResponse(
    page,
    'GET',
    '/invaiback/application',
    (url) => url.searchParams.get('quickSearch') === 'INVAI',
  );
  await search.fill('INVAI');
  await invaiSearch;
  await page.getByRole('option', { name: invaiOption }).click();

  await selectOption(page, 'application-integration-dialog-technology', 'Spring Boot');
  await pickSoffidPerson(page);
  await pickRoles(page, GRANTED_ROLE);
  await page.screenshot({ path: testInfo.outputPath('integrations-application-connection.png') });
  await submitNewConnection(
    page,
    cleanupRegistry,
    dialog.getByRole('button', { name: 'Afegeix la connexió' }),
  );

  await expect(dialog).toBeHidden();
  const springBoot = await findTechnology(page, 'Spring Boot');
  expect(tab.connectionPosts).toEqual([
    {
      appIntegrationId: disposableApplication.appIntegrationId,
      applicationId: invai.id,
      externalSystemId: null,
      technologyId: springBoot.id,
      username: SOFFID_PERSON.userName,
      requiredRoleIds: [GRANTED_ROLE.roleId],
    },
  ]);
  await expect(toast(page).getByText(CONNECTION_SAVED)).toBeVisible();
  await expect(rows(page)).toHaveCount(1);
  await expect(rows(page).first()).toContainText(invai.name!);
  await expect(rows(page).first()).toContainText("Aplicació de l'inventari");
});

// Soffid people without a user code are left out of the suggestions: covered by the unit tests of
// ApplicationIntegrationConnectionsState, since which people lack one depends on Soffid.
test('rejects a connection without its required fields, roles or a Soffid user code', async ({
  page,
  cleanupRegistry,
  disposableApplication,
}, testInfo) => {
  test.setTimeout(90_000);
  const tab = await openIntegrations(page, cleanupRegistry, disposableApplication);
  await startEditing(page);
  await page.getByRole('button', { name: ADD_CONNECTION }).click();
  const dialog = dialogTitled(page, 'Afegir connexió');
  const submit = dialog.getByRole('button', { name: 'Afegeix la connexió' });
  const userError = dialogField(page, 'soffid-person-error');
  const rolesError = dialogField(page, 'roles-error');

  await submit.click();
  await expect(dialogField(page, 'application-error')).toHaveText(REQUIRED);
  await expect(dialogField(page, 'technology-error')).toHaveText(REQUIRED);
  await expect(userError).toHaveText('Selecciona un usuari de Soffid.');
  await expect(rolesError).toHaveText('Selecciona almenys un rol requerit.');
  await expect(dialogField(page, 'roles')).toHaveAttribute('aria-invalid', 'true');
  await page.screenshot({ path: testInfo.outputPath('integrations-dialog-required.png') });

  await dialog.getByRole('radio', { name: 'Sistema extern' }).check();
  await expect(dialogField(page, 'application-error')).toHaveCount(0);
  await expect(dialogField(page, 'external-system-error')).toHaveText(REQUIRED);
  await selectOption(page, 'application-integration-dialog-external-system', 'Soffid');
  await selectOption(page, 'application-integration-dialog-technology', 'Spring Boot');
  await expect(dialogField(page, 'external-system-error')).toHaveCount(0);
  await expect(dialogField(page, 'technology-error')).toHaveCount(0);

  // Typed text is not a selection.
  const user = dialogField(page, 'soffid-person');
  await typeSoffidQuery(page);
  await dialog.getByRole('heading', { name: 'Afegir connexió' }).click();
  await expect(userError).toHaveText(
    "Selecciona un usuari de la llista de Soffid que tingui codi d'usuari.",
  );
  await expect(user).toHaveAttribute('aria-invalid', 'true');
  await submit.click();
  await expect(dialog).toBeVisible();

  await user.clear();
  await pickSoffidPerson(page);
  await expect(userError).toHaveText('');
  await submit.click();
  await expect(rolesError).toHaveText('Selecciona almenys un rol requerit.');
  expect(tab.connectionPosts).toHaveLength(0);

  await pickRoles(page, GRANTED_ROLE);
  await expect(rolesError).toHaveCount(0);
  await submitNewConnection(page, cleanupRegistry, submit);
  await expect(dialog).toBeHidden();
  expect(tab.connectionPosts).toEqual([
    {
      appIntegrationId: disposableApplication.appIntegrationId,
      applicationId: null,
      externalSystemId: expect.any(Number),
      technologyId: expect.any(Number),
      username: SOFFID_PERSON.userName,
      requiredRoleIds: [GRANTED_ROLE.roleId],
    },
  ]);
});

// The integration record is created with its application; one without it only exists among those
// created before the record did. Its creation from the observations is covered by the unit tests
// of ApplicationDetailState and ApplicationIntegrationsSection.

test('reports a forbidden integration record and stays read-only', async ({
  page,
  cleanupRegistry,
  disposableApplication,
}) => {
  await failApiRequest(
    page,
    'GET',
    `${INTEGRATION_API}/${disposableApplication.appIntegrationId}`,
    { status: 403, json: { error: 'Forbidden', message: 'Accés denegat.' } },
    { times: Number.POSITIVE_INFINITY },
  );
  await openIntegrations(page, cleanupRegistry, disposableApplication);

  await expect(section(page).getByRole('alert')).toHaveText(FORBIDDEN);
  await expect(page.getByRole('button', { name: RETRY })).toBeVisible();
  await expect(page.getByRole('heading', { name: 'Connexions' })).toHaveCount(0);

  await page.getByRole('button', { name: EDIT_SECTION, exact: true }).click();
  await expect(toast(page).getByText(FORBIDDEN)).toBeVisible();
  await expect(page.getByRole('button', { name: SAVE_SECTION })).toHaveCount(0);
});

test('shows a deleted integration record without letting it be edited', async ({
  page,
  cleanupRegistry,
  disposableApplication,
}) => {
  await createConnection(page, cleanupRegistry, disposableApplication, [GRANTED_ROLE]);
  const anchorPath = `${INTEGRATION_API}/${disposableApplication.appIntegrationId}`;
  await expectApiJsonResponse(
    await page.request.put(apiUrl(page, anchorPath), {
      data: { applicationId: disposableApplication.id, observation: '<p>Nota antiga</p>' },
    }),
    `PUT ${anchorPath}`,
  );
  const deleted = await page.request.delete(apiUrl(page, anchorPath));
  expect(deleted.ok(), await deleted.text()).toBeTruthy();
  await openIntegrations(page, cleanupRegistry, disposableApplication);

  await expect(section(page).getByRole('alert')).toHaveText(DELETED);
  await expect(page.getByRole('button', { name: RETRY })).toHaveCount(0);
  await expect(rows(page)).toHaveCount(1);
  await expect(page.locator('.invai-form-static-value--rich')).toContainText('Nota antiga');

  await page.getByRole('button', { name: EDIT_SECTION, exact: true }).click();
  await expect(toast(page).getByText(DELETED)).toBeVisible();
  await expect(page.getByRole('button', { name: SAVE_SECTION })).toHaveCount(0);
  await expect(page.getByRole('button', { name: ADD_CONNECTION })).toHaveCount(0);
});
