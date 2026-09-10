import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { SoffidPersonControlValue } from '@features/maintenances/responsibles/responsibles.model';
import { selectedSoffidPersonValidator } from './application-responsible-form.factory';

export interface ApplicationAuthorizedFormControls {
  personalCaib: FormControl<boolean>;
  companyId: FormControl<number | null>;
  personId: FormControl<number | null>;
  soffidPerson: FormControl<SoffidPersonControlValue>;
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
    soffidPerson: formBuilder.control<SoffidPersonControlValue>({ value: null, disabled: true }),
    email: formBuilder.nonNullable.control(''),
    observation: formBuilder.nonNullable.control(''),
    authorizationTypeIds: formBuilder.nonNullable.control<number[]>([], Validators.required),
  });
}

export function setApplicationAuthorizedPersonSource(
  form: ApplicationAuthorizedFormGroup,
  personalCaib: boolean,
): void {
  const { companyId, personId, soffidPerson } = form.controls;
  if (personalCaib) {
    companyId.clearValidators();
    personId.clearValidators();
    soffidPerson.setValidators([Validators.required, selectedSoffidPersonValidator]);
  } else {
    personId.setValidators(Validators.required);
    soffidPerson.clearValidators();
  }
  companyId.updateValueAndValidity({ emitEvent: false });
  personId.updateValueAndValidity({ emitEvent: false });
  soffidPerson.updateValueAndValidity({ emitEvent: false });
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
