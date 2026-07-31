import { inject } from '@angular/core';
import { ResolveFn } from '@angular/router';
import { EnvironmentCatalogOption } from '@features/environments/environments.model';
import { EnvironmentCatalogService } from '@features/environments/services/environment-catalog.service';
import { RoleCatalogOption } from '@features/roles/roles.model';
import { RoleCatalogService } from '@features/roles/services/role-catalog.service';
import { TechnologyCatalogService } from '@features/technologies/services/technology-catalog.service';
import { TechnologyCatalogOption } from '@features/technologies/technologies.model';
import { SpringPage } from '@models/page.model';
import { catchError, forkJoin, map, of } from 'rxjs';

import {
  ApplicationDevelopmentOutput,
  ApplicationProviderOutput,
  ApplicationTechnologyOutput,
} from '../../../../applications.model';
import { ApplicationDevelopmentService } from '../../../../services/application-development.service';
import { ApplicationProvidersService } from '../../../../services/application-providers.service';
import { ApplicationTechnologiesService } from '../../../../services/application-technologies.service';
import {
  APPLICATION_DETAIL_RESOLVE_KEY,
  ApplicationDetailResolvedData,
} from '../../application-detail.resolver';

export const APPLICATION_DEVELOPMENT_RESOLVE_KEY = 'applicationDevelopment';

export interface ApplicationDevelopmentResolvedData {
  applicationId: number | null;
  appDevelopmentId: number | null;
  development: ApplicationDevelopmentOutput | null;
  developmentLoadFailed: boolean;
  providersPage: SpringPage<ApplicationProviderOutput> | null;
  providersLoadFailed: boolean;
  technologiesPage: SpringPage<ApplicationTechnologyOutput> | null;
  technologiesLoadFailed: boolean;
  environmentOptions: EnvironmentCatalogOption[];
  environmentOptionsLoadFailed: boolean;
  roleOptions: RoleCatalogOption[];
  roleOptionsLoadFailed: boolean;
  technologyOptions: TechnologyCatalogOption[];
  technologyOptionsLoadFailed: boolean;
}

const INITIAL_PAGE_PARAMS = {
  page: 0,
  size: 10,
  sort: 'id,asc',
} as const;

export const applicationDevelopmentResolver: ResolveFn<ApplicationDevelopmentResolvedData> = (
  route,
) => {
  const applicationId = Number(route.parent?.paramMap.get('id'));

  if (!Number.isInteger(applicationId) || applicationId <= 0) {
    return of(emptyResolvedData());
  }

  const detailData = route.parent?.data[
    APPLICATION_DETAIL_RESOLVE_KEY
  ] as ApplicationDetailResolvedData | undefined;
  const appDevelopmentId = positiveId(detailData?.application?.appDevelopmentId);
  const developmentService = inject(ApplicationDevelopmentService);
  const providersService = inject(ApplicationProvidersService);
  const technologiesService = inject(ApplicationTechnologiesService);
  const developmentResult =
    appDevelopmentId == null
      ? of({ development: null, failed: false })
      : developmentService.getById(appDevelopmentId).pipe(
          map((development) => ({ development, failed: false })),
          catchError(() => of({ development: null, failed: true })),
        );
  const providersResult =
    appDevelopmentId == null
      ? of({ page: null, failed: false })
      : providersService
          .getPage({ appDevelopmentId, ...INITIAL_PAGE_PARAMS })
          .pipe(
            map((page) => ({ page, failed: false })),
            catchError(() => of({ page: null, failed: true })),
          );
  const technologiesResult =
    appDevelopmentId == null
      ? of({ page: null, failed: false })
      : technologiesService
          .getPage({ appDevelopmentId, ...INITIAL_PAGE_PARAMS })
          .pipe(
            map((page) => ({ page, failed: false })),
            catchError(() => of({ page: null, failed: true })),
          );

  return forkJoin({
    developmentResult,
    providersResult,
    technologiesResult,
    environmentOptionsResult: inject(EnvironmentCatalogService)
      .getActiveOptions()
      .pipe(
        map((options) => ({ options, failed: false })),
        catchError(() => of({ options: [], failed: true })),
      ),
    roleOptionsResult: inject(RoleCatalogService)
      .getActiveOptions()
      .pipe(
        map((options) => ({ options, failed: false })),
        catchError(() => of({ options: [], failed: true })),
      ),
    technologyOptionsResult: inject(TechnologyCatalogService)
      .getActiveOptions()
      .pipe(
        map((options) => ({ options, failed: false })),
        catchError(() => of({ options: [], failed: true })),
      ),
  }).pipe(
    map(
      ({
        developmentResult,
        providersResult,
        technologiesResult,
        environmentOptionsResult,
        roleOptionsResult,
        technologyOptionsResult,
      }) => ({
        applicationId,
        appDevelopmentId,
        development: developmentResult.development,
        developmentLoadFailed: developmentResult.failed,
        providersPage: providersResult.page,
        providersLoadFailed: providersResult.failed,
        technologiesPage: technologiesResult.page,
        technologiesLoadFailed: technologiesResult.failed,
        environmentOptions: environmentOptionsResult.options,
        environmentOptionsLoadFailed: environmentOptionsResult.failed,
        roleOptions: roleOptionsResult.options,
        roleOptionsLoadFailed: roleOptionsResult.failed,
        technologyOptions: technologyOptionsResult.options,
        technologyOptionsLoadFailed: technologyOptionsResult.failed,
      }),
    ),
  );
};

function emptyResolvedData(): ApplicationDevelopmentResolvedData {
  return {
    applicationId: null,
    appDevelopmentId: null,
    development: null,
    developmentLoadFailed: true,
    providersPage: null,
    providersLoadFailed: true,
    technologiesPage: null,
    technologiesLoadFailed: true,
    environmentOptions: [],
    environmentOptionsLoadFailed: true,
    roleOptions: [],
    roleOptionsLoadFailed: true,
    technologyOptions: [],
    technologyOptionsLoadFailed: true,
  };
}

function positiveId(value: number | null | undefined): number | null {
  return Number.isInteger(value) && Number(value) > 0 ? Number(value) : null;
}
