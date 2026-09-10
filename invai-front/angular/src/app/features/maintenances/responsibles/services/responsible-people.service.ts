import { Injectable, inject } from '@angular/core';
import { BaseApiService } from '@core/services/base-api.service';
import { SpringPage } from '@models/page.model';
import { cachedRequest, pageParamsCacheKey } from '@shared/utils/service-cache.utils';
import { Observable, tap } from 'rxjs';

import {
  ResponsiblePerson,
  ResponsiblePersonCombinedSearchOutput,
  ResponsiblePersonCombinedSearchParams,
  ResponsiblePersonInput,
  ResponsiblePersonPageParams,
  SoffidPersonCandidate,
} from '../responsibles.model';
import { ResponsibleDataChangesService } from './responsible-data-changes.service';
import { responsibleCacheKey, responsibleHttpParams } from './responsible-service.utils';

@Injectable({ providedIn: 'root' })
export class ResponsiblePeopleService extends BaseApiService {
  protected override readonly ENTITY_URI = 'person';
  private readonly changes = inject(ResponsibleDataChangesService);
  private readonly pages = new Map<string, Observable<SpringPage<ResponsiblePerson>>>();
  private readonly combinedPages = new Map<string, Observable<ResponsiblePersonCombinedSearchOutput>>();

  constructor() {
    super();
    this.changes.companies.subscribe(() => this.clearCache());
    this.changes.people.subscribe(() => this.clearCache());
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
      this.http.get<SpringPage<ResponsiblePerson>>(this.url('database-search'), {
        params: responsibleHttpParams(normalizedParams, criteria),
      });

    if (normalizedParams?.search) return requestFactory();

    return cachedRequest(
      this.pages,
      responsibleCacheKey(normalizedParams, criteria),
      requestFactory,
    );
  }

  searchCombined(
    params: ResponsiblePersonCombinedSearchParams,
  ): Observable<ResponsiblePersonCombinedSearchOutput> {
    const search = params.search?.trim() || undefined;
    const requestFactory = () =>
      this.http.get<ResponsiblePersonCombinedSearchOutput>(this.url('all'), {
        params: responsibleHttpParams(params, { search }),
      });

    if (search) return requestFactory();

    return cachedRequest(this.combinedPages, pageParamsCacheKey(params), requestFactory);
  }

  getById(id: number): Observable<ResponsiblePerson> {
    return this.http.get<ResponsiblePerson>(this.url(id));
  }

  searchSoffid(fullName: string): Observable<SpringPage<SoffidPersonCandidate>> {
    return this.http.get<SpringPage<SoffidPersonCandidate>>(this.url('soffid-search'), {
      params: { fullName: fullName.trim(), page: 0, size: 20 },
    });
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
    this.combinedPages.clear();
  }

  private changed(): void {
    this.clearCache();
    this.changes.peopleChanged();
  }
}
