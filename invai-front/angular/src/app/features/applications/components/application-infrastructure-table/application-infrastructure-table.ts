import { ChangeDetectionStrategy, Component, signal, ViewEncapsulation } from '@angular/core';
import { TableComponentBase } from '@shared/classes/table-component-base';
import { MenuItem, PrimeIcons } from 'primeng/api';
import { Button } from 'primeng/button';
import { Menu } from 'primeng/menu';
import { TableModule } from 'primeng/table';

import { ApplicationInfrastructureResource } from '../../applications.model';
import {
  APPLICATION_INFRASTRUCTURE_TABLE_ACTIONS_ARIA_LABEL,
  APPLICATION_INFRASTRUCTURE_TABLE_ACTIONS_HEADER,
  APPLICATION_INFRASTRUCTURE_TABLE_EDIT_LABEL,
  APPLICATION_INFRASTRUCTURE_TABLE_WITHDRAW_LABEL,
} from './application-infrastructure-table.i18n';

export enum ApplicationInfrastructureTableAction {
  Edit = 1,
  Withdraw = 2,
}

@Component({
  selector: 'app-application-infrastructure-table',
  standalone: true,
  imports: [Button, Menu, TableModule],
  templateUrl: './application-infrastructure-table.html',
  styleUrl: './application-infrastructure-table.scss',
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationInfrastructureTable extends TableComponentBase<ApplicationInfrastructureResource> {
  private readonly selectedRow = signal<ApplicationInfrastructureResource | null>(null);

  protected readonly PrimeIcons = PrimeIcons;
  protected readonly actionsHeader = APPLICATION_INFRASTRUCTURE_TABLE_ACTIONS_HEADER;
  protected readonly actionsAriaLabel = APPLICATION_INFRASTRUCTURE_TABLE_ACTIONS_ARIA_LABEL;
  protected readonly rowActions: MenuItem[] = [
    {
      label: APPLICATION_INFRASTRUCTURE_TABLE_EDIT_LABEL,
      icon: PrimeIcons.PENCIL,
      command: () => this.emitRowAction(ApplicationInfrastructureTableAction.Edit),
    },
    {
      label: APPLICATION_INFRASTRUCTURE_TABLE_WITHDRAW_LABEL,
      icon: PrimeIcons.TIMES,
      command: () => this.emitRowAction(ApplicationInfrastructureTableAction.Withdraw),
    },
  ];

  protected openActionsMenu(
    event: Event,
    row: ApplicationInfrastructureResource,
    menu: Menu,
  ): void {
    this.selectedRow.set(row);
    menu.toggle(event);
  }

  private emitRowAction(action: ApplicationInfrastructureTableAction): void {
    const row = this.selectedRow();
    if (!row) return;

    this.onSelectedAction(action, row);
  }
}
