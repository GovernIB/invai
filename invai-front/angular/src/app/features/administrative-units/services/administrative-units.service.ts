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

  private readonly departmentsCache = new Map<
    string,
    Observable<SpringPage<AdministrativeUnit>>
  >();
  private readonly administrativeUnitsCache = new Map<
    string,
    Observable<SpringPage<AdministrativeUnit>>
  >();

  getDepartments(
    params?: AdministrativeUnitPageParams,
  ): Observable<SpringPage<AdministrativeUnit>> {
    return cachedRequest(this.departmentsCache, pageParamsCacheKey(params), () =>
      this.http.get<SpringPage<AdministrativeUnit>>(this.url('departments'), {
        params: toPageHttpParams(params),
      }),
    );
  }

  getAdmUnitsByDepartment(
    departmentCode: string,
    params?: AdministrativeUnitPageParams,
  ): Observable<SpringPage<AdministrativeUnit>> {
    const key = `${departmentCode};${pageParamsCacheKey(params)}`;
    return cachedRequest(this.administrativeUnitsCache, key, () =>
      this.http.get<SpringPage<AdministrativeUnit>>(
        this.url('departments', encodeURIComponent(departmentCode), 'adm-units'),
        { params: toPageHttpParams(params) },
      ),
    );
  }

  clearCache(): void {
    this.departmentsCache.clear();
    this.administrativeUnitsCache.clear();
  }
}
