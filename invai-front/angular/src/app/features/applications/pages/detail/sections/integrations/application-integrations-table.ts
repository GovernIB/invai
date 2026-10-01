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

import {
  ApplicationIntegrationConnectionOutput,
  SoffidRole,
} from '../../../../applications.model';
import {
  APPLICATION_INTEGRATIONS_EMPTY_VALUE,
  APPLICATION_INTEGRATIONS_ROLES_TEXTS,
  APPLICATION_INTEGRATIONS_SYSTEM_KINDS,
  APPLICATION_INTEGRATIONS_TABLE_ACTIONS,
} from './application-integrations-section.i18n';

export enum ApplicationIntegrationsTableAction {
  View = 1,
  Edit,
  Delete,
}

export type ApplicationIntegrationRolesMatch = 'mismatch' | 'match' | 'unknown';

export function connectionSystemName(row: ApplicationIntegrationConnectionOutput): string {
  const system = row.application ?? row.externalSystem;
  return system?.name?.trim() || APPLICATION_INTEGRATIONS_EMPTY_VALUE;
}

export function connectionSystemKind(row: ApplicationIntegrationConnectionOutput): string {
  if (row.application) return APPLICATION_INTEGRATIONS_SYSTEM_KINDS.application;
  return row.externalSystem ? APPLICATION_INTEGRATIONS_SYSTEM_KINDS.external : '';
}

export function soffidRoleNames(roles: SoffidRole[] | null): string {
  const names = (roles ?? []).map((role) => role.name?.trim() || `#${role.id}`);
  return names.length ? names.join(', ') : APPLICATION_INTEGRATIONS_EMPTY_VALUE;
}

export function grantedRoleNames(row: ApplicationIntegrationConnectionOutput): string {
  return row.grantedRoles === null
    ? APPLICATION_INTEGRATIONS_ROLES_TEXTS.grantedUnavailable
    : soffidRoleNames(row.grantedRoles);
}

// null means Soffid could not be queried, which is not the same as matching roles.
export function rolesMatch(row: ApplicationIntegrationConnectionOutput): ApplicationIntegrationRolesMatch {
  if (row.rolesMismatch === true) return 'mismatch';
  return row.rolesMismatch === false ? 'match' : 'unknown';
}

@Component({
  selector: 'app-application-integrations-table',
  standalone: true,
  imports: [Button, Menu, TableModule],
  templateUrl: './application-integrations-table.html',
  styleUrls: [
    '../../../../../../shared/styles/development-maintenance-table.scss',
    './application-integrations-table.scss',
  ],
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationIntegrationsTable extends TableComponentBase<ApplicationIntegrationConnectionOutput> {
  first = input(0);
  isReadOnly = input(false);
  showActions = input(false);

  private readonly selectedRow = signal<ApplicationIntegrationConnectionOutput | null>(null);

  protected readonly TableAction = ApplicationIntegrationsTableAction;
  protected readonly icons = PrimeIcons;
  protected readonly actions = APPLICATION_INTEGRATIONS_TABLE_ACTIONS;
  protected readonly rolesTexts = APPLICATION_INTEGRATIONS_ROLES_TEXTS;
  protected readonly emptyValue = APPLICATION_INTEGRATIONS_EMPTY_VALUE;
  protected readonly menuVisible = signal(false);
  protected readonly selectedRowId = computed(() => this.selectedRow()?.id);
  protected readonly rowActions = computed<MenuItem[]>(() => [
    {
      label: this.actions.view,
      icon: PrimeIcons.EYE,
      command: () => this.emitRowAction(ApplicationIntegrationsTableAction.View),
    },
    {
      label: this.actions.edit,
      icon: PrimeIcons.PENCIL,
      disabled: this.isReadOnly(),
      command: () => this.emitRowAction(ApplicationIntegrationsTableAction.Edit),
    },
    {
      label: this.actions.delete,
      icon: PrimeIcons.TRASH,
      disabled: this.isReadOnly(),
      command: () => this.emitRowAction(ApplicationIntegrationsTableAction.Delete),
    },
  ]);

  protected readonly systemName = connectionSystemName;
  protected readonly systemKind = connectionSystemKind;
  protected readonly rolesMatch = rolesMatch;

  protected cellValue(row: ApplicationIntegrationConnectionOutput, key: string): string {
    switch (key) {
      case 'technology':
        return row.technology?.name?.trim() || this.emptyValue;
      case 'username':
        return row.username?.trim() || this.emptyValue;
      case 'requiredRoles':
        return soffidRoleNames(row.requiredRoles);
      case 'grantedRoles':
        return grantedRoleNames(row);
      default:
        return this.emptyValue;
    }
  }

  protected openActionsMenu(event: Event, row: ApplicationIntegrationConnectionOutput, menu: Menu): void {
    this.selectedRow.set(row);
    menu.toggle(event);
  }

  private emitRowAction(action: ApplicationIntegrationsTableAction): void {
    const row = this.selectedRow();
    if (row) this.onSelectedAction(action, row);
  }
}
