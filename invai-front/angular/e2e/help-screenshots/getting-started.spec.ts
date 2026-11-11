import type { Page } from '@playwright/test';

import { expect, test } from '../support/auth.fixture';
import type { HelpLocale } from './capture';
import {
  helpDialog,
  maskText,
  openApp,
  openHelp,
  saveHelpImage,
  topicPath,
  uiText,
} from './capture';

const USING_HELP = 'getting-started/using-help.md';
// Two results, the one with the words in its title first, so the image shows the order.
const SEARCH_TERM = { ca: "menú d'usuari", es: 'menú de usuario' };

test('the help centre with its list of topics and one of them open', async ({ page }) => {
  const locale = await openHelp(page);
  const dialog = helpDialog(page);
  const topics = dialog.getByRole('navigation');
  const [group, topic] = topicPath(locale, USING_HELP);

  // Only the screen's branch starts expanded; reveal this topic's group first.
  await topics.getByRole('button', { name: group, exact: true }).click();
  await topics.getByRole('button', { name: topic }).click();
  await expect(dialog.getByRole('heading', { level: 3, name: topic })).toBeVisible();

  // Down to the paragraph after the first list of the topic, which ends lower than the list of
  // topics (the navigation itself stretches to the height of the topic), so no line is cut.
  await saveHelpImage(page, dialog, locale, 'getting-started/imgs/help-centre.webp', {
    cropTo: [topics.locator('ul').first(), dialog.locator('markdown p').nth(1)],
    margin: 12,
  });
});

test('a search in the help centre and its result', async ({ page }) => {
  const locale = await openHelp(page);
  const dialog = helpDialog(page);
  const [, topic] = topicPath(locale, USING_HELP);

  await dialog.locator('#help-modal-search').pressSequentially(SEARCH_TERM[locale]);
  await expect(page.getByRole('option', { name: topic })).toBeVisible();

  await saveHelpImage(page, dialog, locale, 'getting-started/imgs/help-search.webp', {
    cropTo: [page.getByRole('listbox')],
    // The dialog behind the suggestions is not part of what the image explains.
    margin: 4,
  });
});

const EXAMPLE_USER = { ca: 'Usuari de proves', es: 'Usuario de pruebas' };

/** Puts example data where the header shows the name of whoever takes the captures. */
async function maskUser(page: Page, locale: HelpLocale): Promise<void> {
  const userMenu = page.getByTestId('button-user-menu-testid');
  await maskText(userMenu.getByTestId('user-menu-full-name'), EXAMPLE_USER[locale]);
  await maskText(userMenu.getByTestId('user-menu-username'), 'u00000');
  await maskText(userMenu.locator('.p-avatar-label'), EXAMPLE_USER[locale][0]);
}

test('the header, with example data for the user', async ({ page }) => {
  const locale = await openApp(page);
  await maskUser(page, locale);

  await saveHelpImage(
    page,
    page.locator('header').first(),
    locale,
    'getting-started/imgs/header.webp',
  );
});

test('the side menu with the submenu of the maintenances open', async ({ page }) => {
  const locale = await openApp(page);
  const sidebar = page.getByRole('navigation', { name: uiText(locale, 'Navegació lateral') });

  // The item and its link share the name; the link is what opens the submenu.
  await sidebar
    .locator('a[aria-haspopup="true"]')
    .filter({ hasText: uiText(locale, 'Manteniments') })
    .click();
  const submenu = sidebar.locator('.p-tieredmenu-submenu');
  await expect(submenu).toBeVisible();
  // The submenu opens over the page; without the page behind, the image shows only the menu.
  await page.addStyleTag({ content: 'main { visibility: hidden; }' });

  await saveHelpImage(page, sidebar, locale, 'getting-started/imgs/sidebar.webp', {
    area: [sidebar.locator('button').first(), sidebar.locator('.p-tieredmenu-root-list'), submenu],
    margin: 8,
  });
});

test('the user menu', async ({ page }) => {
  const locale = await openApp(page);

  await page.getByTestId('button-user-menu-testid').click();
  const menu = page.locator('.p-popover');
  await expect(menu.getByRole('button', { name: uiText(locale, 'Obrir ajuda') })).toBeVisible();

  await saveHelpImage(page, menu, locale, 'getting-started/imgs/user-menu.webp');
});

test('the toolbar of a list and its list of columns', async ({ page }) => {
  const locale = await openApp(page);
  const toolbar = page.locator('app-section-actions').first();
  await expect(toolbar).toBeVisible();

  // The badge with the number of filters sticks out of the toolbar; the margin keeps it whole.
  await saveHelpImage(page, toolbar, locale, 'getting-started/imgs/toolbar.webp', {
    area: [toolbar],
    margin: 12,
  });

  // The combobox the selector exposes is hidden; the visible part of the component opens it.
  await expect(toolbar.getByLabel(uiText(locale, 'Mostra o oculta columnes'))).toBeAttached();
  await toolbar.locator('.p-multiselect').click();
  const columns = page.locator('.p-multiselect-overlay');
  await expect(columns).toBeVisible();

  await saveHelpImage(page, columns, locale, 'getting-started/imgs/columns.webp');
});
