import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { MAINTENANCES_ROUTES } from '@features/maintenances/maintenances.routes';
import { MAINTENANCES_ROUTES_LOC } from '@features/maintenances/maintenances.routes.i18n';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';
import { firstValueFrom, Observable, of, throwError } from 'rxjs';

import { ExternalSystemsService } from '../../services/external-systems.service';
import {
  EXTERNAL_SYSTEMS_LIST_RESOLVE_KEY,
  ExternalSystemsListResolvedData,
  externalSystemsListResolver,
} from './external-systems-list.resolver';

describe('externalSystemsListResolver', () => {
  const getAll = vi.fn();

  beforeEach(() => {
    getAll.mockReset();
    TestBed.configureTestingModule({
      providers: [{ provide: ExternalSystemsService, useValue: { getAll } }],
    });
  });

  it('is registered on the integrations maintenance route', () => {
    const route = MAINTENANCES_ROUTES[0].children?.find(
      ({ path }) => path === MAINTENANCES_ROUTES_LOC.INTEGRATIONS,
    );
    expect(route?.resolve?.[EXTERNAL_SYSTEMS_LIST_RESOLVE_KEY]).toBe(externalSystemsListResolver);
  });

  it('loads the first page of active systems', async () => {
    const page = { content: [], totalElements: 0 };
    getAll.mockReturnValue(of(page));

    await expect(resolve()).resolves.toEqual({ page, pageLoadFailed: false });
    expect(getAll).toHaveBeenCalledExactlyOnceWith({
      page: 0,
      size: 10,
      statusId: SoftDeleteStatus.ACTIVE,
    });
  });

  it('degrades a failed load so the page can report it', async () => {
    getAll.mockReturnValue(throwError(() => new Error('offline')));
    await expect(resolve()).resolves.toEqual({ page: null, pageLoadFailed: true });
  });

  function resolve() {
    return firstValueFrom(
      TestBed.runInInjectionContext(
        () =>
          externalSystemsListResolver(
            {} as ActivatedRouteSnapshot,
            {} as RouterStateSnapshot,
          ) as Observable<ExternalSystemsListResolvedData>,
      ),
    );
  }
});
