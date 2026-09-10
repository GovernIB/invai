import { FormBuilder } from '@angular/forms';

import {
  EMPTY_APPLICATION_ACCESSIBILITY_VALUE,
  createApplicationAccessibilityForm,
} from './application-accessibility-form.factory';

describe('createApplicationAccessibilityForm', () => {
  const formBuilder = new FormBuilder();

  it('creates the empty value without inventing data with nullable optional controls', () => {
    const form = createApplicationAccessibilityForm(formBuilder);

    expect(form.getRawValue()).toEqual(EMPTY_APPLICATION_ACCESSIBILITY_VALUE);

    form.controls.reportExpirationDate.setValue(null);
    form.controls.mobileApplicationName.setValue(null);

    expect(form.controls.reportExpirationDate.valid).toBe(true);
    expect(form.controls.mobileApplicationName.valid).toBe(true);
  });

  it.each(['', 'https://git.caib.es/invai', 'http://localhost:4200'])(
    'accepts the optional public URL %s',
    (value) => {
      const form = createApplicationAccessibilityForm(formBuilder);

      form.controls.publicUrl.setValue(value);

      expect(form.controls.publicUrl.valid).toBe(true);
    },
  );

  it('limits free-text mobile names and URLs to 255 characters', () => {
    const form = createApplicationAccessibilityForm(formBuilder);
    form.controls.mobileApplicationName.setValue('x'.repeat(256));
    form.controls.publicUrl.setValue('https://example.com/' + 'x'.repeat(256));
    expect(form.controls.mobileApplicationName.hasError('maxlength')).toBe(true);
    expect(form.controls.publicUrl.hasError('maxlength')).toBe(true);
    form.controls.mobileApplicationName.setValue('Una aplicación móvil libre');
    expect(form.controls.mobileApplicationName.valid).toBe(true);
    expect(form.controls.mobileApplication.value).toBeNull();
  });

  it('rejects a public URL without an HTTP or HTTPS scheme', () => {
    const form = createApplicationAccessibilityForm(formBuilder);

    form.controls.publicUrl.setValue('git.caib.es/invai');

    expect(form.controls.publicUrl.hasError('pattern')).toBe(true);
  });
});
