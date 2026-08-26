import { FormBuilder } from '@angular/forms';

import { ApplicationStatus } from '../applications.model';
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
      commissionExpedientNumber: '',
      commissionApprovalDate: '',
      commissionType: null,
      prefix: '',
      administrativeUnit: null,
      conselleria: "Conselleria d'Educació, Universitats i Ocupació",
      description: '',
      code: '',
    });
    expect(form.valid).toBe(false);
    expect(form.controls.commissionExpedientNumber.disabled).toBe(true);
    expect(form.controls.commissionApprovalDate.disabled).toBe(true);
    expect(form.controls.commissionType.disabled).toBe(true);

    form.controls.application.setValue('Invai');
    form.controls.application.reset();

    expect(form.controls.application.value).toBe('');
  });

  it('requires application codes to contain between four and ten characters', () => {
    const code = createApplicationCreateForm(formBuilder).controls.code;

    expect(code.hasError('required')).toBe(true);

    code.setValue('123');
    expect(code.hasError('minlength')).toBe(true);

    code.setValue('1234');
    expect(code.valid).toBe(true);

    code.setValue('1234567890');
    expect(code.valid).toBe(true);

    code.setValue('12345678901');
    expect(code.hasError('maxlength')).toBe(true);

    code.reset();
    expect(code.value).toBe('');
    expect(code.hasError('required')).toBe(true);
  });

  it('limits application prefixes to three characters in create and detail forms', () => {
    const createPrefix = createApplicationCreateForm(formBuilder).controls.prefix;
    const detailPrefix = createApplicationDetailForm(formBuilder).controls.prefix;

    for (const prefix of [createPrefix, detailPrefix]) {
      prefix.setValue('INV');
      expect(prefix.hasError('maxlength')).toBe(false);

      prefix.setValue('INVAI');
      expect(prefix.hasError('maxlength')).toBe(true);
    }
  });

  it('creates the detail form with audit controls separated from editable data', () => {
    const form = createApplicationDetailForm(formBuilder);

    expect(form.controls.creationDate.value).toBe('');
    expect(form.controls.modificationDate.value).toBe('');
    expect(form.controls.withdrawalDate.value).toBe('');
    expect(form.controls.commissionExpedientNumber.disabled).toBe(true);
    expect(form.controls.commissionApprovalDate.disabled).toBe(true);
    expect(form.controls.commissionType.disabled).toBe(true);
    expect(form.controls.application.hasError('required')).toBe(true);
    expect(form.controls.description.hasError('required')).toBe(false);
  });

  it('creates filters with active status as the resettable default', () => {
    const form = createApplicationFiltersForm(formBuilder);

    expect(form.getRawValue()).toEqual({
      prefix: null,
      application: null,
      category: null,
      informationSystem: null,
      scope: null,
      commission: null,
      administrativeUnit: null,
      status: ApplicationStatus.ACTIVE,
      description: null,
      responsible: null,
      database: null,
      server: null,
      environment: null,
      incomplete: false,
    });

    form.controls.status.setValue(null);
    form.controls.responsible.setValue({ id: 7, label: 'Persona responsable' });
    form.controls.database.setValue(8);
    form.controls.server.setValue(5);
    form.controls.environment.setValue(3);
    form.controls.incomplete.setValue(true);
    form.reset();

    expect(form.controls.status.value).toBe(ApplicationStatus.ACTIVE);
    expect(form.controls.responsible.value).toBeNull();
    expect(form.controls.database.value).toBeNull();
    expect(form.controls.server.value).toBeNull();
    expect(form.controls.environment.value).toBeNull();
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
