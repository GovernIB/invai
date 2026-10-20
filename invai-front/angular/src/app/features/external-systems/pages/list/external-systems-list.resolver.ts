import { inject } from '@angular/core';
import { ResolveFn } from '@angular/router';
import { SpringPage } from '@models/page.model';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';
import { catchError, map, of } from 'rxjs';

import { ExternalSystem } from '../../external-systems.model';
import { ExternalSystemsService } from '../../services/external-systems.service';

export const EXTERNAL_SYSTEMS_LIST_RESOLVE_KEY = 'externalSystemsList';

export interface ExternalSystemsListResolvedData {
  page: SpringPage<ExternalSystem> | null;
  pageLoadFailed: boolean;
}

export const externalSystemsListResolver: ResolveFn<ExternalSystemsListResolvedData> = () =>
  inject(ExternalSystemsService)
    .getAll({ page: 0, size: 10, statusId: SoftDeleteStatus.ACTIVE })
    .pipe(
      map((page) => ({ page, pageLoadFailed: false })),
      catchError(() => of({ page: null, pageLoadFailed: true })),
    );
