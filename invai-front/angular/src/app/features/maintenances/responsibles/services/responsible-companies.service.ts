import { Injectable, inject } from '@angular/core';
import { BaseApiService } from '@core/services/base-api.service';
import { SpringPage } from '@models/page.model';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';
import { cachedRequest } from '@shared/utils/service-cache.utils';
import { Observable, map, tap } from 'rxjs';

import {
  ResponsibleCompany,
  ResponsibleCompanyOption,
  ResponsibleNameInput,
  ResponsibleNamePageParams,
} from '../responsibles.model';
import { ResponsibleDataChangesService } from './responsible-data-changes.service';
import { responsibleCacheKey, responsibleHttpParams } from './responsible-service.utils';

@Injectable({ providedIn: 'root' })
export class ResponsibleCompaniesService extends BaseApiService {
  protected override readonly ENTITY_URI = 'company';
  private readonly changes = inject(ResponsibleDataChangesService);
  private readonly pages = new Map<string, Observable<SpringPage<ResponsibleCompany>>>();

  getPage(params?: ResponsibleNamePageParams): Observable<SpringPage<ResponsibleCompany>> {
    const criteria = { name: params?.name, statusId: params?.statusId, search: params?.search };
    return cachedRequest(this.pages, responsibleCacheKey(params, criteria), () =>
      this.http.get<SpringPage<ResponsibleCompany>>(this.url(), {
        params: responsibleHttpParams(params, criteria),
      }),
    );
  }

  getById(id: number): Observable<ResponsibleCompany> {
    return this.http.get<ResponsibleCompany>(this.url(id));
  }

  getOptions(activeOnly: boolean): Observable<ResponsibleCompanyOption[]> {
    return this.getPage({
      page: 0,
      size: 1000,
      sort: 'name,asc',
      statusId: activeOnly ? SoftDeleteStatus.ACTIVE : undefined,
    }).pipe(map((page) => page.content.map(({ id, name }) => ({ id, label: name }))));
  }

  create(input: ResponsibleNameInput): Observable<ResponsibleCompany> {
    return this.http.post<ResponsibleCompany>(this.url(), input).pipe(tap(() => this.changed()));
  }

  update(id: number, input: ResponsibleNameInput): Observable<ResponsibleCompany> {
    return this.http.put<ResponsibleCompany>(this.url(id), input).pipe(tap(() => this.changed()));
  }

  deactivate(id: number): Observable<void> {
    return this.http.delete<void>(this.url(id)).pipe(tap(() => this.changed()));
  }

  reactivate(id: number): Observable<ResponsibleCompany> {
    return this.http
      .put<ResponsibleCompany>(this.url('reactivate', id), {})
      .pipe(tap(() => this.changed()));
  }

  clearCache(): void {
    this.pages.clear();
  }

  private changed(): void {
    this.clearCache();
    this.changes.companiesChanged();
  }
}
