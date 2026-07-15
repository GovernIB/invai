import { Injectable } from '@angular/core';
import { BaseApiService } from '@core/services/base-api.service';
import { SpringPage } from '@models/page.model';
import { toPageHttpParams } from '@shared/utils/http-params.utils';
import { cachedRequest, pageParamsCacheKey } from '@shared/utils/service-cache.utils';
import { Observable, tap } from 'rxjs';

import {
  AdministrativeUnit,
  AdministrativeUnitInput,
  AdministrativeUnitPageParams,
} from '../administrative-units.model';

@Injectable({ providedIn: 'root' })
export class AdministrativeUnitsService extends BaseApiService {
  protected override readonly ENTITY_URI = 'adm-unit';

  private readonly _administrativeUnitsCache = new Map<
    string,
    Observable<SpringPage<AdministrativeUnit>>
  >();

  getAll(params?: AdministrativeUnitPageParams): Observable<SpringPage<AdministrativeUnit>> {
    return cachedRequest(this._administrativeUnitsCache, pageParamsCacheKey(params), () =>
      this.http.get<SpringPage<AdministrativeUnit>>(this.url(), {
        params: toPageHttpParams(params),
      }),
    );
  }

  getById(id: number): Observable<AdministrativeUnit> {
    return this.http.get<AdministrativeUnit>(this.url(id));
  }

  create(payload: AdministrativeUnitInput): Observable<AdministrativeUnit> {
    return this.http
      .post<AdministrativeUnit>(this.url(), payload)
      .pipe(tap(() => this.clearCache()));
  }

  update(id: number, payload: AdministrativeUnitInput): Observable<AdministrativeUnit> {
    return this.http
      .put<AdministrativeUnit>(this.url(id), payload)
      .pipe(tap(() => this.clearCache()));
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(this.url(id)).pipe(tap(() => this.clearCache()));
  }

  clearCache(): void {
    this._administrativeUnitsCache.clear();
  }
}
