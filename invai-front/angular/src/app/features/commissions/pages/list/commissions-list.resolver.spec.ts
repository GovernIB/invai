import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { SpringPage } from '@models/page.model';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';
import { Observable, firstValueFrom, of, throwError } from 'rxjs';

import { MAINTENANCES_ROUTES } from '@features/maintenances/maintenances.routes';
import { MAINTENANCES_ROUTES_LOC } from '@features/maintenances/maintenances.routes.i18n';
import { Commission, CommissionType } from '../../commissions.model';
import { CommissionsService } from '../../services/commissions.service';
import {
  COMMISSIONS_LIST_RESOLVE_KEY,
  CommissionsListResolvedData,
  commissionsListResolver,
} from './commissions-list.resolver';

const COMMISSION: Commission = {
  id: 1,
  name: 'Comissió tècnica',
  nameEs: 'Comisión técnica',
  expedientNumber: 'EXP-1',
  approvalDate: '2026-07-17',
  commissionType: CommissionType.TECNICA,
  deletedAt: null,
};

describe('commissionsListResolver', () => {
  let getAll: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    getAll = vi.fn(() => of(page([COMMISSION])));
    TestBed.configureTestingModule({
      providers: [{ provide: CommissionsService, useValue: { getAll } }],
    });
  });

  it('should resolve the first commission page', async () => {
    const result = await resolveList();

    expect(getAll).toHaveBeenCalledWith({
      page: 0,
      size: 10,
      statusId: SoftDeleteStatus.ACTIVE,
    });
    expect(result).toEqual({ page: page([COMMISSION]), pageLoadFailed: false });
  });

  it('should complete the route with a degraded state when loading fails', async () => {
    getAll.mockReturnValueOnce(throwError(() => new Error('Commissions unavailable')));

    await expect(resolveList()).resolves.toEqual({
      page: null,
      pageLoadFailed: true,
    });
  });

  it('should be registered on the general maintenance route', () => {
    const route = MAINTENANCES_ROUTES[0]?.children?.find(
      ({ path }) => path === MAINTENANCES_ROUTES_LOC.GENERAL,
    );

    expect(route?.resolve?.[COMMISSIONS_LIST_RESOLVE_KEY]).toBe(commissionsListResolver);
  });

  function resolveList(): Promise<CommissionsListResolvedData> {
    const result = TestBed.runInInjectionContext(() =>
      commissionsListResolver({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot),
    );

    return firstValueFrom(result as Observable<CommissionsListResolvedData>);
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
