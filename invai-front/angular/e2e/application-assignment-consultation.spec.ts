import { expect, test, type Page } from '@playwright/test';

test.use({ storageState: { cookies: [], origins: [] } });

const person = {
  id: 1, firstName: 'Maria', lastName: 'Tur', email: 'maria@example.test',
  personalCaib: false, company: { id: 2, name: 'Example company', deletedAt: null }, deletedAt: null,
};
const responsibility = { id: 1, name: 'Seguretat', nameEs: 'Seguridad', requiresPersonalCaib: false };
const application = {
  id: 1, code: 'TEST', prefix: 'TST', name: 'Consultation test', status: 'ACTIVE',
  appResponsibleAuthorizedId: 91,
};
const observation = 'Observacions detallades\n' + 'Text llarg sense truncar. '.repeat(15);
const responsible = {
  id: 3, appResponsibleAuthorizedId: 91, person, responsibleType: responsibility,
  jobTitle: 'Cap de servei', observation, deletedAt: null,
};
const authorized = {
  id: 4, appResponsibleAuthorizedId: 91, person,
  authorizationTypes: [{ id: 1, name: 'Signatura', nameEs: 'Firma', deletedAt: null }],
  observation, deletedAt: null,
};
const pageOf = (content: unknown[]) => ({ content, totalElements: content.length, totalPages: 1, last: true, number: 0 });

async function mockApi(page: Page, inactive = false, failedCatalogs = false) {
  const requests: string[] = [];
  await page.route('**/invaiback/**', async (route) => {
    const path = new URL(route.request().url()).pathname;
    requests.push(path);
    if (failedCatalogs && /\/(company|person|authorization-type)$/.test(path)) {
      await route.fulfill({ status: 500, json: { message: 'Unavailable test catalog' } });
      return;
    }
    let json: unknown = pageOf([]);
    if (path.endsWith('/auth/me')) json = { authenticated: true, username: 'consultation-test' };
    else if (path.endsWith('/application/1')) json = { ...application, status: inactive ? 'INACTIVE' : 'ACTIVE' };
    else if (path.endsWith('/application/responsible/91')) json = pageOf([responsible]);
    else if (path.endsWith('/application/authorized/91')) json = pageOf([authorized]);
    else if (path.endsWith('/responsible-type')) json = [responsibility, { ...responsibility, id: 2, name: 'Sistemes' }];
    await route.fulfill({ json });
  });
  await page.goto('/aplicacions/1/responsible');
  await expect(page.locator('app-application-responsibles-table tbody tr')).toHaveCount(2);
  await expect(page.locator('app-application-authorized-table tbody tr')).toHaveCount(1);
  return requests;
}

test('opens both details via double click, Enter and menu, restoring focus without API calls', async ({ page }) => {
  const requests = await mockApi(page);
  const before = requests.length;
  for (const kind of ['responsibles', 'authorized']) {
    const table = page.locator(`app-application-${kind}-table`);
    const row = table.locator('tbody .invai-table-consultable-row').first();
    const dialog = page.getByRole('dialog');
    await row.dblclick();
    const email = dialog.getByRole('textbox', { name: 'E-mail', exact: true });
    await expect(email).toHaveValue(person.email);
    await expect(email).toHaveAttribute('readonly');
    await expect(email).toBeEnabled();
    await email.focus();
    await email.press('Home');
    await email.press('x');
    await expect(email).toHaveValue(person.email);
    await expect(dialog.getByRole('textbox', { name: 'Empresa', exact: true })).toHaveValue(person.company.name);
    await expect(dialog.getByRole('textbox', { name: 'Observacions', exact: true })).toHaveValue(observation);
    await expect(dialog.locator('input:not([readonly]), textarea:not([readonly]), select')).toHaveCount(0);
    await expect(dialog.getByRole('button')).toHaveCount(2);
    if (kind === 'responsibles') {
      const positions = await dialog.locator('input[id$="-responsibility"], input[id$="-personal-caib"], input[id$="-job-title"]').evaluateAll(
        (inputs) => inputs.map((input) => ({ x: input.getBoundingClientRect().x, y: input.getBoundingClientRect().y })),
      );
      expect(positions).toHaveLength(3);
      expect(new Set(positions.map((position) => position.y)).size).toBe(1);
      expect(positions[0].x).toBeLessThan(positions[1].x);
      expect(positions[1].x).toBeLessThan(positions[2].x);
    }
    await expect.poll(() => dialog.evaluate((element) => element.contains(document.activeElement))).toBe(true);
    await page.keyboard.press('Escape');
    await expect(dialog).not.toBeVisible();
    await expect(row).toBeFocused();

    await row.press('Enter');
    await expect(dialog).toBeVisible();
    await dialog.getByRole('button', { name: /^Accepta la consulta/ }).click();
    await expect(row).toBeFocused();

    const trigger = row.getByRole('button');
    await trigger.click();
    await expect(page.locator('.application-responsible-row-menu').getByRole('menuitem')).toHaveCount(1);
    await page.getByRole('menuitem', { name: 'Consulta', exact: true }).click();
    await expect(dialog).toBeVisible();
    await dialog.getByRole('button', { name: "Tanca la consulta de l'assignació" }).click();
    await expect(trigger).toBeFocused();
  }
  const vacant = page.locator('app-application-responsibles-table tbody tr').last();
  await vacant.dblclick();
  await expect(page.getByRole('dialog')).not.toBeVisible();
  await expect(vacant.getByRole('button')).toHaveCount(0);
  expect(requests).toHaveLength(before);

  await page.getByRole('button', { name: "Editar Responsables de l'aplicació", exact: true }).click();
  const row = page.locator('app-application-authorized-table tbody tr');
  await row.getByRole('button').click();
  await expect(page.locator('.application-responsible-row-menu').getByRole('menuitem')).toHaveCount(3);
  await page.getByRole('menuitem', { name: 'Consulta', exact: true }).click();
  await expect.poll(() => page.getByRole('dialog').evaluate((element) => element.contains(document.activeElement))).toBe(true);
  await page.keyboard.press('Escape');
  await expect(page.getByRole('button', { name: "Desar els canvis de Responsables de l'aplicació" })).toBeVisible();
});

test('consults inactive applications despite failed edit catalogs', async ({ page }) => {
  await mockApi(page, true, true);
  const trigger = page.locator('app-application-authorized-table tbody button');
  await trigger.click();
  await expect(page.locator('.application-responsible-row-menu').getByRole('menuitem')).toHaveCount(1);
  await page.getByRole('menuitem', { name: 'Consulta', exact: true }).click();
  await expect(page.getByRole('dialog').getByRole('textbox', { name: 'Autoritzacions', exact: true })).toHaveValue('Signatura');
  await expect(page.getByRole('dialog').getByRole('textbox', { name: 'E-mail', exact: true })).toHaveValue(person.email);
});

test('reflows the detail at 320px and with 200 percent text', async ({ page }, testInfo) => {
  await page.setViewportSize({ width: 320, height: 720 });
  await mockApi(page);
  await page.locator('app-application-responsibles-table tbody .invai-table-consultable-row').dblclick();
  const dialog = page.getByRole('dialog');
  await expect(dialog).toBeVisible();
  expect(await dialog.evaluate((element) => element.scrollWidth <= element.clientWidth)).toBe(true);
  await expect.poll(() => dialog.evaluate((element) => element.contains(document.activeElement))).toBe(true);
  await expect(dialog).not.toHaveClass(/p-dialog-enter-active/);
  await page.screenshot({ path: testInfo.outputPath('consultation-mobile.png') });
  await page.keyboard.press('Escape');
  await expect(dialog).not.toBeVisible();
  await page.setViewportSize({ width: 1280, height: 900 });
  await page.evaluate(() => { document.documentElement.style.fontSize = '200%'; });
  await page.locator('app-application-authorized-table tbody .invai-table-consultable-row').dblclick();
  await expect(dialog.getByRole('textbox', { name: 'Observacions', exact: true })).toHaveValue(observation);
  expect(await dialog.evaluate((element) => element.scrollWidth <= element.clientWidth)).toBe(true);
  await expect(dialog).not.toHaveClass(/p-dialog-enter-active/);
  await page.screenshot({ path: testInfo.outputPath('consultation-enlarged-text.png') });
  await dialog.getByRole('button', { name: /^Accepta la consulta/ }).click();
  await expect(dialog).not.toBeVisible();
});
