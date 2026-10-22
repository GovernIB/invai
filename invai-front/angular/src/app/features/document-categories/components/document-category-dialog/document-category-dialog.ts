import { ChangeDetectionStrategy, Component, computed, input, model, output } from '@angular/core';
import { AbstractControl, ReactiveFormsModule } from '@angular/forms';
import { CrudEntityDialog } from '@components/crud-entity-dialog/crud-entity-dialog';
import { FloatLabel } from 'primeng/floatlabel';
import { InputText } from 'primeng/inputtext';
import { ProgressSpinner } from 'primeng/progressspinner';
import { Select } from 'primeng/select';

import { DOCUMENT_CATEGORY_NAME_MAX_LENGTH } from '../../document-categories.constants';
import { DocumentCategoryOption } from '../../document-categories.model';
import { DocumentCategoryFormGroup } from '../../forms/document-category-form.factory';
import {
  DOCUMENT_CATEGORY_DIALOG_ARIA_LABELS,
  DOCUMENT_CATEGORY_DIALOG_ERRORS,
  DOCUMENT_CATEGORY_DIALOG_LABELS,
  DOCUMENT_CATEGORY_DIALOG_LOADING,
  DOCUMENT_CATEGORY_DIALOG_TITLES,
} from './document-category-dialog.i18n';

export type DocumentCategoryDialogMode = 'create' | 'view' | 'edit';
export type DocumentCategoryKind = 'category' | 'subcategory';

// A subcategory also shows its category: fixed while it is created and while it is consulted, and
// selectable among the active categories while it is edited, so that it can be moved.
@Component({
  selector: 'app-document-category-dialog',
  standalone: true,
  imports: [CrudEntityDialog, FloatLabel, InputText, ProgressSpinner, ReactiveFormsModule, Select],
  templateUrl: './document-category-dialog.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DocumentCategoryDialog {
  visible = model(false);
  form = input.required<DocumentCategoryFormGroup>();
  mode = input.required<DocumentCategoryDialogMode>();
  kind = input.required<DocumentCategoryKind>();
  // Name of the category of a subcategory, shown while it is not selectable.
  categoryLabel = input('');
  categoryOptions = input<DocumentCategoryOption[]>([]);
  isLoading = input(false);
  isSaving = input(false);
  isDeleting = input(false);
  canRestore = input(false);
  canEdit = input(true);
  submitForm = output<void>();
  closed = output<void>();
  restore = output<void>();
  edit = output<void>();
  cancelEdit = output<void>();
  deactivate = output<void>();

  protected readonly labels = DOCUMENT_CATEGORY_DIALOG_LABELS;
  protected readonly errors = DOCUMENT_CATEGORY_DIALOG_ERRORS;
  protected readonly loadingLabel = DOCUMENT_CATEGORY_DIALOG_LOADING;
  protected readonly nameMaxLength = DOCUMENT_CATEGORY_NAME_MAX_LENGTH;
  protected readonly title = computed(() => DOCUMENT_CATEGORY_DIALOG_TITLES[this.kind()][this.mode()]);
  protected readonly actionAriaLabels = computed(() => DOCUMENT_CATEGORY_DIALOG_ARIA_LABELS[this.kind()]);
  protected readonly isCategorySelectable = computed(
    () => this.kind() === 'subcategory' && this.mode() === 'edit',
  );
  protected readonly hasUnsavedChanges = () => this.mode() === 'edit' && this.form().dirty;

  protected isInvalid(control: AbstractControl): boolean {
    return control.invalid && (control.dirty || control.touched);
  }

  protected nameError(control: AbstractControl, maxLengthError: string): string {
    return control.hasError('maxlength') ? maxLengthError : this.errors.required;
  }

  protected onSubmit(): void {
    if (!this.isLoading() && !this.isSaving() && this.mode() !== 'view') this.submitForm.emit();
  }
}
