import { NgTemplateOutlet } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  effect,
  ElementRef,
  HostListener,
  inject,
  signal,
  untracked,
  viewChild,
} from '@angular/core';
import { takeUntilDestroyed, toObservable, toSignal } from '@angular/core/rxjs-interop';
import { MarkdownModule } from 'ngx-markdown';
import { AutoComplete } from 'primeng/autocomplete';
import { Button } from 'primeng/button';
import { Card } from 'primeng/card';
import { Dialog } from 'primeng/dialog';
import { DialogPassThrough } from 'primeng/types/dialog';
import { catchError, filter, map, merge, of, startWith, Subject, switchMap } from 'rxjs';
import { HELP_MODAL_LABELS, HELP_MODAL_RESULTS_FOUND } from './help-modal.i18n';
import { flattenTopics, HelpContentService, HelpLink } from './help-content.service';
import { headingAnchor } from './help-search';
import { HelpModalService } from './help-modal.service';
import { HelpTopic, SearchResult } from './help-topic.model';
import { groupsContaining, TopicNode, toTopicNodes } from './help-topic-tree';

type TopicsState =
  { status: 'loading' } | { status: 'error' } | { status: 'ready'; topics: HelpTopic[] };

const OPEN_TOPIC_DESCRIPTION_ID = 'help-modal-open-topic';
const HEADINGS = 'h1, h2, h3, h4, h5, h6';

type DocumentState =
  | { status: 'idle' }
  | { status: 'loading'; path: string }
  | { status: 'error'; path: string }
  | { status: 'ready'; path: string; content: string };

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [AutoComplete, Button, Card, Dialog, MarkdownModule, NgTemplateOutlet],
  selector: 'app-help-modal',
  styleUrl: './help-modal.scss',
  templateUrl: './help-modal.html',
})
export class HelpModal {
  private readonly _helpModalService = inject(HelpModalService);
  private readonly _helpContent = inject(HelpContentService);
  private readonly _documentContent = viewChild('documentContent', { read: ElementRef });
  private readonly _search = viewChild(AutoComplete);

  @HostListener('document:keydown', ['$event'])
  handleKeydown(event: KeyboardEvent) {
    const modifierPressed = event.metaKey || event.ctrlKey;

    if (modifierPressed && event.key.toLowerCase() === 'k') {
      event.preventDefault();
      this._helpModalService.toggle();
    }
  }

  protected readonly visible = this._helpModalService.visible;
  protected readonly labels = HELP_MODAL_LABELS;
  protected readonly searchResults = signal<SearchResult[]>([]);
  protected readonly searchAnnouncement = signal('');
  // PrimeNG's own status region says a topic is selected as soon as anything is typed, because
  // the typed text is the value of the field. A blank message keeps it quiet; the help announces
  // the number of topics found instead (searchAnnouncement).
  protected readonly silentMessage = ' ';
  protected readonly openTopicDescriptionId = OPEN_TOPIC_DESCRIPTION_ID;
  // Focus stays in the search box when the dialog opens, so the dialog describes itself with
  // the topic on show: screen readers announce it on entering the dialog.
  protected readonly dialogPassThrough: DialogPassThrough = {
    root: {
      'aria-describedby': OPEN_TOPIC_DESCRIPTION_ID,
      onkeydown: (event: KeyboardEvent) => this.closeFromKeyboard(event),
    },
    content: { style: { display: 'flex', overflow: 'hidden', minHeight: '0' } },
  };

  private readonly _searchTerms = new Subject<string>();
  private _lastTerm = '';
  private readonly _topicsRetries = new Subject<void>();
  private readonly _documentRequests = new Subject<string>();
  // Focus goes to the heading of a topic only when the reader picks it, not when a reopened
  // dialog renders the last topic again, and only once its document is on the page.
  private _focusDocumentWhenReady = false;
  // Heading a link pointed to (`other.md#anchor`), focused instead of the first one.
  private _anchorToFocus = '';
  // Where each help link of the document on show points to, worked out once it is rendered.
  private _links = new WeakMap<Element, HelpLink>();
  private _documentRendered = false;
  // After a search result is chosen, AutoComplete returns focus to its input in a timeout and
  // then emits onHide; the heading must get focus after that, not before.
  private _waitingForSearchToClose = false;

  constructor() {
    // Each term replaces the previous search, so a slower answer for an older term is
    // discarded. A failed search shows no results. The autocomplete shows its own spinner
    // from the moment it asks for suggestions until they change.
    this._searchTerms
      .pipe(
        switchMap((term) =>
          term
            ? this._helpContent.searchInAllDocs(term).pipe(catchError(() => of<SearchResult[]>([])))
            : of<SearchResult[]>([]),
        ),
        takeUntilDestroyed(),
      )
      .subscribe((results) => {
        this.searchResults.set(results);
        this.searchAnnouncement.set(
          !this._lastTerm
            ? ''
            : results.length
              ? HELP_MODAL_RESULTS_FOUND(results.length)
              : this.labels.emptyMessage,
        );
      });

    this._helpModalService.topicRequests
      .pipe(takeUntilDestroyed())
      .subscribe((path) => this._showContextTopic(path));

    // Each opening starts with only the active topic's ancestors expanded. While open,
    // topic changes reveal their branch without discarding the reader's other expansions.
    let wasVisible = false;
    effect(() => {
      if (!this.visible()) {
        wasVisible = false;
        return;
      }
      const document = this.currentDocument();
      const groups = document ? groupsContaining(this.topicTree(), document) : [];
      const opening = !wasVisible;
      wasVisible = true;
      untracked(() =>
        this._expandedGroups.update((expanded) =>
          opening ? new Set(groups) : new Set([...expanded, ...groups]),
        ),
      );
    });
  }

  // The service caches the index: reopening the dialog does not request it again, and a
  // failed load is requested again on the next opening or retry.
  protected readonly topicsState = toSignal(
    merge(toObservable(this.visible).pipe(filter((visible) => visible)), this._topicsRetries).pipe(
      switchMap(() =>
        this._helpContent.getTopics().pipe(
          map((topics): TopicsState => ({ status: 'ready', topics })),
          catchError(() => of<TopicsState>({ status: 'error' })),
          startWith<TopicsState>({ status: 'loading' }),
        ),
      ),
    ),
    { initialValue: { status: 'loading' } as TopicsState },
  );

  // A newly selected topic replaces the previous request, so a slower answer for the
  // previous topic never replaces the current one.
  protected readonly documentState = toSignal(
    this._documentRequests.pipe(
      switchMap((path) =>
        this._helpContent.loadDocument(path).pipe(
          map((content): DocumentState => ({ status: 'ready', path, content })),
          catchError(() => of<DocumentState>({ status: 'error', path })),
          startWith<DocumentState>({ status: 'loading', path }),
        ),
      ),
    ),
    { initialValue: { status: 'idle' } as DocumentState },
  );

  private readonly _topics = computed(() => {
    const state = this.topicsState();
    return state.status === 'ready' ? state.topics : [];
  });

  protected readonly selectedTopic = computed(() => {
    const state = this.documentState();
    if (state.status === 'idle') return null;

    return flattenTopics(this._topics()).find((topic) => topic.document === state.path) ?? null;
  });

  protected readonly topicTree = computed(() => toTopicNodes(this._topics()));

  /** Document of the topic on show, loaded or not, to mark it in the index. */
  protected readonly currentDocument = computed(() => {
    const state = this.documentState();
    return state.status === 'idle' ? null : state.path;
  });

  private readonly _expandedGroups = signal<ReadonlySet<string>>(new Set());

  protected isExpanded(node: TopicNode): boolean {
    return this._expandedGroups().has(node.key);
  }

  protected toggleGroup(node: TopicNode) {
    this._expandedGroups.update((expanded) => {
      const next = new Set(expanded);
      if (!next.delete(node.key)) next.add(node.key);
      return next;
    });
  }

  protected selectTopic(document: string) {
    this._select(document);
  }

  protected clearSearchAnnouncement() {
    this._lastTerm = '';
    this.searchAnnouncement.set('');
  }

  protected onSelectResult(result: SearchResult) {
    this.clearSearchAnnouncement();
    this._waitingForSearchToClose = true;
    this._select(result.document);
  }

  protected onSearchClosed() {
    if (!this._waitingForSearchToClose) return;
    this._waitingForSearchToClose = false;
    this._focusDocument();
  }

  protected onSearchComplete(event: { query: string }) {
    this._lastTerm = event.query.trim();
    this._searchTerms.next(this._lastTerm);
  }

  protected retryTopics() {
    this._topicsRetries.next();
  }

  protected retryDocument(path: string) {
    this._select(path);
  }

  protected restoreFocus() {
    this._helpModalService.restoreFocus();
  }

  private closeFromKeyboard(event: KeyboardEvent) {
    if (event.key !== 'Escape') return;
    if (this._search()?.overlayVisible) return;
    // Handle Escape before it reaches the document listeners of dialogs underneath.
    // PrimeNG handles an open search popup before the help itself.
    event.stopPropagation();
    this._helpModalService.close();
  }

  protected onDocumentRendered() {
    this._documentRendered = true;
    this._prepareLinks();
    this._focusDocument();
  }

  /**
   * Links to other topics open inside the help, and `#anchor` links move to their heading:
   * left alone, the browser would resolve them against the page and leave the app. Links to
   * other sites open in a new tab (see `_prepareLinks`).
   */
  protected onDocumentClick(event: MouseEvent) {
    const modified = event.ctrlKey || event.metaKey || event.shiftKey || event.altKey;
    if (event.defaultPrevented || event.button !== 0 || modified) return;

    const link = (event.target as Element | null)?.closest('a[href]');
    const current = this.currentDocument();
    const target = link && current ? this._links.get(link) : undefined;
    if (!target) return;

    event.preventDefault();
    if (target.document === current) this._focusHeading(target.anchor);
    else this._select(target.document, target.anchor);
  }

  /**
   * Resolves the links of the rendered document: help links get their real address (so a new
   * tab or a copied link work) and are remembered for `onDocumentClick`; links to other sites
   * open in a new tab and say so, with the icon and hidden text the rest of the app uses.
   */
  private _prepareLinks() {
    const container = this._documentContent()?.nativeElement as HTMLElement | undefined;
    const current = this.currentDocument();
    if (!container || !current) return;

    this._links = new WeakMap();
    for (const link of Array.from(container.querySelectorAll<HTMLAnchorElement>('a[href]'))) {
      const target = this._helpContent.resolveLink(link.getAttribute('href')!, current);
      if (target) {
        const url = this._helpContent.documentUrl(target.document);
        url.hash = target.anchor;
        link.href = url.href;
        this._links.set(link, target);
      } else if (/^https?:/i.test(link.href)) {
        link.target = '_blank';
        link.rel = 'noopener noreferrer';
        const icon = link.ownerDocument.createElement('i');
        icon.className = 'pi pi-external-link';
        icon.setAttribute('aria-hidden', 'true');
        const note = link.ownerDocument.createElement('span');
        note.className = 'sr-only';
        note.textContent = ` ${this.labels.opensInNewTab}`;
        link.append(icon, note);
      }
    }
  }

  /**
   * Moves focus to the heading of the topic the reader just picked, so screen readers
   * announce it and the next Tab continues inside the document.
   */
  private _focusDocument() {
    if (!this._focusDocumentWhenReady || !this._documentRendered || this._waitingForSearchToClose) {
      return;
    }
    this._focusDocumentWhenReady = false;

    const anchor = this._anchorToFocus;
    this._anchorToFocus = '';
    const container = this._documentContent()?.nativeElement as HTMLElement | undefined;
    if (!container) return;
    const target =
      this._headingFor(anchor) ?? container.querySelector<HTMLElement>(HEADINGS) ?? container;
    target.tabIndex = -1;
    target.focus();
  }

  /** Focuses the heading of the document on show that `anchor` names, if there is one. */
  private _focusHeading(anchor: string) {
    const heading = this._headingFor(anchor);
    if (!heading) return;
    heading.tabIndex = -1;
    heading.focus();
  }

  /**
   * The heading `anchor` names, worked out from its text as GitHub does: the sanitizer that
   * renders the document removes the `id` attributes.
   */
  private _headingFor(anchor: string): HTMLElement | null {
    const container = this._documentContent()?.nativeElement as HTMLElement | undefined;
    if (!anchor || !container) return null;
    const wanted = headingAnchor(anchor);
    return (
      Array.from(container.querySelectorAll<HTMLElement>(HEADINGS)).find(
        (heading) => headingAnchor(heading.textContent ?? '') === wanted,
      ) ?? null
    );
  }

  /**
   * Shows the document of the screen or dialog the help was opened from. Focus stays where the
   * dialog puts it, in the search box, and a document already on show is not loaded again.
   */
  private _showContextTopic(path: string) {
    this._focusDocumentWhenReady = false;
    const current = this.documentState();
    if (current.status !== 'idle' && current.status !== 'error' && current.path === path) return;

    this._documentRendered = false;
    this._documentRequests.next(path);
  }

  private _select(path: string, anchor = '') {
    const current = this.documentState();
    this._focusDocumentWhenReady = true;
    this._anchorToFocus = anchor;
    // Choosing the topic already shown renders nothing new, so no ready event will follow.
    this._documentRendered = current.status === 'ready' && current.path === path;
    this._documentRequests.next(path);
    this._focusDocument();
  }
}
