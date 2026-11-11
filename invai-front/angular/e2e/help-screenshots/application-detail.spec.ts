import type { Page } from '@playwright/test';

import type { DisposableApplication } from '../support/application.fixture';
import {
  activeCatalogItem,
  assignAuthorized,
  assignResponsible,
  authorizationTypes,
  responsibleTypes,
} from '../support/application-responsibles';
import type { NamedRecord, PersonRecord } from '../support/application-responsibles';
import {
  createApplicationDatabaseRelation,
  createApplicationSystemRelation,
  ensureApplicationInfrastructureAggregate,
} from '../support/application-systems-databases';
import { expect, test } from '../support/auth.fixture';
import { CleanupRegistry } from '../support/cleanup-registry';
import { uniqueToken } from '../support/data';
import { apiUrl, expectApiJsonResponse } from '../support/http';
import {
  ensureDatabaseVendor,
  ensureEnvironment,
  getServerType,
  registerSoftDeleteCleanup,
  SYSTEMS_API,
} from '../support/systems';
import type {
  DatabaseRecord,
  InfrastructureSystemRecord,
  PhysicalServerRecord,
  ServerTypeCode,
} from '../support/systems';
import { maskText, openApp, saveHelpImage, uiText } from './capture';
import {
  APPLICATION_API_PATH,
  createExampleApplication,
  findCatalogs,
} from './example-applications';

// The responsibles belong to a real company; the captures show an example name instead.
const EXAMPLE_COMPANY = { ca: "Empresa d'exemple", es: 'Empresa de ejemplo' };

interface ApplicationAnchors {
  appResponsibleAuthorizedId: number | null;
  appInformationSystemDbId: number | null;
}

/** An external person with an example name, removed again after the test. */
async function createExamplePerson(
  page: Page,
  cleanup: CleanupRegistry,
  company: NamedRecord,
  firstName: string,
  token: string,
): Promise<PersonRecord> {
  const person = await expectApiJsonResponse<PersonRecord>(
    await page.request.post(apiUrl(page, '/invaiback/person'), {
      data: {
        companyId: company.id,
        firstName,
        lastName: 'Exemple',
        email: `${firstName.toLowerCase()}.${token.toLowerCase()}@example.test`,
        personalCaib: false,
      },
    }),
    'POST /invaiback/person',
  );
  registerSoftDeleteCleanup<PersonRecord>({
    page,
    cleanup,
    label: `help example person ${firstName}`,
    pathname: '/invaiback/person',
    target: { id: person.id, matches: (item) => item.id === person.id },
  });
  return person;
}

/** A physical server with an example name, removed again after the test. */
async function createExampleServer(
  page: Page,
  cleanup: CleanupRegistry,
  token: string,
  code: ServerTypeCode,
): Promise<PhysicalServerRecord> {
  const [environment, serverType] = await Promise.all([
    ensureEnvironment(page, cleanup, token),
    getServerType(page, code),
  ]);
  const name = `${code === 'APPLICATION' ? 'app' : 'bd'}-exemple-${token.toLowerCase()}`;
  const server = await expectApiJsonResponse<PhysicalServerRecord>(
    await page.request.post(apiUrl(page, SYSTEMS_API.servers), {
      data: { name, environmentId: environment.id, serverTypeId: serverType.id },
    }),
    `POST ${SYSTEMS_API.servers}`,
  );
  registerSoftDeleteCleanup<PhysicalServerRecord>({
    page,
    cleanup,
    label: `help example server ${name}`,
    pathname: SYSTEMS_API.servers,
    target: { id: server.id, matches: (item) => item.id === server.id },
  });
  return server;
}

async function createExampleSystem(
  page: Page,
  cleanup: CleanupRegistry,
  token: string,
): Promise<InfrastructureSystemRecord> {
  const server = await createExampleServer(page, cleanup, token, 'APPLICATION');
  const system = await expectApiJsonResponse<InfrastructureSystemRecord>(
    await page.request.post(apiUrl(page, SYSTEMS_API.systems), {
      data: {
        name: server.name,
        serverId: server.id,
        instance: `tramits-${token.toLowerCase()}`,
        port: 8080,
        version: '10.1',
        description: '',
      },
    }),
    `POST ${SYSTEMS_API.systems}`,
  );
  registerSoftDeleteCleanup<InfrastructureSystemRecord>({
    page,
    cleanup,
    label: `help example system ${system.id}`,
    pathname: SYSTEMS_API.systems,
    target: { id: system.id, matches: (item) => item.id === system.id },
  });
  return { ...system, server };
}

async function createExampleDatabase(
  page: Page,
  cleanup: CleanupRegistry,
  token: string,
): Promise<DatabaseRecord> {
  const [server, vendor] = await Promise.all([
    createExampleServer(page, cleanup, token, 'DATABASE'),
    ensureDatabaseVendor(page, cleanup, token),
  ]);
  const database = await expectApiJsonResponse<DatabaseRecord>(
    await page.request.post(apiUrl(page, SYSTEMS_API.databases), {
      data: {
        serverId: server.id,
        service: `EXEMPLE_${token.toUpperCase()}`,
        port: 1521,
        databaseTypeId: vendor.id,
        description: '',
      },
    }),
    `POST ${SYSTEMS_API.databases}`,
  );
  registerSoftDeleteCleanup<DatabaseRecord>({
    page,
    cleanup,
    label: `help example database ${database.id}`,
    pathname: SYSTEMS_API.databases,
    target: { id: database.id, matches: (item) => item.id === database.id },
  });
  return { ...database, server, databaseType: vendor };
}

test('the detail of an application: its tabs, General, Responsables and Sistemes i BD', async ({
  page,
}, testInfo) => {
  test.setTimeout(180_000);
  const cleanup = new CleanupRegistry(testInfo);
  try {
    const locale = await openApp(page);
    const token = uniqueToken(testInfo, 5);
    const application = await createExampleApplication(page, cleanup, {
      name: 'Portal de tràmits (exemple)',
      code: `AJ${token}`,
      catalogs: await findCatalogs(page),
    });
    const anchors = await expectApiJsonResponse<ApplicationAnchors>(
      await page.request.get(apiUrl(page, `${APPLICATION_API_PATH}/${application.id}`)),
      `GET ${APPLICATION_API_PATH}/${application.id}`,
    );

    // Responsables: one external responsible and one authorized person, both fictitious. The
    // types only CAIB staff can fill stay vacant, so no real person appears.
    const company = await activeCatalogItem(page, '/invaiback/company');
    const assignable = {
      appResponsibleAuthorizedId: anchors.appResponsibleAuthorizedId,
    } as DisposableApplication;
    const type = (await responsibleTypes(page)).find((item) => !item.requiresPersonalCaib);
    if (!type) throw new Error('Every responsible type needs CAIB staff');
    const [authorization] = await authorizationTypes(page);
    await assignResponsible(
      page,
      assignable,
      await createExamplePerson(page, cleanup, company, 'Joana', token),
      type,
    );
    await assignAuthorized(
      page,
      assignable,
      await createExamplePerson(page, cleanup, company, 'Pere', token),
      [authorization],
    );

    // Sistemes i BD: an example application server and an example database.
    const aggregate = await ensureApplicationInfrastructureAggregate(
      page,
      application.id,
      anchors.appInformationSystemDbId,
    );
    const system = await createExampleSystem(page, cleanup, token);
    const database = await createExampleDatabase(page, cleanup, token);
    await createApplicationSystemRelation(page, cleanup, aggregate.id, system);
    await createApplicationDatabaseRelation(page, cleanup, aggregate.id, database);

    // The tabs and the General tab.
    await page.goto(`/${uiText(locale, 'aplicacions')}/${application.id}/general`);
    await expect(page.locator('.app-initial-loader')).toBeHidden({ timeout: 30_000 });
    const tabs = page.getByRole('navigation', { name: uiText(locale, "Seccions de l'aplicació") });
    const general = page.locator('app-application-general-section');
    const generalTitle = general.getByRole('heading', {
      name: uiText(locale, "Dades generals de l'aplicació"),
    });
    await expect(generalTitle).toBeVisible();
    await saveHelpImage(
      page,
      page.locator('app-section-container').first(),
      locale,
      'application-detail/imgs/overview.webp',
      { cropTo: [tabs, generalTitle] },
    );
    await saveHelpImage(page, general, locale, 'application-detail/imgs/general.webp');

    // Responsables.
    await tabs.getByRole('link', { name: uiText(locale, 'Responsables') }).click();
    const responsible = page.locator('app-application-responsible-section');
    await expect(
      responsible.getByRole('cell', { name: 'Joana Exemple', exact: true }),
    ).toBeVisible();
    await expect(
      responsible.getByRole('cell', { name: 'Pere Exemple', exact: true }),
    ).toBeVisible();
    await maskText(
      responsible.getByRole('cell').filter({ hasText: company.name }),
      EXAMPLE_COMPANY[locale],
    );
    await saveHelpImage(page, responsible, locale, 'application-detail/imgs/responsible.webp');

    // Sistemes i BD.
    await tabs.getByRole('link', { name: uiText(locale, 'Sistemes i BD') }).click();
    const infrastructure = page.locator('app-application-systems-databases-section');
    await expect(
      infrastructure.getByRole('cell', { name: system.instance, exact: true }),
    ).toBeVisible();
    await expect(
      infrastructure.getByRole('cell', { name: database.service, exact: true }),
    ).toBeVisible();
    await saveHelpImage(
      page,
      infrastructure,
      locale,
      'application-detail/imgs/systems-databases.webp',
    );
  } finally {
    await cleanup.runAll();
  }
});
