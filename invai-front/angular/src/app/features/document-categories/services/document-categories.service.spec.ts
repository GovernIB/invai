import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';
import { Observable } from 'rxjs';

import { documentCategoriesPage, documentCategory } from '../document-categories.testing';
import { DocumentCategoriesService } from './document-categories.service';

describe('DocumentCategoriesService', () => {
  const url = '/invaiback/documentation/category';
  let service: DocumentCategoriesService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(DocumentCategoriesService);
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());

  it('lists the categories with their filters and shares a cached page', () => {
    const page = documentCategoriesPage([documentCategory()]);
    const first = vi.fn();
    const params = {
      page: 1,
      size: 10,
      sort: 'nameEs,desc',
      name: '  tec ',
      statusId: SoftDeleteStatus.INACTIVE,
    };

    service.getAll(params).subscribe(first);
    service.getAll(params).subscribe();
    const request = http.expectOne((req) => req.url === url);
    expect(request.request.params.toString()).toBe(
      'page=1&size=10&sort=nameEs,desc&name=tec&statusId=2',
    );
    request.flush(page);

    expect(first).toHaveBeenCalledWith(page);
  });

  it('does not cache a quick search', () => {
    service.getAll({ page: 0, size: 10, search: ' manuals ' }).subscribe();
    service.getAll({ page: 0, size: 10, search: ' manuals ' }).subscribe();

    const requests = http.match((req) => req.url === url);
    expect(requests).toHaveLength(2);
    expect(requests[0].request.params.get('search')).toBe('manuals');
    requests.forEach((request) => request.flush(documentCategoriesPage([])));
  });

  it.each([
    ['create', 'POST', url],
    ['update', 'PUT', `${url}/4`],
    ['delete', 'DELETE', `${url}/4`],
    ['reactivate', 'PUT', `${url}/reactivate/4`],
  ] as const)('invalidates the cached pages after %s', (operation, method, requestUrl) => {
    const input = { name: 'API', nameEs: 'API', parentId: 1 };
    service.getAll({ page: 0, size: 10 }).subscribe();
    http.expectOne((req) => req.url === url).flush(documentCategoriesPage([]));

    const send: () => Observable<unknown> = {
      create: () => service.create(input),
      update: () => service.update(4, input),
      delete: () => service.delete(4),
      reactivate: () => service.reactivate(4),
    }[operation];
    send().subscribe();
    const request = http.expectOne(requestUrl);
    expect(request.request.method).toBe(method);
    if (operation === 'create' || operation === 'update') expect(request.request.body).toEqual(input);
    if (operation === 'reactivate') expect(request.request.body).toBeNull();
    request.flush(operation === 'delete' ? null : documentCategory({ id: 4 }));

    service.getAll({ page: 0, size: 10 }).subscribe();
    http.expectOne((req) => req.url === url).flush(documentCategoriesPage([]));
  });

  it('reads one category or subcategory by ID', () => {
    const result = vi.fn();
    service.getById(4).subscribe(result);

    const request = http.expectOne(`${url}/4`);
    expect(request.request.method).toBe('GET');
    request.flush(documentCategory({ id: 4, parentId: 1 }));

    expect(result).toHaveBeenCalledWith(documentCategory({ id: 4, parentId: 1 }));
  });
});
