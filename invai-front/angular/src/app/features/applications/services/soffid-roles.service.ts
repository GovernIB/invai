import { Injectable } from '@angular/core';
import { BaseApiService } from '@core/services/base-api.service';
import { SpringPage } from '@models/page.model';
import { Observable } from 'rxjs';

import { ApplicationSecurityRole } from '../applications.model';

export const SOFFID_ROLES_PAGE_SIZE = 20;

// Live Soffid role search; results are candidates, never cached locally.
@Injectable({ providedIn: 'root' })
export class SoffidRolesService extends BaseApiService {
  protected override readonly ENTITY_URI = 'security-role';

  search(name: string): Observable<SpringPage<ApplicationSecurityRole>> {
    const query = name.trim();
    return this.http.get<SpringPage<ApplicationSecurityRole>>(this.url('soffid-search'), {
      params: {
        ...(query ? { name: query } : {}),
        page: 0,
        size: SOFFID_ROLES_PAGE_SIZE,
      },
    });
  }
}
