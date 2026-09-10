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

import { SecurityResource } from '../../security.model';
import {
  SECURITY_TABLE_ACTIONS_ARIA_LABEL,
  SECURITY_TABLE_DEACTIVATE_LABEL,
  SECURITY_TABLE_EDIT_LABEL,
  SECURITY_TABLE_VIEW_LABEL,
} from '../../security.i18n';

export enum SecurityResourceTableAction {
  View = 1,
  Edit,
  Deactivate,
  Restore,
}

@Component({
  selector: 'app-security-resource-table',
  standalone: true,
  imports: [StatusTagComponent, Button, Menu, RestoreRecordMenu, Skeleton, TableModule],
  templateUrl: './security-resource-table.html',
  styleUrl: '../../../../../shared/styles/development-maintenance-table.scss',
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SecurityResourceTable extends TableComponentBase<SecurityResource> {
  first = input(0);
  isInitialLoading = input(false);

  private readonly selectedRow = signal<SecurityResource | null>(null);
  private readonly table = viewChild.required<Table>('table');

  protected readonly icons = PrimeIcons;
  protected readonly tableAction = SecurityResourceTableAction;
  protected readonly actionsAriaLabel = SECURITY_TABLE_ACTIONS_ARIA_LABEL;
  protected readonly statusLabel = softDeleteStatusLabel;
  protected readonly skeletonRows = Array.from({ length: this.PAGINATOR_ROWS });
  protected readonly isRefreshing = computed(() => this.isLoading() && !this.isInitialLoading());
  protected readonly rowActions: MenuItem[] = [
    {
      label: SECURITY_TABLE_VIEW_LABEL,
      icon: PrimeIcons.EYE,
      command: () => this.emitRowAction(SecurityResourceTableAction.View),
    },
    {
      label: SECURITY_TABLE_EDIT_LABEL,
      icon: PrimeIcons.PENCIL,
      command: () => this.emitRowAction(SecurityResourceTableAction.Edit),
    },
    {
      label: SECURITY_TABLE_DEACTIVATE_LABEL,
      icon: PrimeIcons.TRASH,
      command: () => this.emitRowAction(SecurityResourceTableAction.Deactivate),
    },
  ];

  protected cellValue(row: SecurityResource, key: string): string | number | null | undefined {
    if (key === 'status') return this.statusLabel(row.deletedAt);
    if (key === 'name' || key === 'nameEs') return row[key];
    return null;
  }

  protected openActionsMenu(event: Event, row: SecurityResource, menu: Menu): void {
    this.selectedRow.set(row);
    menu.toggle(event);
  }

  resetState(): void {
    this.table().reset();
  }

  private emitRowAction(action: SecurityResourceTableAction): void {
    const row = this.selectedRow();
    if (row) this.onSelectedAction(action, row);
  }
}
