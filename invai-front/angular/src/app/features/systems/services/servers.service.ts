import { Injectable } from '@angular/core';
import { BaseApiService } from '@core/services/base-api.service';
import { SpringPage } from '@models/page.model';
import { cachedRequest } from '@shared/utils/service-cache.utils';
import { Observable, tap } from 'rxjs';

import {
  InfrastructureServer,
  InfrastructureServerInput,
  ServerPageParams,
} from '../systems.model';
import {
  infrastructureCacheKey,
  toInfrastructureHttpParams,
} from './infrastructure-http.utils';

@Injectable({ providedIn: 'root' })
export class ServersService extends BaseApiService {
  protected override readonly ENTITY_URI = 'server';

  private readonly pagesCache = new Map<
    string,
    Observable<SpringPage<InfrastructureServer>>
  >();

  getAll(params: ServerPageParams): Observable<SpringPage<InfrastructureServer>> {
    const criteria = {
      name: params.name?.trim(),
      environmentId: params.environmentId,
      serverTypeCode: params.serverTypeCode,
      statusId: params.statusId,
      search: params.search?.trim(),
    };
    const requestFactory = () =>
      this.http.get<SpringPage<InfrastructureServer>>(this.url(), {
        params: toInfrastructureHttpParams(params, criteria),
      });

    if (criteria.search) return requestFactory();

    return cachedRequest(
      this.pagesCache,
      infrastructureCacheKey(params, criteria),
      requestFactory,
    );
  }

  getById(id: number): Observable<InfrastructureServer> {
    return this.http.get<InfrastructureServer>(this.url(id));
  }

  create(payload: InfrastructureServerInput): Observable<InfrastructureServer> {
    return this.http
      .post<InfrastructureServer>(this.url(), payload)
      .pipe(tap(() => this.clearCache()));
  }

  update(
    id: number,
    payload: InfrastructureServerInput,
  ): Observable<InfrastructureServer> {
    return this.http
      .put<InfrastructureServer>(this.url(id), payload)
      .pipe(tap(() => this.clearCache()));
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(this.url(id)).pipe(tap(() => this.clearCache()));
  }

  reactivate(id: number): Observable<InfrastructureServer> {
    return this.http
      .put<InfrastructureServer>(this.url('reactivate', id), null)
      .pipe(tap(() => this.clearCache()));
  }

  clearCache(): void {
    this.pagesCache.clear();
  }
}
