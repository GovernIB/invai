import { HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BaseApiService } from '@core/services/base-api.service';
import { SpringPage } from '@models/page.model';
import { toPageHttpParams } from '@shared/utils/http-params.utils';
import { cachedRequest, pageParamsCacheKey } from '@shared/utils/service-cache.utils';
import { Observable } from 'rxjs';
import { AdministrativeUnit, AdministrativeUnitPageParams } from '../administrative-units.model';

@Injectable({ providedIn: 'root' })
export class AdministrativeUnitsService extends BaseApiService {
  protected override readonly ENTITY_URI = 'adm-unit';
  private readonly pages = new Map<string, Observable<SpringPage<AdministrativeUnit>>>();

  getPage(params?: AdministrativeUnitPageParams): Observable<SpringPage<AdministrativeUnit>> {
    const search = params?.search?.trim();
    let httpParams = toPageHttpParams(params) ?? new HttpParams();
    if (search) httpParams = httpParams.set('search', search);
    const request = () => this.http.get<SpringPage<AdministrativeUnit>>(this.url(), { params: httpParams });
    return search ? request() : cachedRequest(this.pages, pageParamsCacheKey(params), request);
  }

  clearCache(): void { this.pages.clear(); }
}
