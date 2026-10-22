import { TestBed } from '@angular/core/testing';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';
import { firstValueFrom, of } from 'rxjs';

import { documentCategoriesPage, documentCategory } from '../document-categories.testing';
import { DocumentCategoriesService } from './document-categories.service';
import { DocumentCategoryCatalogService } from './document-category-catalog.service';

describe('DocumentCategoryCatalogService', () => {
  it('reads every page of active categories as sorted options', async () => {
    const getAll = vi.fn((params: { page: number }) =>
      of(
        params.page === 0
          ? documentCategoriesPage([documentCategory({ id: 2, name: 'Seguretat' })], {
              last: false,
              number: 0,
            })
          : documentCategoriesPage([documentCategory({ id: 1, name: '  ' })], { number: 1 }),
      ),
    );
    TestBed.configureTestingModule({
      providers: [{ provide: DocumentCategoriesService, useValue: { getAll } }],
    });

    const options = await firstValueFrom(
      TestBed.inject(DocumentCategoryCatalogService).getActiveCategoryOptions(),
    );

    expect(options).toEqual([
      { id: 1, label: '#1' },
      { id: 2, label: 'Seguretat' },
    ]);
    expect(getAll).toHaveBeenNthCalledWith(1, {
      page: 0,
      size: 100,
      sort: 'name,asc',
      statusId: SoftDeleteStatus.ACTIVE,
    });
    expect(getAll).toHaveBeenNthCalledWith(2, expect.objectContaining({ page: 1 }));
  });

  it('reads the active categories with their active subcategories, sorted by name', async () => {
    const getAll = vi.fn(() =>
      of(
        documentCategoriesPage([
          documentCategory({
            id: 2,
            name: 'Tècnica',
            subcategories: [
              documentCategory({ id: 22, name: 'Manuals', parentId: 2 }),
              documentCategory({ id: 21, name: 'API', parentId: 2 }),
              documentCategory({ id: 23, name: 'Antiga', parentId: 2, deletedAt: '2026-10-01T10:00:00' }),
            ],
          }),
          documentCategory({ id: 1, name: 'Normativa', subcategories: null }),
        ]),
      ),
    );
    TestBed.configureTestingModule({
      providers: [{ provide: DocumentCategoriesService, useValue: { getAll } }],
    });

    const tree = await firstValueFrom(TestBed.inject(DocumentCategoryCatalogService).getActiveCategoryTree());

    expect(tree).toEqual([
      { id: 1, label: 'Normativa', subcategories: [] },
      {
        id: 2,
        label: 'Tècnica',
        subcategories: [
          { id: 21, label: 'API' },
          { id: 22, label: 'Manuals' },
        ],
      },
    ]);
  });
});
