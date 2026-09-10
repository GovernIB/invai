import { Injectable, inject } from '@angular/core';
import { BaseApiService } from '@core/services/base-api.service';
import { cachedRequest } from '@shared/utils/service-cache.utils';
import { Observable, tap } from 'rxjs';

import {
  ApplicationSystemDatabaseInput,
  ApplicationSystemDatabaseOutput,
} from '../applications.model';
import { ApplicationsService } from './applications.service';

@Injectable({ providedIn: 'root' })
export class ApplicationSystemDatabaseService extends BaseApiService {
  private readonly applicationsService = inject(ApplicationsService);
  protected override readonly ENTITY_URI = 'application/system-database';

  private readonly detailsCache = new Map<
    string,
    Observable<ApplicationSystemDatabaseOutput | null>
  >();

  getById(
    informationSystemDbId: number,
  ): Observable<ApplicationSystemDatabaseOutput | null> {
    return cachedRequest(this.detailsCache, String(informationSystemDbId), () =>
      this.http.get<ApplicationSystemDatabaseOutput | null>(this.url(informationSystemDbId)),
    );
  }

  create(
    payload: ApplicationSystemDatabaseInput,
  ): Observable<ApplicationSystemDatabaseOutput> {
    return this.http
      .post<ApplicationSystemDatabaseOutput>(this.url(), payload)
      .pipe(
        tap(() => {
          this.clearCache();
          this.applicationsService.clearCache();
        }),
      );
  }

  update(
    id: number,
    payload: ApplicationSystemDatabaseInput,
  ): Observable<ApplicationSystemDatabaseOutput> {
    return this.http
      .put<ApplicationSystemDatabaseOutput>(this.url(id), payload)
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
