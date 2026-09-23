import type { ConsoleMessage } from '@playwright/test';

import { expect, test } from './support/auth.fixture';

interface ApplicationPageResponse {
  content: Array<{ id: number }>;
}

test('filters application security roles by system using Enter and the search icon', async ({ page }) => {
  const pageErrors: string[] = [];
  const consoleErrors: string[] = [];
  const roleQueries: URL[] = [];
  page.on('request', (request) => {
    const url = new URL(request.url());
    if (/\/application\/security\/role\/\d+$/.test(url.pathname)) roleQueries.push(url);
  });
  page.on('pageerror', (error) => pageErrors.push(error.message));
  page.on('console', (message: ConsoleMessage) => {
    if (message.type() === 'error') consoleErrors.push(message.text());
  });

  await page.goto('/');
  const applicationsResponse = await page.request.get('/invaiback/application', {
    params: { page: 0, size: 1, statusId: 1 },
  });
  expect(applicationsResponse.ok()).toBeTruthy();
  const applications = (await applicationsResponse.json()) as ApplicationPageResponse;
  expect(applications.content.length).toBeGreaterThan(0);

  const applicationId = applications.content[0].id;
  await page.goto(`/aplicacions/${applicationId}/general`);
  await page.getByRole('link', { name: /Seguretat/ }).click();

  await expect(page).toHaveURL(new RegExp(`/aplicacions/${applicationId}/security$`), {
    timeout: 15_000,
  });
  await expect(page.getByRole('heading', { name: 'Seguretat', exact: true })).toBeVisible();
  const system = page.getByRole('textbox', { name: 'Sistema', exact: true });
  const search = page.getByRole('button', { name: 'Cerca rols per sistema', exact: true });
  await expect(system).toHaveValue('weblogic');
  expect(roleQueries).toHaveLength(1);
  expect(roleQueries[0].searchParams.get('system')).toBe('weblogic');
  await expect(page.locator('app-application-security-section app-search-filters')).toHaveCount(0);

  await system.fill('INVA');
  expect(roleQueries).toHaveLength(1);
  const enterResponse = page.waitForResponse((response) => {
    const url = new URL(response.url());
    return /\/application\/security\/role\/\d+$/.test(url.pathname)
      && url.searchParams.get('system') === 'INVA';
  });
  await system.press('Enter');
  expect((await enterResponse).ok()).toBeTruthy();
  await expect(system).toBeFocused();
  expect(roleQueries.at(-1)?.searchParams.get('page')).toBe('0');

  await system.fill('');
  await system.press('Tab');
  await expect(search).toBeFocused();
  const clearResponse = page.waitForResponse((response) => {
    const url = new URL(response.url());
    return /\/application\/security\/role\/\d+$/.test(url.pathname)
      && !url.searchParams.has('system');
  });
  await search.click();
  expect((await clearResponse).ok()).toBeTruthy();

  await page.setViewportSize({ width: 320, height: 800 });
  await expect(system).toBeVisible();
  await expect(search).toBeVisible();
  const bounds = await page.locator('.application-security__role-filter').boundingBox();
  expect(bounds).not.toBeNull();
  expect(bounds!.x + bounds!.width).toBeLessThanOrEqual(320);
  await page.screenshot({ path: 'test-results/security-system-filter-mobile.png', fullPage: true });
  expect(pageErrors).toEqual([]);
  expect(consoleErrors).toEqual([]);
});
