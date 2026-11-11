import type { Page, TestInfo } from '@playwright/test';

import { type DisposableApplication, expect, test } from './support/application.fixture';
import { findApiCatalogItem } from './support/catalog';
import type { CleanupRegistry } from './support/cleanup-registry';
import { uniqueToken } from './support/data';
import { apiUrl, expectApiJsonResponse, failApiRequest } from './support/http';
import { ACTIVE_STATUS_ID, registerSoftDeleteCleanup } from './support/systems';

// The back-end trims what it saves, so the text does not end with a space.
const observation = ('Observacions detallades\n' + 'Text llarg sense truncar. '.repeat(15)).trim();

interface NamedRecord {
  id: number;
  name: string;
  deletedAt?: string | null;
}

interface Assignments {
  person: { id: number; lastName: string; email: string; deletedAt: string | null };
  company: NamedRecord;
  authorizationType: NamedRecord;
  responsibleTypes: number;
}

// Assigns an external person (not CAIB staff, so no DIR3 check is involved) as a responsible of
// one type and as an authorized person of the application.
async function assignExternalPerson(
  page: Page,
  cleanup: CleanupRegistry,
  application: DisposableApplication,
  testInfo: TestInfo,
): Promise<Assignments> {
  const token = uniqueToken(testInfo);
  const company = await findApiCatalogItem<NamedRecord>({
    page,
    pathname: '/invaiback/company',
    params: { statusId: ACTIVE_STATUS_ID },
    matches: (item) => !item.deletedAt && !item.name.startsWith('E2E'),
  });
  const authorizationType = await findApiCatalogItem<NamedRecord>({
    page,
    pathname: '/invaiback/authorization-type',
    params: { statusId: ACTIVE_STATUS_ID },
    matches: (item) => !item.deletedAt && !item.name.startsWith('E2E'),
  });
  if (!company || !authorizationType) {
    throw new Error('An active company and authorization type are required');
  }
  const responsibleTypes = await expectApiJsonResponse<
    Array<{ id: number; requiresPersonalCaib: boolean }>
  >(await page.request.get(apiUrl(page, '/invaiback/responsible-type')), 'GET responsible-type');
  const responsibleType = responsibleTypes.find((type) => !type.requiresPersonalCaib);
  if (!responsibleType) throw new Error('A responsible type open to external staff is required');

  const person = await expectApiJsonResponse<Assignments['person']>(
    await page.request.post(apiUrl(page, '/invaiback/person'), {
      data: {
        companyId: company.id,
        firstName: 'Maria',
        lastName: `Tur ${token}`,
        email: `maria.${token.toLowerCase()}@example.test`,
        personalCaib: false,
      },
    }),
    'POST /invaiback/person',
  );
  registerSoftDeleteCleanup<Assignments['person']>({
    page,
    cleanup,
    label: `person ${person.lastName}`,
    pathname: '/invaiback/person',
    target: { id: person.id, matches: (item) => item.id === person.id },
  });
  const anchor = application.appResponsibleAuthorizedId;
  await expectApiJsonResponse(
    await page.request.post(apiUrl(page, '/invaiback/application/responsible'), {
      data: {
        appResponsibleAuthorizedId: anchor,
        personId: person.id,
        responsibleTypeId: responsibleType.id,
        jobTitle: 'Cap de servei',
        observation,
        personalCaib: false,
      },
    }),
    'POST /invaiback/application/responsible',
  );
  await expectApiJsonResponse(
    await page.request.post(apiUrl(page, '/invaiback/application/authorized'), {
      data: {
        appResponsibleAuthorizedId: anchor,
        personId: person.id,
        authorizationTypeIds: [authorizationType.id],
        observation,
        personalCaib: false,
      },
    }),
    'POST /invaiback/application/authorized',
  );
  return { person, company, authorizationType, responsibleTypes: responsibleTypes.length };
}

async function openResponsibles(
  page: Page,
  application: DisposableApplication,
  assignments: Assignments,
): Promise<string[]> {
  const requests: string[] = [];
  page.on('request', (request) => {
    const path = new URL(request.url()).pathname;
    if (path.startsWith('/invaiback/')) requests.push(path);
  });
  await page.goto(`/aplicacions/${application.id}/responsible`);
  // One row per responsible type, assigned or vacant.
  await expect(page.locator('app-application-responsibles-table tbody tr')).toHaveCount(
    assignments.responsibleTypes,
  );
  await expect(page.locator('app-application-authorized-table tbody tr')).toHaveCount(1);
  return requests;
}

async function deactivate(page: Page, application: DisposableApplication): Promise<void> {
  const deactivated = await page.request.delete(
    apiUrl(page, `/invaiback/application/${application.id}`),
  );
  expect(deactivated.ok(), await deactivated.text()).toBeTruthy();
}

test('opens both details via double click, Enter and menu, restoring focus without API calls', async ({
  page,
  cleanupRegistry,
  disposableApplication,
}, testInfo) => {
  const assignments = await assignExternalPerson(
    page,
    cleanupRegistry,
    disposableApplication,
    testInfo,
  );
  const { person, company } = assignments;
  const requests = await openResponsibles(page, disposableApplication, assignments);
  const before = requests.length;
  for (const kind of ['responsibles', 'authorized']) {
    const table = page.locator(`app-application-${kind}-table`);
    const row = table
      .locator('tbody .invai-table-consultable-row')
      .filter({ hasText: person.lastName });
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
    await expect(dialog.getByRole('textbox', { name: 'Empresa', exact: true })).toHaveValue(
      company.name,
    );
    await expect(dialog.getByRole('textbox', { name: 'Observacions', exact: true })).toHaveValue(
      observation,
    );
    await expect(
      dialog.locator('input:not([readonly]), textarea:not([readonly]), select'),
    ).toHaveCount(0);
    await expect(dialog.getByRole('button')).toHaveCount(2);
    if (kind === 'responsibles') {
      const positions = await dialog
        .locator(
          'input[id$="-responsibility"], input[id$="-personal-caib"], input[id$="-job-title"]',
        )
        .evaluateAll((inputs) =>
          inputs.map((input) => ({
            x: input.getBoundingClientRect().x,
            y: input.getBoundingClientRect().y,
          })),
        );
      expect(positions).toHaveLength(3);
      expect(new Set(positions.map((position) => position.y)).size).toBe(1);
      expect(positions[0].x).toBeLessThan(positions[1].x);
      expect(positions[1].x).toBeLessThan(positions[2].x);
    }
    await expect
      .poll(() => dialog.evaluate((element) => element.contains(document.activeElement)))
      .toBe(true);
    await page.keyboard.press('Escape');
    await expect(dialog).not.toBeVisible();
    await expect(row).toBeFocused();

    await row.press('Enter');
    await expect(dialog).toBeVisible();
    await dialog.getByRole('button', { name: /^Accepta la consulta/ }).click();
    await expect(row).toBeFocused();

    const trigger = row.getByRole('button');
    await trigger.click();
    await expect(
      page.locator('.application-responsible-row-menu').getByRole('menuitem'),
    ).toHaveCount(1);
    await page.getByRole('menuitem', { name: 'Consulta', exact: true }).click();
    await expect(dialog).toBeVisible();
    await dialog.getByRole('button', { name: "Tanca la consulta de l'assignació" }).click();
    await expect(trigger).toBeFocused();
  }
  const vacant = page
    .locator('app-application-responsibles-table tbody tr')
    .filter({ hasNotText: person.lastName })
    .first();
  await vacant.dblclick();
  await expect(page.getByRole('dialog')).not.toBeVisible();
  await expect(vacant.getByRole('button')).toHaveCount(0);
  expect(requests).toHaveLength(before);

  await page
    .getByRole('button', { name: "Editar Responsables de l'aplicació", exact: true })
    .click();
  const row = page.locator('app-application-authorized-table tbody tr');
  await row.getByRole('button').click();
  await expect(page.locator('.application-responsible-row-menu').getByRole('menuitem')).toHaveCount(
    3,
  );
  await page.getByRole('menuitem', { name: 'Consulta', exact: true }).click();
  await expect
    .poll(() =>
      page.getByRole('dialog').evaluate((element) => element.contains(document.activeElement)),
    )
    .toBe(true);
  await page.keyboard.press('Escape');
  await expect(
    page.getByRole('button', { name: "Desar els canvis de Responsables de l'aplicació" }),
  ).toBeVisible();
});

test('consults inactive applications despite failed edit catalogs', async ({
  page,
  cleanupRegistry,
  disposableApplication,
}, testInfo) => {
  const assignments = await assignExternalPerson(
    page,
    cleanupRegistry,
    disposableApplication,
    testInfo,
  );
  await deactivate(page, disposableApplication);
  await failApiRequest(
    page,
    'GET',
    /^\/invaiback\/(company|person|authorization-type)$/,
    { status: 500, json: { message: 'Unavailable test catalog' } },
    { times: Number.POSITIVE_INFINITY },
  );
  await openResponsibles(page, disposableApplication, assignments);
  const trigger = page.locator('app-application-authorized-table tbody button');
  await trigger.click();
  await expect(page.locator('.application-responsible-row-menu').getByRole('menuitem')).toHaveCount(
    1,
  );
  await page.getByRole('menuitem', { name: 'Consulta', exact: true }).click();
  await expect(
    page.getByRole('dialog').getByRole('textbox', { name: 'Autoritzacions', exact: true }),
  ).toHaveValue(assignments.authorizationType.name);
  await expect(
    page.getByRole('dialog').getByRole('textbox', { name: 'E-mail', exact: true }),
  ).toHaveValue(assignments.person.email);
});

test('reflows the detail at 320px and with 200 percent text', async ({
  page,
  cleanupRegistry,
  disposableApplication,
}, testInfo) => {
  const assignments = await assignExternalPerson(
    page,
    cleanupRegistry,
    disposableApplication,
    testInfo,
  );
  await page.setViewportSize({ width: 320, height: 720 });
  await openResponsibles(page, disposableApplication, assignments);
  await page
    .locator('app-application-responsibles-table tbody .invai-table-consultable-row')
    .filter({ hasText: assignments.person.lastName })
    .dblclick();
  const dialog = page.getByRole('dialog');
  await expect(dialog).toBeVisible();
  expect(await dialog.evaluate((element) => element.scrollWidth <= element.clientWidth)).toBe(true);
  await expect
    .poll(() => dialog.evaluate((element) => element.contains(document.activeElement)))
    .toBe(true);
  await expect(dialog).not.toHaveClass(/p-dialog-enter-active/);
  await page.screenshot({ path: testInfo.outputPath('consultation-mobile.png') });
  await page.keyboard.press('Escape');
  await expect(dialog).not.toBeVisible();
  await page.setViewportSize({ width: 1280, height: 900 });
  await page.evaluate(() => {
    document.documentElement.style.fontSize = '200%';
  });
  await page
    .locator('app-application-authorized-table tbody .invai-table-consultable-row')
    .dblclick();
  await expect(dialog.getByRole('textbox', { name: 'Observacions', exact: true })).toHaveValue(
    observation,
  );
  expect(await dialog.evaluate((element) => element.scrollWidth <= element.clientWidth)).toBe(true);
  await expect(dialog).not.toHaveClass(/p-dialog-enter-active/);
  await page.screenshot({ path: testInfo.outputPath('consultation-enlarged-text.png') });
  await dialog.getByRole('button', { name: /^Accepta la consulta/ }).click();
  await expect(dialog).not.toBeVisible();
});
