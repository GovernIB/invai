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

  afterEach(() => {
    httpTesting.verify();
  });

  it('loads administrative units with pagination params', () => {
    const result = vi.fn();

    service.getAll({ page: 1, size: 10, sort: ['name,asc', 'id,desc'] }).subscribe(result);

    const request = httpTesting.expectOne(
      (req) =>
        req.method === 'GET' &&
        req.url === ADMINISTRATIVE_UNITS_URL &&
        req.params.get('page') === '1' &&
        req.params.get('size') === '10' &&
        req.params.getAll('sort')?.join('|') === 'name,asc|id,desc',
    );

    request.flush(page([{ id: 1, code: 'DGEDOT', name: 'Direcció General' }]));

    expect(result).toHaveBeenCalledWith(
      expect.objectContaining({
        content: [{ id: 1, code: 'DGEDOT', name: 'Direcció General' }],
      }),
    );
  });

  it('shares and reuses cached administrative unit pages by params', () => {
    const firstResult = vi.fn();
    const secondResult = vi.fn();
    const cachedResult = vi.fn();

    service.getAll({ page: 1, size: 10, sort: ['name,asc', 'id,desc'] }).subscribe(firstResult);
    service.getAll({ page: 1, size: 10, sort: ['name,asc', 'id,desc'] }).subscribe(secondResult);

    const request = httpTesting.expectOne(
      (req) =>
        req.method === 'GET' &&
        req.url === ADMINISTRATIVE_UNITS_URL &&
        req.params.get('page') === '1',
    );
    request.flush(page([{ id: 1, code: 'DGEDOT', name: 'Direcció General' }]));

    service.getAll({ page: 1, size: 10, sort: ['name,asc', 'id,desc'] }).subscribe(cachedResult);

    httpTesting.expectNone(ADMINISTRATIVE_UNITS_URL);
    expect(firstResult).toHaveBeenCalledWith(expect.objectContaining({ totalElements: 1 }));
    expect(secondResult).toHaveBeenCalledWith(expect.objectContaining({ totalElements: 1 }));
    expect(cachedResult).toHaveBeenCalledWith(expect.objectContaining({ totalElements: 1 }));
  });

  it('keeps administrative unit cache entries separated by params', () => {
    const firstResult = vi.fn();
    const secondResult = vi.fn();

    service.getAll({ page: 0, size: 10, sort: 'name,asc' }).subscribe(firstResult);
    httpTesting
      .expectOne(
        (req) =>
          req.method === 'GET' &&
          req.url === ADMINISTRATIVE_UNITS_URL &&
          req.params.get('page') === '0',
      )
      .flush(page([{ id: 1, code: 'DG', name: 'Direcció General' }]));

    service.getAll({ page: 1, size: 10, sort: 'name,asc' }).subscribe(secondResult);
    httpTesting
      .expectOne(
        (req) =>
          req.method === 'GET' &&
          req.url === ADMINISTRATIVE_UNITS_URL &&
          req.params.get('page') === '1',
      )
      .flush(page([{ id: 2, code: 'ST', name: 'Servei TIC' }]));

    expect(firstResult).toHaveBeenCalledWith(
      expect.objectContaining({ content: [{ id: 1, code: 'DG', name: 'Direcció General' }] }),
    );
    expect(secondResult).toHaveBeenCalledWith(
      expect.objectContaining({ content: [{ id: 2, code: 'ST', name: 'Servei TIC' }] }),
    );
  });

  it('retries loading administrative units after a failed cached request', () => {
    const errorResult = vi.fn();
    const retryResult = vi.fn();

    service.getAll({ page: 0 }).subscribe({ error: errorResult });
    httpTesting
      .expectOne(
        (req) =>
          req.method === 'GET' &&
          req.url === ADMINISTRATIVE_UNITS_URL &&
          req.params.get('page') === '0',
      )
      .flush('Request failed', {
        status: 500,
        statusText: 'Server Error',
      });

    service.getAll({ page: 0 }).subscribe(retryResult);
    httpTesting
      .expectOne(
        (req) =>
          req.method === 'GET' &&
          req.url === ADMINISTRATIVE_UNITS_URL &&
          req.params.get('page') === '0',
      )
      .flush(page([{ id: 1, code: 'DG', name: 'Direcció General' }]));

    expect(errorResult).toHaveBeenCalledOnce();
    expect(retryResult).toHaveBeenCalledWith(expect.objectContaining({ totalElements: 1 }));
  });

  it('loads an administrative unit by id', () => {
    const result = vi.fn();

    service.getById(7).subscribe(result);

    const request = httpTesting.expectOne(`${ADMINISTRATIVE_UNITS_URL}/7`);
    expect(request.request.method).toBe('GET');
    request.flush({ id: 7, code: 'DG', name: 'Direcció General' });

    expect(result).toHaveBeenCalledWith({ id: 7, code: 'DG', name: 'Direcció General' });
  });

  it('creates an administrative unit', () => {
    const result = vi.fn();

    service.create({ code: 'DG', name: 'Direcció General' }).subscribe(result);

    const request = httpTesting.expectOne(ADMINISTRATIVE_UNITS_URL);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ code: 'DG', name: 'Direcció General' });
    request.flush({ id: 9, code: 'DG', name: 'Direcció General' });

    expect(result).toHaveBeenCalledWith({ id: 9, code: 'DG', name: 'Direcció General' });
  });

  it('clears cached administrative units after creating one', () => {
    const refreshedResult = vi.fn();

    service.getAll().subscribe();
    httpTesting
      .expectOne(ADMINISTRATIVE_UNITS_URL)
      .flush(page([{ id: 1, code: 'A', name: 'Unit A' }]));

    service.create({ code: 'B', name: 'Unit B' }).subscribe();
    httpTesting.expectOne(ADMINISTRATIVE_UNITS_URL).flush({ id: 2, code: 'B', name: 'Unit B' });

    service.getAll().subscribe(refreshedResult);
    httpTesting
      .expectOne(ADMINISTRATIVE_UNITS_URL)
      .flush(page([{ id: 2, code: 'B', name: 'Unit B' }]));

    expect(refreshedResult).toHaveBeenCalledWith(
      expect.objectContaining({ content: [{ id: 2, code: 'B', name: 'Unit B' }] }),
    );
  });

  it('updates an administrative unit', () => {
    const result = vi.fn();

    service.update(9, { code: 'DGTIC', name: 'Direcció TIC' }).subscribe(result);

    const request = httpTesting.expectOne(`${ADMINISTRATIVE_UNITS_URL}/9`);
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual({ code: 'DGTIC', name: 'Direcció TIC' });
    request.flush({ id: 9, code: 'DGTIC', name: 'Direcció TIC' });

    expect(result).toHaveBeenCalledWith({ id: 9, code: 'DGTIC', name: 'Direcció TIC' });
  });

  it('clears cached administrative units after updating one', () => {
    const refreshedResult = vi.fn();

    service.getAll().subscribe();
    httpTesting
      .expectOne(ADMINISTRATIVE_UNITS_URL)
      .flush(page([{ id: 1, code: 'A', name: 'Unit A' }]));

    service.update(1, { code: 'B', name: 'Unit B' }).subscribe();
    httpTesting.expectOne(`${ADMINISTRATIVE_UNITS_URL}/1`).flush({
      id: 1,
      code: 'B',
      name: 'Unit B',
    });

    service.getAll().subscribe(refreshedResult);
    httpTesting
      .expectOne(ADMINISTRATIVE_UNITS_URL)
      .flush(page([{ id: 1, code: 'B', name: 'Unit B' }]));

    expect(refreshedResult).toHaveBeenCalledWith(
      expect.objectContaining({ content: [{ id: 1, code: 'B', name: 'Unit B' }] }),
    );
  });

  it('deletes an administrative unit', () => {
    const result = vi.fn();

    service.delete(9).subscribe(result);

    const request = httpTesting.expectOne(`${ADMINISTRATIVE_UNITS_URL}/9`);
    expect(request.request.method).toBe('DELETE');
    request.flush(null);

    expect(result).toHaveBeenCalledWith(null);
  });

  it('clears cached administrative units after deleting one', () => {
    const refreshedResult = vi.fn();

    service.getAll().subscribe();
    httpTesting
      .expectOne(ADMINISTRATIVE_UNITS_URL)
      .flush(page([{ id: 1, code: 'A', name: 'Unit A' }]));

    service.delete(1).subscribe();
    httpTesting.expectOne(`${ADMINISTRATIVE_UNITS_URL}/1`).flush(null);

    service.getAll().subscribe(refreshedResult);
    httpTesting.expectOne(ADMINISTRATIVE_UNITS_URL).flush(page([]));

    expect(refreshedResult).toHaveBeenCalledWith(expect.objectContaining({ content: [] }));
  });
});

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
