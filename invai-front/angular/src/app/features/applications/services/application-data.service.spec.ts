import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { SKIP_SERVER_ERROR_DIALOG } from '@core/interceptors/http-error-logging.interceptor';
import { ApplicationDataInput } from '../applications.model';
import { ApplicationsService } from './applications.service';
import { ApplicationDataService } from './application-data.service';

describe('ApplicationDataService', () => {
  let service: ApplicationDataService;
  let http: HttpTestingController;
  const clearCache = vi.fn();
  const url = '/invaiback/application/data';
  const record = { id: 5, application: { id: 7 }, openData: [], reuse: [] };
  const payload: ApplicationDataInput = {
    applicationId: 7,
    observation: '<p>Notes</p>',
    openDataUrl: 'https://intranet.caib.es/invaiapi/externa/swagger.json',
    useOpenDataUrl: true,
    reuseUrl: null,
    useReuseUrl: false,
  };

  beforeEach(() => {
    clearCache.mockClear();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: ApplicationsService, useValue: { clearCache } },
      ],
    });
    service = TestBed.inject(ApplicationDataService);
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());

  it('reads the anchor by ID, sharing one request and opting out of the global error dialog', () => {
    const first = vi.fn();
    const second = vi.fn();
    service.getById(5).subscribe(first);
    service.getById(5).subscribe(second);

    const request = http.expectOne(`${url}/5`);
    expect(request.request.method).toBe('GET');
    expect(request.request.context.get(SKIP_SERVER_ERROR_DIALOG)).toBe(true);
    request.flush(record);

    expect(first).toHaveBeenCalledWith(record);
    expect(second).toHaveBeenCalledWith(record);
    service.getById(5).subscribe();
    http.expectNone(`${url}/5`);
  });

  it('does not cache an empty response', () => {
    const result = vi.fn();
    service.getById(5).subscribe(result);
    http.expectOne(`${url}/5`).flush(null);
    expect(result).toHaveBeenCalledWith(null);

    service.getById(5).subscribe(result);
    http.expectOne(`${url}/5`).flush(record);
    expect(result).toHaveBeenLastCalledWith(record);
  });

  it('evicts a failed read so a retry reaches the backend again', () => {
    const error = vi.fn();
    service.getById(5).subscribe({ error });
    http
      .expectOne(`${url}/5`)
      .flush(
        { error: 'Error', message: 'Documento no disponible' },
        { status: 400, statusText: 'Bad Request' },
      );
    expect(error).toHaveBeenCalledOnce();

    service.getById(5).subscribe();
    http.expectOne(`${url}/5`).flush(record);
  });

  it('refreshes a cached read', () => {
    service.getById(5).subscribe();
    http.expectOne(`${url}/5`).flush(record);

    const result = vi.fn();
    service.refreshById(5).subscribe(result);
    const fresh = { ...record, observation: 'updated' };
    http.expectOne(`${url}/5`).flush(fresh);
    expect(result).toHaveBeenCalledWith(fresh);
  });

  it.each(['create', 'update'] as const)(
    'invalidates the caches only after a successful %s',
    (operation) => {
      service.getById(5).subscribe();
      http.expectOne(`${url}/5`).flush(record);
      const requestUrl = operation === 'create' ? url : `${url}/5`;
      const send = () =>
        operation === 'create' ? service.create(payload) : service.update(5, payload);

      send().subscribe({ error: () => undefined });
      const failed = http.expectOne(requestUrl);
      expect(failed.request.method).toBe(operation === 'create' ? 'POST' : 'PUT');
      expect(failed.request.body).toEqual(payload);
      expect(failed.request.context.get(SKIP_SERVER_ERROR_DIALOG)).toBe(false);
      failed.flush({}, { status: 403, statusText: 'Forbidden' });
      expect(clearCache).not.toHaveBeenCalled();
      service.getById(5).subscribe();
      http.expectNone(`${url}/5`);

      send().subscribe();
      http.expectOne(requestUrl).flush({ ...record, openData: null, reuse: null });
      expect(clearCache).toHaveBeenCalledOnce();
      service.getById(5).subscribe();
      http.expectOne(`${url}/5`).flush(record);
    },
  );
});
