import { ComponentFixture, TestBed } from '@angular/core/testing';
import { KeyLabel, PaginatedList } from '@models/table.model';

import {
  ApplicationSecurityResourceOutput,
  ApplicationSecurityRiskOutput,
  ApplicationSecurityRoleOutput,
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

  function setInputs(
    kind: 'role' | 'risk',
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
