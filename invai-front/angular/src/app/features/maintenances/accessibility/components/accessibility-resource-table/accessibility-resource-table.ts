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

import { AccessibilityResource } from '../../accessibility.model';
import {
  ACCESSIBILITY_TABLE_ACTIONS_ARIA_LABEL,
  ACCESSIBILITY_TABLE_DEACTIVATE_LABEL,
  ACCESSIBILITY_TABLE_EDIT_LABEL,
  ACCESSIBILITY_TABLE_VIEW_LABEL,
} from '../../accessibility.i18n';

export enum AccessibilityResourceTableAction {
  View = 1,
  Edit,
  Deactivate,
  Restore,
}

@Component({
  selector: 'app-accessibility-resource-table',
  standalone: true,
  imports: [StatusTagComponent, Button, Menu, RestoreRecordMenu, Skeleton, TableModule],
  templateUrl: './accessibility-resource-table.html',
  styleUrl: '../../../../../shared/styles/development-maintenance-table.scss',
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AccessibilityResourceTable extends TableComponentBase<AccessibilityResource> {
  first = input(0);
  isInitialLoading = input(false);

  private readonly selectedRow = signal<AccessibilityResource | null>(null);
  private readonly table = viewChild.required<Table>('table');

  protected readonly icons = PrimeIcons;
  protected readonly tableAction = AccessibilityResourceTableAction;
  protected readonly actionsAriaLabel = ACCESSIBILITY_TABLE_ACTIONS_ARIA_LABEL;
  protected readonly statusLabel = softDeleteStatusLabel;
  protected readonly skeletonRows = Array.from({ length: this.PAGINATOR_ROWS });
  protected readonly isRefreshing = computed(() => this.isLoading() && !this.isInitialLoading());
  protected readonly rowActions: MenuItem[] = [
    {
      label: ACCESSIBILITY_TABLE_VIEW_LABEL,
      icon: PrimeIcons.EYE,
      command: () => this.emitRowAction(AccessibilityResourceTableAction.View),
    },
    {
      label: ACCESSIBILITY_TABLE_EDIT_LABEL,
      icon: PrimeIcons.PENCIL,
      command: () => this.emitRowAction(AccessibilityResourceTableAction.Edit),
    },
    {
      label: ACCESSIBILITY_TABLE_DEACTIVATE_LABEL,
      icon: PrimeIcons.TRASH,
      command: () => this.emitRowAction(AccessibilityResourceTableAction.Deactivate),
    },
  ];

  protected cellValue(row: AccessibilityResource, key: string): string | number | null | undefined {
    if (key === 'status') return this.statusLabel(row.deletedAt);
    if (key === 'id' || key === 'name' || key === 'nameEs') return row[key];
    return null;
  }

  protected openActionsMenu(event: Event, row: AccessibilityResource, menu: Menu): void {
    this.selectedRow.set(row);
    menu.toggle(event);
  }

  resetState(): void {
    this.table().reset();
  }

  private emitRowAction(action: AccessibilityResourceTableAction): void {
    const row = this.selectedRow();
    if (row) this.onSelectedAction(action, row);
  }
}
