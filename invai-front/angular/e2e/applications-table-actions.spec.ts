import type { Page } from '@playwright/test';

import { expect, test } from './support/auth.fixture';
import { expectJsonResponse, waitForApiResponse } from './support/http';

// Uses the first application of the real list and never saves, so no record changes.
async function openList(page: Page): Promise<{ id: number }> {
  const listPromise = waitForApiResponse(page, 'GET', '/invaiback/application');
  await page.goto('/');
  const list = await expectJsonResponse<{ content: Array<{ id: number }> }>(await listPromise);
  expect(list.content.length, 'The application list needs at least one record').toBeGreaterThan(0);
  return list.content[0];
}

test('consults, enters editing once, cancels and reloads in consultation', async ({ page }) => {
  const first = await openList(page);
  const generalUrl = new RegExp(`/${first.id}/general$`);
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
  await expect(page).toHaveURL(generalUrl);
  const name = page.getByRole('textbox', { name: "Nom de l'aplicació", exact: true });
  await expect(name).toBeVisible();
  await expect(name).toHaveAttribute('readonly', '');
  const toolbar = page.locator('app-application-detail-section-actions');
  await expect(toolbar.getByRole('button', { name: /^Editar / })).toBeVisible();

  await page.goto(listUrl);
  await actions.click();
  await page.getByRole('menuitem', { name: 'Edita', exact: true }).click();
  await expect(page).toHaveURL(generalUrl);
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
  await openList(page);
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
