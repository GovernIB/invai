import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActionParams, KeyLabel } from '@models/table.model';
import { MenuItem } from 'primeng/api';
import { Menu } from 'primeng/menu';

import { ApplicationProviderOutput } from '../../applications.model';
import {
  ApplicationProvidersTable,
  ApplicationProviderTableAction,
} from './application-providers-table';

class ResizeObserverMock implements ResizeObserver {
  disconnect(): void {}
  observe(): void {}
  unobserve(): void {}
}

globalThis.ResizeObserver ??= ResizeObserverMock;

const COLUMNS: KeyLabel[] = [
  { key: 'companyName', label: 'Raó social', sortBy: 'companyName' },
  { key: 'role', label: 'Rol', sortBy: 'role.name' },
  { key: 'startDate', label: 'Data inici', sortBy: 'startDate' },
  { key: 'expireDate', label: 'Data fi', sortBy: 'expireDate' },
];

const PROVIDER: ApplicationProviderOutput = {
  id: 4,
  companyName: 'Plexus SL',
  role: {
    id: 3,
    name: 'Desenvolupament',
    nameEs: 'Desarrollo',
    deletedAt: null,
  },
  startDate: '2026-05-02T00:00:00',
  expireDate: null,
  deletedAt: null,
};

describe('ApplicationProvidersTable', () => {
  let fixture: ComponentFixture<ApplicationProvidersTable>;
  let component: ApplicationProvidersTable;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ApplicationProvidersTable],
    }).compileComponents();

    fixture = TestBed.createComponent(ApplicationProvidersTable);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('columns', COLUMNS);
    fixture.componentRef.setInput('itemsList', {
      items: [PROVIDER],
      total: 1,
    });
    fixture.detectChanges();
  });

  it('opens view with double click or Enter without hijacking row controls', () => {
    const emitted: ActionParams<ApplicationProviderOutput>[] = [];
    component.onSelectAction.subscribe((event) => emitted.push(event));
    const row = fixture.nativeElement.querySelector(
      '.invai-table-consultable-row',
    ) as HTMLTableRowElement;

    row.dispatchEvent(new MouseEvent('dblclick', { bubbles: true }));
    row.dispatchEvent(new KeyboardEvent('keydown', { bubbles: true, key: 'Enter' }));
    const actionsButton = row.querySelector('button') as HTMLButtonElement;
    actionsButton.dispatchEvent(new MouseEvent('dblclick', { bubbles: true }));

    expect(emitted).toEqual([
      { action: ApplicationProviderTableAction.View, params: PROVIDER },
      { action: ApplicationProviderTableAction.View, params: PROVIDER },
    ]);
  });

  it('hides actions outside edit mode and keeps row consultation available', () => {
    const emitted: ActionParams<ApplicationProviderOutput>[] = [];
    component.onSelectAction.subscribe((event) => emitted.push(event));
    fixture.componentRef.setInput('isReadOnly', true);
    fixture.componentRef.setInput('showActions', false);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.invai-table-actions-column')).toBeNull();
    expect(fixture.nativeElement.querySelector('p-menu')).toBeNull();
    const row = fixture.nativeElement.querySelector(
      '.invai-table-consultable-row',
    ) as HTMLTableRowElement;
    row.dispatchEvent(new KeyboardEvent('keydown', { bubbles: true, key: 'Enter' }));

    expect(emitted).toEqual([{ action: ApplicationProviderTableAction.View, params: PROVIDER }]);
  });

  it('keeps visible edit-mode actions disabled when mutations are unavailable', () => {
    fixture.componentRef.setInput('isReadOnly', true);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelectorAll('.invai-table-actions-column')).toHaveLength(2);
    expect(
      (fixture.nativeElement.querySelector('tbody button') as HTMLButtonElement).disabled,
    ).toBe(true);
  });

  it('emits contextual view, edit and delete actions for the selected row', () => {
    const emitted: ActionParams<ApplicationProviderOutput>[] = [];
    const table = component as unknown as {
      openActionsMenu(event: Event, row: ApplicationProviderOutput, menu: Menu): void;
      rowActions: () => MenuItem[];
    };
    const menu = { toggle: vi.fn() } as unknown as Menu;
    const event = new Event('click');
    component.onSelectAction.subscribe((action) => emitted.push(action));

    table.openActionsMenu(event, PROVIDER, menu);
    table.rowActions().forEach(({ command }) => (command as () => void)());

    expect(menu.toggle).toHaveBeenCalledWith(event);
    expect(emitted.map(({ action }) => action)).toEqual([
      ApplicationProviderTableAction.View,
      ApplicationProviderTableAction.Edit,
      ApplicationProviderTableAction.Delete,
    ]);
  });

  it('uses an accessible transparent loading layer instead of the PrimeNG mask', () => {
    fixture.componentRef.setInput('isLoading', true);
    fixture.detectChanges();

    const container = fixture.nativeElement.querySelector(
      '.invai-table-loading-container',
    ) as HTMLElement;
    expect(container.getAttribute('aria-busy')).toBe('true');
    expect(container.querySelector('.invai-table-loading-shield')).toBeTruthy();
    expect(container.querySelector('.invai-table-refresh-indicator')).toBeTruthy();
    expect(container.querySelector('.p-datatable-mask')).toBeFalsy();
    expect(container.textContent).toContain('Plexus SL');
  });
});
