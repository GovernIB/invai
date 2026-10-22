import { SpringPage } from '@models/page.model';

import { DocumentCategory } from './document-categories.model';

// Test fixtures shared by the document category specs.
export function documentCategory(overrides: Partial<DocumentCategory> = {}): DocumentCategory {
  return {
    id: 1,
    name: 'Tècnica',
    nameEs: 'Técnica',
    parentId: null,
    subcategories: [],
    deletedAt: null,
    ...overrides,
  };
}

export function documentCategoriesPage(
  content: DocumentCategory[],
  overrides: Partial<SpringPage<DocumentCategory>> = {},
): SpringPage<DocumentCategory> {
  return {
    content,
    empty: content.length === 0,
    first: true,
    last: true,
    number: 0,
    numberOfElements: content.length,
    pageable: {
      offset: 0,
      pageNumber: 0,
      pageSize: 10,
      paged: true,
      sort: { empty: true, sorted: false, unsorted: true },
      unpaged: false,
    },
    size: 10,
    sort: { empty: true, sorted: false, unsorted: true },
    totalElements: content.length,
    totalPages: 1,
    ...overrides,
  };
}
