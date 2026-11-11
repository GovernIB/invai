import { expect, test } from './support/auth.fixture';
import { apiUrl, expectApiJsonResponse } from './support/http';

interface AuthenticatedUser {
  username: string;
  fullName?: string | null;
}

// The fallback to the username without a full name is covered by the component unit test.
test('shows the full name of the signed-in user with the username below it', async ({ page }) => {
  await page.goto('/');
  const me = await expectApiJsonResponse<AuthenticatedUser>(
    await page.request.get(apiUrl(page, '/invaiback/auth/me')),
    'GET /invaiback/auth/me',
  );
  expect(me.fullName, 'The E2E user needs a full name in the IdP').toBeTruthy();

  const userMenuButton = page.getByTestId('button-user-menu-testid');
  await expect(userMenuButton.getByTestId('user-menu-full-name')).toHaveText(me.fullName!);
  await expect(userMenuButton.getByTestId('user-menu-username')).toHaveText(me.username);
  await expect(userMenuButton.locator('p-avatar')).toHaveText(me.fullName!.charAt(0).toUpperCase());
});
