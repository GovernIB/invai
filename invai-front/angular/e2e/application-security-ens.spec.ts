import type { Page } from '@playwright/test';

import { recordSecurityRequests, SECURITY_API } from './support/application-security';
import { type DisposableApplication, expect, test } from './support/application.fixture';
import {
  apiUrl,
  expectApiJsonResponse,
  expectJsonResponse,
  waitForApiResponse,
} from './support/http';
import { selectOption } from './support/ui';

const MAX_BYTES_ERROR =
  'El text supera la mida màxima permesa. Redueix-lo (els accents i el format també ocupen espai).';
// 1400 characters, but 4200 UTF-8 bytes: over the 4000-byte limit of the back-end columns.
const OVERSIZED_TEXT = '€'.repeat(1400);
const SAVED_SECTION = "S'han desat correctament els canvis de Seguretat.";
const ROW_ACTIONS = 'Obre les accions del registre';
const ENS_API = `${SECURITY_API}/ens-classification`;

interface NamedItem {
  id: number;
  name: string;
}

// Identifiers of the reference data of the security catalogues, looked up by name.
async function catalogIds(page: Page, pathname: string, names: string[]) {
  const response = await page.request.get(apiUrl(page, `/invaiback/${pathname}`), {
    params: { page: 0, size: 100, statusId: 1 },
  });
  const body = await expectApiJsonResponse<NamedItem[] | { content: NamedItem[] }>(
    response,
    `GET /invaiback/${pathname}`,
  );
  const items = Array.isArray(body) ? body : body.content;
  return Object.fromEntries(
    names.map((name) => {
      const item = items.find((candidate) => candidate.name === name);
      if (!item) throw new Error(`/invaiback/${pathname} has no ${name}`);
      return [name, item.id];
    }),
  );
}

const levels = (page: Page) => catalogIds(page, 'security-level', ['Alt', 'Mitjà', 'Baix']);

async function createClassification(page: Page, application: DisposableApplication) {
  const [level, provider, subject, personalData] = await Promise.all([
    levels(page),
    catalogIds(page, 'identity-provider', ['Certificat Digital']),
    catalogIds(page, 'ens-subject', ['Sí']),
    catalogIds(page, 'personal-data-processing', ['Dades Econòmiques']),
  ]);
  return expectApiJsonResponse<{ id: number }>(
    await page.request.post(apiUrl(page, ENS_API), {
      data: {
        appSecurityId: application.appSecurityId,
        overallGradeId: level['Mitjà'],
        identityProviderId: provider['Certificat Digital'],
        ensSubjectId: subject['Sí'],
        personalDataProcessingId: personalData['Dades Econòmiques'],
        approvalDate: '2026-03-05T00:00:00',
        confidentialityId: level['Baix'],
        integrityId: level['Mitjà'],
        traceabilityId: level['Mitjà'],
        availabilityId: level['Alt'],
        authenticityId: level['Baix'],
      },
    }),
    `POST ${ENS_API}`,
  );
}

async function openSecurityTab(page: Page, application: DisposableApplication) {
  const recorded = recordSecurityRequests(page);
  await page.goto(`/aplicacions/${application.id}/security`);
  return recorded;
}

const ensRegion = (page: Page) =>
  page.getByRole('region', { name: 'Esquema Nacional de Seguretat' });
const risksRegion = (page: Page) => page.getByRole('region', { name: 'Riscos de seguretat' });
const measuresRegion = (page: Page) =>
  page.getByRole('region', { name: 'Mesures de seguretat aplicades' });
const observations = (page: Page) => page.getByRole('textbox', { name: 'Observacions' });
const saveButton = (page: Page) =>
  page.getByRole('button', { name: 'Desar els canvis de Seguretat', exact: true });
const addRiskButton = (page: Page) =>
  page.getByRole('button', { name: 'Afegeix un risc de seguretat' });
const addMeasureButton = (page: Page) =>
  page.getByRole('button', { name: 'Afegeix una mesura de seguretat' });
// The shared CRUD shell does not name its dialog yet, so it is located by its visible heading.
const dialogTitled = (page: Page, title: string) =>
  page.getByRole('dialog').filter({ has: page.getByRole('heading', { name: title }) });

async function startEditing(page: Page): Promise<void> {
  await page.getByRole('button', { name: 'Editar Seguretat', exact: true }).click();
  await expect(saveButton(page)).toBeVisible();
}

async function expectEnsValues(page: Page, values: Record<string, string>): Promise<void> {
  for (const [label, value] of Object.entries(values)) {
    await expect(ensRegion(page).getByLabel(label, { exact: true }), label).toHaveText(value);
  }
}

test('edits the ENS classification and saves it with the observations', async ({
  page,
  disposableApplication,
}, testInfo) => {
  const classification = await createClassification(page, disposableApplication);
  const level = await levels(page);
  const provider = await catalogIds(page, 'identity-provider', [
    'Certificat Digital',
    'DNI Electrònic',
  ]);
  const subject = await catalogIds(page, 'ens-subject', ['Sí']);
  const personalData = await catalogIds(page, 'personal-data-processing', ['Dades Econòmiques']);
  const tab = await openSecurityTab(page, disposableApplication);

  await expect(ensRegion(page).getByRole('alert')).toHaveCount(0);
  await expect(ensRegion(page).getByRole('group', { name: 'Dimensions ENS' })).toBeVisible();
  await expectEnsValues(page, {
    "Grau d'adequació a l'ENS": 'Mitjà',
    "Proveïdor d'identitat": 'Certificat Digital',
    "Subjecció a l'ENS": 'Sí',
    'Tractament de dades personals': 'Dades Econòmiques',
    "Data d'aprovació": '5/3/2026',
    Confidencialitat: 'Baix',
    Integritat: 'Mitjà',
    Traçabilitat: 'Mitjà',
    Disponibilitat: 'Alt',
    Autenticitat: 'Baix',
  });

  await startEditing(page);
  await selectOption(page, 'application-security-overall-grade', 'Alt');
  await selectOption(page, 'application-security-identity-provider', 'DNI Electrònic');
  await selectOption(page, 'application-security-availability', 'Baix');
  const approvalDate = page.locator('#application-security-approval-date');
  await expect(approvalDate).toHaveValue('05/03/2026');
  // The date picker only parses typed text after real key presses.
  await approvalDate.click();
  await approvalDate.press('Control+A');
  await approvalDate.pressSequentially('15/09/2026');
  await approvalDate.press('Tab');
  await expect(approvalDate).toHaveValue('15/09/2026');
  await observations(page).fill('Revisat');
  await ensRegion(page).screenshot({ path: testInfo.outputPath('ens-edit.png') });
  await saveButton(page).click();

  await expect(page.getByText(SAVED_SECTION)).toBeVisible();
  expect(tab.writes).toEqual([
    {
      method: 'PUT',
      path: `/application/security/${disposableApplication.appSecurityId}`,
      body: { applicationId: disposableApplication.id, observation: '<p>Revisat</p>' },
    },
    {
      method: 'PUT',
      path: `/application/security/ens-classification/${classification.id}`,
      body: {
        appSecurityId: disposableApplication.appSecurityId,
        identityProviderId: provider['DNI Electrònic'],
        ensSubjectId: subject['Sí'],
        personalDataProcessingId: personalData['Dades Econòmiques'],
        approvalDate: '2026-09-15T00:00:00',
        confidentialityId: level['Baix'],
        integrityId: level['Mitjà'],
        traceabilityId: level['Mitjà'],
        availabilityId: level['Baix'],
        authenticityId: level['Baix'],
        overallGradeId: level['Alt'],
      },
    },
  ]);
  await expect(page.getByRole('button', { name: 'Editar Seguretat', exact: true })).toBeVisible();
  await expectEnsValues(page, {
    "Grau d'adequació a l'ENS": 'Alt',
    "Proveïdor d'identitat": 'DNI Electrònic',
    "Data d'aprovació": '15/9/2026',
    Disponibilitat: 'Baix',
  });
  await expect(page.locator('.application-security__observations')).toContainText('Revisat');
});

// The Seguretat record is created with its application; creating it on the first save, before
// risks and measures can be added, is covered by the unit tests of ApplicationDetailState.
test('creates the ENS classification on the first save', async ({
  page,
  disposableApplication,
}) => {
  const level = await levels(page);
  const tab = await openSecurityTab(page, disposableApplication);
  await expectEnsValues(page, { "Grau d'adequació a l'ENS": '-', Integritat: '-' });

  await startEditing(page);
  await selectOption(page, 'application-security-integrity', 'Alt');
  await saveButton(page).click();

  await expect(page.getByText(SAVED_SECTION)).toBeVisible();
  expect(tab.writes.filter(({ path }) => path.includes('ens-classification'))).toEqual([
    {
      method: 'POST',
      path: '/application/security/ens-classification',
      body: {
        appSecurityId: disposableApplication.appSecurityId,
        identityProviderId: null,
        ensSubjectId: null,
        personalDataProcessingId: null,
        approvalDate: null,
        confidentialityId: null,
        integrityId: level['Alt'],
        traceabilityId: null,
        availabilityId: null,
        authenticityId: null,
        overallGradeId: null,
      },
    },
  ]);
  await expectEnsValues(page, { Integritat: 'Alt' });

  await startEditing(page);
  await expect(addRiskButton(page)).toBeEnabled();
  await expect(addMeasureButton(page)).toBeEnabled();
});

// The back-end keeps one classification per application; two only come from inconsistent data,
// so the real listing is answered with its classification twice.
test('locks the ENS classification when the back-end returns more than one', async ({
  page,
  disposableApplication,
}, testInfo) => {
  await createClassification(page, disposableApplication);
  await page.route(
    (url) => url.pathname === `${ENS_API}/${disposableApplication.appSecurityId}`,
    async (route) => {
      if (route.request().method() !== 'GET') return route.fallback();
      const response = await route.fetch();
      const body = (await response.json()) as { content: Array<{ id: number }> };
      body.content = [...body.content, ...body.content.map((row) => ({ ...row, id: row.id + 1 }))];
      return route.fulfill({ response, json: body });
    },
  );
  const tab = await openSecurityTab(page, disposableApplication);

  const alert = ensRegion(page).getByRole('alert');
  await expect(alert).toContainText('Dades ENS inconsistents');
  await expect(alert).toContainText(
    "S'ha trobat més d'una classificació ENS activa. L'edició d'aquest formulari està bloquejada fins que backend corregeixi les dades.",
  );
  await expectEnsValues(page, { "Grau d'adequació a l'ENS": '-', Disponibilitat: '-' });

  await startEditing(page);
  for (const id of [
    'application-security-overall-grade',
    'application-security-identity-provider',
    'application-security-ens-subject',
    'application-security-personal-data',
    'application-security-confidentiality',
    'application-security-integrity',
    'application-security-traceability',
    'application-security-availability',
    'application-security-authenticity',
  ]) {
    await expect(page.locator(`#${id}`), id).toHaveAttribute('aria-disabled', 'true');
  }
  await expect(page.locator('#application-security-approval-date')).toBeDisabled();
  await ensRegion(page).screenshot({ path: testInfo.outputPath('ens-inconsistent.png') });

  // The observations stay editable and are saved without touching the classifications.
  await observations(page).fill('Pendent de backend');
  await saveButton(page).click();
  await expect(page.getByText(SAVED_SECTION)).toBeVisible();
  expect(tab.writes).toEqual([
    {
      method: 'PUT',
      path: `/application/security/${disposableApplication.appSecurityId}`,
      body: { applicationId: disposableApplication.id, observation: '<p>Pendent de backend</p>' },
    },
  ]);
});

test('rejects observations over 4000 bytes until they are shortened', async ({
  page,
  disposableApplication,
}) => {
  const tab = await openSecurityTab(page, disposableApplication);
  await startEditing(page);

  await observations(page).fill(OVERSIZED_TEXT);
  const error = page.locator('.application-security__observations').getByRole('alert');
  await expect(error).toHaveText(MAX_BYTES_ERROR);
  await expect(observations(page)).toHaveAttribute('aria-invalid', 'true');
  await expect(observations(page)).toHaveAttribute(
    'aria-describedby',
    'application-security-observation-error',
  );
  await saveButton(page).click();
  await expect(saveButton(page)).toBeVisible();
  expect(tab.writes).toEqual([]);

  const shortened = '€'.repeat(1300);
  await observations(page).fill(shortened);
  await expect(error).toHaveCount(0);
  await saveButton(page).click();
  await expect(page.getByText(SAVED_SECTION)).toBeVisible();
  expect(tab.writes).toEqual([
    {
      method: 'PUT',
      path: `/application/security/${disposableApplication.appSecurityId}`,
      body: { applicationId: disposableApplication.id, observation: `<p>${shortened}</p>` },
    },
  ]);
});

test('creates, consults and deactivates a security risk', async ({
  page,
  disposableApplication,
}, testInfo) => {
  const level = await levels(page);
  const field = await catalogIds(page, 'field', ['Educació i Formació', 'Salut Pública']);
  await expectApiJsonResponse(
    await page.request.post(apiUrl(page, `${SECURITY_API}/risk`), {
      data: {
        appSecurityId: disposableApplication.appSecurityId,
        levelId: level['Baix'],
        fieldId: field['Educació i Formació'],
        description: 'Pèrdua de disponibilitat',
      },
    }),
    `POST ${SECURITY_API}/risk`,
  );
  const tab = await openSecurityTab(page, disposableApplication);
  const rows = risksRegion(page).locator('tbody tr');
  await expect(rows).toHaveCount(1);
  await expect(risksRegion(page).locator('thead th')).toHaveText(['Nivell', 'Descripció', 'Àmbit']);
  await expect(addRiskButton(page)).toHaveCount(0);
  await expect(risksRegion(page).getByRole('button', { name: ROW_ACTIONS })).toHaveCount(0);

  await startEditing(page);
  await addRiskButton(page).click();
  const create = dialogTitled(page, 'Afegeix un risc de seguretat');
  await expect(create).toBeVisible();
  await selectOption(page, 'application-security-dialog-level', 'Alt');
  await selectOption(page, 'application-security-dialog-risk-field', 'Salut Pública');
  const description = create.getByLabel('Descripció');
  await description.fill(OVERSIZED_TEXT);
  await expect(create.getByRole('alert')).toHaveText(MAX_BYTES_ERROR);
  await expect(description).toHaveAttribute('aria-invalid', 'true');
  await create.getByRole('button', { name: 'Afegeix el registre', exact: true }).click();
  await expect(create).toBeVisible();
  expect(tab.writes).toEqual([]);
  await create.screenshot({ path: testInfo.outputPath('risk-create-error.png') });

  const text = 'Accés no autoritzat a dades de salut';
  await description.fill(text);
  await expect(create.getByRole('alert')).toHaveCount(0);
  const createPromise = waitForApiResponse(page, 'POST', `${SECURITY_API}/risk`);
  await create.getByRole('button', { name: 'Afegeix el registre', exact: true }).click();
  const risk = await expectJsonResponse<{ id: number }>(await createPromise);
  await expect(create).toBeHidden();
  await expect(page.getByText("El registre s'ha desat correctament.")).toBeVisible();
  expect(tab.writes).toEqual([
    {
      method: 'POST',
      path: '/application/security/risk',
      body: {
        appSecurityId: disposableApplication.appSecurityId,
        levelId: level['Alt'],
        fieldId: field['Salut Pública'],
        description: text,
      },
    },
  ]);
  await expect(rows).toHaveCount(2);
  const created = rows.filter({ hasText: text });
  await expect(created.locator('td')).toHaveText(['Alt', text, 'Salut Pública', '']);

  await created.dblclick();
  const consult = dialogTitled(page, 'Consulta el risc de seguretat');
  await expect(consult.getByLabel('Nivell')).toHaveText('Alt');
  await expect(consult.getByLabel('Àmbit')).toHaveText('Salut Pública');
  await expect(consult.getByLabel('Descripció')).toHaveValue(text);
  await expect(consult.getByLabel('Descripció')).toHaveAttribute('readonly', '');
  await expect(consult.getByRole('button', { name: 'Edita el registre' })).toBeVisible();
  await consult.getByRole('button', { name: 'Tanca el diàleg' }).click();
  await expect(consult).toBeHidden();

  await created.getByRole('button', { name: ROW_ACTIONS }).click();
  await page.getByRole('menuitem', { name: 'Dona de baixa' }).click();
  await page.getByRole('button', { name: 'Confirma la baixa del registre' }).click();
  await expect(page.getByText("El registre s'ha donat de baixa correctament.")).toBeVisible();
  expect(tab.writes.at(-1)).toEqual({
    method: 'DELETE',
    path: `/application/security/risk/${risk.id}`,
    body: undefined,
  });
  await expect(rows).toHaveCount(1);
  await expect(rows.first()).toContainText('Pèrdua de disponibilitat');
});

test('creates, consults and deactivates an applied security measure', async ({
  page,
  disposableApplication,
}) => {
  const type = await catalogIds(page, 'security-measure-type', ['Xifratge']);
  const requirement = await catalogIds(page, 'ens-requirement', ['Categoria Alta']);
  const tab = await openSecurityTab(page, disposableApplication);
  const rows = measuresRegion(page).locator('tbody tr');
  await expect(rows).toHaveText(["No s'han trobat resultats"]);

  await startEditing(page);
  await addMeasureButton(page).click();
  const create = dialogTitled(page, 'Afegeix una mesura de seguretat');
  await selectOption(page, 'application-security-dialog-measure-type', 'Xifratge');
  await selectOption(page, 'application-security-dialog-ens-requirement', 'Categoria Alta');
  const description = create.getByLabel('Descripció');
  await description.fill(OVERSIZED_TEXT);
  await expect(create.getByRole('alert')).toHaveText(MAX_BYTES_ERROR);
  await description.fill('  Xifratge TLS 1.3  ');
  const createPromise = waitForApiResponse(page, 'POST', `${SECURITY_API}/measure`);
  await create.getByRole('button', { name: 'Afegeix el registre', exact: true }).click();
  const measure = await expectJsonResponse<{ id: number }>(await createPromise);
  await expect(create).toBeHidden();
  expect(tab.writes).toEqual([
    {
      method: 'POST',
      path: '/application/security/measure',
      body: {
        appSecurityId: disposableApplication.appSecurityId,
        typeId: type['Xifratge'],
        ensRequirementId: requirement['Categoria Alta'],
        description: 'Xifratge TLS 1.3',
      },
    },
  ]);
  await expect(rows).toHaveCount(1);
  await expect(rows.first().locator('td')).toHaveText([
    'Xifratge',
    'Categoria Alta',
    'Xifratge TLS 1.3',
    '',
  ]);

  await rows.first().focus();
  await page.keyboard.press('Enter');
  const consult = dialogTitled(page, 'Consulta la mesura de seguretat');
  await expect(consult.getByLabel('Tipus')).toHaveText('Xifratge');
  await expect(consult.getByLabel('Requisit ENS')).toHaveText('Categoria Alta');
  await expect(consult.getByLabel('Descripció')).toHaveValue('Xifratge TLS 1.3');
  await consult.getByRole('button', { name: 'Edita el registre' }).click();
  const edit = dialogTitled(page, 'Edita la mesura de seguretat');
  await edit.getByRole('button', { name: 'Dona de baixa el registre' }).click();
  const confirmation = dialogTitled(page, 'Dona de baixa el registre?');
  await confirmation.getByRole('button', { name: 'Confirma la baixa del registre' }).click();
  await expect(confirmation).toBeHidden();
  await expect(edit).toBeHidden();
  expect(tab.writes.at(-1)).toEqual({
    method: 'DELETE',
    path: `/application/security/measure/${measure.id}`,
    body: undefined,
  });
  await expect(rows).toHaveText(["No s'han trobat resultats"]);
});

test('tells that the security documentation is not available yet', async ({
  page,
  disposableApplication,
}) => {
  await openSecurityTab(page, disposableApplication);

  await page.getByRole('button', { name: 'Obre la documentació de seguretat' }).click();
  await expect(page.getByText('Funcionalitat pendent')).toBeVisible();
  await expect(
    page.getByText('La documentació de seguretat encara no està disponible.'),
  ).toBeVisible();
});
