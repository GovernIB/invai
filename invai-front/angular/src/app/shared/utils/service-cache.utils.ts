import { Observable, catchError, shareReplay, throwError } from 'rxjs';
import { PageParams } from '@models/page.model';

export function cachedRequest<T>(
  cache: Map<string, Observable<T>>,
  key: string,
  requestFactory: () => Observable<T>,
): Observable<T> {
  const cached = cache.get(key);
  if (cached) return cached;

  const request$ = requestFactory().pipe(
    catchError((error: unknown) => {
      cache.delete(key);
      return throwError(() => error);
    }),
    shareReplay({ bufferSize: 1, refCount: false }),
  );

  cache.set(key, request$);
  return request$;
}

export function pageParamsCacheKey(params?: PageParams): string {
  if (!params) return 'page=;size=;sort=';

  const sort = Array.isArray(params.sort) ? params.sort.join('|') : (params.sort ?? '');
  return `page=${params.page ?? ''};size=${params.size ?? ''};sort=${sort}`;
}
