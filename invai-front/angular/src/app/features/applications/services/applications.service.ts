import { HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BaseApiService } from '@core/services/base-api.service';
import { SpringPage } from '@models/page.model';
import { toPageHttpParams } from '@shared/utils/http-params.utils';
import { cachedRequest } from '@shared/utils/service-cache.utils';
import { Observable, map, tap } from 'rxjs';

import {
  Application,
  ApplicationInput,
  ApplicationOutput,
  ApplicationPageParams,
} from '../applications.model';

@Injectable({ providedIn: 'root' })
export class ApplicationsService extends BaseApiService {
  protected override readonly ENTITY_URI = 'application';

  private readonly _applicationsCache = new Map<string, Observable<SpringPage<Application>>>();
  private readonly _applicationDetailsCache = new Map<string, Observable<ApplicationOutput>>();

  getAll(): Observable<Application[]> {
    return this.getPage().pipe(map((page) => page.content));
  }

  getPage(params?: ApplicationPageParams): Observable<SpringPage<Application>> {
    const requestFactory = () =>
      this.http
        .get<SpringPage<ApplicationOutput>>(this.url(), {
          params: this.toHttpParams(params),
        })
        .pipe(
          map((page) => ({
            ...page,
            content: page.content.map((application) => this.toApplication(application)),
          })),
        );

    if (params?.quickSearch?.trim()) {
      return requestFactory();
    }

    return cachedRequest(this._applicationsCache, this.pageCacheKey(params), requestFactory);
  }

  getById(id: number): Observable<ApplicationOutput> {
    return cachedRequest(this._applicationDetailsCache, String(id), () =>
      this.http.get<ApplicationOutput>(this.url(id)),
    );
  }

  create(payload: ApplicationInput): Observable<ApplicationOutput> {
    return this.http
      .post<ApplicationOutput>(this.url(), payload)
      .pipe(tap(() => this.clearCache()));
  }

  update(id: number, payload: ApplicationInput): Observable<ApplicationOutput> {
    return this.http
      .put<ApplicationOutput>(this.url(id), payload)
      .pipe(tap(() => this.clearCache()));
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(this.url(id)).pipe(tap(() => this.clearCache()));
  }

  clearCache(): void {
    this._applicationsCache.clear();
    this._applicationDetailsCache.clear();
  }

  toApplication(response: ApplicationOutput): Application {
    return {
      id: String(response.id),
      code: response.code ?? '',
      prefix: response.prefix ?? '',
      name: response.name ?? '',
      category: response.category?.name ?? '',
      informationSystem: response.systemType?.name ?? '',
      scope: response.field?.name ?? '',
      commission: response.csCommission?.name ?? '',
      administrativeUnit: response.admUnit?.name ?? '',
      status: response.status?.name ?? '',
      description: response.description ?? '',
      creationDate: response.createdAt ?? '',
      modificationDate: response.updatedAt ?? '',
      withdrawalDate: response.expirationDate ?? '',
      categoryId: response.category?.id,
      informationSystemId: response.systemType?.id,
      scopeId: response.field?.id,
      commissionId: response.csCommission?.id,
      administrativeUnitId: response.admUnit?.id,
      statusId: response.status?.id,
    };
  }

  private toHttpParams(params?: ApplicationPageParams): HttpParams | undefined {
    if (!params) return undefined;

    let httpParams = toPageHttpParams(params) ?? new HttpParams();

    const criteria: Record<string, string | number | undefined> = {
      prefix: params.prefix,
      applicationName: params.applicationName,
      categoryId: params.categoryId,
      systemTypeId: params.systemTypeId,
      fieldId: params.fieldId,
      commissionId: params.commissionId,
      admUnitId: params.admUnitId,
      statusId: params.statusId,
      description: params.description,
      quickSearch: params.quickSearch?.trim() || undefined,
    };

    Object.entries(criteria).forEach(([key, value]) => {
      if (value !== undefined && value !== '') {
        httpParams = httpParams.set(key, String(value));
      }
    });

    return httpParams.keys().length ? httpParams : undefined;
  }

  private pageCacheKey(params?: ApplicationPageParams): string {
    const sort = Array.isArray(params?.sort) ? params.sort.join('|') : (params?.sort ?? '');

    return JSON.stringify({
      page: params?.page ?? null,
      size: params?.size ?? null,
      sort,
      prefix: params?.prefix ?? '',
      applicationName: params?.applicationName ?? '',
      categoryId: params?.categoryId ?? null,
      systemTypeId: params?.systemTypeId ?? null,
      fieldId: params?.fieldId ?? null,
      commissionId: params?.commissionId ?? null,
      admUnitId: params?.admUnitId ?? null,
      statusId: params?.statusId ?? null,
      description: params?.description ?? '',
    });
  }
}
