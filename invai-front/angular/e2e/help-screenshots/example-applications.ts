import { expect } from '@playwright/test';
import type { Page } from '@playwright/test';

import { findAvailableApplicationPrefix } from '../support/application-prefix';
import type { CleanupRegistry } from '../support/cleanup-registry';
import { apiUrl, expectApiJsonResponse } from '../support/http';

export const APPLICATION_API_PATH = '/invaiback/application';
const ACTIVE_STATUS_ID = 1;

interface ApplicationDetail {
  id: number;
  category: { id: number } | null;
  systemType: { id: number } | null;
  field: { id: number } | null;
  admUnit: { code: string } | null;
  csCommission: { id: number } | null;
}

export interface Catalogs {
  categoryId: number;
  systemTypeId: number;
  fieldId: number;
  admUnitCode: string;
  commissionId: number;
}

export interface ExampleApplication {
  id: number;
  name: string;
  code: string;
}

/** Catalog values of an existing active application, so the examples only need valid ids. */
export async function findCatalogs(page: Page): Promise<Catalogs> {
  const list = await expectApiJsonResponse<{ content: Array<{ id: number }> }>(
    await page.request.get(apiUrl(page, APPLICATION_API_PATH), {
      params: { page: 0, size: 20, statusId: ACTIVE_STATUS_ID },
    }),
    `GET ${APPLICATION_API_PATH}`,
  );
  for (const { id } of list.content) {
    const detail = await expectApiJsonResponse<ApplicationDetail>(
      await page.request.get(apiUrl(page, `${APPLICATION_API_PATH}/${id}`)),
      `GET ${APPLICATION_API_PATH}/${id}`,
    );
    if (
      detail.category &&
      detail.systemType &&
      detail.field &&
      detail.admUnit &&
      detail.csCommission
    ) {
      return {
        categoryId: detail.category.id,
        systemTypeId: detail.systemType.id,
        fieldId: detail.field.id,
        admUnitCode: detail.admUnit.code,
        commissionId: detail.csCommission.id,
      };
    }
  }
  throw new Error('No active application has every catalog value to copy');
}

/**
 * Creates an example application through the API, with the catalogs of a real one, and registers
 * its deactivation for the end of the test: real applications never appear in the help.
 */
export async function createExampleApplication(
  page: Page,
  cleanup: CleanupRegistry,
  { name, code, catalogs }: { name: string; code: string; catalogs: Catalogs },
): Promise<ExampleApplication> {
  const prefix = await findAvailableApplicationPrefix(page, code);
  const created = await expectApiJsonResponse<{ id: number }>(
    await page.request.post(apiUrl(page, APPLICATION_API_PATH), {
      data: { name, prefix, code, ...catalogs, description: '', statusId: ACTIVE_STATUS_ID },
    }),
    `POST ${APPLICATION_API_PATH}`,
  );
  cleanup.register(`help example ${name}`, async () => {
    const response = await page.request.delete(
      apiUrl(page, `${APPLICATION_API_PATH}/${created.id}`),
    );
    expect(response.ok(), `DELETE ${APPLICATION_API_PATH}/${created.id}`).toBeTruthy();
  });
  return { id: created.id, name, code };
}
