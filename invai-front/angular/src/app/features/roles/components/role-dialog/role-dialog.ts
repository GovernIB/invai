import { ChangeDetectionStrategy, Component, computed, input, model, output } from '@angular/core';
import { AbstractControl, ReactiveFormsModule } from '@angular/forms';
import {
  CrudEntityDialog,
  CrudEntityDialogAriaLabels,
} from '@components/crud-entity-dialog/crud-entity-dialog';
import { FloatLabel } from 'primeng/floatlabel';
import { InputText } from 'primeng/inputtext';
import { ProgressSpinner } from 'primeng/progressspinner';

import { RoleFormGroup } from '../../forms/role-form.factory';
import {
  ROLE_DIALOG_ACCEPT_ARIA_LABEL,
  ROLE_DIALOG_ADD_ARIA_LABEL,
  ROLE_DIALOG_CANCEL_ARIA_LABEL,
  ROLE_DIALOG_CLOSE_ARIA_LABEL,
  ROLE_DIALOG_DEACTIVATE_ARIA_LABEL,
  ROLE_DIALOG_EDIT_ARIA_LABEL,
  ROLE_DIALOG_LABELS,
  ROLE_DIALOG_LOADING,
  ROLE_DIALOG_NAME_ES_MAX_LENGTH_ERROR,
  ROLE_DIALOG_NAME_MAX_LENGTH_ERROR,
  ROLE_DIALOG_REQUIRED_ERROR,
  ROLE_DIALOG_RESTORE_ARIA_LABEL,
  ROLE_DIALOG_SAVE_ARIA_LABEL,
  ROLE_DIALOG_TITLES,
} from './role-dialog.i18n';

export type RoleDialogMode = 'create' | 'view' | 'edit';

@Component({
  selector: 'app-role-dialog',
  standalone: true,
  imports: [CrudEntityDialog, FloatLabel, InputText, ProgressSpinner, ReactiveFormsModule],
  templateUrl: './role-dialog.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RoleDialog {
  visible = model(false);
  form = input.required<RoleFormGroup>();
  mode = input.required<RoleDialogMode>();
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

  protected readonly labels = ROLE_DIALOG_LABELS;
  protected readonly requiredError = ROLE_DIALOG_REQUIRED_ERROR;
  protected readonly nameMaxLengthError = ROLE_DIALOG_NAME_MAX_LENGTH_ERROR;
  protected readonly nameEsMaxLengthError = ROLE_DIALOG_NAME_ES_MAX_LENGTH_ERROR;
  protected readonly loadingLabel = ROLE_DIALOG_LOADING;
  protected readonly title = computed(() => ROLE_DIALOG_TITLES[this.mode()]);
  protected readonly hasUnsavedChanges = () => this.mode() === 'edit' && this.form().dirty;
  protected readonly actionAriaLabels: CrudEntityDialogAriaLabels = {
    accept: ROLE_DIALOG_ACCEPT_ARIA_LABEL,
    add: ROLE_DIALOG_ADD_ARIA_LABEL,
    cancel: ROLE_DIALOG_CANCEL_ARIA_LABEL,
    close: ROLE_DIALOG_CLOSE_ARIA_LABEL,
    deactivate: ROLE_DIALOG_DEACTIVATE_ARIA_LABEL,
    edit: ROLE_DIALOG_EDIT_ARIA_LABEL,
    restore: ROLE_DIALOG_RESTORE_ARIA_LABEL,
    save: ROLE_DIALOG_SAVE_ARIA_LABEL,
  };

  protected isInvalid(control: AbstractControl): boolean {
    return control.invalid && (control.dirty || control.touched);
  }

  protected onSubmit(): void {
    if (!this.isLoading() && !this.isSaving() && this.mode() !== 'view') this.submitForm.emit();
  }
}
