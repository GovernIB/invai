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

  it.each([
    ['NOT_VALIDATED', true, '#FF9523'],
    ['MANUAL', true, 'text-green-700'],
    ['VALIDATED', true, null],
    ['NOT_APPLY', true, null],
    ['NOT_VALIDATED', false, null],
  ] as const)('renders DIR3 %s for CAIB=%s next to the name', (status, personalCaib, color) => {
    const assignment = {
      ...ROW,
      person: { ...ROW.person, personalCaib },
      dir3Validation: {
        id: 501,
        dir3Status: status,
        reason: null,
        manualValidatedAt: null,
        manualValidatedBy: null,
      },
    };
    fixture.componentRef.setInput('itemsList', { items: [{ ...TABLE_ROW, assignment }], total: 1 });
    fixture.detectChanges();
    const icon = fixture.nativeElement.querySelector(
      '[role="img"] .pi',
    ) as HTMLElement | null;
    if (color) {
      if (status === 'NOT_VALIDATED') {
        expect(icon?.style.color).toBe('rgb(255, 149, 35)');
      } else {
        expect(icon?.classList.contains(color)).toBe(true);
      }
      expect(icon?.classList.contains(status === 'MANUAL' ? 'pi-check-circle' : 'pi-exclamation-circle')).toBe(true);
      expect(icon?.closest('td')?.textContent).toContain('Maria Tur');
      expect(icon?.parentElement?.getAttribute('aria-label')).toContain('DIR3');
    } else {
      expect(icon).toBeNull();
    }
  });

  it('offers manual validation only for pending CAIB responsibles in edit mode', () => {
    const pending = {
      ...TABLE_ROW,
      assignment: {
        ...ROW,
        dir3Validation: {
          id: 501,
          dir3Status: 'NOT_VALIDATED' as const,
          reason: null,
          manualValidatedAt: null,
          manualValidatedBy: null,
        },
      },
    };
    const table = fixture.componentInstance as unknown as {
      openActionsMenu(event: Event, row: ApplicationResponsibleTableRow, menu: Menu): void;
      rowActions: () => MenuItem[];
    };
    const menu = { toggle: vi.fn() } as unknown as Menu;
    table.openActionsMenu(new Event('click'), pending, menu);
    expect(table.rowActions().some((action) => action.label === 'Confirmar discrepància DIR3')).toBe(false);
    fixture.componentRef.setInput('isEditing', true);
    const manual = table.rowActions().find((action) => action.label === 'Confirmar discrepància DIR3');
    const emit = vi.spyOn(fixture.componentInstance.onSelectAction, 'emit');
    (manual?.command as () => void)();
    expect(emit).toHaveBeenCalledWith({
      action: ApplicationAssignmentTableAction.ValidateManually,
      params: pending,
    });
  });

  it('consults assigned rows with double click and Enter even outside edit mode', () => {
    const row = fixture.nativeElement.querySelector('tbody tr') as HTMLTableRowElement;
    const emit = vi.spyOn(fixture.componentInstance.onSelectAction, 'emit');
    expect(row.tabIndex).toBe(0);
    expect(row.classList.contains('invai-table-consultable-row')).toBe(true);
    row.dispatchEvent(new MouseEvent('dblclick', { bubbles: true }));
    row.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter', bubbles: true }));
    expect(emit).toHaveBeenCalledTimes(2);
    expect(emit).toHaveBeenLastCalledWith({
      action: ApplicationAssignmentTableAction.View,
      params: TABLE_ROW,
    });
    row
      .querySelector('button')!
      .dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter', bubbles: true }));
    row.querySelector('button .pi')!.dispatchEvent(new MouseEvent('dblclick', { bubbles: true }));
    expect(emit).toHaveBeenCalledTimes(2);
  });

  it('preserves rows and the consultation menu while exposing transparent busy state', () => {
    fixture.componentRef.setInput('isLoading', true);
    fixture.detectChanges();
    expect(
      fixture.nativeElement
        .querySelector('.invai-table-loading-container')
        .getAttribute('aria-busy'),
    ).toBe('true');
    expect(fixture.nativeElement.querySelector('.p-datatable-mask')).toBeNull();
    expect(fixture.nativeElement.querySelector('tbody button')).not.toBeNull();
    expect(fixture.nativeElement.querySelectorAll('.invai-table-actions-column')).toHaveLength(2);
    expect(fixture.nativeElement.querySelector('p-menu')).not.toBeNull();

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
    expect(emptyCell.colSpan).toBe(1);
  });

  it('shows only the circular alert icon and exposes the incomplete label by tooltip and keyboard', () => {
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
    expect(icon.classList).toContain('pi-exclamation-circle');
    expect(icon.classList).not.toContain('pi-exclamation-triangle');
    expect(icon.classList).toContain('text-red-500');
    expect(icon.getAttribute('aria-hidden')).toBe('true');
    expect(incomplete.querySelector('.sr-only')?.textContent?.trim()).toBe('Incomplet');
    expect(incomplete.classList).toContain('w-full');
    expect(incomplete.classList).toContain('justify-start');
    expect(incomplete.classList).not.toContain('justify-center');
    expect(cell.tabIndex).toBe(0);

    const tooltip = fixture.debugElement.query(By.directive(Tooltip)).injector.get(Tooltip);
    expect(tooltip.content).toBe('Incomplet');
    expect(tooltip.tooltipEvent).toBe('both');
    expect(tooltip.autoHide).toBe(false);
    const emit = vi.spyOn(fixture.componentInstance.onSelectAction, 'emit');
    const vacantRow = cell.closest('tr')!;
    vacantRow.dispatchEvent(new MouseEvent('dblclick', { bubbles: true }));
    cell.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter', bubbles: true }));
    expect(emit).not.toHaveBeenCalled();
    expect(vacantRow.hasAttribute('tabindex')).toBe(false);
    expect(vacantRow.querySelector('button')).toBeNull();
  });

  it('offers consultation always, mutations only in edit mode and no action when vacant', () => {
    const emitted: ActionParams<ApplicationResponsibleTableRow>[] = [];
    const table = fixture.componentInstance as unknown as {
      openActionsMenu(event: Event, row: ApplicationResponsibleTableRow, menu: Menu): void;
      rowActions: () => MenuItem[];
    };
    const menu = { toggle: vi.fn() } as unknown as Menu;
    fixture.componentInstance.onSelectAction.subscribe((event) => emitted.push(event));

    table.openActionsMenu(new Event('click'), TABLE_ROW, menu);
    expect(table.rowActions().map(({ label }) => label)).toEqual(['Consulta']);
    fixture.componentRef.setInput('isEditing', true);
    const assignedActions = table.rowActions();
    expect(assignedActions.map(({ label }) => label)).toEqual([
      'Consulta',
      'Editar',
      'Donar de baixa',
    ]);
    assignedActions.forEach((action) => (action.command as () => void)());
    const incomplete = { ...TABLE_ROW, assignment: null };
    table.openActionsMenu(new Event('click'), incomplete, menu);
    const incompleteActions = table.rowActions();
    expect(incompleteActions).toEqual([]);

    expect(emitted).toEqual([
      { action: ApplicationAssignmentTableAction.View, params: TABLE_ROW },
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

  it('consults authorized people through the menu and Enter without edit mode', async () => {
    await TestBed.configureTestingModule({
      imports: [ApplicationAuthorizedTable],
    }).compileComponents();
    const fixture = TestBed.createComponent(ApplicationAuthorizedTable);
    const authorized = {
      id: 8,
      person: ROW.person,
      authorizationTypes: [],
      observation: null,
      deletedAt: null,
    };
    fixture.componentRef.setInput('columns', [{ key: 'person', label: 'Persona' }]);
    fixture.componentRef.setInput('itemsList', { items: [authorized], total: 1 });
    fixture.detectChanges();
    const emit = vi.spyOn(fixture.componentInstance.onSelectAction, 'emit');
    const row = fixture.nativeElement.querySelector('tbody tr') as HTMLTableRowElement;
    row.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter', bubbles: true }));
    expect(emit).toHaveBeenLastCalledWith({
      action: ApplicationAssignmentTableAction.View,
      params: authorized,
    });
    row.querySelector<HTMLButtonElement>('button')!.click();
    fixture.detectChanges();
    const menu = fixture.debugElement.query(By.directive(Menu)).componentInstance as Menu;
    expect(menu.model?.map(({ label }) => label)).toEqual(['Consulta']);
    (menu.model![0].command as () => void)();
    expect(emit).toHaveBeenCalledTimes(2);
  });
});
