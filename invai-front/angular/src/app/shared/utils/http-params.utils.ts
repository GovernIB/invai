import { HttpParams } from '@angular/common/http';
import { PageParams } from '@models/page.model';

export function toPageHttpParams(params?: PageParams): HttpParams | undefined {
  if (!params) return undefined;

  let httpParams = new HttpParams();

  if (params.page != null) {
    httpParams = httpParams.set('page', String(params.page));
  }

  if (params.size != null) {
    httpParams = httpParams.set('size', String(params.size));
  }

  const sort = params.sort;
  if (Array.isArray(sort)) {
    sort.forEach((sortValue) => {
      httpParams = httpParams.append('sort', sortValue);
    });
  } else if (sort) {
    httpParams = httpParams.set('sort', sort);
  }

  return httpParams.keys().length ? httpParams : undefined;
}
