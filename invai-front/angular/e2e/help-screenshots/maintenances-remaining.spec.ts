import { expect, test } from '../support/auth.fixture';
import { apiUrl, expectApiJsonResponse } from '../support/http';
import { openApp, saveHelpImage, expectScreenHelp, helpDialog, topicPath, uiText } from './capture';

const groups = [
  ['systems', 'sistemes', ['servers', 'databases', 'physical-servers', 'database-servers', 'database-vendors', 'environments']],
  ['development', 'desenvolupament', ['provider-roles', 'technologies', 'layers']],
  ['accessibility', 'accessibilitat', ['classification-segments', 'compliance-situations']],
  ['security', 'seguretat', ['web-contexts', 'identity-providers', 'personal-data-processing', 'security-measure-types', 'ens-requirements']],
  ['integrations', 'integracions', ['external-systems']],
  ['documentation', 'documentacio', ['document-categories']],
] as const;

test.use({ actionTimeout: 15_000 });

const panelTitles: Record<string, string> = {
  servers: "Servidors d'aplicacions", databases: 'Bases de dades',
  'physical-servers': 'Servidors físics', 'database-servers': 'Servidors de bases de dades',
  'database-vendors': 'Proveïdors de bases de dades', environments: 'Entorns',
  'provider-roles': 'Rols de proveïdor', technologies: 'Tecnologies', layers: 'Capes',
  'classification-segments': 'Segments de classificació', 'compliance-situations': 'Situacions de compliment',
  'web-contexts': 'Contexts web', 'identity-providers': "Proveïdors d'identitat",
  'personal-data-processing': 'Tractaments de dades personals', 'security-measure-types': 'Tipus de mesura de seguretat',
  'ens-requirements': 'Requisits ENS', 'external-systems': 'Sistemes externs', 'document-categories': 'Categories documentals',
};

for (const capture of [true, false]) {
  for (const [slug, route, panels] of groups) {
    test(`${capture ? 'captures' : 'validates'} maintenance ${slug}`, async ({ page }) => {
      test.setTimeout(240_000);
      const locale = await openApp(page);
      const pathname = `/${uiText(locale, 'manteniments')}/${uiText(locale, route)}`;
      await page.goto(pathname);
      await expect(page.locator('.app-initial-loader')).toBeHidden({ timeout: 30_000 });
      await expect(page.locator('p-accordion')).toBeVisible();
      if (capture) await saveHelpImage(page, page.locator('p-accordion'), locale, `maintenances/imgs/${slug}.webp`);
      else await expectScreenHelp(page, locale, `maintenances/${slug}.md`);
      for (const panel of panels) {
        await page.goto(`${pathname}#${panel}`);
        const title = uiText(locale, panelTitles[panel]).replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
        const header = page.getByRole('button', { name: new RegExp(`^${title} `) });
        await expect(header).toHaveAttribute('aria-expanded', 'true');
        const activePanel = page.locator('p-accordion-panel').filter({ has: header });
        await activePanel.getByRole('button', { name: /^(Afegir|Afegeix|Añadir|Añade)[ :]/ }).click();
        const form = page.getByRole('dialog').filter({ hasNot: page.locator('#help-modal-search') });
        await expect(form).toBeVisible();
        const helpButton = form.getByRole('button', { name: uiText(locale, 'Obrir ajuda'), exact: true });
        await expect(helpButton).toBeVisible();
        if (capture) await saveHelpImage(page, form, locale, `maintenances/imgs/${slug}-${panel}.webp`);
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
    });
  }
  test(`${capture ? 'captures' : 'validates'} document category detail`, async ({ page }) => {
    test.setTimeout(120_000);
    const locale = await openApp(page);
    const api = '/invaiback/documentation/category';
    const category = await expectApiJsonResponse<{ id: number }>(await page.request.post(apiUrl(page, api), {
      data: { name: 'Manuals (exemple)', nameEs: 'Manuales (ejemplo)', parentId: null },
    }), 'create disposable help category');
    try {
      await page.goto(`/${uiText(locale, 'manteniments')}/${uiText(locale, 'documentacio')}/${uiText(locale, 'categories-documentals')}/${category.id}`);
      await expect(page.locator('.app-initial-loader')).toBeHidden({ timeout: 30_000 });
      const detail = page.locator('form.document-category-detail');
      await expect(detail).toBeVisible();
      if (capture) await saveHelpImage(page, detail, locale, 'maintenances/imgs/document-category.webp');
      else {
        await expectScreenHelp(page, locale, 'maintenances/document-category.md');
        await page.getByRole('button', { name: /^(Afegeix una subcategoria|Añade una subcategoría|Añadir una subcategoría)/ }).click();
        const form = page.getByRole('dialog').filter({ hasNot: page.locator('#help-modal-search') });
        await form.getByRole('button', { name: uiText(locale, 'Obrir ajuda'), exact: true }).focus();
        await expectScreenHelp(page, locale, 'maintenances/document-category.md');
        await form.locator('.invai-dialog__close button').click();
      }
    } finally {
      const response = await page.request.delete(apiUrl(page, `${api}/${category.id}`));
      expect(response.ok(), 'remove disposable category').toBeTruthy();
    }
  });
}
