import { expect, test } from './support/auth.fixture';
import AxeBuilder from '@axe-core/playwright';
import type { Page } from '@playwright/test';
import { expectScreenHelp, helpDialog, openApp, uiText } from './help-screenshots/capture';

async function openConfiguredApp(page: Page, baseURL: string) {
  const locale = await openApp(page);
  // The local backend returns from SSO to port 4200. Re-enter the configured server
  // once authenticated so a Spanish run cannot silently exercise the Catalan app.
  if (new URL(page.url()).origin !== new URL(baseURL).origin) return openApp(page);
  return locale;
}

test('page help follows the active route and modal help keeps its own topic', async ({ page, baseURL }) => {
  test.setTimeout(120_000);
  const locale = await openConfiguredApp(page, baseURL!);
  const pageHelp = () => page.locator('.section-container__header').getByRole('button', { name: uiText(locale, 'Obrir ajuda'), exact: true });
  await pageHelp().click();
  await expect(helpDialog(page).getByRole('heading', { level: 3, name: locale === 'ca' ? "Llista d'aplicacions" : 'Lista de aplicaciones' })).toBeVisible();
  await page.keyboard.press('Escape');
  await expect(pageHelp()).toBeFocused();
  for (const width of [320, 768, 1440]) {
    await page.setViewportSize({ width, height: 900 });
    const bounds = (await pageHelp().boundingBox())!;
    expect(bounds.x).toBeGreaterThanOrEqual(0);
    expect(bounds.x + bounds.width).toBeLessThanOrEqual(width);
    expect(bounds.width).toBe(40);
    const padding = await page.locator('.section-container__body').evaluate(el => {
      const style = getComputedStyle(el);
      return [style.paddingTop, style.paddingRight, style.paddingBottom, style.paddingLeft];
    });
    expect(padding).toEqual(['24px', '24px', '24px', '24px']);
  }
  const headerAudit = await new AxeBuilder({ page }).include('.section-container__header')
    .withTags(['wcag2a', 'wcag2aa', 'wcag21aa', 'wcag22aa']).analyze();
  expect(headerAudit.violations).toEqual([]);
  await page.goto(`/${uiText(locale, 'manteniments')}/general#categories`);
  await expect(page.locator('.app-initial-loader')).toBeHidden({ timeout: 30_000 });
  await pageHelp().focus();
  await expectScreenHelp(page, locale, 'maintenances/general.md');
  await expect(pageHelp()).toBeFocused();
  await page.getByRole('button', { name: uiText(locale, 'Afegeix una categoria'), exact: true }).click();
  const form = page.getByRole('dialog').filter({ hasNot: page.locator('#help-modal-search') });
  const modalHelp = form.getByRole('button', { name: uiText(locale, 'Obrir ajuda'), exact: true });
  await modalHelp.click();
  await expect(helpDialog(page).getByRole('heading', { level: 3, name: uiText(locale, 'Categories'), exact: true })).toBeVisible();
  await page.keyboard.press('Escape');
  await expect(modalHelp).toBeFocused();
});

test('help navigation and document scroll independently with a fixed search field', async ({ page, baseURL }) => {
  test.setTimeout(120_000);
  const locale = await openConfiguredApp(page, baseURL!);
  await page.keyboard.press('Control+k');
  const help = helpDialog(page);
  const nav = help.locator('.help-navigation');
  const article = help.locator('.help-document');
  await expect(help.locator('markdown h3')).toBeVisible();
  // Include all groups so the indexed locators stay stable when expansion changes.
  // Ancestors precede descendants in the DOM, including groups in hidden lists.
  for (const group of await nav.locator('button[aria-expanded]').all()) {
    if (await group.getAttribute('aria-expanded') === 'false') await group.click();
    await expect(group).toHaveAttribute('aria-expanded', 'true');
  }
  await nav.evaluate(element => { element.scrollTop = 0; });
  // Hover waits for the opening animation to finish before measuring the layout.
  await help.locator('#help-modal-search').hover();
  const searchTop = (await help.locator('#help-modal-search').boundingBox())!.y;
  await expect.poll(() => article.evaluate(el => el.scrollHeight - el.clientHeight)).toBeGreaterThan(100);
  await article.hover();
  await page.mouse.wheel(0, 500);
  await expect.poll(() => article.evaluate(el => el.scrollTop)).toBeGreaterThan(100);
  expect(await nav.evaluate(el => el.scrollTop)).toBe(0);
  const articleTop = await article.evaluate(el => el.scrollTop);
  await nav.hover();
  await page.mouse.wheel(0, 300);
  await expect.poll(() => nav.evaluate(el => el.scrollTop)).toBeGreaterThan(0);
  expect(await article.evaluate(el => el.scrollTop)).toBe(articleTop);
  expect((await help.locator('#help-modal-search').boundingBox())!.y).toBe(searchTop);
  await page.keyboard.press('Escape');
  await expectScreenHelp(page, locale, 'applications/list.md');
  await page.setViewportSize({ width: 320, height: 800 });
  await page.locator('.section-container__header').getByRole('button', { name: uiText(locale, 'Obrir ajuda'), exact: true }).click();
  await expect(nav).toBeVisible();
  await expect(article).toBeVisible();
  for (const region of [nav, article]) {
    expect(await region.evaluate(el => el.scrollWidth - el.clientWidth)).toBeLessThanOrEqual(1);
    expect((await region.boundingBox())!.height).toBeGreaterThan(60);
  }
});
