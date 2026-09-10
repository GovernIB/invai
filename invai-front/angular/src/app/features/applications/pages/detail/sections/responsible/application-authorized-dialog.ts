import { ChangeDetectionStrategy, Component, computed, input, model, output } from '@angular/core';
import { AbstractControl, ReactiveFormsModule } from '@angular/forms';
import {
  CrudEntityDialog,
  CrudEntityDialogAriaLabels,
  CrudEntityDialogMode,
} from '@components/crud-entity-dialog/crud-entity-dialog';
import { FloatLabel } from 'primeng/floatlabel';
import { InputText } from 'primeng/inputtext';
import { MultiSelect } from 'primeng/multiselect';
import { Select } from 'primeng/select';
import { Textarea } from 'primeng/textarea';
import { ToggleSwitch } from 'primeng/toggleswitch';
import { MultiSelectPassThrough } from 'primeng/types/multiselect';
import { SelectPassThrough } from 'primeng/types/select';
import { ToggleSwitchPassThrough } from 'primeng/types/toggleswitch';

import { ApplicationAuthorizedFormGroup } from '../../../../forms/application-authorized-form.factory';
import {
  ResponsibleCompanyOption,
  ResponsiblePerson,
  SoffidPersonOption,
} from '../../../../../maintenances/responsibles/responsibles.model';
import { toResponsiblePersonOption } from '../../../../../maintenances/responsibles/responsibles.utils';
import { ApplicationResponsibleSelectOption } from './application-responsible-dialog';
import { ApplicationSoffidPersonField } from './application-soffid-person-field';
import { APPLICATION_RESPONSIBLE_COPY } from './application-responsible-section.i18n';

@Component({
  selector: 'app-application-authorized-dialog',
  standalone: true,
  imports: [
    CrudEntityDialog,
    ApplicationSoffidPersonField,
    FloatLabel,
    InputText,
    MultiSelect,
    ReactiveFormsModule,
    Select,
    Textarea,
    ToggleSwitch,
  ],
  templateUrl: './application-authorized-dialog.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationAuthorizedDialog {
  visible = model(false);
  form = input.required<ApplicationAuthorizedFormGroup>();
  mode = input.required<Extract<CrudEntityDialogMode, 'create' | 'edit'>>();
  authorizationTypeOptions = input.required<ApplicationResponsibleSelectOption[]>();
  companyOptions = input.required<ResponsibleCompanyOption[]>();
  companyOptionsLoadFailed = input(false);
  people = input.required<ResponsiblePerson[]>();
  isSaving = input(false);
  soffidOptions = input.required<SoffidPersonOption[]>();
  soffidLoading = input(false);
  soffidSearched = input(false);
  soffidSearchError = input(false);
  soffidTotal = input(0);

  submitForm = output<void>();
  closed = output<void>();
  cancelEdit = output<void>();
  soffidSearch = output<string>();

  protected readonly copy = APPLICATION_RESPONSIBLE_COPY;
  protected readonly title = computed(() =>
    this.mode() === 'create'
      ? this.copy.authorizedDialogCreateTitle
      : this.copy.authorizedDialogEditTitle,
  );
  protected readonly actionAriaLabels: CrudEntityDialogAriaLabels = this.copy.authorizedActions;
  protected readonly requiredError = $localize`Aquest camp és obligatori.`;
  protected readonly companyLabelId = 'application-authorized-dialog-company-label';
  protected readonly personLabelId = 'application-authorized-dialog-person-label';
  protected readonly authorizationLabelId = 'application-authorized-dialog-authorization-label';
  protected readonly hasUnsavedChanges = () => this.mode() === 'edit' && this.form().dirty;
  protected readonly personOptions = computed(() => this.people().map(toResponsiblePersonOption));
  protected readonly personalCaibPassThrough = computed<ToggleSwitchPassThrough>(() => ({}));

  protected readonly companySelectPassThrough = computed<SelectPassThrough>(() =>
    this.selectPassThrough(this.form().controls.companyId, 'company'),
  );
  protected readonly personSelectPassThrough = computed<SelectPassThrough>(() =>
    this.selectPassThrough(this.form().controls.personId, 'person'),
  );
  protected readonly authorizationPassThrough = computed<MultiSelectPassThrough>(() => ({
    label: {
      'aria-describedby': this.isInvalid(this.form().controls.authorizationTypeIds)
        ? 'application-authorized-dialog-authorization-error'
        : undefined,
      'aria-invalid': this.isInvalid(this.form().controls.authorizationTypeIds) || undefined,
    },
  }));

  protected isInvalid(control: AbstractControl): boolean {
    return control.invalid && (control.dirty || control.touched);
  }

  protected onSubmit(): void {
    if (!this.isSaving()) this.submitForm.emit();
  }

  private selectPassThrough(
    control: AbstractControl,
    field: 'company' | 'person',
  ): SelectPassThrough {
    const describedBy = this.isInvalid(control)
      ? `application-authorized-dialog-${field}-error`
      : field === 'company' && this.companyOptionsLoadFailed()
        ? 'application-authorized-dialog-company-load-error'
        : undefined;

    return {
      label: {
        'aria-describedby': describedBy,
        'aria-invalid': this.isInvalid(control) || undefined,
      },
    };
  }
}
