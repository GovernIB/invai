import { expect, test, type Locator, type Page, type Route } from '@playwright/test';

test.use({ storageState: { cookies: [], origins: [] } });

const UNIT = { code: 'A1', name: 'Unitat de prova' };
const COMPANY = { id: 2, nif: null, name: 'Example company', deletedAt: null };
const CAIB_PERSON = {
  id: 12,
  firstName: 'Joan',
  lastName: 'Serra',
  email: 'joan@caib.es',
  personalCaib: true,
  company: null,
  deletedAt: null,
  userName: 'u00012',
};
const EXTERNAL_PERSON = {
  id: 14,
  firstName: 'Marta',
  lastName: 'Riera',
  email: 'marta@example.test',
  personalCaib: false,
  company: COMPANY,
  deletedAt: null,
  userName: null,
};
const PEOPLE = [CAIB_PERSON, EXTERNAL_PERSON];
const SOFFID_CANDIDATE = {
  id: null,
  company: null,
  firstName: 'Aina',
  lastName: 'Ferrer',
  email: 'aina@caib.es',
  personalCaib: true,
  deletedAt: null,
  userName: 'u00077',
};
const SECURITY = { id: 1, name: 'Seguretat', nameEs: 'Seguridad', requiresPersonalCaib: false };
const SYSTEMS = { id: 2, name: 'Sistemes', nameEs: 'Sistemas', requiresPersonalCaib: false };
const SIGNATURE = { id: 1, name: 'Signatura', nameEs: 'Firma', deletedAt: null };
const QUERY = { id: 2, name: 'Consulta', nameEs: 'Consulta', deletedAt: null };
const AUTHORIZATION_TYPES = [SIGNATURE, QUERY];
const MANUAL_REASON = 'Responsabilitat transversal';

const EDIT_SECTION = "Editar Responsables de l'aplicació";
const CHECK_DIR3 = "Validar el DIR3 de l'aplicació";
const CHECK_DIR3_PROGRESS = 'Validant el DIR3 dels responsables i autoritzats…';
const OBSERVATION_REQUIRED = 'Les observacions són obligatòries.';
const TEXT_TOO_LONG =
  'El text supera la mida màxima permesa. Redueix-lo (els accents i el format també ocupen espai).';
const REQUIRED = 'Aquest camp és obligatori.';

type Kind = 'responsible' | 'authorized';
type Assignment = Record<string, unknown> & { id: number };

interface Write {
  method: string;
  path: string;
  body: unknown;
}

interface MockOptions {
  admUnit?: typeof UNIT | null;
  status?: 'ACTIVE' | 'INACTIVE';
  failedDeletes?: number;
  dir3Check?: { status?: number; dir3Mismatch?: boolean; gate?: Promise<void> };
}

interface ResponsiblesMock {
  writes: Write[];
  reads: Kind[];
  soffidSearches: URLSearchParams[];
  dir3Checks: URLSearchParams[];
}

const pageOf = (content: unknown[]) => ({
  content,
  totalElements: content.length,
  totalPages: 1,
  last: true,
  number: 0,
});

async function openResponsibles(page: Page, options: MockOptions = {}): Promise<ResponsiblesMock> {
  const mock: ResponsiblesMock = { writes: [], reads: [], soffidSearches: [], dir3Checks: [] };
  let failedDeletes = options.failedDeletes ?? 0;
  const lists: Record<Kind, Assignment[]> = {
    responsible: [
      {
        id: 21,
        appResponsibleAuthorizedId: 91,
        person: CAIB_PERSON,
        responsibleType: SECURITY,
        jobTitle: 'Cap de servei',
        observation: 'Observació inicial',
        deletedAt: null,
        dir3Validation: {
          id: 501,
          dir3Status: 'MANUAL',
          reason: MANUAL_REASON,
          manualValidatedAt: '2026-10-01T10:00:00',
          manualValidatedBy: 'admin',
        },
      },
      {
        id: 22,
        appResponsibleAuthorizedId: 91,
        person: EXTERNAL_PERSON,
        responsibleType: SYSTEMS,
        jobTitle: null,
        observation: null,
        deletedAt: null,
        dir3Validation: null,
      },
    ],
    authorized: [
      {
        id: 31,
        appResponsibleAuthorizedId: 91,
        person: EXTERNAL_PERSON,
        authorizationTypes: [SIGNATURE],
        observation: 'Autorització inicial',
        deletedAt: null,
        dir3Validation: null,
      },
    ],
  };

  await page.route('**/invaiback/**', async (route: Route) => {
    const request = route.request();
    const url = new URL(request.url());
    const path = url.pathname;
    const method = request.method();
    if (path.endsWith('/auth/me')) {
      return route.fulfill({ json: { authenticated: true, username: 'responsibles-test' } });
    }
    if (path.endsWith('/application/1')) {
      return route.fulfill({
        json: {
          id: 1,
          code: 'TEST',
          name: 'Responsables test',
          status: options.status ?? 'ACTIVE',
          appResponsibleAuthorizedId: 91,
          admUnit: options.admUnit === undefined ? UNIT : options.admUnit,
        },
      });
    }
    if (path.endsWith('/application/dir3/check/1')) {
      mock.writes.push({ method, path: 'application/dir3/check/1', body: request.postDataJSON() });
      await options.dir3Check?.gate;
      const status = options.dir3Check?.status ?? 200;
      if (status !== 200) {
        return route.fulfill({ status, json: { message: 'DIR3 check test failure' } });
      }
      const dir3Mismatch = options.dir3Check?.dir3Mismatch ?? false;
      if (dir3Mismatch) {
        const responsible = lists.responsible[0];
        responsible.dir3Validation = {
          ...(responsible.dir3Validation as object),
          dir3Status: 'NOT_VALIDATED',
          reason: null,
        };
      }
      return route.fulfill({ json: { id: 1, name: 'Responsables test', dir3Mismatch } });
    }
    const assignment = path.match(/\/application\/(responsible|authorized)(?:\/(\d+))?$/);
    if (assignment) {
      const kind = assignment[1] as Kind;
      const id = Number(assignment[2]);
      if (method === 'GET') {
        mock.reads.push(kind);
        return route.fulfill({ json: pageOf(lists[kind]) });
      }
      const body = request.postDataJSON();
      mock.writes.push({ method, path: path.slice(path.indexOf('application/')), body });
      if (method === 'DELETE') {
        if (failedDeletes > 0) {
          failedDeletes--;
          return route.fulfill({ status: 500, json: { message: 'Delete test failure' } });
        }
        lists[kind] = lists[kind].filter((item) => item.id !== id);
        return route.fulfill({ status: 204 });
      }
      const authorizationTypes = (body.authorizationTypeIds as number[] | undefined)?.map(
        (typeId) => AUTHORIZATION_TYPES.find((type) => type.id === typeId),
      );
      if (method === 'PUT') {
        const target = lists[kind].find((item) => item.id === id)!;
        Object.assign(target, { observation: body.observation });
        if (kind === 'responsible') target.jobTitle = body.jobTitle;
        else target.authorizationTypes = authorizationTypes;
        return route.fulfill({ json: target });
      }
      const created: Assignment = {
        id: 32,
        appResponsibleAuthorizedId: 91,
        person: {
          id: 15,
          firstName: body.personFirstName,
          lastName: body.personLastName,
          email: body.personEmail,
          personalCaib: true,
          company: null,
          deletedAt: null,
          userName: body.personUserName,
        },
        authorizationTypes,
        observation: body.observation,
        deletedAt: null,
        dir3Validation: null,
      };
      lists.authorized.push(created);
      return route.fulfill({ json: created });
    }
    let json: unknown = pageOf([]);
    if (path.endsWith('/responsible-type')) json = [SECURITY, SYSTEMS];
    else if (path.endsWith('/company')) json = pageOf([COMPANY]);
    else if (path.endsWith('/person/database-search')) json = pageOf(PEOPLE);
    else if (path.endsWith('/authorization-type')) json = pageOf(AUTHORIZATION_TYPES);
    else if (path.endsWith('/person/soffid-search')) {
      mock.soffidSearches.push(url.searchParams);
      json = pageOf([SOFFID_CANDIDATE]);
    } else if (path.endsWith('/person/dir3-check')) {
      mock.dir3Checks.push(url.searchParams);
      json = { personGroup: 'CAIB', groupDir3: 'A1', admUnitCode: 'A1', matches: true };
    }
    return route.fulfill({ json });
  });

  await page.goto('/aplicacions/1/responsible');
  await expect(responsiblesTable(page).locator('tbody tr')).toHaveCount(2);
  await expect(authorizedTable(page).locator('tbody tr')).toHaveCount(1);
  return mock;
}

const responsiblesTable = (page: Page) => page.locator('app-application-responsibles-table');
const authorizedTable = (page: Page) => page.locator('app-application-authorized-table');
const toast = (page: Page, detail: string) =>
  page.locator('.p-toast-message').filter({ hasText: detail });
const dialogWith = (page: Page, text: string) => page.getByRole('dialog').filter({ hasText: text });

async function startEditing(page: Page): Promise<void> {
  await page.getByRole('button', { name: EDIT_SECTION, exact: true }).click();
}

async function openRowAction(page: Page, table: Locator, person: string, action: string) {
  await table.getByRole('button', { name: `Accions de ${person}` }).click();
  await page.getByRole('menuitem', { name: action, exact: true }).click();
}

test('shows the Soffid user code below names and the reason of a manual DIR3 validation', async ({
  page,
}) => {
  await openResponsibles(page);

  const caibRow = responsiblesTable(page).locator('tbody tr').filter({ hasText: 'Joan Serra' });
  await expect(caibRow.locator('.application-assignment-person-username')).toHaveText('u00012');
  // External people have no Soffid user, so nothing is shown below their name.
  await expect(
    responsiblesTable(page).locator('tbody tr').filter({ hasText: 'Marta Riera' }),
  ).not.toContainText('u000');
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
}) => {
  const mock = await openResponsibles(page, { failedDeletes: 1 });
  await startEditing(page);
  await openRowAction(page, responsiblesTable(page), 'Joan Serra', 'Donar de baixa');

  const dialog = dialogWith(page, 'Donar de baixa aquest registre?');
  await expect(dialog).toContainText(
    "Joan Serra deixarà d'estar activa en la responsabilitat de Seguretat.",
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
  expect(mock.writes).toEqual([]);

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
    path: 'application/responsible/21',
    body: { observation: 'Baixa per jubilació' },
  };
  expect(mock.writes).toEqual([request, request]);
  const vacant = responsiblesTable(page).locator('tbody tr').filter({ hasText: 'Seguretat' });
  await expect(vacant).toContainText('Incomplet');
  await expect(vacant.getByRole('button')).toHaveCount(0);
});

test('deactivates an authorized person with the observation in the DELETE body', async ({
  page,
}) => {
  const mock = await openResponsibles(page);
  await startEditing(page);
  await openRowAction(page, authorizedTable(page), 'Marta Riera', 'Donar de baixa');

  const dialog = dialogWith(page, 'Donar de baixa aquest registre?');
  await expect(dialog).toContainText("Marta Riera deixarà d'estar autoritzada per a Signatura.");
  const observation = dialog.getByRole('textbox', { name: 'Observacions', exact: true });
  await observation.fill('Ja no hi participa');
  await dialog.getByRole('button', { name: 'Cancel·la la baixa del registre' }).click();
  await expect(dialog).toBeHidden();
  expect(mock.writes).toEqual([]);

  await openRowAction(page, authorizedTable(page), 'Marta Riera', 'Donar de baixa');
  await expect(observation).toHaveValue('');
  await observation.fill('Ja no hi participa');
  await dialog.getByRole('button', { name: 'Dona de baixa la persona autoritzada' }).click();

  await expect(dialog).toBeHidden();
  await expect(
    toast(page, "La persona autoritzada s'ha donat de baixa correctament."),
  ).toBeVisible();
  expect(mock.writes).toEqual([
    {
      method: 'DELETE',
      path: 'application/authorized/31',
      body: { observation: 'Ja no hi participa' },
    },
  ]);
  await expect(authorizedTable(page).locator('tbody tr')).toHaveText(["No s'han trobat resultats"]);
});

test('edits a CAIB and an external responsible with PUT payloads', async ({ page }) => {
  const mock = await openResponsibles(page);
  await startEditing(page);
  await openRowAction(page, responsiblesTable(page), 'Joan Serra', 'Editar');

  let dialog = dialogWith(page, 'Edita el responsable');
  await expect(dialog.getByRole('switch', { name: 'Personal CAIB' })).toBeChecked();
  await expect(dialog.getByRole('switch', { name: 'Personal CAIB' })).toBeDisabled();
  await expect(page.locator('#application-responsible-dialog-type')).toHaveText('Seguretat');
  await expect(page.locator('#application-responsible-dialog-person')).toHaveText('Joan Serra');
  await expect(dialog.getByRole('textbox', { name: 'E-mail' })).toHaveValue(CAIB_PERSON.email);
  await expect(dialog.getByRole('textbox', { name: 'E-mail' })).not.toBeEditable();
  const cargo = dialog.getByRole('textbox', { name: 'Càrrec' });
  await expect(cargo).toHaveValue('Cap de servei');
  const observation = dialog.getByRole('textbox', { name: 'Observacions (Opcional)' });
  await expect(observation).toHaveValue('Observació inicial');

  await cargo.fill('  Cap de servei TIC ');
  await observation.fill('à'.repeat(2001));
  await expect(observation).toHaveAccessibleDescription(TEXT_TOO_LONG);
  await dialog.getByRole('button', { name: 'Desa el responsable' }).click();
  expect(mock.writes).toEqual([]);

  await observation.fill(' Nova observació ');
  await dialog.getByRole('button', { name: 'Desa el responsable' }).click();
  await expect(dialog).toBeHidden();
  await expect(toast(page, "El responsable s'ha actualitzat correctament.")).toBeVisible();
  await expect(
    responsiblesTable(page).locator('tbody tr').filter({ hasText: 'Joan Serra' }),
  ).toContainText('Cap de servei TIC');

  await openRowAction(page, responsiblesTable(page), 'Marta Riera', 'Editar');
  dialog = dialogWith(page, 'Edita el responsable');
  await expect(page.locator('#application-responsible-dialog-company')).toHaveText(
    'Example company',
  );
  await expect(dialog.getByRole('textbox', { name: 'Càrrec' })).toHaveCount(0);
  await dialog.getByRole('textbox', { name: 'Observacions (Opcional)' }).fill('Proveïdor extern');
  await dialog.getByRole('button', { name: 'Desa el responsable' }).click();
  await expect(dialog).toBeHidden();

  expect(mock.writes).toEqual([
    {
      method: 'PUT',
      path: 'application/responsible/21',
      body: {
        personId: 12,
        personalCaib: true,
        appResponsibleAuthorizedId: 91,
        responsibleTypeId: 1,
        jobTitle: 'Cap de servei TIC',
        observation: 'Nova observació',
      },
    },
    {
      method: 'PUT',
      path: 'application/responsible/22',
      body: {
        personId: 14,
        personalCaib: false,
        appResponsibleAuthorizedId: 91,
        responsibleTypeId: 2,
        jobTitle: null,
        observation: 'Proveïdor extern',
      },
    },
  ]);
});

test('edits the authorizations of an authorized person with a PUT payload', async ({ page }) => {
  const mock = await openResponsibles(page);
  await startEditing(page);
  await openRowAction(page, authorizedTable(page), 'Marta Riera', 'Editar');

  const dialog = dialogWith(page, 'Edita la persona autoritzada');
  await expect(dialog.getByRole('switch', { name: 'Personal CAIB' })).toBeDisabled();
  await expect(page.locator('#application-authorized-dialog-person')).toHaveText('Marta Riera');
  await expect(dialog.locator('.p-multiselect-chip')).toHaveText(['Signatura']);

  const authorizations = page.locator('#application-authorized-dialog-authorization');
  await authorizations.focus();
  await page.keyboard.press('Enter');
  await page.getByRole('option', { name: 'Signatura' }).click();
  await page.keyboard.press('Escape');
  await dialog.getByRole('button', { name: 'Desa la persona autoritzada' }).click();
  await expect(dialog.getByText(REQUIRED)).toBeVisible();
  expect(mock.writes).toEqual([]);

  await authorizations.focus();
  await page.keyboard.press('Enter');
  await page.getByRole('option', { name: 'Consulta' }).click();
  await page.getByRole('option', { name: 'Signatura' }).click();
  await page.keyboard.press('Escape');
  await expect(dialog.getByText(REQUIRED)).toHaveCount(0);
  await dialog.getByRole('textbox', { name: 'Observacions (Opcional)' }).fill('');
  await dialog.getByRole('button', { name: 'Desa la persona autoritzada' }).click();

  await expect(dialog).toBeHidden();
  await expect(
    toast(page, "La persona autoritzada s'ha actualitzat correctament."),
  ).toBeVisible();
  expect(mock.writes).toEqual([
    {
      method: 'PUT',
      path: 'application/authorized/31',
      body: {
        personId: 14,
        personalCaib: false,
        appResponsibleAuthorizedId: 91,
        authorizationTypeIds: [2, 1],
        observation: null,
      },
    },
  ]);
  await expect(authorizedTable(page).locator('tbody tr')).toContainText('Consulta, Signatura');
});

test('adds an authorized CAIB person picked from Soffid with their user code', async ({ page }) => {
  const mock = await openResponsibles(page);
  await startEditing(page);
  await page.getByRole('button', { name: 'Afegeix una persona autoritzada', exact: true }).click();

  const dialog = dialogWith(page, 'Afegeix una persona autoritzada');
  await dialog.getByRole('switch', { name: 'Personal CAIB' }).click();
  const add = dialog.getByRole('button', { name: 'Afegeix la persona autoritzada' });
  // A CAIB person can only be added once picked from Soffid and checked against DIR3.
  await expect(add).toBeDisabled();

  const soffid = page.locator('#application-authorized-dialog-soffid-person');
  await soffid.fill('Aina');
  await page.getByRole('option', { name: /Aina Ferrer/ }).click();
  await expect(dialog.getByRole('textbox', { name: 'E-mail' })).toHaveValue('aina@caib.es');
  await expect(add).toBeEnabled();
  expect(mock.soffidSearches.map((params) => Object.fromEntries(params))).toEqual([
    { search: 'Aina', page: '0', size: '20' },
  ]);
  expect(mock.dir3Checks.map((params) => Object.fromEntries(params))).toEqual([
    { emailAddress: 'aina@caib.es', admUnitCode: 'A1' },
  ]);

  await page.locator('#application-authorized-dialog-authorization').focus();
  await page.keyboard.press('Enter');
  await page.getByRole('option', { name: 'Signatura' }).click();
  await page.keyboard.press('Escape');
  await add.click();

  await expect(dialog).toBeHidden();
  await expect(toast(page, "La persona autoritzada s'ha afegit correctament.")).toBeVisible();
  expect(mock.writes).toEqual([
    {
      method: 'POST',
      path: 'application/authorized',
      body: {
        personId: null,
        personFirstName: 'Aina',
        personLastName: 'Ferrer',
        personEmail: 'aina@caib.es',
        personUserName: 'u00077',
        companyId: null,
        personalCaib: true,
        dir3Status: true,
        appResponsibleAuthorizedId: 91,
        authorizationTypeIds: [1],
        observation: null,
      },
    },
  ]);
  const added = authorizedTable(page).locator('tbody tr').filter({ hasText: 'Aina Ferrer' });
  await expect(added.locator('.application-assignment-person-username')).toHaveText('u00077');
});

test('explains that every responsibility is assigned and returns focus to the add button', async ({
  page,
}) => {
  const mock = await openResponsibles(page);
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
  expect(mock.writes).toEqual([]);
});

const DIR3_OUTCOMES = [
  {
    title: 'warns about a DIR3 mismatch',
    dir3Check: { dir3Mismatch: true },
    severity: 'warn',
    summary: 'Validació DIR3 completada',
    detail: "L'aplicació té responsables o autoritzats amb discrepàncies DIR3.",
    refreshed: true,
  },
  {
    title: 'confirms a matching DIR3',
    dir3Check: { dir3Mismatch: false },
    severity: 'success',
    summary: 'Validació DIR3 completada',
    detail:
      "El DIR3 de tots els responsables i autoritzats coincideix amb el de l'aplicació.",
    refreshed: true,
  },
  {
    title: 'reports a failed DIR3 check',
    dir3Check: { status: 500 },
    severity: 'error',
    summary: 'Error',
    detail: "No s'ha pogut completar la validació DIR3 de l'aplicació. Torna-ho a provar.",
    refreshed: false,
  },
  {
    title: 'reports a forbidden DIR3 check',
    dir3Check: { status: 403 },
    severity: 'error',
    summary: 'Error',
    detail: 'No tens permisos per fer aquesta operació.',
    refreshed: false,
  },
];

for (const outcome of DIR3_OUTCOMES) {
  test(`checks the DIR3 of the whole application and ${outcome.title}`, async ({ page }) => {
    let release!: () => void;
    const gate = new Promise<void>((resolve) => (release = resolve));
    const mock = await openResponsibles(page, { dir3Check: { ...outcome.dir3Check, gate } });
    const reads = mock.reads.length;
    const check = page.getByRole('button', { name: CHECK_DIR3, exact: true });

    await check.click();
    await expect(check).toBeDisabled();
    await expect(page.getByRole('status').filter({ hasText: CHECK_DIR3_PROGRESS })).toHaveCount(1);
    release();

    const message = toast(page, outcome.detail);
    await expect(message).toBeVisible();
    await expect(message).toHaveClass(new RegExp(`p-toast-message-${outcome.severity}`));
    await expect(message.locator('.p-toast-summary')).toHaveText(outcome.summary);
    await expect(check).toBeEnabled();
    await expect(page.getByRole('status').filter({ hasText: CHECK_DIR3_PROGRESS })).toHaveCount(0);
    expect(mock.writes).toEqual([{ method: 'PUT', path: 'application/dir3/check/1', body: {} }]);
    if (outcome.refreshed) {
      await expect
        .poll(() => [...mock.reads.slice(reads)].sort())
        .toEqual(['authorized', 'responsible']);
    } else {
      expect(mock.reads.slice(reads)).toEqual([]);
    }
    await expect(responsiblesTable(page).getByRole('img', { name: 'DIR3 no validat' })).toHaveCount(
      outcome.dir3Check.dir3Mismatch ? 1 : 0,
    );
  });
}

test('does not check the DIR3 of an application without an administrative unit', async ({
  page,
}) => {
  const mock = await openResponsibles(page, { admUnit: null });
  await page.getByRole('button', { name: CHECK_DIR3, exact: true }).click();

  const message = toast(
    page,
    "L'aplicació no té unitat administrativa, per això no es pot validar el DIR3.",
  );
  await expect(message).toBeVisible();
  await expect(message).toHaveClass(/p-toast-message-warn/);
  await expect(message.locator('.p-toast-summary')).toHaveText(CHECK_DIR3);
  expect(mock.writes).toEqual([]);
});

test('offers no DIR3 check nor edition for an inactive application', async ({ page }) => {
  await openResponsibles(page, { status: 'INACTIVE' });
  await expect(page.getByRole('button', { name: CHECK_DIR3, exact: true })).toHaveCount(0);
  await expect(page.getByRole('button', { name: EDIT_SECTION, exact: true })).toHaveCount(0);
});
