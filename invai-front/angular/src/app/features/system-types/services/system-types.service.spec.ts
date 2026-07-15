import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { SystemTypesService } from './system-types.service';

const SYSTEM_TYPES_URL = '/invaiapi/interna/system-type';

describe('SystemTypesService', () => {
  let service: SystemTypesService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(SystemTypesService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('loads system types with pagination params', () => {
    const result = vi.fn();

    service.getAll({ page: 3, size: 15, sort: ['name,asc', 'id,desc'] }).subscribe(result);

    const request = httpTesting.expectOne(
      (req) =>
        req.method === 'GET' &&
        req.url === SYSTEM_TYPES_URL &&
        req.params.get('page') === '3' &&
        req.params.get('size') === '15' &&
        req.params.getAll('sort')?.join('|') === 'name,asc|id,desc',
    );

    request.flush(page([{ id: 1, name: 'Instrumental' }]));

    expect(result).toHaveBeenCalledWith(
      expect.objectContaining({
        content: [{ id: 1, name: 'Instrumental' }],
      }),
    );
  });

  it('shares and reuses cached system type pages by params', () => {
    const firstResult = vi.fn();
    const secondResult = vi.fn();
    const cachedResult = vi.fn();

    service.getAll({ page: 3, size: 15, sort: ['name,asc', 'id,desc'] }).subscribe(firstResult);
    service.getAll({ page: 3, size: 15, sort: ['name,asc', 'id,desc'] }).subscribe(secondResult);

    httpTesting
      .expectOne(
        (req) =>
          req.method === 'GET' &&
          req.url === SYSTEM_TYPES_URL &&
          req.params.get('page') === '3',
      )
      .flush(page([{ id: 1, name: 'Instrumental' }]));

    service.getAll({ page: 3, size: 15, sort: ['name,asc', 'id,desc'] }).subscribe(cachedResult);

    httpTesting.expectNone(SYSTEM_TYPES_URL);
    expect(firstResult).toHaveBeenCalledWith(expect.objectContaining({ totalElements: 1 }));
    expect(secondResult).toHaveBeenCalledWith(expect.objectContaining({ totalElements: 1 }));
    expect(cachedResult).toHaveBeenCalledWith(expect.objectContaining({ totalElements: 1 }));
  });

  it('keeps system type cache entries separated by params', () => {
    const firstResult = vi.fn();
    const secondResult = vi.fn();

    service.getAll({ page: 0, size: 10, sort: 'name,asc' }).subscribe(firstResult);
    httpTesting
      .expectOne(
        (req) =>
          req.method === 'GET' &&
          req.url === SYSTEM_TYPES_URL &&
          req.params.get('page') === '0',
      )
      .flush(page([{ id: 1, name: 'Instrumental' }]));

    service.getAll({ page: 1, size: 10, sort: 'name,asc' }).subscribe(secondResult);
    httpTesting
      .expectOne(
        (req) =>
          req.method === 'GET' &&
          req.url === SYSTEM_TYPES_URL &&
          req.params.get('page') === '1',
      )
      .flush(page([{ id: 2, name: 'Corporatiu' }]));

    expect(firstResult).toHaveBeenCalledWith(
      expect.objectContaining({ content: [{ id: 1, name: 'Instrumental' }] }),
    );
    expect(secondResult).toHaveBeenCalledWith(
      expect.objectContaining({ content: [{ id: 2, name: 'Corporatiu' }] }),
    );
  });

  it('retries loading system types after a failed cached request', () => {
    const errorResult = vi.fn();
    const retryResult = vi.fn();

    service.getAll({ page: 0 }).subscribe({ error: errorResult });
    httpTesting
      .expectOne(
        (req) =>
          req.method === 'GET' &&
          req.url === SYSTEM_TYPES_URL &&
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
          req.url === SYSTEM_TYPES_URL &&
          req.params.get('page') === '0',
      )
      .flush(page([{ id: 1, name: 'Instrumental' }]));

    expect(errorResult).toHaveBeenCalledOnce();
    expect(retryResult).toHaveBeenCalledWith(expect.objectContaining({ totalElements: 1 }));
  });

  it('loads a system type by id', () => {
    const result = vi.fn();

    service.getById(7).subscribe(result);

    const request = httpTesting.expectOne(`${SYSTEM_TYPES_URL}/7`);
    expect(request.request.method).toBe('GET');
    request.flush({ id: 7, name: 'Instrumental' });

    expect(result).toHaveBeenCalledWith({ id: 7, name: 'Instrumental' });
  });

  it('creates a system type', () => {
    const result = vi.fn();

    service.create({ name: 'Instrumental' }).subscribe(result);

    const request = httpTesting.expectOne(SYSTEM_TYPES_URL);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ name: 'Instrumental' });
    request.flush({ id: 9, name: 'Instrumental' });

    expect(result).toHaveBeenCalledWith({ id: 9, name: 'Instrumental' });
  });

  it('clears cached system types after creating one', () => {
    const refreshedResult = vi.fn();

    service.getAll().subscribe();
    httpTesting.expectOne(SYSTEM_TYPES_URL).flush(page([{ id: 1, name: 'A' }]));

    service.create({ name: 'B' }).subscribe();
    httpTesting.expectOne(SYSTEM_TYPES_URL).flush({ id: 2, name: 'B' });

    service.getAll().subscribe(refreshedResult);
    httpTesting.expectOne(SYSTEM_TYPES_URL).flush(page([{ id: 2, name: 'B' }]));

    expect(refreshedResult).toHaveBeenCalledWith(
      expect.objectContaining({ content: [{ id: 2, name: 'B' }] }),
    );
  });

  it('updates a system type', () => {
    const result = vi.fn();

    service.update(9, { name: 'Corporatiu' }).subscribe(result);

    const request = httpTesting.expectOne(`${SYSTEM_TYPES_URL}/9`);
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual({ name: 'Corporatiu' });
    request.flush({ id: 9, name: 'Corporatiu' });

    expect(result).toHaveBeenCalledWith({ id: 9, name: 'Corporatiu' });
  });

  it('clears cached system types after updating one', () => {
    const refreshedResult = vi.fn();

    service.getAll().subscribe();
    httpTesting.expectOne(SYSTEM_TYPES_URL).flush(page([{ id: 1, name: 'A' }]));

    service.update(1, { name: 'B' }).subscribe();
    httpTesting.expectOne(`${SYSTEM_TYPES_URL}/1`).flush({ id: 1, name: 'B' });

    service.getAll().subscribe(refreshedResult);
    httpTesting.expectOne(SYSTEM_TYPES_URL).flush(page([{ id: 1, name: 'B' }]));

    expect(refreshedResult).toHaveBeenCalledWith(
      expect.objectContaining({ content: [{ id: 1, name: 'B' }] }),
    );
  });

  it('deletes a system type', () => {
    const result = vi.fn();

    service.delete(9).subscribe(result);

    const request = httpTesting.expectOne(`${SYSTEM_TYPES_URL}/9`);
    expect(request.request.method).toBe('DELETE');
    request.flush(null);

    expect(result).toHaveBeenCalledWith(null);
  });

  it('clears cached system types after deleting one', () => {
    const refreshedResult = vi.fn();

    service.getAll().subscribe();
    httpTesting.expectOne(SYSTEM_TYPES_URL).flush(page([{ id: 1, name: 'A' }]));

    service.delete(1).subscribe();
    httpTesting.expectOne(`${SYSTEM_TYPES_URL}/1`).flush(null);

    service.getAll().subscribe(refreshedResult);
    httpTesting.expectOne(SYSTEM_TYPES_URL).flush(page([]));

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
      pageSize: 15,
      paged: true,
      sort: { empty: false, sorted: true, unsorted: false },
      unpaged: false,
    },
    size: 15,
    sort: { empty: false, sorted: true, unsorted: false },
    totalElements: content.length,
    totalPages: 1,
  };
}
