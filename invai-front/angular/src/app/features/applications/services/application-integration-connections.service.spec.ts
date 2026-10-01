import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { SKIP_SERVER_ERROR_DIALOG } from '@core/interceptors/http-error-logging.interceptor';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';
import { Observable } from 'rxjs';

import { ApplicationIntegrationConnectionInput } from '../applications.model';
import { ApplicationIntegrationConnectionsService } from './application-integration-connections.service';
import { ApplicationsService } from './applications.service';

const URL = '/invaiback/application/integration-connection';
const payload: ApplicationIntegrationConnectionInput = {
  appIntegrationId: 13,
  applicationId: null,
  externalSystemId: 2,
  technologyId: 4,
  username: 'u00004',
  requiredRoleIds: [26, 33],
};
const params = { appIntegrationId: 13, page: 0, size: 10, sort: 'id,asc', statusId: SoftDeleteStatus.ACTIVE };
const page = { content: [], number: 0, totalElements: 0, totalPages: 0 };
const mutations: Array<{
  method: string;
  path: string;
  run: (api: ApplicationIntegrationConnectionsService) => Observable<unknown>;
}> = [
  { method: 'POST', path: URL, run: (api) => api.create(payload) },
  { method: 'PUT', path: `${URL}/4`, run: (api) => api.update(4, payload) },
  { method: 'DELETE', path: `${URL}/4`, run: (api) => api.delete(4) },
];

describe('ApplicationIntegrationConnectionsService', () => {
  let service: ApplicationIntegrationConnectionsService;
  let http: HttpTestingController;
  const clearCache = vi.fn();

  beforeEach(() => {
    clearCache.mockClear();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: ApplicationsService, useValue: { clearCache } },
      ],
    });
    service = TestBed.inject(ApplicationIntegrationConnectionsService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('sends the anchor as a query parameter and handles Soffid failures inline', () => {
    service.getPage(params).subscribe();
    const request = http.expectOne((req) => req.url === URL);
    expect(request.request.method).toBe('GET');
    expect(request.request.params.get('appIntegrationId')).toBe('13');
    expect(request.request.params.get('statusId')).toBe('1');
    expect(request.request.params.get('page')).toBe('0');
    expect(request.request.params.get('size')).toBe('10');
    expect(request.request.params.getAll('sort')).toEqual(['id,asc']);
    expect(request.request.context.get(SKIP_SERVER_ERROR_DIALOG)).toBe(true);
    request.flush(page);
  });

  it('shares identical pages and separates anchors, pagination and status', () => {
    service.getPage(params).subscribe();
    service.getPage(params).subscribe();
    http.expectOne((req) => req.url === URL).flush(page);
    service.getPage(params).subscribe();
    http.expectNone((req) => req.url === URL);

    service.getPage({ ...params, appIntegrationId: 14 }).subscribe();
    http.expectOne((req) => req.params.get('appIntegrationId') === '14').flush(page);
    service.getPage({ ...params, page: 1 }).subscribe();
    http.expectOne((req) => req.params.get('page') === '1').flush(page);
    service.getPage({ ...params, statusId: undefined }).subscribe();
    http.expectOne((req) => req.url === URL && !req.params.has('statusId')).flush(page);
  });

  it('retries a failed page request', () => {
    service.getPage(params).subscribe({ error: () => undefined });
    http
      .expectOne((req) => req.url === URL)
      .flush(
        { error: 'Error', message: "No s'ha pogut consultar els rols atorgats." },
        { status: 400, statusText: 'Bad Request' },
      );
    service.getPage(params).subscribe();
    http.expectOne((req) => req.url === URL).flush(page);
  });

  it.each(mutations)(
    'uses $method and invalidates the pages and application detail only after success',
    ({ method, path, run }) => {
      service.getPage(params).subscribe();
      http.expectOne((req) => req.url === URL && req.method === 'GET').flush(page);

      run(service).subscribe({ error: () => undefined });
      const failed = http.expectOne((req) => req.url === path && req.method === method);
      if (method !== 'DELETE') expect(failed.request.body).toEqual(payload);
      failed.flush(null, { status: 500, statusText: 'Error' });
      expect(clearCache).not.toHaveBeenCalled();
      service.getPage(params).subscribe();
      http.expectNone((req) => req.method === 'GET');

      run(service).subscribe();
      http.expectOne((req) => req.url === path && req.method === method).flush(method === 'DELETE' ? null : {});
      expect(clearCache).toHaveBeenCalledOnce();
      service.getPage(params).subscribe();
      http.expectOne((req) => req.url === URL && req.method === 'GET').flush(page);
    },
  );
});
