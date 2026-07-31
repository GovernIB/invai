import { inject } from '@angular/core';
import { ResolveFn } from '@angular/router';
import { Observable, catchError, map, of } from 'rxjs';

import {
  ApplicationOptionsService,
  ApplicationSelectOptions,
} from '../services/application-options.service';

export const APPLICATION_OPTIONS_RESOLVE_KEY = 'applicationOptions';

export interface ApplicationOptionsResolvedData {
  options: ApplicationSelectOptions;
  loadFailed: boolean;
}

const EMPTY_OPTIONS: ApplicationSelectOptions = {
  categories: [],
  informationSystems: [],
  scopes: [],
  commissions: [],
  administrativeUnits: [],
};

export const applicationOptionsResolver: ResolveFn<ApplicationOptionsResolvedData> = () =>
  resolveApplicationOptions(inject(ApplicationOptionsService));

export function resolveApplicationOptions(
  service: ApplicationOptionsService,
): Observable<ApplicationOptionsResolvedData> {
  return service.getOptions().pipe(
    map((options) => ({ options, loadFailed: false })),
    catchError(() => of({ options: EMPTY_OPTIONS, loadFailed: true })),
  );
}
