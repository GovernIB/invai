import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';

export interface CategoryFiltersFormControls {
  name: FormControl<string | null>;
  nameEs: FormControl<string | null>;
  status: FormControl<SoftDeleteStatus | null>;
}

export type CategoryFiltersFormGroup = FormGroup<CategoryFiltersFormControls>;

export function createCategoryFiltersForm(formBuilder: FormBuilder): CategoryFiltersFormGroup {
  return formBuilder.group({
    name: formBuilder.control<string | null>(null),
    nameEs: formBuilder.control<string | null>(null),
    status: formBuilder.control<SoftDeleteStatus | null>(SoftDeleteStatus.ACTIVE, {
      initialValueIsDefault: true,
    }),
  });
}
