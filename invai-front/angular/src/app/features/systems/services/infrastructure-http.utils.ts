import { HttpParams } from '@angular/common/http';
import { PageParams } from '@models/page.model';
import { toPageHttpParams } from '@shared/utils/http-params.utils';
import { pageParamsCacheKey } from '@shared/utils/service-cache.utils';

export type InfrastructureCriteria = Record<
  string,
  string | number | null | undefined
>;

export function toInfrastructureHttpParams(
  params: PageParams | undefined,
  criteria: InfrastructureCriteria,
): HttpParams | undefined {
  let httpParams = toPageHttpParams(params) ?? new HttpParams();

  Object.entries(criteria).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      httpParams = httpParams.set(key, String(value));
    }
  });

  return httpParams.keys().length ? httpParams : undefined;
}

export function infrastructureCacheKey(
  params: PageParams | undefined,
  criteria: InfrastructureCriteria,
): string {
  return `${pageParamsCacheKey(params)};${Object.entries(criteria)
    .map(([key, value]) => `${key}=${value ?? ''}`)
    .join(';')}`;
}
