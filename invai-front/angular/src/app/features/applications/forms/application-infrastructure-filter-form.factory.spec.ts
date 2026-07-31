import { FormBuilder } from '@angular/forms';

import { ApplicationInfrastructureStatus } from '../applications.model';
import {
  createApplicationDatabaseFiltersForm,
  createApplicationServerFiltersForm,
} from './application-infrastructure-filter-form.factory';

describe('application infrastructure filter form factories', () => {
  const formBuilder = new FormBuilder();

  it('creates server filters with active status as the resettable default', () => {
    const form = createApplicationServerFiltersForm(formBuilder);

    expect(form.getRawValue()).toEqual({
      environment: null,
      server: null,
      instance: null,
      port: null,
      version: null,
      status: ApplicationInfrastructureStatus.ACTIVE,
      observations: null,
    });

    form.patchValue({ server: 5, status: null });
    expect(form.controls.status.value).toBeNull();
    form.reset();

    expect(form.controls.server.value).toBeNull();
    expect(form.controls.status.value).toBe(ApplicationInfrastructureStatus.ACTIVE);
  });

  it('creates database filters with active status as the resettable default', () => {
    const form = createApplicationDatabaseFiltersForm(formBuilder);

    expect(form.getRawValue()).toEqual({
      environment: null,
      server: null,
      version: null,
      database: null,
      service: null,
      port: null,
      type: null,
      status: ApplicationInfrastructureStatus.ACTIVE,
      observations: null,
    });

    form.patchValue({ database: 8, status: null });
    expect(form.controls.status.value).toBeNull();
    form.reset();

    expect(form.controls.database.value).toBeNull();
    expect(form.controls.status.value).toBe(ApplicationInfrastructureStatus.ACTIVE);
  });
});
