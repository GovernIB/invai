import { Injectable, inject } from '@angular/core';
import { BaseApiService } from '@core/services/base-api.service';
import { SpringPage } from '@models/page.model';
import { cachedRequest } from '@shared/utils/service-cache.utils';
import { Observable, tap } from 'rxjs';

import {
  ResponsibleAuthorization,
  ResponsibleAuthorizationInput,
  ResponsibleAuthorizationPageParams,
} from '../responsibles.model';
import { ResponsibleDataChangesService } from './responsible-data-changes.service';
import { responsibleCacheKey, responsibleHttpParams } from './responsible-service.utils';

@Injectable({ providedIn: 'root' })
export class ResponsibleAuthorizationTypesService extends BaseApiService {
  protected override readonly ENTITY_URI = 'authorization-type';
  private readonly changes = inject(ResponsibleDataChangesService);
  private readonly pages = new Map<string, Observable<SpringPage<ResponsibleAuthorization>>>();

  getPage(
    params?: ResponsibleAuthorizationPageParams,
  ): Observable<SpringPage<ResponsibleAuthorization>> {
    const criteria = {
      name: params?.name,
      nameEs: params?.nameEs,
      statusId: params?.statusId,
      search: params?.search,
    };
    return cachedRequest(this.pages, responsibleCacheKey(params, criteria), () =>
      this.http.get<SpringPage<ResponsibleAuthorization>>(this.url(), {
        params: responsibleHttpParams(params, criteria),
      }),
    );
  }

  getById(id: number): Observable<ResponsibleAuthorization> {
    return this.http.get<ResponsibleAuthorization>(this.url(id));
  }

  create(input: ResponsibleAuthorizationInput): Observable<ResponsibleAuthorization> {
    return this.http
      .post<ResponsibleAuthorization>(this.url(), input)
      .pipe(tap(() => this.changed()));
  }

  update(id: number, input: ResponsibleAuthorizationInput): Observable<ResponsibleAuthorization> {
    return this.http
      .put<ResponsibleAuthorization>(this.url(id), input)
      .pipe(tap(() => this.changed()));
  }

  deactivate(id: number): Observable<void> {
    return this.http.delete<void>(this.url(id)).pipe(tap(() => this.changed()));
  }

  reactivate(id: number): Observable<ResponsibleAuthorization> {
    return this.http
      .put<ResponsibleAuthorization>(this.url('reactivate', id), {})
      .pipe(tap(() => this.changed()));
  }

  clearCache(): void {
    this.pages.clear();
  }

  private changed(): void {
    this.clearCache();
    this.changes.authorizationTypesChanged();
  }
}
