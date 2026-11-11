import type { Page } from '@playwright/test';

import type { CleanupRegistry } from './cleanup-registry';
import { apiUrl, expectApiJsonResponse } from './http';

const APPLICATION_API_PATH = '/invaiback/application';
const ACTIVE_STATUS_ID = 1;
const INACTIVE_STATUS_ID = 2;
const PREFIX_RADIX = 36;
const PREFIX_LENGTH = 3;
const PREFIX_SPACE = PREFIX_RADIX ** PREFIX_LENGTH;

interface ApplicationPrefixPage {
  content: Array<{ prefix: string | null }>;
}

export async function findAvailableApplicationPrefix(page: Page, token: string): Promise<string> {
  const seed = Number.parseInt(token, PREFIX_RADIX) % PREFIX_SPACE;

  for (let offset = 0; offset < PREFIX_SPACE; offset += 1) {
    const prefix = ((seed + offset) % PREFIX_SPACE)
      .toString(PREFIX_RADIX)
      .padStart(PREFIX_LENGTH, '0')
      .toUpperCase();

    if (await isApplicationPrefixAvailable(page, prefix)) return prefix;
  }

  throw new Error('No application prefix is available for the E2E fixture.');
}

export async function isApplicationPrefixAvailable(page: Page, prefix: string): Promise<boolean> {
  for (const statusId of [ACTIVE_STATUS_ID, INACTIVE_STATUS_ID]) {
    const response = await page.request.get(apiUrl(page, APPLICATION_API_PATH), {
      params: {
        page: 0,
        size: 1,
        prefix,
        statusId,
      },
    });
    const body = await expectApiJsonResponse<ApplicationPrefixPage>(
      response,
      `GET ${APPLICATION_API_PATH}?prefix=${prefix}&statusId=${statusId}`,
    );

    if (body.content.length > 0) return false;
  }

  return true;
}

export const SOFFID_ROLES_API = '/invaiback/security-role/soffid-search';

export interface SoffidRoleCandidate {
  roleId: number;
  name: string;
  system: string;
  description: string | null;
}

interface ApplicationDetail {
  id: number;
  name: string;
  prefix: string;
  code: string;
  status: 'ACTIVE' | 'INACTIVE';
  category: { id: number } | null;
  systemType: { id: number } | null;
  field: { id: number } | null;
  admUnit: { code: string } | null;
  csCommission: { id: number } | null;
  description: string | null;
}

export interface ApplicationChanges {
  name?: string;
  prefix?: string;
  admUnitCode?: string | null;
}

// The Soffid roles the back-end assigns to an application with this prefix: those whose name
// starts with the prefix and an underscore, matched case-sensitively.
export async function soffidRolesWithPrefix(
  page: Page,
  prefix: string,
): Promise<SoffidRoleCandidate[]> {
  const roles: SoffidRoleCandidate[] = [];
  for (let pageIndex = 0; pageIndex < 50; pageIndex += 1) {
    const body = await expectApiJsonResponse<{ content: SoffidRoleCandidate[]; last: boolean }>(
      await page.request.get(apiUrl(page, SOFFID_ROLES_API), {
        params: { name: `${prefix}_`, page: pageIndex, size: 100 },
      }),
      `GET ${SOFFID_ROLES_API}?name=${prefix}_`,
    );
    roles.push(...body.content.filter((role) => role.name.startsWith(`${prefix}_`)));
    if (body.last || body.content.length === 0) return roles;
  }
  throw new Error(`${SOFFID_ROLES_API} exceeded the E2E safety limit for ${prefix}_`);
}

export async function updateApplication(
  page: Page,
  id: number,
  changes: ApplicationChanges,
): Promise<void> {
  const detail = await expectApiJsonResponse<ApplicationDetail>(
    await page.request.get(apiUrl(page, `${APPLICATION_API_PATH}/${id}`)),
    `GET ${APPLICATION_API_PATH}/${id}`,
  );
  await expectApiJsonResponse(
    await page.request.put(apiUrl(page, `${APPLICATION_API_PATH}/${id}`), {
      data: {
        name: detail.name,
        prefix: detail.prefix,
        code: detail.code,
        categoryId: detail.category?.id,
        systemTypeId: detail.systemType?.id,
        fieldId: detail.field?.id,
        admUnitCode: detail.admUnit?.code,
        commissionId: detail.csCommission?.id,
        description: detail.description ?? '',
        statusId: ACTIVE_STATUS_ID,
        ...changes,
      },
    }),
    `PUT ${APPLICATION_API_PATH}/${id}`,
  );
}

// A prefix stays taken by an application even once it is inactive, so the cleanup gives the
// application its own prefix back before it is deactivated, reactivating it if the test did not.
export function registerPrefixRestore(
  page: Page,
  cleanup: CleanupRegistry,
  id: number,
  ownPrefix: string,
): void {
  cleanup.register(`application ${id} prefix ${ownPrefix}`, async () => {
    const detail = await expectApiJsonResponse<ApplicationDetail>(
      await page.request.get(apiUrl(page, `${APPLICATION_API_PATH}/${id}`)),
      `GET ${APPLICATION_API_PATH}/${id}`,
    );
    if (detail.prefix === ownPrefix) return;
    if (detail.status !== 'ACTIVE') {
      await expectApiJsonResponse(
        await page.request.put(apiUrl(page, `${APPLICATION_API_PATH}/reactivate/${id}`)),
        `PUT ${APPLICATION_API_PATH}/reactivate/${id}`,
      );
    }
    await updateApplication(page, id, { prefix: ownPrefix });
  });
}

export interface PrefixWithRoles {
  prefix: string;
  roles: SoffidRoleCandidate[];
}

// Free candidates of real Soffid prefixes, by the number of roles named after them. The counts are
// checked against Soffid when a test runs, so a candidate that no longer fits is skipped.
export const SINGLE_ROLE_PREFIXES = ['ISI', 'FAC', 'FAS', 'INT', 'VPO', 'BDN', 'HEM', 'RGO'];
export const SEVERAL_ROLES_PREFIXES = ['LLU', 'OWS', 'PID', 'ICS'];

// The first free prefix of the candidates whose Soffid roles satisfy `accepts`.
export async function findFreePrefix(
  page: Page,
  candidates: string[],
  accepts: (roles: SoffidRoleCandidate[]) => boolean,
): Promise<PrefixWithRoles> {
  for (const prefix of candidates) {
    if (!(await isApplicationPrefixAvailable(page, prefix))) continue;
    const roles = await soffidRolesWithPrefix(page, prefix);
    if (accepts(roles)) return { prefix, roles };
  }
  throw new Error(
    `None of the prefixes ${candidates.join(', ')} is free with the Soffid roles needed`,
  );
}

// A free prefix that no Soffid role is named after.
export async function findFreePrefixWithoutRoles(
  page: Page,
  token: string,
): Promise<PrefixWithRoles> {
  for (let attempt = 0; attempt < 20; attempt += 1) {
    const prefix = await findAvailableApplicationPrefix(page, `${token}${attempt}`);
    const roles = await soffidRolesWithPrefix(page, prefix);
    if (roles.length === 0) return { prefix, roles };
  }
  throw new Error('No free prefix without Soffid roles was found');
}

// Gives the application the first free prefix of the candidates whose Soffid roles satisfy
// `accepts`, and returns it with those roles.
export async function borrowApplicationPrefix(
  page: Page,
  cleanup: CleanupRegistry,
  application: { id: number; prefix: string },
  candidates: string[],
  accepts: (roles: SoffidRoleCandidate[]) => boolean = (roles) => roles.length > 0,
): Promise<PrefixWithRoles> {
  const found = await findFreePrefix(page, candidates, accepts);
  registerPrefixRestore(page, cleanup, application.id, application.prefix);
  await updateApplication(page, application.id, { prefix: found.prefix });
  return found;
}
