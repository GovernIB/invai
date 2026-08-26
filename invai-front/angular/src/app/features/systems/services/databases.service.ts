import { Injectable } from '@angular/core';
import { BaseApiService } from '@core/services/base-api.service';
import { SpringPage } from '@models/page.model';
import { cachedRequest } from '@shared/utils/service-cache.utils';
import { Observable, tap } from 'rxjs';

import {
  DatabaseInput,
  DatabasePageParams,
  DatabaseRecord,
} from '../systems.model';
import {
  infrastructureCacheKey,
  toInfrastructureHttpParams,
} from './infrastructure-http.utils';

@Injectable({ providedIn: 'root' })
export class DatabasesService extends BaseApiService {
  protected override readonly ENTITY_URI = 'database';

  private readonly pagesCache = new Map<string, Observable<SpringPage<DatabaseRecord>>>();

  getAll(params?: DatabasePageParams): Observable<SpringPage<DatabaseRecord>> {
    const criteria = {
      serverId: params?.serverId,
      service: params?.service?.trim(),
      databaseTypeId: params?.databaseTypeId,
      statusId: params?.statusId,
      search: params?.search?.trim(),
      unassignedToInformationSystemDbId:
        params?.unassignedToInformationSystemDbId,
    };
    const requestFactory = () =>
      this.http.get<SpringPage<DatabaseRecord>>(this.url(), {
        params: toInfrastructureHttpParams(params, criteria),
      });

    if (criteria.search) return requestFactory();

    return cachedRequest(
      this.pagesCache,
      infrastructureCacheKey(params, criteria),
      requestFactory,
    );
  }

  getById(id: number): Observable<DatabaseRecord> {
    return this.http.get<DatabaseRecord>(this.url(id));
  }

  create(payload: DatabaseInput): Observable<DatabaseRecord> {
    return this.http
      .post<DatabaseRecord>(this.url(), payload)
      .pipe(tap(() => this.clearCache()));
  }

  update(id: number, payload: DatabaseInput): Observable<DatabaseRecord> {
    return this.http
      .put<DatabaseRecord>(this.url(id), payload)
      .pipe(tap(() => this.clearCache()));
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(this.url(id)).pipe(tap(() => this.clearCache()));
  }

  reactivate(id: number): Observable<DatabaseRecord> {
    return this.http
      .put<DatabaseRecord>(this.url('reactivate', id), null)
      .pipe(tap(() => this.clearCache()));
  }

  clearCache(): void {
    this.pagesCache.clear();
  }
}
