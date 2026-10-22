import AxeBuilder from '@axe-core/playwright';
import { expect, test, type Page, type Route } from '@playwright/test';

import { selectOption } from './support/ui';

test.use({ storageState: { cookies: [], origins: [] } });

interface StoredCategory {
  id: number;
  name: string;
  nameEs: string;
  parentId: number | null;
  deletedAt: string | null;
}

interface CategoriesMock {
  records: StoredCategory[];
  writes: { method: string; path: string; body: unknown }[];
}

const SEED: StoredCategory[] = [
  { id: 1, name: 'Tècnica', nameEs: 'Técnica', parentId: null, deletedAt: null },
  { id: 11, name: 'Manuals', nameEs: 'Manuales', parentId: 1, deletedAt: null },
  { id: 12, name: 'API', nameEs: 'API', parentId: 1, deletedAt: null },
  { id: 2, name: 'Normativa', nameEs: 'Normativa', parentId: null, deletedAt: null },
  { id: 3, name: 'Contractació', nameEs: 'Contratación', parentId: null, deletedAt: '2026-10-01T10:00:00' },
];

const byName = (left: StoredCategory, right: StoredCategory) => left.name.localeCompare(right.name);

// In-memory stand-in for /documentation/category with the back-end's listing and delete rules.
async function openDocumentCategories(page: Page): Promise<CategoriesMock> {
  const mock: CategoriesMock = { records: SEED.map((record) => ({ ...record })), writes: [] };
  const withSubcategories = (category: StoredCategory) => ({
    ...category,
    subcategories:
      category.parentId == null
        ? mock.records.filter(({ parentId }) => parentId === category.id).sort(byName)
        : [],
  });

  await page.route('**/invaiback/**', async (route: Route) => {
    const request = route.request();
    const url = new URL(request.url());
    const path = url.pathname;
    const method = request.method();
    if (path.endsWith('/auth/me')) {
      return route.fulfill({ json: { authenticated: true, username: 'document-categories-test' } });
    }
    if (!path.includes('/documentation/category')) {
      return route.fulfill({ json: { content: [], totalElements: 0, totalPages: 1, last: true, number: 0 } });
    }
    const id = Number(path.match(/\/(\d+)$/)?.[1]);
    const record = mock.records.find((item) => item.id === id);

    if (method === 'GET' && !id) {
      const status = url.searchParams.get('statusId');
      const text = (url.searchParams.get('search') ?? url.searchParams.get('name') ?? '').toLowerCase();
      const matches = (item: StoredCategory) =>
        `${item.name} ${item.nameEs}`.toLowerCase().includes(text);
      const content = mock.records
        .filter(({ parentId }) => parentId == null)
        .filter(({ deletedAt }) => !status || (status === '1') === !deletedAt)
        .filter(
          (category) =>
            !text ||
            matches(category) ||
            mock.records.some((item) => item.parentId === category.id && matches(item)),
        )
        .sort(byName)
        .map(withSubcategories);
      return route.fulfill({
        json: { content, totalElements: content.length, totalPages: 1, last: true, number: 0 },
      });
    }
    if (method === 'GET') return route.fulfill({ json: withSubcategories(record!) });

    const body = request.postDataJSON();
    mock.writes.push({ method, path: path.replace(/^.*\/documentation\/category/, ''), body });
    if (method === 'POST') {
      const created = { id: 100 + mock.writes.length, ...body, deletedAt: null };
      mock.records.push(created);
      return route.fulfill({ status: 201, json: withSubcategories(created) });
    }
    if (method === 'DELETE') {
      if (mock.records.some((item) => item.parentId === id && !item.deletedAt)) {
        return route.fulfill({
          status: 400,
          json: {
            error: 'Error de validació',
            message: 'No es pot eliminar la categoria documental perquè té subcategories actives.',
          },
        });
      }
      record!.deletedAt = '2026-10-14T10:00:00';
      return route.fulfill({ status: 204 });
    }
    if (path.includes('/reactivate/')) {
      record!.deletedAt = null;
      return route.fulfill({ json: withSubcategories(record!) });
    }
    Object.assign(record!, body);
    return route.fulfill({ json: withSubcategories(record!) });
  });

  await page.goto('/manteniments/documentacio#document-categories');
  await expect(page.locator('.app-initial-loader')).toBeHidden({ timeout: 30_000 });
  return mock;
}

const categoryRow = (page: Page, name: string) =>
  page.locator('app-document-categories-table tr.invai-table-consultable-row', { hasText: name }).first();
const toggle = (page: Page, name: string) =>
  page.getByRole('button', { name: `Subcategories de «${name}»` });
// The subcategory rows shown under a category: they follow it in the same table, before the next
// category row.
const SUBCATEGORY_ROW = 'contains(@class, "document-categories-table__subcategory")';
const subcategories = (page: Page, name: string) =>
  page.locator(
    `xpath=//app-document-categories-table//tr[${SUBCATEGORY_ROW}]` +
      `[preceding-sibling::tr[not(${SUBCATEGORY_ROW})][1][contains(., "${name}")]]`,
  );
const subcategory = (page: Page, category: string, name: string) =>
  subcategories(page, category).filter({ hasText: name });

async function rowAction(page: Page, kind: 'categoria' | 'subcategoria', name: string, action: string) {
  await page.getByRole('button', { name: `Accions de la ${kind} «${name}»` }).click();
  await page.getByRole('menuitem', { name: action, exact: true }).click();
}

test('adds the Documentació maintenance tab with the document categories panel', async ({ page }, testInfo) => {
  await openDocumentCategories(page);

  await expect(page.locator('.maintenances-tabs a').last()).toHaveText('Documentació');
  await expect(page.locator('.maintenances-tabs a').last()).toHaveAttribute('aria-current', 'page');
  await expect(page.getByRole('button', { name: /Categories documentals/ })).toHaveAttribute(
    'aria-expanded',
    'true',
  );
  await expect(page.locator('app-document-categories-table thead th')).toHaveText([
    'Nom',
    'Nom en castellà',
    'Subcategories actives',
    'Accions',
  ]);
  await expect(categoryRow(page, 'Tècnica')).toContainText('2');
  await expect(categoryRow(page, 'Contractació')).toHaveCount(0);

  const technical = toggle(page, 'Tècnica');
  await expect(technical).toHaveAttribute('aria-expanded', 'false');
  await expect(toggle(page, 'Normativa')).toHaveCount(0);
  await technical.click();
  await expect(technical).toHaveAttribute('aria-expanded', 'true');
  await expect(subcategories(page, 'Tècnica')).toHaveCount(2);
  await expect(subcategory(page, 'Tècnica', 'Manuals')).toBeVisible();
  // The subcategories share the header of the categories.
  await expect(page.locator('app-document-categories-table thead')).toHaveCount(1);
  await page.screenshot({ path: testInfo.outputPath('document-categories.png'), fullPage: true });
});

test('adds a category and a subcategory fixed to its category', async ({ page }, testInfo) => {
  const mock = await openDocumentCategories(page);

  await page.getByRole('button', { name: 'Afegeix una categoria documental' }).click();
  const dialog = page.getByRole('dialog');
  await expect(dialog.getByRole('heading', { name: 'Nova categoria documental' })).toBeVisible();
  await dialog.getByRole('button', { name: 'Afegeix la categoria' }).click();
  await expect(dialog.getByText('Camp obligatori')).toHaveCount(2);
  await dialog.getByLabel('Nom', { exact: true }).fill('Protecció de dades');
  await dialog.getByLabel('Nom en castellà').fill('Protección de datos');
  await dialog.getByRole('button', { name: 'Afegeix la categoria' }).click();
  await expect(dialog).toHaveCount(0);

  // The subcategories are added from the detail of their category, not from the list.
  await page.getByRole('button', { name: 'Accions de la categoria «Normativa»' }).click();
  await expect(page.getByRole('menuitem', { name: 'Afegeix subcategoria' })).toHaveCount(0);
  await page.getByRole('menuitem', { name: 'Consulta', exact: true }).click();
  await page.getByRole('button', { name: 'Afegeix una subcategoria a «Normativa»' }).click();
  await expect(dialog.getByRole('heading', { name: 'Nova subcategoria documental' })).toBeVisible();
  await expect(dialog.getByLabel('Categoria', { exact: true })).toHaveText('Normativa');
  await dialog.getByLabel('Nom', { exact: true }).fill('Legislació');
  await dialog.getByLabel('Nom en castellà').fill('Legislación');
  await page.screenshot({ path: testInfo.outputPath('document-subcategory-dialog.png') });
  await dialog.getByRole('button', { name: 'Afegeix la subcategoria' }).click();

  await expect(dialog).toHaveCount(0);
  await expect(page.getByRole('table', { name: 'Subcategories de «Normativa»' })).toContainText('Legislació');
  expect(mock.writes).toEqual([
    { method: 'POST', path: '', body: { name: 'Protecció de dades', nameEs: 'Protección de datos', parentId: null } },
    { method: 'POST', path: '', body: { name: 'Legislació', nameEs: 'Legislación', parentId: 2 } },
  ]);
});

test('moves a subcategory to another active category', async ({ page }) => {
  const mock = await openDocumentCategories(page);

  await toggle(page, 'Tècnica').click();
  await rowAction(page, 'subcategoria', 'API', 'Edita');
  const dialog = page.getByRole('dialog');
  await expect(dialog.getByRole('heading', { name: 'Edició de la subcategoria documental' })).toBeVisible();
  await selectOption(page, 'document-category-dialog-category', 'Normativa');
  await dialog.getByRole('button', { name: 'Desa els canvis de la subcategoria' }).click();

  await expect(dialog).toHaveCount(0);
  expect(mock.writes).toEqual([
    { method: 'PUT', path: '/12', body: { name: 'API', nameEs: 'API', parentId: 2 } },
  ]);
  // The category the subcategory moved to is expanded to show it.
  await expect(toggle(page, 'Normativa')).toHaveAttribute('aria-expanded', 'true');
  await expect(subcategory(page, 'Normativa', 'API')).toBeVisible();
  await expect(subcategory(page, 'Tècnica', 'API')).toHaveCount(0);
});

test('deletes and restores a subcategory and reports a category that still has subcategories', async ({
  page,
}) => {
  const mock = await openDocumentCategories(page);

  await rowAction(page, 'categoria', 'Tècnica', 'Elimina');
  await page.getByRole('button', { name: "Confirma l'eliminació de la categoria" }).click();
  await expect(page.getByText('té subcategories actives')).toBeVisible();

  await page.keyboard.press('Escape');
  await expect(page.getByRole('dialog')).toHaveCount(0);
  await toggle(page, 'Tècnica').click();
  await rowAction(page, 'subcategoria', 'Manuals', 'Elimina');
  await page.getByRole('button', { name: "Confirma l'eliminació de la subcategoria" }).click();
  await expect(page.getByRole('dialog')).toHaveCount(0);
  // An inactive subcategory stays under its category and can be restored.
  await expect(subcategory(page, 'Tècnica', 'Manuals')).toBeVisible();

  await rowAction(page, 'subcategoria', 'Manuals', 'Restaura');
  await expect(subcategory(page, 'Tècnica', 'Manuals')).toBeVisible();
  await expect.poll(() => mock.writes.length).toBe(3);
  expect(mock.writes.map(({ method, path }) => `${method} ${path}`)).toEqual([
    'DELETE /1',
    'DELETE /11',
    'PUT /reactivate/11',
  ]);
});

test('expands the category whose subcategory matches a quick search', async ({ page }) => {
  await openDocumentCategories(page);

  await page.getByRole('textbox', { name: 'Cerca ràpida de categories documentals' }).fill('manual');

  await expect(categoryRow(page, 'Normativa')).toHaveCount(0);
  await expect(toggle(page, 'Tècnica')).toHaveAttribute('aria-expanded', 'true');
  await expect(subcategory(page, 'Tècnica', 'Manuals')).toBeVisible();
});

test('reflows at 320 px keeping the tables scroll inside their container', async ({ page }, testInfo) => {
  await page.setViewportSize({ width: 320, height: 800 });
  await openDocumentCategories(page);

  await toggle(page, 'Tècnica').click();
  await expect(subcategories(page, 'Tècnica').first()).toBeVisible();
  const pageOverflow = await page.evaluate(
    () => document.documentElement.scrollWidth - document.documentElement.clientWidth,
  );
  expect(pageOverflow).toBeLessThanOrEqual(0);
  await page.screenshot({ path: testInfo.outputPath('document-categories-mobile.png'), fullPage: true });
});

const permission = (page: Page, permission: string, role: string) =>
  page.getByRole('checkbox', { name: `${permission} per a ${role}`, exact: true });

test('opens the detail of a category with its subcategories and access permissions', async ({
  page,
}, testInfo) => {
  await openDocumentCategories(page);

  await rowAction(page, 'categoria', 'Tècnica', 'Consulta');

  await expect(page).toHaveURL(/\/manteniments\/documentacio\/categories-documentals\/1$/);
  await expect(page.getByRole('heading', { level: 1, name: 'Tècnica' })).toBeVisible();
  const breadcrumb = page.getByRole('navigation', { name: 'Ruta de navegació' });
  await expect(breadcrumb.getByRole('link', { name: 'Categories documentals' })).toBeVisible();
  await expect(page.getByLabel('Nom', { exact: true })).toHaveValue('Tècnica');
  await expect(page.getByLabel('Nom en castellà')).toHaveValue('Técnica');
  // "Consulta" shows the category until "Editar" is pressed.
  await expect(page.getByLabel('Nom', { exact: true })).toHaveAttribute('readonly', '');
  await expect(permission(page, 'Lectura', 'Consulta')).toBeDisabled();
  await expect(page.getByRole('button', { name: 'Desa els canvis de la categoria' })).toHaveCount(0);

  const subcategoriesTable = page.getByRole('table', { name: 'Subcategories de «Tècnica»' });
  await expect(subcategoriesTable.getByRole('row')).toHaveCount(3);
  await expect(subcategoriesTable).toContainText('Manuals');

  const permissions = page.getByRole('table', { name: "Permisos d'accés de cada rol" });
  await expect(permissions.getByRole('row')).toHaveCount(5);
  await expect(permission(page, 'Tots els permisos', 'Administració funcional')).toBeChecked();
  await expect(permission(page, 'Càrrega', 'Consulta')).not.toBeChecked();
  await page.screenshot({ path: testInfo.outputPath('document-category-detail.png'), fullPage: true });
  const axe = await new AxeBuilder({ page })
    .withTags(['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa', 'wcag22aa'])
    .analyze();
  expect(axe.violations.map(({ id, nodes }) => `${id}: ${nodes.map(({ target }) => target).join(', ')}`)).toEqual([]);

  await page.getByRole('button', { name: 'Edita la categoria «Tècnica»' }).click();
  await expect(page.getByLabel('Nom', { exact: true })).toBeFocused();
  await expect(page.getByLabel('Nom', { exact: true })).not.toHaveAttribute('readonly', '');
  await expect(permission(page, 'Lectura', 'Consulta')).toBeEnabled();
  await page.screenshot({ path: testInfo.outputPath('document-category-detail-edit.png') });
  await page.getByRole('button', { name: 'Desa els canvis de la categoria' }).scrollIntoViewIfNeeded();
  await page.screenshot({ path: testInfo.outputPath('document-category-detail-actions.png') });

  await page.getByLabel('Nom', { exact: true }).fill('Una altra');
  await page.getByRole('button', { name: 'Cancel·la els canvis de la categoria' }).click();
  await expect(page.getByLabel('Nom', { exact: true })).toHaveValue('Tècnica');
  await expect(page.getByRole('button', { name: 'Edita la categoria «Tècnica»' })).toBeFocused();
});

test('saves the names and the permissions of a category', async ({ page }) => {
  const mock = await openDocumentCategories(page);
  await rowAction(page, 'categoria', 'Tècnica', 'Edita');

  await page.getByLabel('Nom', { exact: true }).fill('Tècnica i arquitectura');
  await permission(page, 'Tots els permisos', 'Consulta').click();
  await expect(permission(page, 'Eliminació (baixa lògica)', 'Consulta')).toBeChecked();
  await page.getByRole('button', { name: 'Desa els canvis de la categoria' }).click();

  await expect(page.getByRole('heading', { level: 1, name: 'Tècnica i arquitectura' })).toBeVisible();
  await expect(page.getByRole('button', { name: 'Edita la categoria «Tècnica i arquitectura»' })).toBeVisible();
  expect(mock.writes).toEqual([
    { method: 'PUT', path: '/1', body: { name: 'Tècnica i arquitectura', nameEs: 'Técnica', parentId: null } },
  ]);

  // The saved permissions are kept for the session.
  await page.getByRole('button', { name: 'Torna al llistat de categories documentals' }).click();
  await expect(page).toHaveURL(/\/manteniments\/documentacio#document-categories$/);
  await rowAction(page, 'categoria', 'Tècnica i arquitectura', 'Consulta');
  await expect(permission(page, 'Tots els permisos', 'Consulta')).toBeChecked();
});

test('asks before leaving a category with unsaved changes', async ({ page }) => {
  await openDocumentCategories(page);
  await rowAction(page, 'categoria', 'Tècnica', 'Edita');

  await permission(page, 'Càrrega', 'Consulta').click();
  await page.getByRole('button', { name: 'Torna al llistat de categories documentals' }).click();
  const dialog = page.getByRole('dialog');
  await expect(dialog.getByText('Sortir sense desar?')).toBeVisible();
  await dialog.getByRole('button', { name: 'Continua editant la categoria' }).click();
  await expect(page).toHaveURL(/categories-documentals\/1$/);
  await expect(permission(page, 'Càrrega', 'Consulta')).toBeChecked();

  await page.getByRole('button', { name: 'Torna al llistat de categories documentals' }).click();
  await page.getByRole('dialog').getByRole('button', { name: 'Surt de la categoria sense desar els canvis' }).click();
  await expect(page).toHaveURL(/\/manteniments\/documentacio#document-categories$/);
});

test('edits a subcategory from the detail of its category', async ({ page }) => {
  const mock = await openDocumentCategories(page);
  await rowAction(page, 'categoria', 'Tècnica', 'Consulta');

  await rowAction(page, 'subcategoria', 'API', 'Edita');
  const dialog = page.getByRole('dialog');
  await dialog.getByLabel('Nom', { exact: true }).fill('API REST');
  await dialog.getByRole('button', { name: 'Desa els canvis de la subcategoria' }).click();

  await expect(dialog).toHaveCount(0);
  await expect(page.getByRole('table', { name: 'Subcategories de «Tècnica»' })).toContainText('API REST');
  expect(mock.writes).toEqual([
    { method: 'PUT', path: '/12', body: { name: 'API REST', nameEs: 'API', parentId: 1 } },
  ]);
});

test('shows an inactive category without letting it change', async ({ page }) => {
  await openDocumentCategories(page);
  await page.goto('/manteniments/documentacio/categories-documentals/3');

  await expect(page.getByRole('heading', { level: 1, name: 'Contractació' })).toBeVisible();
  await expect(page.getByText('Aquesta categoria està donada de baixa')).toBeVisible();
  await expect(page.getByLabel('Nom', { exact: true })).toHaveAttribute('readonly', '');
  await expect(permission(page, 'Lectura', 'Consulta')).toBeDisabled();
  await expect(page.getByRole('button', { name: 'Desa els canvis de la categoria' })).toHaveCount(0);
});
