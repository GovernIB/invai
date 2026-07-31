import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { EnvironmentCatalogOption } from '@features/environments/environments.model';

export interface PhysicalServerFormControls {
  name: FormControl<string>;
  environment: FormControl<EnvironmentCatalogOption | null>;
}

export type PhysicalServerFormGroup = FormGroup<PhysicalServerFormControls>;

export function createPhysicalServerForm(
  formBuilder: FormBuilder,
): PhysicalServerFormGroup {
  return formBuilder.group({
    name: formBuilder.nonNullable.control('', [
      Validators.required,
      Validators.pattern(/\S/),
      Validators.maxLength(255),
    ]),
    environment: formBuilder.control<EnvironmentCatalogOption | null>(
      null,
      Validators.required,
    ),
  });
}
