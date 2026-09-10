import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { AdministrativeUnitsService } from './administrative-units.service';

const ADMINISTRATIVE_UNITS_URL = '/invaiapi/interna/adm-unit';

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

  it('loads departments with pagination params', () => {
    const result = vi.fn();
    service
      .getDepartments({ page: 1, size: 10, sort: ['name,asc', 'code,desc'] })
      .subscribe(result);

    const request = httpTesting.expectOne(
      (req) =>
        req.method === 'GET' &&
        req.url === `${ADMINISTRATIVE_UNITS_URL}/departments` &&
        req.params.get('page') === '1' &&
        req.params.get('size') === '10' &&
        req.params.getAll('sort')?.join('|') === 'name,asc|code,desc',
    );
    request.flush(page([unit('GVA01', 'Conselleria')]));

    expect(result).toHaveBeenCalledWith(
      expect.objectContaining({ content: [unit('GVA01', 'Conselleria')] }),
    );
  });

  it('loads and encodes the selected department when requesting its units', () => {
    const result = vi.fn();
    service.getAdmUnitsByDepartment('GVA/01', { page: 0, size: 25 }).subscribe(result);

    const request = httpTesting.expectOne(
      (req) =>
        req.method === 'GET' &&
        req.url === `${ADMINISTRATIVE_UNITS_URL}/departments/GVA%2F01/adm-units` &&
        req.params.get('size') === '25',
    );
    request.flush(page([unit('UA01', 'Unitat', 'GVA/01')]));

    expect(result).toHaveBeenCalledWith(expect.objectContaining({ totalElements: 1 }));
  });

  it('shares cache entries and keeps departments and unit requests isolated', () => {
    const departmentResult = vi.fn();
    const cachedDepartmentResult = vi.fn();
    const unitResult = vi.fn();

    service.getDepartments({ page: 0 }).subscribe(departmentResult);
    service.getDepartments({ page: 0 }).subscribe(cachedDepartmentResult);
    httpTesting
      .expectOne(`${ADMINISTRATIVE_UNITS_URL}/departments?page=0`)
      .flush(page([unit('GVA01', 'Conselleria')]));

    service.getAdmUnitsByDepartment('GVA01', { page: 0 }).subscribe(unitResult);
    httpTesting
      .expectOne(`${ADMINISTRATIVE_UNITS_URL}/departments/GVA01/adm-units?page=0`)
      .flush(page([unit('UA01', 'Unitat', 'GVA01')]));

    expect(departmentResult).toHaveBeenCalledOnce();
    expect(cachedDepartmentResult).toHaveBeenCalledOnce();
    expect(unitResult).toHaveBeenCalledOnce();
  });

  it('evicts failed requests so they can be retried', () => {
    const error = vi.fn();
    const result = vi.fn();

    service.getDepartments({ page: 0 }).subscribe({ error });
    httpTesting
      .expectOne(`${ADMINISTRATIVE_UNITS_URL}/departments?page=0`)
      .flush('Request failed', { status: 500, statusText: 'Server Error' });

    service.getDepartments({ page: 0 }).subscribe(result);
    httpTesting
      .expectOne(`${ADMINISTRATIVE_UNITS_URL}/departments?page=0`)
      .flush(page([unit('GVA01', 'Conselleria')]));

    expect(error).toHaveBeenCalledOnce();
    expect(result).toHaveBeenCalledWith(expect.objectContaining({ totalElements: 1 }));
  });

  it('clears both catalog caches explicitly', () => {
    service.getDepartments().subscribe();
    httpTesting
      .expectOne(`${ADMINISTRATIVE_UNITS_URL}/departments`)
      .flush(page([unit('GVA01', 'Conselleria')]));
    service.getAdmUnitsByDepartment('GVA01').subscribe();
    httpTesting
      .expectOne(`${ADMINISTRATIVE_UNITS_URL}/departments/GVA01/adm-units`)
      .flush(page([unit('UA01', 'Unitat', 'GVA01')]));

    service.clearCache();
    service.getDepartments().subscribe();
    httpTesting.expectOne(`${ADMINISTRATIVE_UNITS_URL}/departments`).flush(page([]));
    service.getAdmUnitsByDepartment('GVA01').subscribe();
    httpTesting
      .expectOne(`${ADMINISTRATIVE_UNITS_URL}/departments/GVA01/adm-units`)
      .flush(page([]));
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
