import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { SKIP_SERVER_ERROR_DIALOG } from '@core/interceptors/http-error-logging.interceptor';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';

import {
  ApplicationSecurityOutput,
  ApplicationSecurityRoleOutput,
  SecurityCatalogItem,
} from '../applications.model';
import {
  ApplicationSecurityRolesService,
  ApplicationSecurityService,
  ApplicationSoffidRolesAssignmentService,
  ApplicationWebContextsService,
  SecurityLevelsService,
} from './application-security.service';
import { ApplicationsService } from './applications.service';

const BASE_URL = '/invaiback/application/security';

describe('application security services', () => {
  let httpTesting: HttpTestingController;
  let securityService: ApplicationSecurityService;
  let rolesService: ApplicationSecurityRolesService;
  let rolesAssignmentService: ApplicationSoffidRolesAssignmentService;
  let webContextsService: ApplicationWebContextsService;
  let securityLevelsService: SecurityLevelsService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    httpTesting = TestBed.inject(HttpTestingController);
    securityService = TestBed.inject(ApplicationSecurityService);
    rolesService = TestBed.inject(ApplicationSecurityRolesService);
    rolesAssignmentService = TestBed.inject(ApplicationSoffidRolesAssignmentService);
    webContextsService = TestBed.inject(ApplicationWebContextsService);
    securityLevelsService = TestBed.inject(SecurityLevelsService);
  });

  afterEach(() => httpTesting.verify());

  it('normalizes role systems, separates their cache entries and shares identical queries', () => {
    for (const system of [' weblogic ', 'other', '   ']) {
      rolesService.getPage({ appSecurityId: 8, system }).subscribe();
      rolesService.getPage({ appSecurityId: 8, system: system.trim() }).subscribe();
      const request = httpTesting.expectOne(req => req.url === `${BASE_URL}/role/8`);
      expect(request.request.params.get('system')).toBe(system.trim() || null);
      request.flush(page([]));
    }
    rolesService.getPage({ appSecurityId: 8, system: 'weblogic' }).subscribe();
    rolesService.getPage({ appSecurityId: 8 }).subscribe();
    httpTesting.expectNone(req => req.url === `${BASE_URL}/role/8`);
  });

  it('retries a failed filtered role request', () => {
    const params = { appSecurityId: 8, system: 'weblogic' };
    rolesService.getPage(params).subscribe({ error: () => undefined });
    httpTesting.expectOne(req => req.url === `${BASE_URL}/role/8`)
      .flush(null, { status: 500, statusText: 'Server error' });
    rolesService.getPage(params).subscribe();
    httpTesting.expectOne(req => req.url === `${BASE_URL}/role/8`).flush(page([]));
  });

  it('shares detail reads, retries errors and invalidates after an update', () => {
    const first = vi.fn();
    const shared = vi.fn();
    securityService.getById(8).subscribe(first);
    securityService.getById(8).subscribe(shared);
    httpTesting.expectOne(`${BASE_URL}/8`).flush(security());
    expect(first).toHaveBeenCalledWith(security());
    expect(shared).toHaveBeenCalledWith(security());

    securityService.update(8, { applicationId: 7, observation: 'Nova' }).subscribe();
    httpTesting.expectOne(`${BASE_URL}/8`).flush({ ...security(), observation: 'Nova' });

    securityService.getById(8).subscribe();
    httpTesting.expectOne(`${BASE_URL}/8`).flush({ ...security(), observation: 'Nova' });
  });

  it('sends the trimmed role search, code and system criteria with separate cache entries', () => {
    const params = { appSecurityId: 8, search: ' admin ', securityRoleName: ' INV_ ', system: ' weblogic ' };
    rolesService.getPage(params).subscribe();
    const request = httpTesting.expectOne(req => req.url === `${BASE_URL}/role/8`);
    expect(request.request.params.get('search')).toBe('admin');
    expect(request.request.params.get('securityRoleName')).toBe('INV_');
    expect(request.request.params.get('system')).toBe('weblogic');
    request.flush(page([]));
    rolesService.getPage({ ...params, search: 'admin', securityRoleName: 'INV_', system: 'weblogic' }).subscribe();
    httpTesting.expectNone(req => req.url === `${BASE_URL}/role/8`);
    rolesService.getPage({ appSecurityId: 8, search: '   ', securityRoleName: 'INV_' }).subscribe();
    const blankSearch = httpTesting.expectOne(req => req.url === `${BASE_URL}/role/8`);
    expect(blankSearch.request.params.has('search')).toBe(false);
    expect(blankSearch.request.params.has('system')).toBe(false);
    blankSearch.flush(page([]));
  });

  it('creates, updates and deletes role assignments and invalidates role pages', () => {
    const clearApplications = vi.spyOn(TestBed.inject(ApplicationsService), 'clearCache');
    const params = { appSecurityId: 8, page: 0, size: 10 };
    const payload = { appSecurityId: 8, securityRoleId: 501 };
    rolesService.getPage(params).subscribe();
    httpTesting.expectOne(`${BASE_URL}/role/8?page=0&size=10`).flush(page([]));

    rolesService.create(payload).subscribe();
    const create = httpTesting.expectOne(`${BASE_URL}/role`);
    expect(create.request.method).toBe('POST');
    expect(create.request.body).toEqual(payload);
    create.flush({ id: 3 });
    rolesService.update(3, payload).subscribe();
    const update = httpTesting.expectOne(`${BASE_URL}/role/3`);
    expect(update.request.method).toBe('PUT');
    expect(update.request.body).toEqual(payload);
    update.flush({ id: 3 });
    rolesService.delete(3).subscribe();
    const remove = httpTesting.expectOne(`${BASE_URL}/role/3`);
    expect(remove.request.method).toBe('DELETE');
    remove.flush(null, { status: 204, statusText: 'No Content' });

    expect(clearApplications).toHaveBeenCalledTimes(3);
    rolesService.getPage(params).subscribe();
    httpTesting.expectOne(`${BASE_URL}/role/8?page=0&size=10`).flush(page([]));
  });

  it('validates a role assignment without a reason and invalidates caches only on success', () => {
    const clearApplications = vi.spyOn(TestBed.inject(ApplicationsService), 'clearCache');
    const params = { appSecurityId: 8, page: 0, size: 10 };
    rolesService.getPage(params).subscribe();
    httpTesting.expectOne(`${BASE_URL}/role/8?page=0&size=10`).flush(page([]));

    rolesService.validate(3).subscribe({ error: () => undefined });
    const failed = httpTesting.expectOne(`${BASE_URL}/role/validate/3`);
    expect(failed.request.method).toBe('PUT');
    expect(failed.request.body).toEqual({});
    failed.flush(
      { error: 'Bad Request', message: 'Només es pot validar un rol no validat.' },
      { status: 400, statusText: 'Bad Request' },
    );
    rolesService.getPage(params).subscribe();
    httpTesting.expectNone(`${BASE_URL}/role/8?page=0&size=10`);
    expect(clearApplications).not.toHaveBeenCalled();

    rolesService.validate(3).subscribe();
    httpTesting
      .expectOne(`${BASE_URL}/role/validate/3`)
      .flush(null, { status: 204, statusText: 'No Content' });
    expect(clearApplications).toHaveBeenCalledOnce();
    rolesService.getPage(params).subscribe();
    httpTesting.expectOne(`${BASE_URL}/role/8?page=0&size=10`).flush(page([]));
  });

  it('keeps role pages separated by anchor and page parameters', () => {
    rolesService
      .getPage({
        appSecurityId: 8,
        page: 1,
        size: 10,
        sort: 'id,desc',
        statusId: SoftDeleteStatus.ACTIVE,
      })
      .subscribe();

    const request = httpTesting.expectOne((candidate) => candidate.url === `${BASE_URL}/role/8`);
    expect(request.request.params.get('page')).toBe('1');
    expect(request.request.params.get('size')).toBe('10');
    expect(request.request.params.get('sort')).toBe('id,desc');
    expect(request.request.params.get('statusId')).toBe(String(SoftDeleteStatus.ACTIVE));
    request.flush(page([]));
  });

  it('assigns the Soffid roles inline and invalidates role pages and applications only on success', () => {
    const clearApplications = vi.spyOn(TestBed.inject(ApplicationsService), 'clearCache');
    const params = { appSecurityId: 8, page: 0, size: 10 };
    rolesService.getPage(params).subscribe();
    httpTesting.expectOne(`${BASE_URL}/role/8?page=0&size=10`).flush(page([]));

    rolesAssignmentService.assign(7).subscribe({ error: () => undefined });
    const failed = httpTesting.expectOne('/invaiback/application/assign-roles/7');
    expect(failed.request.method).toBe('PUT');
    expect(failed.request.body).toEqual({});
    expect(failed.request.context.get(SKIP_SERVER_ERROR_DIALOG)).toBe(true);
    failed.flush(
      { error: 'Error', message: 'Soffid no disponible' },
      { status: 400, statusText: 'Bad request' },
    );
    rolesService.getPage(params).subscribe();
    httpTesting.expectNone(`${BASE_URL}/role/8?page=0&size=10`);
    expect(clearApplications).not.toHaveBeenCalled();

    const roles = [{ id: 3 }] as ApplicationSecurityRoleOutput[];
    const result = vi.fn();
    rolesAssignmentService.assign(7).subscribe(result);
    httpTesting.expectOne('/invaiback/application/assign-roles/7').flush(roles);
    expect(result).toHaveBeenCalledExactlyOnceWith(roles);
    expect(clearApplications).toHaveBeenCalledOnce();
    rolesService.getPage(params).subscribe();
    httpTesting.expectOne(`${BASE_URL}/role/8?page=0&size=10`).flush(page([]));
  });

  it('validates with a reason and invalidates cached context pages only on success', () => {
    const params = { appSecurityId: 8, page: 0, size: 10 };
    webContextsService.getPage(params).subscribe();
    httpTesting.expectOne(`${BASE_URL}/web-context/8?page=0&size=10`).flush(page([]));

    webContextsService.validate(4, 'Motiu').subscribe({ error: () => undefined });
    const failed = httpTesting.expectOne(`${BASE_URL}/web-context/validate/4`);
    expect(failed.request.method).toBe('PUT');
    expect(failed.request.body).toEqual({ reason: 'Motiu' });
    failed.flush(null, { status: 400, statusText: 'Bad request' });
    webContextsService.getPage(params).subscribe();
    httpTesting.expectNone(`${BASE_URL}/web-context/8?page=0&size=10`);

    webContextsService.validate(4, 'Motiu').subscribe();
    httpTesting.expectOne(`${BASE_URL}/web-context/validate/4`).flush(null, { status: 204, statusText: 'No Content' });
    webContextsService.getPage(params).subscribe();
    httpTesting.expectOne(`${BASE_URL}/web-context/8?page=0&size=10`).flush(page([]));
  });

  it('scans active context pages until it finds a pending verification', () => {
    const result = vi.fn();
    webContextsService.hasUnverified(8).subscribe(result);
    const first = httpTesting.expectOne(req => req.url === `${BASE_URL}/web-context/8` && req.params.get('page') === '0');
    expect(first.request.params.get('statusId')).toBe(String(SoftDeleteStatus.ACTIVE));
    first.flush({ ...page([{ validated: true }]), totalPages: 3, totalElements: 21 });
    httpTesting.expectOne(req => req.url === `${BASE_URL}/web-context/8` && req.params.get('page') === '1')
      .flush({ ...page([{ validated: false }]), number: 1, totalPages: 3, totalElements: 21 });
    expect(result).toHaveBeenCalledExactlyOnceWith(true);
    httpTesting.expectNone(req => req.url === `${BASE_URL}/web-context/8` && req.params.get('page') === '2');
  });

  it('clears the pending indicator only after checking every active page', () => {
    const result = vi.fn();
    webContextsService.hasUnverified(8).subscribe(result);
    httpTesting.expectOne(req => req.url === `${BASE_URL}/web-context/8` && req.params.get('page') === '0')
      .flush({ ...page([{ validated: true }]), number: 0, totalPages: 2, totalElements: 11 });
    expect(result).not.toHaveBeenCalled();
    httpTesting.expectOne(req => req.url === `${BASE_URL}/web-context/8` && req.params.get('page') === '1')
      .flush({ ...page([{ validated: true }]), number: 1, totalPages: 2, totalElements: 11 });
    expect(result).toHaveBeenCalledExactlyOnceWith(false);
  });

  it('shares the unpaged security-level catalog using its real response contract', () => {
    const items: SecurityCatalogItem[] = [{ id: 1, name: 'Alt', nameEs: 'Alto' }];
    const first = vi.fn();
    const shared = vi.fn();

    securityLevelsService.getAll().subscribe(first);
    securityLevelsService.getAll().subscribe(shared);
    httpTesting.expectOne('/invaiback/security-level').flush(items);

    expect(first).toHaveBeenCalledWith(items);
    expect(shared).toHaveBeenCalledWith(items);
    securityLevelsService.getAll().subscribe();
    httpTesting.expectNone('/invaiback/security-level');
  });
});

function security(): ApplicationSecurityOutput {
  return {
    id: 8,
    application: { id: 7 } as ApplicationSecurityOutput['application'],
    observation: 'Inicial',
    deletedAt: null,
  };
}

function page<T>(content: T[]) {
  return {
    content,
    empty: content.length === 0,
    first: true,
    last: true,
    number: 0,
    numberOfElements: content.length,
    pageable: {
      offset: 0,
      pageNumber: 0,
      pageSize: 10,
      paged: true,
      sort: { empty: true, sorted: false, unsorted: true },
      unpaged: false,
    },
    size: 10,
    sort: { empty: true, sorted: false, unsorted: true },
    totalElements: content.length,
    totalPages: content.length ? 1 : 0,
  };
}
