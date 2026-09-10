import { ChangeDetectionStrategy, Component, computed, input, model, output } from '@angular/core';
import { AbstractControl, ReactiveFormsModule } from '@angular/forms';
import {
  CrudEntityDialog,
  CrudEntityDialogAriaLabels,
} from '@components/crud-entity-dialog/crud-entity-dialog';
import { DatePicker } from 'primeng/datepicker';
import { FloatLabel } from 'primeng/floatlabel';
import { InputText } from 'primeng/inputtext';
import { ProgressSpinner } from 'primeng/progressspinner';
import { Select } from 'primeng/select';
import { DatePickerPassThrough } from 'primeng/types/datepicker';

import {
  COMMISSION_DATE_FORMAT,
  COMMISSION_DATE_PLACEHOLDER,
  COMMISSION_TYPE_OPTIONS,
} from '../../commissions.constants';
import { CommissionFormGroup } from '../../forms/commission-form.factory';
import {
  COMMISSION_DIALOG_ACCEPT_ARIA_LABEL,
  COMMISSION_DIALOG_ADD_ARIA_LABEL,
  COMMISSION_DIALOG_CANCEL_ARIA_LABEL,
  COMMISSION_DIALOG_CLOSE_ARIA_LABEL,
  COMMISSION_DIALOG_DEACTIVATE_ARIA_LABEL,
  COMMISSION_DIALOG_EDIT_ARIA_LABEL,
  COMMISSION_DIALOG_EXPEDIENT_MAX_LENGTH_ERROR,
  COMMISSION_DIALOG_LABELS,
  COMMISSION_DIALOG_LOADING,
  COMMISSION_DIALOG_NAME_ES_MAX_LENGTH_ERROR,
  COMMISSION_DIALOG_NAME_MAX_LENGTH_ERROR,
  COMMISSION_DIALOG_REQUIRED_ERROR,
  COMMISSION_DIALOG_RESTORE_ARIA_LABEL,
  COMMISSION_DIALOG_SAVE_ARIA_LABEL,
  COMMISSION_DIALOG_TITLES,
} from './commission-dialog.i18n';

export type CommissionDialogMode = 'create' | 'view' | 'edit';

@Component({
  selector: 'app-commission-dialog',
  standalone: true,
  imports: [
    CrudEntityDialog,
    DatePicker,
    FloatLabel,
    InputText,
    ProgressSpinner,
    ReactiveFormsModule,
    Select,
  ],
  templateUrl: './commission-dialog.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CommissionDialog {
  visible = model(false);
  form = input.required<CommissionFormGroup>();
  mode = input.required<CommissionDialogMode>();
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

  protected readonly labels = COMMISSION_DIALOG_LABELS;
  protected readonly dateFormat = COMMISSION_DATE_FORMAT;
  protected readonly datePlaceholder = COMMISSION_DATE_PLACEHOLDER;
  protected readonly commissionTypeOptions = COMMISSION_TYPE_OPTIONS;
  protected readonly requiredError = COMMISSION_DIALOG_REQUIRED_ERROR;
  protected readonly nameMaxLengthError = COMMISSION_DIALOG_NAME_MAX_LENGTH_ERROR;
  protected readonly nameEsMaxLengthError = COMMISSION_DIALOG_NAME_ES_MAX_LENGTH_ERROR;
  protected readonly expedientMaxLengthError = COMMISSION_DIALOG_EXPEDIENT_MAX_LENGTH_ERROR;
  protected readonly loadingLabel = COMMISSION_DIALOG_LOADING;
  protected readonly title = computed(() => COMMISSION_DIALOG_TITLES[this.mode()]);
  protected readonly actionAriaLabels: CrudEntityDialogAriaLabels = {
    accept: COMMISSION_DIALOG_ACCEPT_ARIA_LABEL,
    add: COMMISSION_DIALOG_ADD_ARIA_LABEL,
    cancel: COMMISSION_DIALOG_CANCEL_ARIA_LABEL,
    close: COMMISSION_DIALOG_CLOSE_ARIA_LABEL,
    deactivate: COMMISSION_DIALOG_DEACTIVATE_ARIA_LABEL,
    edit: COMMISSION_DIALOG_EDIT_ARIA_LABEL,
    restore: COMMISSION_DIALOG_RESTORE_ARIA_LABEL,
    save: COMMISSION_DIALOG_SAVE_ARIA_LABEL,
  };
  protected readonly hasUnsavedChanges = () =>
    this.mode() === 'edit' && this.form().dirty;

  protected isInvalid(control: AbstractControl): boolean {
    return control.invalid && (control.dirty || control.touched);
  }

  protected approvalDatePassThrough(): DatePickerPassThrough {
    const invalid = this.isInvalid(this.form().controls.approvalDate);

    return {
      pcInputText: {
        root: {
          'aria-describedby': invalid ? 'commission-dialog-approval-date-error' : null,
          'aria-invalid': String(invalid),
        },
      },
    };
  }

  protected approvalDateLabel(): string {
    const value = this.form().controls.approvalDate.value;
    return value ? new Intl.DateTimeFormat().format(value) : '-';
  }

  protected commissionTypeLabel(): string {
    const value = this.form().controls.commissionType.value;
    return this.commissionTypeOptions.find((option) => option.value === value)?.label ?? '-';
  }

  protected onSubmit(): void {
    if (this.isLoading() || this.isSaving() || this.mode() === 'view') return;
    this.submitForm.emit();
  }

}
