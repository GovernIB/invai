import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { SpringPage } from '@models/page.model';
import { Observable, firstValueFrom, of, throwError } from 'rxjs';

import { APPLICATIONS_ROUTES } from '../../applications.routes';
import {
  Application,
  ApplicationInfrastructureFilterOptions,
  ApplicationStatus,
} from '../../applications.model';
import { ApplicationInfrastructureFilterOptionsService } from '../../services/application-infrastructure-filter-options.service';
import {
  ApplicationOptionsService,
  ApplicationSelectOptions,
} from '../../services/application-options.service';
import { ApplicationsService } from '../../services/applications.service';
import {
  APPLICATIONS_LIST_RESOLVE_KEY,
  ApplicationsListResolvedData,
  applicationsListResolver,
} from './applications-list.resolver';

const APPLICATION: Application = {
  id: '1',
  code: 'APP-1',
  prefix: 'INV',
  name: 'Invai',
  category: 'DRASSANA',
  informationSystem: 'Instrumental',
  scope: 'Departamental',
  commission: 'Equip directiu',
  department: 'Conselleria',
  administrativeUnit: 'Direcció General',
  status: ApplicationStatus.ACTIVE,
  description: 'Aplicació interna',
  creationDate: '2026-01-01T10:00:00',
  modificationDate: '',
  withdrawalDate: '',
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

const OPTIONS: ApplicationSelectOptions = {
  categories: [{ label: 'DRASSANA', value: 1 }],
  informationSystems: [],
  scopes: [],
  commissions: [],
  departments: [{ label: 'Conselleria', value: 'GVA01' }],
  administrativeUnits: [],
};

const INFRASTRUCTURE_OPTIONS: ApplicationInfrastructureFilterOptions = {
  servers: [{ label: 'app01.caib.es', value: 5 }],
  databases: [{ label: 'INVAI', value: 8 }],
  environments: [{ label: 'Producció', value: 3 }],
};

describe('applicationsListResolver', () => {
  let getPage: ReturnType<typeof vi.fn>;
  let getStaticOptions: ReturnType<typeof vi.fn>;
  let getDepartmentOptions: ReturnType<typeof vi.fn>;
  let getPhysicalServerOptions: ReturnType<typeof vi.fn>;
  let getDatabaseOptions: ReturnType<typeof vi.fn>;
  let getEnvironmentOptions: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    getPage = vi.fn(() => of(page([APPLICATION])));
    getStaticOptions = vi.fn(() =>
      of({
        categories: OPTIONS.categories,
        informationSystems: OPTIONS.informationSystems,
        scopes: OPTIONS.scopes,
        commissions: OPTIONS.commissions,
      }),
    );
    getDepartmentOptions = vi.fn(() => of(OPTIONS.departments));
    getPhysicalServerOptions = vi.fn(() => of(INFRASTRUCTURE_OPTIONS.servers));
    getDatabaseOptions = vi.fn(() => of(INFRASTRUCTURE_OPTIONS.databases));
    getEnvironmentOptions = vi.fn(() => of(INFRASTRUCTURE_OPTIONS.environments));

    TestBed.configureTestingModule({
      providers: [
        { provide: ApplicationsService, useValue: { getPage } },
        {
          provide: ApplicationOptionsService,
          useValue: { getStaticOptions, getDepartmentOptions },
        },
        {
          provide: ApplicationInfrastructureFilterOptionsService,
          useValue: { getPhysicalServerOptions, getDatabaseOptions, getEnvironmentOptions },
        },
      ],
    });
  });

  it('should resolve the active first page and filter options', async () => {
    const result = await resolveList();

    expect(getPage).toHaveBeenCalledWith({
      page: 0,
      size: 10,
      statusId: ApplicationStatus.ACTIVE,
    });
    expect(getStaticOptions).toHaveBeenCalledOnce();
    expect(getDepartmentOptions).toHaveBeenCalledOnce();
    expect(getPhysicalServerOptions).toHaveBeenCalledOnce();
    expect(getDatabaseOptions).toHaveBeenCalledOnce();
    expect(getEnvironmentOptions).toHaveBeenCalledOnce();
    expect(result).toEqual({
      page: page([APPLICATION]),
      options: OPTIONS,
      infrastructureOptions: INFRASTRUCTURE_OPTIONS,
      pageLoadFailed: false,
      optionsLoadFailed: false,
      departmentsLoadFailed: false,
      administrativeUnitsLoadFailed: false,
    });
  });

  it('should complete the route with an application-page error state', async () => {
    getPage.mockReturnValueOnce(throwError(() => new Error('Applications unavailable')));

    const result = await resolveList();

    expect(result).toEqual({
      page: null,
      options: OPTIONS,
      infrastructureOptions: INFRASTRUCTURE_OPTIONS,
      pageLoadFailed: true,
      optionsLoadFailed: false,
      departmentsLoadFailed: false,
      administrativeUnitsLoadFailed: false,
    });
  });

  it('should complete the route with empty options when their orchestration fails', async () => {
    getStaticOptions.mockReturnValueOnce(throwError(() => new Error('Options unavailable')));

    const result = await resolveList();

    expect(result).toEqual({
      page: page([APPLICATION]),
      options: {
        categories: [],
        informationSystems: [],
        scopes: [],
        commissions: [],
        departments: OPTIONS.departments,
        administrativeUnits: [],
      },
      infrastructureOptions: INFRASTRUCTURE_OPTIONS,
      pageLoadFailed: false,
      optionsLoadFailed: true,
      departmentsLoadFailed: false,
      administrativeUnitsLoadFailed: false,
    });
  });

  it('should isolate infrastructure catalog failures', async () => {
    getDatabaseOptions.mockReturnValueOnce(
      throwError(() => new Error('Database options unavailable')),
    );

    const result = await resolveList();

    expect(result.infrastructureOptions).toEqual({
      servers: INFRASTRUCTURE_OPTIONS.servers,
      databases: [],
      environments: INFRASTRUCTURE_OPTIONS.environments,
    });
  });

  it('should be registered on the applications list route', () => {
    expect(APPLICATIONS_ROUTES[0]?.resolve?.[APPLICATIONS_LIST_RESOLVE_KEY]).toBe(
      applicationsListResolver,
    );
  });

  function resolveList(): Promise<ApplicationsListResolvedData> {
    const result = TestBed.runInInjectionContext(() =>
      applicationsListResolver({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot),
    );

    return firstValueFrom(result as Observable<ApplicationsListResolvedData>);
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
