import { Injectable } from '@angular/core';
import { BaseApiService } from '@core/services/base-api.service';
import { cachedRequest } from '@shared/utils/service-cache.utils';
import { Observable } from 'rxjs';

import { ResponsibleType } from '../responsibles.model';

@Injectable({ providedIn: 'root' })
export class ResponsibleTypesService extends BaseApiService {
  protected override readonly ENTITY_URI = 'responsible-type';
  private readonly cache = new Map<string, Observable<ResponsibleType[]>>();

  getAll(): Observable<ResponsibleType[]> {
    return cachedRequest(this.cache, 'all', () => this.http.get<ResponsibleType[]>(this.url()));
  }

  clearCache(): void {
    this.cache.clear();
  }
}
