import { inject } from '@angular/core';
import { ResolveFn } from '@angular/router';
import { SpringPage } from '@models/page.model';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';
import { catchError, map, of } from 'rxjs';

import { Commission } from '../../commissions.model';
import { CommissionsService } from '../../services/commissions.service';

export const COMMISSIONS_LIST_RESOLVE_KEY = 'commissionsList';

export interface CommissionsListResolvedData {
  page: SpringPage<Commission> | null;
  pageLoadFailed: boolean;
}

export const commissionsListResolver: ResolveFn<CommissionsListResolvedData> = () =>
  inject(CommissionsService)
    .getAll({ page: 0, size: 10, statusId: SoftDeleteStatus.ACTIVE })
    .pipe(
      map((page) => ({ page, pageLoadFailed: false })),
      catchError(() => of({ page: null, pageLoadFailed: true })),
    );
