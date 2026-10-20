import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { EMPTY, firstValueFrom, Observable, of, throwError } from 'rxjs';

import { APPLICATIONS_ROUTES } from '../../../../applications.routes';
import { ApplicationDataService } from '../../../../services/application-data.service';
import { ApplicationsService } from '../../../../services/applications.service';
import {
  APPLICATION_DATA_RESOLVE_KEY,
  ApplicationDataLoadResult,
  applicationDataResolver,
  loadApplicationData,
} from './application-data-section.resolver';

describe('application data resolver', () => {
  const data = { getById: vi.fn(), refreshById: vi.fn() };
  const applications = { getById: vi.fn(), refreshById: vi.fn() };
  const record = { id: 5, application: { id: 7 }, deletedAt: null, openData: [], reuse: [] };

  beforeEach(() => {
    data.getById.mockReset().mockReturnValue(of(record));
    data.refreshById.mockReset();
    applications.getById.mockReset().mockReturnValue(of({ id: 7, appDataId: 5 }));
    applications.refreshById.mockReset();
    TestBed.configureTestingModule({
      providers: [
        { provide: ApplicationDataService, useValue: data },
        { provide: ApplicationsService, useValue: applications },
      ],
    });
  });

  it('is registered on the data child route', () => {
    const route = APPLICATIONS_ROUTES.find((route) => route.path === ':id')?.children?.find(
      (route) => route.path === 'data',
    );
    expect(route?.resolve?.[APPLICATION_DATA_RESOLVE_KEY]).toBe(applicationDataResolver);
  });

  it('loads the anchor by its own ID from the cached application detail', async () => {
    const result = await resolve();

    expect(applications.getById).toHaveBeenCalledWith(7);
    expect(data.getById).toHaveBeenCalledWith(5);
    expect(applications.refreshById).not.toHaveBeenCalled();
    expect(result).toEqual({
      applicationId: 7,
      appDataId: 5,
      record,
      status: 'loaded',
      errorMessage: null,
    });
  });

  it('only treats an explicit null anchor ID as absence', async () => {
    applications.getById.mockReturnValue(of({ id: 7, appDataId: null }));
    expect((await resolve()).status).toBe('absent');
    expect(data.getById).not.toHaveBeenCalled();

    applications.getById.mockReturnValue(of({ id: 7 }));
    expect((await resolve()).status).toBe('unavailable');
  });

  it('keeps the localized backend message when a published document is unavailable', async () => {
    const message = "No s'ha pogut consultar el catàleg d'API REST externa.";
    data.getById.mockReturnValue(
      throwError(
        () =>
          new HttpErrorResponse({ status: 400, error: { error: 'Error de validació', message } }),
      ),
    );

    expect(await resolve()).toEqual({
      applicationId: 7,
      appDataId: 5,
      record: null,
      status: 'failed',
      errorMessage: message,
    });
  });

  it('keeps the localized timeout message when both published documents time out', async () => {
    const message = "L'aplicació ha trigat massa a respondre. Torneu-ho a provar d'aquí uns instants.";
    data.getById.mockReturnValue(
      throwError(() => new HttpErrorResponse({ status: 504, error: { error: 'Error', message } })),
    );

    expect(await resolve()).toEqual(
      expect.objectContaining({ appDataId: 5, status: 'failed', errorMessage: message }),
    );
  });

  it('distinguishes forbidden, deleted, empty and mismatched reads', async () => {
    data.getById.mockReturnValue(throwError(() => new HttpErrorResponse({ status: 403 })));
    expect(await resolve()).toEqual(expect.objectContaining({ status: 'forbidden', errorMessage: null }));

    data.getById.mockReturnValue(of({ ...record, deletedAt: '2026-09-01T10:00:00' }));
    expect((await resolve()).status).toBe('deleted');

    data.getById.mockReturnValue(EMPTY);
    expect((await resolve()).status).toBe('failed');

    data.getById.mockReturnValue(of({ ...record, application: { id: 8 } }));
    expect((await resolve()).status).toBe('failed');

    data.getById.mockReturnValue(of(null));
    expect((await resolve()).status).toBe('failed');
  });

  it('fails without emitting an error when the application detail cannot be read', async () => {
    applications.getById.mockReturnValue(throwError(() => new Error('offline')));
    expect(await resolve()).toEqual(
      expect.objectContaining({ appDataId: null, status: 'failed', errorMessage: null }),
    );
  });

  it('forces both reads when retrying', async () => {
    applications.refreshById.mockReturnValue(of({ id: 7, appDataId: 6 }));
    data.refreshById.mockReturnValue(of({ ...record, id: 6 }));

    const result = await firstValueFrom(
      loadApplicationData(
        7,
        TestBed.inject(ApplicationsService),
        TestBed.inject(ApplicationDataService),
        true,
      ),
    );

    expect(result.status).toBe('loaded');
    expect(applications.refreshById).toHaveBeenCalledWith(7);
    expect(data.refreshById).toHaveBeenCalledWith(6);
    expect(applications.getById).not.toHaveBeenCalled();
    expect(data.getById).not.toHaveBeenCalled();
  });

  function resolve() {
    return firstValueFrom(
      TestBed.runInInjectionContext(
        () =>
          applicationDataResolver(
            { parent: { paramMap: { get: () => '7' } } } as unknown as ActivatedRouteSnapshot,
            {} as RouterStateSnapshot,
          ) as Observable<ApplicationDataLoadResult>,
      ),
    );
  }
});
