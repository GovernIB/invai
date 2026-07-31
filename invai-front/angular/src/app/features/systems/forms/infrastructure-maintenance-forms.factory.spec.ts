import { FormBuilder } from '@angular/forms';

import {
  createDatabaseHostForm,
  createSystemHostForm,
} from './infrastructure-maintenance-forms.factory';
import { createDatabaseVendorForm } from './database-vendor-form.factory';
import { createPhysicalServerForm } from './physical-server-form.factory';

describe('infrastructure maintenance form factories', () => {
  const formBuilder = new FormBuilder();
  const environment = { id: 3, code: 'PRO', label: 'Producció' };
  const server = {
    id: 5,
    name: 'app01.caib.es',
    environment: {
      id: 3,
      code: 'PRO',
      name: 'Producció',
      nameEs: 'Producción',
      deletedAt: null,
    },
    label: 'app01.caib.es · Producció',
  };
  const vendor = { id: 2, name: 'PostgreSQL', defaultPort: 5432 };

  it('creates a typed System form with backend length and port validation', () => {
    const form = createSystemHostForm(formBuilder);
    expect(form.invalid).toBe(true);
    form.reset({
      server,
      instance: 'jboss',
      port: 8080,
      version: '7.1',
      description: '',
    });
    expect(form.valid).toBe(true);
    form.controls.instance.setValue('x'.repeat(51));
    expect(form.controls.instance.hasError('maxlength')).toBe(true);
    form.controls.port.setValue(65536);
    expect(form.controls.port.hasError('max')).toBe(true);
  });

  it('requires server and vendor relations in a Database form', () => {
    const form = createDatabaseHostForm(formBuilder);
    form.reset({
      server,
      service: 'INVAI',
      port: 5432,
      databaseType: vendor,
      description: '',
    });
    expect(form.valid).toBe(true);
    form.controls.databaseType.reset();
    expect(form.controls.databaseType.hasError('required')).toBe(true);
  });

  it('validates physical servers and database vendors', () => {
    const physicalServer = createPhysicalServerForm(formBuilder);
    physicalServer.reset({ name: 'app01.caib.es', environment });
    expect(physicalServer.valid).toBe(true);
    physicalServer.controls.name.setValue(' ');
    expect(physicalServer.invalid).toBe(true);

    const databaseVendor = createDatabaseVendorForm(formBuilder);
    databaseVendor.reset({ name: 'PostgreSQL', defaultPort: 5432 });
    expect(databaseVendor.valid).toBe(true);
    databaseVendor.controls.defaultPort.setValue(0);
    expect(databaseVendor.controls.defaultPort.hasError('min')).toBe(true);
  });
});
