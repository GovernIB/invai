import { expect, test, type Page, type Route } from '@playwright/test';

test.use({ storageState: { cookies: [], origins: [] } });

const IDP_LOGIN_URL = 'https://idp.caib.es/e2e-login';
const IDP_LOGOUT_URL = 'https://idp.caib.es/logout.jsp';

const emptyPage = { content: [], totalElements: 0, totalPages: 1, last: true, number: 0 };

interface ShellMock {
  meRequests: number;
  logoutPosts: number;
  authenticated: boolean;
  applicationListResponses: Array<{ status: number; json: unknown }>;
}

async function openShell(page: Page, options: { authenticated?: boolean } = {}): Promise<ShellMock> {
  const mock: ShellMock = {
    meRequests: 0,
    logoutPosts: 0,
    authenticated: options.authenticated ?? true,
    applicationListResponses: [],
  };
  await page.route('https://idp.caib.es/**', (route) =>
    route.fulfill({ contentType: 'text/html', body: '<h1>IdP</h1>' }),
  );
  await page.route('**/invaiapi/externa/config/url', (route) =>
    route.fulfill({ json: { url: IDP_LOGIN_URL } }),
  );
  await page.route('**/invaiback/**', async (route: Route) => {
    const request = route.request();
    const path = new URL(request.url()).pathname;
    if (path.endsWith('/auth/me')) {
      mock.meRequests++;
      return mock.authenticated
        ? route.fulfill({ json: { authenticated: true, username: 'u12345' } })
        : route.fulfill({ status: 401, json: {} });
    }
    if (path.endsWith('/auth/logout')) {
      mock.logoutPosts++;
      return route.fulfill({ status: 204 });
    }
    if (path.endsWith('/invaiback/application') && mock.applicationListResponses.length > 0) {
      return route.fulfill(mock.applicationListResponses.shift()!);
    }
    return route.fulfill({ json: emptyPage });
  });
  return mock;
}

const userMenu = (page: Page) => page.getByTestId('button-user-menu-testid');

test('sends a visitor without a session to the IdP login', async ({ page }) => {
  const mock = await openShell(page, { authenticated: false });

  await page.goto('/');

  await expect(page).toHaveURL(IDP_LOGIN_URL);
  expect(mock.meRequests).toBe(1);
});

test('closes the session and leaves for the IdP logout', async ({ page }) => {
  const mock = await openShell(page);
  await page.goto('/');

  await userMenu(page).click();
  await page.getByRole('button', { name: 'Tancar sessió' }).click();

  await expect(page).toHaveURL(IDP_LOGOUT_URL);
  expect(mock.logoutPosts).toBe(1);
});

test('changes the font size and keeps it after reloading', async ({ page }) => {
  await openShell(page);
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

test('opens the help centre from the user menu and toggles it with Ctrl+K', async ({ page }) => {
  await openShell(page);
  await page.goto('/');
  // The help dialog uses a custom header, so it has no accessible name; find it by its heading.
  const help = page
    .getByRole('dialog')
    .filter({ has: page.getByRole('heading', { name: "Centre d'ajuda" }) });

  await userMenu(page).click();
  await page.getByRole('button', { name: 'Obrir ajuda' }).click();
  await expect(help).toBeVisible();
  await expect(help.getByRole('combobox', { name: "Cerca en tota l'ajuda" })).toBeVisible();

  await page.keyboard.press('Control+k');
  await expect(help).toBeHidden();
  await page.keyboard.press('Control+k');
  await expect(help).toBeVisible();
});

test('remembers the chosen language and loads its bundle', async ({ page }) => {
  await openShell(page);
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
  await openShell(page);
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
  const mock = await openShell(page);
  mock.applicationListResponses.push({
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
  const mock = await openShell(page);
  mock.applicationListResponses.push({ status: 401, json: {} });
  await page.goto('/');

  await expect.poll(() => mock.meRequests).toBe(2);
  await expect(page.locator('app-main-layout')).toBeVisible();
  await expect(page).toHaveURL(/localhost:4200\//);
});

// help-modal.html replaces the dialog header with a template, so PrimeNG no longer labels the
// dialog with its title; every role="dialog" must have an accessible name.
test.fixme('names the help centre dialog after its title', async ({ page }) => {
  await openShell(page);
  await page.goto('/');

  await userMenu(page).click();
  await page.getByRole('button', { name: 'Obrir ajuda' }).click();
  await expect(page.getByRole('dialog', { name: "Centre d'ajuda" })).toBeVisible();
});
