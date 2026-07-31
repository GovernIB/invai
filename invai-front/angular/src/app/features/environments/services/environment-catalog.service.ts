import { Injectable, LOCALE_ID, inject } from '@angular/core';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';
import { SpringPage } from '@models/page.model';
import { localizedName } from '@shared/utils/localized-name.utils';
import { EMPTY, Observable, expand, map, reduce } from 'rxjs';

import { Environment, EnvironmentCatalogOption } from '../environments.model';
import { EnvironmentsService } from './environments.service';

const CATALOG_PAGE_SIZE = 100;

@Injectable({ providedIn: 'root' })
export class EnvironmentCatalogService {
  private readonly locale = inject(LOCALE_ID);
  private readonly environmentsService = inject(EnvironmentsService);

  getActiveOptions(): Observable<EnvironmentCatalogOption[]> {
    return this.getAllPages(0).pipe(
      map((environments) =>
        environments
          .map((environment) => ({
            id: environment.id,
            code: environment.code?.trim() || `#${environment.id}`,
            label: localizedName(
              environment,
              this.locale,
              environment.code || `#${environment.id}`,
            ),
          }))
          .sort((left, right) =>
            left.label.localeCompare(right.label, this.locale, { sensitivity: 'base' }),
          ),
      ),
    );
  }

  private getAllPages(page: number): Observable<Environment[]> {
    return this.loadPage(page).pipe(
      expand((result) => (result.last ? EMPTY : this.loadPage(result.number + 1))),
      reduce((items, result) => [...items, ...result.content], [] as Environment[]),
    );
  }

  private loadPage(page: number): Observable<SpringPage<Environment>> {
    return this.environmentsService.getAll({
      page,
      size: CATALOG_PAGE_SIZE,
      sort: 'name,asc',
      statusId: SoftDeleteStatus.ACTIVE,
    });
  }
}
