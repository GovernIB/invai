import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { AbstractControl, FormControl, ReactiveFormsModule } from '@angular/forms';
import { InputText } from 'primeng/inputtext';
import { Select } from 'primeng/select';
import { Textarea } from 'primeng/textarea';

import {
  APPLICATION_ADMINISTRATIVE_UNIT_OPTIONS,
  APPLICATION_CATEGORY_OPTIONS,
  APPLICATION_COMMISSION_OPTIONS,
  APPLICATION_INFORMATION_SYSTEM_OPTIONS,
  APPLICATION_SCOPE_OPTIONS,
} from '../../applications.constants';
import {
  ApplicationCommonFormControls,
  ApplicationDetailFormControls,
} from '../../forms/application-form.factory';
import { ApplicationSelectOptions } from '../../services/application-options.service';

export interface ApplicationFormFieldLabels {
  application: string;
  category: string;
  informationSystem: string;
  scope: string;
  commission: string;
  prefix: string;
  administrativeUnit: string;
  description: string;
  code?: string;
  creationDate?: string;
  modificationDate?: string;
  withdrawalDate?: string;
}

export type ApplicationAuditFormControls = Pick<
  ApplicationDetailFormControls,
  'creationDate' | 'modificationDate' | 'withdrawalDate'
>;

@Component({
  selector: 'app-application-form-fields',
  standalone: true,
  imports: [InputText, ReactiveFormsModule, Select, Textarea],
  templateUrl: './application-form-fields.html',
  styleUrl: './application-form-fields.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationFormFields {
  controls = input.required<ApplicationCommonFormControls>();
  labels = input.required<ApplicationFormFieldLabels>();
  idPrefix = input.required<string>();
  requiredError = input.required<string>();
  codeMinLengthError = input<string | null>(null);
  selectPlaceholder = input.required<string>();
  codeControl = input<FormControl<string> | null>(null);
  auditControls = input<ApplicationAuditFormControls | null>(null);
  options = input<Partial<ApplicationSelectOptions> | null>(null);

  protected get categoryOptions() {
    return this.options()?.categories ?? APPLICATION_CATEGORY_OPTIONS;
  }

  protected get informationSystemOptions() {
    return this.options()?.informationSystems ?? APPLICATION_INFORMATION_SYSTEM_OPTIONS;
  }

  protected get scopeOptions() {
    return this.options()?.scopes ?? APPLICATION_SCOPE_OPTIONS;
  }

  protected get commissionOptions() {
    return this.options()?.commissions ?? APPLICATION_COMMISSION_OPTIONS;
  }

  protected get administrativeUnitOptions() {
    return this.options()?.administrativeUnits ?? APPLICATION_ADMINISTRATIVE_UNIT_OPTIONS;
  }

  protected isInvalid(control: AbstractControl<unknown>): boolean {
    return control.invalid && (control.dirty || control.touched);
  }

  protected codeError(control: AbstractControl<unknown>): string {
    return control.hasError('minlength')
      ? (this.codeMinLengthError() ?? this.requiredError())
      : this.requiredError();
  }

  protected fieldId(controlName: string): string {
    return `${this.idPrefix()}-${controlName}`;
  }

  protected errorId(controlName: string): string {
    return `${this.fieldId(controlName)}-error`;
  }
}
