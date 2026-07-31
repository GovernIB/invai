import { ChangeDetectionStrategy, Component, computed, input, model, output } from '@angular/core';
import { AbstractControl, ReactiveFormsModule } from '@angular/forms';
import {
  CrudEntityDialog,
  CrudEntityDialogAriaLabels,
} from '@components/crud-entity-dialog/crud-entity-dialog';
import { FloatLabel } from 'primeng/floatlabel';
import { InputText } from 'primeng/inputtext';
import { ProgressSpinner } from 'primeng/progressspinner';

import { LayerFormGroup } from '../../forms/layer-form.factory';
import {
  LAYER_DIALOG_ACCEPT_ARIA_LABEL,
  LAYER_DIALOG_ADD_ARIA_LABEL,
  LAYER_DIALOG_CANCEL_ARIA_LABEL,
  LAYER_DIALOG_CLOSE_ARIA_LABEL,
  LAYER_DIALOG_DEACTIVATE_ARIA_LABEL,
  LAYER_DIALOG_EDIT_ARIA_LABEL,
  LAYER_DIALOG_LABEL,
  LAYER_DIALOG_LOADING,
  LAYER_DIALOG_NAME_MAX_LENGTH_ERROR,
  LAYER_DIALOG_REQUIRED_ERROR,
  LAYER_DIALOG_RESTORE_ARIA_LABEL,
  LAYER_DIALOG_SAVE_ARIA_LABEL,
  LAYER_DIALOG_TITLES,
} from './layer-dialog.i18n';

export type LayerDialogMode = 'create' | 'view' | 'edit';

@Component({
  selector: 'app-layer-dialog',
  standalone: true,
  imports: [CrudEntityDialog, FloatLabel, InputText, ProgressSpinner, ReactiveFormsModule],
  templateUrl: './layer-dialog.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LayerDialog {
  visible = model(false);
  form = input.required<LayerFormGroup>();
  mode = input.required<LayerDialogMode>();
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

  protected readonly label = LAYER_DIALOG_LABEL;
  protected readonly requiredError = LAYER_DIALOG_REQUIRED_ERROR;
  protected readonly nameMaxLengthError = LAYER_DIALOG_NAME_MAX_LENGTH_ERROR;
  protected readonly loadingLabel = LAYER_DIALOG_LOADING;
  protected readonly title = computed(() => LAYER_DIALOG_TITLES[this.mode()]);
  protected readonly hasUnsavedChanges = () => this.mode() === 'edit' && this.form().dirty;
  protected readonly actionAriaLabels: CrudEntityDialogAriaLabels = {
    accept: LAYER_DIALOG_ACCEPT_ARIA_LABEL,
    add: LAYER_DIALOG_ADD_ARIA_LABEL,
    cancel: LAYER_DIALOG_CANCEL_ARIA_LABEL,
    close: LAYER_DIALOG_CLOSE_ARIA_LABEL,
    deactivate: LAYER_DIALOG_DEACTIVATE_ARIA_LABEL,
    edit: LAYER_DIALOG_EDIT_ARIA_LABEL,
    restore: LAYER_DIALOG_RESTORE_ARIA_LABEL,
    save: LAYER_DIALOG_SAVE_ARIA_LABEL,
  };

  protected isInvalid(control: AbstractControl): boolean {
    return control.invalid && (control.dirty || control.touched);
  }

  protected onSubmit(): void {
    if (!this.isLoading() && !this.isSaving() && this.mode() !== 'view') this.submitForm.emit();
  }
}
