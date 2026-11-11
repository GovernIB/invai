import type { Page, TestInfo } from '@playwright/test';

import type { DisposableApplication } from './application.fixture';
import { updateApplication } from './application-prefix';
import { findApiCatalogItem } from './catalog';
import type { CleanupRegistry } from './cleanup-registry';
import { uniqueToken } from './data';
import { apiUrl, expectApiJsonResponse } from './http';
import { ACTIVE_STATUS_ID, registerSoftDeleteCleanup } from './systems';

// Real CAIB staff, read from Soffid through the back-end (it needs the VPN). Francisco Planas
// belongs to a group whose DIR3 is DIR3_UNIT; Soffid knows no DIR3 for Maria Elvira Bibiloni's
// group. Juan Ferra, of the same group, is not used: the database already stores him without his
// Soffid user code, and the back-end keeps that stored person as it is.
export const DIR3_UNIT = 'A04027006';
export const OTHER_UNIT = 'A04019898';

export interface SoffidPerson {
  firstName: string;
  lastName: string;
  email: string;
  userName: string;
  query: string;
}

export const DIR3_PERSON: SoffidPerson = {
  firstName: 'Francisco',
  lastName: 'Planas',
  email: 'fplanas@dgagric.caib.es',
  userName: 'u80657',
  query: 'Planas',
};
export const NO_DIR3_PERSON: SoffidPerson = {
  firstName: 'Maria Elvira',
  lastName: 'Bibiloni',
  email: 'mebibiloni@sgmot.caib.es',
  userName: 'u02173',
  query: 'Bibiloni',
};

export const fullName = (person: { firstName: string; lastName: string }) =>
  `${person.firstName} ${person.lastName}`;

export interface NamedRecord {
  id: number;
  name: string;
  deletedAt?: string | null;
}

export interface ResponsibleType {
  id: number;
  name: string;
  requiresPersonalCaib: boolean;
}

export interface PersonRecord {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  deletedAt: string | null;
}

export interface AssignmentRecord {
  id: number;
  person: PersonRecord;
  dir3Validation: { id: number; dir3Status: string } | null;
}

export async function setApplicationUnit(
  page: Page,
  application: DisposableApplication,
  admUnitCode: string | null,
): Promise<void> {
  await updateApplication(page, application.id, { admUnitCode });
}

export async function responsibleTypes(page: Page): Promise<ResponsibleType[]> {
  return expectApiJsonResponse<ResponsibleType[]>(
    await page.request.get(apiUrl(page, '/invaiback/responsible-type')),
    'GET /invaiback/responsible-type',
  );
}

export async function activeCatalogItem(page: Page, pathname: string): Promise<NamedRecord> {
  const item = await findApiCatalogItem<NamedRecord>({
    page,
    pathname,
    params: { statusId: ACTIVE_STATUS_ID },
    matches: (candidate) => !candidate.deletedAt && !candidate.name.startsWith('E2E'),
  });
  if (!item) throw new Error(`${pathname} has no active reference record`);
  return item;
}

export async function authorizationTypes(page: Page): Promise<NamedRecord[]> {
  const body = await expectApiJsonResponse<{ content: NamedRecord[] }>(
    await page.request.get(apiUrl(page, '/invaiback/authorization-type'), {
      params: { page: 0, size: 100, statusId: ACTIVE_STATUS_ID },
    }),
    'GET /invaiback/authorization-type',
  );
  return body.content;
}

// An external person of a reference company, removed again after the test.
export async function createExternalPerson(
  page: Page,
  cleanup: CleanupRegistry,
  testInfo: TestInfo,
  company: NamedRecord,
  firstName = 'Marta',
): Promise<PersonRecord> {
  const token = uniqueToken(testInfo);
  const person = await expectApiJsonResponse<PersonRecord>(
    await page.request.post(apiUrl(page, '/invaiback/person'), {
      data: {
        companyId: company.id,
        firstName,
        lastName: `Riera ${token}`,
        email: `${firstName.toLowerCase()}.${token.toLowerCase()}@example.test`,
        personalCaib: false,
      },
    }),
    'POST /invaiback/person',
  );
  registerSoftDeleteCleanup<PersonRecord>({
    page,
    cleanup,
    label: `person ${person.lastName}`,
    pathname: '/invaiback/person',
    target: { id: person.id, matches: (item) => item.id === person.id },
  });
  return person;
}

// The person fields of an assignment: a CAIB person picked from Soffid, with the result of their
// DIR3 check, or an external person already stored.
function personFields(person: SoffidPerson | PersonRecord, dir3Matches: boolean) {
  return !('id' in person)
    ? {
        personId: null,
        personFirstName: person.firstName,
        personLastName: person.lastName,
        personEmail: person.email,
        personUserName: person.userName,
        companyId: null,
        personalCaib: true,
        dir3Status: dir3Matches,
      }
    : { personId: person.id, personalCaib: false };
}

export async function assignResponsible(
  page: Page,
  application: DisposableApplication,
  person: SoffidPerson | PersonRecord,
  type: ResponsibleType,
  {
    jobTitle = null,
    observation = null,
    dir3Matches = false,
  }: { jobTitle?: string | null; observation?: string | null; dir3Matches?: boolean } = {},
): Promise<AssignmentRecord> {
  return expectApiJsonResponse<AssignmentRecord>(
    await page.request.post(apiUrl(page, '/invaiback/application/responsible'), {
      data: {
        appResponsibleAuthorizedId: application.appResponsibleAuthorizedId,
        ...personFields(person, dir3Matches),
        responsibleTypeId: type.id,
        jobTitle,
        observation,
      },
    }),
    `POST /invaiback/application/responsible ${type.name}`,
  );
}

export async function assignAuthorized(
  page: Page,
  application: DisposableApplication,
  person: SoffidPerson | PersonRecord,
  types: NamedRecord[],
  {
    observation = null,
    dir3Matches = false,
  }: { observation?: string | null; dir3Matches?: boolean } = {},
): Promise<AssignmentRecord> {
  return expectApiJsonResponse<AssignmentRecord>(
    await page.request.post(apiUrl(page, '/invaiback/application/authorized'), {
      data: {
        appResponsibleAuthorizedId: application.appResponsibleAuthorizedId,
        ...personFields(person, dir3Matches),
        authorizationTypeIds: types.map(({ id }) => id),
        observation,
      },
    }),
    'POST /invaiback/application/authorized',
  );
}

export async function validateManually(
  page: Page,
  assignment: AssignmentRecord,
  reason: string,
): Promise<void> {
  const path = `/invaiback/application/dir3-validation/manual-validate/${assignment.dir3Validation!.id}`;
  await expectApiJsonResponse(
    await page.request.put(apiUrl(page, path), { data: { reason } }),
    `PUT ${path}`,
  );
}
