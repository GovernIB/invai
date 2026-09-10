import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { AbstractControl, FormControl, ReactiveFormsModule } from '@angular/forms';
import { CommissionType } from '@features/commissions/commissions.model';
import { Editor } from 'primeng/editor';
import { InputText } from 'primeng/inputtext';
import { Select } from 'primeng/select';
import { Button } from 'primeng/button';
import type { EditorInitEvent, EditorPassThrough } from 'primeng/types/editor';

import {
  APPLICATION_CATEGORY_OPTIONS,
  APPLICATION_COMMISSION_TYPE_LABELS,
  APPLICATION_INFORMATION_SYSTEM_OPTIONS,
  APPLICATION_SCOPE_OPTIONS,
} from '../../applications.constants';
import {
  APPLICATION_CODE_MAX_LENGTH,
  APPLICATION_PREFIX_MAX_LENGTH,
  ApplicationCommonFormControls,
  ApplicationDetailFormControls,
} from '../../forms/application-form.factory';
import {
  ApplicationCommissionOption,
  ApplicationSelectOptions,
} from '../../services/application-options.service';

export interface ApplicationFormFieldLabels {
  application: string;
  category: string;
  informationSystem: string;
  scope: string;
  commissionSectionTitle: string;
  commissionName: string;
  commissionExpedientNumber: string;
  commissionApprovalDate: string;
  commissionType: string;
  prefix: string;
  administrativeUnit: string;
  conselleria: string;
  departmentsLoading: string;
  departmentsLoadError: string;
  administrativeUnitsLoading: string;
  administrativeUnitsLoadError: string;
  administrativeUnitsEmpty: string;
  selectConselleriaFirst: string;
  retry: string;
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
  imports: [Button, Editor, InputText, ReactiveFormsModule, Select],
  templateUrl: './application-form-fields.html',
  styleUrl: './application-form-fields.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationFormFields {
  controls = input.required<ApplicationCommonFormControls>();
  labels = input.required<ApplicationFormFieldLabels>();
  idPrefix = input.required<string>();
  requiredError = input.required<string>();
  prefixMaxLengthError = input.required<string>();
  codeMinLengthError = input<string | null>(null);
  codeMaxLengthError = input<string | null>(null);
  selectPlaceholder = input.required<string>();
  isReadOnly = input(false);
  codeControl = input<FormControl<string> | null>(null);
  auditControls = input<ApplicationAuditFormControls | null>(null);
  options = input<Partial<ApplicationSelectOptions> | null>(null);
  departmentsLoading = input(false);
  departmentsLoadFailed = input(false);
  administrativeUnitsLoading = input(false);
  administrativeUnitsLoadFailed = input(false);
  commissionSelected = output<ApplicationCommissionOption | null>();
  conselleriaSelected = output<string | null>();
  departmentsRetry = output<void>();
  administrativeUnitsRetry = output<void>();

  protected readonly prefixMaxLength = APPLICATION_PREFIX_MAX_LENGTH;
  protected readonly codeMaxLength = APPLICATION_CODE_MAX_LENGTH;

  protected get descriptionEditorPassThrough(): EditorPassThrough {
    return {
      toolbar: {
        role: 'toolbar',
        'aria-label': this.labels().description,
      },
    };
  }

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
    return this.options()?.commissions ?? [];
  }

  protected get administrativeUnitOptions() {
    return this.options()?.administrativeUnits ?? [];
  }

  protected get conselleriaOptions() {
    return this.options()?.departments ?? [];
  }

  protected isInvalid(control: AbstractControl<unknown>): boolean {
    return control.invalid && (control.dirty || control.touched);
  }

  protected prefixError(control: AbstractControl<unknown>): string {
    return control.hasError('maxlength') ? this.prefixMaxLengthError() : this.requiredError();
  }

  protected codeError(control: AbstractControl<unknown>): string {
    if (control.hasError('minlength')) {
      return this.codeMinLengthError() ?? this.requiredError();
    }

    if (control.hasError('maxlength')) {
      return this.codeMaxLengthError() ?? this.requiredError();
    }

    return this.requiredError();
  }

  protected selectCommission(commissionId: number | null): void {
    const selected =
      this.commissionOptions.find((option) => option.value === commissionId) ?? null;
    this.commissionSelected.emit(selected);
  }

  protected commissionTypeLabel(type: CommissionType | null): string {
    return type ? APPLICATION_COMMISSION_TYPE_LABELS[type] : '';
  }

  protected selectConselleria(code: string | null): void {
    this.conselleriaSelected.emit(code);
  }

  protected retryAdministrativeUnits(): void {
    this.administrativeUnitsRetry.emit();
  }

  protected retryDepartments(): void {
    this.departmentsRetry.emit();
  }

  protected administrativeUnitStatus(): string | null {
    if (this.administrativeUnitsLoading()) return this.labels().administrativeUnitsLoading;
    if (this.administrativeUnitsLoadFailed()) {
      return this.labels().administrativeUnitsLoadError;
    }
    if (!this.controls().conselleria.value) return this.labels().selectConselleriaFirst;
    if (!this.administrativeUnitOptions.length) return this.labels().administrativeUnitsEmpty;
    return null;
  }

  protected administrativeUnitDescribedBy(): string | null {
    const ids: string[] = [];
    if (this.isInvalid(this.controls().administrativeUnit)) {
      ids.push(this.errorId('administrative-unit'));
    }
    if (this.administrativeUnitStatus()) {
      ids.push(this.fieldId('administrative-unit-status'));
    }
    return ids.length ? ids.join(' ') : null;
  }

  protected conselleriaDescribedBy(): string | null {
    const ids: string[] = [];
    if (this.isInvalid(this.controls().conselleria)) ids.push(this.errorId('conselleria'));
    if (this.departmentsLoading() || this.departmentsLoadFailed()) {
      ids.push(this.fieldId('conselleria-status'));
    }
    return ids.length ? ids.join(' ') : null;
  }

  protected optionLabel(
    options: readonly { label: string; value: unknown }[],
    value: unknown,
  ): string {
    return options.find((option) => option.value === value)?.label ?? '-';
  }

  protected initializeDescriptionEditor({ editor }: EditorInitEvent): void {
    const root = editor?.root as HTMLElement | undefined;
    if (!root) return;

    root.id = this.fieldId('description');
    root.setAttribute('role', 'textbox');
    root.setAttribute('aria-multiline', 'true');
    root.setAttribute('aria-labelledby', this.fieldId('description-label'));
  }

  protected fieldId(controlName: string): string {
    return `${this.idPrefix()}-${controlName}`;
  }

  protected errorId(controlName: string): string {
    return `${this.fieldId(controlName)}-error`;
  }
}
