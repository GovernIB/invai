import { HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BaseApiService } from '@core/services/base-api.service';
import { SpringPage } from '@models/page.model';
import { toPageHttpParams } from '@shared/utils/http-params.utils';
import { cachedRequest } from '@shared/utils/service-cache.utils';
import { Observable, tap } from 'rxjs';

import { Category, CategoryInput, CategoryPageParams } from '../categories.model';

@Injectable({ providedIn: 'root' })
export class CategoriesService extends BaseApiService {
  protected override readonly ENTITY_URI = 'category';

  private readonly _categoriesCache = new Map<string, Observable<SpringPage<Category>>>();

  getAll(params?: CategoryPageParams): Observable<SpringPage<Category>> {
    const requestFactory = () =>
      this.http.get<SpringPage<Category>>(this.url(), {
        params: this.toHttpParams(params),
      });

    if (params?.quickSearch?.trim()) return requestFactory();

    return cachedRequest(this._categoriesCache, this.pageCacheKey(params), requestFactory);
  }

  getById(id: number): Observable<Category> {
    return this.http.get<Category>(this.url(id));
  }

  create(payload: CategoryInput): Observable<Category> {
    return this.http.post<Category>(this.url(), payload).pipe(tap(() => this.clearCache()));
  }

  update(id: number, payload: CategoryInput): Observable<Category> {
    return this.http.put<Category>(this.url(id), payload).pipe(tap(() => this.clearCache()));
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(this.url(id)).pipe(tap(() => this.clearCache()));
  }

  clearCache(): void {
    this._categoriesCache.clear();
  }

  private toHttpParams(params?: CategoryPageParams): HttpParams | undefined {
    if (!params) return undefined;

    let httpParams = toPageHttpParams(params) ?? new HttpParams();
    const criteria: Record<string, string | undefined> = {
      quickSearch: params.quickSearch?.trim() || undefined,
      name: params.name,
      nameEs: params.nameEs,
    };

    Object.entries(criteria).forEach(([key, value]) => {
      if (value !== undefined && value !== '') httpParams = httpParams.set(key, value);
    });

    return httpParams.keys().length ? httpParams : undefined;
  }

  private pageCacheKey(params?: CategoryPageParams): string {
    const sort = Array.isArray(params?.sort) ? params.sort.join('|') : (params?.sort ?? '');

    return JSON.stringify({
      page: params?.page ?? null,
      size: params?.size ?? null,
      sort,
      name: params?.name ?? '',
      nameEs: params?.nameEs ?? '',
    });
  }
}
