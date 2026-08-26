import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, convertToParamMap, RouterStateSnapshot } from '@angular/router';
import { EnvironmentCatalogService } from '@features/environments/services/environment-catalog.service';
import { RoleCatalogService } from '@features/roles/services/role-catalog.service';
import { TechnologyCatalogService } from '@features/technologies/services/technology-catalog.service';
import { SpringPage } from '@models/page.model';
import { Observable, firstValueFrom, of, throwError } from 'rxjs';

import { APPLICATIONS_ROUTES } from '../../../../applications.routes';
import {
  ApplicationDevelopmentOutput,
  ApplicationOutput,
  DevelopmentModality,
  DevelopmentStandardAdaption,
} from '../../../../applications.model';
import { ApplicationDevelopmentService } from '../../../../services/application-development.service';
import { ApplicationProvidersService } from '../../../../services/application-providers.service';
import { ApplicationTechnologiesService } from '../../../../services/application-technologies.service';
import { DevelopmentModalityCatalogService } from '../../../../services/development-modality-catalog.service';
import { DevelopmentStandardAdaptionCatalogService } from '../../../../services/development-standard-adaption-catalog.service';
import { APPLICATION_DETAIL_RESOLVE_KEY } from '../../application-detail.resolver';
import {
  APPLICATION_DEVELOPMENT_RESOLVE_KEY,
  ApplicationDevelopmentResolvedData,
  applicationDevelopmentResolver,
} from './application-development-section.resolver';

const DEVELOPMENT: ApplicationDevelopmentOutput = {
  id: 9,
  application: { id: 7 } as ApplicationDevelopmentOutput['application'],
  environment: { id: 3, code: 'PRO', name: 'Producció', nameEs: 'Producción' },
  modality: { id: DevelopmentModality.INTERNAL, name: 'Intern', nameEs: 'Interno' },
  code: 'https://git.caib.es/invai',
  standardAdaption: {
    id: DevelopmentStandardAdaption.CONFORMING,
    name: 'Conforme',
    nameEs: 'Conforme',
  },
  revisionDate: '2026-05-02T00:00:00',
  observation: '<p>Observació</p>',
  deletedAt: null,
};

describe('applicationDevelopmentResolver', () => {
  let getDevelopment: ReturnType<typeof vi.fn>;
  let getProvidersPage: ReturnType<typeof vi.fn>;
  let getTechnologiesPage: ReturnType<typeof vi.fn>;
  let getEnvironmentOptions: ReturnType<typeof vi.fn>;
  let getRoleOptions: ReturnType<typeof vi.fn>;
  let getTechnologyOptions: ReturnType<typeof vi.fn>;
  let getModalityOptions: ReturnType<typeof vi.fn>;
  let getStandardAdaptionOptions: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    getDevelopment = vi.fn(() => of(DEVELOPMENT));
    getProvidersPage = vi.fn(() => of(page([])));
    getTechnologiesPage = vi.fn(() => of(page([])));
    getEnvironmentOptions = vi.fn(() => of([{ id: 3, code: 'PRO', label: 'Producció' }]));
    getRoleOptions = vi.fn(() => of([{ id: 4, label: 'Desenvolupament' }]));
    getTechnologyOptions = vi.fn(() =>
      of([
        {
          id: 2,
          label: 'Angular',
          layerId: 1,
          layerLabel: 'Frontend',
        },
      ]),
    );
    getModalityOptions = vi.fn(() =>
      of([{ id: DevelopmentModality.INTERNAL, name: 'Intern', nameEs: 'Interno' }]),
    );
    getStandardAdaptionOptions = vi.fn(() =>
      of([
        {
          id: DevelopmentStandardAdaption.CONFORMING,
          name: 'Conforme',
          nameEs: 'Conforme',
        },
      ]),
    );
    TestBed.configureTestingModule({
      providers: [
        {
          provide: ApplicationDevelopmentService,
          useValue: { getById: getDevelopment },
        },
        { provide: ApplicationProvidersService, useValue: { getPage: getProvidersPage } },
        {
          provide: ApplicationTechnologiesService,
          useValue: { getPage: getTechnologiesPage },
        },
        {
          provide: EnvironmentCatalogService,
          useValue: { getActiveOptions: getEnvironmentOptions },
        },
        {
          provide: RoleCatalogService,
          useValue: { getActiveOptions: getRoleOptions },
        },
        {
          provide: TechnologyCatalogService,
          useValue: { getActiveOptions: getTechnologyOptions },
        },
        {
          provide: DevelopmentModalityCatalogService,
          useValue: { getAll: getModalityOptions },
        },
        {
          provide: DevelopmentStandardAdaptionCatalogService,
          useValue: { getAll: getStandardAdaptionOptions },
        },
      ],
    });
  });

  it('resolves the stable first render concurrently with exact initial parameters', async () => {
    const result = await resolve('7');
    const expectedParams = {
      appDevelopmentId: 90,
      page: 0,
      size: 10,
      sort: 'id,asc',
    };

    expect(getDevelopment).toHaveBeenCalledWith(90);
    expect(getProvidersPage).toHaveBeenCalledWith(expectedParams);
    expect(getTechnologiesPage).toHaveBeenCalledWith(expectedParams);
    expect(result).toEqual({
      applicationId: 7,
      appDevelopmentId: 90,
      development: DEVELOPMENT,
      developmentLoadFailed: false,
      providersPage: page([]),
      providersLoadFailed: false,
      technologiesPage: page([]),
      technologiesLoadFailed: false,
      environmentOptions: [{ id: 3, code: 'PRO', label: 'Producció' }],
      environmentOptionsLoadFailed: false,
      roleOptions: [{ id: 4, label: 'Desenvolupament' }],
      roleOptionsLoadFailed: false,
      technologyOptions: [
        {
          id: 2,
          label: 'Angular',
          layerId: 1,
          layerLabel: 'Frontend',
        },
      ],
      technologyOptionsLoadFailed: false,
      modalityOptions: [{ id: DevelopmentModality.INTERNAL, name: 'Intern', nameEs: 'Interno' }],
      modalityOptionsLoadFailed: false,
      standardAdaptionOptions: [
        {
          id: DevelopmentStandardAdaption.CONFORMING,
          name: 'Conforme',
          nameEs: 'Conforme',
        },
      ],
      standardAdaptionOptionsLoadFailed: false,
    });
  });

  it('degrades failed resources independently', async () => {
    getDevelopment.mockReturnValueOnce(throwError(() => new Error('Unavailable')));
    getProvidersPage.mockReturnValueOnce(throwError(() => new Error('Unavailable')));
    getRoleOptions.mockReturnValueOnce(throwError(() => new Error('Unavailable')));

    const result = await resolve('7');

    expect(result.providersPage).toBeNull();
    expect(result.providersLoadFailed).toBe(true);
    expect(result.roleOptions).toEqual([]);
    expect(result.roleOptionsLoadFailed).toBe(true);
    expect(result.development).toBeNull();
    expect(result.developmentLoadFailed).toBe(true);
    expect(result.technologiesLoadFailed).toBe(false);
    expect(result.technologyOptionsLoadFailed).toBe(false);
  });

  it('treats a missing development aggregate as an empty successful state', async () => {
    getDevelopment.mockReturnValueOnce(of(null));

    const result = await resolve('7');

    expect(result.development).toBeNull();
    expect(result.developmentLoadFailed).toBe(false);
  });

  it('omits child API requests when the parent detail has no development id', async () => {
    const result = await resolve('7', null);

    expect(getDevelopment).not.toHaveBeenCalled();
    expect(getProvidersPage).not.toHaveBeenCalled();
    expect(getTechnologiesPage).not.toHaveBeenCalled();
    expect(getEnvironmentOptions).toHaveBeenCalledOnce();
    expect(getRoleOptions).toHaveBeenCalledOnce();
    expect(getTechnologyOptions).toHaveBeenCalledOnce();
    expect(getModalityOptions).toHaveBeenCalledOnce();
    expect(getStandardAdaptionOptions).toHaveBeenCalledOnce();
    expect(result).toEqual(
      expect.objectContaining({
        applicationId: 7,
        appDevelopmentId: null,
        development: null,
        developmentLoadFailed: false,
        providersPage: null,
        providersLoadFailed: false,
        technologiesPage: null,
        technologiesLoadFailed: false,
      }),
    );
  });

  it('does not call services for an invalid application id', async () => {
    const result = await resolve('invalid');

    expect(getDevelopment).not.toHaveBeenCalled();
    expect(getProvidersPage).not.toHaveBeenCalled();
    expect(getTechnologiesPage).not.toHaveBeenCalled();
    expect(getEnvironmentOptions).not.toHaveBeenCalled();
    expect(getRoleOptions).not.toHaveBeenCalled();
    expect(getTechnologyOptions).not.toHaveBeenCalled();
    expect(getModalityOptions).not.toHaveBeenCalled();
    expect(getStandardAdaptionOptions).not.toHaveBeenCalled();
    expect(result.applicationId).toBeNull();
    expect(result.appDevelopmentId).toBeNull();
    expect(result.developmentLoadFailed).toBe(true);
  });

  it('is registered on the development child route', () => {
    const detailRoute = APPLICATIONS_ROUTES.find(({ path }) => path === ':id');
    const sectionRoute = detailRoute?.children?.find(({ path }) => path === 'development');

    expect(sectionRoute?.resolve?.[APPLICATION_DEVELOPMENT_RESOLVE_KEY]).toBe(
      applicationDevelopmentResolver,
    );
  });

  function resolve(
    id: string,
    appDevelopmentId: number | null = 90,
  ): Promise<ApplicationDevelopmentResolvedData> {
    const route = {
      parent: {
        paramMap: convertToParamMap({ id }),
        data: {
          [APPLICATION_DETAIL_RESOLVE_KEY]: {
            application: {
              id: 7,
              appInformationSystemDbId: 70,
              appDevelopmentId,
            } as ApplicationOutput,
            loadFailed: false,
          },
        },
      },
    } as unknown as ActivatedRouteSnapshot;
    const result = TestBed.runInInjectionContext(() =>
      applicationDevelopmentResolver(route, {} as RouterStateSnapshot),
    );

    return firstValueFrom(result as Observable<ApplicationDevelopmentResolvedData>);
  }
});

function page<TItem>(content: TItem[]): SpringPage<TItem> {
  return {
    content,
    empty: content.length === 0,
    first: true,
    last: true,
    number: 0,
    numberOfElements: content.length,
    pageable: {
      offset: 0,
      pageNumber: 0,
      pageSize: 10,
      paged: true,
      sort: { empty: true, sorted: false, unsorted: true },
      unpaged: false,
    },
    size: 10,
    sort: { empty: true, sorted: false, unsorted: true },
    totalElements: content.length,
    totalPages: 1,
  };
}
