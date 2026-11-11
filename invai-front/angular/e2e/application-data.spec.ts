import type { Page } from '@playwright/test';

import {
  ANSWERING_URL as OPEN_DATA_URL,
  DATA_API,
  type DataRecord,
  type DataRequest,
  prepareDataAnchor,
  recordDataRequests,
} from './support/application-data';
import { type DisposableApplication, expect, test } from './support/application.fixture';
import { failApiRequest } from './support/http';

const LOAD_ERROR = 'No se ha encontrado ningún anclaje de datos con el identificador especificado.';
const NEW_TAB = "(s'obre en una pestanya nova)";

interface DataTab {
  record: DataRecord;
  checks: string[];
  requests: DataRequest[];
}

async function openDataTab(
  page: Page,
  application: DisposableApplication,
  { openDataUrl = OPEN_DATA_URL, failLoad = false } = {},
): Promise<DataTab> {
  const record = await prepareDataAnchor(page, application, { openDataUrl });
  const { requests, checks } = recordDataRequests(page);
  if (failLoad) {
    await failApiRequest(page, 'GET', `${DATA_API}/${record.id}`, {
      status: 400,
      json: { error: 'Error de validació', message: LOAD_ERROR },
    });
  }
  await page.goto(`/aplicacions/${application.id}/data`);
  await expect(page.getByRole('heading', { name: 'Dades', exact: true })).toBeVisible();
  return { record, checks, requests };
}

// The writes of the anchor, with the source each one sets ('data' for the observation).
function dataWrites({ record, requests }: DataTab) {
  const anchorPath = `application/data/${record.id}`;
  return requests
    .filter(({ method }) => method === 'PUT')
    .map(({ path, body }) => ({ path: path.slice(anchorPath.length + 1) || 'data', body }));
}

test('shows the URL of each source as a link and warns about one that does not answer', async ({
  page,
  disposableApplication,
}, testInfo) => {
  const { record } = await openDataTab(page, disposableApplication);

  const openData = page.getByRole('region', { name: 'Dades obertes' });
  const reuse = page.getByRole('region', { name: 'Reutilització' });
  const openDataLink = openData.getByRole('link', { name: `${OPEN_DATA_URL} ${NEW_TAB}` });
  await expect(openDataLink).toHaveAttribute('href', OPEN_DATA_URL);
  await expect(openDataLink).toHaveAttribute('target', '_blank');
  await expect(openData.getByRole('status')).toHaveText('Aquesta URL respon.');
  await expect(openData.getByText('Aquesta URL no respon.')).toHaveCount(0);

  await expect(reuse.getByRole('link', { name: `${record.reuseUrl} ${NEW_TAB}` })).toBeVisible();
  await expect(
    reuse.getByText('Aquesta URL no respon. Edita les dades per indicar-ne una manualment.'),
  ).toBeVisible();
  await expect(page.getByRole('checkbox')).toHaveCount(0);
  await expect(page.locator('.application-data__observations')).toContainText(
    'Notes de dades obertes',
  );
  await page.screenshot({ path: testInfo.outputPath('data-view.png'), fullPage: true });
});

test('does not check the URLs again when the tab is opened again', async ({
  page,
  disposableApplication,
}) => {
  const tab = await openDataTab(page, disposableApplication);
  const openData = page.getByRole('region', { name: 'Dades obertes' });
  await expect(openData.getByRole('status')).toHaveText('Aquesta URL respon.');
  expect(tab.checks).toEqual([OPEN_DATA_URL, tab.record.reuseUrl]);

  const tabs = page.locator('.application-detail-tabs');
  await tabs.getByRole('link', { name: /^Integracions/ }).click();
  await expect(openData).toHaveCount(0);
  await tabs.getByRole('link', { name: /^Dades/ }).click();

  await expect(openData.getByRole('status')).toHaveText('Aquesta URL respon.');
  await expect(
    page.getByRole('region', { name: 'Reutilització' }).getByText('Aquesta URL no respon.'),
  ).toBeVisible();
  expect(tab.checks).toEqual([OPEN_DATA_URL, tab.record.reuseUrl]);
});

test('saves a URL typed in by hand for one source and warns when it does not answer', async ({
  page,
  disposableApplication,
}, testInfo) => {
  const tab = await openDataTab(page, disposableApplication);

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
  expect(dataWrites(tab)).toHaveLength(0);

  await url.fill('https://reuse.example/api');
  await page.screenshot({ path: testInfo.outputPath('data-edit.png'), fullPage: true });
  await page.getByRole('button', { name: 'Desar els canvis de Dades' }).click();

  await expect(page.getByRole('button', { name: 'Editar Dades', exact: true })).toBeVisible();
  expect(dataWrites(tab)).toEqual([{ path: 'reuse', body: { url: 'https://reuse.example/api' } }]);
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

test('reflows a long URL at 320 px without horizontal scrolling', async ({
  page,
  disposableApplication,
}, testInfo) => {
  await page.setViewportSize({ width: 320, height: 800 });
  const longUrl = `https://intranet.caib.es/${'segment/'.repeat(20)}api/externa`;
  await openDataTab(page, disposableApplication, { openDataUrl: longUrl });

  await expect(page.getByRole('link', { name: `${longUrl} ${NEW_TAB}` })).toBeVisible();
  const pageOverflow = await page.evaluate(
    () => document.documentElement.scrollWidth - document.documentElement.clientWidth,
  );
  expect(pageOverflow).toBeLessThanOrEqual(0);
  await page.screenshot({ path: testInfo.outputPath('data-mobile.png'), fullPage: true });
});

test('reports a load failure inside the tab and recovers on retry', async ({
  page,
  disposableApplication,
}, testInfo) => {
  await openDataTab(page, disposableApplication, { failLoad: true });

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
