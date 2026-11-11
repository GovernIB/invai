import { version } from '../package.json';
import { expect, test } from './support/auth.fixture';
import type { Page } from '@playwright/test';

async function openHome(page: Page) {
  await page.goto('/');
}

const statementTrigger = (page: Page) =>
  page.getByRole('contentinfo').getByRole('button', { name: "Declaració d'accessibilitat" });

const statementDialog = (page: Page) =>
  page.getByRole('dialog', { name: "Declaració d'accessibilitat" });

test('shows the copyright, the version and the statement action in the page footer', async ({
  page,
}) => {
  await openHome(page);

  const footer = page.getByRole('contentinfo');
  await expect(footer).toContainText('© Govern Illes Balears');
  await expect(footer).toContainText(`Versió ${version}`);
  await expect(statementTrigger(page)).toHaveAttribute('aria-haspopup', 'dialog');
});

test('opens the statement from the keyboard and returns focus when it is dismissed', async ({
  page,
}) => {
  await openHome(page);

  await statementTrigger(page).focus();
  await page.keyboard.press('Enter');

  const dialog = statementDialog(page);
  await expect(dialog).toBeVisible();
  await expect(dialog.getByRole('heading', { level: 2 })).toHaveText("Declaració d'accessibilitat");
  const notice = dialog.locator('.accessibility-statement__start');
  await expect(notice).toContainText("La primera auditoria d'accessibilitat");
  await expect(notice).toBeFocused();

  await page.keyboard.press('Tab');
  await expect(
    dialog.getByRole('button', { name: "Tanca la declaració d'accessibilitat" }),
  ).toBeFocused();

  await page.keyboard.press('Escape');
  await expect(dialog).toBeHidden();
  await expect(statementTrigger(page)).toBeFocused();
});

test('closes the statement from its labelled close button', async ({ page }) => {
  await openHome(page);

  await statementTrigger(page).click();
  const dialog = statementDialog(page);
  await expect(dialog).toBeVisible();

  await dialog.getByRole('button', { name: "Tanca la declaració d'accessibilitat" }).click();

  await expect(dialog).toBeHidden();
  await expect(statementTrigger(page)).toBeFocused();
});
