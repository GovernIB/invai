import { Injectable } from '@angular/core';
import { BaseApiService } from '@core/services/base-api.service';
import { SpringPage } from '@models/page.model';
import { toPageHttpParams } from '@shared/utils/http-params.utils';
import { cachedRequest, pageParamsCacheKey } from '@shared/utils/service-cache.utils';
import { Observable, tap } from 'rxjs';

import {
  ApplicationDevelopmentResourcePageParams,
  ApplicationTechnologyInput,
  ApplicationTechnologyOutput,
} from '../applications.model';

@Injectable({ providedIn: 'root' })
export class ApplicationTechnologiesService extends BaseApiService {
  protected override readonly ENTITY_URI = 'application/development/technology';

  private readonly pagesCache = new Map<
    string,
    Observable<SpringPage<ApplicationTechnologyOutput>>
  >();

  getPage(
    params: ApplicationDevelopmentResourcePageParams,
  ): Observable<SpringPage<ApplicationTechnologyOutput>> {
    const { appDevelopmentId, ...pageParams } = params;
    const key = `${appDevelopmentId};${pageParamsCacheKey(pageParams)}`;

    return cachedRequest(this.pagesCache, key, () =>
      this.http.get<SpringPage<ApplicationTechnologyOutput>>(this.url(appDevelopmentId), {
        params: toPageHttpParams(pageParams),
      }),
    );
  }

  create(payload: ApplicationTechnologyInput): Observable<ApplicationTechnologyOutput> {
    return this.http
      .post<ApplicationTechnologyOutput>(this.url(), payload)
      .pipe(tap(() => this.clearCache()));
  }

  update(
    id: number,
    payload: ApplicationTechnologyInput,
  ): Observable<ApplicationTechnologyOutput> {
    return this.http
      .put<ApplicationTechnologyOutput>(this.url(id), payload)
      .pipe(tap(() => this.clearCache()));
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(this.url(id)).pipe(tap(() => this.clearCache()));
  }

  clearCache(): void {
    this.pagesCache.clear();
  }
}
