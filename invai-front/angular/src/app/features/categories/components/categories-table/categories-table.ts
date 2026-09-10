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
import { Category } from '../../categories.model';
import {
  CATEGORIES_TABLE_ACTIONS_ARIA_LABEL,
  CATEGORIES_TABLE_DELETE_LABEL,
  CATEGORIES_TABLE_EDIT_LABEL,
  CATEGORIES_TABLE_VIEW_LABEL,
} from './categories-table.i18n';

export enum CategoryTableAction {
  View = 1,
  Edit,
  Delete,
  Restore,
}

@Component({
  selector: 'app-categories-table',
  standalone: true,
  imports: [StatusTagComponent, Button, Menu, RestoreRecordMenu, Skeleton, TableModule],
  templateUrl: './categories-table.html',
  styleUrl: './categories-table.scss',
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CategoriesTable extends TableComponentBase<Category> {
  first = input(0);
  isInitialLoading = input(false);

  private readonly selectedRow = signal<Category | null>(null);

  protected readonly PrimeIcons = PrimeIcons;
  protected readonly CategoryTableAction = CategoryTableAction;
  protected readonly actionsAriaLabel = CATEGORIES_TABLE_ACTIONS_ARIA_LABEL;
  protected readonly rowActions: MenuItem[] = [
    {
      label: CATEGORIES_TABLE_VIEW_LABEL,
      icon: PrimeIcons.EYE,
      command: () => this.emitRowAction(CategoryTableAction.View),
    },
    {
      label: CATEGORIES_TABLE_EDIT_LABEL,
      icon: PrimeIcons.PENCIL,
      command: () => this.emitRowAction(CategoryTableAction.Edit),
    },
    {
      label: CATEGORIES_TABLE_DELETE_LABEL,
      icon: PrimeIcons.TRASH,
      command: () => this.emitRowAction(CategoryTableAction.Delete),
    },
  ];
  protected readonly skeletonRows = Array.from({ length: this.PAGINATOR_ROWS });
  protected readonly isRefreshing = computed(
    () => this.isLoading() && !this.isInitialLoading(),
  );

  protected readonly statusLabel = softDeleteStatusLabel;
  private readonly table = viewChild.required<Table>('table');

  protected openActionsMenu(event: Event, row: Category, menu: Menu): void {
    this.selectedRow.set(row);
    menu.toggle(event);
  }

  resetState(): void {
    this.table().reset();
  }

  private emitRowAction(action: CategoryTableAction): void {
    const row = this.selectedRow();
    if (!row) return;

    this.onSelectedAction(action, row);
  }
}
