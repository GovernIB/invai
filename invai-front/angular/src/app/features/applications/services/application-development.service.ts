import { Injectable, inject } from '@angular/core';
import { BaseApiService } from '@core/services/base-api.service';
import { cachedRequest } from '@shared/utils/service-cache.utils';
import { Observable, tap } from 'rxjs';

import {
  ApplicationDevelopmentInput,
  ApplicationDevelopmentOutput,
} from '../applications.model';
import { ApplicationsService } from './applications.service';

@Injectable({ providedIn: 'root' })
export class ApplicationDevelopmentService extends BaseApiService {
  private readonly applicationsService = inject(ApplicationsService);
  protected override readonly ENTITY_URI = 'application/development';

  private readonly detailsCache = new Map<
    string,
    Observable<ApplicationDevelopmentOutput | null>
  >();

  getById(
    appDevelopmentId: number,
  ): Observable<ApplicationDevelopmentOutput | null> {
    return cachedRequest(this.detailsCache, String(appDevelopmentId), () =>
      this.http.get<ApplicationDevelopmentOutput | null>(this.url(appDevelopmentId)),
    );
  }

  create(payload: ApplicationDevelopmentInput): Observable<ApplicationDevelopmentOutput> {
    return this.http
      .post<ApplicationDevelopmentOutput>(this.url(), payload)
      .pipe(
        tap(() => {
          this.clearCache();
          this.applicationsService.clearCache();
        }),
      );
  }

  update(
    id: number,
    payload: ApplicationDevelopmentInput,
  ): Observable<ApplicationDevelopmentOutput> {
    return this.http
      .put<ApplicationDevelopmentOutput>(this.url(id), payload)
      .pipe(
        tap(() => {
          this.clearCache();
          this.applicationsService.clearCache();
        }),
      );
  }

  clearCache(): void {
    this.detailsCache.clear();
  }
}
