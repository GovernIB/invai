import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';

export interface DatabaseVendorFiltersFormControls {
  name: FormControl<string | null>;
  defaultPort: FormControl<number | null>;
  status: FormControl<SoftDeleteStatus | null>;
}

export type DatabaseVendorFiltersFormGroup =
  FormGroup<DatabaseVendorFiltersFormControls>;

export function createDatabaseVendorFiltersForm(
  formBuilder: FormBuilder,
): DatabaseVendorFiltersFormGroup {
  return formBuilder.group({
    name: formBuilder.control<string | null>(null),
    defaultPort: formBuilder.control<number | null>(null),
    status: formBuilder.control<SoftDeleteStatus | null>(
      SoftDeleteStatus.ACTIVE,
      { initialValueIsDefault: true },
    ),
  });
}
