import AxeBuilder from '@axe-core/playwright';
import type { Page, TestInfo } from '@playwright/test';

import { expect, test } from './support/application.fixture';
import type { CleanupRegistry } from './support/cleanup-registry';
import { uniqueToken } from './support/data';
import {
  apiUrl,
  expectApiJsonResponse,
  expectJsonResponse,
  waitForApiResponse,
} from './support/http';
import { SEARCH_BUTTON, showFilters, waitForInitialLoader } from './support/maintenance-resources';
import { registerSoftDeleteCleanup } from './support/systems';
import { selectOption } from './support/ui';

const CATEGORIES_API = '/invaiback/documentation/category';
const ROLE_PERMISSIONS_API = '/invaiback/documentation/role-permission';

// Every test creates its own categories through the API before using the page, which can take
// longer than the default 30 seconds while the back-end is busy.
test.describe.configure({ timeout: 90_000 });

interface CategoryRecord {
  id: number;
  name: string;
  nameEs: string;
  parentId: number | null;
  deletedAt?: string | null;
}

interface CategoryTree {
  token: string;
  /** Text only the subcategories contain, so a search for it does not match the categories. */
  subcategoryKey: string;
  technical: CategoryRecord;
  manuals: CategoryRecord;
  api: CategoryRecord;
  regulations: CategoryRecord;
  procurement: CategoryRecord;
}

interface CategoryWrite {
  method: string;
  path: string;
  body: unknown;
}

interface RolePermissionRecord {
  roleId: number;
  name: string;
  canRead: boolean;
  canUpload: boolean;
  canEdit: boolean;
  canDelete: boolean;
}

// Creates the categories each test works with: "Tècnica" with the subcategories "Manuals" and
// "API", "Normativa" without subcategories and the inactive "Contractació".
async function seedCategories(
  page: Page,
  cleanup: CleanupRegistry,
  testInfo: TestInfo,
): Promise<CategoryTree> {
  const token = uniqueToken(testInfo);
  const subcategoryKey = token.split('').reverse().join('').toLowerCase();
  const create = async (name: string, nameEs: string, parentId: number | null = null) => {
    const created = await expectApiJsonResponse<CategoryRecord>(
      await page.request.post(apiUrl(page, CATEGORIES_API), { data: { name, nameEs, parentId } }),
      `POST ${CATEGORIES_API} ${name}`,
    );
    registerCategoryCleanup(page, cleanup, created.id, name);
    return created;
  };

  const technical = await create(`E2E ${token} Tècnica`, `E2E ${token} Técnica`);
  const regulations = await create(`E2E ${token} Normativa`, `E2E ${token} Normativa ES`);
  const procurement = await create(`E2E ${token} Contractació`, `E2E ${token} Contratación`);
  // Registered after their category, so the cleanup removes them first.
  const manuals = await create(
    `Manuals ${subcategoryKey}`,
    `Manuales ${subcategoryKey}`,
    technical.id,
  );
  const api = await create(`API ${subcategoryKey}`, `API ES ${subcategoryKey}`, technical.id);
  const deleted = await page.request.delete(apiUrl(page, `${CATEGORIES_API}/${procurement.id}`));
  expect(deleted.ok(), await deleted.text()).toBeTruthy();

  return { token, subcategoryKey, technical, manuals, api, regulations, procurement };
}

function registerCategoryCleanup(
  page: Page,
  cleanup: CleanupRegistry,
  id: number,
  name: string,
): void {
  registerSoftDeleteCleanup<CategoryRecord>({
    page,
    cleanup,
    label: `document category ${name}`,
    pathname: CATEGORIES_API,
    target: { id, matches: (item) => item.id === id },
  });
}

async function openDocumentCategories(page: Page): Promise<void> {
  await page.goto('/manteniments/documentacio#document-categories');
  await waitForInitialLoader(page);
}

// Narrows the list to the records of the test, which may not be on the first page otherwise.
async function quickSearch(page: Page, text: string): Promise<void> {
  const listPromise = waitForApiResponse(page, 'GET', CATEGORIES_API, (url) =>
    [...url.searchParams.values()].includes(text),
  );
  await page.getByRole('textbox', { name: 'Cerca ràpida de categories documentals' }).fill(text);
  await listPromise;
}

async function openSeededList(page: Page, cleanup: CleanupRegistry, testInfo: TestInfo) {
  await openDocumentCategories(page);
  const tree = await seedCategories(page, cleanup, testInfo);
  await quickSearch(page, tree.token);
  return tree;
}

function recordWrites(page: Page): CategoryWrite[] {
  const writes: CategoryWrite[] = [];
  page.on('request', (request) => {
    const path = new URL(request.url()).pathname;
    if (request.method() === 'GET' || !path.startsWith(CATEGORIES_API)) return;
    writes.push({
      method: request.method(),
      path: path.slice(CATEGORIES_API.length),
      body: request.postDataJSON(),
    });
  });
  return writes;
}

const categoryRow = (page: Page, name: string) =>
  page
    .locator('app-document-categories-table tr.invai-table-consultable-row', { hasText: name })
    .first();
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

async function rowAction(
  page: Page,
  kind: 'categoria' | 'subcategoria',
  name: string,
  action: string,
) {
  await page.getByRole('button', { name: `Accions de la ${kind} «${name}»` }).click();
  await page.getByRole('menuitem', { name: action, exact: true }).click();
}

test('adds the Documentació maintenance tab with the document categories panel', async ({
  page,
  cleanupRegistry,
}, testInfo) => {
  const { technical, manuals, regulations, procurement } = await openSeededList(
    page,
    cleanupRegistry,
    testInfo,
  );

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
  await expect(categoryRow(page, technical.name)).toContainText('2');
  await expect(categoryRow(page, procurement.name)).toHaveCount(0);

  // The quick search that isolates the test records expands every category with subcategories.
  const technicalToggle = toggle(page, technical.name);
  await expect(technicalToggle).toHaveAttribute('aria-expanded', 'true');
  await expect(toggle(page, regulations.name)).toHaveCount(0);
  await technicalToggle.click();
  await expect(technicalToggle).toHaveAttribute('aria-expanded', 'false');
  await expect(subcategories(page, technical.name)).toHaveCount(0);
  await technicalToggle.click();
  await expect(technicalToggle).toHaveAttribute('aria-expanded', 'true');
  await expect(subcategories(page, technical.name)).toHaveCount(2);
  await expect(subcategory(page, technical.name, manuals.name)).toBeVisible();
  // The subcategories share the header of the categories.
  await expect(page.locator('app-document-categories-table thead')).toHaveCount(1);
  await page.screenshot({ path: testInfo.outputPath('document-categories.png'), fullPage: true });
});

test('adds a category and a subcategory fixed to its category', async ({
  page,
  cleanupRegistry,
}, testInfo) => {
  const { token, regulations } = await openSeededList(page, cleanupRegistry, testInfo);
  const writes = recordWrites(page);
  const newCategory = {
    name: `E2E ${token} Protecció de dades`,
    nameEs: `E2E ${token} Protección de datos`,
  };

  await page.getByRole('button', { name: 'Afegeix una categoria documental' }).click();
  const dialog = page.getByRole('dialog');
  await expect(dialog.getByRole('heading', { name: 'Nova categoria documental' })).toBeVisible();
  await dialog.getByRole('button', { name: 'Afegeix la categoria' }).click();
  await expect(dialog.getByText('Camp obligatori')).toHaveCount(2);
  await dialog.getByLabel('Nom', { exact: true }).fill(newCategory.name);
  await dialog.getByLabel('Nom en castellà').fill(newCategory.nameEs);
  const categoryPromise = waitForApiResponse(page, 'POST', CATEGORIES_API);
  await dialog.getByRole('button', { name: 'Afegeix la categoria' }).click();
  const category = await expectJsonResponse<CategoryRecord>(await categoryPromise);
  registerCategoryCleanup(page, cleanupRegistry, category.id, newCategory.name);
  await expect(dialog).toHaveCount(0);

  // The subcategories are added from the detail of their category, not from the list.
  await page.getByRole('button', { name: `Accions de la categoria «${regulations.name}»` }).click();
  await expect(page.getByRole('menuitem', { name: 'Afegeix subcategoria' })).toHaveCount(0);
  await page.getByRole('menuitem', { name: 'Consulta', exact: true }).click();
  await page
    .getByRole('button', { name: `Afegeix una subcategoria a «${regulations.name}»` })
    .click();
  await expect(dialog.getByRole('heading', { name: 'Nova subcategoria documental' })).toBeVisible();
  await expect(dialog.getByLabel('Categoria', { exact: true })).toHaveText(regulations.name);
  await dialog.getByLabel('Nom', { exact: true }).fill('Legislació');
  await dialog.getByLabel('Nom en castellà').fill('Legislación');
  await page.screenshot({ path: testInfo.outputPath('document-subcategory-dialog.png') });
  const subcategoryPromise = waitForApiResponse(page, 'POST', CATEGORIES_API);
  await dialog.getByRole('button', { name: 'Afegeix la subcategoria' }).click();
  const created = await expectJsonResponse<CategoryRecord>(await subcategoryPromise);
  registerCategoryCleanup(page, cleanupRegistry, created.id, 'Legislació');

  await expect(dialog).toHaveCount(0);
  await expect(
    page.getByRole('table', { name: `Subcategories de «${regulations.name}»` }),
  ).toContainText('Legislació');
  expect(writes).toEqual([
    { method: 'POST', path: '', body: { ...newCategory, parentId: null } },
    {
      method: 'POST',
      path: '',
      body: { name: 'Legislació', nameEs: 'Legislación', parentId: regulations.id },
    },
  ]);
});

test('moves a subcategory to another active category', async ({
  page,
  cleanupRegistry,
}, testInfo) => {
  const { technical, api, regulations } = await openSeededList(page, cleanupRegistry, testInfo);
  const writes = recordWrites(page);

  await expect(toggle(page, technical.name)).toHaveAttribute('aria-expanded', 'true');
  await rowAction(page, 'subcategoria', api.name, 'Edita');
  const dialog = page.getByRole('dialog');
  await expect(
    dialog.getByRole('heading', { name: 'Edició de la subcategoria documental' }),
  ).toBeVisible();
  await selectOption(page, 'document-category-dialog-category', regulations.name);
  await dialog.getByRole('button', { name: 'Desa els canvis de la subcategoria' }).click();

  await expect(dialog).toHaveCount(0);
  expect(writes).toEqual([
    {
      method: 'PUT',
      path: `/${api.id}`,
      body: { name: api.name, nameEs: api.nameEs, parentId: regulations.id },
    },
  ]);
  // The category the subcategory moved to is expanded to show it.
  await expect(toggle(page, regulations.name)).toHaveAttribute('aria-expanded', 'true');
  await expect(subcategory(page, regulations.name, api.name)).toBeVisible();
  await expect(subcategory(page, technical.name, api.name)).toHaveCount(0);
});

test('deletes and restores a subcategory and reports a category that still has subcategories', async ({
  page,
  cleanupRegistry,
}, testInfo) => {
  const { technical, manuals } = await openSeededList(page, cleanupRegistry, testInfo);
  const writes = recordWrites(page);

  await rowAction(page, 'categoria', technical.name, 'Elimina');
  await page.getByRole('button', { name: "Confirma l'eliminació de la categoria" }).click();
  await expect(page.getByText('té subcategories actives')).toBeVisible();

  await page.keyboard.press('Escape');
  await expect(page.getByRole('dialog')).toHaveCount(0);
  await expect(toggle(page, technical.name)).toHaveAttribute('aria-expanded', 'true');
  await rowAction(page, 'subcategoria', manuals.name, 'Elimina');
  await page.getByRole('button', { name: "Confirma l'eliminació de la subcategoria" }).click();
  await expect(page.getByRole('dialog')).toHaveCount(0);
  // The Actiu filter also leaves out the inactive subcategories; without it, the inactive
  // subcategory is shown under its category and can be restored.
  await expect(subcategory(page, technical.name, manuals.name)).toHaveCount(0);
  const list = page.locator('app-document-categories-list');
  await showFilters(list, 'Mostra o amaga els filtres');
  await page
    .locator('p-select:has(#document-categories-filter-status) .p-select-clear-icon')
    .click();
  const unfiltered = waitForApiResponse(
    page,
    'GET',
    CATEGORIES_API,
    (url) => !url.searchParams.has('statusId'),
  );
  await list.getByRole('button', { name: SEARCH_BUTTON }).click();
  await expectJsonResponse(await unfiltered);
  await expect(subcategory(page, technical.name, manuals.name)).toBeVisible();

  await rowAction(page, 'subcategoria', manuals.name, 'Restaura');
  await expect(subcategory(page, technical.name, manuals.name)).toBeVisible();
  await expect.poll(() => writes.length).toBe(3);
  expect(writes.map(({ method, path }) => `${method} ${path}`)).toEqual([
    `DELETE /${technical.id}`,
    `DELETE /${manuals.id}`,
    `PUT /reactivate/${manuals.id}`,
  ]);
});

test('expands the category whose subcategory matches a quick search', async ({
  page,
  cleanupRegistry,
}, testInfo) => {
  await openDocumentCategories(page);
  const { technical, manuals, regulations, subcategoryKey } = await seedCategories(
    page,
    cleanupRegistry,
    testInfo,
  );

  await quickSearch(page, `manuals ${subcategoryKey}`);

  await expect(categoryRow(page, regulations.name)).toHaveCount(0);
  await expect(toggle(page, technical.name)).toHaveAttribute('aria-expanded', 'true');
  await expect(subcategory(page, technical.name, manuals.name)).toBeVisible();
});

test('reflows at 320 px keeping the tables scroll inside their container', async ({
  page,
  cleanupRegistry,
}, testInfo) => {
  await page.setViewportSize({ width: 320, height: 800 });
  const { technical } = await openSeededList(page, cleanupRegistry, testInfo);

  await expect(toggle(page, technical.name)).toHaveAttribute('aria-expanded', 'true');
  await expect(subcategories(page, technical.name).first()).toBeVisible();
  const pageOverflow = await page.evaluate(
    () => document.documentElement.scrollWidth - document.documentElement.clientWidth,
  );
  expect(pageOverflow).toBeLessThanOrEqual(0);
  await page.screenshot({
    path: testInfo.outputPath('document-categories-mobile.png'),
    fullPage: true,
  });
});

const permission = (page: Page, permission: string, role: string) =>
  page.getByRole('checkbox', { name: `${permission} per a ${role}`, exact: true });

// The permissions the roles of the application have in a category. The back-end gives every role
// the permission to read the documents of a new category, and no other.
async function rolePermissions(page: Page, categoryId: number): Promise<RolePermissionRecord[]> {
  return expectApiJsonResponse<RolePermissionRecord[]>(
    await page.request.get(apiUrl(page, `${ROLE_PERMISSIONS_API}/${categoryId}`)),
    `GET ${ROLE_PERMISSIONS_API}/${categoryId}`,
  );
}

async function firstRoleOf(page: Page, categoryId: number): Promise<RolePermissionRecord> {
  const [role] = await rolePermissions(page, categoryId);
  if (!role) throw new Error('No role of the application has documentation permissions');
  return role;
}

test('opens the detail of a category with its subcategories and access permissions', async ({
  page,
  cleanupRegistry,
}, testInfo) => {
  const { technical, manuals } = await openSeededList(page, cleanupRegistry, testInfo);
  const roles = await rolePermissions(page, technical.id);
  const [role] = roles;
  if (!role) throw new Error('No role of the application has documentation permissions');

  await rowAction(page, 'categoria', technical.name, 'Consulta');

  await expect(page).toHaveURL(
    new RegExp(`/manteniments/documentacio/categories-documentals/${technical.id}$`),
  );
  await expect(page.getByRole('heading', { level: 1, name: technical.name })).toBeVisible();
  const breadcrumb = page.getByRole('navigation', { name: 'Ruta de navegació' });
  await expect(breadcrumb.getByRole('link', { name: 'Categories documentals' })).toBeVisible();
  await expect(page.getByLabel('Nom', { exact: true })).toHaveValue(technical.name);
  await expect(page.getByLabel('Nom en castellà')).toHaveValue(technical.nameEs);
  // "Consulta" shows the category until "Editar" is pressed.
  await expect(page.getByLabel('Nom', { exact: true })).toHaveAttribute('readonly', '');
  await expect(permission(page, 'Lectura', role.name)).toBeDisabled();
  await expect(page.getByRole('button', { name: 'Desa els canvis de la categoria' })).toHaveCount(
    0,
  );

  const subcategoriesTable = page.getByRole('table', {
    name: `Subcategories de «${technical.name}»`,
  });
  await expect(subcategoriesTable.getByRole('row')).toHaveCount(3);
  await expect(subcategoriesTable).toContainText(manuals.name);

  // One row per role of the application, which may only read the documents of the new category.
  const permissions = page.getByRole('table', { name: "Permisos d'accés de cada rol" });
  await expect(permissions.getByRole('row')).toHaveCount(roles.length + 1);
  await expect(permissions.getByRole('rowheader', { name: role.name, exact: true })).toBeVisible();
  await expect(permission(page, 'Lectura', role.name)).toBeChecked();
  await expect(permission(page, 'Càrrega', role.name)).not.toBeChecked();
  await expect(permission(page, 'Tots els permisos', role.name)).not.toBeChecked();
  await page.screenshot({
    path: testInfo.outputPath('document-category-detail.png'),
    fullPage: true,
  });
  const axe = await new AxeBuilder({ page })
    .withTags(['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa', 'wcag22aa'])
    .analyze();
  expect(
    axe.violations.map(({ id, nodes }) => `${id}: ${nodes.map(({ target }) => target).join(', ')}`),
  ).toEqual([]);

  await page.getByRole('button', { name: `Edita la categoria «${technical.name}»` }).click();
  await expect(page.getByLabel('Nom', { exact: true })).toBeFocused();
  await expect(page.getByLabel('Nom', { exact: true })).not.toHaveAttribute('readonly', '');
  await expect(permission(page, 'Lectura', role.name)).toBeEnabled();
  await page.screenshot({ path: testInfo.outputPath('document-category-detail-edit.png') });
  await page
    .getByRole('button', { name: 'Desa els canvis de la categoria' })
    .scrollIntoViewIfNeeded();
  await page.screenshot({ path: testInfo.outputPath('document-category-detail-actions.png') });

  await page.getByLabel('Nom', { exact: true }).fill('Una altra');
  await page.getByRole('button', { name: 'Cancel·la els canvis de la categoria' }).click();
  await expect(page.getByLabel('Nom', { exact: true })).toHaveValue(technical.name);
  await expect(
    page.getByRole('button', { name: `Edita la categoria «${technical.name}»` }),
  ).toBeFocused();
});

test('saves the names and the permissions of a category', async ({
  page,
  cleanupRegistry,
}, testInfo) => {
  const { token, technical } = await openSeededList(page, cleanupRegistry, testInfo);
  const role = await firstRoleOf(page, technical.id);
  const writes = recordWrites(page);
  const renamed = `E2E ${token} Tècnica i arquitectura`;
  await rowAction(page, 'categoria', technical.name, 'Edita');

  await page.getByLabel('Nom', { exact: true }).fill(renamed);
  await permission(page, 'Tots els permisos', role.name).click();
  await expect(permission(page, 'Eliminació (baixa lògica)', role.name)).toBeChecked();
  const permissionsSaved = waitForApiResponse(
    page,
    'PUT',
    `${ROLE_PERMISSIONS_API}/${technical.id}`,
  );
  await page.getByRole('button', { name: 'Desa els canvis de la categoria' }).click();

  const saved = await permissionsSaved;
  await expectJsonResponse(saved);
  expect(saved.request().postDataJSON().permissions).toContainEqual({
    roleId: role.roleId,
    canRead: true,
    canUpload: true,
    canEdit: true,
    canDelete: true,
  });
  await expect(page.getByRole('heading', { level: 1, name: renamed })).toBeVisible();
  await expect(page.getByRole('button', { name: `Edita la categoria «${renamed}»` })).toBeVisible();
  expect(writes).toEqual([
    {
      method: 'PUT',
      path: `/${technical.id}`,
      body: { name: renamed, nameEs: technical.nameEs, parentId: null },
    },
  ]);

  // The back-end stores the permissions.
  await page.getByRole('button', { name: 'Torna al llistat de categories documentals' }).click();
  await expect(page).toHaveURL(/\/manteniments\/documentacio#document-categories$/);
  await quickSearch(page, token);
  await rowAction(page, 'categoria', renamed, 'Consulta');
  await expect(permission(page, 'Tots els permisos', role.name)).toBeChecked();
  expect(await rolePermissions(page, technical.id)).toContainEqual({
    ...role,
    canUpload: true,
    canEdit: true,
    canDelete: true,
  });
});

test('asks before leaving a category with unsaved changes', async ({
  page,
  cleanupRegistry,
}, testInfo) => {
  const { technical } = await openSeededList(page, cleanupRegistry, testInfo);
  const role = await firstRoleOf(page, technical.id);
  await rowAction(page, 'categoria', technical.name, 'Edita');

  await permission(page, 'Càrrega', role.name).click();
  await page.getByRole('button', { name: 'Torna al llistat de categories documentals' }).click();
  const dialog = page.getByRole('dialog');
  await expect(dialog.getByText('Sortir sense desar?')).toBeVisible();
  await dialog.getByRole('button', { name: 'Continua editant la categoria' }).click();
  await expect(page).toHaveURL(new RegExp(`categories-documentals/${technical.id}$`));
  await expect(permission(page, 'Càrrega', role.name)).toBeChecked();

  await page.getByRole('button', { name: 'Torna al llistat de categories documentals' }).click();
  await page
    .getByRole('dialog')
    .getByRole('button', { name: 'Surt de la categoria sense desar els canvis' })
    .click();
  await expect(page).toHaveURL(/\/manteniments\/documentacio#document-categories$/);
});

test('edits a subcategory from the detail of its category', async ({
  page,
  cleanupRegistry,
}, testInfo) => {
  const { technical, api } = await openSeededList(page, cleanupRegistry, testInfo);
  const writes = recordWrites(page);
  await rowAction(page, 'categoria', technical.name, 'Consulta');

  await rowAction(page, 'subcategoria', api.name, 'Edita');
  const dialog = page.getByRole('dialog');
  await dialog.getByLabel('Nom', { exact: true }).fill(`${api.name} REST`);
  await dialog.getByRole('button', { name: 'Desa els canvis de la subcategoria' }).click();

  await expect(dialog).toHaveCount(0);
  await expect(
    page.getByRole('table', { name: `Subcategories de «${technical.name}»` }),
  ).toContainText(`${api.name} REST`);
  expect(writes).toEqual([
    {
      method: 'PUT',
      path: `/${api.id}`,
      body: { name: `${api.name} REST`, nameEs: api.nameEs, parentId: technical.id },
    },
  ]);
});

test('shows an inactive category without letting it change', async ({
  page,
  cleanupRegistry,
}, testInfo) => {
  const { procurement } = await openSeededList(page, cleanupRegistry, testInfo);
  await page.goto(`/manteniments/documentacio/categories-documentals/${procurement.id}`);

  await expect(page.getByRole('heading', { level: 1, name: procurement.name })).toBeVisible();
  await expect(page.getByText('Aquesta categoria està donada de baixa')).toBeVisible();
  await expect(page.getByLabel('Nom', { exact: true })).toHaveAttribute('readonly', '');
  // The back-end only keeps the permissions of the active categories.
  await expect(
    page.getByText('Els permisos només es poden consultar i modificar en les categories actives.'),
  ).toBeVisible();
  await expect(page.getByRole('table', { name: "Permisos d'accés de cada rol" })).toHaveCount(0);
  await expect(page.getByRole('button', { name: 'Desa els canvis de la categoria' })).toHaveCount(
    0,
  );
});
