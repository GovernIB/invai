import { Injectable } from '@angular/core';
import { BaseApiService } from '@core/services/base-api.service';
import { SpringPage } from '@models/page.model';
import { toPageHttpParams } from '@shared/utils/http-params.utils';
import { cachedRequest, pageParamsCacheKey } from '@shared/utils/service-cache.utils';
import { Observable, tap } from 'rxjs';

import { Commission, CommissionInput, CommissionPageParams } from '../commissions.model';

@Injectable({ providedIn: 'root' })
export class CommissionsService extends BaseApiService {
  protected override readonly ENTITY_URI = 'commission';

  private readonly _commissionsCache = new Map<string, Observable<SpringPage<Commission>>>();

  getAll(params?: CommissionPageParams): Observable<SpringPage<Commission>> {
    return cachedRequest(this._commissionsCache, pageParamsCacheKey(params), () =>
      this.http.get<SpringPage<Commission>>(this.url(), {
        params: toPageHttpParams(params),
      }),
    );
  }

  getById(id: number): Observable<Commission> {
    return this.http.get<Commission>(this.url(id));
  }

  create(payload: CommissionInput): Observable<Commission> {
    return this.http.post<Commission>(this.url(), payload).pipe(tap(() => this.clearCache()));
  }

  update(id: number, payload: CommissionInput): Observable<Commission> {
    return this.http.put<Commission>(this.url(id), payload).pipe(tap(() => this.clearCache()));
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(this.url(id)).pipe(tap(() => this.clearCache()));
  }

  clearCache(): void {
    this._commissionsCache.clear();
  }
}
