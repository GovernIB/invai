import { inject, Injectable } from '@angular/core';
import { BaseApiService } from '@core/services/base-api.service';
import { cachedRequest } from '@shared/utils/service-cache.utils';
import { Observable, tap } from 'rxjs';
import {
  ApplicationAccessibilityInput,
  ApplicationAccessibilityOutput,
} from '../applications.model';
import { ApplicationsService } from './applications.service';
import { APPLICATION_DETAIL_CACHE_TTL_MS } from './application-cache.constants';

@Injectable({ providedIn: 'root' })
export class ApplicationAccessibilityService extends BaseApiService {
  protected override readonly ENTITY_URI = 'application/accessibility';
  private readonly applications = inject(ApplicationsService);
  private readonly detailsCache =
    new Map<string, Observable<ApplicationAccessibilityOutput | null>>();

  getById(id: number): Observable<ApplicationAccessibilityOutput | null> {
    const key = String(id);
    const request$: Observable<ApplicationAccessibilityOutput | null> = cachedRequest(
      this.detailsCache,
      key,
      () =>
        this.http.get<ApplicationAccessibilityOutput | null>(this.url(id)).pipe(
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

  refreshById(id: number): Observable<ApplicationAccessibilityOutput | null> {
    this.detailsCache.delete(String(id));
    return this.getById(id);
  }

  create(payload: ApplicationAccessibilityInput): Observable<ApplicationAccessibilityOutput> {
    return this.http
      .post<ApplicationAccessibilityOutput>(this.url(), payload)
      .pipe(tap(() => this.changed()));
  }

  update(
    id: number,
    payload: ApplicationAccessibilityInput,
  ): Observable<ApplicationAccessibilityOutput> {
    return this.http
      .put<ApplicationAccessibilityOutput>(this.url(id), payload)
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
