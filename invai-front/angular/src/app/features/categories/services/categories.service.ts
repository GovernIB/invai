import { Injectable } from '@angular/core';
import { BaseApiService } from '@core/services/base-api.service';
import { SpringPage } from '@models/page.model';
import { toPageHttpParams } from '@shared/utils/http-params.utils';
import { cachedRequest, pageParamsCacheKey } from '@shared/utils/service-cache.utils';
import { Observable, tap } from 'rxjs';

import { Category, CategoryInput, CategoryPageParams } from '../categories.model';

@Injectable({ providedIn: 'root' })
export class CategoriesService extends BaseApiService {
  protected override readonly ENTITY_URI = 'category';

  private readonly _categoriesCache = new Map<string, Observable<SpringPage<Category>>>();

  getAll(params?: CategoryPageParams): Observable<SpringPage<Category>> {
    return cachedRequest(this._categoriesCache, pageParamsCacheKey(params), () =>
      this.http.get<SpringPage<Category>>(this.url(), {
        params: toPageHttpParams(params),
      }),
    );
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
}
