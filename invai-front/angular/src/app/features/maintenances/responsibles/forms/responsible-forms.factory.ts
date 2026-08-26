import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';

export interface ResponsibleNameFormControls {
  name: FormControl<string>;
}

export type ResponsibleNameFormGroup = FormGroup<ResponsibleNameFormControls>;

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

export interface ResponsibleNameFiltersFormControls {
  name: FormControl<string | null>;
  status: FormControl<SoftDeleteStatus | null>;
}

export type ResponsibleNameFiltersFormGroup = FormGroup<ResponsibleNameFiltersFormControls>;

export interface ResponsibleAuthorizationFiltersFormControls {
  name: FormControl<string | null>;
  nameEs: FormControl<string | null>;
  status: FormControl<SoftDeleteStatus | null>;
}

export type ResponsibleAuthorizationFiltersFormGroup =
  FormGroup<ResponsibleAuthorizationFiltersFormControls>;

export interface ResponsiblePersonFiltersFormControls {
  companyId: FormControl<number | null>;
  firstName: FormControl<string | null>;
  lastName: FormControl<string | null>;
  email: FormControl<string | null>;
  status: FormControl<SoftDeleteStatus | null>;
}

export type ResponsiblePersonFiltersFormGroup = FormGroup<ResponsiblePersonFiltersFormControls>;

export interface RoleTransferFormControls {
  sourcePersonId: FormControl<number | null>;
  destinationPersonId: FormControl<number | null>;
  revoke: FormControl<boolean>;
}

export type RoleTransferFormGroup = FormGroup<RoleTransferFormControls>;

export function createResponsibleNameForm(formBuilder: FormBuilder): ResponsibleNameFormGroup {
  return formBuilder.nonNullable.group({
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

export function createResponsibleNameFiltersForm(
  formBuilder: FormBuilder,
): ResponsibleNameFiltersFormGroup {
  return formBuilder.group({
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
    sourcePersonId: formBuilder.control<number | null>(null, Validators.required),
    destinationPersonId: formBuilder.control<number | null>(
      { value: null, disabled: true },
      Validators.required,
    ),
    revoke: formBuilder.nonNullable.control(false),
  });
}
