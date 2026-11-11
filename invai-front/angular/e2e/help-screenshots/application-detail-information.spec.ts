import type { Locator, Page } from '@playwright/test';

import type { DisposableApplication } from '../support/application.fixture';
import { prepareDataAnchor, DATA_API, ANSWERING_URL } from '../support/application-data';
import { DOCUMENTATION_API, fileOf, type MimeType } from '../support/application-documentation';
import {
  createConnection, GRANTED_ROLE, OTHER_GRANTED_ROLE, SOFFID_PERSON, INTEGRATION_API,
} from '../support/application-integrations';
import { expect, test as authenticatedTest } from '../support/auth.fixture';
import { CleanupRegistry } from '../support/cleanup-registry';
import { uniqueToken } from '../support/data';
import { apiUrl, expectApiJsonResponse } from '../support/http';
import { expectScreenHelp, maskText, openApp, saveHelpImage, uiText, type HelpLocale } from './capture';
import { APPLICATION_API_PATH, createExampleApplication, findCatalogs } from './example-applications';

interface InformationExample {
  locale: HelpLocale;
  application: DisposableApplication;
  route: string;
  documentId: number;
  documentTitle: string;
  linkTitle: string;
  categoryLabel: string;
  subcategoryLabel: string;
}

function registerDelete(page: Page, cleanup: CleanupRegistry, pathname: string): void {
  cleanup.register(`help example ${pathname}`, async () => {
    const response = await page.request.delete(apiUrl(page, pathname));
    expect(response.ok(), `DELETE ${pathname}`).toBeTruthy();
  });
}

// The two tests have separate disposable examples: screenshots can be generated first, then the
// dev server restarted to discover new image files before the help validation is run.
const test = authenticatedTest.extend<{
  cleanup: CleanupRegistry;
  example: InformationExample;
}>({
  cleanup: async ({ page }, use, testInfo) => {
    const cleanup = new CleanupRegistry(testInfo);
    try { await use(cleanup); } finally { await cleanup.runAll(); }
  },
  example: async ({ page, cleanup }, use, testInfo) => {
    const locale = await openApp(page);
    const text = (ca: string, es: string) => locale === 'ca' ? ca : es;
    const token = uniqueToken(testInfo, 5);
    const created = await createExampleApplication(page, cleanup, {
      name: text('Portal de tràmits (exemple)', 'Portal de trámites (ejemplo)'),
      code: `AJ${token}`,
      catalogs: await findCatalogs(page),
    });
    const application = await expectApiJsonResponse<DisposableApplication>(
      await page.request.get(apiUrl(page, `${APPLICATION_API_PATH}/${created.id}`)),
      `GET ${APPLICATION_API_PATH}/${created.id}`,
    );
    expect(application.appDataId).toBeGreaterThan(0);
    expect(application.appIntegrationId).toBeGreaterThan(0);

    const createCategory = async (name: string, nameEs: string, parentId: number | null = null) => {
      const pathname = '/invaiback/documentation/category';
      const category = await expectApiJsonResponse<{ id: number }>(
        await page.request.post(apiUrl(page, pathname), { data: { name, nameEs, parentId } }),
        `POST ${pathname}`,
      );
      registerDelete(page, cleanup, `${pathname}/${category.id}`);
      return category;
    };
    const categoryLabel = text(`Ajuda ${token}`, `Ayuda ${token}`);
    const subcategoryLabel = text(`Manuals ${token}`, `Manuales ${token}`);
    const category = await createCategory(`Ajuda ${token}`, `Ayuda ${token}`);
    const subcategory = await createCategory(`Manuals ${token}`, `Manuales ${token}`, category.id);
    const [statuses, mimeTypes] = await Promise.all([
      expectApiJsonResponse<Array<{ id: number; code: string }>>(
        await page.request.get(apiUrl(page, '/invaiback/document-status')), 'GET /document-status',
      ),
      expectApiJsonResponse<MimeType[]>(
        await page.request.get(apiUrl(page, '/invaiback/mime-type')), 'GET /mime-type',
      ),
    ]);
    const status = statuses.find(({ code }) => code === 'ACTIVE');
    expect(status, 'A current document status exists').toBeDefined();
    const documentTitle = text("Manual d'ús (exemple)", 'Manual de uso (ejemplo)');
    const document = await expectApiJsonResponse<{ id: number }>(
      await page.request.post(apiUrl(page, DOCUMENTATION_API), {
        multipart: {
          data: {
            name: 'data.json', mimeType: 'application/json',
            buffer: Buffer.from(JSON.stringify({
              applicationId: application.id, title: documentTitle,
              categoryId: category.id, subcategoryId: subcategory.id,
              statusId: status!.id, versionLabel: '1.0',
            })),
          },
          file: fileOf(mimeTypes, 'manual-1.txt', 'Document de prova. Versio 1.0.'),
        },
      }),
      `POST ${DOCUMENTATION_API}`,
    );
    registerDelete(page, cleanup, `${DOCUMENTATION_API}/${document.id}`);
    await expectApiJsonResponse(
      await page.request.post(apiUrl(page, `${DOCUMENTATION_API}/${document.id}`), {
        multipart: {
          data: {
            name: 'data.json', mimeType: 'application/json',
            buffer: Buffer.from(JSON.stringify({ versionLabel: '2.0' })),
          },
          file: fileOf(mimeTypes, 'manual-2.txt', 'Document de prova. Versio 2.0.'),
        },
      }),
      `POST ${DOCUMENTATION_API}/${document.id}`,
    );
    const linkTitle = text("Guia de l'aplicació", 'Guía de la aplicación');
    const link = await expectApiJsonResponse<{ id: number }>(
      await page.request.post(apiUrl(page, `${DOCUMENTATION_API}/link`), {
        data: {
          applicationId: application.id, categoryId: category.id, title: linkTitle,
          description: text("Guia d'exemple per a les persones usuàries", 'Guía de ejemplo para las personas usuarias'),
          url: 'https://example.test/guia',
        },
      }),
      `POST ${DOCUMENTATION_API}/link`,
    );
    registerDelete(page, cleanup, `${DOCUMENTATION_API}/link/${link.id}`);
    await use({
      locale, application, documentId: document.id, documentTitle, linkTitle,
      categoryLabel, subcategoryLabel,
      route: `/${uiText(locale, 'aplicacions')}/${application.id}`,
    });
  },
});

test.use({ actionTimeout: 15_000 });

async function maskCategories(target: Locator, example: InformationExample): Promise<void> {
  const { locale, categoryLabel, subcategoryLabel } = example;
  // The document detail can fall back to the Catalan category names even in the Spanish build.
  for (const label of new Set([categoryLabel, categoryLabel.replace(/^Ayuda /, 'Ajuda ')])) {
    await maskText(target.getByText(label, { exact: true }), locale === 'ca' ? "Tècnica d'exemple" : 'Técnica de ejemplo');
  }
  for (const label of new Set([subcategoryLabel, subcategoryLabel.replace(/^Manuales /, 'Manuals ')])) {
    await maskText(target.getByText(label, { exact: true }), locale === 'ca' ? 'Manuals' : 'Manuales');
  }
}

test('captures Dades, Integracions, Documentació and a document detail', async ({
  page, example, cleanup,
}) => {
  test.setTimeout(180_000);
  const { locale, application, route, documentId, documentTitle, linkTitle } = example;
  const text = (ca: string, es: string) => locale === 'ca' ? ca : es;
  await prepareDataAnchor(page, application, {
    observation: `<p>${text('Dades obertes i reutilització revisades.', 'Datos abiertos y reutilización revisados.')}</p>`,
  });
  await expectApiJsonResponse(
    await page.request.put(apiUrl(page, `${DATA_API}/${application.appDataId}/reuse`), {
      data: { url: ANSWERING_URL },
    }), `PUT ${DATA_API}/${application.appDataId}/reuse`,
  );
  await page.goto(`${route}/data`);
  await expect(page.locator('.app-initial-loader')).toBeHidden({ timeout: 30_000 });
  const data = page.locator('app-application-data-section');
  await expect(data.getByText(uiText(locale, 'Aquesta URL respon.'), { exact: true }))
    .toHaveCount(2, { timeout: 30_000 });
  await saveHelpImage(page, data, locale, 'application-detail/imgs/data.webp');

  await expectApiJsonResponse(
    await page.request.put(apiUrl(page, `${INTEGRATION_API}/${application.appIntegrationId}`), {
      data: { applicationId: application.id, observation: `<p>${text('Connexió de prova revisada.', 'Conexión de prueba revisada.')}</p>` },
    }), `PUT ${INTEGRATION_API}/${application.appIntegrationId}`,
  );
  await createConnection(page, cleanup, application, [GRANTED_ROLE, OTHER_GRANTED_ROLE]);
  await page.goto(`${route}/integrations`);
  await expect(page.locator('.app-initial-loader')).toBeHidden({ timeout: 30_000 });
  const integrations = page.locator('app-application-integrations-section');
  const userCell = integrations.getByRole('cell', { name: SOFFID_PERSON.userName, exact: true });
  await expect(userCell).toBeVisible();
  await maskText(userCell, 'usuari.exemple');
  await expect(integrations.getByRole('cell', { name: SOFFID_PERSON.userName, exact: true })).toHaveCount(0);
  await saveHelpImage(page, integrations, locale, 'application-detail/imgs/integrations.webp');

  await page.goto(`${route}/documentation`);
  await expect(page.locator('.app-initial-loader')).toBeHidden({ timeout: 30_000 });
  const documentation = page.locator('app-application-documentation-section');
  await expect(documentation.getByRole('cell', { name: documentTitle, exact: true })).toBeVisible();
  await expect(documentation.getByRole('cell', { name: linkTitle, exact: true })).toBeVisible();
  await maskCategories(documentation, example);
  await saveHelpImage(page, documentation, locale, 'application-detail/imgs/documentation.webp');

  await documentation.getByRole('button', { name: uiText(locale, 'Envia adjunts per correu') }).click();
  const email = page.getByRole('dialog').filter({
    has: page.getByRole('heading', { name: uiText(locale, 'Envia per correu'), exact: true }),
  });
  await expect(email).toBeVisible();
  const recipient = email.getByLabel(uiText(locale, 'Destinataris'), { exact: true });
  await recipient.fill('destinatari@example.test');
  await recipient.press('Enter');
  await email.getByLabel(uiText(locale, 'Assumpte'), { exact: true }).fill(text("Documentació d'exemple", 'Documentación de ejemplo'));
  await email.getByRole('checkbox').check();
  await expect(email.locator('tbody').getByRole('combobox')).toBeVisible();
  await expect(email.locator('tbody .p-select-loading-icon')).toHaveCount(0);
  // Capture the filled dialog; sending e-mail is outside the screenshot workflow.
  await saveHelpImage(page, email, locale, 'application-detail/imgs/documentation-email.webp');
  await email.getByRole('button', {
    name: uiText(locale, "Cancel·la l'enviament per correu"), exact: true,
  }).click();
  await expect(email).toBeHidden();

  await page.goto(`${route}/documentation/${documentId}`);
  await expect(page.locator('.app-initial-loader')).toBeHidden({ timeout: 30_000 });
  const detail = page.locator('app-application-document-detail app-section-container');
  const versions = detail.locator('app-application-document-versions-table');
  await expect(versions.locator('tbody tr')).toHaveCount(2);
  await maskCategories(detail, example);
  await maskText(versions.locator('tbody tr td:nth-child(3)'), text("Usuari d'exemple", 'Usuario de ejemplo'));
  await saveHelpImage(page, detail, locale, 'application-detail/imgs/document-detail.webp');
});

test('validates the localized information help, images, focus and reflow', async ({ page, example }) => {
  test.setTimeout(120_000);
  const { locale, route, documentId } = example;
  for (const [screen, host, document] of [
    ['data', 'app-application-data-section', 'data.md'],
    ['integrations', 'app-application-integrations-section', 'integrations.md'],
    ['documentation', 'app-application-documentation-section', 'documentation.md'],
    [`documentation/${documentId}`, 'app-application-document-detail', 'document-detail.md'],
  ]) {
    await page.goto(`${route}/${screen}`);
    await expect(page.locator('.app-initial-loader')).toBeHidden({ timeout: 30_000 });
    await expect(page.locator(host)).toBeVisible();
    await expectScreenHelp(page, locale, `application-detail/${document}`);
  }
});
