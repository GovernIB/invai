import { expect, test, type Page } from '@playwright/test';

test.use({ storageState: { cookies: [], origins: [] } });

async function openHome(page: Page, me: Record<string, unknown>) {
  await page.route('**/invaiback/**', async (route) => {
    const path = new URL(route.request().url()).pathname;
    const json = path.endsWith('/auth/me')
      ? { authenticated: true, ...me }
      : { content: [], totalElements: 0, totalPages: 1, last: true, number: 0 };
    await route.fulfill({ json });
  });
  await page.goto('/');
}

const userMenuButton = (page: Page) => page.getByTestId('button-user-menu-testid');

test('shows the full name with the username below it', async ({ page }) => {
  await openHome(page, { username: 'u12345', fullName: 'Mario Martínez García' });

  await expect(userMenuButton(page).getByTestId('user-menu-full-name')).toHaveText(
    'Mario Martínez García',
  );
  await expect(userMenuButton(page).getByTestId('user-menu-username')).toHaveText('u12345');
  await expect(userMenuButton(page).locator('p-avatar')).toHaveText('M');
});

test('falls back to the username when there is no full name', async ({ page }) => {
  await openHome(page, { username: 'mgarcia' });

  await expect(userMenuButton(page)).toContainText('Mgarcia');
  await expect(userMenuButton(page).getByTestId('user-menu-full-name')).toHaveCount(0);
  await expect(userMenuButton(page).getByTestId('user-menu-username')).toHaveCount(0);
});
