import { ComponentFixture, TestBed } from '@angular/core/testing';
import { KeyLabel, PaginatedList } from '@models/table.model';

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

  it('renders roles as a striped, non-interactive read-only table', () => {
    setInputs('role', {
      items: [
        {
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
        } as ApplicationSecurityRoleOutput,
      ],
      total: 1,
    });

    const table = fixture.nativeElement.querySelector('p-table');
    const row = fixture.nativeElement.querySelector('tbody tr');
    expect(component).toBeTruthy();
    expect(table).toBeTruthy();
    expect(row.getAttribute('tabindex')).toBeNull();
    expect(row.textContent).toContain('INVAI');
    expect(row.textContent).toContain('Administració');
    expect(fixture.nativeElement.querySelector('.invai-table-actions-column')).toBeNull();
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
