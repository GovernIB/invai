import { ChangeDetectionStrategy, Component, computed, input, model, output } from '@angular/core';
import { AbstractControl, ReactiveFormsModule } from '@angular/forms';
import {
  CrudEntityDialog,
  CrudEntityDialogAriaLabels,
  CrudEntityDialogMode,
} from '@components/crud-entity-dialog/crud-entity-dialog';
import { FloatLabel } from 'primeng/floatlabel';
import { InputText } from 'primeng/inputtext';

import { ResponsibleNameFormGroup } from '../../forms/responsible-forms.factory';
import { RESPONSIBLE_COMMON_COPY } from '../../responsibles.i18n';

export interface ResponsibleNameDialogCopy {
  name: string;
  dialogTitles: Record<CrudEntityDialogMode, string>;
  actions: CrudEntityDialogAriaLabels;
}

@Component({
  selector: 'app-responsible-name-dialog',
  standalone: true,
  imports: [CrudEntityDialog, FloatLabel, InputText, ReactiveFormsModule],
  templateUrl: './responsible-name-dialog.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ResponsibleNameDialog {
  visible = model(false);
  form = input.required<ResponsibleNameFormGroup>();
  mode = input.required<CrudEntityDialogMode>();
  copy = input.required<ResponsibleNameDialogCopy>();
  idPrefix = input.required<string>();
  isSaving = input(false);
  isDeleting = input(false);
  canRestore = input(false);
  submitForm = output<void>();
  closed = output<void>();
  restore = output<void>();
  edit = output<void>();
  cancelEdit = output<void>();
  deactivate = output<void>();

  protected readonly requiredError = RESPONSIBLE_COMMON_COPY.required;
  protected readonly nameMaxLengthError = RESPONSIBLE_COMMON_COPY.nameMaxLength;
  protected readonly title = computed(() => this.copy().dialogTitles[this.mode()]);
  protected readonly hasUnsavedChanges = () => this.mode() === 'edit' && this.form().dirty;

  protected isInvalid(control: AbstractControl): boolean {
    return control.invalid && (control.dirty || control.touched);
  }

  protected onSubmit(): void {
    if (!this.isSaving() && this.mode() !== 'view') this.submitForm.emit();
  }
}
