import { ComponentFixture, TestBed } from '@angular/core/testing';
import { KeyLabel } from '@models/table.model';

import { ApplicationsTable } from './applications-table';

class ResizeObserverMock implements ResizeObserver {
  disconnect(): void {}
  observe(): void {}
  unobserve(): void {}
}

globalThis.ResizeObserver ??= ResizeObserverMock;

const COLUMNS: Partial<KeyLabel>[] = [
  { key: 'code', label: 'Codi' },
  { key: 'name', label: 'Aplicació' },
];

describe('ApplicationsTable', () => {
  let fixture: ComponentFixture<ApplicationsTable>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ApplicationsTable],
    }).compileComponents();

    fixture = TestBed.createComponent(ApplicationsTable);
    fixture.componentRef.setInput('columns', COLUMNS);
    fixture.componentRef.setInput('itemsList', { items: [], total: 0 });
  });

  it('should render initial skeletons with only the selected columns', () => {
    fixture.componentRef.setInput('isLoading', true);
    fixture.componentRef.setInput('isInitialLoading', true);
    fixture.detectChanges();

    const skeletonRows = fixture.nativeElement.querySelectorAll(
      '.invai-table-skeleton-row',
    ) as NodeListOf<HTMLTableRowElement>;

    expect(skeletonRows.length).toBeGreaterThan(0);
    skeletonRows.forEach((row) => {
      expect(row.querySelectorAll('td')).toHaveLength(COLUMNS.length);
    });
  });

  it('should span the empty state across only the selected columns', () => {
    fixture.detectChanges();

    const emptyCell = fixture.nativeElement.querySelector(
      '.p-datatable-tbody > tr > td[colspan]',
    ) as HTMLTableCellElement;

    expect(emptyCell.colSpan).toBe(COLUMNS.length);
  });

  it('wraps only descriptive columns and keeps complete text and status labels', () => {
    const longName = 'Aplicació amb una descripció molt llarga '.repeat(10);
    fixture.componentRef.setInput('columns', [
      { key: 'code', label: 'Codi' },
      {
        key: 'name',
        label: 'Aplicació',
        wrap: true,
        maxWidth: '24rem',
      },
      { key: 'status', label: 'Estat' },
    ]);
    fixture.componentRef.setInput('itemsList', {
      items: [
        { id: '1', code: 'APP-001', name: longName, status: 1 },
        { id: '2', code: 'APP-002', name: 'Segona aplicació', status: 2 },
      ], total: 2,
    });
    fixture.detectChanges();
    const cells = fixture.nativeElement.querySelectorAll('tbody tr:first-child .invai-table-cell-content');
    expect(cells[0].classList.contains('invai-table-cell-content--wrap')).toBe(false);
    expect(cells[1].classList.contains('invai-table-cell-content--wrap')).toBe(true);
    expect(cells[1].style.maxWidth).toBe('24rem');
    expect(cells[1].textContent.trim()).toBe(longName.trim());
    const tags = fixture.nativeElement.querySelectorAll('.invai-status-tag');
    expect(tags).toHaveLength(2);
    expect(tags[0].textContent.trim()).toBe('Actiu');
    expect(tags[0].classList.contains('invai-status-tag--active')).toBe(true);
    expect(tags[1].textContent.trim()).toBe('Inactiu');
    expect(tags[1].classList.contains('invai-status-tag--active')).toBe(false);
  });
});
