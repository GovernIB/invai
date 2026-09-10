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
import { Select } from 'primeng/select';
import { Textarea } from 'primeng/textarea';

import { DatabaseHostFormGroup } from '../../forms/infrastructure-maintenance-forms.factory';
import {
  DatabaseVendorCatalogOption,
  InfrastructureDialogMode,
  ServerCatalogOption,
} from '../../systems.model';
import {
  DATABASE_MAINTENANCE_DIALOG_ACCEPT_ARIA_LABEL,
  DATABASE_MAINTENANCE_DIALOG_ADD_ARIA_LABEL,
  DATABASE_MAINTENANCE_DIALOG_CANCEL_ARIA_LABEL,
  DATABASE_MAINTENANCE_DIALOG_CLOSE_ARIA_LABEL,
  DATABASE_MAINTENANCE_DIALOG_DEACTIVATE_ARIA_LABEL,
  DATABASE_MAINTENANCE_DIALOG_EDIT_ARIA_LABEL,
  DATABASE_MAINTENANCE_DIALOG_LABELS,
  DATABASE_MAINTENANCE_DIALOG_PORT,
  DATABASE_MAINTENANCE_DIALOG_REQUIRED,
  DATABASE_MAINTENANCE_DIALOG_RESTORE_ARIA_LABEL,
  DATABASE_MAINTENANCE_DIALOG_SAVE_ARIA_LABEL,
  DATABASE_MAINTENANCE_DIALOG_TITLES,
} from './database-maintenance-dialog.i18n';

@Component({
  selector: 'app-database-maintenance-dialog',
  standalone: true,
  imports: [
    CrudEntityDialog,
    FloatLabel,
    InputNumber,
    InputText,
    ReactiveFormsModule,
    Select,
    Textarea,
  ],
  templateUrl: './database-maintenance-dialog.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DatabaseMaintenanceDialog {
  visible = model(false);
  mode = input.required<InfrastructureDialogMode>();
  form = input.required<DatabaseHostFormGroup>();
  serverOptions = input.required<ServerCatalogOption[]>();
  databaseTypeOptions = input.required<DatabaseVendorCatalogOption[]>();
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
    () => DATABASE_MAINTENANCE_DIALOG_TITLES[this.mode()],
  );
  protected readonly labels = DATABASE_MAINTENANCE_DIALOG_LABELS;
  protected readonly requiredError = DATABASE_MAINTENANCE_DIALOG_REQUIRED;
  protected readonly portError = DATABASE_MAINTENANCE_DIALOG_PORT;
  protected readonly actionAriaLabels: CrudEntityDialogAriaLabels = {
    accept: DATABASE_MAINTENANCE_DIALOG_ACCEPT_ARIA_LABEL,
    add: DATABASE_MAINTENANCE_DIALOG_ADD_ARIA_LABEL,
    cancel: DATABASE_MAINTENANCE_DIALOG_CANCEL_ARIA_LABEL,
    close: DATABASE_MAINTENANCE_DIALOG_CLOSE_ARIA_LABEL,
    deactivate: DATABASE_MAINTENANCE_DIALOG_DEACTIVATE_ARIA_LABEL,
    edit: DATABASE_MAINTENANCE_DIALOG_EDIT_ARIA_LABEL,
    restore: DATABASE_MAINTENANCE_DIALOG_RESTORE_ARIA_LABEL,
    save: DATABASE_MAINTENANCE_DIALOG_SAVE_ARIA_LABEL,
  };
  protected readonly hasUnsavedChanges = () =>
    this.mode() === 'edit' && this.form().dirty;

  protected isInvalid(control: AbstractControl): boolean {
    return control.invalid && (control.dirty || control.touched);
  }

  protected serverLabel(): string {
    return this.form().controls.server.value?.label ?? '-';
  }

  protected databaseTypeLabel(): string {
    return this.form().controls.databaseType.value?.name ?? '-';
  }

  protected onDatabaseTypeChange(
    databaseType: DatabaseVendorCatalogOption | null,
  ): void {
    const portControl = this.form().controls.port;
    portControl.setValue(databaseType?.defaultPort ?? null);
    portControl.markAsDirty();
  }

  protected onSubmit(): void {
    if (this.mode() !== 'view' && !this.isSaving()) this.submitForm.emit();
  }
}
