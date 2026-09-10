import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';
import { firstValueFrom, Observable, of, throwError } from 'rxjs';

import {
  EnsRequirementsService,
  IdentityProvidersService,
  PersonalDataProcessingService,
  SecurityMeasureTypesService,
  WebContextsService,
} from '../../services/security-resource.services';
import {
  SecurityMaintenanceResolvedData,
  securityMaintenanceResolver,
} from './security-maintenance.resolver';

describe('securityMaintenanceResolver', () => {
  const services = {
    ens: { getAll: vi.fn(() => of(page([]))) },
    identity: { getAll: vi.fn(() => of(page([]))) },
    processing: { getAll: vi.fn(() => of(page([]))) },
    measure: { getAll: vi.fn(() => of(page([]))) },
    web: { getAll: vi.fn(() => of(page([]))) },
  };

  beforeEach(() => {
    vi.clearAllMocks();
    Object.values(services).forEach((service) => service.getAll.mockReturnValue(of(page([]))));
    TestBed.configureTestingModule({
      providers: [
        { provide: EnsRequirementsService, useValue: services.ens },
        { provide: IdentityProvidersService, useValue: services.identity },
        { provide: PersonalDataProcessingService, useValue: services.processing },
        { provide: SecurityMeasureTypesService, useValue: services.measure },
        { provide: WebContextsService, useValue: services.web },
      ],
    });
  });

  it('loads the five active maintenance pages with the same initial contract', async () => {
    const result = await resolve();
    const params = {
      page: 0,
      size: 10,
      sort: 'id,asc',
      statusId: SoftDeleteStatus.ACTIVE,
    };

    Object.values(services).forEach((service) => expect(service.getAll).toHaveBeenCalledWith(params));
    expect(Object.keys(result.resources)).toHaveLength(5);
  });

  it('degrades only the resource whose initial request fails', async () => {
    services.measure.getAll.mockReturnValue(throwError(() => new Error('Unavailable')));

    const result = await resolve();

    expect(result.resources['security-measure-type']).toEqual({ page: null, loadFailed: true });
    expect(result.resources['web-context'].loadFailed).toBe(false);
  });

  function resolve(): Promise<SecurityMaintenanceResolvedData> {
    return firstValueFrom(
      TestBed.runInInjectionContext(
        () =>
          securityMaintenanceResolver(
            {} as ActivatedRouteSnapshot,
            {} as RouterStateSnapshot,
          ) as Observable<SecurityMaintenanceResolvedData>,
      ),
    );
  }
});

function page<T>(content: T[]) {
  return { content, totalElements: content.length };
}
