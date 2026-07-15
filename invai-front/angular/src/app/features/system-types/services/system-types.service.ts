import { Injectable } from '@angular/core';
import { BaseApiService } from '@core/services/base-api.service';
import { SpringPage } from '@models/page.model';
import { toPageHttpParams } from '@shared/utils/http-params.utils';
import { cachedRequest, pageParamsCacheKey } from '@shared/utils/service-cache.utils';
import { Observable, tap } from 'rxjs';

import { SystemType, SystemTypeInput, SystemTypePageParams } from '../system-types.model';

@Injectable({ providedIn: 'root' })
export class SystemTypesService extends BaseApiService {
  protected override readonly ENTITY_URI = 'system-type';

  private readonly _systemTypesCache = new Map<string, Observable<SpringPage<SystemType>>>();

  getAll(params?: SystemTypePageParams): Observable<SpringPage<SystemType>> {
    return cachedRequest(this._systemTypesCache, pageParamsCacheKey(params), () =>
      this.http.get<SpringPage<SystemType>>(this.url(), {
        params: toPageHttpParams(params),
      }),
    );
  }

  getById(id: number): Observable<SystemType> {
    return this.http.get<SystemType>(this.url(id));
  }

  create(payload: SystemTypeInput): Observable<SystemType> {
    return this.http.post<SystemType>(this.url(), payload).pipe(tap(() => this.clearCache()));
  }

  update(id: number, payload: SystemTypeInput): Observable<SystemType> {
    return this.http.put<SystemType>(this.url(id), payload).pipe(tap(() => this.clearCache()));
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(this.url(id)).pipe(tap(() => this.clearCache()));
  }

  clearCache(): void {
    this._systemTypesCache.clear();
  }
}
