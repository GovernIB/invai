import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';
import { By } from '@angular/platform-browser';
import { Select } from 'primeng/select';

import {
  createApplicationDatabaseFiltersForm,
  createApplicationServerFiltersForm,
} from '../forms/application-infrastructure-filter-form.factory';
import { ApplicationDatabaseFiltersForm } from './application-database-filters-form/application-database-filters-form';
import { ApplicationServerFiltersForm } from './application-server-filters-form/application-server-filters-form';

describe('application infrastructure filter forms', () => {
  it('renders accessible server catalog and status selectors', async () => {
    await TestBed.configureTestingModule({ imports: [ApplicationServerFiltersForm] }).compileComponents();
    const fixture = TestBed.createComponent(ApplicationServerFiltersForm);
    fixture.componentRef.setInput('form', createApplicationServerFiltersForm(new FormBuilder()));
    fixture.componentRef.setInput('labels', serverLabels());
    fixture.componentRef.setInput('serverOptions', [{ label: 'app01', value: 5 }]);
    fixture.componentRef.setInput('environmentOptions', [{ label: 'Producció', value: 3 }]);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('label[for="application-servers-filter-server"]'))
      .toBeTruthy();
    expect(fixture.nativeElement.querySelector('label[for="application-servers-filter-port"]'))
      .toBeTruthy();
    expect(selects(fixture).map((select) => select.ariaLabelledBy)).toEqual([
      'application-servers-filter-environment-label',
      'application-servers-filter-server-label',
      'application-servers-filter-status-label',
    ]);
    expect(
      selects(fixture).find(
        (select) => select.inputId === 'application-servers-filter-status',
      )?.showClear,
    ).toBe(true);
    expectCatalogFilters(fixture, [
      'application-servers-filter-environment',
      'application-servers-filter-server',
    ]);
  });

  it('renders accessible database catalog and status selectors', async () => {
    await TestBed.configureTestingModule({ imports: [ApplicationDatabaseFiltersForm] }).compileComponents();
    const fixture = TestBed.createComponent(ApplicationDatabaseFiltersForm);
    fixture.componentRef.setInput('form', createApplicationDatabaseFiltersForm(new FormBuilder()));
    fixture.componentRef.setInput('labels', databaseLabels());
    fixture.componentRef.setInput('databaseOptions', [{ label: 'INVAI', value: 8 }]);
    fixture.componentRef.setInput('environmentOptions', [{ label: 'Producció', value: 3 }]);
    fixture.detectChanges();

    expect(
      fixture.nativeElement.querySelector('label[for="application-databases-filter-database"]'),
    ).toBeTruthy();
    expect(
      fixture.nativeElement.querySelector('label[for="application-databases-filter-status"]'),
    ).toBeTruthy();
    expect(selects(fixture).map((select) => select.ariaLabelledBy)).toEqual([
      'application-databases-filter-environment-label',
      'application-databases-filter-database-label',
      'application-databases-filter-status-label',
    ]);
    expect(
      selects(fixture).find(
        (select) => select.inputId === 'application-databases-filter-status',
      )?.showClear,
    ).toBe(true);
    expectCatalogFilters(fixture, [
      'application-databases-filter-environment',
      'application-databases-filter-database',
    ]);
  });
});

function selects<TComponent>(fixture: ComponentFixture<TComponent>): Select[] {
  return fixture.debugElement
    .queryAll(By.directive(Select))
    .map((element) => element.componentInstance as Select);
}

function expectCatalogFilters<TComponent>(
  fixture: ComponentFixture<TComponent>,
  catalogIds: string[],
): void {
  for (const select of selects(fixture)) {
    if (catalogIds.includes(select.inputId!)) {
      expect(select.filter).toBe(true);
      expect(select.ariaFilterLabel).toBeTruthy();
    } else {
      expect(select.filter).toBeFalsy();
      expect(select.ariaFilterLabel).toBeFalsy();
    }
  }
}

function serverLabels() {
  return {
    environment: 'Entorn',
    server: 'Servidor',
    instance: 'Instància',
    port: 'Port',
    version: 'Versió',
    status: 'Estat',
    observations: 'Observacions',
  };
}

function databaseLabels() {
  return {
    environment: 'Entorn',
    server: 'Servidor',
    version: 'Versió',
    database: 'Base de dades',
    service: 'Servei',
    port: 'Port',
    type: 'Tipus',
    status: 'Estat',
    observations: 'Observacions',
  };
}
