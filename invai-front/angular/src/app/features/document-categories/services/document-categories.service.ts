import { HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BaseApiService } from '@core/services/base-api.service';
import { SpringPage } from '@models/page.model';
import { toPageHttpParams } from '@shared/utils/http-params.utils';
import { cachedRequest } from '@shared/utils/service-cache.utils';
import { Observable, tap } from 'rxjs';

import {
  DocumentCategory,
  DocumentCategoryInput,
  DocumentCategoryPageParams,
} from '../document-categories.model';

// Categories and subcategories share the same endpoints; a page only lists categories, each one
// with all its subcategories.
@Injectable({ providedIn: 'root' })
export class DocumentCategoriesService extends BaseApiService {
  protected override readonly ENTITY_URI = 'documentation/category';

  private readonly pagesCache = new Map<string, Observable<SpringPage<DocumentCategory>>>();

  getAll(params?: DocumentCategoryPageParams): Observable<SpringPage<DocumentCategory>> {
    const requestFactory = () =>
      this.http.get<SpringPage<DocumentCategory>>(this.url(), {
        params: this.toHttpParams(params),
      });

    if (params?.search?.trim()) return requestFactory();
    return cachedRequest(this.pagesCache, this.pageCacheKey(params), requestFactory);
  }

  getById(id: number): Observable<DocumentCategory> {
    return this.http.get<DocumentCategory>(this.url(id));
  }

  create(payload: DocumentCategoryInput): Observable<DocumentCategory> {
    return this.http
      .post<DocumentCategory>(this.url(), payload)
      .pipe(tap(() => this.clearCache()));
  }

  update(id: number, payload: DocumentCategoryInput): Observable<DocumentCategory> {
    return this.http
      .put<DocumentCategory>(this.url(id), payload)
      .pipe(tap(() => this.clearCache()));
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(this.url(id)).pipe(tap(() => this.clearCache()));
  }

  reactivate(id: number): Observable<DocumentCategory> {
    return this.http
      .put<DocumentCategory>(this.url('reactivate', id), null)
      .pipe(tap(() => this.clearCache()));
  }

  clearCache(): void {
    this.pagesCache.clear();
  }

  private toHttpParams(params?: DocumentCategoryPageParams): HttpParams | undefined {
    if (!params) return undefined;
    let httpParams = toPageHttpParams(params) ?? new HttpParams();
    const criteria: Record<string, string | number | undefined> = {
      name: params.name?.trim() || undefined,
      statusId: params.statusId,
      search: params.search?.trim() || undefined,
    };
    Object.entries(criteria).forEach(([key, value]) => {
      if (value !== undefined) httpParams = httpParams.set(key, String(value));
    });
    return httpParams.keys().length ? httpParams : undefined;
  }

  private pageCacheKey(params?: DocumentCategoryPageParams): string {
    const sort = Array.isArray(params?.sort) ? params.sort.join('|') : (params?.sort ?? '');
    return JSON.stringify({
      page: params?.page ?? null,
      size: params?.size ?? null,
      sort,
      name: params?.name?.trim() ?? '',
      statusId: params?.statusId ?? null,
    });
  }
}
