import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';

import {
  DatabaseVendorCatalogOption,
  ServerCatalogOption,
} from '../systems.model';

const REQUIRED_TEXT = [Validators.required, Validators.pattern(/\S/)];
const PORT_VALIDATORS = [
  Validators.required,
  Validators.min(1),
  Validators.max(65535),
];

export interface SystemHostFormControls {
  server: FormControl<ServerCatalogOption | null>;
  instance: FormControl<string>;
  port: FormControl<number | null>;
  version: FormControl<string>;
  description: FormControl<string>;
}

export interface DatabaseHostFormControls {
  server: FormControl<ServerCatalogOption | null>;
  service: FormControl<string>;
  port: FormControl<number | null>;
  databaseType: FormControl<DatabaseVendorCatalogOption | null>;
  description: FormControl<string>;
}

export type SystemHostFormGroup = FormGroup<SystemHostFormControls>;
export type DatabaseHostFormGroup = FormGroup<DatabaseHostFormControls>;

export function createSystemHostForm(
  formBuilder: FormBuilder,
): SystemHostFormGroup {
  return formBuilder.group({
    server: formBuilder.control<ServerCatalogOption | null>(
      null,
      Validators.required,
    ),
    instance: formBuilder.nonNullable.control('', [
      ...REQUIRED_TEXT,
      Validators.maxLength(50),
    ]),
    port: formBuilder.control<number | null>(null, PORT_VALIDATORS),
    version: formBuilder.nonNullable.control('', [
      ...REQUIRED_TEXT,
      Validators.maxLength(20),
    ]),
    description: formBuilder.nonNullable.control(''),
  });
}

export function createDatabaseHostForm(
  formBuilder: FormBuilder,
): DatabaseHostFormGroup {
  return formBuilder.group({
    server: formBuilder.control<ServerCatalogOption | null>(
      null,
      Validators.required,
    ),
    service: formBuilder.nonNullable.control('', [
      ...REQUIRED_TEXT,
      Validators.maxLength(255),
    ]),
    port: formBuilder.control<number | null>(null, PORT_VALIDATORS),
    databaseType: formBuilder.control<DatabaseVendorCatalogOption | null>(
      null,
      Validators.required,
    ),
    description: formBuilder.nonNullable.control(''),
  });
}
