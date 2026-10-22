import { Injectable } from '@angular/core';
import { BaseApiService } from '@core/services/base-api.service';
import { cachedRequest } from '@shared/utils/service-cache.utils';
import { Observable } from 'rxjs';

import { ApplicationDocumentSection } from '../application-documentation.model';

// Sections of an application a document can be filed under.
@Injectable({ providedIn: 'root' })
export class ApplicationDocumentSectionCatalogService extends BaseApiService {
  protected override readonly ENTITY_URI = 'app-section';
  private readonly cache = new Map<string, Observable<ApplicationDocumentSection[]>>();

  getAll(): Observable<ApplicationDocumentSection[]> {
    return cachedRequest(this.cache, 'all', () =>
      this.http.get<ApplicationDocumentSection[]>(this.url()),
    );
  }
}
