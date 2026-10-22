import { Injectable } from '@angular/core';
import { BaseApiService } from '@core/services/base-api.service';
import { cachedRequest } from '@shared/utils/service-cache.utils';
import { Observable } from 'rxjs';

import { ApplicationDocumentMimeType } from '../application-documentation.model';

// File formats a document version can be uploaded in, identified by the file extension.
@Injectable({ providedIn: 'root' })
export class ApplicationDocumentMimeTypeCatalogService extends BaseApiService {
  protected override readonly ENTITY_URI = 'mime-type';
  private readonly cache = new Map<string, Observable<ApplicationDocumentMimeType[]>>();

  getAll(): Observable<ApplicationDocumentMimeType[]> {
    return cachedRequest(this.cache, 'all', () =>
      this.http.get<ApplicationDocumentMimeType[]>(this.url()),
    );
  }
}
