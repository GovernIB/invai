import { Injectable, inject } from '@angular/core';
import { BaseApiService } from '@core/services/base-api.service';
import { SpringPage } from '@models/page.model';
import { cachedRequest } from '@shared/utils/service-cache.utils';
import { Observable, tap } from 'rxjs';

import {
  ResponsiblePerson,
  ResponsiblePersonInput,
  ResponsiblePersonPageParams,
} from '../responsibles.model';
import { ResponsibleDataChangesService } from './responsible-data-changes.service';
import { responsibleCacheKey, responsibleHttpParams } from './responsible-service.utils';

@Injectable({ providedIn: 'root' })
export class ResponsiblePeopleService extends BaseApiService {
  protected override readonly ENTITY_URI = 'person';
  private readonly changes = inject(ResponsibleDataChangesService);
  private readonly pages = new Map<string, Observable<SpringPage<ResponsiblePerson>>>();

  constructor() {
    super();
    this.changes.companies.subscribe(() => this.clearCache());
  }

  getPage(params?: ResponsiblePersonPageParams): Observable<SpringPage<ResponsiblePerson>> {
    const normalizedParams = params?.search
      ? { ...params, search: params.search.trim() || undefined }
      : params;
    const criteria = {
      companyId: normalizedParams?.companyId,
      excludeId: normalizedParams?.excludeId,
      firstName: normalizedParams?.firstName,
      lastName: normalizedParams?.lastName,
      email: normalizedParams?.email,
      statusId: normalizedParams?.statusId,
      search: normalizedParams?.search,
    };
    const requestFactory = () =>
      this.http.get<SpringPage<ResponsiblePerson>>(this.url(), {
        params: responsibleHttpParams(normalizedParams, criteria),
      });

    if (normalizedParams?.search) return requestFactory();

    return cachedRequest(
      this.pages,
      responsibleCacheKey(normalizedParams, criteria),
      requestFactory,
    );
  }

  getById(id: number): Observable<ResponsiblePerson> {
    return this.http.get<ResponsiblePerson>(this.url(id));
  }

  create(input: ResponsiblePersonInput): Observable<ResponsiblePerson> {
    return this.http.post<ResponsiblePerson>(this.url(), input).pipe(tap(() => this.changed()));
  }

  update(id: number, input: ResponsiblePersonInput): Observable<ResponsiblePerson> {
    return this.http.put<ResponsiblePerson>(this.url(id), input).pipe(tap(() => this.changed()));
  }

  deactivate(id: number): Observable<void> {
    return this.http.delete<void>(this.url(id)).pipe(tap(() => this.changed()));
  }

  reactivate(id: number): Observable<ResponsiblePerson> {
    return this.http
      .put<ResponsiblePerson>(this.url('reactivate', id), {})
      .pipe(tap(() => this.changed()));
  }

  clearCache(): void {
    this.pages.clear();
  }

  private changed(): void {
    this.clearCache();
    this.changes.peopleChanged();
  }
}
