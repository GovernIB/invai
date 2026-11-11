import { expect, test } from '../support/auth.fixture';
import { openApp, saveHelpImage, expectScreenHelp, helpDialog, topicPath, uiText } from './capture';

const panels = [
  ['general', 'categories', 'Afegeix una categoria'],
  ['general', 'system-types', "Afegeix un sistema d'informació"],
  ['general', 'fields', 'Afegeix un àmbit'],
  ['general', 'commissions', 'Afegeix una comissió informàtica'],
  ['responsables', 'people', 'Afegeix una persona'],
  ['responsables', 'authorizations', 'Afegeix una autorització'],
  ['responsables', 'companies', 'Afegeix una empresa'],
] as const;

test.use({ actionTimeout: 15_000 });

for (const capture of [true, false]) {
  test(`${capture ? 'captures' : 'validates'} General and Responsables help`, async ({ page }) => {
    test.setTimeout(240_000);
    const locale = await openApp(page);
    // The external directory may be unavailable locally. These captures only use empty forms;
    // dismiss its reported error without replacing any response or using real personal data.
    const directoryError = page.getByRole('dialog').filter({
      hasText: /(?:servei de cerca de personal|servicio de búsqueda de personal) de Soffid/,
    });
    await page.addLocatorHandler(directoryError, async () => {
      test.info().annotations.push({ type: 'environment', description: 'Soffid search unavailable; empty forms only.' });
      await directoryError.getByRole('button', { name: uiText(locale, "Tanca el missatge d'error"), exact: true }).last().click();
    });
    const go = async (tab: string, fragment = '') => {
      await page.goto(`/${uiText(locale, 'manteniments')}/${uiText(locale, tab)}${fragment ? '#' + fragment : ''}`);
      await expect(page.locator('.app-initial-loader')).toBeHidden({ timeout: 30_000 });
      await expect(page.locator('p-accordion')).toBeVisible();
    };
    for (const [tab, slug] of [['general', 'general'], ['responsables', 'responsibles']]) {
      await go(tab);
      if (capture) await saveHelpImage(page, page.locator('p-accordion'), locale, `maintenances/imgs/${slug}.webp`);
      else await expectScreenHelp(page, locale, `maintenances/${slug}.md`);
    }
    for (const [tab, slug, add] of panels) {
      await go(tab, slug);
      await page.getByRole('button', { name: uiText(locale, add), exact: true }).click();
      const form = page.getByRole('dialog').filter({ hasNot: page.locator('#help-modal-search') });
      await expect(form).toBeVisible();
      const helpButton = form.getByRole('button', { name: uiText(locale, 'Obrir ajuda'), exact: true });
      await expect(helpButton).toBeVisible();
      if (capture) await saveHelpImage(page, form, locale, `maintenances/imgs/${slug}.webp`);
      else {
        await helpButton.focus();
        await expectScreenHelp(page, locale, `maintenances/${slug}.md`);
        await expect(helpButton).toBeFocused();
        await helpButton.click();
        await expect(helpDialog(page).getByRole('heading', { level: 3, name: topicPath(locale, `maintenances/${slug}.md`).at(-1)!, exact: true })).toBeVisible();
        await helpDialog(page).getByRole('button', { name: uiText(locale, "Tanca el centre d'ajuda"), exact: true }).click();
        await expect(helpDialog(page)).toBeHidden();
      }
      await form.locator('.invai-dialog__close button').click();
      await expect(form).toBeHidden();
    }
    await go('responsables', 'role-transfer');
    const transfer = page.locator('app-role-transfer');
    await expect(transfer).toBeVisible();
    if (capture) await saveHelpImage(page, transfer, locale, 'maintenances/imgs/role-transfer.webp');
    else {
      await page.keyboard.press('Control+k');
      const help = helpDialog(page);
      await help.getByRole('link', { name: topicPath(locale, 'maintenances/role-transfer.md').at(-1)!, exact: true }).click();
      await expect(help.locator('markdown img')).toBeVisible();
      await expect.poll(() => help.locator('markdown img').evaluate(el => (el as HTMLImageElement).naturalWidth)).toBeGreaterThan(0);
      await page.keyboard.press('Escape');
    }
  });
}
