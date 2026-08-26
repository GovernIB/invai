import {
  ChangeDetectionStrategy,
  Component,
  computed,
  input,
  signal,
  viewChild,
  ViewEncapsulation,
} from '@angular/core';
import { TableComponentBase } from '@shared/classes/table-component-base';
import { MenuItem, PrimeIcons } from 'primeng/api';
import { Button } from 'primeng/button';
import { Menu } from 'primeng/menu';
import { TableModule } from 'primeng/table';

import { ApplicationInfrastructureResource } from '../../applications.model';
import {
  APPLICATION_INFRASTRUCTURE_TABLE_ACTIONS_ARIA_LABEL,
  APPLICATION_INFRASTRUCTURE_TABLE_ACTIONS_HEADER,
  APPLICATION_INFRASTRUCTURE_TABLE_DELETE_LABEL,
  APPLICATION_INFRASTRUCTURE_TABLE_VIEW_LABEL,
} from './application-infrastructure-table.i18n';

export enum ApplicationInfrastructureTableAction {
  View = 1,
  Delete,
}

@Component({
  selector: 'app-application-infrastructure-table',
  standalone: true,
  imports: [Button, Menu, TableModule],
  templateUrl: './application-infrastructure-table.html',
  styleUrls: [
    './application-infrastructure-table.scss',
    '../../../../shared/styles/development-maintenance-table.scss',
  ],
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationInfrastructureTable extends TableComponentBase<ApplicationInfrastructureResource> {
  first = input(0);
  isReadOnly = input(false);
  showActions = input(true);
  private readonly selectedRow = signal<ApplicationInfrastructureResource | null>(null);
  private readonly rowMenu = viewChild<Menu>('rowMenu');

  protected readonly PrimeIcons = PrimeIcons;
  protected readonly ApplicationInfrastructureTableAction = ApplicationInfrastructureTableAction;
  protected readonly actionsHeader = APPLICATION_INFRASTRUCTURE_TABLE_ACTIONS_HEADER;
  protected readonly actionsAriaLabel = APPLICATION_INFRASTRUCTURE_TABLE_ACTIONS_ARIA_LABEL;
  protected readonly rowActions = computed<MenuItem[]>(() => {
    const isMutationDisabled = this.isReadOnly() || Boolean(this.selectedRow()?.deletedAt);
    return [
      {
        label: APPLICATION_INFRASTRUCTURE_TABLE_VIEW_LABEL,
        icon: PrimeIcons.EYE,
        command: () => this.emitRowAction(ApplicationInfrastructureTableAction.View),
      },
      {
        label: APPLICATION_INFRASTRUCTURE_TABLE_DELETE_LABEL,
        icon: PrimeIcons.TRASH,
        disabled: isMutationDisabled,
        command: () => this.emitRowAction(ApplicationInfrastructureTableAction.Delete),
      },
    ];
  });

  protected openActionsMenu(
    event: Event,
    row: ApplicationInfrastructureResource,
    menu = this.rowMenu(),
  ): void {
    this.selectedRow.set(row);
    menu?.toggle(event);
  }

  private emitRowAction(action: ApplicationInfrastructureTableAction): void {
    const row = this.selectedRow();
    if (!row) return;

    this.onSelectedAction(action, row);
  }
}
