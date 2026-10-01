import { HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { ResolveFn } from '@angular/router';
import { readApiErrorMessage } from '@core/models/api-error.model';
import { SpringPage } from '@models/page.model';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';
import { PAGINATOR_ROWS } from '@shared/constants/table.constants';
import { catchError, defaultIfEmpty, forkJoin, map, Observable, of, switchMap } from 'rxjs';

import {
  ApplicationIntegrationConnectionOutput,
  ApplicationIntegrationConnectionPageParams,
  ApplicationIntegrationOutput,
} from '../../../../applications.model';
import { ApplicationIntegrationConnectionsService } from '../../../../services/application-integration-connections.service';
import { ApplicationIntegrationService } from '../../../../services/application-integration.service';
import { ApplicationsService } from '../../../../services/applications.service';

export const APPLICATION_INTEGRATIONS_RESOLVE_KEY = 'applicationIntegrations';

export const APPLICATION_INTEGRATION_CONNECTIONS_INITIAL_PARAMS: Omit<
  ApplicationIntegrationConnectionPageParams,
  'appIntegrationId'
> = {
  page: 0,
  size: PAGINATOR_ROWS,
  sort: 'id,asc',
  statusId: SoftDeleteStatus.ACTIVE,
};

export type ApplicationIntegrationLoadStatus =
  | 'loaded'
  | 'absent'
  | 'unavailable'
  | 'failed'
  | 'forbidden'
  | 'deleted';

export interface ApplicationIntegrationsLoadResult {
  applicationId: number | null;
  appIntegrationId: number | null;
  record: ApplicationIntegrationOutput | null;
  status: ApplicationIntegrationLoadStatus;
  // Localized backend message for the anchor read.
  errorMessage: string | null;
  connectionsPage: SpringPage<ApplicationIntegrationConnectionOutput> | null;
  connectionsLoadFailed: boolean;
  // Localized backend message, e.g. when Soffid cannot resolve the roles of a row.
  connectionsErrorMessage: string | null;
}

export interface ApplicationIntegrationsServices {
  applications: ApplicationsService;
  integrations: ApplicationIntegrationService;
  connections: ApplicationIntegrationConnectionsService;
}

type AnchorResult = Pick<ApplicationIntegrationsLoadResult, 'record' | 'status' | 'errorMessage'>;
type ConnectionsResult = Pick<
  ApplicationIntegrationsLoadResult,
  'connectionsPage' | 'connectionsLoadFailed' | 'connectionsErrorMessage'
>;

export function loadApplicationIntegrations(
  applicationId: number,
  services: ApplicationIntegrationsServices,
  refresh = false,
): Observable<ApplicationIntegrationsLoadResult> {
  const empty: ApplicationIntegrationsLoadResult = {
    applicationId,
    appIntegrationId: null,
    record: null,
    status: 'unavailable',
    errorMessage: null,
    connectionsPage: null,
    connectionsLoadFailed: false,
    connectionsErrorMessage: null,
  };
  if (!Number.isInteger(applicationId) || applicationId <= 0) return of(empty);
  const detail = refresh
    ? services.applications.refreshById(applicationId)
    : services.applications.getById(applicationId);
  return detail.pipe(
    switchMap((response): Observable<ApplicationIntegrationsLoadResult> => {
      if (!response || response.id !== applicationId) return of(empty);
      const id = response.appIntegrationId;
      // Only an explicit null means the anchor is missing; an omitted field is unexpected.
      if (id === null) return of({ ...empty, status: 'absent' });
      if (id === undefined || !Number.isInteger(id) || id <= 0) return of(empty);
      if (refresh) services.connections.clearCache();
      return forkJoin({
        anchor: loadAnchor(services.integrations, id, applicationId, refresh),
        connections: loadConnections(services.connections, id),
      }).pipe(
        map(({ anchor, connections }) => ({
          ...empty,
          appIntegrationId: id,
          ...anchor,
          ...connections,
        })),
      );
    }),
    defaultIfEmpty({ ...empty, status: 'failed' as const }),
    catchError(() => of({ ...empty, status: 'failed' as const })),
  );
}

function loadAnchor(
  integrations: ApplicationIntegrationService,
  id: number,
  applicationId: number,
  refresh: boolean,
): Observable<AnchorResult> {
  const failed: AnchorResult = { record: null, status: 'failed', errorMessage: null };
  return (refresh ? integrations.refreshById(id) : integrations.getById(id)).pipe(
    map((record): AnchorResult => {
      if (!record || record.id !== id || record.applicationId !== applicationId) return failed;
      return { record, status: record.deletedAt ? 'deleted' : 'loaded', errorMessage: null };
    }),
    defaultIfEmpty(failed),
    catchError((error: unknown) =>
      of({
        ...failed,
        status:
          error instanceof HttpErrorResponse && error.status === 403
            ? ('forbidden' as const)
            : ('failed' as const),
        errorMessage: readApiErrorMessage(error),
      }),
    ),
  );
}

function loadConnections(
  connections: ApplicationIntegrationConnectionsService,
  appIntegrationId: number,
): Observable<ConnectionsResult> {
  const failed = (errorMessage: string | null): ConnectionsResult => ({
    connectionsPage: null,
    connectionsLoadFailed: true,
    connectionsErrorMessage: errorMessage,
  });
  return connections
    .getPage({ appIntegrationId, ...APPLICATION_INTEGRATION_CONNECTIONS_INITIAL_PARAMS })
    .pipe(
      map(
        (connectionsPage): ConnectionsResult => ({
          connectionsPage,
          connectionsLoadFailed: false,
          connectionsErrorMessage: null,
        }),
      ),
      defaultIfEmpty(failed(null)),
      catchError((error: unknown) => of(failed(readApiErrorMessage(error)))),
    );
}

export const applicationIntegrationsResolver: ResolveFn<ApplicationIntegrationsLoadResult> = (
  route,
) =>
  loadApplicationIntegrations(Number(route.parent?.paramMap.get('id')), {
    applications: inject(ApplicationsService),
    integrations: inject(ApplicationIntegrationService),
    connections: inject(ApplicationIntegrationConnectionsService),
  });
