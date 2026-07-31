import { FormBuilder } from '@angular/forms';

import {
  createApplicationDatabaseRelationForm,
  createApplicationSystemRelationForm,
} from './application-infrastructure-relation-forms.factory';

describe('application infrastructure relation forms', () => {
  const formBuilder = new FormBuilder();

  it('requires one maintenance system selection', () => {
    const form = createApplicationSystemRelationForm(formBuilder);

    expect(form.controls.system.value).toBeNull();
    expect(form.invalid).toBe(true);

    form.controls.system.setValue({
      id: 5,
      server: 'app01.caib.es',
      environment: 'Producció',
      instance: 'jboss',
      port: 8080,
      version: '7.4',
      description: '',
      source: null!,
    });

    expect(form.valid).toBe(true);
  });

  it('requires one maintenance database selection', () => {
    const form = createApplicationDatabaseRelationForm(formBuilder);

    expect(form.controls.database.value).toBeNull();
    expect(form.invalid).toBe(true);

    form.controls.database.setValue({
      id: 8,
      server: 'db01.caib.es',
      environment: 'Producció',
      service: 'invai_svc',
      port: 5432,
      databaseType: 'PostgreSQL',
      description: '',
      source: null!,
    });

    expect(form.valid).toBe(true);
  });
});
