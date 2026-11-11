import type { Page } from '@playwright/test';

import {
  assignRoles,
  borrowRolePrefix,
  manualValidationLabel,
  OTHER_PREFIXED_ROLE,
  OTHER_SYSTEM_ROLE,
  PREFIXED_ROLE,
  type RealSoffidRole,
  recordSecurityRequests,
  ROLE_PREFIX,
  signedInUsername,
  WEBLOGIC_ROLE,
} from './support/application-security';
import { type DisposableApplication, expect, test } from './support/application.fixture';
import { apiUrl } from './support/http';

const PREFIX_NOTE = `Aquest rol no comença per «${ROLE_PREFIX}_». Es desarà com a validat manualment al teu nom.`;
const ROW_ACTIONS = 'Obre les accions del registre';

const rolesRegion = (page: Page) => page.getByRole('region', { name: "Rols de l'aplicació" });
const roleRows = (page: Page) => rolesRegion(page).locator('tbody tr');
const rowWith = (page: Page, code: string) => roleRows(page).filter({ hasText: code });
const filtersButton = (page: Page) =>
  page.getByRole('button', { name: /^Mostra o oculta els filtres de rols/ });
const filtersBadge = (page: Page) => rolesRegion(page).locator('.p-badge');
const addRoleButton = (page: Page) =>
  page.getByRole('button', { name: "Afegeix un rol de l'aplicació", exact: true });
const rowMenuItems = (page: Page) => page.locator('.p-menu-overlay').getByRole('menuitem');
// The shared CRUD shell does not name its dialog yet, so it is located by its visible heading.
const dialogTitled = (page: Page, title: string) =>
  page.getByRole('dialog').filter({ has: page.getByRole('heading', { name: title }) });
// Options are named after the code and show "<system> · <description>" below it; the same code
// can exist in several systems (weblogic and weblogic-pre).
const roleOption = (page: Page, role: RealSoffidRole) =>
  page
    .getByRole('option', { name: role.name, exact: true })
    .filter({ hasText: new RegExp(`${role.system}(?!-)`) });

async function openSecurityTab(page: Page, application: DisposableApplication) {
  const recorded = recordSecurityRequests(page);
  await page.goto(`/aplicacions/${application.id}/security`);
  return recorded;
}

async function startEditing(page: Page): Promise<void> {
  await page.getByRole('button', { name: 'Editar Seguretat', exact: true }).click();
  await expect(page.getByRole('button', { name: 'Desar els canvis de Seguretat' })).toBeVisible();
}

async function openRowMenu(page: Page, code: string): Promise<void> {
  await rowWith(page, code).getByRole('button', { name: ROW_ACTIONS }).click();
}

// Filters the Soffid roles of the role picker by code and picks one.
async function pickRole(page: Page, role: RealSoffidRole): Promise<void> {
  await page.locator('#application-security-dialog-role').click();
  const search = page.waitForResponse(
    (response) =>
      new URL(response.url()).pathname === '/invaiback/security-role/soffid-search' &&
      new URL(response.url()).searchParams.get('name') === role.name,
  );
  await page.getByRole('searchbox', { name: 'Codi' }).fill(role.name);
  await search;
  await roleOption(page, role).click();
}

test('loads the weblogic roles and filters them by quick search and the filters panel', async ({
  page,
  disposableApplication,
}, testInfo) => {
  await assignRoles(page, disposableApplication, [PREFIXED_ROLE, WEBLOGIC_ROLE, OTHER_SYSTEM_ROLE]);
  const tab = await openSecurityTab(page, disposableApplication);

  await expect(roleRows(page)).toHaveCount(2);
  await expect(rolesRegion(page).locator('thead th')).toHaveText(['Codi', 'Sistema', 'Descripció']);
  await expect(roleRows(page).nth(0)).toContainText(PREFIXED_ROLE.name);
  await expect(roleRows(page).nth(1)).toContainText(WEBLOGIC_ROLE.name);
  expect(tab.roleRequests).toHaveLength(1);
  expect(Object.fromEntries(tab.roleRequests[0].searchParams)).toEqual({
    page: '0',
    size: '10',
    sort: 'id,asc',
    statusId: '1',
    system: 'weblogic',
  });

  // The weblogic default counts as an applied filter while the panel stays collapsed.
  await expect(page.locator('app-search-filters')).toHaveCount(0);
  await expect(filtersButton(page)).toHaveAttribute('aria-expanded', 'false');
  await expect(filtersButton(page)).toHaveAccessibleName(
    'Mostra o oculta els filtres de rols (1 filtre aplicat)',
  );
  await expect(filtersBadge(page)).toHaveText('1');

  await filtersButton(page).click();
  await expect(filtersButton(page)).toHaveAttribute('aria-expanded', 'true');
  const filters = page.locator('app-search-filters');
  const code = filters.getByLabel('Codi');
  const system = filters.getByLabel('Sistema');
  await expect(code).toHaveValue('');
  await expect(system).toHaveValue('weblogic');
  await code.fill('INV_');
  await system.fill(OTHER_SYSTEM_ROLE.system);
  await expect(filtersButton(page)).toHaveAccessibleName(
    'Mostra o oculta els filtres de rols (2 filtres aplicats)',
  );
  await expect(filtersBadge(page)).toHaveText('2');
  expect(tab.roleRequests).toHaveLength(1);
  await page.screenshot({ path: testInfo.outputPath('roles-filters.png') });

  await filters.getByRole('button', { name: 'Cerca' }).click();
  await expect(roleRows(page)).toHaveCount(1);
  await expect(roleRows(page).first()).toContainText(OTHER_SYSTEM_ROLE.name);
  const applied = tab.roleRequests.at(-1)!.searchParams;
  expect(applied.get('page')).toBe('0');
  expect(applied.get('securityRoleName')).toBe('INV_');
  expect(applied.get('system')).toBe(OTHER_SYSTEM_ROLE.system);

  await filters.getByRole('button', { name: 'Neteja' }).click();
  await expect(code).toHaveValue('');
  await expect(system).toHaveValue('weblogic');
  await expect(filtersBadge(page)).toHaveText('1');
  await expect(roleRows(page)).toHaveCount(2);

  const quickSearch = page.getByRole('textbox', {
    name: "Cerca rols de l'aplicació per codi o descripció",
  });
  await expect(quickSearch).toHaveAttribute('placeholder', 'Cerca per codi o descripció');
  await quickSearch.pressSequentially('centres');
  await expect(roleRows(page)).toHaveCount(1);
  await expect(roleRows(page).first()).toContainText(PREFIXED_ROLE.name);
  // One debounced request for the whole word, combined with the restored weblogic default.
  const searches = tab.roleRequests.filter((url) => url.searchParams.has('search'));
  expect(searches).toHaveLength(1);
  expect(searches[0].searchParams.get('search')).toBe('centres');
  expect(searches[0].searchParams.get('system')).toBe('weblogic');
  expect(searches[0].searchParams.has('securityRoleName')).toBe(false);
  expect(searches[0].searchParams.get('page')).toBe('0');

  await system.fill('');
  await expect(filtersButton(page)).toHaveAccessibleName('Mostra o oculta els filtres de rols');
  await expect(filtersBadge(page)).toBeHidden();
  await filters.getByRole('button', { name: 'Cerca' }).click();
  await expect.poll(() => tab.roleRequests.at(-1)?.searchParams.has('system')).toBe(false);
  expect(tab.roleRequests.at(-1)?.searchParams.get('search')).toBe('centres');

  await page.getByRole('button', { name: 'Esborra la cerca de rols' }).click();
  await expect(roleRows(page)).toHaveCount(3);
  expect(tab.roleRequests.at(-1)?.searchParams.has('search')).toBe(false);
  expect(tab.writes).toEqual([]);
});

test('adds a Soffid role and warns when it does not start with the application prefix', async ({
  page,
  cleanupRegistry,
  disposableApplication,
}, testInfo) => {
  await borrowRolePrefix(page, cleanupRegistry, disposableApplication);
  const username = await signedInUsername(page);
  const tab = await openSecurityTab(page, disposableApplication);
  await expect(roleRows(page)).toHaveText(["No s'han trobat resultats"]);
  await expect(addRoleButton(page)).toHaveCount(0);
  await startEditing(page);

  await addRoleButton(page).click();
  const dialog = dialogTitled(page, "Afegeix un rol de l'aplicació");
  await expect(dialog).toBeVisible();
  await dialog.getByRole('button', { name: 'Afegeix el registre', exact: true }).click();
  await expect(dialog.getByRole('alert')).toHaveText('Selecciona un rol de Soffid.');
  expect(tab.writes).toEqual([]);

  const prefixNote = page.locator('#application-security-dialog-role-prefix-note');
  await pickRole(page, PREFIXED_ROLE);
  expect(Object.fromEntries(tab.soffidRequests[0].searchParams)).toEqual({
    page: '0',
    size: '20',
  });
  await expect(dialog.getByRole('alert')).toHaveCount(0);
  await expect(prefixNote).toHaveText('');
  await expect(dialog.getByLabel('Sistema')).toHaveText(PREFIXED_ROLE.system);
  await expect(dialog.getByLabel('Descripció')).toHaveText(PREFIXED_ROLE.description);

  await pickRole(page, WEBLOGIC_ROLE);
  await expect(prefixNote).toHaveText(PREFIX_NOTE);
  await expect(dialog.getByLabel('Descripció')).toHaveText(WEBLOGIC_ROLE.description);
  await dialog.screenshot({ path: testInfo.outputPath('roles-create-prefix-note.png') });

  await dialog.getByRole('button', { name: 'Afegeix el registre', exact: true }).click();
  await expect(dialog).toBeHidden();
  expect(tab.writes).toEqual([
    {
      method: 'POST',
      path: '/application/security/role',
      body: {
        appSecurityId: disposableApplication.appSecurityId,
        securityRoleId: WEBLOGIC_ROLE.roleId,
      },
    },
  ]);
  await expect(page.getByText("El registre s'ha desat correctament.")).toBeVisible();
  await expect(roleRows(page)).toHaveCount(1);
  await expect(
    rowWith(page, WEBLOGIC_ROLE.name).getByRole('img', { name: manualValidationLabel(username) }),
  ).toBeVisible();
});

test('consults a role, then changes and deactivates it while editing', async ({
  page,
  disposableApplication,
}) => {
  const [assigned] = await assignRoles(page, disposableApplication, [PREFIXED_ROLE]);
  const tab = await openSecurityTab(page, disposableApplication);
  await expect(roleRows(page)).toHaveCount(1);

  await rowWith(page, PREFIXED_ROLE.name).dblclick();
  const consult = dialogTitled(page, "Consulta el rol de l'aplicació");
  await expect(consult).toBeVisible();
  await expect(consult.getByLabel('Codi')).toHaveText(PREFIXED_ROLE.name);
  await expect(consult.getByLabel('Sistema')).toHaveText(PREFIXED_ROLE.system);
  await expect(consult.getByLabel('Descripció')).toHaveText(PREFIXED_ROLE.description);
  await expect(consult.getByRole('button', { name: 'Edita el registre' })).toHaveCount(0);
  await consult.getByRole('button', { name: 'Accepta i tanca el diàleg' }).click();
  await expect(consult).toBeHidden();

  await startEditing(page);
  await rowWith(page, PREFIXED_ROLE.name).focus();
  await page.keyboard.press('Enter');
  await expect(consult).toBeVisible();
  await consult.getByRole('button', { name: 'Edita el registre' }).click();
  const edit = dialogTitled(page, "Edita el rol de l'aplicació");
  await expect(edit).toBeVisible();
  await edit.getByRole('button', { name: 'Cancel·la els canvis' }).click();
  await expect(consult).toBeVisible();
  await consult.getByRole('button', { name: 'Tanca el diàleg' }).click();
  await expect(consult).toBeHidden();

  await openRowMenu(page, PREFIXED_ROLE.name);
  await expect(rowMenuItems(page)).toHaveText(['Consulta', 'Edita', 'Dona de baixa']);
  await page.getByRole('menuitem', { name: 'Edita' }).click();
  await expect(edit).toBeVisible();
  await pickRole(page, OTHER_PREFIXED_ROLE);
  await edit.getByRole('button', { name: 'Desa el registre', exact: true }).click();
  await expect(edit).toBeHidden();
  await expect(rowWith(page, OTHER_PREFIXED_ROLE.name)).toHaveCount(1);
  expect(tab.writes).toEqual([
    {
      method: 'PUT',
      path: `/application/security/role/${assigned.id}`,
      body: {
        appSecurityId: disposableApplication.appSecurityId,
        securityRoleId: OTHER_PREFIXED_ROLE.roleId,
      },
    },
  ]);

  await openRowMenu(page, OTHER_PREFIXED_ROLE.name);
  await page.getByRole('menuitem', { name: 'Dona de baixa' }).click();
  const confirmation = dialogTitled(page, 'Dona de baixa el registre?');
  await expect(confirmation).toContainText(
    "El registre deixarà d'aparèixer a la taula de registres actius.",
  );
  await confirmation.getByRole('button', { name: 'Confirma la baixa del registre' }).click();
  await expect(confirmation).toBeHidden();
  await expect(page.getByText("El registre s'ha donat de baixa correctament.")).toBeVisible();
  await expect(roleRows(page)).toHaveText(["No s'han trobat resultats"]);
  expect(tab.writes.at(-1)).toEqual({
    method: 'DELETE',
    path: `/application/security/role/${assigned.id}`,
    body: undefined,
  });
});

// The back-end only saves a role NOT_VALIDATED when the prefix of its application changes, and no
// longer recalculates them then, so "Validar manualment" cannot be reached: the action, its
// confirmation and the focus afterwards are covered by the unit tests of ApplicationSecuritySection
// and ApplicationSecurityResourceTable.
test('marks the roles validated by hand and offers no manual validation for them', async ({
  page,
  cleanupRegistry,
  disposableApplication,
}, testInfo) => {
  await borrowRolePrefix(page, cleanupRegistry, disposableApplication);
  const username = await signedInUsername(page);
  const assigned = await assignRoles(page, disposableApplication, [PREFIXED_ROLE, WEBLOGIC_ROLE]);
  expect(assigned.map(({ validationStatus }) => validationStatus)).toEqual(['VALIDATED', 'MANUAL']);
  await openSecurityTab(page, disposableApplication);
  await expect(roleRows(page)).toHaveCount(2);

  await expect(rowWith(page, PREFIXED_ROLE.name).getByRole('img')).toHaveCount(0);
  const manual = rowWith(page, WEBLOGIC_ROLE.name).getByRole('img', {
    name: manualValidationLabel(username),
  });
  await expect(manual.locator('i')).toHaveClass(/pi-check-circle/);
  await manual.focus();
  await expect(page.getByRole('tooltip')).toHaveText(manualValidationLabel(username));
  await manual.press('Escape');
  // Outside edit mode nothing can be changed: there is no row menu at all.
  await expect(rolesRegion(page).getByRole('button', { name: ROW_ACTIONS })).toHaveCount(0);
  await rolesRegion(page).screenshot({ path: testInfo.outputPath('roles-validation-states.png') });

  await startEditing(page);
  for (const role of [PREFIXED_ROLE, WEBLOGIC_ROLE]) {
    await openRowMenu(page, role.name);
    await expect(rowMenuItems(page)).toHaveText(['Consulta', 'Edita', 'Dona de baixa']);
    await page.keyboard.press('Escape');
  }
});

test('keeps the roles read-only for an inactive application', async ({
  page,
  disposableApplication,
}) => {
  const username = await signedInUsername(page);
  await assignRoles(page, disposableApplication, [PREFIXED_ROLE, WEBLOGIC_ROLE]);
  const deactivated = await page.request.delete(
    apiUrl(page, `/invaiback/application/${disposableApplication.id}`),
  );
  expect(deactivated.ok(), await deactivated.text()).toBeTruthy();
  await openSecurityTab(page, disposableApplication);
  await expect(roleRows(page)).toHaveCount(2);

  await expect(page.getByRole('button', { name: 'Editar Seguretat', exact: true })).toHaveCount(0);
  await expect(addRoleButton(page)).toHaveCount(0);
  await expect(rolesRegion(page).getByRole('button', { name: ROW_ACTIONS })).toHaveCount(0);
  await expect(
    rowWith(page, WEBLOGIC_ROLE.name).getByRole('img', { name: manualValidationLabel(username) }),
  ).toBeVisible();

  await rowWith(page, WEBLOGIC_ROLE.name).dblclick();
  const dialog = dialogTitled(page, "Consulta el rol de l'aplicació");
  await expect(dialog.getByLabel('Codi')).toHaveText(WEBLOGIC_ROLE.name);
  await expect(dialog.getByRole('button', { name: 'Edita el registre' })).toHaveCount(0);
  await expect(dialog.getByRole('button', { name: 'Accepta i tanca el diàleg' })).toBeVisible();
});

// The Seguretat record is created with its application; one without it only exists among those
// created before the record did. Adding roles without it is covered by the unit tests of
// ApplicationSecuritySection.

test('reflows the roles toolbar at 320 px without horizontal scrolling', async ({
  page,
  disposableApplication,
}, testInfo) => {
  await page.setViewportSize({ width: 320, height: 800 });
  await assignRoles(page, disposableApplication, [PREFIXED_ROLE, WEBLOGIC_ROLE]);
  await openSecurityTab(page, disposableApplication);
  await expect(roleRows(page)).toHaveCount(2);

  const toolbar = page.locator('.application-security__resource-header--toolbar');
  await expect(
    toolbar.getByRole('textbox', { name: "Cerca rols de l'aplicació per codi o descripció" }),
  ).toBeVisible();
  await expect(filtersButton(page)).toBeVisible();
  const bounds = await toolbar.boundingBox();
  expect(bounds!.x).toBeGreaterThanOrEqual(0);
  expect(bounds!.x + bounds!.width).toBeLessThanOrEqual(320);
  const pageOverflow = await page.evaluate(
    () => document.documentElement.scrollWidth - document.documentElement.clientWidth,
  );
  expect(pageOverflow).toBeLessThanOrEqual(0);
  await page.screenshot({ path: testInfo.outputPath('roles-mobile.png'), fullPage: true });
});
