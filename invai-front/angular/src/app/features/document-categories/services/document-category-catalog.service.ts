import { Injectable, inject } from '@angular/core';
import { SpringPage } from '@models/page.model';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';
import { EMPTY, Observable, expand, map, reduce } from 'rxjs';

import {
  DocumentCategory,
  DocumentCategoryOption,
  DocumentCategoryTreeOption,
} from '../document-categories.model';
import { DocumentCategoriesService } from './document-categories.service';

const CATALOG_PAGE_SIZE = 100;

// Active categories a subcategory can belong to, read through every page of the listing.
@Injectable({ providedIn: 'root' })
export class DocumentCategoryCatalogService {
  private readonly categoriesService = inject(DocumentCategoriesService);

  getActiveCategoryOptions(): Observable<DocumentCategoryOption[]> {
    return this.getAllPages().pipe(
      map((categories) => categories.map(toOption).sort(byLabel)),
    );
  }

  // Active categories with their active subcategories, both sorted by name.
  getActiveCategoryTree(): Observable<DocumentCategoryTreeOption[]> {
    return this.getAllPages().pipe(
      map((categories) =>
        categories
          .map((category) => ({
            ...toOption(category),
            subcategories: (category.subcategories ?? [])
              .filter(({ deletedAt }) => !deletedAt)
              .map(toOption)
              .sort(byLabel),
          }))
          .sort(byLabel),
      ),
    );
  }

  private getAllPages(): Observable<DocumentCategory[]> {
    return this.loadPage(0).pipe(
      expand((result) => (result.last ? EMPTY : this.loadPage(result.number + 1))),
      reduce((items, result) => [...items, ...result.content], [] as DocumentCategory[]),
    );
  }

  private loadPage(page: number): Observable<SpringPage<DocumentCategory>> {
    return this.categoriesService.getAll({
      page,
      size: CATALOG_PAGE_SIZE,
      sort: 'name,asc',
      statusId: SoftDeleteStatus.ACTIVE,
    });
  }
}

function toOption({ id, name }: DocumentCategory): DocumentCategoryOption {
  return { id, label: name?.trim() || `#${id}` };
}

function byLabel(left: DocumentCategoryOption, right: DocumentCategoryOption): number {
  return left.label.localeCompare(right.label);
}
