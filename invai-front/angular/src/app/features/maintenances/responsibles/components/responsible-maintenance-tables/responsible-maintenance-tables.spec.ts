import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RESPONSIBLE_COMPANY_COLUMNS } from '../../responsibles.constants';
import { ResponsibleCompany } from '../../responsibles.model';
import {
  ResponsibleCompaniesTable,
  ResponsibleMaintenanceTableAction,
} from './responsible-maintenance-tables';

class ResizeObserverMock implements ResizeObserver {
  disconnect(): void {}
  observe(): void {}
  unobserve(): void {}
}

globalThis.ResizeObserver ??= ResizeObserverMock;

describe('ResponsibleCompaniesTable', () => {
  let fixture: ComponentFixture<ResponsibleCompaniesTable>;
  const company: ResponsibleCompany = { id: 1, name: 'Plexus', deletedAt: null };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ResponsibleCompaniesTable],
    }).compileComponents();
    fixture = TestBed.createComponent(ResponsibleCompaniesTable);
    fixture.componentRef.setInput('columns', RESPONSIBLE_COMPANY_COLUMNS.slice(0, 1));
    fixture.componentRef.setInput('itemsList', { items: [company], total: 1 });
    fixture.detectChanges();
  });

  it('opens consultation on double click and Enter', () => {
    const actions: number[] = [];
    fixture.componentInstance.onSelectAction.subscribe(({ action }) => actions.push(action));
    const row = fixture.nativeElement.querySelector('tbody tr') as HTMLTableRowElement;

    row.dispatchEvent(new MouseEvent('dblclick', { bubbles: true }));
    row.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter', bubbles: true }));

    expect(actions).toEqual([
      ResponsibleMaintenanceTableAction.View,
      ResponsibleMaintenanceTableAction.View,
    ]);
  });

  it('ignores activation originating in an interactive descendant', () => {
    const actions: number[] = [];
    fixture.componentInstance.onSelectAction.subscribe(({ action }) => actions.push(action));
    const button = fixture.nativeElement.querySelector('tbody button') as HTMLButtonElement;

    button.dispatchEvent(new MouseEvent('dblclick', { bubbles: true }));
    button.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter', bubbles: true }));

    expect(actions).toEqual([]);
  });

  it('uses the transparent busy layer without a PrimeNG table mask', () => {
    fixture.componentRef.setInput('isLoading', true);
    fixture.detectChanges();

    const container = fixture.nativeElement.querySelector('.invai-table-loading-container');
    expect(container.getAttribute('aria-busy')).toBe('true');
    expect(fixture.nativeElement.querySelector('.invai-table-loading-shield')).not.toBeNull();
    expect(fixture.nativeElement.querySelector('.p-datatable-mask')).toBeNull();
  });
});
