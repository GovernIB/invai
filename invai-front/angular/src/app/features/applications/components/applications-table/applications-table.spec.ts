import { ComponentFixture, TestBed } from '@angular/core/testing';
import { KeyLabel } from '@models/table.model';
import { ApplicationStatus } from '../../applications.model';

import { ApplicationTableAction, ApplicationsTable } from './applications-table';

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

  it('renders initial skeletons including the actions column', () => {
    fixture.componentRef.setInput('isLoading', true);
    fixture.componentRef.setInput('isInitialLoading', true);
    fixture.detectChanges();

    const skeletonRows = fixture.nativeElement.querySelectorAll(
      '.invai-table-skeleton-row',
    ) as NodeListOf<HTMLTableRowElement>;

    expect(skeletonRows.length).toBeGreaterThan(0);
    skeletonRows.forEach((row) => {
      expect(row.querySelectorAll('td')).toHaveLength(COLUMNS.length + 1);
    });
  });

  it('spans the empty state across the selected columns and actions', () => {
    fixture.detectChanges();

    const emptyCell = fixture.nativeElement.querySelector(
      '.p-datatable-tbody > tr > td[colspan]',
    ) as HTMLTableCellElement;

    expect(emptyCell.colSpan).toBe(COLUMNS.length + 1);
  });

  function renderRows(): HTMLButtonElement[] {
    fixture.componentRef.setInput('itemsList', {
      items: [
        { id: '1', name: 'First', status: ApplicationStatus.ACTIVE },
        { id: '2', name: 'Second', status: ApplicationStatus.ACTIVE },
        { id: '3', name: 'Inactive', status: ApplicationStatus.INACTIVE },
      ],
      total: 3,
    });
    fixture.detectChanges();
    return [...fixture.nativeElement.querySelectorAll('tbody .invai-table-actions-column button')];
  }

  async function openMenu(button: HTMLButtonElement): Promise<HTMLElement[]> {
    button.click();
    fixture.detectChanges();
    await fixture.whenStable();
    await vi.waitFor(() => {
      fixture.detectChanges();
      expect(button.getAttribute('aria-expanded')).toBe('true');
    });
    return [...document.querySelectorAll<HTMLElement>('.maintenance-row-menu [role="menuitem"]')];
  }

  it('emits consultation and editing for the row whose menu was opened', async () => {
    const buttons = renderRows();
    const emit = vi.spyOn(fixture.componentInstance.onSelectAction, 'emit');
    let items = await openMenu(buttons[0]);
    expect(items.map((item) => item.textContent?.trim())).toEqual(['Consulta', 'Edita']);
    expect(buttons[0].querySelector('.pi-ellipsis-h')).not.toBeNull();
    expect(buttons[0].getAttribute('aria-haspopup')).toBe('menu');
    expect(buttons[0].getAttribute('aria-expanded')).toBe('true');
    expect(document.getElementById(buttons[0].getAttribute('aria-controls')!)).not.toBeNull();
    items[0].querySelector<HTMLElement>('a')!.click();
    fixture.detectChanges();
    await fixture.whenStable();
    expect(emit).toHaveBeenLastCalledWith({ action: ApplicationTableAction.Detail, params: expect.objectContaining({ id: '1' }) });

    items = await openMenu(buttons[1]);
    items[1].querySelector<HTMLElement>('a')!.click();
    fixture.detectChanges();
    await fixture.whenStable();
    expect(emit).toHaveBeenLastCalledWith({ action: ApplicationTableAction.Edit, params: expect.objectContaining({ id: '2' }) });
    expect(emit).toHaveBeenCalledTimes(2);
  });

  it('disables editing inactive applications while retaining consultation', async () => {
    const buttons = renderRows();
    const emit = vi.spyOn(fixture.componentInstance.onSelectAction, 'emit');
    const items = await openMenu(buttons[2]);
    expect(items[1].getAttribute('aria-disabled')).toBe('true');
    items[1].querySelector<HTMLElement>('a')!.click();
    expect(emit).not.toHaveBeenCalled();
    items[0].querySelector<HTMLElement>('a')!.click();
    expect(emit).toHaveBeenCalledWith({ action: ApplicationTableAction.Detail, params: expect.objectContaining({ id: '3' }) });
  });

  it('ignores row activation from the menu button and its icon', () => {
    const buttons = renderRows();
    const emit = vi.spyOn(fixture.componentInstance.onSelectAction, 'emit');
    buttons[0].dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter', bubbles: true }));
    buttons[0].querySelector('.pi')!.dispatchEvent(new MouseEvent('dblclick', { bubbles: true }));
    expect(emit).not.toHaveBeenCalled();
    buttons[0].closest('tr')!.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter', bubbles: true }));
    expect(emit).toHaveBeenCalledOnce();
  });

  it('supports menu keyboard navigation and returns focus on Escape', async () => {
    const buttons = renderRows();
    await openMenu(buttons[0]);
    const menu = document.querySelector<HTMLElement>('.maintenance-row-menu [role="menu"]')!;
    expect(document.activeElement).toBe(menu);
    menu.dispatchEvent(new KeyboardEvent('keydown', { key: 'ArrowDown', code: 'ArrowDown', bubbles: true }));
    fixture.detectChanges();
    expect(document.getElementById(menu.getAttribute('aria-activedescendant')!)?.textContent?.trim()).toBe('Consulta');
    menu.dispatchEvent(new KeyboardEvent('keydown', { key: 'ArrowDown', code: 'ArrowDown', bubbles: true }));
    fixture.detectChanges();
    expect(document.getElementById(menu.getAttribute('aria-activedescendant')!)?.textContent?.trim()).toBe('Edita');
    menu.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', code: 'Escape', bubbles: true }));
    fixture.detectChanges();
    await fixture.whenStable();
    expect(document.activeElement).toBe(buttons[0]);
    await vi.waitFor(() => {
      fixture.detectChanges();
      expect(buttons[0].getAttribute('aria-expanded')).toBe('false');
    });
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
