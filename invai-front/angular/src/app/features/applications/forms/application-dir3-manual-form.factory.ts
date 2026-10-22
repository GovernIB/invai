import { FormBuilder, FormControl, FormGroup, ValidatorFn } from '@angular/forms';
import { TEXT_FIELD_MAX_BYTES } from '@shared/constants/text-field.constants';
import { maxUtf8BytesValidator } from '@shared/utils/text-length.utils';

export type ApplicationDir3ManualForm = FormGroup<{
  validate: FormControl<boolean>;
  reason: FormControl<string>;
}>;

const manualReasonRequired: ValidatorFn = (form) =>
  form.get('validate')?.value && !form.get('reason')?.value?.trim()
    ? { manualReasonRequired: true }
    : null;

const reasonMaxBytes = maxUtf8BytesValidator(TEXT_FIELD_MAX_BYTES);

// The reason is only shown, and only sent, while the manual validation is accepted.
const manualReasonMaxBytes: ValidatorFn = (form) => {
  const reason = form.get('reason');
  return form.get('validate')?.value && reason ? reasonMaxBytes(reason) : null;
};

export function createApplicationDir3ManualForm(builder: FormBuilder): ApplicationDir3ManualForm {
  return builder.nonNullable.group(
    {
      validate: builder.nonNullable.control<boolean>(false),
      reason: builder.nonNullable.control(''),
    },
    { validators: [manualReasonRequired, manualReasonMaxBytes] },
  );
}
