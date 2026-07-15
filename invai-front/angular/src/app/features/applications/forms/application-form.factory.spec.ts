import { FormBuilder } from '@angular/forms';

import {
  createApplicationCreateForm,
  createApplicationDetailForm,
  createApplicationFiltersForm,
  createApplicationSystemsDatabasesForm,
} from './application-form.factory';

describe('application form factories', () => {
  const formBuilder = new FormBuilder();

  it('creates a non-nullable create form with the required controls', () => {
    const form = createApplicationCreateForm(formBuilder);

    expect(form.getRawValue()).toEqual({
      application: '',
      category: null,
      informationSystem: null,
      scope: null,
      commission: null,
      prefix: '',
      administrativeUnit: null,
      description: '',
      code: '',
    });
    expect(form.valid).toBe(false);

    form.controls.application.setValue('Invai');
    form.controls.application.reset();

    expect(form.controls.application.value).toBe('');
  });

  it('requires application codes to contain at least four characters', () => {
    const code = createApplicationCreateForm(formBuilder).controls.code;

    expect(code.hasError('required')).toBe(true);

    code.setValue('123');
    expect(code.hasError('minlength')).toBe(true);

    code.setValue('1234');
    expect(code.valid).toBe(true);

    code.reset();
    expect(code.value).toBe('');
    expect(code.hasError('required')).toBe(true);
  });

  it('creates the detail form with audit controls separated from editable data', () => {
    const form = createApplicationDetailForm(formBuilder);

    expect(form.controls.creationDate.value).toBe('');
    expect(form.controls.modificationDate.value).toBe('');
    expect(form.controls.withdrawalDate.value).toBe('');
    expect(form.controls.application.hasError('required')).toBe(true);
    expect(form.controls.description.hasError('required')).toBe(false);
  });

  it('creates nullable filters with the expected initial values', () => {
    const form = createApplicationFiltersForm(formBuilder);

    expect(form.getRawValue()).toEqual({
      prefix: null,
      application: null,
      category: null,
      informationSystem: null,
      scope: null,
      commission: null,
      administrativeUnit: null,
      status: null,
      description: null,
      incomplete: false,
    });

    form.controls.incomplete.setValue(true);
    form.reset();

    expect(form.controls.incomplete.value).toBe(false);
  });

  it('creates non-nullable systems and databases observations', () => {
    const form = createApplicationSystemsDatabasesForm(formBuilder);

    expect(form.getRawValue()).toEqual({ observations: '' });

    form.controls.observations.setValue('<p>Observació</p>');
    form.controls.observations.reset();

    expect(form.controls.observations.value).toBe('');
  });
});
