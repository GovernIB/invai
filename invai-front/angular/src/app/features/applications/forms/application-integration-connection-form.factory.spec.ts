import { FormBuilder, FormControl } from '@angular/forms';
import { SoffidPersonControlValue } from '@features/maintenances/responsibles/responsibles.model';

import {
  EMPTY_APPLICATION_INTEGRATION_CONNECTION_VALUE,
  configureApplicationIntegrationSystemKind,
  createApplicationIntegrationConnectionForm,
  selectedIntegrationUserValidator,
} from './application-integration-connection-form.factory';

const USER = {
  id: null,
  company: null,
  firstName: 'Maria',
  lastName: 'Tur',
  email: 'maria@caib.es',
  personalCaib: true as const,
  deletedAt: null,
  userName: 'u00004',
  label: 'Maria Tur (u00004)',
};

describe('application integration connection form factory', () => {
  it('starts empty with an inventory application as the other system', () => {
    const form = createApplicationIntegrationConnectionForm(new FormBuilder());

    expect(form.getRawValue()).toEqual(EMPTY_APPLICATION_INTEGRATION_CONNECTION_VALUE);
    expect(form.valid).toBe(false);
    expect(form.controls.applicationId.hasError('required')).toBe(true);
    expect(form.controls.externalSystemId.errors).toBeNull();
    expect(form.controls.technologyId.hasError('required')).toBe(true);
    expect(form.controls.user.hasError('required')).toBe(true);
    expect(form.controls.requiredRoles.hasError('required')).toBe(true);
  });

  it('accepts a complete connection with at least one required role', () => {
    const form = createApplicationIntegrationConnectionForm(new FormBuilder());
    form.setValue({
      systemKind: 'application',
      applicationId: 8,
      externalSystemId: null,
      technologyId: 4,
      user: USER,
      requiredRoles: [{ roleId: 26, label: 'INV_ADMIN' }],
    });

    expect(form.valid).toBe(true);
  });

  it('requires only the selector of the chosen system kind and clears the other one', () => {
    const form = createApplicationIntegrationConnectionForm(new FormBuilder());
    form.controls.applicationId.setValue(8);

    form.controls.systemKind.setValue('external');
    configureApplicationIntegrationSystemKind(form);

    expect(form.controls.applicationId.value).toBeNull();
    expect(form.controls.applicationId.errors).toBeNull();
    expect(form.controls.externalSystemId.hasError('required')).toBe(true);

    form.controls.externalSystemId.setValue(2);
    form.controls.systemKind.setValue('application');
    configureApplicationIntegrationSystemKind(form);

    expect(form.controls.externalSystemId.value).toBeNull();
    expect(form.controls.applicationId.hasError('required')).toBe(true);
  });

  it.each<[SoffidPersonControlValue, boolean]>([
    [null, true],
    ['', true],
    ['maria', false],
    [{ ...USER, userName: null }, false],
    [{ ...USER, userName: '  ' }, false],
    [USER, true],
    [{ ...USER, firstName: '', lastName: '', email: '', label: 'u00004' }, true],
  ])('validates the Soffid user %o by its user code', (value, valid) => {
    expect(selectedIntegrationUserValidator(new FormControl(value)) === null).toBe(valid);
  });
});
