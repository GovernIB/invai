import { expect, test } from '@playwright/test';
import type { APIResponse, Locator, Page, Response, TestInfo } from '@playwright/test';

const ACTIVE_STATUS_ID = 1;
const INACTIVE_STATUS_ID = 2;
const ROW_ACTIONS_BUTTON = 'Obrir les accions del registre';
const INACTIVE_ROW_ACTIONS_BUTTON = 'Obrir les accions del registre inactiu';
const VIEW_ACTION = 'Consulta';
const EDIT_ACTION = 'Edita';
const DELETE_ACTION = 'Elimina';

type MaintenanceInput = Record<string, string>;

interface MaintenanceResponse {
  id: number;
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
  deleteMode?: 'environment-inactive' | 'api-inactive';
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
    closeViewButton: 'Tanca la consulta de la categoria',
    editDialogTitle: 'Editar categoria',
    saveButton: 'Desa els canvis de la categoria',
    confirmDeleteButton: "Confirma l'eliminació de la categoria",
    inputPrefix: 'category-dialog',
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
    closeViewButton: "Tanca la consulta del sistema d'informació",
    editDialogTitle: "Editar sistema d'informació",
    saveButton: "Desa els canvis del sistema d'informació",
    confirmDeleteButton: "Confirma l'eliminació del sistema d'informació",
    inputPrefix: 'system-type-dialog',
  }),
  {
    key: 'environment',
    title: 'Entorns',
    route: '/sistemes#environments',
    apiPath: '/invaiapi/interna/environment',
    addButton: 'Afegeix un entorn',
    addDialogTitle: 'Afegir entorn',
    addSubmitButton: "Afegeix l'entorn",
    viewDialogTitle: 'Consultar entorn',
    closeViewButton: "Tanca la consulta de l'entorn",
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
    closeViewButton: "Tanca la consulta de l'àmbit",
    editDialogTitle: 'Editar àmbit',
    saveButton: "Desa els canvis de l'àmbit",
    confirmDeleteButton: "Confirma l'eliminació de l'àmbit",
    inputPrefix: 'field-dialog',
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
    closeViewButton: 'Tanca la consulta de la comissió informàtica',
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
    await expect(tabs.getByRole('link')).toHaveText(['General', 'Responsables']);

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
    await expect(
      page.getByRole('region', { name: 'Responsables' }),
    ).toContainText('Encara no hi ha manteniments disponibles per a aquesta secció.');
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
    await page.goto('/sistemes');

    const databases = page.getByRole('button', { name: /^Bases de dades/ });
    const servers = page.getByRole('button', { name: /^Servidors d'aplicacions/ });
    const environments = page.getByRole('button', { name: /^Entorns/ });

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
    await expect(page).toHaveURL(/\/sistemes#databases$/);
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
    await expect(page).toHaveURL(/\/sistemes#environments$/);
    await expect(environments).toHaveAttribute('aria-expanded', 'true');
    await expect(databases).toHaveAttribute('aria-expanded', 'false');
  });

  test('redirects previous systems and maintenance URLs to their panels', async ({ page }) => {
    await page.goto('/sistemes/bases-de-dades');
    await expect(page).toHaveURL(/\/sistemes#databases$/);
    await expect(page.getByRole('button', { name: /^Bases de dades/ })).toHaveAttribute(
      'aria-expanded',
      'true',
    );

    await page.goto('/manteniments/entorns');
    await expect(page).toHaveURL(/\/sistemes#environments$/);
    await expect(page.getByRole('button', { name: /^Entorns/ })).toHaveAttribute(
      'aria-expanded',
      'true',
    );
  });

  test('manages a flattened local application host and blocks hosts in use', async ({ page }) => {
    test.setTimeout(120_000);
    const serverName = `e2e-local-${Date.now()}.caib.es`;
    await page.goto('/sistemes');

    const servers = page.getByRole('button', { name: /^Servidors d'aplicacions/ });
    await servers.click();
    const panel = page.locator('p-accordion-panel').filter({ has: servers });

    await expect(panel.getByRole('tab')).toHaveCount(0);
    await panel.getByRole('button', { name: "Afegir host d'aplicació" }).click();
    await expect(page.getByRole('heading', { name: "Afegir host d'aplicació" })).toBeVisible();
    const serverInput = page.locator('#application-host-server');
    const serverAutocomplete = page.locator('p-autocomplete').filter({ has: serverInput });
    await expect(serverInput).not.toBeFocused();
    await serverInput.focus();
    const focusStyles = await serverAutocomplete.evaluate((root) => {
      const input = root.querySelector<HTMLElement>('.p-autocomplete-input');
      const dropdown = root.querySelector<HTMLElement>('.p-autocomplete-dropdown');
      if (!input || !dropdown) throw new Error('Incomplete server autocomplete');

      return {
        rootBoxShadow: getComputedStyle(root).boxShadow,
        inputBoxShadow: getComputedStyle(input).boxShadow,
        inputBorderColor: getComputedStyle(input).borderTopColor,
        dropdownBorderColor: getComputedStyle(dropdown).borderTopColor,
      };
    });
    expect(focusStyles.rootBoxShadow).not.toBe('none');
    expect(focusStyles.inputBoxShadow).toBe('none');
    expect(focusStyles.dropdownBorderColor).toBe(focusStyles.inputBorderColor);
    await serverInput.fill(serverName);
    await page.waitForTimeout(500);
    await expect(page.locator('.p-autocomplete-empty-message')).toHaveCount(0);
    await page.locator('#application-host-environment').click();
    const environmentOption = page.getByRole('option').first();
    const environmentLabel = (await environmentOption.textContent())?.trim() ?? '';
    await environmentOption.click();
    await page.locator('#application-host-instance').fill('e2e_tomcat');
    await page.locator('#application-host-port').fill('8090');
    await page.locator('#application-host-version').fill('11');
    await page.getByRole('button', { name: 'Afegeix', exact: true }).click();

    await expect(page.getByText('Canvi desat només en local').last()).toBeVisible();
    let row = panel.getByRole('row').filter({ hasText: serverName });
    await expect(row).toBeVisible();
    await expect(row).toContainText(environmentLabel);

    await selectRowAction(page, row, VIEW_ACTION);
    await expect(
      page.getByRole('heading', { name: "Consultar host d'aplicació" }),
    ).toBeVisible();
    await expect(page.locator('#application-host-server')).toBeDisabled();
    await page.getByRole('button', { name: 'Tanca', exact: true }).click();

    row = panel.getByRole('row').filter({ hasText: serverName });
    await selectRowAction(page, row, EDIT_ACTION);
    await expect(page.getByRole('heading', { name: "Editar host d'aplicació" })).toBeVisible();
    await page.locator('#application-host-instance').fill('e2e_tomcat_updated');
    await page.getByRole('button', { name: 'Desa', exact: true }).click();
    await expect(panel.getByRole('row').filter({ hasText: 'e2e_tomcat_updated' })).toBeVisible();

    row = panel.getByRole('row').filter({ hasText: serverName });
    await selectRowAction(page, row, DELETE_ACTION);
    await page.getByRole('button', { name: "Confirma l'acció" }).click();

    await expect(row).toHaveCount(0);
    await expect(page.getByText('Canvi desat només en local').last()).toBeVisible();

    const usedRow = panel.getByRole('row').filter({ hasText: 'exapp01.caib.es' });
    await selectRowAction(page, usedRow, DELETE_ACTION);
    await expect(page.getByText("No es pot donar de baixa l'host")).toBeVisible();
    await expect(page.getByRole('button', { name: "Confirma l'acció" })).toHaveCount(0);

    await panel.getByRole('button', { name: 'Mostra o amaga els filtres' }).click();
    await selectOption(page, 'servers-filter-status', 'Inactiu');
    await panel.getByRole('button', { name: 'Cerca', exact: true }).click();
    row = panel.getByRole('row').filter({ hasText: serverName });
    await expect(row).toBeVisible();
    await row.getByRole('button', { name: INACTIVE_ROW_ACTIONS_BUTTON }).click();
    await page.getByRole('menuitem', { name: 'Restaura' }).click();
    await expect(row).toHaveCount(0);
  });
});

test.describe('maintenance API lifecycles through the UI', () => {
  for (const scenario of SCENARIOS) {
    test(`${scenario.key}: list, create, view, update and delete`, async ({ page }, testInfo) => {
      test.setTimeout(180_000);

      const token = uniqueToken(testInfo);
      const data = scenario.createData(token);
      const lookupValue = data.initial[scenario.uniqueField];

      let createdId: number | undefined;
      let creationAttempted = false;
      let deleted = false;

      try {
        await test.step('GET loads the initial maintenance page', async () => {
          const responsePromise = waitForMaintenanceResponse(
            page,
            'GET',
            scenario.apiPath,
            (url) => matchesInitialList(url, scenario),
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

          const responsePromise = waitForMaintenanceResponse(
            page,
            'POST',
            scenario.apiPath,
          );
          const refreshPromise = waitForMaintenanceResponse(
            page,
            'GET',
            scenario.apiPath,
            (url) => matchesInitialList(url, scenario),
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
          const refreshPromise = waitForMaintenanceResponse(
            page,
            'GET',
            scenario.apiPath,
            (url) => matchesInitialList(url, scenario),
          );
          await page.getByRole('button', { name: scenario.saveButton }).click();

          const response = await responsePromise;
          expect(response.request().postDataJSON()).toEqual(data.updated);
          const updated = await expectJsonResponse<MaintenanceResponse>(response);
          expect(updated).toMatchObject({ id: createdId, ...data.updated });
          await expectSuccessfulResponse(await refreshPromise);

          const updatedRow = await findMaintenanceRow(page, scenario, lookupValue, createdId!);
          await expect(updatedRow).toContainText(data.updated.name ?? lookupValue);
        });

        await test.step('DELETE removes or deactivates the maintenance record', async () => {
          const row = await findMaintenanceRow(page, scenario, lookupValue, createdId!);
          await selectRowAction(page, row, DELETE_ACTION);

          const deleteResponsePromise = waitForMaintenanceResponse(
            page,
            'DELETE',
            `${scenario.apiPath}/${createdId}`,
          );
          const refreshPromise = waitForMaintenanceResponse(
            page,
            'GET',
            scenario.apiPath,
            (url) => matchesInitialList(url, scenario),
          );
          await page.getByRole('button', { name: scenario.confirmDeleteButton }).click();

          await expectSuccessfulResponse(await deleteResponsePromise);
          deleted = true;
          await expectSuccessfulResponse(await refreshPromise);

          if (scenario.deleteMode === 'environment-inactive') {
            await expectInactiveEnvironment(page, scenario, data.updated, createdId!);
          } else if (scenario.deleteMode === 'api-inactive') {
            await expectInactiveEntity(page, scenario, data.updated, createdId!);
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
    const currentRow = maintenanceRow(page, uniqueValue);
    if ((await currentRow.count()) > 0) {
      await expect(currentRow).toBeVisible();
      return currentRow;
    }

    const responsePromise = waitForMaintenanceResponse(
      page,
      'GET',
      scenario.apiPath,
      (url) => url.searchParams.get(scenario.quickSearch!.queryParam) === uniqueValue,
    );
    await page.getByLabel(scenario.quickSearch.ariaLabel).fill(uniqueValue);
    const body = await expectJsonResponse<MaintenancePageResponse>(await responsePromise);
    expect(body.content).toContainEqual(expect.objectContaining({ id }));

    const row = maintenanceRow(page, uniqueValue);
    await expect(row).toBeVisible();
    return row;
  }

  for (let pageIndex = 0; pageIndex < 100; pageIndex += 1) {
    const row = maintenanceRow(page, uniqueValue);
    if ((await row.count()) > 0) {
      await expect(row).toBeVisible();
      return row;
    }

    const nextButton = page.locator('button.p-paginator-next');
    if ((await nextButton.count()) === 0 || (await nextButton.isDisabled())) break;

    const responsePromise = waitForMaintenanceResponse(page, 'GET', scenario.apiPath);
    await nextButton.click();
    await expectSuccessfulResponse(await responsePromise);
  }

  throw new Error(`Could not find ${scenario.key} row containing ${uniqueValue}`);
}

async function selectRowAction(page: Page, row: Locator, action: string): Promise<void> {
  await row.getByRole('button', { name: ROW_ACTIONS_BUTTON }).click();
  await page.getByRole('menuitem', { name: action, exact: true }).click();
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
  const row = maintenanceRow(page, input.code);
  await expect(row).toBeVisible();
  await expect(
    row.getByRole('button', { name: INACTIVE_ROW_ACTIONS_BUTTON }),
  ).toBeVisible();
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
  statusId = scenario.deleteMode ? ACTIVE_STATUS_ID : undefined,
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
    (
      await scanEntities(
        page,
        scenario,
        (entity) => entity[scenario.uniqueField] === uniqueValue,
      )
    )?.id;
  if (!id) return;

  const response = await page.request.delete(apiUrl(page, `${scenario.apiPath}/${id}`));
  if (response.ok()) return;

  await testInfo.attach(`${scenario.key}-cleanup-error`, {
    body: await response.body(),
    contentType: response.headers()['content-type'] ?? 'text/plain',
  });
  expect
    .soft(
      response.ok(),
      `Cleanup DELETE ${scenario.apiPath}/${id} returned ${response.status()}`,
    )
    .toBeTruthy();
}

function waitForMaintenanceResponse(
  page: Page,
  method: string,
  pathname: string,
  matchesUrl: (url: URL) => boolean = () => true,
): Promise<Response> {
  return page.waitForResponse(
    (response) => {
      const url = new URL(response.url());
      return (
        response.request().method() === method &&
        url.pathname === pathname &&
        matchesUrl(url)
      );
    },
    { timeout: 30_000 },
  );
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

async function expectJsonResponse<T>(response: Response): Promise<T> {
  const body = await response.text();
  expect(
    response.ok(),
    `${response.request().method()} ${response.url()} returned ${response.status()}: ${body}`,
  ).toBeTruthy();

  return JSON.parse(body) as T;
}

async function expectSuccessfulResponse(response: Response): Promise<void> {
  const body = await response.text();
  expect(
    response.ok(),
    `${response.request().method()} ${response.url()} returned ${response.status()}: ${body}`,
  ).toBeTruthy();
}

async function expectApiJsonResponse<T>(response: APIResponse, request: string): Promise<T> {
  const body = await response.text();
  expect(
    response.ok(),
    `${request} returned ${response.status()}: ${body}`,
  ).toBeTruthy();

  return JSON.parse(body) as T;
}

async function selectOption(page: Page, inputId: string, optionName: string): Promise<void> {
  await page.locator(`#${inputId}`).click();
  const option = page.getByRole('option', { name: optionName, exact: true });
  await expect(option).toBeVisible();
  await option.click();
}

function maintenanceRow(page: Page, text: string): Locator {
  return page.getByRole('row').filter({ hasText: text }).first();
}

function displayDate(date: string): string {
  const [year, month, day] = date.split('-');
  return `${day}/${month}/${year}`;
}

function uniqueToken(testInfo: TestInfo): string {
  return `${Date.now().toString(36)}${testInfo.workerIndex}`.slice(-8).toUpperCase();
}

function apiUrl(page: Page, pathname: string): string {
  return new URL(pathname, page.url()).toString();
}
