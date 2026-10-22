import { expect, test, type Page } from '@playwright/test';

import { openSecurityTab, roleRow } from './support/application-security-mocks';

test.use({ storageState: { cookies: [], origins: [] } });

const NOT_VALIDATED = "Rol no validat: el codi no comença pel prefix de l'aplicació.";
const PREFIX_NOTE =
  'Aquest rol no comença per «INV_». Es desarà com a validat manualment al teu nom.';
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

async function startEditing(page: Page): Promise<void> {
  await page.getByRole('button', { name: 'Editar Seguretat', exact: true }).click();
  await expect(page.getByRole('button', { name: 'Desar els canvis de Seguretat' })).toBeVisible();
}

async function openRowMenu(page: Page, code: string): Promise<void> {
  await rowWith(page, code).getByRole('button', { name: ROW_ACTIONS }).click();
}

test('loads the weblogic roles and filters them by quick search and the filters panel', async ({
  page,
}, testInfo) => {
  const mock = await openSecurityTab(page, 'roles-test');

  await expect(roleRows(page)).toHaveCount(2);
  await expect(rolesRegion(page).locator('thead th')).toHaveText([
    'Codi',
    'Sistema',
    'Descripció',
  ]);
  await expect(roleRows(page).nth(0)).toContainText('INV_ADMIN');
  await expect(roleRows(page).nth(1)).toContainText('ADM_ROOT');
  expect(mock.roleRequests).toHaveLength(1);
  expect(Object.fromEntries(mock.roleRequests[0].searchParams)).toEqual({
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
  await system.fill('SEYCON');
  await expect(filtersButton(page)).toHaveAccessibleName(
    'Mostra o oculta els filtres de rols (2 filtres aplicats)',
  );
  await expect(filtersBadge(page)).toHaveText('2');
  expect(mock.roleRequests).toHaveLength(1);
  await page.screenshot({ path: testInfo.outputPath('roles-filters.png') });

  await filters.getByRole('button', { name: 'Cerca' }).click();
  await expect(roleRows(page)).toHaveCount(1);
  await expect(roleRows(page).first()).toContainText('INV_CONSULTA');
  const applied = mock.roleRequests.at(-1)!.searchParams;
  expect(applied.get('page')).toBe('0');
  expect(applied.get('securityRoleName')).toBe('INV_');
  expect(applied.get('system')).toBe('SEYCON');

  await filters.getByRole('button', { name: 'Neteja' }).click();
  await expect(code).toHaveValue('');
  await expect(system).toHaveValue('weblogic');
  await expect(filtersBadge(page)).toHaveText('1');
  await expect(roleRows(page)).toHaveCount(2);

  const quickSearch = page.getByRole('textbox', {
    name: "Cerca rols de l'aplicació per codi o descripció",
  });
  await expect(quickSearch).toHaveAttribute('placeholder', 'Cerca per codi o descripció');
  await quickSearch.pressSequentially('root');
  await expect(roleRows(page)).toHaveCount(1);
  await expect(roleRows(page).first()).toContainText('ADM_ROOT');
  // One debounced request for the whole word, combined with the restored weblogic default.
  const searches = mock.roleRequests.filter((url) => url.searchParams.has('search'));
  expect(searches).toHaveLength(1);
  expect(searches[0].searchParams.get('search')).toBe('root');
  expect(searches[0].searchParams.get('system')).toBe('weblogic');
  expect(searches[0].searchParams.has('securityRoleName')).toBe(false);
  expect(searches[0].searchParams.get('page')).toBe('0');

  await system.fill('');
  await expect(filtersButton(page)).toHaveAccessibleName('Mostra o oculta els filtres de rols');
  await expect(filtersBadge(page)).toBeHidden();
  await filters.getByRole('button', { name: 'Cerca' }).click();
  await expect.poll(() => mock.roleRequests.at(-1)?.searchParams.has('system')).toBe(false);
  expect(mock.roleRequests.at(-1)?.searchParams.get('search')).toBe('root');

  await page.getByRole('button', { name: 'Esborra la cerca de rols' }).click();
  await expect(roleRows(page)).toHaveCount(3);
  expect(mock.roleRequests.at(-1)?.searchParams.has('search')).toBe(false);
  expect(mock.writes).toEqual([]);
});

test('adds a Soffid role and warns when it does not start with the application prefix', async ({
  page,
}, testInfo) => {
  const mock = await openSecurityTab(page, 'roles-test');
  await expect(roleRows(page)).toHaveCount(2);
  await expect(addRoleButton(page)).toHaveCount(0);
  await startEditing(page);

  await addRoleButton(page).click();
  const dialog = dialogTitled(page, "Afegeix un rol de l'aplicació");
  await expect(dialog).toBeVisible();
  await dialog.getByRole('button', { name: 'Afegeix el registre', exact: true }).click();
  await expect(dialog.getByRole('alert')).toHaveText('Selecciona un rol de Soffid.');
  expect(mock.writes).toEqual([]);

  const select = page.locator('#application-security-dialog-role');
  const prefixNote = page.locator('#application-security-dialog-role-prefix-note');
  await select.click();
  await expect(page.getByRole('option', { name: /^INV_USER/ })).toBeVisible();
  expect(Object.fromEntries(mock.soffidRequests[0].searchParams)).toEqual({
    page: '0',
    size: '20',
  });
  await page.getByRole('option', { name: /^INV_USER/ }).click();
  await expect(dialog.getByRole('alert')).toHaveCount(0);
  await expect(prefixNote).toHaveText('');
  await expect(dialog.getByLabel('Sistema')).toHaveText('weblogic');
  await expect(dialog.getByLabel('Descripció')).toHaveText('Usuari');

  await select.click();
  await page.getByRole('searchbox', { name: 'Codi' }).fill('adm');
  await expect.poll(() => mock.soffidRequests.at(-1)?.searchParams.get('name')).toBe('adm');
  await expect(page.getByRole('option', { name: /^INV_GESTOR/ })).toHaveCount(0);
  await page.getByRole('option', { name: /^ADM_AUDIT/ }).click();
  await expect(prefixNote).toHaveText(PREFIX_NOTE);
  await expect(dialog.getByLabel('Descripció')).toHaveText('Auditoria');
  await dialog.screenshot({ path: testInfo.outputPath('roles-create-prefix-note.png') });

  await dialog.getByRole('button', { name: 'Afegeix el registre', exact: true }).click();
  await expect(dialog).toBeHidden();
  expect(mock.writes).toEqual([
    {
      method: 'POST',
      path: '/application/security/role',
      body: { appSecurityId: 9, securityRoleId: 504 },
    },
  ]);
  await expect(page.getByText("El registre s'ha desat correctament.")).toBeVisible();
  await expect(roleRows(page)).toHaveCount(3);
  await expect(
    rowWith(page, 'ADM_AUDIT').getByRole('img', {
      name: 'Rol validat manualment per roles-test el 14/10/2026 10:30',
    }),
  ).toBeVisible();
});

test('consults a role, then changes and deactivates it while editing', async ({ page }) => {
  const mock = await openSecurityTab(page, 'roles-test');
  await expect(roleRows(page)).toHaveCount(2);

  await rowWith(page, 'INV_ADMIN').dblclick();
  const consult = dialogTitled(page, "Consulta el rol de l'aplicació");
  await expect(consult).toBeVisible();
  await expect(consult.getByLabel('Codi')).toHaveText('INV_ADMIN');
  await expect(consult.getByLabel('Sistema')).toHaveText('weblogic');
  await expect(consult.getByLabel('Descripció')).toHaveText('Administració');
  await expect(consult.getByRole('button', { name: 'Edita el registre' })).toHaveCount(0);
  await consult.getByRole('button', { name: 'Accepta i tanca el diàleg' }).click();
  await expect(consult).toBeHidden();

  await startEditing(page);
  await rowWith(page, 'INV_ADMIN').focus();
  await page.keyboard.press('Enter');
  await expect(consult).toBeVisible();
  await consult.getByRole('button', { name: 'Edita el registre' }).click();
  const edit = dialogTitled(page, "Edita el rol de l'aplicació");
  await expect(edit).toBeVisible();
  await edit.getByRole('button', { name: 'Cancel·la els canvis' }).click();
  await expect(consult).toBeVisible();
  await consult.getByRole('button', { name: 'Tanca el diàleg' }).click();
  await expect(consult).toBeHidden();

  await openRowMenu(page, 'INV_ADMIN');
  await expect(rowMenuItems(page)).toHaveText(['Consulta', 'Edita', 'Dona de baixa']);
  await page.getByRole('menuitem', { name: 'Edita' }).click();
  await expect(edit).toBeVisible();
  await page.locator('#application-security-dialog-role').click();
  await page.getByRole('option', { name: /^INV_GESTOR/ }).click();
  await edit.getByRole('button', { name: 'Desa el registre', exact: true }).click();
  await expect(edit).toBeHidden();
  await expect(rowWith(page, 'INV_GESTOR')).toHaveCount(1);
  expect(mock.writes).toEqual([
    {
      method: 'PUT',
      path: '/application/security/role/12',
      body: { appSecurityId: 9, securityRoleId: 503 },
    },
  ]);

  await openRowMenu(page, 'INV_GESTOR');
  await page.getByRole('menuitem', { name: 'Dona de baixa' }).click();
  const confirmation = dialogTitled(page, 'Dona de baixa el registre?');
  await expect(confirmation).toContainText(
    "El registre deixarà d'aparèixer a la taula de registres actius.",
  );
  await confirmation.getByRole('button', { name: 'Confirma la baixa del registre' }).click();
  await expect(confirmation).toBeHidden();
  await expect(page.getByText("El registre s'ha donat de baixa correctament.")).toBeVisible();
  await expect(roleRows(page)).toHaveCount(1);
  expect(mock.writes.at(-1)).toEqual({
    method: 'DELETE',
    path: '/application/security/role/12',
    body: undefined,
  });
});

test('flags the roles outside the prefix and validates one manually', async ({
  page,
}, testInfo) => {
  const mock = await openSecurityTab(page, 'roles-test', {
    roles: [
      roleRow(12, { roleId: 501, name: 'INV_ADMIN', system: 'weblogic', description: null }),
      roleRow(
        13,
        { roleId: 505, name: 'ADM_ROOT', system: 'weblogic', description: null },
        'NOT_VALIDATED',
      ),
      roleRow(
        14,
        { roleId: 506, name: 'OPS_READ', system: 'weblogic', description: null },
        'MANUAL',
        { by: 'maria', at: '2026-10-07T16:21:12' },
      ),
    ],
  });
  await expect(roleRows(page)).toHaveCount(3);

  await expect(rowWith(page, 'INV_ADMIN').getByRole('img')).toHaveCount(0);
  const pending = rowWith(page, 'ADM_ROOT').getByRole('img', { name: NOT_VALIDATED });
  await expect(pending.locator('i')).toHaveClass(/pi-exclamation-circle/);
  await pending.focus();
  await expect(page.getByRole('tooltip')).toHaveText(NOT_VALIDATED);
  await pending.press('Escape');
  const manual = rowWith(page, 'OPS_READ').getByRole('img', {
    name: 'Rol validat manualment per maria el 07/10/2026 16:21',
  });
  await expect(manual.locator('i')).toHaveClass(/pi-check-circle/);
  // Outside edit mode nothing can be validated: there is no row menu at all.
  await expect(rolesRegion(page).getByRole('button', { name: ROW_ACTIONS })).toHaveCount(0);
  await rolesRegion(page).screenshot({ path: testInfo.outputPath('roles-validation-states.png') });

  await startEditing(page);
  await openRowMenu(page, 'ADM_ROOT');
  await expect(rowMenuItems(page)).toHaveText([
    'Consulta',
    'Validar manualment',
    'Edita',
    'Dona de baixa',
  ]);
  await page.getByRole('menuitem', { name: 'Validar manualment' }).click();
  const confirmation = dialogTitled(page, 'Validar manualment el rol?');
  await expect(confirmation).toContainText(
    "El rol «ADM_ROOT» no comença pel prefix de l'aplicació. Si el valides, quedarà registrat al teu nom.",
  );
  await confirmation
    .getByRole('button', { name: 'Cancel·la la validació manual del rol' })
    .click();
  await expect(confirmation).toBeHidden();
  await expect(rowWith(page, 'ADM_ROOT')).toBeFocused();
  expect(mock.writes).toEqual([]);

  await openRowMenu(page, 'ADM_ROOT');
  await page.getByRole('menuitem', { name: 'Validar manualment' }).click();
  await confirmation.getByRole('button', { name: 'Confirma la validació manual del rol' }).click();
  await expect(confirmation).toBeHidden();
  expect(mock.writes).toEqual([
    { method: 'PUT', path: '/application/security/role/validate/13', body: {} },
  ]);
  await expect(page.getByText("El rol s'ha validat manualment.")).toBeVisible();
  await expect(
    rowWith(page, 'ADM_ROOT').getByRole('img', {
      name: 'Rol validat manualment per roles-test el 14/10/2026 10:30',
    }),
  ).toBeVisible();
  await openRowMenu(page, 'ADM_ROOT');
  await expect(rowMenuItems(page)).toHaveText(['Consulta', 'Edita', 'Dona de baixa']);
  await openRowMenu(page, 'ADM_ROOT');
  await expect(rowMenuItems(page)).toHaveCount(0);
  await openRowMenu(page, 'INV_ADMIN');
  await expect(rowMenuItems(page)).toHaveText(['Consulta', 'Edita', 'Dona de baixa']);
});

// App bug: focus moves before the reloaded rows render, so it ends up on <body>.
test.fixme('returns focus to the role row after validating it manually', async ({ page }) => {
  await openSecurityTab(page, 'roles-test');
  await startEditing(page);
  await openRowMenu(page, 'ADM_ROOT');
  await page.getByRole('menuitem', { name: 'Validar manualment' }).click();
  await page.getByRole('button', { name: 'Confirma la validació manual del rol' }).click();
  await expect(
    rowWith(page, 'ADM_ROOT').getByRole('img', { name: /manualment per/ }),
  ).toBeVisible();
  await expect(rowWith(page, 'ADM_ROOT')).toBeFocused();
});

test('keeps the roles read-only for an inactive application', async ({ page }) => {
  await openSecurityTab(page, 'roles-test', { status: 'INACTIVE' });
  await expect(roleRows(page)).toHaveCount(2);

  await expect(page.getByRole('button', { name: 'Editar Seguretat', exact: true })).toHaveCount(0);
  await expect(addRoleButton(page)).toHaveCount(0);
  await expect(rolesRegion(page).getByRole('button', { name: ROW_ACTIONS })).toHaveCount(0);
  await expect(rowWith(page, 'ADM_ROOT').getByRole('img', { name: NOT_VALIDATED })).toBeVisible();

  await rowWith(page, 'ADM_ROOT').dblclick();
  const dialog = dialogTitled(page, "Consulta el rol de l'aplicació");
  await expect(dialog.getByLabel('Codi')).toHaveText('ADM_ROOT');
  await expect(dialog.getByRole('button', { name: 'Edita el registre' })).toHaveCount(0);
  await expect(dialog.getByRole('button', { name: 'Accepta i tanca el diàleg' })).toBeVisible();
});

test('disables adding roles until the Seguretat data exists', async ({ page }) => {
  const mock = await openSecurityTab(page, 'roles-test', { appSecurityId: null });
  await expect(page.getByRole('heading', { name: "Rols de l'aplicació" })).toBeVisible();
  await expect(roleRows(page)).toHaveText(["No s'han trobat resultats"]);
  expect(mock.roleRequests).toEqual([]);

  await startEditing(page);
  await expect(addRoleButton(page)).toBeDisabled();
});

test('reflows the roles toolbar at 320 px without horizontal scrolling', async ({
  page,
}, testInfo) => {
  await page.setViewportSize({ width: 320, height: 800 });
  await openSecurityTab(page, 'roles-test');
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
