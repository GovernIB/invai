import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { ServerErrorDialogService } from '@core/services/server-error-dialog.service';
import { PrimeIcons } from 'primeng/api';
import { Button } from 'primeng/button';
import { Dialog } from 'primeng/dialog';

import {
  SERVER_ERROR_DIALOG_CLOSE_ARIA_LABEL,
  SERVER_ERROR_DIALOG_CLOSE_LABEL,
} from './server-error-dialog.i18n';

@Component({
  selector: 'app-server-error-dialog',
  standalone: true,
  imports: [Button, Dialog],
  templateUrl: './server-error-dialog.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ServerErrorDialog {
  private readonly _dialogService = inject(ServerErrorDialogService);

  protected readonly visible = this._dialogService.visible;
  protected readonly error = this._dialogService.error;
  protected readonly closeLabel = SERVER_ERROR_DIALOG_CLOSE_LABEL;
  protected readonly closeAriaLabel = SERVER_ERROR_DIALOG_CLOSE_ARIA_LABEL;
  protected readonly errorIcon = PrimeIcons.EXCLAMATION_TRIANGLE;

  protected close(): void {
    this._dialogService.close();
  }

  protected onVisibleChange(visible: boolean): void {
    if (!visible) this._dialogService.close();
  }
}
