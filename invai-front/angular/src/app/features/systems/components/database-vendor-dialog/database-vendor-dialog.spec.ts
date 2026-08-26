import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';

import {
  DatabaseVendorFormGroup,
  createDatabaseVendorForm,
} from '../../forms/database-vendor-form.factory';
import { DatabaseVendorDialog } from './database-vendor-dialog';

describe('DatabaseVendorDialog', () => {
  let fixture: ComponentFixture<DatabaseVendorDialog>;
  let form: DatabaseVendorFormGroup;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [DatabaseVendorDialog] }).compileComponents();
    form = createDatabaseVendorForm(new FormBuilder());
    form.setValue({ name: 'PostgreSQL', defaultPort: 5432 });
    fixture = TestBed.createComponent(DatabaseVendorDialog);
    fixture.componentRef.setInput('visible', true);
    fixture.componentRef.setInput('mode', 'view');
    fixture.componentRef.setInput('form', form);
    fixture.detectChanges();
  });

  it('keeps the name and port perceivable as readonly values in view mode', () => {
    const name = fixture.nativeElement.querySelector('#database-vendor-name') as HTMLInputElement;
    const port = fixture.nativeElement.querySelector('#database-vendor-port') as HTMLInputElement;

    expect(name.value).toBe('PostgreSQL');
    expect(name.readOnly).toBe(true);
    expect(name.disabled).toBe(false);
    expect(port.value).toBe('5432');
    expect(port.readOnly).toBe(true);
    expect(port.disabled).toBe(false);
  });

  it('makes both controls editable in edit mode', () => {
    fixture.componentRef.setInput('mode', 'edit');
    fixture.detectChanges();
    const name = fixture.nativeElement.querySelector('#database-vendor-name') as HTMLInputElement;
    const port = fixture.nativeElement.querySelector('#database-vendor-port') as HTMLInputElement;

    expect(name.readOnly).toBe(false);
    expect(port.readOnly).toBe(false);
  });

  it('shows the restored snapshot after cancelling a numeric edit', () => {
    fixture.componentRef.setInput('mode', 'edit');
    fixture.detectChanges();
    const editedPort = fixture.nativeElement.querySelector(
      '#database-vendor-port',
    ) as HTMLInputElement;
    editedPort.value = '15499';
    editedPort.dispatchEvent(new Event('input', { bubbles: true }));
    editedPort.dispatchEvent(new FocusEvent('blur', { bubbles: true }));
    fixture.detectChanges();
    expect(form.controls.defaultPort.value).toBe(15499);

    form.reset({ name: 'PostgreSQL', defaultPort: 5432 });
    fixture.componentRef.setInput('mode', 'view');
    fixture.detectChanges();

    const restoredPort = fixture.nativeElement.querySelector(
      '#database-vendor-port',
    ) as HTMLInputElement;
    expect(restoredPort.value).toBe('5432');
    expect(restoredPort.readOnly).toBe(true);
  });
});
