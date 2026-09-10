import { FormBuilder } from '@angular/forms';

import {
  createApplicationAuthorizedForm,
  setApplicationAuthorizedCompanyRequired,
  setApplicationAuthorizedPersonSource,
} from './application-authorized-form.factory';

describe('application authorized form', () => {
  const formBuilder = new FormBuilder();

  it('requires person, authorizations and company for external people', () => {
    const form = createApplicationAuthorizedForm(formBuilder);

    expect(form.getRawValue()).toEqual({
      personalCaib: false,
      companyId: null,
      personId: null,
      soffidPerson: null,
      email: '',
      observation: '',
      authorizationTypeIds: [],
    });
    expect(form.controls.email.enabled).toBe(true);
    expect(form.controls.soffidPerson.disabled).toBe(true);

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

    setApplicationAuthorizedPersonSource(form, true);
    form.controls.soffidPerson.enable();
    form.patchValue({ personId: null, soffidPerson: 'Maria' });
    expect(form.controls.soffidPerson.hasError('soffidSelection')).toBe(true);

    form.controls.soffidPerson.setValue({
      id: null,
      company: null,
      firstName: 'Maria',
      lastName: 'Tur',
      email: 'maria@caib.es',
      personalCaib: true,
      deletedAt: null,
      label: 'Maria Tur — maria@caib.es',
    });
    expect(form.valid).toBe(true);
  });
});
