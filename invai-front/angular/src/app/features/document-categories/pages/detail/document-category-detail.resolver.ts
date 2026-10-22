import { inject } from '@angular/core';
import { ResolveFn } from '@angular/router';
import { catchError, forkJoin, map, of } from 'rxjs';

import { DocumentCategory, DocumentCategoryRolePermissions } from '../../document-categories.model';
import { DocumentCategoriesService } from '../../services/document-categories.service';
import { DocumentCategoryPermissionsService } from '../../services/document-category-permissions.service';

export const DOCUMENT_CATEGORY_DETAIL_RESOLVE_KEY = 'documentCategoryDetail';

export interface DocumentCategoryDetailResolvedData {
  // null when the category could not be read, or the ID is not one of a category.
  category: DocumentCategory | null;
  permissions: DocumentCategoryRolePermissions[];
  loadFailed: boolean;
}

const FAILED: DocumentCategoryDetailResolvedData = {
  category: null,
  permissions: [],
  loadFailed: true,
};

export const documentCategoryDetailResolver: ResolveFn<DocumentCategoryDetailResolvedData> = (
  route,
) => {
  const id = Number(route.paramMap.get('id'));
  if (!Number.isInteger(id) || id <= 0) return of(FAILED);

  return forkJoin({
    category: inject(DocumentCategoriesService).getById(id),
    permissions: inject(DocumentCategoryPermissionsService).getByCategory(id),
  }).pipe(
    // A subcategory has no detail of its own: it is consulted from its category.
    map(({ category, permissions }) =>
      category.parentId == null ? { category, permissions, loadFailed: false } : FAILED,
    ),
    catchError(() => of(FAILED)),
  );
};
