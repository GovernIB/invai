import { ApplicationDir3ManualForm } from '../../../../forms/application-dir3-manual-form.factory';
import { ApplicationAssignedResponsibleOutput } from '../../../../applications.model';
import { DIR3_COPY } from './application-dir3.i18n';
import { ApplicationDir3CheckState } from './application-dir3-check.state';
import { ApplicationDir3Feedback } from './application-dir3-feedback';
import { ChangeDetectionStrategy, Component, computed, input, model, output } from '@angular/core';
import { AbstractControl, ReactiveFormsModule } from '@angular/forms';
import {
  CrudEntityDialog,
  CrudEntityDialogAriaLabels,
  CrudEntityDialogMode,
} from '@components/crud-entity-dialog/crud-entity-dialog';
import { FloatLabel } from 'primeng/floatlabel';
import { Button } from 'primeng/button';
import { InputText } from 'primeng/inputtext';
import { Select } from 'primeng/select';
import { Textarea } from 'primeng/textarea';
import { SelectPassThrough } from 'primeng/types/select';
import { ToggleSwitch } from 'primeng/toggleswitch';
import { ToggleSwitchPassThrough } from 'primeng/types/toggleswitch';

import { ApplicationResponsibleFormGroup } from '../../../../forms/application-responsible-form.factory';
import {
  ResponsibleCompanyOption,
  ResponsiblePerson,
  SoffidPersonOption,
} from '../../../../../maintenances/responsibles/responsibles.model';
import { toResponsiblePersonOption } from '../../../../../maintenances/responsibles/responsibles.utils';
import { ApplicationSoffidPersonField } from './application-soffid-person-field';
import { APPLICATION_RESPONSIBLE_COPY } from './application-responsible-section.i18n';

export interface ApplicationResponsibleSelectOption {
  id: number;
  label: string;
  disabled?: boolean;
}

@Component({
  selector: 'app-application-responsible-dialog',
  standalone: true,
  imports: [
    CrudEntityDialog,
    Button,
    ApplicationDir3Feedback,
    ApplicationSoffidPersonField,
    FloatLabel,
    InputText,
    ReactiveFormsModule,
    Select,
    Textarea,
    ToggleSwitch,
  ],
  templateUrl: './application-responsible-dialog.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationResponsibleDialog {
  visible = model(false);
  manualForm = input<ApplicationDir3ManualForm>();
  manualTarget = input<ApplicationAssignedResponsibleOutput | null>(null);
  manualPartial = input(false);
  manualError = input<string | null>(null);
  manualConflict = input(false);
  protected readonly dir3Copy = DIR3_COPY;
  protected readonly manualOnly = computed(() => this.manualTarget() !== null);
  protected readonly manualCheckBlocked = computed(() => this.dir3Check()?.mismatch() !== true);
  protected readonly manualNotice = computed(() => {
    const check = this.dir3Check();
    switch (check?.phase()) {
      case 'loading':
        return this.dir3Copy.checking;
      case 'error':
        return check.denied() ? this.dir3Copy.denied : this.dir3Copy.error;
      case 'ready':
        if (check.result()?.matches) return this.dir3Copy.matches;
        return check.result()?.groupDir3?.trim() ? this.dir3Copy.mismatch : this.dir3Copy.missing;
      default:
        return this.dir3Copy.insufficientData;
    }
  });
  protected manualReasonInvalid(): boolean {
    const form = this.manualForm();
    return Boolean(
      form?.hasError('manualReasonRequired') &&
      (form.controls.reason.touched || form.controls.reason.dirty),
    );
  }
  form = input.required<ApplicationResponsibleFormGroup>();
  mode = input.required<Extract<CrudEntityDialogMode, 'create' | 'edit'>>();
  responsibleTypeOptions = input.required<ApplicationResponsibleSelectOption[]>();
  companyOptions = input.required<ResponsibleCompanyOption[]>();
  companyOptionsLoadFailed = input(false);
  people = input.required<ResponsiblePerson[]>();
  isSaving = input(false);
  dir3Check = input<ApplicationDir3CheckState>();
  retryDir3 = output<void>();
  personalCaibLocked = input(false);
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
    this.manualOnly()
      ? this.dir3Copy.confirmDiscrepancy
      : this.mode() === 'create'
        ? this.copy.responsibleDialogCreateTitle
        : this.copy.responsibleDialogEditTitle,
  );
  protected readonly actionAriaLabels = computed<CrudEntityDialogAriaLabels>(() =>
    this.manualOnly()
      ? {
          ...this.copy.actions,
          cancel: this.dir3Copy.cancel,
          close: this.dir3Copy.close,
          save: this.dir3Copy.confirm,
        }
      : this.copy.actions,
  );
  protected readonly requiredError = $localize`Aquest camp és obligatori.`;
  protected readonly typeLabelId = 'application-responsible-dialog-type-label';
  protected readonly companyLabelId = 'application-responsible-dialog-company-label';
  protected readonly personLabelId = 'application-responsible-dialog-person-label';
  protected readonly hasUnsavedChanges = () =>
    this.manualOnly()
      ? Boolean(this.manualForm()?.dirty)
      : this.mode() === 'edit' && this.form().dirty;
  protected readonly personOptions = computed(() => this.people().map(toResponsiblePersonOption));
  protected readonly personalCaibPassThrough = computed<ToggleSwitchPassThrough>(() => ({
    input: {
      'aria-describedby': this.personalCaibLocked()
        ? 'application-responsible-dialog-caib-required'
        : undefined,
    },
  }));

  protected readonly typeSelectPassThrough = computed<SelectPassThrough>(() =>
    this.selectPassThrough(this.form().controls.responsibleTypeId, 'type'),
  );
  protected readonly companySelectPassThrough = computed<SelectPassThrough>(() =>
    this.selectPassThrough(this.form().controls.companyId, 'company'),
  );
  protected readonly personSelectPassThrough = computed<SelectPassThrough>(() =>
    this.selectPassThrough(this.form().controls.personId, 'person'),
  );

  protected isInvalid(control: AbstractControl): boolean {
    return control.invalid && (control.dirty || control.touched);
  }

  protected onSubmit(): void {
    if (
      !this.isSaving() &&
      (this.manualOnly()
        ? !this.manualConflict() &&
          !this.manualCheckBlocked() &&
          this.manualForm()?.controls.validate.value
        : this.mode() !== 'create' || this.dir3Check()?.canSubmit() !== false)
    )
      this.submitForm.emit();
  }

  private selectPassThrough(
    control: AbstractControl,
    field: 'type' | 'company' | 'person',
  ): SelectPassThrough {
    const describedBy = this.isInvalid(control)
      ? `application-responsible-dialog-${field}-error`
      : field === 'company' && this.companyOptionsLoadFailed()
        ? 'application-responsible-dialog-company-load-error'
        : undefined;

    return {
      label: {
        'aria-describedby': describedBy,
        'aria-invalid': this.isInvalid(control) || undefined,
      },
    };
  }
}
