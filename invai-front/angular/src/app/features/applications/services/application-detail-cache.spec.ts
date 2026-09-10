import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Observable } from 'rxjs';
import { ApplicationAccessibilityService } from './application-accessibility.service';
import { ApplicationsService } from './applications.service';

describe.each([
  { name: 'application', url: '/invaiapi/interna/application', service: ApplicationsService },
  {
    name: 'accessibility',
    url: '/invaiapi/interna/application/accessibility',
    service: ApplicationAccessibilityService,
  },
])('$name detail cache', ({ url, service: serviceType }) => {
  let http: HttpTestingController;
  let service: {
    getById(id: number): Observable<unknown>;
    refreshById(id: number): Observable<unknown>;
    clearCache(): void;
  };
  const record = { id: 7, application: { id: 1 } };

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = serviceType === ApplicationsService
      ? TestBed.inject(ApplicationsService)
      : TestBed.inject(ApplicationAccessibilityService);
    http = TestBed.inject(HttpTestingController);
    vi.useFakeTimers();
    vi.setSystemTime(0);
  });

  afterEach(() => {
    vi.useRealTimers();
    http.verify();
  });

  it('shares simultaneous reads and reuses the response by ID', () => {
    const result = vi.fn();
    const request$ = service.getById(7);
    request$.subscribe(result);
    request$.subscribe(result);
    service.getById(7).subscribe(result);
    http.expectOne(`${url}/7`).flush(record);
    service.getById(8).subscribe();
    http.expectOne(`${url}/8`).flush({ ...record, id: 8 });
    service.getById(7).subscribe(result);
    http.expectNone(`${url}/7`);
    expect(result).toHaveBeenCalledTimes(4);
    expect(result.mock.calls).toEqual(Array.from({ length: 4 }, () => [record]));
  });

  it('expires five minutes after reception, without extending TTL on reads', () => {
    service.getById(7).subscribe();
    const pending = http.expectOne(`${url}/7`);
    vi.advanceTimersByTime(300000);
    service.getById(7).subscribe();
    http.expectNone(`${url}/7`);
    pending.flush(record);
    vi.advanceTimersByTime(299999);
    service.getById(7).subscribe();
    http.expectNone(`${url}/7`);
    vi.advanceTimersByTime(1);
    http.expectNone(`${url}/7`);
    service.getById(7).subscribe();
    http.expectOne(`${url}/7`).flush(record);
  });

  it('forces a fresh read for one ID and caches the new result with a new TTL', () => {
    service.getById(7).subscribe();
    http.expectOne(`${url}/7`).flush(record);
    service.getById(8).subscribe();
    http.expectOne(`${url}/8`).flush({ ...record, id: 8 });
    vi.advanceTimersByTime(200000);
    service.refreshById(7).subscribe();
    const updated = { ...record, observations: 'fresh' };
    http.expectOne(`${url}/7`).flush(updated);
    vi.advanceTimersByTime(100000);
    const result = vi.fn();
    service.getById(7).subscribe(result);
    http.expectNone(`${url}/7`);
    expect(result).toHaveBeenCalledWith(updated);
    // The other ID retains its original expiry.
    service.getById(8).subscribe();
    http.expectOne(`${url}/8`).flush({ ...record, id: 8 });
  });

  it('retries errors and retains other cached IDs', () => {
    service.getById(8).subscribe();
    http.expectOne(`${url}/8`).flush({ ...record, id: 8 });
    service.getById(7).subscribe({ error: () => undefined });
    http.expectOne(`${url}/7`).flush('failed', { status: 500, statusText: 'Error' });
    service.getById(8).subscribe();
    http.expectNone(`${url}/8`);
    service.getById(7).subscribe();
    http.expectOne(`${url}/7`).flush(record);
  });

  it.each(['success', 'error'])('keeps a fresh entry after an older request ends with %s', (outcome) => {
    service.getById(7).subscribe({ error: () => undefined });
    const old = http.expectOne(`${url}/7`);
    service.refreshById(7).subscribe();
    const fresh = { ...record, observations: 'fresh' };
    http.expectOne(`${url}/7`).flush(fresh);
    if (outcome === 'success') old.flush(record);
    else old.flush('failed', { status: 500, statusText: 'Error' });
    const result = vi.fn();
    service.getById(7).subscribe(result);
    http.expectNone(`${url}/7`);
    expect(result).toHaveBeenCalledWith(fresh);
  });

  it('invalidates pending entries without restoring them when they finish', () => {
    service.getById(7).subscribe();
    const old = http.expectOne(`${url}/7`);
    service.clearCache();
    old.flush(record);
    service.getById(7).subscribe();
    http.expectOne(`${url}/7`).flush(record);
  });
});
