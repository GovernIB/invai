import { inject } from '@angular/core';
import { ResolveFn } from '@angular/router';
import { SpringPage } from '@models/page.model';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';
import { catchError, map, of } from 'rxjs';

import { Role } from '../../roles.model';
import { RolesService } from '../../services/roles.service';

export const ROLES_LIST_RESOLVE_KEY = 'rolesList';

export interface RolesListResolvedData {
  page: SpringPage<Role> | null;
  pageLoadFailed: boolean;
}

export const rolesListResolver: ResolveFn<RolesListResolvedData> = () =>
  inject(RolesService)
    .getAll({ page: 0, size: 10, statusId: SoftDeleteStatus.ACTIVE })
    .pipe(
      map((page) => ({ page, pageLoadFailed: false })),
      catchError(() => of({ page: null, pageLoadFailed: true })),
    );
