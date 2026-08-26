import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { CommissionType } from '@features/commissions/commissions.model';
import { ResponsiblePersonOption } from '@features/maintenances/responsibles/responsibles.model';

import { APPLICATION_CONSELLERIA_MOCK_VALUE } from '../applications.constants';
import { ApplicationStatus } from '../applications.model';

export const APPLICATION_CODE_MIN_LENGTH = 4;
export const APPLICATION_CODE_MAX_LENGTH = 10;
export const APPLICATION_PREFIX_MAX_LENGTH = 3;

export interface ApplicationCommonFormControls {
  application: FormControl<string>;
  category: FormControl<number | null>;
  informationSystem: FormControl<number | null>;
  scope: FormControl<number | null>;
  commission: FormControl<number | null>;
  commissionExpedientNumber: FormControl<string>;
  commissionApprovalDate: FormControl<string>;
  commissionType: FormControl<CommissionType | null>;
  prefix: FormControl<string>;
  administrativeUnit: FormControl<number | null>;
  conselleria: FormControl<string>;
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
  status: FormControl<ApplicationStatus | null>;
  description: FormControl<string | null>;
  responsible: FormControl<ResponsiblePersonOption | null>;
  database: FormControl<number | null>;
  server: FormControl<number | null>;
  environment: FormControl<number | null>;
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
  commissionExpedientNumber: string;
  commissionApprovalDate: string;
  commissionType: CommissionType | null;
  prefix: string;
  administrativeUnit: number | null;
  conselleria: string;
  description: string;
  creationDate: string;
  modificationDate: string;
  withdrawalDate: string;
}

export function createApplicationCreateForm(formBuilder: FormBuilder): ApplicationCreateFormGroup {
  return formBuilder.nonNullable.group({
    ...createCommonControls(formBuilder),
    code: [
      '',
      [
        Validators.required,
        Validators.minLength(APPLICATION_CODE_MIN_LENGTH),
        Validators.maxLength(APPLICATION_CODE_MAX_LENGTH),
      ],
    ],
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
    status: formBuilder.control<ApplicationStatus | null>(ApplicationStatus.ACTIVE, {
      initialValueIsDefault: true,
    }),
    description: formBuilder.control<string | null>(null),
    responsible: formBuilder.control<ResponsiblePersonOption | null>(null),
    database: formBuilder.control<number | null>(null),
    server: formBuilder.control<number | null>(null),
    environment: formBuilder.control<number | null>(null),
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
    commissionExpedientNumber: formBuilder.nonNullable.control({
      value: '',
      disabled: true,
    }),
    commissionApprovalDate: formBuilder.nonNullable.control({ value: '', disabled: true }),
    commissionType: formBuilder.control<CommissionType | null>({ value: null, disabled: true }),
    prefix: formBuilder.nonNullable.control('', [
      Validators.required,
      Validators.maxLength(APPLICATION_PREFIX_MAX_LENGTH),
    ]),
    administrativeUnit: formBuilder.control<number | null>(null, Validators.required),
    conselleria: formBuilder.nonNullable.control({
      value: APPLICATION_CONSELLERIA_MOCK_VALUE,
      disabled: true,
    }),
    description: formBuilder.nonNullable.control(''),
  };
}
