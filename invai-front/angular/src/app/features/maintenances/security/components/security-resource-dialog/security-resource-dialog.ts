import { ChangeDetectionStrategy, Component, computed, input, model, output } from '@angular/core';
import { AbstractControl, ReactiveFormsModule } from '@angular/forms';
import {
  CrudEntityDialog,
  CrudEntityDialogMode,
} from '@components/crud-entity-dialog/crud-entity-dialog';
import { FloatLabel } from 'primeng/floatlabel';
import { InputText } from 'primeng/inputtext';
import { ProgressSpinner } from 'primeng/progressspinner';

import { SecurityResourceFormGroup } from '../../forms/security-resource-forms.factory';
import {
  SECURITY_DIALOG_ACTIONS,
  SECURITY_DIALOG_LOADING,
  SECURITY_DIALOG_TITLES,
  SECURITY_FILTER_LABELS,
  SECURITY_NAME_ES_MAX_LENGTH_ERROR,
  SECURITY_NAME_MAX_LENGTH_ERROR,
  SECURITY_REQUIRED_ERROR,
} from '../../security.i18n';

@Component({
  selector: 'app-security-resource-dialog',
  standalone: true,
  imports: [CrudEntityDialog, FloatLabel, InputText, ProgressSpinner, ReactiveFormsModule],
  templateUrl: './security-resource-dialog.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SecurityResourceDialog {
  visible = model(false);
  form = input.required<SecurityResourceFormGroup>();
  mode = input.required<CrudEntityDialogMode>();
  entityLabel = input.required<string>();
  bilingual = input.required<boolean>();
  idPrefix = input.required<string>();
  isLoading = input(false);
  isSaving = input(false);
  isDeleting = input(false);
  canRestore = input(false);

  submitForm = output<void>();
  closed = output<void>();
  restore = output<void>();
  edit = output<void>();
  cancelEdit = output<void>();
  deactivate = output<void>();

  protected readonly labels = SECURITY_FILTER_LABELS;
  protected readonly requiredError = SECURITY_REQUIRED_ERROR;
  protected readonly nameMaxLengthError = SECURITY_NAME_MAX_LENGTH_ERROR;
  protected readonly nameEsMaxLengthError = SECURITY_NAME_ES_MAX_LENGTH_ERROR;
  protected readonly loadingLabel = SECURITY_DIALOG_LOADING;
  protected readonly title = computed(() => SECURITY_DIALOG_TITLES(this.entityLabel())[this.mode()]);
  protected readonly actionAriaLabels = computed(() => SECURITY_DIALOG_ACTIONS(this.entityLabel()));
  protected readonly hasUnsavedChanges = () => this.mode() === 'edit' && this.form().dirty;

  protected isInvalid(control: AbstractControl): boolean {
    return control.invalid && (control.dirty || control.touched);
  }

  protected onSubmit(): void {
    if (!this.isLoading() && !this.isSaving() && this.mode() !== 'view') this.submitForm.emit();
  }
}
