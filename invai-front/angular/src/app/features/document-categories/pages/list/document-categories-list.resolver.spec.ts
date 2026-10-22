import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';
import { firstValueFrom, Observable, of, throwError } from 'rxjs';

import { documentCategoriesPage, documentCategory } from '../../document-categories.testing';
import { DocumentCategoriesService } from '../../services/document-categories.service';
import {
  DocumentCategoriesListResolvedData,
  documentCategoriesListResolver,
} from './document-categories-list.resolver';

describe('documentCategoriesListResolver', () => {
  const getAll = vi.fn();

  beforeEach(() => {
    getAll.mockReset();
    TestBed.configureTestingModule({
      providers: [{ provide: DocumentCategoriesService, useValue: { getAll } }],
    });
  });

  it('loads the first page of active categories', async () => {
    const page = documentCategoriesPage([documentCategory()]);
    getAll.mockReturnValue(of(page));

    await expect(resolve()).resolves.toEqual({ page, pageLoadFailed: false });
    expect(getAll).toHaveBeenCalledWith({ page: 0, size: 10, statusId: SoftDeleteStatus.ACTIVE });
  });

  it('degrades a failed load so that the list can report it', async () => {
    getAll.mockReturnValue(throwError(() => new Error('offline')));

    await expect(resolve()).resolves.toEqual({ page: null, pageLoadFailed: true });
  });

  function resolve() {
    return firstValueFrom(
      TestBed.runInInjectionContext(
        () =>
          documentCategoriesListResolver(
            {} as ActivatedRouteSnapshot,
            {} as RouterStateSnapshot,
          ) as Observable<DocumentCategoriesListResolvedData>,
      ),
    );
  }
});
