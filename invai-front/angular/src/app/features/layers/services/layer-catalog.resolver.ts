import { inject } from '@angular/core';
import { ResolveFn } from '@angular/router';
import { catchError, map, of } from 'rxjs';

import { LayerOption } from '../layers.model';
import { LayerCatalogService } from './layer-catalog.service';

export const LAYER_CATALOG_RESOLVE_KEY = 'layerCatalog';

export interface LayerCatalogResolvedData {
  options: LayerOption[];
  loadFailed: boolean;
}

export const layerCatalogResolver: ResolveFn<LayerCatalogResolvedData> = () =>
  inject(LayerCatalogService)
    .getActiveOptions()
    .pipe(
      map((options) => ({ options, loadFailed: false })),
      catchError(() => of({ options: [], loadFailed: true })),
    );
