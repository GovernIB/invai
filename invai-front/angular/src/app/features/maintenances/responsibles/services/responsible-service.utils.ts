import { HttpParams } from '@angular/common/http';
import { PageParams } from '@models/page.model';
import { toPageHttpParams } from '@shared/utils/http-params.utils';

export function responsibleHttpParams(
  params: PageParams | undefined,
  criteria: Record<string, string | number | undefined>,
): HttpParams | undefined {
  let httpParams = toPageHttpParams(params) ?? new HttpParams();
  Object.entries(criteria).forEach(([key, value]) => {
    if (value !== undefined && value !== '') httpParams = httpParams.set(key, String(value));
  });
  return httpParams.keys().length ? httpParams : undefined;
}

export function responsibleCacheKey(
  params: PageParams | undefined,
  criteria: Record<string, string | number | undefined>,
): string {
  return JSON.stringify({
    page: params?.page ?? null,
    size: params?.size ?? null,
    sort: Array.isArray(params?.sort) ? params.sort.join('|') : (params?.sort ?? ''),
    ...criteria,
  });
}
