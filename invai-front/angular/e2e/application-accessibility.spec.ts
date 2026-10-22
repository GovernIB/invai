import { expect, test, type Page, type Route } from '@playwright/test';

import { selectOption } from './support/ui';

test.use({ storageState: { cookies: [], origins: [] } });

const SEGMENTS = [
  { id: 11, name: 'Segment A', nameEs: 'Segmento A', deletedAt: null },
  { id: 12, name: 'Segment B', nameEs: 'Segmento B', deletedAt: null },
];
const COMPLIANCES = [
  { id: 21, name: 'Totalment conforme', nameEs: 'Totalmente conforme', deletedAt: null },
  { id: 22, name: 'Parcialment conforme', nameEs: 'Parcialmente conforme', deletedAt: null },
];
const RECORD = {
  id: 7,
  application: { id: 1 },
  classificationSegment: SEGMENTS[0],
  compliance: COMPLIANCES[1],
  publicUrl: 'https://www.caib.es/invai',
  mobileApplication: true,
  mobileApplicationName: 'InvAI mòbil',
  nonAccessibleContent: '<p>Vídeos sense subtítols</p>',
  observations: '<p>Revisió anual</p>',
  expireDate: '2027-03-15T00:00:00',
  deletedAt: null as string | null,
};
const SAVED_PAYLOAD = {
  applicationId: 1,
  complianceId: 22,
  classificationSegmentId: 11,
  publicUrl: RECORD.publicUrl,
  mobileApplication: true,
  mobileApplicationName: RECORD.mobileApplicationName,
  nonAccessibleContent: RECORD.nonAccessibleContent,
  observations: RECORD.observations,
  expireDate: RECORD.expireDate,
};

const SECTION = 'Accessibilitat';
const EDIT = 'Editar Accessibilitat';
const SAVE = 'Desar els canvis de Accessibilitat';
const NO_VALUE = 'No informat';
const PUBLIC_URL_LINK = "Obrir la URL pública de l'aplicació";
const URL_ERROR = 'Introdueix una URL HTTP o HTTPS vàlida';
const MOBILE_NAME_UNAVAILABLE = "Disponible quan l'aplicació mòbil està marcada com a «Sí».";
const SAVED = "S'han desat correctament els canvis de Accessibilitat.";
const CLEAR_BLOCKED =
  'No es pot deixar en blanc una classificació o situació de compliment ja desada. Pots seleccionar un altre valor.';
const TEXT_TOO_LONG =
  'El text supera la mida màxima permesa. Redueix-lo (els accents i el format també ocupen espai).';
const RETRY_RECORD = "Torna a carregar l'accessibilitat";
const RETRY_CATALOGS = 'Torna a carregar els catàlegs';
const CATALOG_FAILED =
  "No s'han pogut carregar els catàlegs d'accessibilitat. Torna-ho a provar.";

type Catalog = 'classification-segment' | 'compliance-situation';

interface MockOptions {
  status?: 'ACTIVE' | 'INACTIVE';
  appAccessibilityId?: number | null;
  record?: Partial<typeof RECORD>;
  // Statuses answered, in order, before the record or a catalog is served.
  recordFailures?: number[];
  catalogFailures?: Partial<Record<Catalog, number[]>>;
}

interface AccessibilityMock {
  writes: { method: string; path: string; body: unknown }[];
  reads: string[];
  catalogParams: URLSearchParams[];
}

const pageOf = (content: unknown[]) => ({
  content,
  totalElements: content.length,
  totalPages: 1,
  last: true,
  number: 0,
});

async function openAccessibilityTab(
  page: Page,
  options: MockOptions = {},
): Promise<AccessibilityMock> {
  const mock: AccessibilityMock = { writes: [], reads: [], catalogParams: [] };
  const recordFailures = [...(options.recordFailures ?? [])];
  const catalogFailures: Record<Catalog, number[]> = {
    'classification-segment': [...(options.catalogFailures?.['classification-segment'] ?? [])],
    'compliance-situation': [...(options.catalogFailures?.['compliance-situation'] ?? [])],
  };
  let appAccessibilityId =
    options.appAccessibilityId === undefined ? RECORD.id : options.appAccessibilityId;
  let record: Record<string, unknown> = { ...RECORD, ...options.record };

  await page.route('**/invaiback/**', async (route: Route) => {
    const request = route.request();
    const url = new URL(request.url());
    const path = url.pathname;
    const method = request.method();
    if (path.endsWith('/auth/me')) {
      return route.fulfill({ json: { authenticated: true, username: 'accessibility-test' } });
    }
    if (path.endsWith('/application/1')) {
      return route.fulfill({
        json: {
          id: 1,
          code: 'TEST',
          name: 'Accessibilitat test',
          status: options.status ?? 'ACTIVE',
          appAccessibilityId,
        },
      });
    }
    const catalog = path.match(/\/(classification-segment|compliance-situation)$/)?.[1] as
      | Catalog
      | undefined;
    if (catalog) {
      mock.reads.push(catalog);
      mock.catalogParams.push(url.searchParams);
      const failure = catalogFailures[catalog].shift();
      if (failure) {
        return route.fulfill({ status: failure, json: { message: 'Catalog test failure' } });
      }
      return route.fulfill({
        json: pageOf(catalog === 'classification-segment' ? SEGMENTS : COMPLIANCES),
      });
    }
    if (/\/application\/accessibility(\/\d+)?$/.test(path)) {
      if (method === 'GET') {
        mock.reads.push(path.slice(path.indexOf('application/')));
        const failure = recordFailures.shift();
        if (failure) {
          return route.fulfill({ status: failure, json: { message: 'Record test failure' } });
        }
        return route.fulfill({ json: record });
      }
      const body = request.postDataJSON();
      mock.writes.push({ method, path: path.slice(path.indexOf('application/')), body });
      record = {
        id: RECORD.id,
        application: { id: body.applicationId },
        classificationSegment:
          SEGMENTS.find((item) => item.id === body.classificationSegmentId) ?? null,
        compliance: COMPLIANCES.find((item) => item.id === body.complianceId) ?? null,
        publicUrl: body.publicUrl,
        mobileApplication: body.mobileApplication,
        mobileApplicationName: body.mobileApplicationName,
        nonAccessibleContent: body.nonAccessibleContent,
        observations: body.observations,
        expireDate: body.expireDate,
        deletedAt: null,
      };
      appAccessibilityId = RECORD.id;
      return route.fulfill({ json: record });
    }
    return route.fulfill({ json: pageOf([]) });
  });

  await page.goto('/aplicacions/1/accessibility');
  await expect(page.getByRole('heading', { name: SECTION, exact: true })).toBeVisible();
  return mock;
}

const section = (page: Page) => page.locator('app-application-accessibility-section');
const value = (page: Page, label: string) => section(page).getByLabel(label, { exact: true });
// Each rich text is a region labelled like its own static value.
const richTextValue = (page: Page, label: string) =>
  section(page).getByRole('region', { name: label }).getByLabel(label, { exact: true });
const publicUrl = (page: Page) => section(page).getByRole('textbox', { name: 'URL pública' });
const mobileName = (page: Page) =>
  section(page).getByRole('textbox', { name: 'Nom aplicació mòbil' });
const editor = (page: Page, labelId: string) =>
  section(page).locator(`.ql-editor[aria-labelledby="${labelId}"]`);
const observationsEditor = (page: Page) =>
  editor(page, 'application-accessibility-observations-label');
const inaccessibleContentEditor = (page: Page) =>
  editor(page, 'application-accessibility-inaccessible-content-label');
const toast = (page: Page, detail: string) =>
  page.locator('.p-toast-message').filter({ hasText: detail });

async function startEditing(page: Page): Promise<void> {
  await page.getByRole('button', { name: EDIT, exact: true }).click();
  await expect(page.getByRole('button', { name: SAVE, exact: true })).toBeVisible();
}

async function save(page: Page): Promise<void> {
  await page.getByRole('button', { name: SAVE, exact: true }).click();
}

test('shows a saved record as read-only values with a link to its public URL', async ({
  page,
}) => {
  const mock = await openAccessibilityTab(page);

  await expect(value(page, 'Segment de classificació')).toHaveText('Segment A');
  await expect(value(page, 'Situació de compliment')).toHaveText('Parcialment conforme');
  await expect(value(page, 'Caducitat del darrer informe')).toHaveText('15/03/2027');
  await expect(value(page, 'Aplicació mòbil')).toHaveText('Sí');
  await expect(value(page, 'Nom aplicació mòbil')).toHaveText('InvAI mòbil');
  await expect(richTextValue(page, 'Contingut no accessible')).toHaveText(
    'Vídeos sense subtítols',
  );
  await expect(richTextValue(page, 'Observacions')).toHaveText('Revisió anual');
  await expect(publicUrl(page)).toHaveValue(RECORD.publicUrl);
  await expect(publicUrl(page)).not.toBeEditable();
  const link = section(page).getByRole('link', { name: PUBLIC_URL_LINK });
  await expect(link).toHaveAttribute('href', RECORD.publicUrl);
  await expect(link).toHaveAttribute('target', '_blank');
  await expect(section(page).getByRole('combobox')).toHaveCount(0);
  await expect(section(page).locator('.ql-editor')).toHaveCount(0);
  expect([...mock.reads].sort()).toEqual([
    'application/accessibility/7',
    'classification-segment',
    'compliance-situation',
  ]);
  for (const params of mock.catalogParams) {
    expect(Object.fromEntries(params)).toEqual({
      page: '0',
      size: '100',
      sort: 'id,asc',
      statusId: '1',
    });
  }

  await section(page)
    .getByRole('button', { name: "Obrir la documentació d'accessibilitat" })
    .click();
  await expect(
    toast(page, "La documentació d'accessibilitat encara no està implementada."),
  ).toBeVisible();
});

test('creates the record with a POST on the first save and updates it with a PUT', async ({
  page,
}) => {
  const mock = await openAccessibilityTab(page, { appAccessibilityId: null });

  expect(mock.reads).not.toContain('application/accessibility/7');
  for (const label of [
    'Segment de classificació',
    'Situació de compliment',
    'Caducitat del darrer informe',
    'Aplicació mòbil',
    'Nom aplicació mòbil',
  ]) {
    await expect(value(page, label)).toHaveText(NO_VALUE);
  }
  await expect(richTextValue(page, 'Contingut no accessible')).toHaveText(NO_VALUE);
  await expect(richTextValue(page, 'Observacions')).toHaveText(NO_VALUE);
  await expect(publicUrl(page)).toHaveValue('');
  await expect(section(page).getByRole('link', { name: PUBLIC_URL_LINK })).toHaveCount(0);

  await startEditing(page);
  await expect(mobileName(page)).toBeDisabled();
  await expect(mobileName(page)).toHaveAccessibleDescription(MOBILE_NAME_UNAVAILABLE);
  await selectOption(page, 'application-accessibility-classification', 'Segment B');
  await selectOption(page, 'application-accessibility-compliance', 'Totalment conforme');
  // The date picker only parses typed keys, so the date is typed in rather than filled.
  const date = page.locator('#application-accessibility-publication-date');
  await date.click();
  await date.pressSequentially('31/12/2027');
  await date.press('Tab');
  await expect(date).toHaveValue('31/12/2027');
  await selectOption(page, 'application-accessibility-mobile', 'Sí');
  await expect(mobileName(page)).toBeEnabled();
  await expect(section(page).getByText(MOBILE_NAME_UNAVAILABLE)).toHaveCount(0);
  await mobileName(page).fill(' App CAIB ');
  await publicUrl(page).fill('https://invai.caib.es/app');
  await expect(section(page).getByRole('link', { name: PUBLIC_URL_LINK })).toHaveAttribute(
    'href',
    'https://invai.caib.es/app',
  );
  await inaccessibleContentEditor(page).click();
  await inaccessibleContentEditor(page).pressSequentially('Formularis');
  await observationsEditor(page).click();
  await observationsEditor(page).pressSequentially('Revisat');
  await save(page);

  await expect(toast(page, SAVED)).toBeVisible();
  await expect(page.getByRole('button', { name: EDIT, exact: true })).toBeVisible();
  const created = {
    applicationId: 1,
    complianceId: 21,
    classificationSegmentId: 12,
    publicUrl: 'https://invai.caib.es/app',
    mobileApplication: true,
    mobileApplicationName: 'App CAIB',
    nonAccessibleContent: '<p>Formularis</p>',
    observations: '<p>Revisat</p>',
    expireDate: '2027-12-31T00:00:00',
  };
  expect(mock.writes).toEqual([
    { method: 'POST', path: 'application/accessibility', body: created },
  ]);
  await expect(value(page, 'Segment de classificació')).toHaveText('Segment B');
  await expect(value(page, 'Caducitat del darrer informe')).toHaveText('31/12/2027');
  await expect(value(page, 'Nom aplicació mòbil')).toHaveText('App CAIB');

  await startEditing(page);
  await selectOption(page, 'application-accessibility-mobile', 'No');
  await expect(mobileName(page)).toBeDisabled();
  await expect(mobileName(page)).toHaveValue('');
  await expect(mobileName(page)).toHaveAccessibleDescription(MOBILE_NAME_UNAVAILABLE);
  await save(page);

  await expect(page.getByRole('button', { name: EDIT, exact: true })).toBeVisible();
  expect(mock.writes).toEqual([
    { method: 'POST', path: 'application/accessibility', body: created },
    {
      method: 'PUT',
      path: 'application/accessibility/7',
      body: { ...created, mobileApplication: false, mobileApplicationName: null },
    },
  ]);
  await expect(value(page, 'Aplicació mòbil')).toHaveText('No');
  await expect(value(page, 'Nom aplicació mòbil')).toHaveText(NO_VALUE);
});

test('validates the public URL and the length of the texts before saving', async ({ page }) => {
  const mock = await openAccessibilityTab(page);
  await startEditing(page);

  for (const invalid of ['ftp://files.caib.es', 'www.caib.es']) {
    await publicUrl(page).fill(invalid);
    await expect(publicUrl(page)).toHaveAttribute('aria-invalid', 'true');
    await expect(publicUrl(page)).toHaveAccessibleDescription(URL_ERROR);
    await expect(section(page).getByRole('link', { name: PUBLIC_URL_LINK })).toHaveCount(0);
  }
  await save(page);
  await expect(page.getByRole('button', { name: SAVE, exact: true })).toBeVisible();

  const longUrl = `https://invai.caib.es/${'a'.repeat(300)}`;
  await publicUrl(page).fill(longUrl);
  await expect(publicUrl(page)).toHaveValue(longUrl.slice(0, 255));
  await expect(publicUrl(page)).toHaveAttribute('aria-invalid', 'false');
  await mobileName(page).fill('n'.repeat(300));
  await expect(mobileName(page)).toHaveValue('n'.repeat(255));

  // Rich texts are limited to the 4000 bytes of their stored HTML.
  await observationsEditor(page).fill('a'.repeat(4001));
  await expect(observationsEditor(page)).toHaveAttribute('aria-invalid', 'true');
  await expect(observationsEditor(page)).toHaveAccessibleDescription(TEXT_TOO_LONG);
  await save(page);
  await expect(page.getByRole('button', { name: SAVE, exact: true })).toBeVisible();
  expect(mock.writes).toEqual([]);

  await observationsEditor(page).fill('Correcte');
  await expect(section(page).getByText(TEXT_TOO_LONG)).toHaveCount(0);
  await save(page);

  await expect(toast(page, SAVED)).toBeVisible();
  expect(mock.writes).toEqual([
    {
      method: 'PUT',
      path: 'application/accessibility/7',
      body: {
        ...SAVED_PAYLOAD,
        publicUrl: longUrl.slice(0, 255),
        mobileApplicationName: 'n'.repeat(255),
        observations: '<p>Correcte</p>',
      },
    },
  ]);
});

test('keeps a saved catalog value that is cleared and explains why', async ({ page }) => {
  const mock = await openAccessibilityTab(page);
  await startEditing(page);

  for (const [id, label] of [
    ['application-accessibility-classification', 'Segment A'],
    ['application-accessibility-compliance', 'Parcialment conforme'],
  ]) {
    await page.locator(`p-select:has(#${id}) .p-select-clear-icon`).click();
    const message = toast(page, CLEAR_BLOCKED);
    await expect(message).toBeVisible();
    await expect(message.locator('.p-toast-summary')).toHaveText('Informació');
    await expect(page.locator(`#${id}`)).toHaveText(label);
    await message.getByRole('button').click();
    await expect(message).toHaveCount(0);
  }

  await selectOption(page, 'application-accessibility-classification', 'Segment B');
  await save(page);
  await expect(toast(page, SAVED)).toBeVisible();
  expect(mock.writes).toEqual([
    {
      method: 'PUT',
      path: 'application/accessibility/7',
      body: { ...SAVED_PAYLOAD, classificationSegmentId: 12 },
    },
  ]);
});

const LOAD_PROBLEMS = [
  {
    title: 'a failed load',
    options: { recordFailures: [500] },
    message: "No s'han pogut carregar les dades d'accessibilitat. Torna-ho a provar.",
    recovers: true,
  },
  {
    title: 'a forbidden load',
    options: { recordFailures: [403] },
    message: "El teu perfil no té permisos per fer aquesta operació d'accessibilitat.",
    recovers: true,
  },
  {
    title: 'a deleted record',
    options: { record: { deletedAt: '2026-09-01T00:00:00' } },
    message: "El registre d'accessibilitat està donat de baixa i no es pot modificar.",
    recovers: false,
  },
];

for (const problem of LOAD_PROBLEMS) {
  test(`blocks editing after ${problem.title} and reloads the record on retry`, async ({
    page,
  }) => {
    const mock = await openAccessibilityTab(page, problem.options);

    const error = toast(page, problem.message);
    await expect(error).toBeVisible();
    await expect(error.locator('.p-toast-summary')).toHaveText('Error');
    await expect(section(page).getByText(problem.message)).toBeVisible();
    await page.getByRole('button', { name: EDIT, exact: true }).click();
    await expect(
      page.locator('.p-toast-message-info').filter({ hasText: problem.message }),
    ).toBeVisible();
    await expect(page.getByRole('button', { name: SAVE, exact: true })).toHaveCount(0);

    await section(page).getByRole('button', { name: RETRY_RECORD }).click();
    await expect
      .poll(() => mock.reads.filter((read) => read === 'application/accessibility/7').length)
      .toBe(2);
    if (problem.recovers) {
      await expect(section(page).getByText(problem.message)).toHaveCount(0);
      await expect(section(page).getByRole('button', { name: RETRY_RECORD })).toHaveCount(0);
      await expect(value(page, 'Segment de classificació')).toHaveText('Segment A');
      await startEditing(page);
    } else {
      await expect(section(page).getByText(problem.message)).toBeVisible();
      await expect(section(page).getByRole('button', { name: RETRY_RECORD })).toBeEnabled();
    }
    expect(mock.writes).toEqual([]);
  });
}

const CATALOG_PROBLEMS = [
  { status: 500, toast: CATALOG_FAILED },
  {
    status: 403,
    toast: "El teu perfil no té permisos per consultar els catàlegs d'accessibilitat.",
  },
];

for (const problem of CATALOG_PROBLEMS) {
  test(`shows a catalog answering ${problem.status} as text and retries only that catalog`, async ({
    page,
  }) => {
    const mock = await openAccessibilityTab(page, {
      catalogFailures: { 'classification-segment': [problem.status] },
    });

    await expect(toast(page, problem.toast)).toBeVisible();
    await expect(section(page).getByText(CATALOG_FAILED)).toBeVisible();
    await expect(value(page, 'Segment de classificació')).toHaveText('Segment A');

    await startEditing(page);
    await expect(page.locator('#application-accessibility-classification')).toHaveCount(0);
    await expect(value(page, 'Segment de classificació')).toHaveText('Segment A');
    await expect(page.locator('#application-accessibility-compliance')).toHaveText(
      'Parcialment conforme',
    );

    await section(page).getByRole('button', { name: RETRY_CATALOGS }).click();
    await expect(section(page).getByText(CATALOG_FAILED)).toHaveCount(0);
    await expect(page.locator('#application-accessibility-classification')).toHaveText('Segment A');
    expect(mock.reads.filter((read) => read === 'classification-segment')).toHaveLength(2);
    expect(mock.reads.filter((read) => read === 'compliance-situation')).toHaveLength(1);
  });
}

test('keeps the tab read-only for an inactive application', async ({ page }) => {
  await openAccessibilityTab(page, { status: 'INACTIVE' });

  await expect(value(page, 'Segment de classificació')).toHaveText('Segment A');
  await expect(page.getByRole('button', { name: EDIT, exact: true })).toHaveCount(0);
  await expect(publicUrl(page)).not.toBeEditable();
  await expect(section(page).getByRole('combobox')).toHaveCount(0);
  await expect(section(page).locator('.ql-editor')).toHaveCount(0);
  await expect(section(page).getByRole('link', { name: PUBLIC_URL_LINK })).toHaveAttribute(
    'href',
    RECORD.publicUrl,
  );
});
