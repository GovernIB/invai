import {
  ChangeDetectionStrategy,
  Component,
  computed,
  input,
  model,
  output,
} from '@angular/core';
import { AbstractControl, ReactiveFormsModule } from '@angular/forms';
import {
  CrudEntityDialog,
  CrudEntityDialogMode,
} from '@components/crud-entity-dialog/crud-entity-dialog';
import { RoleCatalogOption } from '@features/roles/roles.model';
import { DatePicker } from 'primeng/datepicker';
import { FloatLabel } from 'primeng/floatlabel';
import { InputText } from 'primeng/inputtext';
import { Select } from 'primeng/select';
import { DatePickerPassThrough } from 'primeng/types/datepicker';

import {
  APPLICATION_DEVELOPMENT_DATE_RANGE_ERROR,
  ApplicationProviderFormGroup,
} from '../../forms/application-development-form.factory';
import {
  APPLICATION_PROVIDER_DIALOG_ARIA_LABELS,
  APPLICATION_PROVIDER_DIALOG_DATE_FORMAT,
  APPLICATION_PROVIDER_DIALOG_DATE_PLACEHOLDER,
  APPLICATION_PROVIDER_DIALOG_DATE_RANGE_ERROR,
  APPLICATION_PROVIDER_DIALOG_LABELS,
  APPLICATION_PROVIDER_DIALOG_MAX_LENGTH_ERROR,
  APPLICATION_PROVIDER_DIALOG_REQUIRED_ERROR,
  APPLICATION_PROVIDER_DIALOG_TITLES,
} from './application-provider-dialog.i18n';

@Component({
  selector: 'app-application-provider-dialog',
  standalone: true,
  imports: [
    CrudEntityDialog,
    DatePicker,
    FloatLabel,
    InputText,
    ReactiveFormsModule,
    Select,
  ],
  templateUrl: './application-provider-dialog.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationProviderDialog {
  visible = model(false);
  form = input.required<ApplicationProviderFormGroup>();
  mode = input.required<CrudEntityDialogMode>();
  roleOptions = input.required<RoleCatalogOption[]>();
  isSaving = input(false);
  isDeleting = input(false);
  canEdit = input(false);

  submitForm = output<void>();
  closed = output<void>();
  edit = output<void>();
  cancelEdit = output<void>();
  deactivate = output<void>();

  protected readonly title = computed(
    () => APPLICATION_PROVIDER_DIALOG_TITLES[this.mode()],
  );
  protected readonly labels = APPLICATION_PROVIDER_DIALOG_LABELS;
  protected readonly requiredError = APPLICATION_PROVIDER_DIALOG_REQUIRED_ERROR;
  protected readonly maxLengthError =
    APPLICATION_PROVIDER_DIALOG_MAX_LENGTH_ERROR;
  protected readonly dateRangeError =
    APPLICATION_PROVIDER_DIALOG_DATE_RANGE_ERROR;
  protected readonly dateFormat = APPLICATION_PROVIDER_DIALOG_DATE_FORMAT;
  protected readonly datePlaceholder =
    APPLICATION_PROVIDER_DIALOG_DATE_PLACEHOLDER;
  protected readonly actionAriaLabels =
    APPLICATION_PROVIDER_DIALOG_ARIA_LABELS;
  protected readonly hasUnsavedChanges = () =>
    this.mode() === 'edit' && this.form().dirty;

  protected isInvalid(control: AbstractControl): boolean {
    return control.invalid && (control.dirty || control.touched);
  }

  protected isDateRangeInvalid(): boolean {
    const form = this.form();
    return (
      form.hasError(APPLICATION_DEVELOPMENT_DATE_RANGE_ERROR) &&
      [form.controls.startDate, form.controls.expireDate].some(
        (control) => control.dirty || control.touched,
      )
    );
  }

  protected datePassThrough(): DatePickerPassThrough {
    const invalid = this.isDateRangeInvalid();
    return {
      pcInputText: {
        root: {
          'aria-describedby': invalid
            ? 'application-provider-dialog-date-range-error'
            : null,
          'aria-invalid': String(invalid),
        },
      },
    };
  }

  protected onSubmit(): void {
    if (!this.isSaving() && !this.isDeleting() && this.mode() !== 'view') {
      this.submitForm.emit();
    }
  }
}
