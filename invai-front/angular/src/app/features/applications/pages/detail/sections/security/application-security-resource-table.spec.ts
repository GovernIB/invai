import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { KeyLabel, PaginatedList } from '@models/table.model';
import { Tooltip } from 'primeng/tooltip';

import {
  ApplicationSecurityResourceOutput,
  ApplicationSecurityRiskOutput,
  ApplicationSecurityRoleOutput,
  ApplicationWebContextOutput,
} from '../../../../applications.model';
import {
  ApplicationSecurityResourceTable,
  ApplicationSecurityTableAction,
} from './application-security-resource-table';

describe('ApplicationSecurityResourceTable', () => {
  let fixture: ComponentFixture<ApplicationSecurityResourceTable>;
  let component: ApplicationSecurityResourceTable;
  const columns: Partial<KeyLabel>[] = [
    { key: 'role', label: 'Codi' },
    { key: 'system', label: 'Sistema' },
    { key: 'description', label: 'Descripció' },
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ApplicationSecurityResourceTable],
    }).compileComponents();
    fixture = TestBed.createComponent(ApplicationSecurityResourceTable);
    component = fixture.componentInstance;
  });

  it('renders roles as consultable rows and hides their actions outside edit mode', () => {
    const role = roleRow();
    const action = vi.fn();
    component.onSelectAction.subscribe(action);
    setInputs('role', { items: [role], total: 1 });

    const table = fixture.nativeElement.querySelector('p-table');
    const row = fixture.nativeElement.querySelector('tbody tr') as HTMLElement;
    expect(table).toBeTruthy();
    expect(row.getAttribute('tabindex')).toBe('0');
    expect(row.classList).toContain('invai-table-consultable-row');
    expect(row.textContent).toContain('INV_ADMIN');
    expect(row.textContent).toContain('INVAI');
    expect(row.textContent).toContain('Administració');
    expect(fixture.nativeElement.querySelector('.invai-table-actions-column')).toBeNull();
    expect(fixture.nativeElement.querySelector('.p-datatable-mask')).toBeNull();

    row.dispatchEvent(new MouseEvent('dblclick', { bubbles: true }));
    row.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter', bubbles: true }));
    expect(action).toHaveBeenCalledTimes(2);
    expect(action).toHaveBeenLastCalledWith({ action: ApplicationSecurityTableAction.View, params: role });
  });

  it('groups the role actions in a menu that keeps mutations disabled in readonly mode', () => {
    const role = roleRow();
    const action = vi.fn();
    component.onSelectAction.subscribe(action);
    fixture.componentRef.setInput('showActions', true);
    fixture.componentRef.setInput('isReadOnly', true);
    setInputs('role', { items: [role], total: 1 });

    const trigger = fixture.nativeElement.querySelector(
      'tbody .invai-table-actions-column button',
    ) as HTMLButtonElement;
    expect(trigger.getAttribute('aria-label')).toBe('Obre les accions del registre');
    expect(trigger.disabled).toBe(true);
    fixture.componentRef.setInput('isReadOnly', false);
    fixture.detectChanges();
    expect(trigger.disabled).toBe(false);
    trigger.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter', bubbles: true }));
    expect(action).not.toHaveBeenCalled();
    trigger.click();
    fixture.detectChanges();
    const menu = (component as unknown as { rowActions(): { label: string; disabled?: boolean; command(): void }[] }).rowActions();
    expect(menu.map((item) => [item.label, Boolean(item.disabled)])).toEqual([
      ['Consulta', false],
      ['Edita', false],
      ['Dona de baixa', false],
    ]);
    menu[1].command();
    expect(action).toHaveBeenCalledExactlyOnceWith({ action: ApplicationSecurityTableAction.Edit, params: role });
  });

  it('opens a consultable resource with Enter', () => {
    const row = {
      id: 4,
      level: null,
      field: null,
      description: 'Risc',
      deletedAt: null,
    } as ApplicationSecurityRiskOutput;
    const action = vi.fn();
    component.onSelectAction.subscribe(action);
    setInputs('risk', { items: [row], total: 1 });

    fixture.nativeElement
      .querySelector('tbody tr')
      .dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter', bubbles: true }));

    expect(action).toHaveBeenCalledWith({
      action: ApplicationSecurityTableAction.View,
      params: row,
    });
  });

  it('renders direct verification in readonly mode without activating the consultable row', () => {
    const row = { id: 3, appSecurity: { id: 9 }, webContext: { id: 1, name: 'Web' }, field: { id: 2, name: 'Intern' }, url: null, observation: '', deletedAt: null } as ApplicationWebContextOutput;
    const action = vi.fn();
    component.onSelectAction.subscribe(action);
    fixture.componentRef.setInput('showVerify', true);
    fixture.componentRef.setInput('isReadOnly', true);
    setInputs('web-context', { items: [row], total: 1 });
    const button = fixture.nativeElement.querySelector('tbody button') as HTMLButtonElement;
    expect(button.textContent).toContain('Verificar');
    expect(button.getAttribute('aria-label')).toBe('Verificar');
    expect(button.disabled).toBe(false);
    button.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter', bubbles: true }));
    button.dispatchEvent(new MouseEvent('dblclick', { bubbles: true }));
    expect(action).not.toHaveBeenCalled();
    button.click();
    expect(action).toHaveBeenCalledExactlyOnceWith({ action: ApplicationSecurityTableAction.Verify, params: row });
    expect(fixture.nativeElement.querySelector('p-menu')).toBeNull();
    action.mockClear();
    fixture.nativeElement.querySelector('tbody tr').dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter', bubbles: true }));
    expect(action).toHaveBeenCalledExactlyOnceWith({ action: ApplicationSecurityTableAction.View, params: row });
  });

  it('counts the verification column in the empty state', () => {
    fixture.componentRef.setInput('showVerify', true);
    setInputs('web-context', { items: [], total: 0 });
    expect(fixture.nativeElement.querySelector('tbody td').getAttribute('colspan')).toBe('2');
  });

  it('keeps the context label and an accessible pending icon even when verification actions are hidden', () => {
    const row = { id: 3, appSecurity: { id: 9 }, webContext: { id: 1, name: 'Web' }, field: { id: 2, name: 'Intern' }, url: null, observation: '', deletedAt: null } as ApplicationWebContextOutput;
    fixture.componentRef.setInput('showVerificationWarnings', true);
    setInputs('web-context', { items: [row], total: 1 });
    fixture.componentRef.setInput('columns', [{ key: 'webContext', label: 'Context web' }]);
    fixture.detectChanges();
    const warning = fixture.nativeElement.querySelector('.application-web-context-pending') as HTMLElement;
    expect(warning.closest('td')?.textContent).toContain('Web');
    expect(warning.querySelector('.pi-exclamation-circle.text-red-500')?.getAttribute('aria-hidden')).toBe('true');
    expect(warning.querySelector('.sr-only')?.textContent).toBe('Context web pendent de verificar.');
    expect(warning.tabIndex).toBe(0);
    expect(fixture.nativeElement.querySelector('.application-security-verification-column')).toBeNull();
    fixture.componentRef.setInput('showVerify', true);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.application-web-context-pending')).not.toBeNull();
    fixture.componentRef.setInput('showVerificationWarnings', false);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.application-web-context-pending')).toBeNull();
  });

  it('shows the verified reason on an accessible green icon in Security', () => {
    const row = { id: 3, appSecurity: { id: 9 }, webContext: { id: 1, name: 'Web' }, field: { id: 2, name: 'Intern' }, validated: true, validatedReason: 'Validat per auditoria', deletedAt: null } as ApplicationWebContextOutput;
    fixture.componentRef.setInput('showVerificationWarnings', true);
    fixture.componentRef.setInput('showVerifiedReason', true);
    fixture.componentRef.setInput('showVerify', true);
    setInputs('web-context', { items: [row], total: 1 });
    fixture.componentRef.setInput('columns', [{ key: 'webContext', label: 'Context web' }]);
    fixture.detectChanges();
    const icon = fixture.nativeElement.querySelector('.application-web-context-verified-icon') as HTMLElement;
    expect(icon.querySelector('.pi-check-circle.text-green-700')?.getAttribute('aria-hidden')).toBe('true');
    expect(icon.getAttribute('aria-label')).toBe('Verificat: Validat per auditoria');
    expect(icon.tabIndex).toBe(0);
    expect(fixture.debugElement.query(By.css('.application-web-context-verified-icon')).injector.get(Tooltip).content).toBe('Validat per auditoria');
    const action = vi.fn();
    component.onSelectAction.subscribe(action);
    icon.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter', bubbles: true }));
    expect(action).not.toHaveBeenCalled();
    icon.dispatchEvent(new FocusEvent('focus'));
    expect(document.querySelector('.p-tooltip-text')?.textContent).toBe('Validat per auditoria');
    icon.dispatchEvent(new FocusEvent('blur'));
    icon.dispatchEvent(new MouseEvent('mouseenter'));
    expect(document.querySelector('.p-tooltip-text')?.textContent).toBe('Validat per auditoria');
    expect(fixture.nativeElement.querySelector('.application-web-context-verified')).toBeNull();
    expect(fixture.nativeElement.querySelector('.application-web-context-pending')).toBeNull();
    expect(fixture.nativeElement.querySelector('tbody button')).toBeNull();
  });

  it('falls back to the verified label when the reason is absent and keeps Development unchanged', () => {
    const row = { id: 3, webContext: { id: 1, name: 'Web' }, validated: true, validatedReason: null } as ApplicationWebContextOutput;
    fixture.componentRef.setInput('showVerifiedReason', true);
    setInputs('web-context', { items: [row], total: 1 });
    fixture.componentRef.setInput('columns', [{ key: 'webContext', label: 'Context web' }]);
    fixture.detectChanges();
    const icon = fixture.nativeElement.querySelector('.application-web-context-verified-icon') as HTMLElement;
    expect(icon.getAttribute('aria-label')).toBe('Verificat');
    expect(fixture.debugElement.query(By.css('.application-web-context-verified-icon')).injector.get(Tooltip).content).toBe('Verificat');
    fixture.componentRef.setInput('showVerifiedReason', false);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('.application-web-context-verified')?.textContent).toBe('Verificat');
    expect(fixture.nativeElement.querySelector('.application-web-context-verified-icon')).toBeNull();
  });

  it.each([
    ['VALIDATED', null],
    ['NOT_APPLY', null],
    [
      'NOT_VALIDATED',
      {
        icon: '.pi-exclamation-circle.text-orange-600',
        label: "Rol no validat: el codi no comença pel prefix de l'aplicació.",
      },
    ],
    [
      'MANUAL',
      {
        icon: '.pi-check-circle.text-green-700',
        label: 'Rol validat manualment per admin el 07/10/2026 16:21',
      },
    ],
  ] as const)('marks a %s role assignment next to its code', (validationStatus, expected) => {
    setInputs('role', {
      items: [
        roleRow({
          validationStatus,
          manualValidatedAt: validationStatus === 'MANUAL' ? '2026-10-07T16:21:12' : null,
          manualValidatedBy: validationStatus === 'MANUAL' ? 'admin' : null,
        }),
      ],
      total: 1,
    });

    const indicator = fixture.nativeElement.querySelector(
      '.application-security-role-validation',
    ) as HTMLElement | null;
    if (!expected) {
      expect(indicator).toBeNull();
      return;
    }
    expect(indicator?.closest('td')?.textContent).toContain('INV_ADMIN');
    expect(indicator?.getAttribute('role')).toBe('img');
    expect(indicator?.tabIndex).toBe(0);
    expect(indicator?.getAttribute('aria-label')).toBe(expected.label);
    expect(indicator?.querySelector(expected.icon)?.getAttribute('aria-hidden')).toBe('true');
    expect(
      fixture.debugElement.query(By.css('.application-security-role-validation')).injector.get(Tooltip)
        .content,
    ).toBe(expected.label);
  });

  it('falls back to a plain manual label and hides indicators on inactive roles', () => {
    setInputs('role', {
      items: [
        roleRow({ validationStatus: 'MANUAL' }),
        roleRow({ id: 2, validationStatus: 'NOT_VALIDATED', deletedAt: '2026-10-01T10:00:00' }),
      ],
      total: 2,
    });

    const indicators = fixture.nativeElement.querySelectorAll(
      '.application-security-role-validation',
    ) as NodeListOf<HTMLElement>;
    expect(indicators).toHaveLength(1);
    expect(indicators[0].getAttribute('aria-label')).toBe('Rol validat manualment');
  });

  it('does not open the row when the validation indicator receives Enter or a double click', () => {
    const action = vi.fn();
    component.onSelectAction.subscribe(action);
    setInputs('role', { items: [roleRow({ validationStatus: 'NOT_VALIDATED' })], total: 1 });
    const indicator = fixture.nativeElement.querySelector(
      '.application-security-role-validation',
    ) as HTMLElement;

    indicator.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter', bubbles: true }));
    indicator.dispatchEvent(new MouseEvent('dblclick', { bubbles: true }));

    expect(action).not.toHaveBeenCalled();
  });

  it('offers manual validation in the row menu only for an editable, not validated role', () => {
    const pending = roleRow({ validationStatus: 'NOT_VALIDATED' });
    const action = vi.fn();
    component.onSelectAction.subscribe(action);
    fixture.componentRef.setInput('showActions', true);
    setInputs('role', { items: [pending, roleRow({ id: 2 })], total: 2 });
    const table = component as unknown as {
      openActionsMenu(event: Event, row: ApplicationSecurityResourceOutput): void;
      rowActions(): { label: string; command(): void }[];
    };
    const labels = () => table.rowActions().map((item) => item.label);

    expect(fixture.nativeElement.querySelector('tbody tr')?.getAttribute('data-role-id')).toBe('1');
    table.openActionsMenu(new MouseEvent('click'), pending);
    expect(labels()).toEqual(['Consulta', 'Validar manualment', 'Edita', 'Dona de baixa']);
    table.rowActions()[1].command();
    expect(action).toHaveBeenCalledExactlyOnceWith({
      action: ApplicationSecurityTableAction.Verify,
      params: pending,
    });

    table.openActionsMenu(new MouseEvent('click'), roleRow({ id: 2 }));
    expect(labels()).not.toContain('Validar manualment');
    fixture.componentRef.setInput('isReadOnly', true);
    table.openActionsMenu(new MouseEvent('click'), pending);
    expect(labels()).not.toContain('Validar manualment');
  });

  function roleRow(overrides: Partial<ApplicationSecurityRoleOutput> = {}): ApplicationSecurityRoleOutput {
    return {
      id: 1,
      appSecurity: {} as ApplicationSecurityRoleOutput['appSecurity'],
      securityRole: {
        id: null,
        roleId: 22,
        name: 'INV_ADMIN',
        system: 'INVAI',
        description: 'Administració',
      },
      deletedAt: null,
      validationStatus: 'VALIDATED',
      manualValidatedAt: null,
      manualValidatedBy: null,
      ...overrides,
    };
  }

  function setInputs(
    kind: 'role' | 'risk' | 'web-context',
    itemsList: PaginatedList<ApplicationSecurityResourceOutput>,
  ): void {
    fixture.componentRef.setInput('kind', kind);
    fixture.componentRef.setInput(
      'columns',
      kind === 'role' ? columns : [{ key: 'description', label: 'Descripció' }],
    );
    fixture.componentRef.setInput('itemsList', itemsList);
    fixture.detectChanges();
  }
});
