import { expect as baseExpect, test as base, type Page, type Route } from '@playwright/test';

import { expect, test } from './support/auth.fixture';
import { CleanupRegistry } from './support/cleanup-registry';
import { uniqueToken } from './support/data';
import {
  apiUrl,
  expectApiJsonResponse,
  expectJsonResponse,
  expectSuccessfulResponse,
  waitForApiResponse,
} from './support/http';
import {
  expectToast,
  findRow,
  hasQuery,
  INACTIVE_ROW_ACTIONS_BUTTON,
  openMaintenancePanel,
  SEARCH_BUTTON,
  showFilters,
  waitForInitialLoader,
} from './support/maintenance-resources';
import {
  ACTIVE_STATUS_ID,
  type EnvironmentRecord,
  INACTIVE_STATUS_ID,
  registerSoftDeleteCleanup,
  SYSTEMS_API,
} from './support/systems';
import { selectOption, selectRowAction } from './support/ui';

const FILTERS_BUTTON = 'Mostra o amaga els filtres';
const GENERAL_ROUTE = '/manteniments/general';
const UNSUPPORTED_STATUS = 'La consulta de registres inactius encara no està disponible.';
const UNSUPPORTED_STATUS_LISTS: Array<{
  panelId: string;
  title: string;
  prefix: string;
  apiPath: string;
  searchMessage: string | null;
}> = [
  {
    panelId: 'categories',
    title: 'Categories',
    prefix: 'categories',
    apiPath: '/invaiback/category',
    searchMessage: null,
  },
  {
    panelId: 'system-types',
    title: "Sistemes d'informació",
    prefix: 'system-types',
    apiPath: '/invaiback/system-type',
    searchMessage:
      "La cerca i els filtres de sistemes d'informació encara no estan suportats pel backend.",
  },
  {
    panelId: 'fields',
    title: 'Àmbits',
    prefix: 'fields',
    apiPath: '/invaiback/field',
    searchMessage: "La cerca i els filtres d'àmbits encara no estan suportats pel backend.",
  },
];

test.describe('environments restore', () => {
  test('an inactive environment is found with the inactive filter and restored', async ({
    page,
  }, testInfo) => {
    test.setTimeout(180_000);
    const cleanup = new CleanupRegistry(testInfo);
    const token = uniqueToken(testInfo);
    const payload = {
      code: `E2E${token}`,
      name: `E2E Entorn restaurable ${token}`,
      nameEs: `E2E Entorno restaurable ${token}`,
    };

    try {
      await page.goto('/manteniments/sistemes#environments');
      await waitForInitialLoader(page);
      const environment = await expectApiJsonResponse<EnvironmentRecord>(
        await page.request.post(apiUrl(page, SYSTEMS_API.environments), { data: payload }),
        `POST ${SYSTEMS_API.environments}`,
      );
      registerSoftDeleteCleanup<EnvironmentRecord>({
        page,
        cleanup,
        label: `environment ${payload.code}`,
        pathname: SYSTEMS_API.environments,
        target: { id: environment.id, matches: (item) => item.code === payload.code },
      });
      const deleteResponse = await page.request.delete(
        apiUrl(page, `${SYSTEMS_API.environments}/${environment.id}`),
      );
      expect(deleteResponse.ok(), await deleteResponse.text()).toBeTruthy();

      const panel = await openMaintenancePanel(page, 'Entorns');
      await showFilters(panel, FILTERS_BUTTON);
      await page.locator('#environments-filter-code').fill(payload.code);
      await selectOption(page, 'environments-filter-status', 'Inactiu');
      const inactivePromise = waitForApiResponse(page, 'GET', SYSTEMS_API.environments, (url) =>
        hasQuery(url, { code: payload.code, statusId: INACTIVE_STATUS_ID }),
      );
      await panel.getByRole('button', { name: SEARCH_BUTTON }).click();
      const inactive = await expectJsonResponse<{ content: EnvironmentRecord[] }>(
        await inactivePromise,
      );
      expect(inactive.content.map(({ id }) => id)).toEqual([environment.id]);

      const row = findRow(panel, payload.code);
      await row.dblclick();
      const dialog = page.getByRole('dialog');
      await expect(
        dialog.getByRole('heading', { name: 'Consultar entorn', exact: true }),
      ).toBeVisible();
      await expect(dialog.getByRole('button', { name: "Edita l'entorn" })).toHaveCount(0);
      const restorePromise = waitForApiResponse(
        page,
        'PUT',
        `${SYSTEMS_API.environments}/reactivate/${environment.id}`,
      );
      await dialog.getByRole('button', { name: "Restaura l'entorn", exact: true }).click();
      const response = await restorePromise;
      expect(response.request().postDataJSON()).toBeNull();
      expect(await expectJsonResponse<EnvironmentRecord>(response)).toMatchObject({
        id: environment.id,
        deletedAt: null,
      });
      await expectToast(page, "L'entorn s'ha restaurat correctament.");
      await expect(dialog).toHaveCount(0);

      // After a mutation the list goes back to the default active filter.
      await showFilters(panel, FILTERS_BUTTON);
      await page.locator('#environments-filter-code').fill(payload.code);
      const activePromise = waitForApiResponse(page, 'GET', SYSTEMS_API.environments, (url) =>
        hasQuery(url, { code: payload.code, statusId: ACTIVE_STATUS_ID }),
      );
      await panel.getByRole('button', { name: SEARCH_BUTTON }).click();
      await expectSuccessfulResponse(await activePromise);
      await expect(findRow(panel, payload.code)).toBeVisible();
      await expect(
        findRow(panel, payload.code).getByRole('button', { name: INACTIVE_ROW_ACTIONS_BUTTON }),
      ).toHaveCount(0);
      await selectRowAction(page, findRow(panel, payload.code), 'Consulta');
      await expect(
        dialog.getByRole('button', { name: "Edita l'entorn", exact: true }),
      ).toBeVisible();
    } finally {
      await cleanup.runAll();
    }
  });
});

base.describe('general maintenance filters (mocked)', () => {
  base.use({ storageState: { cookies: [], origins: [] } });

  base('commission filters validate the date range and send dates, type and quick search', async ({
    page,
  }) => {
    const requests = await mockGeneralMaintenance(page);
    const commissionRequests = () =>
      requests.filter((url) => url.pathname === '/invaiback/commission');
    await page.goto(`${GENERAL_ROUTE}#commissions`);
    await waitForInitialLoader(page);
    const panel = await openMaintenancePanel(page, 'Comissió informàtica');
    await showFilters(panel, FILTERS_BUTTON);

    await fillDate(page, 'commissions-filter-date-from', '10/05/2026');
    await fillDate(page, 'commissions-filter-date-to', '01/05/2026');
    const before = commissionRequests().length;
    await panel.getByRole('button', { name: SEARCH_BUTTON }).click();
    await baseExpect(
      panel.getByRole('alert').filter({
        hasText: 'La data inicial no pot ser posterior a la data final.',
      }),
    ).toBeVisible();
    await baseExpect(page.locator('#commissions-filter-date-from')).toHaveAttribute(
      'aria-invalid',
      'true',
    );
    await baseExpect(page.locator('#commissions-filter-date-from')).toHaveAttribute(
      'aria-describedby',
      'commissions-filter-date-range-error',
    );
    expect(commissionRequests()).toHaveLength(before);

    await fillDate(page, 'commissions-filter-date-to', '31/05/2026');
    await baseExpect(panel.getByRole('alert')).toHaveCount(0);
    await selectOption(page, 'commissions-filter-type', 'Tècnica');
    const filtered = {
      approvalDateFrom: '2026-05-10',
      approvalDateTo: '2026-05-31',
      commissionType: 'TECNICA',
      statusId: ACTIVE_STATUS_ID,
    };
    const filteredPromise = waitForApiResponse(page, 'GET', '/invaiback/commission', (url) =>
      hasQuery(url, filtered),
    );
    await panel.getByRole('button', { name: SEARCH_BUTTON }).click();
    const filteredUrl = new URL((await filteredPromise).url());
    baseExpect(filteredUrl.searchParams.has('quickSearch')).toBe(false);
    await baseExpect(
      panel.getByRole('button', { name: `${FILTERS_BUTTON} (4 filtres aplicats)`, exact: true }),
    ).toBeVisible();

    const quickSearchPromise = waitForApiResponse(page, 'GET', '/invaiback/commission', (url) =>
      hasQuery(url, { ...filtered, quickSearch: 'EXP-42' }),
    );
    await panel.getByRole('textbox', { name: 'Cerca ràpida de comissions' }).fill('  EXP-42 ');
    await quickSearchPromise;
  });

  for (const list of UNSUPPORTED_STATUS_LISTS) {
    base(`${list.panelId}: selecting an unsupported status informs and never requests it`, async ({
      page,
    }) => {
      const requests = await mockGeneralMaintenance(page);
      await page.goto(`${GENERAL_ROUTE}#${list.panelId}`);
      await waitForInitialLoader(page);
      const panel = await openMaintenancePanel(page, list.title);
      await showFilters(panel, FILTERS_BUTTON);
      await baseExpect(page.locator(`#${list.prefix}-filter-status`)).toHaveText('Actiu');

      await selectOption(page, `${list.prefix}-filter-status`, 'Inactiu');
      await expectToast(page, UNSUPPORTED_STATUS);
      await panel.getByRole('button', { name: SEARCH_BUTTON }).click();
      if (list.searchMessage) await expectToast(page, list.searchMessage);
      const inactiveRequests = requests.filter(
        (url) =>
          url.pathname === list.apiPath &&
          url.searchParams.get('statusId') === String(INACTIVE_STATUS_ID),
      );
      baseExpect(inactiveRequests.map(String)).toEqual([]);
    });
  }

  base.fixme(
    'categories: the status select keeps showing Actiu after the unsupported status is rejected',
    // The form control is reset to Actiu, but PrimeNG Select still renders "Inactiu".
    async ({ page }) => {
      await mockGeneralMaintenance(page);
      await page.goto(`${GENERAL_ROUTE}#categories`);
      await waitForInitialLoader(page);
      const panel = await openMaintenancePanel(page, 'Categories');
      await showFilters(panel, FILTERS_BUTTON);
      await selectOption(page, 'categories-filter-status', 'Inactiu');
      await expectToast(page, UNSUPPORTED_STATUS);
      await baseExpect(page.locator('#categories-filter-status')).toHaveText('Actiu');
    },
  );
});

async function mockGeneralMaintenance(page: Page): Promise<URL[]> {
  const requests: URL[] = [];
  await page.route('**/invaiback/**', async (route: Route) => {
    const url = new URL(route.request().url());
    if (url.pathname.endsWith('/auth/me')) {
      return route.fulfill({ json: { authenticated: true, username: 'general-filters-test' } });
    }
    requests.push(url);
    return route.fulfill({ json: pageOf([]) });
  });
  return requests;
}

async function fillDate(page: Page, inputId: string, value: string): Promise<void> {
  const input = page.locator(`#${inputId}`);
  await input.clear();
  // The datepicker only parses typed input that comes with key events.
  await input.pressSequentially(value);
  await input.press('Escape');
  await baseExpect(input).toHaveValue(value);
}

function pageOf(content: unknown[]) {
  return { content, totalElements: content.length, totalPages: 1, last: true, number: 0 };
}
