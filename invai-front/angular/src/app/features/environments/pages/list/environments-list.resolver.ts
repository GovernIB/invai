import { inject } from '@angular/core';
import { ResolveFn } from '@angular/router';
import { SpringPage } from '@models/page.model';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';
import { catchError, map, of } from 'rxjs';

import { Environment } from '../../environments.model';
import { EnvironmentsService } from '../../services/environments.service';

export const ENVIRONMENTS_LIST_RESOLVE_KEY = 'environmentsList';

export interface EnvironmentsListResolvedData {
  page: SpringPage<Environment> | null;
  pageLoadFailed: boolean;
}

export const environmentsListResolver: ResolveFn<EnvironmentsListResolvedData> = () =>
  inject(EnvironmentsService)
    .getAll({ page: 0, size: 10, statusId: SoftDeleteStatus.ACTIVE })
    .pipe(
      map((page) => ({ page, pageLoadFailed: false })),
      catchError(() => of({ page: null, pageLoadFailed: true })),
    );
