import { ChangeDetectionStrategy, Component, computed, input, model, output } from '@angular/core';
import { AbstractControl, ReactiveFormsModule } from '@angular/forms';
import {
  CrudEntityDialog,
  CrudEntityDialogAriaLabels,
} from '@components/crud-entity-dialog/crud-entity-dialog';
import { FloatLabel } from 'primeng/floatlabel';
import { InputText } from 'primeng/inputtext';
import { ProgressSpinner } from 'primeng/progressspinner';

import { CategoryFormGroup } from '../../forms/category-form.factory';
import {
  CATEGORY_DIALOG_ACCEPT_ARIA_LABEL,
  CATEGORY_DIALOG_ADD_ARIA_LABEL,
  CATEGORY_DIALOG_CANCEL_ARIA_LABEL,
  CATEGORY_DIALOG_CLOSE_ARIA_LABEL,
  CATEGORY_DIALOG_DEACTIVATE_ARIA_LABEL,
  CATEGORY_DIALOG_EDIT_ARIA_LABEL,
  CATEGORY_DIALOG_LABELS,
  CATEGORY_DIALOG_LOADING,
  CATEGORY_DIALOG_NAME_ES_MAX_LENGTH_ERROR,
  CATEGORY_DIALOG_NAME_MAX_LENGTH_ERROR,
  CATEGORY_DIALOG_REQUIRED_ERROR,
  CATEGORY_DIALOG_RESTORE_ARIA_LABEL,
  CATEGORY_DIALOG_SAVE_ARIA_LABEL,
  CATEGORY_DIALOG_TITLES,
} from './category-dialog.i18n';

export type CategoryDialogMode = 'create' | 'view' | 'edit';

@Component({
  selector: 'app-category-dialog',
  standalone: true,
  imports: [
    CrudEntityDialog,
    FloatLabel,
    InputText,
    ProgressSpinner,
    ReactiveFormsModule,
  ],
  templateUrl: './category-dialog.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CategoryDialog {
  visible = model(false);
  form = input.required<CategoryFormGroup>();
  mode = input.required<CategoryDialogMode>();
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

  protected readonly labels = CATEGORY_DIALOG_LABELS;
  protected readonly requiredError = CATEGORY_DIALOG_REQUIRED_ERROR;
  protected readonly nameMaxLengthError = CATEGORY_DIALOG_NAME_MAX_LENGTH_ERROR;
  protected readonly nameEsMaxLengthError = CATEGORY_DIALOG_NAME_ES_MAX_LENGTH_ERROR;
  protected readonly loadingLabel = CATEGORY_DIALOG_LOADING;
  protected readonly title = computed(() => CATEGORY_DIALOG_TITLES[this.mode()]);
  protected readonly actionAriaLabels: CrudEntityDialogAriaLabels = {
    accept: CATEGORY_DIALOG_ACCEPT_ARIA_LABEL,
    add: CATEGORY_DIALOG_ADD_ARIA_LABEL,
    cancel: CATEGORY_DIALOG_CANCEL_ARIA_LABEL,
    close: CATEGORY_DIALOG_CLOSE_ARIA_LABEL,
    deactivate: CATEGORY_DIALOG_DEACTIVATE_ARIA_LABEL,
    edit: CATEGORY_DIALOG_EDIT_ARIA_LABEL,
    restore: CATEGORY_DIALOG_RESTORE_ARIA_LABEL,
    save: CATEGORY_DIALOG_SAVE_ARIA_LABEL,
  };
  protected readonly hasUnsavedChanges = () =>
    this.mode() === 'edit' && this.form().dirty;

  protected isInvalid(control: AbstractControl): boolean {
    return control.invalid && (control.dirty || control.touched);
  }

  protected onSubmit(): void {
    if (this.isLoading() || this.isSaving() || this.mode() === 'view') return;
    this.submitForm.emit();
  }

}
