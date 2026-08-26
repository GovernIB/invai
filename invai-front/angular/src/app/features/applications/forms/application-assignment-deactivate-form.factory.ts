import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';

export interface ApplicationAssignmentDeactivateFormControls {
  observation: FormControl<string>;
}

export type ApplicationAssignmentDeactivateFormGroup =
  FormGroup<ApplicationAssignmentDeactivateFormControls>;

export function createApplicationAssignmentDeactivateForm(
  formBuilder: FormBuilder,
): ApplicationAssignmentDeactivateFormGroup {
  return formBuilder.nonNullable.group({
    observation: ['', [Validators.required, Validators.pattern(/\S/)]],
  });
}
