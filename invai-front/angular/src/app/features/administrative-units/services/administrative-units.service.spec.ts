import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { AdministrativeUnitsService } from './administrative-units.service';

const ADMINISTRATIVE_UNITS_URL = '/invaiback/adm-unit';

describe('AdministrativeUnitsService', () => {
  let service: AdministrativeUnitsService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AdministrativeUnitsService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTesting.verify());

  it('loads the unified DIR3 page with pagination and sorting', () => {
    service.getPage({ page: 1, size: 20, sort: ['name,asc', 'code,desc'] }).subscribe();
    const request = httpTesting.expectOne(req => req.url === ADMINISTRATIVE_UNITS_URL);
    expect(request.request.params.get('page')).toBe('1');
    expect(request.request.params.get('size')).toBe('20');
    expect(request.request.params.getAll('sort')).toEqual(['name,asc', 'code,desc']);
    request.flush(page([unit('D1', 'Department')]));
  });

  it('shares only blank searches and keys pages independently', () => {
    service.getPage({ page: 0, search: '   ' }).subscribe();
    service.getPage({ page: 0 }).subscribe();
    httpTesting.expectOne(`${ADMINISTRATIVE_UNITS_URL}?page=0`).flush(page([]));
    service.getPage({ page: 1 }).subscribe();
    httpTesting.expectOne(`${ADMINISTRATIVE_UNITS_URL}?page=1`).flush(page([]));
    for (let index = 0; index < 2; index++) {
      service.getPage({ page: 0, search: '  Educació  ' }).subscribe();
      const request = httpTesting.expectOne(req => req.url === ADMINISTRATIVE_UNITS_URL);
      expect(request.request.params.get('search')).toBe('Educació');
      request.flush(page([]));
    }
  });

  it('evicts failed entries and clears successful pages explicitly', () => {
    service.getPage().subscribe({ error: () => {} });
    httpTesting.expectOne(ADMINISTRATIVE_UNITS_URL).flush('failed', { status: 500, statusText: 'Error' });
    service.getPage().subscribe();
    httpTesting.expectOne(ADMINISTRATIVE_UNITS_URL).flush(page([]));
    service.clearCache();
    service.getPage().subscribe();
    httpTesting.expectOne(ADMINISTRATIVE_UNITS_URL).flush(page([]));
  });
});

function unit(code: string, name: string, parentCode: string | null = null) {
  return { code, name, parentCode, level: parentCode ? 2 : 1 };
}

function page<T>(content: T[]): Record<string, unknown> {
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
      sort: { empty: false, sorted: true, unsorted: false },
      unpaged: false,
    },
    size: 10,
    sort: { empty: false, sorted: true, unsorted: false },
    totalElements: content.length,
    totalPages: 1,
  };
}
