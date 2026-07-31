import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';

export interface PhysicalServerFiltersFormControls {
  name: FormControl<string | null>;
  environmentId: FormControl<number | null>;
  status: FormControl<SoftDeleteStatus | null>;
}

export type PhysicalServerFiltersFormGroup =
  FormGroup<PhysicalServerFiltersFormControls>;

export function createPhysicalServerFiltersForm(
  formBuilder: FormBuilder,
): PhysicalServerFiltersFormGroup {
  return formBuilder.group({
    name: formBuilder.control<string | null>(null),
    environmentId: formBuilder.control<number | null>(null),
    status: formBuilder.control<SoftDeleteStatus | null>(
      SoftDeleteStatus.ACTIVE,
      { initialValueIsDefault: true },
    ),
  });
}
