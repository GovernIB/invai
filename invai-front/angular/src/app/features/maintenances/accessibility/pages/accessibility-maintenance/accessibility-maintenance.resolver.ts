import { HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { ResolveFn } from '@angular/router';
import { SpringPage } from '@models/page.model';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';
import { catchError, defaultIfEmpty, forkJoin, map, Observable, of } from 'rxjs';
import { AccessibilityResource, AccessibilityResourceKey } from '../../accessibility.model';
import {
  ClassificationSegmentsService,
  ComplianceSituationsService,
} from '../../services/accessibility-resource.services';

export const ACCESSIBILITY_MAINTENANCE_RESOLVE_KEY = 'accessibilityMaintenance';
export interface AccessibilityResolvedResource {
  page: SpringPage<AccessibilityResource> | null;
  loadFailed: boolean;
  forbidden: boolean;
}
export interface AccessibilityMaintenanceResolvedData {
  resources: Record<AccessibilityResourceKey, AccessibilityResolvedResource>;
}
export const ACCESSIBILITY_INITIAL_PARAMS = {
  page: 0,
  size: 10,
  sort: 'id,asc',
  statusId: SoftDeleteStatus.ACTIVE,
} as const;

function result(
  source: Observable<SpringPage<AccessibilityResource>>,
): Observable<AccessibilityResolvedResource> {
  return source.pipe(
    map((page) => ({ page, loadFailed: false, forbidden: false })),
    defaultIfEmpty({ page: null, loadFailed: true, forbidden: false }),
    catchError((error: unknown) =>
      of({
        page: null,
        loadFailed: true,
        forbidden: error instanceof HttpErrorResponse && error.status === 403,
      }),
    ),
  );
}

export const accessibilityMaintenanceResolver: ResolveFn<
  AccessibilityMaintenanceResolvedData
> = () =>
  forkJoin({
    'classification-segment': result(
      inject(ClassificationSegmentsService).getAll(ACCESSIBILITY_INITIAL_PARAMS),
    ),
    'compliance-situation': result(
      inject(ComplianceSituationsService).getAll(ACCESSIBILITY_INITIAL_PARAMS),
    ),
  }).pipe(map((resources) => ({ resources })));
