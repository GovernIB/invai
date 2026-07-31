import {
  ChangeDetectionStrategy,
  Component,
  computed,
  input,
  signal,
  viewChild,
  ViewEncapsulation,
} from '@angular/core';
import { RestoreRecordMenu } from '@components/restore-record-menu/restore-record-menu';
import { TableComponentBase } from '@shared/classes/table-component-base';
import { softDeleteStatusLabel } from '@shared/constants/soft-delete-status.constants';
import { MenuItem, PrimeIcons } from 'primeng/api';
import { Button } from 'primeng/button';
import { Menu } from 'primeng/menu';
import { Skeleton } from 'primeng/skeleton';
import { Table, TableModule } from 'primeng/table';

import { Role } from '../../roles.model';
import {
  ROLES_TABLE_ACTIONS_ARIA_LABEL,
  ROLES_TABLE_DELETE_LABEL,
  ROLES_TABLE_EDIT_LABEL,
  ROLES_TABLE_VIEW_LABEL,
} from './roles-table.i18n';

export enum RoleTableAction {
  View = 1,
  Edit,
  Delete,
  Restore,
}

@Component({
  selector: 'app-roles-table',
  standalone: true,
  imports: [Button, Menu, RestoreRecordMenu, Skeleton, TableModule],
  templateUrl: './roles-table.html',
  styleUrl: '../../../../shared/styles/development-maintenance-table.scss',
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RolesTable extends TableComponentBase<Role> {
  first = input(0);
  isInitialLoading = input(false);
  private readonly selectedRow = signal<Role | null>(null);
  private readonly table = viewChild.required<Table>('table');

  protected readonly icons = PrimeIcons;
  protected readonly RoleTableAction = RoleTableAction;
  protected readonly actionsAriaLabel = ROLES_TABLE_ACTIONS_ARIA_LABEL;
  protected readonly statusLabel = softDeleteStatusLabel;
  protected readonly skeletonRows = Array.from({ length: this.PAGINATOR_ROWS });
  protected readonly isRefreshing = computed(() => this.isLoading() && !this.isInitialLoading());
  protected readonly rowActions: MenuItem[] = [
    {
      label: ROLES_TABLE_VIEW_LABEL,
      icon: PrimeIcons.EYE,
      command: () => this.emitRowAction(RoleTableAction.View),
    },
    {
      label: ROLES_TABLE_EDIT_LABEL,
      icon: PrimeIcons.PENCIL,
      command: () => this.emitRowAction(RoleTableAction.Edit),
    },
    {
      label: ROLES_TABLE_DELETE_LABEL,
      icon: PrimeIcons.TRASH,
      command: () => this.emitRowAction(RoleTableAction.Delete),
    },
  ];

  protected openActionsMenu(event: Event, row: Role, menu: Menu): void {
    this.selectedRow.set(row);
    menu.toggle(event);
  }

  resetState(): void {
    this.table().reset();
  }

  private emitRowAction(action: RoleTableAction): void {
    const row = this.selectedRow();
    if (row) this.onSelectedAction(action, row);
  }
}
