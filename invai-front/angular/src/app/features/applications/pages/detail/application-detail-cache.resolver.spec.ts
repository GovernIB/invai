import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, RouterStateSnapshot, convertToParamMap } from '@angular/router';
import {
  ClassificationSegmentsService,
  ComplianceSituationsService,
} from '@features/maintenances/accessibility/services/accessibility-resource.services';
import { Observable, firstValueFrom, of } from 'rxjs';
import { ApplicationAccessibilityInput } from '../../applications.model';
import { ApplicationAccessibilityService } from '../../services/application-accessibility.service';
import { ApplicationsService } from '../../services/applications.service';
import { ApplicationDetailResolvedData, applicationDetailResolver } from './application-detail.resolver';
import {
  ApplicationAccessibilityResolvedData,
  applicationAccessibilityResolver,
  loadApplicationAccessibility,
} from './sections/accessibility/application-accessibility-section.resolver';

describe('application detail and accessibility resolver caches', () => {
  let http: HttpTestingController;
  const applicationUrl = '/invaiapi/interna/application/7';
  const accessibilityUrl = '/invaiapi/interna/application/accessibility';
  const application = { id: 7, appAccessibilityId: 99 };
  const record = { id: 99, application: { id: 7 }, deletedAt: null };

  beforeEach(() => {
    const catalog = { getAll: () => of({ content: [], totalPages: 1 }) };
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: ClassificationSegmentsService, useValue: catalog },
        { provide: ComplianceSituationsService, useValue: catalog },
      ],
    });
    http = TestBed.inject(HttpTestingController);
    vi.useFakeTimers();
    vi.setSystemTime(0);
  });

  afterEach(() => {
    vi.useRealTimers();
    http.verify();
  });

  function resolveDetail() {
    return firstValueFrom(TestBed.runInInjectionContext(() => applicationDetailResolver(
      { paramMap: convertToParamMap({ id: '7' }) } as ActivatedRouteSnapshot,
      {} as RouterStateSnapshot,
    )) as Observable<ApplicationDetailResolvedData>);
  }

  function resolveAccessibility() {
    return firstValueFrom(TestBed.runInInjectionContext(() => applicationAccessibilityResolver(
      {
        parent: {
          paramMap: convertToParamMap({ id: '7' }),
          // The child must consult the service, not this older parent snapshot.
          data: { applicationDetail: { application: { id: 7, appAccessibilityId: 88 } } },
        },
      } as unknown as ActivatedRouteSnapshot,
      {} as RouterStateSnapshot,
    )) as Observable<ApplicationAccessibilityResolvedData>);
  }

  async function loadInitial() {
    const detail = resolveDetail();
    http.expectOne(applicationUrl).flush(application);
    await detail;
    const accessibility = resolveAccessibility();
    http.expectNone(applicationUrl);
    http.expectOne(`${accessibilityUrl}/99`).flush(record);
    return accessibility;
  }

  it('reuses both responses when reentering the application and the tab', async () => {
    expect((await loadInitial()).accessibility.status).toBe('loaded');
    const detail = resolveDetail();
    const accessibility = resolveAccessibility();
    http.expectNone((request) => request.method === 'GET');
    expect((await detail).application).toEqual(application);
    expect((await accessibility).accessibility.record).toEqual(record);
  });

  it('refreshes expired detail on tab entry and follows its current relation ID', async () => {
    await loadInitial();
    vi.advanceTimersByTime(300000);
    const next = resolveAccessibility();
    http.expectOne(applicationUrl).flush({ ...application, appAccessibilityId: 100 });
    const updated = { ...record, id: 100 };
    http.expectOne(`${accessibilityUrl}/100`).flush(updated);
    expect((await next).accessibility.record).toEqual(updated);
  });

  it('reuses an explicit absent relation and checks again after expiry', async () => {
    const first = resolveAccessibility();
    http.expectOne(applicationUrl).flush({ ...application, appAccessibilityId: null });
    expect((await first).accessibility.status).toBe('absent');
    const second = resolveAccessibility();
    http.expectNone((request) => request.method === 'GET');
    expect((await second).accessibility.status).toBe('absent');
    vi.advanceTimersByTime(300000);
    const third = resolveAccessibility();
    http.expectOne(applicationUrl).flush(application);
    http.expectOne(`${accessibilityUrl}/99`).flush(record);
    expect((await third).accessibility.status).toBe('loaded');
  });

  it('forces both requests on explicit retry even with populated caches', async () => {
    await loadInitial();
    const result = firstValueFrom(loadApplicationAccessibility(
      7,
      TestBed.inject(ApplicationsService),
      TestBed.inject(ApplicationAccessibilityService),
      true,
    ));
    http.expectOne(applicationUrl).flush(application);
    const updated = { ...record, observations: 'fresh' };
    http.expectOne(`${accessibilityUrl}/99`).flush(updated);
    expect((await result).record).toEqual(updated);
    const next = resolveAccessibility();
    http.expectNone((request) => request.method === 'GET');
    expect((await next).accessibility.record).toEqual(updated);
  });

  it('allows a repeated tab entry to retry a failed record load', async () => {
    const first = resolveAccessibility();
    http.expectOne(applicationUrl).flush(application);
    http.expectOne(`${accessibilityUrl}/99`).flush('failed', { status: 403, statusText: 'Forbidden' });
    expect((await first).accessibility.status).toBe('forbidden');
    const next = resolveAccessibility();
    http.expectNone(applicationUrl);
    http.expectOne(`${accessibilityUrl}/99`).flush(record);
    expect((await next).accessibility.status).toBe('loaded');
  });

  it('allows repeated detail entry to retry a failed application load', async () => {
    const first = resolveDetail();
    http.expectOne(applicationUrl).flush('failed', { status: 500, statusText: 'Error' });
    expect((await first).loadFailed).toBe(true);
    const next = resolveDetail();
    http.expectOne(applicationUrl).flush(application);
    expect((await next).loadFailed).toBe(false);
  });

  it.each(['create', 'update'] as const)('refreshes both caches after accessibility %s', async (operation) => {
    await loadInitial();
    const service = TestBed.inject(ApplicationAccessibilityService);
    const payload: ApplicationAccessibilityInput = {
      applicationId: 7,
      complianceId: null,
      classificationSegmentId: null,
      publicUrl: null,
      mobileApplication: false,
      mobileApplicationName: null,
      expireDate: null,
      nonAccessibleContent: null,
      observations: 'updated',
    };
    const mutation = operation === 'create' ? service.create(payload) : service.update(99, payload);
    mutation.subscribe();
    http.expectOne(operation === 'create' ? accessibilityUrl : `${accessibilityUrl}/99`)
      .flush({ ...record, observations: 'updated' });
    const next = resolveAccessibility();
    http.expectOne(applicationUrl).flush(application);
    http.expectOne(`${accessibilityUrl}/99`).flush({ ...record, observations: 'updated' });
    expect((await next).accessibility.record?.observations).toBe('updated');
  });
});
