import { HttpParams } from '@angular/common/http';
import { Injectable, LOCALE_ID, inject } from '@angular/core';
import { BaseApiService } from '@core/services/base-api.service';
import { SpringPage } from '@models/page.model';
import { toPageHttpParams } from '@shared/utils/http-params.utils';
import { cachedRequest, pageParamsCacheKey } from '@shared/utils/service-cache.utils';
import { Observable, map, tap } from 'rxjs';

import { toApplicationDatabaseCatalogRow } from '../application-infrastructure.utils';
import { APPLICATION_INFRASTRUCTURE_STATUS_LABELS } from '../applications.constants';
import {
  ApplicationDatabase,
  ApplicationDatabaseRelationInput,
  ApplicationDatabaseRelationOutput,
  ApplicationDatabasesPageParams,
  ApplicationInfrastructureStatus,
} from '../applications.model';

@Injectable({ providedIn: 'root' })
export class ApplicationDatabasesService extends BaseApiService {
  protected override readonly ENTITY_URI = 'application/database';

  private readonly locale = inject(LOCALE_ID);
  private readonly pagesCache = new Map<string, Observable<SpringPage<ApplicationDatabase>>>();

  getPage(
    params: ApplicationDatabasesPageParams,
  ): Observable<SpringPage<ApplicationDatabase>> {
    return cachedRequest(this.pagesCache, this.cacheKey(params), () =>
      this.http
        .get<SpringPage<ApplicationDatabaseRelationOutput>>(
          this.url(params.informationSystemDbId),
          {
            params: this.toHttpParams(params),
          },
        )
        .pipe(
          map((page) => ({
            ...page,
            content: page.content.map((relation) => this.toApplicationDatabase(relation)),
          })),
        ),
    );
  }

  create(
    payload: ApplicationDatabaseRelationInput,
  ): Observable<ApplicationDatabaseRelationOutput> {
    return this.http
      .post<ApplicationDatabaseRelationOutput>(this.url(), payload)
      .pipe(tap(() => this.clearCache()));
  }

  update(
    id: number,
    payload: ApplicationDatabaseRelationInput,
  ): Observable<ApplicationDatabaseRelationOutput> {
    return this.http
      .put<ApplicationDatabaseRelationOutput>(this.url(id), payload)
      .pipe(tap(() => this.clearCache()));
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(this.url(id)).pipe(tap(() => this.clearCache()));
  }

  clearCache(): void {
    this.pagesCache.clear();
  }

  private toApplicationDatabase(relation: ApplicationDatabaseRelationOutput): ApplicationDatabase {
    const catalogItem = toApplicationDatabaseCatalogRow(relation.database, this.locale);
    return {
      id: relation.id,
      informationSystemDbId: relation.informationSystemDb.id,
      databaseId: relation.database.id,
      deletedAt: relation.deletedAt,
      environment: catalogItem.environment,
      server: catalogItem.server,
      database: relation.database.service ?? '',
      service: relation.database.service ?? '',
      port: relation.database.port,
      type: relation.database.databaseType?.name ?? '',
      status: this.statusLabel(relation.deletedAt),
      observations: relation.database.description ?? '',
      catalogItem,
    };
  }

  private statusLabel(deletedAt: string | null): string {
    return APPLICATION_INFRASTRUCTURE_STATUS_LABELS[
      deletedAt
        ? ApplicationInfrastructureStatus.INACTIVE
        : ApplicationInfrastructureStatus.ACTIVE
    ];
  }

  private toHttpParams(params: ApplicationDatabasesPageParams): HttpParams {
    let httpParams = toPageHttpParams(params) ?? new HttpParams();

    if (params.statusId != null) {
      httpParams = httpParams.set('statusId', String(params.statusId));
    }
    if (params.databaseId != null) {
      httpParams = httpParams.set('databaseId', String(params.databaseId));
    }
    return httpParams;
  }

  private cacheKey(params: ApplicationDatabasesPageParams): string {
    return `${pageParamsCacheKey(params)};informationSystemDbId=${params.informationSystemDbId};statusId=${params.statusId ?? ''};databaseId=${params.databaseId ?? ''}`;
  }
}
