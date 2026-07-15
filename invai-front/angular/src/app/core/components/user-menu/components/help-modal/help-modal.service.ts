import { DOCUMENT } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { MenuItem } from 'primeng/api';
import { forkJoin, map, Observable, of, switchMap, take } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class HelpModalService {
  private readonly _basePath = 'help/';
  private readonly _markdownImagePattern = /!\[([^\]]*)\]\(([^)]+)\)/g;
  private readonly _contentCache = new Map<string, string>();

  private readonly _http = inject(HttpClient);
  private readonly _document = inject(DOCUMENT);

  readonly visible = signal(false);

  open() {
    this.visible.set(true);
  }

  close() {
    this.visible.set(false);
  }

  toggle() {
    this.visible.update((v) => !v);
  }

  getGroups() {
    return this._http.get<MenuItem[]>(this._basePath + 'index.json');
  }

  loadContent(path: string) {
    const cached = this._contentCache.get(path);
    if (cached) {
      return of(cached);
    }

    return this._http
      .get(this._basePath + path, {
        responseType: 'text',
      })
      .pipe(
        map((md) => this._fixRelativeImages(md, path)),
        map((md) => {
          this._contentCache.set(path, md);
          return md;
        }),
      );
  }

  searchInAllDocs(term: string): Observable<SearchResult[]> {
    const lower = term.toLowerCase();

    return this.getGroups().pipe(
      take(1),
      map((groups) => this._flattenMenu(groups)),
      switchMap((items) => {
        const docs = items.filter(
          (item): item is MenuItem & { value: string } =>
            typeof item['value'] === 'string' && item['value'].length > 0,
        );

        if (!docs.length) {
          return of([]);
        }

        const loaders = docs.map((doc) =>
          this.loadContent(doc.value).pipe(map((content) => ({ doc, content }))),
        );

        return forkJoin(loaders).pipe(
          map((results) =>
            results
              .filter((r) => r.content.toLowerCase().includes(lower))
              .map((r) => ({
                label: r.doc.label ?? r.doc.value,
                value: r.doc.value,
                snippet: this._buildSnippet(r.content, lower),
              })),
          ),
        );
      }),
    );
  }

  private _flattenMenu(items: MenuItem[] | undefined) {
    const result: MenuItem[] = [];
    if (!items) return result;

    const stack: MenuItem[] = [...items];

    while (stack.length) {
      const item = stack.shift()!;
      result.push(item);
      if (item.items?.length) {
        stack.push(...(item.items));
      }
    }

    return result;
  }

  private _buildSnippet(content: string, termLower: string) {
    const lines = content.split('\n');

    let hitIndex = -1;
    for (let i = 0; i < lines.length; i++) {
      if (lines[i].toLowerCase().includes(termLower)) {
        hitIndex = i;
        break;
      }
    }

    if (hitIndex === -1) {
      const slice = lines.slice(0, 6);
      const filtered = slice.filter((line) => !/!\[[^\]]*]\([^)]+\)/.test(line));
      return filtered.join('\n');
    }

    const start = Math.max(0, hitIndex - 3);
    const end = Math.min(lines.length, hitIndex + 4);
    const slice = lines.slice(start, end);
    const filtered = slice.filter((line) => !/!\[[^\]]*]\([^)]+\)/.test(line));

    const prefix = start > 0 ? '…\n' : '';
    const suffix = end < lines.length ? '\n…' : '';

    return prefix + filtered.join('\n') + suffix;
  }

  private _fixRelativeImages(md: string, docValue: string): string {
    // docValue: "01_Expedientes/02_Documents_del_jutjat_i_delictes/01_Demo_Markdown.md"
    const lastSlashIndex = docValue.lastIndexOf('/');
    const docDirectory = lastSlashIndex === -1 ? '' : docValue.slice(0, lastSlashIndex + 1);
    // ej: "01_Expedientes/02_Documents_del_jutjat_i_delictes/"

    // <base href>, ej: "/invaifront/ca/" (sale del index.html)
    const rawBaseHref = this._document.querySelector('base')?.getAttribute('href') ?? '/';

    // nos aseguramos de que termina en "/"
    const baseHref = rawBaseHref.endsWith('/') ? rawBaseHref : rawBaseHref + '/';

    // ruta base de las imágenes, ej:
    // "/invaifront/ca/help/01_Expedientes/02_Documents_del_jutjat_i_delictes/"
    const imagesBaseUrl = `${baseHref}${this._basePath}${docDirectory}`;

    return md.replace(this._markdownImagePattern, (_match, altText, originalSrc) => {
      const relativeSrc = String(originalSrc).trim(); // "imgs/00_DocJuzgadoCrear.webp"
      const absoluteSrc = `${imagesBaseUrl}${relativeSrc}`;

      return `![${altText}](${absoluteSrc})`;
    });
  }
}

export interface SearchResult {
  label: string;
  value: string;
  snippet: string;
}
