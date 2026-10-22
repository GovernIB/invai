import { HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BaseApiService } from '@core/services/base-api.service';
import { SpringPage } from '@models/page.model';
import { toPageHttpParams } from '@shared/utils/http-params.utils';
import { Observable } from 'rxjs';

import {
  ApplicationLink,
  ApplicationLinkInput,
  ApplicationLinkPageParams,
} from '../application-documentation.model';

// Links of the applications to external resources. The listing is not cached: it changes with
// every addition, edit or deletion.
@Injectable({ providedIn: 'root' })
export class ApplicationLinksService extends BaseApiService {
  protected override readonly ENTITY_URI = 'application/documentation/link';

  getLinks(
    applicationId: number,
    params: ApplicationLinkPageParams = {},
  ): Observable<SpringPage<ApplicationLink>> {
    return this.http.get<SpringPage<ApplicationLink>>(this.url(), {
      params: linkHttpParams(applicationId, params),
    });
  }

  createLink(applicationId: number, input: ApplicationLinkInput): Observable<ApplicationLink> {
    return this.http.post<ApplicationLink>(this.url(), { applicationId, ...input });
  }

  // The application of a link never changes, so it is not sent.
  updateLink(id: number, input: ApplicationLinkInput): Observable<ApplicationLink> {
    return this.http.put<ApplicationLink>(this.url(id), input);
  }

  deleteLink(id: number): Observable<void> {
    return this.http.delete<void>(this.url(id));
  }
}

function linkHttpParams(
  applicationId: number,
  { quickSearch, sectionId, deleted, ...page }: ApplicationLinkPageParams,
): HttpParams {
  let params = (toPageHttpParams(page) ?? new HttpParams()).set('applicationId', applicationId);
  const filters = { quickSearch: quickSearch?.trim() || undefined, sectionId, deleted };
  for (const [key, value] of Object.entries(filters)) {
    if (value != null) params = params.set(key, String(value));
  }
  return params;
}
