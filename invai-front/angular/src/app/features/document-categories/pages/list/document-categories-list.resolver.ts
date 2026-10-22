import { inject } from '@angular/core';
import { ResolveFn } from '@angular/router';
import { SpringPage } from '@models/page.model';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';
import { catchError, map, of } from 'rxjs';

import { DocumentCategory } from '../../document-categories.model';
import { DocumentCategoriesService } from '../../services/document-categories.service';

export const DOCUMENT_CATEGORIES_LIST_RESOLVE_KEY = 'documentCategoriesList';

export interface DocumentCategoriesListResolvedData {
  page: SpringPage<DocumentCategory> | null;
  pageLoadFailed: boolean;
}

export const documentCategoriesListResolver: ResolveFn<DocumentCategoriesListResolvedData> = () =>
  inject(DocumentCategoriesService)
    .getAll({ page: 0, size: 10, statusId: SoftDeleteStatus.ACTIVE })
    .pipe(
      map((page) => ({ page, pageLoadFailed: false })),
      catchError(() => of({ page: null, pageLoadFailed: true })),
    );
