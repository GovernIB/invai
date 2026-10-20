import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';

export interface ExternalSystemFormControls {
  name: FormControl<string>;
  companyId: FormControl<number | null>;
}

export type ExternalSystemFormGroup = FormGroup<ExternalSystemFormControls>;

export function createExternalSystemForm(formBuilder: FormBuilder): ExternalSystemFormGroup {
  return formBuilder.group({
    name: formBuilder.nonNullable.control('', [
      Validators.required,
      Validators.pattern(/\S/),
      Validators.maxLength(150),
    ]),
    companyId: formBuilder.control<number | null>(null, Validators.required),
  });
}
