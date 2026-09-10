import { HttpParams } from '@angular/common/http';
import { Injectable, LOCALE_ID, inject } from '@angular/core';
import { BaseApiService } from '@core/services/base-api.service';
import { ResponsibleDataChangesService } from '@features/maintenances/responsibles/services/responsible-data-changes.service';
import { SpringPage } from '@models/page.model';
import { toPageHttpParams } from '@shared/utils/http-params.utils';
import { cachedRequest } from '@shared/utils/service-cache.utils';
import { localizedName } from '@shared/utils/localized-name.utils';
import { Observable, map, tap } from 'rxjs';
import { readApplicationCompleteness } from '../application-completeness';
import { APPLICATION_DETAIL_CACHE_TTL_MS } from './application-cache.constants';

import {
  Application,
  ApplicationInput,
  ApplicationOutput,
  ApplicationPageParams,
  ApplicationStatus,
  ApplicationStatusCode,
} from '../applications.model';

const APPLICATION_STATUS_BY_CODE: Record<ApplicationStatusCode, ApplicationStatus> = {
  [ApplicationStatusCode.ACTIVE]: ApplicationStatus.ACTIVE,
  [ApplicationStatusCode.INACTIVE]: ApplicationStatus.INACTIVE,
};

@Injectable({ providedIn: 'root' })
export class ApplicationsService extends BaseApiService {
  protected override readonly ENTITY_URI = 'application';

  private readonly locale = inject(LOCALE_ID);
  private readonly responsibleChanges = inject(ResponsibleDataChangesService);

  private readonly _applicationsCache = new Map<string, Observable<SpringPage<Application>>>();
  private readonly detailsCache = new Map<string, Observable<ApplicationOutput>>();

  constructor() {
    super();
    this.responsibleChanges.assignments.subscribe(() => this.clearCache());
  }

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
    return cachedRequest(
      this.detailsCache,
      String(id),
      () => this.http.get<ApplicationOutput>(this.url(id)),
      APPLICATION_DETAIL_CACHE_TTL_MS,
    );
  }

  refreshById(id: number): Observable<ApplicationOutput> {
    this.detailsCache.delete(String(id));
    return this.getById(id);
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

  reactivate(id: number): Observable<ApplicationOutput> {
    return this.http
      .put<ApplicationOutput>(this.url('reactivate', id), {})
      .pipe(tap(() => this.clearCache()));
  }

  clearCache(): void {
    this._applicationsCache.clear();
    this.detailsCache.clear();
  }

  toApplication(response: ApplicationOutput): Application {
    const status = response.status ? APPLICATION_STATUS_BY_CODE[response.status] : null;

    return {
      id: String(response.id),
      code: response.code ?? '',
      prefix: response.prefix ?? '',
      name: response.name ?? '',
      category: response.category ? localizedName(response.category, this.locale) : '',
      informationSystem: response.systemType ? localizedName(response.systemType, this.locale) : '',
      scope: response.field ? localizedName(response.field, this.locale) : '',
      commission: response.csCommission ? localizedName(response.csCommission, this.locale) : '',
      administrativeUnit: response.admUnit
        ? response.admUnit.name || response.admUnit.code
        : '',
      department: response.department
        ? response.department.name || response.department.code
        : '',
      status,
      description: response.description ?? '',
      creationDate: response.createdAt ?? '',
      modificationDate: response.updatedAt ?? '',
      withdrawalDate: response.expirationDate ?? '',
      categoryId: response.category?.id,
      informationSystemId: response.systemType?.id,
      scopeId: response.field?.id,
      commissionId: response.csCommission?.id,
      admUnitCode: response.admUnit?.code,
      departmentCode: response.department?.code,
      statusId: status ?? undefined,
      informationSystemDbId: response.appInformationSystemDbId,
      appDevelopmentId: response.appDevelopmentId,
      appSecurityId: response.appSecurityId ?? null,
      appAccessibilityId: response.appAccessibilityId ?? null,
      appResponsibleAuthorizedId: response.appResponsibleAuthorizedId,
      ...readApplicationCompleteness(response),
    };
  }

  private toHttpParams(params?: ApplicationPageParams): HttpParams | undefined {
    if (!params) return undefined;

    let httpParams = toPageHttpParams(params) ?? new HttpParams();

    const criteria: Record<string, string | number | boolean | undefined> = {
      incomplete: params.incomplete,
      prefix: params.prefix,
      applicationName: params.applicationName,
      categoryId: params.categoryId,
      systemTypeId: params.systemTypeId,
      fieldId: params.fieldId,
      commissionId: params.commissionId,
      admUnitCode: params.admUnitCode,
      statusId: params.statusId,
      description: params.description,
      quickSearch: params.quickSearch?.trim() || undefined,
      responsibleId: params.responsibleId,
      databaseId: params.databaseId,
      serverId: params.serverId,
      environmentId: params.environmentId,
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
      incomplete: params?.incomplete ?? null,
      page: params?.page ?? null,
      size: params?.size ?? null,
      sort,
      prefix: params?.prefix ?? '',
      applicationName: params?.applicationName ?? '',
      categoryId: params?.categoryId ?? null,
      systemTypeId: params?.systemTypeId ?? null,
      fieldId: params?.fieldId ?? null,
      commissionId: params?.commissionId ?? null,
      admUnitCode: params?.admUnitCode ?? null,
      statusId: params?.statusId ?? null,
      description: params?.description ?? '',
      responsibleId: params?.responsibleId ?? null,
      databaseId: params?.databaseId ?? null,
      serverId: params?.serverId ?? null,
      environmentId: params?.environmentId ?? null,
    });
  }
}
