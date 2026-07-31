import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';

import { CommissionType } from '../commissions.model';

export interface CommissionFormControls {
  name: FormControl<string>;
  nameEs: FormControl<string>;
  expedientNumber: FormControl<string>;
  approvalDate: FormControl<Date | null>;
  commissionType: FormControl<CommissionType | null>;
}

export type CommissionFormGroup = FormGroup<CommissionFormControls>;

export function createCommissionForm(formBuilder: FormBuilder): CommissionFormGroup {
  return formBuilder.group({
    name: formBuilder.nonNullable.control('', [
      Validators.required,
      Validators.pattern(/\S/),
      Validators.maxLength(100),
    ]),
    nameEs: formBuilder.nonNullable.control('', [
      Validators.required,
      Validators.pattern(/\S/),
      Validators.maxLength(100),
    ]),
    expedientNumber: formBuilder.nonNullable.control('', [
      Validators.required,
      Validators.pattern(/\S/),
      Validators.maxLength(50),
    ]),
    approvalDate: formBuilder.control<Date | null>(null, Validators.required),
    commissionType: formBuilder.control<CommissionType | null>(null, Validators.required),
  });
}
