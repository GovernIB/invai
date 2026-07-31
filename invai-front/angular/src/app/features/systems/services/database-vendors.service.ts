import { Injectable } from '@angular/core';
import { BaseApiService } from '@core/services/base-api.service';
import { SpringPage } from '@models/page.model';
import { cachedRequest } from '@shared/utils/service-cache.utils';
import { Observable, tap } from 'rxjs';

import {
  DatabaseVendor,
  DatabaseVendorInput,
  DatabaseVendorPageParams,
} from '../systems.model';
import {
  infrastructureCacheKey,
  toInfrastructureHttpParams,
} from './infrastructure-http.utils';

@Injectable({ providedIn: 'root' })
export class DatabaseVendorsService extends BaseApiService {
  protected override readonly ENTITY_URI = 'database-vendor';

  private readonly pagesCache = new Map<string, Observable<SpringPage<DatabaseVendor>>>();

  getAll(params?: DatabaseVendorPageParams): Observable<SpringPage<DatabaseVendor>> {
    const criteria = {
      name: params?.name?.trim(),
      defaultPort: params?.defaultPort,
      statusId: params?.statusId,
      search: params?.search?.trim(),
    };
    const requestFactory = () =>
      this.http.get<SpringPage<DatabaseVendor>>(this.url(), {
        params: toInfrastructureHttpParams(params, criteria),
      });

    if (criteria.search) return requestFactory();

    return cachedRequest(
      this.pagesCache,
      infrastructureCacheKey(params, criteria),
      requestFactory,
    );
  }

  getById(id: number): Observable<DatabaseVendor> {
    return this.http.get<DatabaseVendor>(this.url(id));
  }

  create(payload: DatabaseVendorInput): Observable<DatabaseVendor> {
    return this.http
      .post<DatabaseVendor>(this.url(), payload)
      .pipe(tap(() => this.clearCache()));
  }

  update(id: number, payload: DatabaseVendorInput): Observable<DatabaseVendor> {
    return this.http
      .put<DatabaseVendor>(this.url(id), payload)
      .pipe(tap(() => this.clearCache()));
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(this.url(id)).pipe(tap(() => this.clearCache()));
  }

  reactivate(id: number): Observable<DatabaseVendor> {
    return this.http
      .put<DatabaseVendor>(this.url('reactivate', id), null)
      .pipe(tap(() => this.clearCache()));
  }

  clearCache(): void {
    this.pagesCache.clear();
  }
}
