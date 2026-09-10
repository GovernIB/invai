import {
  AbstractControl,
  FormBuilder,
  FormControl,
  FormGroup,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { SoffidPersonControlValue } from '@features/maintenances/responsibles/responsibles.model';

export interface ApplicationResponsibleFormControls {
  personalCaib: FormControl<boolean>;
  responsibleTypeId: FormControl<number | null>;
  companyId: FormControl<number | null>;
  personId: FormControl<number | null>;
  soffidPerson: FormControl<SoffidPersonControlValue>;
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
    soffidPerson: formBuilder.control<SoffidPersonControlValue>({ value: null, disabled: true }),
    email: formBuilder.nonNullable.control(''),
    cargo: formBuilder.nonNullable.control({ value: '', disabled: true }),
    observation: formBuilder.nonNullable.control(''),
  });
}

export function setApplicationResponsiblePersonSource(
  form: ApplicationResponsibleFormGroup,
  personalCaib: boolean,
): void {
  configurePersonSource(
    form.controls.companyId,
    form.controls.personId,
    form.controls.soffidPerson,
    personalCaib,
  );
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

function configurePersonSource(
  company: FormControl<number | null>,
  person: FormControl<number | null>,
  soffidPerson: FormControl<SoffidPersonControlValue>,
  personalCaib: boolean,
): void {
  if (personalCaib) {
    company.clearValidators();
    person.clearValidators();
    soffidPerson.setValidators([Validators.required, selectedSoffidPersonValidator]);
  } else {
    person.setValidators(Validators.required);
    soffidPerson.clearValidators();
  }
  company.updateValueAndValidity({ emitEvent: false });
  person.updateValueAndValidity({ emitEvent: false });
  soffidPerson.updateValueAndValidity({ emitEvent: false });
}

export function selectedSoffidPersonValidator(control: AbstractControl): ValidationErrors | null {
  const value = control.value as SoffidPersonControlValue;
  if (value === null || value === '') return null;
  if (typeof value === 'string') return { soffidSelection: true };
  return value.personalCaib === true &&
    value.firstName.trim() &&
    value.lastName.trim() &&
    value.email.trim()
    ? null
    : { soffidSelection: true };
}
