import { HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BaseApiService } from '@core/services/base-api.service';
import { SpringPage } from '@models/page.model';
import { toPageHttpParams } from '@shared/utils/http-params.utils';
import { cachedRequest } from '@shared/utils/service-cache.utils';
import { Observable, tap } from 'rxjs';

import {
  AccessibilityResource,
  AccessibilityResourceInput,
  AccessibilityResourcePageParams,
} from '../accessibility.model';

export abstract class AccessibilityResourceService extends BaseApiService {
  private readonly pagesCache = new Map<string, Observable<SpringPage<AccessibilityResource>>>();

  getAll(params?: AccessibilityResourcePageParams): Observable<SpringPage<AccessibilityResource>> {
    const requestFactory = () =>
      this.http.get<SpringPage<AccessibilityResource>>(this.url(), {
        params: this.toHttpParams(params),
      });
    if (params?.search?.trim()) return requestFactory();
    return cachedRequest(this.pagesCache, this.cacheKey(params), requestFactory);
  }

  getById(id: number): Observable<AccessibilityResource> {
    return this.http.get<AccessibilityResource>(this.url(id));
  }

  create(input: AccessibilityResourceInput): Observable<AccessibilityResource> {
    return this.http
      .post<AccessibilityResource>(this.url(), input)
      .pipe(tap(() => this.clearCache()));
  }

  update(id: number, input: AccessibilityResourceInput): Observable<AccessibilityResource> {
    return this.http
      .put<AccessibilityResource>(this.url(id), input)
      .pipe(tap(() => this.clearCache()));
  }

  deactivate(id: number): Observable<void> {
    return this.http.delete<void>(this.url(id)).pipe(tap(() => this.clearCache()));
  }

  reactivate(id: number): Observable<AccessibilityResource> {
    return this.http
      .put<AccessibilityResource>(this.url('reactivate', id), null)
      .pipe(tap(() => this.clearCache()));
  }

  clearCache(): void {
    this.pagesCache.clear();
  }

  private toHttpParams(params?: AccessibilityResourcePageParams): HttpParams | undefined {
    if (!params) return undefined;
    let httpParams = toPageHttpParams(params) ?? new HttpParams();
    const criteria: Record<string, string | number | undefined> = {
      name: params.name,
      nameEs: params.nameEs,
      statusId: params.statusId,
      search: params.search?.trim() || undefined,
    };
    Object.entries(criteria).forEach(([key, value]) => {
      if (value !== undefined && value !== '') httpParams = httpParams.set(key, String(value));
    });
    return httpParams.keys().length ? httpParams : undefined;
  }

  private cacheKey(params?: AccessibilityResourcePageParams): string {
    return JSON.stringify({
      page: params?.page ?? null,
      size: params?.size ?? null,
      sort: Array.isArray(params?.sort) ? params.sort.join('|') : (params?.sort ?? ''),
      name: params?.name ?? '',
      nameEs: params?.nameEs ?? '',
      statusId: params?.statusId ?? null,
    });
  }
}

@Injectable({ providedIn: 'root' })
export class ClassificationSegmentsService extends AccessibilityResourceService {
  protected override readonly ENTITY_URI = 'classification-segment';
}

@Injectable({ providedIn: 'root' })
export class ComplianceSituationsService extends AccessibilityResourceService {
  protected override readonly ENTITY_URI = 'compliance-situation';
}
