import { HttpContext } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { SKIP_SERVER_ERROR_DIALOG } from '@core/interceptors/http-error-logging.interceptor';
import { BaseApiService } from '@core/services/base-api.service';
import { cachedRequest } from '@shared/utils/service-cache.utils';
import { Observable, tap } from 'rxjs';
import { ApplicationDataInput, ApplicationDataOutput } from '../applications.model';
import { ApplicationsService } from './applications.service';
import { APPLICATION_DETAIL_CACHE_TTL_MS } from './application-cache.constants';

@Injectable({ providedIn: 'root' })
export class ApplicationDataService extends BaseApiService {
  protected override readonly ENTITY_URI = 'application/data';
  private readonly applications = inject(ApplicationsService);
  private readonly detailsCache = new Map<string, Observable<ApplicationDataOutput | null>>();

  // The backend resolves the published endpoints live on every read, so the detail is cached
  // for the detail TTL and invalidated after every successful mutation.
  getById(id: number): Observable<ApplicationDataOutput | null> {
    const key = String(id);
    const request$: Observable<ApplicationDataOutput | null> = cachedRequest(
      this.detailsCache,
      key,
      () =>
        this.http
          .get<ApplicationDataOutput | null>(this.url(id), {
            // The data tab reports unavailable documents inline.
            context: new HttpContext().set(SKIP_SERVER_ERROR_DIALOG, true),
          })
          .pipe(
            tap((record) => {
              if (record === null && this.detailsCache.get(key) === request$) {
                this.detailsCache.delete(key);
              }
            }),
          ),
      APPLICATION_DETAIL_CACHE_TTL_MS,
    );
    return request$;
  }

  refreshById(id: number): Observable<ApplicationDataOutput | null> {
    this.detailsCache.delete(String(id));
    return this.getById(id);
  }

  create(payload: ApplicationDataInput): Observable<ApplicationDataOutput> {
    return this.http
      .post<ApplicationDataOutput>(this.url(), payload)
      .pipe(tap(() => this.changed()));
  }

  update(id: number, payload: ApplicationDataInput): Observable<ApplicationDataOutput> {
    return this.http
      .put<ApplicationDataOutput>(this.url(id), payload)
      .pipe(tap(() => this.changed()));
  }

  clearCache(): void {
    this.detailsCache.clear();
  }

  private changed(): void {
    this.clearCache();
    this.applications.clearCache();
  }
}
