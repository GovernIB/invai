import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';

export interface RoleFormControls {
  name: FormControl<string>;
  nameEs: FormControl<string>;
}

export type RoleFormGroup = FormGroup<RoleFormControls>;

export function createRoleForm(formBuilder: FormBuilder): RoleFormGroup {
  return formBuilder.nonNullable.group({
    name: ['', [Validators.required, Validators.pattern(/\S/), Validators.maxLength(100)]],
    nameEs: ['', Validators.maxLength(100)],
  });
}
