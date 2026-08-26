import type { Page } from '@playwright/test';

import { ensureApiPrerequisite, findApiCatalogItem } from './catalog';
import type { CleanupRegistry } from './cleanup-registry';
import { apiUrl, expectApiJsonResponse } from './http';

export const ACTIVE_STATUS_ID = 1;
export const INACTIVE_STATUS_ID = 2;

export const SYSTEMS_API = {
  environments: '/invaiapi/interna/environment',
  servers: '/invaiapi/interna/server',
  systems: '/invaiapi/interna/system',
  databaseVendors: '/invaiapi/interna/database-vendor',
  databases: '/invaiapi/interna/database',
  serverTypes: '/invaiapi/interna/server-type',
} as const;

export type ServerTypeCode = 'APPLICATION' | 'DATABASE';

export interface EnvironmentRecord {
  id: number;
  code: string | null;
  name: string | null;
  nameEs: string | null;
  deletedAt: string | null;
}

export interface ServerTypeRecord {
  id: number;
  code: string;
  name: string | null;
  nameEs: string | null;
}

export interface PhysicalServerRecord {
  id: number;
  name: string;
  environment: EnvironmentRecord;
  serverType: ServerTypeRecord;
  deletedAt: string | null;
}

export interface DatabaseVendorRecord {
  id: number;
  name: string;
  defaultPort: number;
  deletedAt: string | null;
}

export interface DatabaseRecord {
  id: number;
  server: PhysicalServerRecord;
  service: string;
  port: number;
  databaseType: DatabaseVendorRecord;
  description: string | null;
  deletedAt: string | null;
}

export interface InfrastructureSystemRecord {
  id: number;
  server: PhysicalServerRecord;
  instance: string;
  port: number;
  version: string;
  description: string | null;
  deletedAt: string | null;
}

interface SoftDeleteRecord {
  id: number;
  deletedAt?: string | null;
}

interface CleanupTarget<T extends SoftDeleteRecord> {
  id?: number;
  matches: (item: T) => boolean;
}

export async function getServerType(
  page: Page,
  code: ServerTypeCode,
): Promise<ServerTypeRecord> {
  const response = await page.request.get(apiUrl(page, SYSTEMS_API.serverTypes));
  const serverTypes = await expectApiJsonResponse<ServerTypeRecord[]>(
    response,
    `GET ${SYSTEMS_API.serverTypes}`,
  );
  const serverType = serverTypes.find((candidate) => candidate.code === code);

  if (!serverType) throw new Error(`Server type ${code} is not available`);
  return serverType;
}

export async function ensureEnvironment(
  page: Page,
  cleanup: CleanupRegistry,
  token: string,
): Promise<EnvironmentRecord> {
  const payload = {
    code: `E2E${token}`,
    name: `E2E Entorn ${token}`,
    nameEs: `E2E Entorno ${token}`,
  };
  const ensured = await ensureApiPrerequisite<EnvironmentRecord>({
    page,
    pathname: SYSTEMS_API.environments,
    params: { statusId: ACTIVE_STATUS_ID },
    matches: (item) => !item.deletedAt,
    create: async () => {
      const response = await page.request.post(apiUrl(page, SYSTEMS_API.environments), {
        data: payload,
      });
      return expectApiJsonResponse<EnvironmentRecord>(
        response,
        `POST ${SYSTEMS_API.environments}`,
      );
    },
  });

  if (ensured.created) {
    registerSoftDeleteCleanup<EnvironmentRecord>({
      page,
      cleanup,
      label: `environment ${payload.code}`,
      pathname: SYSTEMS_API.environments,
      target: {
        id: ensured.item.id,
        matches: (item) => item.code === payload.code,
      },
      findParams: { statusId: ACTIVE_STATUS_ID, search: payload.code },
    });
  }

  return ensured.item;
}

export async function ensurePhysicalServer(
  page: Page,
  cleanup: CleanupRegistry,
  token: string,
  serverTypeCode: ServerTypeCode,
): Promise<PhysicalServerRecord> {
  const [environment, serverType] = await Promise.all([
    ensureEnvironment(page, cleanup, token),
    getServerType(page, serverTypeCode),
  ]);
  const name = `E2E ${serverTypeCode === 'APPLICATION' ? 'APP' : 'DB'} ${token}`;
  const ensured = await ensureApiPrerequisite<PhysicalServerRecord>({
    page,
    pathname: SYSTEMS_API.servers,
    params: { statusId: ACTIVE_STATUS_ID, serverTypeCode },
    matches: (item) => !item.deletedAt && item.serverType.code === serverTypeCode,
    create: async () => {
      const response = await page.request.post(apiUrl(page, SYSTEMS_API.servers), {
        data: {
          name,
          environmentId: environment.id,
          serverTypeId: serverType.id,
        },
      });
      return expectApiJsonResponse<PhysicalServerRecord>(
        response,
        `POST ${SYSTEMS_API.servers}`,
      );
    },
  });

  if (ensured.created) {
    registerSoftDeleteCleanup<PhysicalServerRecord>({
      page,
      cleanup,
      label: `${serverTypeCode.toLowerCase()} physical server ${name}`,
      pathname: SYSTEMS_API.servers,
      target: {
        id: ensured.item.id,
        matches: (item) => item.name === name,
      },
      findParams: {
        statusId: ACTIVE_STATUS_ID,
        serverTypeCode,
        search: name,
      },
    });
  }

  return ensured.item;
}

export async function ensureDatabaseVendor(
  page: Page,
  cleanup: CleanupRegistry,
  token: string,
): Promise<DatabaseVendorRecord> {
  const name = `E2E Vendor ${token}`;
  const ensured = await ensureApiPrerequisite<DatabaseVendorRecord>({
    page,
    pathname: SYSTEMS_API.databaseVendors,
    params: { statusId: ACTIVE_STATUS_ID },
    matches: (item) => !item.deletedAt,
    create: async () => {
      const response = await page.request.post(apiUrl(page, SYSTEMS_API.databaseVendors), {
        data: { name, defaultPort: 15432 },
      });
      return expectApiJsonResponse<DatabaseVendorRecord>(
        response,
        `POST ${SYSTEMS_API.databaseVendors}`,
      );
    },
  });

  if (ensured.created) {
    registerSoftDeleteCleanup<DatabaseVendorRecord>({
      page,
      cleanup,
      label: `database vendor ${name}`,
      pathname: SYSTEMS_API.databaseVendors,
      target: {
        id: ensured.item.id,
        matches: (item) => item.name === name,
      },
      findParams: { statusId: ACTIVE_STATUS_ID, search: name },
    });
  }

  return ensured.item;
}

export function registerSoftDeleteCleanup<T extends SoftDeleteRecord>({
  page,
  cleanup,
  label,
  pathname,
  target,
  findParams = { statusId: ACTIVE_STATUS_ID },
}: {
  page: Page;
  cleanup: CleanupRegistry;
  label: string;
  pathname: string;
  target: CleanupTarget<T>;
  findParams?: Record<string, string | number | boolean>;
}): void {
  cleanup.register(label, async () => {
    let id = target.id;

    if (id === undefined) {
      const item = await findApiCatalogItem<T>({
        page,
        pathname,
        params: findParams,
        matches: target.matches,
      });
      id = item?.id;
    }

    if (id === undefined) return;

    const detailResponse = await page.request.get(apiUrl(page, `${pathname}/${id}`));
    if (detailResponse.status() === 404) return;
    const detail = await expectApiJsonResponse<T>(detailResponse, `GET ${pathname}/${id}`);
    if (detail.deletedAt) return;

    const deleteResponse = await page.request.delete(apiUrl(page, `${pathname}/${id}`));
    const body = await deleteResponse.text();
    if (!deleteResponse.ok()) {
      throw new Error(`DELETE ${pathname}/${id} returned ${deleteResponse.status()}: ${body}`);
    }
  });
}
