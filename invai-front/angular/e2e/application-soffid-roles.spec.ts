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

const CATEGORY = { id: 1, name: 'Gestió', nameEs: 'Gestión', deletedAt: null };
const SYSTEM_TYPE = { id: 2, name: 'Intern', nameEs: 'Interno', deletedAt: null };
const FIELD = { id: 3, name: 'Hisenda', nameEs: 'Hacienda', deletedAt: null };
const COMMISSION = {
  id: 4,
  name: 'Comissió tècnica',
  nameEs: 'Comisión técnica',
  expedientNumber: 'EXP-4',
  approvalDate: '2026-01-15',
  commissionType: 'TECNICA',
  deletedAt: null,
};
const ADM_UNIT = { code: 'A1', name: 'Unitat de proves', parentCode: null, level: 1 };

const APPLICATION = {
  id: 1,
  code: 'INVAI',
  prefix: 'INV',
  name: 'Inventari',
  status: 'ACTIVE',
  category: CATEGORY,
  systemType: SYSTEM_TYPE,
  field: FIELD,
  csCommission: COMMISSION,
  admUnit: ADM_UNIT,
  description: '<p>Inventari de proves</p>',
};
const CREATED_ID = 42;

// The page only counts the assignments; their content mirrors ApplicationSecurityRoleOutput.
const assignedRoles = (count: number) =>
  Array.from({ length: count }, (_, index) => ({
    id: 100 + index,
    appSecurity: { id: 7 },
    securityRole: { id: 200 + index, roleId: 300 + index, name: `ROLE_${index}`, system: 'SEYCON' },
    deletedAt: null,
    validationStatus: 'VALIDATED',
    manualValidatedAt: null,
    manualValidatedBy: null,
  }));

const ROLES_FAILURE = {
  status: 400,
  json: { error: 'Error de Soffid', message: "No s'ha pogut consultar Soffid." },
};

interface RolesMock {
  application: typeof APPLICATION;
  // Answer to PUT application/assign-roles/{id}: the number of roles created, or a failure.
  assignReply: number | { status: number; json: unknown };
  // While set, assign-roles waits for it before answering.
  assignGate: Promise<void> | null;
  assignRequests: { path: string; body: unknown }[];
  creations: unknown[];
  updates: unknown[];
  // Application and assignment requests in the order they arrived.
  calls: string[];
}

async function mockBackend(page: Page, application = APPLICATION): Promise<RolesMock> {
  const mock: RolesMock = {
    application: { ...application },
    assignReply: 0,
    assignGate: null,
    assignRequests: [],
    creations: [],
    updates: [],
    calls: [],
  };
  await page.route('**/invaiback/**', async (route: Route) => {
    const request = route.request();
    const path = new URL(request.url()).pathname;
    const method = request.method();
    if (path.endsWith('/auth/me')) {
      return route.fulfill({ json: { authenticated: true, username: 'soffid-roles-test' } });
    }
    if (/\/application\/assign-roles\/\d+$/.test(path) && method === 'PUT') {
      mock.calls.push(`${method} ${path}`);
      mock.assignRequests.push({ path, body: request.postDataJSON() });
      if (mock.assignGate) await mock.assignGate;
      const reply = mock.assignReply;
      if (typeof reply === 'number') return route.fulfill({ json: assignedRoles(reply) });
      return route.fulfill(reply);
    }
    if (path.endsWith('/application') && method === 'POST') {
      mock.calls.push(`${method} ${path}`);
      const body = request.postDataJSON();
      mock.creations.push(body);
      return route.fulfill({
        status: 201,
        json: { ...APPLICATION, ...body, id: CREATED_ID, status: 'ACTIVE' },
      });
    }
    if (path.endsWith('/application')) {
      return route.fulfill({ json: pageOf([mock.application]) });
    }
    if (path.endsWith('/application/reactivate/1') && method === 'PUT') {
      mock.calls.push(`${method} ${path}`);
      mock.application = { ...mock.application, status: 'ACTIVE' };
      return route.fulfill({ json: mock.application });
    }
    if (path.endsWith('/application/1')) {
      mock.calls.push(`${method} ${path}`);
      if (method === 'PUT') {
        const body = request.postDataJSON();
        mock.updates.push(body);
        mock.application = { ...mock.application, name: body.name, prefix: body.prefix };
      }
      return route.fulfill({ json: mock.application });
    }
    if (path.endsWith('/category')) return route.fulfill({ json: pageOf([CATEGORY]) });
    if (path.endsWith('/system-type')) return route.fulfill({ json: pageOf([SYSTEM_TYPE]) });
    if (path.endsWith('/field')) return route.fulfill({ json: pageOf([FIELD]) });
    if (path.endsWith('/commission')) return route.fulfill({ json: pageOf([COMMISSION]) });
    if (path.endsWith('/adm-unit')) return route.fulfill({ json: pageOf([ADM_UNIT]) });
    return route.fulfill({ json: pageOf([]) });
  });
  return mock;
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

async function fillCreateForm(page: Page): Promise<void> {
  await page.goto('/aplicacions/new');
  const form = page.locator('app-application-create-form');
  await form.getByRole('textbox', { name: 'Aplicació', exact: true }).fill('Gestor de proves');
  await selectOption(page, 'create-application-category', 'Gestió');
  await selectOption(page, 'create-application-information-system', 'Intern');
  await selectOption(page, 'create-application-scope', 'Hisenda');
  await form.getByRole('textbox', { name: 'Prefix', exact: true }).fill('GDP');
  await form.getByRole('textbox', { name: 'Codi', exact: true }).fill('GDP001');
  await selectOption(page, 'create-application-administrative-unit', 'Unitat de proves (A1)');
  await selectOption(page, 'create-application-commission', 'Comissió tècnica');
}

const submitButton = (page: Page) => page.getByRole('button', { name: "Afegeix l'aplicació" });

test.describe('after creating an application', () => {
  const outcomes = [
    {
      roles: 0,
      detail: 'Aplicació afegida correctament. No hi ha cap rol de Soffid amb el prefix GDP.',
    },
    { roles: 1, detail: 'Aplicació afegida correctament amb 1 rol de Soffid assignat.' },
    { roles: 3, detail: 'Aplicació afegida correctament amb 3 rols de Soffid assignats.' },
  ];

  for (const { roles, detail } of outcomes) {
    test(`assigns the Soffid roles of the new application and reports ${roles}`, async ({
      page,
    }) => {
      const mock = await mockBackend(page);
      mock.assignReply = roles;
      let releaseAssignment = () => {};
      mock.assignGate = new Promise((resolve) => (releaseAssignment = resolve));
      await fillCreateForm(page);

      await submitButton(page).click();
      await expect.poll(() => mock.assignRequests.length).toBe(1);
      // The form keeps saving until the roles are assigned.
      await expect(submitButton(page)).toBeDisabled();
      await expect(page).toHaveURL(/\/aplicacions\/new$/);
      releaseAssignment();

      await expect(page).toHaveURL(/\/aplicacions$/);
      await expectToast(page, 'success', 'Aplicació afegida', detail);
      expect(mock.creations).toEqual([
        {
          name: 'Gestor de proves',
          prefix: 'GDP',
          code: 'GDP001',
          categoryId: 1,
          systemTypeId: 2,
          fieldId: 3,
          admUnitCode: 'A1',
          commissionId: 4,
          description: '',
          statusId: 1,
        },
      ]);
      expect(mock.assignRequests).toEqual([
        { path: `/invaiback/application/assign-roles/${CREATED_ID}`, body: {} },
      ]);
      expect(mock.calls).toEqual([
        'POST /invaiback/application',
        `PUT /invaiback/application/assign-roles/${CREATED_ID}`,
      ]);
    });
  }

  test('warns that the roles were not assigned but still returns to the list', async ({
    page,
  }) => {
    const mock = await mockBackend(page);
    mock.assignReply = ROLES_FAILURE;
    await fillCreateForm(page);

    await submitButton(page).click();

    await expect(page).toHaveURL(/\/aplicacions$/);
    await expectToast(
      page,
      'warn',
      'Aplicació afegida',
      "L'aplicació s'ha afegit, però no s'han pogut assignar els seus rols de Soffid.",
    );
    await expect(page.getByRole('dialog')).toHaveCount(0);
    expect(mock.creations).toHaveLength(1);
    expect(mock.assignRequests).toHaveLength(1);
  });
});

test.describe('General tab', () => {
  const GENERAL = "Dades generals de l'aplicació";
  const SAVED = `S'han desat correctament els canvis de ${GENERAL}.`;

  async function editGeneral(page: Page, mock: RolesMock) {
    await page.goto('/aplicacions/1/general');
    const prefix = page.getByRole('textbox', { name: 'Prefix', exact: true });
    await expect(prefix).toHaveValue('INV');
    await page.getByRole('button', { name: `Editar ${GENERAL}`, exact: true }).click();
    await expect(prefix).not.toHaveAttribute('readonly');
    mock.calls = [];
    return prefix;
  }

  const save = (page: Page) =>
    page.getByRole('button', { name: `Desar els canvis de ${GENERAL}` }).click();

  test('assigns the Soffid roles of a new prefix before re-reading the application', async ({
    page,
  }) => {
    const mock = await mockBackend(page);
    mock.assignReply = 2;
    const prefix = await editGeneral(page, mock);

    await prefix.fill('NEW');
    await save(page);

    await expectToast(
      page,
      'success',
      'Canvis desats',
      `${SAVED} S'han assignat 2 rols de Soffid amb el prefix NEW.`,
    );
    await expect(prefix).toHaveValue('NEW');
    await expect(prefix).toHaveAttribute('readonly');
    expect(mock.updates).toEqual([expect.objectContaining({ prefix: 'NEW', code: 'INVAI' })]);
    expect(mock.assignRequests).toEqual([
      { path: '/invaiback/application/assign-roles/1', body: {} },
    ]);
    expect(mock.calls).toEqual([
      'PUT /invaiback/application/1',
      'PUT /invaiback/application/assign-roles/1',
      'GET /invaiback/application/1',
    ]);
  });

  test('reports when no new Soffid role matches the new prefix', async ({ page }) => {
    const mock = await mockBackend(page);
    mock.assignReply = 0;
    const prefix = await editGeneral(page, mock);

    await prefix.fill('NEW');
    await save(page);

    await expectToast(
      page,
      'success',
      'Canvis desats',
      `${SAVED} No s'ha assignat cap rol nou de Soffid amb el prefix NEW.`,
    );
    expect(mock.assignRequests).toHaveLength(1);
  });

  test('does not assign roles when the prefix is unchanged', async ({ page }) => {
    const mock = await mockBackend(page);
    const prefix = await editGeneral(page, mock);

    await page
      .getByRole('textbox', { name: "Nom de l'aplicació", exact: true })
      .fill('Inventari renovat');
    await save(page);

    await expectToast(page, 'success', 'Canvis desats', SAVED);
    await expect(prefix).toHaveAttribute('readonly');
    expect(mock.updates).toEqual([
      expect.objectContaining({ name: 'Inventari renovat', prefix: 'INV' }),
    ]);
    expect(mock.assignRequests).toHaveLength(0);
    expect(mock.calls).toEqual(['PUT /invaiback/application/1', 'GET /invaiback/application/1']);
  });

  test('warns that the change was saved without the roles when the assignment fails', async ({
    page,
  }) => {
    const mock = await mockBackend(page);
    mock.assignReply = ROLES_FAILURE;
    const prefix = await editGeneral(page, mock);

    await prefix.fill('NEW');
    await save(page);

    await expectToast(
      page,
      'warn',
      'Canvis desats',
      "Els canvis s'han desat, però no s'han pogut assignar els rols de Soffid del prefix NEW.",
    );
    // The request opts out of the global server error dialog.
    await expect(page.getByRole('dialog')).toHaveCount(0);
    await expect(prefix).toHaveValue('NEW');
    await expect(prefix).toHaveAttribute('readonly');
    expect(mock.calls).toEqual([
      'PUT /invaiback/application/1',
      'PUT /invaiback/application/assign-roles/1',
      'GET /invaiback/application/1',
    ]);
  });
});

test.describe('restoring an inactive application', () => {
  async function restore(page: Page, mock: RolesMock) {
    await page.goto('/aplicacions/1/general');
    await expect(page.getByText("L'aplicació està donada de baixa")).toBeVisible();
    mock.calls = [];
    await page.getByRole('button', { name: "Donar d'alta l'aplicació" }).click();
    await page.getByRole('button', { name: "Confirma l'alta de l'aplicació" }).click();
  }

  test('assigns the Soffid roles of the restored application', async ({ page }) => {
    const mock = await mockBackend(page, { ...APPLICATION, status: 'INACTIVE' });
    mock.assignReply = 1;

    await restore(page, mock);

    await expectToast(
      page,
      'success',
      "Aplicació donada d'alta",
      "L'aplicació s'ha donat d'alta correctament. " +
        "S'ha assignat 1 rol de Soffid amb el prefix INV.",
    );
    await expect(page.getByText("L'aplicació està donada de baixa")).toHaveCount(0);
    await expect(
      page.getByRole('button', { name: "Editar Dades generals de l'aplicació", exact: true }),
    ).toBeVisible();
    expect(mock.assignRequests).toEqual([
      { path: '/invaiback/application/assign-roles/1', body: {} },
    ]);
    expect(mock.calls).toEqual([
      'PUT /invaiback/application/reactivate/1',
      'PUT /invaiback/application/assign-roles/1',
      'GET /invaiback/application/1',
    ]);
  });

  test('warns that the application was restored without its roles', async ({ page }) => {
    const mock = await mockBackend(page, { ...APPLICATION, status: 'INACTIVE' });
    mock.assignReply = ROLES_FAILURE;

    await restore(page, mock);

    await expectToast(
      page,
      'warn',
      "Aplicació donada d'alta",
      "L'aplicació s'ha donat d'alta, però no s'han pogut assignar els seus rols de Soffid.",
    );
    await expect(page.getByRole('dialog')).toHaveCount(0);
    await expect(page.getByText("L'aplicació està donada de baixa")).toHaveCount(0);
    expect(mock.assignRequests).toHaveLength(1);
  });
});
