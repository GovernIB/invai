import type { Page } from '@playwright/test';

import {
  CONNECTIONS_API,
  type ConnectionRecord,
  createConnection,
  GRANTED_ROLE,
  INTEGRATION_API,
  MISSING_ROLE,
  pickRoles,
  pickSoffidPerson,
  registerConnectionCleanup,
  SOFFID_PERSON,
} from './support/application-integrations';
import { type DisposableApplication, expect, test } from './support/application.fixture';
import type { CleanupRegistry } from './support/cleanup-registry';
import { expectJsonResponse, failApiRequest, waitForApiResponse } from './support/http';
import { selectOption } from './support/ui';

interface IntegrationsTab {
  connection: ConnectionRecord;
  listRequests: URL[];
  anchorPuts: unknown[];
  connectionPosts: unknown[];
}

// Opens the tab of the application with one connection whose required roles the Soffid person
// only partly holds.
async function openApplication(
  page: Page,
  cleanup: CleanupRegistry,
  application: DisposableApplication,
  path: 'integrations' | 'data',
): Promise<IntegrationsTab> {
  const connection = await createConnection(page, cleanup, application, [
    GRANTED_ROLE,
    MISSING_ROLE,
  ]);
  const tab: IntegrationsTab = {
    connection,
    listRequests: [],
    anchorPuts: [],
    connectionPosts: [],
  };
  page.on('request', (request) => {
    const url = new URL(request.url());
    if (url.pathname === CONNECTIONS_API && request.method() === 'GET') {
      tab.listRequests.push(url);
    } else if (url.pathname === CONNECTIONS_API && request.method() === 'POST') {
      tab.connectionPosts.push(request.postDataJSON());
    } else if (
      url.pathname === `${INTEGRATION_API}/${application.appIntegrationId}` &&
      request.method() === 'PUT'
    ) {
      tab.anchorPuts.push(request.postDataJSON());
    }
  });
  await page.goto(`/aplicacions/${application.id}/${path}`);
  return tab;
}

const rows = (page: Page) => page.locator('app-application-integrations-table tbody tr');
// The shared CRUD shell does not name its dialog yet, so it is located by its visible heading.
const dialogTitled = (page: Page, title: string) =>
  page.getByRole('dialog').filter({ has: page.getByRole('heading', { name: title }) });

// The Dades tab content is covered by application-data.spec.ts.
test('shows the Dades and Integracions tabs', async ({
  page,
  cleanupRegistry,
  disposableApplication,
}) => {
  await openApplication(page, cleanupRegistry, disposableApplication, 'data');

  await expect(page.locator('.application-detail-tab')).toHaveCount(9);
  await expect(page.locator('.application-detail-tab').nth(6)).toHaveText('Dades');
  await expect(page.locator('.application-detail-tab').nth(7)).toHaveText('Integracions');
  await expect(page.getByRole('heading', { name: 'Dades', exact: true })).toBeVisible();
});

test('lists the backend connections with their role comparison', async ({
  page,
  cleanupRegistry,
  disposableApplication,
}, testInfo) => {
  const tab = await openApplication(page, cleanupRegistry, disposableApplication, 'integrations');

  await expect(rows(page)).toHaveCount(1);
  await expect(page.locator('app-application-integrations-table thead th')).toHaveText([
    'Sistema',
    'Tecnologia',
    "Usuari d'integració",
    'Rols requerits',
    'Rols atorgats',
  ]);
  await expect(rows(page).first()).toContainText('Soffid');
  await expect(rows(page).first()).toContainText('Sistema extern');
  // The back-end lists the required roles by their Soffid identifier.
  await expect(rows(page).first()).toContainText(`${MISSING_ROLE.name}, ${GRANTED_ROLE.name}`);
  await expect(
    rows(page).first().getByRole('img', {
      name: 'Els rols atorgats a Soffid no coincideixen amb els rols requerits.',
    }),
  ).toBeVisible();
  expect(tab.listRequests[0].searchParams.get('appIntegrationId')).toBe(
    String(disposableApplication.appIntegrationId),
  );
  expect(tab.listRequests[0].searchParams.get('statusId')).toBe('1');
  await page.screenshot({ path: testInfo.outputPath('integrations-view.png'), fullPage: true });

  await page.getByRole('columnheader', { name: "Usuari d'integració" }).click();
  await expect
    .poll(() => tab.listRequests.at(-1)?.searchParams.getAll('sort'))
    .toEqual(['username,asc']);

  await rows(page).first().dblclick();
  const dialog = dialogTitled(page, 'Consultar connexió');
  await expect(dialog).toBeVisible();
  await expect(dialog.getByLabel("Usuari d'integració")).toHaveValue(SOFFID_PERSON.userName);
  await expect(dialog.getByLabel('Coincidència de rols')).toHaveValue('No coincideixen');
  await dialog.screenshot({ path: testInfo.outputPath('integrations-connection-view.png') });
});

test('saves the observations through the integration anchor', async ({
  page,
  cleanupRegistry,
  disposableApplication,
}) => {
  const tab = await openApplication(page, cleanupRegistry, disposableApplication, 'integrations');
  await expect(rows(page)).toHaveCount(1);

  await page.getByRole('button', { name: 'Editar Integracions', exact: true }).click();
  const editor = page.locator('p-editor .ql-editor');
  await editor.click();
  await editor.pressSequentially('Nota de prova');
  await page.getByRole('button', { name: 'Desar els canvis de Integracions' }).click();

  await expect(
    page.getByText("S'han desat correctament els canvis de Integracions."),
  ).toBeVisible();
  // Quill stores typed spaces as non-breaking spaces.
  expect(tab.anchorPuts).toEqual([
    {
      applicationId: disposableApplication.id,
      observation: expect.stringMatching(/^<p>Nota(&nbsp;| )de(&nbsp;| )prova<\/p>$/),
    },
  ]);
  await expect(page.locator('p-editor')).toHaveCount(0);
  await expect(page.locator('.invai-form-static-value--rich')).toContainText('Nota de prova');
});

test('adds a connection to an external system and deactivates another one', async ({
  page,
  cleanupRegistry,
  disposableApplication,
}, testInfo) => {
  test.setTimeout(90_000);
  const tab = await openApplication(page, cleanupRegistry, disposableApplication, 'integrations');
  await expect(rows(page)).toHaveCount(1);
  await page.getByRole('button', { name: 'Editar Integracions', exact: true }).click();

  await page.getByRole('button', { name: 'Afegeix una connexió' }).click();
  const dialog = dialogTitled(page, 'Afegir connexió');
  await expect(dialog).toBeVisible();
  await dialog.getByRole('radio', { name: 'Sistema extern' }).check();
  await selectOption(page, 'application-integration-dialog-external-system', 'Soffid');
  await selectOption(page, 'application-integration-dialog-technology', 'Spring Boot');
  await pickSoffidPerson(page);
  await pickRoles(page, GRANTED_ROLE);
  await page.screenshot({ path: testInfo.outputPath('integrations-create.png'), fullPage: true });

  const createPromise = waitForApiResponse(page, 'POST', CONNECTIONS_API);
  await dialog.getByRole('button', { name: 'Afegeix la connexió' }).click();
  const created = await expectJsonResponse<{ id: number }>(await createPromise);
  registerConnectionCleanup(page, cleanupRegistry, created.id);
  await expect(dialog).toBeHidden();
  expect(tab.connectionPosts).toEqual([
    {
      appIntegrationId: disposableApplication.appIntegrationId,
      applicationId: null,
      externalSystemId: expect.any(Number),
      technologyId: expect.any(Number),
      username: SOFFID_PERSON.userName,
      requiredRoleIds: [GRANTED_ROLE.roleId],
    },
  ]);
  await expect(rows(page)).toHaveCount(2);

  const deletePromise = waitForApiResponse(
    page,
    'DELETE',
    `${CONNECTIONS_API}/${tab.connection.id}`,
  );
  const existingRow = rows(page).filter({ hasText: MISSING_ROLE.name });
  await existingRow.getByRole('button', { name: 'Obre les accions del registre' }).click();
  await page.getByRole('menuitem', { name: 'Dona de baixa' }).click();
  await page.getByRole('button', { name: 'Confirma la baixa de la connexió' }).click();
  expect((await deletePromise).ok()).toBe(true);
  await expect(rows(page)).toHaveCount(1);
});

// A Soffid outage no longer fails the list (rows come back with rolesUnavailable); this covers any
// other structured error on the listing.
test('reports a listing error inline and recovers on retry', async ({
  page,
  cleanupRegistry,
  disposableApplication,
}) => {
  await openApplication(page, cleanupRegistry, disposableApplication, 'integrations');
  await expect(rows(page)).toHaveCount(1);
  await failApiRequest(page, 'GET', CONNECTIONS_API, {
    status: 400,
    json: {
      error: 'Error',
      message: "No s'ha pogut consultar els rols atorgats a aquest usuari en aquest moment.",
    },
  });

  await page.getByRole('columnheader', { name: 'Tecnologia' }).click();
  await expect(page.getByRole('alert')).toHaveText(
    "No s'ha pogut consultar els rols atorgats a aquest usuari en aquest moment.",
  );
  await expect(page.getByRole('dialog')).toHaveCount(0);

  await page.getByRole('button', { name: 'Torna a carregar les integracions' }).click();
  await expect(page.getByRole('alert')).toHaveCount(0);
  await expect(rows(page)).toHaveCount(1);
});
