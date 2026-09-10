import { HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BaseApiService } from '@core/services/base-api.service';
import { SpringPage } from '@models/page.model';
import { toPageHttpParams } from '@shared/utils/http-params.utils';
import { cachedRequest } from '@shared/utils/service-cache.utils';
import { Observable, tap } from 'rxjs';

import {
  SecurityResource,
  SecurityResourceInput,
  SecurityResourcePageParams,
} from '../security.model';

export abstract class SecurityResourceService extends BaseApiService {
  private readonly pagesCache = new Map<string, Observable<SpringPage<SecurityResource>>>();

  getAll(params?: SecurityResourcePageParams): Observable<SpringPage<SecurityResource>> {
    const requestFactory = () =>
      this.http.get<SpringPage<SecurityResource>>(this.url(), {
        params: this.toHttpParams(params),
      });
    if (params?.search?.trim()) return requestFactory();
    return cachedRequest(this.pagesCache, this.cacheKey(params), requestFactory);
  }

  getById(id: number): Observable<SecurityResource> {
    return this.http.get<SecurityResource>(this.url(id));
  }

  create(input: SecurityResourceInput): Observable<SecurityResource> {
    return this.http
      .post<SecurityResource>(this.url(), input)
      .pipe(tap(() => this.clearCache()));
  }

  update(id: number, input: SecurityResourceInput): Observable<SecurityResource> {
    return this.http
      .put<SecurityResource>(this.url(id), input)
      .pipe(tap(() => this.clearCache()));
  }

  deactivate(id: number): Observable<void> {
    return this.http.delete<void>(this.url(id)).pipe(tap(() => this.clearCache()));
  }

  reactivate(id: number): Observable<SecurityResource> {
    return this.http
      .put<SecurityResource>(this.url('reactivate', id), null)
      .pipe(tap(() => this.clearCache()));
  }

  clearCache(): void {
    this.pagesCache.clear();
  }

  private toHttpParams(params?: SecurityResourcePageParams): HttpParams | undefined {
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

  private cacheKey(params?: SecurityResourcePageParams): string {
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
export class EnsRequirementsService extends SecurityResourceService {
  protected override readonly ENTITY_URI = 'ens-requirement';
}

@Injectable({ providedIn: 'root' })
export class IdentityProvidersService extends SecurityResourceService {
  protected override readonly ENTITY_URI = 'identity-provider';
}

@Injectable({ providedIn: 'root' })
export class PersonalDataProcessingService extends SecurityResourceService {
  protected override readonly ENTITY_URI = 'personal-data-processing';
}

@Injectable({ providedIn: 'root' })
export class SecurityMeasureTypesService extends SecurityResourceService {
  protected override readonly ENTITY_URI = 'security-measure-type';
}

@Injectable({ providedIn: 'root' })
export class WebContextsService extends SecurityResourceService {
  protected override readonly ENTITY_URI = 'web-context';
}
