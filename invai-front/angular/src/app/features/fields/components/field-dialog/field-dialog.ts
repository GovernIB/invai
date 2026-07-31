import { ChangeDetectionStrategy, Component, computed, input, model, output } from '@angular/core';
import { AbstractControl, ReactiveFormsModule } from '@angular/forms';
import {
  CrudEntityDialog,
  CrudEntityDialogAriaLabels,
} from '@components/crud-entity-dialog/crud-entity-dialog';
import { FloatLabel } from 'primeng/floatlabel';
import { InputText } from 'primeng/inputtext';
import { ProgressSpinner } from 'primeng/progressspinner';

import { FieldFormGroup } from '../../forms/field-form.factory';
import {
  FIELD_DIALOG_ACCEPT_ARIA_LABEL,
  FIELD_DIALOG_ADD_ARIA_LABEL,
  FIELD_DIALOG_CANCEL_ARIA_LABEL,
  FIELD_DIALOG_CLOSE_ARIA_LABEL,
  FIELD_DIALOG_DEACTIVATE_ARIA_LABEL,
  FIELD_DIALOG_EDIT_ARIA_LABEL,
  FIELD_DIALOG_LABELS,
  FIELD_DIALOG_LOADING,
  FIELD_DIALOG_NAME_ES_MAX_LENGTH_ERROR,
  FIELD_DIALOG_NAME_MAX_LENGTH_ERROR,
  FIELD_DIALOG_REQUIRED_ERROR,
  FIELD_DIALOG_RESTORE_ARIA_LABEL,
  FIELD_DIALOG_SAVE_ARIA_LABEL,
  FIELD_DIALOG_TITLES,
} from './field-dialog.i18n';

export type FieldDialogMode = 'create' | 'view' | 'edit';

@Component({
  selector: 'app-field-dialog',
  standalone: true,
  imports: [
    CrudEntityDialog,
    FloatLabel,
    InputText,
    ProgressSpinner,
    ReactiveFormsModule,
  ],
  templateUrl: './field-dialog.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FieldDialog {
  visible = model(false);
  form = input.required<FieldFormGroup>();
  mode = input.required<FieldDialogMode>();
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

  protected readonly labels = FIELD_DIALOG_LABELS;
  protected readonly requiredError = FIELD_DIALOG_REQUIRED_ERROR;
  protected readonly nameMaxLengthError = FIELD_DIALOG_NAME_MAX_LENGTH_ERROR;
  protected readonly nameEsMaxLengthError = FIELD_DIALOG_NAME_ES_MAX_LENGTH_ERROR;
  protected readonly loadingLabel = FIELD_DIALOG_LOADING;
  protected readonly title = computed(() => FIELD_DIALOG_TITLES[this.mode()]);
  protected readonly actionAriaLabels: CrudEntityDialogAriaLabels = {
    accept: FIELD_DIALOG_ACCEPT_ARIA_LABEL,
    add: FIELD_DIALOG_ADD_ARIA_LABEL,
    cancel: FIELD_DIALOG_CANCEL_ARIA_LABEL,
    close: FIELD_DIALOG_CLOSE_ARIA_LABEL,
    deactivate: FIELD_DIALOG_DEACTIVATE_ARIA_LABEL,
    edit: FIELD_DIALOG_EDIT_ARIA_LABEL,
    restore: FIELD_DIALOG_RESTORE_ARIA_LABEL,
    save: FIELD_DIALOG_SAVE_ARIA_LABEL,
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
