import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActionParams } from '@models/table.model';
import { MenuItem } from 'primeng/api';
import { Menu } from 'primeng/menu';

import { EXTERNAL_SYSTEMS_TABLE_COLUMNS } from '../../external-systems.constants';
import { ExternalSystem } from '../../external-systems.model';
import { ExternalSystemsTable, ExternalSystemTableAction } from './external-systems-table';

class ResizeObserverMock implements ResizeObserver {
  disconnect(): void {}
  observe(): void {}
  unobserve(): void {}
}

globalThis.ResizeObserver ??= ResizeObserverMock;

const ACTIVE: ExternalSystem = {
  id: 1,
  name: 'Soffid',
  company: { id: 3, name: 'Plexus Tech', nif: null as never, deletedAt: null },
  deletedAt: null,
};
const INACTIVE: ExternalSystem = { ...ACTIVE, id: 2, name: 'Portal antic', deletedAt: '2026-02-01T00:00:00' };

describe('ExternalSystemsTable', () => {
  let fixture: ComponentFixture<ExternalSystemsTable>;
  const root = () => fixture.nativeElement as HTMLElement;
  const rows = () => [...root().querySelectorAll<HTMLTableRowElement>('.p-datatable-tbody > tr')];

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [ExternalSystemsTable] }).compileComponents();
    fixture = TestBed.createComponent(ExternalSystemsTable);
    fixture.componentRef.setInput('columns', EXTERNAL_SYSTEMS_TABLE_COLUMNS);
    fixture.componentRef.setInput('itemsList', { items: [ACTIVE, INACTIVE], total: 2 });
    fixture.detectChanges();
  });

  it('renders the name, company and status of every system', () => {
    const cells = (row: Element) =>
      [...row.querySelectorAll('td')].slice(0, 3).map((cell) => cell.textContent?.trim());

    expect(cells(rows()[0])).toEqual(['Soffid', 'Plexus Tech', 'Actiu']);
    expect(cells(rows()[1])).toEqual(['Portal antic', 'Plexus Tech', 'Inactiu']);
    expect(root().querySelector('.p-datatable-striped')).not.toBeNull();
  });

  it('emits every contextual action and exposes the menu relationship on the native trigger', () => {
    const emitted: ActionParams<ExternalSystem>[] = [];
    fixture.componentInstance.onSelectAction.subscribe((action) => emitted.push(action));
    const trigger = rows()[0].querySelector<HTMLButtonElement>('.invai-table-actions-column button')!;

    expect(trigger.getAttribute('aria-haspopup')).toBe('menu');
    expect(trigger.getAttribute('aria-expanded')).toBe('false');
    expect(trigger.getAttribute('aria-label')).toBe('Obrir les accions del sistema extern');

    const table = fixture.componentInstance as unknown as {
      openActionsMenu: (event: Event, row: ExternalSystem, menu: Menu) => void;
      rowActions: MenuItem[];
    };
    table.openActionsMenu(new Event('click'), ACTIVE, { toggle: vi.fn() } as unknown as Menu);
    table.rowActions.forEach((item) => (item.command as () => void)());

    expect(table.rowActions.map((item) => item.label)).toEqual(['Consulta', 'Edita', 'Dona de baixa']);
    expect(emitted.map(({ action }) => action)).toEqual([
      ExternalSystemTableAction.View,
      ExternalSystemTableAction.Edit,
      ExternalSystemTableAction.Delete,
    ]);
  });

  it('opens a consultation by double click or Enter, including inactive systems', () => {
    const emitted: ActionParams<ExternalSystem>[] = [];
    fixture.componentInstance.onSelectAction.subscribe((action) => emitted.push(action));

    rows()[0].dispatchEvent(new MouseEvent('dblclick', { bubbles: true }));
    rows()[1].dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter', bubbles: true }));

    expect(emitted).toEqual([
      { action: ExternalSystemTableAction.View, params: ACTIVE },
      { action: ExternalSystemTableAction.View, params: INACTIVE },
    ]);
  });

  it('offers restoration instead of the mutation menu for inactive systems', () => {
    expect(rows()[1].querySelector('app-restore-record-menu')).not.toBeNull();
    expect(rows()[0].querySelector('app-restore-record-menu')).toBeNull();
  });
});
