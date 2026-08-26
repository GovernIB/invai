import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { ActionParams } from '@models/table.model';
import { MenuItem } from 'primeng/api';
import { Menu } from 'primeng/menu';
import { Tooltip } from 'primeng/tooltip';

import { ApplicationAssignedResponsibleOutput } from '../../../../applications.model';
import {
  ApplicationAssignmentTableAction,
  ApplicationAuthorizedTable,
  ApplicationResponsibleTableRow,
  ApplicationResponsiblesTable,
  assignmentRowsToCsv,
} from './application-assignment-tables';

const ROW: ApplicationAssignedResponsibleOutput = {
  id: 1,
  appResponsibleAuthorizedId: 91,
  person: {
    id: 2,
    company: null,
    firstName: 'Maria',
    lastName: 'Tur',
    email: 'maria@caib.es',
    personalCaib: true,
    deletedAt: null,
  },
  responsibleType: {
    id: 3,
    name: 'Responsable',
    nameEs: 'Responsable',
    requiresPersonalCaib: true,
  },
  jobTitle: 'Cap de servei',
  observation: null,
  deletedAt: null,
};
const TABLE_ROW: ApplicationResponsibleTableRow = {
  rowKey: 'responsible-type-3',
  responsibleType: ROW.responsibleType,
  assignment: ROW,
};

describe('ApplicationResponsiblesTable', () => {
  let fixture: ComponentFixture<ApplicationResponsiblesTable>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ApplicationResponsiblesTable],
    }).compileComponents();
    fixture = TestBed.createComponent(ApplicationResponsiblesTable);
    fixture.componentRef.setInput('columns', [
      { key: 'person', label: 'Persona', sortBy: 'person.firstName' },
    ]);
    fixture.componentRef.setInput('itemsList', { items: [TABLE_ROW], total: 1 });
    fixture.detectChanges();
  });

  it('renders rows as non-activatable read-only display rows', () => {
    const row = fixture.nativeElement.querySelector('tbody tr') as HTMLTableRowElement;
    expect(row.hasAttribute('tabindex')).toBe(false);
    expect(row.classList.contains('invai-table-consultable-row')).toBe(false);
  });

  it('exposes transparent busy state and only renders row actions while editing', () => {
    fixture.componentRef.setInput('isLoading', true);
    fixture.detectChanges();
    expect(
      fixture.nativeElement
        .querySelector('.invai-table-loading-container')
        .getAttribute('aria-busy'),
    ).toBe('true');
    expect(fixture.nativeElement.querySelector('.p-datatable-mask')).toBeNull();
    expect(fixture.nativeElement.querySelector('tbody button')).toBeNull();
    expect(fixture.nativeElement.querySelectorAll('.invai-table-actions-column')).toHaveLength(0);
    expect(fixture.nativeElement.querySelector('p-menu')).toBeNull();

    fixture.componentRef.setInput('isEditing', true);
    fixture.detectChanges();
    expect(
      (fixture.nativeElement.querySelector('tbody button') as HTMLButtonElement).disabled,
    ).toBe(false);
    expect(fixture.nativeElement.querySelectorAll('.invai-table-actions-column')).toHaveLength(2);
    expect(fixture.nativeElement.querySelector('p-menu')).not.toBeNull();
  });

  it('spans the empty state across only the rendered columns', () => {
    fixture.componentRef.setInput('itemsList', { items: [], total: 0 });
    fixture.detectChanges();

    let emptyCell = fixture.nativeElement.querySelector(
      'tbody td[colspan]',
    ) as HTMLTableCellElement;
    expect(emptyCell.colSpan).toBe(1);

    fixture.componentRef.setInput('isEditing', true);
    fixture.detectChanges();

    emptyCell = fixture.nativeElement.querySelector('tbody td[colspan]') as HTMLTableCellElement;
    expect(emptyCell.colSpan).toBe(2);
  });

  it('shows only the alert icon and exposes the incomplete label by tooltip and keyboard', () => {
    expect(fixture.nativeElement.querySelector('.application-responsible-incomplete')).toBeNull();

    fixture.componentRef.setInput('itemsList', {
      items: [{ ...TABLE_ROW, assignment: null }],
      total: 1,
    });
    fixture.detectChanges();

    const incomplete = fixture.nativeElement.querySelector(
      '.application-responsible-incomplete',
    ) as HTMLSpanElement;
    const cell = incomplete.closest('td') as HTMLTableCellElement;
    const icon = incomplete.querySelector('i') as HTMLElement;
    expect(incomplete.firstElementChild).toBe(icon);
    expect(icon.classList).toContain('pi-exclamation-triangle');
    expect(icon.classList).toContain('text-red-500');
    expect(icon.getAttribute('aria-hidden')).toBe('true');
    expect(incomplete.querySelector('.sr-only')?.textContent?.trim()).toBe('Incomplet');
    expect(incomplete.classList).toContain('w-full');
    expect(incomplete.classList).toContain('justify-center');
    expect(cell.tabIndex).toBe(0);

    const tooltip = fixture.debugElement.query(By.directive(Tooltip)).injector.get(Tooltip);
    expect(tooltip.content).toBe('Incomplet');
    expect(tooltip.tooltipEvent).toBe('both');
    expect(tooltip.autoHide).toBe(false);
  });

  it('offers edit and deactivate for an assignment and no action when vacant', () => {
    const emitted: ActionParams<ApplicationResponsibleTableRow>[] = [];
    const table = fixture.componentInstance as unknown as {
      openActionsMenu(event: Event, row: ApplicationResponsibleTableRow, menu: Menu): void;
      rowActions: () => MenuItem[];
    };
    const menu = { toggle: vi.fn() } as unknown as Menu;
    fixture.componentInstance.onSelectAction.subscribe((event) => emitted.push(event));

    table.openActionsMenu(new Event('click'), TABLE_ROW, menu);
    const assignedActions = table.rowActions();
    expect(assignedActions.map(({ label }) => label)).toEqual(['Editar', 'Donar de baixa']);
    (assignedActions[0].command as () => void)();
    (assignedActions[1].command as () => void)();
    const incomplete = { ...TABLE_ROW, assignment: null };
    table.openActionsMenu(new Event('click'), incomplete, menu);
    const incompleteActions = table.rowActions();
    expect(incompleteActions).toEqual([]);

    expect(emitted).toEqual([
      { action: ApplicationAssignmentTableAction.Edit, params: TABLE_ROW },
      { action: ApplicationAssignmentTableAction.Deactivate, params: TABLE_ROW },
    ]);
  });

  it('uses the shared body-appended menu styling contract', () => {
    fixture.componentRef.setInput('isEditing', true);
    fixture.detectChanges();
    const menu = fixture.debugElement.query(By.directive(Menu)).componentInstance as Menu;
    const styles = (
      ApplicationResponsiblesTable as unknown as { ɵcmp: { styles: string[] } }
    ).ɵcmp.styles.join('\n');

    expect(menu.appendTo()).toBe('body');
    expect(menu.styleClass).toBe('application-responsible-row-menu');
    expect(styles).toContain('.application-responsible-row-menu.p-menu');
    expect(styles).toContain('padding: 0.5rem');
    expect(styles).toContain('gap: 0.125rem');
    expect(styles).toContain('padding: 0.625rem 0.75rem');
  });

  it('escapes commas, quotes and line breaks as one CSV cell', () => {
    expect(
      assignmentRowsToCsv(
        [{ key: 'value', label: 'Valor' }],
        [TABLE_ROW],
        () => 'Un, "dos"\ny tres',
      ),
    ).toBe('Valor\r\n"Un, ""dos""\ny tres"');
  });
});

describe('ApplicationAuthorizedTable', () => {
  it('uses the same shared body-appended contextual menu', async () => {
    await TestBed.configureTestingModule({
      imports: [ApplicationAuthorizedTable],
    }).compileComponents();
    const fixture = TestBed.createComponent(ApplicationAuthorizedTable);
    fixture.componentRef.setInput('columns', [{ key: 'person', label: 'Persona' }]);
    fixture.componentRef.setInput('itemsList', { items: [], total: 0 });
    fixture.componentRef.setInput('isEditing', true);
    fixture.detectChanges();

    const menu = fixture.debugElement.query(By.directive(Menu)).componentInstance as Menu;
    expect(menu.appendTo()).toBe('body');
    expect(menu.styleClass).toBe('application-responsible-row-menu');
  });
});
