import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';

export interface AccessibilityResourceFormControls {
  name: FormControl<string>;
  nameEs: FormControl<string>;
}

export type AccessibilityResourceFormGroup = FormGroup<AccessibilityResourceFormControls>;

export function createAccessibilityResourceForm(
  formBuilder: FormBuilder,
  bilingual: boolean,
): AccessibilityResourceFormGroup {
  return formBuilder.nonNullable.group({
    name: ['', [Validators.required, Validators.pattern(/\S/), Validators.maxLength(150)]],
    nameEs: [
      '',
      bilingual ? [Validators.required, Validators.pattern(/\S/), Validators.maxLength(100)] : [],
    ],
  });
}

export interface AccessibilityResourceFiltersFormControls {
  name: FormControl<string | null>;
  nameEs: FormControl<string | null>;
  status: FormControl<SoftDeleteStatus | null>;
}

export type AccessibilityResourceFiltersFormGroup =
  FormGroup<AccessibilityResourceFiltersFormControls>;

export function createAccessibilityResourceFiltersForm(
  formBuilder: FormBuilder,
): AccessibilityResourceFiltersFormGroup {
  return formBuilder.group({
    name: formBuilder.control<string | null>(null),
    nameEs: formBuilder.control<string | null>(null),
    status: formBuilder.control<SoftDeleteStatus | null>(SoftDeleteStatus.ACTIVE, {
      initialValueIsDefault: true,
    }),
  });
}
