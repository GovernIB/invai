import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';

export interface DatabaseVendorFormControls {
  name: FormControl<string>;
  defaultPort: FormControl<number | null>;
}

export type DatabaseVendorFormGroup = FormGroup<DatabaseVendorFormControls>;

export function createDatabaseVendorForm(
  formBuilder: FormBuilder,
): DatabaseVendorFormGroup {
  return formBuilder.group({
    name: formBuilder.nonNullable.control('', [
      Validators.required,
      Validators.pattern(/\S/),
      Validators.maxLength(100),
    ]),
    defaultPort: formBuilder.control<number | null>(null, [
      Validators.required,
      Validators.min(1),
      Validators.max(65535),
    ]),
  });
}
