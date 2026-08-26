import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { ActionParams } from '@models/table.model';
import { MenuItem } from 'primeng/api';
import { Menu } from 'primeng/menu';

import { APPLICATION_SERVERS_TABLE_COLUMNS } from '../../applications.constants';
import { ApplicationInfrastructureResource, ApplicationServer } from '../../applications.model';
import {
  ApplicationInfrastructureTable,
  ApplicationInfrastructureTableAction,
} from './application-infrastructure-table';

class ResizeObserverMock implements ResizeObserver {
  disconnect(): void {}
  observe(): void {}
  unobserve(): void {}
}

globalThis.ResizeObserver ??= ResizeObserverMock;

const SERVER: ApplicationServer = {
  id: 1,
  informationSystemDbId: 70,
  systemId: 5,
  deletedAt: null,
  environment: 'Producció',
  server: 'app01.caib.es',
  instance: 'jboss',
  port: 8080,
  version: '7.4',
  status: 'Actiu',
  observations: '',
  catalogItem: {
    id: 5,
    environment: 'Producció',
    server: 'app01.caib.es',
    instance: 'jboss',
    port: 8080,
    version: '7.4',
    description: '',
    source: null!,
  },
};

describe('ApplicationInfrastructureTable', () => {
  let component: ApplicationInfrastructureTable;
  let fixture: ComponentFixture<ApplicationInfrastructureTable>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ApplicationInfrastructureTable],
    }).compileComponents();

    fixture = TestBed.createComponent(ApplicationInfrastructureTable);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('columns', APPLICATION_SERVERS_TABLE_COLUMNS);
    fixture.componentRef.setInput('itemsList', {
      items: [SERVER],
      total: 1,
    });
    fixture.componentRef.setInput('first', 20);
    fixture.detectChanges();
  });

  it('emits view and delete from the contextual menu for the selected row', () => {
    const emittedActions: ActionParams<ApplicationInfrastructureResource>[] = [];
    const table = component as unknown as {
      openActionsMenu: (event: Event, row: ApplicationInfrastructureResource, menu: Menu) => void;
      rowActions: () => MenuItem[];
    };
    const menu = { toggle: vi.fn() } as unknown as Menu;
    const event = new Event('click');

    component.onSelectAction.subscribe((action) => emittedActions.push(action));
    table.openActionsMenu(event, SERVER, menu);
    table.rowActions().forEach(({ command }) => (command as () => void)());

    expect(menu.toggle).toHaveBeenCalledWith(event);
    expect(emittedActions).toEqual([
      {
        action: ApplicationInfrastructureTableAction.View,
        params: SERVER,
      },
      {
        action: ApplicationInfrastructureTableAction.Delete,
        params: SERVER,
      },
    ]);
  });

  it('uses action visibility independently from temporary mutation availability', () => {
    expect(fixture.nativeElement.querySelectorAll('.invai-table-actions-column')).toHaveLength(2);

    fixture.componentRef.setInput('isReadOnly', true);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelectorAll('.invai-table-actions-column')).toHaveLength(2);
    expect(
      (fixture.nativeElement.querySelector('tbody button') as HTMLButtonElement).disabled,
    ).toBe(true);

    fixture.componentRef.setInput('showActions', false);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelectorAll('.invai-table-actions-column')).toHaveLength(0);
    expect(fixture.nativeElement.querySelector('p-menu')).toBeNull();
  });

  it('keeps row view activation available while the actions column is hidden', () => {
    const emittedActions: ActionParams<ApplicationInfrastructureResource>[] = [];
    fixture.componentRef.setInput('isReadOnly', true);
    fixture.componentRef.setInput('showActions', false);
    fixture.detectChanges();

    component.onSelectAction.subscribe((action) => emittedActions.push(action));
    const row = fixture.nativeElement.querySelector(
      '.p-datatable-tbody > .invai-table-consultable-row',
    ) as HTMLTableRowElement;
    row.dispatchEvent(new MouseEvent('dblclick', { bubbles: true }));

    expect(emittedActions).toEqual([
      {
        action: ApplicationInfrastructureTableAction.View,
        params: SERVER,
      },
    ]);
  });

  it('spans the empty state across the rendered columns in both modes', () => {
    fixture.componentRef.setInput('itemsList', { items: [], total: 0 });
    fixture.detectChanges();

    let emptyCell = fixture.nativeElement.querySelector(
      '.p-datatable-tbody > tr > td[colspan]',
    ) as HTMLTableCellElement;
    expect(emptyCell.colSpan).toBe(APPLICATION_SERVERS_TABLE_COLUMNS.length + 1);

    fixture.componentRef.setInput('showActions', false);
    fixture.detectChanges();

    emptyCell = fixture.nativeElement.querySelector(
      '.p-datatable-tbody > tr > td[colspan]',
    ) as HTMLTableCellElement;
    expect(emptyCell.colSpan).toBe(APPLICATION_SERVERS_TABLE_COLUMNS.length);
  });

  it('forwards the controlled first row and enables striped rows', () => {
    const primeTable = fixture.debugElement.query(By.css('p-table'));

    expect(primeTable.componentInstance.first).toBe(20);
    expect(primeTable.componentInstance.stripedRows).toBe(true);
  });

  it('shows a progress bar over the preserved rows without a dark table mask', () => {
    fixture.componentRef.setInput('isLoading', true);
    fixture.detectChanges();

    const container = fixture.nativeElement.querySelector(
      '.invai-table-loading-container',
    ) as HTMLElement;
    expect(container.getAttribute('aria-busy')).toBe('true');
    expect(container.querySelector('.invai-table-loading-shield')).toBeTruthy();
    expect(container.querySelector('.invai-table-refresh-indicator')).toBeTruthy();
    expect(container.querySelector('.p-datatable-mask')).toBeFalsy();
    expect(container.textContent).toContain('app01.caib.es');
  });
});
