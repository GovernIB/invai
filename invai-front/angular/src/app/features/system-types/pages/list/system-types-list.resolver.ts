import { inject } from '@angular/core';
import { ResolveFn } from '@angular/router';
import { SpringPage } from '@models/page.model';
import { catchError, map, of } from 'rxjs';

import { SystemType } from '../../system-types.model';
import { SystemTypesService } from '../../services/system-types.service';

export const SYSTEM_TYPES_LIST_RESOLVE_KEY = 'systemTypesList';

export interface SystemTypesListResolvedData {
  page: SpringPage<SystemType> | null;
  pageLoadFailed: boolean;
}

export const systemTypesListResolver: ResolveFn<SystemTypesListResolvedData> = () =>
  inject(SystemTypesService)
    .getAll({ page: 0, size: 10 })
    .pipe(
      map((page) => ({ page, pageLoadFailed: false })),
      catchError(() => of({ page: null, pageLoadFailed: true })),
    );
