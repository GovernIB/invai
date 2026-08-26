import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';

export interface ApplicationResponsibleFormControls {
  personalCaib: FormControl<boolean>;
  responsibleTypeId: FormControl<number | null>;
  companyId: FormControl<number | null>;
  personId: FormControl<number | null>;
  email: FormControl<string>;
  cargo: FormControl<string>;
  observation: FormControl<string>;
}

export type ApplicationResponsibleFormGroup = FormGroup<ApplicationResponsibleFormControls>;

export function createApplicationResponsibleForm(
  formBuilder: FormBuilder,
): ApplicationResponsibleFormGroup {
  return formBuilder.group({
    personalCaib: formBuilder.nonNullable.control(false),
    responsibleTypeId: formBuilder.control<number | null>(null, Validators.required),
    companyId: formBuilder.control<number | null>(null),
    personId: formBuilder.control<number | null>(null, Validators.required),
    email: formBuilder.nonNullable.control({ value: '', disabled: true }),
    cargo: formBuilder.nonNullable.control({ value: '', disabled: true }),
    observation: formBuilder.nonNullable.control(''),
  });
}

export function setApplicationResponsibleCompanyRequired(
  form: ApplicationResponsibleFormGroup,
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
