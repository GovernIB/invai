import { inject } from '@angular/core';
import { ResolveFn } from '@angular/router';
import { SpringPage } from '@models/page.model';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';
import { catchError, map, of } from 'rxjs';

import { Technology } from '../../technologies.model';
import { TechnologiesService } from '../../services/technologies.service';

export const TECHNOLOGIES_LIST_RESOLVE_KEY = 'technologiesList';

export interface TechnologiesListResolvedData {
  page: SpringPage<Technology> | null;
  pageLoadFailed: boolean;
}

export const technologiesListResolver: ResolveFn<TechnologiesListResolvedData> = () =>
  inject(TechnologiesService)
    .getAll({ page: 0, size: 10, statusId: SoftDeleteStatus.ACTIVE })
    .pipe(
      map((page) => ({ page, pageLoadFailed: false })),
      catchError(() => of({ page: null, pageLoadFailed: true })),
    );
