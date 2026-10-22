import { expect, test, type Locator, type Page, type Route } from '@playwright/test';

import { selectOption } from './support/ui';

test.use({ storageState: { cookies: [], origins: [] } });

const pageOf = (content: unknown[]) => ({
  content,
  totalElements: content.length,
  totalPages: 1,
  last: true,
  number: 0,
});

const SOFFID = { id: 2, name: 'Soffid', company: null, deletedAt: null };
const SPRING_BOOT = {
  id: 4,
  name: 'Spring Boot',
  layer: { id: 1, name: 'Backend', deletedAt: null },
  deletedAt: null,
};
const ANGULAR = {
  id: 5,
  name: 'Angular',
  layer: { id: 2, name: 'Frontend', deletedAt: null },
  deletedAt: null,
};
const TECHNOLOGIES = [ANGULAR, SPRING_BOOT];
const INV_ADMIN = { id: 26, name: 'INV_ADMIN', description: null, system: 'SEYCON' };
const INV_READ = { id: 33, name: 'INV_READ', description: null, system: 'SEYCON' };
const INV_WRITE = { id: 40, name: 'INV_WRITE', description: null, system: 'SEYCON' };
const SOFFID_ROLES = [INV_ADMIN, INV_READ, INV_WRITE];

const MISMATCH = {
  id: 1,
  appIntegrationId: 13,
  application: null,
  externalSystem: SOFFID,
  technology: SPRING_BOOT,
  username: 'u00004',
  requiredRoles: [INV_ADMIN, INV_READ],
  grantedRoles: [INV_ADMIN],
  rolesMismatch: true,
  rolesUnavailable: false,
  deletedAt: null,
};
const MATCHING = {
  ...MISMATCH,
  id: 3,
  username: 'u00005',
  requiredRoles: [INV_ADMIN],
  rolesMismatch: false,
};
// With Soffid unreachable the list still answers, flagging rows whose roles it could not resolve.
const UNAVAILABLE = {
  ...MISMATCH,
  id: 4,
  username: 'u00006',
  requiredRoles: null,
  grantedRoles: null,
  rolesMismatch: null,
  rolesUnavailable: true,
};

const INVENTORY = [
  { id: 1, code: 'TEST', name: 'Integracions test', status: 'ACTIVE' },
  { id: 7, code: 'APP2', name: 'Aplicació dos', status: 'ACTIVE' },
];
const SOFFID_PEOPLE = [
  {
    id: null,
    company: null,
    firstName: 'Maria',
    lastName: 'Tur',
    email: 'maria@caib.es',
    personalCaib: true,
    deletedAt: null,
    userName: 'u00009',
  },
  {
    id: null,
    company: null,
    firstName: 'Marta',
    lastName: 'Sense Codi',
    email: 'marta@caib.es',
    personalCaib: true,
    deletedAt: null,
    userName: null,
  },
];

const EDIT_SECTION = 'Editar Integracions';
const SAVE_SECTION = 'Desar els canvis de Integracions';
const ADD_CONNECTION = 'Afegeix una connexió';
const ROW_ACTIONS = 'Obre les accions del registre';
const RETRY = 'Torna a carregar les integracions';
const UNAVAILABLE_ROLES = 'No disponible';
const UNAVAILABLE_DETAIL =
  "No s'han pogut consultar els rols a Soffid. Torna a carregar la pestanya per poder editar la connexió.";
const MISMATCH_DETAIL = 'Els rols atorgats a Soffid no coincideixen amb els rols requerits.';
const ANCHOR_REQUIRED =
  "Desa les observacions de la pestanya per crear el registre d'integració abans d'afegir connexions.";
const CONNECTION_SAVED = "La connexió s'ha desat correctament.";
const OBSERVATIONS_SAVED = "S'han desat correctament els canvis de Integracions.";
const FORBIDDEN = "El teu perfil no té permisos per fer aquesta operació d'integracions.";
const DELETED = "El registre d'integració està donat de baixa i no es pot modificar.";
const REQUIRED = 'Camp obligatori';

interface MockOptions {
  connections?: Record<string, unknown>[];
  appIntegrationId?: number | null;
  anchor?: Record<string, unknown>;
  anchorStatus?: number;
}

interface IntegrationsMock {
  connections: Record<string, unknown>[];
  listRequests: URL[];
  applicationSearches: URL[];
  roleSearches: URL[];
  catalogRequests: string[];
  anchorPosts: unknown[];
  connectionPosts: unknown[];
  connectionPuts: { path: string; body: unknown }[];
}

interface ConnectionInput {
  appIntegrationId: number;
  applicationId: number | null;
  externalSystemId: number | null;
  technologyId: number;
  username: string;
  requiredRoleIds: number[];
}

function toConnection(id: number, body: ConnectionInput): Record<string, unknown> {
  const application = INVENTORY.find((candidate) => candidate.id === body.applicationId);
  return {
    ...MATCHING,
    id,
    appIntegrationId: body.appIntegrationId,
    application: application
      ? { id: application.id, code: application.code, name: application.name }
      : null,
    externalSystem: body.externalSystemId === SOFFID.id ? SOFFID : null,
    technology: TECHNOLOGIES.find((technology) => technology.id === body.technologyId) ?? null,
    username: body.username,
    requiredRoles: SOFFID_ROLES.filter((role) => body.requiredRoleIds.includes(role.id)),
  };
}

// Write responses carry no roles resolved against Soffid.
const writeResponse = (connection: Record<string, unknown>) => ({
  ...connection,
  requiredRoles: null,
  grantedRoles: null,
  rolesMismatch: null,
  rolesUnavailable: null,
});

async function openIntegrations(page: Page, options: MockOptions = {}): Promise<IntegrationsMock> {
  const mock: IntegrationsMock = {
    connections: options.connections ?? [MISMATCH],
    listRequests: [],
    applicationSearches: [],
    roleSearches: [],
    catalogRequests: [],
    anchorPosts: [],
    connectionPosts: [],
    connectionPuts: [],
  };
  let appIntegrationId = options.appIntegrationId === undefined ? 13 : options.appIntegrationId;
  let anchor = {
    id: 13,
    applicationId: 1,
    observation: null as string | null,
    deletedAt: null as string | null,
    ...options.anchor,
  };
  await page.route('**/invaiback/**', async (route: Route) => {
    const request = route.request();
    const url = new URL(request.url());
    const resource = url.pathname.replace(/^.*\/invaiback\//, '');
    const method = request.method();
    if (resource === 'auth/me') {
      return route.fulfill({ json: { authenticated: true, username: 'integrations-test' } });
    }
    if (resource === 'application/1') {
      return route.fulfill({
        json: {
          id: 1,
          code: 'TEST',
          name: 'Integracions test',
          status: 'ACTIVE',
          appIntegrationId,
        },
      });
    }
    if (resource === 'application' && method === 'GET') {
      mock.applicationSearches.push(url);
      const query = url.searchParams.get('quickSearch')?.toLowerCase() ?? '';
      return route.fulfill({
        json: pageOf(
          INVENTORY.filter((app) => `${app.code} ${app.name}`.toLowerCase().includes(query)),
        ),
      });
    }
    if (resource === 'application/integration' && method === 'POST') {
      const body = request.postDataJSON();
      mock.anchorPosts.push(body);
      anchor = {
        id: 21,
        applicationId: body.applicationId,
        observation: body.observation,
        deletedAt: null,
      };
      appIntegrationId = anchor.id;
      return route.fulfill({ status: 201, json: anchor });
    }
    if (resource === `application/integration/${anchor.id}`) {
      if (options.anchorStatus) {
        return route.fulfill({
          status: options.anchorStatus,
          json: { error: 'Forbidden', message: 'Accés denegat.' },
        });
      }
      return route.fulfill({ json: anchor });
    }
    if (resource === 'application/integration-connection' && method === 'GET') {
      mock.listRequests.push(url);
      return route.fulfill({ json: pageOf(mock.connections) });
    }
    if (resource === 'application/integration-connection' && method === 'POST') {
      const body = request.postDataJSON() as ConnectionInput;
      mock.connectionPosts.push(body);
      const created = toConnection(50, body);
      mock.connections = [...mock.connections, created];
      return route.fulfill({ status: 201, json: writeResponse(created) });
    }
    const connectionId = resource.match(/^application\/integration-connection\/(\d+)$/)?.[1];
    if (connectionId && method === 'PUT') {
      const body = request.postDataJSON() as ConnectionInput;
      mock.connectionPuts.push({ path: resource, body });
      const updated = toConnection(Number(connectionId), body);
      mock.connections = mock.connections.map((row) => (row.id === updated.id ? updated : row));
      return route.fulfill({ json: writeResponse(updated) });
    }
    if (resource === 'technology') {
      mock.catalogRequests.push(resource);
      return route.fulfill({ json: pageOf(TECHNOLOGIES) });
    }
    if (resource === 'external-system') {
      mock.catalogRequests.push(resource);
      return route.fulfill({ json: pageOf([SOFFID]) });
    }
    if (resource === 'person/soffid-search') {
      const query = url.searchParams.get('search')?.toLowerCase() ?? '';
      return route.fulfill({
        json: pageOf(
          SOFFID_PEOPLE.filter((person) =>
            `${person.firstName} ${person.lastName}`.toLowerCase().includes(query),
          ),
        ),
      });
    }
    if (resource === 'security-role/soffid-search') {
      mock.roleSearches.push(url);
      const query = url.searchParams.get('name')?.toLowerCase() ?? '';
      return route.fulfill({
        json: pageOf(
          SOFFID_ROLES.filter((role) => role.name.toLowerCase().includes(query)).map((role) => ({
            id: null,
            roleId: role.id,
            name: role.name,
            system: role.system,
            description: null,
          })),
        ),
      });
    }
    return route.fulfill({ json: pageOf([]) });
  });
  await page.goto('/aplicacions/1/integrations');
  await expect(page.getByRole('heading', { name: 'Integracions', exact: true })).toBeVisible();
  return mock;
}

const section = (page: Page) => page.locator('app-application-integrations-section');
const rows = (page: Page) => page.locator('app-application-integrations-table tbody tr');
const toast = (page: Page) => page.locator('p-toast');
const dialogField = (page: Page, suffix: string) =>
  page.locator(`#application-integration-dialog-${suffix}`);
// The shared CRUD shell does not name its dialog yet, so it is located by its visible heading.
const dialogTitled = (page: Page, title: string) =>
  page.getByRole('dialog').filter({ has: page.getByRole('heading', { name: title }) });

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

async function pickSoffidUser(page: Page, query: string, option: RegExp): Promise<void> {
  // Waits for the answer so a suggestion left from an earlier query is not clicked.
  const search = page.waitForResponse((response) =>
    response.url().includes('/person/soffid-search'),
  );
  await dialogField(page, 'soffid-person').pressSequentially(query);
  await search;
  await page.getByRole('option', { name: option }).click();
}

async function pickRoles(page: Page, ...labels: string[]): Promise<void> {
  await dialogField(page, 'roles').focus();
  await page.keyboard.press('Enter');
  for (const label of labels) {
    await page.getByRole('option', { name: label }).click();
  }
  await page.keyboard.press('Escape');
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

test('keeps a connection whose Soffid roles are unavailable consultable but not editable', async ({
  page,
}, testInfo) => {
  const mock = await openIntegrations(page, { connections: [UNAVAILABLE, MATCHING] });

  await expect(rows(page)).toHaveCount(2);
  const unavailable = rows(page).first();
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

  await openRowMenu(page, rows(page).nth(1));
  await expect(rowMenu(page).getByRole('menuitem', { name: 'Edita' })).toBeEnabled();
  await closeRowMenu(page);

  await openRowMenu(page, unavailable);
  await rowMenu(page).getByRole('menuitem', { name: 'Consulta' }).click();
  const dialog = dialogTitled(page, 'Consultar connexió');
  await expect(dialog).toBeVisible();
  await expect(dialog.getByLabel("Usuari d'integració")).toHaveValue('u00006');
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
  expect(mock.roleSearches).toHaveLength(0);
  expect(mock.connectionPuts).toHaveLength(0);
});

test('flags a role mismatch next to the system name without opening the row', async ({
  page,
}, testInfo) => {
  await openIntegrations(page, { connections: [MISMATCH, MATCHING] });

  await expect(rows(page)).toHaveCount(2);
  const mismatchRow = rows(page).first();
  const warning = mismatchRow.getByRole('cell').first().getByRole('img', { name: MISMATCH_DETAIL });
  await expect(warning).toBeVisible();
  await expect(rows(page).nth(1).getByRole('img')).toHaveCount(0);

  await mismatchRow.focus();
  await page.keyboard.press('Tab');
  await expect(warning).toBeFocused();
  await expect(page.getByRole('tooltip')).toHaveText(MISMATCH_DETAIL);
  await page.screenshot({ path: testInfo.outputPath('integrations-roles-mismatch.png') });

  await page.keyboard.press('Enter');
  await warning.dblclick();
  await settle(page);
  await expect(page.getByRole('dialog')).toHaveCount(0);

  await mismatchRow.focus();
  await page.keyboard.press('Enter');
  const dialog = dialogTitled(page, 'Consultar connexió');
  await expect(dialog).toBeVisible();
  await expect(dialog.getByLabel('Coincidència de rols')).toHaveValue('No coincideixen');
});

test('edits a connection from the pickers loaded with the edit form', async ({ page }) => {
  const mock = await openIntegrations(page, { connections: [MISMATCH] });
  await expect(rows(page)).toHaveCount(1);
  await startEditing(page);

  await rows(page).first().dblclick();
  const view = dialogTitled(page, 'Consultar connexió');
  await expect(view).toBeVisible();
  await view.getByRole('button', { name: 'Edita la connexió' }).click();

  const dialog = dialogTitled(page, 'Editar connexió');
  await expect(dialog).toBeVisible();
  // The unfiltered picker lists are requested with the edit form, before any panel opens.
  await expect.poll(() => mock.roleSearches.length).toBe(1);
  await expect.poll(() => mock.applicationSearches.length).toBe(1);
  await expect
    .poll(() => [...mock.catalogRequests].sort())
    .toEqual(['external-system', 'technology']);
  expect(Object.fromEntries(mock.roleSearches[0].searchParams)).toEqual({ page: '0', size: '20' });
  expect(Object.fromEntries(mock.applicationSearches[0].searchParams)).toEqual({
    page: '0',
    size: '20',
    sort: 'name,asc',
    statusId: '1',
  });

  await expect(dialog.getByRole('radio', { name: 'Sistema extern' })).toBeChecked();
  await expect(dialogField(page, 'external-system')).toHaveText('Soffid');
  await expect(dialogField(page, 'technology')).toHaveText('Spring Boot');
  await expect(dialogField(page, 'soffid-person')).toHaveValue('u00004');
  await expect(dialog.locator('.p-multiselect-chip')).toHaveText([
    'INV_ADMIN — SEYCON',
    'INV_READ — SEYCON',
  ]);

  await selectOption(page, 'application-integration-dialog-technology', 'Angular');
  await pickRoles(page, 'INV_READ — SEYCON', 'INV_WRITE — SEYCON');
  await expect(dialog.locator('.p-multiselect-chip')).toHaveText([
    'INV_ADMIN — SEYCON',
    'INV_WRITE — SEYCON',
  ]);
  await dialog.getByRole('button', { name: 'Desa els canvis de la connexió' }).click();

  await expect(dialog).toBeHidden();
  expect(mock.connectionPuts).toEqual([
    {
      path: 'application/integration-connection/1',
      body: {
        appIntegrationId: 13,
        applicationId: null,
        externalSystemId: 2,
        technologyId: 5,
        username: 'u00004',
        requiredRoleIds: [26, 40],
      },
    },
  ]);
  await expect(toast(page).getByText(CONNECTION_SAVED)).toBeVisible();
  await expect.poll(() => mock.listRequests.length).toBe(2);
  await expect(rows(page).first()).toContainText('Angular');
  await expect(rows(page).first()).toContainText('INV_ADMIN, INV_WRITE');
  // Opening the roles panel reused the list requested with the form.
  expect(mock.roleSearches).toHaveLength(1);
});

test('adds a connection to another active application of the inventory', async ({
  page,
}, testInfo) => {
  const mock = await openIntegrations(page, { connections: [] });
  await startEditing(page);

  await page.getByRole('button', { name: ADD_CONNECTION }).click();
  const dialog = dialogTitled(page, 'Afegir connexió');
  await expect(dialog).toBeVisible();
  await expect(dialog.getByRole('radio', { name: "Aplicació de l'inventari" })).toBeChecked();
  await expect.poll(() => mock.applicationSearches.length).toBe(1);

  await dialogField(page, 'application').click();
  await expect(page.getByRole('option', { name: 'APP2 — Aplicació dos' })).toBeVisible();
  // The application being edited is never offered as the other end of its own connection.
  await expect(page.getByRole('option', { name: 'TEST — Integracions test' })).toHaveCount(0);
  await page.getByRole('searchbox', { name: 'Aplicació' }).fill('dos');
  await expect
    .poll(() => mock.applicationSearches.at(-1)?.searchParams.get('quickSearch'))
    .toBe('dos');
  await page.getByRole('option', { name: 'APP2 — Aplicació dos' }).click();

  await selectOption(page, 'application-integration-dialog-technology', 'Spring Boot');
  await pickSoffidUser(page, 'mar', /Maria Tur/);
  await pickRoles(page, 'INV_ADMIN — SEYCON');
  await page.screenshot({ path: testInfo.outputPath('integrations-application-connection.png') });
  await dialog.getByRole('button', { name: 'Afegeix la connexió' }).click();

  await expect(dialog).toBeHidden();
  expect(mock.connectionPosts).toEqual([
    {
      appIntegrationId: 13,
      applicationId: 7,
      externalSystemId: null,
      technologyId: 4,
      username: 'u00009',
      requiredRoleIds: [26],
    },
  ]);
  await expect(toast(page).getByText(CONNECTION_SAVED)).toBeVisible();
  await expect(rows(page)).toHaveCount(1);
  await expect(rows(page).first()).toContainText('Aplicació dos');
  await expect(rows(page).first()).toContainText("Aplicació de l'inventari");
});

test('rejects a connection without its required fields, roles or a Soffid user code', async ({
  page,
}, testInfo) => {
  const mock = await openIntegrations(page, { connections: [] });
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
  await expect(dialogField(page, 'roles')).toHaveAttribute(
    'aria-invalid',
    'true',
  );
  await page.screenshot({ path: testInfo.outputPath('integrations-dialog-required.png') });

  await dialog.getByRole('radio', { name: 'Sistema extern' }).check();
  await expect(dialogField(page, 'application-error')).toHaveCount(0);
  await expect(dialogField(page, 'external-system-error')).toHaveText(
    REQUIRED,
  );
  await selectOption(page, 'application-integration-dialog-external-system', 'Soffid');
  await selectOption(page, 'application-integration-dialog-technology', 'Spring Boot');
  await expect(dialogField(page, 'external-system-error')).toHaveCount(0);
  await expect(dialogField(page, 'technology-error')).toHaveCount(0);

  // Soffid people without a user code are not offered, and typed text is not a selection.
  const user = dialogField(page, 'soffid-person');
  await user.pressSequentially('mar');
  await expect(page.getByRole('option', { name: /Maria Tur/ })).toBeVisible();
  await expect(page.getByRole('option', { name: /Marta Sense Codi/ })).toHaveCount(0);
  await dialog.getByRole('heading', { name: 'Afegir connexió' }).click();
  await expect(userError).toHaveText(
    "Selecciona un usuari de la llista de Soffid que tingui codi d'usuari.",
  );
  await expect(user).toHaveAttribute('aria-invalid', 'true');
  await submit.click();
  await expect(dialog).toBeVisible();

  await user.clear();
  await pickSoffidUser(page, 'mar', /Maria Tur/);
  await expect(userError).toHaveText('');
  await submit.click();
  await expect(rolesError).toHaveText('Selecciona almenys un rol requerit.');
  expect(mock.connectionPosts).toHaveLength(0);

  await pickRoles(page, 'INV_ADMIN — SEYCON');
  await expect(rolesError).toHaveCount(0);
  await submit.click();
  await expect(dialog).toBeHidden();
  expect(mock.connectionPosts).toEqual([
    {
      appIntegrationId: 13,
      applicationId: null,
      externalSystemId: 2,
      technologyId: 4,
      username: 'u00009',
      requiredRoleIds: [26],
    },
  ]);
});

test('creates the integration record from the observations before adding connections', async ({
  page,
}, testInfo) => {
  const mock = await openIntegrations(page, { appIntegrationId: null, connections: [] });

  await expect(page.getByRole('heading', { name: 'Connexions' })).toBeVisible();
  await expect(rows(page)).toHaveText(["No s'han trobat resultats"]);
  expect(mock.listRequests).toHaveLength(0);

  await startEditing(page);
  await expect(section(page).getByText(ANCHOR_REQUIRED)).toBeVisible();
  await expect(page.getByRole('button', { name: ADD_CONNECTION })).toBeDisabled();
  await page.screenshot({
    path: testInfo.outputPath('integrations-anchor-required.png'),
    fullPage: true,
  });

  const editor = page.locator('p-editor .ql-editor');
  await editor.click();
  await editor.pressSequentially('Primera nota');
  await page.getByRole('button', { name: SAVE_SECTION }).click();

  await expect(page.getByText(OBSERVATIONS_SAVED)).toBeVisible();
  // Quill stores typed spaces as non-breaking spaces.
  expect(mock.anchorPosts).toEqual([
    { applicationId: 1, observation: expect.stringMatching(/^<p>Primera(&nbsp;| )nota<\/p>$/) },
  ]);
  await expect(page.getByRole('button', { name: EDIT_SECTION, exact: true })).toBeVisible();

  await startEditing(page);
  await expect(section(page).getByText(ANCHOR_REQUIRED)).toHaveCount(0);
  await page.getByRole('button', { name: ADD_CONNECTION }).click();
  const dialog = dialogTitled(page, 'Afegir connexió');
  await dialog.getByRole('radio', { name: 'Sistema extern' }).check();
  await selectOption(page, 'application-integration-dialog-external-system', 'Soffid');
  await selectOption(page, 'application-integration-dialog-technology', 'Spring Boot');
  await pickSoffidUser(page, 'mar', /Maria Tur/);
  await pickRoles(page, 'INV_ADMIN — SEYCON');
  await dialog.getByRole('button', { name: 'Afegeix la connexió' }).click();

  await expect(dialog).toBeHidden();
  expect(mock.connectionPosts).toEqual([expect.objectContaining({ appIntegrationId: 21 })]);
  await expect
    .poll(() => mock.listRequests.at(-1)?.searchParams.get('appIntegrationId'))
    .toBe('21');
  await expect(rows(page)).toHaveCount(1);
  await expect(rows(page).first()).toContainText('u00009');
});

test('reports a forbidden integration record and stays read-only', async ({ page }) => {
  await openIntegrations(page, { anchorStatus: 403 });

  await expect(section(page).getByRole('alert')).toHaveText(FORBIDDEN);
  await expect(page.getByRole('button', { name: RETRY })).toBeVisible();
  await expect(page.getByRole('heading', { name: 'Connexions' })).toHaveCount(0);

  await page.getByRole('button', { name: EDIT_SECTION, exact: true }).click();
  await expect(toast(page).getByText(FORBIDDEN)).toBeVisible();
  await expect(page.getByRole('button', { name: SAVE_SECTION })).toHaveCount(0);
});

test('shows a deleted integration record without letting it be edited', async ({ page }) => {
  await openIntegrations(page, {
    anchor: { deletedAt: '2026-10-01T10:00:00', observation: '<p>Nota antiga</p>' },
    connections: [MATCHING],
  });

  await expect(section(page).getByRole('alert')).toHaveText(DELETED);
  await expect(page.getByRole('button', { name: RETRY })).toHaveCount(0);
  await expect(rows(page)).toHaveCount(1);
  await expect(page.locator('.invai-form-static-value--rich')).toContainText('Nota antiga');

  await page.getByRole('button', { name: EDIT_SECTION, exact: true }).click();
  await expect(toast(page).getByText(DELETED)).toBeVisible();
  await expect(page.getByRole('button', { name: SAVE_SECTION })).toHaveCount(0);
  await expect(page.getByRole('button', { name: ADD_CONNECTION })).toHaveCount(0);
});
