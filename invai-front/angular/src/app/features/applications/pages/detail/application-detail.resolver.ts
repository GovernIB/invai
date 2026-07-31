import { inject } from '@angular/core';
import { ResolveFn } from '@angular/router';
import { catchError, map, of } from 'rxjs';

import { ApplicationOutput } from '../../applications.model';
import { ApplicationsService } from '../../services/applications.service';

export const APPLICATION_DETAIL_RESOLVE_KEY = 'applicationDetail';

export interface ApplicationDetailResolvedData {
  application: ApplicationOutput | null;
  loadFailed: boolean;
}

export const applicationDetailResolver: ResolveFn<ApplicationDetailResolvedData> = (route) => {
  const id = Number(route.paramMap.get('id'));
  if (!Number.isInteger(id) || id <= 0) {
    return of({ application: null, loadFailed: true });
  }

  return inject(ApplicationsService)
    .getById(id)
    .pipe(
      map((application) => ({ application, loadFailed: false })),
      catchError(() => of({ application: null, loadFailed: true })),
    );
};
