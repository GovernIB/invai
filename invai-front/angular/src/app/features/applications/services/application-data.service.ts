import { HttpContext, HttpParameterCodec, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { SKIP_SERVER_ERROR_DIALOG } from '@core/interceptors/http-error-logging.interceptor';
import { BaseApiService } from '@core/services/base-api.service';
import { cachedRequest } from '@shared/utils/service-cache.utils';
import { Observable, of, tap } from 'rxjs';
import {
  ApplicationDataInput,
  ApplicationDataOutput,
  ApplicationDataSource,
  ApplicationDataUrlInput,
  ApplicationDataUrlOutput,
} from '../applications.model';
import { ApplicationsService } from './applications.service';
import { APPLICATION_DETAIL_CACHE_TTL_MS } from './application-cache.constants';

const URL_PATHS: Record<ApplicationDataSource, string> = {
  openData: 'open-data',
  reuse: 'reuse',
};

// Encodes every reserved character, so a URL sent as a parameter reaches the back-end unchanged:
// the default codec leaves `+` as is, which the back-end reads as a space.
const URL_PARAM_CODEC: HttpParameterCodec = {
  encodeKey: (key) => encodeURIComponent(key),
  encodeValue: (value) => encodeURIComponent(value),
  decodeKey: (key) => decodeURIComponent(key),
  decodeValue: (value) => decodeURIComponent(value),
};

@Injectable({ providedIn: 'root' })
export class ApplicationDataService extends BaseApiService {
  protected override readonly ENTITY_URI = 'application/data';
  private readonly applications = inject(ApplicationsService);
  private readonly detailsCache = new Map<string, Observable<ApplicationDataOutput | null>>();
  // Whether each URL answers, by URL: the tab does not wait again for a URL already checked.
  private readonly checksCache = new Map<string, Observable<ApplicationDataUrlOutput>>();

  // The detail is cached for the detail TTL and invalidated after every successful mutation.
  // Whether its URLs answer is not part of it: see checkUrl.
  getById(id: number): Observable<ApplicationDataOutput | null> {
    const key = String(id);
    const request$: Observable<ApplicationDataOutput | null> = cachedRequest(
      this.detailsCache,
      key,
      () =>
        this.http
          .get<ApplicationDataOutput | null>(this.url(id), {
            // The data tab reports load errors inline.
            context: new HttpContext().set(SKIP_SERVER_ERROR_DIALOG, true),
          })
          .pipe(
            tap((record) => {
              if (record === null && this.detailsCache.get(key) === request$) {
                this.detailsCache.delete(key);
              }
            }),
          ),
      APPLICATION_DETAIL_CACHE_TTL_MS,
    );
    return request$;
  }

  refreshById(id: number): Observable<ApplicationDataOutput | null> {
    this.detailsCache.delete(String(id));
    return this.getById(id);
  }

  // Queries a URL to find out whether it answers. The answer is cached for the detail TTL; a check
  // that could not be carried out is not, so the next one queries the URL again.
  checkUrl(url: string): Observable<ApplicationDataUrlOutput> {
    return cachedRequest(
      this.checksCache,
      url,
      () =>
        this.http.get<ApplicationDataUrlOutput>(this.url('check-url'), {
          params: new HttpParams({ encoder: URL_PARAM_CODEC }).set('url', url),
          // The data tab reports a URL that cannot be checked inline.
          context: new HttpContext().set(SKIP_SERVER_ERROR_DIALOG, true),
        }),
      APPLICATION_DETAIL_CACHE_TTL_MS,
    );
  }

  create(payload: ApplicationDataInput): Observable<ApplicationDataOutput> {
    return this.http
      .post<ApplicationDataOutput>(this.url(), payload)
      .pipe(tap(() => this.changed()));
  }

  update(id: number, payload: ApplicationDataInput): Observable<ApplicationDataOutput> {
    return this.http
      .put<ApplicationDataOutput>(this.url(id), payload)
      .pipe(tap(() => this.changed()));
  }

  // Saves the URL of one source, even if it does not answer; a null URL goes back to the default
  // one. The response gives the URL the source now uses and whether it answered.
  updateUrl(
    id: number,
    source: ApplicationDataSource,
    url: string | null,
  ): Observable<ApplicationDataUrlOutput> {
    const payload: ApplicationDataUrlInput = { url };
    return this.http
      .put<ApplicationDataUrlOutput>(this.url(id, URL_PATHS[source]), payload)
      .pipe(
        tap((result) => {
          this.changed();
          if (result.url) this.rememberCheck(result.url, result);
        }),
      );
  }

  // Clears the cached details; the checks of the URLs are kept, they do not depend on them.
  clearCache(): void {
    this.detailsCache.clear();
  }

  private changed(): void {
    this.clearCache();
    this.applications.clearCache();
  }

  // The PUT of a source has just checked the URL it saved: its answer serves as the check of that
  // URL, so reading the tab again does not query it once more.
  private rememberCheck(url: string, result: ApplicationDataUrlOutput): void {
    this.checksCache.delete(url);
    // Subscribing starts the TTL of the entry.
    cachedRequest(this.checksCache, url, () => of(result), APPLICATION_DETAIL_CACHE_TTL_MS).subscribe();
  }
}
