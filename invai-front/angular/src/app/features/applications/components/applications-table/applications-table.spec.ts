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
});
