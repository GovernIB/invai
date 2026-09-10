import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ApplicationAccessibilityInput } from '../applications.model';
import { ApplicationsService } from './applications.service';
import { ApplicationAccessibilityService } from './application-accessibility.service';

describe('ApplicationAccessibilityService', () => {
  let service: ApplicationAccessibilityService;
  let http: HttpTestingController;
  const clearCache = vi.fn();
  const url = '/invaiapi/interna/application/accessibility';
  const payload: ApplicationAccessibilityInput = {
    applicationId: 7,
    complianceId: 29,
    classificationSegmentId: 17,
    publicUrl: 'https://example.com',
    mobileApplication: false,
    mobileApplicationName: null,
    expireDate: '2026-09-09T00:00:00',
    nonAccessibleContent: null,
    observations: 'Revisión',
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
    service = TestBed.inject(ApplicationAccessibilityService);
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());

  it('reads by relation ID without caching nullable detail responses', () => {
    const result = vi.fn();
    service.getById(99).subscribe(result);
    http.expectOne(`${url}/99`).flush(null);
    expect(result).toHaveBeenCalledWith(null);
    service.getById(99).subscribe(result);
    http.expectOne(`${url}/99`).flush({ id: 99, application: { id: 7 } });
    expect(clearCache).not.toHaveBeenCalled();
  });

  it('does not let an older null response evict a refreshed record', () => {
    service.getById(99).subscribe();
    const old = http.expectOne(`${url}/99`);
    service.refreshById(99).subscribe();
    const record = { id: 99, application: { id: 7 } };
    http.expectOne(`${url}/99`).flush(record);
    old.flush(null);
    const result = vi.fn();
    service.getById(99).subscribe(result);
    http.expectNone(`${url}/99`);
    expect(result).toHaveBeenCalledWith(record);
  });

  it.each(['create', 'update'] as const)(
    'invalidates application caches only after a successful %s',
    (operation) => {
      const record = { id: 99, application: { id: 7 } };
      service.getById(99).subscribe();
      http.expectOne(`${url}/99`).flush(record);
      const requestUrl = operation === 'create' ? url : `${url}/99`;
      const send = () =>
        operation === 'create' ? service.create(payload) : service.update(99, payload);
      send().subscribe({ error: () => undefined });
      const failed = http.expectOne(requestUrl);
      expect(failed.request.method).toBe(operation === 'create' ? 'POST' : 'PUT');
      expect(failed.request.body).toEqual(payload);
      failed.flush({}, { status: 403, statusText: 'Forbidden' });
      expect(clearCache).not.toHaveBeenCalled();
      const cached = vi.fn();
      service.getById(99).subscribe(cached);
      http.expectNone(`${url}/99`);
      expect(cached).toHaveBeenCalledWith(record);
      send().subscribe();
      expect(clearCache).not.toHaveBeenCalled();
      const pending = http.expectOne(requestUrl);
      service.getById(99).subscribe();
      http.expectNone((request) => request.method === 'GET');
      pending.flush(record);
      expect(clearCache).toHaveBeenCalledOnce();
      service.getById(99).subscribe();
      http.expectOne(`${url}/99`).flush(record);
    },
  );
});
