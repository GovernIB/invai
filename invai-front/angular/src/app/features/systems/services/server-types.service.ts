import { Injectable } from '@angular/core';
import { BaseApiService } from '@core/services/base-api.service';
import { cachedRequest } from '@shared/utils/service-cache.utils';
import { Observable, map } from 'rxjs';

import { ServerTypeCode, ServerTypeOutput } from '../systems.model';

const ALL_SERVER_TYPES_CACHE_KEY = 'all';

@Injectable({ providedIn: 'root' })
export class ServerTypesService extends BaseApiService {
  protected override readonly ENTITY_URI = 'server-type';

  private readonly cache = new Map<string, Observable<ServerTypeOutput[]>>();

  getAll(): Observable<ServerTypeOutput[]> {
    return cachedRequest(this.cache, ALL_SERVER_TYPES_CACHE_KEY, () =>
      this.http.get<ServerTypeOutput[]>(this.url()),
    );
  }

  getByCode(code: ServerTypeCode): Observable<ServerTypeOutput> {
    return this.getAll().pipe(
      map((serverTypes) => {
        const serverType = serverTypes.find((candidate) => candidate.code === code);
        if (!serverType) {
          throw new Error(`Server type ${code} is not available`);
        }
        return serverType;
      }),
    );
  }

  clearCache(): void {
    this.cache.clear();
  }
}
