import { inject } from '@angular/core';
import { ResolveFn } from '@angular/router';
import { SpringPage } from '@models/page.model';
import { catchError, map, of } from 'rxjs';

import { Category } from '../../categories.model';
import { CategoriesService } from '../../services/categories.service';

export const CATEGORIES_LIST_RESOLVE_KEY = 'categoriesList';

export interface CategoriesListResolvedData {
  page: SpringPage<Category> | null;
  pageLoadFailed: boolean;
}

export const categoriesListResolver: ResolveFn<CategoriesListResolvedData> = () =>
  inject(CategoriesService)
    .getAll({ page: 0, size: 10 })
    .pipe(
      map((page) => ({ page, pageLoadFailed: false })),
      catchError(() => of({ page: null, pageLoadFailed: true })),
    );
