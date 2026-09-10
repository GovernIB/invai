import { expect, test } from './support/auth.fixture';
import type { Locator, Page } from '@playwright/test';

import { CleanupRegistry } from './support/cleanup-registry';
import { uniqueToken } from './support/data';
import { expectJsonResponse, expectSuccessfulResponse, waitForApiResponse } from './support/http';
import {
  ACTIVE_STATUS_ID,
  type DatabaseRecord,
  type DatabaseVendorRecord,
  ensureDatabaseVendor,
  ensureEnvironment,
  ensurePhysicalServer,
  getServerType,
  INACTIVE_STATUS_ID,
  type PhysicalServerRecord,
  registerSoftDeleteCleanup,
  type ServerTypeCode,
  SYSTEMS_API,
} from './support/systems';
import { selectOption, selectRowAction } from './support/ui';

const INACTIVE_ROW_ACTIONS_BUTTON = 'Obrir les accions del registre inactiu';
const VIEW_ACTION = 'Consulta';
const EDIT_ACTION = 'Edita';
const DELETE_ACTION = 'Elimina';
const RESTORE_ACTION = 'Restaura';

interface DialogActions {
  accept: string;
  edit: string;
  cancel: string;
  deactivate: string;
  save: string;
}

const PHYSICAL_SERVER_ACTIONS: DialogActions = {
  accept: 'Accepta la consulta del servidor',
  edit: 'Edita el servidor',
  cancel: 'Cancel·la els canvis del servidor',
  deactivate: 'Dona de baixa el servidor',
  save: 'Desa els canvis del servidor',
};

const DATABASE_VENDOR_ACTIONS: DialogActions = {
  accept: 'Accepta la consulta del proveïdor',
  edit: 'Edita el proveïdor',
  cancel: 'Cancel·la els canvis del proveïdor',
  deactivate: 'Dona de baixa el proveïdor',
  save: 'Desa els canvis del proveïdor',
};

const DATABASE_ACTIONS: DialogActions = {
  accept: 'Accepta la consulta de la base de dades',
  edit: 'Edita la base de dades',
  cancel: 'Cancel·la els canvis de la base de dades',
  deactivate: 'Dona de baixa la base de dades',
  save: 'Desa els canvis de la base de dades',
};

test.describe('systems CRUD lifecycles through the UI', () => {
  for (const serverTypeCode of ['APPLICATION', 'DATABASE'] as const) {
    const copy =
      serverTypeCode === 'APPLICATION'
        ? {
            panel: 'Servidors físics',
            add: 'Afegir servidor físic',
            createTitle: 'Afegir servidor físic',
            viewTitle: 'Consultar servidor físic',
            editTitle: 'Editar servidor físic',
            prefix: 'APP',
          }
        : {
            panel: 'Servidors de bases de dades',
            add: 'Afegir servidor de base de dades',
            createTitle: 'Afegir servidor de base de dades',
            viewTitle: 'Consultar servidor de base de dades',
            editTitle: 'Editar servidor de base de dades',
            prefix: 'DB',
          };

    test(`${serverTypeCode.toLowerCase()} physical server: create, inspect, update, filter, delete and restore`, async ({
      page,
    }, testInfo) => {
      test.setTimeout(180_000);
      const cleanup = new CleanupRegistry(testInfo);
      const token = uniqueToken(testInfo);
      const initialName = `E2E ${copy.prefix} físic ${token}`;
      const updatedName = `${initialName} actualitzat`;
      const cleanupTarget: {
        id?: number;
        matches: (item: PhysicalServerRecord) => boolean;
      } = {
        matches: (item) => item.name === initialName || item.name === updatedName,
      };

      try {
        await page.goto('/manteniments/sistemes');
        await expect(page.locator('.app-initial-loader')).toBeHidden({ timeout: 30_000 });
        const [environment, serverType] = await Promise.all([
          ensureEnvironment(page, cleanup, token),
          getServerType(page, serverTypeCode),
        ]);
        const environmentLabel = localizedEnvironmentName(environment);

        registerSoftDeleteCleanup<PhysicalServerRecord>({
          page,
          cleanup,
          label: `${serverTypeCode.toLowerCase()} physical server ${token}`,
          pathname: SYSTEMS_API.servers,
          target: cleanupTarget,
          findParams: {
            statusId: ACTIVE_STATUS_ID,
            serverTypeCode,
            search: token,
          },
        });

        await page.reload();
        await expect(page.locator('.app-initial-loader')).toBeHidden({ timeout: 30_000 });
        const panel = await openSystemsPanel(page, copy.panel);

        await test.step('POST creates a physical server with its immutable type', async () => {
          await panel.getByRole('button', { name: copy.add, exact: true }).click();
          await expect(page.getByRole('heading', { name: copy.createTitle })).toBeVisible();
          await page.locator('#physical-server-name').fill(initialName);
          await selectOption(page, 'physical-server-environment', environmentLabel);

          const responsePromise = waitForApiResponse(page, 'POST', SYSTEMS_API.servers);
          await page.getByRole('button', { name: 'Afegeix el servidor', exact: true }).click();
          const response = await responsePromise;
          expect(response.request().postDataJSON()).toEqual({
            name: initialName,
            environmentId: environment.id,
            serverTypeId: serverType.id,
          });
          const created = await expectJsonResponse<PhysicalServerRecord>(response);
          cleanupTarget.id = created.id;
          expect(created).toMatchObject({
            id: expect.any(Number),
            name: initialName,
            environment: { id: environment.id },
            serverType: { id: serverType.id },
            deletedAt: null,
          });
          await expect(findRow(panel, initialName)).toBeVisible();
        });

        await test.step('Enter opens View, View to Edit can cancel, and focus returns', async () => {
          const row = findRow(panel, initialName);
          const detailPromise = waitForApiResponse(
            page,
            'GET',
            `${SYSTEMS_API.servers}/${cleanupTarget.id}`,
          );
          await row.focus();
          await row.press('Enter');
          await expectJsonResponse<PhysicalServerRecord>(await detailPromise);
          await expect(page.getByRole('heading', { name: copy.viewTitle })).toBeVisible();
          await expect(page.locator('#physical-server-name')).toHaveValue(initialName);
          await expectSelectedOption(page, 'physical-server-environment', environmentLabel);
          await expectViewActions(page, PHYSICAL_SERVER_ACTIONS);

          await page
            .getByRole('button', { name: PHYSICAL_SERVER_ACTIONS.edit, exact: true })
            .click();
          await expect(page.getByRole('heading', { name: copy.editTitle })).toBeVisible();
          await expectEditActions(page, PHYSICAL_SERVER_ACTIONS);
          await page.locator('#physical-server-name').fill('E2E canvi cancel·lat');
          await page
            .getByRole('button', { name: PHYSICAL_SERVER_ACTIONS.cancel, exact: true })
            .click();

          await expect(page.getByRole('heading', { name: copy.viewTitle })).toBeVisible();
          await expect(page.locator('#physical-server-name')).toHaveValue(initialName);
          await page
            .getByRole('button', { name: PHYSICAL_SERVER_ACTIONS.accept, exact: true })
            .click();
          await expectFocusReturn(row);
        });

        await test.step('PUT edits the record from the row action', async () => {
          let row = findRow(panel, initialName);
          const detailPromise = waitForApiResponse(
            page,
            'GET',
            `${SYSTEMS_API.servers}/${cleanupTarget.id}`,
          );
          await selectRowAction(page, row, EDIT_ACTION);
          await expectJsonResponse<PhysicalServerRecord>(await detailPromise);
          await expect(page.getByRole('heading', { name: copy.editTitle })).toBeVisible();
          await page.locator('#physical-server-name').fill(updatedName);

          const responsePromise = waitForApiResponse(
            page,
            'PUT',
            `${SYSTEMS_API.servers}/${cleanupTarget.id}`,
          );
          await page
            .getByRole('button', { name: PHYSICAL_SERVER_ACTIONS.save, exact: true })
            .click();
          const response = await responsePromise;
          expect(response.request().postDataJSON()).toEqual({
            name: updatedName,
            environmentId: environment.id,
            serverTypeId: serverType.id,
          });
          expect(await expectJsonResponse<PhysicalServerRecord>(response)).toMatchObject({
            id: cleanupTarget.id,
            name: updatedName,
            serverType: { id: serverType.id },
          });
          row = findRow(panel, updatedName);
          await expect(row).toBeVisible();
        });

        await test.step('filters send name, environment, type and active status', async () => {
          await showFilters(panel);
          await page.locator('#physical-servers-filter-name').fill(updatedName);
          await selectOption(page, 'physical-servers-filter-environment', environmentLabel);
          await selectOption(page, 'physical-servers-filter-status', 'Actiu');

          const responsePromise = waitForApiResponse(page, 'GET', SYSTEMS_API.servers, (url) =>
            hasQuery(url, {
              name: updatedName,
              environmentId: environment.id,
              serverTypeCode,
              statusId: ACTIVE_STATUS_ID,
            }),
          );
          await panel.getByRole('button', { name: 'Cerca' }).click();
          await expectSuccessfulResponse(await responsePromise);
          await expect(findRow(panel, updatedName)).toBeVisible();
        });

        await test.step('DELETE moves the record to inactive results', async () => {
          const row = findRow(panel, updatedName);
          await selectRowAction(page, row, DELETE_ACTION);
          const responsePromise = waitForApiResponse(
            page,
            'DELETE',
            `${SYSTEMS_API.servers}/${cleanupTarget.id}`,
          );
          await page.getByRole('button', { name: "Confirma l'acció", exact: true }).click();
          await expectSuccessfulResponse(await responsePromise);
          await expect(row).toHaveCount(0);

          await showFilters(panel);
          await page.locator('#physical-servers-filter-name').fill(updatedName);
          await selectOption(page, 'physical-servers-filter-status', 'Inactiu');
          const inactivePromise = waitForApiResponse(page, 'GET', SYSTEMS_API.servers, (url) =>
            hasQuery(url, {
              name: updatedName,
              statusId: INACTIVE_STATUS_ID,
              serverTypeCode,
            }),
          );
          await panel.getByRole('button', { name: 'Cerca' }).click();
          await expectSuccessfulResponse(await inactivePromise);
          await expect(findRow(panel, updatedName)).toBeVisible();
        });

        await test.step('PUT reactivate restores it and active filters find it again', async () => {
          const inactiveRow = findRow(panel, updatedName);
          const restorePromise = waitForApiResponse(
            page,
            'PUT',
            `${SYSTEMS_API.servers}/reactivate/${cleanupTarget.id}`,
          );
          const activeRefreshPromise = waitForApiResponse(page, 'GET', SYSTEMS_API.servers, (url) =>
            hasQuery(url, { statusId: ACTIVE_STATUS_ID, serverTypeCode }),
          );
          await selectRowAction(page, inactiveRow, RESTORE_ACTION, INACTIVE_ROW_ACTIONS_BUTTON);
          const response = await restorePromise;
          expect(response.request().postDataJSON()).toBeNull();
          expect(await expectJsonResponse<PhysicalServerRecord>(response)).toMatchObject({
            id: cleanupTarget.id,
            deletedAt: null,
          });
          await expectSuccessfulResponse(await activeRefreshPromise);
          await expect(findRow(panel, updatedName)).toBeVisible();
        });
      } finally {
        await cleanup.runAll();
      }
    });
  }

  test('database vendor: create, inspect, update, filter, delete and restore', async ({
    page,
  }, testInfo) => {
    test.setTimeout(180_000);
    const cleanup = new CleanupRegistry(testInfo);
    const token = uniqueToken(testInfo);
    const initialName = `E2E Vendor ${token}`;
    const updatedName = `${initialName} actualitzat`;
    const initialPort = 15432;
    const updatedPort = 15433;
    const cleanupTarget: {
      id?: number;
      matches: (item: DatabaseVendorRecord) => boolean;
    } = {
      matches: (item) => item.name === initialName || item.name === updatedName,
    };

    try {
      await page.goto('/manteniments/sistemes');
      await expect(page.locator('.app-initial-loader')).toBeHidden({ timeout: 30_000 });
      registerSoftDeleteCleanup<DatabaseVendorRecord>({
        page,
        cleanup,
        label: `database vendor ${token}`,
        pathname: SYSTEMS_API.databaseVendors,
        target: cleanupTarget,
        findParams: { statusId: ACTIVE_STATUS_ID, search: token },
      });
      const panel = await openSystemsPanel(page, 'Proveïdors de bases de dades');

      await test.step('POST creates the vendor', async () => {
        await panel
          .getByRole('button', { name: 'Afegir proveïdor de base de dades', exact: true })
          .click();
        await expect(
          page.getByRole('heading', { name: 'Afegir proveïdor de base de dades' }),
        ).toBeVisible();
        await page.locator('#database-vendor-name').fill(initialName);
        await page.locator('#database-vendor-port').fill(String(initialPort));

        const responsePromise = waitForApiResponse(page, 'POST', SYSTEMS_API.databaseVendors);
        await page.getByRole('button', { name: 'Afegeix el proveïdor', exact: true }).click();
        const response = await responsePromise;
        expect(response.request().postDataJSON()).toEqual({
          name: initialName,
          defaultPort: initialPort,
        });
        const created = await expectJsonResponse<DatabaseVendorRecord>(response);
        cleanupTarget.id = created.id;
        expect(created).toMatchObject({
          id: expect.any(Number),
          name: initialName,
          defaultPort: initialPort,
          deletedAt: null,
        });
        await expect(findRow(panel, initialName)).toBeVisible();
      });

      await test.step('Enter opens View, cancelled edits restore the snapshot and focus', async () => {
        const row = findRow(panel, initialName);
        const detailPromise = waitForApiResponse(
          page,
          'GET',
          `${SYSTEMS_API.databaseVendors}/${cleanupTarget.id}`,
        );
        await row.focus();
        await row.press('Enter');
        await expectJsonResponse<DatabaseVendorRecord>(await detailPromise);
        await expect(
          page.getByRole('heading', { name: 'Consultar proveïdor de base de dades' }),
        ).toBeVisible();
        await expect(page.locator('#database-vendor-name')).toHaveValue(initialName);
        await expect(page.locator('#database-vendor-port')).toHaveValue(String(initialPort));
        await expectViewActions(page, DATABASE_VENDOR_ACTIONS);

        await page.getByRole('button', { name: DATABASE_VENDOR_ACTIONS.edit, exact: true }).click();
        await expect(
          page.getByRole('heading', { name: 'Editar proveïdor de base de dades' }),
        ).toBeVisible();
        await expectEditActions(page, DATABASE_VENDOR_ACTIONS);
        await page.locator('#database-vendor-port').fill('15499');
        await page
          .getByRole('button', { name: DATABASE_VENDOR_ACTIONS.cancel, exact: true })
          .click();
        await expect(page.locator('#database-vendor-port')).toHaveValue(String(initialPort));
        await page
          .getByRole('button', { name: DATABASE_VENDOR_ACTIONS.accept, exact: true })
          .click();
        await expectFocusReturn(row);
      });

      await test.step('PUT updates the vendor from the row action', async () => {
        const detailPromise = waitForApiResponse(
          page,
          'GET',
          `${SYSTEMS_API.databaseVendors}/${cleanupTarget.id}`,
        );
        await selectRowAction(page, findRow(panel, initialName), EDIT_ACTION);
        await expectJsonResponse<DatabaseVendorRecord>(await detailPromise);
        await page.locator('#database-vendor-name').fill(updatedName);
        await page.locator('#database-vendor-port').fill(String(updatedPort));

        const responsePromise = waitForApiResponse(
          page,
          'PUT',
          `${SYSTEMS_API.databaseVendors}/${cleanupTarget.id}`,
        );
        await page.getByRole('button', { name: DATABASE_VENDOR_ACTIONS.save, exact: true }).click();
        const response = await responsePromise;
        expect(response.request().postDataJSON()).toEqual({
          name: updatedName,
          defaultPort: updatedPort,
        });
        expect(await expectJsonResponse<DatabaseVendorRecord>(response)).toMatchObject({
          id: cleanupTarget.id,
          name: updatedName,
          defaultPort: updatedPort,
        });
        await expect(findRow(panel, updatedName)).toBeVisible();
      });

      await test.step('filters send vendor name, port and active status', async () => {
        await showFilters(panel);
        await page.locator('#database-vendors-filter-name').fill(updatedName);
        await page.locator('#database-vendors-filter-port').fill(String(updatedPort));
        await selectOption(page, 'database-vendors-filter-status', 'Actiu');
        const responsePromise = waitForApiResponse(
          page,
          'GET',
          SYSTEMS_API.databaseVendors,
          (url) =>
            hasQuery(url, {
              name: updatedName,
              defaultPort: updatedPort,
              statusId: ACTIVE_STATUS_ID,
            }),
        );
        await panel.getByRole('button', { name: 'Cerca' }).click();
        await expectSuccessfulResponse(await responsePromise);
        await expect(findRow(panel, updatedName)).toBeVisible();
      });

      await test.step('DELETE and inactive lookup expose the restore action', async () => {
        const row = findRow(panel, updatedName);
        await selectRowAction(page, row, DELETE_ACTION);
        const deletePromise = waitForApiResponse(
          page,
          'DELETE',
          `${SYSTEMS_API.databaseVendors}/${cleanupTarget.id}`,
        );
        await page.getByRole('button', { name: "Confirma l'acció", exact: true }).click();
        await expectSuccessfulResponse(await deletePromise);
        await showFilters(panel);
        await page.locator('#database-vendors-filter-name').fill(updatedName);
        await page.locator('#database-vendors-filter-port').fill(String(updatedPort));
        await selectOption(page, 'database-vendors-filter-status', 'Inactiu');
        const inactivePromise = waitForApiResponse(
          page,
          'GET',
          SYSTEMS_API.databaseVendors,
          (url) =>
            hasQuery(url, {
              name: updatedName,
              defaultPort: updatedPort,
              statusId: INACTIVE_STATUS_ID,
            }),
        );
        await panel.getByRole('button', { name: 'Cerca' }).click();
        await expectSuccessfulResponse(await inactivePromise);
        await expect(findRow(panel, updatedName)).toBeVisible();
      });

      await test.step('PUT reactivate restores the vendor and active results find it', async () => {
        const restorePromise = waitForApiResponse(
          page,
          'PUT',
          `${SYSTEMS_API.databaseVendors}/reactivate/${cleanupTarget.id}`,
        );
        const activeRefreshPromise = waitForApiResponse(
          page,
          'GET',
          SYSTEMS_API.databaseVendors,
          (url) => hasQuery(url, { statusId: ACTIVE_STATUS_ID }),
        );
        await selectRowAction(
          page,
          findRow(panel, updatedName),
          RESTORE_ACTION,
          INACTIVE_ROW_ACTIONS_BUTTON,
        );
        const response = await restorePromise;
        expect(response.request().postDataJSON()).toBeNull();
        expect(await expectJsonResponse<DatabaseVendorRecord>(response)).toMatchObject({
          id: cleanupTarget.id,
          deletedAt: null,
        });
        await expectSuccessfulResponse(await activeRefreshPromise);
        await expect(findRow(panel, updatedName)).toBeVisible();
      });
    } finally {
      await cleanup.runAll();
    }
  });

  test('database: create, inspect, update, filter, delete and restore', async ({
    page,
  }, testInfo) => {
    test.setTimeout(180_000);
    const cleanup = new CleanupRegistry(testInfo);
    const token = uniqueToken(testInfo);
    const initialService = `E2E_SVC_${token}`;
    const updatedService = `${initialService}_UPD`;
    const initialPort = 25432;
    const updatedPort = 25433;
    const description = `E2E database ${token}`;
    const updatedDescription = `${description} actualitzada`;
    const cleanupTarget: {
      id?: number;
      matches: (item: DatabaseRecord) => boolean;
    } = {
      matches: (item) => item.service === initialService || item.service === updatedService,
    };

    try {
      await page.goto('/manteniments/sistemes');
      await expect(page.locator('.app-initial-loader')).toBeHidden({ timeout: 30_000 });
      const [server, vendor] = await Promise.all([
        ensurePhysicalServer(page, cleanup, token, 'DATABASE'),
        ensureDatabaseVendor(page, cleanup, token),
      ]);
      const serverLabel = `${server.name} · ${localizedEnvironmentName(server.environment)}`;

      registerSoftDeleteCleanup<DatabaseRecord>({
        page,
        cleanup,
        label: `database ${token}`,
        pathname: SYSTEMS_API.databases,
        target: cleanupTarget,
        findParams: { statusId: ACTIVE_STATUS_ID, search: token },
      });

      await page.reload();
      await expect(page.locator('.app-initial-loader')).toBeHidden({ timeout: 30_000 });
      const panel = await openSystemsPanel(page, 'Bases de dades');

      await test.step('POST creates the database with server and vendor prerequisites', async () => {
        await panel.getByRole('button', { name: 'Afegir base de dades', exact: true }).click();
        await expect(page.getByRole('heading', { name: 'Afegir base de dades' })).toBeVisible();
        await selectOption(page, 'database-host-server', serverLabel);
        await page.locator('#database-host-service').fill(initialService);
        await selectOption(page, 'database-host-type', vendor.name);
        await page.locator('#database-host-port').fill(String(initialPort));
        await page.locator('#database-host-description').fill(description);

        const responsePromise = waitForApiResponse(page, 'POST', SYSTEMS_API.databases);
        await page.getByRole('button', { name: 'Afegeix la base de dades', exact: true }).click();
        const response = await responsePromise;
        expect(response.request().postDataJSON()).toEqual({
          serverId: server.id,
          service: initialService,
          port: initialPort,
          databaseTypeId: vendor.id,
          description,
        });
        const created = await expectJsonResponse<DatabaseRecord>(response);
        cleanupTarget.id = created.id;
        expect(created).toMatchObject({
          id: expect.any(Number),
          server: { id: server.id },
          service: initialService,
          port: initialPort,
          databaseType: { id: vendor.id },
          description,
          deletedAt: null,
        });
        await expect(findRow(panel, initialService)).toBeVisible();
      });

      await test.step('Enter opens View, cancelled edits restore the snapshot and focus', async () => {
        const row = findRow(panel, initialService);
        const detailPromise = waitForApiResponse(
          page,
          'GET',
          `${SYSTEMS_API.databases}/${cleanupTarget.id}`,
        );
        await row.focus();
        await row.press('Enter');
        await expectJsonResponse<DatabaseRecord>(await detailPromise);
        await expect(page.getByRole('heading', { name: 'Consultar base de dades' })).toBeVisible();
        await expectSelectedOption(page, 'database-host-server', serverLabel);
        await expect(page.locator('#database-host-service')).toHaveValue(initialService);
        await expect(page.locator('#database-host-port')).toHaveValue(String(initialPort));
        await expectSelectedOption(page, 'database-host-type', vendor.name);
        await expect(page.locator('#database-host-description')).toHaveValue(description);
        await expectViewActions(page, DATABASE_ACTIONS);

        await page.getByRole('button', { name: DATABASE_ACTIONS.edit, exact: true }).click();
        await expect(page.getByRole('heading', { name: 'Editar base de dades' })).toBeVisible();
        await expectEditActions(page, DATABASE_ACTIONS);
        await page.locator('#database-host-service').fill('E2E_CANVI_CANCEL_LAT');
        await page.getByRole('button', { name: DATABASE_ACTIONS.cancel, exact: true }).click();
        await expect(page.locator('#database-host-service')).toHaveValue(initialService);
        await page.getByRole('button', { name: DATABASE_ACTIONS.accept, exact: true }).click();
        await expectFocusReturn(row);
      });

      await test.step('PUT updates the database from the row action', async () => {
        const detailPromise = waitForApiResponse(
          page,
          'GET',
          `${SYSTEMS_API.databases}/${cleanupTarget.id}`,
        );
        await selectRowAction(page, findRow(panel, initialService), EDIT_ACTION);
        await expectJsonResponse<DatabaseRecord>(await detailPromise);
        await page.locator('#database-host-service').fill(updatedService);
        await page.locator('#database-host-port').fill(String(updatedPort));
        await page.locator('#database-host-description').fill(updatedDescription);

        const responsePromise = waitForApiResponse(
          page,
          'PUT',
          `${SYSTEMS_API.databases}/${cleanupTarget.id}`,
        );
        await page.getByRole('button', { name: DATABASE_ACTIONS.save, exact: true }).click();
        const response = await responsePromise;
        expect(response.request().postDataJSON()).toEqual({
          serverId: server.id,
          service: updatedService,
          port: updatedPort,
          databaseTypeId: vendor.id,
          description: updatedDescription,
        });
        expect(await expectJsonResponse<DatabaseRecord>(response)).toMatchObject({
          id: cleanupTarget.id,
          service: updatedService,
          port: updatedPort,
          description: updatedDescription,
        });
        await expect(findRow(panel, updatedService)).toBeVisible();
      });

      await test.step('filters send server, service, vendor and active status', async () => {
        await showFilters(panel);
        await selectOption(page, 'databases-filter-server', serverLabel);
        await page.locator('#databases-filter-service').fill(updatedService);
        await selectOption(page, 'databases-filter-type', vendor.name);
        await selectOption(page, 'databases-filter-status', 'Actiu');
        const responsePromise = waitForApiResponse(page, 'GET', SYSTEMS_API.databases, (url) =>
          hasQuery(url, {
            serverId: server.id,
            service: updatedService,
            databaseTypeId: vendor.id,
            statusId: ACTIVE_STATUS_ID,
          }),
        );
        await panel.getByRole('button', { name: 'Cerca' }).click();
        await expectSuccessfulResponse(await responsePromise);
        await expect(findRow(panel, updatedService)).toBeVisible();
      });

      await test.step('DELETE moves the database to inactive results', async () => {
        const row = findRow(panel, updatedService);
        await selectRowAction(page, row, DELETE_ACTION);
        const deletePromise = waitForApiResponse(
          page,
          'DELETE',
          `${SYSTEMS_API.databases}/${cleanupTarget.id}`,
        );
        await page.getByRole('button', { name: "Confirma l'acció", exact: true }).click();
        await expectSuccessfulResponse(await deletePromise);
        await showFilters(panel);
        await page.locator('#databases-filter-service').fill(updatedService);
        await selectOption(page, 'databases-filter-status', 'Inactiu');
        const inactivePromise = waitForApiResponse(page, 'GET', SYSTEMS_API.databases, (url) =>
          hasQuery(url, {
            service: updatedService,
            statusId: INACTIVE_STATUS_ID,
          }),
        );
        await panel.getByRole('button', { name: 'Cerca' }).click();
        await expectSuccessfulResponse(await inactivePromise);
        await expect(findRow(panel, updatedService)).toBeVisible();
      });

      await test.step('PUT reactivate restores the database and active results find it', async () => {
        const restorePromise = waitForApiResponse(
          page,
          'PUT',
          `${SYSTEMS_API.databases}/reactivate/${cleanupTarget.id}`,
        );
        const activeRefreshPromise = waitForApiResponse(page, 'GET', SYSTEMS_API.databases, (url) =>
          hasQuery(url, { statusId: ACTIVE_STATUS_ID }),
        );
        await selectRowAction(
          page,
          findRow(panel, updatedService),
          RESTORE_ACTION,
          INACTIVE_ROW_ACTIONS_BUTTON,
        );
        const response = await restorePromise;
        expect(response.request().postDataJSON()).toBeNull();
        expect(await expectJsonResponse<DatabaseRecord>(response)).toMatchObject({
          id: cleanupTarget.id,
          deletedAt: null,
        });
        await expectSuccessfulResponse(await activeRefreshPromise);
        await expect(findRow(panel, updatedService)).toBeVisible();
      });
    } finally {
      await cleanup.runAll();
    }
  });
});

async function openSystemsPanel(page: Page, name: string): Promise<Locator> {
  const header = page.getByRole('button', { name: new RegExp(`^${escapeRegExp(name)}`) });
  const panel = page.locator('p-accordion-panel').filter({ has: header });
  if ((await header.getAttribute('aria-expanded')) !== 'true') await header.click();
  await expect(header).toHaveAttribute('aria-expanded', 'true');
  await expect(panel.locator('.invai-table-loading-container')).toHaveAttribute(
    'aria-busy',
    'false',
  );
  return panel;
}

function findRow(panel: Locator, value: string): Locator {
  return panel.locator('.invai-table-consultable-row').filter({ hasText: value });
}

async function showFilters(panel: Locator): Promise<void> {
  const toggle = panel.getByRole('button', { name: 'Mostra o amaga els filtres' });
  const search = panel.getByRole('button', { name: 'Cerca' });
  if (!(await search.isVisible())) await toggle.click();
  await expect(search).toBeVisible();
}

async function expectFocusReturn(row: Locator): Promise<void> {
  await expect(row).toBeFocused();
}

async function expectViewActions(page: Page, actions: DialogActions): Promise<void> {
  await expect(page.getByRole('button', { name: actions.accept, exact: true })).toBeVisible();
  await expect(page.getByRole('button', { name: actions.edit, exact: true })).toBeVisible();
  await expect(page.getByRole('button', { name: actions.cancel, exact: true })).toHaveCount(0);
  await expect(page.getByRole('button', { name: actions.deactivate, exact: true })).toHaveCount(0);
  await expect(page.getByRole('button', { name: actions.save, exact: true })).toHaveCount(0);
}

async function expectEditActions(page: Page, actions: DialogActions): Promise<void> {
  await expect(page.getByRole('button', { name: actions.accept, exact: true })).toHaveCount(0);
  await expect(page.getByRole('button', { name: actions.edit, exact: true })).toHaveCount(0);
  await expect(page.getByRole('button', { name: actions.cancel, exact: true })).toBeVisible();
  await expect(page.getByRole('button', { name: actions.deactivate, exact: true })).toBeVisible();
  await expect(page.getByRole('button', { name: actions.save, exact: true })).toBeVisible();
}

async function expectSelectedOption(page: Page, inputId: string, value: string): Promise<void> {
  await expect(page.locator(`#${inputId}`).locator('..')).toContainText(value);
}

function hasQuery(url: URL, expected: Record<string, string | number | boolean>): boolean {
  return Object.entries(expected).every(
    ([key, value]) => url.searchParams.get(key) === String(value),
  );
}

function localizedEnvironmentName(environment: {
  id: number;
  code: string | null;
  name: string | null;
}): string {
  return environment.name ?? environment.code ?? `#${environment.id}`;
}

function escapeRegExp(value: string): string {
  return value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}
