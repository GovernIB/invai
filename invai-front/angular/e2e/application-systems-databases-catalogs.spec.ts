import type { Locator, Page, TestInfo } from '@playwright/test';

import {
  APPLICATION_INFRASTRUCTURE_API,
  createInfrastructureDatabase,
  createInfrastructureSystem,
  registerApplicationDatabaseRelationCleanup,
  registerApplicationSystemRelationCleanup,
} from './support/application-systems-databases';
import { type DisposableApplication, expect, test } from './support/application.fixture';
import type { CleanupRegistry } from './support/cleanup-registry';
import { uniqueToken } from './support/data';
import {
  apiUrl,
  expectApiJsonResponse,
  expectJsonResponse,
  waitForApiResponse,
} from './support/http';
import { type PhysicalServerRecord, SYSTEMS_API } from './support/systems';
import { selectOption } from './support/ui';

const SECTION_TITLE = 'Infraestructura de sistemes i bases de dades';
const SERVER_DIALOG = "Afegir servidor a l'aplicació";
const DATABASE_DIALOG = "Afegir base de dades a l'aplicació";
const ADD_SERVER = "Afegeix el servidor a l'aplicació";
const ADD_DATABASE = "Afegeix la base de dades a l'aplicació";
const FILTERS = 'Mostra o amaga els filtres';
const NARROWING_PARAMS = ['search', 'serverId', 'instance', 'version', 'service', 'databaseTypeId'];

interface CatalogRequests {
  systemCatalog: URL[];
  databaseCatalog: URL[];
  systemRelationPosts: unknown[];
  databaseRelationPosts: unknown[];
}

// Every catalog request lists the active records not related to the application yet.
const catalogParams = (application: DisposableApplication) => ({
  page: '0',
  size: '10',
  statusId: '1',
  unassignedToInformationSystemDbId: String(application.appInformationSystemDbId),
});

// Two systems or two databases of the test, each on its own server: their names, instances,
// versions and services all carry the token of the test, plus 1 or 2.
const recordTokens = (testInfo: TestInfo) => {
  const token = uniqueToken(testInfo, 7);
  return { token, first: `${token}1`, second: `${token}2` };
};

async function serverLabel(page: Page, server: PhysicalServerRecord): Promise<string> {
  const detail = await expectApiJsonResponse<PhysicalServerRecord>(
    await page.request.get(apiUrl(page, `${SYSTEMS_API.servers}/${server.id}`)),
    `GET ${SYSTEMS_API.servers}/${server.id}`,
  );
  return `${detail.name} · ${detail.environment.name}`;
}

async function openSystemsDatabases(
  page: Page,
  application: DisposableApplication,
): Promise<CatalogRequests> {
  const requests: CatalogRequests = {
    systemCatalog: [],
    databaseCatalog: [],
    systemRelationPosts: [],
    databaseRelationPosts: [],
  };
  page.on('request', (request) => {
    const url = new URL(request.url());
    const method = request.method();
    // The section filter options read every record; only the catalog leaves out related ones.
    const catalog = url.searchParams.has('unassignedToInformationSystemDbId');
    if (url.pathname === SYSTEMS_API.systems && catalog) requests.systemCatalog.push(url);
    else if (url.pathname === SYSTEMS_API.databases && catalog) requests.databaseCatalog.push(url);
    else if (url.pathname === APPLICATION_INFRASTRUCTURE_API.systems && method === 'POST') {
      requests.systemRelationPosts.push(request.postDataJSON());
    } else if (url.pathname === APPLICATION_INFRASTRUCTURE_API.databases && method === 'POST') {
      requests.databaseRelationPosts.push(request.postDataJSON());
    }
  });
  await page.goto(`/aplicacions/${application.id}/systems-databases`);
  await expect(page.getByRole('heading', { name: SECTION_TITLE })).toBeVisible();
  return requests;
}

// The shared CRUD shell does not name its dialog yet, so it is located by its visible heading.
const dialogTitled = (page: Page, title: string) =>
  page.getByRole('dialog').filter({ has: page.getByRole('heading', { name: title }) });
const catalogRows = (dialog: Locator) =>
  dialog.locator('app-application-infrastructure-catalog-table tbody tr');
// The filter buttons' names start with their icon glyph, so they are looked up inside the panel.
const filterPanel = (dialog: Locator) => dialog.locator('app-search-filters');
const lastParams = (requests: URL[]) => Object.fromEntries(requests.at(-1)?.searchParams ?? []);
const narrowingParams = (request: URL) =>
  NARROWING_PARAMS.filter((name) => request.searchParams.has(name));

async function openAddDialog(page: Page, resource: string, title: string): Promise<Locator> {
  await page.getByRole('button', { name: `Editar ${SECTION_TITLE}`, exact: true }).click();
  await page.getByRole('button', { name: `Afegir ${resource}`, exact: true }).click();
  const dialog = dialogTitled(page, title);
  await expect(dialog).toBeVisible();
  return dialog;
}

async function createSystems(page: Page, cleanup: CleanupRegistry, testInfo: TestInfo) {
  const tokens = recordTokens(testInfo);
  const first = await createInfrastructureSystem(page, cleanup, tokens.first);
  const second = await createInfrastructureSystem(page, cleanup, tokens.second);
  return { tokens, first, second };
}

async function createDatabases(page: Page, cleanup: CleanupRegistry, testInfo: TestInfo) {
  const tokens = recordTokens(testInfo);
  const first = await createInfrastructureDatabase(page, cleanup, tokens.first);
  const second = await createInfrastructureDatabase(page, cleanup, tokens.second);
  return { tokens, first, second };
}

test('searches and filters the server catalog of the add dialog', async ({
  page,
  cleanupRegistry,
  disposableApplication,
}, testInfo) => {
  test.setTimeout(120_000);
  const { tokens, first } = await createSystems(page, cleanupRegistry, testInfo);
  const firstServer = await serverLabel(page, first.server);
  const requests = await openSystemsDatabases(page, disposableApplication);
  const dialog = await openAddDialog(page, 'servidor', SERVER_DIALOG);
  await expect(catalogRows(dialog).first()).toBeVisible();

  const search = dialog.getByRole('textbox', { name: 'Cerca ràpida de servidors' });
  await search.fill(tokens.token);
  await expect
    .poll(() => lastParams(requests.systemCatalog))
    .toEqual({ ...catalogParams(disposableApplication), search: tokens.token });
  await expect(catalogRows(dialog)).toHaveCount(2);
  await search.fill(tokens.first);
  await expect
    .poll(() => lastParams(requests.systemCatalog))
    .toEqual({ ...catalogParams(disposableApplication), search: tokens.first });
  await expect(catalogRows(dialog)).toHaveCount(1);
  await expect(catalogRows(dialog).first()).toContainText(first.server.name);

  const filters = dialog.getByRole('button', { name: FILTERS, exact: true });
  await expect(filters).toHaveAttribute('aria-expanded', 'false');
  await filters.click();
  await expect(filters).toHaveAttribute('aria-expanded', 'true');
  await expect(dialog.getByRole('heading', { name: 'Cercador', level: 3 })).toBeVisible();
  await selectOption(page, 'application-system-catalog-filter-server', firstServer);
  const serverFilter = dialog.getByRole('combobox', { name: 'Servidor' });
  await expect(serverFilter).toHaveText(firstServer);
  await dialog.getByLabel('Instància').fill(first.instance);
  await dialog.getByLabel('Versió').fill(first.version);
  await expect(
    dialog.getByRole('button', { name: `${FILTERS} (3 filtres aplicats)` }),
  ).toBeVisible();
  await page.screenshot({
    path: testInfo.outputPath('server-catalog-filters.png'),
    fullPage: true,
  });

  const requestsBeforeApplying = requests.systemCatalog.length;
  await filterPanel(dialog).getByRole('button', { name: 'Cerca' }).click();
  await expect.poll(() => requests.systemCatalog.length).toBe(requestsBeforeApplying + 1);
  // Applying the filters keeps the quick search.
  expect(lastParams(requests.systemCatalog)).toEqual({
    ...catalogParams(disposableApplication),
    search: tokens.first,
    serverId: String(first.server.id),
    instance: first.instance,
    version: first.version,
  });
  await expect(catalogRows(dialog)).toHaveCount(1);

  const requestsBeforeReset = requests.systemCatalog.length;
  await filterPanel(dialog).getByRole('button', { name: 'Neteja' }).click();
  await expect(dialog.getByRole('button', { name: FILTERS, exact: true })).toBeVisible();
  await expect(search).toHaveValue('');
  await expect(serverFilter).toHaveText(/^\s*$/);
  await expect(dialog.getByLabel('Instància')).toHaveValue('');
  await expect(dialog.getByLabel('Versió')).toHaveValue('');
  await expect(catalogRows(dialog).first()).toBeVisible();
  // The unfiltered page may come from the cache; a request, if any, must not narrow it.
  expect(requests.systemCatalog.slice(requestsBeforeReset).flatMap(narrowingParams)).toEqual([]);
});

test('searches and filters the database catalog of the add dialog', async ({
  page,
  cleanupRegistry,
  disposableApplication,
}, testInfo) => {
  test.setTimeout(120_000);
  const { tokens, first } = await createDatabases(page, cleanupRegistry, testInfo);
  const firstServer = await serverLabel(page, first.server);
  const requests = await openSystemsDatabases(page, disposableApplication);
  const dialog = await openAddDialog(page, 'base de dades', DATABASE_DIALOG);
  await expect(catalogRows(dialog).first()).toBeVisible();

  const search = dialog.getByRole('textbox', { name: 'Cerca ràpida de bases de dades' });
  await search.fill(first.service);
  await expect
    .poll(() => lastParams(requests.databaseCatalog))
    .toEqual({ ...catalogParams(disposableApplication), search: first.service });
  await expect(catalogRows(dialog)).toHaveCount(1);
  await expect(catalogRows(dialog).first()).toContainText(first.service);
  await dialog.getByRole('button', { name: 'Neteja la cerca ràpida' }).click();
  await expect(search).toHaveValue('');
  await expect(catalogRows(dialog).first()).toBeVisible();

  await dialog.getByRole('button', { name: FILTERS, exact: true }).click();
  await selectOption(page, 'application-database-catalog-filter-server', firstServer);
  await dialog.getByLabel('Servei / SID').fill(tokens.first);
  await selectOption(page, 'application-database-catalog-filter-type', first.databaseType.name);
  const serverFilter = dialog.getByRole('combobox', { name: 'Servidor' });
  const typeFilter = dialog.getByRole('combobox', { name: 'Proveïdor' });
  await expect(serverFilter).toHaveText(firstServer);
  await expect(typeFilter).toHaveText(first.databaseType.name);
  await expect(
    dialog.getByRole('button', { name: `${FILTERS} (3 filtres aplicats)` }),
  ).toBeVisible();
  await filterPanel(dialog).getByRole('button', { name: 'Cerca' }).click();

  await expect
    .poll(() => lastParams(requests.databaseCatalog))
    .toEqual({
      ...catalogParams(disposableApplication),
      serverId: String(first.server.id),
      service: tokens.first,
      databaseTypeId: String(first.databaseType.id),
    });
  await expect(catalogRows(dialog)).toHaveCount(1);
  await expect(catalogRows(dialog).first()).toContainText(first.service);
  await page.screenshot({
    path: testInfo.outputPath('database-catalog-filters.png'),
    fullPage: true,
  });

  const requestsBeforeReset = requests.databaseCatalog.length;
  await filterPanel(dialog).getByRole('button', { name: 'Neteja' }).click();
  await expect(dialog.getByRole('button', { name: FILTERS, exact: true })).toBeVisible();
  await expect(serverFilter).toHaveText(/^\s*$/);
  await expect(dialog.getByLabel('Servei / SID')).toHaveValue('');
  await expect(typeFilter).toHaveText(/^\s*$/);
  await expect(catalogRows(dialog).first()).toBeVisible();
  expect(requests.databaseCatalog.slice(requestsBeforeReset).flatMap(narrowingParams)).toEqual([]);
});

test('relates a server or a database only once one is selected in the catalog', async ({
  page,
  cleanupRegistry,
  disposableApplication,
}, testInfo) => {
  test.setTimeout(150_000);
  const aggregateId = disposableApplication.appInformationSystemDbId!;
  const { tokens, second: system } = await createSystems(page, cleanupRegistry, testInfo);
  const { first: database } = await createDatabases(page, cleanupRegistry, testInfo);
  registerApplicationSystemRelationCleanup(page, cleanupRegistry, aggregateId, {
    systemId: system.id,
  });
  registerApplicationDatabaseRelationCleanup(page, cleanupRegistry, aggregateId, {
    databaseId: database.id,
  });
  const requests = await openSystemsDatabases(page, disposableApplication);
  const servers = await openAddDialog(page, 'servidor', SERVER_DIALOG);
  await servers.getByRole('button', { name: ADD_SERVER }).click();
  await expect(servers).toBeVisible();
  expect(requests.systemRelationPosts).toEqual([]);

  await servers.getByRole('textbox', { name: 'Cerca ràpida de servidors' }).fill(tokens.token);
  await catalogRows(servers)
    .filter({ hasText: system.instance })
    .getByRole('radio', { name: 'Selecciona el registre' })
    .check();
  const systemPromise = waitForApiResponse(page, 'POST', APPLICATION_INFRASTRUCTURE_API.systems);
  await servers.getByRole('button', { name: ADD_SERVER }).click();
  await expectJsonResponse(await systemPromise);
  await expect(servers).toBeHidden();
  expect(requests.systemRelationPosts).toEqual([
    { informationSystemDbId: aggregateId, systemId: system.id },
  ]);

  await page.getByRole('button', { name: 'Afegir base de dades', exact: true }).click();
  const databases = dialogTitled(page, DATABASE_DIALOG);
  await databases.getByRole('button', { name: ADD_DATABASE }).click();
  await expect(databases).toBeVisible();
  expect(requests.databaseRelationPosts).toEqual([]);

  await databases
    .getByRole('textbox', { name: 'Cerca ràpida de bases de dades' })
    .fill(database.service);
  await catalogRows(databases).filter({ hasText: database.service }).click();
  const databasePromise = waitForApiResponse(
    page,
    'POST',
    APPLICATION_INFRASTRUCTURE_API.databases,
  );
  await databases.getByRole('button', { name: ADD_DATABASE }).click();
  await expectJsonResponse(await databasePromise);
  await expect(databases).toBeHidden();
  expect(requests.databaseRelationPosts).toEqual([
    { informationSystemDbId: aggregateId, databaseId: database.id },
  ]);
});

// App bug: the dialogs derive isInvalid with computed() from FormControl state, which signals
// do not track, so the error never renders after an empty submit.
test.fixme('explains that a server or a database must be selected', async ({
  page,
  disposableApplication,
}, testInfo) => {
  await openSystemsDatabases(page, disposableApplication);
  const servers = await openAddDialog(page, 'servidor', SERVER_DIALOG);
  const serverSelection = servers.getByRole('group', { name: SERVER_DIALOG });
  await servers.getByRole('button', { name: ADD_SERVER }).click();
  await expect(serverSelection.getByRole('alert')).toHaveText('Selecciona un servidor.');
  await expect(serverSelection).toHaveAttribute('aria-invalid', 'true');
  await expect(serverSelection).toHaveAccessibleDescription('Selecciona un servidor.');
  await page.screenshot({
    path: testInfo.outputPath('server-catalog-required.png'),
    fullPage: true,
  });
  await catalogRows(servers).first().click();
  await expect(serverSelection.getByRole('alert')).toHaveCount(0);
  await servers
    .getByRole('button', { name: "Tanca el diàleg del servidor de l'aplicació" })
    .click();

  await page.getByRole('button', { name: 'Afegir base de dades', exact: true }).click();
  const databases = dialogTitled(page, DATABASE_DIALOG);
  const databaseSelection = databases.getByRole('group', { name: DATABASE_DIALOG });
  await databases.getByRole('button', { name: ADD_DATABASE }).click();
  await expect(databaseSelection.getByRole('alert')).toHaveText('Selecciona una base de dades.');
  await expect(databaseSelection).toHaveAttribute('aria-invalid', 'true');
  await expect(databaseSelection).toHaveAccessibleDescription('Selecciona una base de dades.');
});
