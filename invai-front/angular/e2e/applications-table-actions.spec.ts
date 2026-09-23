import { expect, test } from '@playwright/test';

// Exercise the actual router, menu and form without depending on an IdP session
// or changing backend records.
test.use({ storageState: { cookies: [], origins: [] } });

const application = {
  id: 1,
  name: 'Menu test application',
  code: 'TEST',
  prefix: 'TST',
  status: 'ACTIVE',
  category: { id: 1, name: 'Category' },
  systemType: { id: 1, name: 'System type' },
  field: { id: 1, name: 'Scope' },
  department: { code: 'D1', name: 'Department' },
  admUnit: { code: 'U1', name: 'Unit' },
  csCommission: { id: 1, name: 'Commission' },
  description: 'Action menu browser test',
};

test.beforeEach(async ({ page }) => {
  await page.route('**/invaiback/**', async (route) => {
    const path = new URL(route.request().url()).pathname;
    let json: unknown = { content: [], totalElements: 0, totalPages: 0, last: true, number: 0 };
    if (path.endsWith('/auth/me')) {
      json = { authenticated: true, username: 'table-actions-test' };
    } else if (path.endsWith('/application/1')) {
      json = application;
    } else if (path.endsWith('/application')) {
      json = { content: [application], totalElements: 1, totalPages: 1, last: true, number: 0 };
    }
    await route.fulfill({ json });
  });
});

test('consults, enters editing once, cancels and reloads in consultation', async ({ page }) => {
  await page.goto('/');
  const actions = page.locator('app-applications-table tbody .invai-table-actions-column button').first();
  await expect(actions).toBeVisible();
  const listUrl = page.url();
  await actions.focus();
  await actions.press('Enter');
  await expect(actions).toHaveAttribute('aria-expanded', 'true');
  const menu = page.locator('.maintenance-row-menu').getByRole('menu');
  await expect(menu).toBeFocused();
  await menu.press('Escape');
  await expect(actions).toBeFocused();
  await expect(actions).toHaveAttribute('aria-expanded', 'false');
  await expect(page).toHaveURL(listUrl);

  await actions.press('Space');
  await page.getByRole('menuitem', { name: 'Consulta', exact: true }).click();
  await expect(page).toHaveURL(/\/1\/general$/);
  const name = page.getByRole('textbox', { name: "Nom de l'aplicació", exact: true });
  await expect(name).toBeVisible();
  await expect(name).toHaveAttribute('readonly', '');
  const toolbar = page.locator('app-application-detail-section-actions');
  await expect(toolbar.getByRole('button', { name: /^Editar / })).toBeVisible();

  await page.goto(listUrl);
  await actions.click();
  await page.getByRole('menuitem', { name: 'Edita', exact: true }).click();
  await expect(page).toHaveURL(/\/1\/general$/);
  await expect(toolbar.getByRole('button', { name: /^Desar els canvis de / })).toBeVisible();
  await expect(name).not.toHaveAttribute('readonly');
  await toolbar.getByRole('button', { name: /^Cancel·lar els canvis de / }).click();
  await expect(toolbar.getByRole('button', { name: /^Editar / })).toBeVisible();

  await page.goto(listUrl);
  await actions.click();
  await page.getByRole('menuitem', { name: 'Edita', exact: true }).click();
  await expect(toolbar.getByRole('button', { name: /^Desar els canvis de / })).toBeVisible();
  await page.reload();
  await expect(toolbar.getByRole('button', { name: /^Editar / })).toBeVisible();
});

test('keeps the actions accessible at 320px and returns focus from the menu', async ({ page }) => {
  await page.setViewportSize({ width: 320, height: 720 });
  await page.goto('/');
  const actions = page.locator('app-applications-table tbody .invai-table-actions-column button').first();
  await expect(actions).toBeVisible();
  const box = await actions.boundingBox();
  expect(box!.width).toBeGreaterThanOrEqual(24);
  expect(box!.height).toBeGreaterThanOrEqual(24);
  expect(box!.x + box!.width).toBeLessThanOrEqual(320);
  await actions.click();
  await expect(page.getByRole('menuitem', { name: 'Edita', exact: true })).toBeVisible();
  await page.locator('.maintenance-row-menu').getByRole('menu').press('Escape');
  await expect(actions).toBeFocused();
});
