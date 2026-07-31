import { Injectable, inject } from '@angular/core';
import { SpringPage } from '@models/page.model';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';
import { EMPTY, Observable, expand, map, reduce } from 'rxjs';

import { Layer, LayerOption } from '../layers.model';
import { LayersService } from './layers.service';

const CATALOG_PAGE_SIZE = 100;

@Injectable({ providedIn: 'root' })
export class LayerCatalogService {
  private readonly layersService = inject(LayersService);

  getActiveOptions(): Observable<LayerOption[]> {
    return this.getAllPages(0).pipe(
      map((layers) =>
        layers
          .map(({ id, name }) => ({ id, label: name?.trim() || `#${id}` }))
          .sort((left, right) => left.label.localeCompare(right.label)),
      ),
    );
  }

  private getAllPages(page: number): Observable<Layer[]> {
    return this.loadPage(page).pipe(
      expand((result) => (result.last ? EMPTY : this.loadPage(result.number + 1))),
      reduce((items, result) => [...items, ...result.content], [] as Layer[]),
    );
  }

  private loadPage(page: number): Observable<SpringPage<Layer>> {
    return this.layersService.getAll({
      page,
      size: CATALOG_PAGE_SIZE,
      sort: 'name,asc',
      statusId: SoftDeleteStatus.ACTIVE,
    });
  }
}
