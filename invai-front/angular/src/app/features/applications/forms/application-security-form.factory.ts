import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';

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

export type ApplicationSecurityResourceKind = 'web-context' | 'risk' | 'measure';

export interface ApplicationSecurityResourceFormControls {
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
    observation: formBuilder.nonNullable.control(''),
  });
}

export function createApplicationSecurityResourceForm(
  formBuilder: FormBuilder,
): ApplicationSecurityResourceFormGroup {
  return formBuilder.group({
    webContextId: formBuilder.control<number | null>(null),
    fieldId: formBuilder.control<number | null>(null),
    observation: formBuilder.nonNullable.control(''),
    levelId: formBuilder.control<number | null>(null),
    description: formBuilder.nonNullable.control(''),
    typeId: formBuilder.control<number | null>(null),
    ensRequirementId: formBuilder.control<number | null>(null),
  });
}

export function configureApplicationSecurityResourceForm(
  form: ApplicationSecurityResourceFormGroup,
  kind: ApplicationSecurityResourceKind,
): void {
  form.reset();
  form.controls.webContextId.clearValidators();
  form.controls.fieldId.clearValidators();
  if (kind === 'web-context') {
    form.controls.webContextId.addValidators(Validators.required);
    form.controls.fieldId.addValidators(Validators.required);
  }
  form.controls.webContextId.updateValueAndValidity({ emitEvent: false });
  form.controls.fieldId.updateValueAndValidity({ emitEvent: false });
}
