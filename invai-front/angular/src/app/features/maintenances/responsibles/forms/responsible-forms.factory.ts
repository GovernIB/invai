import {
  AbstractControl,
  FormBuilder,
  FormControl,
  FormGroup,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';

import { RoleTransferPersonControlValue } from '../responsibles.model';

export interface ResponsibleCompanyFormControls {
  nif: FormControl<string>;
  name: FormControl<string>;
}

export type ResponsibleCompanyFormGroup = FormGroup<ResponsibleCompanyFormControls>;

export interface ResponsibleAuthorizationFormControls {
  name: FormControl<string>;
  nameEs: FormControl<string>;
}

export type ResponsibleAuthorizationFormGroup = FormGroup<ResponsibleAuthorizationFormControls>;

export interface ResponsiblePersonFormControls {
  companyId: FormControl<number | null>;
  firstName: FormControl<string>;
  lastName: FormControl<string>;
  email: FormControl<string>;
}

export type ResponsiblePersonFormGroup = FormGroup<ResponsiblePersonFormControls>;

export interface ResponsibleCompanyFiltersFormControls {
  nif: FormControl<string | null>;
  name: FormControl<string | null>;
  status: FormControl<SoftDeleteStatus | null>;
}

export type ResponsibleCompanyFiltersFormGroup = FormGroup<ResponsibleCompanyFiltersFormControls>;

export interface ResponsibleAuthorizationFiltersFormControls {
  name: FormControl<string | null>;
  nameEs: FormControl<string | null>;
  status: FormControl<SoftDeleteStatus | null>;
}

export type ResponsibleAuthorizationFiltersFormGroup =
  FormGroup<ResponsibleAuthorizationFiltersFormControls>;

export interface ResponsiblePersonFiltersFormControls {
  personalCaib: FormControl<boolean | null>;
  companyId: FormControl<number | null>;
  firstName: FormControl<string | null>;
  lastName: FormControl<string | null>;
  email: FormControl<string | null>;
  status: FormControl<SoftDeleteStatus | null>;
}

export type ResponsiblePersonFiltersFormGroup = FormGroup<ResponsiblePersonFiltersFormControls>;

export interface RoleTransferFormControls {
  sourcePerson: FormControl<RoleTransferPersonControlValue>;
  destinationPerson: FormControl<RoleTransferPersonControlValue>;
  revoke: FormControl<boolean>;
}

export type RoleTransferFormGroup = FormGroup<RoleTransferFormControls>;

export function createResponsibleCompanyForm(formBuilder: FormBuilder): ResponsibleCompanyFormGroup {
  return formBuilder.nonNullable.group({
    nif: ['', Validators.maxLength(20)],
    name: ['', [Validators.required, Validators.pattern(/\S/), Validators.maxLength(150)]],
  });
}

export function createResponsibleAuthorizationForm(
  formBuilder: FormBuilder,
): ResponsibleAuthorizationFormGroup {
  return formBuilder.nonNullable.group({
    name: ['', [Validators.required, Validators.pattern(/\S/), Validators.maxLength(150)]],
    nameEs: ['', [Validators.required, Validators.pattern(/\S/), Validators.maxLength(100)]],
  });
}

export function createResponsiblePersonForm(formBuilder: FormBuilder): ResponsiblePersonFormGroup {
  return formBuilder.group({
    companyId: formBuilder.control<number | null>(null, Validators.required),
    firstName: formBuilder.nonNullable.control('', [
      Validators.required,
      Validators.pattern(/\S/),
      Validators.maxLength(150),
    ]),
    lastName: formBuilder.nonNullable.control('', [
      Validators.required,
      Validators.pattern(/\S/),
      Validators.maxLength(150),
    ]),
    email: formBuilder.nonNullable.control('', [
      Validators.required,
      Validators.email,
      Validators.maxLength(150),
    ]),
  });
}

export function createResponsibleCompanyFiltersForm(
  formBuilder: FormBuilder,
): ResponsibleCompanyFiltersFormGroup {
  return formBuilder.group({
    nif: formBuilder.control<string | null>(null),
    name: formBuilder.control<string | null>(null),
    status: formBuilder.control<SoftDeleteStatus | null>(SoftDeleteStatus.ACTIVE, {
      initialValueIsDefault: true,
    }),
  });
}

export function createResponsiblePersonFiltersForm(
  formBuilder: FormBuilder,
): ResponsiblePersonFiltersFormGroup {
  return formBuilder.group({
    personalCaib: formBuilder.control<boolean | null>(false, { initialValueIsDefault: true }),
    companyId: formBuilder.control<number | null>(null),
    firstName: formBuilder.control<string | null>(null),
    lastName: formBuilder.control<string | null>(null),
    email: formBuilder.control<string | null>(null),
    status: formBuilder.control<SoftDeleteStatus | null>(SoftDeleteStatus.ACTIVE, {
      initialValueIsDefault: true,
    }),
  });
}

export function createResponsibleAuthorizationFiltersForm(
  formBuilder: FormBuilder,
): ResponsibleAuthorizationFiltersFormGroup {
  return formBuilder.group({
    name: formBuilder.control<string | null>(null),
    nameEs: formBuilder.control<string | null>(null),
    status: formBuilder.control<SoftDeleteStatus | null>(SoftDeleteStatus.ACTIVE, {
      initialValueIsDefault: true,
    }),
  });
}

export function createRoleTransferForm(formBuilder: FormBuilder): RoleTransferFormGroup {
  return formBuilder.group({
    sourcePerson: formBuilder.control<RoleTransferPersonControlValue>(null, [
      Validators.required,
      selectedRoleTransferPersonValidator,
      transferableRoleSourceValidator,
    ]),
    destinationPerson: formBuilder.control<RoleTransferPersonControlValue>(
      { value: null, disabled: true },
      [Validators.required, selectedRoleTransferPersonValidator],
    ),
    revoke: formBuilder.nonNullable.control(false),
  });
}

export function selectedRoleTransferPersonValidator(
  control: AbstractControl<RoleTransferPersonControlValue>,
): ValidationErrors | null {
  return typeof control.value === 'string' ? { personSelection: true } : null;
}

export function transferableRoleSourceValidator(
  control: AbstractControl<RoleTransferPersonControlValue>,
): ValidationErrors | null {
  const value = control.value;
  return value && typeof value !== 'string' && value.id === null
    ? { sourceWithoutLocalId: true }
    : null;
}
