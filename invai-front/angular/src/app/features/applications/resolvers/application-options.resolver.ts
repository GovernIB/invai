import { inject } from '@angular/core';
import { ResolveFn } from '@angular/router';
import { Observable, catchError, forkJoin, map, of } from 'rxjs';

import {
  ApplicationOptionsService,
  ApplicationSelectOptions,
} from '../services/application-options.service';
import {
  APPLICATION_DETAIL_RESOLVE_KEY,
  ApplicationDetailResolvedData,
} from '../pages/detail/application-detail.resolver';

export const APPLICATION_OPTIONS_RESOLVE_KEY = 'applicationOptions';

export interface ApplicationOptionsResolvedData {
  options: ApplicationSelectOptions;
  loadFailed: boolean;
  departmentsLoadFailed: boolean;
  administrativeUnitsLoadFailed: boolean;
}

const EMPTY_OPTIONS: ApplicationSelectOptions = {
  categories: [],
  informationSystems: [],
  scopes: [],
  commissions: [],
  departments: [],
  administrativeUnits: [],
};

export const applicationOptionsResolver: ResolveFn<ApplicationOptionsResolvedData> = (route) => {
  const detail = route.parent?.data[
    APPLICATION_DETAIL_RESOLVE_KEY
  ] as ApplicationDetailResolvedData | undefined;
  return resolveApplicationOptions(
    inject(ApplicationOptionsService),
    detail?.application?.department?.code,
  );
};

export function resolveApplicationOptions(
  service: ApplicationOptionsService,
  departmentCode?: string | null,
): Observable<ApplicationOptionsResolvedData> {
  return forkJoin({
    staticOptions: service.getStaticOptions().pipe(
      map((options) => ({ options, failed: false })),
      catchError(() =>
        of({
          options: {
            categories: [],
            informationSystems: [],
            scopes: [],
            commissions: [],
          },
          failed: true,
        }),
      ),
    ),
    departments: service.getDepartmentOptions().pipe(
      map((options) => ({ options, failed: false })),
      catchError(() => of({ options: [], failed: true })),
    ),
    administrativeUnits: departmentCode
      ? service.getAdministrativeUnitOptions(departmentCode).pipe(
          map((options) => ({ options, failed: false })),
          catchError(() => of({ options: [], failed: true })),
        )
      : of({ options: [], failed: false }),
  }).pipe(
    map(({ staticOptions, departments, administrativeUnits }) => ({
      options: {
        ...staticOptions.options,
        departments: departments.options,
        administrativeUnits: administrativeUnits.options,
      },
      loadFailed:
        staticOptions.failed || departments.failed || administrativeUnits.failed,
      departmentsLoadFailed: departments.failed,
      administrativeUnitsLoadFailed: administrativeUnits.failed,
    })),
    catchError(() =>
      of({
        options: EMPTY_OPTIONS,
        loadFailed: true,
        departmentsLoadFailed: true,
        administrativeUnitsLoadFailed: Boolean(departmentCode),
      }),
    ),
  );
}
