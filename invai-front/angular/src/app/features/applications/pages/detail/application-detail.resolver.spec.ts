import { ApplicationResponsiblesService } from '../../services/application-responsibles.service';
import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, RouterStateSnapshot, convertToParamMap } from '@angular/router';
import { EMPTY, Observable, firstValueFrom, of, throwError } from 'rxjs';
import { APPLICATIONS_ROUTES } from '../../applications.routes';
import { ApplicationOutput } from '../../applications.model';
import {
  ApplicationEnsClassificationsService,
  ApplicationWebContextsService,
} from '../../services/application-security.service';
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
  const getContexts = vi.fn();
  const getResponsibles = vi.fn();

  beforeEach(() => {
    getById.mockReset().mockReturnValue(of(APPLICATION));
    getClassifications.mockReset();
    getResponsibles.mockReset().mockReturnValue(of({ content: [], totalElements: 0 }));
    getContexts.mockReset().mockReturnValue(of({ content: [], totalElements: 0 }));
    TestBed.configureTestingModule({
      providers: [
        { provide: ApplicationResponsiblesService, useValue: { getPage: getResponsibles } },
        { provide: ApplicationWebContextsService, useValue: { getPage: getContexts } },
        { provide: ApplicationsService, useValue: { getById } },
        {
          provide: ApplicationEnsClassificationsService,
          useValue: { getPage: getClassifications },
        },
      ],
    });
  });

  it('resolves backend flags without loading ENS classifications', async () => {
    const response = {
      ...APPLICATION,
      appSecurityId: 8,
      incomplete: true,
      missingSecurityData: true,
    };
    getById.mockReturnValue(of(response));
    await expect(resolveDetail('7')).resolves.toEqual({
      application: response,
      loadFailed: false,
      hasUnverifiedWebContexts: false,
      hasPendingResponsibleDir3: false,
    });
    expect(getById).toHaveBeenCalledExactlyOnceWith(7);
    expect(getClassifications).not.toHaveBeenCalled();
  });

  it.each(['', 'invalid', '0', '-1'])('rejects an invalid id (%s)', async (id) => {
    await expect(resolveDetail(id)).resolves.toEqual({
      application: null,
      loadFailed: true,
      hasUnverifiedWebContexts: null,
      hasPendingResponsibleDir3: null,
    });
    expect(getById).not.toHaveBeenCalled();
  });

  it('returns a degraded route when the application load fails', async () => {
    getById.mockReturnValue(throwError(() => new Error('Unavailable')));
    await expect(resolveDetail('7')).resolves.toEqual({
      application: null,
      loadFailed: true,
      hasUnverifiedWebContexts: null,
      hasPendingResponsibleDir3: null,
    });
  });

  it('checks contexts on detail entry using the shared first page and its total, independently of completeness flags', async () => {
    getById.mockReturnValue(of({ ...APPLICATION, appSecurityId: 8 }));
    getContexts.mockReturnValue(of({ content: [], totalElements: 25 }));
    const result = await resolveDetail('7');
    expect(result.hasUnverifiedWebContexts).toBe(true);
    expect(result.application?.missingSecurityData).toBe(false);
    expect(getContexts).toHaveBeenCalledExactlyOnceWith({
      appSecurityId: 8,
      page: 0,
      size: 10,
      sort: 'id,asc',
      statusId: 1,
    });
  });

  it('keeps the application available when the context list fails', async () => {
    getById.mockReturnValue(of({ ...APPLICATION, appSecurityId: 8 }));
    getContexts.mockReturnValue(throwError(() => new Error('Unavailable')));
    const result = await resolveDetail('7');
    expect(result.loadFailed).toBe(false);
    expect(result.application?.id).toBe(7);
    expect(result.hasUnverifiedWebContexts).toBeNull();
  });

  it('does not request contexts when no security anchor exists', async () => {
    const result = await resolveDetail('7');
    expect(result.hasUnverifiedWebContexts).toBe(false);
    expect(getContexts).not.toHaveBeenCalled();
  });

  it.each([
    ['NOT_VALIDATED', true, null, true],
    ['MANUAL', true, null, false],
    ['VALIDATED', true, null, false],
    ['NOT_APPLY', true, null, false],
    ['NOT_VALIDATED', false, null, false],
    ['NOT_VALIDATED', true, '2026-09-22', false],
    [null, true, null, false],
  ])(
    'counts only an active pending CAIB responsible (%s, %s, %s)',
    async (dir3Status, personalCaib, deletedAt, expected) => {
      getById.mockReturnValue(of({ ...APPLICATION, appResponsibleAuthorizedId: 91 }));
      getResponsibles.mockReturnValue(
        of({
          content: [
            { person: null, deletedAt: null },
            {
              person: { personalCaib: true },
              deletedAt: null,
              dir3Validation: { dir3Status: 'MANUAL' },
            },
            { person: { personalCaib }, deletedAt, dir3Validation: { dir3Status } },
          ],
        }),
      );
      const result = await resolveDetail('7');
      expect(result.hasPendingResponsibleDir3).toBe(expected);
      expect(result.application?.missingResponsibleTypes).toBe(false);
      expect(getResponsibles).toHaveBeenCalledExactlyOnceWith({
        appResponsibleAuthorizedId: 91,
        page: 0,
        size: 1000,
        sort: 'id,asc',
        statusId: 1,
      });
    },
  );

  it.each([EMPTY, throwError(() => new Error('Unavailable'))])(
    'keeps the application available when pending DIR3 cannot be loaded',
    async (source) => {
      getById.mockReturnValue(of({ ...APPLICATION, appResponsibleAuthorizedId: 91 }));
      getResponsibles.mockReturnValue(source);
      const result = await resolveDetail('7');
      expect(result.loadFailed).toBe(false);
      expect(result.application?.id).toBe(7);
      expect(result.hasPendingResponsibleDir3).toBeNull();
    },
  );

  it('does not request responsibles without their anchor', async () => {
    expect((await resolveDetail('7')).hasPendingResponsibleDir3).toBe(false);
    expect(getResponsibles).not.toHaveBeenCalled();
  });

  it('is registered on the detail route', () => {
    expect(
      APPLICATIONS_ROUTES.find(({ path }) => path === ':id')?.resolve?.[
        APPLICATION_DETAIL_RESOLVE_KEY
      ],
    ).toBe(applicationDetailResolver);
  });

  function resolveDetail(id: string): Promise<ApplicationDetailResolvedData> {
    const result = TestBed.runInInjectionContext(() =>
      applicationDetailResolver(
        { paramMap: convertToParamMap({ id }) } as ActivatedRouteSnapshot,
        {} as RouterStateSnapshot,
      ),
    );
    return firstValueFrom(result as Observable<ApplicationDetailResolvedData>);
  }
});
