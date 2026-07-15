import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { CategoriesService } from './categories.service';

describe('CategoriesService', () => {
  let service: CategoriesService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(CategoriesService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('loads categories with pagination params', () => {
    const result = vi.fn();

    service.getAll({ page: 2, size: 25, sort: ['name,asc', 'id,desc'] }).subscribe(result);

    const request = httpTesting.expectOne(
      (req) =>
        req.method === 'GET' &&
        req.url === '/invaiapi/interna/category' &&
        req.params.get('page') === '2' &&
        req.params.get('size') === '25' &&
        req.params.getAll('sort')?.join('|') === 'name,asc|id,desc',
    );

    request.flush({
      content: [{ id: 1, name: 'DRASSANA' }],
      empty: false,
      first: false,
      last: true,
      number: 2,
      numberOfElements: 1,
      pageable: {
        offset: 50,
        pageNumber: 2,
        pageSize: 25,
        paged: true,
        sort: { empty: false, sorted: true, unsorted: false },
        unpaged: false,
      },
      size: 25,
      sort: { empty: false, sorted: true, unsorted: false },
      totalElements: 51,
      totalPages: 3,
    });

    expect(result).toHaveBeenCalledWith(
      expect.objectContaining({
        content: [{ id: 1, name: 'DRASSANA' }],
        totalElements: 51,
      }),
    );
  });

  it('shares and reuses cached category pages by params', () => {
    const firstResult = vi.fn();
    const secondResult = vi.fn();
    const cachedResult = vi.fn();

    service.getAll({ page: 2, size: 25, sort: ['name,asc', 'id,desc'] }).subscribe(firstResult);
    service.getAll({ page: 2, size: 25, sort: ['name,asc', 'id,desc'] }).subscribe(secondResult);

    const request = httpTesting.expectOne(
      (req) =>
        req.method === 'GET' &&
        req.url === '/invaiapi/interna/category' &&
        req.params.get('page') === '2',
    );

    request.flush(categoryPage([{ id: 1, name: 'DRASSANA' }]));

    service.getAll({ page: 2, size: 25, sort: ['name,asc', 'id,desc'] }).subscribe(cachedResult);

    httpTesting.expectNone('/invaiapi/interna/category');
    expect(firstResult).toHaveBeenCalledWith(expect.objectContaining({ totalElements: 1 }));
    expect(secondResult).toHaveBeenCalledWith(expect.objectContaining({ totalElements: 1 }));
    expect(cachedResult).toHaveBeenCalledWith(expect.objectContaining({ totalElements: 1 }));
  });

  it('keeps category cache entries separated by params', () => {
    const firstResult = vi.fn();
    const secondResult = vi.fn();

    service.getAll({ page: 0, size: 10, sort: 'name,asc' }).subscribe(firstResult);
    httpTesting
      .expectOne(
        (req) =>
          req.method === 'GET' &&
          req.url === '/invaiapi/interna/category' &&
          req.params.get('page') === '0',
      )
      .flush(categoryPage([{ id: 1, name: 'DRASSANA' }]));

    service.getAll({ page: 1, size: 10, sort: 'name,asc' }).subscribe(secondResult);
    httpTesting
      .expectOne(
        (req) =>
          req.method === 'GET' &&
          req.url === '/invaiapi/interna/category' &&
          req.params.get('page') === '1',
      )
      .flush(categoryPage([{ id: 2, name: 'Operacional' }]));

    expect(firstResult).toHaveBeenCalledWith(
      expect.objectContaining({ content: [{ id: 1, name: 'DRASSANA' }] }),
    );
    expect(secondResult).toHaveBeenCalledWith(
      expect.objectContaining({ content: [{ id: 2, name: 'Operacional' }] }),
    );
  });

  it('retries loading categories after a failed cached request', () => {
    const errorResult = vi.fn();
    const retryResult = vi.fn();

    service.getAll({ page: 0 }).subscribe({ error: errorResult });

    httpTesting
      .expectOne(
        (req) =>
          req.method === 'GET' &&
          req.url === '/invaiapi/interna/category' &&
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
          req.url === '/invaiapi/interna/category' &&
          req.params.get('page') === '0',
      )
      .flush(categoryPage([{ id: 1, name: 'DRASSANA' }]));

    expect(errorResult).toHaveBeenCalledOnce();
    expect(retryResult).toHaveBeenCalledWith(expect.objectContaining({ totalElements: 1 }));
  });

  it('loads a category by id', () => {
    const result = vi.fn();

    service.getById(7).subscribe(result);

    const request = httpTesting.expectOne('/invaiapi/interna/category/7');
    expect(request.request.method).toBe('GET');
    request.flush({ id: 7, name: 'Operacional' });

    expect(result).toHaveBeenCalledWith({ id: 7, name: 'Operacional' });
  });

  it('creates a category', () => {
    const result = vi.fn();

    service.create({ name: 'Sistemes' }).subscribe(result);

    const request = httpTesting.expectOne('/invaiapi/interna/category');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ name: 'Sistemes' });
    request.flush({ id: 9, name: 'Sistemes' });

    expect(result).toHaveBeenCalledWith({ id: 9, name: 'Sistemes' });
  });

  it('clears cached categories after creating a category', () => {
    const cachedResult = vi.fn();
    const refreshedResult = vi.fn();

    service.getAll().subscribe(cachedResult);
    httpTesting.expectOne('/invaiapi/interna/category').flush(categoryPage([{ id: 1, name: 'A' }]));

    service.create({ name: 'B' }).subscribe();
    httpTesting.expectOne('/invaiapi/interna/category').flush({ id: 2, name: 'B' });

    service.getAll().subscribe(refreshedResult);
    httpTesting.expectOne('/invaiapi/interna/category').flush(categoryPage([{ id: 2, name: 'B' }]));

    expect(refreshedResult).toHaveBeenCalledWith(
      expect.objectContaining({ content: [{ id: 2, name: 'B' }] }),
    );
  });

  it('updates a category', () => {
    const result = vi.fn();

    service.update(9, { name: 'Sistemes interns' }).subscribe(result);

    const request = httpTesting.expectOne('/invaiapi/interna/category/9');
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual({ name: 'Sistemes interns' });
    request.flush({ id: 9, name: 'Sistemes interns' });

    expect(result).toHaveBeenCalledWith({ id: 9, name: 'Sistemes interns' });
  });

  it('clears cached categories after updating a category', () => {
    const refreshedResult = vi.fn();

    service.getAll().subscribe();
    httpTesting.expectOne('/invaiapi/interna/category').flush(categoryPage([{ id: 1, name: 'A' }]));

    service.update(1, { name: 'B' }).subscribe();
    httpTesting.expectOne('/invaiapi/interna/category/1').flush({ id: 1, name: 'B' });

    service.getAll().subscribe(refreshedResult);
    httpTesting.expectOne('/invaiapi/interna/category').flush(categoryPage([{ id: 1, name: 'B' }]));

    expect(refreshedResult).toHaveBeenCalledWith(
      expect.objectContaining({ content: [{ id: 1, name: 'B' }] }),
    );
  });

  it('deletes a category', () => {
    const result = vi.fn();

    service.delete(9).subscribe(result);

    const request = httpTesting.expectOne('/invaiapi/interna/category/9');
    expect(request.request.method).toBe('DELETE');
    request.flush(null);

    expect(result).toHaveBeenCalledWith(null);
  });

  it('clears cached categories after deleting a category', () => {
    const refreshedResult = vi.fn();

    service.getAll().subscribe();
    httpTesting.expectOne('/invaiapi/interna/category').flush(categoryPage([{ id: 1, name: 'A' }]));

    service.delete(1).subscribe();
    httpTesting.expectOne('/invaiapi/interna/category/1').flush(null);

    service.getAll().subscribe(refreshedResult);
    httpTesting.expectOne('/invaiapi/interna/category').flush(categoryPage([]));

    expect(refreshedResult).toHaveBeenCalledWith(expect.objectContaining({ content: [] }));
  });
});

function categoryPage(content: { id: number; name: string | null }[]): Record<string, unknown> {
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
      pageSize: 25,
      paged: true,
      sort: { empty: false, sorted: true, unsorted: false },
      unpaged: false,
    },
    size: 25,
    sort: { empty: false, sorted: true, unsorted: false },
    totalElements: content.length,
    totalPages: 1,
  };
}
