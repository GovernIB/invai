import { ApplicationAccessibilityService } from '../../../../services/application-accessibility.service';
import { ApplicationsService } from '../../../../services/applications.service';
import { ApplicationOutput } from '../../../../applications.model';
import { APPLICATION_DETAIL_RESOLVE_KEY } from '../../application-detail.resolver';
import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { AccessibilityResource } from '@features/maintenances/accessibility/accessibility.model';
import {
  ClassificationSegmentsService,
  ComplianceSituationsService,
} from '@features/maintenances/accessibility/services/accessibility-resource.services';
import { SpringPage } from '@models/page.model';
import { EMPTY, firstValueFrom, Observable, of, throwError } from 'rxjs';
import { APPLICATIONS_ROUTES } from '../../../../applications.routes';
import {
  APPLICATION_ACCESSIBILITY_RESOLVE_KEY,
  ApplicationAccessibilityResolvedData,
  applicationAccessibilityResolver,
  loadApplicationAccessibility,
} from './application-accessibility-section.resolver';

describe('application accessibility catalog resolver', () => {
  const accessibility = { getById: vi.fn(), refreshById: vi.fn() };
  const applications = { getById: vi.fn(), refreshById: vi.fn() };
  const classification = { getAll: vi.fn() };
  const compliance = { getAll: vi.fn() };
  const row = (id: number): AccessibilityResource => ({
    id,
    name: `Segment ${id}`,
    nameEs: `Segmento ${id}`,
    deletedAt: null,
  });
  const page = (items: AccessibilityResource[], totalPages = 1) =>
    ({ content: items, totalPages }) as SpringPage<AccessibilityResource>;

  beforeEach(() => {
    accessibility.getById
      .mockReset()
      .mockReturnValue(of({ id: 99, application: { id: 7 }, deletedAt: null }));
    applications.refreshById.mockReset().mockReturnValue(of({ id: 7, appAccessibilityId: null }));
    applications.getById.mockReset().mockReturnValue(of({ id: 7, appAccessibilityId: 99 }));
    accessibility.refreshById.mockReset();
    classification.getAll.mockReset().mockReturnValue(of(page([])));
    compliance.getAll.mockReset().mockReturnValue(of(page([])));
    TestBed.configureTestingModule({
      providers: [
        { provide: ApplicationAccessibilityService, useValue: accessibility },
        { provide: ApplicationsService, useValue: applications },
        { provide: ClassificationSegmentsService, useValue: classification },
        { provide: ComplianceSituationsService, useValue: compliance },
      ],
    });
  });

  it('is registered only on the accessibility child route', () => {
    const route = APPLICATIONS_ROUTES.find((route) => route.path === ':id')?.children?.find(
      (route) => route.path === 'accessibility',
    );
    expect(route?.resolve?.[APPLICATION_ACCESSIBILITY_RESOLVE_KEY]).toBe(
      applicationAccessibilityResolver,
    );
  });

  it('loads all active catalog pages without truncating the options', async () => {
    classification.getAll.mockImplementation(({ page: index }) => of(page([row(index + 1)], 3)));
    const data = await resolve();
    expect(data.classification.items.map((item) => item.id)).toEqual([1, 2, 3]);
    expect(classification.getAll.mock.calls.map(([params]) => params)).toEqual(
      [0, 1, 2].map((page) => ({ page, size: 100, sort: 'id,asc', statusId: 1 })),
    );
    expect(compliance.getAll).toHaveBeenCalledOnce();
  });

  it('distinguishes permission failure from a successfully empty catalog', async () => {
    classification.getAll.mockReturnValue(throwError(() => new HttpErrorResponse({ status: 403 })));
    const data = await resolve();
    expect(data.classification).toEqual({ items: [], failed: true, forbidden: true });
    expect(data.compliance).toEqual({ items: [], failed: false, forbidden: false });
  });

  it('does not expose an incomplete catalog when a later page fails', async () => {
    classification.getAll
      .mockReturnValueOnce(of(page([row(1)], 2)))
      .mockReturnValueOnce(throwError(() => new Error('Unavailable')));
    expect((await resolve()).classification).toEqual({ items: [], failed: true, forbidden: false });
  });

  it('always resolves a degraded value when a source completes without data', async () => {
    compliance.getAll.mockReturnValue(EMPTY);
    expect((await resolve()).compliance.failed).toBe(true);
  });

  it('loads the relation ID, never the application ID', async () => {
    const data = await resolve();
    expect(accessibility.getById).toHaveBeenCalledWith(99);
    expect(applications.getById).toHaveBeenCalledWith(7);
    expect(applications.refreshById).not.toHaveBeenCalled();
    expect(data.accessibility.status).toBe('loaded');
  });

  it('uses the current cached detail and only accepts explicit null as absence', async () => {
    applications.getById.mockReturnValue(of({ id: 7, appAccessibilityId: null }));
    expect((await resolve()).accessibility.status).toBe('absent');
    expect(applications.getById).toHaveBeenCalledWith(7);
    expect(applications.refreshById).not.toHaveBeenCalled();
    expect(accessibility.getById).not.toHaveBeenCalled();
    applications.getById.mockReturnValue(of({ id: 7 }));
    expect((await resolve()).accessibility.status).toBe('unavailable');
  });

  it('forces both reads when retrying accessibility', async () => {
    applications.refreshById.mockReturnValue(of({ id: 7, appAccessibilityId: 100 }));
    accessibility.refreshById.mockReturnValue(of({ id: 100, application: { id: 7 } }));
    const result = await firstValueFrom(loadApplicationAccessibility(
      7,
      TestBed.inject(ApplicationsService),
      TestBed.inject(ApplicationAccessibilityService),
      true,
    ));
    expect(result.status).toBe('loaded');
    expect(applications.refreshById).toHaveBeenCalledWith(7);
    expect(accessibility.refreshById).toHaveBeenCalledWith(100);
    expect(applications.getById).not.toHaveBeenCalled();
    expect(accessibility.getById).not.toHaveBeenCalled();
  });

  it.each([null, { id: 99, application: { id: 8 } }, { id: 100, application: { id: 7 } }])(
    'rejects missing or mismatched records',
    async (record) => {
      accessibility.getById.mockReturnValue(of(record));
      expect((await resolve()).accessibility.status).toBe('failed');
    },
  );

  it('distinguishes deleted records and forbidden requests', async () => {
    accessibility.getById.mockReturnValue(
      of({ id: 99, application: { id: 7 }, deletedAt: '2026-01-01' }),
    );
    expect((await resolve()).accessibility.status).toBe('deleted');
    accessibility.getById.mockReturnValue(throwError(() => new HttpErrorResponse({ status: 403 })));
    expect((await resolve()).accessibility.status).toBe('forbidden');
    accessibility.getById.mockReturnValue(EMPTY);
    expect((await resolve()).accessibility.status).toBe('failed');
  });

  function resolve() {
    return firstValueFrom(
      TestBed.runInInjectionContext(
        () =>
          applicationAccessibilityResolver(
            {
              parent: {
                paramMap: { get: () => '7' },
                data: {
                  [APPLICATION_DETAIL_RESOLVE_KEY]: {
                    application: { id: 7, appAccessibilityId: 88 } as ApplicationOutput,
                  },
                },
              },
            } as unknown as ActivatedRouteSnapshot,
            {} as RouterStateSnapshot,
          ) as Observable<ApplicationAccessibilityResolvedData>,
      ),
    );
  }
});
