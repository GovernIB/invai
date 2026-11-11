import type { Page } from '@playwright/test';

import type { DisposableApplication } from './application.fixture';
import { borrowApplicationPrefix } from './application-prefix';
import type { CleanupRegistry } from './cleanup-registry';
import { apiUrl, expectApiJsonResponse } from './http';

export const SECURITY_API = '/invaiback/application/security';

export interface RealSoffidRole {
  roleId: number;
  name: string;
  system: string;
  description: string;
}

// Real Soffid roles, read through the back-end (it needs the VPN). No application uses the LLU
// prefix, so a test can borrow it to have roles named after the prefix of its application.
export const ROLE_PREFIX = 'LLU';
export const PREFIXED_ROLE: RealSoffidRole = {
  roleId: 103121480,
  name: 'LLU_CENTRES',
  system: 'weblogic',
  description: 'Rol de centres',
};
export const OTHER_PREFIXED_ROLE: RealSoffidRole = {
  roleId: 103897090,
  name: 'LLU_CONSELLERIA',
  system: 'weblogic',
  description: '',
};
export const WEBLOGIC_ROLE: RealSoffidRole = {
  roleId: 5465,
  name: 'ICS_INVIT',
  system: 'weblogic',
  description: 'Convidat.',
};
export const OTHER_SYSTEM_ROLE: RealSoffidRole = {
  roleId: 5745,
  name: 'INV_ADMIN',
  system: 'domino-lweb2',
  description: 'Administración de la aplicación INVAI',
};

export interface AppRoleRecord {
  id: number;
  validationStatus: string;
}

// Gives the application the LLU prefix until the end of the test.
export async function borrowRolePrefix(
  page: Page,
  cleanup: CleanupRegistry,
  application: DisposableApplication,
): Promise<void> {
  await borrowApplicationPrefix(page, cleanup, application, [ROLE_PREFIX], (roles) =>
    roles.some((role) => role.roleId === PREFIXED_ROLE.roleId),
  );
}

// Assigns the roles by hand, as the role dialog does: a role named after the prefix of the
// application is saved VALIDATED and any other one MANUAL, in the name of the E2E user.
export async function assignRoles(
  page: Page,
  application: DisposableApplication,
  roles: RealSoffidRole[],
): Promise<AppRoleRecord[]> {
  const assigned: AppRoleRecord[] = [];
  for (const role of roles) {
    assigned.push(
      await expectApiJsonResponse<AppRoleRecord>(
        await page.request.post(apiUrl(page, `${SECURITY_API}/role`), {
          data: { appSecurityId: application.appSecurityId, securityRoleId: role.roleId },
        }),
        `POST ${SECURITY_API}/role ${role.name}`,
      ),
    );
  }
  return assigned;
}

export async function signedInUsername(page: Page): Promise<string> {
  const me = await expectApiJsonResponse<{ username: string }>(
    await page.request.get(apiUrl(page, '/invaiback/auth/me')),
    'GET /invaiback/auth/me',
  );
  return me.username;
}

// "Rol validat manualment per <user> el dd/mm/yyyy hh:mm", with the moment of the validation.
export function manualValidationLabel(username: string): RegExp {
  return new RegExp(
    `^Rol validat manualment per ${username} el \\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}$`,
  );
}

export function recordSecurityRequests(page: Page) {
  const recorded = {
    roleRequests: [] as URL[],
    soffidRequests: [] as URL[],
    writes: [] as { method: string; path: string; body: unknown }[],
  };
  page.on('request', (request) => {
    const url = new URL(request.url());
    const method = request.method();
    if (url.pathname === '/invaiback/security-role/soffid-search') {
      recorded.soffidRequests.push(url);
    } else if (url.pathname.startsWith(SECURITY_API)) {
      if (
        method === 'GET' &&
        /^\/invaiback\/application\/security\/role\/\d+$/.test(url.pathname)
      ) {
        recorded.roleRequests.push(url);
      } else if (method !== 'GET') {
        recorded.writes.push({
          method,
          path: url.pathname.replace('/invaiback', ''),
          body: method === 'DELETE' ? undefined : request.postDataJSON(),
        });
      }
    }
  });
  return recorded;
}
