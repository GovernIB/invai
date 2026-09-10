import { expect, test } from './support/auth.fixture';
import type { Locator, Page, TestInfo } from '@playwright/test';

import { CleanupRegistry } from './support/cleanup-registry';
import { uniqueToken } from './support/data';
import {
  apiUrl,
  expectApiJsonResponse,
  expectJsonResponse,
  expectSuccessfulResponse,
  waitForApiResponse as waitForMaintenanceResponse,
} from './support/http';
import {
  ensurePhysicalServer,
  type InfrastructureSystemRecord,
  registerSoftDeleteCleanup,
  SYSTEMS_API,
} from './support/systems';
import { selectOption, selectRowAction } from './support/ui';

const ACTIVE_STATUS_ID = 1;
const INACTIVE_STATUS_ID = 2;
const INACTIVE_ROW_ACTIONS_BUTTON = 'Obrir les accions del registre inactiu';
const VIEW_ACTION = 'Consulta';
const EDIT_ACTION = 'Edita';
const DELETE_ACTION = 'Elimina';

type MaintenanceInput = Record<string, string>;

interface MaintenanceResponse {
  id: number;
  deletedAt?: string | null;
  [key: string]: unknown;
}

interface MaintenancePageResponse {
  content: MaintenanceResponse[];
  totalElements: number;
  totalPages?: number;
}

interface MaintenanceTestData {
  initial: MaintenanceInput;
  updated: MaintenanceInput;
}

interface RoleTransferRequest {
  items: { id: number; type: 'RESPONSIBLE' | 'AUTHORIZED' }[];
  toPersonId: number | null;
  revoke: boolean;
}

interface RoleAssignmentResponse {
  id: number;
  type: 'RESPONSIBLE' | 'AUTHORIZED';
  applicationId: number;
  applicationName: string;
  responsibleType: { id: number; name: string; nameEs: string } | null;
  authorizationTypes: Array<{ id: number; name: string; nameEs: string }> | null;
}

interface QuickSearchConfig {
  ariaLabel: string;
  queryParam: string;
}

interface MaintenanceScenario {
  key: string;
  title: string;
  route: string;
  apiPath: string;
  addButton: string;
  addDialogTitle: string;
  addSubmitButton: string;
  viewDialogTitle: string;
  closeViewButton: string;
  editDialogTitle: string;
  saveButton: string;
  confirmDeleteButton: string;
  uniqueField: string;
  quickSearch?: QuickSearchConfig;
  initialStatusId?: number;
  deleteMode?: 'environment-inactive' | 'api-inactive' | 'soft-delete-marker';
  createData: (token: string) => MaintenanceTestData;
  fillDialog: (page: Page, input: MaintenanceInput) => Promise<void>;
  dialogValues: (input: MaintenanceInput) => Record<string, string>;
}

const SCENARIOS: MaintenanceScenario[] = [
  simpleScenario({
    key: 'category',
    title: 'Categories',
    route: '/manteniments/categories',
    apiPath: '/invaiapi/interna/category',
    prefix: 'CAT',
    addButton: 'Afegeix una categoria',
    addDialogTitle: 'Afegir categoria',
    addSubmitButton: 'Afegeix la categoria',
    viewDialogTitle: 'Consultar categoria',
    closeViewButton: 'Tanca el formulari de la categoria',
    editDialogTitle: 'Editar categoria',
    saveButton: 'Desa els canvis de la categoria',
    confirmDeleteButton: "Confirma l'eliminació de la categoria",
    inputPrefix: 'category-dialog',
    deleteMode: 'soft-delete-marker',
    quickSearch: {
      ariaLabel: 'Cerca ràpida de categories',
      queryParam: 'quickSearch',
    },
  }),
  simpleScenario({
    key: 'system-type',
    title: "Sistemes d'informació",
    route: '/manteniments/sistemes-informacio',
    apiPath: '/invaiapi/interna/system-type',
    prefix: 'SYS',
    addButton: "Afegeix un sistema d'informació",
    addDialogTitle: "Afegir sistema d'informació",
    addSubmitButton: "Afegeix el sistema d'informació",
    viewDialogTitle: "Consultar sistema d'informació",
    closeViewButton: "Tanca el formulari del sistema d'informació",
    editDialogTitle: "Editar sistema d'informació",
    saveButton: "Desa els canvis del sistema d'informació",
    confirmDeleteButton: "Confirma l'eliminació del sistema d'informació",
    inputPrefix: 'system-type-dialog',
    deleteMode: 'soft-delete-marker',
  }),
  {
    key: 'environment',
    title: 'Entorns',
    route: '/manteniments/sistemes#environments',
    apiPath: '/invaiapi/interna/environment',
    addButton: 'Afegeix un entorn',
    addDialogTitle: 'Afegir entorn',
    addSubmitButton: "Afegeix l'entorn",
    viewDialogTitle: 'Consultar entorn',
    closeViewButton: "Tanca el formulari de l'entorn",
    editDialogTitle: 'Editar entorn',
    saveButton: "Desa els canvis de l'entorn",
    confirmDeleteButton: "Confirma l'eliminació de l'entorn",
    uniqueField: 'code',
    quickSearch: {
      ariaLabel: "Cerca ràpida d'entorns",
      queryParam: 'search',
    },
    initialStatusId: ACTIVE_STATUS_ID,
    deleteMode: 'environment-inactive',
    createData: (token) => ({
      initial: {
        code: `E2E${token}`,
        name: `E2E Entorn ${token}`,
        nameEs: `E2E Entorno ${token}`,
      },
      updated: {
        code: `E2E${token}`,
        name: `E2E Entorn ${token} actualitzat`,
        nameEs: `E2E Entorno ${token} actualizado`,
      },
    }),
    fillDialog: async (page, input) => {
      await page.locator('#environment-dialog-code').fill(input.code);
      await page.locator('#environment-dialog-name').fill(input.name);
      await page.locator('#environment-dialog-name-es').fill(input.nameEs);
    },
    dialogValues: (input) => ({
      'environment-dialog-code': input.code,
      'environment-dialog-name': input.name,
      'environment-dialog-name-es': input.nameEs,
    }),
  },
  simpleScenario({
    key: 'field',
    title: 'Àmbits',
    route: '/manteniments/ambits',
    apiPath: '/invaiapi/interna/field',
    prefix: 'AMB',
    addButton: 'Afegeix un àmbit',
    addDialogTitle: 'Afegir àmbit',
    addSubmitButton: "Afegeix l'àmbit",
    viewDialogTitle: 'Consultar àmbit',
    closeViewButton: "Tanca el formulari de l'àmbit",
    editDialogTitle: 'Editar àmbit',
    saveButton: "Desa els canvis de l'àmbit",
    confirmDeleteButton: "Confirma l'eliminació de l'àmbit",
    inputPrefix: 'field-dialog',
    deleteMode: 'soft-delete-marker',
  }),
  {
    key: 'commission',
    title: 'Comissió informàtica',
    route: '/manteniments/comissio-informatica',
    apiPath: '/invaiapi/interna/commission',
    addButton: 'Afegeix una comissió informàtica',
    addDialogTitle: 'Afegir comissió informàtica',
    addSubmitButton: 'Afegeix la comissió informàtica',
    viewDialogTitle: 'Consultar comissió informàtica',
    closeViewButton: 'Tanca el formulari de la comissió informàtica',
    editDialogTitle: 'Editar comissió informàtica',
    saveButton: 'Desa els canvis de la comissió informàtica',
    confirmDeleteButton: "Confirma l'eliminació de la comissió informàtica",
    uniqueField: 'expedientNumber',
    quickSearch: {
      ariaLabel: 'Cerca ràpida de comissions',
      queryParam: 'quickSearch',
    },
    deleteMode: 'api-inactive',
    createData: (token) => ({
      initial: {
        name: `E2E Comissió ${token}`,
        nameEs: `E2E Comisión ${token}`,
        expedientNumber: `E2E-${token}`,
        approvalDate: '2026-07-21',
        commissionType: 'TECNICA',
      },
      updated: {
        name: `E2E Comissió ${token} actualitzada`,
        nameEs: `E2E Comisión ${token} actualizada`,
        expedientNumber: `E2E-${token}`,
        approvalDate: '2026-07-22',
        commissionType: 'TECNICA',
      },
    }),
    fillDialog: fillCommissionDialog,
    dialogValues: (input) => ({
      'commission-dialog-name': input.name,
      'commission-dialog-name-es': input.nameEs,
      'commission-dialog-expedient-number': input.expedientNumber,
      'commission-dialog-approval-date': displayDate(input.approvalDate),
    }),
  },
];

test.describe('maintenance section navigation', () => {
  test('groups maintenance lists into the application detail sections', async ({ page }) => {
    await page.goto('/manteniments/general');

    const tabs = page.locator('.maintenances-tabs');
    await expect(tabs.getByRole('link')).toHaveText([
      'General',
      'Responsables',
      'Sistemes',
      'Desenvolupament',
    ]);
    await expect(tabs.getByRole('link', { name: 'General' })).toHaveAttribute(
      'aria-current',
      'page',
    );

    const categories = page.getByRole('button', { name: /^Categories/ });
    const systemTypes = page.getByRole('button', { name: /^Sistemes d'informació/ });
    await expect(categories).toHaveAttribute('aria-expanded', 'false');
    await expect(systemTypes).toHaveAttribute('aria-expanded', 'false');

    await categories.click();
    await expect(page).toHaveURL(/\/manteniments\/general#categories$/);
    await expect(categories).toHaveAttribute('aria-expanded', 'true');

    await systemTypes.click();
    await expect(page).toHaveURL(/\/manteniments\/general#system-types$/);
    await expect(systemTypes).toHaveAttribute('aria-expanded', 'true');
    await expect(categories).toHaveAttribute('aria-expanded', 'false');

    await tabs.getByRole('link', { name: 'Responsables' }).click();
    await expect(page).toHaveURL(/\/manteniments\/responsables$/);

    const companies = page.getByRole('button', { name: /^Empreses/ });
    const people = page.getByRole('button', { name: /^Persones/ });
    const authorizations = page.getByRole('button', { name: /^Autoritzacions/ });
    const roleTransfer = page.getByRole('button', {
      name: /^Transferència i revocació de rols/,
    });
    await expect(page.locator('.maintenance-panel-title')).toHaveText([
      'Transferència i revocació de rols',
      'Empreses',
      'Persones',
      'Autoritzacions',
    ]);
    await expect(companies).toHaveAttribute('aria-expanded', 'false');
    await expect(people).toHaveAttribute('aria-expanded', 'false');
    await expect(authorizations).toHaveAttribute('aria-expanded', 'false');
    await expect(roleTransfer).toHaveAttribute('aria-expanded', 'false');

    await companies.click();
    await expect(page).toHaveURL(/\/manteniments\/responsables#companies$/);
    await expect(companies).toHaveAttribute('aria-expanded', 'true');
    const companiesPanel = page.locator('p-accordion-panel').filter({ has: companies });
    await expect(companiesPanel.locator('.p-datatable-table-container')).toBeVisible();

    await people.click();
    await expect(page).toHaveURL(/\/manteniments\/responsables#people$/);
    await expect(people).toHaveAttribute('aria-expanded', 'true');
    await expect(companies).toHaveAttribute('aria-expanded', 'false');
  });

  test('prepares and confirms a controlled role transfer without mutating test data', async ({
    page,
  }) => {
    let postedBatch: RoleTransferRequest | null = null;
    const people = [
      {
        id: 901,
        firstName: 'E2E Persona',
        lastName: 'origen',
        email: 'origen@example.org',
        personalCaib: false,
        company: null,
        deletedAt: null,
      },
      {
        id: 902,
        firstName: 'E2E Persona',
        lastName: 'destí',
        email: 'desti@example.org',
        personalCaib: false,
        company: null,
        deletedAt: null,
      },
    ];

    await page.route('**/invaiapi/interna/person*', (route) =>
      route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ content: people, totalElements: people.length }),
      }),
    );
    await page.route('**/invaiapi/interna/role-transfer**', async (route) => {
      if (route.request().method() === 'POST') {
        postedBatch = route.request().postDataJSON() as RoleTransferRequest;
        await route.fulfill({ status: 204 });
        return;
      }
      const assignments: RoleAssignmentResponse[] = route.request().url().endsWith('/901')
        ? [
            {
              id: 7001,
              type: 'RESPONSIBLE',
              applicationId: 8001,
              applicationName: 'E2E Aplicació',
              responsibleType: {
                id: 8002,
                name: 'Responsable de servei',
                nameEs: 'Responsable de servicio',
              },
              authorizationTypes: null,
            },
          ]
        : [];
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(assignments),
      });
    });

    await page.goto('/manteniments/responsables#role-transfer');
    await expect(page.locator('.app-initial-loader')).toBeHidden({ timeout: 30_000 });
    await selectOption(page, 'role-transfer-source', 'E2E Persona origen');
    await expect(page.getByRole('heading', { name: 'E2E Aplicació' })).toBeVisible();
    await page.getByRole('checkbox', { name: /Responsable de servei/ }).check();
    await page.getByRole('button', { name: 'Mou els rols seleccionats al lot' }).click();
    await selectOption(page, 'role-transfer-destination', 'E2E Persona destí');
    await page.getByRole('button', { name: 'Transfereix els rols preparats' }).click();
    await page.getByRole('button', { name: "Confirma l'acció" }).click();

    await expect
      .poll(() => postedBatch)
      .toEqual({
        items: [{ id: 7001, type: 'RESPONSIBLE' }],
        toPersonId: 902,
        revoke: false,
      });
    await expect(
      page.locator('.role-transfer-status').getByText("Els rols s'han transferit correctament."),
    ).toBeVisible();
  });

  test('keeps wide maintenance tables inside the expanded panel', async ({ page }) => {
    await page.setViewportSize({ width: 1280, height: 800 });
    await page.goto('/manteniments/general#commissions');
    await expect(page.locator('.app-initial-loader')).toBeHidden({ timeout: 30_000 });

    const commissionsHeader = page.getByRole('button', {
      name: /^Comissió informàtica/,
    });
    const commissionsPanel = page.locator('p-accordion-panel').filter({
      has: commissionsHeader,
    });
    const tableContainer = commissionsPanel.locator('.p-datatable-table-container');
    const paginator = commissionsPanel.locator('.p-paginator');

    await expect(commissionsHeader).toHaveAttribute('aria-expanded', 'true');
    await expect(tableContainer).toBeVisible();

    const mainLayoutWidths = await page.locator('app-main-layout main').evaluate((element) => ({
      clientWidth: element.clientWidth,
      scrollWidth: element.scrollWidth,
    }));
    const tableWidths = await tableContainer.evaluate((element) => ({
      clientWidth: element.clientWidth,
      scrollWidth: element.scrollWidth,
    }));
    const panelBox = await commissionsPanel.boundingBox();
    const tableBox = await tableContainer.boundingBox();
    const paginatorBox = await paginator.boundingBox();

    expect(mainLayoutWidths.scrollWidth).toBeLessThanOrEqual(mainLayoutWidths.clientWidth + 1);
    expect(tableWidths.scrollWidth).toBeGreaterThan(tableWidths.clientWidth);
    expect(panelBox).not.toBeNull();
    expect(tableBox).not.toBeNull();
    expect(paginatorBox).not.toBeNull();
    expect(tableBox!.x + tableBox!.width).toBeLessThanOrEqual(panelBox!.x + panelBox!.width + 1);
    expect(paginatorBox!.x + paginatorBox!.width).toBeLessThanOrEqual(
      panelBox!.x + panelBox!.width + 1,
    );
  });
});

test.describe('systems maintenance navigation', () => {
  test('groups system resources into expansion panels', async ({ page }) => {
    await page.setViewportSize({ width: 1280, height: 800 });
    await page.goto('/manteniments/sistemes');

    const databases = page.getByRole('button', { name: /^Bases de dades/ });
    const servers = page.getByRole('button', { name: /^Servidors d'aplicacions/ });
    const environments = page.getByRole('button', { name: /^Entorns/ });
    await expect(
      page.locator('.maintenances-tabs').getByRole('link', { name: 'Sistemes' }),
    ).toHaveAttribute('aria-current', 'page');

    await expect(page.locator('.maintenance-panel-title')).toHaveText([
      'Entorns',
      'Servidors físics',
      "Servidors d'aplicacions",
      'Servidors de bases de dades',
      'Bases de dades',
      'Proveïdors de bases de dades',
    ]);

    await expect(databases).toHaveAttribute('aria-expanded', 'false');
    await expect(servers).toHaveAttribute('aria-expanded', 'false');
    await expect(environments).toHaveAttribute('aria-expanded', 'false');

    await databases.click();
    await expect(page).toHaveURL(/\/manteniments\/sistemes#databases$/);
    await expect(databases).toHaveAttribute('aria-expanded', 'true');

    const databasesPanel = page.locator('p-accordion-panel').filter({ has: databases });
    const tableContainer = databasesPanel.locator('.p-datatable-table-container');
    await expect(tableContainer).toBeVisible();

    const mainLayoutWidths = await page.locator('app-main-layout main').evaluate((element) => ({
      clientWidth: element.clientWidth,
      scrollWidth: element.scrollWidth,
    }));
    const tableWidths = await tableContainer.evaluate((element) => ({
      clientWidth: element.clientWidth,
      scrollWidth: element.scrollWidth,
    }));

    expect(mainLayoutWidths.scrollWidth).toBeLessThanOrEqual(mainLayoutWidths.clientWidth + 1);
    expect(tableWidths.scrollWidth).toBeGreaterThan(tableWidths.clientWidth);

    await environments.click();
    await expect(page).toHaveURL(/\/manteniments\/sistemes#environments$/);
    await expect(environments).toHaveAttribute('aria-expanded', 'true');
    await expect(databases).toHaveAttribute('aria-expanded', 'false');
  });

  test('redirects previous systems and maintenance URLs to their panels', async ({ page }) => {
    await page.goto('/sistemes/bases-de-dades');
    await expect(page).toHaveURL(/\/manteniments\/sistemes#databases$/);
    await expect(page.getByRole('button', { name: /^Bases de dades/ })).toHaveAttribute(
      'aria-expanded',
      'true',
    );

    await page.goto('/manteniments/entorns?view=compact');
    await expect(page).toHaveURL(/\/manteniments\/sistemes\?view=compact#environments$/);
    await expect(page.getByRole('button', { name: /^Entorns/ })).toHaveAttribute(
      'aria-expanded',
      'true',
    );
  });

  test('manages a flattened application server through the CRUD dialog', async ({
    page,
  }, testInfo) => {
    test.setTimeout(180_000);
    const cleanup = new CleanupRegistry(testInfo);
    const token = uniqueToken(testInfo);
    const instanceName = `E2E_TOMCAT_${token}`;
    const updatedInstanceName = `${instanceName}_UPDATED`;
    const cleanupTarget: {
      id?: number;
      matches: (item: InfrastructureSystemRecord) => boolean;
    } = {
      matches: (item) => item.instance === instanceName || item.instance === updatedInstanceName,
    };

    try {
      await page.goto('/manteniments/sistemes');
      await expect(page.locator('.app-initial-loader')).toBeHidden({ timeout: 30_000 });
      const physicalServer = await ensurePhysicalServer(page, cleanup, token, 'APPLICATION');
      const environmentLabel =
        physicalServer.environment.name ??
        physicalServer.environment.code ??
        `#${physicalServer.environment.id}`;
      const serverOptionLabel = `${physicalServer.name} · ${environmentLabel}`;

      registerSoftDeleteCleanup<InfrastructureSystemRecord>({
        page,
        cleanup,
        label: `application server ${token}`,
        pathname: SYSTEMS_API.systems,
        target: cleanupTarget,
        findParams: { statusId: ACTIVE_STATUS_ID, instance: token },
      });

      await page.reload();
      await expect(page.locator('.app-initial-loader')).toBeHidden({ timeout: 30_000 });
      const servers = page.getByRole('button', { name: /^Servidors d'aplicacions/ });
      await servers.click();
      const panel = page.locator('p-accordion-panel').filter({ has: servers });

      await expect(panel.getByRole('tab')).toHaveCount(0);
      await panel.getByRole('button', { name: "Afegir servidor d'aplicacions" }).click();
      await expect(
        page.getByRole('heading', { name: "Afegir servidor d'aplicacions" }),
      ).toBeVisible();
      await selectOption(page, 'application-host-server', serverOptionLabel);
      await page.locator('#application-host-instance').fill(instanceName);
      await page.locator('#application-host-port').fill('8090');
      await page.locator('#application-host-version').fill('11');

      const createPromise = waitForMaintenanceResponse(page, 'POST', SYSTEMS_API.systems);
      await page
        .getByRole('button', { name: "Afegeix el servidor d'aplicacions", exact: true })
        .click();
      const createResponse = await createPromise;
      expect(createResponse.request().postDataJSON()).toEqual({
        name: physicalServer.name,
        serverId: physicalServer.id,
        instance: instanceName,
        port: 8090,
        version: '11',
        description: null,
      });
      const created = await expectJsonResponse<InfrastructureSystemRecord>(createResponse);
      cleanupTarget.id = created.id;
      expect(created).toMatchObject({
        id: expect.any(Number),
        server: { id: physicalServer.id },
        instance: instanceName,
        port: 8090,
        version: '11',
        deletedAt: null,
      });

      await expect(
        page.getByText("El servidor d'aplicacions s'ha afegit correctament.").last(),
      ).toBeVisible();
      let row = panel.getByRole('row').filter({ hasText: instanceName });
      await expect(row).toBeVisible();
      await expect(row).toContainText(physicalServer.name);
      await expect(row).toContainText(environmentLabel);

      await selectRowAction(page, row, VIEW_ACTION);
      await expect(
        page.getByRole('heading', { name: "Consultar servidor d'aplicacions" }),
      ).toBeVisible();
      await expect(page.locator('#application-host-server')).toBeDisabled();
      await page
        .getByRole('button', {
          name: "Tanca el formulari del servidor d'aplicacions",
          exact: true,
        })
        .click();

      row = panel.getByRole('row').filter({ hasText: instanceName });
      await selectRowAction(page, row, EDIT_ACTION);
      await expect(
        page.getByRole('heading', { name: "Editar servidor d'aplicacions" }),
      ).toBeVisible();
      await page.locator('#application-host-instance').fill(updatedInstanceName);
      const updatePromise = waitForMaintenanceResponse(
        page,
        'PUT',
        `${SYSTEMS_API.systems}/${cleanupTarget.id}`,
      );
      await page
        .getByRole('button', {
          name: "Desa els canvis del servidor d'aplicacions",
          exact: true,
        })
        .click();
      const updateResponse = await updatePromise;
      expect(updateResponse.request().postDataJSON()).toEqual({
        name: physicalServer.name,
        serverId: physicalServer.id,
        instance: updatedInstanceName,
        port: 8090,
        version: '11',
        description: null,
      });
      await expectJsonResponse<InfrastructureSystemRecord>(updateResponse);
      await expect(panel.getByRole('row').filter({ hasText: updatedInstanceName })).toBeVisible();

      row = panel.getByRole('row').filter({ hasText: updatedInstanceName });
      await selectRowAction(page, row, DELETE_ACTION);
      const deletePromise = waitForMaintenanceResponse(
        page,
        'DELETE',
        `${SYSTEMS_API.systems}/${cleanupTarget.id}`,
      );
      await page.getByRole('button', { name: "Confirma l'acció" }).click();
      await expectSuccessfulResponse(await deletePromise);

      await expect(row).toHaveCount(0);
      await expect(
        page.getByText("El servidor d'aplicacions s'ha donat de baixa correctament.").last(),
      ).toBeVisible();

      await panel.getByRole('button', { name: 'Mostra o amaga els filtres' }).click();
      await page.locator('#systems-filter-instance').fill(updatedInstanceName);
      await selectOption(page, 'systems-filter-status', 'Inactiu');
      const inactivePromise = waitForMaintenanceResponse(
        page,
        'GET',
        SYSTEMS_API.systems,
        (url) =>
          url.searchParams.get('statusId') === String(INACTIVE_STATUS_ID) &&
          url.searchParams.get('instance') === updatedInstanceName,
      );
      await panel.getByRole('button', { name: 'Cerca' }).click();
      await expectSuccessfulResponse(await inactivePromise);
      row = panel.getByRole('row').filter({ hasText: updatedInstanceName });
      await expect(row).toBeVisible();

      const restorePromise = waitForMaintenanceResponse(
        page,
        'PUT',
        `${SYSTEMS_API.systems}/reactivate/${cleanupTarget.id}`,
      );
      const activeRefreshPromise = waitForMaintenanceResponse(
        page,
        'GET',
        SYSTEMS_API.systems,
        (url) => url.searchParams.get('statusId') === String(ACTIVE_STATUS_ID),
      );
      await selectRowAction(page, row, 'Restaura', INACTIVE_ROW_ACTIONS_BUTTON);
      const restoreResponse = await restorePromise;
      expect(restoreResponse.request().postDataJSON()).toBeNull();
      expect(await expectJsonResponse<InfrastructureSystemRecord>(restoreResponse)).toMatchObject({
        id: cleanupTarget.id,
        deletedAt: null,
      });
      await expectSuccessfulResponse(await activeRefreshPromise);
      await expect(panel.getByRole('row').filter({ hasText: updatedInstanceName })).toBeVisible();
    } finally {
      await cleanup.runAll();
    }
  });
});

test.describe('maintenance API lifecycles through the UI', () => {
  for (const scenario of SCENARIOS) {
    test(`${scenario.key}: list, create, view, update and delete`, async ({ page }, testInfo) => {
      test.setTimeout(180_000);

      const token = uniqueToken(testInfo);
      const data = scenario.createData(token);
      const lookupValue = data.initial[scenario.uniqueField];
      const updatedLookupValue = data.updated[scenario.uniqueField];

      let createdId: number | undefined;
      let creationAttempted = false;
      let deleted = false;

      try {
        await test.step('GET loads the initial maintenance page', async () => {
          const responsePromise = waitForMaintenanceResponse(page, 'GET', scenario.apiPath, (url) =>
            matchesInitialList(url, scenario),
          );

          await page.goto(scenario.route);

          const body = await expectJsonResponse<MaintenancePageResponse>(await responsePromise);
          expect(Array.isArray(body.content)).toBeTruthy();
          expect(body.totalElements).toEqual(expect.any(Number));
          await expect(
            page.getByRole('region', { name: scenario.title, exact: true }),
          ).toBeVisible();
        });

        await test.step('POST creates the maintenance record', async () => {
          await page.getByRole('button', { name: scenario.addButton }).click();
          await expect(page.getByRole('heading', { name: scenario.addDialogTitle })).toBeVisible();
          await scenario.fillDialog(page, data.initial);

          const responsePromise = waitForMaintenanceResponse(page, 'POST', scenario.apiPath);
          const refreshPromise = waitForMaintenanceResponse(page, 'GET', scenario.apiPath, (url) =>
            matchesInitialList(url, scenario),
          );
          creationAttempted = true;
          await page.getByRole('button', { name: scenario.addSubmitButton }).click();

          const response = await responsePromise;
          expect(response.request().postDataJSON()).toEqual(data.initial);
          const created = await expectJsonResponse<MaintenanceResponse>(response);
          createdId = created.id;
          expect(created).toMatchObject({ id: expect.any(Number), ...data.initial });
          await expectSuccessfulResponse(await refreshPromise);
        });

        await test.step('GET finds and opens the created record in view mode', async () => {
          const row = await findMaintenanceRow(page, scenario, lookupValue, createdId!);
          const responsePromise = waitForMaintenanceResponse(
            page,
            'GET',
            `${scenario.apiPath}/${createdId}`,
          );
          await selectRowAction(page, row, VIEW_ACTION);

          const detail = await expectJsonResponse<MaintenanceResponse>(await responsePromise);
          expect(detail).toMatchObject({ id: createdId, ...data.initial });
          await expect(page.getByRole('heading', { name: scenario.viewDialogTitle })).toBeVisible();
          await expectDialogValues(page, scenario, data.initial, true);
          await page.getByRole('button', { name: scenario.closeViewButton }).click();
          await expect(page.getByRole('heading', { name: scenario.viewDialogTitle })).toBeHidden();
        });

        await test.step('PUT updates the maintenance record', async () => {
          const row = await findMaintenanceRow(page, scenario, lookupValue, createdId!);
          const detailResponsePromise = waitForMaintenanceResponse(
            page,
            'GET',
            `${scenario.apiPath}/${createdId}`,
          );
          await selectRowAction(page, row, EDIT_ACTION);
          await expectJsonResponse<MaintenanceResponse>(await detailResponsePromise);
          await expect(page.getByRole('heading', { name: scenario.editDialogTitle })).toBeVisible();
          await scenario.fillDialog(page, data.updated);

          const responsePromise = waitForMaintenanceResponse(
            page,
            'PUT',
            `${scenario.apiPath}/${createdId}`,
          );
          const refreshPromise = waitForMaintenanceResponse(page, 'GET', scenario.apiPath, (url) =>
            matchesInitialList(url, scenario),
          );
          await page.getByRole('button', { name: scenario.saveButton }).click();

          const response = await responsePromise;
          expect(response.request().postDataJSON()).toEqual(data.updated);
          const updated = await expectJsonResponse<MaintenanceResponse>(response);
          expect(updated).toMatchObject({ id: createdId, ...data.updated });
          await expectSuccessfulResponse(await refreshPromise);

          const updatedRow = await findMaintenanceRow(
            page,
            scenario,
            updatedLookupValue,
            createdId!,
          );
          await expect(updatedRow).toContainText(data.updated.name ?? lookupValue);
        });

        await test.step('DELETE removes or deactivates the maintenance record', async () => {
          const row = await findMaintenanceRow(page, scenario, updatedLookupValue, createdId!);
          await selectRowAction(page, row, DELETE_ACTION);

          const deleteResponsePromise = waitForMaintenanceResponse(
            page,
            'DELETE',
            `${scenario.apiPath}/${createdId}`,
          );
          const refreshPromise = waitForMaintenanceResponse(page, 'GET', scenario.apiPath, (url) =>
            matchesInitialList(url, scenario),
          );
          await page.getByRole('button', { name: scenario.confirmDeleteButton }).click();

          await expectSuccessfulResponse(await deleteResponsePromise);
          deleted = true;
          await expectSuccessfulResponse(await refreshPromise);

          if (scenario.deleteMode === 'environment-inactive') {
            await expectInactiveEnvironment(page, scenario, data.updated, createdId!);
          } else if (scenario.deleteMode === 'api-inactive') {
            await expectInactiveEntity(page, scenario, data.updated, createdId!);
          } else if (scenario.deleteMode === 'soft-delete-marker') {
            await expectSoftDeletedEntity(page, scenario, data.updated, createdId!);
          } else {
            const persisted = await findEntityById(page, scenario, createdId!);
            expect(persisted).toBeUndefined();
          }
        });
      } finally {
        await test.step('cleanup removes any active test record left by a failure', async () => {
          await cleanupMaintenanceRecord({
            page,
            testInfo,
            scenario,
            uniqueValue: lookupValue,
            createdId,
            creationAttempted,
            deleted,
          });
        });
      }
    });
  }
});

function simpleScenario({
  prefix,
  inputPrefix,
  ...scenario
}: Omit<MaintenanceScenario, 'createData' | 'fillDialog' | 'dialogValues' | 'uniqueField'> & {
  prefix: string;
  inputPrefix: string;
}): MaintenanceScenario {
  return {
    ...scenario,
    uniqueField: 'name',
    createData: (token) => ({
      initial: {
        name: `E2E ${prefix} ${token}`,
        nameEs: `E2E ${prefix} ES ${token}`,
      },
      updated: {
        name: `E2E ${prefix} ${token} actualitzat`,
        nameEs: `E2E ${prefix} ES ${token} actualizado`,
      },
    }),
    fillDialog: async (page, input) => {
      await page.locator(`#${inputPrefix}-name`).fill(input.name);
      await page.locator(`#${inputPrefix}-name-es`).fill(input.nameEs);
    },
    dialogValues: (input) => ({
      [`${inputPrefix}-name`]: input.name,
      [`${inputPrefix}-name-es`]: input.nameEs,
    }),
  };
}

async function fillCommissionDialog(page: Page, input: MaintenanceInput): Promise<void> {
  await page.locator('#commission-dialog-name').fill(input.name);
  await page.locator('#commission-dialog-name-es').fill(input.nameEs);
  await page.locator('#commission-dialog-expedient-number').fill(input.expedientNumber);
  const approvalDate = page.locator('#commission-dialog-approval-date');
  await approvalDate.fill('');
  await approvalDate.pressSequentially(displayDate(input.approvalDate));
  await approvalDate.press('Tab');
  await selectOption(page, 'commission-dialog-type', 'Tècnica');
}

async function expectDialogValues(
  page: Page,
  scenario: MaintenanceScenario,
  input: MaintenanceInput,
  disabled: boolean,
): Promise<void> {
  for (const [id, value] of Object.entries(scenario.dialogValues(input))) {
    const control = page.locator(`#${id}`);
    await expect(control).toHaveValue(value);
    if (disabled) await expect(control).toBeDisabled();
  }
}

async function findMaintenanceRow(
  page: Page,
  scenario: MaintenanceScenario,
  uniqueValue: string,
  id: number,
): Promise<Locator> {
  if (scenario.quickSearch) {
    const currentRow = maintenanceRow(page, scenario, uniqueValue);
    if ((await currentRow.count()) > 0) {
      await expect(currentRow).toBeVisible();
      return currentRow;
    }

    const quickSearch = page.getByLabel(scenario.quickSearch.ariaLabel);
    if ((await quickSearch.inputValue()).trim() === uniqueValue) {
      return findMaintenanceRowAcrossPages(page, scenario, uniqueValue);
    }
    const responsePromise = waitForMaintenanceResponse(
      page,
      'GET',
      scenario.apiPath,
      (url) => url.searchParams.get(scenario.quickSearch!.queryParam) === uniqueValue,
    );
    await quickSearch.fill(uniqueValue);
    const body = await expectJsonResponse<MaintenancePageResponse>(await responsePromise);
    if (body.content.some((item) => item.id === id)) {
      const row = maintenanceRow(page, scenario, uniqueValue);
      await expect(row).toBeVisible();
      return row;
    }

    return findMaintenanceRowAcrossPages(page, scenario, uniqueValue);
  }

  return findMaintenanceRowAcrossPages(page, scenario, uniqueValue);
}

async function findMaintenanceRowAcrossPages(
  page: Page,
  scenario: MaintenanceScenario,
  uniqueValue: string,
): Promise<Locator> {
  for (let pageIndex = 0; pageIndex < 100; pageIndex += 1) {
    const row = maintenanceRow(page, scenario, uniqueValue);
    if ((await row.count()) > 0) {
      await expect(row).toBeVisible();
      return row;
    }

    const nextButton = maintenanceRegion(page, scenario).locator('button.p-paginator-next');
    if ((await nextButton.count()) === 0 || (await nextButton.isDisabled())) break;

    const responsePromise = waitForMaintenanceResponse(page, 'GET', scenario.apiPath);
    await nextButton.click();
    await expectSuccessfulResponse(await responsePromise);
  }

  throw new Error(`Could not find ${scenario.key} row containing ${uniqueValue}`);
}

async function expectInactiveEnvironment(
  page: Page,
  scenario: MaintenanceScenario,
  input: MaintenanceInput,
  id: number,
): Promise<void> {
  await page.getByRole('button', { name: 'Mostra o amaga els filtres' }).click();
  await page.locator('#environments-filter-code').fill(input.code);
  await selectOption(page, 'environments-filter-status', 'Inactiu');

  const responsePromise = waitForMaintenanceResponse(
    page,
    'GET',
    scenario.apiPath,
    (url) =>
      url.searchParams.get('statusId') === String(INACTIVE_STATUS_ID) &&
      url.searchParams.get('code') === input.code,
  );
  await page.getByRole('button', { name: 'Cerca' }).click();

  const body = await expectJsonResponse<MaintenancePageResponse>(await responsePromise);
  expect(body.content).toContainEqual(expect.objectContaining({ id, code: input.code }));
  const row = maintenanceRow(page, scenario, input.code);
  await expect(row).toBeVisible();
  await expect(row.getByRole('button', { name: INACTIVE_ROW_ACTIONS_BUTTON })).toBeVisible();
}

async function expectInactiveEntity(
  page: Page,
  scenario: MaintenanceScenario,
  input: MaintenanceInput,
  id: number,
): Promise<void> {
  const active = await findEntityById(page, scenario, id, ACTIVE_STATUS_ID);
  expect(active).toBeUndefined();

  const inactive = await findEntityById(page, scenario, id, INACTIVE_STATUS_ID);
  expect(inactive).toMatchObject({ id, ...input });
}

async function expectSoftDeletedEntity(
  page: Page,
  scenario: MaintenanceScenario,
  input: MaintenanceInput,
  id: number,
): Promise<void> {
  const deleted = await findEntityById(page, scenario, id);
  expect(deleted).toMatchObject({ id, ...input });
  expect(deleted?.deletedAt).toEqual(expect.any(String));
}

async function findEntityById(
  page: Page,
  scenario: MaintenanceScenario,
  id: number,
  statusId?: number,
): Promise<MaintenanceResponse | undefined> {
  return scanEntities(page, scenario, (entity) => entity.id === id, statusId);
}

async function scanEntities(
  page: Page,
  scenario: MaintenanceScenario,
  predicate: (entity: MaintenanceResponse) => boolean,
  statusId = scenario.deleteMode === 'environment-inactive' ||
  scenario.deleteMode === 'api-inactive'
    ? ACTIVE_STATUS_ID
    : undefined,
): Promise<MaintenanceResponse | undefined> {
  for (let pageIndex = 0; pageIndex < 100; pageIndex += 1) {
    const response = await page.request.get(apiUrl(page, scenario.apiPath), {
      params: {
        page: pageIndex,
        size: 100,
        ...(statusId ? { statusId } : {}),
      },
    });
    const body = await expectApiJsonResponse<MaintenancePageResponse>(
      response,
      `GET ${scenario.apiPath}`,
    );
    const entity = body.content.find(predicate);
    if (entity) return entity;

    const totalPages = body.totalPages ?? Math.ceil(body.totalElements / 100);
    if (pageIndex + 1 >= totalPages) return undefined;
  }

  return undefined;
}

async function cleanupMaintenanceRecord({
  page,
  testInfo,
  scenario,
  uniqueValue,
  createdId,
  creationAttempted,
  deleted,
}: {
  page: Page;
  testInfo: TestInfo;
  scenario: MaintenanceScenario;
  uniqueValue: string;
  createdId: number | undefined;
  creationAttempted: boolean;
  deleted: boolean;
}): Promise<void> {
  if (!creationAttempted || deleted) return;

  const id =
    createdId ??
    (await scanEntities(page, scenario, (entity) => entity[scenario.uniqueField] === uniqueValue))
      ?.id;
  if (!id) return;

  const response = await page.request.delete(apiUrl(page, `${scenario.apiPath}/${id}`));
  if (response.ok()) return;

  await testInfo.attach(`${scenario.key}-cleanup-error`, {
    body: await response.body(),
    contentType: response.headers()['content-type'] ?? 'text/plain',
  });
  expect
    .soft(response.ok(), `Cleanup DELETE ${scenario.apiPath}/${id} returned ${response.status()}`)
    .toBeTruthy();
}

function matchesInitialList(url: URL, scenario: MaintenanceScenario): boolean {
  const quickSearchParam = scenario.quickSearch?.queryParam;

  return (
    url.searchParams.get('page') === '0' &&
    url.searchParams.get('size') === '10' &&
    (!scenario.initialStatusId ||
      url.searchParams.get('statusId') === String(scenario.initialStatusId)) &&
    (!quickSearchParam || !url.searchParams.has(quickSearchParam))
  );
}

function maintenanceRegion(page: Page, scenario: MaintenanceScenario): Locator {
  return page.getByRole('region', { name: scenario.title, exact: true });
}

function maintenanceRow(page: Page, scenario: MaintenanceScenario, text: string): Locator {
  return maintenanceRegion(page, scenario).getByRole('row').filter({ hasText: text }).first();
}

function displayDate(date: string): string {
  const [year, month, day] = date.split('-');
  return `${day}/${month}/${year}`;
}
