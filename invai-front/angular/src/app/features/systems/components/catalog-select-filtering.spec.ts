import { Type } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';
import { By } from '@angular/platform-browser';
import { EnvironmentCatalogOption } from '@features/environments/environments.model';
import { Select } from 'primeng/select';

import { createDatabaseFiltersForm } from '../forms/database-filters-form.factory';
import {
  createDatabaseHostForm,
  createSystemHostForm,
} from '../forms/infrastructure-maintenance-forms.factory';
import { createPhysicalServerFiltersForm } from '../forms/physical-server-filters-form.factory';
import { createPhysicalServerForm } from '../forms/physical-server-form.factory';
import { createServerFiltersForm } from '../forms/server-filters-form.factory';
import {
  DatabaseVendorCatalogOption,
  ServerCatalogOption,
} from '../systems.model';
import { DatabaseFiltersForm } from './database-filters-form/database-filters-form';
import { DatabaseMaintenanceDialog } from './database-maintenance-dialog/database-maintenance-dialog';
import { PhysicalServerDialog } from './physical-server-dialog/physical-server-dialog';
import { PhysicalServerFiltersForm } from './physical-server-filters-form/physical-server-filters-form';
import { ServerFiltersForm } from './server-filters-form/server-filters-form';
import { ServerMaintenanceDialog } from './server-maintenance-dialog/server-maintenance-dialog';

const ENVIRONMENT: EnvironmentCatalogOption = {
  id: 1,
  code: 'PRO',
  label: 'Producció',
};
const SERVER: ServerCatalogOption = {
  id: 2,
  name: 'app01.caib.es',
  environment: {
    id: ENVIRONMENT.id,
    code: ENVIRONMENT.code,
    name: ENVIRONMENT.label,
    nameEs: 'Producción',
    deletedAt: null,
  },
  label: 'app01.caib.es · Producció',
};
const DATABASE_VENDOR: DatabaseVendorCatalogOption = {
  id: 3,
  name: 'PostgreSQL',
  defaultPort: 5432,
};

describe('systems catalog select filtering', () => {
  const formBuilder = new FormBuilder();

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        DatabaseFiltersForm,
        DatabaseMaintenanceDialog,
        PhysicalServerDialog,
        PhysicalServerFiltersForm,
        ServerFiltersForm,
        ServerMaintenanceDialog,
      ],
    }).compileComponents();
  });

  it('filters catalog selectors but not local status selectors in search forms', () => {
    const physicalServers = createFixture(PhysicalServerFiltersForm, {
      form: createPhysicalServerFiltersForm(formBuilder),
      environmentOptions: [ENVIRONMENT],
    });
    expectFilterState(
      physicalServers,
      ['physical-servers-filter-environment'],
      ['physical-servers-filter-status'],
    );

    const servers = createFixture(ServerFiltersForm, {
      form: createServerFiltersForm(formBuilder),
      labels: {
        server: 'Servidor',
        instance: 'Instància',
        version: 'Versió',
        status: 'Estat',
      },
      serverOptions: [SERVER],
    });
    expectFilterState(
      servers,
      ['systems-filter-server'],
      ['systems-filter-status'],
    );

    const databases = createFixture(DatabaseFiltersForm, {
      form: createDatabaseFiltersForm(formBuilder),
      labels: {
        server: 'Servidor',
        service: 'Servei',
        databaseType: 'Proveïdor',
        status: 'Estat',
      },
      serverOptions: [SERVER],
      databaseTypeOptions: [DATABASE_VENDOR],
    });
    expectFilterState(
      databases,
      ['databases-filter-server', 'databases-filter-type'],
      ['databases-filter-status'],
    );
  });

  it('filters every maintenance relation selector in CRUD dialogs', () => {
    const physicalServer = createFixture(PhysicalServerDialog, {
      visible: true,
      mode: 'create',
      serverTypeCode: 'APPLICATION',
      form: createPhysicalServerForm(formBuilder),
      environmentOptions: [ENVIRONMENT],
    });
    expectFilterState(physicalServer, ['physical-server-environment']);

    const server = createFixture(ServerMaintenanceDialog, {
      visible: true,
      mode: 'create',
      form: createSystemHostForm(formBuilder),
      serverOptions: [SERVER],
    });
    expectFilterState(server, ['application-host-server']);

    const database = createFixture(DatabaseMaintenanceDialog, {
      visible: true,
      mode: 'create',
      form: createDatabaseHostForm(formBuilder),
      serverOptions: [SERVER],
      databaseTypeOptions: [DATABASE_VENDOR],
    });
    expectFilterState(database, ['database-host-server', 'database-host-type']);
  });
});

function createFixture<T>(
  component: Type<T>,
  inputs: Record<string, unknown>,
): ComponentFixture<T> {
  const fixture = TestBed.createComponent(component);
  for (const [name, value] of Object.entries(inputs)) {
    fixture.componentRef.setInput(name, value);
  }
  fixture.detectChanges();
  return fixture;
}

function expectFilterState<T>(
  fixture: ComponentFixture<T>,
  catalogIds: string[],
  localIds: string[] = [],
): void {
  const selects = fixture.debugElement
    .queryAll(By.directive(Select))
    .map(({ componentInstance }) => componentInstance as Select);

  expect(
    selects
      .filter((select) => catalogIds.includes(select.inputId!))
      .map((select) => select.inputId),
  ).toEqual(catalogIds);

  for (const inputId of catalogIds) {
    const select = selects.find((candidate) => candidate.inputId === inputId)!;
    expect(select.filter).toBe(true);
    expect(select.ariaFilterLabel).toBeTruthy();
  }

  for (const inputId of localIds) {
    const select = selects.find((candidate) => candidate.inputId === inputId)!;
    expect(select.filter).toBeFalsy();
    expect(select.ariaFilterLabel).toBeFalsy();
  }
}
