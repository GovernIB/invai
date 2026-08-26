import type { Locator, Page, Request, Response, TestInfo } from '@playwright/test';

import {
  APPLICATION_INFRASTRUCTURE_API,
  type ApplicationDatabaseRelationRecord,
  type ApplicationSystemRelationRecord,
  createApplicationDatabaseRelation,
  createApplicationSystemRelation,
  createInfrastructureDatabase,
  createInfrastructureSystem,
  ensureApplicationInfrastructureAggregate,
  registerApplicationDatabaseRelationCleanup,
  registerApplicationReactivationForCleanup,
  registerApplicationSystemRelationCleanup,
} from './support/application-systems-databases';
import { expect, test } from './support/application.fixture';
import { uniqueToken } from './support/data';
import {
  apiUrl,
  expectJsonResponse,
  expectSuccessfulResponse,
  waitForApiResponse,
} from './support/http';
import {
  type EnvironmentRecord,
  ensureEnvironment,
  INACTIVE_STATUS_ID,
  type InfrastructureSystemRecord,
  SYSTEMS_API,
} from './support/systems';
import { selectOption, selectRowAction } from './support/ui';

const SECTION_TITLE = 'Infraestructura de sistemes i bases de dades';
const EDIT_SECTION = `Editar ${SECTION_TITLE}`;
const SAVE_SECTION = `Desar els canvis de ${SECTION_TITLE}`;
const SERVER_FILTERS = 'Mostra o oculta els filtres de servidors';
const DATABASE_FILTERS = 'Mostra o oculta els filtres de bases de dades';
const DELETE_ACTION = 'Elimina';

const SYSTEM_DIALOG_ACTIONS = {
  accept: "Accepta la consulta del servidor de l'aplicació",
  add: "Afegeix el servidor a l'aplicació",
  edit: "Edita el servidor de l'aplicació",
  deactivate: "Dona de baixa el servidor de l'aplicació",
  save: "Desa els canvis del servidor de l'aplicació",
};

const DATABASE_DIALOG_ACTIONS = {
  accept: "Accepta la consulta de la base de dades de l'aplicació",
  add: "Afegeix la base de dades a l'aplicació",
  edit: "Edita la base de dades de l'aplicació",
  deactivate: "Dona de baixa la base de dades de l'aplicació",
  save: "Desa els canvis de la base de dades de l'aplicació",
};

test.describe('application systems and databases', () => {
  test('creates observations and reloads every initial contract without duplicate requests', async ({
    page,
    disposableApplication,
  }) => {
    test.setTimeout(180_000);
    const observation = `Observacions E2E ${disposableApplication.code}`;

    await page.goto(infrastructureRoute(disposableApplication.id));
    await waitForInfrastructureSection(page);
    await expect(page.getByRole('button', { name: 'Afegir servidor' })).toHaveCount(0);
    await expect(page.getByRole('button', { name: 'Afegir base de dades' })).toHaveCount(0);

    await page.getByRole('button', { name: EDIT_SECTION, exact: true }).click();
    const editor = page.locator('.application-systems-databases__observations .ql-editor');
    await expect(editor).toHaveAttribute('contenteditable', 'true');
    await editor.fill(observation);

    const createPromise = waitForAggregateSave(page);
    await page.getByRole('button', { name: SAVE_SECTION, exact: true }).click();
    const createResponse = await createPromise;
    const payload = createResponse.request().postDataJSON() as {
      applicationId: number;
      observation: string;
    };
    expect(payload.applicationId).toBe(disposableApplication.id);
    expect(payload.observation).toContain(disposableApplication.code);
    const aggregate = await expectJsonResponse<{ id: number; observation: string }>(createResponse);
    expect(aggregate.observation).toContain(disposableApplication.code);
    await expect(page.getByRole('button', { name: EDIT_SECTION, exact: true })).toBeVisible();

    await page.getByRole('link', { name: 'Responsables', exact: true }).click();
    await expect(
      page.getByRole('button', {
        name: "Editar Responsables de l'aplicació",
        exact: true,
      }),
    ).toBeVisible();
    await expect(page.getByRole('button', { name: /Desar els canvis de/ })).toHaveCount(0);

    const requests: string[] = [];
    const listener = (request: Request) => {
      if (request.method() !== 'GET') return;
      const url = new URL(request.url());
      if (isInfrastructureInitialRequest(url.pathname, aggregate.id)) {
        requests.push(url.toString());
      }
    };
    page.on('request', listener);
    await page.goto(infrastructureRoute(disposableApplication.id));
    await waitForInfrastructureSection(page);
    page.off('request', listener);

    const duplicates = duplicateUrls(requests);
    expect(duplicates, `Duplicate initial requests: ${duplicates.join(', ')}`).toEqual([]);
    expect(countPath(requests, `${APPLICATION_INFRASTRUCTURE_API.aggregate}/${aggregate.id}`)).toBe(
      1,
    );
    expect(countPath(requests, `${APPLICATION_INFRASTRUCTURE_API.systems}/${aggregate.id}`)).toBe(
      1,
    );
    expect(countPath(requests, `${APPLICATION_INFRASTRUCTURE_API.databases}/${aggregate.id}`)).toBe(
      1,
    );
    expect(countPath(requests, SYSTEMS_API.systems)).toBeGreaterThan(0);
    expect(countPath(requests, SYSTEMS_API.databases)).toBeGreaterThan(0);
    await expect(editor).toContainText(observation);
  });

  test('server relation supports create, immutable view, delete, filters, paging and sorting', async ({
    page,
    cleanupRegistry,
    disposableApplication,
  }, testInfo) => {
    test.setTimeout(240_000);
    const token = uniqueToken(testInfo);
    const aggregate = await ensureApplicationInfrastructureAggregate(
      page,
      disposableApplication.id,
      disposableApplication.appInformationSystemDbId,
    );
    const firstSystem = await createInfrastructureSystem(page, cleanupRegistry, `${token}A`);
    const relationTarget: { id?: number; systemId: number } = {
      systemId: firstSystem.id,
    };
    registerApplicationSystemRelationCleanup(page, cleanupRegistry, aggregate.id, relationTarget);

    await page.goto(infrastructureRoute(disposableApplication.id));
    await waitForInfrastructureSection(page);
    const servers = infrastructurePanel(page, 'Servidors');
    const databases = infrastructurePanel(page, 'Bases de dades');
    const add = servers.getByRole('button', { name: 'Afegir servidor', exact: true });
    await expect(add).toHaveCount(0);

    await test.step('creation is available only during section editing', async () => {
      await page.getByRole('button', { name: EDIT_SECTION, exact: true }).click();
      await expect(add).toBeEnabled();
      await add.click();
      const dialog = relationDialog(page, 'application-system-relation-dialog');
      await expect(
        page.getByRole('heading', { name: "Afegir servidor a l'aplicació" }),
      ).toBeVisible();
      await selectCatalogRow(dialog, firstSystem.instance);

      const createPromise = waitForApiResponse(
        page,
        'POST',
        APPLICATION_INFRASTRUCTURE_API.systems,
      );
      await page.getByRole('button', { name: SYSTEM_DIALOG_ACTIONS.add, exact: true }).click();
      const response = await createPromise;
      expect(response.request().postDataJSON()).toEqual({
        informationSystemDbId: aggregate.id,
        systemId: firstSystem.id,
      });
      const relation = await expectJsonResponse<ApplicationSystemRelationRecord>(response);
      relationTarget.id = relation.id;
      await expect(applicationRow(servers, firstSystem.instance)).toBeVisible();
      await page.getByRole('button', { name: SAVE_SECTION, exact: true }).click();
      await expect(add).toHaveCount(0);
    });

    await test.step('double-click opens an immutable View', async () => {
      const row = applicationRow(servers, firstSystem.instance);
      await row.focus();
      await row.dblclick();
      await expect(
        page.getByRole('heading', { name: "Consultar servidor de l'aplicació" }),
      ).toBeVisible();
      await expectOnlyViewActions(page, SYSTEM_DIALOG_ACTIONS);
      await page.getByRole('button', { name: SYSTEM_DIALOG_ACTIONS.accept, exact: true }).click();
      await expectFocusReturn(row);
    });

    await test.step('confirmed deletion and supported filters expose the inactive relation', async () => {
      await page.getByRole('button', { name: EDIT_SECTION, exact: true }).click();
      const row = applicationRow(servers, firstSystem.instance);
      await selectRowAction(page, row, DELETE_ACTION);
      await expect(
        page.getByRole('heading', { name: 'Donar de baixa el servidor?' }),
      ).toBeVisible();
      const deletePromise = waitForApiResponse(
        page,
        'DELETE',
        `${APPLICATION_INFRASTRUCTURE_API.systems}/${relationTarget.id}`,
      );
      await page.getByRole('button', { name: "Confirma l'acció", exact: true }).click();
      await expectSuccessfulResponse(await deletePromise);
      await showFilters(servers, SERVER_FILTERS);
      await selectSystemFilterOption(page, firstSystem);
      await selectOption(page, 'application-servers-filter-status', 'Inactiu');
      const filterPromise = waitForApiResponse(
        page,
        'GET',
        `${APPLICATION_INFRASTRUCTURE_API.systems}/${aggregate.id}`,
        (url) =>
          hasQuery(url, {
            systemId: firstSystem.id,
            statusId: INACTIVE_STATUS_ID,
          }),
      );
      await servers.getByRole('button', { name: 'Cerca' }).click();
      await expectSuccessfulResponse(await filterPromise);
      await expect(applicationRow(servers, firstSystem.instance)).toBeVisible();
    });

    await test.step('page size and nested sort affect only the server table', async () => {
      let databaseRequests = 0;
      const listener = (request: Request) => {
        if (
          request.method() === 'GET' &&
          new URL(request.url()).pathname ===
            `${APPLICATION_INFRASTRUCTURE_API.databases}/${aggregate.id}`
        ) {
          databaseRequests += 1;
        }
      };
      page.on('request', listener);
      const sizePromise = waitForApiResponse(
        page,
        'GET',
        `${APPLICATION_INFRASTRUCTURE_API.systems}/${aggregate.id}`,
        (url) => hasQuery(url, { size: 20, systemId: firstSystem.id }),
      );
      await selectPageSize(page, servers, '20');
      await expectSuccessfulResponse(await sizePromise);

      const sortPromise = waitForApiResponse(
        page,
        'GET',
        `${APPLICATION_INFRASTRUCTURE_API.systems}/${aggregate.id}`,
        (url) => url.searchParams.get('sort') === 'system.server.name,asc',
      );
      await servers.getByRole('columnheader', { name: /Servidor/ }).click();
      await expectSuccessfulResponse(await sortPromise);
      page.off('request', listener);
      expect(databaseRequests).toBe(0);
      await expect(databases.locator('.invai-table-loading-container')).toHaveAttribute(
        'aria-busy',
        'false',
      );
    });
  });

  test('database relation supports create, immutable view, delete, filters, paging and sorting', async ({
    page,
    cleanupRegistry,
    disposableApplication,
  }, testInfo) => {
    test.setTimeout(240_000);
    const token = uniqueToken(testInfo);
    const aggregate = await ensureApplicationInfrastructureAggregate(
      page,
      disposableApplication.id,
      disposableApplication.appInformationSystemDbId,
    );
    const firstDatabase = await createInfrastructureDatabase(page, cleanupRegistry, `${token}A`);
    const relationTarget: { id?: number; databaseId: number } = {
      databaseId: firstDatabase.id,
    };
    registerApplicationDatabaseRelationCleanup(page, cleanupRegistry, aggregate.id, relationTarget);

    await page.goto(infrastructureRoute(disposableApplication.id));
    await waitForInfrastructureSection(page);
    const servers = infrastructurePanel(page, 'Servidors');
    const databases = infrastructurePanel(page, 'Bases de dades');
    const add = databases.getByRole('button', { name: 'Afegir base de dades', exact: true });
    await expect(add).toHaveCount(0);

    await test.step('creation is available only during section editing', async () => {
      await page.getByRole('button', { name: EDIT_SECTION, exact: true }).click();
      await expect(add).toBeEnabled();
      await add.click();
      const dialog = relationDialog(page, 'application-database-relation-dialog');
      await expect(
        page.getByRole('heading', { name: "Afegir base de dades a l'aplicació" }),
      ).toBeVisible();
      await selectCatalogRow(dialog, firstDatabase.service);
      const createPromise = waitForApiResponse(
        page,
        'POST',
        APPLICATION_INFRASTRUCTURE_API.databases,
      );
      await page.getByRole('button', { name: DATABASE_DIALOG_ACTIONS.add, exact: true }).click();
      const response = await createPromise;
      expect(response.request().postDataJSON()).toEqual({
        informationSystemDbId: aggregate.id,
        databaseId: firstDatabase.id,
      });
      const relation = await expectJsonResponse<ApplicationDatabaseRelationRecord>(response);
      relationTarget.id = relation.id;
      await expect(applicationRow(databases, firstDatabase.service)).toBeVisible();
      await page.getByRole('button', { name: SAVE_SECTION, exact: true }).click();
      await expect(add).toHaveCount(0);
    });

    await test.step('Enter opens an immutable View', async () => {
      const row = applicationRow(databases, firstDatabase.service);
      await row.focus();
      await row.press('Enter');
      await expect(
        page.getByRole('heading', { name: "Consultar base de dades de l'aplicació" }),
      ).toBeVisible();
      await expectOnlyViewActions(page, DATABASE_DIALOG_ACTIONS);
      await page.getByRole('button', { name: DATABASE_DIALOG_ACTIONS.accept, exact: true }).click();
      await expectFocusReturn(row);
    });

    await test.step('confirmed deletion and supported filters expose the inactive relation', async () => {
      await page.getByRole('button', { name: EDIT_SECTION, exact: true }).click();
      const row = applicationRow(databases, firstDatabase.service);
      await selectRowAction(page, row, DELETE_ACTION);
      await expect(
        page.getByRole('heading', { name: 'Donar de baixa la base de dades?' }),
      ).toBeVisible();
      const deletePromise = waitForApiResponse(
        page,
        'DELETE',
        `${APPLICATION_INFRASTRUCTURE_API.databases}/${relationTarget.id}`,
      );
      await page.getByRole('button', { name: "Confirma l'acció", exact: true }).click();
      await expectSuccessfulResponse(await deletePromise);
      await showFilters(databases, DATABASE_FILTERS);
      await selectOption(page, 'application-databases-filter-database', firstDatabase.service);
      await selectOption(page, 'application-databases-filter-status', 'Inactiu');
      const filterPromise = waitForApiResponse(
        page,
        'GET',
        `${APPLICATION_INFRASTRUCTURE_API.databases}/${aggregate.id}`,
        (url) =>
          hasQuery(url, {
            databaseId: firstDatabase.id,
            statusId: INACTIVE_STATUS_ID,
          }),
      );
      await databases.getByRole('button', { name: 'Cerca' }).click();
      await expectSuccessfulResponse(await filterPromise);
      await expect(applicationRow(databases, firstDatabase.service)).toBeVisible();
    });

    await test.step('page size and nested sort affect only the database table', async () => {
      let serverRequests = 0;
      const listener = (request: Request) => {
        if (
          request.method() === 'GET' &&
          new URL(request.url()).pathname ===
            `${APPLICATION_INFRASTRUCTURE_API.systems}/${aggregate.id}`
        ) {
          serverRequests += 1;
        }
      };
      page.on('request', listener);
      const sizePromise = waitForApiResponse(
        page,
        'GET',
        `${APPLICATION_INFRASTRUCTURE_API.databases}/${aggregate.id}`,
        (url) => hasQuery(url, { size: 20, databaseId: firstDatabase.id }),
      );
      await selectPageSize(page, databases, '20');
      await expectSuccessfulResponse(await sizePromise);

      const sortPromise = waitForApiResponse(
        page,
        'GET',
        `${APPLICATION_INFRASTRUCTURE_API.databases}/${aggregate.id}`,
        (url) => url.searchParams.get('sort') === 'database.service,asc',
      );
      await databases.getByRole('columnheader', { name: /Base de dades/ }).click();
      await expectSuccessfulResponse(await sortPromise);
      page.off('request', listener);
      expect(serverRequests).toBe(0);
      await expect(servers.locator('.invai-table-loading-container')).toHaveAttribute(
        'aria-busy',
        'false',
      );
    });
  });

  test('unsupported filters warn and never reach either relation endpoint', async ({
    page,
    cleanupRegistry,
    disposableApplication,
  }, testInfo) => {
    test.setTimeout(180_000);
    const token = uniqueToken(testInfo);
    const aggregate = await ensureApplicationInfrastructureAggregate(
      page,
      disposableApplication.id,
      disposableApplication.appInformationSystemDbId,
    );
    const environment = await ensureEnvironment(page, cleanupRegistry, token);
    const environmentLabel = localizedEnvironmentName(environment);

    await page.goto(infrastructureRoute(disposableApplication.id));
    await waitForInfrastructureSection(page);
    const servers = infrastructurePanel(page, 'Servidors');
    const databases = infrastructurePanel(page, 'Bases de dades');

    await showFilters(servers, SERVER_FILTERS);
    await selectOption(page, 'application-servers-filter-environment', environmentLabel);
    await page.locator('#application-servers-filter-instance').fill(`instance-${token}`);
    await expect(
      page.getByText(/El filtre «Instància» encara no està suportat pel servidor/),
    ).toBeVisible({ timeout: 5_000 });
    await expectNoApiRequest(
      page,
      `${APPLICATION_INFRASTRUCTURE_API.systems}/${aggregate.id}`,
      () => servers.getByRole('button', { name: 'Cerca' }).click(),
    );

    await showFilters(databases, DATABASE_FILTERS);
    await selectOption(page, 'application-databases-filter-environment', environmentLabel);
    await page.locator('#application-databases-filter-service').fill(`service-${token}`);
    await expect(
      page.getByText(/El filtre «Servei» encara no està suportat pel servidor/),
    ).toBeVisible({ timeout: 5_000 });
    await expectNoApiRequest(
      page,
      `${APPLICATION_INFRASTRUCTURE_API.databases}/${aggregate.id}`,
      () => databases.getByRole('button', { name: 'Cerca' }).click(),
    );
    const searchWarnings = page.getByText(
      /Alguns filtres encara no estan suportats pel servidor i no s.aplicaran/,
    );
    await expect(searchWarnings).toHaveCount(2);
    await expect(searchWarnings.last()).toBeVisible();
  });

  test('inactive applications keep existing server and database relations view-only', async ({
    page,
    cleanupRegistry,
    disposableApplication,
  }, testInfo) => {
    test.setTimeout(240_000);
    const token = uniqueToken(testInfo);
    const aggregate = await ensureApplicationInfrastructureAggregate(
      page,
      disposableApplication.id,
      disposableApplication.appInformationSystemDbId,
    );
    const [system, database] = await Promise.all([
      createInfrastructureSystem(page, cleanupRegistry, `${token}S`),
      createInfrastructureDatabase(page, cleanupRegistry, `${token}D`),
    ]);
    await Promise.all([
      createApplicationSystemRelation(page, cleanupRegistry, aggregate.id, system),
      createApplicationDatabaseRelation(page, cleanupRegistry, aggregate.id, database),
    ]);
    registerApplicationReactivationForCleanup(page, cleanupRegistry, disposableApplication.id);
    const deactivateResponse = await page.request.delete(
      apiUrl(page, `${APPLICATION_INFRASTRUCTURE_API.applications}/${disposableApplication.id}`),
    );
    expect(deactivateResponse.ok()).toBeTruthy();

    await page.goto(infrastructureRoute(disposableApplication.id));
    await waitForInfrastructureSection(page);
    const servers = infrastructurePanel(page, 'Servidors');
    const databases = infrastructurePanel(page, 'Bases de dades');
    await expect(page.getByRole('button', { name: EDIT_SECTION, exact: true })).toHaveCount(0);
    await expect(servers.getByRole('button', { name: 'Afegir servidor' })).toHaveCount(0);
    await expect(databases.getByRole('button', { name: 'Afegir base de dades' })).toHaveCount(0);

    const serverRow = applicationRow(servers, system.instance);
    await serverRow.focus();
    await serverRow.dblclick();
    await expect(
      page.getByRole('heading', { name: "Consultar servidor de l'aplicació" }),
    ).toBeVisible();
    await expectOnlyViewActions(page, SYSTEM_DIALOG_ACTIONS);
    await page.getByRole('button', { name: SYSTEM_DIALOG_ACTIONS.accept, exact: true }).click();
    await expectFocusReturn(serverRow);

    const databaseRow = applicationRow(databases, database.service);
    await databaseRow.focus();
    await databaseRow.press('Enter');
    await expect(
      page.getByRole('heading', { name: "Consultar base de dades de l'aplicació" }),
    ).toBeVisible();
    await expectOnlyViewActions(page, DATABASE_DIALOG_ACTIONS);
    await page.getByRole('button', { name: DATABASE_DIALOG_ACTIONS.accept, exact: true }).click();
    await expectFocusReturn(databaseRow);
  });
});

function infrastructureRoute(applicationId: number): string {
  return `/aplicacions/${applicationId}/systems-databases`;
}

async function expectNoApiRequest(
  page: Page,
  pathname: string,
  action: () => Promise<void>,
): Promise<void> {
  const requests: Request[] = [];
  const listener = (request: Request) => {
    if (request.method() === 'GET' && new URL(request.url()).pathname === pathname) {
      requests.push(request);
    }
  };
  page.on('request', listener);
  await action();
  await page.waitForTimeout(750);
  page.off('request', listener);
  expect(requests).toHaveLength(0);
}

function waitForAggregateSave(page: Page): Promise<Response> {
  return page.waitForResponse(
    (response) => {
      const url = new URL(response.url());
      return (
        ['POST', 'PUT'].includes(response.request().method()) &&
        (url.pathname === APPLICATION_INFRASTRUCTURE_API.aggregate ||
          url.pathname.startsWith(`${APPLICATION_INFRASTRUCTURE_API.aggregate}/`))
      );
    },
    { timeout: 30_000 },
  );
}

async function waitForInfrastructureSection(page: Page): Promise<void> {
  await expect(page.locator('.app-initial-loader')).toBeHidden({ timeout: 30_000 });
  await expect(page.getByRole('heading', { name: SECTION_TITLE })).toBeVisible();
  for (const panel of [
    infrastructurePanel(page, 'Servidors'),
    infrastructurePanel(page, 'Bases de dades'),
  ]) {
    await expect(panel.locator('.invai-table-loading-container')).toHaveAttribute(
      'aria-busy',
      'false',
    );
  }
}

function infrastructurePanel(page: Page, title: string): Locator {
  return page.locator('app-application-infrastructure-list').filter({
    has: page.getByRole('heading', { name: title, exact: true }),
  });
}

function applicationRow(panel: Locator, value: string): Locator {
  return panel.locator('.invai-table-consultable-row').filter({ hasText: value });
}

function relationDialog(page: Page, _selector: string): Locator {
  return page.locator('.p-dialog:visible');
}

async function selectCatalogRow(dialog: Locator, text: string): Promise<void> {
  for (let pageIndex = 0; pageIndex < 100; pageIndex += 1) {
    const row = dialog.locator('tbody tr').filter({ hasText: text });
    if ((await row.count()) > 0) {
      await row.click();
      await expect(row.locator('input[type="radio"]')).toBeChecked();
      return;
    }

    const next = dialog.locator('.p-paginator-next');
    if ((await next.count()) === 0 || (await next.isDisabled())) break;
    await next.click();
    await expect(dialog.locator('.invai-table-loading-container')).toHaveAttribute(
      'aria-busy',
      'false',
    );
    await dialog.page().waitForTimeout(100);
  }
  throw new Error(`Catalog row containing "${text}" was not found`);
}

async function showFilters(panel: Locator, toggleName: string): Promise<void> {
  const search = panel.getByRole('button', { name: 'Cerca' });
  if (!(await search.isVisible())) {
    await panel.getByRole('button', { name: toggleName, exact: true }).click();
  }
  await expect(search).toBeVisible();
}

async function selectSystemFilterOption(
  page: Page,
  system: InfrastructureSystemRecord,
): Promise<void> {
  await page.locator('#application-servers-filter-server').click();
  const overlay = page.locator('.p-select-overlay:visible');
  const filter = overlay.locator('input.p-select-filter');
  await filter.fill(system.server.name);
  const instanceOption = overlay.getByRole('option').filter({ hasText: system.instance });
  if ((await instanceOption.count()) > 0) {
    await instanceOption.first().click();
    return;
  }
  await overlay.getByRole('option', { name: system.server.name, exact: true }).click();
}

async function selectPageSize(page: Page, panel: Locator, pageSize: string): Promise<void> {
  await panel.locator('.p-paginator-rpp-dropdown').click();
  await page.getByRole('option', { name: pageSize, exact: true }).click();
}

async function expectOnlyViewActions(
  page: Page,
  actions: { accept: string; edit: string; deactivate: string; save: string },
): Promise<void> {
  await expect(page.getByRole('button', { name: actions.accept, exact: true })).toBeVisible();
  await expect(page.getByRole('button', { name: actions.edit, exact: true })).toHaveCount(0);
  await expect(page.getByRole('button', { name: actions.deactivate, exact: true })).toHaveCount(0);
  await expect(page.getByRole('button', { name: actions.save, exact: true })).toHaveCount(0);
  await expect(page.locator('.p-dialog:visible input[type="radio"]')).toHaveCount(0);
}

async function expectFocusReturn(row: Locator): Promise<void> {
  await expect(row).toBeFocused();
}

function isInfrastructureInitialRequest(pathname: string, aggregateId: number): boolean {
  return [
    `${APPLICATION_INFRASTRUCTURE_API.aggregate}/${aggregateId}`,
    `${APPLICATION_INFRASTRUCTURE_API.systems}/${aggregateId}`,
    `${APPLICATION_INFRASTRUCTURE_API.databases}/${aggregateId}`,
    SYSTEMS_API.systems,
    SYSTEMS_API.databases,
    SYSTEMS_API.environments,
  ].includes(pathname);
}

function duplicateUrls(urls: string[]): string[] {
  const counts = new Map<string, number>();
  urls.forEach((url) => counts.set(url, (counts.get(url) ?? 0) + 1));
  return [...counts.entries()].filter(([, count]) => count > 1).map(([url]) => url);
}

function countPath(urls: string[], pathname: string): number {
  return urls.filter((url) => new URL(url).pathname === pathname).length;
}

function hasQuery(url: URL, expected: Record<string, string | number>): boolean {
  return Object.entries(expected).every(
    ([key, value]) => url.searchParams.get(key) === String(value),
  );
}

function localizedEnvironmentName(environment: EnvironmentRecord): string {
  return environment.name ?? environment.code ?? `#${environment.id}`;
}
