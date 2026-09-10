import {
  ChangeDetectionStrategy,
  Component,
  LOCALE_ID,
  computed,
  inject,
  input,
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

import {
  ApplicationSecurityMeasureOutput,
  ApplicationSecurityResourceOutput,
  ApplicationSecurityRiskOutput,
  ApplicationSecurityRoleOutput,
  ApplicationWebContextOutput,
  SecurityCatalogItem,
} from '../../../../applications.model';
import {
  APPLICATION_SECURITY_EMPTY_VALUE,
  APPLICATION_SECURITY_TABLE_ACTIONS,
} from './application-security-section.i18n';

export type ApplicationSecurityTableKind = 'role' | 'web-context' | 'risk' | 'measure';

export enum ApplicationSecurityTableAction {
  View = 1,
  Edit,
  Delete,
}

@Component({
  selector: 'app-application-security-resource-table',
  standalone: true,
  imports: [Button, Menu, TableModule],
  templateUrl: './application-security-resource-table.html',
  styleUrl: '../../../../../../shared/styles/development-maintenance-table.scss',
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationSecurityResourceTable extends TableComponentBase<ApplicationSecurityResourceOutput> {
  kind = input.required<ApplicationSecurityTableKind>();
  first = input(0);
  isReadOnly = input(false);
  showActions = input(false);

  private readonly locale = inject(LOCALE_ID);
  private readonly selectedRow = signal<ApplicationSecurityResourceOutput | null>(null);
  private readonly rowMenu = viewChild<Menu>('rowMenu');

  protected readonly icons = PrimeIcons;
  protected readonly actions = APPLICATION_SECURITY_TABLE_ACTIONS;
  protected readonly emptyValue = APPLICATION_SECURITY_EMPTY_VALUE;
  protected readonly ApplicationSecurityTableAction = ApplicationSecurityTableAction;
  protected readonly isConsultable = computed(() => this.kind() !== 'role');
  protected readonly rowActions = computed<MenuItem[]>(() => [
    {
      label: this.actions.view,
      icon: PrimeIcons.EYE,
      command: () => this.emitRowAction(ApplicationSecurityTableAction.View),
    },
    {
      label: this.actions.edit,
      icon: PrimeIcons.PENCIL,
      disabled: this.isReadOnly() || Boolean(this.selectedRow()?.deletedAt),
      command: () => this.emitRowAction(ApplicationSecurityTableAction.Edit),
    },
    {
      label: this.actions.delete,
      icon: PrimeIcons.TRASH,
      disabled: this.isReadOnly() || Boolean(this.selectedRow()?.deletedAt),
      command: () => this.emitRowAction(ApplicationSecurityTableAction.Delete),
    },
  ]);

  protected cellValue(row: ApplicationSecurityResourceOutput, key: string): string | number {
    switch (key) {
      case 'id':
        return row.id;
      case 'description':
        return this.kind() === 'role'
          ? (row as ApplicationSecurityRoleOutput).securityRole.description || this.emptyValue
          : 'description' in row
            ? row.description || this.emptyValue
            : this.emptyValue;
      case 'observation':
        return 'observation' in row ? row.observation || this.emptyValue : this.emptyValue;
      case 'role':
        return this.catalogLabel((row as ApplicationSecurityRoleOutput).securityRole);
      case 'system':
        return (row as ApplicationSecurityRoleOutput).securityRole.system || this.emptyValue;
      case 'webContext':
        return this.catalogLabel((row as ApplicationWebContextOutput).webContext);
      case 'field':
        return this.catalogLabel(
          (row as ApplicationWebContextOutput | ApplicationSecurityRiskOutput).field,
        );
      case 'level':
        return this.catalogLabel((row as ApplicationSecurityRiskOutput).level);
      case 'type':
        return this.catalogLabel((row as ApplicationSecurityMeasureOutput).type);
      case 'ensRequirement':
        return this.catalogLabel((row as ApplicationSecurityMeasureOutput).ensRequirement);
      default:
        return this.emptyValue;
    }
  }

  protected activateRow(event: Event, row: ApplicationSecurityResourceOutput): void {
    if (!this.isConsultable()) return;
    this.onRowActivate(event, ApplicationSecurityTableAction.View, row);
  }

  protected openActionsMenu(event: Event, row: ApplicationSecurityResourceOutput): void {
    this.selectedRow.set(row);
    this.rowMenu()?.toggle(event);
  }

  private catalogLabel(
    item: (Omit<SecurityCatalogItem, 'id'> & { id: number | null }) | null | undefined,
  ): string {
    if (!item) return this.emptyValue;
    if (item.id == null) {
      return (
        (this.locale.toLowerCase().startsWith('es') ? item.nameEs : item.name) ??
        item.name ??
        item.nameEs ??
        this.emptyValue
      );
    }
    return localizedName(item as SecurityCatalogItem, this.locale, `#${item.id}`);
  }

  private emitRowAction(action: ApplicationSecurityTableAction): void {
    const row = this.selectedRow();
    if (row) this.onSelectedAction(action, row);
  }
}
