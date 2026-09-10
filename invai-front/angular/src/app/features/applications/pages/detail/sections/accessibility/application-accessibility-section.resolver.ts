import { ApplicationAccessibilityOutput } from '../../../../applications.model';
import { ApplicationsService } from '../../../../services/applications.service';
import { ApplicationAccessibilityService } from '../../../../services/application-accessibility.service';
import { HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { ResolveFn } from '@angular/router';
import { AccessibilityResource } from '@features/maintenances/accessibility/accessibility.model';
import {
  AccessibilityResourceService,
  ClassificationSegmentsService,
  ComplianceSituationsService,
} from '@features/maintenances/accessibility/services/accessibility-resource.services';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';
import { catchError, defaultIfEmpty, forkJoin, map, Observable, of, switchMap } from 'rxjs';

export const APPLICATION_ACCESSIBILITY_RESOLVE_KEY = 'applicationAccessibility';

export interface AccessibilityCatalogResult {
  items: AccessibilityResource[];
  failed: boolean;
  forbidden: boolean;
}

export interface ApplicationAccessibilityResolvedData {
  accessibility: ApplicationAccessibilityLoadResult;
  classification: AccessibilityCatalogResult;
  compliance: AccessibilityCatalogResult;
}

export function loadAccessibilityCatalog(
  service: AccessibilityResourceService,
): Observable<AccessibilityCatalogResult> {
  const getPage = (page: number) =>
    service.getAll({
      page,
      size: 100,
      sort: 'id,asc',
      statusId: SoftDeleteStatus.ACTIVE,
    });
  return getPage(0).pipe(
    switchMap((first) => {
      if (first.totalPages <= 1) return of(first.content);
      return forkJoin(
        Array.from({ length: first.totalPages - 1 }, (_, index) => getPage(index + 1)),
      ).pipe(map((pages) => [first, ...pages].flatMap((page) => page.content)));
    }),
    map((items) => ({ items, failed: false, forbidden: false })),
    defaultIfEmpty({ items: [], failed: true, forbidden: false }),
    catchError((error: unknown) =>
      of({
        items: [],
        failed: true,
        forbidden: error instanceof HttpErrorResponse && error.status === 403,
      }),
    ),
  );
}

export type AccessibilityLoadStatus =
  'loaded' | 'absent' | 'unavailable' | 'failed' | 'forbidden' | 'deleted';
export interface ApplicationAccessibilityLoadResult {
  applicationId: number | null;
  appAccessibilityId: number | null;
  record: ApplicationAccessibilityOutput | null;
  status: AccessibilityLoadStatus;
}

export function loadApplicationAccessibility(
  applicationId: number,
  applications: ApplicationsService,
  accessibility: ApplicationAccessibilityService,
  refresh = false,
): Observable<ApplicationAccessibilityLoadResult> {
  const empty: ApplicationAccessibilityLoadResult = {
    applicationId,
    appAccessibilityId: null,
    record: null,
    status: 'unavailable',
  };
  if (!Number.isInteger(applicationId) || applicationId <= 0) return of(empty);
  const detail = refresh
    ? applications.refreshById(applicationId)
    : applications.getById(applicationId);
  return detail.pipe(
    switchMap((response): Observable<ApplicationAccessibilityLoadResult> => {
      if (!response || response.id !== applicationId) return of(empty);
      const id = response.appAccessibilityId;
      if (id === null) return of({ ...empty, status: 'absent' });
      if (!Number.isInteger(id) || Number(id) <= 0) return of(empty);
      const record$ = refresh ? accessibility.refreshById(id!) : accessibility.getById(id!);
      return record$.pipe(
        map((record): ApplicationAccessibilityLoadResult => {
          if (!record || record.id !== id || record.application?.id !== applicationId) {
            return { ...empty, appAccessibilityId: id!, status: 'failed' };
          }
          return {
            applicationId,
            appAccessibilityId: id!,
            record,
            status: record.deletedAt ? 'deleted' : 'loaded',
          };
        }),
      );
    }),
    defaultIfEmpty({ ...empty, status: 'failed' as const }),
    catchError((error: unknown) =>
      of({
        ...empty,
        status:
          error instanceof HttpErrorResponse && error.status === 403
            ? ('forbidden' as const)
            : ('failed' as const),
      }),
    ),
  );
}

export const applicationAccessibilityResolver: ResolveFn<ApplicationAccessibilityResolvedData> = (
  route,
) => {
  const applicationId = Number(route.parent?.paramMap.get('id'));
  return forkJoin({
    accessibility: loadApplicationAccessibility(
      applicationId,
      inject(ApplicationsService),
      inject(ApplicationAccessibilityService),
    ),
    classification: loadAccessibilityCatalog(inject(ClassificationSegmentsService)),
    compliance: loadAccessibilityCatalog(inject(ComplianceSituationsService)),
  });
};
