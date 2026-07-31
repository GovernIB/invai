import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';

export interface FieldFormControls {
  name: FormControl<string>;
  nameEs: FormControl<string>;
}

export type FieldFormGroup = FormGroup<FieldFormControls>;

export function createFieldForm(formBuilder: FormBuilder): FieldFormGroup {
  return formBuilder.group({
    name: formBuilder.nonNullable.control('', [
      Validators.required,
      Validators.pattern(/\S/),
      Validators.maxLength(50),
    ]),
    nameEs: formBuilder.nonNullable.control('', [
      Validators.required,
      Validators.pattern(/\S/),
      Validators.maxLength(100),
    ]),
  });
}
