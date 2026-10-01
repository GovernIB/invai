import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';

import {
  ApplicationSecurityOutput,
  SecurityCatalogItem,
} from '../applications.model';
import {
  ApplicationSecurityRolesService,
  ApplicationSecurityService,
  ApplicationWebContextsService,
  SecurityLevelsService,
} from './application-security.service';

const BASE_URL = '/invaiback/application/security';

describe('application security services', () => {
  let httpTesting: HttpTestingController;
  let securityService: ApplicationSecurityService;
  let rolesService: ApplicationSecurityRolesService;
  let webContextsService: ApplicationWebContextsService;
  let securityLevelsService: SecurityLevelsService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    httpTesting = TestBed.inject(HttpTestingController);
    securityService = TestBed.inject(ApplicationSecurityService);
    rolesService = TestBed.inject(ApplicationSecurityRolesService);
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
