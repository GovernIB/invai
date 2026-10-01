import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';
import { Observable } from 'rxjs';

import { ApplicationWebContextInput, ApplicationWebContextOutput } from '../applications.model';
import { ApplicationDevelopmentWebContextsService } from './application-development-web-contexts.service';

const BASE_URL = '/invaiback/application/development/web-context';
const payload: ApplicationWebContextInput = {
  appDevelopmentId: 9,
  webContextId: 2,
  fieldId: 3,
  url: null,
  observation: 'Nota',
};
const params = { appDevelopmentId: 9, page: 0, size: 10, statusId: SoftDeleteStatus.ACTIVE };
const page = { content: [], number: 0, totalElements: 0, totalPages: 0 };
const mutations: Array<{
  method: string;
  path: string;
  run: (api: ApplicationDevelopmentWebContextsService) => Observable<unknown>;
}> = [
  { method: 'POST', path: BASE_URL, run: (api) => api.create(payload) },
  { method: 'PUT', path: `${BASE_URL}/4`, run: (api) => api.update(4, payload) },
  { method: 'DELETE', path: `${BASE_URL}/4`, run: (api) => api.delete(4) },
];

describe('ApplicationDevelopmentWebContextsService', () => {
  let service: ApplicationDevelopmentWebContextsService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(ApplicationDevelopmentWebContextsService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('shares identical pages and separates anchors, pagination and status', () => {
    service.getPage(params).subscribe();
    service.getPage(params).subscribe();
    const first = http.expectOne(req => req.url === `${BASE_URL}/9`);
    expect(first.request.method).toBe('GET');
    expect(first.request.params.get('page')).toBe('0');
    expect(first.request.params.get('size')).toBe('10');
    expect(first.request.params.get('statusId')).toBe('1');
    first.flush(page);
    service.getPage(params).subscribe();
    http.expectNone(req => req.url === `${BASE_URL}/9`);

    service.getPage({ ...params, appDevelopmentId: 10 }).subscribe();
    http.expectOne(req => req.url === `${BASE_URL}/10`).flush(page);
    service.getPage({ ...params, page: 1 }).subscribe();
    http.expectOne(req => req.url === `${BASE_URL}/9` && req.params.get('page') === '1').flush(page);
    service.getPage({ ...params, statusId: undefined }).subscribe();
    const all = http.expectOne(req => req.url === `${BASE_URL}/9` && !req.params.has('statusId'));
    all.flush(page);
  });

  it('retries a failed page request', () => {
    service.getPage(params).subscribe({ error: () => undefined });
    http.expectOne(req => req.url === `${BASE_URL}/9`).flush(null, { status: 500, statusText: 'Error' });
    service.getPage(params).subscribe();
    http.expectOne(req => req.url === `${BASE_URL}/9`).flush(page);
  });

  it.each(mutations)('uses $method on the Development endpoint and invalidates only after success', ({ method, path, run }) => {
    service.getPage(params).subscribe();
    http.expectOne(req => req.url === `${BASE_URL}/9`).flush(page);

    run(service).subscribe({ error: () => undefined });
    const failed = http.expectOne(path);
    expect(failed.request.method).toBe(method);
    if (method !== 'DELETE') expect(failed.request.body).toEqual(payload);
    failed.flush(null, { status: 500, statusText: 'Error' });
    service.getPage(params).subscribe();
    http.expectNone(req => req.method === 'GET');

    run(service).subscribe();
    http.expectOne(path).flush(method === 'DELETE' ? null : ({ id: 4 } as ApplicationWebContextOutput));
    service.getPage(params).subscribe();
    http.expectOne(req => req.url === `${BASE_URL}/9`).flush(page);
  });
});
