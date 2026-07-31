import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';

export interface CategoryFormControls {
  name: FormControl<string>;
  nameEs: FormControl<string>;
}

export type CategoryFormGroup = FormGroup<CategoryFormControls>;

export function createCategoryForm(formBuilder: FormBuilder): CategoryFormGroup {
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
