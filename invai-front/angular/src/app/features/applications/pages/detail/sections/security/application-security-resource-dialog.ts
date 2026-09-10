import { ChangeDetectionStrategy, Component, computed, input, model, output } from '@angular/core';
import { AbstractControl, ReactiveFormsModule } from '@angular/forms';
import {
  CrudEntityDialog,
  CrudEntityDialogMode,
} from '@components/crud-entity-dialog/crud-entity-dialog';
import { SelectOption } from '../../../../applications.model';
import {
  ApplicationSecurityResourceFormGroup,
  ApplicationSecurityResourceKind,
} from '../../../../forms/application-security-form.factory';
import { Select } from 'primeng/select';
import { Textarea } from 'primeng/textarea';
import {
  APPLICATION_SECURITY_DIALOG_ARIA_LABELS,
  APPLICATION_SECURITY_DIALOG_TITLES,
  APPLICATION_SECURITY_EMPTY_VALUE,
  APPLICATION_SECURITY_LABELS,
  APPLICATION_SECURITY_REQUIRED_ERROR,
} from './application-security-section.i18n';

@Component({
  selector: 'app-application-security-resource-dialog',
  standalone: true,
  imports: [CrudEntityDialog, ReactiveFormsModule, Select, Textarea],
  templateUrl: './application-security-resource-dialog.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationSecurityResourceDialog {
  visible = model(false);
  kind = input.required<ApplicationSecurityResourceKind>();
  form = input.required<ApplicationSecurityResourceFormGroup>();
  mode = input.required<CrudEntityDialogMode>();
  webContextOptions = input<SelectOption<number>[]>([]);
  fieldOptions = input<SelectOption<number>[]>([]);
  securityLevelOptions = input<SelectOption<number>[]>([]);
  measureTypeOptions = input<SelectOption<number>[]>([]);
  ensRequirementOptions = input<SelectOption<number>[]>([]);
  isSaving = input(false);
  isDeleting = input(false);
  canEdit = input(false);

  submitForm = output<void>();
  closed = output<void>();
  edit = output<void>();
  cancelEdit = output<void>();
  deactivate = output<void>();

  protected readonly labels = APPLICATION_SECURITY_LABELS;
  protected readonly requiredError = APPLICATION_SECURITY_REQUIRED_ERROR;
  protected readonly emptyValue = APPLICATION_SECURITY_EMPTY_VALUE;
  protected readonly actionAriaLabels = APPLICATION_SECURITY_DIALOG_ARIA_LABELS;
  protected readonly title = computed(
    () => APPLICATION_SECURITY_DIALOG_TITLES[this.kind()][this.mode()],
  );
  protected readonly hasUnsavedChanges = () => this.mode() === 'edit' && this.form().dirty;

  protected isInvalid(control: AbstractControl): boolean {
    return control.invalid && (control.dirty || control.touched);
  }

  protected optionLabel(options: SelectOption<number>[], value: number | null): string {
    return options.find((option) => option.value === value)?.label ?? this.emptyValue;
  }

  protected onSubmit(): void {
    if (!this.isSaving() && !this.isDeleting() && this.mode() !== 'view') {
      this.submitForm.emit();
    }
  }
}
