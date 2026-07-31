import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';

export interface TechnologyFiltersFormControls {
  name: FormControl<string | null>;
  layerId: FormControl<number | null>;
  status: FormControl<SoftDeleteStatus | null>;
}

export type TechnologyFiltersFormGroup = FormGroup<TechnologyFiltersFormControls>;

export function createTechnologyFiltersForm(
  formBuilder: FormBuilder,
): TechnologyFiltersFormGroup {
  return formBuilder.group({
    name: formBuilder.control<string | null>(null),
    layerId: formBuilder.control<number | null>(null),
    status: formBuilder.control<SoftDeleteStatus | null>(SoftDeleteStatus.ACTIVE, {
      initialValueIsDefault: true,
    }),
  });
}
