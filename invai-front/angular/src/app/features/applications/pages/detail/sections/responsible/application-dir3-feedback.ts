import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { Button } from 'primeng/button';
import { ApplicationDir3CheckState } from './application-dir3-check.state';
import { DIR3_COPY } from './application-dir3.i18n';

@Component({
  selector: 'app-application-dir3-feedback',
  imports: [Button],
  template: `
    <div [attr.aria-busy]="check().phase() === 'loading'">
      @if (check().phase() === 'loading') {
        <p role="status">{{ copy.checking }}</p>
      } @else if (check().phase() === 'error') {
        <p role="alert">{{ check().denied() ? copy.denied : copy.error }}</p>
        <p-button
          type="button"
          icon="pi pi-refresh"
          [label]="copy.retry"
          [outlined]="true"
          (onClick)="retry.emit()"
        />
      } @else if (check().mismatch()) {
        <p role="status">{{ check().result()?.groupDir3 ? copy.mismatch : missingDir3Message() }}</p>
      }
    </div>
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationDir3Feedback {
  check = input.required<ApplicationDir3CheckState>();
  missingDir3Message = input(DIR3_COPY.unknown);
  retry = output<void>();
  protected readonly copy = DIR3_COPY;
}
