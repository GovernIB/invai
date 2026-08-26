import { FormBuilder } from '@angular/forms';

import {
  createApplicationAuthorizedForm,
  setApplicationAuthorizedCompanyRequired,
} from './application-authorized-form.factory';

describe('application authorized form', () => {
  const formBuilder = new FormBuilder();

  it('requires person, authorizations and company for external people', () => {
    const form = createApplicationAuthorizedForm(formBuilder);

    expect(form.getRawValue()).toEqual({
      personalCaib: false,
      companyId: null,
      personId: null,
      email: '',
      observation: '',
      authorizationTypeIds: [],
    });
    expect(form.controls.email.disabled).toBe(true);

    setApplicationAuthorizedCompanyRequired(form, true);
    form.patchValue({ personId: 2, authorizationTypeIds: [4] });
    expect(form.invalid).toBe(true);

    form.controls.companyId.setValue(3);
    expect(form.valid).toBe(true);

    form.controls.authorizationTypeIds.setValue([]);
    expect(form.controls.authorizationTypeIds.hasError('required')).toBe(true);

    setApplicationAuthorizedCompanyRequired(form, false);
    form.patchValue({ companyId: null, authorizationTypeIds: [4] });
    expect(form.valid).toBe(true);
  });
});
