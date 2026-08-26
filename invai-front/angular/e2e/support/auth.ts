import { expect } from '@playwright/test';
import type { BrowserContext, Page } from '@playwright/test';

const canonicalAppOrigin = 'https://invai.plexus.services';
const apiSessionCookieName = 'JSESSIONID';
const apiSessionCookiePath = '/invaiapi';
const authMePath = `${apiSessionCookiePath}/interna/auth/me`;
const idpNavigationTimeout = 60_000;

export async function ensureAuthenticatedSession(page: Page, baseURL: string): Promise<boolean> {
  const localAppOrigin = new URL(baseURL).origin;
  const sessionResponse = await page.request.get(`${localAppOrigin}${authMePath}`);
  const isAuthenticated = sessionResponse.ok();
  await sessionResponse.dispose();

  if (isAuthenticated) return false;

  await authenticateAgainstRealIdp(page, localAppOrigin);
  return true;
}

export async function authenticateAgainstRealIdp(page: Page, baseURL: string): Promise<void> {
  const localAppOrigin = new URL(baseURL).origin;
  await page.goto(localAppOrigin);

  const usernameInput = page.locator('input[type="text"]').first();
  const passwordLoginButton = page.locator('#loginpassbutton');
  const authenticationState = await Promise.race([
    passwordLoginButton
      .waitFor({ state: 'visible', timeout: idpNavigationTimeout })
      .then(() => 'credentials' as const),
    page
      .locator('app-main-layout')
      .waitFor({ state: 'visible', timeout: idpNavigationTimeout })
      .then(() => 'authenticated' as const),
  ]);

  if (authenticationState === 'credentials') {
    const username = process.env.E2E_USERNAME;
    const password = process.env.E2E_PASSWORD;

    if (!username || !password) {
      throw new Error(
        'E2E_USERNAME / E2E_PASSWORD are not set. Copy .env.example to .env and fill in valid test credentials.',
      );
    }

    await usernameInput.fill(username);
    await passwordLoginButton.click();
    await page.locator('input[type="password"]').fill(password);
    await passwordLoginButton.click();
    await page
      .locator('app-main-layout')
      .waitFor({ state: 'visible', timeout: idpNavigationTimeout });
  }

  if (page.url().startsWith(canonicalAppOrigin)) {
    await copyApiSessionToLocalOrigin(page.context(), localAppOrigin);

    const authenticatedSessionResponse = page.waitForResponse(
      (response) =>
        response.request().method() === 'GET' &&
        new URL(response.url()).pathname === authMePath &&
        response.status() === 200,
      { timeout: idpNavigationTimeout },
    );
    await page.goto(localAppOrigin);
    await authenticatedSessionResponse;
  }

  await expect(page.locator('app-main-layout')).toBeVisible();
}

async function copyApiSessionToLocalOrigin(
  context: BrowserContext,
  localAppOrigin: string,
): Promise<void> {
  const canonicalCookies = await context.cookies(`${canonicalAppOrigin}${apiSessionCookiePath}`);
  const sessionCookie = canonicalCookies.find((cookie) => cookie.name === apiSessionCookieName);

  if (!sessionCookie) {
    throw new Error(
      `The IdP returned to ${canonicalAppOrigin}, but no ${apiSessionCookieName} cookie was found.`,
    );
  }

  const localUrl = new URL(localAppOrigin);
  await context.addCookies([
    {
      name: sessionCookie.name,
      value: sessionCookie.value,
      domain: localUrl.hostname,
      path: apiSessionCookiePath,
      expires: sessionCookie.expires,
      httpOnly: sessionCookie.httpOnly,
      secure: localUrl.protocol === 'https:',
      sameSite: 'Lax',
    },
  ]);
}
