import type { Page } from '@playwright/test';

import {
  findAvailableApplicationPrefix,
  findFreePrefix,
  findFreePrefixWithoutRoles,
  type PrefixWithRoles,
  registerPrefixRestore,
  SEVERAL_ROLES_PREFIXES,
  SINGLE_ROLE_PREFIXES,
  updateApplication,
  borrowApplicationPrefix,
} from './support/application-prefix';
import { type DisposableApplication, expect, test } from './support/application.fixture';
import type { CleanupRegistry } from './support/cleanup-registry';
import { uniqueToken } from './support/data';
import { apiUrl, expectJsonResponse, failApiRequest, waitForApiResponse } from './support/http';
import { selectFirstOption } from './support/ui';

const APPLICATION_API = '/invaiback/application';
const ASSIGN_ROLES = /^\/invaiback\/application\/assign-roles\/\d+$/;
const ROLES_FAILURE = {
  status: 400,
  json: { error: 'Error de Soffid', message: "No s'ha pogut consultar Soffid." },
};

// Application and assignment requests in the order they leave the browser.
function recordApplicationCalls(page: Page): string[] {
  const calls: string[] = [];
  page.on('request', (request) => {
    const path = new URL(request.url()).pathname;
    const method = request.method();
    if (
      (path === APPLICATION_API && method === 'POST') ||
      /^\/invaiback\/application\/(\d+|assign-roles\/\d+|reactivate\/\d+)$/.test(path)
    ) {
      calls.push(`${method} ${path}`);
    }
  });
  return calls;
}

const toast = (page: Page) => page.locator('.p-toast-message');

async function expectToast(
  page: Page,
  severity: 'success' | 'warn',
  summary: string,
  detail: string,
): Promise<void> {
  await expect(toast(page)).toHaveCount(1);
  await expect(toast(page)).toHaveClass(new RegExp(`p-toast-message-${severity}`));
  await expect(toast(page).locator('.p-toast-summary')).toHaveText(summary);
  await expect(toast(page).locator('.p-toast-detail')).toHaveText(detail);
}

const submitButton = (page: Page) => page.getByRole('button', { name: "Afegeix l'aplicació" });

test.describe('after creating an application', () => {
  // Deactivates the application the test created, giving it a fresh prefix first when it holds a
  // real Soffid prefix, which would otherwise stay taken.
  function registerCreatedApplicationCleanup(
    page: Page,
    cleanup: CleanupRegistry,
    created: { id?: number },
    token: string,
    ownsRealPrefix: boolean,
  ): void {
    cleanup.register(`created application ${token}`, async () => {
      if (created.id === undefined) return;
      if (ownsRealPrefix) {
        const prefix = await findAvailableApplicationPrefix(page, `${token}Z`);
        await updateApplication(page, created.id, { prefix });
      }
      await page.request.delete(apiUrl(page, `${APPLICATION_API}/${created.id}`));
    });
  }

  async function fillCreateForm(page: Page, token: string, prefix: string) {
    await page.goto('/aplicacions/new');
    const form = page.locator('app-application-create-form');
    const values = { name: `E2E Gestor de proves ${token}`, code: `E2E${token}` };
    await form.getByRole('textbox', { name: 'Aplicació', exact: true }).fill(values.name);
    await selectFirstOption(page, 'create-application-category');
    await selectFirstOption(page, 'create-application-information-system');
    await selectFirstOption(page, 'create-application-scope');
    await form.getByRole('textbox', { name: 'Prefix', exact: true }).fill(prefix);
    await form.getByRole('textbox', { name: 'Codi', exact: true }).fill(values.code);
    await selectFirstOption(page, 'create-application-administrative-unit');
    await selectFirstOption(page, 'create-application-commission');
    return values;
  }

  const outcomes: Array<{
    title: string;
    prefix: (page: Page, token: string) => Promise<PrefixWithRoles>;
    detail: (found: PrefixWithRoles) => string;
  }> = [
    {
      title: 'no role',
      prefix: findFreePrefixWithoutRoles,
      detail: ({ prefix }) =>
        `Aplicació afegida correctament. No hi ha cap rol de Soffid amb el prefix ${prefix}.`,
    },
    {
      title: 'one role',
      prefix: (page) => findFreePrefix(page, SINGLE_ROLE_PREFIXES, (roles) => roles.length === 1),
      detail: () => 'Aplicació afegida correctament amb 1 rol de Soffid assignat.',
    },
    {
      title: 'several roles',
      prefix: (page) => findFreePrefix(page, SEVERAL_ROLES_PREFIXES, (roles) => roles.length > 1),
      detail: ({ roles }) =>
        `Aplicació afegida correctament amb ${roles.length} rols de Soffid assignats.`,
    },
  ];

  for (const outcome of outcomes) {
    test(`assigns the Soffid roles of the new application and reports ${outcome.title}`, async ({
      page,
      cleanupRegistry,
    }, testInfo) => {
      test.setTimeout(90_000);
      const token = uniqueToken(testInfo, 7);
      await page.goto('/');
      const found = await outcome.prefix(page, token);
      const created: { id?: number } = {};
      registerCreatedApplicationCleanup(
        page,
        cleanupRegistry,
        created,
        token,
        outcome.title !== 'no role',
      );
      // The real assignment is held back until the saving form has been checked.
      let releaseAssignment = () => {};
      const assignmentGate = new Promise<void>((resolve) => (releaseAssignment = resolve));
      await page.route(
        (url) => ASSIGN_ROLES.test(url.pathname),
        async (route) => {
          await assignmentGate;
          await route.fallback();
        },
      );
      const values = await fillCreateForm(page, token, found.prefix);
      const calls = recordApplicationCalls(page);
      const createPromise = waitForApiResponse(page, 'POST', APPLICATION_API);

      await submitButton(page).click();
      const response = await createPromise;
      created.id = (await expectJsonResponse<{ id: number }>(response)).id;
      await expect.poll(() => calls.length).toBe(2);
      // The form keeps saving until the roles are assigned.
      await expect(submitButton(page)).toBeDisabled();
      await expect(page).toHaveURL(/\/aplicacions\/new$/);
      releaseAssignment();

      await expect(page).toHaveURL(/\/aplicacions$/);
      await expectToast(page, 'success', 'Aplicació afegida', outcome.detail(found));
      expect(response.request().postDataJSON()).toEqual({
        name: values.name,
        prefix: found.prefix,
        code: values.code,
        categoryId: expect.any(Number),
        systemTypeId: expect.any(Number),
        fieldId: expect.any(Number),
        admUnitCode: expect.any(String),
        commissionId: expect.any(Number),
        description: '',
        statusId: 1,
      });
      expect(calls).toEqual([
        `POST ${APPLICATION_API}`,
        `PUT ${APPLICATION_API}/assign-roles/${created.id}`,
      ]);
    });
  }

  test('warns that the roles were not assigned but still returns to the list', async ({
    page,
    cleanupRegistry,
  }, testInfo) => {
    const token = uniqueToken(testInfo, 7);
    await page.goto('/');
    const { prefix } = await findFreePrefixWithoutRoles(page, token);
    const created: { id?: number } = {};
    registerCreatedApplicationCleanup(page, cleanupRegistry, created, token, false);
    const assignment = await failApiRequest(page, 'PUT', ASSIGN_ROLES, ROLES_FAILURE);
    await fillCreateForm(page, token, prefix);
    const createPromise = waitForApiResponse(page, 'POST', APPLICATION_API);

    await submitButton(page).click();
    created.id = (await expectJsonResponse<{ id: number }>(await createPromise)).id;

    await expect(page).toHaveURL(/\/aplicacions$/);
    await expectToast(
      page,
      'warn',
      'Aplicació afegida',
      "L'aplicació s'ha afegit, però no s'han pogut assignar els seus rols de Soffid.",
    );
    await expect(page.getByRole('dialog')).toHaveCount(0);
    expect(assignment.hits).toHaveLength(1);
  });
});

test.describe('General tab', () => {
  const GENERAL = "Dades generals de l'aplicació";
  const SAVED = `S'han desat correctament els canvis de ${GENERAL}.`;

  async function editGeneral(page: Page, application: DisposableApplication) {
    await page.goto(`/aplicacions/${application.id}/general`);
    const prefix = page.getByRole('textbox', { name: 'Prefix', exact: true });
    await expect(prefix).toHaveValue(application.prefix);
    await page.getByRole('button', { name: `Editar ${GENERAL}`, exact: true }).click();
    await expect(prefix).not.toHaveAttribute('readonly');
    return { prefix, calls: recordApplicationCalls(page) };
  }

  const save = (page: Page) =>
    page.getByRole('button', { name: `Desar els canvis de ${GENERAL}` }).click();

  const updates = (page: Page, application: DisposableApplication) => {
    const bodies: unknown[] = [];
    page.on('request', (request) => {
      if (
        request.method() === 'PUT' &&
        new URL(request.url()).pathname === `${APPLICATION_API}/${application.id}`
      ) {
        bodies.push(request.postDataJSON());
      }
    });
    return bodies;
  };

  const expectedCalls = (application: DisposableApplication, assigned: boolean) => [
    `PUT ${APPLICATION_API}/${application.id}`,
    ...(assigned ? [`PUT ${APPLICATION_API}/assign-roles/${application.id}`] : []),
    `GET ${APPLICATION_API}/${application.id}`,
  ];

  test('assigns the Soffid roles of a new prefix before re-reading the application', async ({
    page,
    cleanupRegistry,
    disposableApplication,
  }) => {
    const found = await findFreePrefix(page, SEVERAL_ROLES_PREFIXES, (roles) => roles.length > 1);
    registerPrefixRestore(
      page,
      cleanupRegistry,
      disposableApplication.id,
      disposableApplication.prefix,
    );
    const bodies = updates(page, disposableApplication);
    const { prefix, calls } = await editGeneral(page, disposableApplication);

    await prefix.fill(found.prefix);
    await save(page);

    await expectToast(
      page,
      'success',
      'Canvis desats',
      `${SAVED} S'han assignat ${found.roles.length} rols de Soffid amb el prefix ${found.prefix}.`,
    );
    await expect(prefix).toHaveValue(found.prefix);
    await expect(prefix).toHaveAttribute('readonly');
    expect(bodies).toEqual([
      expect.objectContaining({ prefix: found.prefix, code: disposableApplication.code }),
    ]);
    expect(calls).toEqual(expectedCalls(disposableApplication, true));
  });

  test('reports when no new Soffid role matches the new prefix', async ({
    page,
    disposableApplication,
  }, testInfo) => {
    const found = await findFreePrefixWithoutRoles(page, uniqueToken(testInfo, 7));
    const { prefix, calls } = await editGeneral(page, disposableApplication);

    await prefix.fill(found.prefix);
    await save(page);

    await expectToast(
      page,
      'success',
      'Canvis desats',
      `${SAVED} No s'ha assignat cap rol nou de Soffid amb el prefix ${found.prefix}.`,
    );
    expect(calls).toEqual(expectedCalls(disposableApplication, true));
  });

  test('does not assign roles when the prefix is unchanged', async ({
    page,
    disposableApplication,
  }) => {
    const bodies = updates(page, disposableApplication);
    const { prefix, calls } = await editGeneral(page, disposableApplication);
    const renamed = `${disposableApplication.name} renovat`;

    await page.getByRole('textbox', { name: "Nom de l'aplicació", exact: true }).fill(renamed);
    await save(page);

    await expectToast(page, 'success', 'Canvis desats', SAVED);
    await expect(prefix).toHaveAttribute('readonly');
    expect(bodies).toEqual([
      expect.objectContaining({ name: renamed, prefix: disposableApplication.prefix }),
    ]);
    expect(calls).toEqual(expectedCalls(disposableApplication, false));
  });

  test('warns that the change was saved without the roles when the assignment fails', async ({
    page,
    disposableApplication,
  }, testInfo) => {
    const found = await findFreePrefixWithoutRoles(page, uniqueToken(testInfo, 7));
    await failApiRequest(page, 'PUT', ASSIGN_ROLES, ROLES_FAILURE);
    const { prefix, calls } = await editGeneral(page, disposableApplication);

    await prefix.fill(found.prefix);
    await save(page);

    await expectToast(
      page,
      'warn',
      'Canvis desats',
      `Els canvis s'han desat, però no s'han pogut assignar els rols de Soffid del prefix ${found.prefix}.`,
    );
    // The request opts out of the global server error dialog.
    await expect(page.getByRole('dialog')).toHaveCount(0);
    await expect(prefix).toHaveValue(found.prefix);
    await expect(prefix).toHaveAttribute('readonly');
    expect(calls).toEqual(expectedCalls(disposableApplication, true));
  });
});

test.describe('restoring an inactive application', () => {
  async function restore(page: Page, application: DisposableApplication) {
    const deactivated = await page.request.delete(
      apiUrl(page, `${APPLICATION_API}/${application.id}`),
    );
    expect(deactivated.ok(), await deactivated.text()).toBeTruthy();
    await page.goto(`/aplicacions/${application.id}/general`);
    await expect(page.getByText("L'aplicació està donada de baixa")).toBeVisible();
    const calls = recordApplicationCalls(page);
    await page.getByRole('button', { name: "Donar d'alta l'aplicació" }).click();
    await page.getByRole('button', { name: "Confirma l'alta de l'aplicació" }).click();
    return calls;
  }

  test('assigns the Soffid roles of the restored application', async ({
    page,
    cleanupRegistry,
    disposableApplication,
  }) => {
    // Given through the API, so its role is still to be assigned when the application returns.
    const { prefix } = await borrowApplicationPrefix(
      page,
      cleanupRegistry,
      disposableApplication,
      SINGLE_ROLE_PREFIXES,
      (roles) => roles.length === 1,
    );

    const calls = await restore(page, disposableApplication);

    await expectToast(
      page,
      'success',
      "Aplicació donada d'alta",
      "L'aplicació s'ha donat d'alta correctament. " +
        `S'ha assignat 1 rol de Soffid amb el prefix ${prefix}.`,
    );
    await expect(page.getByText("L'aplicació està donada de baixa")).toHaveCount(0);
    await expect(
      page.getByRole('button', { name: "Editar Dades generals de l'aplicació", exact: true }),
    ).toBeVisible();
    expect(calls).toEqual([
      `PUT ${APPLICATION_API}/reactivate/${disposableApplication.id}`,
      `PUT ${APPLICATION_API}/assign-roles/${disposableApplication.id}`,
      `GET ${APPLICATION_API}/${disposableApplication.id}`,
    ]);
  });

  test('warns that the application was restored without its roles', async ({
    page,
    disposableApplication,
  }) => {
    const assignment = await failApiRequest(page, 'PUT', ASSIGN_ROLES, ROLES_FAILURE);

    await restore(page, disposableApplication);

    await expectToast(
      page,
      'warn',
      "Aplicació donada d'alta",
      "L'aplicació s'ha donat d'alta, però no s'han pogut assignar els seus rols de Soffid.",
    );
    await expect(page.getByRole('dialog')).toHaveCount(0);
    await expect(page.getByText("L'aplicació està donada de baixa")).toHaveCount(0);
    expect(assignment.hits).toHaveLength(1);
  });
});
