import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { FieldsService } from './fields.service';

const FIELDS_URL = '/invaiapi/interna/field';

describe('FieldsService', () => {
  let service: FieldsService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(FieldsService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('loads fields with pagination params', () => {
    const result = vi.fn();

    service.getAll({ page: 1, size: 20, sort: 'name,asc' }).subscribe(result);

    const request = httpTesting.expectOne(
      (req) =>
        req.method === 'GET' &&
        req.url === FIELDS_URL &&
        req.params.get('page') === '1' &&
        req.params.get('size') === '20' &&
        req.params.get('sort') === 'name,asc',
    );

    request.flush(page([{ id: 1, name: 'Departamental' }]));

    expect(result).toHaveBeenCalledWith(
      expect.objectContaining({
        content: [{ id: 1, name: 'Departamental' }],
      }),
    );
  });

  it('shares and reuses cached field pages by params', () => {
    const firstResult = vi.fn();
    const secondResult = vi.fn();
    const cachedResult = vi.fn();

    service.getAll({ page: 1, size: 20, sort: 'name,asc' }).subscribe(firstResult);
    service.getAll({ page: 1, size: 20, sort: 'name,asc' }).subscribe(secondResult);

    httpTesting
      .expectOne(
        (req) =>
          req.method === 'GET' &&
          req.url === FIELDS_URL &&
          req.params.get('page') === '1',
      )
      .flush(page([{ id: 1, name: 'Departamental' }]));

    service.getAll({ page: 1, size: 20, sort: 'name,asc' }).subscribe(cachedResult);

    httpTesting.expectNone(FIELDS_URL);
    expect(firstResult).toHaveBeenCalledWith(expect.objectContaining({ totalElements: 1 }));
    expect(secondResult).toHaveBeenCalledWith(expect.objectContaining({ totalElements: 1 }));
    expect(cachedResult).toHaveBeenCalledWith(expect.objectContaining({ totalElements: 1 }));
  });

  it('keeps field cache entries separated by params', () => {
    const firstResult = vi.fn();
    const secondResult = vi.fn();

    service.getAll({ page: 0, size: 10, sort: 'name,asc' }).subscribe(firstResult);
    httpTesting
      .expectOne(
        (req) =>
          req.method === 'GET' &&
          req.url === FIELDS_URL &&
          req.params.get('page') === '0',
      )
      .flush(page([{ id: 1, name: 'Departamental' }]));

    service.getAll({ page: 1, size: 10, sort: 'name,asc' }).subscribe(secondResult);
    httpTesting
      .expectOne(
        (req) =>
          req.method === 'GET' &&
          req.url === FIELDS_URL &&
          req.params.get('page') === '1',
      )
      .flush(page([{ id: 2, name: 'Transversal' }]));

    expect(firstResult).toHaveBeenCalledWith(
      expect.objectContaining({ content: [{ id: 1, name: 'Departamental' }] }),
    );
    expect(secondResult).toHaveBeenCalledWith(
      expect.objectContaining({ content: [{ id: 2, name: 'Transversal' }] }),
    );
  });

  it('retries loading fields after a failed cached request', () => {
    const errorResult = vi.fn();
    const retryResult = vi.fn();

    service.getAll({ page: 0 }).subscribe({ error: errorResult });
    httpTesting
      .expectOne(
        (req) =>
          req.method === 'GET' &&
          req.url === FIELDS_URL &&
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
          req.url === FIELDS_URL &&
          req.params.get('page') === '0',
      )
      .flush(page([{ id: 1, name: 'Departamental' }]));

    expect(errorResult).toHaveBeenCalledOnce();
    expect(retryResult).toHaveBeenCalledWith(expect.objectContaining({ totalElements: 1 }));
  });

  it('loads a field by id', () => {
    const result = vi.fn();

    service.getById(7).subscribe(result);

    const request = httpTesting.expectOne(`${FIELDS_URL}/7`);
    expect(request.request.method).toBe('GET');
    request.flush({ id: 7, name: 'Departamental' });

    expect(result).toHaveBeenCalledWith({ id: 7, name: 'Departamental' });
  });

  it('creates a field', () => {
    const result = vi.fn();

    service.create({ name: 'Departamental' }).subscribe(result);

    const request = httpTesting.expectOne(FIELDS_URL);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ name: 'Departamental' });
    request.flush({ id: 9, name: 'Departamental' });

    expect(result).toHaveBeenCalledWith({ id: 9, name: 'Departamental' });
  });

  it('clears cached fields after creating one', () => {
    const refreshedResult = vi.fn();

    service.getAll().subscribe();
    httpTesting.expectOne(FIELDS_URL).flush(page([{ id: 1, name: 'A' }]));

    service.create({ name: 'B' }).subscribe();
    httpTesting.expectOne(FIELDS_URL).flush({ id: 2, name: 'B' });

    service.getAll().subscribe(refreshedResult);
    httpTesting.expectOne(FIELDS_URL).flush(page([{ id: 2, name: 'B' }]));

    expect(refreshedResult).toHaveBeenCalledWith(
      expect.objectContaining({ content: [{ id: 2, name: 'B' }] }),
    );
  });

  it('updates a field', () => {
    const result = vi.fn();

    service.update(9, { name: 'Transversal' }).subscribe(result);

    const request = httpTesting.expectOne(`${FIELDS_URL}/9`);
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual({ name: 'Transversal' });
    request.flush({ id: 9, name: 'Transversal' });

    expect(result).toHaveBeenCalledWith({ id: 9, name: 'Transversal' });
  });

  it('clears cached fields after updating one', () => {
    const refreshedResult = vi.fn();

    service.getAll().subscribe();
    httpTesting.expectOne(FIELDS_URL).flush(page([{ id: 1, name: 'A' }]));

    service.update(1, { name: 'B' }).subscribe();
    httpTesting.expectOne(`${FIELDS_URL}/1`).flush({ id: 1, name: 'B' });

    service.getAll().subscribe(refreshedResult);
    httpTesting.expectOne(FIELDS_URL).flush(page([{ id: 1, name: 'B' }]));

    expect(refreshedResult).toHaveBeenCalledWith(
      expect.objectContaining({ content: [{ id: 1, name: 'B' }] }),
    );
  });

  it('deletes a field', () => {
    const result = vi.fn();

    service.delete(9).subscribe(result);

    const request = httpTesting.expectOne(`${FIELDS_URL}/9`);
    expect(request.request.method).toBe('DELETE');
    request.flush(null);

    expect(result).toHaveBeenCalledWith(null);
  });

  it('clears cached fields after deleting one', () => {
    const refreshedResult = vi.fn();

    service.getAll().subscribe();
    httpTesting.expectOne(FIELDS_URL).flush(page([{ id: 1, name: 'A' }]));

    service.delete(1).subscribe();
    httpTesting.expectOne(`${FIELDS_URL}/1`).flush(null);

    service.getAll().subscribe(refreshedResult);
    httpTesting.expectOne(FIELDS_URL).flush(page([]));

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
      pageSize: 20,
      paged: true,
      sort: { empty: false, sorted: true, unsorted: false },
      unpaged: false,
    },
    size: 20,
    sort: { empty: false, sorted: true, unsorted: false },
    totalElements: content.length,
    totalPages: 1,
  };
}
