import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';

export interface ServerFiltersFormControls {
  serverId: FormControl<number | null>;
  instance: FormControl<string | null>;
  version: FormControl<string | null>;
  status: FormControl<SoftDeleteStatus | null>;
}

export type ServerFiltersFormGroup = FormGroup<ServerFiltersFormControls>;

export function createServerFiltersForm(
  formBuilder: FormBuilder,
): ServerFiltersFormGroup {
  return formBuilder.group({
    serverId: formBuilder.control<number | null>(null),
    instance: formBuilder.control<string | null>(null),
    version: formBuilder.control<string | null>(null),
    status: formBuilder.control<SoftDeleteStatus | null>(
      SoftDeleteStatus.ACTIVE,
      { initialValueIsDefault: true },
    ),
  });
}
