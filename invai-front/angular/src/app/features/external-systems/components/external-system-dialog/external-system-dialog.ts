import { ChangeDetectionStrategy, Component, computed, input, model, output } from '@angular/core';
import { AbstractControl, ReactiveFormsModule } from '@angular/forms';
import {
  CrudEntityDialog,
  CrudEntityDialogAriaLabels,
} from '@components/crud-entity-dialog/crud-entity-dialog';
import { ResponsibleCompanyOption } from '@features/maintenances/responsibles/responsibles.model';
import { FloatLabel } from 'primeng/floatlabel';
import { InputText } from 'primeng/inputtext';
import { ProgressSpinner } from 'primeng/progressspinner';
import { Select } from 'primeng/select';

import { ExternalSystemFormGroup } from '../../forms/external-system-form.factory';
import {
  EXTERNAL_SYSTEM_DIALOG_ACCEPT_ARIA_LABEL,
  EXTERNAL_SYSTEM_DIALOG_ADD_ARIA_LABEL,
  EXTERNAL_SYSTEM_DIALOG_CANCEL_ARIA_LABEL,
  EXTERNAL_SYSTEM_DIALOG_CLOSE_ARIA_LABEL,
  EXTERNAL_SYSTEM_DIALOG_DEACTIVATE_ARIA_LABEL,
  EXTERNAL_SYSTEM_DIALOG_EDIT_ARIA_LABEL,
  EXTERNAL_SYSTEM_DIALOG_LABELS,
  EXTERNAL_SYSTEM_DIALOG_LOADING,
  EXTERNAL_SYSTEM_DIALOG_NAME_MAX_LENGTH_ERROR,
  EXTERNAL_SYSTEM_DIALOG_REQUIRED_ERROR,
  EXTERNAL_SYSTEM_DIALOG_RESTORE_ARIA_LABEL,
  EXTERNAL_SYSTEM_DIALOG_SAVE_ARIA_LABEL,
  EXTERNAL_SYSTEM_DIALOG_TITLES,
} from './external-system-dialog.i18n';

export type ExternalSystemDialogMode = 'create' | 'view' | 'edit';

@Component({
  selector: 'app-external-system-dialog',
  standalone: true,
  imports: [
    CrudEntityDialog,
    FloatLabel,
    InputText,
    ProgressSpinner,
    ReactiveFormsModule,
    Select,
  ],
  templateUrl: './external-system-dialog.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ExternalSystemDialog {
  visible = model(false);
  form = input.required<ExternalSystemFormGroup>();
  mode = input.required<ExternalSystemDialogMode>();
  companyOptions = input.required<ResponsibleCompanyOption[]>();
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

  protected readonly labels = EXTERNAL_SYSTEM_DIALOG_LABELS;
  protected readonly requiredError = EXTERNAL_SYSTEM_DIALOG_REQUIRED_ERROR;
  protected readonly nameMaxLengthError = EXTERNAL_SYSTEM_DIALOG_NAME_MAX_LENGTH_ERROR;
  protected readonly loadingLabel = EXTERNAL_SYSTEM_DIALOG_LOADING;
  protected readonly title = computed(() => EXTERNAL_SYSTEM_DIALOG_TITLES[this.mode()]);
  protected readonly hasUnsavedChanges = () => this.mode() === 'edit' && this.form().dirty;
  protected readonly actionAriaLabels: CrudEntityDialogAriaLabels = {
    accept: EXTERNAL_SYSTEM_DIALOG_ACCEPT_ARIA_LABEL,
    add: EXTERNAL_SYSTEM_DIALOG_ADD_ARIA_LABEL,
    cancel: EXTERNAL_SYSTEM_DIALOG_CANCEL_ARIA_LABEL,
    close: EXTERNAL_SYSTEM_DIALOG_CLOSE_ARIA_LABEL,
    deactivate: EXTERNAL_SYSTEM_DIALOG_DEACTIVATE_ARIA_LABEL,
    edit: EXTERNAL_SYSTEM_DIALOG_EDIT_ARIA_LABEL,
    restore: EXTERNAL_SYSTEM_DIALOG_RESTORE_ARIA_LABEL,
    save: EXTERNAL_SYSTEM_DIALOG_SAVE_ARIA_LABEL,
  };

  protected isInvalid(control: AbstractControl): boolean {
    return control.invalid && (control.dirty || control.touched);
  }

  protected companyLabel(): string {
    const value = this.form().controls.companyId.value;
    return this.companyOptions().find((option) => option.id === value)?.label ?? '-';
  }

  protected onSubmit(): void {
    if (!this.isLoading() && !this.isSaving() && this.mode() !== 'view') this.submitForm.emit();
  }
}
