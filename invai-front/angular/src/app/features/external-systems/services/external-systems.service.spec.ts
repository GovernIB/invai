import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';
import { Observable } from 'rxjs';

import { ExternalSystemInput } from '../external-systems.model';
import { ExternalSystemsService } from './external-systems.service';

const URL = '/invaiback/external-system';
const payload: ExternalSystemInput = { name: 'Soffid', companyId: 3 };
const page = { content: [], number: 0, totalElements: 0, totalPages: 0, last: true };

describe('ExternalSystemsService', () => {
  let service: ExternalSystemsService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(ExternalSystemsService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('shares equal list requests and separates filter parameters', () => {
    const params = { page: 0, size: 10, name: 'Sof', companyId: 3, statusId: SoftDeleteStatus.ACTIVE };
    service.getAll(params).subscribe();
    service.getAll(params).subscribe();
    const request = http.expectOne((req) => req.url === URL);
    expect(request.request.params.get('name')).toBe('Sof');
    expect(request.request.params.get('companyId')).toBe('3');
    expect(request.request.params.get('statusId')).toBe('1');
    request.flush(page);
    service.getAll(params).subscribe();
    http.expectNone((req) => req.url === URL);

    service.getAll({ ...params, companyId: 4 }).subscribe();
    http.expectOne((req) => req.params.get('companyId') === '4').flush(page);
    service.getAll({ ...params, statusId: undefined }).subscribe();
    http.expectOne((req) => req.url === URL && !req.params.has('statusId')).flush(page);
  });

  it('does not cache quick searches', () => {
    service.getAll({ page: 0, search: 'soff' }).subscribe();
    http.expectOne((req) => req.params.get('search') === 'soff').flush(page);
    service.getAll({ page: 0, search: 'soff' }).subscribe();
    http.expectOne((req) => req.params.get('search') === 'soff').flush(page);
  });

  it('retries a failed cached request', () => {
    service.getAll({ page: 0 }).subscribe({ error: () => undefined });
    http.expectOne((req) => req.url === URL).flush('Failed', { status: 500, statusText: 'Error' });
    service.getAll({ page: 0 }).subscribe();
    http.expectOne((req) => req.url === URL).flush(page);
  });

  it('reads a single record without caching it', () => {
    service.getById(7).subscribe();
    service.getById(7).subscribe();
    expect(http.match(`${URL}/7`)).toHaveLength(2);
  });

  it.each<[string, string, string, (api: ExternalSystemsService) => Observable<unknown>]>([
    ['create', 'POST', URL, (api) => api.create(payload)],
    ['update', 'PUT', `${URL}/7`, (api) => api.update(7, payload)],
    ['delete', 'DELETE', `${URL}/7`, (api) => api.delete(7)],
    ['reactivate', 'PUT', `${URL}/reactivate/7`, (api) => api.reactivate(7)],
  ])('invalidates the list cache after a successful %s', (_name, method, path, run) => {
    service.getAll({ page: 0 }).subscribe();
    http.expectOne((req) => req.url === URL && req.method === 'GET').flush(page);

    run(service).subscribe({ error: () => undefined });
    http.expectOne(path).flush(null, { status: 500, statusText: 'Error' });
    service.getAll({ page: 0 }).subscribe();
    http.expectNone((req) => req.method === 'GET');

    run(service).subscribe();
    const request = http.expectOne(path);
    expect(request.request.method).toBe(method);
    if (method === 'PUT' && path.includes('reactivate')) expect(request.request.body).toBeNull();
    else if (method !== 'DELETE') expect(request.request.body).toEqual(payload);
    request.flush(method === 'DELETE' ? null : { id: 7 });
    service.getAll({ page: 0 }).subscribe();
    http.expectOne((req) => req.url === URL && req.method === 'GET').flush(page);
  });
});
