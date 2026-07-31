import {
  AbstractControl,
  FormBuilder,
  FormControl,
  FormGroup,
  ValidationErrors,
  ValidatorFn,
} from '@angular/forms';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';

import { CommissionType } from '../commissions.model';

export const COMMISSION_DATE_RANGE_ERROR = 'approvalDateRange';

export interface CommissionFiltersFormControls {
  name: FormControl<string | null>;
  nameEs: FormControl<string | null>;
  expedientNumber: FormControl<string | null>;
  approvalDateFrom: FormControl<Date | null>;
  approvalDateTo: FormControl<Date | null>;
  commissionType: FormControl<CommissionType | null>;
  status: FormControl<SoftDeleteStatus | null>;
}

export type CommissionFiltersFormGroup = FormGroup<CommissionFiltersFormControls>;

export function createCommissionFiltersForm(
  formBuilder: FormBuilder,
): CommissionFiltersFormGroup {
  return formBuilder.group(
    {
      name: formBuilder.control<string | null>(null),
      nameEs: formBuilder.control<string | null>(null),
      expedientNumber: formBuilder.control<string | null>(null),
      approvalDateFrom: formBuilder.control<Date | null>(null),
      approvalDateTo: formBuilder.control<Date | null>(null),
      commissionType: formBuilder.control<CommissionType | null>(null),
      status: formBuilder.control<SoftDeleteStatus | null>(SoftDeleteStatus.ACTIVE, {
        initialValueIsDefault: true,
      }),
    },
    { validators: commissionApprovalDateRangeValidator },
  );
}

export const commissionApprovalDateRangeValidator: ValidatorFn = (
  control: AbstractControl,
): ValidationErrors | null => {
  const approvalDateFrom = control.get('approvalDateFrom')?.value as Date | null;
  const approvalDateTo = control.get('approvalDateTo')?.value as Date | null;

  if (
    !approvalDateFrom ||
    !approvalDateTo ||
    approvalDateFrom.getTime() <= approvalDateTo.getTime()
  ) {
    return null;
  }

  return { [COMMISSION_DATE_RANGE_ERROR]: true };
};
