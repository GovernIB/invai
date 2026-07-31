import { Injectable, inject } from '@angular/core';
import { SpringPage } from '@models/page.model';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';
import { EMPTY, Observable, expand, map, reduce } from 'rxjs';

import { Technology, TechnologyCatalogOption } from '../technologies.model';
import { TechnologiesService } from './technologies.service';

const CATALOG_PAGE_SIZE = 100;

@Injectable({ providedIn: 'root' })
export class TechnologyCatalogService {
  private readonly technologiesService = inject(TechnologiesService);

  getActiveOptions(): Observable<TechnologyCatalogOption[]> {
    return this.getAllPages(0).pipe(
      map((technologies) =>
        technologies
          .map((technology) => ({
            id: technology.id,
            label: technology.name?.trim() || `#${technology.id}`,
            layerId: technology.layer.id,
            layerLabel:
              technology.layer.name?.trim() || `#${technology.layer.id}`,
          }))
          .sort((left, right) => left.label.localeCompare(right.label)),
      ),
    );
  }

  private getAllPages(page: number): Observable<Technology[]> {
    return this.loadPage(page).pipe(
      expand((result) => (result.last ? EMPTY : this.loadPage(result.number + 1))),
      reduce(
        (items, result) => [...items, ...result.content],
        [] as Technology[],
      ),
    );
  }

  private loadPage(page: number): Observable<SpringPage<Technology>> {
    return this.technologiesService.getAll({
      page,
      size: CATALOG_PAGE_SIZE,
      sort: 'name,asc',
      statusId: SoftDeleteStatus.ACTIVE,
    });
  }
}
