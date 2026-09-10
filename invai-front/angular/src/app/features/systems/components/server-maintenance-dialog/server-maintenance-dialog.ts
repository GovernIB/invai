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

import { SystemHostFormGroup } from '../../forms/infrastructure-maintenance-forms.factory';
import {
  InfrastructureDialogMode,
  ServerCatalogOption,
} from '../../systems.model';
import {
  SERVER_MAINTENANCE_DIALOG_ACCEPT_ARIA_LABEL,
  SERVER_MAINTENANCE_DIALOG_ADD_ARIA_LABEL,
  SERVER_MAINTENANCE_DIALOG_CANCEL_ARIA_LABEL,
  SERVER_MAINTENANCE_DIALOG_CLOSE_ARIA_LABEL,
  SERVER_MAINTENANCE_DIALOG_DEACTIVATE_ARIA_LABEL,
  SERVER_MAINTENANCE_DIALOG_EDIT_ARIA_LABEL,
  SERVER_MAINTENANCE_DIALOG_LABELS,
  SERVER_MAINTENANCE_DIALOG_PORT,
  SERVER_MAINTENANCE_DIALOG_REQUIRED,
  SERVER_MAINTENANCE_DIALOG_RESTORE_ARIA_LABEL,
  SERVER_MAINTENANCE_DIALOG_SAVE_ARIA_LABEL,
  SERVER_MAINTENANCE_DIALOG_TITLES,
} from './server-maintenance-dialog.i18n';

@Component({
  selector: 'app-server-maintenance-dialog',
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
  templateUrl: './server-maintenance-dialog.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ServerMaintenanceDialog {
  visible = model(false);
  mode = input.required<InfrastructureDialogMode>();
  form = input.required<SystemHostFormGroup>();
  serverOptions = input.required<ServerCatalogOption[]>();
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
    () => SERVER_MAINTENANCE_DIALOG_TITLES[this.mode()],
  );
  protected readonly labels = SERVER_MAINTENANCE_DIALOG_LABELS;
  protected readonly requiredError = SERVER_MAINTENANCE_DIALOG_REQUIRED;
  protected readonly portError = SERVER_MAINTENANCE_DIALOG_PORT;
  protected readonly actionAriaLabels: CrudEntityDialogAriaLabels = {
    accept: SERVER_MAINTENANCE_DIALOG_ACCEPT_ARIA_LABEL,
    add: SERVER_MAINTENANCE_DIALOG_ADD_ARIA_LABEL,
    cancel: SERVER_MAINTENANCE_DIALOG_CANCEL_ARIA_LABEL,
    close: SERVER_MAINTENANCE_DIALOG_CLOSE_ARIA_LABEL,
    deactivate: SERVER_MAINTENANCE_DIALOG_DEACTIVATE_ARIA_LABEL,
    edit: SERVER_MAINTENANCE_DIALOG_EDIT_ARIA_LABEL,
    restore: SERVER_MAINTENANCE_DIALOG_RESTORE_ARIA_LABEL,
    save: SERVER_MAINTENANCE_DIALOG_SAVE_ARIA_LABEL,
  };
  protected readonly hasUnsavedChanges = () =>
    this.mode() === 'edit' && this.form().dirty;

  protected isInvalid(control: AbstractControl): boolean {
    return control.invalid && (control.dirty || control.touched);
  }

  protected serverLabel(): string {
    return this.form().controls.server.value?.label ?? '-';
  }

  protected onSubmit(): void {
    if (this.mode() !== 'view' && !this.isSaving()) this.submitForm.emit();
  }
}
