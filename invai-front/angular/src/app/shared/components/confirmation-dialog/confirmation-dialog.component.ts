import { ChangeDetectionStrategy, Component, input, model, output } from '@angular/core';
import { PrimeIcons } from 'primeng/api';
import { Button } from 'primeng/button';
import { Dialog } from 'primeng/dialog';
import {
  CONFIRMATION_DIALOG_CANCEL_ARIA_LABEL,
  CONFIRMATION_DIALOG_CANCEL_LABEL,
  CONFIRMATION_DIALOG_CONFIRM_ARIA_LABEL,
  CONFIRMATION_DIALOG_CONFIRM_LABEL,
} from './confirmation-dialog.i18n';

@Component({
  selector: 'app-confirmation-dialog',
  standalone: true,
  imports: [Button, Dialog],
  templateUrl: './confirmation-dialog.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ConfirmationDialogComponent {
  visible = model(false);

  title = input.required<string>();
  message = input.required<string>();
  cancelLabel = input(CONFIRMATION_DIALOG_CANCEL_LABEL);
  confirmLabel = input(CONFIRMATION_DIALOG_CONFIRM_LABEL);
  cancelAriaLabel = input(CONFIRMATION_DIALOG_CANCEL_ARIA_LABEL);
  confirmAriaLabel = input(CONFIRMATION_DIALOG_CONFIRM_ARIA_LABEL);
  confirmIcon = input(PrimeIcons.CHECK);
  severity = input<'secondary' | 'success' | 'info' | 'warn' | 'danger' | 'help' | 'contrast'>(
    'danger',
  );

  confirm = output<void>();
  cancel = output<void>();

  protected readonly PrimeIcons = PrimeIcons;
  private _closingFromAction = false;

  protected onCancel(): void {
    this._closingFromAction = true;
    this.cancel.emit();
    this.visible.set(false);
  }

  protected onConfirm(): void {
    this._closingFromAction = true;
    this.confirm.emit();
    this.visible.set(false);
  }

  protected onHide(): void {
    if (this._closingFromAction) {
      this._closingFromAction = false;
      return;
    }

    this.cancel.emit();
  }
}
