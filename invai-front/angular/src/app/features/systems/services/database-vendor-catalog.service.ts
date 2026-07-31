import { Injectable, inject } from '@angular/core';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';
import { SpringPage } from '@models/page.model';
import { EMPTY, Observable, expand, map, reduce } from 'rxjs';

import {
  DatabaseVendor,
  DatabaseVendorCatalogOption,
} from '../systems.model';
import { DatabaseVendorsService } from './database-vendors.service';

const CATALOG_PAGE_SIZE = 100;

@Injectable({ providedIn: 'root' })
export class DatabaseVendorCatalogService {
  private readonly vendorsService = inject(DatabaseVendorsService);

  getActiveOptions(): Observable<DatabaseVendorCatalogOption[]> {
    return this.getAllPages(0).pipe(
      map((vendors) =>
        vendors
          .map(({ id, name, defaultPort }) => ({ id, name, defaultPort }))
          .sort((left, right) => left.name.localeCompare(right.name)),
      ),
    );
  }

  private getAllPages(page: number): Observable<DatabaseVendor[]> {
    return this.loadPage(page).pipe(
      expand((result) =>
        result.last ? EMPTY : this.loadPage(result.number + 1),
      ),
      reduce(
        (items, result) => [...items, ...result.content],
        [] as DatabaseVendor[],
      ),
    );
  }

  private loadPage(page: number): Observable<SpringPage<DatabaseVendor>> {
    return this.vendorsService.getAll({
      page,
      size: CATALOG_PAGE_SIZE,
      sort: 'name,asc',
      statusId: SoftDeleteStatus.ACTIVE,
    });
  }
}
