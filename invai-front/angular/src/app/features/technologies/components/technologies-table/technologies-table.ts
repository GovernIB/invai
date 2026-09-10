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

import { Technology } from '../../technologies.model';
import {
  TECHNOLOGIES_TABLE_ACTIONS_ARIA_LABEL,
  TECHNOLOGIES_TABLE_DELETE_LABEL,
  TECHNOLOGIES_TABLE_EDIT_LABEL,
  TECHNOLOGIES_TABLE_VIEW_LABEL,
} from './technologies-table.i18n';

export enum TechnologyTableAction {
  View = 1,
  Edit,
  Delete,
  Restore,
}

@Component({
  selector: 'app-technologies-table',
  standalone: true,
  imports: [StatusTagComponent, Button, Menu, RestoreRecordMenu, Skeleton, TableModule],
  templateUrl: './technologies-table.html',
  styleUrl: '../../../../shared/styles/development-maintenance-table.scss',
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TechnologiesTable extends TableComponentBase<Technology> {
  first = input(0);
  isInitialLoading = input(false);
  private readonly selectedRow = signal<Technology | null>(null);
  private readonly table = viewChild.required<Table>('table');

  protected readonly icons = PrimeIcons;
  protected readonly TechnologyTableAction = TechnologyTableAction;
  protected readonly actionsAriaLabel = TECHNOLOGIES_TABLE_ACTIONS_ARIA_LABEL;
  protected readonly statusLabel = softDeleteStatusLabel;
  protected readonly skeletonRows = Array.from({ length: this.PAGINATOR_ROWS });
  protected readonly isRefreshing = computed(() => this.isLoading() && !this.isInitialLoading());
  protected readonly rowActions: MenuItem[] = [
    {
      label: TECHNOLOGIES_TABLE_VIEW_LABEL,
      icon: PrimeIcons.EYE,
      command: () => this.emitRowAction(TechnologyTableAction.View),
    },
    {
      label: TECHNOLOGIES_TABLE_EDIT_LABEL,
      icon: PrimeIcons.PENCIL,
      command: () => this.emitRowAction(TechnologyTableAction.Edit),
    },
    {
      label: TECHNOLOGIES_TABLE_DELETE_LABEL,
      icon: PrimeIcons.TRASH,
      command: () => this.emitRowAction(TechnologyTableAction.Delete),
    },
  ];

  protected openActionsMenu(event: Event, row: Technology, menu: Menu): void {
    this.selectedRow.set(row);
    menu.toggle(event);
  }

  resetState(): void {
    this.table().reset();
  }

  private emitRowAction(action: TechnologyTableAction): void {
    const row = this.selectedRow();
    if (row) this.onSelectedAction(action, row);
  }
}
