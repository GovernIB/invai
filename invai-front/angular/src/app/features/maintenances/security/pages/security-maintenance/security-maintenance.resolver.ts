import { inject } from '@angular/core';
import { ResolveFn } from '@angular/router';
import { SpringPage } from '@models/page.model';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';
import { catchError, forkJoin, map, Observable, of } from 'rxjs';

import { SecurityResource, SecurityResourceKey } from '../../security.model';
import {
  EnsRequirementsService,
  IdentityProvidersService,
  PersonalDataProcessingService,
  SecurityMeasureTypesService,
  WebContextsService,
} from '../../services/security-resource.services';

export const SECURITY_MAINTENANCE_RESOLVE_KEY = 'securityMaintenance';

export interface SecurityResolvedResource {
  page: SpringPage<SecurityResource> | null;
  loadFailed: boolean;
}

export interface SecurityMaintenanceResolvedData {
  resources: Record<SecurityResourceKey, SecurityResolvedResource>;
}

const INITIAL_PARAMS = {
  page: 0,
  size: 10,
  sort: 'id,asc',
  statusId: SoftDeleteStatus.ACTIVE,
} as const;

function result(source: Observable<SpringPage<SecurityResource>>) {
  return source.pipe(
    map((page) => ({ page, loadFailed: false })),
    catchError(() => of({ page: null, loadFailed: true })),
  );
}

export const securityMaintenanceResolver: ResolveFn<SecurityMaintenanceResolvedData> = () =>
  forkJoin({
    'ens-requirement': result(inject(EnsRequirementsService).getAll(INITIAL_PARAMS)),
    'identity-provider': result(inject(IdentityProvidersService).getAll(INITIAL_PARAMS)),
    'personal-data-processing': result(
      inject(PersonalDataProcessingService).getAll(INITIAL_PARAMS),
    ),
    'security-measure-type': result(inject(SecurityMeasureTypesService).getAll(INITIAL_PARAMS)),
    'web-context': result(inject(WebContextsService).getAll(INITIAL_PARAMS)),
  }).pipe(map((resources) => ({ resources })));
