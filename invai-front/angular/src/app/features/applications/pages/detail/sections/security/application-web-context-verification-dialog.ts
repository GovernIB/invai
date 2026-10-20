import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  OnDestroy,
  input,
  model,
  output,
  viewChild,
} from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { CrudEntityDialog } from '@components/crud-entity-dialog/crud-entity-dialog';
import { PrimeIcons } from 'primeng/api';
import { Textarea } from 'primeng/textarea';
import { ApplicationWebContextVerificationForm } from '../../../../forms/application-security-form.factory';
import {
  APPLICATION_SECURITY_DIALOG_ARIA_LABELS,
  APPLICATION_SECURITY_TABLE_ACTIONS,
  APPLICATION_WEB_CONTEXT_VERIFICATION,
} from './application-security-section.i18n';

@Component({
  selector: 'app-application-web-context-verification-dialog',
  standalone: true,
  imports: [CrudEntityDialog, ReactiveFormsModule, Textarea],
  templateUrl: './application-web-context-verification-dialog.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationWebContextVerificationDialog implements AfterViewInit, OnDestroy {
  visible = model(false);
  form = input.required<ApplicationWebContextVerificationForm>();
  contextName = input.required<string>();
  isSaving = input(false);

  closed = output<void>();
  submitForm = output<void>();

  private readonly reasonInput = viewChild<ElementRef<HTMLTextAreaElement>>('reasonInput');
  private focusTimer: ReturnType<typeof setTimeout> | null = null;
  protected readonly copy = APPLICATION_WEB_CONTEXT_VERIFICATION;
  protected readonly verifyLabel = APPLICATION_SECURITY_TABLE_ACTIONS.verify;
  protected readonly icons = PrimeIcons;
  protected readonly ariaLabels = {
    ...APPLICATION_SECURITY_DIALOG_ARIA_LABELS,
    cancel: APPLICATION_WEB_CONTEXT_VERIFICATION.cancel,
    close: APPLICATION_WEB_CONTEXT_VERIFICATION.close,
  };
  protected readonly hasUnsavedChanges = () => this.form().dirty;

  ngAfterViewInit(): void {
    this.focusTimer = setTimeout(() => this.reasonInput()?.nativeElement.focus());
  }

  ngOnDestroy(): void {
    if (this.focusTimer !== null) clearTimeout(this.focusTimer);
  }

  protected reasonInvalid(): boolean {
    const control = this.form().controls.reason;
    return control.invalid && (control.touched || control.dirty);
  }
}
