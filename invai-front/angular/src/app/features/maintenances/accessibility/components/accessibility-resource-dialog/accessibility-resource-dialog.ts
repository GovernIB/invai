import { ChangeDetectionStrategy, Component, computed, input, model, output } from '@angular/core';
import { AbstractControl, ReactiveFormsModule } from '@angular/forms';
import {
  CrudEntityDialog,
  CrudEntityDialogMode,
} from '@components/crud-entity-dialog/crud-entity-dialog';
import { FloatLabel } from 'primeng/floatlabel';
import { InputText } from 'primeng/inputtext';
import { ProgressSpinner } from 'primeng/progressspinner';

import { AccessibilityResourceFormGroup } from '../../forms/accessibility-resource-forms.factory';
import {
  ACCESSIBILITY_DIALOG_ACTIONS,
  ACCESSIBILITY_DIALOG_LOADING,
  ACCESSIBILITY_DIALOG_TITLES,
  ACCESSIBILITY_FILTER_LABELS,
  ACCESSIBILITY_NAME_ES_MAX_LENGTH_ERROR,
  ACCESSIBILITY_NAME_MAX_LENGTH_ERROR,
  ACCESSIBILITY_REQUIRED_ERROR,
} from '../../accessibility.i18n';

@Component({
  selector: 'app-accessibility-resource-dialog',
  standalone: true,
  imports: [CrudEntityDialog, FloatLabel, InputText, ProgressSpinner, ReactiveFormsModule],
  templateUrl: './accessibility-resource-dialog.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AccessibilityResourceDialog {
  visible = model(false);
  form = input.required<AccessibilityResourceFormGroup>();
  mode = input.required<CrudEntityDialogMode>();
  entityLabel = input.required<string>();
  bilingual = input.required<boolean>();
  idPrefix = input.required<string>();
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

  protected readonly labels = ACCESSIBILITY_FILTER_LABELS;
  protected readonly requiredError = ACCESSIBILITY_REQUIRED_ERROR;
  protected readonly nameMaxLengthError = ACCESSIBILITY_NAME_MAX_LENGTH_ERROR;
  protected readonly nameEsMaxLengthError = ACCESSIBILITY_NAME_ES_MAX_LENGTH_ERROR;
  protected readonly loadingLabel = ACCESSIBILITY_DIALOG_LOADING;
  protected readonly title = computed(
    () => ACCESSIBILITY_DIALOG_TITLES(this.entityLabel())[this.mode()],
  );
  protected readonly actionAriaLabels = computed(() =>
    ACCESSIBILITY_DIALOG_ACTIONS(this.entityLabel()),
  );
  protected readonly hasUnsavedChanges = () => this.mode() === 'edit' && this.form().dirty;

  protected isInvalid(control: AbstractControl): boolean {
    return control.invalid && (control.dirty || control.touched);
  }

  protected onSubmit(): void {
    if (!this.isLoading() && !this.isSaving() && this.mode() !== 'view') this.submitForm.emit();
  }
}
