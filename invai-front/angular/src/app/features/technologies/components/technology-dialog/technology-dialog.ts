import { ChangeDetectionStrategy, Component, computed, input, model, output } from '@angular/core';
import { AbstractControl, ReactiveFormsModule } from '@angular/forms';
import {
  CrudEntityDialog,
  CrudEntityDialogAriaLabels,
} from '@components/crud-entity-dialog/crud-entity-dialog';
import { LayerOption } from '@features/layers/layers.model';
import { FloatLabel } from 'primeng/floatlabel';
import { InputText } from 'primeng/inputtext';
import { ProgressSpinner } from 'primeng/progressspinner';
import { Select } from 'primeng/select';

import { TechnologyFormGroup } from '../../forms/technology-form.factory';
import {
  TECHNOLOGY_DIALOG_ACCEPT_ARIA_LABEL,
  TECHNOLOGY_DIALOG_ADD_ARIA_LABEL,
  TECHNOLOGY_DIALOG_CANCEL_ARIA_LABEL,
  TECHNOLOGY_DIALOG_CLOSE_ARIA_LABEL,
  TECHNOLOGY_DIALOG_DEACTIVATE_ARIA_LABEL,
  TECHNOLOGY_DIALOG_EDIT_ARIA_LABEL,
  TECHNOLOGY_DIALOG_LABELS,
  TECHNOLOGY_DIALOG_LOADING,
  TECHNOLOGY_DIALOG_NAME_MAX_LENGTH_ERROR,
  TECHNOLOGY_DIALOG_REQUIRED_ERROR,
  TECHNOLOGY_DIALOG_RESTORE_ARIA_LABEL,
  TECHNOLOGY_DIALOG_SAVE_ARIA_LABEL,
  TECHNOLOGY_DIALOG_TITLES,
} from './technology-dialog.i18n';

export type TechnologyDialogMode = 'create' | 'view' | 'edit';

@Component({
  selector: 'app-technology-dialog',
  standalone: true,
  imports: [
    CrudEntityDialog,
    FloatLabel,
    InputText,
    ProgressSpinner,
    ReactiveFormsModule,
    Select,
  ],
  templateUrl: './technology-dialog.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TechnologyDialog {
  visible = model(false);
  form = input.required<TechnologyFormGroup>();
  mode = input.required<TechnologyDialogMode>();
  layerOptions = input.required<LayerOption[]>();
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

  protected readonly labels = TECHNOLOGY_DIALOG_LABELS;
  protected readonly requiredError = TECHNOLOGY_DIALOG_REQUIRED_ERROR;
  protected readonly nameMaxLengthError = TECHNOLOGY_DIALOG_NAME_MAX_LENGTH_ERROR;
  protected readonly loadingLabel = TECHNOLOGY_DIALOG_LOADING;
  protected readonly title = computed(() => TECHNOLOGY_DIALOG_TITLES[this.mode()]);
  protected readonly hasUnsavedChanges = () => this.mode() === 'edit' && this.form().dirty;
  protected readonly actionAriaLabels: CrudEntityDialogAriaLabels = {
    accept: TECHNOLOGY_DIALOG_ACCEPT_ARIA_LABEL,
    add: TECHNOLOGY_DIALOG_ADD_ARIA_LABEL,
    cancel: TECHNOLOGY_DIALOG_CANCEL_ARIA_LABEL,
    close: TECHNOLOGY_DIALOG_CLOSE_ARIA_LABEL,
    deactivate: TECHNOLOGY_DIALOG_DEACTIVATE_ARIA_LABEL,
    edit: TECHNOLOGY_DIALOG_EDIT_ARIA_LABEL,
    restore: TECHNOLOGY_DIALOG_RESTORE_ARIA_LABEL,
    save: TECHNOLOGY_DIALOG_SAVE_ARIA_LABEL,
  };

  protected isInvalid(control: AbstractControl): boolean {
    return control.invalid && (control.dirty || control.touched);
  }

  protected onSubmit(): void {
    if (!this.isLoading() && !this.isSaving() && this.mode() !== 'view') this.submitForm.emit();
  }
}
