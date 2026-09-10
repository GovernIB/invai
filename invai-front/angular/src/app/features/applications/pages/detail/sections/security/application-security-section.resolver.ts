import { inject } from '@angular/core';
import { ResolveFn } from '@angular/router';
import { Field } from '@features/fields/fields.model';
import { FieldsService } from '@features/fields/services/fields.service';
import { SecurityResource } from '@features/maintenances/security/security.model';
import {
  EnsRequirementsService,
  IdentityProvidersService,
  PersonalDataProcessingService,
  SecurityMeasureTypesService,
  WebContextsService,
} from '@features/maintenances/security/services/security-resource.services';
import { SpringPage } from '@models/page.model';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';
import { PAGINATOR_ROWS } from '@shared/constants/table.constants';
import { Observable, catchError, defaultIfEmpty, forkJoin, map, of, switchMap } from 'rxjs';

import {
  ApplicationEnsClassificationOutput,
  ApplicationSecurityMeasureOutput,
  ApplicationSecurityOutput,
  ApplicationSecurityRiskOutput,
  ApplicationSecurityRoleOutput,
  ApplicationWebContextOutput,
  SecurityCatalogItem,
} from '../../../../applications.model';
import {
  ApplicationEnsClassificationsService,
  ApplicationSecurityMeasuresService,
  ApplicationSecurityRisksService,
  ApplicationSecurityRolesService,
  ApplicationSecurityService,
  ApplicationWebContextsService,
  EnsSubjectsService,
  SecurityLevelsService,
} from '../../../../services/application-security.service';
import { ApplicationsService } from '../../../../services/applications.service';
import {
  APPLICATION_DETAIL_RESOLVE_KEY,
  ApplicationDetailResolvedData,
} from '../../application-detail.resolver';

export const APPLICATION_SECURITY_RESOLVE_KEY = 'applicationSecurity';

// Fetch two active records so the editor can detect an ambiguous ENS classification.
const APPLICATION_ENS_CLASSIFICATION_PARAMS = {
  page: 0,
  size: 2,
  sort: 'id,asc',
  statusId: SoftDeleteStatus.ACTIVE,
} as const;

export interface ApplicationSecurityOptions {
  securityLevels: SecurityCatalogItem[];
  identityProviders: SecurityResource[];
  ensSubjects: SecurityCatalogItem[];
  personalDataProcessing: SecurityResource[];
  webContexts: SecurityResource[];
  fields: Field[];
  measureTypes: SecurityResource[];
  ensRequirements: SecurityResource[];
}

export interface ApplicationSecurityLoadFailures {
  security: boolean;
  roles: boolean;
  webContexts: boolean;
  classifications: boolean;
  risks: boolean;
  measures: boolean;
  securityLevels: boolean;
  identityProviders: boolean;
  ensSubjects: boolean;
  personalDataProcessing: boolean;
  webContextOptions: boolean;
  fields: boolean;
  measureTypes: boolean;
  ensRequirements: boolean;
}

export interface ApplicationSecurityResolvedData {
  applicationId: number | null;
  appSecurityId: number | null;
  security: ApplicationSecurityOutput | null;
  rolesPage: SpringPage<ApplicationSecurityRoleOutput> | null;
  webContextsPage: SpringPage<ApplicationWebContextOutput> | null;
  classifications: ApplicationEnsClassificationOutput[];
  risksPage: SpringPage<ApplicationSecurityRiskOutput> | null;
  measuresPage: SpringPage<ApplicationSecurityMeasureOutput> | null;
  options: ApplicationSecurityOptions;
  failures: ApplicationSecurityLoadFailures;
}

interface ResolvedValue<T> {
  value: T;
  failed: boolean;
}

const INITIAL_PAGE_PARAMS = {
  page: 0,
  size: PAGINATOR_ROWS,
  sort: 'id,asc',
  statusId: SoftDeleteStatus.ACTIVE,
} as const;

const CATALOG_PAGE_SIZE = 100;

function resolveValue<T>(source: Observable<T>, fallback: T): Observable<ResolvedValue<T>> {
  return source.pipe(
    map((value) => ({ value, failed: false })),
    catchError(() => of({ value: fallback, failed: true })),
    defaultIfEmpty({ value: fallback, failed: true }),
  );
}

function loadAllPages<T>(getPage: (page: number) => Observable<SpringPage<T>>): Observable<T[]> {
  return getPage(0).pipe(
    switchMap((firstPage) => {
      if (firstPage.totalPages <= 1) return of(firstPage.content);
      const remaining = Array.from({ length: firstPage.totalPages - 1 }, (_, index) =>
        getPage(index + 1),
      );
      return forkJoin(remaining).pipe(
        map((pages) => [firstPage, ...pages].flatMap((page) => page.content)),
      );
    }),
  );
}

export const applicationSecurityResolver: ResolveFn<ApplicationSecurityResolvedData> = (route) => {
  const parentData = route.parent?.data[APPLICATION_DETAIL_RESOLVE_KEY] as
    ApplicationDetailResolvedData | undefined;
  const application = parentData?.application ?? null;
  const applicationId = application?.id ?? null;
  const appSecurityId = application?.appSecurityId ?? null;

  const securityService = inject(ApplicationSecurityService);
  const rolesService = inject(ApplicationSecurityRolesService);
  const appWebContextsService = inject(ApplicationWebContextsService);
  const classificationsService = inject(ApplicationEnsClassificationsService);
  const risksService = inject(ApplicationSecurityRisksService);
  const measuresService = inject(ApplicationSecurityMeasuresService);
  const securityLevelsService = inject(SecurityLevelsService);
  const identityProvidersService = inject(IdentityProvidersService);
  const ensSubjectsService = inject(EnsSubjectsService);
  const personalDataProcessingService = inject(PersonalDataProcessingService);
  const webContextsService = inject(WebContextsService);
  const fieldsService = inject(FieldsService);
  const measureTypesService = inject(SecurityMeasureTypesService);
  const ensRequirementsService = inject(EnsRequirementsService);
  const applicationsService = inject(ApplicationsService);

  const catalogParams = {
    size: CATALOG_PAGE_SIZE,
    sort: 'id,asc',
    statusId: SoftDeleteStatus.ACTIVE,
  } as const;

  const resolveForAnchor = (
    resolvedAppSecurityId: number | null,
  ): Observable<ApplicationSecurityResolvedData> => {
    const pageParams =
      resolvedAppSecurityId == null
        ? null
        : { appSecurityId: resolvedAppSecurityId, ...INITIAL_PAGE_PARAMS };

    return forkJoin({
      security: resolveValue(
        resolvedAppSecurityId == null ? of(null) : securityService.getById(resolvedAppSecurityId),
        null,
      ),
      roles: resolveValue(pageParams == null ? of(null) : rolesService.getPage(pageParams), null),
      webContexts: resolveValue(
        pageParams == null ? of(null) : appWebContextsService.getPage(pageParams),
        null,
      ),
      classifications: resolveValue(
        resolvedAppSecurityId == null
          ? of([])
          : classificationsService
              .getPage({
                appSecurityId: resolvedAppSecurityId,
                ...APPLICATION_ENS_CLASSIFICATION_PARAMS,
              })
              .pipe(map((page) => page.content)),
        [],
      ),
      risks: resolveValue(pageParams == null ? of(null) : risksService.getPage(pageParams), null),
      measures: resolveValue(
        pageParams == null ? of(null) : measuresService.getPage(pageParams),
        null,
      ),
      securityLevels: resolveValue(
        securityLevelsService.getAll(),
        [],
      ),
      identityProviders: resolveValue(
        loadAllPages((page) => identityProvidersService.getAll({ ...catalogParams, page })),
        [],
      ),
      ensSubjects: resolveValue(
        ensSubjectsService.getAll(),
        [],
      ),
      personalDataProcessing: resolveValue(
        loadAllPages((page) => personalDataProcessingService.getAll({ ...catalogParams, page })),
        [],
      ),
      webContextOptions: resolveValue(
        loadAllPages((page) => webContextsService.getAll({ ...catalogParams, page })),
        [],
      ),
      fields: resolveValue(
        loadAllPages((page) =>
          fieldsService.getAll({ page, size: CATALOG_PAGE_SIZE, sort: 'id,asc' }).pipe(
            map((result) => ({
              ...result,
              content: result.content.filter((field) => field.deletedAt == null),
            })),
          ),
        ),
        [],
      ),
      measureTypes: resolveValue(
        loadAllPages((page) => measureTypesService.getAll({ ...catalogParams, page })),
        [],
      ),
      ensRequirements: resolveValue(
        loadAllPages((page) => ensRequirementsService.getAll({ ...catalogParams, page })),
        [],
      ),
    }).pipe(
      map((result) => ({
        applicationId,
        appSecurityId: resolvedAppSecurityId,
        security: result.security.value,
        rolesPage: result.roles.value,
        webContextsPage: result.webContexts.value,
        classifications: result.classifications.value,
        risksPage: result.risks.value,
        measuresPage: result.measures.value,
        options: {
          securityLevels: result.securityLevels.value,
          identityProviders: result.identityProviders.value,
          ensSubjects: result.ensSubjects.value,
          personalDataProcessing: result.personalDataProcessing.value,
          webContexts: result.webContextOptions.value,
          fields: result.fields.value,
          measureTypes: result.measureTypes.value,
          ensRequirements: result.ensRequirements.value,
        },
        failures: {
          security: result.security.failed,
          roles: result.roles.failed,
          webContexts: result.webContexts.failed,
          classifications: result.classifications.failed,
          risks: result.risks.failed,
          measures: result.measures.failed,
          securityLevels: result.securityLevels.failed,
          identityProviders: result.identityProviders.failed,
          ensSubjects: result.ensSubjects.failed,
          personalDataProcessing: result.personalDataProcessing.failed,
          webContextOptions: result.webContextOptions.failed,
          fields: result.fields.failed,
          measureTypes: result.measureTypes.failed,
          ensRequirements: result.ensRequirements.failed,
        },
      })),
    );
  };

  if (appSecurityId != null || applicationId == null) {
    return resolveForAnchor(appSecurityId);
  }

  return applicationsService.refreshById(applicationId).pipe(
    map((refreshed) => refreshed.appSecurityId ?? null),
    catchError(() => of(null)),
    switchMap((refreshedAppSecurityId) => resolveForAnchor(refreshedAppSecurityId)),
  );
};
