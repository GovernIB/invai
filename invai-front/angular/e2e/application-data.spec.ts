import { expect, test, type Page, type Route } from '@playwright/test';

test.use({ storageState: { cookies: [], origins: [] } });

const OPEN_DATA_URL = 'https://intranet.caib.es/testapi/externa';
const REUSE_URL = 'https://intranet.caib.es/testapi/interna';
const LOAD_ERROR = 'No se ha encontrado ningún anclaje de datos con el identificador especificado.';
const NEW_TAB = "(s'obre en una pestanya nova)";

const RECORD = {
  id: 5,
  application: { id: 1 },
  observation: '<p>Notes de dades obertes</p>',
  openDataUrl: OPEN_DATA_URL,
  reuseUrl: REUSE_URL,
  deletedAt: null,
};

interface DataMock {
  failuresBeforeSuccess: number;
  writes: { path: string; body: unknown }[];
  checks: (string | null)[];
}

async function openDataTab(
  page: Page,
  failuresBeforeSuccess = 0,
  initial: Record<string, unknown> = RECORD,
): Promise<DataMock> {
  const mock: DataMock = { failuresBeforeSuccess, writes: [], checks: [] };
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
    // Every URL answers but the reuse one of RECORD.
    if (path.endsWith('/application/data/check-url')) {
      const url = new URL(request.url()).searchParams.get('url');
      mock.checks.push(url);
      return route.fulfill({ json: { url, validUrl: url !== REUSE_URL } });
    }
    const source = path.match(/\/application\/data\/5\/(open-data|reuse)$/)?.[1];
    if (source && request.method() === 'PUT') {
      const body = request.postDataJSON() as { url: string | null };
      mock.writes.push({ path: source, body });
      record = { ...record, [source === 'reuse' ? 'reuseUrl' : 'openDataUrl']: body.url };
      return route.fulfill({ json: { url: body.url, validUrl: false } });
    }
    if (path.endsWith('/application/data/5') && request.method() === 'PUT') {
      const body = request.postDataJSON();
      mock.writes.push({ path: 'data', body });
      record = { ...record, observation: body.observation };
      return route.fulfill({ json: { ...record, openDataUrl: null, reuseUrl: null } });
    }
    if (path.endsWith('/application/data/5')) {
      if (mock.failuresBeforeSuccess > 0) {
        mock.failuresBeforeSuccess--;
        return route.fulfill({
          status: 400,
          json: { error: 'Error de validació', message: LOAD_ERROR },
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

test('shows the URL of each source as a link and warns about one that does not answer', async ({
  page,
}, testInfo) => {
  await openDataTab(page);

  const openData = page.getByRole('region', { name: 'Dades obertes' });
  const reuse = page.getByRole('region', { name: 'Reutilització' });
  const openDataLink = openData.getByRole('link', { name: `${OPEN_DATA_URL} ${NEW_TAB}` });
  await expect(openDataLink).toHaveAttribute('href', OPEN_DATA_URL);
  await expect(openDataLink).toHaveAttribute('target', '_blank');
  await expect(openData.getByRole('status')).toHaveText('Aquesta URL respon.');
  await expect(openData.getByText('Aquesta URL no respon.')).toHaveCount(0);

  await expect(reuse.getByRole('link', { name: `${REUSE_URL} ${NEW_TAB}` })).toBeVisible();
  await expect(
    reuse.getByText('Aquesta URL no respon. Edita les dades per indicar-ne una manualment.'),
  ).toBeVisible();
  await expect(page.getByRole('checkbox')).toHaveCount(0);
  await expect(page.locator('.application-data__observations')).toContainText(
    'Notes de dades obertes',
  );
  await page.screenshot({ path: testInfo.outputPath('data-view.png'), fullPage: true });
});

test('does not check the URLs again when the tab is opened again', async ({ page }) => {
  const mock = await openDataTab(page);
  const openData = page.getByRole('region', { name: 'Dades obertes' });
  await expect(openData.getByRole('status')).toHaveText('Aquesta URL respon.');
  expect(mock.checks).toEqual([OPEN_DATA_URL, REUSE_URL]);

  const tabs = page.locator('.application-detail-tabs');
  await tabs.getByRole('link', { name: /^Integracions/ }).click();
  await expect(openData).toHaveCount(0);
  await tabs.getByRole('link', { name: /^Dades/ }).click();

  await expect(openData.getByRole('status')).toHaveText('Aquesta URL respon.');
  await expect(
    page.getByRole('region', { name: 'Reutilització' }).getByText('Aquesta URL no respon.'),
  ).toBeVisible();
  expect(mock.checks).toEqual([OPEN_DATA_URL, REUSE_URL]);
});

test('saves a URL typed in by hand for one source and warns when it does not answer', async ({
  page,
}, testInfo) => {
  const mock = await openDataTab(page);

  await page.getByRole('button', { name: 'Editar Dades', exact: true }).click();
  const reuse = page.getByRole('region', { name: 'Reutilització' });
  const manual = reuse.getByRole('checkbox', { name: 'Indicar la URL manualment' });
  await expect(manual).not.toBeChecked();
  await expect(manual).toHaveAccessibleDescription(
    'Aquesta URL no respon. Marca «Indicar la URL manualment» per indicar-ne una altra.',
  );
  await expect(reuse.getByRole('textbox', { name: 'Nova URL' })).toHaveCount(0);

  await manual.check();
  const url = reuse.getByRole('textbox', { name: 'Nova URL' });
  await page.getByRole('button', { name: 'Desar els canvis de Dades' }).click();
  await expect(reuse.getByText('Indica la nova URL o desmarca «Indicar la URL manualment».')).toBeVisible();
  await expect(url).toHaveAttribute('aria-invalid', 'true');
  expect(mock.writes).toHaveLength(0);

  await url.fill('https://reuse.example/api');
  await page.screenshot({ path: testInfo.outputPath('data-edit.png'), fullPage: true });
  await page.getByRole('button', { name: 'Desar els canvis de Dades' }).click();

  await expect(page.getByRole('button', { name: 'Editar Dades', exact: true })).toBeVisible();
  expect(mock.writes).toEqual([{ path: 'reuse', body: { url: 'https://reuse.example/api' } }]);
  await expect(
    page.getByText(
      "S'han desat les dades, però la URL de Reutilització no respon. Comprova-la o indica'n una altra.",
    ),
  ).toBeVisible();
  await expect(
    reuse.getByRole('link', { name: `https://reuse.example/api ${NEW_TAB}` }),
  ).toBeVisible();
  await expect(page.getByRole('checkbox')).toHaveCount(0);
});

test('reflows a long URL at 320 px without horizontal scrolling', async ({ page }, testInfo) => {
  await page.setViewportSize({ width: 320, height: 800 });
  const longUrl = `https://intranet.caib.es/${'segment/'.repeat(20)}api/externa`;
  await openDataTab(page, 0, { openDataUrl: longUrl });

  await expect(page.getByRole('link', { name: `${longUrl} ${NEW_TAB}` })).toBeVisible();
  const pageOverflow = await page.evaluate(
    () => document.documentElement.scrollWidth - document.documentElement.clientWidth,
  );
  expect(pageOverflow).toBeLessThanOrEqual(0);
  await page.screenshot({ path: testInfo.outputPath('data-mobile.png'), fullPage: true });
});

test('reports a load failure inside the tab and recovers on retry', async ({ page }, testInfo) => {
  await openDataTab(page, 1);

  await expect(page.getByRole('alert')).toHaveText(LOAD_ERROR);
  await expect(page.getByRole('dialog')).toHaveCount(0);
  await expect(page.getByRole('region', { name: 'Dades obertes' })).toHaveCount(0);
  await page.screenshot({ path: testInfo.outputPath('data-error.png'), fullPage: true });

  await page.getByRole('button', { name: 'Torna a carregar les dades' }).click();

  await expect(page.getByRole('alert')).toHaveCount(0);
  await expect(page.getByRole('heading', { name: 'Dades obertes' })).toBeFocused();
  await expect(
    page.getByRole('link', { name: `${OPEN_DATA_URL} ${NEW_TAB}` }),
  ).toBeVisible();
});
