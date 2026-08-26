import { Injectable } from '@angular/core';
import { BaseApiService } from '@core/services/base-api.service';
import { cachedRequest } from '@shared/utils/service-cache.utils';
import { Observable } from 'rxjs';

import { DevelopmentLookupOutput, DevelopmentModality } from '../applications.model';

@Injectable({ providedIn: 'root' })
export class DevelopmentModalityCatalogService extends BaseApiService {
  protected override readonly ENTITY_URI = 'modality';
  private readonly cache = new Map<
    string,
    Observable<DevelopmentLookupOutput<DevelopmentModality>[]>
  >();

  getAll(): Observable<DevelopmentLookupOutput<DevelopmentModality>[]> {
    return cachedRequest(this.cache, 'all', () =>
      this.http.get<DevelopmentLookupOutput<DevelopmentModality>[]>(this.url()),
    );
  }
}
