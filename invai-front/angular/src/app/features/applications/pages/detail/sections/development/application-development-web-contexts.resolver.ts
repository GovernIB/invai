import { inject } from '@angular/core';
import { FieldsService } from '@features/fields/services/fields.service';
import { WebContextsService } from '@features/maintenances/security/services/security-resource.services';
import { SpringPage } from '@models/page.model';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';
import { PAGINATOR_ROWS } from '@shared/constants/table.constants';
import { Observable, catchError, defaultIfEmpty, forkJoin, map, of, switchMap } from 'rxjs';
import { ApplicationWebContextOutput, SecurityCatalogItem } from '../../../../applications.model';
import { ApplicationWebContextsService } from '../../../../services/application-security.service';
import { ApplicationsService } from '../../../../services/applications.service';

export interface ApplicationDevelopmentWebContextsData {
  appSecurityId: number | null;
  page: SpringPage<ApplicationWebContextOutput> | null;
  webContextOptions: SecurityCatalogItem[];
  fieldOptions: SecurityCatalogItem[];
  loadFailed: boolean;
}

export function emptyWebContextsData(): ApplicationDevelopmentWebContextsData {
  return {
    appSecurityId: null,
    page: null,
    webContextOptions: [],
    fieldOptions: [],
    loadFailed: false,
  };
}

/** Called in the route resolver injection context; no initialization requests in the component. */
export function resolveDevelopmentWebContexts(applicationId: number, appSecurityId: number | null) {
  const contexts = inject(ApplicationWebContextsService);
  const catalog = inject(WebContextsService);
  const fields = inject(FieldsService);
  const applications = inject(ApplicationsService);
  const anchor =
    appSecurityId == null
      ? applications.refreshById(applicationId).pipe(
          map((application) => application.appSecurityId ?? null),
          catchError(() => of(null)),
          defaultIfEmpty(null),
        )
      : of(appSecurityId);
  const params = { size: 100, sort: 'id,asc', statusId: SoftDeleteStatus.ACTIVE };
  return forkJoin({
    context: anchor.pipe(
      switchMap((id) =>
        resolveValue(
          id == null
            ? of(null)
            : contexts.getPage({
                appSecurityId: id,
                page: 0,
                size: PAGINATOR_ROWS,
                sort: 'id,asc',
                statusId: SoftDeleteStatus.ACTIVE,
              }),
          null,
        ).pipe(map((result) => ({ ...result, appSecurityId: id }))),
      ),
    ),
    options: resolveValue(
      allPages((page) => catalog.getAll({ ...params, page })),
      [],
    ),
    fields: resolveValue(
      allPages((page) => fields.getAll({ page, size: 100, sort: 'id,asc' })).pipe(
        map((items) => items.filter((field) => field.deletedAt == null)),
      ),
      [],
    ),
  }).pipe(
    map(({ context, options, fields }): ApplicationDevelopmentWebContextsData => ({
      appSecurityId: context.appSecurityId,
      page: context.value,
      webContextOptions: options.value,
      fieldOptions: fields.value,
      loadFailed: context.failed || options.failed || fields.failed,
    })),
  );
}

function resolveValue<T>(source: Observable<T>, fallback: T) {
  return source.pipe(
    map((value) => ({ value, failed: false })),
    catchError(() => of({ value: fallback, failed: true })),
    defaultIfEmpty({ value: fallback, failed: true }),
  );
}

function allPages<T>(getPage: (page: number) => Observable<SpringPage<T>>): Observable<T[]> {
  return getPage(0).pipe(
    switchMap((first) =>
      first.totalPages <= 1
        ? of(first.content)
        : forkJoin(Array.from({ length: first.totalPages - 1 }, (_, i) => getPage(i + 1))).pipe(
            map((pages) => [first, ...pages].flatMap((page) => page.content)),
          ),
    ),
  );
}
