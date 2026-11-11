import { expect, test } from './support/application.fixture';
import { uniqueToken } from './support/data';
import {
  apiUrl,
  expectApiJsonResponse,
  expectJsonResponse,
  waitForApiResponse,
} from './support/http';
import { registerSoftDeleteCleanup } from './support/systems';
import { selectOption, selectRowAction } from './support/ui';

const WEB_CONTEXTS_API = '/invaiback/web-context';
const DEVELOPMENT_CONTEXTS_API = '/invaiback/application/development/web-context';

interface CatalogEntry {
  id: number;
  name: string;
  deletedAt: string | null;
}

// Manages the web contexts of a disposable application, with a web context of its own.
test('manages web contexts in Development and only consults or verifies them in Security', async ({
  page,
  cleanupRegistry,
  disposableApplication,
}, testInfo) => {
  test.setTimeout(120_000);
  const name = `E2E Context ${uniqueToken(testInfo)}`;
  const catalog = await expectApiJsonResponse<CatalogEntry>(
    await page.request.post(apiUrl(page, WEB_CONTEXTS_API), {
      data: { name, nameEs: `${name} ES` },
    }),
    `POST ${WEB_CONTEXTS_API}`,
  );
  registerSoftDeleteCleanup<CatalogEntry>({
    page,
    cleanup: cleanupRegistry,
    label: `web context ${name}`,
    pathname: WEB_CONTEXTS_API,
    target: { id: catalog.id, matches: (item) => item.id === catalog.id },
  });
  const field = { name: 'Educació i Formació' };
  const mutations: string[] = [];
  page.on('request', (request) => {
    const path = new URL(request.url()).pathname;
    const method = request.method();
    if (path.startsWith(DEVELOPMENT_CONTEXTS_API) && method !== 'GET') mutations.push(method);
    if (/^\/invaiback\/application\/security\/web-context\/validate\/\d+$/.test(path)) {
      expect(method).toBe('PUT');
      expect(request.postDataJSON()).toEqual({ reason: 'Context comprovat' });
      mutations.push('VALIDATE');
    }
  });

  await page.goto(`/aplicacions/${disposableApplication.id}/development`);
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
  const createPromise = waitForApiResponse(page, 'POST', DEVELOPMENT_CONTEXTS_API);
  await dialog.getByRole('button', { name: 'Afegeix el registre', exact: true }).click();
  const created = await createPromise;
  expect(created.request().postDataJSON()).toMatchObject({
    appDevelopmentId: disposableApplication.appDevelopmentId,
  });
  expect(created.request().postDataJSON().appSecurityId).toBeUndefined();
  await expectJsonResponse(created);
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
  // The rows render again once the tab has loaded; focus the icon again if the first focus landed
  // on the row that was replaced.
  await expect(async () => {
    await pendingIcon.blur();
    await pendingIcon.focus();
    await expect(page.getByRole('tooltip')).toHaveText('Context web pendent de verificar.', {
      timeout: 2_000,
    });
  }).toPass({ timeout: 15_000 });
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
  // Security shows a verified context as an icon whose name carries the verification reason.
  const verifiedIcon = row.getByRole('img', { name: 'Verificat: Context comprovat' });
  await expect(verifiedIcon).toBeVisible();
  await expect(verify).toHaveCount(0);
  await expect(pendingIcon).toHaveCount(0);
  await expect(securityTab).not.toContainText('contextos web pendents de verificar');
  for (const width of [320, 768, 1440]) {
    await page.setViewportSize({ width, height: 900 });
    await expect(verifiedIcon).toBeVisible();
  }
  await page.screenshot({ path: 'test-results/web-context-security.png', fullPage: true });
  expect(mutations).toEqual(['POST', 'PUT', 'VALIDATE']);
  await page
    .getByRole('button', { name: 'Cancel·lar els canvis de Seguretat', exact: true })
    .click();
  await expect(security.locator('.application-security-verification-column')).toHaveCount(0);
  await expect(verifiedIcon).toBeVisible();
  await page.getByRole('link', { name: /^Desenvolupament/ }).click();
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
