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

import { Layer } from '../../layers.model';
import {
  LAYERS_TABLE_ACTIONS_ARIA_LABEL,
  LAYERS_TABLE_DELETE_LABEL,
  LAYERS_TABLE_EDIT_LABEL,
  LAYERS_TABLE_VIEW_LABEL,
} from './layers-table.i18n';

export enum LayerTableAction {
  View = 1,
  Edit,
  Delete,
  Restore,
}

@Component({
  selector: 'app-layers-table',
  standalone: true,
  imports: [Button, Menu, RestoreRecordMenu, Skeleton, TableModule],
  templateUrl: './layers-table.html',
  styleUrl: '../../../../shared/styles/development-maintenance-table.scss',
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LayersTable extends TableComponentBase<Layer> {
  first = input(0);
  isInitialLoading = input(false);
  private readonly selectedRow = signal<Layer | null>(null);
  private readonly table = viewChild.required<Table>('table');

  protected readonly icons = PrimeIcons;
  protected readonly LayerTableAction = LayerTableAction;
  protected readonly actionsAriaLabel = LAYERS_TABLE_ACTIONS_ARIA_LABEL;
  protected readonly statusLabel = softDeleteStatusLabel;
  protected readonly skeletonRows = Array.from({ length: this.PAGINATOR_ROWS });
  protected readonly isRefreshing = computed(() => this.isLoading() && !this.isInitialLoading());
  protected readonly rowActions: MenuItem[] = [
    {
      label: LAYERS_TABLE_VIEW_LABEL,
      icon: PrimeIcons.EYE,
      command: () => this.emitRowAction(LayerTableAction.View),
    },
    {
      label: LAYERS_TABLE_EDIT_LABEL,
      icon: PrimeIcons.PENCIL,
      command: () => this.emitRowAction(LayerTableAction.Edit),
    },
    {
      label: LAYERS_TABLE_DELETE_LABEL,
      icon: PrimeIcons.TRASH,
      command: () => this.emitRowAction(LayerTableAction.Delete),
    },
  ];

  protected openActionsMenu(event: Event, row: Layer, menu: Menu): void {
    this.selectedRow.set(row);
    menu.toggle(event);
  }

  resetState(): void {
    this.table().reset();
  }

  private emitRowAction(action: LayerTableAction): void {
    const row = this.selectedRow();
    if (row) this.onSelectedAction(action, row);
  }
}
