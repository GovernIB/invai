import type { Page } from '@playwright/test';

import type { CleanupRegistry } from './cleanup-registry';
import { findApiCatalogItem } from './catalog';
import { apiUrl, expectApiJsonResponse } from './http';
import {
  ACTIVE_STATUS_ID,
  INACTIVE_STATUS_ID,
  registerSoftDeleteCleanup,
} from './systems';

export const DEVELOPMENT_MAINTENANCE_API = {
  roles: '/invaiapi/interna/role',
  layers: '/invaiapi/interna/layer',
  technologies: '/invaiapi/interna/technology',
} as const;

export interface DevelopmentRoleRecord {
  id: number;
  name: string | null;
  nameEs: string | null;
  deletedAt: string | null;
}

export interface DevelopmentLayerRecord {
  id: number;
  name: string | null;
  deletedAt: string | null;
}

export interface DevelopmentTechnologyRecord {
  id: number;
  name: string | null;
  layer: DevelopmentLayerRecord;
  deletedAt: string | null;
}

export interface AuxiliaryLayerFixture {
  layer: DevelopmentLayerRecord;
  fallbackLayer: DevelopmentLayerRecord;
}

export async function createAuxiliaryLayer(
  page: Page,
  cleanup: CleanupRegistry,
  token: string,
): Promise<AuxiliaryLayerFixture> {
  const fallbackLayer = await findApiCatalogItem<DevelopmentLayerRecord>({
    page,
    pathname: DEVELOPMENT_MAINTENANCE_API.layers,
    params: { statusId: ACTIVE_STATUS_ID },
    matches: (item) => !item.deletedAt && !item.name?.startsWith('E2E'),
  });
  if (!fallbackLayer) {
    throw new Error('An existing active non-E2E layer is required to clean technology links');
  }

  const name = `E2E Capa auxiliar ${token}`;
  const response = await page.request.post(
    apiUrl(page, DEVELOPMENT_MAINTENANCE_API.layers),
    { data: { name } },
  );
  const layer = await expectApiJsonResponse<DevelopmentLayerRecord>(
    response,
    `POST ${DEVELOPMENT_MAINTENANCE_API.layers}`,
  );

  registerSoftDeleteCleanup<DevelopmentLayerRecord>({
    page,
    cleanup,
    label: `auxiliary development layer ${name}`,
    pathname: DEVELOPMENT_MAINTENANCE_API.layers,
    target: { id: layer.id, matches: (item) => item.name === name },
    findParams: { statusId: ACTIVE_STATUS_ID, search: name },
  });

  return { layer, fallbackLayer };
}

export function registerTechnologyCleanup({
  page,
  cleanup,
  label,
  target,
  fallbackLayerId,
}: {
  page: Page;
  cleanup: CleanupRegistry;
  label: string;
  target: {
    id?: number;
    matches: (item: DevelopmentTechnologyRecord) => boolean;
  };
  fallbackLayerId: number;
}): void {
  cleanup.register(label, async () => {
    let id = target.id;
    if (id === undefined) {
      const active =
        await findApiCatalogItem<DevelopmentTechnologyRecord>({
          page,
          pathname: DEVELOPMENT_MAINTENANCE_API.technologies,
          params: { statusId: ACTIVE_STATUS_ID },
          matches: target.matches,
        });
      const inactive = active
        ? undefined
        : await findApiCatalogItem<DevelopmentTechnologyRecord>({
            page,
            pathname: DEVELOPMENT_MAINTENANCE_API.technologies,
            params: { statusId: INACTIVE_STATUS_ID },
            matches: target.matches,
          });
      id = (active ?? inactive)?.id;
    }
    if (id === undefined) return;

    const pathname = `${DEVELOPMENT_MAINTENANCE_API.technologies}/${id}`;
    const detailResponse = await page.request.get(apiUrl(page, pathname));
    if (detailResponse.status() === 404) return;
    let detail = await expectApiJsonResponse<DevelopmentTechnologyRecord>(
      detailResponse,
      `GET ${pathname}`,
    );

    if (detail.deletedAt) {
      const restorePath = `${DEVELOPMENT_MAINTENANCE_API.technologies}/reactivate/${id}`;
      const restoreResponse = await page.request.put(apiUrl(page, restorePath), { data: null });
      detail = await expectApiJsonResponse<DevelopmentTechnologyRecord>(
        restoreResponse,
        `PUT ${restorePath}`,
      );
    }

    const updateResponse = await page.request.put(apiUrl(page, pathname), {
      data: { name: detail.name, layerId: fallbackLayerId },
    });
    await expectApiJsonResponse<DevelopmentTechnologyRecord>(
      updateResponse,
      `PUT ${pathname} during cleanup`,
    );

    const deleteResponse = await page.request.delete(apiUrl(page, pathname));
    const body = await deleteResponse.text();
    if (!deleteResponse.ok()) {
      throw new Error(`DELETE ${pathname} returned ${deleteResponse.status()}: ${body}`);
    }
  });
}
