import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { MAINTENANCES_ROUTES } from '@features/maintenances/maintenances.routes';
import { SYSTEMS_ROUTES_LOC } from '@features/systems/systems.routes.i18n';
import { SpringPage } from '@models/page.model';
import { Observable, firstValueFrom, of, throwError } from 'rxjs';

import { Environment, EnvironmentStatus } from '../../environments.model';
import { EnvironmentsService } from '../../services/environments.service';
import {
  ENVIRONMENTS_LIST_RESOLVE_KEY,
  EnvironmentsListResolvedData,
  environmentsListResolver,
} from './environments-list.resolver';

const ENVIRONMENT: Environment = {
  id: 1,
  code: 'DEV',
  name: 'Desenvolupament',
  nameEs: 'Desarrollo',
  deletedAt: null,
};

describe('environmentsListResolver', () => {
  let getAll: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    getAll = vi.fn(() => of(page([ENVIRONMENT])));
    TestBed.configureTestingModule({
      providers: [{ provide: EnvironmentsService, useValue: { getAll } }],
    });
  });

  it('resolves the first active environments page', async () => {
    const result = await resolveList();

    expect(getAll).toHaveBeenCalledWith({
      page: 0,
      size: 10,
      statusId: EnvironmentStatus.ACTIVE,
    });
    expect(result).toEqual({ page: page([ENVIRONMENT]), pageLoadFailed: false });
  });

  it('completes the route with a degraded state when loading fails', async () => {
    getAll.mockReturnValueOnce(throwError(() => new Error('Environments unavailable')));

    await expect(resolveList()).resolves.toEqual({
      page: null,
      pageLoadFailed: true,
    });
  });

  it('is registered on the grouped systems route', () => {
    const route = MAINTENANCES_ROUTES[0].children?.find(
      ({ path }) => path === SYSTEMS_ROUTES_LOC.BASE,
    );

    expect(route?.resolve?.[ENVIRONMENTS_LIST_RESOLVE_KEY]).toBe(environmentsListResolver);
  });

  function resolveList(): Promise<EnvironmentsListResolvedData> {
    const result = TestBed.runInInjectionContext(() =>
      environmentsListResolver({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot),
    );

    return firstValueFrom(result as Observable<EnvironmentsListResolvedData>);
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
