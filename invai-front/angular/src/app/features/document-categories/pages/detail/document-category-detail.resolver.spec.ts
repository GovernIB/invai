import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, convertToParamMap, RouterStateSnapshot } from '@angular/router';
import { firstValueFrom, Observable, of, throwError } from 'rxjs';

import { documentCategory } from '../../document-categories.testing';
import { DocumentCategoriesService } from '../../services/document-categories.service';
import { DocumentCategoryPermissionsService } from '../../services/document-category-permissions.service';
import {
  DocumentCategoryDetailResolvedData,
  documentCategoryDetailResolver,
} from './document-category-detail.resolver';

const PERMISSIONS = [
  { roleId: 4, roleName: 'Consulta', read: true, upload: false, modify: false, delete: false },
];
const FAILED = { category: null, permissions: [], loadFailed: true };

describe('documentCategoryDetailResolver', () => {
  const getById = vi.fn();
  const getByCategory = vi.fn();

  beforeEach(() => {
    getById.mockReset();
    getByCategory.mockReset().mockReturnValue(of(PERMISSIONS));
    TestBed.configureTestingModule({
      providers: [
        { provide: DocumentCategoriesService, useValue: { getById } },
        { provide: DocumentCategoryPermissionsService, useValue: { getByCategory } },
      ],
    });
  });

  it('loads the category of the route with its permissions', async () => {
    const category = documentCategory({ id: 7 });
    getById.mockReturnValue(of(category));

    await expect(resolve('7')).resolves.toEqual({
      category,
      permissions: PERMISSIONS,
      loadFailed: false,
    });
    expect(getById).toHaveBeenCalledWith(7);
    expect(getByCategory).toHaveBeenCalledWith(7);
  });

  it('degrades an invalid ID, a subcategory or a failed load', async () => {
    await expect(resolve('abc')).resolves.toEqual(FAILED);
    expect(getById).not.toHaveBeenCalled();

    getById.mockReturnValueOnce(of(documentCategory({ id: 11, parentId: 1 })));
    await expect(resolve('11')).resolves.toEqual(FAILED);

    getById.mockReturnValueOnce(throwError(() => new Error('offline')));
    await expect(resolve('7')).resolves.toEqual(FAILED);
  });

  function resolve(id: string) {
    return firstValueFrom(
      TestBed.runInInjectionContext(
        () =>
          documentCategoryDetailResolver(
            { paramMap: convertToParamMap({ id }) } as ActivatedRouteSnapshot,
            {} as RouterStateSnapshot,
          ) as Observable<DocumentCategoryDetailResolvedData>,
      ),
    );
  }
});
