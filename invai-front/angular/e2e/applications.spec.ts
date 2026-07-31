import { expect, test } from '@playwright/test';
import type { Page, Response, TestInfo } from '@playwright/test';

const APPLICATION_API_PATH = '/invaiapi/interna/application';
const ACTIVE_STATUS_ID = 1;
const INACTIVE_STATUS_ID = 2;

interface ApplicationResponse {
  id: number;
  code: string | null;
  name: string | null;
  description: string | null;
  status: 'ACTIVE' | 'INACTIVE' | null;
}

interface ApplicationPageResponse {
  content: ApplicationResponse[];
  totalElements: number;
}

interface ApplicationInput {
  name: string;
  prefix: string;
  code: string;
  categoryId: number;
  systemTypeId: number;
  fieldId: number;
  admUnitId: number;
  commissionId: number;
  description: string | null;
  statusId: number;
}

test('covers the applications API lifecycle through the UI', async ({ page }, testInfo) => {
  test.setTimeout(120_000);

  const token = `${Date.now().toString(36)}${testInfo.workerIndex}`.slice(-7).toUpperCase();
  const code = `E2E${token}`;
  const prefix = token.slice(-3);
  const initialName = `E2E Playwright ${code}`;
  const updatedName = `${initialName} updated`;
  const initialDescription = `Created by Playwright run ${token}`;
  const updatedDescription = `Updated by Playwright run ${token}`;

  let createdId: number | undefined;
  let creationAttempted = false;
  let isActive = false;

  try {
    await test.step('GET returns the active applications page', async () => {
      const responsePromise = waitForApplicationResponse(
        page,
        'GET',
        APPLICATION_API_PATH,
        (url) => url.searchParams.get('statusId') === String(ACTIVE_STATUS_ID),
      );

      await page.goto('/');

      const response = await responsePromise;
      const body = await expectJsonResponse<ApplicationPageResponse>(response);

      expect(Array.isArray(body.content)).toBeTruthy();
      expect(body.totalElements).toEqual(expect.any(Number));
      expect(new URL(response.url()).searchParams.get('page')).toBe('0');
      expect(new URL(response.url()).searchParams.get('size')).toBe('10');
      await expect(page.getByRole('heading', { name: 'Aplicacions' })).toBeVisible();
    });

    await test.step('POST creates an active application', async () => {
      await page.getByRole('button', { name: 'Afegeix una aplicació' }).click();
      await expect(page.getByRole('heading', { name: 'Afegir aplicació' })).toBeVisible();

      await page.locator('#create-application-application').fill(initialName);
      await selectOption(page, 'create-application-category');
      await selectOption(page, 'create-application-information-system');
      await selectOption(page, 'create-application-scope');
      await page.locator('#create-application-prefix').fill(prefix);
      await page.locator('#create-application-code').fill(code);
      await selectOption(page, 'create-application-administrative-unit');
      await selectOption(page, 'create-application-commission');
      await page.locator('#create-application-description').fill(initialDescription);

      const responsePromise = waitForApplicationResponse(page, 'POST', APPLICATION_API_PATH);
      creationAttempted = true;
      await page.getByRole('button', { name: "Afegeix l'aplicació" }).click();

      const response = await responsePromise;
      const requestBody = response.request().postDataJSON() as ApplicationInput;
      const created = await expectJsonResponse<ApplicationResponse>(response);

      createdId = created.id;
      isActive = true;

      expect(requestBody).toMatchObject({
        name: initialName,
        prefix,
        code,
        description: initialDescription,
        statusId: ACTIVE_STATUS_ID,
      });
      expectPositiveReferenceIds(requestBody);
      expect(created).toMatchObject({
        id: expect.any(Number),
        code,
        name: initialName,
        description: initialDescription,
        status: 'ACTIVE',
      });

      await expect(page).toHaveURL(/\/aplicacions$/);
    });

    await test.step('GET finds the created application from the list', async () => {
      const responsePromise = waitForApplicationResponse(
        page,
        'GET',
        APPLICATION_API_PATH,
        (url) => url.searchParams.get('quickSearch') === code,
      );

      await page.getByLabel("Cerca ràpida d'aplicacions").fill(code);

      const response = await responsePromise;
      const body = await expectJsonResponse<ApplicationPageResponse>(response);

      expect(body.content).toContainEqual(
        expect.objectContaining({ id: createdId, code, status: 'ACTIVE' }),
      );
      await expect(applicationRow(page, code)).toBeVisible();
    });

    await test.step('PUT edits the application', async () => {
      const detailResponsePromise = waitForApplicationResponse(
        page,
        'GET',
        `${APPLICATION_API_PATH}/${createdId}`,
      );
      await applicationRow(page, code).dblclick();

      const detailResponse = await detailResponsePromise;
      const detail = await expectJsonResponse<ApplicationResponse>(detailResponse);
      expect(detail).toMatchObject({ id: createdId, code, name: initialName, status: 'ACTIVE' });

      await page
        .getByRole('button', { name: "Editar Dades generals de l'aplicació" })
        .click();
      await page.locator('#detail-application-application').fill(updatedName);
      await page.locator('#detail-application-description').fill(updatedDescription);

      const responsePromise = waitForApplicationResponse(
        page,
        'PUT',
        `${APPLICATION_API_PATH}/${createdId}`,
      );
      await page
        .getByRole('button', {
          name: "Desar els canvis de Dades generals de l'aplicació",
        })
        .click();

      const response = await responsePromise;
      const requestBody = response.request().postDataJSON() as ApplicationInput;
      const updated = await expectJsonResponse<ApplicationResponse>(response);

      expect(requestBody).toMatchObject({
        name: updatedName,
        prefix,
        code,
        description: updatedDescription,
        statusId: ACTIVE_STATUS_ID,
      });
      expectPositiveReferenceIds(requestBody);
      expect(updated).toMatchObject({
        id: createdId,
        code,
        name: updatedName,
        description: updatedDescription,
        status: 'ACTIVE',
      });
      await expect(page.locator('#detail-application-application')).toHaveValue(updatedName);
      await expect(page.locator('#detail-application-application')).toBeDisabled();
    });

    await test.step('DELETE withdraws the application', async () => {
      await page
        .getByRole('button', { name: "Editar Dades generals de l'aplicació" })
        .click();
      await page.getByRole('button', { name: "Donar de baixa l'aplicació" }).click();

      const deleteResponsePromise = waitForApplicationResponse(
        page,
        'DELETE',
        `${APPLICATION_API_PATH}/${createdId}`,
      );
      const activeListResponsePromise = waitForApplicationResponse(
        page,
        'GET',
        APPLICATION_API_PATH,
        (url) => url.searchParams.get('statusId') === String(ACTIVE_STATUS_ID),
      );
      await page.getByRole('button', { name: "Confirma la baixa de l'aplicació" }).click();

      const deleteResponse = await deleteResponsePromise;
      await expectSuccessfulResponse(deleteResponse);
      isActive = false;

      const activeListResponse = await activeListResponsePromise;
      const activePage =
        await expectJsonResponse<ApplicationPageResponse>(activeListResponse);
      expect(activePage.content).not.toContainEqual(expect.objectContaining({ id: createdId }));
      await expect(page).toHaveURL(/\/aplicacions$/);
    });

    await test.step('PUT reactivate restores the inactive application', async () => {
      await page.getByRole('button', { name: 'Mostra o amaga els filtres' }).click();
      await selectOption(page, 'applications-filter-status', 'Inactiu');
      await page.getByRole('button', { name: 'Cerca' }).click();

      const inactiveListResponsePromise = waitForApplicationResponse(
        page,
        'GET',
        APPLICATION_API_PATH,
        (url) =>
          url.searchParams.get('statusId') === String(INACTIVE_STATUS_ID) &&
          url.searchParams.get('quickSearch') === code,
      );
      await page.getByLabel("Cerca ràpida d'aplicacions").fill(code);

      const inactiveListResponse = await inactiveListResponsePromise;
      const inactivePage =
        await expectJsonResponse<ApplicationPageResponse>(inactiveListResponse);
      expect(inactivePage.content).toContainEqual(
        expect.objectContaining({ id: createdId, code, status: 'INACTIVE' }),
      );

      const detailResponsePromise = waitForApplicationResponse(
        page,
        'GET',
        `${APPLICATION_API_PATH}/${createdId}`,
      );
      await applicationRow(page, code).dblclick();
      await expectJsonResponse<ApplicationResponse>(await detailResponsePromise);

      await expect(page.getByText("L'aplicació està donada de baixa")).toBeVisible();
      await page.getByRole('button', { name: "Donar d'alta l'aplicació" }).click();

      const responsePromise = waitForApplicationResponse(
        page,
        'PUT',
        `${APPLICATION_API_PATH}/reactivate/${createdId}`,
      );
      await page.getByRole('button', { name: "Confirma l'alta de l'aplicació" }).click();

      const response = await responsePromise;
      const reactivated = await expectJsonResponse<ApplicationResponse>(response);
      isActive = true;

      expect(response.request().postDataJSON()).toEqual({});
      expect(reactivated).toMatchObject({
        id: createdId,
        code,
        name: updatedName,
        status: 'ACTIVE',
      });
      await expect(page.getByText("L'aplicació està donada de baixa")).toBeHidden();
      await expect(
        page.getByRole('button', { name: "Editar Dades generals de l'aplicació" }),
      ).toBeVisible();
    });
  } finally {
    await test.step('cleanup withdraws the test application', async () => {
      await cleanupActiveApplication({
        page,
        testInfo,
        code,
        createdId,
        creationAttempted,
        isActive,
      });
    });
  }
});

function waitForApplicationResponse(
  page: Page,
  method: string,
  pathname: string,
  matchesUrl: (url: URL) => boolean = () => true,
): Promise<Response> {
  return page.waitForResponse(
    (response) => {
      const url = new URL(response.url());
      return (
        response.request().method() === method &&
        url.pathname === pathname &&
        matchesUrl(url)
      );
    },
    { timeout: 30_000 },
  );
}

async function expectJsonResponse<T>(response: Response): Promise<T> {
  const body = await response.text();
  expect(
    response.ok(),
    `${response.request().method()} ${response.url()} returned ${response.status()}: ${body}`,
  ).toBeTruthy();

  return JSON.parse(body) as T;
}

async function expectSuccessfulResponse(response: Response): Promise<void> {
  const body = await response.text();
  expect(
    response.ok(),
    `${response.request().method()} ${response.url()} returned ${response.status()}: ${body}`,
  ).toBeTruthy();
}

function expectPositiveReferenceIds(input: ApplicationInput): void {
  for (const id of [
    input.categoryId,
    input.systemTypeId,
    input.fieldId,
    input.admUnitId,
    input.commissionId,
  ]) {
    expect(id).toEqual(expect.any(Number));
    expect(id).toBeGreaterThan(0);
  }
}

async function selectOption(page: Page, inputId: string, optionName?: string): Promise<void> {
  await page.locator(`#${inputId}`).click();

  const option = optionName
    ? page.getByRole('option', { name: optionName, exact: true })
    : page.getByRole('option').first();

  await expect(option, `No options are available for #${inputId}`).toBeVisible();
  await option.click();
}

function applicationRow(page: Page, code: string) {
  return page.getByRole('row').filter({ hasText: code });
}

async function cleanupActiveApplication({
  page,
  testInfo,
  code,
  createdId,
  creationAttempted,
  isActive,
}: {
  page: Page;
  testInfo: TestInfo;
  code: string;
  createdId: number | undefined;
  creationAttempted: boolean;
  isActive: boolean;
}): Promise<void> {
  if (!creationAttempted || !isActive) return;

  const id = createdId ?? (await findActiveApplicationId(page, code));
  if (!id) return;

  const response = await page.request.delete(apiUrl(page, `${APPLICATION_API_PATH}/${id}`));
  if (response.ok()) return;

  const body = await response.body();
  await testInfo.attach('application-cleanup-error', {
    body,
    contentType: response.headers()['content-type'] ?? 'text/plain',
  });
  expect
    .soft(
      response.ok(),
      `Cleanup DELETE ${APPLICATION_API_PATH}/${id} returned ${response.status()}`,
    )
    .toBeTruthy();
}

async function findActiveApplicationId(page: Page, code: string): Promise<number | undefined> {
  const response = await page.request.get(apiUrl(page, APPLICATION_API_PATH), {
    params: {
      page: 0,
      size: 10,
      statusId: ACTIVE_STATUS_ID,
      quickSearch: code,
    },
  });
  if (!response.ok()) return undefined;

  const body = (await response.json()) as ApplicationPageResponse;
  return body.content.find((application) => application.code === code)?.id;
}

function apiUrl(page: Page, pathname: string): string {
  return new URL(pathname, page.url()).toString();
}
