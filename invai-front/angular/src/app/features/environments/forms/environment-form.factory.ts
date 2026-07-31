import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';

export interface EnvironmentFormControls {
  code: FormControl<string>;
  name: FormControl<string>;
  nameEs: FormControl<string>;
}

export type EnvironmentFormGroup = FormGroup<EnvironmentFormControls>;

export function createEnvironmentForm(formBuilder: FormBuilder): EnvironmentFormGroup {
  return formBuilder.nonNullable.group({
    code: [
      '',
      [Validators.required, Validators.pattern(/\S/), Validators.maxLength(50)],
    ],
    name: [
      '',
      [Validators.required, Validators.pattern(/\S/), Validators.maxLength(100)],
    ],
    nameEs: [
      '',
      [Validators.required, Validators.pattern(/\S/), Validators.maxLength(100)],
    ],
  });
}
