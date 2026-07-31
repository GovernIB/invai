import { ChangeDetectionStrategy, Component, computed, input, model, output } from '@angular/core';
import { AbstractControl, ReactiveFormsModule } from '@angular/forms';
import {
  CrudEntityDialog,
  CrudEntityDialogAriaLabels,
} from '@components/crud-entity-dialog/crud-entity-dialog';
import { FloatLabel } from 'primeng/floatlabel';
import { InputText } from 'primeng/inputtext';
import { ProgressSpinner } from 'primeng/progressspinner';

import { SystemTypeFormGroup } from '../../forms/system-type-form.factory';
import {
  SYSTEM_TYPE_DIALOG_ACCEPT_ARIA_LABEL,
  SYSTEM_TYPE_DIALOG_ADD_ARIA_LABEL,
  SYSTEM_TYPE_DIALOG_CANCEL_ARIA_LABEL,
  SYSTEM_TYPE_DIALOG_CLOSE_ARIA_LABEL,
  SYSTEM_TYPE_DIALOG_DEACTIVATE_ARIA_LABEL,
  SYSTEM_TYPE_DIALOG_EDIT_ARIA_LABEL,
  SYSTEM_TYPE_DIALOG_LABELS,
  SYSTEM_TYPE_DIALOG_LOADING,
  SYSTEM_TYPE_DIALOG_NAME_ES_MAX_LENGTH_ERROR,
  SYSTEM_TYPE_DIALOG_NAME_MAX_LENGTH_ERROR,
  SYSTEM_TYPE_DIALOG_REQUIRED_ERROR,
  SYSTEM_TYPE_DIALOG_RESTORE_ARIA_LABEL,
  SYSTEM_TYPE_DIALOG_SAVE_ARIA_LABEL,
  SYSTEM_TYPE_DIALOG_TITLES,
} from './system-type-dialog.i18n';

export type SystemTypeDialogMode = 'create' | 'view' | 'edit';

@Component({
  selector: 'app-system-type-dialog',
  standalone: true,
  imports: [
    CrudEntityDialog,
    FloatLabel,
    InputText,
    ProgressSpinner,
    ReactiveFormsModule,
  ],
  templateUrl: './system-type-dialog.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SystemTypeDialog {
  visible = model(false);
  form = input.required<SystemTypeFormGroup>();
  mode = input.required<SystemTypeDialogMode>();
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

  protected readonly labels = SYSTEM_TYPE_DIALOG_LABELS;
  protected readonly requiredError = SYSTEM_TYPE_DIALOG_REQUIRED_ERROR;
  protected readonly nameMaxLengthError = SYSTEM_TYPE_DIALOG_NAME_MAX_LENGTH_ERROR;
  protected readonly nameEsMaxLengthError = SYSTEM_TYPE_DIALOG_NAME_ES_MAX_LENGTH_ERROR;
  protected readonly loadingLabel = SYSTEM_TYPE_DIALOG_LOADING;
  protected readonly title = computed(() => SYSTEM_TYPE_DIALOG_TITLES[this.mode()]);
  protected readonly actionAriaLabels: CrudEntityDialogAriaLabels = {
    accept: SYSTEM_TYPE_DIALOG_ACCEPT_ARIA_LABEL,
    add: SYSTEM_TYPE_DIALOG_ADD_ARIA_LABEL,
    cancel: SYSTEM_TYPE_DIALOG_CANCEL_ARIA_LABEL,
    close: SYSTEM_TYPE_DIALOG_CLOSE_ARIA_LABEL,
    deactivate: SYSTEM_TYPE_DIALOG_DEACTIVATE_ARIA_LABEL,
    edit: SYSTEM_TYPE_DIALOG_EDIT_ARIA_LABEL,
    restore: SYSTEM_TYPE_DIALOG_RESTORE_ARIA_LABEL,
    save: SYSTEM_TYPE_DIALOG_SAVE_ARIA_LABEL,
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
