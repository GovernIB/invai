import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';

export interface LayerFormControls {
  name: FormControl<string>;
}

export type LayerFormGroup = FormGroup<LayerFormControls>;

export function createLayerForm(formBuilder: FormBuilder): LayerFormGroup {
  return formBuilder.nonNullable.group({
    name: ['', [Validators.required, Validators.pattern(/\S/), Validators.maxLength(100)]],
  });
}
