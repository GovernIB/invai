import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';

export interface TechnologyFormControls {
  name: FormControl<string>;
  layerId: FormControl<number | null>;
}

export type TechnologyFormGroup = FormGroup<TechnologyFormControls>;

export function createTechnologyForm(formBuilder: FormBuilder): TechnologyFormGroup {
  return formBuilder.group({
    name: formBuilder.nonNullable.control('', [
      Validators.required,
      Validators.pattern(/\S/),
      Validators.maxLength(100),
    ]),
    layerId: formBuilder.control<number | null>(null, Validators.required),
  });
}
