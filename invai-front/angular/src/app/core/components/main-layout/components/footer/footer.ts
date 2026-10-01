import { ChangeDetectionStrategy, Component, ElementRef, signal, viewChild } from '@angular/core';
import { version } from '@pkg';

import { AccessibilityStatementDialog } from '../accessibility-statement-dialog/accessibility-statement-dialog';
import { FOOTER_LABELS } from './footer.i18n';

@Component({
  selector: 'app-footer',
  imports: [AccessibilityStatementDialog],
  templateUrl: './footer.html',
  styleUrl: './footer.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Footer {
  private readonly _statementTrigger =
    viewChild.required<ElementRef<HTMLButtonElement>>('statementTrigger');

  protected readonly appVersion = version;
  protected readonly labels = FOOTER_LABELS;
  protected readonly isStatementVisible = signal(false);

  protected openStatement(): void {
    this.isStatementVisible.set(true);
  }

  protected restoreFocus(): void {
    this._statementTrigger().nativeElement.focus();
  }
}
