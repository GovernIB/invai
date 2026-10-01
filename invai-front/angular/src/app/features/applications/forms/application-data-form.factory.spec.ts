import { FormBuilder } from '@angular/forms';

import {
  APPLICATION_DATA_URL_MAX_LENGTH,
  EMPTY_APPLICATION_DATA_VALUE,
  createApplicationDataForm,
} from './application-data-form.factory';

describe('createApplicationDataForm', () => {
  const formBuilder = new FormBuilder();

  it('creates the empty value with both explicit URLs unused and valid', () => {
    const form = createApplicationDataForm(formBuilder);

    expect(form.getRawValue()).toEqual(EMPTY_APPLICATION_DATA_VALUE);
    expect(form.valid).toBe(true);
  });

  it.each([
    ['useOpenDataUrl', 'openDataUrl'],
    ['useReuseUrl', 'reuseUrl'],
  ] as const)('requires %s\'s URL only while the flag is on', (flag, url) => {
    const form = createApplicationDataForm(formBuilder);

    form.controls[flag].setValue(true);
    form.controls[url].updateValueAndValidity();
    expect(form.controls[url].hasError('required')).toBe(true);

    form.controls[url].setValue('   ');
    expect(form.controls[url].hasError('required')).toBe(true);

    form.controls[url].setValue('https://intranet.caib.es/appapi/externa/swagger.json');
    expect(form.controls[url].valid).toBe(true);

    form.controls[url].setValue('');
    form.controls[flag].setValue(false);
    form.controls[url].updateValueAndValidity();
    expect(form.controls[url].valid).toBe(true);
  });

  it('keeps a detected URL valid while its flag is off', () => {
    const form = createApplicationDataForm(formBuilder);

    form.controls.openDataUrl.setValue('https://intranet.caib.es/appapi/externa/openapi.json');

    expect(form.controls.openDataUrl.valid).toBe(true);
  });

  it('limits both URLs to the backend maximum length', () => {
    const form = createApplicationDataForm(formBuilder);
    const base = 'https://example.com/';

    form.controls.openDataUrl.setValue(base + 'x'.repeat(APPLICATION_DATA_URL_MAX_LENGTH - base.length));
    form.controls.reuseUrl.setValue(base + 'x'.repeat(APPLICATION_DATA_URL_MAX_LENGTH));

    expect(form.controls.openDataUrl.valid).toBe(true);
    expect(form.controls.reuseUrl.hasError('maxlength')).toBe(true);
  });

  it.each(['intranet.caib.es/swagger.json', 'ftp://host/swagger.json', 'https://'])(
    'rejects %s as a non HTTP(S) URL',
    (value) => {
      const form = createApplicationDataForm(formBuilder);

      form.controls.reuseUrl.setValue(value);

      expect(form.controls.reuseUrl.hasError('pattern')).toBe(true);
    },
  );
});
