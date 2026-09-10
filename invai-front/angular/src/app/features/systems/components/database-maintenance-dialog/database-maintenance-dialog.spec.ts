import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';
import { By } from '@angular/platform-browser';
import { Select } from 'primeng/select';

import { createDatabaseHostForm } from '../../forms/infrastructure-maintenance-forms.factory';
import {
  DatabaseVendorCatalogOption,
  ServerCatalogOption,
} from '../../systems.model';
import { DatabaseMaintenanceDialog } from './database-maintenance-dialog';

const SERVER: ServerCatalogOption = {
  id: 2,
  name: 'db01.caib.es',
  environment: {
    id: 1,
    code: 'PRO',
    name: 'Producció',
    nameEs: 'Producción',
    deletedAt: null,
  },
  label: 'db01.caib.es · Producció',
};
const POSTGRESQL: DatabaseVendorCatalogOption = {
  id: 3,
  name: 'PostgreSQL',
  defaultPort: 5432,
};
const ORACLE: DatabaseVendorCatalogOption = {
  id: 4,
  name: 'Oracle',
  defaultPort: 1521,
};

describe('DatabaseMaintenanceDialog', () => {
  const formBuilder = new FormBuilder();

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DatabaseMaintenanceDialog],
    }).compileComponents();
  });

  it('fills the editable port with the selected vendor default', () => {
    const form = createDatabaseHostForm(formBuilder);
    const fixture = createFixture(form);

    databaseTypeSelect(fixture).onChange.emit({
      originalEvent: new Event('change'),
      value: POSTGRESQL,
    });

    expect(form.controls.port.value).toBe(POSTGRESQL.defaultPort);
    expect(form.controls.port.enabled).toBe(true);
    expect(form.controls.port.dirty).toBe(true);

    form.controls.port.setValue(6432);

    expect(form.controls.port.value).toBe(6432);
  });

  it('preserves a stored custom port until the user selects another vendor', () => {
    const form = createDatabaseHostForm(formBuilder);
    form.reset({
      server: SERVER,
      service: 'INVAI',
      port: 15432,
      databaseType: POSTGRESQL,
      description: '',
    });

    const fixture = createFixture(form, 'edit');

    expect(form.controls.port.value).toBe(15432);

    databaseTypeSelect(fixture).onChange.emit({
      originalEvent: new Event('change'),
      value: ORACLE,
    });

    expect(form.controls.port.value).toBe(ORACLE.defaultPort);
  });

  it('renders the vendor before the compact port in reading order', () => {
    const fixture = createFixture(createDatabaseHostForm(formBuilder));
    const dialog = fixture.nativeElement as HTMLElement;
    const labels = Array.from(
      dialog.querySelectorAll<HTMLLabelElement>(
        'label[for="database-host-type"], label[for="database-host-port"]',
      ),
    );

    expect(labels.map(({ htmlFor }) => htmlFor)).toEqual([
      'database-host-type',
      'database-host-port',
    ]);
    expect(labels[0].closest('div')?.classList.contains('md:col-span-8')).toBe(
      true,
    );
    expect(labels[1].closest('div')?.classList.contains('md:col-span-4')).toBe(
      true,
    );
  });

  it('renders complex view values statically and text as readonly', () => {
    const form = createDatabaseHostForm(formBuilder);
    form.reset({
      server: SERVER,
      service: 'INVAI',
      port: 15432,
      databaseType: POSTGRESQL,
      description: 'Principal',
    });
    const fixture = createFixture(form, 'view');
    const service = fixture.nativeElement.querySelector('#database-host-service') as HTMLInputElement;

    expect(service.readOnly).toBe(true);
    expect(service.disabled).toBe(false);
    expect(fixture.debugElement.query(By.directive(Select))).toBeNull();
    expect(fixture.nativeElement.textContent).toContain(SERVER.label);
    expect(fixture.nativeElement.textContent).toContain(POSTGRESQL.name);
    expect(fixture.nativeElement.textContent).toContain('15432');
  });

  function createFixture(
    form: ReturnType<typeof createDatabaseHostForm>,
    mode: 'create' | 'view' | 'edit' = 'create',
  ): ComponentFixture<DatabaseMaintenanceDialog> {
    const fixture = TestBed.createComponent(DatabaseMaintenanceDialog);
    fixture.componentRef.setInput('visible', true);
    fixture.componentRef.setInput('mode', mode);
    fixture.componentRef.setInput('form', form);
    fixture.componentRef.setInput('serverOptions', [SERVER]);
    fixture.componentRef.setInput('databaseTypeOptions', [POSTGRESQL, ORACLE]);
    fixture.detectChanges();
    return fixture;
  }

  function databaseTypeSelect(
    fixture: ComponentFixture<DatabaseMaintenanceDialog>,
  ): Select {
    return fixture.debugElement
      .queryAll(By.directive(Select))
      .map(({ componentInstance }) => componentInstance as Select)
      .find(({ inputId }) => inputId === 'database-host-type')!;
  }
});
