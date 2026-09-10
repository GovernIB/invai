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
  auxiliaryLabel = input<string | null>(null);
  auxiliaryIcon = input<string>(PrimeIcons.ARROW_RIGHT);
  auxiliaryAriaLabel = input<string | null>(null);
  cancelAriaLabel = input(CONFIRMATION_DIALOG_CANCEL_ARIA_LABEL);
  confirmAriaLabel = input(CONFIRMATION_DIALOG_CONFIRM_ARIA_LABEL);
  confirmIcon = input(PrimeIcons.CHECK);
  confirmOutlined = input(true);
  width = input('24rem');
  cancelOnHide = input(true);
  closeOnConfirm = input(true);
  pending = input(false);
  severity = input<
    'primary' | 'secondary' | 'success' | 'info' | 'warn' | 'danger' | 'help' | 'contrast'
  >('danger');

  confirm = output<void>();
  auxiliary = output<void>();
  cancel = output<void>();
  dismissed = output<void>();

  protected readonly PrimeIcons = PrimeIcons;
  private _closingFromAction = false;

  protected onCancel(): void {
    if (this.pending()) return;
    this._closingFromAction = true;
    this.cancel.emit();
    this.visible.set(false);
  }

  protected onConfirm(): void {
    if (this.pending()) return;
    if (!this.closeOnConfirm()) {
      this.confirm.emit();
      return;
    }
    this._closingFromAction = true;
    this.confirm.emit();
    this.visible.set(false);
  }

  protected onAuxiliary(): void {
    if (this.pending() || !this.auxiliaryLabel()) return;
    this._closingFromAction = true;
    this.auxiliary.emit();
    this.visible.set(false);
  }

  protected onHide(): void {
    if (this._closingFromAction) {
      this._closingFromAction = false;
      return;
    }

    if (this.cancelOnHide()) {
      this.cancel.emit();
    } else {
      this.dismissed.emit();
    }
  }
}
