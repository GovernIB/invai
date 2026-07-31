import { DatePipe } from '@angular/common';
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
import { COMMISSION_TYPE_LABELS } from '../../commissions.constants';
import { Commission, CommissionType } from '../../commissions.model';
import {
  COMMISSIONS_TABLE_ACTIONS_ARIA_LABEL,
  COMMISSIONS_TABLE_DELETE_LABEL,
  COMMISSIONS_TABLE_EDIT_LABEL,
  COMMISSIONS_TABLE_VIEW_LABEL,
} from './commissions-table.i18n';

export enum CommissionTableAction {
  View = 1,
  Edit,
  Delete,
  Restore,
}

@Component({
  selector: 'app-commissions-table',
  standalone: true,
  imports: [Button, DatePipe, Menu, RestoreRecordMenu, Skeleton, TableModule],
  templateUrl: './commissions-table.html',
  styleUrl: './commissions-table.scss',
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CommissionsTable extends TableComponentBase<Commission> {
  first = input(0);
  isInitialLoading = input(false);

  private readonly selectedRow = signal<Commission | null>(null);

  protected readonly PrimeIcons = PrimeIcons;
  protected readonly CommissionTableAction = CommissionTableAction;
  protected readonly actionsAriaLabel = COMMISSIONS_TABLE_ACTIONS_ARIA_LABEL;
  protected readonly rowActions: MenuItem[] = [
    {
      label: COMMISSIONS_TABLE_VIEW_LABEL,
      icon: PrimeIcons.EYE,
      command: () => this.emitRowAction(CommissionTableAction.View),
    },
    {
      label: COMMISSIONS_TABLE_EDIT_LABEL,
      icon: PrimeIcons.PENCIL,
      command: () => this.emitRowAction(CommissionTableAction.Edit),
    },
    {
      label: COMMISSIONS_TABLE_DELETE_LABEL,
      icon: PrimeIcons.TRASH,
      command: () => this.emitRowAction(CommissionTableAction.Delete),
    },
  ];
  protected readonly skeletonRows = Array.from({ length: this.PAGINATOR_ROWS });
  protected readonly isRefreshing = computed(
    () => this.isLoading() && !this.isInitialLoading(),
  );

  protected readonly statusLabel = softDeleteStatusLabel;

  protected openActionsMenu(event: Event, row: Commission, menu: Menu): void {
    this.selectedRow.set(row);
    menu.toggle(event);
  }

  protected getCommissionTypeLabel(type: CommissionType | null): string {
    return type ? COMMISSION_TYPE_LABELS[type] : '';
  }

  private readonly table = viewChild.required<Table>('table');

  resetState(): void {
    this.table().reset();
  }

  private emitRowAction(action: CommissionTableAction): void {
    const row = this.selectedRow();
    if (!row) return;

    this.onSelectedAction(action, row);
  }
}
