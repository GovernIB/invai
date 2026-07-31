import { inject } from '@angular/core';
import { ResolveFn } from '@angular/router';
import { SpringPage } from '@models/page.model';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';
import { catchError, map, of } from 'rxjs';

import { Layer } from '../../layers.model';
import { LayersService } from '../../services/layers.service';

export const LAYERS_LIST_RESOLVE_KEY = 'layersList';

export interface LayersListResolvedData {
  page: SpringPage<Layer> | null;
  pageLoadFailed: boolean;
}

export const layersListResolver: ResolveFn<LayersListResolvedData> = () =>
  inject(LayersService)
    .getAll({ page: 0, size: 10, statusId: SoftDeleteStatus.ACTIVE })
    .pipe(
      map((page) => ({ page, pageLoadFailed: false })),
      catchError(() => of({ page: null, pageLoadFailed: true })),
    );
