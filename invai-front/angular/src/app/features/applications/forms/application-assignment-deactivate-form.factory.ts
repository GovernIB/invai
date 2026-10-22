import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { TEXT_FIELD_MAX_BYTES } from '@shared/constants/text-field.constants';
import { maxUtf8BytesValidator } from '@shared/utils/text-length.utils';

export interface ApplicationAssignmentDeactivateFormControls {
  observation: FormControl<string>;
}

export type ApplicationAssignmentDeactivateFormGroup =
  FormGroup<ApplicationAssignmentDeactivateFormControls>;

export function createApplicationAssignmentDeactivateForm(
  formBuilder: FormBuilder,
): ApplicationAssignmentDeactivateFormGroup {
  return formBuilder.nonNullable.group({
    observation: [
      '',
      [Validators.required, Validators.pattern(/\S/), maxUtf8BytesValidator(TEXT_FIELD_MAX_BYTES)],
    ],
  });
}
