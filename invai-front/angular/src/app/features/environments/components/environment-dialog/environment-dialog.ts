import { ChangeDetectionStrategy, Component, computed, input, model, output } from '@angular/core';
import { AbstractControl, ReactiveFormsModule } from '@angular/forms';
import {
  CrudEntityDialog,
  CrudEntityDialogAriaLabels,
} from '@components/crud-entity-dialog/crud-entity-dialog';
import { FloatLabel } from 'primeng/floatlabel';
import { InputText } from 'primeng/inputtext';
import { ProgressSpinner } from 'primeng/progressspinner';

import { EnvironmentFormGroup } from '../../forms/environment-form.factory';
import {
  ENVIRONMENT_DIALOG_ACCEPT_ARIA_LABEL,
  ENVIRONMENT_DIALOG_ADD_ARIA_LABEL,
  ENVIRONMENT_DIALOG_CANCEL_ARIA_LABEL,
  ENVIRONMENT_DIALOG_CLOSE_ARIA_LABEL,
  ENVIRONMENT_DIALOG_DEACTIVATE_ARIA_LABEL,
  ENVIRONMENT_DIALOG_EDIT_ARIA_LABEL,
  ENVIRONMENT_DIALOG_CODE_MAX_LENGTH_ERROR,
  ENVIRONMENT_DIALOG_LABELS,
  ENVIRONMENT_DIALOG_LOADING,
  ENVIRONMENT_DIALOG_NAME_ES_MAX_LENGTH_ERROR,
  ENVIRONMENT_DIALOG_NAME_MAX_LENGTH_ERROR,
  ENVIRONMENT_DIALOG_REQUIRED_ERROR,
  ENVIRONMENT_DIALOG_RESTORE_ARIA_LABEL,
  ENVIRONMENT_DIALOG_SAVE_ARIA_LABEL,
  ENVIRONMENT_DIALOG_TITLES,
} from './environment-dialog.i18n';

export type EnvironmentDialogMode = 'create' | 'view' | 'edit';

@Component({
  selector: 'app-environment-dialog',
  standalone: true,
  imports: [
    CrudEntityDialog,
    FloatLabel,
    InputText,
    ProgressSpinner,
    ReactiveFormsModule,
  ],
  templateUrl: './environment-dialog.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EnvironmentDialog {
  visible = model(false);
  form = input.required<EnvironmentFormGroup>();
  mode = input.required<EnvironmentDialogMode>();
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

  protected readonly labels = ENVIRONMENT_DIALOG_LABELS;
  protected readonly requiredError = ENVIRONMENT_DIALOG_REQUIRED_ERROR;
  protected readonly codeMaxLengthError = ENVIRONMENT_DIALOG_CODE_MAX_LENGTH_ERROR;
  protected readonly nameMaxLengthError = ENVIRONMENT_DIALOG_NAME_MAX_LENGTH_ERROR;
  protected readonly nameEsMaxLengthError = ENVIRONMENT_DIALOG_NAME_ES_MAX_LENGTH_ERROR;
  protected readonly loadingLabel = ENVIRONMENT_DIALOG_LOADING;
  protected readonly title = computed(() => ENVIRONMENT_DIALOG_TITLES[this.mode()]);
  protected readonly actionAriaLabels: CrudEntityDialogAriaLabels = {
    accept: ENVIRONMENT_DIALOG_ACCEPT_ARIA_LABEL,
    add: ENVIRONMENT_DIALOG_ADD_ARIA_LABEL,
    cancel: ENVIRONMENT_DIALOG_CANCEL_ARIA_LABEL,
    close: ENVIRONMENT_DIALOG_CLOSE_ARIA_LABEL,
    deactivate: ENVIRONMENT_DIALOG_DEACTIVATE_ARIA_LABEL,
    edit: ENVIRONMENT_DIALOG_EDIT_ARIA_LABEL,
    restore: ENVIRONMENT_DIALOG_RESTORE_ARIA_LABEL,
    save: ENVIRONMENT_DIALOG_SAVE_ARIA_LABEL,
  };
  protected readonly hasUnsavedChanges = () =>
    this.mode() === 'edit' && this.form().dirty;

  protected isInvalid(control: AbstractControl): boolean {
    return control.invalid && (control.dirty || control.touched);
  }

  protected onSubmit(): void {
    if (this.isLoading() || this.isSaving() || this.mode() === 'view') return;
    this.submitForm.emit();
  }

}
