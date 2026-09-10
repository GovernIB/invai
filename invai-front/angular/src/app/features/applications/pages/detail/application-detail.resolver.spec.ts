import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, RouterStateSnapshot, convertToParamMap } from '@angular/router';
import { Observable, firstValueFrom, of, throwError } from 'rxjs';
import { APPLICATIONS_ROUTES } from '../../applications.routes';
import { ApplicationOutput } from '../../applications.model';
import { ApplicationEnsClassificationsService } from '../../services/application-security.service';
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
  department: null,
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
  incomplete: false,
  missingResponsibleTypes: false,
  missingAuthorized: false,
  missingDevelopmentFields: false,
  missingSystems: false,
  missingDatabases: false,
  missingAccessibilityFields: false,
  missingSecurityData: false,
};

describe('applicationDetailResolver', () => {
  const getById = vi.fn();
  const getClassifications = vi.fn();

  beforeEach(() => {
    getById.mockReset().mockReturnValue(of(APPLICATION));
    getClassifications.mockReset();
    TestBed.configureTestingModule({
      providers: [
        { provide: ApplicationsService, useValue: { getById } },
        { provide: ApplicationEnsClassificationsService, useValue: { getPage: getClassifications } },
      ],
    });
  });

  it('resolves backend flags without loading ENS classifications', async () => {
    const response = { ...APPLICATION, appSecurityId: 8, incomplete: true, missingSecurityData: true };
    getById.mockReturnValue(of(response));
    await expect(resolveDetail('7')).resolves.toEqual({ application: response, loadFailed: false });
    expect(getById).toHaveBeenCalledExactlyOnceWith(7);
    expect(getClassifications).not.toHaveBeenCalled();
  });

  it.each(['', 'invalid', '0', '-1'])('rejects an invalid id (%s)', async (id) => {
    await expect(resolveDetail(id)).resolves.toEqual({ application: null, loadFailed: true });
    expect(getById).not.toHaveBeenCalled();
  });

  it('returns a degraded route when the application load fails', async () => {
    getById.mockReturnValue(throwError(() => new Error('Unavailable')));
    await expect(resolveDetail('7')).resolves.toEqual({ application: null, loadFailed: true });
  });

  it('is registered on the detail route', () => {
    expect(APPLICATIONS_ROUTES.find(({ path }) => path === ':id')?.resolve?.[APPLICATION_DETAIL_RESOLVE_KEY])
      .toBe(applicationDetailResolver);
  });

  function resolveDetail(id: string): Promise<ApplicationDetailResolvedData> {
    const result = TestBed.runInInjectionContext(() => applicationDetailResolver(
      { paramMap: convertToParamMap({ id }) } as ActivatedRouteSnapshot,
      {} as RouterStateSnapshot,
    ));
    return firstValueFrom(result as Observable<ApplicationDetailResolvedData>);
  }
});
