import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';

import {
  ApplicationSecurityOutput,
  ApplicationWebContextInput,
  ApplicationWebContextOutput,
  SecurityCatalogItem,
} from '../applications.model';
import {
  ApplicationSecurityRolesService,
  ApplicationSecurityService,
  ApplicationWebContextsService,
  SecurityLevelsService,
} from './application-security.service';

const BASE_URL = '/invaiapi/interna/application/security';

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

  it('invalidates a web-context page only after a successful mutation', () => {
    const params = { appSecurityId: 8, page: 0, size: 10 };
    webContextsService.getPage(params).subscribe();
    httpTesting.expectOne(`${BASE_URL}/web-context/8?page=0&size=10`).flush(page([]));

    const payload: ApplicationWebContextInput = {
      appSecurityId: 8,
      webContextId: 2,
      fieldId: 3,
      observation: null,
    };
    webContextsService.create(payload).subscribe();
    const createRequest = httpTesting.expectOne(`${BASE_URL}/web-context`);
    expect(createRequest.request.body).toEqual(payload);
    createRequest.flush({ id: 4 } as ApplicationWebContextOutput);

    webContextsService.getPage(params).subscribe();
    httpTesting.expectOne(`${BASE_URL}/web-context/8?page=0&size=10`).flush(page([]));
  });

  it('shares the unpaged security-level catalog using its real response contract', () => {
    const items: SecurityCatalogItem[] = [{ id: 1, name: 'Alt', nameEs: 'Alto' }];
    const first = vi.fn();
    const shared = vi.fn();

    securityLevelsService.getAll().subscribe(first);
    securityLevelsService.getAll().subscribe(shared);
    httpTesting.expectOne('/invaiapi/interna/security-level').flush(items);

    expect(first).toHaveBeenCalledWith(items);
    expect(shared).toHaveBeenCalledWith(items);
    securityLevelsService.getAll().subscribe();
    httpTesting.expectNone('/invaiapi/interna/security-level');
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
