import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';

import {
  ApplicationDatabaseCatalogRow,
  ApplicationSystemCatalogRow,
} from '../applications.model';

export interface ApplicationSystemRelationFormControls {
  system: FormControl<ApplicationSystemCatalogRow | null>;
}

export interface ApplicationDatabaseRelationFormControls {
  database: FormControl<ApplicationDatabaseCatalogRow | null>;
}

export type ApplicationSystemRelationFormGroup =
  FormGroup<ApplicationSystemRelationFormControls>;
export type ApplicationDatabaseRelationFormGroup =
  FormGroup<ApplicationDatabaseRelationFormControls>;

export function createApplicationSystemRelationForm(
  formBuilder: FormBuilder,
): ApplicationSystemRelationFormGroup {
  return formBuilder.group({
    system: formBuilder.control<ApplicationSystemCatalogRow | null>(
      null,
      Validators.required,
    ),
  });
}

export function createApplicationDatabaseRelationForm(
  formBuilder: FormBuilder,
): ApplicationDatabaseRelationFormGroup {
  return formBuilder.group({
    database: formBuilder.control<ApplicationDatabaseCatalogRow | null>(
      null,
      Validators.required,
    ),
  });
}
