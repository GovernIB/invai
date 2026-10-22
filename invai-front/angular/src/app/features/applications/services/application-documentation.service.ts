import { HttpParams, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BaseApiService } from '@core/services/base-api.service';
import { SpringPage } from '@models/page.model';
import { toPageHttpParams } from '@shared/utils/http-params.utils';
import { map, Observable } from 'rxjs';

import {
  ApplicationAttachment,
  ApplicationAttachmentPageParams,
  ApplicationDocument,
  ApplicationDocumentFile,
  ApplicationDocumentInput,
  ApplicationDocumentUpdateInput,
  ApplicationDocumentVersionInput,
} from '../application-documentation.model';

// Documents attached to the applications and their versions. Creating a document or adding a
// version sends a multipart request: the JSON of the data in the `data` part and the file in the
// `file` part. The listings are not cached: they change with every upload, edit or deletion.
@Injectable({ providedIn: 'root' })
export class ApplicationDocumentationService extends BaseApiService {
  protected override readonly ENTITY_URI = 'application/documentation';

  getAttachments(
    applicationId: number,
    params: ApplicationAttachmentPageParams = {},
  ): Observable<SpringPage<ApplicationAttachment>> {
    return this.http.get<SpringPage<ApplicationAttachment>>(this.url(), {
      params: documentHttpParams(applicationId, params),
    });
  }

  getDocument(id: number): Observable<ApplicationDocument> {
    return this.http.get<ApplicationDocument>(this.url(id));
  }

  createDocument(input: ApplicationDocumentInput, file: File): Observable<ApplicationDocument> {
    return this.http.post<ApplicationDocument>(this.url(), multipart(input, file));
  }

  updateDocument(id: number, input: ApplicationDocumentUpdateInput): Observable<ApplicationDocument> {
    return this.http.put<ApplicationDocument>(this.url(id), input);
  }

  // Answers with the whole document, the new version being the current one.
  addDocumentVersion(
    id: number,
    input: ApplicationDocumentVersionInput,
    file: File,
  ): Observable<ApplicationDocument> {
    return this.http.post<ApplicationDocument>(this.url(id), multipart(input, file));
  }

  deleteDocument(id: number): Observable<void> {
    return this.http.delete<void>(this.url(id));
  }

  // `inline` asks for the file to be shown in the browser instead of downloaded.
  getDocumentVersionFile(
    id: number,
    versionId: number,
    fallbackName: string,
    inline = false,
  ): Observable<ApplicationDocumentFile> {
    return this.http
      .get(this.url(id, 'attachment', versionId, inline ? 'view' : 'download'), {
        observe: 'response',
        responseType: 'blob',
      })
      .pipe(
        map((response: HttpResponse<Blob>) => ({
          blob: response.body ?? new Blob(),
          fileName: contentDispositionFileName(response.headers.get('Content-Disposition')) ?? fallbackName,
        })),
      );
  }
}

function documentHttpParams(
  applicationId: number,
  { search, sectionId, statusId, categoryId, mimeTypeId, deleted, ...page }: ApplicationAttachmentPageParams,
): HttpParams {
  let params = (toPageHttpParams(page) ?? new HttpParams()).set('applicationId', applicationId);
  const filters = { search: search?.trim() || undefined, sectionId, statusId, categoryId, mimeTypeId, deleted };
  for (const [key, value] of Object.entries(filters)) {
    if (value != null) params = params.set(key, String(value));
  }
  return params;
}

function multipart(data: object, file: File): FormData {
  const body = new FormData();
  body.append('data', new Blob([JSON.stringify(data)], { type: 'application/json' }));
  body.append('file', file, file.name);
  return body;
}

// Reads the RFC 6266 file name, preferring the UTF-8 `filename*` form.
export function contentDispositionFileName(header: string | null): string | null {
  if (!header) return null;
  const encoded = /filename\*\s*=\s*UTF-8''([^;]+)/i.exec(header)?.[1];
  if (encoded) {
    try {
      return decodeURIComponent(encoded.trim());
    } catch {
      // Falls back to the plain form below.
    }
  }
  const plain = /filename\s*=\s*"([^"]*)"|filename\s*=\s*([^;]+)/i.exec(header);
  const name = (plain?.[1] ?? plain?.[2])?.trim();
  return name && !name.startsWith('=?') ? name : null;
}
