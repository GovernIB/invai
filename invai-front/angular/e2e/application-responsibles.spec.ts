import type { Locator, Page, TestInfo } from '@playwright/test';

import {
  activeCatalogItem,
  assignAuthorized,
  assignResponsible,
  type AssignmentRecord,
  authorizationTypes,
  createExternalPerson,
  DIR3_PERSON,
  DIR3_UNIT,
  fullName,
  type NamedRecord,
  OTHER_UNIT,
  type PersonRecord,
  responsibleTypes,
  type ResponsibleType,
  setApplicationUnit,
  validateManually,
} from './support/application-responsibles';
import { type DisposableApplication, expect, test } from './support/application.fixture';
import type { CleanupRegistry } from './support/cleanup-registry';
import { apiUrl, failApiRequest } from './support/http';

const MANUAL_REASON = 'Responsabilitat transversal';
const EDIT_SECTION = "Editar Responsables de l'aplicació";
const CHECK_DIR3 = "Validar el DIR3 de l'aplicació";
const CHECK_DIR3_PROGRESS = 'Validant el DIR3 dels responsables i autoritzats…';
const OBSERVATION_REQUIRED = 'Les observacions són obligatòries.';
const TEXT_TOO_LONG =
  'El text supera la mida màxima permesa. Redueix-lo (els accents i el format també ocupen espai).';
const REQUIRED = 'Aquest camp és obligatori.';
const DIR3_CHECK = /^\/invaiback\/application\/dir3\/check\/\d+$/;

type Kind = 'responsible' | 'authorized';

interface Write {
  method: string;
  path: string;
  body: unknown;
}

interface Seed {
  types: ResponsibleType[];
  security: ResponsibleType;
  systems: ResponsibleType;
  authorizations: NamedRecord[];
  company: NamedRecord;
  external: PersonRecord;
  caibResponsible: AssignmentRecord;
  externalResponsible: AssignmentRecord;
  authorized: AssignmentRecord;
}

interface SeedOptions {
  unit?: string | null;
  // How the DIR3 of the CAIB responsible ends up: validated by a match, validated by hand over a
  // mismatch, or pending.
  caibDir3?: 'matches' | 'manual' | 'pending';
  allTypes?: boolean;
}

// The responsibles the tests start from: Francisco Planas (CAIB staff) and an external person for two
// responsibilities open to anybody, and the external person authorized for one authorization type.
async function seedResponsibles(
  page: Page,
  cleanup: CleanupRegistry,
  application: DisposableApplication,
  testInfo: TestInfo,
  { unit = OTHER_UNIT, caibDir3 = 'manual', allTypes = false }: SeedOptions = {},
): Promise<Seed> {
  const types = await responsibleTypes(page);
  const [security, systems] = types.filter((type) => !type.requiresPersonalCaib);
  const [authorizations, company] = await Promise.all([
    authorizationTypes(page),
    activeCatalogItem(page, '/invaiback/company'),
  ]);
  await setApplicationUnit(page, application, unit);
  const external = await createExternalPerson(page, cleanup, testInfo, company);
  const caibResponsible = await assignResponsible(page, application, DIR3_PERSON, security, {
    jobTitle: 'Cap de servei',
    observation: 'Observació inicial',
    dir3Matches: caibDir3 === 'matches',
  });
  if (caibDir3 === 'manual') await validateManually(page, caibResponsible, MANUAL_REASON);
  const externalResponsible = await assignResponsible(page, application, external, systems);
  const authorized = await assignAuthorized(page, application, external, [authorizations[0]], {
    observation: 'Autorització inicial',
  });
  if (allTypes) {
    for (const type of types.filter(
      (candidate) => candidate !== security && candidate !== systems,
    )) {
      await assignResponsible(
        page,
        application,
        type.requiresPersonalCaib ? DIR3_PERSON : external,
        type,
        {
          dir3Matches: caibDir3 === 'matches',
        },
      );
    }
  }
  return {
    types,
    security,
    systems,
    authorizations,
    company,
    external,
    caibResponsible,
    externalResponsible,
    authorized,
  };
}

interface ResponsiblesTab {
  writes: Write[];
  reads: Kind[];
  soffidSearches: URLSearchParams[];
  dir3Checks: URLSearchParams[];
}

async function openResponsibles(
  page: Page,
  application: DisposableApplication,
  seed: Seed,
  before?: () => Promise<void>,
): Promise<ResponsiblesTab> {
  const tab: ResponsiblesTab = { writes: [], reads: [], soffidSearches: [], dir3Checks: [] };
  await before?.();
  page.on('request', (request) => {
    const url = new URL(request.url());
    const path = url.pathname.replace('/invaiback/', '');
    const method = request.method();
    const assignment = path.match(/^application\/(responsible|authorized)(?:\/\d+)?$/);
    if (path === 'person/soffid-search') tab.soffidSearches.push(url.searchParams);
    else if (path === 'person/dir3-check') tab.dir3Checks.push(url.searchParams);
    else if (assignment && method === 'GET') tab.reads.push(assignment[1] as Kind);
    else if ((assignment || DIR3_CHECK.test(url.pathname)) && method !== 'GET') {
      tab.writes.push({ method, path, body: request.postDataJSON() });
    }
  });
  await page.goto(`/aplicacions/${application.id}/responsible`);
  // One row per responsible type, assigned or vacant.
  await expect(responsiblesTable(page).locator('tbody tr')).toHaveCount(seed.types.length);
  await expect(authorizedTable(page).locator('tbody tr')).toHaveCount(1);
  return tab;
}

const responsiblesTable = (page: Page) => page.locator('app-application-responsibles-table');
const authorizedTable = (page: Page) => page.locator('app-application-authorized-table');
const toast = (page: Page, detail: string) =>
  page.locator('.p-toast-message').filter({ hasText: detail });
const dialogWith = (page: Page, text: string) => page.getByRole('dialog').filter({ hasText: text });
const caibName = fullName(DIR3_PERSON);

async function startEditing(page: Page): Promise<void> {
  await page.getByRole('button', { name: EDIT_SECTION, exact: true }).click();
}

async function openRowAction(page: Page, table: Locator, person: string, action: string) {
  await table.getByRole('button', { name: `Accions de ${person}` }).click();
  await page.getByRole('menuitem', { name: action, exact: true }).click();
}

test('shows the Soffid user code below names and the reason of a manual DIR3 validation', async ({
  page,
  cleanupRegistry,
  disposableApplication,
}, testInfo) => {
  const seed = await seedResponsibles(page, cleanupRegistry, disposableApplication, testInfo);
  await openResponsibles(page, disposableApplication, seed);

  const caibRow = responsiblesTable(page).locator('tbody tr').filter({ hasText: caibName });
  await expect(caibRow.locator('.application-assignment-person-username')).toHaveText(
    DIR3_PERSON.userName,
  );
  // External people have no Soffid user, so nothing is shown below their name.
  await expect(
    responsiblesTable(page)
      .locator('tbody tr')
      .filter({ hasText: fullName(seed.external) })
      .locator('.application-assignment-person-username'),
  ).toHaveCount(0);
  await expect(
    authorizedTable(page).locator('.application-assignment-person-username'),
  ).toHaveCount(0);

  const manual = caibRow.getByRole('img', { name: 'DIR3 validat manualment' });
  await expect(manual).toHaveAttribute(
    'aria-label',
    `DIR3 validat manualment\nMotiu: ${MANUAL_REASON}`,
  );
  await manual.hover();
  await expect(page.getByRole('tooltip')).toHaveText(
    `DIR3 validat manualment Motiu: ${MANUAL_REASON}`,
  );
});

test('deactivates a responsible only with an observation, sent in the DELETE body', async ({
  page,
  cleanupRegistry,
  disposableApplication,
}, testInfo) => {
  const seed = await seedResponsibles(page, cleanupRegistry, disposableApplication, testInfo);
  const tab = await openResponsibles(page, disposableApplication, seed, async () => {
    await failApiRequest(page, 'DELETE', /^\/invaiback\/application\/responsible\/\d+$/, {
      status: 500,
      json: { message: 'Delete test failure' },
    });
  });
  await startEditing(page);
  await openRowAction(page, responsiblesTable(page), caibName, 'Donar de baixa');

  const dialog = dialogWith(page, 'Donar de baixa aquest registre?');
  await expect(dialog).toContainText(
    `${caibName} deixarà d'estar activa en la responsabilitat de ${seed.security.name}.`,
  );
  const observation = dialog.getByRole('textbox', { name: 'Observacions', exact: true });
  const confirm = dialog.getByRole('button', { name: 'Dona de baixa el responsable' });
  await confirm.click();
  await expect(observation).toHaveAttribute('aria-invalid', 'true');
  await expect(observation).toHaveAccessibleDescription(OBSERVATION_REQUIRED);

  await observation.fill('   ');
  await confirm.click();
  await expect(observation).toHaveAccessibleDescription(OBSERVATION_REQUIRED);

  // The limit is 4000 UTF-8 bytes: 2001 two-byte characters exceed it, 2000 still fit.
  await observation.fill('à'.repeat(2001));
  await expect(observation).toHaveAccessibleDescription(TEXT_TOO_LONG);
  await confirm.click();
  await observation.fill('à'.repeat(2000));
  await expect(observation).toHaveAttribute('aria-invalid', 'false');
  expect(tab.writes).toEqual([]);

  await observation.fill('  Baixa per jubilació  ');
  await confirm.click();
  await expect(toast(page, "No s'ha pogut donar de baixa el responsable.")).toBeVisible();
  await expect(dialog).toBeVisible();
  await expect(observation).toHaveValue('  Baixa per jubilació  ');

  await confirm.click();
  await expect(dialog).toBeHidden();
  await expect(toast(page, "El responsable s'ha donat de baixa correctament.")).toBeVisible();
  const request = {
    method: 'DELETE',
    path: `application/responsible/${seed.caibResponsible.id}`,
    body: { observation: 'Baixa per jubilació' },
  };
  expect(tab.writes).toEqual([request, request]);
  const vacant = responsiblesTable(page)
    .locator('tbody tr')
    .filter({ hasText: seed.security.name });
  await expect(vacant).toContainText('Incomplet');
  await expect(vacant.getByRole('button')).toHaveCount(0);
});

test('deactivates an authorized person with the observation in the DELETE body', async ({
  page,
  cleanupRegistry,
  disposableApplication,
}, testInfo) => {
  const seed = await seedResponsibles(page, cleanupRegistry, disposableApplication, testInfo);
  const tab = await openResponsibles(page, disposableApplication, seed);
  const externalName = fullName(seed.external);
  await startEditing(page);
  await openRowAction(page, authorizedTable(page), externalName, 'Donar de baixa');

  const dialog = dialogWith(page, 'Donar de baixa aquest registre?');
  await expect(dialog).toContainText(
    `${externalName} deixarà d'estar autoritzada per a ${seed.authorizations[0].name}.`,
  );
  const observation = dialog.getByRole('textbox', { name: 'Observacions', exact: true });
  await observation.fill('Ja no hi participa');
  await dialog.getByRole('button', { name: 'Cancel·la la baixa del registre' }).click();
  await expect(dialog).toBeHidden();
  expect(tab.writes).toEqual([]);

  await openRowAction(page, authorizedTable(page), externalName, 'Donar de baixa');
  await expect(observation).toHaveValue('');
  await observation.fill('Ja no hi participa');
  await dialog.getByRole('button', { name: 'Dona de baixa la persona autoritzada' }).click();

  await expect(dialog).toBeHidden();
  await expect(
    toast(page, "La persona autoritzada s'ha donat de baixa correctament."),
  ).toBeVisible();
  expect(tab.writes).toEqual([
    {
      method: 'DELETE',
      path: `application/authorized/${seed.authorized.id}`,
      body: { observation: 'Ja no hi participa' },
    },
  ]);
  await expect(authorizedTable(page).locator('tbody tr')).toHaveText(["No s'han trobat resultats"]);
});

test('edits a CAIB and an external responsible with PUT payloads', async ({
  page,
  cleanupRegistry,
  disposableApplication,
}, testInfo) => {
  const seed = await seedResponsibles(page, cleanupRegistry, disposableApplication, testInfo);
  const tab = await openResponsibles(page, disposableApplication, seed);
  await startEditing(page);
  await openRowAction(page, responsiblesTable(page), caibName, 'Editar');

  let dialog = dialogWith(page, 'Edita el responsable');
  await expect(dialog.getByRole('switch', { name: 'Personal CAIB' })).toBeChecked();
  await expect(dialog.getByRole('switch', { name: 'Personal CAIB' })).toBeDisabled();
  await expect(page.locator('#application-responsible-dialog-type')).toHaveText(seed.security.name);
  await expect(page.locator('#application-responsible-dialog-person')).toHaveText(caibName);
  await expect(dialog.getByRole('textbox', { name: 'E-mail' })).toHaveValue(DIR3_PERSON.email);
  await expect(dialog.getByRole('textbox', { name: 'E-mail' })).not.toBeEditable();
  const cargo = dialog.getByRole('textbox', { name: 'Càrrec' });
  await expect(cargo).toHaveValue('Cap de servei');
  const observation = dialog.getByRole('textbox', { name: 'Observacions (Opcional)' });
  await expect(observation).toHaveValue('Observació inicial');

  await cargo.fill('  Cap de servei TIC ');
  await observation.fill('à'.repeat(2001));
  await expect(observation).toHaveAccessibleDescription(TEXT_TOO_LONG);
  await dialog.getByRole('button', { name: 'Desa el responsable' }).click();
  expect(tab.writes).toEqual([]);

  await observation.fill(' Nova observació ');
  await dialog.getByRole('button', { name: 'Desa el responsable' }).click();
  await expect(dialog).toBeHidden();
  await expect(toast(page, "El responsable s'ha actualitzat correctament.")).toBeVisible();
  await expect(
    responsiblesTable(page).locator('tbody tr').filter({ hasText: caibName }),
  ).toContainText('Cap de servei TIC');

  await openRowAction(page, responsiblesTable(page), fullName(seed.external), 'Editar');
  dialog = dialogWith(page, 'Edita el responsable');
  await expect(page.locator('#application-responsible-dialog-company')).toHaveText(
    seed.company.name,
  );
  await expect(dialog.getByRole('textbox', { name: 'Càrrec' })).toHaveCount(0);
  await dialog.getByRole('textbox', { name: 'Observacions (Opcional)' }).fill('Proveïdor extern');
  await dialog.getByRole('button', { name: 'Desa el responsable' }).click();
  await expect(dialog).toBeHidden();

  expect(tab.writes).toEqual([
    {
      method: 'PUT',
      path: `application/responsible/${seed.caibResponsible.id}`,
      body: {
        personId: seed.caibResponsible.person.id,
        personalCaib: true,
        appResponsibleAuthorizedId: disposableApplication.appResponsibleAuthorizedId,
        responsibleTypeId: seed.security.id,
        jobTitle: 'Cap de servei TIC',
        observation: 'Nova observació',
      },
    },
    {
      method: 'PUT',
      path: `application/responsible/${seed.externalResponsible.id}`,
      body: {
        personId: seed.external.id,
        personalCaib: false,
        appResponsibleAuthorizedId: disposableApplication.appResponsibleAuthorizedId,
        responsibleTypeId: seed.systems.id,
        jobTitle: null,
        observation: 'Proveïdor extern',
      },
    },
  ]);
});

test('edits the authorizations of an authorized person with a PUT payload', async ({
  page,
  cleanupRegistry,
  disposableApplication,
}, testInfo) => {
  const seed = await seedResponsibles(page, cleanupRegistry, disposableApplication, testInfo);
  const [first, second] = seed.authorizations;
  const tab = await openResponsibles(page, disposableApplication, seed);
  await startEditing(page);
  await openRowAction(page, authorizedTable(page), fullName(seed.external), 'Editar');

  const dialog = dialogWith(page, 'Edita la persona autoritzada');
  await expect(dialog.getByRole('switch', { name: 'Personal CAIB' })).toBeDisabled();
  await expect(page.locator('#application-authorized-dialog-person')).toHaveText(
    fullName(seed.external),
  );
  await expect(dialog.locator('.p-multiselect-chip')).toHaveText([first.name]);

  const authorizations = page.locator('#application-authorized-dialog-authorization');
  await authorizations.focus();
  await page.keyboard.press('Enter');
  await page.getByRole('option', { name: first.name, exact: true }).click();
  await page.keyboard.press('Escape');
  await dialog.getByRole('button', { name: 'Desa la persona autoritzada' }).click();
  await expect(dialog.getByText(REQUIRED)).toBeVisible();
  expect(tab.writes).toEqual([]);

  await authorizations.focus();
  await page.keyboard.press('Enter');
  await page.getByRole('option', { name: second.name, exact: true }).click();
  await page.getByRole('option', { name: first.name, exact: true }).click();
  await page.keyboard.press('Escape');
  await expect(dialog.getByText(REQUIRED)).toHaveCount(0);
  await dialog.getByRole('textbox', { name: 'Observacions (Opcional)' }).fill('');
  await dialog.getByRole('button', { name: 'Desa la persona autoritzada' }).click();

  await expect(dialog).toBeHidden();
  await expect(toast(page, "La persona autoritzada s'ha actualitzat correctament.")).toBeVisible();
  expect(tab.writes).toEqual([
    {
      method: 'PUT',
      path: `application/authorized/${seed.authorized.id}`,
      body: {
        personId: seed.external.id,
        personalCaib: false,
        appResponsibleAuthorizedId: disposableApplication.appResponsibleAuthorizedId,
        authorizationTypeIds: [second.id, first.id],
        observation: null,
      },
    },
  ]);
  const row = authorizedTable(page).locator('tbody tr');
  await expect(row).toContainText(first.name);
  await expect(row).toContainText(second.name);
});

test('adds an authorized CAIB person picked from Soffid with their user code', async ({
  page,
  cleanupRegistry,
  disposableApplication,
}, testInfo) => {
  // The unit of the application is the DIR3 of the person, so the check matches.
  const seed = await seedResponsibles(page, cleanupRegistry, disposableApplication, testInfo, {
    unit: DIR3_UNIT,
    caibDir3: 'matches',
  });
  const tab = await openResponsibles(page, disposableApplication, seed);
  await startEditing(page);
  await page.getByRole('button', { name: 'Afegeix una persona autoritzada', exact: true }).click();

  const dialog = dialogWith(page, 'Afegeix una persona autoritzada');
  await dialog.getByRole('switch', { name: 'Personal CAIB' }).click();
  const add = dialog.getByRole('button', { name: 'Afegeix la persona autoritzada' });
  // A CAIB person can only be added once picked from Soffid and checked against DIR3.
  await expect(add).toBeDisabled();

  const soffid = page.locator('#application-authorized-dialog-soffid-person');
  await soffid.fill(DIR3_PERSON.query);
  await page.getByRole('option', { name: new RegExp(caibName) }).click();
  await expect(dialog.getByRole('textbox', { name: 'E-mail' })).toHaveValue(DIR3_PERSON.email);
  await expect(add).toBeEnabled();
  expect(tab.soffidSearches.map((params) => Object.fromEntries(params))).toEqual([
    { search: DIR3_PERSON.query, page: '0', size: '20' },
  ]);
  expect(tab.dir3Checks.map((params) => Object.fromEntries(params))).toEqual([
    { emailAddress: DIR3_PERSON.email, admUnitCode: DIR3_UNIT },
  ]);

  await page.locator('#application-authorized-dialog-authorization').focus();
  await page.keyboard.press('Enter');
  await page.getByRole('option', { name: seed.authorizations[0].name, exact: true }).click();
  await page.keyboard.press('Escape');
  await add.click();

  await expect(dialog).toBeHidden();
  await expect(toast(page, "La persona autoritzada s'ha afegit correctament.")).toBeVisible();
  expect(tab.writes).toEqual([
    {
      method: 'POST',
      path: 'application/authorized',
      body: {
        personId: null,
        personFirstName: DIR3_PERSON.firstName,
        personLastName: DIR3_PERSON.lastName,
        personEmail: DIR3_PERSON.email,
        personUserName: DIR3_PERSON.userName,
        companyId: null,
        personalCaib: true,
        dir3Status: true,
        appResponsibleAuthorizedId: disposableApplication.appResponsibleAuthorizedId,
        authorizationTypeIds: [seed.authorizations[0].id],
        observation: null,
      },
    },
  ]);
  const added = authorizedTable(page).locator('tbody tr').filter({ hasText: caibName });
  await expect(added.locator('.application-assignment-person-username')).toHaveText(
    DIR3_PERSON.userName,
  );
});

test('explains that every responsibility is assigned and returns focus to the add button', async ({
  page,
  cleanupRegistry,
  disposableApplication,
}, testInfo) => {
  const seed = await seedResponsibles(page, cleanupRegistry, disposableApplication, testInfo, {
    allTypes: true,
  });
  const tab = await openResponsibles(page, disposableApplication, seed);
  await startEditing(page);
  const add = page.getByRole('button', { name: 'Afegeix un responsable', exact: true });
  await expect(add).toBeEnabled();
  await add.click();

  const dialog = page.getByRole('dialog', { name: 'Totes les responsabilitats estan assignades' });
  await expect(dialog).toContainText(
    "Per afegir un responsable nou, primer has de donar de baixa una de les assignacions actuals des del menú d'accions de la taula.",
  );
  const accept = dialog.getByRole('button', { name: 'Entesos' });
  await expect(accept).toBeFocused();
  await expect(page.getByRole('button', { name: 'Afegeix el responsable' })).toHaveCount(0);
  await accept.click();
  await expect(dialog).toBeHidden();
  await expect(add).toBeFocused();

  await add.press('Enter');
  await expect(dialog).toBeVisible();
  await page.keyboard.press('Escape');
  await expect(dialog).toBeHidden();
  await expect(add).toBeFocused();
  expect(tab.writes).toEqual([]);
});

// The real check runs for the mismatch and the match; the failures answer the check with an
// error. Every answer is held back until the check in progress has been seen.
const DIR3_OUTCOMES: Array<{
  title: string;
  seed: SeedOptions;
  status?: number;
  severity: string;
  summary: string;
  detail: string;
  refreshed: boolean;
  pending: number;
}> = [
  {
    title: 'warns about a DIR3 mismatch',
    seed: { unit: OTHER_UNIT, caibDir3: 'pending' },
    severity: 'warn',
    summary: 'Validació DIR3 completada',
    detail: "L'aplicació té responsables o autoritzats amb discrepàncies DIR3.",
    refreshed: true,
    pending: 1,
  },
  {
    title: 'confirms a matching DIR3',
    seed: { unit: DIR3_UNIT, caibDir3: 'matches' },
    severity: 'success',
    summary: 'Validació DIR3 completada',
    detail: "El DIR3 de tots els responsables i autoritzats coincideix amb el de l'aplicació.",
    refreshed: true,
    pending: 0,
  },
  {
    title: 'reports a failed DIR3 check',
    seed: { unit: DIR3_UNIT, caibDir3: 'matches' },
    status: 500,
    severity: 'error',
    summary: 'Error',
    detail: "No s'ha pogut completar la validació DIR3 de l'aplicació. Torna-ho a provar.",
    refreshed: false,
    pending: 0,
  },
  {
    title: 'reports a forbidden DIR3 check',
    seed: { unit: DIR3_UNIT, caibDir3: 'matches' },
    status: 403,
    severity: 'error',
    summary: 'Error',
    detail: 'No tens permisos per fer aquesta operació.',
    refreshed: false,
    pending: 0,
  },
];

for (const outcome of DIR3_OUTCOMES) {
  test(`checks the DIR3 of the whole application and ${outcome.title}`, async ({
    page,
    cleanupRegistry,
    disposableApplication,
  }, testInfo) => {
    test.setTimeout(90_000);
    const seed = await seedResponsibles(
      page,
      cleanupRegistry,
      disposableApplication,
      testInfo,
      outcome.seed,
    );
    let release!: () => void;
    const gate = new Promise<void>((resolve) => (release = resolve));
    const tab = await openResponsibles(page, disposableApplication, seed, async () => {
      await page.route(
        (url) => DIR3_CHECK.test(url.pathname),
        async (route) => {
          await gate;
          if (!outcome.status) return route.fallback();
          return route.fulfill({
            status: outcome.status,
            json: { message: 'DIR3 check test failure' },
          });
        },
      );
    });
    const reads = tab.reads.length;
    const check = page.getByRole('button', { name: CHECK_DIR3, exact: true });

    await check.click();
    await expect(check).toBeDisabled();
    await expect(page.getByRole('status').filter({ hasText: CHECK_DIR3_PROGRESS })).toHaveCount(1);
    release();

    const message = toast(page, outcome.detail);
    await expect(message).toBeVisible({ timeout: 30_000 });
    await expect(message).toHaveClass(new RegExp(`p-toast-message-${outcome.severity}`));
    await expect(message.locator('.p-toast-summary')).toHaveText(outcome.summary);
    await expect(check).toBeEnabled();
    await expect(page.getByRole('status').filter({ hasText: CHECK_DIR3_PROGRESS })).toHaveCount(0);
    expect(tab.writes).toEqual([
      { method: 'PUT', path: `application/dir3/check/${disposableApplication.id}`, body: {} },
    ]);
    if (outcome.refreshed) {
      await expect
        .poll(() => [...tab.reads.slice(reads)].sort())
        .toEqual(['authorized', 'responsible']);
    } else {
      expect(tab.reads.slice(reads)).toEqual([]);
    }
    await expect(responsiblesTable(page).getByRole('img', { name: 'DIR3 no validat' })).toHaveCount(
      outcome.pending,
    );
  });
}

test('does not check the DIR3 of an application without an administrative unit', async ({
  page,
  cleanupRegistry,
  disposableApplication,
}, testInfo) => {
  const seed = await seedResponsibles(page, cleanupRegistry, disposableApplication, testInfo);
  await setApplicationUnit(page, disposableApplication, null);
  const tab = await openResponsibles(page, disposableApplication, seed);
  await page.getByRole('button', { name: CHECK_DIR3, exact: true }).click();

  const message = toast(
    page,
    "L'aplicació no té unitat administrativa, per això no es pot validar el DIR3.",
  );
  await expect(message).toBeVisible();
  await expect(message).toHaveClass(/p-toast-message-warn/);
  await expect(message.locator('.p-toast-summary')).toHaveText(CHECK_DIR3);
  expect(tab.writes).toEqual([]);
});

test('offers no DIR3 check nor edition for an inactive application', async ({
  page,
  cleanupRegistry,
  disposableApplication,
}, testInfo) => {
  const seed = await seedResponsibles(page, cleanupRegistry, disposableApplication, testInfo);
  const deactivated = await page.request.delete(
    apiUrl(page, `/invaiback/application/${disposableApplication.id}`),
  );
  expect(deactivated.ok(), await deactivated.text()).toBeTruthy();
  await openResponsibles(page, disposableApplication, seed);
  await expect(page.getByRole('button', { name: CHECK_DIR3, exact: true })).toHaveCount(0);
  await expect(page.getByRole('button', { name: EDIT_SECTION, exact: true })).toHaveCount(0);
});
