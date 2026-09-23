import { expect, test, type Page } from '@playwright/test';

test.use({ storageState: { cookies: [], origins: [] } });

async function mockDir3(page: Page, failManualOnce = false, groupDir3: string | null = 'A2') {
  const person = {
    id: 12,
    firstName: 'Joan',
    lastName: 'Serra',
    email: 'joan@caib.es',
    personalCaib: true,
    company: null,
    deletedAt: null,
  };
  const candidate = {
    ...person,
    id: null,
    firstName: 'Aina',
    lastName: 'Ferrer',
    email: 'aina@caib.es',
  };
  const type = { id: 1, name: 'Seguretat', nameEs: 'Seguridad', requiresPersonalCaib: true };
  const vacantType = { ...type, id: 2, name: 'Sistemes', nameEs: 'Sistemas' };
  const validation = {
    id: 501,
    dir3Status: 'NOT_VALIDATED',
    reason: null,
    manualValidatedAt: null,
    manualValidatedBy: null,
  };
  const responsibles = [
    {
      id: 21,
      appResponsibleAuthorizedId: 91,
      person,
      responsibleType: type,
      jobTitle: null,
      observation: null,
      deletedAt: null,
      dir3Validation: { ...validation },
    },
  ];
  const counts = { checks: 0, posts: 0, manual: 0 };
  const pageOf = (content: unknown[]) => ({
    content,
    totalElements: content.length,
    totalPages: 1,
    last: true,
    number: 0,
  });
  await page.route('**/invaiback/**', async (route) => {
    const request = route.request();
    const url = new URL(request.url());
    const path = url.pathname;
    let json: unknown = pageOf([]);
    if (path.endsWith('/auth/me')) json = { authenticated: true, username: 'dir3-test' };
    else if (path.endsWith('/application/1'))
      json = {
        id: 1,
        code: 'TEST',
        name: 'DIR3 test',
        status: 'ACTIVE',
        appResponsibleAuthorizedId: 91,
        admUnit: { code: 'A1', name: 'Unitat' },
      };
    else if (path.endsWith('/application/responsible/91')) json = pageOf(responsibles);
    else if (path.endsWith('/application/authorized/91'))
      json = pageOf([
        {
          id: 31,
          appResponsibleAuthorizedId: 91,
          person,
          authorizationTypes: [],
          deletedAt: null,
          dir3Validation: validation,
        },
      ]);
    else if (path.endsWith('/responsible-type')) json = [type, vacantType];
    else if (path.endsWith('/person/soffid-search')) json = pageOf([candidate]);
    else if (path.endsWith('/person/dir3-check')) {
      counts.checks++;
      expect([candidate.email, person.email]).toContain(url.searchParams.get('emailAddress'));
      expect(url.searchParams.get('admUnitCode')).toBe('A1');
      json = { personGroup: 'group', groupDir3, admUnitCode: 'A1', matches: false };
    } else if (path.endsWith('/application/responsible') && request.method() === 'POST') {
      counts.posts++;
      expect(request.postDataJSON().dir3Status).toBe(false);
      expect(request.postDataJSON().personId).toBeNull();
      const added = {
        ...responsibles[0],
        id: 22,
        person: { ...candidate, id: 13 },
        responsibleType: vacantType,
        dir3Validation: { ...validation, id: 502 },
      };
      responsibles.push(added);
      json = added;
    } else if (path.includes('/dir3-validation/manual-validate/')) {
      counts.manual++;
      if (failManualOnce && counts.manual === 1) {
        await route.fulfill({ status: 500, json: { message: 'Retry test' } });
        return;
      }
      const id = Number(path.split('/').at(-1));
      const assignment = responsibles.find((row) => row.dir3Validation.id === id)!;
      assignment.dir3Validation.dir3Status = 'MANUAL';
      json = { ...assignment.dir3Validation, reason: request.postDataJSON().reason };
    }
    await route.fulfill({ json });
  });
  await page.goto('/aplicacions/1/responsible');
  await expect(page.locator('app-application-responsibles-table tbody tr')).toHaveCount(2);
  return counts;
}

for (const groupDir3 of ['A2', null]) {
  test(`validates ${groupDir3 === null ? 'missing' : 'different'} DIR3 from the edit menu and restores focus`, async ({
    page,
  }, testInfo) => {
    const counts = await mockDir3(page, false, groupDir3);
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
    await expect(dialog).toContainText(
      groupDir3 === null
        ? "No s'ha trobat un DIR3 associat a aquesta persona a Soffid."
        : "El DIR3 de la persona no coincideix amb el de l'aplicació.",
    );
    await page.screenshot({
      path: testInfo.outputPath('dir3-manual-compact.png'),
      animations: 'disabled',
    });
    await expect(dialog.getByRole('button', { name: 'Confirmar', exact: true })).toBeDisabled();
    await expect(dialog.getByRole('combobox')).toHaveCount(0);
    await expect(dialog.getByRole('textbox')).toHaveCount(0);
    await expect(dialog.locator('input[readonly], textarea[readonly]')).toHaveCount(0);
    await expect
      .poll(() => dialog.evaluate((element) => element.contains(document.activeElement)))
      .toBe(true);
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
    await expect(tab.locator('.pi-exclamation-circle')).toHaveCount(0);
    await expect(tab).not.toContainText('DIR3 pendent de validar');
    await expect(table.getByRole('img', { name: 'DIR3 no validat' })).toHaveCount(0);
    expect(counts).toEqual({ checks: 1, posts: 0, manual: 1 });
  });
}

test('retries only manual validation after creating, with a usable mobile dialog', async ({
  page,
}, testInfo) => {
  await page.setViewportSize({ width: 320, height: 720 });
  const counts = await mockDir3(page, true);
  await page
    .getByRole('button', { name: "Editar Responsables de l'aplicació", exact: true })
    .click();
  await page.getByRole('button', { name: 'Afegeix un responsable', exact: true }).click();
  let dialog = page.getByRole('dialog');
  await dialog.locator('#application-responsible-dialog-soffid-person').fill('Aina');
  await page.getByRole('option', { name: /Aina Ferrer/ }).click();
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

test('explains missing Soffid DIR3 and permits adding without manual consent', async ({ page }) => {
  const counts = await mockDir3(page, false, null);
  await page
    .getByRole('button', { name: "Editar Responsables de l'aplicació", exact: true })
    .click();
  await page.getByRole('button', { name: 'Afegeix un responsable', exact: true }).click();
  const dialog = page.getByRole('dialog');
  await dialog.locator('#application-responsible-dialog-soffid-person').fill('Aina');
  await page.getByRole('option', { name: /Aina Ferrer/ }).click();
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
