import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActionParams } from '@models/table.model';
import { MenuItem } from 'primeng/api';
import { Menu } from 'primeng/menu';

import { ApplicationIntegrationConnectionOutput } from '../../../../applications.model';
import { APPLICATION_INTEGRATIONS_COLUMNS } from './application-integrations-section.i18n';
import {
  ApplicationIntegrationsTable,
  ApplicationIntegrationsTableAction,
} from './application-integrations-table';

class ResizeObserverMock implements ResizeObserver {
  disconnect(): void {}
  observe(): void {}
  unobserve(): void {}
}

globalThis.ResizeObserver ??= ResizeObserverMock;

const CONNECTIONS: ApplicationIntegrationConnectionOutput[] = [
  {
    id: 1,
    appIntegrationId: 13,
    application: null,
    externalSystem: { id: 2, name: 'Soffid', company: null, deletedAt: null },
    technology: { id: 4, name: 'Spring Boot', layer: { id: 1, name: 'Backend', deletedAt: null }, deletedAt: null },
    username: 'u00004',
    requiredRoles: [
      { id: 26, name: 'INV_ADMIN', description: null, system: 'SEYCON' },
      { id: 33, name: 'INV_READ', description: null, system: 'SEYCON' },
    ],
    grantedRoles: [{ id: 26, name: 'INV_ADMIN', description: null, system: 'SEYCON' }],
    rolesMismatch: true,
    deletedAt: null,
  },
  {
    id: 2,
    appIntegrationId: 13,
    application: { id: 8, code: 'APP-002', name: 'Portal Salut' },
    externalSystem: null,
    technology: null,
    username: 'u00005',
    requiredRoles: [{ id: 26, name: 'INV_ADMIN', description: null, system: null }],
    grantedRoles: [{ id: 26, name: 'INV_ADMIN', description: null, system: null }],
    rolesMismatch: false,
    deletedAt: null,
  },
  {
    id: 3,
    appIntegrationId: 13,
    application: { id: 9, code: 'APP-003', name: 'Registre' },
    externalSystem: null,
    technology: null,
    username: 'u00006',
    requiredRoles: [],
    grantedRoles: null,
    rolesMismatch: null,
    deletedAt: null,
  },
];

describe('ApplicationIntegrationsTable', () => {
  let component: ApplicationIntegrationsTable;
  let fixture: ComponentFixture<ApplicationIntegrationsTable>;

  const root = () => fixture.nativeElement as HTMLElement;
  const rows = () => [...root().querySelectorAll<HTMLTableRowElement>('tbody tr')];
  const cells = (row: Element) =>
    [...row.querySelectorAll('td')].map((cell) => cell.textContent?.replace(/\s+/g, ' ').trim());

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ApplicationIntegrationsTable],
    }).compileComponents();

    fixture = TestBed.createComponent(ApplicationIntegrationsTable);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('columns', APPLICATION_INTEGRATIONS_COLUMNS);
    fixture.componentRef.setInput('itemsList', { items: CONNECTIONS, total: CONNECTIONS.length });
    fixture.detectChanges();
  });

  it('renders only the backend columns and sorts by persisted fields', () => {
    const headers = [...root().querySelectorAll('thead th')];

    expect(headers.map((header) => header.textContent?.trim())).toEqual([
      'Sistema',
      'Tecnologia',
      "Usuari d'integració",
      'Rols requerits',
      'Rols atorgats',
      'Coincidència de rols',
    ]);
    expect(headers.map((header) => header.querySelector('p-sorticon') != null)).toEqual([
      false,
      true,
      true,
      false,
      false,
      false,
    ]);
    expect(root().querySelector('.p-datatable-striped')).not.toBeNull();
  });

  it('renders the system kind, roles and the three role comparison states as text', () => {
    expect(cells(rows()[0])).toEqual([
      'Soffid, Sistema extern',
      'Spring Boot',
      'u00004',
      'INV_ADMIN, INV_READ',
      'INV_ADMIN',
      'No coincideixen: Els rols atorgats a Soffid no coincideixen amb els rols requerits.',
    ]);
    expect(cells(rows()[1])).toEqual([
      "Portal Salut, Aplicació de l'inventari",
      '-',
      'u00005',
      'INV_ADMIN',
      'INV_ADMIN',
      'Coincideixen',
    ]);
    expect(cells(rows()[2]).slice(3)).toEqual(['-', 'No disponible', "No s'ha pogut comprovar"]);
    expect(rows()[0].querySelector('.pi-exclamation-triangle')?.getAttribute('aria-hidden')).toBe('true');
  });

  it('keeps rows consultable by double click and Enter without the action column', () => {
    const emitted: ActionParams<ApplicationIntegrationConnectionOutput>[] = [];
    component.onSelectAction.subscribe((action) => emitted.push(action));

    expect(root().querySelector('.invai-table-actions-column')).toBeNull();
    expect(rows()[0].getAttribute('tabindex')).toBe('0');
    rows()[0].dispatchEvent(new MouseEvent('dblclick', { bubbles: true }));
    rows()[1].dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter', bubbles: true }));

    expect(emitted).toEqual([
      { action: ApplicationIntegrationsTableAction.View, params: CONNECTIONS[0] },
      { action: ApplicationIntegrationsTableAction.View, params: CONNECTIONS[1] },
    ]);
  });

  it('exposes the row menu relationship on the native trigger and emits the chosen action', () => {
    const emitted: ActionParams<ApplicationIntegrationConnectionOutput>[] = [];
    component.onSelectAction.subscribe((action) => emitted.push(action));
    fixture.componentRef.setInput('showActions', true);
    fixture.detectChanges();

    const trigger = rows()[1].querySelector<HTMLButtonElement>('.invai-table-actions-column button')!;
    expect(trigger.getAttribute('aria-haspopup')).toBe('menu');
    expect(trigger.getAttribute('aria-expanded')).toBe('false');
    expect(trigger.getAttribute('aria-label')).toBe('Obre les accions del registre');

    const table = component as unknown as {
      openActionsMenu: (event: Event, row: ApplicationIntegrationConnectionOutput, menu: Menu) => void;
      rowActions: () => MenuItem[];
    };
    table.openActionsMenu(new MouseEvent('click'), CONNECTIONS[1], { toggle: vi.fn() } as unknown as Menu);
    table.rowActions()[2].command?.({});

    expect(emitted).toEqual([
      { action: ApplicationIntegrationsTableAction.Delete, params: CONNECTIONS[1] },
    ]);
  });

  it('disables mutations in read-only mode while keeping consultation', () => {
    fixture.componentRef.setInput('showActions', true);
    fixture.componentRef.setInput('isReadOnly', true);
    fixture.detectChanges();
    const actions = (component as unknown as { rowActions: () => MenuItem[] }).rowActions();

    expect(actions.map((action) => [action.label, !!action.disabled])).toEqual([
      ['Consulta', false],
      ['Edita', true],
      ['Dona de baixa', true],
    ]);
  });

  it('keeps the refresh layer without the PrimeNG mask', () => {
    fixture.componentRef.setInput('isLoading', true);
    fixture.detectChanges();

    expect(root().querySelector('.invai-table-loading-container')?.getAttribute('aria-busy')).toBe('true');
    expect(root().querySelector('.invai-table-refresh-indicator')).not.toBeNull();
    expect(root().querySelector('.p-datatable-mask')).toBeNull();
    expect(rows()).toHaveLength(3);
  });
});
