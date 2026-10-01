import { Injectable, inject } from '@angular/core';
import { SpringPage } from '@models/page.model';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';
import { EMPTY, Observable, expand, map, reduce } from 'rxjs';

import { ExternalSystem, ExternalSystemCatalogOption } from '../external-systems.model';
import { ExternalSystemsService } from './external-systems.service';

const CATALOG_PAGE_SIZE = 100;

// Active external systems as select options; pages come from the cached list service.
@Injectable({ providedIn: 'root' })
export class ExternalSystemCatalogService {
  private readonly externalSystemsService = inject(ExternalSystemsService);

  getActiveOptions(): Observable<ExternalSystemCatalogOption[]> {
    return this.getAllPages().pipe(
      map((systems) =>
        systems
          .map((system) => toExternalSystemOption(system))
          .sort((left, right) => left.label.localeCompare(right.label)),
      ),
    );
  }

  private getAllPages(): Observable<ExternalSystem[]> {
    return this.loadPage(0).pipe(
      expand((result) => (result.last ? EMPTY : this.loadPage(result.number + 1))),
      reduce((items, result) => [...items, ...result.content], [] as ExternalSystem[]),
    );
  }

  private loadPage(page: number): Observable<SpringPage<ExternalSystem>> {
    return this.externalSystemsService.getAll({
      page,
      size: CATALOG_PAGE_SIZE,
      sort: 'name,asc',
      statusId: SoftDeleteStatus.ACTIVE,
    });
  }
}

export function toExternalSystemOption(system: ExternalSystem): ExternalSystemCatalogOption {
  return { id: system.id, label: system.name?.trim() || `#${system.id}` };
}
