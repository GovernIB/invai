import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { Observable, firstValueFrom, of, throwError } from 'rxjs';

import { APPLICATIONS_ROUTES } from '../../../../applications.routes';
import { ApplicationOutput } from '../../../../applications.model';
import { ApplicationAuthorizedService } from '../../../../services/application-authorized.service';
import { ApplicationResponsiblesService } from '../../../../services/application-responsibles.service';
import { ResponsibleCompaniesService } from '../../../../../maintenances/responsibles/services/responsible-companies.service';
import { ResponsibleAuthorizationTypesService } from '../../../../../maintenances/responsibles/services/responsible-authorization-types.service';
import { ResponsiblePeopleService } from '../../../../../maintenances/responsibles/services/responsible-people.service';
import { ResponsibleTypesService } from '../../../../../maintenances/responsibles/services/responsible-types.service';
import { APPLICATION_DETAIL_RESOLVE_KEY } from '../../application-detail.resolver';
import {
  APPLICATION_RESPONSIBLE_RESOLVE_KEY,
  ApplicationResponsibleResolvedData,
  applicationResponsibleResolver,
} from './application-responsible-section.resolver';

describe('applicationResponsibleResolver', () => {
  const getResponsibles = vi.fn(() => of(page([])));
  const getAuthorized = vi.fn(() => of(page([])));
  const getResponsibleTypes = vi.fn(() => of([]));
  const getCompanyOptions = vi.fn(() => of([]));
  const getPeople = vi.fn(() => of(page([])));
  const getAuthorizationTypes = vi.fn(() => of(page([])));

  beforeEach(() => {
    vi.clearAllMocks();
    getResponsibles.mockReturnValue(of(page([])));
    getAuthorized.mockReturnValue(of(page([])));
    getResponsibleTypes.mockReturnValue(of([]));
    getCompanyOptions.mockReturnValue(of([]));
    getPeople.mockReturnValue(of(page([])));
    getAuthorizationTypes.mockReturnValue(of(page([])));
    TestBed.configureTestingModule({
      providers: [
        { provide: ApplicationResponsiblesService, useValue: { getPage: getResponsibles } },
        { provide: ApplicationAuthorizedService, useValue: { getPage: getAuthorized } },
        { provide: ResponsibleTypesService, useValue: { getAll: getResponsibleTypes } },
        { provide: ResponsibleCompaniesService, useValue: { getOptions: getCompanyOptions } },
        { provide: ResponsiblePeopleService, useValue: { getPage: getPeople } },
        {
          provide: ResponsibleAuthorizationTypesService,
          useValue: { getPage: getAuthorizationTypes },
        },
      ],
    });
  });

  it('loads only the two active pages for a valid anchor', async () => {
    const result = await resolve(91);
    const expected = {
      appResponsibleAuthorizedId: 91,
      page: 0,
      size: 10,
      sort: 'id,asc',
      statusId: 1,
    };
    expect(getResponsibles).toHaveBeenCalledOnce();
    expect(getResponsibles).toHaveBeenCalledWith({ ...expected, size: 1000 });
    expect(getAuthorized).toHaveBeenCalledOnce();
    expect(getAuthorized).toHaveBeenCalledWith(expected);
    expect(getResponsibleTypes).toHaveBeenCalledOnce();
    expect(getCompanyOptions).toHaveBeenCalledWith(true);
    expect(getPeople).toHaveBeenCalledWith({
      page: 0,
      size: 1000,
      sort: ['firstName,asc', 'lastName,asc'],
      statusId: 1,
    });
    expect(getAuthorizationTypes).toHaveBeenCalledWith({
      page: 0,
      size: 1000,
      sort: 'name,asc',
      statusId: 1,
    });
    expect(result).toEqual(
      expect.objectContaining({
        anchorId: 91,
        available: true,
        responsiblesLoadFailed: false,
        authorizedLoadFailed: false,
      }),
    );
  });

  it('does not issue requests when the parent detail has no anchor', async () => {
    const result = await resolve(null);
    expect(result.available).toBe(false);
    expect(getResponsibles).not.toHaveBeenCalled();
    expect(getAuthorized).not.toHaveBeenCalled();
    expect(getResponsibleTypes).not.toHaveBeenCalled();
    expect(getCompanyOptions).not.toHaveBeenCalled();
    expect(getPeople).not.toHaveBeenCalled();
    expect(getAuthorizationTypes).not.toHaveBeenCalled();
  });

  it('degrades each failed page independently', async () => {
    getResponsibles.mockReturnValueOnce(throwError(() => new Error('Unavailable')));
    const result = await resolve(91);
    expect(result.responsiblesPage).toBeNull();
    expect(result.responsiblesLoadFailed).toBe(true);
    expect(result.authorizedLoadFailed).toBe(false);
  });

  it('degrades the authorization type catalog independently', async () => {
    getAuthorizationTypes.mockReturnValueOnce(throwError(() => new Error('Unavailable')));
    const result = await resolve(91);
    expect(result.authorizationTypes).toEqual([]);
    expect(result.authorizationTypesLoadFailed).toBe(true);
    expect(result.authorizedLoadFailed).toBe(false);
  });

  it('is registered on the responsible child route', () => {
    const detail = APPLICATIONS_ROUTES.find(({ path }) => path === ':id');
    const section = detail?.children?.find(({ path }) => path === 'responsible');
    expect(section?.resolve?.[APPLICATION_RESPONSIBLE_RESOLVE_KEY]).toBe(
      applicationResponsibleResolver,
    );
  });

  function resolve(anchorId: number | null): Promise<ApplicationResponsibleResolvedData> {
    const route = {
      parent: {
        data: {
          [APPLICATION_DETAIL_RESOLVE_KEY]: {
            application: { id: 7, appResponsibleAuthorizedId: anchorId } as ApplicationOutput,
            loadFailed: false,
          },
        },
      },
    } as unknown as ActivatedRouteSnapshot;
    const source = TestBed.runInInjectionContext(() =>
      applicationResponsibleResolver(route, {} as RouterStateSnapshot),
    );
    return firstValueFrom(source as Observable<ApplicationResponsibleResolvedData>);
  }
});

function page<T>(content: T[]) {
  return { content, totalElements: content.length } as never;
}
