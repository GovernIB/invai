import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';
import { By } from '@angular/platform-browser';
import { CommissionType } from '@features/commissions/commissions.model';
import { AutoComplete } from 'primeng/autocomplete';
import { Select } from 'primeng/select';
import { ToggleSwitch } from 'primeng/toggleswitch';

import { ApplicationInfrastructureFilterOptions } from '../../applications.model';
import { createApplicationFiltersForm } from '../../forms/application-form.factory';
import { ApplicationSelectOptions } from '../../services/application-options.service';
import { ApplicationFiltersForm, ApplicationFilterLabels } from './application-filters-form';

const LABELS: ApplicationFilterLabels = {
  prefix: 'Prefix',
  application: 'Aplicació',
  category: 'Categoria',
  informationSystem: "Sistema d'informació",
  scope: 'Àmbit',
  commission: 'Comissió',
  administrativeUnit: 'Unitat administrativa',
  status: 'Estat',
  description: 'Descripció',
  responsible: 'Responsables',
  database: 'Bases de dades',
  server: 'Servidor',
  environment: 'Entorn',
  incomplete: 'Incomplets',
  responsibleEmpty: "No s'han trobat persones actives",
  responsibleLoading: 'Cercant persones responsables…',
};

const OPTIONS: ApplicationSelectOptions = {
  categories: [{ label: 'Categoria', value: 1 }],
  informationSystems: [{ label: 'Sistema', value: 2 }],
  scopes: [{ label: 'Àmbit', value: 3 }],
  commissions: [
    {
      label: 'Comissió',
      value: 4,
      expedientNumber: 'EXP-4',
      approvalDate: '2026-07-14',
      commissionType: CommissionType.TECNICA,
    },
  ],
  administrativeUnits: [{ label: 'Unitat', value: 5 }],
};

const INFRASTRUCTURE_OPTIONS: ApplicationInfrastructureFilterOptions = {
  databases: [{ label: 'INVAI', value: 8 }],
  servers: [{ label: 'app01.caib.es', value: 5 }],
  environments: [{ label: 'Producció', value: 3 }],
};

describe('ApplicationFiltersForm', () => {
  let fixture: ComponentFixture<ApplicationFiltersForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [ApplicationFiltersForm] }).compileComponents();
    fixture = TestBed.createComponent(ApplicationFiltersForm);
    fixture.componentRef.setInput('form', createApplicationFiltersForm(new FormBuilder()));
    fixture.componentRef.setInput('labels', LABELS);
    fixture.componentRef.setInput('options', OPTIONS);
    fixture.componentRef.setInput('infrastructureOptions', INFRASTRUCTURE_OPTIONS);
    fixture.detectChanges();
  });

  it('should render context-prefixed ids and the inline incomplete toggle', () => {
    expect(fixture.nativeElement.querySelector('#applications-filter-prefix')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('#applications-filter-description')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('#applications-filter-responsible')).toBeTruthy();

    const responsible = fixture.debugElement.query(By.directive(AutoComplete))
      .componentInstance as AutoComplete;
    expect(responsible.inputId).toBe('applications-filter-responsible');
    expect(responsible.ariaLabelledBy).toBe('applications-filter-responsible-label');
    expect(responsible.forceSelection).toBe(true);
    expect(responsible.optionLabel).toBe('label');
    expect(responsible.minLength).toBe(1);
    expect(responsible.showClear).toBe(true);

    const toggleDebug = fixture.debugElement.query(By.directive(ToggleSwitch));
    const toggle = toggleDebug.componentInstance as ToggleSwitch;
    expect(toggle.inputId).toBe('applications-filter-incomplete');
    expect(toggle.ariaLabelledBy).toBe('applications-filter-incomplete-label');
    expect(toggleDebug.nativeElement.parentElement.classList.contains('items-center')).toBe(true);
  });

  it('should use the application and infrastructure catalog options', () => {
    const selects = fixture.debugElement
      .queryAll(By.directive(Select))
      .map((debugElement) => debugElement.componentInstance as Select);

    expect(selects[0]!.options).toEqual(OPTIONS.categories);
    expect(selects[3]!.options).toEqual(OPTIONS.commissions);
    expect(selects[5]!.options).toEqual([
      { label: 'Actiu', value: 1 },
      { label: 'Inactiu', value: 2 },
    ]);
    expect(selects[6]!.inputId).toBe('applications-filter-database');
    expect(selects[6]!.ariaLabelledBy).toBe('applications-filter-database-label');
    expect(selects[6]!.options).toEqual(INFRASTRUCTURE_OPTIONS.databases);
    expect(selects[7]!.options).toEqual(INFRASTRUCTURE_OPTIONS.servers);
    expect(selects[8]!.options).toEqual(INFRASTRUCTURE_OPTIONS.environments);
  });

  it('should filter catalog selectors but not the local status selector', () => {
    const selects = fixture.debugElement
      .queryAll(By.directive(Select))
      .map((debugElement) => debugElement.componentInstance as Select);
    const status = selects.find(
      (select) => select.inputId === 'applications-filter-status',
    );
    const catalogs = selects.filter((select) => select !== status);

    expect(catalogs).toHaveLength(8);
    expect(catalogs.every((select) => select.filter === true)).toBe(true);
    expect(catalogs.every((select) => Boolean(select.ariaFilterLabel))).toBe(
      true,
    );
    expect(status?.filter).toBeFalsy();
    expect(status?.ariaFilterLabel).toBeFalsy();
  });
});
