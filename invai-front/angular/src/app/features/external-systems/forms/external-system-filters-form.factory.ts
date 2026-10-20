import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';

export interface ExternalSystemFiltersFormControls {
  name: FormControl<string | null>;
  companyId: FormControl<number | null>;
  status: FormControl<SoftDeleteStatus | null>;
}

export type ExternalSystemFiltersFormGroup = FormGroup<ExternalSystemFiltersFormControls>;

export function createExternalSystemFiltersForm(
  formBuilder: FormBuilder,
): ExternalSystemFiltersFormGroup {
  return formBuilder.group({
    name: formBuilder.control<string | null>(null),
    companyId: formBuilder.control<number | null>(null),
    status: formBuilder.control<SoftDeleteStatus | null>(SoftDeleteStatus.ACTIVE, {
      initialValueIsDefault: true,
    }),
  });
}
