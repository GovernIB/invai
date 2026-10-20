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
import { RestoreRecordMenu } from '@components/restore-record-menu/restore-record-menu';
import { TableComponentBase } from '@shared/classes/table-component-base';
import { softDeleteStatusLabel } from '@shared/constants/soft-delete-status.constants';
import { MenuItem, PrimeIcons } from 'primeng/api';
import { Button } from 'primeng/button';
import { Menu } from 'primeng/menu';
import { Skeleton } from 'primeng/skeleton';
import { Table, TableModule } from 'primeng/table';

import { ExternalSystem } from '../../external-systems.model';
import {
  EXTERNAL_SYSTEMS_TABLE_ACTIONS_ARIA_LABEL,
  EXTERNAL_SYSTEMS_TABLE_DELETE_LABEL,
  EXTERNAL_SYSTEMS_TABLE_EDIT_LABEL,
  EXTERNAL_SYSTEMS_TABLE_VIEW_LABEL,
} from './external-systems-table.i18n';

export enum ExternalSystemTableAction {
  View = 1,
  Edit,
  Delete,
  Restore,
}

@Component({
  selector: 'app-external-systems-table',
  standalone: true,
  imports: [StatusTagComponent, Button, Menu, RestoreRecordMenu, Skeleton, TableModule],
  templateUrl: './external-systems-table.html',
  styleUrl: '../../../../shared/styles/development-maintenance-table.scss',
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ExternalSystemsTable extends TableComponentBase<ExternalSystem> {
  first = input(0);
  isInitialLoading = input(false);
  private readonly selectedRow = signal<ExternalSystem | null>(null);
  private readonly table = viewChild.required<Table>('table');

  protected readonly icons = PrimeIcons;
  protected readonly ExternalSystemTableAction = ExternalSystemTableAction;
  protected readonly actionsAriaLabel = EXTERNAL_SYSTEMS_TABLE_ACTIONS_ARIA_LABEL;
  protected readonly statusLabel = softDeleteStatusLabel;
  protected readonly skeletonRows = Array.from({ length: this.PAGINATOR_ROWS });
  protected readonly isRefreshing = computed(() => this.isLoading() && !this.isInitialLoading());
  protected readonly menuVisible = signal(false);
  protected readonly selectedRowId = computed(() => this.selectedRow()?.id);
  protected readonly rowActions: MenuItem[] = [
    {
      label: EXTERNAL_SYSTEMS_TABLE_VIEW_LABEL,
      icon: PrimeIcons.EYE,
      command: () => this.emitRowAction(ExternalSystemTableAction.View),
    },
    {
      label: EXTERNAL_SYSTEMS_TABLE_EDIT_LABEL,
      icon: PrimeIcons.PENCIL,
      command: () => this.emitRowAction(ExternalSystemTableAction.Edit),
    },
    {
      label: EXTERNAL_SYSTEMS_TABLE_DELETE_LABEL,
      icon: PrimeIcons.TRASH,
      command: () => this.emitRowAction(ExternalSystemTableAction.Delete),
    },
  ];

  protected openActionsMenu(event: Event, row: ExternalSystem, menu: Menu): void {
    this.selectedRow.set(row);
    menu.toggle(event);
  }

  resetState(): void {
    this.table().reset();
  }

  private emitRowAction(action: ExternalSystemTableAction): void {
    const row = this.selectedRow();
    if (row) this.onSelectedAction(action, row);
  }
}
