import { StatusTagComponent } from '@components/status-tag/status-tag.component';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  input,
  signal,
  viewChild,
  ViewEncapsulation,
} from '@angular/core';
import { MenuItem, PrimeIcons } from 'primeng/api';
import { RestoreRecordMenu } from '@components/restore-record-menu/restore-record-menu';
import { Button } from 'primeng/button';
import { Menu } from 'primeng/menu';
import { Skeleton } from 'primeng/skeleton';
import { Table, TableModule } from 'primeng/table';

import { TableComponentBase } from '@shared/classes/table-component-base';
import { softDeleteStatusLabel } from '@shared/constants/soft-delete-status.constants';
import { SystemType } from '../../system-types.model';
import {
  SYSTEM_TYPES_TABLE_ACTIONS_ARIA_LABEL,
  SYSTEM_TYPES_TABLE_DELETE_LABEL,
  SYSTEM_TYPES_TABLE_EDIT_LABEL,
  SYSTEM_TYPES_TABLE_VIEW_LABEL,
} from './system-types-table.i18n';

export enum SystemTypeTableAction {
  View = 1,
  Edit,
  Delete,
  Restore,
}

@Component({
  selector: 'app-system-types-table',
  standalone: true,
  imports: [StatusTagComponent, Button, Menu, RestoreRecordMenu, Skeleton, TableModule],
  templateUrl: './system-types-table.html',
  styleUrl: './system-types-table.scss',
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SystemTypesTable extends TableComponentBase<SystemType> {
  first = input(0);
  isInitialLoading = input(false);

  private readonly selectedRow = signal<SystemType | null>(null);

  protected readonly PrimeIcons = PrimeIcons;
  protected readonly SystemTypeTableAction = SystemTypeTableAction;
  protected readonly actionsAriaLabel = SYSTEM_TYPES_TABLE_ACTIONS_ARIA_LABEL;
  protected readonly rowActions: MenuItem[] = [
    {
      label: SYSTEM_TYPES_TABLE_VIEW_LABEL,
      icon: PrimeIcons.EYE,
      command: () => this.emitRowAction(SystemTypeTableAction.View),
    },
    {
      label: SYSTEM_TYPES_TABLE_EDIT_LABEL,
      icon: PrimeIcons.PENCIL,
      command: () => this.emitRowAction(SystemTypeTableAction.Edit),
    },
    {
      label: SYSTEM_TYPES_TABLE_DELETE_LABEL,
      icon: PrimeIcons.TRASH,
      command: () => this.emitRowAction(SystemTypeTableAction.Delete),
    },
  ];
  protected readonly skeletonRows = Array.from({ length: this.PAGINATOR_ROWS });
  protected readonly isRefreshing = computed(
    () => this.isLoading() && !this.isInitialLoading(),
  );

  protected readonly statusLabel = softDeleteStatusLabel;
  private readonly table = viewChild.required<Table>('table');

  protected openActionsMenu(event: Event, row: SystemType, menu: Menu): void {
    this.selectedRow.set(row);
    menu.toggle(event);
  }

  resetState(): void {
    this.table().reset();
  }

  private emitRowAction(action: SystemTypeTableAction): void {
    const row = this.selectedRow();
    if (!row) return;

    this.onSelectedAction(action, row);
  }
}
