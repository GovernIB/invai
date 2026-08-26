import type { Page } from '@playwright/test';

import type { CleanupRegistry } from './cleanup-registry';
import { apiUrl, expectApiJsonResponse } from './http';
import {
  ACTIVE_STATUS_ID,
  type DatabaseRecord,
  ensureDatabaseVendor,
  ensureEnvironment,
  getServerType,
  type InfrastructureSystemRecord,
  type PhysicalServerRecord,
  registerSoftDeleteCleanup,
  type ServerTypeCode,
  SYSTEMS_API,
} from './systems';

export const APPLICATION_INFRASTRUCTURE_API = {
  aggregate: '/invaiapi/interna/application/system-database',
  applications: '/invaiapi/interna/application',
  databases: '/invaiapi/interna/application/database',
  systems: '/invaiapi/interna/application/system',
} as const;

export interface ApplicationInfrastructureAggregate {
  id: number;
  application: { id: number };
  observation: string | null;
  deletedAt: string | null;
}

export interface ApplicationSystemRelationRecord {
  id: number;
  informationSystemDb: ApplicationInfrastructureAggregate;
  system: InfrastructureSystemRecord;
  deletedAt: string | null;
}

export interface ApplicationDatabaseRelationRecord {
  id: number;
  informationSystemDb: ApplicationInfrastructureAggregate;
  database: DatabaseRecord;
  deletedAt: string | null;
}

interface SpringPage<T> {
  content: T[];
}

export async function ensureApplicationInfrastructureAggregate(
  page: Page,
  applicationId: number,
  existingAggregateId: number | null,
  observation: string | null = null,
): Promise<ApplicationInfrastructureAggregate> {
  if (existingAggregateId != null) {
    const pathname = `${APPLICATION_INFRASTRUCTURE_API.aggregate}/${existingAggregateId}`;
    return expectApiJsonResponse<ApplicationInfrastructureAggregate>(
      await page.request.get(apiUrl(page, pathname)),
      `GET ${pathname}`,
    );
  }

  const response = await page.request.post(
    apiUrl(page, APPLICATION_INFRASTRUCTURE_API.aggregate),
    { data: { applicationId, observation } },
  );
  return expectApiJsonResponse<ApplicationInfrastructureAggregate>(
    response,
    `POST ${APPLICATION_INFRASTRUCTURE_API.aggregate}`,
  );
}

export async function createInfrastructureSystem(
  page: Page,
  cleanup: CleanupRegistry,
  token: string,
): Promise<InfrastructureSystemRecord> {
  const server = await createUniquePhysicalServer(page, cleanup, token, 'APPLICATION');
  const instance = `E2E-${token}`;
  const payload = {
    name: server.name,
    serverId: server.id,
    instance,
    port: portFromToken(18_000, token),
    version: `E2E-${token}`,
    description: `E2E application system ${token}`,
  };
  const response = await page.request.post(apiUrl(page, SYSTEMS_API.systems), {
    data: payload,
  });
  const system = await expectApiJsonResponse<InfrastructureSystemRecord>(
    response,
    `POST ${SYSTEMS_API.systems}`,
  );
  const normalizedSystem: InfrastructureSystemRecord = { ...system, server };

  registerSoftDeleteCleanup<InfrastructureSystemRecord>({
    page,
    cleanup,
    label: `application system ${instance}`,
    pathname: SYSTEMS_API.systems,
    target: {
      id: normalizedSystem.id,
      matches: (item) => item.instance === instance,
    },
    findParams: { statusId: ACTIVE_STATUS_ID, instance },
  });

  return normalizedSystem;
}

export async function createInfrastructureDatabase(
  page: Page,
  cleanup: CleanupRegistry,
  token: string,
): Promise<DatabaseRecord> {
  const [server, vendor] = await Promise.all([
    createUniquePhysicalServer(page, cleanup, token, 'DATABASE'),
    ensureDatabaseVendor(page, cleanup, token),
  ]);
  const service = `E2E_${token}`;
  const payload = {
    serverId: server.id,
    service,
    port: portFromToken(25_000, token),
    databaseTypeId: vendor.id,
    description: `E2E application database ${token}`,
  };
  const response = await page.request.post(apiUrl(page, SYSTEMS_API.databases), {
    data: payload,
  });
  const database = await expectApiJsonResponse<DatabaseRecord>(
    response,
    `POST ${SYSTEMS_API.databases}`,
  );
  const normalizedDatabase: DatabaseRecord = {
    ...database,
    server,
    databaseType: vendor,
  };

  registerSoftDeleteCleanup<DatabaseRecord>({
    page,
    cleanup,
    label: `application database ${service}`,
    pathname: SYSTEMS_API.databases,
    target: {
      id: normalizedDatabase.id,
      matches: (item) => item.service === service,
    },
    findParams: { statusId: ACTIVE_STATUS_ID, search: service },
  });

  return normalizedDatabase;
}

async function createUniquePhysicalServer(
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
  const server = await expectApiJsonResponse<PhysicalServerRecord>(
    await page.request.post(apiUrl(page, SYSTEMS_API.servers), {
      data: {
        name,
        environmentId: environment.id,
        serverTypeId: serverType.id,
      },
    }),
    `POST ${SYSTEMS_API.servers}`,
  );

  registerSoftDeleteCleanup<PhysicalServerRecord>({
    page,
    cleanup,
    label: `${serverTypeCode.toLowerCase()} physical server ${name}`,
    pathname: SYSTEMS_API.servers,
    target: { id: server.id, matches: (item) => item.name === name },
    findParams: { statusId: ACTIVE_STATUS_ID, serverTypeCode, search: name },
  });

  return server;
}

function portFromToken(base: number, token: string): number {
  const offset = [...token].reduce((sum, character) => sum + character.charCodeAt(0), 0);
  return base + (offset % 1_000);
}

export async function createApplicationSystemRelation(
  page: Page,
  cleanup: CleanupRegistry,
  aggregateId: number,
  system: InfrastructureSystemRecord,
): Promise<ApplicationSystemRelationRecord> {
  const relation = await expectApiJsonResponse<ApplicationSystemRelationRecord>(
    await page.request.post(apiUrl(page, APPLICATION_INFRASTRUCTURE_API.systems), {
      data: { informationSystemDbId: aggregateId, systemId: system.id },
    }),
    `POST ${APPLICATION_INFRASTRUCTURE_API.systems}`,
  );
  registerApplicationSystemRelationCleanup(page, cleanup, aggregateId, {
    id: relation.id,
    systemId: system.id,
  });
  return relation;
}

export async function createApplicationDatabaseRelation(
  page: Page,
  cleanup: CleanupRegistry,
  aggregateId: number,
  database: DatabaseRecord,
): Promise<ApplicationDatabaseRelationRecord> {
  const relation = await expectApiJsonResponse<ApplicationDatabaseRelationRecord>(
    await page.request.post(apiUrl(page, APPLICATION_INFRASTRUCTURE_API.databases), {
      data: { informationSystemDbId: aggregateId, databaseId: database.id },
    }),
    `POST ${APPLICATION_INFRASTRUCTURE_API.databases}`,
  );
  registerApplicationDatabaseRelationCleanup(page, cleanup, aggregateId, {
    id: relation.id,
    databaseId: database.id,
  });
  return relation;
}

export function registerApplicationSystemRelationCleanup(
  page: Page,
  cleanup: CleanupRegistry,
  aggregateId: number,
  target: { id?: number; systemId: number },
): void {
  registerRelationCleanup<ApplicationSystemRelationRecord>({
    page,
    cleanup,
    aggregateId,
    label: `application system relation ${target.systemId}`,
    pathname: APPLICATION_INFRASTRUCTURE_API.systems,
    targetId: () => target.id,
    matches: (relation) => relation.system.id === target.systemId,
  });
}

export function registerApplicationDatabaseRelationCleanup(
  page: Page,
  cleanup: CleanupRegistry,
  aggregateId: number,
  target: { id?: number; databaseId: number },
): void {
  registerRelationCleanup<ApplicationDatabaseRelationRecord>({
    page,
    cleanup,
    aggregateId,
    label: `application database relation ${target.databaseId}`,
    pathname: APPLICATION_INFRASTRUCTURE_API.databases,
    targetId: () => target.id,
    matches: (relation) => relation.database.id === target.databaseId,
  });
}

export function registerApplicationReactivationForCleanup(
  page: Page,
  cleanup: CleanupRegistry,
  applicationId: number,
): void {
  cleanup.register(`reactivate application ${applicationId} before relation cleanup`, async () => {
    const pathname = `${APPLICATION_INFRASTRUCTURE_API.applications}/${applicationId}`;
    const response = await page.request.get(apiUrl(page, pathname));
    if (response.status() === 404) return;
    const application = await expectApiJsonResponse<{ status: 'ACTIVE' | 'INACTIVE' }>(
      response,
      `GET ${pathname}`,
    );
    if (application.status === 'ACTIVE') return;

    const reactivatePath = `${APPLICATION_INFRASTRUCTURE_API.applications}/reactivate/${applicationId}`;
    const reactivateResponse = await page.request.put(apiUrl(page, reactivatePath), {
      data: null,
    });
    await expectApiJsonResponse(reactivateResponse, `PUT ${reactivatePath}`);
  });
}

function registerRelationCleanup<T extends { id: number }>({
  page,
  cleanup,
  aggregateId,
  label,
  pathname,
  targetId,
  matches,
}: {
  page: Page;
  cleanup: CleanupRegistry;
  aggregateId: number;
  label: string;
  pathname: string;
  targetId: () => number | undefined;
  matches: (relation: T) => boolean;
}): void {
  cleanup.register(label, async () => {
    const response = await page.request.get(apiUrl(page, `${pathname}/${aggregateId}`), {
      params: { statusId: ACTIVE_STATUS_ID, page: 0, size: 100 },
    });
    const relations = await expectApiJsonResponse<SpringPage<T>>(
      response,
      `GET ${pathname}/${aggregateId}`,
    );
    const expectedId = targetId();
    const relation = relations.content.find(
      (candidate) => candidate.id === expectedId || (expectedId == null && matches(candidate)),
    );
    if (!relation) return;

    const deletePath = `${pathname}/${relation.id}`;
    const deleteResponse = await page.request.delete(apiUrl(page, deletePath));
    const body = await deleteResponse.text();
    if (!deleteResponse.ok()) {
      throw new Error(`DELETE ${deletePath} returned ${deleteResponse.status()}: ${body}`);
    }
  });
}
