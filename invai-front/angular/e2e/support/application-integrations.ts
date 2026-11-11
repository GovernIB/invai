import { expect } from '@playwright/test';
import type { Locator, Page } from '@playwright/test';

import type { DisposableApplication } from './application.fixture';
import { findApiCatalogItem } from './catalog';
import type { CleanupRegistry } from './cleanup-registry';
import { apiUrl, expectApiJsonResponse } from './http';
import { ACTIVE_STATUS_ID } from './systems';

export const CONNECTIONS_API = '/invaiback/application/integration-connection';
export const INTEGRATION_API = '/invaiback/application/integration';

// Real Soffid data, read through the back-end (it needs the VPN). The person holds exactly the
// GRANTED and OTHER_GRANTED roles: roles only match when the required ones are those same roles,
// so a connection requiring MISSING, or only one of them, has a role mismatch.
export const SOFFID_PERSON = {
  query: 'batanero',
  option: /Maria Lourdes Batanero/,
  userName: 'u08887',
};

export interface SoffidRole {
  roleId: number;
  name: string;
  label: string;
}

const soffidRole = (roleId: number, name: string, system: string): SoffidRole => ({
  roleId,
  name,
  label: `${name} — ${system}`,
});

export const GRANTED_ROLE = soffidRole(9064614, 'usuari-tipus-E', 'soffid');
export const OTHER_GRANTED_ROLE = soffidRole(103121480, 'LLU_CENTRES', 'weblogic');
export const MISSING_ROLE = soffidRole(5745, 'INV_ADMIN', 'domino-lweb2');

interface NamedRecord {
  id: number;
  name: string | null;
  deletedAt: string | null;
}

export interface ConnectionRecord {
  id: number;
  username: string;
  technology: { id: number; name: string | null };
  rolesMismatch: boolean | null;
  rolesUnavailable: boolean | null;
}

export async function findActiveByName(
  page: Page,
  pathname: string,
  name: string,
): Promise<NamedRecord> {
  const item = await findApiCatalogItem<NamedRecord>({
    page,
    pathname,
    params: { statusId: ACTIVE_STATUS_ID },
    matches: (candidate) => candidate.name === name && !candidate.deletedAt,
  });
  if (!item) throw new Error(`${pathname} has no active record named ${name}`);
  return item;
}

export const findSoffidSystem = (page: Page) =>
  findActiveByName(page, '/invaiback/external-system', 'Soffid');
export const findTechnology = (page: Page, name: string) =>
  findActiveByName(page, '/invaiback/technology', name);

// Adds a connection of the person to the Soffid external system, removed again after the test.
export async function createConnection(
  page: Page,
  cleanup: CleanupRegistry,
  application: DisposableApplication,
  roles: SoffidRole[],
  { technologyName = 'Spring Boot' }: { technologyName?: string } = {},
): Promise<ConnectionRecord> {
  const [soffid, technology] = await Promise.all([
    findSoffidSystem(page),
    findTechnology(page, technologyName),
  ]);
  const created = await expectApiJsonResponse<ConnectionRecord>(
    await page.request.post(apiUrl(page, CONNECTIONS_API), {
      data: {
        appIntegrationId: application.appIntegrationId,
        applicationId: null,
        externalSystemId: soffid.id,
        technologyId: technology.id,
        username: SOFFID_PERSON.userName,
        requiredRoleIds: roles.map(({ roleId }) => roleId),
      },
    }),
    `POST ${CONNECTIONS_API}`,
  );
  registerConnectionCleanup(page, cleanup, created.id);
  return created;
}

export function registerConnectionCleanup(page: Page, cleanup: CleanupRegistry, id: number): void {
  cleanup.register(`integration connection ${id}`, async () => {
    const response = await page.request.delete(apiUrl(page, `${CONNECTIONS_API}/${id}`));
    // An already deactivated connection answers with an error and needs nothing else.
    if (!response.ok() && response.status() !== 400 && response.status() !== 404) {
      throw new Error(`DELETE ${CONNECTIONS_API}/${id} returned ${response.status()}`);
    }
  });
}

// Types the whole query at once: typed key by key under load, partial queries go out and each one
// cancels the previous search. The query is typed again when no suggestion comes back.
export async function typeSoffidQuery(page: Page): Promise<Locator> {
  const field = page.locator('#application-integration-dialog-soffid-person');
  const option = page.getByRole('option', { name: SOFFID_PERSON.option });
  await expect(async () => {
    await field.fill('');
    await field.fill(SOFFID_PERSON.query);
    await expect(option).toBeVisible({ timeout: 10_000 });
  }).toPass({ timeout: 45_000 });
  return option;
}

export async function pickSoffidPerson(page: Page): Promise<void> {
  await (await typeSoffidQuery(page)).click();
}

// Toggles each role in the required roles picker, filtering the Soffid roles by its name. The
// page only asks Soffid when the roles it already holds cannot answer the filter, so the test waits
// for the option rather than for a search, and types the filter again if it does not show up.
export async function pickRoles(page: Page, ...roles: SoffidRole[]): Promise<void> {
  const picker = page.locator('#application-integration-dialog-roles');
  const filter = page.getByRole('searchbox', { name: 'Rols requerits' });
  for (const role of roles) {
    const option = page.getByRole('option', { name: role.label, exact: true });
    await expect(async () => {
      // A Soffid user picked just before takes the focus back once its panel closes, which can
      // swallow the key that opens the roles: open them again until their filter shows.
      if (!(await filter.isVisible())) {
        await picker.focus();
        await page.keyboard.press('Enter');
        await expect(filter).toBeVisible({ timeout: 2_000 });
      }
      await filter.fill('');
      await filter.fill(role.name);
      await expect(option).toBeVisible({ timeout: 10_000 });
    }).toPass({ timeout: 45_000 });
    await option.click();
  }
  await page.keyboard.press('Escape');
}
