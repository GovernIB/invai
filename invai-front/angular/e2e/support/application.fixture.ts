import type { Page } from '@playwright/test';

import { CleanupRegistry } from './cleanup-registry';
import { uniqueToken } from './data';
import { findAvailableApplicationPrefix } from './application-prefix';
import { apiUrl, expectApiJsonResponse, expectJsonResponse, waitForApiResponse } from './http';
import { selectOption } from './ui';
import { expect, test as authenticatedTest } from './auth.fixture';

const APPLICATION_API_PATH = '/invaiapi/interna/application';
const ACTIVE_STATUS_ID = 1;

interface ApplicationOutput {
  id: number;
  code: string | null;
  name: string | null;
  status: 'ACTIVE' | 'INACTIVE' | null;
  appInformationSystemDbId: number | null;
  appDevelopmentId: number | null;
}

interface ApplicationPageResponse {
  content: ApplicationOutput[];
}

export interface DisposableApplication {
  id: number;
  code: string;
  name: string;
  status: 'ACTIVE' | 'INACTIVE' | null;
  appInformationSystemDbId: number | null;
  appDevelopmentId: number | null;
}

interface ApplicationFixtures {
  cleanupRegistry: CleanupRegistry;
  disposableApplication: DisposableApplication;
}

export const test = authenticatedTest.extend<ApplicationFixtures>({
  cleanupRegistry: async ({ page }, use, testInfo) => {
    void page;
    const registry = new CleanupRegistry(testInfo);
    await use(registry);
    await registry.runAll();
  },

  disposableApplication: async ({ page, cleanupRegistry }, use, testInfo) => {
    const token = uniqueToken(testInfo, 7);
    const code = `E2E${token}`;
    const name = `E2E Playwright fixture ${code}`;
    let createdId: number | undefined;

    cleanupRegistry.register(`application-${code}`, async () => {
      const id = createdId ?? (await findActiveApplicationId(page, code));
      if (!id) return;

      await deactivateApplicationIfActive(page, id);
    });

    await page.goto('/');
    const prefix = await findAvailableApplicationPrefix(page, token);
    await page.getByRole('button', { name: 'Afegeix una aplicació' }).click();
    await expect(page.getByRole('heading', { name: 'Afegir aplicació' })).toBeVisible();

    await page.locator('#create-application-application').fill(name);
    await selectOption(page, 'create-application-category');
    await selectOption(page, 'create-application-information-system');
    await selectOption(page, 'create-application-scope');
    await page.locator('#create-application-prefix').fill(prefix);
    await page.locator('#create-application-code').fill(code);
    await selectOption(page, 'create-application-administrative-unit');
    await selectOption(page, 'create-application-commission');
    await page
      .locator('#create-application-description')
      .fill(`Created by the disposable Playwright fixture ${token}`);

    const createResponsePromise = waitForApiResponse(page, 'POST', APPLICATION_API_PATH);
    await page.getByRole('button', { name: "Afegeix l'aplicació" }).click();

    const created = await expectJsonResponse<ApplicationOutput>(await createResponsePromise);
    createdId = created.id;

    const detailResponse = await page.request.get(
      apiUrl(page, `${APPLICATION_API_PATH}/${created.id}`),
    );
    const detail = await expectApiJsonResponse<ApplicationOutput>(
      detailResponse,
      `GET ${APPLICATION_API_PATH}/${created.id}`,
    );

    await use({
      id: detail.id,
      code: detail.code ?? code,
      name: detail.name ?? name,
      status: detail.status,
      appInformationSystemDbId: detail.appInformationSystemDbId,
      appDevelopmentId: detail.appDevelopmentId,
    });
  },
});

export { expect };

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

async function deactivateApplicationIfActive(page: Page, id: number): Promise<void> {
  const detailResponse = await page.request.get(apiUrl(page, `${APPLICATION_API_PATH}/${id}`));
  if (detailResponse.status() === 404) return;

  const detail = await expectApiJsonResponse<ApplicationOutput>(
    detailResponse,
    `GET ${APPLICATION_API_PATH}/${id} during cleanup`,
  );
  if (detail.status !== 'ACTIVE') return;

  const deleteResponse = await page.request.delete(apiUrl(page, `${APPLICATION_API_PATH}/${id}`));
  if (deleteResponse.ok()) return;

  throw new Error(
    `DELETE ${APPLICATION_API_PATH}/${id} returned ${deleteResponse.status()}: ${await deleteResponse.text()}`,
  );
}
