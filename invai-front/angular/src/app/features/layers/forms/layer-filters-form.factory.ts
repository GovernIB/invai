import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';

export interface LayerFiltersFormControls {
  name: FormControl<string | null>;
  status: FormControl<SoftDeleteStatus | null>;
}

export type LayerFiltersFormGroup = FormGroup<LayerFiltersFormControls>;

export function createLayerFiltersForm(formBuilder: FormBuilder): LayerFiltersFormGroup {
  return formBuilder.group({
    name: formBuilder.control<string | null>(null),
    status: formBuilder.control<SoftDeleteStatus | null>(SoftDeleteStatus.ACTIVE, {
      initialValueIsDefault: true,
    }),
  });
}
