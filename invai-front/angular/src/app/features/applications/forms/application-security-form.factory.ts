import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { TEXT_FIELD_MAX_BYTES } from '@shared/constants/text-field.constants';
import {
  maxUtf8BytesValidator,
  richTextMaxBytesValidator,
} from '@shared/utils/text-length.utils';
import { APPLICATION_SECURITY_DEFAULT_ROLE_SYSTEM } from '../applications.constants';

export interface ApplicationSecurityFormControls {
  overallGradeId: FormControl<number | null>;
  identityProviderId: FormControl<number | null>;
  ensSubjectId: FormControl<number | null>;
  personalDataProcessingId: FormControl<number | null>;
  approvalDate: FormControl<Date | null>;
  confidentialityId: FormControl<number | null>;
  integrityId: FormControl<number | null>;
  traceabilityId: FormControl<number | null>;
  availabilityId: FormControl<number | null>;
  authenticityId: FormControl<number | null>;
  observation: FormControl<string>;
}

export type ApplicationSecurityFormGroup = FormGroup<ApplicationSecurityFormControls>;

export type ApplicationSecurityResourceKind = 'role' | 'web-context' | 'risk' | 'measure';

// A Soffid role candidate; roleId is Soffid's own identifier, sent as securityRoleId.
export interface ApplicationSecurityRoleOption {
  roleId: number;
  name: string;
  system: string | null;
  description: string | null;
}

export interface ApplicationSecurityResourceFormControls {
  role: FormControl<ApplicationSecurityRoleOption | null>;
  url: FormControl<string>;
  webContextId: FormControl<number | null>;
  fieldId: FormControl<number | null>;
  observation: FormControl<string>;
  levelId: FormControl<number | null>;
  description: FormControl<string>;
  typeId: FormControl<number | null>;
  ensRequirementId: FormControl<number | null>;
}

export type ApplicationSecurityResourceFormGroup =
  FormGroup<ApplicationSecurityResourceFormControls>;

export type ApplicationWebContextVerificationForm = FormGroup<{
  reason: FormControl<string>;
}>;

export function createApplicationWebContextVerificationForm(
  formBuilder: FormBuilder,
): ApplicationWebContextVerificationForm {
  return formBuilder.nonNullable.group({
    reason: formBuilder.nonNullable.control('', [
      (control) => control.value.trim() ? null : { required: true },
      maxUtf8BytesValidator(TEXT_FIELD_MAX_BYTES),
    ]),
  });
}

export function createApplicationSecurityForm(
  formBuilder: FormBuilder,
): ApplicationSecurityFormGroup {
  return formBuilder.group({
    overallGradeId: formBuilder.control<number | null>(null),
    identityProviderId: formBuilder.control<number | null>(null),
    ensSubjectId: formBuilder.control<number | null>(null),
    personalDataProcessingId: formBuilder.control<number | null>(null),
    approvalDate: formBuilder.control<Date | null>(null),
    confidentialityId: formBuilder.control<number | null>(null),
    integrityId: formBuilder.control<number | null>(null),
    traceabilityId: formBuilder.control<number | null>(null),
    availabilityId: formBuilder.control<number | null>(null),
    authenticityId: formBuilder.control<number | null>(null),
    observation: formBuilder.nonNullable.control(
      '',
      richTextMaxBytesValidator(TEXT_FIELD_MAX_BYTES),
    ),
  });
}

export function createApplicationSecurityResourceForm(
  formBuilder: FormBuilder,
): ApplicationSecurityResourceFormGroup {
  return formBuilder.group({
    role: formBuilder.control<ApplicationSecurityRoleOption | null>(null),
    url: formBuilder.nonNullable.control(''),
    webContextId: formBuilder.control<number | null>(null),
    fieldId: formBuilder.control<number | null>(null),
    observation: formBuilder.nonNullable.control('', maxUtf8BytesValidator(TEXT_FIELD_MAX_BYTES)),
    levelId: formBuilder.control<number | null>(null),
    description: formBuilder.nonNullable.control('', maxUtf8BytesValidator(TEXT_FIELD_MAX_BYTES)),
    typeId: formBuilder.control<number | null>(null),
    ensRequirementId: formBuilder.control<number | null>(null),
  });
}

export function configureApplicationSecurityResourceForm(
  form: ApplicationSecurityResourceFormGroup,
  kind: ApplicationSecurityResourceKind,
): void {
  form.reset();
  form.controls.role.clearValidators();
  form.controls.url.clearValidators();
  form.controls.webContextId.clearValidators();
  form.controls.fieldId.clearValidators();
  if (kind === 'role') form.controls.role.addValidators(Validators.required);
  if (kind === 'web-context') {
    form.controls.url.addValidators([Validators.maxLength(255), control => {
      const value = (control.value as string).trim();
      return !value || /^https?:\/\/.+/.test(value) ? null : { pattern: true };
    }]);
    form.controls.webContextId.addValidators(Validators.required);
    form.controls.fieldId.addValidators(Validators.required);
  }
  form.controls.role.updateValueAndValidity({ emitEvent: false });
  form.controls.url.updateValueAndValidity({ emitEvent: false });
  form.controls.webContextId.updateValueAndValidity({ emitEvent: false });
  form.controls.fieldId.updateValueAndValidity({ emitEvent: false });
}

export interface ApplicationSecurityRoleFiltersControls {
  securityRoleName: FormControl<string>;
  system: FormControl<string>;
}

export type ApplicationSecurityRoleFiltersFormGroup = FormGroup<ApplicationSecurityRoleFiltersControls>;

// The system keeps its weblogic default after a reset, like the default status of other filters.
export function createApplicationSecurityRoleFiltersForm(
  formBuilder: FormBuilder,
): ApplicationSecurityRoleFiltersFormGroup {
  return formBuilder.group({
    securityRoleName: formBuilder.nonNullable.control(''),
    system: formBuilder.nonNullable.control(APPLICATION_SECURITY_DEFAULT_ROLE_SYSTEM),
  });
}
