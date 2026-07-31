import { Injectable, LOCALE_ID, inject } from '@angular/core';
import { SpringPage } from '@models/page.model';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';
import { localizedName } from '@shared/utils/localized-name.utils';
import { EMPTY, Observable, expand, map, reduce } from 'rxjs';

import { Role, RoleCatalogOption } from '../roles.model';
import { RolesService } from './roles.service';

const CATALOG_PAGE_SIZE = 100;

@Injectable({ providedIn: 'root' })
export class RoleCatalogService {
  private readonly locale = inject(LOCALE_ID);
  private readonly rolesService = inject(RolesService);

  getActiveOptions(): Observable<RoleCatalogOption[]> {
    return this.getAllPages(0).pipe(
      map((roles) =>
        roles
          .map((role) => ({
            id: role.id,
            label: localizedName(role, this.locale, `#${role.id}`),
          }))
          .sort((left, right) =>
            left.label.localeCompare(right.label, this.locale, { sensitivity: 'base' }),
          ),
      ),
    );
  }

  private getAllPages(page: number): Observable<Role[]> {
    return this.loadPage(page).pipe(
      expand((result) => (result.last ? EMPTY : this.loadPage(result.number + 1))),
      reduce((items, result) => [...items, ...result.content], [] as Role[]),
    );
  }

  private loadPage(page: number): Observable<SpringPage<Role>> {
    return this.rolesService.getAll({
      page,
      size: CATALOG_PAGE_SIZE,
      sort: 'name,asc',
      statusId: SoftDeleteStatus.ACTIVE,
    });
  }
}
