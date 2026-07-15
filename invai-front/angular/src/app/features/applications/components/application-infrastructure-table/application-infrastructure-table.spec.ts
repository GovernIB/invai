import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActionParams } from '@models/table.model';
import { MenuItem } from 'primeng/api';
import { Menu } from 'primeng/menu';

import {
  APPLICATION_SERVERS_SEED_DATA,
  APPLICATION_SERVERS_TABLE_COLUMNS,
} from '../../applications.constants';
import { ApplicationInfrastructureResource } from '../../applications.model';
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
      items: APPLICATION_SERVERS_SEED_DATA,
      total: APPLICATION_SERVERS_SEED_DATA.length,
    });
    fixture.detectChanges();
  });

  it('should emit the selected row and contextual action', () => {
    const emittedActions: ActionParams<ApplicationInfrastructureResource>[] = [];
    const table = component as unknown as {
      openActionsMenu: (
        event: Event,
        row: ApplicationInfrastructureResource,
        menu: Menu,
      ) => void;
      rowActions: MenuItem[];
    };
    const menu = { toggle: vi.fn() } as unknown as Menu;
    const event = new Event('click');

    component.onSelectAction.subscribe((action) => emittedActions.push(action));
    table.openActionsMenu(event, APPLICATION_SERVERS_SEED_DATA[0], menu);
    (table.rowActions[0].command as () => void)();

    expect(menu.toggle).toHaveBeenCalledWith(event);
    expect(emittedActions).toEqual([
      {
        action: ApplicationInfrastructureTableAction.Edit,
        params: APPLICATION_SERVERS_SEED_DATA[0],
      },
    ]);
  });
});
