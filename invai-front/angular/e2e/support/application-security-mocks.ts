import type { Page, Route } from '@playwright/test';

const APP_SECURITY_ID = 9;

const pageOf = (content: unknown[]) => ({
  content,
  totalElements: content.length,
  totalPages: 1,
  last: true,
  number: 0,
});

interface CatalogItem {
  id: number;
  name: string;
  nameEs: string;
  deletedAt: null;
}

const item = (id: number, name: string, nameEs = name): CatalogItem => ({
  id,
  name,
  nameEs,
  deletedAt: null,
});

const SECURITY_LEVELS = [
  item(1, 'Baix', 'Bajo'),
  item(2, 'Mitjà', 'Medio'),
  item(3, 'Alt', 'Alto'),
];
const IDENTITY_PROVIDERS = [item(4, 'Cl@ve'), item(5, 'Seycon')];
const ENS_SUBJECTS = [item(6, 'Subjecta', 'Sujeta'), item(7, 'No subjecta', 'No sujeta')];
const PERSONAL_DATA_PROCESSING = [item(8, 'Amb dades personals'), item(10, 'Sense dades')];
const FIELDS = [item(21, 'Salut', 'Salud'), item(22, 'Educació', 'Educación')];
const MEASURE_TYPES = [item(31, 'Organitzativa'), item(32, 'Tècnica', 'Técnica')];
const ENS_REQUIREMENTS = [item(41, 'op.acc.1 Identificació'), item(42, 'mp.info.3 Xifratge')];

const findItem = (items: CatalogItem[], id: number | null | undefined) =>
  items.find((candidate) => candidate.id === id) ?? null;

interface SoffidRole {
  roleId: number;
  name: string;
  system: string;
  description: string | null;
}

// Candidates answered by security-role/soffid-search; roleId is Soffid's own identifier.
const SOFFID_ROLES: SoffidRole[] = [
  { roleId: 502, name: 'INV_USER', system: 'weblogic', description: 'Usuari' },
  { roleId: 504, name: 'ADM_AUDIT', system: 'weblogic', description: 'Auditoria' },
  { roleId: 503, name: 'INV_GESTOR', system: 'weblogic', description: 'Gestió' },
];

type RoleValidationStatus = 'VALIDATED' | 'NOT_VALIDATED' | 'MANUAL' | 'NOT_APPLY';

export function roleRow(
  id: number,
  role: SoffidRole,
  validationStatus: RoleValidationStatus = 'VALIDATED',
  manual: { by: string; at: string } | null = null,
) {
  return {
    id,
    appSecurity: { id: APP_SECURITY_ID },
    securityRole: { id: id + 100, ...role },
    deletedAt: null,
    validationStatus,
    manualValidatedAt: manual?.at ?? null,
    manualValidatedBy: manual?.by ?? null,
  };
}

type RoleRow = ReturnType<typeof roleRow>;

const DEFAULT_ROLES: RoleRow[] = [
  roleRow(
    12,
    { roleId: 501, name: 'INV_ADMIN', system: 'weblogic', description: 'Administració' },
  ),
  roleRow(
    13,
    { roleId: 505, name: 'ADM_ROOT', system: 'weblogic', description: 'Superusuari' },
    'NOT_VALIDATED',
  ),
  roleRow(
    14,
    { roleId: 506, name: 'INV_CONSULTA', system: 'SEYCON', description: 'Consulta' },
    'MANUAL',
    { by: 'maria', at: '2026-10-07T16:21:12' },
  ),
];

// Builds the classification the back-end returns for the given ids (an ENS input payload).
export function ensClassification(
  id: number,
  ids: Record<string, number | string | null>,
  approvalDate: string | null = null,
) {
  const level = (key: string) => findItem(SECURITY_LEVELS, ids[key] as number | null);
  return {
    id,
    appSecurity: { id: APP_SECURITY_ID },
    overallGrade: level('overallGradeId'),
    identityProvider: findItem(IDENTITY_PROVIDERS, ids['identityProviderId'] as number | null),
    ensSubject: findItem(ENS_SUBJECTS, ids['ensSubjectId'] as number | null),
    personalDataProcessing: findItem(
      PERSONAL_DATA_PROCESSING,
      ids['personalDataProcessingId'] as number | null,
    ),
    approvalDate,
    confidentiality: level('confidentialityId'),
    integrity: level('integrityId'),
    traceability: level('traceabilityId'),
    availability: level('availabilityId'),
    authenticity: level('authenticityId'),
    deletedAt: null,
  };
}

export function riskRow(
  id: number,
  levelId: number | null,
  fieldId: number | null,
  description: string | null,
) {
  return {
    id,
    appSecurity: { id: APP_SECURITY_ID },
    level: findItem(SECURITY_LEVELS, levelId),
    field: findItem(FIELDS, fieldId),
    description,
    deletedAt: null,
  };
}

function measureRow(
  id: number,
  typeId: number | null,
  ensRequirementId: number | null,
  description: string | null,
) {
  return {
    id,
    appSecurity: { id: APP_SECURITY_ID },
    type: findItem(MEASURE_TYPES, typeId),
    ensRequirement: findItem(ENS_REQUIREMENTS, ensRequirementId),
    description,
    deletedAt: null,
  };
}

export interface SecurityMockOptions {
  status?: 'ACTIVE' | 'INACTIVE';
  // null opens an application whose Seguretat record has not been created yet.
  appSecurityId?: number | null;
  roles?: RoleRow[];
  classifications?: ReturnType<typeof ensClassification>[];
  risks?: ReturnType<typeof riskRow>[];
}

export interface SecurityMock {
  roleRequests: URL[];
  soffidRequests: URL[];
  writes: { method: string; path: string; body: unknown }[];
}

const contains = (value: string | null, query: string) =>
  (value ?? '').toLowerCase().includes(query.toLowerCase());

// Answers every back-end call of the Seguretat tab from memory and records each mutation.
export async function openSecurityTab(
  page: Page,
  username: string,
  options: SecurityMockOptions = {},
): Promise<SecurityMock> {
  const mock: SecurityMock = { roleRequests: [], soffidRequests: [], writes: [] };
  let appSecurityId = options.appSecurityId === undefined ? APP_SECURITY_ID : options.appSecurityId;
  let security = {
    id: APP_SECURITY_ID,
    application: { id: 1 },
    observation: null as string | null,
    deletedAt: null,
  };
  let roles = options.roles ?? DEFAULT_ROLES;
  let classifications = options.classifications ?? [];
  const resources: Record<string, { id: number }[]> = {
    risk: [...(options.risks ?? [])],
    measure: [],
  };
  let nextId = 60;

  await page.route('**/invaiback/**', async (route: Route) => {
    const request = route.request();
    const url = new URL(request.url());
    const path = url.pathname.replace(/^\/invaiback/, '');
    const method = request.method();
    const body = method === 'GET' || method === 'DELETE' ? undefined : request.postDataJSON();
    if (method !== 'GET') mock.writes.push({ method, path, body });

    if (path === '/auth/me') {
      return route.fulfill({ json: { authenticated: true, username } });
    }
    if (path === '/application/1') {
      return route.fulfill({
        json: {
          id: 1,
          code: 'INV',
          prefix: 'INV',
          name: 'Seguretat test',
          status: options.status ?? 'ACTIVE',
          appSecurityId,
        },
      });
    }
    if (
      path === `/application/security/${APP_SECURITY_ID}` ||
      (path === '/application/security' && method === 'POST')
    ) {
      if (method !== 'GET') security = { ...security, observation: body.observation };
      if (method === 'POST') appSecurityId = APP_SECURITY_ID;
      return route.fulfill({ status: method === 'POST' ? 201 : 200, json: security });
    }

    if (path === `/application/security/role/${APP_SECURITY_ID}` && method === 'GET') {
      mock.roleRequests.push(url);
      const search = url.searchParams.get('search');
      const name = url.searchParams.get('securityRoleName');
      const system = url.searchParams.get('system');
      const found = roles.filter(
        ({ securityRole: role }) =>
          (!search || contains(role.name, search) || contains(role.description, search)) &&
          (!name || contains(role.name, name)) &&
          (!system || role.system.toLowerCase() === system.toLowerCase()),
      );
      return route.fulfill({ json: pageOf(found) });
    }
    if (path === '/application/security/role' && method === 'POST') {
      const role = SOFFID_ROLES.find((candidate) => candidate.roleId === body.securityRoleId)!;
      // The back-end saves a hand-picked role outside the prefix as validated manually.
      const created = role.name.startsWith('INV_')
        ? roleRow(nextId++, role)
        : roleRow(nextId++, role, 'MANUAL', { by: username, at: '2026-10-14T10:30:00' });
      roles = [...roles, created];
      return route.fulfill({ status: 201, json: created });
    }
    const validatedId = Number(path.match(/^\/application\/security\/role\/validate\/(\d+)$/)?.[1]);
    if (validatedId && method === 'PUT') {
      roles = roles.map((row) =>
        row.id === validatedId
          ? roleRow(row.id, row.securityRole, 'MANUAL', { by: username, at: '2026-10-14T10:30:00' })
          : row,
      );
      return route.fulfill({ status: 204 });
    }
    const roleId = Number(path.match(/^\/application\/security\/role\/(\d+)$/)?.[1]);
    if (roleId && method === 'PUT') {
      const role = SOFFID_ROLES.find((candidate) => candidate.roleId === body.securityRoleId)!;
      roles = roles.map((row) => (row.id === roleId ? roleRow(roleId, role) : row));
      return route.fulfill({ json: roleRow(roleId, role) });
    }
    if (roleId && method === 'DELETE') {
      roles = roles.filter((row) => row.id !== roleId);
      return route.fulfill({ status: 204 });
    }
    if (path === '/security-role/soffid-search') {
      mock.soffidRequests.push(url);
      const name = url.searchParams.get('name');
      const found = SOFFID_ROLES.filter((role) => !name || contains(role.name, name));
      return route.fulfill({ json: pageOf(found.map((role) => ({ id: null, ...role }))) });
    }

    const classification = path.match(/^\/application\/security\/ens-classification(\/\d+)?$/);
    if (classification && method === 'GET') {
      return route.fulfill({ json: pageOf(classifications) });
    }
    if (classification) {
      const id = classification[1] ? Number(classification[1].slice(1)) : nextId++;
      classifications = [ensClassification(id, body, body.approvalDate)];
      return route.fulfill({ status: method === 'POST' ? 201 : 200, json: classifications[0] });
    }

    const resource = path.match(/^\/application\/security\/(risk|measure)(?:\/(\d+))?$/);
    if (resource) {
      const [, kind, id] = resource;
      const rows = resources[kind];
      if (method === 'GET') return route.fulfill({ json: pageOf(rows) });
      if (method === 'DELETE') {
        rows.splice(rows.findIndex((row) => row.id === Number(id)), 1);
        return route.fulfill({ status: 204 });
      }
      const created =
        kind === 'risk'
          ? riskRow(nextId++, body.levelId, body.fieldId, body.description)
          : measureRow(nextId++, body.typeId, body.ensRequirementId, body.description);
      rows.push(created);
      return route.fulfill({ status: 201, json: created });
    }

    const catalogs: Record<string, unknown> = {
      '/security-level': SECURITY_LEVELS,
      '/ens-subject': ENS_SUBJECTS,
      '/identity-provider': pageOf(IDENTITY_PROVIDERS),
      '/personal-data-processing': pageOf(PERSONAL_DATA_PROCESSING),
      '/field': pageOf(FIELDS),
      '/security-measure-type': pageOf(MEASURE_TYPES),
      '/ens-requirement': pageOf(ENS_REQUIREMENTS),
    };
    return route.fulfill({ json: catalogs[path] ?? pageOf([]) });
  });
  await page.goto('/aplicacions/1/security');
  return mock;
}
