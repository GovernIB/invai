import { HttpParams } from '@angular/common/http';
import { Injectable, LOCALE_ID, inject } from '@angular/core';
import { BaseApiService } from '@core/services/base-api.service';
import { SpringPage } from '@models/page.model';
import { toPageHttpParams } from '@shared/utils/http-params.utils';
import { cachedRequest, pageParamsCacheKey } from '@shared/utils/service-cache.utils';
import { Observable, map, tap } from 'rxjs';

import { toApplicationSystemCatalogRow } from '../application-infrastructure.utils';
import { APPLICATION_INFRASTRUCTURE_STATUS_LABELS } from '../applications.constants';
import {
  ApplicationInfrastructureStatus,
  ApplicationServer,
  ApplicationSystemRelationInput,
  ApplicationSystemsPageParams,
  ApplicationSystemRelationOutput,
} from '../applications.model';

@Injectable({ providedIn: 'root' })
export class ApplicationSystemsService extends BaseApiService {
  protected override readonly ENTITY_URI = 'application/system';

  private readonly locale = inject(LOCALE_ID);
  private readonly pagesCache = new Map<string, Observable<SpringPage<ApplicationServer>>>();

  getPage(params: ApplicationSystemsPageParams): Observable<SpringPage<ApplicationServer>> {
    return cachedRequest(this.pagesCache, this.cacheKey(params), () =>
      this.http
        .get<SpringPage<ApplicationSystemRelationOutput>>(this.url(params.informationSystemDbId), {
          params: this.toHttpParams(params),
        })
        .pipe(
          map((page) => ({
            ...page,
            content: page.content.map((relation) => this.toApplicationServer(relation)),
          })),
        ),
    );
  }

  create(payload: ApplicationSystemRelationInput): Observable<ApplicationSystemRelationOutput> {
    return this.http
      .post<ApplicationSystemRelationOutput>(this.url(), payload)
      .pipe(tap(() => this.clearCache()));
  }

  update(
    id: number,
    payload: ApplicationSystemRelationInput,
  ): Observable<ApplicationSystemRelationOutput> {
    return this.http
      .put<ApplicationSystemRelationOutput>(this.url(id), payload)
      .pipe(tap(() => this.clearCache()));
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(this.url(id)).pipe(tap(() => this.clearCache()));
  }

  clearCache(): void {
    this.pagesCache.clear();
  }

  private toApplicationServer(relation: ApplicationSystemRelationOutput): ApplicationServer {
    const catalogItem = toApplicationSystemCatalogRow(relation.system, this.locale);
    return {
      id: relation.id,
      informationSystemDbId: relation.informationSystemDb.id,
      systemId: relation.system.id,
      deletedAt: relation.deletedAt,
      environment: catalogItem.environment,
      server: catalogItem.server,
      instance: relation.system.instance ?? '',
      port: relation.system.port,
      version: relation.system.version ?? '',
      status: this.statusLabel(relation.deletedAt),
      observations: relation.system.description ?? '',
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

  private toHttpParams(params: ApplicationSystemsPageParams): HttpParams {
    let httpParams = toPageHttpParams(params) ?? new HttpParams();

    if (params.statusId != null) {
      httpParams = httpParams.set('statusId', String(params.statusId));
    }
    if (params.systemId != null) httpParams = httpParams.set('systemId', String(params.systemId));

    return httpParams;
  }

  private cacheKey(params: ApplicationSystemsPageParams): string {
    return `${pageParamsCacheKey(params)};informationSystemDbId=${params.informationSystemDbId};statusId=${params.statusId ?? ''};systemId=${params.systemId ?? ''}`;
  }
}
