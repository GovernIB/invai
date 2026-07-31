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
  CrudEntityDialogAriaLabels,
} from '@components/crud-entity-dialog/crud-entity-dialog';
import { FloatLabel } from 'primeng/floatlabel';
import { InputNumber } from 'primeng/inputnumber';
import { InputText } from 'primeng/inputtext';

import { DatabaseVendorFormGroup } from '../../forms/database-vendor-form.factory';
import { InfrastructureDialogMode } from '../../systems.model';
import {
  DATABASE_VENDOR_DIALOG_ARIA_LABELS,
  DATABASE_VENDOR_DIALOG_LABELS,
  DATABASE_VENDOR_DIALOG_PORT,
  DATABASE_VENDOR_DIALOG_REQUIRED,
  DATABASE_VENDOR_DIALOG_TITLES,
} from './database-vendor-dialog.i18n';

@Component({
  selector: 'app-database-vendor-dialog',
  standalone: true,
  imports: [CrudEntityDialog, FloatLabel, InputNumber, InputText, ReactiveFormsModule],
  templateUrl: './database-vendor-dialog.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DatabaseVendorDialog {
  visible = model(false);
  mode = input.required<InfrastructureDialogMode>();
  form = input.required<DatabaseVendorFormGroup>();
  canRestore = input(false);
  isLoading = input(false);
  isSaving = input(false);

  submitForm = output<void>();
  closed = output<void>();
  restore = output<void>();
  edit = output<void>();
  cancelEdit = output<void>();
  deactivate = output<void>();

  protected readonly title = computed(
    () => DATABASE_VENDOR_DIALOG_TITLES[this.mode()],
  );
  protected readonly labels = DATABASE_VENDOR_DIALOG_LABELS;
  protected readonly requiredError = DATABASE_VENDOR_DIALOG_REQUIRED;
  protected readonly portError = DATABASE_VENDOR_DIALOG_PORT;
  protected readonly actionAriaLabels: CrudEntityDialogAriaLabels =
    DATABASE_VENDOR_DIALOG_ARIA_LABELS;
  protected readonly hasUnsavedChanges = () =>
    this.mode() === 'edit' && this.form().dirty;

  protected isInvalid(control: AbstractControl): boolean {
    return control.invalid && (control.dirty || control.touched);
  }

  protected onSubmit(): void {
    if (this.mode() !== 'view' && !this.isSaving()) this.submitForm.emit();
  }
}
