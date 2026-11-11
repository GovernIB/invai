import { ChangeDetectionStrategy, Component, computed, inject, Injector, input } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { Router } from '@angular/router';
import { of } from 'rxjs';
import { HELP_MODAL_LABELS } from './help-modal.i18n';
import { HelpModalService } from './help-modal.service';
import { HelpTopicPath, routeHelpTopic } from './help-topics';

/** Contextual help affordance shared by page headers and feature dialogs. */
@Component({
  selector: 'app-help-button',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (document()) {
      <button type="button" class="help-button" [class.help-button--on-primary]="onPrimary()"
        [attr.aria-label]="label" [title]="label" aria-keyshortcuts="Control+K" (click)="open($event)">
        <i class="pi pi-question-circle" aria-hidden="true"></i>
      </button>
    }
  `,
  styles: `
    :host { display: inline-flex; flex-shrink: 0; }
    .help-button { display: inline-flex; align-items: center; justify-content: center;
      width: 40px; height: 40px; border: 0; border-radius: 50%; background: transparent;
      color: var(--p-text-muted-color); cursor: pointer; }
    .help-button:hover { background: var(--p-content-hover-background); color: var(--p-text-color); }
    .help-button:focus-visible { outline: 2px solid var(--p-primary-color); outline-offset: -2px; }
    .help-button--on-primary { color: var(--invai-page-frame-color); }
    .help-button--on-primary:hover { background: var(--invai-page-frame-color); color: var(--invai-page-frame-background); }
    .help-button--on-primary:focus-visible { outline-color: var(--invai-page-frame-color); }
  `,
})
export class HelpButton {
  readonly topic = input<HelpTopicPath | null>(null);
  readonly onPrimary = input(false);
  private readonly router = inject(Router, { optional: true });
  private readonly injector = inject(Injector);
  private readonly navigation = toSignal(this.router?.events ?? of(null), { initialValue: null });
  protected readonly label = HELP_MODAL_LABELS.openHelp;
  protected readonly document = computed(() => {
    this.navigation();
    const root = this.router?.routerState?.snapshot?.root;
    return this.topic() ?? (root ? routeHelpTopic(root) : null);
  });
  protected open(event: Event): void {
    (event.currentTarget as HTMLButtonElement).focus();
    this.injector.get(HelpModalService).open(this.document() ?? undefined);
  }
}
