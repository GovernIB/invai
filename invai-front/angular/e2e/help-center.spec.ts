import type { Page } from '@playwright/test';

import { expect, test } from './support/auth.fixture';
import { failApiRequest } from './support/http';

// The help centre reads static files from public/help/ca; these tests use the real first topic.
const TOPIC = "Com fer servir l'ajuda";

const userMenuTrigger = (page: Page) => page.getByRole('button', { name: "Obre el menú d'usuari" });
const helpDialog = (page: Page) => page.getByRole('dialog', { name: "Centre d'ajuda" });
const searchField = (page: Page) =>
  helpDialog(page).getByRole('combobox', { name: "Cerca en tota l'ajuda" });
const topics = (page: Page) => helpDialog(page).getByRole('navigation', { name: "Temes d'ajuda" });

async function openWithShortcut(page: Page): Promise<void> {
  await page.goto('/');
  await expect(page.locator('.app-initial-loader')).toBeHidden({ timeout: 30_000 });
  await page.keyboard.press('Control+k');
  await expect(helpDialog(page)).toBeVisible();
  // The dialog moves focus to the search field once it has opened; acting before that, the
  // focus a test expects elsewhere would be taken back by it.
  await expect(searchField(page)).toBeFocused();
}

test('opens the help centre from the user menu and toggles it with Ctrl+K', async ({ page }) => {
  await page.goto('/');

  await userMenuTrigger(page).click();
  await page.locator('.p-popover').getByRole('button', { name: 'Obrir ajuda' }).click();
  await expect(helpDialog(page)).toBeVisible();
  await expect(searchField(page)).toBeVisible();

  await page.keyboard.press('Control+k');
  await expect(helpDialog(page)).toBeHidden();
  await page.keyboard.press('Control+k');
  await expect(helpDialog(page)).toBeVisible();
});

test('starts on the search field and returns focus when it is closed with Escape', async ({
  page,
}) => {
  await page.goto('/');
  await userMenuTrigger(page).focus();

  await page.keyboard.press('Control+k');
  await expect(helpDialog(page)).toBeVisible();
  await expect(searchField(page)).toBeFocused();
  await expect(
    helpDialog(page).getByRole('button', { name: "Tanca el centre d'ajuda" }),
  ).toBeVisible();

  await page.keyboard.press('Escape');
  await expect(helpDialog(page)).toBeHidden();
  await expect(userMenuTrigger(page)).toBeFocused();
});

test('opens on the topic of the current screen and keeps focus on the search field', async ({
  page,
}) => {
  await openWithShortcut(page);

  await expect(
    helpDialog(page).getByRole('heading', { level: 3, name: "Llista d'aplicacions" }),
  ).toBeVisible();
  await expect(searchField(page)).toBeFocused();
  await expect(helpDialog(page)).toHaveAccessibleDescription("Tema obert: Llista d'aplicacions");

  await page.keyboard.press('Escape');
  await page.goto('/aplicacions/new');
  await expect(page.locator('.app-initial-loader')).toBeHidden({ timeout: 30_000 });
  await page.keyboard.press('Control+k');

  await expect(
    helpDialog(page).getByRole('heading', { level: 3, name: "Alta d'una aplicació" }),
  ).toBeVisible();
});

test('reopens with only the active branch expanded', async ({ page }) => {
  await openWithShortcut(page);
  const groups = topics(page).locator('button[aria-expanded="true"]');
  const applications = topics(page).getByRole('button', { name: 'Aplicacions', exact: true });
  const firstSteps = topics(page).getByRole('button', { name: 'Primers passos', exact: true });
  await expect(groups).toHaveCount(1);
  await expect(applications).toHaveAttribute('aria-expanded', 'true');
  await firstSteps.click();
  await expect(groups).toHaveCount(2);
  await applications.click();
  await page.keyboard.press('Escape');
  await expect(helpDialog(page)).toBeHidden();
  await page.keyboard.press('Control+k');
  await expect(searchField(page)).toBeFocused();
  await expect(groups).toHaveCount(1);
  await expect(applications).toHaveAttribute('aria-expanded', 'true');
  await expect(firstSteps).toHaveAttribute('aria-expanded', 'false');
  await expect(topics(page).getByRole('button', { name: "Llista d'aplicacions", exact: true }))
    .toHaveAttribute('aria-current', 'true');
});

test('opens the own topic of every application tab and keeps the search focused', async ({
  page,
}) => {
  test.setTimeout(90_000);
  await page.goto('/');
  await expect(page.locator('.app-initial-loader')).toBeHidden({ timeout: 30_000 });
  await page.locator('app-applications-table').getByRole('row').nth(1).dblclick();
  const tabs = page.getByRole('navigation', { name: "Seccions de l'aplicació" });
  await expect(tabs).toBeVisible();

  await page.keyboard.press('Control+k');
  await expect(helpDialog(page).getByRole('heading', { level: 3, name: 'General' })).toBeVisible();
  await page.keyboard.press('Escape');

  for (const [tab, route] of [
    ['Responsables', 'responsible'],
    ['Sistemes i BD', 'systems-databases'],
    ['Desenvolupament', 'development'],
    ['Accessibilitat', 'accessibility'],
    ['Seguretat', 'security'],
    ['Dades', 'data'],
    ['Integracions', 'integrations'],
    ['Documentació', 'documentation'],
  ]) {
    await tabs.getByRole('link', { name: new RegExp(`^${tab}`) }).click();
    // The tab loads its data before the route changes; the help follows the route.
    await expect(page).toHaveURL(new RegExp(`/${route}$`));
    await page.keyboard.press('Control+k');
    await expect(helpDialog(page).getByRole('heading', { level: 3, name: tab })).toBeVisible();
    const images = helpDialog(page).locator('markdown img');
    await expect(images).not.toHaveCount(0);
    for (const image of await images.all()) {
      await expect.poll(() => image.evaluate((img) => (img as HTMLImageElement).naturalWidth))
        .toBeGreaterThan(0);
    }
    await expect(searchField(page)).toBeFocused();
    await expect(helpDialog(page)).toHaveAccessibleDescription(`Tema obert: ${tab}`);
    await page.keyboard.press('Escape');
    await expect(helpDialog(page)).toBeHidden();
  }

});

test('opens a topic from the list, marks it and moves focus to its heading', async ({ page }) => {
  await openWithShortcut(page);
  await topics(page).getByRole('button', { name: 'Primers passos', exact: true }).click();
  const topic = topics(page).getByRole('button', { name: TOPIC });

  await topic.click();

  const heading = helpDialog(page).getByRole('heading', { level: 3, name: TOPIC });
  await expect(heading).toBeVisible();
  await expect(heading).toBeFocused();
  await expect(topic).toHaveAttribute('aria-current', 'true');
  await expect(topics(page).locator('[aria-current]')).toHaveCount(1);
});

test('opens a linked topic inside the help, without leaving the page', async ({ page }) => {
  await openWithShortcut(page);
  const url = page.url();
  await topics(page).getByRole('button', { name: "Alta d'una aplicació" }).click();

  await helpDialog(page)
    .locator('markdown')
    .getByRole('link', { name: "Llista d'aplicacions" })
    .click();

  await expect(
    helpDialog(page).getByRole('heading', { level: 3, name: "Llista d'aplicacions" }),
  ).toBeFocused();
  await expect(topics(page).getByRole('button', { name: "Llista d'aplicacions" })).toHaveAttribute(
    'aria-current',
    'true',
  );
  expect(page.url()).toBe(url);
});

test('moves to the section a link of the same topic points to', async ({ page }) => {
  await openWithShortcut(page);
  await topics(page).getByRole('button', { name: "Alta d'una aplicació" }).click();

  await helpDialog(page).locator('markdown').getByRole('link', { name: 'Desar' }).click();

  await expect(helpDialog(page).getByRole('heading', { level: 4, name: 'Desar' })).toBeFocused();
});

test('shows and hides the topics of a group from the keyboard', async ({ page }) => {
  await openWithShortcut(page);
  const group = topics(page).getByRole('button', { name: 'Primers passos' });
  const topic = topics(page).getByRole('button', { name: TOPIC });
  await expect(group).toHaveAttribute('aria-expanded', 'false');
  await expect(topic).toBeHidden();

  await group.focus();
  await page.keyboard.press('Enter');
  await expect(group).toHaveAttribute('aria-expanded', 'true');
  await expect(topic).toBeVisible();

  await page.keyboard.press('Space');
  await expect(group).toHaveAttribute('aria-expanded', 'false');
  await expect(topic).toBeHidden();
  await page.keyboard.press('Space');
  await expect(group).toHaveAttribute('aria-expanded', 'true');
  await page.keyboard.press('Tab');
  await expect(topic).toBeFocused();
});

test('finds a topic by its visible text, ignoring case, and opens it from the keyboard', async ({
  page,
}) => {
  await openWithShortcut(page);

  // Only this topic speaks of maximizing the help; the result is named with its group.
  await searchField(page).pressSequentially('MAXIMITZAR');
  const option = page.getByRole('option').first();
  await expect(option).toHaveAccessibleName(`${TOPIC}, Primers passos`);
  await expect(option).toContainText('Primers passos');
  await expect(option.locator('mark')).toHaveText('maximitzar');
  await expect(option.locator('h1, h2, h3, h4, ul, ol')).toHaveCount(0);

  await page.keyboard.press('Enter');

  await expect(helpDialog(page).getByRole('heading', { level: 3, name: TOPIC })).toBeFocused();
  await expect(searchField(page)).toHaveValue('');
});

test('finds a topic by words in any order and puts first the one with them in its title', async ({
  page,
}) => {
  await openWithShortcut(page);

  await searchField(page).pressSequentially('aplicacio alta');
  await expect(page.getByRole('option').first()).toHaveAccessibleName(
    "Alta d'una aplicació, Aplicacions",
  );
  await page.keyboard.press('Enter');

  await expect(
    helpDialog(page).getByRole('heading', { level: 3, name: "Alta d'una aplicació" }),
  ).toBeFocused();
});

test('closes only the suggestions on the first Escape', async ({ page }) => {
  await openWithShortcut(page);

  await searchField(page).pressSequentially('ajuda');
  await expect(page.getByRole('option', { name: TOPIC })).toBeVisible();

  await page.keyboard.press('Escape');
  await expect(page.getByRole('option', { name: TOPIC })).toBeHidden();
  await expect(helpDialog(page)).toBeVisible();

  await page.keyboard.press('Escape');
  await expect(helpDialog(page)).toBeHidden();
});

test('announces an index that does not load and loads it again on retry', async ({ page }) => {
  const index = await failApiRequest(page, 'GET', /\/help\/ca\/index\.json$/, {
    status: 404,
    body: '',
  });
  await openWithShortcut(page);

  await expect(topics(page).getByRole('alert')).toHaveText(
    "No s'han pogut carregar els temes d'ajuda.",
  );
  await topics(page).getByRole('button', { name: 'Reintentar' }).click();

  await expect(topics(page).getByText('Primers passos')).toBeVisible();
  await expect(topics(page).getByRole('alert')).toHaveCount(0);
  expect(index.hits).toHaveLength(1);
});

test('announces a topic that does not load and loads it again on retry', async ({ page }) => {
  const topic = await failApiRequest(page, 'GET', /\/help\/ca\/getting-started\/using-help\.md$/, {
    status: 500,
    body: '',
  });
  await openWithShortcut(page);

  await topics(page).getByRole('button', { name: 'Primers passos', exact: true }).click();
  await topics(page).getByRole('button', { name: TOPIC }).click();
  const alert = helpDialog(page).getByRole('alert');
  await expect(alert).toHaveText("No s'ha pogut carregar el tema d'ajuda.");
  await helpDialog(page).getByRole('button', { name: 'Reintentar' }).click();

  await expect(helpDialog(page).getByRole('heading', { level: 3, name: TOPIC })).toBeFocused();
  await expect(alert).toHaveCount(0);
  expect(topic.hits).toHaveLength(1);
});
