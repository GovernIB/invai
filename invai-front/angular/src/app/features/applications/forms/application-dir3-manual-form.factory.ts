import { FormBuilder, FormControl, FormGroup, ValidatorFn } from '@angular/forms';

export type ApplicationDir3ManualForm = FormGroup<{
  validate: FormControl<boolean>;
  reason: FormControl<string>;
}>;

const manualReasonRequired: ValidatorFn = (form) =>
  form.get('validate')?.value && !form.get('reason')?.value?.trim()
    ? { manualReasonRequired: true }
    : null;

export function createApplicationDir3ManualForm(builder: FormBuilder): ApplicationDir3ManualForm {
  return builder.nonNullable.group(
    {
      validate: builder.nonNullable.control<boolean>(false),
      reason: builder.nonNullable.control(''),
    },
    { validators: manualReasonRequired },
  );
}
