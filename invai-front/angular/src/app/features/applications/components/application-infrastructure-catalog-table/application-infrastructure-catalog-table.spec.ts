import { ComponentFixture, TestBed } from '@angular/core/testing';
import { KeyLabel } from '@models/table.model';

import { ApplicationInfrastructureCatalogRow } from '../../applications.model';
import { ApplicationInfrastructureCatalogTable } from './application-infrastructure-catalog-table';

class ResizeObserverMock implements ResizeObserver {
  disconnect(): void {}
  observe(): void {}
  unobserve(): void {}
}

globalThis.ResizeObserver ??= ResizeObserverMock;

const COLUMNS: KeyLabel[] = [
  { key: 'server', label: 'Servidor', sortBy: 'server.name' },
];
const ROW = {
  id: 5,
  server: 'app01.caib.es',
} as ApplicationInfrastructureCatalogRow;

describe('ApplicationInfrastructureCatalogTable', () => {
  let fixture: ComponentFixture<ApplicationInfrastructureCatalogTable>;
  let component: ApplicationInfrastructureCatalogTable;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ApplicationInfrastructureCatalogTable],
    }).compileComponents();

    fixture = TestBed.createComponent(ApplicationInfrastructureCatalogTable);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('columns', COLUMNS);
    fixture.componentRef.setInput('itemsList', { items: [ROW], total: 1 });
    fixture.detectChanges();
  });

  it('keeps single-click and Enter selection semantics for catalog rows', () => {
    const selected: ApplicationInfrastructureCatalogRow[] = [];
    component.selectionChange.subscribe((row) => selected.push(row));
    const row = fixture.nativeElement.querySelector(
      '.application-infrastructure-catalog-table__selectable',
    ) as HTMLTableRowElement;

    row.click();
    row.dispatchEvent(
      new KeyboardEvent('keydown', { bubbles: true, key: 'Enter' }),
    );

    expect(selected).toEqual([ROW, ROW]);
  });

  it('renders view mode as static data without a disabled selection control', () => {
    const selectionChange = vi.fn();
    component.selectionChange.subscribe(selectionChange);
    fixture.componentRef.setInput('isReadOnly', true);
    fixture.componentRef.setInput('selection', ROW);
    fixture.detectChanges();

    const row = fixture.nativeElement.querySelector(
      '.p-datatable-tbody > tr',
    ) as HTMLTableRowElement;
    row.click();
    row.dispatchEvent(new KeyboardEvent('keydown', { bubbles: true, key: 'Enter' }));

    expect(row.tabIndex).toBe(-1);
    expect(fixture.nativeElement.querySelector('input[type="radio"]')).toBeNull();
    expect(selectionChange).not.toHaveBeenCalled();
    expect(row.textContent).toContain('app01.caib.es');
  });

  it('uses the shared progress indicator without the PrimeNG loading mask', () => {
    fixture.componentRef.setInput('isLoading', true);
    fixture.detectChanges();

    const container = fixture.nativeElement.querySelector(
      '.invai-table-loading-container',
    ) as HTMLElement;
    expect(container.getAttribute('aria-busy')).toBe('true');
    expect(container.querySelector('.invai-table-refresh-indicator')).toBeTruthy();
    expect(container.querySelector('.p-datatable-mask')).toBeFalsy();
    expect(container.textContent).toContain('app01.caib.es');
  });
});
