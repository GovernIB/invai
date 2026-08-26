import { Injectable, inject } from '@angular/core';
import { BaseApiService } from '@core/services/base-api.service';
import { ResponsibleDataChangesService } from '@features/maintenances/responsibles/services/responsible-data-changes.service';
import { SpringPage } from '@models/page.model';
import { cachedRequest } from '@shared/utils/service-cache.utils';
import { Observable, tap } from 'rxjs';

import {
  ApplicationAssignmentDeactivateInput,
  ApplicationAssignedResponsibleOutput,
  ApplicationResponsibleInput,
  ApplicationResponsibleOutput,
  ApplicationResponsiblePageParams,
} from '../applications.model';
import {
  responsibleCacheKey,
  responsibleHttpParams,
} from '../../maintenances/responsibles/services/responsible-service.utils';

@Injectable({ providedIn: 'root' })
export class ApplicationResponsiblesService extends BaseApiService {
  protected override readonly ENTITY_URI = 'application/responsible';
  private readonly cache = new Map<string, Observable<SpringPage<ApplicationResponsibleOutput>>>();
  private readonly changes = inject(ResponsibleDataChangesService);

  constructor() {
    super();
    this.changes.people.subscribe(() => this.clearCache());
    this.changes.assignments.subscribe(() => this.clearCache());
  }

  getPage(
    params: ApplicationResponsiblePageParams,
  ): Observable<SpringPage<ApplicationResponsibleOutput>> {
    const criteria = {
      statusId: params.statusId,
      personId: params.personId,
      responsibleTypeId: params.responsibleTypeId,
      search: params.search,
    };
    const cacheKey = `${params.appResponsibleAuthorizedId};${responsibleCacheKey(
      params,
      criteria,
    )}`;
    return cachedRequest(this.cache, cacheKey, () =>
      this.http.get<SpringPage<ApplicationResponsibleOutput>>(
        this.url(params.appResponsibleAuthorizedId),
        { params: responsibleHttpParams(params, criteria) },
      ),
    );
  }

  create(input: ApplicationResponsibleInput): Observable<ApplicationAssignedResponsibleOutput> {
    return this.http
      .post<ApplicationAssignedResponsibleOutput>(this.url(), input)
      .pipe(tap(() => this.clearCache()));
  }

  update(
    id: number,
    input: ApplicationResponsibleInput,
  ): Observable<ApplicationAssignedResponsibleOutput> {
    return this.http
      .put<ApplicationAssignedResponsibleOutput>(this.url(id), input)
      .pipe(tap(() => this.clearCache()));
  }

  deactivate(
    id: number,
    input: ApplicationAssignmentDeactivateInput,
  ): Observable<ApplicationResponsibleOutput> {
    return this.http
      .put<ApplicationResponsibleOutput>(this.url('deactivate', id), input)
      .pipe(tap(() => this.clearCache()));
  }

  reactivate(id: number): Observable<ApplicationAssignedResponsibleOutput> {
    return this.http
      .put<ApplicationAssignedResponsibleOutput>(this.url('reactivate', id), {})
      .pipe(tap(() => this.clearCache()));
  }

  clearCache(): void {
    this.cache.clear();
  }
}
