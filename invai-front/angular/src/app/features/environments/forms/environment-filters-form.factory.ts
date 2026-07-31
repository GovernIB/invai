import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';

export interface EnvironmentFiltersFormControls {
  code: FormControl<string | null>;
  name: FormControl<string | null>;
  nameEs: FormControl<string | null>;
  status: FormControl<SoftDeleteStatus | null>;
}

export type EnvironmentFiltersFormGroup = FormGroup<EnvironmentFiltersFormControls>;

export function createEnvironmentFiltersForm(
  formBuilder: FormBuilder,
): EnvironmentFiltersFormGroup {
  return formBuilder.group({
    code: formBuilder.control<string | null>(null),
    name: formBuilder.control<string | null>(null),
    nameEs: formBuilder.control<string | null>(null),
    status: formBuilder.control<SoftDeleteStatus | null>(SoftDeleteStatus.ACTIVE, {
      initialValueIsDefault: true,
    }),
  });
}
