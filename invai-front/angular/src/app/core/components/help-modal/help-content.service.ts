import { DOCUMENT } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { inject, Injectable, LOCALE_ID } from '@angular/core';
import { cachedRequest } from '@shared/utils/service-cache.utils';
import { catchError, forkJoin, map, Observable, of, switchMap, take } from 'rxjs';
import { markdownToPlainText, searchDocuments } from './help-search';
import { HelpDocument, HelpLocale, HelpTopic, SearchResult } from './help-topic.model';

const HELP_ROOT = 'help/';
const INDEX_FILE = 'index.json';
const MARKDOWN_IMAGE = /!\[([^\]]*)\]\(([^)]+)\)/g;

/** A help document a link points to, and the heading it points to inside it, if any. */
export interface HelpLink {
  document: string;
  anchor: string;
}

/** Help folder of the running build; the source locale when the build is not localized. */
export function helpLocale(localeId: string): HelpLocale {
  return localeId.toLowerCase().split('-')[0] === 'es' ? 'es' : 'ca';
}

/** Index, documents and search of the help, in the language of the running build. */
@Injectable({ providedIn: 'root' })
export class HelpContentService {
  private readonly _http = inject(HttpClient);
  private readonly _document = inject(DOCUMENT);
  private readonly _basePath = `${HELP_ROOT}${helpLocale(inject(LOCALE_ID))}/`;
  private readonly _topicsCache = new Map<string, Observable<HelpTopic[]>>();
  private readonly _documentsCache = new Map<string, Observable<string>>();

  getTopics(): Observable<HelpTopic[]> {
    return cachedRequest(this._topicsCache, INDEX_FILE, () =>
      this._http.get<HelpTopic[]>(this._basePath + INDEX_FILE),
    );
  }

  loadDocument(path: string): Observable<string> {
    return cachedRequest(this._documentsCache, path, () =>
      this._http
        .get(this._basePath + path, { responseType: 'text' })
        .pipe(map((markdown) => this._resolveImages(markdown, path))),
    );
  }

  /**
   * Documents with every word of `query` in their title or visible text, ignoring case and
   * diacritics, best matches first (see `searchDocuments`). A document that does not load is
   * searched by its title only, so it does not leave the search without results.
   */
  searchInAllDocs(query: string): Observable<SearchResult[]> {
    return this.getTopics().pipe(
      take(1),
      map((topics) => listDocuments(topics)),
      switchMap((documents) => {
        if (!documents.length) {
          return of([]);
        }

        const loaders = documents.map((document) =>
          this.loadDocument(document.document).pipe(
            catchError(() => of('')),
            map((markdown) => ({ ...document, text: markdownToPlainText(markdown) })),
          ),
        );

        return forkJoin(loaders).pipe(map((texts) => searchDocuments(texts, query)));
      }),
    );
  }

  /** Absolute URL of a help document of the running language. */
  documentUrl(path: string): URL {
    return new URL(this._basePath + path, this._document.baseURI);
  }

  /**
   * The help document and heading that `href`, written in `fromDocument`, points to: a relative
   * path to another `.md` of the same language, or `#anchor` for a heading of the same document.
   * `null` for anything else (another site, a page of the app).
   */
  resolveLink(href: string, fromDocument: string): HelpLink | null {
    let url: URL;
    try {
      url = new URL(href, this.documentUrl(fromDocument));
    } catch {
      return null;
    }
    const base = new URL(this._basePath, this._document.baseURI);
    if (url.origin !== base.origin || !url.pathname.startsWith(base.pathname)) return null;
    if (!url.pathname.endsWith('.md')) return null;

    return {
      document: decodeURIComponent(url.pathname.slice(base.pathname.length)),
      anchor: decodeURIComponent(url.hash.slice(1)),
    };
  }

  /**
   * Documents write image paths relative to themselves (`imgs/capture.webp`), but the
   * rendered HTML resolves them against the page, so they are made absolute here.
   */
  private _resolveImages(markdown: string, path: string): string {
    const documentUrl = this.documentUrl(path);

    return markdown.replace(MARKDOWN_IMAGE, (_match, alt: string, target: string) => {
      const [source, ...title] = target.trim().split(/\s+/);
      const resolved = new URL(source, documentUrl).href;
      return `![${alt}](${[resolved, ...title].join(' ')})`;
    });
  }
}

export function flattenTopics(topics: readonly HelpTopic[]): HelpTopic[] {
  return topics.flatMap((topic) => [topic, ...flattenTopics(topic.items ?? [])]);
}

/** Documents of the index in the order they appear, each with the groups that contain it. */
export function listDocuments(topics: readonly HelpTopic[], trail: string[] = []): HelpDocument[] {
  return topics.flatMap(({ label, document, items }) => [
    ...(document ? [{ label, document, trail }] : []),
    ...listDocuments(items ?? [], [...trail, label]),
  ]);
}
