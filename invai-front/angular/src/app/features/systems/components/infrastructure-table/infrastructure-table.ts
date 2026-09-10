import { StatusTagComponent } from '@components/status-tag/status-tag.component';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  input,
  signal,
  ViewEncapsulation,
} from '@angular/core';
import { RestoreRecordMenu } from '@components/restore-record-menu/restore-record-menu';
import { TableComponentBase } from '@shared/classes/table-component-base';
import { MenuItem, PrimeIcons } from 'primeng/api';
import { Button } from 'primeng/button';
import { Menu } from 'primeng/menu';
import { Skeleton } from 'primeng/skeleton';
import { TableModule } from 'primeng/table';

import { InfrastructureTableRow } from '../../systems.model';
import {
  INFRASTRUCTURE_TABLE_ACTIONS_ARIA_LABEL,
  INFRASTRUCTURE_TABLE_ACTIONS_HEADER,
  INFRASTRUCTURE_TABLE_DELETE_LABEL,
  INFRASTRUCTURE_TABLE_EDIT_LABEL,
  INFRASTRUCTURE_TABLE_VIEW_LABEL,
} from './infrastructure-table.i18n';

export enum InfrastructureTableAction {
  View = 1,
  Edit,
  Delete,
  Restore,
}

@Component({
  selector: 'app-infrastructure-table',
  standalone: true,
  imports: [StatusTagComponent, Button, Menu, RestoreRecordMenu, Skeleton, TableModule],
  templateUrl: './infrastructure-table.html',
  styleUrl: './infrastructure-table.scss',
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class InfrastructureTable extends TableComponentBase<InfrastructureTableRow> {
  first = input(0);
  isInitialLoading = input(false);

  private readonly selectedRow = signal<InfrastructureTableRow | null>(null);

  protected readonly PrimeIcons = PrimeIcons;
  protected readonly InfrastructureTableAction = InfrastructureTableAction;
  protected readonly actionsHeader = INFRASTRUCTURE_TABLE_ACTIONS_HEADER;
  protected readonly actionsAriaLabel = INFRASTRUCTURE_TABLE_ACTIONS_ARIA_LABEL;
  protected readonly skeletonRows = Array.from({ length: this.PAGINATOR_ROWS });
  protected readonly isRefreshing = computed(() => this.isLoading() && !this.isInitialLoading());
  protected readonly rowActions: MenuItem[] = [
    {
      label: INFRASTRUCTURE_TABLE_VIEW_LABEL,
      icon: PrimeIcons.EYE,
      command: () => this.emitRowAction(InfrastructureTableAction.View),
    },
    {
      label: INFRASTRUCTURE_TABLE_EDIT_LABEL,
      icon: PrimeIcons.PENCIL,
      command: () => this.emitRowAction(InfrastructureTableAction.Edit),
    },
    {
      label: INFRASTRUCTURE_TABLE_DELETE_LABEL,
      icon: PrimeIcons.TRASH,
      command: () => this.emitRowAction(InfrastructureTableAction.Delete),
    },
  ];

  protected openActionsMenu(event: Event, row: InfrastructureTableRow, menu: Menu): void {
    this.selectedRow.set(row);
    menu.toggle(event);
  }

  private emitRowAction(action: InfrastructureTableAction): void {
    const row = this.selectedRow();
    if (!row) return;

    this.onSelectedAction(action, row);
  }
}
