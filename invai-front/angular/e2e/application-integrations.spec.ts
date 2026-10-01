import { expect, test, type Page, type Route } from '@playwright/test';

import { selectOption } from './support/ui';

test.use({ storageState: { cookies: [], origins: [] } });

const pageOf = (content: unknown[]) => ({
  content,
  totalElements: content.length,
  totalPages: 1,
  last: true,
  number: 0,
});

const CONNECTION = {
  id: 1,
  appIntegrationId: 13,
  application: null,
  externalSystem: { id: 2, name: 'Soffid', company: null, deletedAt: null },
  technology: { id: 4, name: 'Spring Boot', layer: { id: 1, name: 'Backend', deletedAt: null }, deletedAt: null },
  username: 'u00004',
  requiredRoles: [
    { id: 26, name: 'INV_ADMIN', description: null, system: 'SEYCON' },
    { id: 33, name: 'INV_READ', description: null, system: 'SEYCON' },
  ],
  grantedRoles: [{ id: 26, name: 'INV_ADMIN', description: null, system: 'SEYCON' }],
  rolesMismatch: true,
  deletedAt: null,
};

interface IntegrationsMock {
  connections: unknown[];
  listRequests: URL[];
  anchorPuts: unknown[];
  connectionPosts: unknown[];
  connectionDeletes: string[];
  failListing: boolean;
}

async function openApplication(page: Page, path: 'integrations' | 'data'): Promise<IntegrationsMock> {
  const mock: IntegrationsMock = {
    connections: [CONNECTION],
    listRequests: [],
    anchorPuts: [],
    connectionPosts: [],
    connectionDeletes: [],
    failListing: false,
  };
  let anchor = { id: 13, applicationId: 1, observation: null as string | null, deletedAt: null };
  await page.route('**/invaiback/**', async (route: Route) => {
    const request = route.request();
    const url = new URL(request.url());
    const path = url.pathname;
    const method = request.method();
    if (path.endsWith('/auth/me')) {
      return route.fulfill({ json: { authenticated: true, username: 'integrations-test' } });
    }
    if (path.endsWith('/application/1')) {
      return route.fulfill({
        json: {
          id: 1,
          code: 'TEST',
          name: 'Integracions test',
          status: 'ACTIVE',
          appIntegrationId: 13,
          missingIntegrationData: mock.connections.length === 0,
        },
      });
    }
    if (path.endsWith('/application/integration/13')) {
      if (method === 'PUT') {
        const body = request.postDataJSON();
        mock.anchorPuts.push(body);
        anchor = { ...anchor, observation: body.observation };
      }
      return route.fulfill({ json: anchor });
    }
    if (path.endsWith('/application/integration-connection') && method === 'GET') {
      mock.listRequests.push(url);
      if (mock.failListing) {
        mock.failListing = false;
        return route.fulfill({
          status: 400,
          json: {
            error: 'Error',
            message: "No s'ha pogut consultar els rols atorgats a aquest usuari en aquest moment.",
          },
        });
      }
      return route.fulfill({ json: pageOf(mock.connections) });
    }
    if (path.endsWith('/application/integration-connection') && method === 'POST') {
      const body = request.postDataJSON();
      mock.connectionPosts.push(body);
      const created = { ...CONNECTION, id: 2, username: body.username, rolesMismatch: false };
      mock.connections = [...mock.connections, created];
      return route.fulfill({ status: 201, json: { ...created, requiredRoles: null, grantedRoles: null } });
    }
    if (/\/application\/integration-connection\/\d+$/.test(path) && method === 'DELETE') {
      mock.connectionDeletes.push(path);
      mock.connections = [];
      return route.fulfill({ status: 204 });
    }
    if (path.endsWith('/technology')) {
      return route.fulfill({ json: pageOf([CONNECTION.technology]) });
    }
    if (path.endsWith('/external-system')) {
      return route.fulfill({ json: pageOf([CONNECTION.externalSystem]) });
    }
    if (path.endsWith('/person/soffid-search')) {
      return route.fulfill({
        json: pageOf([
          {
            id: null,
            company: null,
            firstName: 'Maria',
            lastName: 'Tur',
            email: 'maria@caib.es',
            personalCaib: true,
            deletedAt: null,
            userName: 'u00009',
          },
        ]),
      });
    }
    if (path.endsWith('/security-role/soffid-search')) {
      return route.fulfill({
        json: pageOf([{ id: null, roleId: 26, name: 'INV_ADMIN', system: 'SEYCON', description: null }]),
      });
    }
    return route.fulfill({ json: pageOf([]) });
  });
  await page.goto(`/aplicacions/1/${path}`);
  return mock;
}

const rows = (page: Page) => page.locator('app-application-integrations-table tbody tr');
// The shared CRUD shell does not name its dialog yet, so it is located by its visible heading.
const dialogTitled = (page: Page, title: string) =>
  page.getByRole('dialog').filter({ has: page.getByRole('heading', { name: title }) });

// The Dades tab content is covered by application-data.spec.ts.
test('shows the Dades and Integracions tabs', async ({ page }) => {
  await openApplication(page, 'data');

  await expect(page.locator('.application-detail-tab')).toHaveCount(8);
  await expect(page.locator('.application-detail-tab').nth(6)).toHaveText('Dades');
  await expect(page.locator('.application-detail-tab').nth(7)).toHaveText('Integracions');
  await expect(page.getByRole('heading', { name: 'Dades', exact: true })).toBeVisible();
});

test('lists the backend connections with their role comparison', async ({ page }, testInfo) => {
  const mock = await openApplication(page, 'integrations');

  await expect(rows(page)).toHaveCount(1);
  await expect(page.locator('app-application-integrations-table thead th')).toHaveText([
    'Sistema',
    'Tecnologia',
    "Usuari d'integració",
    'Rols requerits',
    'Rols atorgats',
    'Coincidència de rols',
  ]);
  await expect(rows(page).first()).toContainText('Soffid');
  await expect(rows(page).first()).toContainText('Sistema extern');
  await expect(rows(page).first()).toContainText('INV_ADMIN, INV_READ');
  await expect(rows(page).first()).toContainText('No coincideixen');
  expect(mock.listRequests[0].searchParams.get('appIntegrationId')).toBe('13');
  expect(mock.listRequests[0].searchParams.get('statusId')).toBe('1');
  await page.screenshot({ path: testInfo.outputPath('integrations-view.png'), fullPage: true });

  await page.getByRole('columnheader', { name: "Usuari d'integració" }).click();
  await expect.poll(() => mock.listRequests.at(-1)?.searchParams.getAll('sort')).toEqual(['username,asc']);

  await rows(page).first().dblclick();
  const dialog = dialogTitled(page, 'Consultar connexió');
  await expect(dialog).toBeVisible();
  await expect(dialog.getByLabel("Usuari d'integració")).toHaveValue('u00004');
  await expect(dialog.getByLabel('Coincidència de rols')).toHaveValue('No coincideixen');
  await dialog.screenshot({ path: testInfo.outputPath('integrations-connection-view.png') });
});

test('saves the observations through the integration anchor', async ({ page }) => {
  const mock = await openApplication(page, 'integrations');
  await expect(rows(page)).toHaveCount(1);

  await page.getByRole('button', { name: 'Editar Integracions', exact: true }).click();
  const editor = page.locator('p-editor .ql-editor');
  await editor.click();
  await editor.pressSequentially('Nota de prova');
  await page.getByRole('button', { name: 'Desar els canvis de Integracions' }).click();

  await expect(page.getByText("S'han desat correctament els canvis de Integracions.")).toBeVisible();
  // Quill stores typed spaces as non-breaking spaces.
  expect(mock.anchorPuts).toEqual([
    { applicationId: 1, observation: expect.stringMatching(/^<p>Nota(&nbsp;| )de(&nbsp;| )prova<\/p>$/) },
  ]);
  await expect(page.locator('p-editor')).toHaveCount(0);
  await expect(page.locator('.invai-form-static-value--rich')).toContainText('Nota de prova');
});

test('adds a connection to an external system and deactivates another one', async ({
  page,
}, testInfo) => {
  const mock = await openApplication(page, 'integrations');
  await expect(rows(page)).toHaveCount(1);
  await page.getByRole('button', { name: 'Editar Integracions', exact: true }).click();

  await page.getByRole('button', { name: 'Afegeix una connexió' }).click();
  const dialog = dialogTitled(page, 'Afegir connexió');
  await expect(dialog).toBeVisible();
  await dialog.getByRole('radio', { name: 'Sistema extern' }).check();
  await selectOption(page, 'application-integration-dialog-external-system', 'Soffid');
  await selectOption(page, 'application-integration-dialog-technology', 'Spring Boot');

  const user = page.locator('#application-integration-dialog-soffid-person');
  await user.pressSequentially('mar');
  await page.getByRole('option', { name: /Maria Tur/ }).click();

  await page.locator('#application-integration-dialog-roles').focus();
  await page.keyboard.press('Enter');
  await page.getByRole('option', { name: 'INV_ADMIN — SEYCON' }).click();
  await page.keyboard.press('Escape');
  await page.screenshot({ path: testInfo.outputPath('integrations-create.png'), fullPage: true });

  await dialog.getByRole('button', { name: 'Afegeix la connexió' }).click();
  await expect(dialog).toBeHidden();
  expect(mock.connectionPosts).toEqual([
    {
      appIntegrationId: 13,
      applicationId: null,
      externalSystemId: 2,
      technologyId: 4,
      username: 'u00009',
      requiredRoleIds: [26],
    },
  ]);
  await expect(rows(page)).toHaveCount(2);

  await rows(page).first().getByRole('button', { name: 'Obre les accions del registre' }).click();
  await page.getByRole('menuitem', { name: 'Dona de baixa' }).click();
  await page.getByRole('button', { name: 'Confirma la baixa de la connexió' }).click();
  await expect.poll(() => mock.connectionDeletes).toEqual(['/invaiback/application/integration-connection/1']);
  await expect(rows(page)).toHaveCount(1);
});

test('reports a Soffid outage inline and recovers on retry', async ({ page }) => {
  const mock = await openApplication(page, 'integrations');
  await expect(rows(page)).toHaveCount(1);
  mock.failListing = true;

  await page.getByRole('columnheader', { name: 'Tecnologia' }).click();
  await expect(page.getByRole('alert')).toHaveText(
    "No s'ha pogut consultar els rols atorgats a aquest usuari en aquest moment.",
  );
  await expect(page.getByRole('dialog')).toHaveCount(0);

  await page.getByRole('button', { name: 'Torna a carregar les integracions' }).click();
  await expect(page.getByRole('alert')).toHaveCount(0);
  await expect(rows(page)).toHaveCount(1);
});
