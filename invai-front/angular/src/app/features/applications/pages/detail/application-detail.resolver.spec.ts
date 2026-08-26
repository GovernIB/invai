import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, RouterStateSnapshot, convertToParamMap } from '@angular/router';
import { Observable, firstValueFrom, of, throwError } from 'rxjs';

import { APPLICATIONS_ROUTES } from '../../applications.routes';
import { ApplicationOutput } from '../../applications.model';
import { ApplicationsService } from '../../services/applications.service';
import {
  APPLICATION_DETAIL_RESOLVE_KEY,
  ApplicationDetailResolvedData,
  applicationDetailResolver,
} from './application-detail.resolver';

const APPLICATION: ApplicationOutput = {
  id: 7,
  code: 'APP-7',
  prefix: 'INV',
  name: 'Invai',
  category: null,
  systemType: null,
  field: null,
  admUnit: null,
  csCommission: null,
  description: null,
  status: null,
  expirationDate: null,
  createdAt: null,
  createdBy: null,
  updatedAt: null,
  updatedBy: null,
  loadUser: null,
  loadDate: null,
  appInformationSystemDbId: null,
  appDevelopmentId: null,
  appResponsibleAuthorizedId: null,
};

describe('applicationDetailResolver', () => {
  let getById: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    getById = vi.fn(() => of(APPLICATION));
    TestBed.configureTestingModule({
      providers: [{ provide: ApplicationsService, useValue: { getById } }],
    });
  });

  it('should resolve the application identified by the route', async () => {
    const result = await resolveDetail('7');

    expect(getById).toHaveBeenCalledWith(7);
    expect(result).toEqual({ application: APPLICATION, loadFailed: false });
  });

  it.each(['', 'invalid', '0', '-1'])(
    'should reject an invalid application id without requesting it (%s)',
    async (id) => {
      await expect(resolveDetail(id)).resolves.toEqual({
        application: null,
        loadFailed: true,
      });
      expect(getById).not.toHaveBeenCalled();
    },
  );

  it('should complete the route with a degraded state when loading fails', async () => {
    getById.mockReturnValueOnce(throwError(() => new Error('Application unavailable')));

    await expect(resolveDetail('7')).resolves.toEqual({
      application: null,
      loadFailed: true,
    });
  });

  it('should be registered on the application detail route', () => {
    const route = APPLICATIONS_ROUTES.find(({ path }) => path === ':id');

    expect(route?.resolve?.[APPLICATION_DETAIL_RESOLVE_KEY]).toBe(applicationDetailResolver);
  });

  function resolveDetail(id: string): Promise<ApplicationDetailResolvedData> {
    const route = {
      paramMap: convertToParamMap({ id }),
    } as ActivatedRouteSnapshot;
    const result = TestBed.runInInjectionContext(() =>
      applicationDetailResolver(route, {} as RouterStateSnapshot),
    );

    return firstValueFrom(result as Observable<ApplicationDetailResolvedData>);
  }
});
