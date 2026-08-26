import type { Page } from '@playwright/test';

import type { CleanupRegistry } from './cleanup-registry';
import { apiUrl, expectApiJsonResponse } from './http';

export const APPLICATION_DEVELOPMENT_API = {
  aggregate: '/invaiapi/interna/application/development',
  applications: '/invaiapi/interna/application',
  environments: '/invaiapi/interna/environment',
  providers: '/invaiapi/interna/application/development/provider',
  roles: '/invaiapi/interna/role',
  technologies: '/invaiapi/interna/application/development/technology',
  technologyCatalog: '/invaiapi/interna/technology',
} as const;

export interface ApplicationDevelopmentAggregate {
  id: number;
  application: { id: number };
  environment: { id: number; code: string | null; name: string | null } | null;
  modality: { id: number; name: string | null; nameEs: string | null } | null;
  code: string | null;
  standardAdaption: {
    id: number;
    name: string | null;
    nameEs: string | null;
  } | null;
  revisionDate: string | null;
  observation: string | null;
  deletedAt: string | null;
}

export interface ApplicationProviderRecord {
  id: number;
  companyName: string;
  role: { id: number; name: string | null; nameEs: string | null } | null;
  startDate: string | null;
  expireDate: string | null;
  deletedAt: string | null;
}

export interface ApplicationTechnologyRecord {
  id: number;
  layer: { id: number; name: string | null };
  technology: { id: number; name: string | null };
  version: string;
  architecture: string;
  deletedAt: string | null;
}

interface ResourcePage<T> {
  content: T[];
  totalPages?: number;
}

export function requireDevelopmentId(value: number | null): number {
  if (!Number.isInteger(value) || Number(value) <= 0) {
    throw new Error('The disposable application did not expose its development aggregate ID.');
  }

  return Number(value);
}

export function registerApplicationProviderCleanup({
  page,
  cleanup,
  appDevelopmentId,
  target,
}: {
  page: Page;
  cleanup: CleanupRegistry;
  appDevelopmentId: number;
  target: { id?: number; matches: (item: ApplicationProviderRecord) => boolean };
}): void {
  registerResourceCleanup({
    page,
    cleanup,
    label: `application provider for development ${appDevelopmentId}`,
    pathname: APPLICATION_DEVELOPMENT_API.providers,
    appDevelopmentId,
    target,
  });
}

export function registerApplicationTechnologyCleanup({
  page,
  cleanup,
  appDevelopmentId,
  target,
}: {
  page: Page;
  cleanup: CleanupRegistry;
  appDevelopmentId: number;
  target: { id?: number; matches: (item: ApplicationTechnologyRecord) => boolean };
}): void {
  registerResourceCleanup({
    page,
    cleanup,
    label: `application technology for development ${appDevelopmentId}`,
    pathname: APPLICATION_DEVELOPMENT_API.technologies,
    appDevelopmentId,
    target,
  });
}

function registerResourceCleanup<T extends { id: number; deletedAt: string | null }>({
  page,
  cleanup,
  label,
  pathname,
  appDevelopmentId,
  target,
}: {
  page: Page;
  cleanup: CleanupRegistry;
  label: string;
  pathname: string;
  appDevelopmentId: number;
  target: { id?: number; matches: (item: T) => boolean };
}): void {
  cleanup.register(label, async () => {
    const item = await findDevelopmentResource(
      page,
      pathname,
      appDevelopmentId,
      target,
    );
    if (!item || item.deletedAt) return;

    const response = await page.request.delete(apiUrl(page, `${pathname}/${item.id}`));
    if (response.ok()) return;

    throw new Error(
      `DELETE ${pathname}/${item.id} returned ${response.status()}: ${await response.text()}`,
    );
  });
}

async function findDevelopmentResource<T extends { id: number }>(
  page: Page,
  pathname: string,
  appDevelopmentId: number,
  target: { id?: number; matches: (item: T) => boolean },
): Promise<T | undefined> {
  for (let pageIndex = 0; pageIndex < 100; pageIndex += 1) {
    const response = await page.request.get(apiUrl(page, `${pathname}/${appDevelopmentId}`), {
      params: { page: pageIndex, size: 100, sort: 'id,asc' },
    });
    const body = await expectApiJsonResponse<ResourcePage<T>>(
      response,
      `GET ${pathname}/${appDevelopmentId} page ${pageIndex}`,
    );
    const match = body.content.find((item) =>
      target.id === undefined ? target.matches(item) : item.id === target.id,
    );
    if (match) return match;
    if (pageIndex + 1 >= (body.totalPages ?? 1)) return undefined;
  }

  throw new Error(`GET ${pathname}/${appDevelopmentId} exceeded the E2E safety limit`);
}
