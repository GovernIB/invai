import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';

export interface SecurityResourceFormControls {
  name: FormControl<string>;
  nameEs: FormControl<string>;
}

export type SecurityResourceFormGroup = FormGroup<SecurityResourceFormControls>;

export function createSecurityResourceForm(
  formBuilder: FormBuilder,
  bilingual: boolean,
): SecurityResourceFormGroup {
  return formBuilder.nonNullable.group({
    name: ['', [Validators.required, Validators.pattern(/\S/), Validators.maxLength(150)]],
    nameEs: [
      '',
      bilingual
        ? [Validators.required, Validators.pattern(/\S/), Validators.maxLength(100)]
        : [],
    ],
  });
}

export interface SecurityResourceFiltersFormControls {
  name: FormControl<string | null>;
  nameEs: FormControl<string | null>;
  status: FormControl<SoftDeleteStatus | null>;
}

export type SecurityResourceFiltersFormGroup = FormGroup<SecurityResourceFiltersFormControls>;

export function createSecurityResourceFiltersForm(
  formBuilder: FormBuilder,
): SecurityResourceFiltersFormGroup {
  return formBuilder.group({
    name: formBuilder.control<string | null>(null),
    nameEs: formBuilder.control<string | null>(null),
    status: formBuilder.control<SoftDeleteStatus | null>(SoftDeleteStatus.ACTIVE, {
      initialValueIsDefault: true,
    }),
  });
}
