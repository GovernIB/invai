import { Observable, catchError, shareReplay, tap, throwError } from 'rxjs';
import { PageParams } from '@models/page.model';

const requestExpirations = new WeakMap<Observable<unknown>, number>();

export function cachedRequest<T>(
  cache: Map<string, Observable<T>>,
  key: string,
  requestFactory: () => Observable<T>,
  ttlMs?: number,
): Observable<T> {
  const cached = cache.get(key);
  if (cached) {
    const expiresAt = requestExpirations.get(cached);
    if (expiresAt === undefined || Date.now() < expiresAt) return cached;
    cache.delete(key);
  }

  const request$ = requestFactory().pipe(
    tap(() => {
      // Pending requests share one entry; the TTL starts with the first response.
      if (ttlMs !== undefined && !requestExpirations.has(request$)) {
        requestExpirations.set(request$, Date.now() + ttlMs);
      }
    }),
    catchError((error: unknown) => {
      if (cache.get(key) === request$) cache.delete(key);
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
