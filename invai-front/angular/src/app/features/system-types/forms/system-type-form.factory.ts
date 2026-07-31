import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';

export interface SystemTypeFormControls {
  name: FormControl<string>;
  nameEs: FormControl<string>;
}

export type SystemTypeFormGroup = FormGroup<SystemTypeFormControls>;

export function createSystemTypeForm(formBuilder: FormBuilder): SystemTypeFormGroup {
  return formBuilder.group({
    name: formBuilder.nonNullable.control('', [
      Validators.required,
      Validators.pattern(/\S/),
      Validators.maxLength(100),
    ]),
    nameEs: formBuilder.nonNullable.control('', [
      Validators.required,
      Validators.pattern(/\S/),
      Validators.maxLength(100),
    ]),
  });
}
