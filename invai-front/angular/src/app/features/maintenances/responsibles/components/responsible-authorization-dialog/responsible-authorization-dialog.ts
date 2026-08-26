import { ChangeDetectionStrategy, Component, computed, input, model, output } from '@angular/core';
import { AbstractControl, ReactiveFormsModule } from '@angular/forms';
import {
  CrudEntityDialog,
  CrudEntityDialogMode,
} from '@components/crud-entity-dialog/crud-entity-dialog';
import { FloatLabel } from 'primeng/floatlabel';
import { InputText } from 'primeng/inputtext';

import { ResponsibleAuthorizationFormGroup } from '../../forms/responsible-forms.factory';
import { RESPONSIBLE_AUTHORIZATION_COPY, RESPONSIBLE_COMMON_COPY } from '../../responsibles.i18n';

@Component({
  selector: 'app-responsible-authorization-dialog',
  standalone: true,
  imports: [CrudEntityDialog, FloatLabel, InputText, ReactiveFormsModule],
  templateUrl: './responsible-authorization-dialog.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ResponsibleAuthorizationDialog {
  visible = model(false);
  form = input.required<ResponsibleAuthorizationFormGroup>();
  mode = input.required<CrudEntityDialogMode>();
  isSaving = input(false);
  isDeleting = input(false);
  canRestore = input(false);
  submitForm = output<void>();
  closed = output<void>();
  restore = output<void>();
  edit = output<void>();
  cancelEdit = output<void>();
  deactivate = output<void>();

  protected readonly copy = RESPONSIBLE_AUTHORIZATION_COPY;
  protected readonly commonCopy = RESPONSIBLE_COMMON_COPY;
  protected readonly title = computed(() => this.copy.dialogTitles[this.mode()]);
  protected readonly hasUnsavedChanges = () => this.mode() === 'edit' && this.form().dirty;

  protected isInvalid(control: AbstractControl): boolean {
    return control.invalid && (control.dirty || control.touched);
  }
}
