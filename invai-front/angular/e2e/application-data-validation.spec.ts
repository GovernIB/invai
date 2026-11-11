import type { Page } from '@playwright/test';

import {
  ANSWERING_URL,
  type DataRecord,
  type DataRequest,
  prepareDataAnchor,
  recordDataRequests,
} from './support/application-data';
import { type DisposableApplication, expect, test } from './support/application.fixture';
import { apiUrl } from './support/http';

const NEW_TAB = "(s'obre en una pestanya nova)";
const EDIT_SECTION = 'Editar Dades';
const SAVE_SECTION = 'Desar els canvis de Dades';
const MANUAL_URL = 'Indicar la URL manualment';
const URL_HELP = 'Ha de començar per http:// o https://. En desar es comprovarà si respon.';
const PATTERN_ERROR = 'Introdueix una URL HTTP o HTTPS vàlida.';
const SAVED = "S'han desat correctament els canvis de Dades.";
const NOT_RESPONDING = 'Aquesta URL no respon.';

interface DataTab {
  record: DataRecord;
  requests: DataRequest[];
  checks: string[];
}

async function openDataTab(page: Page, application: DisposableApplication): Promise<DataTab> {
  const record = await prepareDataAnchor(page, application);
  const recorded = recordDataRequests(page);
  await gotoDataTab(page, application);
  return { record, ...recorded };
}

async function gotoDataTab(page: Page, application: DisposableApplication): Promise<void> {
  await page.goto(`/aplicacions/${application.id}/data`);
  await expect(page.getByRole('heading', { name: 'Dades', exact: true })).toBeVisible();
}

const writes = (tab: Pick<DataTab, 'requests'>) =>
  tab.requests.filter((request) => request.method !== 'GET');
const openDataRegion = (page: Page) => page.getByRole('region', { name: 'Dades obertes' });
const reuseRegion = (page: Page) => page.getByRole('region', { name: 'Reutilització' });

async function startEditing(page: Page): Promise<void> {
  await page.getByRole('button', { name: EDIT_SECTION, exact: true }).click();
  await expect(page.getByRole('button', { name: SAVE_SECTION })).toBeVisible();
}

async function save(page: Page): Promise<void> {
  await page.getByRole('button', { name: SAVE_SECTION }).click();
}

// An application without data anchor only exists among those created before the anchor did: a
// deleted anchor is still linked to its application. Its first save is covered by the unit tests
// of ApplicationDetailState and ApplicationDataSection.

test('rejects a URL typed in by hand that is not an absolute HTTP(S) URL', async ({
  page,
  disposableApplication,
}, testInfo) => {
  const tab = await openDataTab(page, disposableApplication);
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
  expect(writes(tab)).toEqual([]);

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
  expect(writes(tab)).toEqual([
    {
      method: 'PUT',
      path: `application/data/${tab.record.id}/open-data`,
      body: { url: longest },
    },
  ]);
});

test('warns about both sources at once when neither URL typed in by hand answers', async ({
  page,
  disposableApplication,
}, testInfo) => {
  const tab = await openDataTab(page, disposableApplication);
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
  expect(writes(tab)).toEqual([
    {
      method: 'PUT',
      path: `application/data/${tab.record.id}/open-data`,
      body: { url: 'https://open.example/api' },
    },
    {
      method: 'PUT',
      path: `application/data/${tab.record.id}/reuse`,
      body: { url: 'https://reuse.example/api' },
    },
  ]);
  for (const region of [openDataRegion(page), reuseRegion(page)]) {
    await expect(
      region.getByText(`${NOT_RESPONDING} Edita les dades per indicar-ne una manualment.`),
    ).toBeVisible();
  }
  await expect(page.getByRole('checkbox')).toHaveCount(0);
  await page.screenshot({ path: testInfo.outputPath('data-both-invalid.png'), fullPage: true });
});

test('saves only the observation and keeps the URLs already known', async ({
  page,
  disposableApplication,
}) => {
  const tab = await openDataTab(page, disposableApplication);
  const reads = () => tab.requests.filter((request) => request.method === 'GET').length;
  const initialReads = reads();

  // Saving without changes writes nothing and leaves edit mode.
  await startEditing(page);
  await save(page);
  await expect(page.getByRole('button', { name: EDIT_SECTION, exact: true })).toBeVisible();
  expect(writes(tab)).toEqual([]);

  await startEditing(page);
  const editor = page.locator('p-editor .ql-editor');
  await editor.click();
  await page.keyboard.press('ControlOrMeta+A');
  await editor.pressSequentially('Nova observació');
  await save(page);

  await expect(page.getByText(SAVED)).toBeVisible();
  expect(writes(tab)).toEqual([
    {
      method: 'PUT',
      path: `application/data/${tab.record.id}`,
      body: {
        applicationId: disposableApplication.id,
        observation: expect.stringMatching(/^<p>Nova(&nbsp;| )observació<\/p>$/),
      },
    },
  ]);
  expect(reads()).toBe(initialReads);
  await expect(page.locator('.application-data__observations')).toContainText('Nova observació');
  await expect(
    openDataRegion(page).getByRole('link', { name: `${ANSWERING_URL} ${NEW_TAB}` }),
  ).toBeVisible();
  await expect(
    reuseRegion(page).getByRole('link', { name: `${tab.record.reuseUrl} ${NEW_TAB}` }),
  ).toBeVisible();
});

test('keeps the tab read-only for an inactive application', async ({
  page,
  disposableApplication,
}, testInfo) => {
  const record = await prepareDataAnchor(page, disposableApplication);
  const deactivated = await page.request.delete(
    apiUrl(page, `/invaiback/application/${disposableApplication.id}`),
  );
  expect(deactivated.ok(), await deactivated.text()).toBeTruthy();
  await gotoDataTab(page, disposableApplication);

  await expect(
    openDataRegion(page).getByRole('link', { name: `${ANSWERING_URL} ${NEW_TAB}` }),
  ).toBeVisible();
  const reuse = reuseRegion(page);
  await expect(reuse.getByRole('link', { name: `${record.reuseUrl} ${NEW_TAB}` })).toBeVisible();
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
