import { HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { BaseApiService } from '@core/services/base-api.service';
import { SpringPage } from '@models/page.model';
import { toPageHttpParams } from '@shared/utils/http-params.utils';
import { cachedRequest, pageParamsCacheKey } from '@shared/utils/service-cache.utils';
import { Observable, tap } from 'rxjs';

import {
  ApplicationEnsClassificationInput,
  ApplicationEnsClassificationOutput,
  ApplicationSecurityInput,
  ApplicationSecurityMeasureInput,
  ApplicationSecurityMeasureOutput,
  ApplicationSecurityOutput,
  ApplicationSecurityPageParams,
  ApplicationSecurityRiskInput,
  ApplicationSecurityRiskOutput,
  ApplicationSecurityRoleOutput,
  ApplicationWebContextInput,
  ApplicationWebContextOutput,
  SecurityCatalogItem,
} from '../applications.model';
import { ApplicationsService } from './applications.service';

@Injectable({ providedIn: 'root' })
export class ApplicationSecurityService extends BaseApiService {
  private readonly applicationsService = inject(ApplicationsService);
  protected override readonly ENTITY_URI = 'application/security';
  private readonly detailsCache = new Map<string, Observable<ApplicationSecurityOutput>>();

  getById(id: number): Observable<ApplicationSecurityOutput> {
    return cachedRequest(this.detailsCache, String(id), () =>
      this.http.get<ApplicationSecurityOutput>(this.url(id)),
    );
  }

  create(payload: ApplicationSecurityInput): Observable<ApplicationSecurityOutput> {
    return this.http
      .post<ApplicationSecurityOutput>(this.url(), payload)
      .pipe(
        tap(() => {
          this.clearCache();
          this.applicationsService.clearCache();
        }),
      );
  }

  update(id: number, payload: ApplicationSecurityInput): Observable<ApplicationSecurityOutput> {
    return this.http
      .put<ApplicationSecurityOutput>(this.url(id), payload)
      .pipe(
        tap(() => {
          this.clearCache();
          this.applicationsService.clearCache();
        }),
      );
  }

  clearCache(): void {
    this.detailsCache.clear();
  }
}

abstract class ApplicationSecurityPageService<TOutput> extends BaseApiService {
  private readonly pagesCache = new Map<string, Observable<SpringPage<TOutput>>>();

  getPage(params: ApplicationSecurityPageParams): Observable<SpringPage<TOutput>> {
    const { appSecurityId, statusId, ...pageParams } = params;
    const key = `${appSecurityId};status=${statusId ?? ''};${pageParamsCacheKey(pageParams)}`;
    let httpParams = toPageHttpParams(pageParams) ?? new HttpParams();
    if (statusId != null) httpParams = httpParams.set('statusId', String(statusId));

    return cachedRequest(this.pagesCache, key, () =>
      this.http.get<SpringPage<TOutput>>(this.url(appSecurityId), { params: httpParams }),
    );
  }

  clearCache(): void {
    this.pagesCache.clear();
  }
}

abstract class MutableApplicationSecurityPageService<
  TOutput,
  TInput,
> extends ApplicationSecurityPageService<TOutput> {
  private readonly applicationsService = inject(ApplicationsService);
  create(payload: TInput): Observable<TOutput> {
    return this.http.post<TOutput>(this.url(), payload).pipe(
      tap(() => {
        this.clearCache();
        this.applicationsService.clearCache();
      }),
    );
  }

  update(id: number, payload: TInput): Observable<TOutput> {
    return this.http.put<TOutput>(this.url(id), payload).pipe(
      tap(() => {
        this.clearCache();
        this.applicationsService.clearCache();
      }),
    );
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(this.url(id)).pipe(
      tap(() => {
        this.clearCache();
        this.applicationsService.clearCache();
      }),
    );
  }
}

@Injectable({ providedIn: 'root' })
export class ApplicationSecurityRolesService extends ApplicationSecurityPageService<ApplicationSecurityRoleOutput> {
  protected override readonly ENTITY_URI = 'application/security/role';
}

@Injectable({ providedIn: 'root' })
export class ApplicationWebContextsService extends MutableApplicationSecurityPageService<
  ApplicationWebContextOutput,
  ApplicationWebContextInput
> {
  protected override readonly ENTITY_URI = 'application/security/web-context';
}

@Injectable({ providedIn: 'root' })
export class ApplicationSecurityRisksService extends MutableApplicationSecurityPageService<
  ApplicationSecurityRiskOutput,
  ApplicationSecurityRiskInput
> {
  protected override readonly ENTITY_URI = 'application/security/risk';
}

@Injectable({ providedIn: 'root' })
export class ApplicationSecurityMeasuresService extends MutableApplicationSecurityPageService<
  ApplicationSecurityMeasureOutput,
  ApplicationSecurityMeasureInput
> {
  protected override readonly ENTITY_URI = 'application/security/measure';
}

@Injectable({ providedIn: 'root' })
export class ApplicationEnsClassificationsService extends MutableApplicationSecurityPageService<
  ApplicationEnsClassificationOutput,
  ApplicationEnsClassificationInput
> {
  protected override readonly ENTITY_URI = 'application/security/ens-classification';
}

abstract class SecurityCatalogService extends BaseApiService {
  private readonly itemsCache = new Map<string, Observable<SecurityCatalogItem[]>>();

  getAll(): Observable<SecurityCatalogItem[]> {
    return cachedRequest(this.itemsCache, 'all', () =>
      this.http.get<SecurityCatalogItem[]>(this.url()),
    );
  }
}

@Injectable({ providedIn: 'root' })
export class SecurityLevelsService extends SecurityCatalogService {
  protected override readonly ENTITY_URI = 'security-level';
}

@Injectable({ providedIn: 'root' })
export class EnsSubjectsService extends SecurityCatalogService {
  protected override readonly ENTITY_URI = 'ens-subject';
}
