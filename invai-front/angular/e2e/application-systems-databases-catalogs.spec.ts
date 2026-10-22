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

const PRO = { id: 1, code: 'PRO', name: 'Producció', nameEs: 'Producción', deletedAt: null };
const PRE = {
  id: 2,
  code: 'PRE',
  name: 'Preproducció',
  nameEs: 'Preproducción',
  deletedAt: null,
};
const APPLICATION_TYPE = { id: 1, code: 'APPLICATION', name: 'Aplicació', nameEs: 'Aplicación' };
const DATABASE_TYPE = { id: 2, code: 'DATABASE', name: 'Base de dades', nameEs: 'Base de datos' };
const server = (
  id: number,
  name: string,
  environment: typeof PRO,
  serverType: typeof APPLICATION_TYPE,
) => ({ id, name, environment, serverType, deletedAt: null });
const APP01 = server(11, 'app01.caib.es', PRO, APPLICATION_TYPE);
const APP02 = server(12, 'app02.caib.es', PRE, APPLICATION_TYPE);
const DB01 = server(21, 'db01.caib.es', PRO, DATABASE_TYPE);
const DB02 = server(22, 'db02.caib.es', PRE, DATABASE_TYPE);
const ORACLE = { id: 1, name: 'Oracle', defaultPort: 1521, deletedAt: null };
const POSTGRES = { id: 2, name: 'PostgreSQL', defaultPort: 5432, deletedAt: null };

const SYSTEMS = [
  {
    id: 101,
    server: APP01,
    instance: 'weblogic-01',
    port: 7001,
    version: '14c',
    description: 'Servidor principal',
    deletedAt: null,
  },
  {
    id: 102,
    server: APP02,
    instance: 'tomcat-02',
    port: 8080,
    version: '10',
    description: null,
    deletedAt: null,
  },
];
const DATABASES = [
  {
    id: 201,
    server: DB01,
    service: 'INVAI',
    port: 1521,
    databaseType: ORACLE,
    description: null,
    deletedAt: null,
  },
  {
    id: 202,
    server: DB02,
    service: 'GESTIO',
    port: 5432,
    databaseType: POSTGRES,
    description: null,
    deletedAt: null,
  },
];

const INFORMATION_SYSTEM_DB = {
  id: 30,
  application: { id: 1 },
  observation: null,
  deletedAt: null,
};
const SECTION_TITLE = 'Infraestructura de sistemes i bases de dades';
const SERVER_DIALOG = "Afegir servidor a l'aplicació";
const DATABASE_DIALOG = "Afegir base de dades a l'aplicació";
const ADD_SERVER = "Afegeix el servidor a l'aplicació";
const ADD_DATABASE = "Afegeix la base de dades a l'aplicació";
const FILTERS = 'Mostra o amaga els filtres';
// Every catalog request lists the active records not related to the application yet.
const CATALOG_PARAMS = {
  page: '0',
  size: '10',
  statusId: '1',
  unassignedToInformationSystemDbId: '30',
};
const NARROWING_PARAMS = ['search', 'serverId', 'instance', 'version', 'service', 'databaseTypeId'];

type SystemRecord = (typeof SYSTEMS)[number];
type DatabaseRecord = (typeof DATABASES)[number];

interface CatalogsMock {
  systemCatalog: URL[];
  databaseCatalog: URL[];
  systemRelationPosts: unknown[];
  databaseRelationPosts: unknown[];
}

const contains = (value: string | number, query: string | null) =>
  !query || String(value).toLowerCase().includes(query.toLowerCase());
const matchesId = (id: number, param: string | null) => param === null || id === Number(param);

async function openSystemsDatabases(page: Page): Promise<CatalogsMock> {
  const mock: CatalogsMock = {
    systemCatalog: [],
    databaseCatalog: [],
    systemRelationPosts: [],
    databaseRelationPosts: [],
  };
  let relatedSystems: SystemRecord[] = [];
  let relatedDatabases: DatabaseRecord[] = [];
  await page.route('**/invaiback/**', async (route: Route) => {
    const request = route.request();
    const url = new URL(request.url());
    const params = url.searchParams;
    const resource = url.pathname.replace(/^.*\/invaiback\//, '');
    const method = request.method();
    if (resource === 'auth/me') {
      return route.fulfill({ json: { authenticated: true, username: 'catalogs-test' } });
    }
    if (resource === 'application/1') {
      return route.fulfill({
        json: {
          id: 1,
          code: 'TEST',
          name: 'Sistemes test',
          status: 'ACTIVE',
          appInformationSystemDbId: INFORMATION_SYSTEM_DB.id,
        },
      });
    }
    if (resource === 'application/system-database/30') {
      return route.fulfill({ json: INFORMATION_SYSTEM_DB });
    }
    const relation = (id: number, key: 'system' | 'database', record: unknown) => ({
      id,
      informationSystemDb: INFORMATION_SYSTEM_DB,
      [key]: record,
      deletedAt: null,
    });
    if (resource === 'application/system/30') {
      return route.fulfill({
        json: pageOf(relatedSystems.map((system) => relation(300 + system.id, 'system', system))),
      });
    }
    if (resource === 'application/database/30') {
      return route.fulfill({
        json: pageOf(
          relatedDatabases.map((database) => relation(400 + database.id, 'database', database)),
        ),
      });
    }
    if (resource === 'application/system' && method === 'POST') {
      const body = request.postDataJSON();
      mock.systemRelationPosts.push(body);
      relatedSystems = SYSTEMS.filter((system) => system.id === body.systemId);
      return route.fulfill({
        status: 201,
        json: relation(300 + body.systemId, 'system', relatedSystems[0]),
      });
    }
    if (resource === 'application/database' && method === 'POST') {
      const body = request.postDataJSON();
      mock.databaseRelationPosts.push(body);
      relatedDatabases = DATABASES.filter((database) => database.id === body.databaseId);
      return route.fulfill({
        status: 201,
        json: relation(400 + body.databaseId, 'database', relatedDatabases[0]),
      });
    }
    // The section filter options read every record; only the catalog leaves out related ones.
    const catalog = params.has('unassignedToInformationSystemDbId');
    const search = params.get('search');
    if (resource === 'system') {
      if (!catalog) return route.fulfill({ json: pageOf(SYSTEMS) });
      mock.systemCatalog.push(url);
      const found = SYSTEMS.filter(
        (system) =>
          !relatedSystems.includes(system) &&
          (!search ||
            [system.server.name, system.instance, system.version].some((value) =>
              contains(value, search),
            )) &&
          matchesId(system.server.id, params.get('serverId')) &&
          contains(system.instance, params.get('instance')) &&
          contains(system.version, params.get('version')),
      );
      return route.fulfill({ json: pageOf(found) });
    }
    if (resource === 'database') {
      if (!catalog) return route.fulfill({ json: pageOf(DATABASES) });
      mock.databaseCatalog.push(url);
      const found = DATABASES.filter(
        (database) =>
          !relatedDatabases.includes(database) &&
          (!search ||
            [database.server.name, database.service].some((value) => contains(value, search))) &&
          matchesId(database.server.id, params.get('serverId')) &&
          contains(database.service, params.get('service')) &&
          matchesId(database.databaseType.id, params.get('databaseTypeId')),
      );
      return route.fulfill({ json: pageOf(found) });
    }
    if (resource === 'server') {
      const databaseServers = params.get('serverTypeCode') === 'DATABASE';
      return route.fulfill({ json: pageOf(databaseServers ? [DB01, DB02] : [APP01, APP02]) });
    }
    if (resource === 'database-vendor') return route.fulfill({ json: pageOf([ORACLE, POSTGRES]) });
    if (resource === 'environment') return route.fulfill({ json: pageOf([PRE, PRO]) });
    return route.fulfill({ json: pageOf([]) });
  });
  await page.goto('/aplicacions/1/systems-databases');
  await expect(page.getByRole('heading', { name: SECTION_TITLE })).toBeVisible();
  return mock;
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

test('searches and filters the server catalog of the add dialog', async ({ page }, testInfo) => {
  const mock = await openSystemsDatabases(page);
  const dialog = await openAddDialog(page, 'servidor', SERVER_DIALOG);
  await expect(catalogRows(dialog)).toHaveCount(2);

  const search = dialog.getByRole('textbox', { name: 'Cerca ràpida de servidors' });
  await search.fill('app01');
  await expect
    .poll(() => lastParams(mock.systemCatalog))
    .toEqual({ ...CATALOG_PARAMS, search: 'app01' });
  await expect(catalogRows(dialog)).toHaveCount(1);
  await expect(catalogRows(dialog).first()).toContainText('app01.caib.es');

  const filters = dialog.getByRole('button', { name: FILTERS, exact: true });
  await expect(filters).toHaveAttribute('aria-expanded', 'false');
  await filters.click();
  await expect(filters).toHaveAttribute('aria-expanded', 'true');
  await expect(dialog.getByRole('heading', { name: 'Cercador', level: 3 })).toBeVisible();
  await selectOption(
    page,
    'application-system-catalog-filter-server',
    'app01.caib.es · Producció',
  );
  const serverFilter = dialog.getByRole('combobox', { name: 'Servidor' });
  await expect(serverFilter).toHaveText('app01.caib.es · Producció');
  await dialog.getByLabel('Instància').fill('weblogic');
  await dialog.getByLabel('Versió').fill('14c');
  await expect(
    dialog.getByRole('button', { name: `${FILTERS} (3 filtres aplicats)` }),
  ).toBeVisible();
  await page.screenshot({
    path: testInfo.outputPath('server-catalog-filters.png'),
    fullPage: true,
  });

  const requestsBeforeApplying = mock.systemCatalog.length;
  await filterPanel(dialog).getByRole('button', { name: 'Cerca' }).click();
  await expect.poll(() => mock.systemCatalog.length).toBe(requestsBeforeApplying + 1);
  // Applying the filters keeps the quick search.
  expect(lastParams(mock.systemCatalog)).toEqual({
    ...CATALOG_PARAMS,
    search: 'app01',
    serverId: '11',
    instance: 'weblogic',
    version: '14c',
  });
  await expect(catalogRows(dialog)).toHaveCount(1);

  const requestsBeforeReset = mock.systemCatalog.length;
  await filterPanel(dialog).getByRole('button', { name: 'Neteja' }).click();
  await expect(dialog.getByRole('button', { name: FILTERS, exact: true })).toBeVisible();
  await expect(search).toHaveValue('');
  await expect(serverFilter).toHaveText(/^\s*$/);
  await expect(dialog.getByLabel('Instància')).toHaveValue('');
  await expect(dialog.getByLabel('Versió')).toHaveValue('');
  await expect(catalogRows(dialog)).toHaveCount(2);
  // The unfiltered page may come from the cache; a request, if any, must not narrow it.
  expect(mock.systemCatalog.slice(requestsBeforeReset).flatMap(narrowingParams)).toEqual([]);
});

test('searches and filters the database catalog of the add dialog', async ({ page }, testInfo) => {
  const mock = await openSystemsDatabases(page);
  const dialog = await openAddDialog(page, 'base de dades', DATABASE_DIALOG);
  await expect(catalogRows(dialog)).toHaveCount(2);

  const search = dialog.getByRole('textbox', { name: 'Cerca ràpida de bases de dades' });
  await search.fill('gestio');
  await expect
    .poll(() => lastParams(mock.databaseCatalog))
    .toEqual({ ...CATALOG_PARAMS, search: 'gestio' });
  await expect(catalogRows(dialog)).toHaveCount(1);
  await expect(catalogRows(dialog).first()).toContainText('GESTIO');
  await dialog.getByRole('button', { name: 'Neteja la cerca ràpida' }).click();
  await expect(search).toHaveValue('');
  await expect(catalogRows(dialog)).toHaveCount(2);

  await dialog.getByRole('button', { name: FILTERS, exact: true }).click();
  await selectOption(
    page,
    'application-database-catalog-filter-server',
    'db01.caib.es · Producció',
  );
  await dialog.getByLabel('Servei / SID').fill('inv');
  await selectOption(page, 'application-database-catalog-filter-type', 'Oracle');
  const serverFilter = dialog.getByRole('combobox', { name: 'Servidor' });
  const typeFilter = dialog.getByRole('combobox', { name: 'Proveïdor' });
  await expect(serverFilter).toHaveText('db01.caib.es · Producció');
  await expect(typeFilter).toHaveText('Oracle');
  await expect(
    dialog.getByRole('button', { name: `${FILTERS} (3 filtres aplicats)` }),
  ).toBeVisible();
  await filterPanel(dialog).getByRole('button', { name: 'Cerca' }).click();

  await expect
    .poll(() => lastParams(mock.databaseCatalog))
    .toEqual({ ...CATALOG_PARAMS, serverId: '21', service: 'inv', databaseTypeId: '1' });
  await expect(catalogRows(dialog)).toHaveCount(1);
  await expect(catalogRows(dialog).first()).toContainText('INVAI');
  await page.screenshot({
    path: testInfo.outputPath('database-catalog-filters.png'),
    fullPage: true,
  });

  const requestsBeforeReset = mock.databaseCatalog.length;
  await filterPanel(dialog).getByRole('button', { name: 'Neteja' }).click();
  await expect(dialog.getByRole('button', { name: FILTERS, exact: true })).toBeVisible();
  await expect(serverFilter).toHaveText(/^\s*$/);
  await expect(dialog.getByLabel('Servei / SID')).toHaveValue('');
  await expect(typeFilter).toHaveText(/^\s*$/);
  await expect(catalogRows(dialog)).toHaveCount(2);
  expect(mock.databaseCatalog.slice(requestsBeforeReset).flatMap(narrowingParams)).toEqual([]);
});

test('relates a server or a database only once one is selected in the catalog', async ({
  page,
}) => {
  const mock = await openSystemsDatabases(page);
  const servers = await openAddDialog(page, 'servidor', SERVER_DIALOG);
  await servers.getByRole('button', { name: ADD_SERVER }).click();
  await expect(servers).toBeVisible();
  expect(mock.systemRelationPosts).toEqual([]);

  await catalogRows(servers)
    .filter({ hasText: 'tomcat-02' })
    .getByRole('radio', { name: 'Selecciona el registre' })
    .check();
  await servers.getByRole('button', { name: ADD_SERVER }).click();
  await expect(servers).toBeHidden();
  expect(mock.systemRelationPosts).toEqual([{ informationSystemDbId: 30, systemId: 102 }]);

  await page.getByRole('button', { name: 'Afegir base de dades', exact: true }).click();
  const databases = dialogTitled(page, DATABASE_DIALOG);
  await databases.getByRole('button', { name: ADD_DATABASE }).click();
  await expect(databases).toBeVisible();
  expect(mock.databaseRelationPosts).toEqual([]);

  await catalogRows(databases).filter({ hasText: 'INVAI' }).click();
  await databases.getByRole('button', { name: ADD_DATABASE }).click();
  await expect(databases).toBeHidden();
  expect(mock.databaseRelationPosts).toEqual([{ informationSystemDbId: 30, databaseId: 201 }]);
});

// App bug: the dialogs derive isInvalid with computed() from FormControl state, which signals
// do not track, so the error never renders after an empty submit.
test.fixme('explains that a server or a database must be selected', async ({ page }, testInfo) => {
  await openSystemsDatabases(page);
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
