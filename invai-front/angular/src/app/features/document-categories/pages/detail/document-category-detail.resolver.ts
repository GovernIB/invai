import { inject } from '@angular/core';
import { ResolveFn } from '@angular/router';
import { catchError, map, of, switchMap } from 'rxjs';

import { DocumentCategory, DocumentCategoryRolePermissions } from '../../document-categories.model';
import { DocumentCategoriesService } from '../../services/document-categories.service';
import { DocumentCategoryPermissionsService } from '../../services/document-category-permissions.service';

export const DOCUMENT_CATEGORY_DETAIL_RESOLVE_KEY = 'documentCategoryDetail';

export interface DocumentCategoryDetailResolvedData {
  // null when the category could not be read, or the ID is not one of a category.
  category: DocumentCategory | null;
  // Empty for an inactive category: the back-end only gives the permissions of active ones.
  permissions: DocumentCategoryRolePermissions[];
  loadFailed: boolean;
}

const FAILED: DocumentCategoryDetailResolvedData = {
  category: null,
  permissions: [],
  loadFailed: true,
};

// The permissions are read once the category is known, because the back-end refuses those of an
// inactive category or of a subcategory.
export const documentCategoryDetailResolver: ResolveFn<DocumentCategoryDetailResolvedData> = (
  route,
) => {
  const id = Number(route.paramMap.get('id'));
  if (!Number.isInteger(id) || id <= 0) return of(FAILED);

  const permissionsService = inject(DocumentCategoryPermissionsService);
  return inject(DocumentCategoriesService)
    .getById(id)
    .pipe(
      switchMap((category) => {
        // A subcategory has no detail of its own: it is consulted from its category.
        if (category.parentId != null) return of(FAILED);
        if (category.deletedAt) return of({ category, permissions: [], loadFailed: false });
        return permissionsService
          .getByCategory(id)
          .pipe(map((permissions) => ({ category, permissions, loadFailed: false })));
      }),
      catchError(() => of(FAILED)),
    );
};
