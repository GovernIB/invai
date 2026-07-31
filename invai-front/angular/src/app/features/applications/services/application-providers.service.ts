import { Injectable } from '@angular/core';
import { BaseApiService } from '@core/services/base-api.service';
import { SpringPage } from '@models/page.model';
import { toPageHttpParams } from '@shared/utils/http-params.utils';
import { cachedRequest, pageParamsCacheKey } from '@shared/utils/service-cache.utils';
import { Observable, tap } from 'rxjs';

import {
  ApplicationDevelopmentResourcePageParams,
  ApplicationProviderInput,
  ApplicationProviderOutput,
} from '../applications.model';

@Injectable({ providedIn: 'root' })
export class ApplicationProvidersService extends BaseApiService {
  protected override readonly ENTITY_URI = 'application/development/provider';

  private readonly pagesCache = new Map<string, Observable<SpringPage<ApplicationProviderOutput>>>();

  getPage(
    params: ApplicationDevelopmentResourcePageParams,
  ): Observable<SpringPage<ApplicationProviderOutput>> {
    const { appDevelopmentId, ...pageParams } = params;
    const key = `${appDevelopmentId};${pageParamsCacheKey(pageParams)}`;

    return cachedRequest(this.pagesCache, key, () =>
      this.http.get<SpringPage<ApplicationProviderOutput>>(this.url(appDevelopmentId), {
        params: toPageHttpParams(pageParams),
      }),
    );
  }

  create(payload: ApplicationProviderInput): Observable<ApplicationProviderOutput> {
    return this.http
      .post<ApplicationProviderOutput>(this.url(), payload)
      .pipe(tap(() => this.clearCache()));
  }

  update(
    id: number,
    payload: ApplicationProviderInput,
  ): Observable<ApplicationProviderOutput> {
    return this.http
      .put<ApplicationProviderOutput>(this.url(id), payload)
      .pipe(tap(() => this.clearCache()));
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(this.url(id)).pipe(tap(() => this.clearCache()));
  }

  clearCache(): void {
    this.pagesCache.clear();
  }
}
