import { Type } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';
import { CommissionDialog } from '@features/commissions/components/commission-dialog/commission-dialog';
import { createCommissionForm } from '@features/commissions/forms/commission-form.factory';
import { DatabaseMaintenanceDialog } from '@features/systems/components/database-maintenance-dialog/database-maintenance-dialog';
import { DatabaseVendorDialog } from '@features/systems/components/database-vendor-dialog/database-vendor-dialog';
import { PhysicalServerDialog } from '@features/systems/components/physical-server-dialog/physical-server-dialog';
import { ServerMaintenanceDialog } from '@features/systems/components/server-maintenance-dialog/server-maintenance-dialog';
import { createDatabaseVendorForm } from '@features/systems/forms/database-vendor-form.factory';
import {
  createDatabaseHostForm,
  createSystemHostForm,
} from '@features/systems/forms/infrastructure-maintenance-forms.factory';
import { createPhysicalServerForm } from '@features/systems/forms/physical-server-form.factory';

describe('maintenance dialog form layouts', () => {
  const formBuilder = new FormBuilder();

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        CommissionDialog,
        DatabaseMaintenanceDialog,
        DatabaseVendorDialog,
        PhysicalServerDialog,
        ServerMaintenanceDialog,
      ],
    }).compileComponents();
  });

  it('groups application server fields by expected content length', () => {
    expectLayout(
      ServerMaintenanceDialog,
      {
        visible: true,
        mode: 'create',
        form: createSystemHostForm(formBuilder),
        serverOptions: [],
      },
      [7, 5, 5, 4, 12],
    );
  });

  it('groups database fields by expected content length', () => {
    expectLayout(
      DatabaseMaintenanceDialog,
      {
        visible: true,
        mode: 'create',
        form: createDatabaseHostForm(formBuilder),
        serverOptions: [],
        databaseTypeOptions: [],
      },
      [6, 6, 4, 8, 12],
    );
  });

  it('keeps physical server and database vendor fields on one responsive row', () => {
    expectLayout(
      PhysicalServerDialog,
      {
        visible: true,
        mode: 'create',
        serverTypeCode: 'APPLICATION',
        form: createPhysicalServerForm(formBuilder),
        environmentOptions: [],
      },
      [7, 5],
    );

    expectLayout(
      DatabaseVendorDialog,
      {
        visible: true,
        mode: 'create',
        form: createDatabaseVendorForm(formBuilder),
      },
      [8, 4],
    );
  });

  it('uses responsive spans instead of a fixed width for commission type', () => {
    const form = expectLayout(
      CommissionDialog,
      {
        visible: true,
        mode: 'create',
        form: createCommissionForm(formBuilder),
      },
      [6, 6, 6, 6, 6],
    );
    const typeField = form.querySelector('.commission-dialog-type-field') as HTMLElement;

    expect(typeField.hasAttribute('style')).toBe(false);
  });
});

function expectLayout(
  component: Type<unknown>,
  inputs: Record<string, unknown>,
  expectedDesktopSpans: number[],
): HTMLFormElement {
  const fixture = TestBed.createComponent(component);
  for (const [name, value] of Object.entries(inputs)) {
    fixture.componentRef.setInput(name, value);
  }
  fixture.detectChanges();

  const form = fixture.nativeElement.querySelector('form') as HTMLFormElement;
  const desktopSpans = Array.from(form.children).map((field) => {
    const spanClass = Array.from(field.classList).find((className) =>
      className.startsWith('md:col-span-'),
    );
    return Number(spanClass?.replace('md:col-span-', ''));
  });

  expect(form.classList.contains('grid-cols-1')).toBe(true);
  expect(form.classList.contains('md:grid-cols-12')).toBe(true);
  expect(desktopSpans).toEqual(expectedDesktopSpans);

  return form;
}
