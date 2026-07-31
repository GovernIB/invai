import { test, expect } from '@playwright/test';

test('loads the authenticated app shell', async ({ page }) => {
  await page.goto('/');

  await expect(page).toHaveTitle('INVAI Front');
  await expect(page.getByTestId('button-notifications-testid')).toBeVisible();
});
