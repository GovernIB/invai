import { ChangeDetectionStrategy, Component, computed, input, model, output } from '@angular/core';
import { AbstractControl, ReactiveFormsModule } from '@angular/forms';
import {
  CrudEntityDialog,
  CrudEntityDialogMode,
} from '@components/crud-entity-dialog/crud-entity-dialog';
import { FloatLabel } from 'primeng/floatlabel';
import { InputText } from 'primeng/inputtext';
import { Select } from 'primeng/select';
import { SelectPassThrough } from 'primeng/types/select';

import { ResponsiblePersonFormGroup } from '../../forms/responsible-forms.factory';
import { RESPONSIBLE_COMMON_COPY, RESPONSIBLE_PERSON_COPY } from '../../responsibles.i18n';
import { ResponsibleCompanyOption } from '../../responsibles.model';

@Component({
  selector: 'app-responsible-person-dialog',
  standalone: true,
  imports: [CrudEntityDialog, FloatLabel, InputText, ReactiveFormsModule, Select],
  templateUrl: './responsible-person-dialog.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ResponsiblePersonDialog {
  visible = model(false);
  form = input.required<ResponsiblePersonFormGroup>();
  mode = input.required<CrudEntityDialogMode>();
  companyOptions = input.required<ResponsibleCompanyOption[]>();
  companyName = input('');
  isSaving = input(false);
  isDeleting = input(false);
  canRestore = input(false);
  canMutate = input(true);
  submitForm = output<void>();
  closed = output<void>();
  restore = output<void>();
  edit = output<void>();
  cancelEdit = output<void>();
  deactivate = output<void>();

  protected readonly copy = RESPONSIBLE_PERSON_COPY;
  protected readonly requiredError = RESPONSIBLE_COMMON_COPY.required;
  protected readonly nameMaxLengthError = RESPONSIBLE_COMMON_COPY.nameMaxLength;
  protected readonly title = computed(() => this.copy.dialogTitles[this.mode()]);
  protected readonly companyLabelId = 'responsible-person-dialog-company-label';
  protected readonly hasUnsavedChanges = () => this.mode() === 'edit' && this.form().dirty;
  protected readonly companySelectPassThrough = computed<SelectPassThrough>(() => ({
    label: {
      'aria-describedby': this.isInvalid(this.form().controls.companyId)
        ? 'responsible-person-dialog-company-error'
        : undefined,
      'aria-invalid': this.isInvalid(this.form().controls.companyId) || undefined,
    },
  }));

  protected isInvalid(control: AbstractControl): boolean {
    return control.invalid && (control.dirty || control.touched);
  }

  protected onSubmit(): void {
    if (!this.isSaving() && this.mode() !== 'view') this.submitForm.emit();
  }
}
