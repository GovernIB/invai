import { expect, test } from './support/auth.fixture';
import type { Locator, Page } from '@playwright/test';

import { CleanupRegistry } from './support/cleanup-registry';
import { uniqueToken } from './support/data';
import {
  createAuxiliaryLayer,
  DEVELOPMENT_MAINTENANCE_API,
  type DevelopmentLayerRecord,
  type DevelopmentRoleRecord,
  type DevelopmentTechnologyRecord,
  registerTechnologyCleanup,
} from './support/development-maintenances';
import { expectJsonResponse, expectSuccessfulResponse, waitForApiResponse } from './support/http';
import { ACTIVE_STATUS_ID, INACTIVE_STATUS_ID, registerSoftDeleteCleanup } from './support/systems';
import { selectFilteredOption, selectOption, selectRowAction } from './support/ui';

const DEVELOPMENT_ROUTE = '/manteniments/desenvolupament';
const INACTIVE_ROW_ACTIONS_BUTTON = 'Obrir les accions del registre inactiu';
const EDIT_ACTION = 'Edita';
const DELETE_ACTION = 'Elimina';
const RESTORE_ACTION = 'Restaura';
const EMPTY_RESULTS = "No s'han trobat resultats";

interface DialogActions {
  accept: string;
  edit: string;
  cancel: string;
  deactivate: string;
  save: string;
}

const ROLE_ACTIONS: DialogActions = {
  accept: 'Accepta la consulta del rol',
  edit: 'Edita el rol',
  cancel: 'Cancel·la els canvis del rol',
  deactivate: 'Dona de baixa el rol',
  save: 'Desa els canvis del rol',
};

const LAYER_ACTIONS: DialogActions = {
  accept: 'Accepta la consulta de la capa',
  edit: 'Edita la capa',
  cancel: 'Cancel·la els canvis de la capa',
  deactivate: 'Dona de baixa la capa',
  save: 'Desa els canvis de la capa',
};

const TECHNOLOGY_ACTIONS: DialogActions = {
  accept: 'Accepta la consulta de la tecnologia',
  edit: 'Edita la tecnologia',
  cancel: 'Cancel·la els canvis de la tecnologia',
  deactivate: 'Dona de baixa la tecnologia',
  save: 'Desa els canvis de la tecnologia',
};

test.describe('development maintenances', () => {
  test('navigation keeps provider roles, layers and technologies synchronized with the URL', async ({
    page,
  }) => {
    test.setTimeout(120_000);
    const panels = [
      { title: 'Rols de proveïdor', fragment: 'provider-roles' },
      { title: 'Capes', fragment: 'layers' },
      { title: 'Tecnologies', fragment: 'technologies' },
    ];

    await page.goto(DEVELOPMENT_ROUTE);
    await waitForInitialLoader(page);

    for (const panel of panels) {
      const header = developmentPanelHeader(page, panel.title);
      await header.click();
      await expect(page).toHaveURL(`${DEVELOPMENT_ROUTE}#${panel.fragment}`);
      await expect(header).toHaveAttribute('aria-expanded', 'true');
    }

    for (const panel of panels) {
      await page.goto(`${DEVELOPMENT_ROUTE}#${panel.fragment}`);
      await waitForInitialLoader(page);
      await expect(developmentPanelHeader(page, panel.title)).toHaveAttribute(
        'aria-expanded',
        'true',
      );
      await expect(page).toHaveURL(`${DEVELOPMENT_ROUTE}#${panel.fragment}`);
    }
  });

  test('provider role: localized CRUD, filters, inactive lookup and restore', async ({
    page,
  }, testInfo) => {
    test.setTimeout(240_000);
    const cleanup = new CleanupRegistry(testInfo);
    const token = uniqueToken(testInfo);
    const initial = {
      name: `E2E Rol ${token}`,
      nameEs: `E2E Rol ES ${token}`,
    };
    const updated = {
      name: `E2E Rol actualitzat ${token}`,
      nameEs: `E2E Rol actualizado ES ${token}`,
    };
    const cleanupTarget: {
      id?: number;
      matches: (item: DevelopmentRoleRecord) => boolean;
    } = {
      matches: (item) => item.name === initial.name || item.name === updated.name,
    };

    try {
      await page.goto(`${DEVELOPMENT_ROUTE}#provider-roles`);
      await waitForInitialLoader(page);
      registerSoftDeleteCleanup<DevelopmentRoleRecord>({
        page,
        cleanup,
        label: `provider role ${token}`,
        pathname: DEVELOPMENT_MAINTENANCE_API.roles,
        target: cleanupTarget,
        findParams: { statusId: ACTIVE_STATUS_ID, search: token },
      });
      const panel = await openDevelopmentPanel(page, 'Rols de proveïdor');

      await test.step('POST creates both localized names and announces success', async () => {
        await panel
          .getByRole('button', { name: 'Afegeix un rol de proveïdor', exact: true })
          .click();
        await expect(page.getByRole('heading', { name: 'Afegir rol de proveïdor' })).toBeVisible();
        await page.locator('#role-dialog-name').fill(initial.name);
        await page.locator('#role-dialog-name-es').fill(initial.nameEs);

        const responsePromise = waitForApiResponse(page, 'POST', DEVELOPMENT_MAINTENANCE_API.roles);
        await page.getByRole('button', { name: 'Afegeix el rol', exact: true }).click();
        const response = await responsePromise;
        expect(response.request().postDataJSON()).toEqual(initial);
        const created = await expectJsonResponse<DevelopmentRoleRecord>(response);
        cleanupTarget.id = created.id;
        expect(created).toMatchObject({
          id: expect.any(Number),
          ...initial,
          deletedAt: null,
        });
        await expectToast(page, 'Rol afegit');
        await expect(findRow(panel, initial.name)).toBeVisible();
      });

      await test.step('Enter opens View, Cancel restores the snapshot and close returns focus', async () => {
        const row = findRow(panel, initial.name);
        const detailPromise = waitForApiResponse(
          page,
          'GET',
          `${DEVELOPMENT_MAINTENANCE_API.roles}/${cleanupTarget.id}`,
        );
        await row.focus();
        await row.press('Enter');
        await expectJsonResponse<DevelopmentRoleRecord>(await detailPromise);
        await expect(
          page.getByRole('heading', { name: 'Consultar rol de proveïdor' }),
        ).toBeVisible();
        await expect(page.locator('#role-dialog-name')).toHaveValue(initial.name);
        await expect(page.locator('#role-dialog-name-es')).toHaveValue(initial.nameEs);
        await expectViewActions(page, ROLE_ACTIONS);

        await page.getByRole('button', { name: ROLE_ACTIONS.edit, exact: true }).click();
        await expect(page.getByRole('heading', { name: 'Editar rol de proveïdor' })).toBeVisible();
        await expectEditActions(page, ROLE_ACTIONS);
        await page.locator('#role-dialog-name').fill('E2E canvi cancel·lat');
        await page.locator('#role-dialog-name-es').fill('E2E cambio cancelado');
        await page.getByRole('button', { name: ROLE_ACTIONS.cancel, exact: true }).click();

        await expect(
          page.getByRole('heading', { name: 'Consultar rol de proveïdor' }),
        ).toBeVisible();
        await expect(page.locator('#role-dialog-name')).toHaveValue(initial.name);
        await expect(page.locator('#role-dialog-name-es')).toHaveValue(initial.nameEs);
        await page.getByRole('button', { name: ROLE_ACTIONS.accept, exact: true }).click();
        await expectFocusReturn(row);
      });

      await test.step('PUT updates both localized values from the row action', async () => {
        const detailPromise = waitForApiResponse(
          page,
          'GET',
          `${DEVELOPMENT_MAINTENANCE_API.roles}/${cleanupTarget.id}`,
        );
        await selectRowAction(
          page,
          findRow(panel, initial.name),
          EDIT_ACTION,
          'Obrir les accions del rol',
        );
        await expectJsonResponse<DevelopmentRoleRecord>(await detailPromise);
        await page.locator('#role-dialog-name').fill(updated.name);
        await page.locator('#role-dialog-name-es').fill(updated.nameEs);

        const responsePromise = waitForApiResponse(
          page,
          'PUT',
          `${DEVELOPMENT_MAINTENANCE_API.roles}/${cleanupTarget.id}`,
        );
        await page.getByRole('button', { name: ROLE_ACTIONS.save, exact: true }).click();
        const response = await responsePromise;
        expect(response.request().postDataJSON()).toEqual(updated);
        expect(await expectJsonResponse<DevelopmentRoleRecord>(response)).toMatchObject({
          id: cleanupTarget.id,
          ...updated,
        });
        await expectToast(page, 'Rol actualitzat');
        await expect(findRow(panel, updated.name)).toBeVisible();
      });

      await test.step('localized filters send exact params and the obsolete name is empty', async () => {
        await showFilters(panel);
        await page.locator('#roles-filter-name').fill(updated.name);
        await page.locator('#roles-filter-name-es').fill(updated.nameEs);
        await selectOption(page, 'roles-filter-status', 'Actiu');
        const currentPromise = waitForApiResponse(
          page,
          'GET',
          DEVELOPMENT_MAINTENANCE_API.roles,
          (url) =>
            hasQuery(url, {
              name: updated.name,
              nameEs: updated.nameEs,
              statusId: ACTIVE_STATUS_ID,
            }),
        );
        await panel.getByRole('button', { name: 'Cerca' }).click();
        await expectSuccessfulResponse(await currentPromise);
        await expect(findRow(panel, updated.name)).toBeVisible();

        await page.locator('#roles-filter-name').fill(initial.name);
        await page.locator('#roles-filter-name-es').fill('');
        const emptyPromise = waitForApiResponse(
          page,
          'GET',
          DEVELOPMENT_MAINTENANCE_API.roles,
          (url) =>
            hasQuery(url, { name: initial.name, statusId: ACTIVE_STATUS_ID }) &&
            !url.searchParams.has('nameEs'),
        );
        await panel.getByRole('button', { name: 'Cerca' }).click();
        await expectSuccessfulResponse(await emptyPromise);
        await expect(panel.getByText(EMPTY_RESULTS, { exact: true })).toBeVisible();

        await page.locator('#roles-filter-name').fill(updated.name);
        const restoredResultsPromise = waitForApiResponse(
          page,
          'GET',
          DEVELOPMENT_MAINTENANCE_API.roles,
          (url) => hasQuery(url, { name: updated.name, statusId: ACTIVE_STATUS_ID }),
        );
        await panel.getByRole('button', { name: 'Cerca' }).click();
        await expectSuccessfulResponse(await restoredResultsPromise);
        await expect(findRow(panel, updated.name)).toBeVisible();
      });

      await test.step('DELETE confirms the record and inactive filters expose restore', async () => {
        await selectRowAction(
          page,
          findRow(panel, updated.name),
          DELETE_ACTION,
          'Obrir les accions del rol',
        );
        await expect(page.getByRole('heading', { name: 'Eliminar el rol?' })).toBeVisible();
        await expect(page.getByText(`Estàs a punt d'eliminar «${updated.name}».`)).toBeVisible();
        const deletePromise = waitForApiResponse(
          page,
          'DELETE',
          `${DEVELOPMENT_MAINTENANCE_API.roles}/${cleanupTarget.id}`,
        );
        await page
          .getByRole('button', { name: "Confirma l'eliminació del rol", exact: true })
          .click();
        await expectSuccessfulResponse(await deletePromise);
        await expectToast(page, 'Rol eliminat');

        await showFilters(panel);
        await page.locator('#roles-filter-name').fill(updated.name);
        await selectOption(page, 'roles-filter-status', 'Inactiu');
        const inactivePromise = waitForApiResponse(
          page,
          'GET',
          DEVELOPMENT_MAINTENANCE_API.roles,
          (url) => hasQuery(url, { name: updated.name, statusId: INACTIVE_STATUS_ID }),
        );
        await panel.getByRole('button', { name: 'Cerca' }).click();
        await expectSuccessfulResponse(await inactivePromise);
        await expect(findRow(panel, updated.name)).toBeVisible();
      });

      await test.step('PUT reactivate restores the role and active results find it', async () => {
        const restorePromise = waitForApiResponse(
          page,
          'PUT',
          `${DEVELOPMENT_MAINTENANCE_API.roles}/reactivate/${cleanupTarget.id}`,
        );
        const activeRefreshPromise = waitForApiResponse(
          page,
          'GET',
          DEVELOPMENT_MAINTENANCE_API.roles,
          (url) => hasQuery(url, { statusId: ACTIVE_STATUS_ID }),
        );
        await selectRowAction(
          page,
          findRow(panel, updated.name),
          RESTORE_ACTION,
          INACTIVE_ROW_ACTIONS_BUTTON,
        );
        const response = await restorePromise;
        expect(response.request().postDataJSON()).toBeNull();
        expect(await expectJsonResponse<DevelopmentRoleRecord>(response)).toMatchObject({
          id: cleanupTarget.id,
          deletedAt: null,
        });
        await expectSuccessfulResponse(await activeRefreshPromise);
        await expectToast(page, 'Rol restaurat');
        await expect(findRow(panel, updated.name)).toBeVisible();
      });
    } finally {
      await cleanup.runAll();
    }
  });

  test('layer: CRUD, catalog refresh, inactive lookup and restore', async ({ page }, testInfo) => {
    test.setTimeout(300_000);
    const cleanup = new CleanupRegistry(testInfo);
    const token = uniqueToken(testInfo);
    const initialName = `E2E Capa ${token}`;
    const updatedName = `E2E Capa actualitzada ${token}`;
    const cleanupTarget: {
      id?: number;
      matches: (item: DevelopmentLayerRecord) => boolean;
    } = {
      matches: (item) => item.name === initialName || item.name === updatedName,
    };

    try {
      await page.goto(`${DEVELOPMENT_ROUTE}#layers`);
      await waitForInitialLoader(page);
      registerSoftDeleteCleanup<DevelopmentLayerRecord>({
        page,
        cleanup,
        label: `development layer ${token}`,
        pathname: DEVELOPMENT_MAINTENANCE_API.layers,
        target: cleanupTarget,
        findParams: { statusId: ACTIVE_STATUS_ID, search: token },
      });
      let panel = await openDevelopmentPanel(page, 'Capes');

      await test.step('POST creates the layer and refreshes the technology catalog', async () => {
        await panel.getByRole('button', { name: 'Afegeix una capa', exact: true }).click();
        await expect(page.getByRole('heading', { name: 'Afegir capa' })).toBeVisible();
        await page.locator('#layer-dialog-name').fill(initialName);
        const responsePromise = waitForApiResponse(
          page,
          'POST',
          DEVELOPMENT_MAINTENANCE_API.layers,
        );
        const catalogPromise = waitForLayerCatalogRefresh(page);
        await page.getByRole('button', { name: 'Afegeix la capa', exact: true }).click();
        const response = await responsePromise;
        expect(response.request().postDataJSON()).toEqual({ name: initialName });
        const created = await expectJsonResponse<DevelopmentLayerRecord>(response);
        cleanupTarget.id = created.id;
        expect(created).toMatchObject({
          id: expect.any(Number),
          name: initialName,
          deletedAt: null,
        });
        await expectSuccessfulResponse(await catalogPromise);
        await expectToast(page, 'Capa afegida');
        await expect(findRow(panel, initialName)).toBeVisible();

        await expectLayerCatalogOption(page, initialName, true);
        panel = await openDevelopmentPanel(page, 'Capes');
      });

      await test.step('Enter opens View, Cancel restores the snapshot and close returns focus', async () => {
        const row = findRow(panel, initialName);
        await row.focus();
        await row.press('Enter');
        await expect(page.getByRole('heading', { name: 'Consultar capa' })).toBeVisible();
        await expect(page.locator('#layer-dialog-name')).toHaveValue(initialName);
        await expectViewActions(page, LAYER_ACTIONS);
        await page.getByRole('button', { name: LAYER_ACTIONS.edit, exact: true }).click();
        await expect(page.getByRole('heading', { name: 'Editar capa' })).toBeVisible();
        await expectEditActions(page, LAYER_ACTIONS);
        await page.locator('#layer-dialog-name').fill('E2E canvi cancel·lat');
        await page.getByRole('button', { name: LAYER_ACTIONS.cancel, exact: true }).click();
        await expect(page.locator('#layer-dialog-name')).toHaveValue(initialName);
        await page.getByRole('button', { name: LAYER_ACTIONS.accept, exact: true }).click();
        await expectFocusReturn(row);
      });

      await test.step('PUT updates the layer and localized filters expose empty results', async () => {
        const detailPromise = waitForApiResponse(
          page,
          'GET',
          `${DEVELOPMENT_MAINTENANCE_API.layers}/${cleanupTarget.id}`,
        );
        await selectRowAction(
          page,
          findRow(panel, initialName),
          EDIT_ACTION,
          'Obrir les accions de la capa',
        );
        await expectJsonResponse<DevelopmentLayerRecord>(await detailPromise);
        await page.locator('#layer-dialog-name').fill(updatedName);
        const responsePromise = waitForApiResponse(
          page,
          'PUT',
          `${DEVELOPMENT_MAINTENANCE_API.layers}/${cleanupTarget.id}`,
        );
        const catalogPromise = waitForLayerCatalogRefresh(page);
        await page.getByRole('button', { name: LAYER_ACTIONS.save, exact: true }).click();
        const response = await responsePromise;
        expect(response.request().postDataJSON()).toEqual({ name: updatedName });
        expect(await expectJsonResponse<DevelopmentLayerRecord>(response)).toMatchObject({
          id: cleanupTarget.id,
          name: updatedName,
        });
        await expectSuccessfulResponse(await catalogPromise);
        await expectToast(page, 'Capa actualitzada');
        await expect(findRow(panel, updatedName)).toBeVisible();

        await showFilters(panel);
        await page.locator('#layers-filter-name').fill(updatedName);
        await selectOption(page, 'layers-filter-status', 'Actiu');
        const currentPromise = waitForApiResponse(
          page,
          'GET',
          DEVELOPMENT_MAINTENANCE_API.layers,
          (url) => hasQuery(url, { name: updatedName, statusId: ACTIVE_STATUS_ID }),
        );
        await panel.getByRole('button', { name: 'Cerca' }).click();
        await expectSuccessfulResponse(await currentPromise);
        await expect(findRow(panel, updatedName)).toBeVisible();

        await page.locator('#layers-filter-name').fill(initialName);
        const emptyPromise = waitForApiResponse(
          page,
          'GET',
          DEVELOPMENT_MAINTENANCE_API.layers,
          (url) => hasQuery(url, { name: initialName, statusId: ACTIVE_STATUS_ID }),
        );
        await panel.getByRole('button', { name: 'Cerca' }).click();
        await expectSuccessfulResponse(await emptyPromise);
        await expect(panel.getByText(EMPTY_RESULTS, { exact: true })).toBeVisible();

        await page.locator('#layers-filter-name').fill(updatedName);
        await panel.getByRole('button', { name: 'Cerca' }).click();
        await expect(findRow(panel, updatedName)).toBeVisible();
      });

      await test.step('DELETE removes the layer from the active technology catalog', async () => {
        await selectRowAction(
          page,
          findRow(panel, updatedName),
          DELETE_ACTION,
          'Obrir les accions de la capa',
        );
        await expect(page.getByRole('heading', { name: 'Eliminar la capa?' })).toBeVisible();
        await expect(page.getByText(`Estàs a punt d'eliminar «${updatedName}».`)).toBeVisible();
        const deletePromise = waitForApiResponse(
          page,
          'DELETE',
          `${DEVELOPMENT_MAINTENANCE_API.layers}/${cleanupTarget.id}`,
        );
        const catalogPromise = waitForLayerCatalogRefresh(page);
        await page
          .getByRole('button', { name: "Confirma l'eliminació de la capa", exact: true })
          .click();
        await expectSuccessfulResponse(await deletePromise);
        await expectSuccessfulResponse(await catalogPromise);
        await expectToast(page, 'Capa eliminada');

        await expectLayerCatalogOption(page, updatedName, false);
        panel = await openDevelopmentPanel(page, 'Capes');
        await showFilters(panel);
        await page.locator('#layers-filter-name').fill(updatedName);
        await selectOption(page, 'layers-filter-status', 'Inactiu');
        const inactivePromise = waitForApiResponse(
          page,
          'GET',
          DEVELOPMENT_MAINTENANCE_API.layers,
          (url) => hasQuery(url, { name: updatedName, statusId: INACTIVE_STATUS_ID }),
        );
        await panel.getByRole('button', { name: 'Cerca' }).click();
        await expectSuccessfulResponse(await inactivePromise);
        await expect(findRow(panel, updatedName)).toBeVisible();
      });

      await test.step('PUT reactivate restores the layer and republishes it in the catalog', async () => {
        const restorePromise = waitForApiResponse(
          page,
          'PUT',
          `${DEVELOPMENT_MAINTENANCE_API.layers}/reactivate/${cleanupTarget.id}`,
        );
        const activeRefreshPromise = waitForApiResponse(
          page,
          'GET',
          DEVELOPMENT_MAINTENANCE_API.layers,
          (url) =>
            hasQuery(url, { statusId: ACTIVE_STATUS_ID }) && url.searchParams.get('size') === '10',
        );
        const catalogPromise = waitForLayerCatalogRefresh(page);
        await selectRowAction(
          page,
          findRow(panel, updatedName),
          RESTORE_ACTION,
          INACTIVE_ROW_ACTIONS_BUTTON,
        );
        const response = await restorePromise;
        expect(response.request().postDataJSON()).toBeNull();
        expect(await expectJsonResponse<DevelopmentLayerRecord>(response)).toMatchObject({
          id: cleanupTarget.id,
          deletedAt: null,
        });
        await expectSuccessfulResponse(await activeRefreshPromise);
        await expectSuccessfulResponse(await catalogPromise);
        await expectToast(page, 'Capa restaurada');
        await expect(findRow(panel, updatedName)).toBeVisible();
        await expectLayerCatalogOption(page, updatedName, true);
      });
    } finally {
      await cleanup.runAll();
    }
  });

  test('technology: associated layer CRUD, filters, inactive lookup and restore', async ({
    page,
  }, testInfo) => {
    test.setTimeout(300_000);
    const cleanup = new CleanupRegistry(testInfo);
    const token = uniqueToken(testInfo);
    const initialName = `E2E Tecnologia ${token}`;
    const updatedName = `E2E Tecnologia actualitzada ${token}`;
    const cleanupTarget: {
      id?: number;
      matches: (item: DevelopmentTechnologyRecord) => boolean;
    } = {
      matches: (item) => item.name === initialName || item.name === updatedName,
    };

    try {
      await page.goto(`${DEVELOPMENT_ROUTE}#technologies`);
      await waitForInitialLoader(page);
      const { layer, fallbackLayer } = await createAuxiliaryLayer(page, cleanup, token);
      registerTechnologyCleanup({
        page,
        cleanup,
        label: `development technology ${token}`,
        target: cleanupTarget,
        fallbackLayerId: fallbackLayer.id,
      });
      await page.reload();
      await waitForInitialLoader(page);
      const panel = await openDevelopmentPanel(page, 'Tecnologies');

      await test.step('POST selects a filtered layer and creates the exact association', async () => {
        await panel.getByRole('button', { name: 'Afegeix una tecnologia', exact: true }).click();
        await expect(page.getByRole('heading', { name: 'Afegir tecnologia' })).toBeVisible();
        await page.locator('#technology-dialog-name').fill(initialName);
        await selectFilteredOption(page, 'technology-dialog-layer', layer.name!, layer.name!);
        const responsePromise = waitForApiResponse(
          page,
          'POST',
          DEVELOPMENT_MAINTENANCE_API.technologies,
        );
        await page.getByRole('button', { name: 'Afegeix la tecnologia', exact: true }).click();
        const response = await responsePromise;
        expect(response.request().postDataJSON()).toEqual({
          name: initialName,
          layerId: layer.id,
        });
        const created = await expectJsonResponse<DevelopmentTechnologyRecord>(response);
        cleanupTarget.id = created.id;
        expect(created).toMatchObject({
          id: expect.any(Number),
          name: initialName,
          layer: { id: layer.id, name: null },
          deletedAt: null,
        });
        await expectToast(page, 'Tecnologia afegida');
        await expect(findRow(panel, initialName)).toBeVisible();
      });

      await test.step('Enter opens View, Cancel restores name and layer, and close returns focus', async () => {
        const row = findRow(panel, initialName);
        const detailPromise = waitForApiResponse(
          page,
          'GET',
          `${DEVELOPMENT_MAINTENANCE_API.technologies}/${cleanupTarget.id}`,
        );
        await row.focus();
        await row.press('Enter');
        await expectJsonResponse<DevelopmentTechnologyRecord>(await detailPromise);
        await expect(page.getByRole('heading', { name: 'Consultar tecnologia' })).toBeVisible();
        await expect(page.locator('#technology-dialog-name')).toHaveValue(initialName);
        await expectSelectedOption(page, 'technology-dialog-layer', layer.name!);
        await expectViewActions(page, TECHNOLOGY_ACTIONS);
        await page.getByRole('button', { name: TECHNOLOGY_ACTIONS.edit, exact: true }).click();
        await expect(page.getByRole('heading', { name: 'Editar tecnologia' })).toBeVisible();
        await expectEditActions(page, TECHNOLOGY_ACTIONS);
        await page.locator('#technology-dialog-name').fill('E2E canvi cancel·lat');
        await page.getByRole('button', { name: TECHNOLOGY_ACTIONS.cancel, exact: true }).click();
        await expect(page.locator('#technology-dialog-name')).toHaveValue(initialName);
        await expectSelectedOption(page, 'technology-dialog-layer', layer.name!);
        await page.getByRole('button', { name: TECHNOLOGY_ACTIONS.accept, exact: true }).click();
        await expectFocusReturn(row);
      });

      await test.step('PUT updates the technology and filters include layer and active status', async () => {
        const detailPromise = waitForApiResponse(
          page,
          'GET',
          `${DEVELOPMENT_MAINTENANCE_API.technologies}/${cleanupTarget.id}`,
        );
        await selectRowAction(
          page,
          findRow(panel, initialName),
          EDIT_ACTION,
          'Obrir les accions de la tecnologia',
        );
        await expectJsonResponse<DevelopmentTechnologyRecord>(await detailPromise);
        await page.locator('#technology-dialog-name').fill(updatedName);
        const responsePromise = waitForApiResponse(
          page,
          'PUT',
          `${DEVELOPMENT_MAINTENANCE_API.technologies}/${cleanupTarget.id}`,
        );
        await page.getByRole('button', { name: TECHNOLOGY_ACTIONS.save, exact: true }).click();
        const response = await responsePromise;
        expect(response.request().postDataJSON()).toEqual({
          name: updatedName,
          layerId: layer.id,
        });
        expect(await expectJsonResponse<DevelopmentTechnologyRecord>(response)).toMatchObject({
          id: cleanupTarget.id,
          name: updatedName,
          layer: { id: layer.id },
        });
        await expectToast(page, 'Tecnologia actualitzada');
        await expect(findRow(panel, updatedName)).toBeVisible();

        await showFilters(panel);
        await page.locator('#technologies-filter-name').fill(updatedName);
        await selectFilteredOption(page, 'technologies-filter-layer', layer.name!, layer.name!);
        await selectOption(page, 'technologies-filter-status', 'Actiu');
        const currentPromise = waitForApiResponse(
          page,
          'GET',
          DEVELOPMENT_MAINTENANCE_API.technologies,
          (url) =>
            hasQuery(url, {
              name: updatedName,
              layerId: layer.id,
              statusId: ACTIVE_STATUS_ID,
            }),
        );
        await panel.getByRole('button', { name: 'Cerca' }).click();
        await expectSuccessfulResponse(await currentPromise);
        await expect(findRow(panel, updatedName)).toBeVisible();

        await page.locator('#technologies-filter-name').fill(initialName);
        const emptyPromise = waitForApiResponse(
          page,
          'GET',
          DEVELOPMENT_MAINTENANCE_API.technologies,
          (url) =>
            hasQuery(url, {
              name: initialName,
              layerId: layer.id,
              statusId: ACTIVE_STATUS_ID,
            }),
        );
        await panel.getByRole('button', { name: 'Cerca' }).click();
        await expectSuccessfulResponse(await emptyPromise);
        await expect(panel.getByText(EMPTY_RESULTS, { exact: true })).toBeVisible();

        await page.locator('#technologies-filter-name').fill(updatedName);
        await panel.getByRole('button', { name: 'Cerca' }).click();
        await expect(findRow(panel, updatedName)).toBeVisible();
      });

      await test.step('DELETE confirms the association and inactive filters expose restore', async () => {
        await selectRowAction(
          page,
          findRow(panel, updatedName),
          DELETE_ACTION,
          'Obrir les accions de la tecnologia',
        );
        await expect(page.getByRole('heading', { name: 'Eliminar la tecnologia?' })).toBeVisible();
        await expect(page.getByText(`Estàs a punt d'eliminar «${updatedName}».`)).toBeVisible();
        const deletePromise = waitForApiResponse(
          page,
          'DELETE',
          `${DEVELOPMENT_MAINTENANCE_API.technologies}/${cleanupTarget.id}`,
        );
        await page
          .getByRole('button', { name: "Confirma l'eliminació de la tecnologia", exact: true })
          .click();
        await expectSuccessfulResponse(await deletePromise);
        await expectToast(page, 'Tecnologia eliminada');

        await showFilters(panel);
        await page.locator('#technologies-filter-name').fill(updatedName);
        await selectFilteredOption(page, 'technologies-filter-layer', layer.name!, layer.name!);
        await selectOption(page, 'technologies-filter-status', 'Inactiu');
        const inactivePromise = waitForApiResponse(
          page,
          'GET',
          DEVELOPMENT_MAINTENANCE_API.technologies,
          (url) =>
            hasQuery(url, {
              name: updatedName,
              layerId: layer.id,
              statusId: INACTIVE_STATUS_ID,
            }),
        );
        await panel.getByRole('button', { name: 'Cerca' }).click();
        await expectSuccessfulResponse(await inactivePromise);
        await expect(findRow(panel, updatedName)).toBeVisible();
      });

      await test.step('PUT reactivate restores the technology and active results find it', async () => {
        const restorePromise = waitForApiResponse(
          page,
          'PUT',
          `${DEVELOPMENT_MAINTENANCE_API.technologies}/reactivate/${cleanupTarget.id}`,
        );
        const activeRefreshPromise = waitForApiResponse(
          page,
          'GET',
          DEVELOPMENT_MAINTENANCE_API.technologies,
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
        expect(await expectJsonResponse<DevelopmentTechnologyRecord>(response)).toMatchObject({
          id: cleanupTarget.id,
          deletedAt: null,
        });
        await expectSuccessfulResponse(await activeRefreshPromise);
        await expectToast(page, 'Tecnologia restaurada');
        await expect(findRow(panel, updatedName)).toBeVisible();
      });
    } finally {
      await cleanup.runAll();
    }
  });
});

async function waitForInitialLoader(page: Page): Promise<void> {
  await expect(page.locator('.app-initial-loader')).toBeHidden({ timeout: 30_000 });
}

function developmentPanelHeader(page: Page, name: string): Locator {
  return page.getByRole('button', { name: new RegExp(`^${escapeRegExp(name)}`) });
}

async function openDevelopmentPanel(page: Page, name: string): Promise<Locator> {
  const header = developmentPanelHeader(page, name);
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

async function expectToast(page: Page, title: string): Promise<void> {
  await expect(page.getByText(title, { exact: true })).toBeVisible();
}

async function expectFocusReturn(row: Locator): Promise<void> {
  await expect(row).toBeFocused();
}

function waitForLayerCatalogRefresh(page: Page) {
  return waitForApiResponse(
    page,
    'GET',
    DEVELOPMENT_MAINTENANCE_API.layers,
    (url) =>
      hasQuery(url, { statusId: ACTIVE_STATUS_ID, size: 100 }) &&
      url.searchParams.get('sort') === 'name,asc',
  );
}

async function expectLayerCatalogOption(
  page: Page,
  layerName: string,
  available: boolean,
): Promise<void> {
  const panel = await openDevelopmentPanel(page, 'Tecnologies');
  await panel.getByRole('button', { name: 'Afegeix una tecnologia', exact: true }).click();
  await page.locator('#technology-dialog-layer').click();
  const overlay = page.locator('.p-select-overlay:visible');
  await overlay.locator('input.p-select-filter').fill(layerName);
  const option = overlay.getByRole('option', { name: layerName, exact: true });
  if (available) await expect(option).toBeVisible();
  else await expect(option).toHaveCount(0);
  await page.keyboard.press('Escape');
  await page
    .getByRole('button', { name: 'Tanca el formulari de la tecnologia', exact: true })
    .click();
  await expect(page.getByRole('heading', { name: 'Afegir tecnologia' })).toHaveCount(0);
}

function hasQuery(url: URL, expected: Record<string, string | number | boolean>): boolean {
  return Object.entries(expected).every(
    ([key, value]) => url.searchParams.get(key) === String(value),
  );
}

function escapeRegExp(value: string): string {
  return value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}
