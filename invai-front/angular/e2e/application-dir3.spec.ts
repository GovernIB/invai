import type { Page, TestInfo } from '@playwright/test';

import {
  assignResponsible,
  DIR3_PERSON,
  fullName,
  NO_DIR3_PERSON,
  OTHER_UNIT,
  responsibleTypes,
  setApplicationUnit,
  type SoffidPerson,
} from './support/application-responsibles';
import { type DisposableApplication, expect, test } from './support/application.fixture';
import { failApiRequest } from './support/http';
import { selectOption } from './support/ui';

const MANUAL_VALIDATE = /^\/invaiback\/application\/dir3-validation\/manual-validate\/\d+$/;

interface Dir3Counts {
  checks: number;
  posts: number;
  manual: number;
}

// The application's unit is not the DIR3 of any of the people, so the CAIB responsible of the
// test is pending: the person is either in another unit or has no DIR3 in Soffid. Nobody is
// authorized, so the responsible is the only pending DIR3 of the application.
async function openWithPendingDir3(
  page: Page,
  application: DisposableApplication,
  person: SoffidPerson,
  { failManualOnce = false } = {},
): Promise<Dir3Counts> {
  await setApplicationUnit(page, application, OTHER_UNIT);
  const types = await responsibleTypes(page);
  // The responsibility reserved to CAIB staff stays vacant for the tests that add one.
  const openType = types.find((type) => !type.requiresPersonalCaib)!;
  await assignResponsible(page, application, person, openType);

  const counts: Dir3Counts = { checks: 0, posts: 0, manual: 0 };
  if (failManualOnce) {
    await failApiRequest(page, 'PUT', MANUAL_VALIDATE, {
      status: 500,
      json: { message: 'Retry test' },
    });
  }
  page.on('request', (request) => {
    const url = new URL(request.url());
    if (url.pathname === '/invaiback/person/dir3-check') {
      counts.checks++;
      expect(url.searchParams.get('admUnitCode')).toBe(OTHER_UNIT);
    } else if (
      url.pathname === '/invaiback/application/responsible' &&
      request.method() === 'POST'
    ) {
      counts.posts++;
      expect(request.postDataJSON().dir3Status).toBe(false);
      expect(request.postDataJSON().personId).toBeNull();
    } else if (MANUAL_VALIDATE.test(url.pathname)) {
      counts.manual++;
    }
  });
  await page.goto(`/aplicacions/${application.id}/responsible`);
  await expect(page.locator('app-application-responsibles-table tbody tr')).toHaveCount(
    types.length,
  );
  return counts;
}

async function addResponsible(page: Page, person: SoffidPerson) {
  await page
    .getByRole('button', { name: "Editar Responsables de l'aplicació", exact: true })
    .click();
  await page.getByRole('button', { name: 'Afegeix un responsable', exact: true }).click();
  const dialog = page.getByRole('dialog');
  const caibOnly = (await responsibleTypes(page)).find((type) => type.requiresPersonalCaib)!;
  // A responsibility reserved to CAIB staff turns the Soffid search on.
  await selectOption(page, 'application-responsible-dialog-type', caibOnly.name);
  await dialog.locator('#application-responsible-dialog-soffid-person').fill(person.query);
  await page.getByRole('option', { name: new RegExp(fullName(person)) }).click();
  return dialog;
}

const CASES: Array<{ title: string; person: SoffidPerson; message: string }> = [
  {
    title: 'different',
    person: DIR3_PERSON,
    message: "El DIR3 de la persona no coincideix amb el de l'aplicació.",
  },
  {
    title: 'missing',
    person: NO_DIR3_PERSON,
    message: "No s'ha trobat un DIR3 associat a aquesta persona a Soffid.",
  },
];

for (const { title, person, message } of CASES) {
  test(`validates ${title} DIR3 from the edit menu and restores focus`, async ({
    page,
    disposableApplication,
  }, testInfo: TestInfo) => {
    const counts = await openWithPendingDir3(page, disposableApplication, person);
    const table = page.locator('app-application-responsibles-table');
    await expect(table.getByRole('img', { name: 'DIR3 no validat' })).toBeVisible();
    const tab = page.locator('.application-detail-tab').filter({ hasText: /^Responsables/ });
    await expect(tab.locator('.pi-exclamation-circle.text-red-500')).toBeVisible();
    await expect(tab).toContainText('DIR3 pendent de validar');
    await expect(
      page
        .locator('app-application-authorized-table')
        .getByRole('img', { name: 'DIR3 no validat' }),
    ).toHaveCount(0);
    const trigger = table.locator('tbody button').first();
    await trigger.click();
    await expect(page.getByRole('menuitem', { name: 'Confirmar discrepància DIR3' })).toHaveCount(
      0,
    );
    await page.keyboard.press('Escape');
    await page
      .getByRole('button', { name: "Editar Responsables de l'aplicació", exact: true })
      .click();
    await trigger.focus();
    await trigger.press('Enter');
    await page.getByRole('menuitem', { name: 'Confirmar discrepància DIR3' }).click();
    const dialog = page.getByRole('dialog');
    const consent = dialog.getByRole('checkbox', {
      name: "Confirm la validació manual d'aquesta discrepància",
    });
    await expect(consent).not.toBeChecked();
    await expect(dialog).toContainText(message);
    await page.screenshot({
      path: testInfo.outputPath('dir3-manual-compact.png'),
      animations: 'disabled',
    });
    await expect(dialog.getByRole('button', { name: 'Confirmar', exact: true })).toBeDisabled();
    await expect(dialog.getByRole('combobox')).toHaveCount(0);
    await expect(dialog.getByRole('textbox')).toHaveCount(0);
    await expect(dialog.locator('input[readonly], textarea[readonly]')).toHaveCount(0);
    // Where the focus goes once the real DIR3 check answers is the test.fixme below.
    await consent.focus();
    await consent.press('Space');
    await dialog.getByRole('button', { name: 'Confirmar', exact: true }).click();
    await expect(dialog.getByRole('textbox', { name: 'Motiu' })).toHaveAttribute(
      'aria-invalid',
      'true',
    );
    await dialog.getByRole('textbox', { name: 'Motiu' }).fill('Responsabilitat transversal');
    await dialog.getByRole('button', { name: 'Confirmar', exact: true }).click();
    await expect(dialog).not.toBeVisible();
    await expect(trigger).toBeFocused();
    await expect(table.getByRole('img', { name: 'DIR3 validat manualment' })).toBeVisible();
    await expect(table.locator('.pi-check-circle')).toBeVisible();
    // The tab keeps its red mark for the vacant responsibilities; only the DIR3 notice goes.
    await expect(tab).not.toContainText('DIR3 pendent de validar');
    await expect(table.getByRole('img', { name: 'DIR3 no validat' })).toHaveCount(0);
    expect(counts).toEqual({ checks: 1, posts: 0, manual: 1 });
  });
}

// App bug: the back-end answers the POST of a responsible with the person's fields empty. Once the
// manual validation fails, the dialog claims the e-mail is missing instead of explaining the
// failure, and keeps Confirmar disabled, so the manual validation cannot be retried.
test.fixme('retries only manual validation after creating, with a usable mobile dialog', async ({
  page,
  disposableApplication,
}, testInfo) => {
  await page.setViewportSize({ width: 320, height: 720 });
  const counts = await openWithPendingDir3(page, disposableApplication, NO_DIR3_PERSON, {
    failManualOnce: true,
  });
  const dialog = await addResponsible(page, DIR3_PERSON);
  await dialog
    .getByRole('checkbox', { name: "Confirm la validació manual d'aquesta discrepància" })
    .check();
  await dialog.getByRole('textbox', { name: 'Motiu' }).fill('Motiu justificat');
  await dialog.getByRole('button', { name: 'Afegeix el responsable', exact: true }).click();
  await expect(dialog).toContainText('El responsable ja');
  await expect(dialog).toContainText("No s'ha pogut validar manualment");
  await expect(dialog.getByRole('textbox', { name: 'Motiu' })).toHaveValue('Motiu justificat');
  await expect(dialog.getByRole('button', { name: 'Confirmar', exact: true })).toBeEnabled();
  expect(await dialog.evaluate((element) => element.scrollWidth <= element.clientWidth)).toBe(true);
  await page.screenshot({ path: testInfo.outputPath('dir3-mobile-partial.png') });
  await page.setViewportSize({ width: 1280, height: 900 });
  await page.evaluate(() => {
    document.documentElement.style.fontSize = '200%';
  });
  expect(await dialog.evaluate((element) => element.scrollWidth <= element.clientWidth)).toBe(true);
  await dialog.getByRole('button', { name: 'Confirmar', exact: true }).click();
  await expect(dialog).not.toBeVisible();
  expect(counts).toEqual({ checks: 1, posts: 1, manual: 2 });
});

test('explains missing Soffid DIR3 and permits adding without manual consent', async ({
  page,
  disposableApplication,
}) => {
  const counts = await openWithPendingDir3(page, disposableApplication, DIR3_PERSON);
  const dialog = await addResponsible(page, NO_DIR3_PERSON);
  await expect(dialog).toContainText("No s'ha trobat un DIR3 associat a aquesta persona a Soffid.");
  await expect(dialog.getByRole('checkbox')).not.toBeChecked();
  await dialog.getByRole('button', { name: 'Afegeix el responsable', exact: true }).click();
  await expect(dialog).not.toBeVisible();
  await expect(
    page
      .locator('app-application-responsibles-table')
      .getByRole('img', { name: 'DIR3 no validat' }),
  ).toHaveCount(2);
  expect(counts).toEqual({ checks: 1, posts: 1, manual: 0 });
});

// App bug: with the real DIR3 check, which answers once the dialog is open, the focus stays on the
// row action instead of moving into the dialog (an instant answer used to hide it).
test.fixme('moves the focus into the DIR3 confirmation once the check answers', async ({
  page,
  disposableApplication,
}) => {
  await openWithPendingDir3(page, disposableApplication, DIR3_PERSON);
  await page
    .getByRole('button', { name: "Editar Responsables de l'aplicació", exact: true })
    .click();
  const trigger = page.locator('app-application-responsibles-table tbody button').first();
  await trigger.focus();
  await trigger.press('Enter');
  await page.getByRole('menuitem', { name: 'Confirmar discrepància DIR3' }).click();
  const dialog = page.getByRole('dialog');
  await expect(dialog).toContainText("El DIR3 de la persona no coincideix amb el de l'aplicació.");
  await expect
    .poll(() => dialog.evaluate((element) => element.contains(document.activeElement)))
    .toBe(true);
});
