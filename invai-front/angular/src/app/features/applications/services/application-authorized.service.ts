import { Injectable, inject } from '@angular/core';
import { BaseApiService } from '@core/services/base-api.service';
import { ResponsibleDataChangesService } from '@features/maintenances/responsibles/services/responsible-data-changes.service';
import { SpringPage } from '@models/page.model';
import { cachedRequest } from '@shared/utils/service-cache.utils';
import { Observable, tap } from 'rxjs';

import {
  ApplicationAssignmentDeactivateInput,
  ApplicationAuthorizedInput,
  ApplicationAuthorizedOutput,
  ApplicationAuthorizedPageParams,
} from '../applications.model';
import {
  responsibleCacheKey,
  responsibleHttpParams,
} from '../../maintenances/responsibles/services/responsible-service.utils';

@Injectable({ providedIn: 'root' })
export class ApplicationAuthorizedService extends BaseApiService {
  protected override readonly ENTITY_URI = 'application/authorized';
  private readonly cache = new Map<string, Observable<SpringPage<ApplicationAuthorizedOutput>>>();
  private readonly changes = inject(ResponsibleDataChangesService);

  constructor() {
    super();
    this.changes.people.subscribe(() => this.clearCache());
    this.changes.authorizationTypes.subscribe(() => this.clearCache());
    this.changes.assignments.subscribe(() => this.clearCache());
  }

  getPage(
    params: ApplicationAuthorizedPageParams,
  ): Observable<SpringPage<ApplicationAuthorizedOutput>> {
    const criteria = {
      statusId: params.statusId,
      personId: params.personId,
      search: params.search,
    };
    const cacheKey = `${params.appResponsibleAuthorizedId};${responsibleCacheKey(
      params,
      criteria,
    )}`;
    return cachedRequest(this.cache, cacheKey, () =>
      this.http.get<SpringPage<ApplicationAuthorizedOutput>>(
        this.url(params.appResponsibleAuthorizedId),
        { params: responsibleHttpParams(params, criteria) },
      ),
    );
  }

  create(input: ApplicationAuthorizedInput): Observable<ApplicationAuthorizedOutput> {
    return this.http
      .post<ApplicationAuthorizedOutput>(this.url(), input)
      .pipe(tap(() => this.clearCache()));
  }

  update(id: number, input: ApplicationAuthorizedInput): Observable<ApplicationAuthorizedOutput> {
    return this.http
      .put<ApplicationAuthorizedOutput>(this.url(id), input)
      .pipe(tap(() => this.clearCache()));
  }

  deactivate(id: number, input: ApplicationAssignmentDeactivateInput): Observable<void> {
    return this.http
      .put<void>(this.url('deactivate', id), input)
      .pipe(tap(() => this.clearCache()));
  }

  reactivate(id: number): Observable<ApplicationAuthorizedOutput> {
    return this.http
      .put<ApplicationAuthorizedOutput>(this.url('reactivate', id), {})
      .pipe(tap(() => this.clearCache()));
  }

  clearCache(): void {
    this.cache.clear();
  }
}
