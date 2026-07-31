import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';

export interface FieldFiltersFormControls {
  name: FormControl<string | null>;
  nameEs: FormControl<string | null>;
  status: FormControl<SoftDeleteStatus | null>;
}

export type FieldFiltersFormGroup = FormGroup<FieldFiltersFormControls>;

export function createFieldFiltersForm(formBuilder: FormBuilder): FieldFiltersFormGroup {
  return formBuilder.group({
    name: formBuilder.control<string | null>(null),
    nameEs: formBuilder.control<string | null>(null),
    status: formBuilder.control<SoftDeleteStatus | null>(SoftDeleteStatus.ACTIVE, {
      initialValueIsDefault: true,
    }),
  });
}
