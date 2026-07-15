import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';

export const APPLICATION_CODE_MIN_LENGTH = 4;

export interface ApplicationCommonFormControls {
  application: FormControl<string>;
  category: FormControl<number | null>;
  informationSystem: FormControl<number | null>;
  scope: FormControl<number | null>;
  commission: FormControl<number | null>;
  prefix: FormControl<string>;
  administrativeUnit: FormControl<number | null>;
  description: FormControl<string>;
}

export interface ApplicationCreateFormControls extends ApplicationCommonFormControls {
  code: FormControl<string>;
}

export interface ApplicationDetailFormControls extends ApplicationCommonFormControls {
  creationDate: FormControl<string>;
  modificationDate: FormControl<string>;
  withdrawalDate: FormControl<string>;
}

export interface ApplicationFiltersFormControls {
  prefix: FormControl<string | null>;
  application: FormControl<string | null>;
  category: FormControl<number | null>;
  informationSystem: FormControl<number | null>;
  scope: FormControl<number | null>;
  commission: FormControl<number | null>;
  administrativeUnit: FormControl<number | null>;
  status: FormControl<number | null>;
  description: FormControl<string | null>;
  incomplete: FormControl<boolean>;
}

export interface ApplicationSystemsDatabasesFormControls {
  observations: FormControl<string>;
}

export type ApplicationCreateFormGroup = FormGroup<ApplicationCreateFormControls>;
export type ApplicationDetailFormGroup = FormGroup<ApplicationDetailFormControls>;
export type ApplicationFiltersFormGroup = FormGroup<ApplicationFiltersFormControls>;
export type ApplicationSystemsDatabasesFormGroup =
  FormGroup<ApplicationSystemsDatabasesFormControls>;

export interface ApplicationDetailFormValue {
  application: string;
  category: number | null;
  informationSystem: number | null;
  scope: number | null;
  commission: number | null;
  prefix: string;
  administrativeUnit: number | null;
  description: string;
  creationDate: string;
  modificationDate: string;
  withdrawalDate: string;
}

export function createApplicationCreateForm(formBuilder: FormBuilder): ApplicationCreateFormGroup {
  return formBuilder.nonNullable.group({
    ...createCommonControls(formBuilder),
    code: ['', [Validators.required, Validators.minLength(APPLICATION_CODE_MIN_LENGTH)]],
  });
}

export function createApplicationDetailForm(formBuilder: FormBuilder): ApplicationDetailFormGroup {
  return formBuilder.nonNullable.group({
    ...createCommonControls(formBuilder),
    creationDate: [''],
    modificationDate: [''],
    withdrawalDate: [''],
  });
}

export function createApplicationFiltersForm(formBuilder: FormBuilder): ApplicationFiltersFormGroup {
  return formBuilder.group({
    prefix: formBuilder.control<string | null>(null),
    application: formBuilder.control<string | null>(null),
    category: formBuilder.control<number | null>(null),
    informationSystem: formBuilder.control<number | null>(null),
    scope: formBuilder.control<number | null>(null),
    commission: formBuilder.control<number | null>(null),
    administrativeUnit: formBuilder.control<number | null>(null),
    status: formBuilder.control<number | null>(null),
    description: formBuilder.control<string | null>(null),
    incomplete: formBuilder.nonNullable.control(false),
  });
}

export function createApplicationSystemsDatabasesForm(
  formBuilder: FormBuilder,
): ApplicationSystemsDatabasesFormGroup {
  return formBuilder.nonNullable.group({
    observations: [''],
  });
}

function createCommonControls(formBuilder: FormBuilder): ApplicationCommonFormControls {
  return {
    application: formBuilder.nonNullable.control('', Validators.required),
    category: formBuilder.control<number | null>(null, Validators.required),
    informationSystem: formBuilder.control<number | null>(null, Validators.required),
    scope: formBuilder.control<number | null>(null, Validators.required),
    commission: formBuilder.control<number | null>(null, Validators.required),
    prefix: formBuilder.nonNullable.control('', Validators.required),
    administrativeUnit: formBuilder.control<number | null>(null, Validators.required),
    description: formBuilder.nonNullable.control(''),
  };
}
