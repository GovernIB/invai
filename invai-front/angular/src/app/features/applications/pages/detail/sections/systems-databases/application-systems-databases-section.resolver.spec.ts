import { TestBed } from '@angular/core/testing';
import {
  ActivatedRouteSnapshot,
  convertToParamMap,
  RouterStateSnapshot,
} from '@angular/router';
import { DatabasesService } from '@features/systems/services/databases.service';
import { SystemsService } from '@features/systems/services/systems.service';
import {
  DatabaseRecord,
  InfrastructureStatus,
  InfrastructureSystem,
} from '@features/systems/systems.model';
import { SpringPage } from '@models/page.model';
import { Observable, firstValueFrom, of, throwError } from 'rxjs';

import {
  ApplicationDatabase,
  ApplicationInfrastructureStatus,
  ApplicationOutput,
  ApplicationServer,
  ApplicationSystemDatabaseOutput,
} from '../../../../applications.model';
import { APPLICATIONS_ROUTES } from '../../../../applications.routes';
import { ApplicationDatabasesService } from '../../../../services/application-databases.service';
import { ApplicationInfrastructureFilterOptionsService } from '../../../../services/application-infrastructure-filter-options.service';
import { ApplicationSystemDatabaseService } from '../../../../services/application-system-database.service';
import { ApplicationSystemsService } from '../../../../services/application-systems.service';
import { APPLICATION_DETAIL_RESOLVE_KEY } from '../../application-detail.resolver';
import {
  APPLICATION_SYSTEMS_DATABASES_RESOLVE_KEY,
  ApplicationSystemsDatabasesResolvedData,
  applicationSystemsDatabasesResolver,
} from './application-systems-databases-section.resolver';

const SERVER = { id: 10 } as ApplicationServer;
const DATABASE = { id: 20 } as ApplicationDatabase;
const SYSTEM_CATALOG_ITEM = { id: 5 } as InfrastructureSystem;
const DATABASE_CATALOG_ITEM = { id: 8 } as DatabaseRecord;
const SYSTEM_DATABASE = {
  id: 70,
  application: {
    id: 7,
    appInformationSystemDbId: 70,
    appDevelopmentId: 90,
  } as ApplicationOutput,
  observation: '<p>Observacions</p>',
  deletedAt: null,
} as ApplicationSystemDatabaseOutput;

describe('applicationSystemsDatabasesResolver', () => {
  let getSystemsPage: ReturnType<typeof vi.fn>;
  let getDatabasesPage: ReturnType<typeof vi.fn>;
  let getSystemsCatalog: ReturnType<typeof vi.fn>;
  let getDatabasesCatalog: ReturnType<typeof vi.fn>;
  let getSystemDatabase: ReturnType<typeof vi.fn>;
  let getServerOptions: ReturnType<typeof vi.fn>;
  let getDatabaseOptions: ReturnType<typeof vi.fn>;
  let getEnvironmentOptions: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    getSystemsPage = vi.fn(() => of(page([SERVER])));
    getDatabasesPage = vi.fn(() => of(page([DATABASE])));
    getSystemsCatalog = vi.fn(() => of(page([SYSTEM_CATALOG_ITEM])));
    getDatabasesCatalog = vi.fn(() => of(page([DATABASE_CATALOG_ITEM])));
    getSystemDatabase = vi.fn(() => of(SYSTEM_DATABASE));
    getServerOptions = vi.fn(() => of([{ label: 'app01.caib.es', value: 5 }]));
    getDatabaseOptions = vi.fn(() => of([{ label: 'INVAI_PRD', value: 6 }]));
    getEnvironmentOptions = vi.fn(() => of([{ label: 'Producció', value: 3 }]));

    TestBed.configureTestingModule({
      providers: [
        { provide: ApplicationSystemsService, useValue: { getPage: getSystemsPage } },
        { provide: ApplicationDatabasesService, useValue: { getPage: getDatabasesPage } },
        { provide: SystemsService, useValue: { getAll: getSystemsCatalog } },
        { provide: DatabasesService, useValue: { getAll: getDatabasesCatalog } },
        {
          provide: ApplicationSystemDatabaseService,
          useValue: { getById: getSystemDatabase },
        },
        {
          provide: ApplicationInfrastructureFilterOptionsService,
          useValue: { getServerOptions, getDatabaseOptions, getEnvironmentOptions },
        },
      ],
    });
  });

  it('preloads application relations, active maintenance catalogs and observations', async () => {
    const result = await resolveSection('7');
    const relationParams = {
      informationSystemDbId: 70,
      statusId: ApplicationInfrastructureStatus.ACTIVE,
      page: 0,
      size: 10,
    };
    const catalogParams = {
      statusId: InfrastructureStatus.ACTIVE,
      page: 0,
      size: 10,
      unassignedToInformationSystemDbId: 70,
    };

    expect(getSystemsPage).toHaveBeenCalledWith(relationParams);
    expect(getDatabasesPage).toHaveBeenCalledWith(relationParams);
    expect(getSystemsCatalog).toHaveBeenCalledWith(catalogParams);
    expect(getDatabasesCatalog).toHaveBeenCalledWith(catalogParams);
    expect(getSystemDatabase).toHaveBeenCalledWith(70);
    expect(result).toEqual({
      applicationId: 7,
      informationSystemDbId: 70,
      serversPage: page([SERVER]),
      databasesPage: page([DATABASE]),
      systemCatalogPage: page([SYSTEM_CATALOG_ITEM]),
      databaseCatalogPage: page([DATABASE_CATALOG_ITEM]),
      systemDatabase: SYSTEM_DATABASE,
      serversLoadFailed: false,
      databasesLoadFailed: false,
      systemCatalogLoadFailed: false,
      databaseCatalogLoadFailed: false,
      systemDatabaseLoadFailed: false,
      filterOptions: {
        servers: [{ label: 'app01.caib.es', value: 5 }],
        databases: [{ label: 'INVAI_PRD', value: 6 }],
        environments: [{ label: 'Producció', value: 3 }],
      },
      filterOptionsLoadFailed: { servers: false, databases: false, environments: false },
    });
  });

  it('degrades relation, catalog and observation requests independently', async () => {
    getSystemsPage.mockReturnValueOnce(throwError(() => new Error('Systems unavailable')));
    getDatabasesCatalog.mockReturnValueOnce(
      throwError(() => new Error('Database catalog unavailable')),
    );
    getSystemDatabase.mockReturnValueOnce(
      throwError(() => new Error('Observations unavailable')),
    );

    const result = await resolveSection('7');

    expect(result.serversPage).toBeNull();
    expect(result.databasesPage).toEqual(page([DATABASE]));
    expect(result.databaseCatalogPage).toBeNull();
    expect(result.systemDatabase).toBeNull();
    expect(result.serversLoadFailed).toBe(true);
    expect(result.databasesLoadFailed).toBe(false);
    expect(result.databaseCatalogLoadFailed).toBe(true);
    expect(result.systemDatabaseLoadFailed).toBe(true);
  });

  it('treats a missing observations aggregate as an empty successful state', async () => {
    getSystemDatabase.mockReturnValueOnce(of(null));

    const result = await resolveSection('7');

    expect(result.systemDatabase).toBeNull();
    expect(result.systemDatabaseLoadFailed).toBe(false);
  });

  it('omits relation and assignable catalog requests when the parent detail has no child id', async () => {
    const result = await resolveSection('7', null);

    expect(getSystemsPage).not.toHaveBeenCalled();
    expect(getDatabasesPage).not.toHaveBeenCalled();
    expect(getSystemDatabase).not.toHaveBeenCalled();
    expect(getSystemsCatalog).not.toHaveBeenCalled();
    expect(getDatabasesCatalog).not.toHaveBeenCalled();
    expect(result).toEqual(
      expect.objectContaining({
        applicationId: 7,
        informationSystemDbId: null,
        serversPage: null,
        databasesPage: null,
        systemCatalogPage: null,
        databaseCatalogPage: null,
        systemDatabase: null,
        serversLoadFailed: false,
        databasesLoadFailed: false,
        systemDatabaseLoadFailed: false,
      }),
    );
  });

  it('degrades selector catalogs independently', async () => {
    getDatabaseOptions.mockReturnValueOnce(
      throwError(() => new Error('Database options unavailable')),
    );

    const result = await resolveSection('7');

    expect(result.filterOptions.databases).toEqual([]);
    expect(result.filterOptionsLoadFailed).toEqual({
      servers: false,
      databases: true,
      environments: false,
    });
  });

  it('does not request data for an invalid parent application id', async () => {
    const result = await resolveSection('invalid');

    expect(getSystemsPage).not.toHaveBeenCalled();
    expect(getDatabasesPage).not.toHaveBeenCalled();
    expect(getSystemsCatalog).not.toHaveBeenCalled();
    expect(getDatabasesCatalog).not.toHaveBeenCalled();
    expect(getSystemDatabase).not.toHaveBeenCalled();
    expect(result).toEqual({
      applicationId: null,
      informationSystemDbId: null,
      serversPage: null,
      databasesPage: null,
      systemCatalogPage: null,
      databaseCatalogPage: null,
      systemDatabase: null,
      serversLoadFailed: true,
      databasesLoadFailed: true,
      systemCatalogLoadFailed: true,
      databaseCatalogLoadFailed: true,
      systemDatabaseLoadFailed: true,
      filterOptions: { servers: [], databases: [], environments: [] },
      filterOptionsLoadFailed: { servers: true, databases: true, environments: true },
    });
  });

  it('is registered on the systems and databases child route', () => {
    const detailRoute = APPLICATIONS_ROUTES.find(({ path }) => path === ':id');
    const sectionRoute = detailRoute?.children?.find(({ path }) => path === 'systems-databases');

    expect(sectionRoute?.resolve?.[APPLICATION_SYSTEMS_DATABASES_RESOLVE_KEY]).toBe(
      applicationSystemsDatabasesResolver,
    );
  });

  function resolveSection(
    id: string,
    appInformationSystemDbId: number | null = 70,
  ): Promise<ApplicationSystemsDatabasesResolvedData> {
    const route = {
      parent: {
        paramMap: convertToParamMap({ id }),
        data: {
          [APPLICATION_DETAIL_RESOLVE_KEY]: {
            application: {
              id: 7,
              appInformationSystemDbId,
              appDevelopmentId: 90,
            } as ApplicationOutput,
            loadFailed: false,
          },
        },
      },
    } as unknown as ActivatedRouteSnapshot;
    const result = TestBed.runInInjectionContext(() =>
      applicationSystemsDatabasesResolver(route, {} as RouterStateSnapshot),
    );

    return firstValueFrom(result as Observable<ApplicationSystemsDatabasesResolvedData>);
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
