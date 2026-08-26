import { FormBuilder } from '@angular/forms';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';

import {
  createResponsibleNameFiltersForm,
  createResponsibleNameForm,
  createResponsiblePersonFiltersForm,
  createResponsiblePersonForm,
  createRoleTransferForm,
} from './responsible-forms.factory';

describe('responsible form factories', () => {
  const formBuilder = new FormBuilder();

  it('requires and trims whitespace-only names through validation', () => {
    const form = createResponsibleNameForm(formBuilder);

    expect(form.invalid).toBe(true);
    form.controls.name.setValue('   ');
    expect(form.controls.name.hasError('pattern')).toBe(true);
    form.controls.name.setValue('Plexus');
    expect(form.valid).toBe(true);
  });

  it('requires company, first name, last name and a valid email', () => {
    const form = createResponsiblePersonForm(formBuilder);
    expect(form.invalid).toBe(true);

    form.setValue({
      companyId: 1,
      firstName: 'Maria',
      lastName: 'Tur Roig',
      email: 'not-an-email',
    });
    expect(form.controls.email.hasError('email')).toBe(true);

    form.patchValue({ email: 'maria.tur@invai.es' });
    expect(form.valid).toBe(true);
  });

  it('limits both name fields to 150 characters', () => {
    const form = createResponsiblePersonForm(formBuilder);
    form.controls.firstName.setValue('a'.repeat(151));
    form.controls.lastName.setValue('b'.repeat(151));
    expect(form.controls.firstName.hasError('maxlength')).toBe(true);
    expect(form.controls.lastName.hasError('maxlength')).toBe(true);
  });

  it('restores active status when filters are reset', () => {
    const nameFilters = createResponsibleNameFiltersForm(formBuilder);
    const personFilters = createResponsiblePersonFiltersForm(formBuilder);
    nameFilters.controls.status.setValue(null);
    personFilters.controls.status.setValue(SoftDeleteStatus.INACTIVE);

    nameFilters.reset();
    personFilters.reset();

    expect(nameFilters.controls.status.value).toBe(SoftDeleteStatus.ACTIVE);
    expect(personFilters.controls.status.value).toBe(SoftDeleteStatus.ACTIVE);
  });

  it('starts role transfer without people and requires source and destination', () => {
    const form = createRoleTransferForm(formBuilder);

    expect(form.getRawValue()).toEqual({
      sourcePersonId: null,
      destinationPersonId: null,
      revoke: false,
    });
    expect(form.invalid).toBe(true);
    expect(form.controls.destinationPersonId.disabled).toBe(true);
    form.controls.destinationPersonId.enable();
    form.patchValue({ sourcePersonId: 1, destinationPersonId: 2 });
    expect(form.valid).toBe(true);
  });
});
