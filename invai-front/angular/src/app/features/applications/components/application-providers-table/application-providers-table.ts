import { DatePipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  inject,
  input,
  LOCALE_ID,
  signal,
  viewChild,
  ViewEncapsulation,
} from '@angular/core';
import { TableComponentBase } from '@shared/classes/table-component-base';
import { localizedName } from '@shared/utils/localized-name.utils';
import { MenuItem, PrimeIcons } from 'primeng/api';
import { Button } from 'primeng/button';
import { Menu } from 'primeng/menu';
import { TableModule } from 'primeng/table';

import { ApplicationProviderOutput } from '../../applications.model';
import {
  APPLICATION_PROVIDERS_TABLE_ACTIONS_ARIA_LABEL,
  APPLICATION_PROVIDERS_TABLE_ACTIONS_HEADER,
  APPLICATION_PROVIDERS_TABLE_DELETE_LABEL,
  APPLICATION_PROVIDERS_TABLE_EDIT_LABEL,
  APPLICATION_PROVIDERS_TABLE_EMPTY_VALUE,
  APPLICATION_PROVIDERS_TABLE_VIEW_LABEL,
} from './application-providers-table.i18n';

export enum ApplicationProviderTableAction {
  View = 1,
  Edit,
  Delete,
}

@Component({
  selector: 'app-application-providers-table',
  standalone: true,
  imports: [Button, DatePipe, Menu, TableModule],
  templateUrl: './application-providers-table.html',
  styleUrl: '../../../../shared/styles/development-maintenance-table.scss',
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationProvidersTable extends TableComponentBase<ApplicationProviderOutput> {
  first = input(0);
  isReadOnly = input(false);
  showActions = input(true);

  private readonly locale = inject(LOCALE_ID);
  private readonly selectedRow = signal<ApplicationProviderOutput | null>(null);
  private readonly rowMenu = viewChild<Menu>('rowMenu');

  protected readonly icons = PrimeIcons;
  protected readonly ApplicationProviderTableAction = ApplicationProviderTableAction;
  protected readonly actionsAriaLabel = APPLICATION_PROVIDERS_TABLE_ACTIONS_ARIA_LABEL;
  protected readonly actionsHeader = APPLICATION_PROVIDERS_TABLE_ACTIONS_HEADER;
  protected readonly emptyValue = APPLICATION_PROVIDERS_TABLE_EMPTY_VALUE;
  protected readonly rowActions = computed<MenuItem[]>(() => {
    const isMutationDisabled = this.isReadOnly() || Boolean(this.selectedRow()?.deletedAt);
    return [
      {
        label: APPLICATION_PROVIDERS_TABLE_VIEW_LABEL,
        icon: PrimeIcons.EYE,
        command: () => this.emitRowAction(ApplicationProviderTableAction.View),
      },
      {
        label: APPLICATION_PROVIDERS_TABLE_EDIT_LABEL,
        icon: PrimeIcons.PENCIL,
        disabled: isMutationDisabled,
        command: () => this.emitRowAction(ApplicationProviderTableAction.Edit),
      },
      {
        label: APPLICATION_PROVIDERS_TABLE_DELETE_LABEL,
        icon: PrimeIcons.TRASH,
        disabled: isMutationDisabled,
        command: () => this.emitRowAction(ApplicationProviderTableAction.Delete),
      },
    ];
  });

  protected roleLabel(provider: ApplicationProviderOutput): string {
    return provider.role
      ? localizedName(
          provider.role,
          this.locale,
          provider.role.name?.trim() || `#${provider.role.id}`,
        )
      : this.emptyValue;
  }

  protected openActionsMenu(
    event: Event,
    row: ApplicationProviderOutput,
    menu = this.rowMenu(),
  ): void {
    this.selectedRow.set(row);
    menu?.toggle(event);
  }

  private emitRowAction(action: ApplicationProviderTableAction): void {
    const row = this.selectedRow();
    if (row) this.onSelectedAction(action, row);
  }
}
