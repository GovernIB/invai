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
  const record = {
    id: 5,
    application: { id: 7 },
    openDataUrl: 'https://intranet.caib.es/invaiapi/externa',
    reuseUrl: 'https://intranet.caib.es/invaiapi/interna',
  };
  const payload: ApplicationDataInput = { applicationId: 7, observation: '<p>Notes</p>' };

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
      http.expectOne(requestUrl).flush({ ...record, openDataUrl: null, reuseUrl: null });
      expect(clearCache).toHaveBeenCalledOnce();
      service.getById(5).subscribe();
      http.expectOne(`${url}/5`).flush(record);
    },
  );

  it.each([
    ['openData', 'open-data'],
    ['reuse', 'reuse'],
  ] as const)('sets the %s URL on its own and invalidates the caches after it', (source, path) => {
    service.getById(5).subscribe();
    http.expectOne(`${url}/5`).flush(record);
    const result = vi.fn();
    const answer = { url: 'https://manual.example/api', validUrl: false };

    service.updateUrl(5, source, 'https://manual.example/api').subscribe(result);
    const request = http.expectOne(`${url}/5/${path}`);
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual({ url: 'https://manual.example/api' });
    expect(request.request.context.get(SKIP_SERVER_ERROR_DIALOG)).toBe(false);
    request.flush(answer);

    expect(result).toHaveBeenCalledWith(answer);
    expect(clearCache).toHaveBeenCalledOnce();
    service.getById(5).subscribe();
    http.expectOne(`${url}/5`).flush(record);
  });

  describe('URL checks', () => {
    const checkUrl = `${url}/check-url`;
    const checks = () => http.match((request) => request.url === checkUrl);

    it('checks a URL once, encoding it and opting out of the global error dialog', () => {
      const checked = 'https://intranet.caib.es/app/api?q=a+b&v=2';
      const first = vi.fn();
      const second = vi.fn();

      service.checkUrl(checked).subscribe(first);
      service.checkUrl(checked).subscribe(second);

      const request = http.expectOne((pending) => pending.url === checkUrl);
      expect(request.request.method).toBe('GET');
      expect(request.request.params.get('url')).toBe(checked);
      // A `+` left as is would reach the back-end as a space.
      expect(request.request.urlWithParams).toBe(
        `${checkUrl}?url=https%3A%2F%2Fintranet.caib.es%2Fapp%2Fapi%3Fq%3Da%2Bb%26v%3D2`,
      );
      expect(request.request.context.get(SKIP_SERVER_ERROR_DIALOG)).toBe(true);
      request.flush({ url: checked, validUrl: true });

      // Opening the tab again gives the answer at once.
      const again = vi.fn();
      service.checkUrl(checked).subscribe(again);
      expect(checks()).toHaveLength(0);
      expect([first, second, again].map((result) => result.mock.calls)).toEqual(
        Array.from({ length: 3 }, () => [[{ url: checked, validUrl: true }]]),
      );
      expect(clearCache).not.toHaveBeenCalled();
    });

    it('checks each URL apart', () => {
      service.checkUrl('https://a.example').subscribe();
      service.checkUrl('https://b.example').subscribe();

      expect(checks().map((request) => request.request.params.get('url'))).toEqual([
        'https://a.example',
        'https://b.example',
      ]);
    });

    it('checks a URL again once its answer is five minutes old', () => {
      vi.useFakeTimers();
      vi.setSystemTime(0);
      try {
        service.checkUrl('https://a.example').subscribe();
        http.expectOne((request) => request.url === checkUrl).flush({ url: 'https://a.example', validUrl: true });

        vi.advanceTimersByTime(299999);
        service.checkUrl('https://a.example').subscribe();
        expect(checks()).toHaveLength(0);

        vi.advanceTimersByTime(1);
        service.checkUrl('https://a.example').subscribe();
        http.expectOne((request) => request.url === checkUrl).flush({ url: 'https://a.example', validUrl: false });
      } finally {
        vi.useRealTimers();
      }
    });

    it('queries a URL again after a check that could not be carried out', () => {
      service.checkUrl('https://a.example').subscribe({ error: () => undefined });
      http
        .expectOne((request) => request.url === checkUrl)
        .flush({}, { status: 400, statusText: 'Bad Request' });

      service.checkUrl('https://a.example').subscribe();
      http.expectOne((request) => request.url === checkUrl).flush({ url: 'https://a.example', validUrl: true });
    });

    it('takes the answer of saving the URL of a source as its check', () => {
      service.updateUrl(5, 'reuse', 'https://manual.example/api').subscribe();
      http.expectOne(`${url}/5/reuse`).flush({ url: 'https://manual.example/api', validUrl: false });

      const result = vi.fn();
      service.checkUrl('https://manual.example/api').subscribe(result);

      expect(checks()).toHaveLength(0);
      expect(result).toHaveBeenCalledWith({ url: 'https://manual.example/api', validUrl: false });
    });
  });

  it('keeps the caches when setting a URL fails', () => {
    service.getById(5).subscribe();
    http.expectOne(`${url}/5`).flush(record);

    service.updateUrl(5, 'reuse', null).subscribe({ error: () => undefined });
    const request = http.expectOne(`${url}/5/reuse`);
    expect(request.request.body).toEqual({ url: null });
    request.flush({}, { status: 504, statusText: 'Gateway Timeout' });

    expect(clearCache).not.toHaveBeenCalled();
    service.getById(5).subscribe();
    http.expectNone(`${url}/5`);
  });
});
