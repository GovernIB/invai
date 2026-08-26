import { Injectable } from '@angular/core';
import { BaseApiService } from '@core/services/base-api.service';
import { cachedRequest } from '@shared/utils/service-cache.utils';
import { Observable } from 'rxjs';

import { DevelopmentLookupOutput, DevelopmentStandardAdaption } from '../applications.model';

@Injectable({ providedIn: 'root' })
export class DevelopmentStandardAdaptionCatalogService extends BaseApiService {
  protected override readonly ENTITY_URI = 'standard-adaption';
  private readonly cache = new Map<
    string,
    Observable<DevelopmentLookupOutput<DevelopmentStandardAdaption>[]>
  >();

  getAll(): Observable<DevelopmentLookupOutput<DevelopmentStandardAdaption>[]> {
    return cachedRequest(this.cache, 'all', () =>
      this.http.get<DevelopmentLookupOutput<DevelopmentStandardAdaption>[]>(this.url()),
    );
  }
}
