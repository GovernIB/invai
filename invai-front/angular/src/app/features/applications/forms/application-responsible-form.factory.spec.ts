import { FormBuilder } from '@angular/forms';

import {
  createApplicationResponsibleForm,
  setApplicationResponsibleCompanyRequired,
  setApplicationResponsiblePersonSource,
} from './application-responsible-form.factory';

describe('application responsible form', () => {
  const formBuilder = new FormBuilder();

  it('requires the responsibility type and person, with company only for external people', () => {
    const form = createApplicationResponsibleForm(formBuilder);

    expect(form.invalid).toBe(true);
    expect(form.getRawValue()).toEqual({
      personalCaib: false,
      responsibleTypeId: null,
      companyId: null,
      personId: null,
      soffidPerson: null,
      email: '',
      cargo: '',
      observation: '',
    });
    expect(form.controls.email.enabled).toBe(true);
    expect(form.controls.soffidPerson.disabled).toBe(true);
    expect(form.controls.cargo.disabled).toBe(true);

    form.controls.observation.setValue('Nota opcional');
    expect(form.controls.observation.errors).toBeNull();

    setApplicationResponsibleCompanyRequired(form, true);
    form.patchValue({ responsibleTypeId: 1, personId: 2 });
    expect(form.invalid).toBe(true);

    form.controls.companyId.setValue(3);
    expect(form.valid).toBe(true);

    setApplicationResponsibleCompanyRequired(form, false);
    form.controls.companyId.setValue(null);
    expect(form.valid).toBe(true);

    setApplicationResponsiblePersonSource(form, true);
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
