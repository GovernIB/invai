import { HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BaseApiService } from '@core/services/base-api.service';
import { SpringPage } from '@models/page.model';
import { toPageHttpParams } from '@shared/utils/http-params.utils';
import { cachedRequest } from '@shared/utils/service-cache.utils';
import { Observable, tap } from 'rxjs';

import { Environment, EnvironmentInput, EnvironmentPageParams } from '../environments.model';

@Injectable({ providedIn: 'root' })
export class EnvironmentsService extends BaseApiService {
  protected override readonly ENTITY_URI = 'environment';

  private readonly _environmentsCache = new Map<string, Observable<SpringPage<Environment>>>();

  getAll(params?: EnvironmentPageParams): Observable<SpringPage<Environment>> {
    const requestFactory = () =>
      this.http.get<SpringPage<Environment>>(this.url(), {
        params: this.toHttpParams(params),
      });

    if (params?.search?.trim()) return requestFactory();

    return cachedRequest(this._environmentsCache, this.pageCacheKey(params), requestFactory);
  }

  getById(id: number): Observable<Environment> {
    return this.http.get<Environment>(this.url(id));
  }

  create(payload: EnvironmentInput): Observable<Environment> {
    return this.http.post<Environment>(this.url(), payload).pipe(tap(() => this.clearCache()));
  }

  update(id: number, payload: EnvironmentInput): Observable<Environment> {
    return this.http.put<Environment>(this.url(id), payload).pipe(tap(() => this.clearCache()));
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(this.url(id)).pipe(tap(() => this.clearCache()));
  }

  reactivate(id: number): Observable<Environment> {
    return this.http
      .put<Environment>(this.url('reactivate', id), null)
      .pipe(tap(() => this.clearCache()));
  }

  clearCache(): void {
    this._environmentsCache.clear();
  }

  private toHttpParams(params?: EnvironmentPageParams): HttpParams | undefined {
    if (!params) return undefined;

    let httpParams = toPageHttpParams(params) ?? new HttpParams();
    const criteria: Record<string, string | number | undefined> = {
      code: params.code,
      name: params.name,
      nameEs: params.nameEs,
      statusId: params.statusId,
      search: params.search?.trim() || undefined,
    };

    Object.entries(criteria).forEach(([key, value]) => {
      if (value !== undefined && value !== '') httpParams = httpParams.set(key, String(value));
    });

    return httpParams.keys().length ? httpParams : undefined;
  }

  private pageCacheKey(params?: EnvironmentPageParams): string {
    const sort = Array.isArray(params?.sort) ? params.sort.join('|') : (params?.sort ?? '');

    return JSON.stringify({
      page: params?.page ?? null,
      size: params?.size ?? null,
      sort,
      code: params?.code ?? '',
      name: params?.name ?? '',
      nameEs: params?.nameEs ?? '',
      statusId: params?.statusId ?? null,
    });
  }
}
