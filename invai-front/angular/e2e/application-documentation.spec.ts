import AxeBuilder from '@axe-core/playwright';
import type { Page } from '@playwright/test';

import {
  type Documentation,
  DOCUMENTATION_API,
  recordDocumentationWrites,
  seedDocumentation,
} from './support/application-documentation';
import { signedInUsername } from './support/application-security';
import { type DisposableApplication, expect, test } from './support/application.fixture';
import type { CleanupRegistry } from './support/cleanup-registry';
import { apiUrl, expectJsonResponse, failApiRequest, waitForApiResponse } from './support/http';
import { selectOption } from './support/ui';

interface DocumentationTab {
  documentation: Documentation;
  writes: ReturnType<typeof recordDocumentationWrites>;
}

// Seeds the documents and links of the application and opens its Documentació tab.
async function openDocumentationTab(
  page: Page,
  cleanup: CleanupRegistry,
  application: DisposableApplication,
  { inactive = false, mimeTypesAvailable = true } = {},
): Promise<DocumentationTab> {
  test.setTimeout(150_000);
  const documentation = await seedDocumentation(page, cleanup, application, test.info());
  if (inactive) {
    const deactivated = await page.request.delete(
      apiUrl(page, `/invaiback/application/${application.id}`),
    );
    expect(deactivated.ok(), await deactivated.text()).toBeTruthy();
  }
  if (!mimeTypesAvailable) {
    await failApiRequest(
      page,
      'GET',
      '/invaiback/mime-type',
      { status: 500, json: {} },
      { times: Number.POSITIVE_INFINITY },
    );
  }
  const writes = recordDocumentationWrites(page);
  await page.goto(`/aplicacions/${application.id}/documentation`);
  await expect(page.getByRole('heading', { level: 2, name: 'Documentació' })).toBeVisible();
  return { documentation, writes };
}

const attachments = (page: Page) => page.getByRole('table', { name: "Adjunts de l'aplicació" });
const links = (page: Page) => page.getByRole('table', { name: "Enllaços de l'aplicació" });
const documentUrl = (application: DisposableApplication, id: number) =>
  new RegExp(`/aplicacions/${application.id}/documentation/${id}$`);
// "dd/mm/yy", as the versions table shows the day a version was uploaded.
const today = () => {
  const now = new Date();
  const pad = (value: number) => String(value).padStart(2, '0');
  return `${pad(now.getDate())}/${pad(now.getMonth() + 1)}/${String(now.getFullYear()).slice(2)}`;
};

async function rowAction(page: Page, kind: "l'adjunt" | "l'enllaç", title: string, action: string) {
  await page.getByRole('button', { name: `Accions de ${kind} «${title}»` }).click();
  await page.getByRole('menuitem', { name: action, exact: true }).click();
}

test('shows the attachments and links of an application in its Documentació tab', async ({
  page,
  cleanupRegistry,
  disposableApplication,
}, testInfo) => {
  const { documentation } = await openDocumentationTab(
    page,
    cleanupRegistry,
    disposableApplication,
  );

  const tab = page.locator('.application-detail-tabs').getByRole('link', { name: 'Documentació' });
  await expect(tab).toHaveAttribute('aria-current', 'page');
  await expect(page.getByRole('heading', { level: 3, name: 'Adjunts' })).toBeVisible();
  await expect(page.getByRole('heading', { level: 3, name: 'Enllaços' })).toBeVisible();

  await expect(attachments(page).locator('thead th')).toHaveText([
    'Títol',
    'Tipus doc.',
    'Categoria',
    'Subcategoria',
    'Versió',
    'Estat',
    'Accions',
  ]);
  await expect(attachments(page).locator('tbody tr')).toHaveCount(10);
  const manual = attachments(page).getByRole('row', { name: /Manual API v2/ });
  await expect(manual).toContainText('PDF');
  await expect(manual).toContainText(documentation.categories.technical.name);
  await expect(manual).toContainText(documentation.categories.manuals.name);
  await expect(manual).toContainText('Vigent');
  await expect(page.getByText('Mostrant 1 a 10 de 12 registres')).toBeVisible();

  await expect(links(page).locator('tbody tr')).toHaveCount(6);
  await expect(links(page).locator('thead th')).toHaveText([
    'Títol',
    'Descripció',
    'Categoria',
    'URL',
    'Accions',
  ]);
  await expect(links(page).getByRole('row', { name: /Postman/ })).toContainText(
    documentation.categories.technical.name,
  );
  const postman = links(page).getByRole('link', {
    name: "https://www.postman.com (s'obre en una pestanya nova)",
  });
  await expect(postman).toHaveAttribute('target', '_blank');

  await page.screenshot({
    path: testInfo.outputPath('application-documentation.png'),
    fullPage: true,
  });
  const axe = await new AxeBuilder({ page })
    .withTags(['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa', 'wcag22aa'])
    .analyze();
  expect(
    axe.violations.map(({ id, nodes }) => `${id}: ${nodes.map(({ target }) => target).join(', ')}`),
  ).toEqual([]);
});

test('searches by title, filters by state and sorts the attachments', async ({
  page,
  cleanupRegistry,
  disposableApplication,
}) => {
  await openDocumentationTab(page, cleanupRegistry, disposableApplication);

  await expect(page.getByRole('textbox', { name: "Cerca ràpida d'adjunts" })).toHaveAttribute(
    'placeholder',
    'Cerca per títol',
  );
  await page.getByRole('textbox', { name: "Cerca ràpida d'adjunts" }).fill('manual');
  await expect(attachments(page).locator('tbody tr')).toHaveCount(3);
  await page.getByRole('textbox', { name: "Cerca ràpida d'adjunts" }).fill('');
  await expect(attachments(page).locator('tbody tr')).toHaveCount(10);

  await page.getByRole('button', { name: /Filtres dels adjunts/ }).click();
  await selectOption(page, 'application-attachments-filter-state', 'Obsolet');
  await page.getByRole('button', { name: /^\W*Cerca$/ }).click();
  await expect(attachments(page).locator('tbody tr')).toHaveCount(2);

  await attachments(page).getByRole('columnheader', { name: /Títol/ }).click();
  await expect(attachments(page).locator('tbody tr').first()).toContainText(
    'Documentació per als usuaris finals',
  );
  await attachments(page)
    .getByRole('columnheader', { name: /Versió/ })
    .click();
  await expect(attachments(page).locator('tbody tr').first()).toContainText('1.0');
});

test('filters the attachments by type of document, category and subcategory', async ({
  page,
  cleanupRegistry,
  disposableApplication,
}) => {
  const {
    documentation: { categories },
  } = await openDocumentationTab(page, cleanupRegistry, disposableApplication);
  const filters = page.getByRole('button', { name: /Filtres dels adjunts/ });
  const search = page.getByRole('button', { name: /^\W*Cerca$/ });
  const rows = attachments(page).locator('tbody tr');
  const subcategory = page.locator('#application-attachments-filter-subcategory');

  await filters.click();
  await expect(subcategory).toHaveAttribute('aria-disabled', 'true');

  // A category also lists the documents of its subcategories.
  await selectOption(page, 'application-attachments-filter-category', categories.technical.name);
  await search.click();
  await expect(rows).toHaveCount(8);

  await selectOption(page, 'application-attachments-filter-subcategory', categories.manuals.name);
  await selectOption(page, 'application-attachments-filter-type', 'PDF (.pdf)');
  await search.click();
  await expect(rows).toHaveCount(4);
  await expect(filters).toHaveAccessibleName(/3 filtres aplicats/);
  const axe = await new AxeBuilder({ page })
    .include('.application-documentation-list')
    .withTags(['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa', 'wcag22aa'])
    .analyze();
  expect(
    axe.violations.map(({ id, nodes }) => `${id}: ${nodes.map(({ target }) => target).join(', ')}`),
  ).toEqual([]);

  // A category without subcategories leaves none to choose.
  await selectOption(page, 'application-attachments-filter-category', categories.regulations.name);
  await expect(subcategory).toHaveAttribute('aria-disabled', 'true');
  await search.click();
  await expect(rows).toHaveCount(1);
  await expect(rows.first()).toContainText("Declaració d'accessibilitat");

  await page.getByRole('button', { name: /Neteja/ }).click();
  await expect(rows).toHaveCount(10);
  await expect(subcategory).toHaveAttribute('aria-disabled', 'true');
});

test('adds an attachment classified in an active category', async ({
  page,
  cleanupRegistry,
  disposableApplication,
}, testInfo) => {
  const { documentation, writes } = await openDocumentationTab(
    page,
    cleanupRegistry,
    disposableApplication,
  );
  const { categories } = documentation;

  await page.getByRole('button', { name: 'Afegeix un adjunt' }).click();
  const dialog = page.getByRole('dialog');
  await expect(dialog.getByRole('heading', { name: 'Afegeix un adjunt' })).toBeVisible();
  await expect(
    dialog.getByText('Tots els camps són obligatoris, excepte els indicats com a opcionals.'),
  ).toBeVisible();
  await expect(dialog.getByText('Arrossega el fitxer aquí per carregar-lo.')).toBeVisible();
  await expect(dialog.getByText('Mida màxima: 5 MB.')).toBeVisible();
  await expect(page.locator('#application-attachment-file')).toHaveAttribute(
    'accept',
    documentation.mimeTypes.map(({ extension }) => `.${extension}`).join(','),
  );
  await page.screenshot({ path: testInfo.outputPath('application-attachment-empty.png') });
  await dialog.getByRole('button', { name: 'Desa', exact: true }).click();
  // Title, category, version and file; a document is no longer filed under a section.
  await expect(dialog.getByText('Camp obligatori')).toHaveCount(4);
  await expect(page.locator('#application-attachment-section')).toHaveCount(0);

  await dialog.getByLabel('Títol', { exact: true }).fill('Pla de proves');
  await dialog.getByLabel('Versió', { exact: true }).fill('1.0');
  const upload = dialog.locator('.application-attachment-upload');
  const drop = async (name: string, type: string) => {
    const dataTransfer = await page.evaluateHandle(
      ([fileName, fileType]) => {
        const transfer = new DataTransfer();
        transfer.items.add(new File(['content'], fileName, { type: fileType }));
        return transfer;
      },
      [name, type],
    );
    await upload.dispatchEvent('dragover', { dataTransfer });
    await expect(upload).toHaveClass(/application-attachment-upload--dragging/);
    await upload.dispatchEvent('drop', { dataTransfer });
    await dataTransfer.dispose();
  };
  await drop('provisional.pdf', 'application/pdf');
  await expect(dialog.getByRole('status')).toContainText('provisional.pdf');
  // Dropping ignores the filter of the picker: a file of a format not accepted is refused and the
  // chosen one is kept.
  await drop('provisional.exe', 'application/octet-stream');
  await expect(page.locator('#application-attachment-file-error')).toHaveText(
    "No s'ha adjuntat «provisional.exe»: el format del fitxer no està admès.",
  );
  await expect(dialog.getByRole('status')).toContainText('provisional.pdf');
  await dialog.getByRole('button', { name: 'Elimina el fitxer seleccionat' }).click();
  await expect(page.locator('#application-attachment-file-error')).toHaveText('Camp obligatori');
  await expect(dialog.getByRole('button', { name: 'Adjunta', exact: true })).toBeFocused();
  const [fileChooser] = await Promise.all([
    page.waitForEvent('filechooser'),
    page.keyboard.press('Enter'),
  ]);
  await fileChooser.setFiles({
    name: 'pla-de-proves.docx',
    mimeType: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    buffer: Buffer.from('content'),
  });
  await expect(dialog.getByRole('status')).toContainText('pla-de-proves.docx');
  await expect(dialog.getByRole('status')).toContainText('Tipus de document: Word');
  await selectOption(page, 'application-attachment-category', categories.technical.name);
  await page.locator('#application-attachment-subcategory').click();
  // Only the active subcategories of the chosen category are offered.
  await expect(page.getByRole('option')).toHaveText([categories.manuals.name]);
  await page.getByRole('option', { name: categories.manuals.name }).click();
  await page.screenshot({ path: testInfo.outputPath('application-attachment-dialog.png') });
  const createPromise = waitForApiResponse(page, 'POST', DOCUMENTATION_API);
  await dialog.getByRole('button', { name: 'Desa', exact: true }).click();

  // The multipart body is not readable from the browser: the saved document tells what was sent.
  expect(await expectJsonResponse(await createPromise)).toMatchObject({
    applicationId: disposableApplication.id,
    title: 'Pla de proves',
    category: { id: categories.technical.id },
    subcategory: { id: categories.manuals.id },
    status: { id: documentation.statuses['ACTIVE'] },
    attachments: [{ versionLabel: '1.0', fileName: 'pla-de-proves.docx' }],
  });
  await expect(dialog).toHaveCount(0);
  expect(writes).toContainEqual({ method: 'POST', path: '/application/documentation' });
  await page.getByRole('textbox', { name: "Cerca ràpida d'adjunts" }).fill('pla de proves');
  const added = attachments(page).getByRole('row', { name: /Pla de proves/ });
  await expect(added).toContainText(categories.technical.name);
  await expect(added).toContainText('Word');
  await expect(added).toContainText(categories.manuals.name);
  await expect(added).toContainText('Vigent');
});

test('reflows the new attachment design and preserves keyboard access and focus return', async ({
  page,
  cleanupRegistry,
  disposableApplication,
}, testInfo) => {
  await openDocumentationTab(page, cleanupRegistry, disposableApplication);
  const open = page.getByRole('button', { name: 'Afegeix un adjunt' });
  await open.click();
  const dialog = page.getByRole('dialog');

  for (const width of [1440, 768, 320]) {
    await page.setViewportSize({ width, height: 900 });
    const title = await dialog.getByLabel('Títol', { exact: true }).boundingBox();
    const category = await page.locator('#application-attachment-category').boundingBox();
    const subcategory = await page.locator('#application-attachment-subcategory').boundingBox();
    expect(title).not.toBeNull();
    expect(category).not.toBeNull();
    expect(subcategory).not.toBeNull();
    // The title takes its own row, and the category keeps its subcategory beside it.
    expect(category!.y).toBeGreaterThan(title!.y + title!.height);
    if (width >= 640) {
      expect(Math.abs(category!.y - subcategory!.y)).toBeLessThan(3);
      expect(subcategory!.x).toBeGreaterThan(category!.x + category!.width);
    } else {
      expect(subcategory!.y).toBeGreaterThan(category!.y + category!.height);
    }
    expect(await dialog.evaluate((element) => element.scrollWidth <= element.clientWidth + 1)).toBe(
      true,
    );
    await page.screenshot({ path: testInfo.outputPath('attachment-design-' + width + '.png') });
  }

  await dialog.getByLabel('Títol', { exact: true }).focus();
  for (const field of ['category', 'subcategory', 'version']) {
    await page.keyboard.press('Tab');
    await expect(page.locator('#application-attachment-' + field)).toBeFocused();
  }
  await page.keyboard.press('Tab');
  await expect(dialog.getByRole('button', { name: 'Adjunta', exact: true })).toBeFocused();
  const axe = await new AxeBuilder({ page })
    .include('[role="dialog"]')
    .withTags(['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa', 'wcag22aa'])
    .analyze();
  expect(
    axe.violations.map(({ id, nodes }) => id + ': ' + nodes.map(({ target }) => target).join(', ')),
  ).toEqual([]);
  await page.addStyleTag({ content: 'html { font-size: 200%; }' });
  expect(await dialog.evaluate((element) => element.scrollWidth <= element.clientWidth + 1)).toBe(
    true,
  );
  const overflow = await dialog.locator('.p-dialog-content').evaluate((element) => ({
    clientWidth: element.clientWidth,
    scrollWidth: element.scrollWidth,
    overflowing: [...element.querySelectorAll<HTMLElement>('*')]
      .filter(
        (child) => child.getBoundingClientRect().right > element.getBoundingClientRect().right,
      )
      .map((child) => ({
        tag: child.tagName,
        className: child.className,
        width: child.getBoundingClientRect().width,
      })),
  }));
  expect(overflow, JSON.stringify(overflow)).toMatchObject({ scrollWidth: overflow.clientWidth });
  await page.screenshot({ path: testInfo.outputPath('attachment-design-320-text-200.png') });
  await page.keyboard.press('Escape');
  await expect(dialog).toHaveCount(0);
  await expect(open).toBeFocused();
});

test('opens an attachment on its own page, downloads it and deletes it from the list', async ({
  page,
  cleanupRegistry,
  disposableApplication,
}) => {
  const { documentation, writes } = await openDocumentationTab(
    page,
    cleanupRegistry,
    disposableApplication,
  );
  const impactId = documentation.documents["Avaluació d'impacte"];

  await rowAction(page, "l'adjunt", "Avaluació d'impacte", 'Consulta');
  await expect(page).toHaveURL(documentUrl(disposableApplication, impactId));
  await expect(page.getByRole('dialog')).toHaveCount(0);
  await expect(page.getByLabel('Títol', { exact: true })).toHaveValue("Avaluació d'impacte");
  await expect(page.getByRole('table', { name: 'Versions del document' })).toBeVisible();
  // Protecció de dades is not an active category, but stays shown as the one of the document.
  await expect(page.locator('#document-detail-category-value')).toContainText(
    documentation.categories.protection.name,
  );
  await expect(page.locator('#document-detail-subcategory-value')).toContainText(
    documentation.categories.impact.name,
  );
  await expect(page.locator('#document-detail-state-value')).toContainText('En revisió');
  await page.getByRole('button', { name: 'Tornar', exact: true }).click();

  const downloadPromise = page.waitForEvent('download');
  await rowAction(page, "l'adjunt", "Avaluació d'impacte", 'Descarrega');
  const download = await downloadPromise;
  expect(download.suggestedFilename()).toBe('avaluacio-impacte.pdf');

  await rowAction(page, "l'adjunt", "Avaluació d'impacte", 'Elimina');
  await page.getByRole('button', { name: "Confirma l'eliminació de l'adjunt" }).click();
  await expect(attachments(page).getByRole('row', { name: /Avaluació d'impacte/ })).toHaveCount(0);
  expect(writes).toContainEqual({
    method: 'DELETE',
    path: `/application/documentation/${impactId}`,
  });
});

test('saves the document, adds versions and opens and downloads their files', async ({
  page,
  context,
  cleanupRegistry,
  disposableApplication,
}, testInfo) => {
  await page.setViewportSize({ width: 1440, height: 1000 });
  const { documentation, writes } = await openDocumentationTab(
    page,
    cleanupRegistry,
    disposableApplication,
  );
  const username = await signedInUsername(page);
  const manualId = documentation.documents['Manual API v2'];
  const row = attachments(page).getByRole('row', { name: /Manual API v2/ });
  await row.focus();
  await page.keyboard.press('Enter');
  await expect(page).toHaveURL(documentUrl(disposableApplication, manualId));
  await expect(page.getByRole('dialog')).toHaveCount(0);
  await expect(page).toHaveTitle(`Manual API v2 · ${disposableApplication.name}`);
  await expect(page.getByRole('heading', { level: 1 })).toBeFocused();
  const versions = page.getByRole('table', { name: 'Versions del document' });
  await expect(versions.locator('tbody tr')).toHaveCount(2);
  await expect(versions).toContainText(today());
  await expect(versions).toContainText(username);
  await page.screenshot({
    path: testInfo.outputPath('document-detail-desktop.png'),
    fullPage: true,
  });

  const title = page.getByLabel('Títol', { exact: true });
  await expect(title).toHaveAttribute('readonly', '');
  await page.getByRole('button', { name: 'Editar Document', exact: true }).click();
  await expect(title).toBeFocused();
  await title.fill('Manual revisat');
  await selectOption(page, 'document-detail-state', 'En revisió');
  await page.getByRole('button', { name: 'Desar els canvis de Document', exact: true }).click();
  await expect(page.getByText('Document desat', { exact: true })).toBeVisible();
  expect(writes).toContainEqual({
    method: 'PUT',
    path: `/application/documentation/${manualId}`,
    data: {
      title: 'Manual revisat',
      categoryId: documentation.categories.technical.id,
      subcategoryId: documentation.categories.manuals.id,
      statusId: documentation.statuses['IN_REVIEW'],
    },
  });
  await expect(page.getByRole('button', { name: 'Editar Document', exact: true })).toBeFocused();
  await expect(title).toHaveAttribute('readonly', '');
  await page.getByRole('button', { name: 'Nova versió', exact: true }).click();
  const dialog = page.getByRole('dialog');
  await dialog.getByRole('button', { name: 'Desa', exact: true }).click();
  await expect(dialog.getByText('Camp obligatori')).toHaveCount(2);
  await expect(dialog.getByText('Mida màxima: 5 MB.')).toBeVisible();
  await dialog.getByLabel('Versió', { exact: true }).fill('2.1');
  await dialog.getByLabel('Fitxer').setInputFiles({
    name: 'programa.exe',
    mimeType: 'application/octet-stream',
    buffer: Buffer.from('MZ'),
  });
  await expect(dialog.getByRole('alert').filter({ hasText: 'programa.exe' })).toHaveText(
    "No s'ha adjuntat «programa.exe»: el format del fitxer no està admès.",
  );
  // The picker does not keep showing the refused file.
  expect(
    await dialog.getByLabel('Fitxer').evaluate((input: HTMLInputElement) => input.files?.length),
  ).toBe(0);
  await dialog.getByLabel('Fitxer').setInputFiles({
    name: 'manual-v3.txt',
    mimeType: 'text/plain',
    buffer: Buffer.from('version 3 document contents'),
  });
  await dialog.getByRole('button', { name: 'Desa', exact: true }).click();
  await expect(dialog.getByText('Ja existeix aquesta versió.')).toBeVisible();
  await dialog.getByLabel('Versió', { exact: true }).fill('3.0');
  await dialog.getByRole('button', { name: 'Desa', exact: true }).click();
  await expect(dialog).toHaveCount(0);
  await expect(versions.locator('tbody tr')).toHaveCount(3);
  await expect(versions.locator('tbody tr').first()).toContainText('3.0');
  await page.getByRole('button', { name: 'Accions de la versió 3.0', exact: true }).click();
  const downloadPromise = page.waitForEvent('download');
  await page.getByRole('menuitem', { name: 'Descarregar', exact: true }).click();
  const download = await downloadPromise;
  expect(download.suggestedFilename()).toBe('manual-v3.txt');
  expect(await download.failure()).toBeNull();

  await page.getByRole('button', { name: 'Accions de la versió 3.0', exact: true }).click();
  const popupPromise = context.waitForEvent('page');
  await page.getByRole('menuitem', { name: 'Veure', exact: true }).click();
  const popup = await popupPromise;
  await expect(popup).toHaveURL(/^blob:/);
  await expect(popup.locator('body')).toContainText('version 3 document contents');
  await popup.close();
  await page.getByRole('button', { name: 'Tornar', exact: true }).click();
  await expect(attachments(page).getByRole('row', { name: /Manual revisat/ })).toContainText('3.0');
});

test('guards unsaved document changes and opens a document directly at mobile widths', async ({
  page,
  cleanupRegistry,
  disposableApplication,
}, testInfo) => {
  const { documentation } = await openDocumentationTab(
    page,
    cleanupRegistry,
    disposableApplication,
  );
  const detailPath = `/aplicacions/${disposableApplication.id}/documentation/${documentation.documents['Manual API v2']}`;
  await page.goto(detailPath);
  await expect(page.getByLabel('Títol', { exact: true })).toHaveValue('Manual API v2');
  await page.getByRole('button', { name: 'Editar Document', exact: true }).click();
  await page.getByLabel('Títol', { exact: true }).fill('Canvis pendents');
  await page.getByRole('button', { name: 'Tornar', exact: true }).click();
  const confirmation = page.getByRole('dialog');
  await expect(confirmation.getByText('Canvis sense desar', { exact: true })).toBeVisible();
  await confirmation.getByRole('button', { name: 'Continua editant', exact: true }).click();
  await expect(page.getByLabel('Títol', { exact: true })).toHaveValue('Canvis pendents');
  await page.getByRole('button', { name: 'Tornar', exact: true }).click();
  await page.getByRole('button', { name: 'Descarta els canvis', exact: true }).click();
  await expect(page).toHaveURL(/\/documentation$/);
  await expect(attachments(page)).toContainText('Manual API v2');
  await page.goto(detailPath);
  await expect(page.getByLabel('Títol', { exact: true })).toHaveValue('Manual API v2');
  for (const width of [768, 320]) {
    await page.setViewportSize({ width, height: 1000 });
    expect(
      await page.locator('body').evaluate((element) => element.scrollWidth <= innerWidth + 1),
    ).toBe(true);
    await page.screenshot({
      path: testInfo.outputPath('document-detail-' + width + '.png'),
      fullPage: true,
    });
  }
  const axe = await new AxeBuilder({ page })
    .withTags(['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa', 'wcag22aa'])
    .analyze();
  expect(
    axe.violations.map(({ id, nodes }) => id + ': ' + nodes.map(({ target }) => target).join(', ')),
  ).toEqual([]);
  await page.addStyleTag({ content: 'html { font-size: 200%; }' });
  const overflow = await page.locator('body').evaluate((element) => ({
    width: innerWidth,
    scrollWidth: element.scrollWidth,
    overflowing: [
      ...document.querySelectorAll<HTMLElement>(
        '.document-detail-actions *, .document-detail-versions__header *, app-application-document-detail',
      ),
    ]
      .filter((child) => child.getBoundingClientRect().right > innerWidth)
      .map((child) => ({
        tag: child.tagName,
        className: child.className,
        right: child.getBoundingClientRect().right,
      })),
  }));
  expect(overflow.scrollWidth, JSON.stringify(overflow)).toBeLessThanOrEqual(overflow.width + 1);
  await page.screenshot({
    path: testInfo.outputPath('document-detail-320-text-200.png'),
    fullPage: true,
  });
});

test('places document metadata in one desktop row and supports Edit and Cancel in the top toolbar', async ({
  page,
  cleanupRegistry,
  disposableApplication,
}, testInfo) => {
  const { documentation } = await openDocumentationTab(
    page,
    cleanupRegistry,
    disposableApplication,
  );
  await page.goto(
    `/aplicacions/${disposableApplication.id}/documentation/${documentation.documents['Manual API v2']}`,
  );
  const title = page.getByLabel('Títol', { exact: true });
  const edit = page.getByRole('button', { name: 'Editar Document', exact: true });
  await expect(title).toHaveAttribute('readonly', '');
  await expect(page.locator('.document-detail-form p-select')).toHaveCount(0);

  for (const mode of ['view', 'edit']) {
    if (mode === 'edit') {
      await edit.click();
      await expect(title).toBeFocused();
      await expect(page.locator('.document-detail-form p-select')).toHaveCount(3);
    }
    for (const width of [1440, 768, 320]) {
      await page.setViewportSize({ width, height: 1000 });
      const ids = ['title', 'category', 'subcategory', 'state'].map(
        (field) =>
          '#document-detail-' + field + (mode === 'view' && field !== 'title' ? '-value' : ''),
      );
      const boxes = await Promise.all(ids.map((id) => page.locator(id).boundingBox()));
      expect(boxes.every(Boolean)).toBe(true);
      if (width === 1440) {
        expect(
          Math.max(...boxes.map((box) => box!.y)) - Math.min(...boxes.map((box) => box!.y)),
        ).toBeLessThan(3);
        const toolbar = await page.locator('.document-detail-toolbar').boundingBox();
        const actions = await page
          .locator('.document-detail-toolbar app-application-detail-section-actions')
          .boundingBox();
        expect(Math.abs(toolbar!.x + toolbar!.width - actions!.x - actions!.width)).toBeLessThan(2);
      } else if (width === 768) {
        expect(Math.abs(boxes[0]!.y - boxes[1]!.y)).toBeLessThan(3);
        expect(boxes[2]!.y).toBeGreaterThan(boxes[0]!.y + boxes[0]!.height);
      } else {
        for (let index = 1; index < boxes.length; index++) {
          expect(boxes[index]!.y).toBeGreaterThan(boxes[index - 1]!.y + boxes[index - 1]!.height);
        }
      }
      expect(
        await page.locator('body').evaluate((element) => element.scrollWidth <= innerWidth + 1),
      ).toBe(true);
      await page.screenshot({
        path: testInfo.outputPath('document-detail-' + mode + '-' + width + '.png'),
        fullPage: true,
      });
    }
  }

  await title.fill('Canvi cancel·lat');
  await page
    .getByRole('button', { name: 'Cancel·lar els canvis de Document', exact: true })
    .click();
  await expect(title).toHaveValue('Manual API v2');
  await expect(title).toHaveAttribute('readonly', '');
  await expect(edit).toBeFocused();
  await page.keyboard.press('Enter');
  await expect(title).toBeFocused();
  await page.addStyleTag({ content: 'html { font-size: 200%; }' });
  const overflow = await page.locator('body').evaluate((element) => ({
    width: innerWidth,
    scrollWidth: element.scrollWidth,
    overflowing: [...document.querySelectorAll<HTMLElement>('.document-detail-toolbar *')]
      .filter((child) => child.getBoundingClientRect().right > innerWidth)
      .map((child) => ({
        tag: child.tagName,
        className: child.className,
        right: child.getBoundingClientRect().right,
      })),
  }));
  expect(overflow.scrollWidth, JSON.stringify(overflow)).toBeLessThanOrEqual(overflow.width + 1);
  const axe = await new AxeBuilder({ page })
    .withTags(['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa', 'wcag22aa'])
    .analyze();
  expect(
    axe.violations.map(({ id, nodes }) => id + ': ' + nodes.map(({ target }) => target).join(', ')),
  ).toEqual([]);
  await page.screenshot({
    path: testInfo.outputPath('document-detail-edit-320-text-200.png'),
    fullPage: true,
  });
});

test('does not let attach a file while the accepted formats cannot be read', async ({
  page,
  cleanupRegistry,
  disposableApplication,
}) => {
  await openDocumentationTab(page, cleanupRegistry, disposableApplication, {
    mimeTypesAvailable: false,
  });
  await expect(
    page.getByText("No s'han pogut carregar els estats i els formats dels documents.").first(),
  ).toBeVisible();

  await page.getByRole('button', { name: 'Afegeix un adjunt' }).click();
  const dialog = page.getByRole('dialog');
  await expect(dialog.getByRole('button', { name: 'Adjunta', exact: true })).toBeDisabled();
  await expect(dialog.getByRole('status')).toHaveText(
    "No s'han pogut carregar els formats admesos: ara no es pot adjuntar cap fitxer.",
  );
  await dialog.getByRole('button', { name: /Cancel·la/ }).click();
  await expect(dialog).toHaveCount(0);

  await rowAction(page, "l'adjunt", 'Manual API v2', 'Consulta');
  await page.getByRole('button', { name: 'Nova versió', exact: true }).click();
  await expect(page.getByRole('dialog').getByLabel('Fitxer')).toBeDisabled();
  await expect(page.locator('#document-version-file-hint')).toHaveText(
    "No s'han pogut carregar els formats admesos: ara no es pot adjuntar cap fitxer.",
  );
});

test('keeps documents read-only when opening them from an inactive application', async ({
  page,
  cleanupRegistry,
  disposableApplication,
}) => {
  const { documentation } = await openDocumentationTab(
    page,
    cleanupRegistry,
    disposableApplication,
    { inactive: true },
  );
  await rowAction(page, "l'adjunt", 'Manual API v2', 'Consulta');
  await expect(page).toHaveURL(
    documentUrl(disposableApplication, documentation.documents['Manual API v2']),
  );
  await expect(page.getByLabel('Títol', { exact: true })).toHaveAttribute('readonly', '');
  await expect(page.getByText('Aquest document és de només lectura.')).toBeVisible();
  await expect(page.getByRole('button', { name: 'Nova versió' })).toHaveCount(0);
  await expect(page.getByRole('button', { name: 'Desa', exact: true })).toHaveCount(0);
  await expect(page.getByRole('button', { name: 'Editar Document', exact: true })).toHaveCount(0);
});

test('adds, edits and deletes a link', async ({ page, cleanupRegistry, disposableApplication }) => {
  const {
    documentation: { categories },
  } = await openDocumentationTab(page, cleanupRegistry, disposableApplication);

  await page.getByRole('button', { name: 'Afegeix un enllaç' }).click();
  const dialog = page.getByRole('dialog');
  await expect(
    dialog.getByRole('heading', { name: 'Afegeix un Enllaç', exact: true }),
  ).toBeVisible();
  await dialog.getByRole('button', { name: 'Desa', exact: true }).click();
  await expect(dialog.getByText('Camp obligatori')).toHaveCount(3);
  await expect(page.locator('#application-link-category')).toHaveAttribute('aria-invalid', 'true');
  await expect(page.locator('#application-link-category')).toHaveAttribute(
    'aria-describedby',
    'application-link-category-error',
  );
  await dialog.getByLabel('Títol', { exact: true }).fill(' ');
  await expect(page.locator('#application-link-title-error')).toHaveText('Camp obligatori');
  await dialog.getByLabel('Títol', { exact: true }).fill('Portal de proves');
  await selectOption(page, 'application-link-category', categories.regulations.name);
  await dialog.getByLabel('URL').fill('portal.example.com');
  await dialog.getByRole('button', { name: 'Desa', exact: true }).click();
  await expect(dialog.getByText('Indica una URL que comenci per http:// o https://')).toBeVisible();
  await dialog.getByLabel('URL').fill('https://portal.example.com');
  await dialog.getByRole('button', { name: 'Desa', exact: true }).click();
  await expect(dialog).toHaveCount(0);
  await expect(links(page).getByRole('row', { name: /Portal de proves/ })).toContainText(
    categories.regulations.name,
  );

  await rowAction(page, "l'enllaç", 'Portal de proves', 'Consulta');
  await expect(
    dialog.getByRole('definition').filter({ hasText: categories.regulations.name }),
  ).toHaveCount(1);
  await dialog.getByRole('button', { name: "Edita l'enllaç" }).click();
  await dialog.getByLabel('Descripció (opcional)').fill('Entorn de proves');
  await dialog.getByRole('button', { name: 'Desa', exact: true }).click();
  await expect(dialog).toHaveCount(0);
  await expect(links(page).getByRole('row', { name: /Portal de proves/ })).toContainText(
    'Entorn de proves',
  );

  await rowAction(page, "l'enllaç", 'Portal de proves', 'Elimina');
  await page.getByRole('button', { name: "Confirma l'eliminació de l'enllaç" }).click();
  await expect(links(page)).not.toContainText('Portal de proves');
});

test('matches the new link dialog design across sizes with keyboard access and focus return', async ({
  page,
  cleanupRegistry,
  disposableApplication,
}, testInfo) => {
  const {
    documentation: { categories },
  } = await openDocumentationTab(page, cleanupRegistry, disposableApplication);
  const open = page.getByRole('button', { name: 'Afegeix un enllaç' });
  await open.click();
  const dialog = page.getByRole('dialog');
  await expect(
    dialog.getByText('Tots els camps són obligatoris, excepte els indicats com a opcionals.'),
  ).toBeVisible();
  await dialog.getByLabel('Títol', { exact: true }).fill('Manual API v2.1');
  await selectOption(page, 'application-link-category', categories.technical.name);
  await dialog.getByLabel('Descripció (opcional)').fill('CI/CD workflows and automation');
  await dialog.getByLabel('URL', { exact: true }).fill('https://www.example.com/integrations');

  for (const width of [1440, 768, 320]) {
    await page.setViewportSize({ width, height: 900 });
    const title = await page.locator('#application-link-title').boundingBox();
    const category = await page.locator('#application-link-category').boundingBox();
    const description = await page.locator('#application-link-description').boundingBox();
    const url = await page.locator('#application-link-url').boundingBox();
    expect(title).not.toBeNull();
    expect(category).not.toBeNull();
    expect(description).not.toBeNull();
    expect(url).not.toBeNull();
    if (width >= 640) {
      expect(Math.abs(title!.y - category!.y)).toBeLessThan(3);
      expect(category!.x).toBeGreaterThan(title!.x + title!.width);
      expect(description!.width).toBeGreaterThan(title!.width);
    } else {
      expect(category!.y).toBeGreaterThan(title!.y + title!.height);
    }
    expect(description!.y).toBeGreaterThan(category!.y + category!.height);
    expect(url!.y).toBeGreaterThan(description!.y + description!.height);
    expect(Math.abs(description!.width - url!.width)).toBeLessThan(2);
    expect(
      await dialog
        .locator('.p-dialog-content')
        .evaluate((element) => element.scrollWidth <= element.clientWidth + 1),
    ).toBe(true);
    await page.screenshot({
      path: testInfo.outputPath('link-design-' + width + '.png'),
      animations: 'disabled',
    });
  }

  await page.locator('#application-link-title').focus();
  for (const field of ['category', 'description', 'url']) {
    await page.keyboard.press('Tab');
    await expect(page.locator('#application-link-' + field)).toBeFocused();
  }
  await page.keyboard.press('Tab');
  await expect(dialog.getByRole('button', { name: /Cancel·la/ })).toBeFocused();
  await page.keyboard.press('Tab');
  await expect(dialog.getByRole('button', { name: 'Desa', exact: true })).toBeFocused();
  await page.keyboard.press('Tab');
  await expect(
    dialog.getByRole('button', { name: "Tanca el formulari de l'enllaç" }),
  ).toBeFocused();
  const axe = await new AxeBuilder({ page })
    .include('[role="dialog"]')
    .withTags(['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa', 'wcag22aa'])
    .analyze();
  expect(
    axe.violations.map(({ id, nodes }) => id + ': ' + nodes.map(({ target }) => target).join(', ')),
  ).toEqual([]);
  await page.addStyleTag({ content: 'html { font-size: 200%; }' });
  expect(
    await dialog
      .locator('.p-dialog-content')
      .evaluate((element) => element.scrollWidth <= element.clientWidth + 1),
  ).toBe(true);
  await page.screenshot({
    path: testInfo.outputPath('link-design-320-text-200.png'),
    animations: 'disabled',
  });
  await page.keyboard.press('Escape');
  await expect(dialog).toHaveCount(0);
  await expect(open).toBeFocused();
});

test('only lets consult and download the documentation of an inactive application', async ({
  page,
  cleanupRegistry,
  disposableApplication,
}) => {
  await openDocumentationTab(page, cleanupRegistry, disposableApplication, { inactive: true });

  await expect(page.getByRole('button', { name: 'Afegeix un adjunt' })).toHaveCount(0);
  await expect(page.getByRole('button', { name: 'Afegeix un enllaç' })).toHaveCount(0);
  await page.getByRole('button', { name: "Accions de l'adjunt «Manual API v2»" }).click();
  await expect(page.locator('.maintenance-row-menu').getByRole('menuitem')).toHaveText([
    'Consulta',
    'Descarrega',
  ]);
});
