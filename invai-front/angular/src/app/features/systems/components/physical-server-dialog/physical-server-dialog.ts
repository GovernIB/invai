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
import { EnvironmentCatalogOption } from '@features/environments/environments.model';
import { FloatLabel } from 'primeng/floatlabel';
import { InputText } from 'primeng/inputtext';
import { Select } from 'primeng/select';

import { PhysicalServerFormGroup } from '../../forms/physical-server-form.factory';
import {
  InfrastructureDialogMode,
  ServerTypeCode,
} from '../../systems.model';
import {
  PHYSICAL_SERVER_DIALOG_ARIA_LABELS,
  PHYSICAL_SERVER_DIALOG_LABELS,
  PHYSICAL_SERVER_DIALOG_REQUIRED,
  PHYSICAL_SERVER_DIALOG_TITLES,
} from './physical-server-dialog.i18n';

@Component({
  selector: 'app-physical-server-dialog',
  standalone: true,
  imports: [
    CrudEntityDialog,
    FloatLabel,
    InputText,
    ReactiveFormsModule,
    Select,
  ],
  templateUrl: './physical-server-dialog.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PhysicalServerDialog {
  visible = model(false);
  mode = input.required<InfrastructureDialogMode>();
  serverTypeCode = input.required<ServerTypeCode>();
  form = input.required<PhysicalServerFormGroup>();
  environmentOptions = input.required<EnvironmentCatalogOption[]>();
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
    () => PHYSICAL_SERVER_DIALOG_TITLES[this.serverTypeCode()][this.mode()],
  );
  protected readonly labels = PHYSICAL_SERVER_DIALOG_LABELS;
  protected readonly requiredError = PHYSICAL_SERVER_DIALOG_REQUIRED;
  protected readonly actionAriaLabels: CrudEntityDialogAriaLabels =
    PHYSICAL_SERVER_DIALOG_ARIA_LABELS;
  protected readonly hasUnsavedChanges = () =>
    this.mode() === 'edit' && this.form().dirty;

  protected isInvalid(control: AbstractControl): boolean {
    return control.invalid && (control.dirty || control.touched);
  }

  protected environmentLabel(): string {
    return this.form().controls.environment.value?.label ?? '-';
  }

  protected onSubmit(): void {
    if (this.mode() !== 'view' && !this.isSaving()) this.submitForm.emit();
  }
}
