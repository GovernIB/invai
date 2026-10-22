import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';

export interface DocumentCategoryFiltersFormControls {
  name: FormControl<string | null>;
  status: FormControl<SoftDeleteStatus | null>;
}

export type DocumentCategoryFiltersFormGroup = FormGroup<DocumentCategoryFiltersFormControls>;

export function createDocumentCategoryFiltersForm(
  formBuilder: FormBuilder,
): DocumentCategoryFiltersFormGroup {
  return formBuilder.group({
    name: formBuilder.control<string | null>(null),
    status: formBuilder.control<SoftDeleteStatus | null>(SoftDeleteStatus.ACTIVE, {
      initialValueIsDefault: true,
    }),
  });
}
