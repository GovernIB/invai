import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';
import { By } from '@angular/platform-browser';
import { Select } from 'primeng/select';

import { createTechnologyFiltersForm } from '../forms/technology-filters-form.factory';
import { createTechnologyForm } from '../forms/technology-form.factory';
import { TechnologyDialog } from './technology-dialog/technology-dialog';
import { TechnologyFiltersForm } from './technology-filters-form/technology-filters-form';

describe('technology catalog select filtering', () => {
  const formBuilder = new FormBuilder();

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TechnologyDialog, TechnologyFiltersForm],
    }).compileComponents();
  });

  it('filters the layer maintenance in the technology dialog', () => {
    const fixture = TestBed.createComponent(TechnologyDialog);
    fixture.componentRef.setInput('visible', true);
    fixture.componentRef.setInput('mode', 'create');
    fixture.componentRef.setInput('form', createTechnologyForm(formBuilder));
    fixture.componentRef.setInput('layerOptions', [
      { id: 1, label: 'Frontend' },
    ]);
    fixture.detectChanges();

    const layer = selectById(fixture, 'technology-dialog-layer');
    expect(layer.filter).toBe(true);
    expect(layer.ariaFilterLabel).toBeTruthy();
  });

  it('filters the layer catalog but not the local status filter', () => {
    const fixture = TestBed.createComponent(TechnologyFiltersForm);
    fixture.componentRef.setInput(
      'form',
      createTechnologyFiltersForm(formBuilder),
    );
    fixture.componentRef.setInput('labels', {
      name: 'Tecnologia',
      layer: 'Capa',
      status: 'Estat',
    });
    fixture.componentRef.setInput('layerOptions', [
      { id: 1, label: 'Frontend' },
    ]);
    fixture.detectChanges();

    const layer = selectById(fixture, 'technologies-filter-layer');
    const status = selectById(fixture, 'technologies-filter-status');

    expect(layer.filter).toBe(true);
    expect(layer.ariaFilterLabel).toBe('Capa');
    expect(status.filter).toBeFalsy();
    expect(status.ariaFilterLabel).toBeFalsy();
  });
});

function selectById<T>(
  fixture: ComponentFixture<T>,
  inputId: string,
): Select {
  return fixture.debugElement
    .queryAll(By.directive(Select))
    .map(({ componentInstance }) => componentInstance as Select)
    .find((select) => select.inputId === inputId)!;
}
