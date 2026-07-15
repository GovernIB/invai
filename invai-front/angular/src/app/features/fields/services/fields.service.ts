import { Injectable } from '@angular/core';
import { BaseApiService } from '@core/services/base-api.service';
import { SpringPage } from '@models/page.model';
import { toPageHttpParams } from '@shared/utils/http-params.utils';
import { cachedRequest, pageParamsCacheKey } from '@shared/utils/service-cache.utils';
import { Observable, tap } from 'rxjs';

import { Field, FieldInput, FieldPageParams } from '../fields.model';

@Injectable({ providedIn: 'root' })
export class FieldsService extends BaseApiService {
  protected override readonly ENTITY_URI = 'field';

  private readonly _fieldsCache = new Map<string, Observable<SpringPage<Field>>>();

  getAll(params?: FieldPageParams): Observable<SpringPage<Field>> {
    return cachedRequest(this._fieldsCache, pageParamsCacheKey(params), () =>
      this.http.get<SpringPage<Field>>(this.url(), {
        params: toPageHttpParams(params),
      }),
    );
  }

  getById(id: number): Observable<Field> {
    return this.http.get<Field>(this.url(id));
  }

  create(payload: FieldInput): Observable<Field> {
    return this.http.post<Field>(this.url(), payload).pipe(tap(() => this.clearCache()));
  }

  update(id: number, payload: FieldInput): Observable<Field> {
    return this.http.put<Field>(this.url(id), payload).pipe(tap(() => this.clearCache()));
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(this.url(id)).pipe(tap(() => this.clearCache()));
  }

  clearCache(): void {
    this._fieldsCache.clear();
  }
}
