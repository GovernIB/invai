import { inject } from '@angular/core';
import { ResolveFn } from '@angular/router';
import { SpringPage } from '@models/page.model';
import { PAGINATOR_ROWS } from '@shared/constants/table.constants';
import { catchError, forkJoin, map, Observable, of } from 'rxjs';

import {
  ApplicationAttachment,
  ApplicationAttachmentPageParams,
  ApplicationLink,
  ApplicationLinkPageParams,
} from '../../../../application-documentation.model';
import { ApplicationDocumentationService } from '../../../../services/application-documentation.service';
import { ApplicationLinksService } from '../../../../services/application-links.service';

export const APPLICATION_DOCUMENTATION_RESOLVE_KEY = 'applicationDocumentation';

export const APPLICATION_DOCUMENTATION_INITIAL_PARAMS = { page: 0, size: PAGINATOR_ROWS };

// The attachments listing leaves out the deleted documents.
export const APPLICATION_ATTACHMENTS_INITIAL_PARAMS: ApplicationAttachmentPageParams = {
  ...APPLICATION_DOCUMENTATION_INITIAL_PARAMS,
  deleted: false,
};

// The links listing leaves out the deleted links.
export const APPLICATION_LINKS_INITIAL_PARAMS: ApplicationLinkPageParams = {
  ...APPLICATION_DOCUMENTATION_INITIAL_PARAMS,
  deleted: false,
};

export interface ApplicationDocumentationResolvedData {
  applicationId: number | null;
  // null when the page could not be read; the list then reads it again.
  attachments: SpringPage<ApplicationAttachment> | null;
  links: SpringPage<ApplicationLink> | null;
}

export const applicationDocumentationResolver: ResolveFn<ApplicationDocumentationResolvedData> = (
  route,
) => {
  const applicationId = Number(route.parent?.paramMap.get('id'));
  if (!Number.isInteger(applicationId) || applicationId <= 0) {
    return of({ applicationId: null, attachments: null, links: null });
  }
  const documents = inject(ApplicationDocumentationService);
  const links = inject(ApplicationLinksService);
  return forkJoin({
    attachments: orNull(documents.getAttachments(applicationId, APPLICATION_ATTACHMENTS_INITIAL_PARAMS)),
    links: orNull(links.getLinks(applicationId, APPLICATION_LINKS_INITIAL_PARAMS)),
  }).pipe(map((pages) => ({ applicationId, ...pages })));
};

function orNull<T>(page$: Observable<SpringPage<T>>): Observable<SpringPage<T> | null> {
  return page$.pipe(catchError(() => of(null)));
}
