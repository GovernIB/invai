import { inject } from '@angular/core';
import { ResolveFn } from '@angular/router';
import { DatabasesService } from '@features/systems/services/databases.service';
import { SystemsService } from '@features/systems/services/systems.service';
import {
  DatabaseRecord,
  InfrastructureStatus,
  InfrastructureSystem,
} from '@features/systems/systems.model';
import { SpringPage } from '@models/page.model';
import { catchError, forkJoin, map, of } from 'rxjs';

import {
  ApplicationDatabase,
  ApplicationInfrastructureFilterOptions,
  ApplicationInfrastructureStatus,
  ApplicationServer,
  ApplicationSystemDatabaseOutput,
} from '../../../../applications.model';
import { ApplicationDatabasesService } from '../../../../services/application-databases.service';
import { ApplicationSystemsService } from '../../../../services/application-systems.service';
import { ApplicationInfrastructureFilterOptionsService } from '../../../../services/application-infrastructure-filter-options.service';
import { ApplicationSystemDatabaseService } from '../../../../services/application-system-database.service';
import {
  APPLICATION_DETAIL_RESOLVE_KEY,
  ApplicationDetailResolvedData,
} from '../../application-detail.resolver';

export const APPLICATION_SYSTEMS_DATABASES_RESOLVE_KEY = 'applicationSystemsDatabases';

export interface ApplicationSystemsDatabasesResolvedData {
  applicationId: number | null;
  informationSystemDbId: number | null;
  serversPage: SpringPage<ApplicationServer> | null;
  databasesPage: SpringPage<ApplicationDatabase> | null;
  systemCatalogPage: SpringPage<InfrastructureSystem> | null;
  databaseCatalogPage: SpringPage<DatabaseRecord> | null;
  systemDatabase: ApplicationSystemDatabaseOutput | null;
  serversLoadFailed: boolean;
  databasesLoadFailed: boolean;
  systemCatalogLoadFailed: boolean;
  databaseCatalogLoadFailed: boolean;
  systemDatabaseLoadFailed: boolean;
  filterOptions: ApplicationInfrastructureFilterOptions;
  filterOptionsLoadFailed: Record<keyof ApplicationInfrastructureFilterOptions, boolean>;
}

const INITIAL_PAGE = 0;
const INITIAL_PAGE_SIZE = 10;

export const applicationSystemsDatabasesResolver: ResolveFn<
  ApplicationSystemsDatabasesResolvedData
> = (route) => {
  const applicationId = Number(route.parent?.paramMap.get('id'));

  if (!Number.isInteger(applicationId) || applicationId <= 0) {
    return of({
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
  }

  const detailData = route.parent?.data[
    APPLICATION_DETAIL_RESOLVE_KEY
  ] as ApplicationDetailResolvedData | undefined;
  const informationSystemDbId = positiveId(
    detailData?.application?.appInformationSystemDbId,
  );
  const optionsService = inject(ApplicationInfrastructureFilterOptionsService);
  const systemsService = inject(ApplicationSystemsService);
  const databasesService = inject(ApplicationDatabasesService);
  const systemDatabaseService = inject(ApplicationSystemDatabaseService);
  const initialParams =
    informationSystemDbId == null
      ? null
      : {
          informationSystemDbId,
          statusId: ApplicationInfrastructureStatus.ACTIVE,
          page: INITIAL_PAGE,
          size: INITIAL_PAGE_SIZE,
        };
  const serversResult =
    initialParams == null
      ? of({ page: null, failed: false })
      : systemsService.getPage(initialParams).pipe(
          map((page) => ({ page, failed: false })),
          catchError(() => of({ page: null, failed: true })),
        );
  const databasesResult =
    initialParams == null
      ? of({ page: null, failed: false })
      : databasesService.getPage(initialParams).pipe(
          map((page) => ({ page, failed: false })),
          catchError(() => of({ page: null, failed: true })),
        );
  const systemDatabaseResult =
    informationSystemDbId == null
      ? of({ record: null, failed: false })
      : systemDatabaseService.getById(informationSystemDbId).pipe(
          map((record) => ({ record, failed: false })),
          catchError(() => of({ record: null, failed: true })),
        );
  const activeCatalogParams = {
    statusId: InfrastructureStatus.ACTIVE,
    page: INITIAL_PAGE,
    size: INITIAL_PAGE_SIZE,
  } as const;

  return forkJoin({
    serversResult,
    databasesResult,
    serverOptionsResult: optionsService.getServerOptions().pipe(
      map((options) => ({ options, failed: false })),
      catchError(() => of({ options: [], failed: true })),
    ),
    databaseOptionsResult: optionsService.getDatabaseOptions().pipe(
      map((options) => ({ options, failed: false })),
      catchError(() => of({ options: [], failed: true })),
    ),
    environmentOptionsResult: optionsService.getEnvironmentOptions().pipe(
      map((options) => ({ options, failed: false })),
      catchError(() => of({ options: [], failed: true })),
    ),
    systemCatalogResult: inject(SystemsService)
      .getAll(activeCatalogParams)
      .pipe(
        map((page) => ({ page, failed: false })),
        catchError(() => of({ page: null, failed: true })),
      ),
    databaseCatalogResult: inject(DatabasesService)
      .getAll(activeCatalogParams)
      .pipe(
        map((page) => ({ page, failed: false })),
        catchError(() => of({ page: null, failed: true })),
      ),
    systemDatabaseResult,
  }).pipe(
    map(({
      serversResult,
      databasesResult,
      serverOptionsResult,
      databaseOptionsResult,
      environmentOptionsResult,
      systemCatalogResult,
      databaseCatalogResult,
      systemDatabaseResult,
    }) => ({
      applicationId,
      informationSystemDbId,
      serversPage: serversResult.page,
      databasesPage: databasesResult.page,
      systemCatalogPage: systemCatalogResult.page,
      databaseCatalogPage: databaseCatalogResult.page,
      systemDatabase: systemDatabaseResult.record,
      serversLoadFailed: serversResult.failed,
      databasesLoadFailed: databasesResult.failed,
      systemCatalogLoadFailed: systemCatalogResult.failed,
      databaseCatalogLoadFailed: databaseCatalogResult.failed,
      systemDatabaseLoadFailed: systemDatabaseResult.failed,
      filterOptions: {
        servers: serverOptionsResult.options,
        databases: databaseOptionsResult.options,
        environments: environmentOptionsResult.options,
      },
      filterOptionsLoadFailed: {
        servers: serverOptionsResult.failed,
        databases: databaseOptionsResult.failed,
        environments: environmentOptionsResult.failed,
      },
    })),
  );
};

function positiveId(value: number | null | undefined): number | null {
  return Number.isInteger(value) && Number(value) > 0 ? Number(value) : null;
}
