import { FormBuilder } from '@angular/forms';

import {
  createApplicationDocumentationEmailForm,
  normalizeEmailRecipients,
} from './application-documentation-email-form.factory';

describe('createApplicationDocumentationEmailForm', () => {
  const create = (subject?: string) => createApplicationDocumentationEmailForm(new FormBuilder(), subject);

  it('starts without recipients or message and with the proposed subject', () => {
    const form = create('Documentació de l\'aplicació INVAI');

    expect(form.getRawValue()).toEqual({
      recipients: [],
      subject: 'Documentació de l\'aplicació INVAI',
      message: '',
    });
    expect(form.controls.recipients.hasError('required')).toBe(true);
    expect(form.controls.subject.valid).toBe(true);
    expect(form.controls.message.valid).toBe(true);
  });

  it('accepts up to 20 well-formed addresses', () => {
    const recipients = create().controls.recipients;

    recipients.setValue(['ana@example.org', 'pere.serra@caib.es']);
    expect(recipients.valid).toBe(true);

    recipients.setValue(Array.from({ length: 20 }, (_, index) => `user${index}@example.org`));
    expect(recipients.valid).toBe(true);
  });

  it('names the addresses the back-end would refuse', () => {
    const recipients = create().controls.recipients;

    recipients.setValue(['ana@example.org', 'pere', 'a b@example.org', `${'a'.repeat(310)}@example.org`]);

    expect(recipients.errors).toEqual({
      invalidRecipients: { values: ['pere', 'a b@example.org', `${'a'.repeat(310)}@example.org`] },
    });
  });

  it('checks the addresses as they are sent: trimmed, in lower case and once', () => {
    const recipients = create().controls.recipients;

    recipients.setValue([' Ana@Example.org ', 'ana@example.org', '   ']);
    expect(recipients.valid).toBe(true);

    recipients.setValue(Array.from({ length: 21 }, () => 'ana@example.org'));
    expect(recipients.valid).toBe(true);
  });

  it('refuses more than 20 addresses', () => {
    const recipients = create().controls.recipients;

    recipients.setValue(Array.from({ length: 21 }, (_, index) => `user${index}@example.org`));

    expect(recipients.errors).toEqual({ maxRecipients: { max: 20, actual: 21 } });
  });

  it('requires a subject on a single line of at most 250 characters', () => {
    const subject = create().controls.subject;

    expect(subject.hasError('required')).toBe(true);
    subject.setValue('   ');
    expect(subject.hasError('pattern')).toBe(true);
    subject.setValue('Primera línia\nSegona línia');
    expect(subject.hasError('lineBreak')).toBe(true);
    subject.setValue('a'.repeat(251));
    expect(subject.hasError('maxlength')).toBe(true);
    subject.setValue('a'.repeat(250));
    expect(subject.valid).toBe(true);
  });

  it('keeps the message optional up to 4000 characters', () => {
    const message = create().controls.message;

    message.setValue('a'.repeat(4001));
    expect(message.hasError('maxlength')).toBe(true);
    message.setValue('a'.repeat(4000));
    expect(message.valid).toBe(true);
  });

  it('restores the proposed subject on reset', () => {
    const form = create('Documentació');
    form.setValue({ recipients: ['ana@example.org'], subject: 'Altre', message: 'Hola' });

    form.reset();

    expect(form.getRawValue()).toEqual({ recipients: [], subject: 'Documentació', message: '' });
  });
});

describe('normalizeEmailRecipients', () => {
  it('trims, lowers and removes repeated or empty addresses as the back-end does', () => {
    expect(normalizeEmailRecipients([' Ana@Example.org', 'ana@example.org', '', 'PERE@caib.es '])).toEqual([
      'ana@example.org',
      'pere@caib.es',
    ]);
  });
});
