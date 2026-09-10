import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { EMPTY, firstValueFrom, Observable, of, throwError } from 'rxjs';
import { MAINTENANCES_ROUTES } from '../../../maintenances.routes';
import { MAINTENANCES_ROUTES_LOC } from '../../../maintenances.routes.i18n';
import {
  ClassificationSegmentsService,
  ComplianceSituationsService,
} from '../../services/accessibility-resource.services';
import {
  ACCESSIBILITY_INITIAL_PARAMS,
  ACCESSIBILITY_MAINTENANCE_RESOLVE_KEY,
  AccessibilityMaintenanceResolvedData,
  accessibilityMaintenanceResolver,
} from './accessibility-maintenance.resolver';

describe('accessibility maintenance resolver', () => {
  const classification = { getAll: vi.fn() };
  const compliance = { getAll: vi.fn() };
  beforeEach(() => {
    classification.getAll.mockReset().mockReturnValue(of({ content: [], totalElements: 0 }));
    compliance.getAll.mockReset().mockReturnValue(of({ content: [], totalElements: 0 }));
    TestBed.configureTestingModule({
      providers: [
        { provide: ClassificationSegmentsService, useValue: classification },
        { provide: ComplianceSituationsService, useValue: compliance },
      ],
    });
  });

  it('registers accessibility between development and security with the initial resolver', () => {
    const routes = MAINTENANCES_ROUTES[0].children!;
    const index = routes.findIndex((route) => route.path === MAINTENANCES_ROUTES_LOC.ACCESSIBILITY);
    expect(routes[index - 1].path).toBe(MAINTENANCES_ROUTES_LOC.DEVELOPMENT);
    expect(routes[index + 1].path).toBe(MAINTENANCES_ROUTES_LOC.SECURITY);
    expect(routes[index].resolve?.[ACCESSIBILITY_MAINTENANCE_RESOLVE_KEY]).toBe(
      accessibilityMaintenanceResolver,
    );
  });

  it('loads both pages with the active initial criteria', async () => {
    const data = await resolve();
    expect(Object.keys(data.resources)).toHaveLength(2);
    for (const service of [classification, compliance])
      expect(service.getAll).toHaveBeenCalledExactlyOnceWith(ACCESSIBILITY_INITIAL_PARAMS);
  });

  it('preserves successful results and identifies permission failures', async () => {
    compliance.getAll.mockReturnValue(throwError(() => new HttpErrorResponse({ status: 403 })));
    const data = await resolve();
    expect(data.resources['classification-segment'].loadFailed).toBe(false);
    expect(data.resources['compliance-situation']).toEqual({
      page: null,
      loadFailed: true,
      forbidden: true,
    });
  });

  it('returns a value even when a source completes without emitting', async () => {
    classification.getAll.mockReturnValue(EMPTY);
    expect((await resolve()).resources['classification-segment']).toEqual({
      page: null,
      loadFailed: true,
      forbidden: false,
    });
  });

  function resolve() {
    return firstValueFrom(
      TestBed.runInInjectionContext(
        () =>
          accessibilityMaintenanceResolver(
            {} as ActivatedRouteSnapshot,
            {} as RouterStateSnapshot,
          ) as Observable<AccessibilityMaintenanceResolvedData>,
      ),
    );
  }
});
