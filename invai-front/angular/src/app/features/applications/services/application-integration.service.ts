import { HttpContext } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { SKIP_SERVER_ERROR_DIALOG } from '@core/interceptors/http-error-logging.interceptor';
import { BaseApiService } from '@core/services/base-api.service';
import { cachedRequest } from '@shared/utils/service-cache.utils';
import { Observable, tap } from 'rxjs';

import { ApplicationIntegrationInput, ApplicationIntegrationOutput } from '../applications.model';
import { APPLICATION_DETAIL_CACHE_TTL_MS } from './application-cache.constants';
import { ApplicationsService } from './applications.service';

// Anchor of the "Integracions" tab; its connections live in ApplicationIntegrationConnectionsService.
@Injectable({ providedIn: 'root' })
export class ApplicationIntegrationService extends BaseApiService {
  protected override readonly ENTITY_URI = 'application/integration';
  private readonly applications = inject(ApplicationsService);
  private readonly detailsCache = new Map<string, Observable<ApplicationIntegrationOutput | null>>();

  // The backend answers 200 with an empty body for an unknown id; that result is not cached.
  getById(id: number): Observable<ApplicationIntegrationOutput | null> {
    const key = String(id);
    const request$: Observable<ApplicationIntegrationOutput | null> = cachedRequest(
      this.detailsCache,
      key,
      () =>
        this.http
          .get<ApplicationIntegrationOutput | null>(this.url(id), {
            // The integrations tab reports load failures inline.
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

  refreshById(id: number): Observable<ApplicationIntegrationOutput | null> {
    this.detailsCache.delete(String(id));
    return this.getById(id);
  }

  create(payload: ApplicationIntegrationInput): Observable<ApplicationIntegrationOutput> {
    return this.http
      .post<ApplicationIntegrationOutput>(this.url(), payload)
      .pipe(tap(() => this.changed()));
  }

  update(id: number, payload: ApplicationIntegrationInput): Observable<ApplicationIntegrationOutput> {
    return this.http
      .put<ApplicationIntegrationOutput>(this.url(id), payload)
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
