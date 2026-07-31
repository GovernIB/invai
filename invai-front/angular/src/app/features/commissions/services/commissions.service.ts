import { HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BaseApiService } from '@core/services/base-api.service';
import { SpringPage } from '@models/page.model';
import { toPageHttpParams } from '@shared/utils/http-params.utils';
import { cachedRequest } from '@shared/utils/service-cache.utils';
import { Observable, tap } from 'rxjs';

import { Commission, CommissionInput, CommissionPageParams } from '../commissions.model';

@Injectable({ providedIn: 'root' })
export class CommissionsService extends BaseApiService {
  protected override readonly ENTITY_URI = 'commission';

  private readonly _commissionsCache = new Map<string, Observable<SpringPage<Commission>>>();

  getAll(params?: CommissionPageParams): Observable<SpringPage<Commission>> {
    const requestFactory = () =>
      this.http.get<SpringPage<Commission>>(this.url(), {
        params: this.toHttpParams(params),
      });

    if (params?.quickSearch?.trim()) return requestFactory();

    return cachedRequest(this._commissionsCache, this.pageCacheKey(params), requestFactory);
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

  private toHttpParams(params?: CommissionPageParams): HttpParams | undefined {
    if (!params) return undefined;

    let httpParams = toPageHttpParams(params) ?? new HttpParams();
    const criteria: Record<string, string | number | undefined> = {
      quickSearch: params.quickSearch?.trim() || undefined,
      name: params.name,
      nameEs: params.nameEs,
      expedientNumber: params.expedientNumber,
      approvalDateFrom: params.approvalDateFrom,
      approvalDateTo: params.approvalDateTo,
      commissionType: params.commissionType,
      statusId: params.statusId,
    };

    Object.entries(criteria).forEach(([key, value]) => {
      if (value !== undefined && value !== '') httpParams = httpParams.set(key, String(value));
    });

    return httpParams.keys().length ? httpParams : undefined;
  }

  private pageCacheKey(params?: CommissionPageParams): string {
    const sort = Array.isArray(params?.sort) ? params.sort.join('|') : (params?.sort ?? '');

    return JSON.stringify({
      page: params?.page ?? null,
      size: params?.size ?? null,
      sort,
      name: params?.name ?? '',
      nameEs: params?.nameEs ?? '',
      expedientNumber: params?.expedientNumber ?? '',
      approvalDateFrom: params?.approvalDateFrom ?? '',
      approvalDateTo: params?.approvalDateTo ?? '',
      commissionType: params?.commissionType ?? null,
      statusId: params?.statusId ?? null,
    });
  }
}
