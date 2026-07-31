import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';

export interface DatabaseFiltersFormControls {
  serverId: FormControl<number | null>;
  service: FormControl<string | null>;
  databaseTypeId: FormControl<number | null>;
  status: FormControl<SoftDeleteStatus | null>;
}

export type DatabaseFiltersFormGroup = FormGroup<DatabaseFiltersFormControls>;

export function createDatabaseFiltersForm(
  formBuilder: FormBuilder,
): DatabaseFiltersFormGroup {
  return formBuilder.group({
    serverId: formBuilder.control<number | null>(null),
    service: formBuilder.control<string | null>(null),
    databaseTypeId: formBuilder.control<number | null>(null),
    status: formBuilder.control<SoftDeleteStatus | null>(
      SoftDeleteStatus.ACTIVE,
      { initialValueIsDefault: true },
    ),
  });
}
