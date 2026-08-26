import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';

export interface ApplicationAuthorizedFormControls {
  personalCaib: FormControl<boolean>;
  companyId: FormControl<number | null>;
  personId: FormControl<number | null>;
  email: FormControl<string>;
  observation: FormControl<string>;
  authorizationTypeIds: FormControl<number[]>;
}

export type ApplicationAuthorizedFormGroup = FormGroup<ApplicationAuthorizedFormControls>;

export function createApplicationAuthorizedForm(
  formBuilder: FormBuilder,
): ApplicationAuthorizedFormGroup {
  return formBuilder.group({
    personalCaib: formBuilder.nonNullable.control(false),
    companyId: formBuilder.control<number | null>(null),
    personId: formBuilder.control<number | null>(null, Validators.required),
    email: formBuilder.nonNullable.control({ value: '', disabled: true }),
    observation: formBuilder.nonNullable.control(''),
    authorizationTypeIds: formBuilder.nonNullable.control<number[]>([], Validators.required),
  });
}

export function setApplicationAuthorizedCompanyRequired(
  form: ApplicationAuthorizedFormGroup,
  required: boolean,
): void {
  const control = form.controls.companyId;
  if (required) {
    control.addValidators(Validators.required);
  } else {
    control.removeValidators(Validators.required);
  }
  control.updateValueAndValidity({ emitEvent: false });
}
