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
  CrudEntityDialogMode,
} from '@components/crud-entity-dialog/crud-entity-dialog';
import { TechnologyCatalogOption } from '@features/technologies/technologies.model';
import { FloatLabel } from 'primeng/floatlabel';
import { InputText } from 'primeng/inputtext';
import { Select } from 'primeng/select';

import { ApplicationTechnologyFormGroup } from '../../forms/application-development-form.factory';
import {
  APPLICATION_TECHNOLOGY_DIALOG_ARIA_LABELS,
  APPLICATION_TECHNOLOGY_DIALOG_LABELS,
  APPLICATION_TECHNOLOGY_DIALOG_MAX_LENGTH_ERROR,
  APPLICATION_TECHNOLOGY_DIALOG_REQUIRED_ERROR,
  APPLICATION_TECHNOLOGY_DIALOG_TITLES,
} from './application-technology-dialog.i18n';

@Component({
  selector: 'app-application-technology-dialog',
  standalone: true,
  imports: [
    CrudEntityDialog,
    FloatLabel,
    InputText,
    ReactiveFormsModule,
    Select,
  ],
  templateUrl: './application-technology-dialog.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationTechnologyDialog {
  visible = model(false);
  form = input.required<ApplicationTechnologyFormGroup>();
  mode = input.required<CrudEntityDialogMode>();
  technologyOptions = input.required<TechnologyCatalogOption[]>();
  isSaving = input(false);
  isDeleting = input(false);
  canEdit = input(false);

  submitForm = output<void>();
  closed = output<void>();
  edit = output<void>();
  cancelEdit = output<void>();
  deactivate = output<void>();

  protected readonly title = computed(
    () => APPLICATION_TECHNOLOGY_DIALOG_TITLES[this.mode()],
  );
  protected readonly labels = APPLICATION_TECHNOLOGY_DIALOG_LABELS;
  protected readonly requiredError =
    APPLICATION_TECHNOLOGY_DIALOG_REQUIRED_ERROR;
  protected readonly maxLengthError =
    APPLICATION_TECHNOLOGY_DIALOG_MAX_LENGTH_ERROR;
  protected readonly actionAriaLabels =
    APPLICATION_TECHNOLOGY_DIALOG_ARIA_LABELS;
  protected readonly hasUnsavedChanges = () =>
    this.mode() === 'edit' && this.form().dirty;

  protected isInvalid(control: AbstractControl): boolean {
    return control.invalid && (control.dirty || control.touched);
  }

  protected onTechnologyChange(technologyId: number | null): void {
    const option = this.technologyOptions().find(
      (candidate) => candidate.id === technologyId,
    );
    const layerControl = this.form().controls.layerId;
    layerControl.setValue(option?.layerId ?? null);
    layerControl.markAsDirty();
  }

  protected layerLabel(): string {
    const technologyId = this.form().controls.technologyId.value;
    return (
      this.technologyOptions().find((option) => option.id === technologyId)
        ?.layerLabel ?? ''
    );
  }

  protected onSubmit(): void {
    if (!this.isSaving() && !this.isDeleting() && this.mode() !== 'view') {
      this.submitForm.emit();
    }
  }
}
