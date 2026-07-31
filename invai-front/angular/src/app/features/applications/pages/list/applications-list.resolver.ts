import { inject } from '@angular/core';
import { ResolveFn } from '@angular/router';
import { SpringPage } from '@models/page.model';
import { catchError, forkJoin, map, of } from 'rxjs';

import {
  Application,
  ApplicationInfrastructureFilterOptions,
  ApplicationStatus,
} from '../../applications.model';
import {
  ApplicationOptionsService,
  ApplicationSelectOptions,
} from '../../services/application-options.service';
import {
  ApplicationInfrastructureFilterOptionsService,
} from '../../services/application-infrastructure-filter-options.service';
import { ApplicationsService } from '../../services/applications.service';
import { resolveApplicationOptions } from '../../resolvers/application-options.resolver';

export const APPLICATIONS_LIST_RESOLVE_KEY = 'applicationsList';

export interface ApplicationsListResolvedData {
  page: SpringPage<Application> | null;
  options: ApplicationSelectOptions;
  infrastructureOptions: ApplicationInfrastructureFilterOptions;
  pageLoadFailed: boolean;
  optionsLoadFailed: boolean;
}

const INITIAL_PAGE_PARAMS = {
  page: 0,
  size: 10,
  statusId: ApplicationStatus.ACTIVE,
} as const;

export const applicationsListResolver: ResolveFn<ApplicationsListResolvedData> = () => {
  const applicationsService = inject(ApplicationsService);
  const applicationOptionsService = inject(ApplicationOptionsService);
  const infrastructureOptionsService = inject(ApplicationInfrastructureFilterOptionsService);

  return forkJoin({
    pageResult: applicationsService.getPage(INITIAL_PAGE_PARAMS).pipe(
      map((page) => ({ page, failed: false })),
      catchError(() => of({ page: null, failed: true })),
    ),
    optionsResult: resolveApplicationOptions(applicationOptionsService),
    serverOptions: infrastructureOptionsService
      .getServerOptions()
      .pipe(catchError(() => of([]))),
    databaseOptions: infrastructureOptionsService
      .getDatabaseOptions()
      .pipe(catchError(() => of([]))),
    environmentOptions: infrastructureOptionsService
      .getEnvironmentOptions()
      .pipe(catchError(() => of([]))),
  }).pipe(
    map(
      ({
        pageResult,
        optionsResult,
        serverOptions,
        databaseOptions,
        environmentOptions,
      }) => ({
        page: pageResult.page,
        options: optionsResult.options,
        infrastructureOptions: {
          servers: serverOptions,
          databases: databaseOptions,
          environments: environmentOptions,
        },
        pageLoadFailed: pageResult.failed,
        optionsLoadFailed: optionsResult.loadFailed,
      }),
    ),
  );
};
