import { HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { ResolveFn } from '@angular/router';
import { readApiErrorMessage } from '@core/models/api-error.model';
import { catchError, defaultIfEmpty, map, Observable, of, switchMap } from 'rxjs';

import { ApplicationDataOutput } from '../../../../applications.model';
import { ApplicationDataService } from '../../../../services/application-data.service';
import { ApplicationsService } from '../../../../services/applications.service';

export const APPLICATION_DATA_RESOLVE_KEY = 'applicationData';

export type ApplicationDataLoadStatus =
  | 'loaded'
  | 'absent'
  | 'unavailable'
  | 'failed'
  | 'forbidden'
  | 'deleted';

export interface ApplicationDataLoadResult {
  applicationId: number | null;
  appDataId: number | null;
  record: ApplicationDataOutput | null;
  status: ApplicationDataLoadStatus;
  // Localized backend message, e.g. when a published document cannot be fetched.
  errorMessage: string | null;
}

export function loadApplicationData(
  applicationId: number,
  applications: ApplicationsService,
  data: ApplicationDataService,
  refresh = false,
): Observable<ApplicationDataLoadResult> {
  const empty: ApplicationDataLoadResult = {
    applicationId,
    appDataId: null,
    record: null,
    status: 'unavailable',
    errorMessage: null,
  };
  if (!Number.isInteger(applicationId) || applicationId <= 0) return of(empty);
  let appDataId: number | null = null;
  const detail = refresh
    ? applications.refreshById(applicationId)
    : applications.getById(applicationId);
  return detail.pipe(
    switchMap((response): Observable<ApplicationDataLoadResult> => {
      if (!response || response.id !== applicationId) return of(empty);
      const id = response.appDataId;
      // Only an explicit null means the anchor is missing; an omitted field is unexpected.
      if (id === null) return of({ ...empty, status: 'absent' });
      if (id === undefined || !Number.isInteger(id) || id <= 0) return of(empty);
      appDataId = id;
      const record$ = refresh ? data.refreshById(id) : data.getById(id);
      return record$.pipe(
        map((record): ApplicationDataLoadResult => {
          if (!record || record.id !== id || record.application?.id !== applicationId) {
            return { ...empty, appDataId: id, status: 'failed' };
          }
          return {
            applicationId,
            appDataId: id,
            record,
            status: record.deletedAt ? 'deleted' : 'loaded',
            errorMessage: null,
          };
        }),
      );
    }),
    defaultIfEmpty({ ...empty, status: 'failed' as const }),
    catchError((error: unknown) =>
      of({
        ...empty,
        appDataId,
        status:
          error instanceof HttpErrorResponse && error.status === 403
            ? ('forbidden' as const)
            : ('failed' as const),
        errorMessage: readApiErrorMessage(error),
      }),
    ),
  );
}

export const applicationDataResolver: ResolveFn<ApplicationDataLoadResult> = (route) =>
  loadApplicationData(
    Number(route.parent?.paramMap.get('id')),
    inject(ApplicationsService),
    inject(ApplicationDataService),
  );
