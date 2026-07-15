import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';
import { By } from '@angular/platform-browser';
import { Select } from 'primeng/select';
import { ToggleSwitch } from 'primeng/toggleswitch';

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
  incomplete: 'Incomplets',
};

const OPTIONS: ApplicationSelectOptions = {
  categories: [{ label: 'Categoria', value: 1 }],
  informationSystems: [{ label: 'Sistema', value: 2 }],
  scopes: [{ label: 'Àmbit', value: 3 }],
  commissions: [{ label: 'Comissió', value: 4 }],
  administrativeUnits: [{ label: 'Unitat', value: 5 }],
};

describe('ApplicationFiltersForm', () => {
  let fixture: ComponentFixture<ApplicationFiltersForm>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [ApplicationFiltersForm] }).compileComponents();
    fixture = TestBed.createComponent(ApplicationFiltersForm);
    fixture.componentRef.setInput('form', createApplicationFiltersForm(new FormBuilder()));
    fixture.componentRef.setInput('labels', LABELS);
    fixture.componentRef.setInput('options', OPTIONS);
    fixture.detectChanges();
  });

  it('should render context-prefixed ids and the inline incomplete toggle', () => {
    expect(fixture.nativeElement.querySelector('#applications-filter-prefix')).toBeTruthy();
    expect(fixture.nativeElement.querySelector('#applications-filter-description')).toBeTruthy();

    const toggleDebug = fixture.debugElement.query(By.directive(ToggleSwitch));
    const toggle = toggleDebug.componentInstance as ToggleSwitch;
    expect(toggle.inputId).toBe('applications-filter-incomplete');
    expect(toggle.ariaLabelledBy).toBe('applications-filter-incomplete-label');
    expect(toggleDebug.nativeElement.parentElement.classList.contains('items-center')).toBe(true);
  });

  it('should use dynamic ids for catalog selectors and fixed ids for statuses', () => {
    const selects = fixture.debugElement
      .queryAll(By.directive(Select))
      .map((debugElement) => debugElement.componentInstance as Select);

    expect(selects[0]!.options).toEqual(OPTIONS.categories);
    expect(selects[3]!.options).toEqual(OPTIONS.commissions);
    expect(selects[5]!.options).toEqual([
      { label: 'Actiu', value: 1 },
      { label: 'Manteniment', value: 2 },
      { label: 'Deprecat', value: 3 },
    ]);
  });
});
