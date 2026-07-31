import { test as setup, expect } from '@playwright/test';
import type { BrowserContext } from '@playwright/test';
import path from 'node:path';

const authFile = path.join(__dirname, '.auth/user.json');
const canonicalAppOrigin = 'https://invai.plexus.services';
const apiSessionCookieName = 'JSESSIONID';
const apiSessionCookiePath = '/invaiapi';
const idpNavigationTimeout = 60_000;

setup('authenticate against the real IdP', async ({ page, baseURL }) => {
  setup.setTimeout(90_000);

  const username = process.env.E2E_USERNAME;
  const password = process.env.E2E_PASSWORD;

  if (!username || !password) {
    throw new Error(
      'E2E_USERNAME / E2E_PASSWORD are not set. Copy .env.example to .env and fill in valid test credentials.',
    );
  }
  if (!baseURL) {
    throw new Error('Playwright baseURL is not configured.');
  }

  const localAppOrigin = new URL(baseURL).origin;

  await page.goto('/');

  // The app redirects to the external IdP (Soffid SAML, idp.caib.es) when there is no session cookie.
  await page.waitForURL((url) => url.origin !== localAppOrigin, {
    timeout: idpNavigationTimeout,
  });

  // Soffid's login is a two-step form: username first, then password on the next screen.
  // The username page has two "Accediu" submit buttons (password login vs. certificate login),
  // so target the password one by id instead of by accessible name.
  await page.locator('input[type="text"]').first().fill(username);
  await page.locator('#loginpassbutton').click();

  await page.locator('input[type="password"]').fill(password);
  await page.locator('#loginpassbutton').click();

  // Depending on the backend configuration, the IdP can return either to localhost or to the
  // canonical Plexus frontend. Both origins use the same backend session.
  await page.waitForURL(
    (url) => url.origin === localAppOrigin || url.origin === canonicalAppOrigin,
    { timeout: idpNavigationTimeout, waitUntil: 'domcontentloaded' },
  );

  if (page.url().startsWith(canonicalAppOrigin)) {
    await copyApiSessionToLocalOrigin(page.context(), localAppOrigin);

    const authenticatedSessionResponse = page.waitForResponse(
      (response) =>
        response.request().method() === 'GET' &&
        new URL(response.url()).pathname === `${apiSessionCookiePath}/interna/auth/me` &&
        response.status() === 200,
      { timeout: idpNavigationTimeout },
    );
    await page.goto(localAppOrigin);
    await authenticatedSessionResponse;
  }

  await expect(page.locator('app-main-layout')).toBeVisible();

  await page.context().storageState({ path: authFile });
});

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
