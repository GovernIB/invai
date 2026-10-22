import { inject } from '@angular/core';
import { ResolveFn } from '@angular/router';
import { catchError, map, of } from 'rxjs';
import { ApplicationDocumentDetailData } from '../../application-documentation.model';
import { ApplicationDocumentDetailLoader } from './application-document-detail.loader';

export const DOCUMENT_DETAIL_RESOLVE_KEY = 'applicationDocumentDetail';
export interface ApplicationDocumentDetailResolvedData {
  applicationId: number | null;
  documentId: number | null;
  detail: ApplicationDocumentDetailData | null;
}

export const applicationDocumentDetailResolver: ResolveFn<ApplicationDocumentDetailResolvedData> = (route) => {
  const applicationId = Number(route.paramMap.get('id'));
  const documentId = Number(route.paramMap.get('documentId'));
  if (![applicationId, documentId].every((id) => Number.isInteger(id) && id > 0)) {
    return of({ applicationId: null, documentId: null, detail: null });
  }
  return inject(ApplicationDocumentDetailLoader).load(applicationId, documentId).pipe(
    map((detail) => ({ applicationId, documentId, detail })),
    catchError(() => of({ applicationId, documentId, detail: null })),
  );
};
