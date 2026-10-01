import { HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BaseApiService } from '@core/services/base-api.service';
import { SpringPage } from '@models/page.model';
import { toPageHttpParams } from '@shared/utils/http-params.utils';
import { cachedRequest } from '@shared/utils/service-cache.utils';
import { Observable, tap } from 'rxjs';

import {
  ExternalSystem,
  ExternalSystemInput,
  ExternalSystemPageParams,
} from '../external-systems.model';

@Injectable({ providedIn: 'root' })
export class ExternalSystemsService extends BaseApiService {
  protected override readonly ENTITY_URI = 'external-system';

  private readonly pagesCache = new Map<string, Observable<SpringPage<ExternalSystem>>>();

  getAll(params?: ExternalSystemPageParams): Observable<SpringPage<ExternalSystem>> {
    const requestFactory = () =>
      this.http.get<SpringPage<ExternalSystem>>(this.url(), {
        params: this.toHttpParams(params),
      });

    if (params?.search?.trim()) return requestFactory();
    return cachedRequest(this.pagesCache, this.pageCacheKey(params), requestFactory);
  }

  getById(id: number): Observable<ExternalSystem> {
    return this.http.get<ExternalSystem>(this.url(id));
  }

  create(payload: ExternalSystemInput): Observable<ExternalSystem> {
    return this.http
      .post<ExternalSystem>(this.url(), payload)
      .pipe(tap(() => this.clearCache()));
  }

  update(id: number, payload: ExternalSystemInput): Observable<ExternalSystem> {
    return this.http
      .put<ExternalSystem>(this.url(id), payload)
      .pipe(tap(() => this.clearCache()));
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(this.url(id)).pipe(tap(() => this.clearCache()));
  }

  reactivate(id: number): Observable<ExternalSystem> {
    return this.http
      .put<ExternalSystem>(this.url('reactivate', id), null)
      .pipe(tap(() => this.clearCache()));
  }

  clearCache(): void {
    this.pagesCache.clear();
  }

  private toHttpParams(params?: ExternalSystemPageParams): HttpParams | undefined {
    if (!params) return undefined;
    let httpParams = toPageHttpParams(params) ?? new HttpParams();
    const criteria: Record<string, string | number | undefined> = {
      name: params.name,
      companyId: params.companyId,
      statusId: params.statusId,
      search: params.search?.trim() || undefined,
    };
    Object.entries(criteria).forEach(([key, value]) => {
      if (value !== undefined && value !== '') httpParams = httpParams.set(key, String(value));
    });
    return httpParams.keys().length ? httpParams : undefined;
  }

  private pageCacheKey(params?: ExternalSystemPageParams): string {
    const sort = Array.isArray(params?.sort) ? params.sort.join('|') : (params?.sort ?? '');
    return JSON.stringify({
      page: params?.page ?? null,
      size: params?.size ?? null,
      sort,
      name: params?.name ?? '',
      companyId: params?.companyId ?? null,
      statusId: params?.statusId ?? null,
    });
  }
}
