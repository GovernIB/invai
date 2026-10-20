import { inject } from '@angular/core';
import { FieldsService } from '@features/fields/services/fields.service';
import { WebContextsService } from '@features/maintenances/security/services/security-resource.services';
import { SpringPage } from '@models/page.model';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';
import { PAGINATOR_ROWS } from '@shared/constants/table.constants';
import { Observable, catchError, defaultIfEmpty, forkJoin, map, of, switchMap } from 'rxjs';
import { ApplicationWebContextOutput, SecurityCatalogItem } from '../../../../applications.model';
import { ApplicationDevelopmentWebContextsService } from '../../../../services/application-development-web-contexts.service';
import { ApplicationsService } from '../../../../services/applications.service';

export interface ApplicationDevelopmentWebContextsData {
  appDevelopmentId: number | null;
  page: SpringPage<ApplicationWebContextOutput> | null;
  webContextOptions: SecurityCatalogItem[];
  fieldOptions: SecurityCatalogItem[];
  loadFailed: boolean;
}

export function emptyWebContextsData(): ApplicationDevelopmentWebContextsData {
  return {
    appDevelopmentId: null,
    page: null,
    webContextOptions: [],
    fieldOptions: [],
    loadFailed: false,
  };
}

/** Called in the route resolver injection context; no initialization requests in the component. */
export function resolveDevelopmentWebContexts(applicationId: number, appDevelopmentId: number | null) {
  const contexts = inject(ApplicationDevelopmentWebContextsService);
  const catalog = inject(WebContextsService);
  const fields = inject(FieldsService);
  const applications = inject(ApplicationsService);
  const anchor =
    appDevelopmentId == null
      ? applications.refreshById(applicationId).pipe(
          map((application) => application.appDevelopmentId ?? null),
          catchError(() => of(null)),
          defaultIfEmpty(null),
        )
      : of(appDevelopmentId);
  const params = { size: 100, sort: 'id,asc', statusId: SoftDeleteStatus.ACTIVE };
  return forkJoin({
    context: anchor.pipe(
      switchMap((id) =>
        resolveValue(
          id == null
            ? of(null)
            : contexts.getPage({
                appDevelopmentId: id,
                page: 0,
                size: PAGINATOR_ROWS,
                sort: 'id,asc',
                statusId: SoftDeleteStatus.ACTIVE,
              }),
          null,
        ).pipe(map((result) => ({ ...result, appDevelopmentId: id }))),
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
      appDevelopmentId: context.appDevelopmentId,
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
