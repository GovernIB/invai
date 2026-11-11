import type { Page } from '@playwright/test';

import { type DisposableApplication, expect, test } from './support/application.fixture';
import { apiUrl, expectApiJsonResponse, failApiRequest } from './support/http';
import { selectOption } from './support/ui';

const ACCESSIBILITY_API = '/invaiback/application/accessibility';
const PUBLIC_URL = 'https://www.caib.es/invai';

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
const CATALOG_FAILED = "No s'han pogut carregar els catàlegs d'accessibilitat. Torna-ho a provar.";

type Catalog = 'classification-segment' | 'compliance-situation';

interface AccessibilityTab {
  recordPath: string;
  segments: Record<string, number>;
  compliances: Record<string, number>;
  writes: { method: string; path: string; body: unknown }[];
  reads: string[];
  catalogParams: URLSearchParams[];
}

async function catalogIds(page: Page, catalog: Catalog): Promise<Record<string, number>> {
  const body = await expectApiJsonResponse<{ content: Array<{ id: number; name: string }> }>(
    await page.request.get(apiUrl(page, `/invaiback/${catalog}`), {
      params: { page: 0, size: 100, statusId: 1 },
    }),
    `GET /invaiback/${catalog}`,
  );
  return Object.fromEntries(body.content.map(({ id, name }) => [name, id]));
}

// The record the tests start from, saved through the API on the record of the application.
function savedPayload(application: DisposableApplication, tab: AccessibilityTab) {
  return {
    applicationId: application.id,
    complianceId: tab.compliances['Parcialment'],
    classificationSegmentId: tab.segments['Segment I'],
    publicUrl: PUBLIC_URL,
    mobileApplication: true,
    mobileApplicationName: 'InvAI mòbil',
    nonAccessibleContent: '<p>Vídeos sense subtítols</p>',
    observations: '<p>Revisió anual</p>',
    expireDate: '2027-03-15T00:00:00',
  };
}

async function openAccessibilityTab(
  page: Page,
  application: DisposableApplication,
  {
    filled = true,
    before,
  }: { filled?: boolean; before?: (tab: AccessibilityTab) => Promise<void> } = {},
): Promise<AccessibilityTab> {
  const recordPath = `${ACCESSIBILITY_API}/${application.appAccessibilityId}`;
  const [segments, compliances] = await Promise.all([
    catalogIds(page, 'classification-segment'),
    catalogIds(page, 'compliance-situation'),
  ]);
  const tab: AccessibilityTab = {
    recordPath,
    segments,
    compliances,
    writes: [],
    reads: [],
    catalogParams: [],
  };
  if (filled) {
    await expectApiJsonResponse(
      await page.request.put(apiUrl(page, recordPath), { data: savedPayload(application, tab) }),
      `PUT ${recordPath}`,
    );
  }
  await before?.(tab);
  page.on('request', (request) => {
    const url = new URL(request.url());
    const catalog = url.pathname.match(/\/(classification-segment|compliance-situation)$/)?.[1];
    if (catalog) {
      tab.reads.push(catalog);
      tab.catalogParams.push(url.searchParams);
    } else if (url.pathname.startsWith(ACCESSIBILITY_API)) {
      const path = url.pathname.replace('/invaiback/', '');
      if (request.method() === 'GET') tab.reads.push(path);
      else tab.writes.push({ method: request.method(), path, body: request.postDataJSON() });
    }
  });
  await page.goto(`/aplicacions/${application.id}/accessibility`);
  await expect(page.getByRole('heading', { name: SECTION, exact: true })).toBeVisible();
  return tab;
}

const recordRead = (tab: AccessibilityTab) => tab.recordPath.replace('/invaiback/', '');

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
  disposableApplication,
}) => {
  const tab = await openAccessibilityTab(page, disposableApplication);

  await expect(value(page, 'Segment de classificació')).toHaveText('Segment I');
  await expect(value(page, 'Situació de compliment')).toHaveText('Parcialment');
  await expect(value(page, 'Caducitat del darrer informe')).toHaveText('15/03/2027');
  await expect(value(page, 'Aplicació mòbil')).toHaveText('Sí');
  await expect(value(page, 'Nom aplicació mòbil')).toHaveText('InvAI mòbil');
  await expect(richTextValue(page, 'Contingut no accessible')).toHaveText('Vídeos sense subtítols');
  await expect(richTextValue(page, 'Observacions')).toHaveText('Revisió anual');
  await expect(publicUrl(page)).toHaveValue(PUBLIC_URL);
  await expect(publicUrl(page)).not.toBeEditable();
  const link = section(page).getByRole('link', { name: PUBLIC_URL_LINK });
  await expect(link).toHaveAttribute('href', PUBLIC_URL);
  await expect(link).toHaveAttribute('target', '_blank');
  await expect(section(page).getByRole('combobox')).toHaveCount(0);
  await expect(section(page).locator('.ql-editor')).toHaveCount(0);
  expect([...tab.reads].sort()).toEqual([
    recordRead(tab),
    'classification-segment',
    'compliance-situation',
  ]);
  for (const params of tab.catalogParams) {
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

// The record is created with its application, empty: creating it with a POST when an older
// application has none is covered by the unit tests of ApplicationDetailState.
test('fills the empty record on the first save and updates it afterwards', async ({
  page,
  disposableApplication,
}) => {
  const tab = await openAccessibilityTab(page, disposableApplication, { filled: false });

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
  await selectOption(page, 'application-accessibility-classification', 'Segment II');
  await selectOption(page, 'application-accessibility-compliance', 'Totalment');
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
    applicationId: disposableApplication.id,
    complianceId: tab.compliances['Totalment'],
    classificationSegmentId: tab.segments['Segment II'],
    publicUrl: 'https://invai.caib.es/app',
    mobileApplication: true,
    mobileApplicationName: 'App CAIB',
    nonAccessibleContent: '<p>Formularis</p>',
    observations: '<p>Revisat</p>',
    expireDate: '2027-12-31T00:00:00',
  };
  expect(tab.writes).toEqual([{ method: 'PUT', path: recordRead(tab), body: created }]);
  await expect(value(page, 'Segment de classificació')).toHaveText('Segment II');
  await expect(value(page, 'Caducitat del darrer informe')).toHaveText('31/12/2027');
  await expect(value(page, 'Nom aplicació mòbil')).toHaveText('App CAIB');

  await startEditing(page);
  await selectOption(page, 'application-accessibility-mobile', 'No');
  await expect(mobileName(page)).toBeDisabled();
  await expect(mobileName(page)).toHaveValue('');
  await expect(mobileName(page)).toHaveAccessibleDescription(MOBILE_NAME_UNAVAILABLE);
  await save(page);

  await expect(page.getByRole('button', { name: EDIT, exact: true })).toBeVisible();
  expect(tab.writes).toEqual([
    { method: 'PUT', path: recordRead(tab), body: created },
    {
      method: 'PUT',
      path: recordRead(tab),
      body: { ...created, mobileApplication: false, mobileApplicationName: null },
    },
  ]);
  await expect(value(page, 'Aplicació mòbil')).toHaveText('No');
  await expect(value(page, 'Nom aplicació mòbil')).toHaveText(NO_VALUE);
});

test('validates the public URL and the length of the texts before saving', async ({
  page,
  disposableApplication,
}) => {
  const tab = await openAccessibilityTab(page, disposableApplication);
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
  expect(tab.writes).toEqual([]);

  await observationsEditor(page).fill('Correcte');
  await expect(section(page).getByText(TEXT_TOO_LONG)).toHaveCount(0);
  await save(page);

  await expect(toast(page, SAVED)).toBeVisible();
  expect(tab.writes).toEqual([
    {
      method: 'PUT',
      path: recordRead(tab),
      body: {
        ...savedPayload(disposableApplication, tab),
        publicUrl: longUrl.slice(0, 255),
        mobileApplicationName: 'n'.repeat(255),
        observations: '<p>Correcte</p>',
      },
    },
  ]);
});

test('keeps a saved catalog value that is cleared and explains why', async ({
  page,
  disposableApplication,
}) => {
  const tab = await openAccessibilityTab(page, disposableApplication);
  await startEditing(page);

  for (const [id, label] of [
    ['application-accessibility-classification', 'Segment I'],
    ['application-accessibility-compliance', 'Parcialment'],
  ]) {
    await page.locator(`p-select:has(#${id}) .p-select-clear-icon`).click();
    const message = toast(page, CLEAR_BLOCKED);
    await expect(message).toBeVisible();
    await expect(message.locator('.p-toast-summary')).toHaveText('Informació');
    await expect(page.locator(`#${id}`)).toHaveText(label);
    await message.getByRole('button').click();
    await expect(message).toHaveCount(0);
  }

  await selectOption(page, 'application-accessibility-classification', 'Segment II');
  await save(page);
  await expect(toast(page, SAVED)).toBeVisible();
  expect(tab.writes).toEqual([
    {
      method: 'PUT',
      path: recordRead(tab),
      body: {
        ...savedPayload(disposableApplication, tab),
        classificationSegmentId: tab.segments['Segment II'],
      },
    },
  ]);
});

// Failed and forbidden loads answer only the first read of the record with the error; a deleted
// record is deleted for real.
const LOAD_PROBLEMS: Array<{ title: string; status?: number; message: string; recovers: boolean }> =
  [
    {
      title: 'a failed load',
      status: 500,
      message: "No s'han pogut carregar les dades d'accessibilitat. Torna-ho a provar.",
      recovers: true,
    },
    {
      title: 'a forbidden load',
      status: 403,
      message: "El teu perfil no té permisos per fer aquesta operació d'accessibilitat.",
      recovers: true,
    },
    {
      title: 'a deleted record',
      message: "El registre d'accessibilitat està donat de baixa i no es pot modificar.",
      recovers: false,
    },
  ];

for (const problem of LOAD_PROBLEMS) {
  test(`blocks editing after ${problem.title} and reloads the record on retry`, async ({
    page,
    disposableApplication,
  }) => {
    const tab = await openAccessibilityTab(page, disposableApplication, {
      before: async ({ recordPath }) => {
        if (problem.status) {
          await failApiRequest(page, 'GET', recordPath, {
            status: problem.status,
            json: { message: 'Record test failure' },
          });
          return;
        }
        const deleted = await page.request.delete(apiUrl(page, recordPath));
        expect(deleted.ok(), await deleted.text()).toBeTruthy();
      },
    });

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
    await expect.poll(() => tab.reads.filter((read) => read === recordRead(tab)).length).toBe(2);
    if (problem.recovers) {
      await expect(section(page).getByText(problem.message)).toHaveCount(0);
      await expect(section(page).getByRole('button', { name: RETRY_RECORD })).toHaveCount(0);
      await expect(value(page, 'Segment de classificació')).toHaveText('Segment I');
      await startEditing(page);
    } else {
      await expect(section(page).getByText(problem.message)).toBeVisible();
      await expect(section(page).getByRole('button', { name: RETRY_RECORD })).toBeEnabled();
    }
    expect(tab.writes).toEqual([]);
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
    disposableApplication,
  }) => {
    const tab = await openAccessibilityTab(page, disposableApplication, {
      before: async () => {
        await failApiRequest(page, 'GET', '/invaiback/classification-segment', {
          status: problem.status,
          json: { message: 'Catalog test failure' },
        });
      },
    });

    await expect(toast(page, problem.toast)).toBeVisible();
    await expect(section(page).getByText(CATALOG_FAILED)).toBeVisible();
    await expect(value(page, 'Segment de classificació')).toHaveText('Segment I');

    await startEditing(page);
    await expect(page.locator('#application-accessibility-classification')).toHaveCount(0);
    await expect(value(page, 'Segment de classificació')).toHaveText('Segment I');
    await expect(page.locator('#application-accessibility-compliance')).toHaveText('Parcialment');

    await section(page).getByRole('button', { name: RETRY_CATALOGS }).click();
    await expect(section(page).getByText(CATALOG_FAILED)).toHaveCount(0);
    await expect(page.locator('#application-accessibility-classification')).toHaveText('Segment I');
    expect(tab.reads.filter((read) => read === 'classification-segment')).toHaveLength(2);
    expect(tab.reads.filter((read) => read === 'compliance-situation')).toHaveLength(1);
  });
}

test('keeps the tab read-only for an inactive application', async ({
  page,
  disposableApplication,
}) => {
  await openAccessibilityTab(page, disposableApplication, {
    before: async () => {
      const deactivated = await page.request.delete(
        apiUrl(page, `/invaiback/application/${disposableApplication.id}`),
      );
      expect(deactivated.ok(), await deactivated.text()).toBeTruthy();
    },
  });

  await expect(value(page, 'Segment de classificació')).toHaveText('Segment I');
  await expect(page.getByRole('button', { name: EDIT, exact: true })).toHaveCount(0);
  await expect(publicUrl(page)).not.toBeEditable();
  await expect(section(page).getByRole('combobox')).toHaveCount(0);
  await expect(section(page).locator('.ql-editor')).toHaveCount(0);
  await expect(section(page).getByRole('link', { name: PUBLIC_URL_LINK })).toHaveAttribute(
    'href',
    PUBLIC_URL,
  );
});
