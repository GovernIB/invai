import { Type } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder } from '@angular/forms';
import { By } from '@angular/platform-browser';
import { Button } from 'primeng/button';

import { CategoryDialog } from '@features/categories/components/category-dialog/category-dialog';
import { createCategoryForm } from '@features/categories/forms/category-form.factory';
import { CommissionDialog } from '@features/commissions/components/commission-dialog/commission-dialog';
import { createCommissionForm } from '@features/commissions/forms/commission-form.factory';
import { EnvironmentDialog } from '@features/environments/components/environment-dialog/environment-dialog';
import { createEnvironmentForm } from '@features/environments/forms/environment-form.factory';
import { FieldDialog } from '@features/fields/components/field-dialog/field-dialog';
import { createFieldForm } from '@features/fields/forms/field-form.factory';
import { SystemTypeDialog } from '@features/system-types/components/system-type-dialog/system-type-dialog';
import { createSystemTypeForm } from '@features/system-types/forms/system-type-form.factory';
import { LayerDialog } from '@features/layers/components/layer-dialog/layer-dialog';
import { createLayerForm } from '@features/layers/forms/layer-form.factory';
import { RoleDialog } from '@features/roles/components/role-dialog/role-dialog';
import { createRoleForm } from '@features/roles/forms/role-form.factory';
import { TechnologyDialog } from '@features/technologies/components/technology-dialog/technology-dialog';
import { createTechnologyForm } from '@features/technologies/forms/technology-form.factory';

interface DialogScenario {
  name: string;
  component: Type<unknown>;
  createForm: (formBuilder: FormBuilder) => unknown;
  inputs?: Record<string, unknown>;
}

const SCENARIOS: DialogScenario[] = [
  { name: 'category', component: CategoryDialog, createForm: createCategoryForm },
  { name: 'commission', component: CommissionDialog, createForm: createCommissionForm },
  { name: 'environment', component: EnvironmentDialog, createForm: createEnvironmentForm },
  { name: 'field', component: FieldDialog, createForm: createFieldForm },
  { name: 'system type', component: SystemTypeDialog, createForm: createSystemTypeForm },
  { name: 'role', component: RoleDialog, createForm: createRoleForm },
  { name: 'layer', component: LayerDialog, createForm: createLayerForm },
  {
    name: 'technology',
    component: TechnologyDialog,
    createForm: createTechnologyForm,
    inputs: { layerOptions: [] },
  },
];

describe('maintenance dialog actions', () => {
  for (const scenario of SCENARIOS) {
    it(`renders the active and inactive view actions for ${scenario.name}`, async () => {
      await TestBed.configureTestingModule({ imports: [scenario.component] }).compileComponents();
      const fixture: ComponentFixture<unknown> = TestBed.createComponent(scenario.component);
      fixture.componentRef.setInput('visible', true);
      fixture.componentRef.setInput('mode', 'view');
      fixture.componentRef.setInput('canRestore', false);
      fixture.componentRef.setInput('form', scenario.createForm(new FormBuilder()));
      Object.entries(scenario.inputs ?? {}).forEach(([key, value]) =>
        fixture.componentRef.setInput(key, value),
      );
      fixture.detectChanges();

      const restore = vi.fn();
      (
        fixture.componentInstance as unknown as {
          restore: { subscribe: (callback: () => void) => void };
        }
      ).restore.subscribe(restore);

      expect(footerButtonLabels(fixture)).toEqual(['Editar', 'Acceptar']);
      expect(buttonByLabel(fixture, 'Acceptar').icon).toBeUndefined();

      fixture.componentRef.setInput('canRestore', true);
      fixture.detectChanges();
      expect(footerButtonLabels(fixture)).toEqual(['Restaurar', 'Acceptar']);
      buttonByLabel(fixture, 'Restaurar').onClick.emit(new MouseEvent('click'));
      expect(restore).toHaveBeenCalledOnce();

      fixture.destroy();
    });
  }
});

function footerButtonLabels(fixture: ComponentFixture<unknown>): string[] {
  return fixture.debugElement
    .queryAll(By.directive(Button))
    .map(({ componentInstance }) => (componentInstance as Button).label)
    .filter((label): label is string => Boolean(label));
}

function buttonByLabel(fixture: ComponentFixture<unknown>, label: string): Button {
  return fixture.debugElement
    .queryAll(By.directive(Button))
    .map(({ componentInstance }) => componentInstance as Button)
    .find((button) => button.label === label)!;
}
