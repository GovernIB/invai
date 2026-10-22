import { expect, test, type Page, type Route } from '@playwright/test';

test.use({ storageState: { cookies: [], origins: [] } });

const pageOf = (content: unknown[]) => ({
  content,
  totalElements: content.length,
  totalPages: 1,
  last: true,
  number: 0,
});

const OPEN_DATA_URL = 'https://intranet.caib.es/testapi/externa';
const REUSE_URL = 'https://intranet.caib.es/testapi/interna';
const NEW_TAB = "(s'obre en una pestanya nova)";
const EDIT_SECTION = 'Editar Dades';
const SAVE_SECTION = 'Desar els canvis de Dades';
const MANUAL_URL = 'Indicar la URL manualment';
const PENDING = 'Es coneixerà quan es desin les dades de la pestanya.';
const URL_HELP = 'Ha de començar per http:// o https://. En desar es comprovarà si respon.';
const PATTERN_ERROR = 'Introdueix una URL HTTP o HTTPS vàlida.';
const SAVED = "S'han desat correctament els canvis de Dades.";
const NOT_RESPONDING = 'Aquesta URL no respon.';

type UrlSource = 'open-data' | 'reuse';

const RECORD = {
  id: 5,
  application: { id: 1 },
  observation: '<p>Notes de dades obertes</p>',
  openDataUrl: OPEN_DATA_URL,
  reuseUrl: REUSE_URL,
  deletedAt: null,
};

interface DataOptions {
  appDataId?: number | null;
  status?: 'ACTIVE' | 'INACTIVE';
  record?: Record<string, unknown>;
  validUrls?: Partial<Record<UrlSource, boolean>>;
}

interface DataMock {
  requests: { method: string; path: string; body: unknown }[];
  // URLs checked with check-url, kept apart from the requests that read or write the data.
  checks: (string | null)[];
  // Whether the URL saved for each source answers when the back-end checks it.
  validUrls: Record<UrlSource, boolean>;
}

async function openDataTab(page: Page, options: DataOptions = {}): Promise<DataMock> {
  const mock: DataMock = {
    requests: [],
    checks: [],
    validUrls: { 'open-data': true, reuse: true, ...options.validUrls },
  };
  let appDataId = options.appDataId === undefined ? RECORD.id : options.appDataId;
  let record = { ...RECORD, ...options.record };
  await page.route('**/invaiback/**', async (route: Route) => {
    const request = route.request();
    const resource = new URL(request.url()).pathname.replace(/^.*\/invaiback\//, '');
    const method = request.method();
    if (resource === 'auth/me') {
      return route.fulfill({ json: { authenticated: true, username: 'data-test' } });
    }
    if (resource === 'application/1') {
      return route.fulfill({
        json: {
          id: 1,
          code: 'TEST',
          name: 'Dades test',
          status: options.status ?? 'ACTIVE',
          appDataId,
        },
      });
    }
    if (!resource.startsWith('application/data')) return route.fulfill({ json: pageOf([]) });
    if (resource === 'application/data/check-url') {
      const url = new URL(request.url()).searchParams.get('url');
      mock.checks.push(url);
      const checked: UrlSource = url === record.reuseUrl ? 'reuse' : 'open-data';
      return route.fulfill({ json: { url, validUrl: mock.validUrls[checked] } });
    }

    const body = method === 'GET' ? null : request.postDataJSON();
    mock.requests.push({ method, path: resource, body });
    // POST and PUT answer without the URL of the sources.
    if (resource === 'application/data' && method === 'POST') {
      record = { ...record, observation: body.observation };
      appDataId = record.id;
      return route.fulfill({ status: 201, json: { ...record, openDataUrl: null, reuseUrl: null } });
    }
    const source = resource.match(/^application\/data\/\d+\/(open-data|reuse)$/)?.[1] as
      | UrlSource
      | undefined;
    if (source && method === 'PUT') {
      record = { ...record, [source === 'reuse' ? 'reuseUrl' : 'openDataUrl']: body.url };
      return route.fulfill({ json: { url: body.url, validUrl: mock.validUrls[source] } });
    }
    if (resource === `application/data/${record.id}` && method === 'PUT') {
      record = { ...record, observation: body.observation };
      return route.fulfill({ json: { ...record, openDataUrl: null, reuseUrl: null } });
    }
    if (resource === `application/data/${record.id}`) return route.fulfill({ json: record });
    return route.fulfill({ status: 404, json: { error: 'Not found', message: resource } });
  });
  await page.goto('/aplicacions/1/data');
  await expect(page.getByRole('heading', { name: 'Dades', exact: true })).toBeVisible();
  return mock;
}

const writes = (mock: DataMock) => mock.requests.filter((request) => request.method !== 'GET');
const openDataRegion = (page: Page) => page.getByRole('region', { name: 'Dades obertes' });
const reuseRegion = (page: Page) => page.getByRole('region', { name: 'Reutilització' });

async function startEditing(page: Page): Promise<void> {
  await page.getByRole('button', { name: EDIT_SECTION, exact: true }).click();
  await expect(page.getByRole('button', { name: SAVE_SECTION })).toBeVisible();
}

async function save(page: Page): Promise<void> {
  await page.getByRole('button', { name: SAVE_SECTION }).click();
}

test('creates the data record on the first save and then reads the URLs of its sources', async ({
  page,
}, testInfo) => {
  const mock = await openDataTab(page, { appDataId: null, record: { id: 9, observation: null } });
  const openData = openDataRegion(page);
  const reuse = reuseRegion(page);

  await expect(openData.getByText(PENDING)).toBeVisible();
  await expect(reuse.getByText(PENDING)).toBeVisible();
  await expect(openData.getByRole('link')).toHaveCount(0);
  await expect(reuse.getByRole('link')).toHaveCount(0);
  expect(mock.requests).toEqual([]);
  expect(mock.checks).toEqual([]);
  await page.screenshot({ path: testInfo.outputPath('data-pending.png'), fullPage: true });

  await startEditing(page);
  const editor = page.locator('p-editor .ql-editor');
  await editor.click();
  await editor.pressSequentially('Primera nota');
  await reuse.getByRole('checkbox', { name: MANUAL_URL }).check();
  await reuse.getByRole('textbox', { name: 'Nova URL' }).fill('https://reuse.example/api');
  await save(page);

  await expect(page.getByText(SAVED)).toBeVisible();
  // The new record is written first, then the URL typed in by hand, and only then read back.
  expect(mock.requests).toEqual([
    {
      method: 'POST',
      path: 'application/data',
      body: {
        applicationId: 1,
        observation: expect.stringMatching(/^<p>Primera(&nbsp;| )nota<\/p>$/),
      },
    },
    { method: 'PUT', path: 'application/data/9/reuse', body: { url: 'https://reuse.example/api' } },
    { method: 'GET', path: 'application/data/9', body: null },
  ]);
  await expect(openData.getByRole('link', { name: `${OPEN_DATA_URL} ${NEW_TAB}` })).toBeVisible();
  await expect(
    reuse.getByRole('link', { name: `https://reuse.example/api ${NEW_TAB}` }),
  ).toBeVisible();
  await expect(page.getByText(PENDING)).toHaveCount(0);
  await expect(page.getByRole('button', { name: EDIT_SECTION, exact: true })).toBeVisible();
  // Once read back, only the default URL is checked: saving the typed one already checked it.
  await expect(openData.getByRole('status')).toHaveText('Aquesta URL respon.');
  expect(mock.checks).toEqual([OPEN_DATA_URL]);
});

test('rejects a URL typed in by hand that is not an absolute HTTP(S) URL', async ({
  page,
}, testInfo) => {
  const mock = await openDataTab(page);
  await startEditing(page);
  const openData = openDataRegion(page);
  await openData.getByRole('checkbox', { name: MANUAL_URL }).check();
  const url = openData.getByRole('textbox', { name: 'Nova URL' });
  await expect(url).toHaveAccessibleDescription(URL_HELP);

  for (const invalid of [
    'intranet.caib.es/testapi/externa',
    'ftp://intranet.caib.es/testapi/externa',
    // The back-end only accepts the scheme in lower case.
    'HTTPS://intranet.caib.es/testapi/externa',
    'https://',
  ]) {
    await url.fill(invalid);
    await expect(openData.getByRole('alert'), invalid).toHaveText(PATTERN_ERROR);
    await expect(url).toHaveAttribute('aria-invalid', 'true');
    await expect(url).toHaveAccessibleDescription(`${PATTERN_ERROR} ${URL_HELP}`);
  }
  await page.screenshot({ path: testInfo.outputPath('data-url-pattern.png'), fullPage: true });
  await save(page);
  await expect(page.getByRole('button', { name: SAVE_SECTION })).toBeVisible();
  expect(writes(mock)).toEqual([]);

  // The field stops at the 500 characters the back-end accepts.
  const base = 'https://intranet.caib.es/';
  const longest = `${base}${'a'.repeat(500 - base.length)}`;
  await expect(url).toHaveAttribute('maxlength', '500');
  await url.fill(`${longest}b`);
  await expect(url).toHaveValue(longest);
  await expect(openData.getByRole('alert')).toHaveCount(0);
  await expect(url).not.toHaveAttribute('aria-invalid', 'true');
  await save(page);

  await expect(page.getByText(SAVED)).toBeVisible();
  expect(writes(mock)).toEqual([
    { method: 'PUT', path: 'application/data/5/open-data', body: { url: longest } },
  ]);
});

test('warns about both sources at once when neither URL typed in by hand answers', async ({
  page,
}, testInfo) => {
  const mock = await openDataTab(page);
  mock.validUrls = { 'open-data': false, reuse: false };
  await startEditing(page);

  for (const [region, value] of [
    [openDataRegion(page), 'https://open.example/api'],
    [reuseRegion(page), 'https://reuse.example/api'],
  ] as const) {
    await region.getByRole('checkbox', { name: MANUAL_URL }).check();
    await region.getByRole('textbox', { name: 'Nova URL' }).fill(value);
  }
  await save(page);

  await expect(
    page.getByText(
      "S'han desat les dades, però les URL de Dades obertes i de Reutilització no responen. Comprova-les o indica'n unes altres.",
    ),
  ).toBeVisible();
  expect(writes(mock)).toEqual([
    {
      method: 'PUT',
      path: 'application/data/5/open-data',
      body: { url: 'https://open.example/api' },
    },
    { method: 'PUT', path: 'application/data/5/reuse', body: { url: 'https://reuse.example/api' } },
  ]);
  for (const region of [openDataRegion(page), reuseRegion(page)]) {
    await expect(
      region.getByText(`${NOT_RESPONDING} Edita les dades per indicar-ne una manualment.`),
    ).toBeVisible();
  }
  await expect(page.getByRole('checkbox')).toHaveCount(0);
  await page.screenshot({ path: testInfo.outputPath('data-both-invalid.png'), fullPage: true });
});

test('saves only the observation and keeps the URLs already known', async ({ page }) => {
  const mock = await openDataTab(page);
  const reads = () => mock.requests.filter((request) => request.method === 'GET').length;
  const initialReads = reads();

  // Saving without changes writes nothing and leaves edit mode.
  await startEditing(page);
  await save(page);
  await expect(page.getByRole('button', { name: EDIT_SECTION, exact: true })).toBeVisible();
  expect(writes(mock)).toEqual([]);

  await startEditing(page);
  const editor = page.locator('p-editor .ql-editor');
  await editor.click();
  await page.keyboard.press('ControlOrMeta+A');
  await editor.pressSequentially('Nova observació');
  await save(page);

  await expect(page.getByText(SAVED)).toBeVisible();
  expect(writes(mock)).toEqual([
    {
      method: 'PUT',
      path: 'application/data/5',
      body: {
        applicationId: 1,
        observation: expect.stringMatching(/^<p>Nova(&nbsp;| )observació<\/p>$/),
      },
    },
  ]);
  expect(reads()).toBe(initialReads);
  await expect(page.locator('.application-data__observations')).toContainText('Nova observació');
  await expect(
    openDataRegion(page).getByRole('link', { name: `${OPEN_DATA_URL} ${NEW_TAB}` }),
  ).toBeVisible();
  await expect(
    reuseRegion(page).getByRole('link', { name: `${REUSE_URL} ${NEW_TAB}` }),
  ).toBeVisible();
});

test('keeps the tab read-only for an inactive application', async ({ page }, testInfo) => {
  await openDataTab(page, {
    status: 'INACTIVE',
    validUrls: { reuse: false },
  });

  await expect(
    openDataRegion(page).getByRole('link', { name: `${OPEN_DATA_URL} ${NEW_TAB}` }),
  ).toBeVisible();
  const reuse = reuseRegion(page);
  await expect(reuse.getByRole('link', { name: `${REUSE_URL} ${NEW_TAB}` })).toBeVisible();
  // Nothing suggests editing an application that cannot be edited.
  await expect(reuse.getByText(NOT_RESPONDING, { exact: true })).toBeVisible();
  await expect(reuse.getByText(/Edita les dades/)).toHaveCount(0);
  await expect(page.getByRole('button', { name: EDIT_SECTION, exact: true })).toHaveCount(0);
  await expect(page.getByRole('button', { name: SAVE_SECTION })).toHaveCount(0);
  await expect(page.getByRole('checkbox')).toHaveCount(0);
  await expect(page.locator('p-editor')).toHaveCount(0);
  await expect(page.locator('.application-data__observations')).toContainText(
    'Notes de dades obertes',
  );
  await page.screenshot({ path: testInfo.outputPath('data-inactive.png'), fullPage: true });
});
