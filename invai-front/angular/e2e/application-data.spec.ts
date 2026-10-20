import { expect, test, type Page, type Route } from '@playwright/test';

test.use({ storageState: { cookies: [], origins: [] } });

const DETECTED_URL = 'https://intranet.caib.es/testapi/externa/swagger.json';
const UNAVAILABLE_MESSAGE = "No s'ha pogut consultar el catàleg d'API REST externa d'aquesta aplicació.";

const RECORD = {
  id: 5,
  application: { id: 1 },
  observation: '<p>Notes de dades obertes</p>',
  openDataUrl: DETECTED_URL,
  useOpenDataUrl: false,
  openData: [
    {
      path: '/api/aplicacions',
      method: 'GET',
      operation: {
        operationId: 'list',
        summary: 'Llista aplicacions',
        description: 'Retorna les aplicacions publicades.',
        parameters: [
          {
            name: 'page',
            in: 'query',
            required: false,
            description: 'Pàgina',
            type: 'integer',
            format: 'int32',
            defaultValue: 0,
            enumValues: null,
          },
        ],
      },
    },
  ],
  reuseUrl: null,
  useReuseUrl: false,
  reuse: [
    {
      path: '/api/reutilitzacio',
      method: 'GET',
      operation: { operationId: 'reuse', summary: 'Conjunt reutilitzable', description: null, parameters: [] },
    },
  ],
  deletedAt: null,
};

interface DataMock {
  failuresBeforeSuccess: number;
  puts: unknown[];
}

async function openDataTab(
  page: Page,
  failuresBeforeSuccess = 0,
  initial: Record<string, unknown> = RECORD,
): Promise<DataMock> {
  const mock: DataMock = { failuresBeforeSuccess, puts: [] };
  let record = { ...RECORD, ...initial };
  await page.route('**/invaiback/**', async (route: Route) => {
    const request = route.request();
    const path = new URL(request.url()).pathname;
    if (path.endsWith('/auth/me')) {
      return route.fulfill({ json: { authenticated: true, username: 'data-test' } });
    }
    if (path.endsWith('/application/1')) {
      return route.fulfill({
        json: { id: 1, code: 'TEST', name: 'Dades test', status: 'ACTIVE', appDataId: 5 },
      });
    }
    if (path.endsWith('/application/data/5') && request.method() === 'PUT') {
      const body = request.postDataJSON();
      mock.puts.push(body);
      record = { ...record, ...body, observation: body.observation };
      return route.fulfill({ json: { ...record, openData: null, reuse: null } });
    }
    if (path.endsWith('/application/data/5')) {
      if (mock.failuresBeforeSuccess > 0) {
        mock.failuresBeforeSuccess--;
        return route.fulfill({
          status: 400,
          json: { error: 'Error de validació', message: UNAVAILABLE_MESSAGE },
        });
      }
      return route.fulfill({ json: record });
    }
    return route.fulfill({
      json: { content: [], totalElements: 0, totalPages: 1, last: true, number: 0 },
    });
  });
  await page.goto('/aplicacions/1/data');
  await expect(page.getByRole('heading', { name: 'Dades', exact: true })).toBeVisible();
  return mock;
}

test('shows the published endpoints of each source with expandable parameters', async ({
  page,
}, testInfo) => {
  await openDataTab(page);

  const sources = page.getByRole('tablist', { name: 'Fonts de dades' });
  await expect(sources.getByRole('tab')).toHaveText(['Open Data', 'Reutilització']);
  await expect(page.getByRole('tab', { name: 'Open Data' })).toHaveAttribute('aria-selected', 'true');
  await expect(page.getByRole('textbox', { name: 'URL de consulta' })).toHaveValue(DETECTED_URL);
  await expect(page.getByRole('textbox', { name: 'URL de consulta' })).toHaveAttribute('readonly', '');

  const endpoints = page.getByRole('table', { name: 'Open Data: Endpoints GET publicats' });
  await expect(endpoints.getByRole('row', { name: /\/api\/aplicacions/ })).toBeVisible();
  const toggle = page.getByRole('button', { name: 'Paràmetres de /api/aplicacions' });
  await expect(toggle).toHaveAttribute('aria-expanded', 'false');
  await toggle.click();
  await expect(toggle).toHaveAttribute('aria-expanded', 'true');
  const parameters = page.getByRole('table', { name: 'Paràmetres de /api/aplicacions' });
  await expect(parameters.getByRole('row', { name: /page/ })).toContainText('integer');
  await page.screenshot({ path: testInfo.outputPath('data-open-data.png'), fullPage: true });

  await page.getByRole('tab', { name: 'Reutilització' }).click();
  await expect(
    page
      .getByRole('table', { name: 'Reutilització: Endpoints GET publicats' })
      .getByRole('row', { name: /\/api\/reutilitzacio/ }),
  ).toBeVisible();
  await expect(page.locator('.application-data__observations')).toContainText(
    'Notes de dades obertes',
  );
});

test('saves a custom Open Data URL and reloads the endpoints', async ({ page }, testInfo) => {
  const mock = await openDataTab(page);

  await page.getByRole('button', { name: 'Editar Dades', exact: true }).click();
  const openData = page.getByRole('tabpanel', { name: 'Open Data' });
  const url = openData.getByRole('textbox', { name: 'URL de consulta' });
  await expect(url).toHaveAttribute('readonly', '');
  await expect(openData.getByText('URL detectada automàticament')).toBeVisible();
  await expect(
    page.getByRole('tabpanel', { name: 'Reutilització', includeHidden: true }),
  ).toBeHidden();

  await page.getByRole('switch', { name: 'Utilitza una URL pròpia' }).click();
  await expect(url).not.toHaveAttribute('readonly', '');
  await url.fill('');
  await page.getByRole('button', { name: 'Desar els canvis de Dades' }).click();
  await expect(page.getByText('Indica la URL de consulta quan utilitzes una URL pròpia.')).toBeVisible();
  await expect(url).toHaveAttribute('aria-invalid', 'true');
  expect(mock.puts).toHaveLength(0);

  await url.fill('https://custom.example/openapi.json');
  await page.screenshot({ path: testInfo.outputPath('data-edit.png'), fullPage: true });
  const reload = page.waitForRequest(
    (request) => request.method() === 'GET' && request.url().endsWith('/application/data/5'),
  );
  await page.getByRole('button', { name: 'Desar els canvis de Dades' }).click();
  await reload;

  expect(mock.puts).toEqual([
    expect.objectContaining({
      applicationId: 1,
      openDataUrl: 'https://custom.example/openapi.json',
      useOpenDataUrl: true,
      reuseUrl: null,
      useReuseUrl: false,
    }),
  ]);
  await expect(page.getByRole('button', { name: 'Editar Dades', exact: true })).toBeVisible();
  await expect(url).toHaveValue('https://custom.example/openapi.json');
  await expect(
    page.getByRole('table', { name: 'Open Data: Endpoints GET publicats' }).getByRole('row', {
      name: /\/api\/aplicacions/,
    }),
  ).toBeVisible();
});

test('reflows at 320 px keeping the endpoint table scroll inside its container', async ({
  page,
}, testInfo) => {
  await page.setViewportSize({ width: 320, height: 800 });
  await openDataTab(page);

  const table = page.getByRole('table', { name: 'Open Data: Endpoints GET publicats' });
  await expect(table).toBeAttached();
  const pageOverflow = await page.evaluate(
    () => document.documentElement.scrollWidth - document.documentElement.clientWidth,
  );
  expect(pageOverflow).toBeLessThanOrEqual(0);
  const tableWrapper = page.locator('app-application-data-endpoints-table .p-datatable-table-container').first();
  const wrapperBox = await tableWrapper.boundingBox();
  expect(wrapperBox?.width).toBeLessThanOrEqual(320);

  // The tablist itself may overflow its scroll container, so measure the visible host.
  const sources = await page.locator('app-application-data-section p-tablist').boundingBox();
  const reuseTab = await page.getByRole('tab', { name: 'Reutilització' }).boundingBox();
  expect(reuseTab!.x + reuseTab!.width).toBeLessThanOrEqual(sources!.x + sources!.width + 1);
  await page.screenshot({ path: testInfo.outputPath('data-mobile.png') });
});

test('reports an unavailable document inside the tab and recovers on retry', async ({
  page,
}, testInfo) => {
  await openDataTab(page, 1);

  await expect(page.getByRole('alert')).toHaveText(UNAVAILABLE_MESSAGE);
  await expect(page.getByRole('dialog')).toHaveCount(0);
  await expect(page.getByRole('tablist', { name: 'Fonts de dades' })).toHaveCount(0);
  await page.screenshot({ path: testInfo.outputPath('data-error.png'), fullPage: true });

  await page.getByRole('button', { name: 'Torna a carregar les dades' }).click();

  await expect(page.getByRole('alert')).toHaveCount(0);
  await expect(page.getByRole('tab', { name: 'Open Data' })).toBeFocused();
  await expect(
    page.getByRole('table', { name: 'Open Data: Endpoints GET publicats' }).getByRole('row', {
      name: /\/api\/aplicacions/,
    }),
  ).toBeVisible();
});

test('keeps the available source and reports the one the backend could not fetch', async ({
  page,
}, testInfo) => {
  await openDataTab(page, 0, { reuse: null });

  await expect(
    page.getByRole('table', { name: 'Open Data: Endpoints GET publicats' }).getByRole('row', {
      name: /\/api\/aplicacions/,
    }),
  ).toBeVisible();
  await page.getByRole('tab', { name: 'Reutilització' }).click();
  const reuse = page.getByRole('tabpanel', { name: 'Reutilització' });
  await expect(reuse.getByRole('status')).toHaveText(
    "No s'ha pogut consultar el catàleg d'aquesta font en aquest moment. Torna-ho a provar.",
  );
  await expect(reuse.getByText("L'aplicació no publica cap endpoint GET.")).toHaveCount(0);
  await expect(page.getByRole('alert')).toHaveCount(0);
  await page.screenshot({ path: testInfo.outputPath('data-partial.png'), fullPage: true });
});
