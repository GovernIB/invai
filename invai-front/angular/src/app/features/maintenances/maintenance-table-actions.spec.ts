import { Type } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActionParams } from '@models/table.model';
import { MenuItem } from 'primeng/api';
import { Menu } from 'primeng/menu';

import {
  CategoriesTable,
  CategoryTableAction,
} from '@features/categories/components/categories-table/categories-table';
import {
  CommissionsTable,
  CommissionTableAction,
} from '@features/commissions/components/commissions-table/commissions-table';
import {
  EnvironmentsTable,
  EnvironmentTableAction,
} from '@features/environments/components/environments-table/environments-table';
import {
  FieldsTable,
  FieldTableAction,
} from '@features/fields/components/fields-table/fields-table';
import {
  SystemTypesTable,
  SystemTypeTableAction,
} from '@features/system-types/components/system-types-table/system-types-table';
import {
  InfrastructureTable,
  InfrastructureTableAction,
} from '@features/systems/components/infrastructure-table/infrastructure-table';
import {
  LayersTable,
  LayerTableAction,
} from '@features/layers/components/layers-table/layers-table';
import {
  RolesTable,
  RoleTableAction,
} from '@features/roles/components/roles-table/roles-table';
import {
  TechnologiesTable,
  TechnologyTableAction,
} from '@features/technologies/components/technologies-table/technologies-table';

class ResizeObserverMock implements ResizeObserver {
  disconnect(): void {}
  observe(): void {}
  unobserve(): void {}
}

globalThis.ResizeObserver ??= ResizeObserverMock;

interface MaintenanceTableHarness {
  onSelectAction: {
    subscribe: (callback: (action: ActionParams<unknown>) => void) => void;
  };
  openActionsMenu: (event: Event, row: unknown, menu: Menu) => void;
  rowActions: MenuItem[];
}

interface MaintenanceTableScenario {
  name: string;
  component: Type<unknown>;
  actions: number[];
}

const SCENARIOS: MaintenanceTableScenario[] = [
  {
    name: 'categories',
    component: CategoriesTable,
    actions: [
      CategoryTableAction.View,
      CategoryTableAction.Edit,
      CategoryTableAction.Delete,
    ],
  },
  {
    name: 'system types',
    component: SystemTypesTable,
    actions: [
      SystemTypeTableAction.View,
      SystemTypeTableAction.Edit,
      SystemTypeTableAction.Delete,
    ],
  },
  {
    name: 'environments',
    component: EnvironmentsTable,
    actions: [
      EnvironmentTableAction.View,
      EnvironmentTableAction.Edit,
      EnvironmentTableAction.Delete,
    ],
  },
  {
    name: 'fields',
    component: FieldsTable,
    actions: [FieldTableAction.View, FieldTableAction.Edit, FieldTableAction.Delete],
  },
  {
    name: 'commissions',
    component: CommissionsTable,
    actions: [
      CommissionTableAction.View,
      CommissionTableAction.Edit,
      CommissionTableAction.Delete,
    ],
  },
  {
    name: 'infrastructure',
    component: InfrastructureTable,
    actions: [
      InfrastructureTableAction.View,
      InfrastructureTableAction.Edit,
      InfrastructureTableAction.Delete,
    ],
  },
  {
    name: 'roles',
    component: RolesTable,
    actions: [RoleTableAction.View, RoleTableAction.Edit, RoleTableAction.Delete],
  },
  {
    name: 'layers',
    component: LayersTable,
    actions: [LayerTableAction.View, LayerTableAction.Edit, LayerTableAction.Delete],
  },
  {
    name: 'technologies',
    component: TechnologiesTable,
    actions: [
      TechnologyTableAction.View,
      TechnologyTableAction.Edit,
      TechnologyTableAction.Delete,
    ],
  },
];

describe('Maintenance table row actions', () => {
  for (const scenario of SCENARIOS) {
    describe(scenario.name, () => {
      let fixture: ComponentFixture<unknown>;
      let table: MaintenanceTableHarness;

      beforeEach(async () => {
        await TestBed.configureTestingModule({
          imports: [scenario.component],
        }).compileComponents();

        fixture = TestBed.createComponent(scenario.component);
        fixture.componentRef.setInput('columns', []);
        fixture.componentRef.setInput('itemsList', {
          items: [{ id: 1, deletedAt: null }],
          total: 1,
        });
        fixture.detectChanges();
        table = fixture.componentInstance as MaintenanceTableHarness;
      });

      it('opens the menu and emits every contextual action for the selected row', () => {
        const emittedActions: ActionParams<unknown>[] = [];
        const menu = { toggle: vi.fn() } as unknown as Menu;
        const event = new Event('click');
        const row = { id: 1 };

        table.onSelectAction.subscribe((action) => emittedActions.push(action));
        table.openActionsMenu(event, row, menu);
        table.rowActions.forEach((item) => (item.command as () => void)());

        expect(menu.toggle).toHaveBeenCalledWith(event);
        expect(table.rowActions.map((item) => item.label)).toEqual([
          'Consulta',
          'Edita',
          'Elimina',
        ]);
        expect(emittedActions).toEqual(
          scenario.actions.map((action) => ({
            action,
            params: row,
          })),
        );
      });

      it.each([null, '2026-07-28T00:00:00.000Z'])(
        'opens view with double click or Enter for deletedAt=%s without hijacking row controls',
        (deletedAt) => {
          const record = { id: 1, deletedAt };
          fixture.componentRef.setInput('itemsList', { items: [record], total: 1 });
          fixture.detectChanges();
          const emittedActions: ActionParams<unknown>[] = [];
          table.onSelectAction.subscribe((action) => emittedActions.push(action));
          const row = fixture.nativeElement.querySelector(
            '.p-datatable-tbody > tr',
          ) as HTMLTableRowElement;
          const actionButton = row.querySelector('button') as HTMLButtonElement;

          expect(row.tabIndex).toBe(0);

          row.dispatchEvent(new MouseEvent('dblclick', { bubbles: true }));
          row.dispatchEvent(new KeyboardEvent('keydown', { key: 'Space', bubbles: true }));
          row.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter', bubbles: true }));

          expect(emittedActions).toEqual([
            { action: scenario.actions[0], params: record },
            { action: scenario.actions[0], params: record },
          ]);

          emittedActions.length = 0;
          actionButton.dispatchEvent(new MouseEvent('dblclick', { bubbles: true }));
          actionButton.dispatchEvent(
            new KeyboardEvent('keydown', { key: 'Enter', bubbles: true }),
          );

          expect(emittedActions).toEqual([]);
        },
      );
    });
  }
});
