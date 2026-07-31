import { Injectable } from '@angular/core';
import { BaseApiService } from '@core/services/base-api.service';
import { SpringPage } from '@models/page.model';
import { cachedRequest } from '@shared/utils/service-cache.utils';
import { Observable, tap } from 'rxjs';

import {
  InfrastructureSystem,
  InfrastructureSystemInput,
  SystemPageParams,
} from '../systems.model';
import {
  infrastructureCacheKey,
  toInfrastructureHttpParams,
} from './infrastructure-http.utils';

@Injectable({ providedIn: 'root' })
export class SystemsService extends BaseApiService {
  protected override readonly ENTITY_URI = 'system';

  private readonly pagesCache = new Map<
    string,
    Observable<SpringPage<InfrastructureSystem>>
  >();

  getAll(params?: SystemPageParams): Observable<SpringPage<InfrastructureSystem>> {
    const criteria = {
      serverId: params?.serverId,
      instance: params?.instance?.trim(),
      version: params?.version?.trim(),
      statusId: params?.statusId,
    };

    return cachedRequest(
      this.pagesCache,
      infrastructureCacheKey(params, criteria),
      () =>
        this.http.get<SpringPage<InfrastructureSystem>>(this.url(), {
          params: toInfrastructureHttpParams(params, criteria),
        }),
    );
  }

  getById(id: number): Observable<InfrastructureSystem> {
    return this.http.get<InfrastructureSystem>(this.url(id));
  }

  create(payload: InfrastructureSystemInput): Observable<InfrastructureSystem> {
    return this.http
      .post<InfrastructureSystem>(this.url(), payload)
      .pipe(tap(() => this.clearCache()));
  }

  update(
    id: number,
    payload: InfrastructureSystemInput,
  ): Observable<InfrastructureSystem> {
    return this.http
      .put<InfrastructureSystem>(this.url(id), payload)
      .pipe(tap(() => this.clearCache()));
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(this.url(id)).pipe(tap(() => this.clearCache()));
  }

  reactivate(id: number): Observable<InfrastructureSystem> {
    return this.http
      .put<InfrastructureSystem>(this.url('reactivate', id), null)
      .pipe(tap(() => this.clearCache()));
  }

  clearCache(): void {
    this.pagesCache.clear();
  }
}
