import {
  AbstractControl,
  FormBuilder,
  FormControl,
  FormGroup,
  ValidationErrors,
  ValidatorFn,
  Validators,
} from '@angular/forms';

import { DevelopmentModality, DevelopmentStandardAdaption } from '../applications.model';

export const APPLICATION_DEVELOPMENT_CODE_MAX_LENGTH = 1000;
export const APPLICATION_DEVELOPMENT_TEXT_MAX_LENGTH = 255;
export const APPLICATION_DEVELOPMENT_DATE_RANGE_ERROR = 'dateRange';

export interface ApplicationDevelopmentFormControls {
  environment: FormControl<number | null>;
  modality: FormControl<DevelopmentModality | null>;
  code: FormControl<string>;
  standardAdaption: FormControl<DevelopmentStandardAdaption | null>;
  revisionDate: FormControl<Date | null>;
  observation: FormControl<string>;
}

export interface ApplicationProviderFormControls {
  companyName: FormControl<string>;
  roleId: FormControl<number | null>;
  startDate: FormControl<Date | null>;
  expireDate: FormControl<Date | null>;
}

export interface ApplicationTechnologyFormControls {
  layerId: FormControl<number | null>;
  technologyId: FormControl<number | null>;
  version: FormControl<string>;
  architecture: FormControl<string>;
}

export type ApplicationDevelopmentFormGroup = FormGroup<ApplicationDevelopmentFormControls>;
export type ApplicationProviderFormGroup = FormGroup<ApplicationProviderFormControls>;
export type ApplicationTechnologyFormGroup = FormGroup<ApplicationTechnologyFormControls>;

const REQUIRED_TEXT_VALIDATORS = [
  Validators.required,
  Validators.pattern(/\S/),
  Validators.maxLength(APPLICATION_DEVELOPMENT_TEXT_MAX_LENGTH),
];

export function createApplicationDevelopmentForm(
  formBuilder: FormBuilder,
): ApplicationDevelopmentFormGroup {
  return formBuilder.group({
    environment: formBuilder.control<number | null>(null, Validators.required),
    modality: formBuilder.control<DevelopmentModality | null>(null, Validators.required),
    code: formBuilder.nonNullable.control('', [
      Validators.required,
      Validators.pattern(/^https?:\/\/\S+$/i),
      Validators.maxLength(APPLICATION_DEVELOPMENT_CODE_MAX_LENGTH),
    ]),
    standardAdaption: formBuilder.control<DevelopmentStandardAdaption | null>(
      null,
      Validators.required,
    ),
    revisionDate: formBuilder.control<Date | null>(null, Validators.required),
    observation: formBuilder.nonNullable.control(''),
  });
}

export function createApplicationProviderForm(
  formBuilder: FormBuilder,
): ApplicationProviderFormGroup {
  return formBuilder.group(
    {
      companyName: formBuilder.nonNullable.control('', REQUIRED_TEXT_VALIDATORS),
      roleId: formBuilder.control<number | null>(null, Validators.required),
      startDate: formBuilder.control<Date | null>(null),
      expireDate: formBuilder.control<Date | null>(null),
    },
    { validators: dateRangeValidator('startDate', 'expireDate') },
  );
}

export function createApplicationTechnologyForm(
  formBuilder: FormBuilder,
): ApplicationTechnologyFormGroup {
  return formBuilder.group({
    layerId: formBuilder.control<number | null>(null, Validators.required),
    technologyId: formBuilder.control<number | null>(null, Validators.required),
    version: formBuilder.nonNullable.control('', REQUIRED_TEXT_VALIDATORS),
    architecture: formBuilder.nonNullable.control('', REQUIRED_TEXT_VALIDATORS),
  });
}

export function dateRangeValidator(startKey: string, endKey: string): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const start = control.get(startKey)?.value as Date | null;
    const end = control.get(endKey)?.value as Date | null;

    return !start || !end || start.getTime() <= end.getTime()
      ? null
      : { [APPLICATION_DEVELOPMENT_DATE_RANGE_ERROR]: true };
  };
}

export function parseLocalDateTime(value: string | null | undefined): Date | null {
  const match = value?.match(/^(\d{4})-(\d{2})-(\d{2})/);
  if (!match) return null;

  const year = Number(match[1]);
  const month = Number(match[2]);
  const day = Number(match[3]);
  const result = new Date(year, month - 1, day);

  return Number.isNaN(result.getTime()) ? null : result;
}

export function formatLocalDateTime(value: Date | null): string | null {
  if (!value || Number.isNaN(value.getTime())) return null;

  const year = String(value.getFullYear()).padStart(4, '0');
  const month = String(value.getMonth() + 1).padStart(2, '0');
  const day = String(value.getDate()).padStart(2, '0');

  return `${year}-${month}-${day}T00:00:00`;
}
