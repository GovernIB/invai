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
import { Field } from '../../fields.model';
import {
  FIELDS_TABLE_ACTIONS_ARIA_LABEL,
  FIELDS_TABLE_DELETE_LABEL,
  FIELDS_TABLE_EDIT_LABEL,
  FIELDS_TABLE_VIEW_LABEL,
} from './fields-table.i18n';

export enum FieldTableAction {
  View = 1,
  Edit,
  Delete,
  Restore,
}

@Component({
  selector: 'app-fields-table',
  standalone: true,
  imports: [StatusTagComponent, Button, Menu, RestoreRecordMenu, Skeleton, TableModule],
  templateUrl: './fields-table.html',
  styleUrl: './fields-table.scss',
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FieldsTable extends TableComponentBase<Field> {
  first = input(0);
  isInitialLoading = input(false);

  private readonly selectedRow = signal<Field | null>(null);

  protected readonly PrimeIcons = PrimeIcons;
  protected readonly FieldTableAction = FieldTableAction;
  protected readonly actionsAriaLabel = FIELDS_TABLE_ACTIONS_ARIA_LABEL;
  protected readonly rowActions: MenuItem[] = [
    {
      label: FIELDS_TABLE_VIEW_LABEL,
      icon: PrimeIcons.EYE,
      command: () => this.emitRowAction(FieldTableAction.View),
    },
    {
      label: FIELDS_TABLE_EDIT_LABEL,
      icon: PrimeIcons.PENCIL,
      command: () => this.emitRowAction(FieldTableAction.Edit),
    },
    {
      label: FIELDS_TABLE_DELETE_LABEL,
      icon: PrimeIcons.TRASH,
      command: () => this.emitRowAction(FieldTableAction.Delete),
    },
  ];
  protected readonly skeletonRows = Array.from({ length: this.PAGINATOR_ROWS });
  protected readonly isRefreshing = computed(
    () => this.isLoading() && !this.isInitialLoading(),
  );

  protected readonly statusLabel = softDeleteStatusLabel;
  private readonly table = viewChild.required<Table>('table');

  protected openActionsMenu(event: Event, row: Field, menu: Menu): void {
    this.selectedRow.set(row);
    menu.toggle(event);
  }

  resetState(): void {
    this.table().reset();
  }

  private emitRowAction(action: FieldTableAction): void {
    const row = this.selectedRow();
    if (!row) return;

    this.onSelectedAction(action, row);
  }
}
