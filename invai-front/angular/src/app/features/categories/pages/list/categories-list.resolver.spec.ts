import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { SpringPage } from '@models/page.model';
import { Observable, firstValueFrom, of, throwError } from 'rxjs';

import { MAINTENANCES_ROUTES } from '@features/maintenances/maintenances.routes';
import { MAINTENANCES_ROUTES_LOC } from '@features/maintenances/maintenances.routes.i18n';
import { Category } from '../../categories.model';
import { CategoriesService } from '../../services/categories.service';
import {
  CATEGORIES_LIST_RESOLVE_KEY,
  CategoriesListResolvedData,
  categoriesListResolver,
} from './categories-list.resolver';

const CATEGORY: Category = {
  id: 1,
  name: 'DRASSANA',
  nameEs: 'ASTILLERO',
  deletedAt: null,
};

describe('categoriesListResolver', () => {
  let getAll: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    getAll = vi.fn(() => of(page([CATEGORY])));
    TestBed.configureTestingModule({
      providers: [{ provide: CategoriesService, useValue: { getAll } }],
    });
  });

  it('should resolve the first category page', async () => {
    const result = await resolveList();

    expect(getAll).toHaveBeenCalledWith({ page: 0, size: 10 });
    expect(result).toEqual({ page: page([CATEGORY]), pageLoadFailed: false });
  });

  it('should complete the route with a degraded state when loading fails', async () => {
    getAll.mockReturnValueOnce(throwError(() => new Error('Categories unavailable')));

    await expect(resolveList()).resolves.toEqual({
      page: null,
      pageLoadFailed: true,
    });
  });

  it('should be registered on the general maintenance route', () => {
    const route = MAINTENANCES_ROUTES[0]?.children?.find(
      ({ path }) => path === MAINTENANCES_ROUTES_LOC.GENERAL,
    );

    expect(route?.resolve?.[CATEGORIES_LIST_RESOLVE_KEY]).toBe(categoriesListResolver);
  });

  function resolveList(): Promise<CategoriesListResolvedData> {
    const result = TestBed.runInInjectionContext(() =>
      categoriesListResolver({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot),
    );

    return firstValueFrom(result as Observable<CategoriesListResolvedData>);
  }
});

function page<TItem>(content: TItem[]): SpringPage<TItem> {
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
  };
}
