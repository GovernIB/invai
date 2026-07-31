import {
  ChangeDetectionStrategy,
  Component,
  computed,
  input,
  signal,
  ViewEncapsulation,
} from '@angular/core';
import { TableComponentBase } from '@shared/classes/table-component-base';
import { MenuItem, PrimeIcons } from 'primeng/api';
import { Button } from 'primeng/button';
import { Menu } from 'primeng/menu';
import { TableModule } from 'primeng/table';

import { ApplicationTechnologyOutput } from '../../applications.model';
import {
  APPLICATION_TECHNOLOGIES_TABLE_ACTIONS_ARIA_LABEL,
  APPLICATION_TECHNOLOGIES_TABLE_ACTIONS_HEADER,
  APPLICATION_TECHNOLOGIES_TABLE_DELETE_LABEL,
  APPLICATION_TECHNOLOGIES_TABLE_EDIT_LABEL,
  APPLICATION_TECHNOLOGIES_TABLE_VIEW_LABEL,
} from './application-technologies-table.i18n';

export enum ApplicationTechnologyTableAction {
  View = 1,
  Edit,
  Delete,
}

@Component({
  selector: 'app-application-technologies-table',
  standalone: true,
  imports: [Button, Menu, TableModule],
  templateUrl: './application-technologies-table.html',
  styleUrl: '../../../../shared/styles/development-maintenance-table.scss',
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationTechnologiesTable extends TableComponentBase<ApplicationTechnologyOutput> {
  first = input(0);
  isReadOnly = input(false);

  private readonly selectedRow = signal<ApplicationTechnologyOutput | null>(null);

  protected readonly icons = PrimeIcons;
  protected readonly ApplicationTechnologyTableAction =
    ApplicationTechnologyTableAction;
  protected readonly actionsAriaLabel =
    APPLICATION_TECHNOLOGIES_TABLE_ACTIONS_ARIA_LABEL;
  protected readonly actionsHeader =
    APPLICATION_TECHNOLOGIES_TABLE_ACTIONS_HEADER;
  protected readonly rowActions = computed<MenuItem[]>(() => {
    const isMutationDisabled =
      this.isReadOnly() || Boolean(this.selectedRow()?.deletedAt);
    return [
      {
        label: APPLICATION_TECHNOLOGIES_TABLE_VIEW_LABEL,
        icon: PrimeIcons.EYE,
        command: () => this.emitRowAction(ApplicationTechnologyTableAction.View),
      },
      {
        label: APPLICATION_TECHNOLOGIES_TABLE_EDIT_LABEL,
        icon: PrimeIcons.PENCIL,
        disabled: isMutationDisabled,
        command: () => this.emitRowAction(ApplicationTechnologyTableAction.Edit),
      },
      {
        label: APPLICATION_TECHNOLOGIES_TABLE_DELETE_LABEL,
        icon: PrimeIcons.TRASH,
        disabled: isMutationDisabled,
        command: () => this.emitRowAction(ApplicationTechnologyTableAction.Delete),
      },
    ];
  });

  protected openActionsMenu(
    event: Event,
    row: ApplicationTechnologyOutput,
    menu: Menu,
  ): void {
    this.selectedRow.set(row);
    menu.toggle(event);
  }

  private emitRowAction(action: ApplicationTechnologyTableAction): void {
    const row = this.selectedRow();
    if (row) this.onSelectedAction(action, row);
  }
}
