import { inject } from '@angular/core';
import { ResolveFn } from '@angular/router';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';
import { catchError, defaultIfEmpty, forkJoin, map, of, switchMap } from 'rxjs';

import { ApplicationOutput } from '../../applications.model';
import { ApplicationsService } from '../../services/applications.service';
import { ApplicationWebContextsService } from '../../services/application-security.service';
import { ApplicationResponsiblesService } from '../../services/application-responsibles.service';
import { hasPendingResponsibleDir3 } from '../../application-dir3.utils';

export const APPLICATION_DETAIL_RESOLVE_KEY = 'applicationDetail';

export interface ApplicationDetailResolvedData {
  application: ApplicationOutput | null;
  loadFailed: boolean;
  hasUnverifiedWebContexts: boolean | null;
  hasPendingResponsibleDir3: boolean | null;
}

export const applicationDetailResolver: ResolveFn<ApplicationDetailResolvedData> = (route) => {
  const id = Number(route.paramMap.get('id'));
  if (!Number.isInteger(id) || id <= 0) {
    return of({
      application: null,
      loadFailed: true,
      hasUnverifiedWebContexts: null,
      hasPendingResponsibleDir3: null,
    });
  }

  const contexts = inject(ApplicationWebContextsService);
  const responsibles = inject(ApplicationResponsiblesService);
  return inject(ApplicationsService)
    .getById(id)
    .pipe(
      switchMap((application) => {
        const pending =
          application.appSecurityId == null
            ? of(false)
            : contexts
                .hasUnverified(application.appSecurityId)
                .pipe(
                  catchError(() => of(null)),
                  defaultIfEmpty(null),
                );
        // Use the same cached active-assignment page as the Responsibles section.
        const pendingDir3 =
          application.appResponsibleAuthorizedId == null
            ? of(false)
            : responsibles
                .getPage({
                  appResponsibleAuthorizedId: application.appResponsibleAuthorizedId,
                  page: 0,
                  size: 1000,
                  sort: 'id,asc',
                  statusId: SoftDeleteStatus.ACTIVE,
                })
                .pipe(
                  map((page) => hasPendingResponsibleDir3(page.content)),
                  catchError(() => of(null)),
                  defaultIfEmpty(null),
                );
        return forkJoin({
          hasUnverifiedWebContexts: pending,
          hasPendingResponsibleDir3: pendingDir3,
        }).pipe(
          map((indicators) => ({
            application,
            loadFailed: false,
            ...indicators,
          })),
        );
      }),
      catchError(() =>
        of({
          application: null,
          loadFailed: true,
          hasUnverifiedWebContexts: null,
          hasPendingResponsibleDir3: null,
        }),
      ),
    );
};
