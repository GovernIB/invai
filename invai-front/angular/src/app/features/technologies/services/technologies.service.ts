import { HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BaseApiService } from '@core/services/base-api.service';
import { SpringPage } from '@models/page.model';
import { toPageHttpParams } from '@shared/utils/http-params.utils';
import { cachedRequest } from '@shared/utils/service-cache.utils';
import { Observable, tap } from 'rxjs';

import {
  Technology,
  TechnologyInput,
  TechnologyPageParams,
} from '../technologies.model';

@Injectable({ providedIn: 'root' })
export class TechnologiesService extends BaseApiService {
  protected override readonly ENTITY_URI = 'technology';

  private readonly pagesCache = new Map<string, Observable<SpringPage<Technology>>>();

  getAll(params?: TechnologyPageParams): Observable<SpringPage<Technology>> {
    const requestFactory = () =>
      this.http.get<SpringPage<Technology>>(this.url(), {
        params: this.toHttpParams(params),
      });

    if (params?.search?.trim()) return requestFactory();
    return cachedRequest(this.pagesCache, this.pageCacheKey(params), requestFactory);
  }

  getById(id: number): Observable<Technology> {
    return this.http.get<Technology>(this.url(id));
  }

  create(payload: TechnologyInput): Observable<Technology> {
    return this.http
      .post<Technology>(this.url(), payload)
      .pipe(tap(() => this.clearCache()));
  }

  update(id: number, payload: TechnologyInput): Observable<Technology> {
    return this.http
      .put<Technology>(this.url(id), payload)
      .pipe(tap(() => this.clearCache()));
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(this.url(id)).pipe(tap(() => this.clearCache()));
  }

  reactivate(id: number): Observable<Technology> {
    return this.http
      .put<Technology>(this.url('reactivate', id), null)
      .pipe(tap(() => this.clearCache()));
  }

  clearCache(): void {
    this.pagesCache.clear();
  }

  private toHttpParams(params?: TechnologyPageParams): HttpParams | undefined {
    if (!params) return undefined;
    let httpParams = toPageHttpParams(params) ?? new HttpParams();
    const criteria: Record<string, string | number | undefined> = {
      name: params.name,
      layerId: params.layerId,
      statusId: params.statusId,
      search: params.search?.trim() || undefined,
    };
    Object.entries(criteria).forEach(([key, value]) => {
      if (value !== undefined && value !== '') httpParams = httpParams.set(key, String(value));
    });
    return httpParams.keys().length ? httpParams : undefined;
  }

  private pageCacheKey(params?: TechnologyPageParams): string {
    const sort = Array.isArray(params?.sort) ? params.sort.join('|') : (params?.sort ?? '');
    return JSON.stringify({
      page: params?.page ?? null,
      size: params?.size ?? null,
      sort,
      name: params?.name ?? '',
      layerId: params?.layerId ?? null,
      statusId: params?.statusId ?? null,
    });
  }
}
