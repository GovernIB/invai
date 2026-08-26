import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActionParams, KeyLabel } from '@models/table.model';

import { ApplicationTechnologyOutput } from '../../applications.model';
import {
  ApplicationTechnologiesTable,
  ApplicationTechnologyTableAction,
} from './application-technologies-table';

class ResizeObserverMock implements ResizeObserver {
  disconnect(): void {}
  observe(): void {}
  unobserve(): void {}
}

globalThis.ResizeObserver ??= ResizeObserverMock;

const COLUMNS: KeyLabel[] = [
  { key: 'layer', label: 'Capa', sortBy: 'layer.name' },
  { key: 'technology', label: 'Tecnologia', sortBy: 'technology.name' },
  { key: 'version', label: 'Versió', sortBy: 'version' },
  { key: 'architecture', label: 'Arquitectura', sortBy: 'architecture' },
];

const TECHNOLOGY: ApplicationTechnologyOutput = {
  id: 5,
  layer: { id: 1, name: 'Frontend', deletedAt: null },
  technology: {
    id: 2,
    name: 'Angular',
    layer: { id: 1, name: 'Frontend', deletedAt: null },
    deletedAt: null,
  },
  version: '21',
  architecture: 'Monolítica',
  deletedAt: null,
};

describe('ApplicationTechnologiesTable', () => {
  let fixture: ComponentFixture<ApplicationTechnologiesTable>;
  let component: ApplicationTechnologiesTable;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ApplicationTechnologiesTable],
    }).compileComponents();

    fixture = TestBed.createComponent(ApplicationTechnologiesTable);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('columns', COLUMNS);
    fixture.componentRef.setInput('itemsList', {
      items: [TECHNOLOGY],
      total: 1,
    });
    fixture.detectChanges();
  });

  it('opens view with double click and keeps consultation in read-only mode', () => {
    const emitted: ActionParams<ApplicationTechnologyOutput>[] = [];
    component.onSelectAction.subscribe((event) => emitted.push(event));
    fixture.componentRef.setInput('isReadOnly', true);
    fixture.componentRef.setInput('showActions', false);
    fixture.detectChanges();

    const row = fixture.nativeElement.querySelector(
      '.invai-table-consultable-row',
    ) as HTMLTableRowElement;
    row.dispatchEvent(new MouseEvent('dblclick', { bubbles: true }));

    expect(emitted).toEqual([
      { action: ApplicationTechnologyTableAction.View, params: TECHNOLOGY },
    ]);
    expect(fixture.nativeElement.querySelector('.invai-table-actions-column')).toBeFalsy();
    expect(fixture.nativeElement.querySelector('p-menu')).toBeNull();
  });

  it('keeps visible edit-mode actions disabled when mutations are unavailable', () => {
    fixture.componentRef.setInput('isReadOnly', true);
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelectorAll('.invai-table-actions-column')).toHaveLength(2);
    expect(
      (fixture.nativeElement.querySelector('tbody button') as HTMLButtonElement).disabled,
    ).toBe(true);
  });

  it('shows the progress indicator without replacing existing rows', () => {
    fixture.componentRef.setInput('isLoading', true);
    fixture.detectChanges();

    const container = fixture.nativeElement.querySelector(
      '.invai-table-loading-container',
    ) as HTMLElement;
    expect(container.getAttribute('aria-busy')).toBe('true');
    expect(container.querySelector('.invai-table-refresh-indicator')).toBeTruthy();
    expect(container.querySelector('.p-datatable-mask')).toBeFalsy();
    expect(container.textContent).toContain('Angular');
  });
});
