import { inject } from '@angular/core';
import { ResolveFn } from '@angular/router';
import { catchError, defaultIfEmpty, forkJoin, map, of, switchMap } from 'rxjs';
import { PAGINATOR_ROWS } from '@shared/constants/table.constants';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';

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
        // Backend has no verification state yet: every active context is pending.
        // Reuse the section's first-page cache and use the server total across all pages.
        const pending =
          application.appSecurityId == null
            ? of(false)
            : contexts
                .getPage({
                  appSecurityId: application.appSecurityId,
                  page: 0,
                  size: PAGINATOR_ROWS,
                  sort: 'id,asc',
                  statusId: SoftDeleteStatus.ACTIVE,
                })
                .pipe(
                  map((page) => page.totalElements > 0),
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
