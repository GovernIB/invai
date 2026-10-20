import { HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { BaseApiService } from '@core/services/base-api.service';
import { SpringPage } from '@models/page.model';
import { toPageHttpParams } from '@shared/utils/http-params.utils';
import { cachedRequest, pageParamsCacheKey } from '@shared/utils/service-cache.utils';
import { Observable, tap } from 'rxjs';

import {
  ApplicationDevelopmentWebContextPageParams,
  ApplicationWebContextInput,
  ApplicationWebContextOutput,
} from '../applications.model';
import { ApplicationsService } from './applications.service';
import { ApplicationWebContextsService } from './application-security.service';

@Injectable({ providedIn: 'root' })
export class ApplicationDevelopmentWebContextsService extends BaseApiService {
  protected override readonly ENTITY_URI = 'application/development/web-context';
  private readonly applicationsService = inject(ApplicationsService);
  private readonly securityWebContextsService = inject(ApplicationWebContextsService);
  private readonly pagesCache = new Map<string, Observable<SpringPage<ApplicationWebContextOutput>>>();

  getPage(params: ApplicationDevelopmentWebContextPageParams): Observable<SpringPage<ApplicationWebContextOutput>> {
    const { appDevelopmentId, statusId, ...pageParams } = params;
    const key = `${appDevelopmentId};status=${statusId ?? ''};${pageParamsCacheKey(pageParams)}`;
    let httpParams = toPageHttpParams(pageParams) ?? new HttpParams();
    if (statusId != null) httpParams = httpParams.set('statusId', String(statusId));

    return cachedRequest(this.pagesCache, key, () =>
      this.http.get<SpringPage<ApplicationWebContextOutput>>(this.url(appDevelopmentId), { params: httpParams }),
    );
  }

  create(payload: ApplicationWebContextInput): Observable<ApplicationWebContextOutput> {
    return this.http.post<ApplicationWebContextOutput>(this.url(), payload).pipe(tap(() => this.invalidate()));
  }

  update(id: number, payload: ApplicationWebContextInput): Observable<ApplicationWebContextOutput> {
    return this.http.put<ApplicationWebContextOutput>(this.url(id), payload).pipe(tap(() => this.invalidate()));
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(this.url(id)).pipe(tap(() => this.invalidate()));
  }

  clearCache(): void {
    this.pagesCache.clear();
  }

  private invalidate(): void {
    this.clearCache();
    this.securityWebContextsService.clearCache();
    this.applicationsService.clearCache();
  }
}
