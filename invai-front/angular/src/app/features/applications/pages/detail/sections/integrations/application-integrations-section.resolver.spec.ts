import { HttpErrorResponse } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { EMPTY, firstValueFrom, Observable, of, throwError } from 'rxjs';

import { APPLICATIONS_ROUTES } from '../../../../applications.routes';
import { ApplicationIntegrationConnectionsService } from '../../../../services/application-integration-connections.service';
import { ApplicationIntegrationService } from '../../../../services/application-integration.service';
import { ApplicationsService } from '../../../../services/applications.service';
import {
  APPLICATION_INTEGRATIONS_RESOLVE_KEY,
  ApplicationIntegrationsLoadResult,
  applicationIntegrationsResolver,
  loadApplicationIntegrations,
} from './application-integrations-section.resolver';

describe('application integrations resolver', () => {
  const integrations = { getById: vi.fn(), refreshById: vi.fn() };
  const connections = { getPage: vi.fn(), clearCache: vi.fn() };
  const applications = { getById: vi.fn(), refreshById: vi.fn() };
  const record = { id: 13, applicationId: 7, observation: '<p>Nota</p>', deletedAt: null };
  const page = { content: [{ id: 1 }], totalElements: 1, number: 0 };

  beforeEach(() => {
    integrations.getById.mockReset().mockReturnValue(of(record));
    integrations.refreshById.mockReset();
    connections.getPage.mockReset().mockReturnValue(of(page));
    connections.clearCache.mockReset();
    applications.getById.mockReset().mockReturnValue(of({ id: 7, appIntegrationId: 13 }));
    applications.refreshById.mockReset();
    TestBed.configureTestingModule({
      providers: [
        { provide: ApplicationIntegrationService, useValue: integrations },
        { provide: ApplicationIntegrationConnectionsService, useValue: connections },
        { provide: ApplicationsService, useValue: applications },
      ],
    });
  });

  it('is registered on the integrations child route', () => {
    const route = APPLICATIONS_ROUTES.find((route) => route.path === ':id')?.children?.find(
      (route) => route.path === 'integrations',
    );
    expect(route?.resolve?.[APPLICATION_INTEGRATIONS_RESOLVE_KEY]).toBe(
      applicationIntegrationsResolver,
    );
  });

  it('loads the anchor and the first page of active connections', async () => {
    const result = await resolve();

    expect(applications.getById).toHaveBeenCalledWith(7);
    expect(integrations.getById).toHaveBeenCalledWith(13);
    expect(connections.getPage).toHaveBeenCalledExactlyOnceWith({
      appIntegrationId: 13,
      page: 0,
      size: 10,
      sort: 'id,asc',
      statusId: 1,
    });
    expect(result).toEqual({
      applicationId: 7,
      appIntegrationId: 13,
      record,
      status: 'loaded',
      errorMessage: null,
      connectionsPage: page,
      connectionsLoadFailed: false,
      connectionsErrorMessage: null,
    });
  });

  it('only treats an explicit null anchor ID as absence and skips the connections', async () => {
    applications.getById.mockReturnValue(of({ id: 7, appIntegrationId: null }));
    expect(await resolve()).toEqual(
      expect.objectContaining({ status: 'absent', appIntegrationId: null, connectionsPage: null }),
    );
    expect(connections.getPage).not.toHaveBeenCalled();

    applications.getById.mockReturnValue(of({ id: 7 }));
    expect((await resolve()).status).toBe('unavailable');
  });

  it('keeps the anchor when Soffid fails the connections page', async () => {
    const message = "No s'ha pogut consultar els rols atorgats a aquest usuari en aquest moment.";
    connections.getPage.mockReturnValue(
      throwError(() => new HttpErrorResponse({ status: 400, error: { error: 'Error', message } })),
    );

    expect(await resolve()).toEqual(
      expect.objectContaining({
        status: 'loaded',
        record,
        connectionsPage: null,
        connectionsLoadFailed: true,
        connectionsErrorMessage: message,
      }),
    );
  });

  it('distinguishes forbidden, deleted, empty and mismatched anchors', async () => {
    integrations.getById.mockReturnValue(throwError(() => new HttpErrorResponse({ status: 403 })));
    expect((await resolve()).status).toBe('forbidden');

    integrations.getById.mockReturnValue(of({ ...record, deletedAt: '2026-09-01T10:00:00' }));
    expect((await resolve()).status).toBe('deleted');

    integrations.getById.mockReturnValue(EMPTY);
    expect((await resolve()).status).toBe('failed');

    integrations.getById.mockReturnValue(of(null));
    expect((await resolve()).status).toBe('failed');

    integrations.getById.mockReturnValue(of({ ...record, applicationId: 8 }));
    expect((await resolve()).status).toBe('failed');
  });

  it('fails without emitting an error when the application detail cannot be read', async () => {
    applications.getById.mockReturnValue(throwError(() => new Error('offline')));
    expect(await resolve()).toEqual(
      expect.objectContaining({ appIntegrationId: null, status: 'failed', errorMessage: null }),
    );
  });

  it('forces every read when retrying', async () => {
    applications.refreshById.mockReturnValue(of({ id: 7, appIntegrationId: 13 }));
    integrations.refreshById.mockReturnValue(of(record));

    const result = await firstValueFrom(
      loadApplicationIntegrations(
        7,
        {
          applications: TestBed.inject(ApplicationsService),
          integrations: TestBed.inject(ApplicationIntegrationService),
          connections: TestBed.inject(ApplicationIntegrationConnectionsService),
        },
        true,
      ),
    );

    expect(result.status).toBe('loaded');
    expect(applications.refreshById).toHaveBeenCalledWith(7);
    expect(integrations.refreshById).toHaveBeenCalledWith(13);
    expect(connections.clearCache).toHaveBeenCalledOnce();
    expect(applications.getById).not.toHaveBeenCalled();
    expect(integrations.getById).not.toHaveBeenCalled();
  });

  function resolve() {
    return firstValueFrom(
      TestBed.runInInjectionContext(
        () =>
          applicationIntegrationsResolver(
            { parent: { paramMap: { get: () => '7' } } } as unknown as ActivatedRouteSnapshot,
            {} as RouterStateSnapshot,
          ) as Observable<ApplicationIntegrationsLoadResult>,
      ),
    );
  }
});
