import { expect, test } from './support/auth.fixture';
import { selectOption, selectRowAction } from './support/ui';

// Isolate context mutations while exercising the real routed application and its dialogs.
test('manages web contexts in Development and only consults or verifies them in Security', async ({
  page,
}) => {
  test.setTimeout(120_000);
  await page.goto('/');
  const applications = await page.request.get('/invaiback/application', {
    params: { page: 0, size: 1, statusId: 1 },
  });
  expect(applications.ok()).toBeTruthy();
  const applicationId = (await applications.json()).content[0].id as number;
  const detailResponse = await page.request.get(`/invaiback/application/${applicationId}`);
  expect(detailResponse.ok()).toBeTruthy();
  const application = await detailResponse.json();
  expect(application.appSecurityId).toBeTruthy();
  expect(application.appDevelopmentId).toBeTruthy();

  const catalog = { id: 901, name: 'Context E2E', nameEs: 'Contexto E2E', deletedAt: null };
  const field = { id: 902, name: 'Àmbit E2E', nameEs: 'Ámbito E2E', deletedAt: null };
  let records: Array<Record<string, unknown>> = [];
  const mutations: string[] = [];
  const envelope = (content: unknown[]) => ({
    content,
    number: 0,
    totalElements: content.length,
    totalPages: 1,
    first: true,
    last: true,
    size: 10,
    numberOfElements: content.length,
    empty: content.length === 0,
  });
  await page.route(/\/invaiback\/web-context(?:\?.*)?$/, (route) =>
    route.fulfill({ json: envelope([catalog]) }),
  );
  await page.route(/\/invaiback\/field(?:\?.*)?$/, (route) =>
    route.fulfill({ json: envelope([field]) }),
  );
  await page.route(/\/application\/security\/web-context\/\d+(?:\?.*)?$/, async (route) => {
    expect(route.request().method()).toBe('GET');
    await route.fulfill({ json: envelope(records) });
  });
  await page.route(/\/application\/development\/web-context(?:\/\d+)?(?:\?.*)?$/, async (route) => {
    const method = route.request().method();
    if (method === 'GET') {
      await route.fulfill({ json: envelope(records) });
      return;
    }
    mutations.push(method);
    if (method === 'DELETE') {
      records = [];
      await route.fulfill({ status: 204 });
      return;
    }
    const payload = route.request().postDataJSON();
    expect(payload.appDevelopmentId).toBe(application.appDevelopmentId);
    expect(payload.appSecurityId).toBeUndefined();
    records = [
      {
        id: 903,
        appSecurity: { id: application.appSecurityId },
        appDevelopment: { id: application.appDevelopmentId },
        webContext: catalog,
        field,
        url: payload.url,
        observation: payload.observation,
        validated: false,
        validatedAt: null,
        validatedBy: null,
        validatedReason: null,
        deletedAt: null,
      },
    ];
    await route.fulfill({ json: records[0] });
  });
  await page.route(/\/application\/security\/web-context\/validate\/903$/, async (route) => {
    expect(route.request().method()).toBe('PUT');
    const payload = route.request().postDataJSON();
    expect(payload).toEqual({ reason: 'Context comprovat' });
    mutations.push('VALIDATE');
    records = records.map((record) => ({
      ...record,
      validated: true,
      validatedAt: '2026-09-23T09:00:00',
      validatedBy: 'e2e',
      validatedReason: payload.reason,
    }));
    await route.fulfill({ status: 204 });
  });

  await page.goto(`/aplicacions/${applicationId}/development`);
  const panel = page.locator(
    'section[aria-labelledby="application-development-web-contexts-title"]',
  );
  await expect(panel.getByRole('heading', { name: 'Contextos web' })).toBeVisible();
  await expect(page.getByRole('button', { name: 'Afegeix un context web' })).toHaveCount(0);
  await page
    .getByRole('button', { name: 'Editar Configuració del desenvolupament', exact: true })
    .click();
  await page.getByRole('button', { name: 'Afegeix un context web' }).click();
  const dialog = page.getByRole('dialog');
  await expect(dialog).toBeVisible();
  await dialog.getByRole('button', { name: 'Afegeix el registre', exact: true }).click();
  await expect(dialog.getByText('Aquest camp és obligatori.')).toHaveCount(2);
  await selectOption(page, 'application-security-dialog-web-context', catalog.name);
  await selectOption(page, 'application-security-dialog-field', field.name);
  await page.locator('#application-security-dialog-url').fill('https://example.test/context');
  await page.locator('#application-security-dialog-observation').fill('Created in Development');
  await dialog.getByRole('button', { name: 'Afegeix el registre', exact: true }).click();
  await expect(dialog).toHaveCount(0);
  await expect(panel.getByText('Created in Development')).toBeVisible();
  expect(mutations).toEqual(['POST']);

  let row = panel.locator('tbody tr').first();
  await row.press('Enter');
  await expect(dialog.getByRole('heading', { name: 'Consulta el context web' })).toBeVisible();
  await expect(page.locator('#application-security-dialog-url')).toHaveAttribute('readonly', '');
  await dialog.getByRole('button', { name: 'Edita el registre', exact: true }).click();
  await page.locator('#application-security-dialog-url').fill('https://temporary.test');
  await dialog.getByRole('button', { name: 'Cancel·la els canvis', exact: true }).click();
  await expect(page.locator('#application-security-dialog-url')).toHaveValue(
    'https://example.test/context',
  );
  await dialog.getByRole('button', { name: 'Edita el registre', exact: true }).click();
  await page.locator('#application-security-dialog-observation').fill('Updated in Development');
  await dialog.getByRole('button', { name: 'Desa el registre', exact: true }).click();
  await expect(dialog).toHaveCount(0);
  await expect(panel.getByText('Updated in Development')).toBeVisible();
  expect(mutations).toEqual(['POST', 'PUT']);
  await page
    .getByRole('button', {
      name: 'Cancel·lar els canvis de Configuració del desenvolupament',
      exact: true,
    })
    .click();
  const securityTab = page.locator('.application-detail-tabs a[href$="/security"]');
  await expect(securityTab).toContainText('contextos web pendents de verificar');
  await page.reload();
  await expect(securityTab).toContainText('contextos web pendents de verificar');
  await securityTab.click();

  const security = page.locator(
    'section[aria-labelledby="application-security-web-contexts-title"]',
  );
  await expect(security.getByText('Updated in Development')).toBeVisible();
  await expect(security.getByRole('button', { name: 'Afegeix un context web' })).toHaveCount(0);
  row = security.locator('tbody tr').first();
  await expect(security.locator('.application-security-verification-column')).toHaveCount(0);
  const pendingIcon = row.locator('.application-web-context-pending');
  await expect(pendingIcon).toBeVisible();
  await pendingIcon.focus();
  await expect(page.getByRole('tooltip')).toHaveText('Context web pendent de verificar.');
  await pendingIcon.press('Escape');
  await row.press('Enter');
  await expect(dialog.getByRole('heading', { name: 'Consulta el context web' })).toBeVisible();
  await expect(dialog.getByRole('button', { name: 'Edita el registre' })).toHaveCount(0);
  await dialog.getByRole('button', { name: 'Accepta i tanca el diàleg', exact: true }).click();
  await page.getByRole('button', { name: 'Editar Seguretat', exact: true }).click();
  const verify = row.getByRole('button', { name: 'Verificar', exact: true });
  await expect(verify).toBeVisible();
  await verify.focus();
  await verify.press('Enter');
  await expect(dialog.getByRole('heading', { name: 'Verifica el context web' })).toBeVisible();
  const reason = dialog.getByLabel('Motiu de la verificació');
  await expect(reason).toBeFocused();
  await dialog.getByRole('button', { name: 'Verificar', exact: true }).click();
  await expect(dialog.getByText('Indica el motiu de la verificació.')).toBeVisible();
  await reason.fill('Context comprovat');
  await dialog.getByRole('button', { name: 'Verificar', exact: true }).click();
  await expect(dialog).toHaveCount(0);
  await expect(row.getByText('Verificat')).toBeVisible();
  await expect(verify).toHaveCount(0);
  await expect(pendingIcon).toHaveCount(0);
  await expect(securityTab).not.toContainText('contextos web pendents de verificar');
  for (const width of [320, 768, 1440]) {
    await page.setViewportSize({ width, height: 900 });
    await expect(row.getByText('Verificat')).toBeVisible();
  }
  await page.screenshot({ path: 'test-results/web-context-security.png', fullPage: true });
  expect(mutations).toEqual(['POST', 'PUT', 'VALIDATE']);
  await page
    .getByRole('button', { name: 'Cancel·lar els canvis de Seguretat', exact: true })
    .click();
  await expect(security.locator('.application-security-verification-column')).toHaveCount(0);
  await expect(row.getByText('Verificat')).toBeVisible();
  await page.getByRole('link', { name: 'Desenvolupament', exact: true }).click();
  await page
    .getByRole('button', { name: 'Editar Configuració del desenvolupament', exact: true })
    .click();
  await selectRowAction(
    page,
    panel.locator('tbody tr').first(),
    'Dona de baixa',
    'Obre les accions del registre',
  );
  await dialog.getByRole('button', { name: 'Confirma la baixa del registre', exact: true }).click();
  await expect(dialog).toHaveCount(0);
  await expect(panel.getByText('Updated in Development')).toHaveCount(0);
  expect(mutations).toEqual(['POST', 'PUT', 'VALIDATE', 'DELETE']);
  await expect(securityTab).not.toContainText('contextos web pendents de verificar');
});
