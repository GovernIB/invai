import { Injectable } from '@angular/core';
import { BaseApiService } from '@core/services/base-api.service';
import { cachedRequest } from '@shared/utils/service-cache.utils';
import { Observable } from 'rxjs';

import { ApplicationDocumentStatus } from '../application-documentation.model';

// States a document can be in: active, in review or obsolete.
@Injectable({ providedIn: 'root' })
export class ApplicationDocumentStatusCatalogService extends BaseApiService {
  protected override readonly ENTITY_URI = 'document-status';
  private readonly cache = new Map<string, Observable<ApplicationDocumentStatus[]>>();

  getAll(): Observable<ApplicationDocumentStatus[]> {
    return cachedRequest(this.cache, 'all', () =>
      this.http.get<ApplicationDocumentStatus[]>(this.url()),
    );
  }
}
