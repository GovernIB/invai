import type { Page } from '@playwright/test';

import { expect, test } from './support/auth.fixture';
import { failApiRequest } from './support/http';

const IDP_ORIGIN = /^https:\/\/idp\.caib\.es\//;
const IDP_LOGOUT_URL = 'https://idp.caib.es/logout.jsp';
const APPLICATION_API_PATH = '/invaiback/application';

const userMenu = (page: Page) => page.getByTestId('button-user-menu-testid');

test('sends a visitor without a session to the IdP login', async ({ browser, baseURL }) => {
  const visitor = await browser.newContext({ baseURL, storageState: { cookies: [], origins: [] } });
  try {
    const page = await visitor.newPage();
    const meStatuses: number[] = [];
    page.on('response', (response) => {
      if (new URL(response.url()).pathname === '/invaiback/auth/me') {
        meStatuses.push(response.status());
      }
    });

    await page.goto('/');

    await expect(page).toHaveURL(IDP_ORIGIN, { timeout: 30_000 });
    expect(meStatuses).toEqual([401]);
  } finally {
    await visitor.close();
  }
});

// Ends the real session: the next test signs in again through the auth fixture.
test('closes the session and leaves for the IdP logout', async ({ page }) => {
  await page.goto('/');
  const logout = page.waitForResponse(
    (response) =>
      response.request().method() === 'POST' &&
      new URL(response.url()).pathname === '/invaiback/auth/logout',
  );

  await userMenu(page).click();
  await page.getByRole('button', { name: 'Tancar sessió' }).click();

  expect((await logout).status()).toBe(204);
  await expect(page).toHaveURL((url) => url.href.startsWith(IDP_LOGOUT_URL));
  const me = await page.request.get('/invaiback/auth/me');
  expect(me.status()).toBe(401);
});

test('changes the font size and keeps it after reloading', async ({ page }) => {
  await page.goto('/');
  const fontSize = () =>
    page.evaluate(() => document.documentElement.style.getPropertyValue('--font-size'));
  await expect.poll(fontSize).toBe('0.9rem');

  await userMenu(page).click();
  await page.getByRole('button', { name: 'Augmenta la mida de la font' }).click();
  await page.getByRole('button', { name: 'Augmenta la mida de la font' }).click();
  await expect.poll(fontSize).toBe('1.1rem');

  await page.reload();
  await expect.poll(fontSize).toBe('1.1rem');

  await userMenu(page).click();
  await page.getByRole('button', { name: 'Redueix la mida de la font' }).click();
  await expect.poll(fontSize).toBe('1rem');
  await page.getByRole('button', { name: 'Restableix la mida de la font' }).click();
  await expect.poll(fontSize).toBe('0.9rem');
  expect(await page.evaluate(() => localStorage.getItem('fontSize'))).toBe('0.9');
});

test('remembers the chosen language and loads its bundle', async ({ page }) => {
  // The Spanish bundle only exists in the localized build, not in the dev server.
  await page.route('**/invaifront/es/**', (route) =>
    route.fulfill({ contentType: 'text/html', body: '<h1>ES</h1>' }),
  );
  await page.goto('/');

  await userMenu(page).click();
  await expect(page.getByRole('button', { name: 'CA', exact: true })).toHaveAttribute('aria-pressed', 'true');
  await page.getByRole('button', { name: 'ES', exact: true }).click();

  await expect(page).toHaveURL(/\/invaifront\/es\/$/);
  expect(await page.evaluate(() => localStorage.getItem('invai.language'))).toBe('es');
});

test('collapses and expands the side navigation', async ({ page }) => {
  await page.setViewportSize({ width: 1280, height: 800 });
  await page.goto('/');

  const close = page.getByRole('button', { name: 'Tanca el menú', exact: true });
  await expect(close).toHaveAttribute('aria-expanded', 'true');
  await close.click();

  const open = page.getByRole('button', { name: 'Obre el menú', exact: true });
  await expect(open).toHaveAttribute('aria-expanded', 'false');
  await open.click();
  await expect(page.getByRole('button', { name: 'Tanca el menú', exact: true })).toHaveAttribute(
    'aria-expanded',
    'true',
  );
});

test('shows a structured bad request in the server error dialog', async ({ page }) => {
  await failApiRequest(page, 'GET', APPLICATION_API_PATH, {
    status: 400,
    json: { error: 'Petició incorrecta', message: 'El filtre E2E no és vàlid.' },
  });
  await page.goto('/');

  const dialog = page.getByRole('dialog', { name: 'Petició incorrecta' });
  await expect(dialog).toBeVisible();
  await expect(dialog.getByRole('alert')).toHaveText('El filtre E2E no és vàlid.');
  await dialog.getByRole('button', { name: "Tanca el missatge d'error" }).click();
  await expect(dialog).toBeHidden();
});

test('keeps the user in the app when a 401 arrives with the session still alive', async ({
  page,
}) => {
  const meStatuses: number[] = [];
  page.on('response', (response) => {
    if (new URL(response.url()).pathname === '/invaiback/auth/me') meStatuses.push(response.status());
  });
  const listing = await failApiRequest(page, 'GET', APPLICATION_API_PATH, { status: 401, json: {} });
  await page.goto('/');

  await expect.poll(() => listing.hits.length).toBe(1);
  await expect.poll(() => meStatuses).toEqual([200, 200]);
  await expect(page.locator('app-main-layout')).toBeVisible();
  await expect(page).toHaveURL(/localhost:4200\//);
});
