import {
  AbstractControl,
  FormBuilder,
  FormControl,
  FormGroup,
  ValidationErrors,
  Validators,
} from '@angular/forms';

import {
  APPLICATION_EMAIL_MAX_RECIPIENTS,
  APPLICATION_EMAIL_MESSAGE_MAX_LENGTH,
  APPLICATION_EMAIL_RECIPIENT_MAX_LENGTH,
  APPLICATION_EMAIL_SUBJECT_MAX_LENGTH,
} from '../pages/detail/sections/documentation/application-documentation.constants';

// The address pattern of the back-end: no spaces, one @ and a dot after it.
export const APPLICATION_EMAIL_ADDRESS_PATTERN = /^[^@\s]+@[^@\s]+\.[^@\s]+$/;

// Recipients, subject and message of an e-mail with documents of an application. The documents
// to attach are chosen in a table, outside the form.
export interface ApplicationDocumentationEmailFormControls {
  recipients: FormControl<string[]>;
  subject: FormControl<string>;
  message: FormControl<string>;
}

export type ApplicationDocumentationEmailFormGroup =
  FormGroup<ApplicationDocumentationEmailFormControls>;

// The addresses as the back-end keeps them: trimmed, in lower case and without repetitions.
export function normalizeEmailRecipients(values: readonly string[]): string[] {
  return [...new Set(values.map((value) => value.trim().toLowerCase()).filter(Boolean))];
}

// `required` without addresses, `invalidRecipients` with the ones the back-end would refuse, and
// `maxRecipients` above the 20 it sends to at once. The addresses are checked as they are sent.
export function emailRecipientsValidator(
  control: AbstractControl<string[] | null>,
): ValidationErrors | null {
  const values = normalizeEmailRecipients(control.value ?? []);
  if (!values.length) return { required: true };

  const invalid = values.filter(
    (value) =>
      value.length > APPLICATION_EMAIL_RECIPIENT_MAX_LENGTH ||
      !APPLICATION_EMAIL_ADDRESS_PATTERN.test(value),
  );
  if (invalid.length) return { invalidRecipients: { values: invalid } };

  return values.length > APPLICATION_EMAIL_MAX_RECIPIENTS
    ? { maxRecipients: { max: APPLICATION_EMAIL_MAX_RECIPIENTS, actual: values.length } }
    : null;
}

// The subject goes in a mail header, which a line break would split.
export function noLineBreaksValidator(control: AbstractControl<string | null>): ValidationErrors | null {
  return /[\r\n]/.test(control.value ?? '') ? { lineBreak: true } : null;
}

export function createApplicationDocumentationEmailForm(
  fb: FormBuilder,
  subject = '',
): ApplicationDocumentationEmailFormGroup {
  return fb.group({
    recipients: new FormControl<string[]>([], {
      nonNullable: true,
      validators: emailRecipientsValidator,
    }),
    subject: fb.nonNullable.control(subject, [
      Validators.required,
      Validators.pattern(/\S/),
      Validators.maxLength(APPLICATION_EMAIL_SUBJECT_MAX_LENGTH),
      noLineBreaksValidator,
    ]),
    message: fb.nonNullable.control('', Validators.maxLength(APPLICATION_EMAIL_MESSAGE_MAX_LENGTH)),
  });
}
