import { HttpContext, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { SKIP_SERVER_ERROR_DIALOG } from '@core/interceptors/http-error-logging.interceptor';
import { BaseApiService } from '@core/services/base-api.service';
import { SpringPage } from '@models/page.model';
import { toPageHttpParams } from '@shared/utils/http-params.utils';
import { cachedRequest, pageParamsCacheKey } from '@shared/utils/service-cache.utils';
import { Observable, tap } from 'rxjs';

import {
  ApplicationIntegrationConnectionInput,
  ApplicationIntegrationConnectionOutput,
  ApplicationIntegrationConnectionPageParams,
} from '../applications.model';
import { APPLICATION_DETAIL_CACHE_TTL_MS } from './application-cache.constants';
import { ApplicationsService } from './applications.service';

@Injectable({ providedIn: 'root' })
export class ApplicationIntegrationConnectionsService extends BaseApiService {
  protected override readonly ENTITY_URI = 'application/integration-connection';
  private readonly applicationsService = inject(ApplicationsService);
  private readonly pagesCache = new Map<
    string,
    Observable<SpringPage<ApplicationIntegrationConnectionOutput>>
  >();

  // Each row carries roles resolved live against Soffid, so pages expire after the detail TTL.
  getPage(
    params: ApplicationIntegrationConnectionPageParams,
  ): Observable<SpringPage<ApplicationIntegrationConnectionOutput>> {
    const { appIntegrationId, statusId, ...pageParams } = params;
    const key = `${appIntegrationId};status=${statusId ?? ''};${pageParamsCacheKey(pageParams)}`;
    let httpParams = (toPageHttpParams(pageParams) ?? new HttpParams()).set(
      'appIntegrationId',
      String(appIntegrationId),
    );
    if (statusId != null) httpParams = httpParams.set('statusId', String(statusId));

    return cachedRequest(
      this.pagesCache,
      key,
      () =>
        this.http.get<SpringPage<ApplicationIntegrationConnectionOutput>>(this.url(), {
          params: httpParams,
          // A Soffid outage fails the whole page; the tab reports it inline.
          context: new HttpContext().set(SKIP_SERVER_ERROR_DIALOG, true),
        }),
      APPLICATION_DETAIL_CACHE_TTL_MS,
    );
  }

  create(
    payload: ApplicationIntegrationConnectionInput,
  ): Observable<ApplicationIntegrationConnectionOutput> {
    return this.http
      .post<ApplicationIntegrationConnectionOutput>(this.url(), payload)
      .pipe(tap(() => this.invalidate()));
  }

  update(
    id: number,
    payload: ApplicationIntegrationConnectionInput,
  ): Observable<ApplicationIntegrationConnectionOutput> {
    return this.http
      .put<ApplicationIntegrationConnectionOutput>(this.url(id), payload)
      .pipe(tap(() => this.invalidate()));
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(this.url(id)).pipe(tap(() => this.invalidate()));
  }

  clearCache(): void {
    this.pagesCache.clear();
  }

  // The application completeness depends on the number of active connections.
  private invalidate(): void {
    this.clearCache();
    this.applicationsService.clearCache();
  }
}
