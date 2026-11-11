import { expect, test } from '../support/auth.fixture';
import { CleanupRegistry } from '../support/cleanup-registry';
import { uniqueToken } from '../support/data';
import { openApp, saveHelpImage, uiText } from './capture';
import { createExampleApplication, findCatalogs } from './example-applications';

// Example applications for the captures; real ones never appear in the help.
const EXAMPLE_NAMES = [
  "Registre d'entrada (exemple)",
  'Portal de tràmits (exemple)',
  'Gestió de subvencions (exemple)',
];

test('the applications list, its filters, its row actions and the new application form', async ({
  page,
}, testInfo) => {
  test.setTimeout(120_000);
  const cleanup = new CleanupRegistry(testInfo);
  try {
    const locale = await openApp(page);
    const token = uniqueToken(testInfo, 5);
    const catalogs = await findCatalogs(page);

    for (const [index, name] of EXAMPLE_NAMES.entries()) {
      await createExampleApplication(page, cleanup, {
        name,
        code: `AJ${token}${index}`,
        catalogs,
      });
    }

    await page.reload();
    await expect(page.locator('.app-initial-loader')).toBeHidden({ timeout: 30_000 });
    const section = page.locator('app-section-container').first();
    const table = page.locator('app-applications-table');

    // The list, narrowed to the examples with the quick search.
    await section
      .getByRole('textbox', { name: uiText(locale, "Cerca ràpida d'aplicacions") })
      .fill(`AJ${token}`);
    for (const name of EXAMPLE_NAMES) {
      await expect(table.getByRole('cell', { name, exact: true })).toBeVisible();
    }
    await expect(table.getByRole('row')).toHaveCount(EXAMPLE_NAMES.length + 1);
    await saveHelpImage(page, section, locale, 'applications/imgs/list.webp');

    // The actions of a row.
    await table
      .getByRole('row')
      .filter({ hasText: EXAMPLE_NAMES[0] })
      .getByRole('button', { name: uiText(locale, 'Obrir les accions del registre') })
      .click();
    // The side navigation is a menu too, so the row menu is told apart by its first action.
    const rowMenu = page.getByRole('menu').filter({ hasText: uiText(locale, 'Consulta') });
    await expect(rowMenu).toBeVisible();
    await saveHelpImage(page, table, locale, 'applications/imgs/row-actions.webp');
    await page.keyboard.press('Escape');
    await expect(rowMenu).toBeHidden();

    // The filters.
    await section
      .getByRole('button', { name: uiText(locale, 'Mostra o amaga els filtres') })
      .click();
    const filters = page.locator('app-search-filters');
    await expect(filters).toBeVisible();
    await saveHelpImage(page, filters, locale, 'applications/imgs/filters.webp');

    // The new application form.
    await section.getByRole('button', { name: uiText(locale, 'Afegeix una aplicació') }).click();
    await expect(
      page.getByRole('heading', { name: uiText(locale, 'Afegir aplicació') }),
    ).toBeVisible();
    await saveHelpImage(
      page,
      page.locator('app-section-container').first(),
      locale,
      'applications/imgs/create.webp',
    );
  } finally {
    await cleanup.runAll();
  }
});
