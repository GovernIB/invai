import { httpResource } from '@angular/common/http';
import { signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { BaseApiService } from './base-api.service';

export abstract class CrudService<T> extends BaseApiService {
  readonly selectedId = signal<number | string | null>(null);

  readonly entities = httpResource<T[]>(() => this.url());

  readonly entity = httpResource<T>(() => {
    const id = this.selectedId();
    return id == null ? undefined : this.url(id);
  });

  createEntity<R = void>(payload: T): Observable<R> {
    return this.http.post<R>(this.url(), payload).pipe(tap(() => this.entities.reload()));
  }

  updateEntity<R = void>(id: number | string, payload: T): Observable<R> {
    return this.http.put<R>(this.url(id), payload).pipe(
      tap(() => {
        this.entities.reload();
        if (`${this.selectedId()}` === `${id}`) this.entity.reload();
      }),
    );
  }

  patchEntity<R = void>(id: number | string, payload: Partial<T>): Observable<R> {
    return this.http.patch<R>(this.url(id), payload).pipe(
      tap(() => {
        this.entities.reload();
        if (`${this.selectedId()}` === `${id}`) this.entity.reload();
      }),
    );
  }

  deleteEntity<R = void>(id: number | string): Observable<R> {
    return this.http.delete<R>(this.url(id)).pipe(
      tap(() => {
        this.entities.reload();
        if (`${this.selectedId()}` === `${id}`) this.selectedId.set(null);
      }),
    );
  }

  // downloadBlobFromBase64(url: string, options?: { params?: HttpParams }): Observable<FileWithBlob> {
  //   return this.http.get<FileMessage>(url, options).pipe(
  //     map(({ data, mime, fileName }) => ({
  //       blob: fnDecodeBase64ToBlob(data, mime),
  //       mime,
  //       fileName,
  //     })),
  //   );
  // }
}
