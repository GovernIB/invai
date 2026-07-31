import { FormBuilder, FormControl, FormGroup } from '@angular/forms';

import { ApplicationInfrastructureStatus } from '../applications.model';

export interface ApplicationServerFiltersFormControls {
  environment: FormControl<number | null>;
  server: FormControl<number | null>;
  instance: FormControl<string | null>;
  port: FormControl<number | null>;
  version: FormControl<string | null>;
  status: FormControl<ApplicationInfrastructureStatus | null>;
  observations: FormControl<string | null>;
}

export interface ApplicationDatabaseFiltersFormControls {
  environment: FormControl<number | null>;
  server: FormControl<string | null>;
  version: FormControl<string | null>;
  database: FormControl<number | null>;
  service: FormControl<string | null>;
  port: FormControl<number | null>;
  type: FormControl<string | null>;
  status: FormControl<ApplicationInfrastructureStatus | null>;
  observations: FormControl<string | null>;
}

export type ApplicationServerFiltersFormGroup = FormGroup<ApplicationServerFiltersFormControls>;
export type ApplicationDatabaseFiltersFormGroup = FormGroup<ApplicationDatabaseFiltersFormControls>;

export function createApplicationServerFiltersForm(
  formBuilder: FormBuilder,
): ApplicationServerFiltersFormGroup {
  return formBuilder.group({
    environment: formBuilder.control<number | null>(null),
    server: formBuilder.control<number | null>(null),
    instance: formBuilder.control<string | null>(null),
    port: formBuilder.control<number | null>(null),
    version: formBuilder.control<string | null>(null),
    status: formBuilder.control<ApplicationInfrastructureStatus | null>(
      ApplicationInfrastructureStatus.ACTIVE,
      { initialValueIsDefault: true },
    ),
    observations: formBuilder.control<string | null>(null),
  });
}

export function createApplicationDatabaseFiltersForm(
  formBuilder: FormBuilder,
): ApplicationDatabaseFiltersFormGroup {
  return formBuilder.group({
    environment: formBuilder.control<number | null>(null),
    server: formBuilder.control<string | null>(null),
    version: formBuilder.control<string | null>(null),
    database: formBuilder.control<number | null>(null),
    service: formBuilder.control<string | null>(null),
    port: formBuilder.control<number | null>(null),
    type: formBuilder.control<string | null>(null),
    status: formBuilder.control<ApplicationInfrastructureStatus | null>(
      ApplicationInfrastructureStatus.ACTIVE,
      { initialValueIsDefault: true },
    ),
    observations: formBuilder.control<string | null>(null),
  });
}
