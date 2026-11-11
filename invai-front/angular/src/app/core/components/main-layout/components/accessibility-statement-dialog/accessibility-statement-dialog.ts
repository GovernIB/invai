import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  model,
  output,
  viewChild,
} from '@angular/core';
import { accessibilityEvaluation } from '@pkg';
import { Dialog } from 'primeng/dialog';

import {
  ACCESSIBILITY_STATEMENT_DIALOG_CLOSE_ARIA_LABEL,
  ACCESSIBILITY_STATEMENT_DIALOG_TITLE,
  ACCESSIBILITY_STATEMENT_LINKS,
} from './accessibility-statement-dialog.i18n';

@Component({
  selector: 'app-accessibility-statement-dialog',
  imports: [Dialog],
  templateUrl: './accessibility-statement-dialog.html',
  styleUrl: './accessibility-statement-dialog.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AccessibilityStatementDialog {
  visible = model(false);
  hidden = output<void>();

  private readonly _start = viewChild<ElementRef<HTMLElement>>('statementStart');

  protected readonly title = ACCESSIBILITY_STATEMENT_DIALOG_TITLE;
  protected readonly closeAriaLabel = ACCESSIBILITY_STATEMENT_DIALOG_CLOSE_ARIA_LABEL;
  protected readonly evaluation = accessibilityEvaluation;
  protected readonly links = ACCESSIBILITY_STATEMENT_LINKS;

  /**
   * The dialog only holds reading content, so focus starts on its first
   * paragraph instead of the close button: screen readers read the statement
   * as soon as the dialog opens.
   */
  protected focusStart(): void {
    this._start()?.nativeElement.focus();
  }
}
