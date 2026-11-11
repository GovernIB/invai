import { DOCUMENT } from '@angular/common';
import { inject, Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { HelpTopicPath, routeHelpTopic } from './help-topics';

/**
 * Opens and closes the help and decides which document it shows. It depends on nothing but
 * the page and the router, so any screen or dialog can open the help; the content is loaded
 * by `HelpContentService`.
 */
@Injectable({ providedIn: 'root' })
export class HelpModalService {
  private readonly _document = inject(DOCUMENT);
  private readonly _router = inject(Router);

  readonly visible = signal(false);

  private readonly _topicRequests = new Subject<string>();
  /** Document to show when the dialog opens: the one of the open dialog or of the screen. */
  readonly topicRequests = this._topicRequests.asObservable();

  // Open dialogs with their own document, in opening order. Each entry is its own object, so
  // closing a dialog removes its entry even when another open dialog declares the same document.
  private readonly _dialogTopics: { document: string }[] = [];

  // The dialog opens from anywhere (Ctrl + K, the user menu), so the element to return to is
  // the one focused at the moment of opening.
  private _returnFocusTo: HTMLElement | null = null;

  /**
   * Opens the help on the document of the last open dialog that declares one or, failing that,
   * of the active screen. Without either, the last document read stays on show.
   */
  open(document?: string) {
    if (!this.visible()) {
      const active = this._document.activeElement;
      this._returnFocusTo =
        active instanceof HTMLElement && active !== this._document.body ? active : null;

      const topic =
        document ?? this._dialogTopics.at(-1)?.document ??
        routeHelpTopic(this._router.routerState.snapshot.root);
      if (topic) this._topicRequests.next(topic);
    }
    this.visible.set(true);
  }

  close() {
    this.visible.set(false);
  }

  toggle() {
    if (this.visible()) this.close();
    else this.open();
  }

  /**
   * Declares the document of a dialog while it is open; the returned function withdraws it.
   * Call it when the dialog opens and the release when it closes or is destroyed.
   */
  registerDialogTopic(document: HelpTopicPath): () => void {
    const entry = { document };
    this._dialogTopics.push(entry);
    return () => {
      const index = this._dialogTopics.indexOf(entry);
      if (index !== -1) this._dialogTopics.splice(index, 1);
    };
  }

  /** Focuses the element that had focus when the dialog opened, if it is still on the page. */
  restoreFocus() {
    const target = this._returnFocusTo;
    this._returnFocusTo = null;
    if (target?.isConnected) target.focus();
  }
}
