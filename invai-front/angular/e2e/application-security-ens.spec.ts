import { expect, test, type Page } from '@playwright/test';

import { ensClassification, openSecurityTab, riskRow } from './support/application-security-mocks';
import { selectOption } from './support/ui';

test.use({ storageState: { cookies: [], origins: [] } });

const MAX_BYTES_ERROR =
  'El text supera la mida màxima permesa. Redueix-lo (els accents i el format també ocupen espai).';
// 1400 characters, but 4200 UTF-8 bytes: over the 4000-byte limit of the back-end columns.
const OVERSIZED_TEXT = '€'.repeat(1400);
const SAVED_SECTION = "S'han desat correctament els canvis de Seguretat.";
const ROW_ACTIONS = 'Obre les accions del registre';

const CLASSIFICATION = ensClassification(
  31,
  {
    overallGradeId: 2,
    identityProviderId: 4,
    ensSubjectId: 6,
    personalDataProcessingId: 8,
    confidentialityId: 1,
    integrityId: 2,
    traceabilityId: 2,
    availabilityId: 3,
    authenticityId: 1,
  },
  '2026-03-05T00:00:00',
);

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
}, testInfo) => {
  const mock = await openSecurityTab(page, 'ens-test', { classifications: [CLASSIFICATION] });

  await expect(ensRegion(page).getByRole('alert')).toHaveCount(0);
  await expect(ensRegion(page).getByRole('group', { name: 'Dimensions ENS' })).toBeVisible();
  await expectEnsValues(page, {
    "Grau d'adequació a l'ENS": 'Mitjà',
    "Proveïdor d'identitat": 'Cl@ve',
    "Subjecció a l'ENS": 'Subjecta',
    'Tractament de dades personals': 'Amb dades personals',
    "Data d'aprovació": '5/3/2026',
    Confidencialitat: 'Baix',
    Integritat: 'Mitjà',
    Traçabilitat: 'Mitjà',
    Disponibilitat: 'Alt',
    Autenticitat: 'Baix',
  });

  await startEditing(page);
  await selectOption(page, 'application-security-overall-grade', 'Alt');
  await selectOption(page, 'application-security-identity-provider', 'Seycon');
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
  expect(mock.writes).toEqual([
    {
      method: 'PUT',
      path: '/application/security/9',
      body: { applicationId: 1, observation: '<p>Revisat</p>' },
    },
    {
      method: 'PUT',
      path: '/application/security/ens-classification/31',
      body: {
        appSecurityId: 9,
        identityProviderId: 5,
        ensSubjectId: 6,
        personalDataProcessingId: 8,
        approvalDate: '2026-09-15T00:00:00',
        confidentialityId: 1,
        integrityId: 2,
        traceabilityId: 2,
        availabilityId: 1,
        authenticityId: 1,
        overallGradeId: 3,
      },
    },
  ]);
  await expect(page.getByRole('button', { name: 'Editar Seguretat', exact: true })).toBeVisible();
  await expectEnsValues(page, {
    "Grau d'adequació a l'ENS": 'Alt',
    "Proveïdor d'identitat": 'Seycon',
    "Data d'aprovació": '15/9/2026',
    Disponibilitat: 'Baix',
  });
  await expect(page.locator('.application-security__observations')).toContainText('Revisat');
});

test('creates the Seguretat data and its ENS classification on the first save', async ({
  page,
}) => {
  const mock = await openSecurityTab(page, 'ens-test', { appSecurityId: null });
  await expectEnsValues(page, { "Grau d'adequació a l'ENS": '-', Integritat: '-' });

  await startEditing(page);
  // Risks and measures hang from the Seguretat record, which does not exist yet.
  await expect(addRiskButton(page)).toBeDisabled();
  await expect(addMeasureButton(page)).toBeDisabled();
  await selectOption(page, 'application-security-integrity', 'Alt');
  await saveButton(page).click();

  await expect(page.getByText(SAVED_SECTION)).toBeVisible();
  expect(mock.writes).toEqual([
    { method: 'POST', path: '/application/security', body: { applicationId: 1, observation: '' } },
    {
      method: 'POST',
      path: '/application/security/ens-classification',
      body: {
        appSecurityId: 9,
        identityProviderId: null,
        ensSubjectId: null,
        personalDataProcessingId: null,
        approvalDate: null,
        confidentialityId: null,
        integrityId: 3,
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

test('locks the ENS classification when the back-end returns more than one', async ({
  page,
}, testInfo) => {
  const mock = await openSecurityTab(page, 'ens-test', {
    classifications: [CLASSIFICATION, { ...CLASSIFICATION, id: 32 }],
  });

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
  expect(mock.writes).toEqual([
    {
      method: 'PUT',
      path: '/application/security/9',
      body: { applicationId: 1, observation: '<p>Pendent de backend</p>' },
    },
  ]);
});

test('rejects observations over 4000 bytes until they are shortened', async ({ page }) => {
  const mock = await openSecurityTab(page, 'ens-test');
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
  expect(mock.writes).toEqual([]);

  const shortened = '€'.repeat(1300);
  await observations(page).fill(shortened);
  await expect(error).toHaveCount(0);
  await saveButton(page).click();
  await expect(page.getByText(SAVED_SECTION)).toBeVisible();
  expect(mock.writes).toEqual([
    {
      method: 'PUT',
      path: '/application/security/9',
      body: { applicationId: 1, observation: `<p>${shortened}</p>` },
    },
  ]);
});

test('creates, consults and deactivates a security risk', async ({ page }, testInfo) => {
  const mock = await openSecurityTab(page, 'ens-test', {
    risks: [riskRow(51, 1, 22, 'Pèrdua de disponibilitat')],
  });
  const rows = risksRegion(page).locator('tbody tr');
  await expect(rows).toHaveCount(1);
  await expect(risksRegion(page).locator('thead th')).toHaveText([
    'Nivell',
    'Descripció',
    'Àmbit',
  ]);
  await expect(addRiskButton(page)).toHaveCount(0);
  await expect(risksRegion(page).getByRole('button', { name: ROW_ACTIONS })).toHaveCount(0);

  await startEditing(page);
  await addRiskButton(page).click();
  const create = dialogTitled(page, 'Afegeix un risc de seguretat');
  await expect(create).toBeVisible();
  await selectOption(page, 'application-security-dialog-level', 'Alt');
  await selectOption(page, 'application-security-dialog-risk-field', 'Salut');
  const description = create.getByLabel('Descripció');
  await description.fill(OVERSIZED_TEXT);
  await expect(create.getByRole('alert')).toHaveText(MAX_BYTES_ERROR);
  await expect(description).toHaveAttribute('aria-invalid', 'true');
  await create.getByRole('button', { name: 'Afegeix el registre', exact: true }).click();
  await expect(create).toBeVisible();
  expect(mock.writes).toEqual([]);
  await create.screenshot({ path: testInfo.outputPath('risk-create-error.png') });

  const text = 'Accés no autoritzat a dades de salut';
  await description.fill(text);
  await expect(create.getByRole('alert')).toHaveCount(0);
  await create.getByRole('button', { name: 'Afegeix el registre', exact: true }).click();
  await expect(create).toBeHidden();
  await expect(page.getByText("El registre s'ha desat correctament.")).toBeVisible();
  expect(mock.writes).toEqual([
    {
      method: 'POST',
      path: '/application/security/risk',
      body: { appSecurityId: 9, levelId: 3, fieldId: 21, description: text },
    },
  ]);
  await expect(rows).toHaveCount(2);
  const created = rows.filter({ hasText: text });
  await expect(created.locator('td')).toHaveText(['Alt', text, 'Salut', '']);

  await created.dblclick();
  const consult = dialogTitled(page, 'Consulta el risc de seguretat');
  await expect(consult.getByLabel('Nivell')).toHaveText('Alt');
  await expect(consult.getByLabel('Àmbit')).toHaveText('Salut');
  await expect(consult.getByLabel('Descripció')).toHaveValue(text);
  await expect(consult.getByLabel('Descripció')).toHaveAttribute('readonly', '');
  await expect(consult.getByRole('button', { name: 'Edita el registre' })).toBeVisible();
  await consult.getByRole('button', { name: 'Tanca el diàleg' }).click();
  await expect(consult).toBeHidden();

  await created.getByRole('button', { name: ROW_ACTIONS }).click();
  await page.getByRole('menuitem', { name: 'Dona de baixa' }).click();
  await page.getByRole('button', { name: 'Confirma la baixa del registre' }).click();
  await expect(page.getByText("El registre s'ha donat de baixa correctament.")).toBeVisible();
  expect(mock.writes.at(-1)).toEqual({
    method: 'DELETE',
    path: '/application/security/risk/60',
    body: undefined,
  });
  await expect(rows).toHaveCount(1);
  await expect(rows.first()).toContainText('Pèrdua de disponibilitat');
});

test('creates, consults and deactivates an applied security measure', async ({ page }) => {
  const mock = await openSecurityTab(page, 'ens-test');
  const rows = measuresRegion(page).locator('tbody tr');
  await expect(rows).toHaveText(["No s'han trobat resultats"]);

  await startEditing(page);
  await addMeasureButton(page).click();
  const create = dialogTitled(page, 'Afegeix una mesura de seguretat');
  await selectOption(page, 'application-security-dialog-measure-type', 'Tècnica');
  await selectOption(page, 'application-security-dialog-ens-requirement', 'mp.info.3 Xifratge');
  const description = create.getByLabel('Descripció');
  await description.fill(OVERSIZED_TEXT);
  await expect(create.getByRole('alert')).toHaveText(MAX_BYTES_ERROR);
  await description.fill('  Xifratge TLS 1.3  ');
  await create.getByRole('button', { name: 'Afegeix el registre', exact: true }).click();
  await expect(create).toBeHidden();
  expect(mock.writes).toEqual([
    {
      method: 'POST',
      path: '/application/security/measure',
      body: { appSecurityId: 9, typeId: 32, ensRequirementId: 42, description: 'Xifratge TLS 1.3' },
    },
  ]);
  await expect(rows).toHaveCount(1);
  await expect(rows.first().locator('td')).toHaveText([
    'Tècnica',
    'mp.info.3 Xifratge',
    'Xifratge TLS 1.3',
    '',
  ]);

  await rows.first().focus();
  await page.keyboard.press('Enter');
  const consult = dialogTitled(page, 'Consulta la mesura de seguretat');
  await expect(consult.getByLabel('Tipus')).toHaveText('Tècnica');
  await expect(consult.getByLabel('Requisit ENS')).toHaveText('mp.info.3 Xifratge');
  await expect(consult.getByLabel('Descripció')).toHaveValue('Xifratge TLS 1.3');
  await consult.getByRole('button', { name: 'Edita el registre' }).click();
  const edit = dialogTitled(page, 'Edita la mesura de seguretat');
  await edit.getByRole('button', { name: 'Dona de baixa el registre' }).click();
  const confirmation = dialogTitled(page, 'Dona de baixa el registre?');
  await confirmation.getByRole('button', { name: 'Confirma la baixa del registre' }).click();
  await expect(confirmation).toBeHidden();
  await expect(edit).toBeHidden();
  expect(mock.writes.at(-1)).toEqual({
    method: 'DELETE',
    path: '/application/security/measure/60',
    body: undefined,
  });
  await expect(rows).toHaveText(["No s'han trobat resultats"]);
});

test('tells that the security documentation is not available yet', async ({ page }) => {
  await openSecurityTab(page, 'ens-test');

  await page.getByRole('button', { name: 'Obre la documentació de seguretat' }).click();
  await expect(page.getByText('Funcionalitat pendent')).toBeVisible();
  await expect(
    page.getByText('La documentació de seguretat encara no està disponible.'),
  ).toBeVisible();
});
