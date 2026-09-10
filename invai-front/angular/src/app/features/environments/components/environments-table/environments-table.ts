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
import { Environment } from '../../environments.model';
import {
  ENVIRONMENTS_TABLE_ACTIONS_ARIA_LABEL,
  ENVIRONMENTS_TABLE_DELETE_LABEL,
  ENVIRONMENTS_TABLE_EDIT_LABEL,
  ENVIRONMENTS_TABLE_VIEW_LABEL,
} from './environments-table.i18n';

export enum EnvironmentTableAction {
  View = 1,
  Edit,
  Delete,
  Restore,
}

@Component({
  selector: 'app-environments-table',
  standalone: true,
  imports: [StatusTagComponent, Button, Menu, RestoreRecordMenu, Skeleton, TableModule],
  templateUrl: './environments-table.html',
  styleUrl: './environments-table.scss',
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EnvironmentsTable extends TableComponentBase<Environment> {
  first = input(0);
  isInitialLoading = input(false);

  private readonly selectedRow = signal<Environment | null>(null);

  protected readonly PrimeIcons = PrimeIcons;
  protected readonly EnvironmentTableAction = EnvironmentTableAction;
  protected readonly actionsAriaLabel = ENVIRONMENTS_TABLE_ACTIONS_ARIA_LABEL;
  protected readonly rowActions: MenuItem[] = [
    {
      label: ENVIRONMENTS_TABLE_VIEW_LABEL,
      icon: PrimeIcons.EYE,
      command: () => this.emitRowAction(EnvironmentTableAction.View),
    },
    {
      label: ENVIRONMENTS_TABLE_EDIT_LABEL,
      icon: PrimeIcons.PENCIL,
      command: () => this.emitRowAction(EnvironmentTableAction.Edit),
    },
    {
      label: ENVIRONMENTS_TABLE_DELETE_LABEL,
      icon: PrimeIcons.TRASH,
      command: () => this.emitRowAction(EnvironmentTableAction.Delete),
    },
  ];
  protected readonly skeletonRows = Array.from({ length: this.PAGINATOR_ROWS });
  protected readonly isRefreshing = computed(
    () => this.isLoading() && !this.isInitialLoading(),
  );

  protected readonly statusLabel = softDeleteStatusLabel;
  private readonly table = viewChild.required<Table>('table');

  protected openActionsMenu(event: Event, row: Environment, menu: Menu): void {
    this.selectedRow.set(row);
    menu.toggle(event);
  }

  resetState(): void {
    this.table().reset();
  }

  private emitRowAction(action: EnvironmentTableAction): void {
    const row = this.selectedRow();
    if (!row) return;

    this.onSelectedAction(action, row);
  }
}
