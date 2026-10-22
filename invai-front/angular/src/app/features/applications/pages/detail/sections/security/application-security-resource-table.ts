import { formatDate } from '@angular/common';
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
import { TooltipModule } from 'primeng/tooltip';

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
  APPLICATION_SECURITY_ROLE_VALIDATION,
  APPLICATION_SECURITY_TABLE_ACTIONS,
  APPLICATION_WEB_CONTEXT_PENDING_VERIFICATION,
} from './application-security-section.i18n';

export type ApplicationSecurityTableKind = 'role' | 'web-context' | 'risk' | 'measure';

export enum ApplicationSecurityTableAction {
  View = 1,
  Edit,
  Delete,
  Verify,
}

@Component({
  selector: 'app-application-security-resource-table',
  standalone: true,
  imports: [Button, Menu, TableModule, TooltipModule],
  templateUrl: './application-security-resource-table.html',
  styleUrls: [
    '../../../../../../shared/styles/development-maintenance-table.scss',
    './application-security-resource-table.scss',
  ],
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationSecurityResourceTable extends TableComponentBase<ApplicationSecurityResourceOutput> {
  kind = input.required<ApplicationSecurityTableKind>();
  first = input(0);
  isReadOnly = input(false);
  showActions = input(false);
  showVerify = input(false);
  showVerificationWarnings = input(false);
  showVerifiedReason = input(false);
  verifyDisabled = input(false);

  private readonly locale = inject(LOCALE_ID);
  private readonly selectedRow = signal<ApplicationSecurityResourceOutput | null>(null);
  private readonly rowMenu = viewChild<Menu>('rowMenu');

  protected readonly icons = PrimeIcons;
  protected readonly actions = APPLICATION_SECURITY_TABLE_ACTIONS;
  protected readonly pendingVerificationLabel = APPLICATION_WEB_CONTEXT_PENDING_VERIFICATION;
  protected readonly emptyValue = APPLICATION_SECURITY_EMPTY_VALUE;
  protected readonly ApplicationSecurityTableAction = ApplicationSecurityTableAction;
  protected readonly rowActions = computed<MenuItem[]>(() => [
    {
      label: this.actions.view,
      icon: PrimeIcons.EYE,
      command: () => this.emitRowAction(ApplicationSecurityTableAction.View),
    },
    ...(!this.isReadOnly() && this.roleValidationStatus(this.selectedRow()) === 'NOT_VALIDATED'
      ? [
          {
            label: APPLICATION_SECURITY_ROLE_VALIDATION.validate,
            icon: PrimeIcons.CHECK_CIRCLE,
            command: () => this.emitRowAction(ApplicationSecurityTableAction.Verify),
          },
        ]
      : []),
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
      case 'url':
        return 'url' in row ? row.url ?? '-' : '-';
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

  // Only the states that need attention get an indicator; VALIDATED and NOT_APPLY show nothing.
  protected roleValidationStatus(
    row: ApplicationSecurityResourceOutput | null,
  ): 'NOT_VALIDATED' | 'MANUAL' | null {
    if (this.kind() !== 'role' || !row || row.deletedAt) return null;
    const status = (row as ApplicationSecurityRoleOutput).validationStatus;
    return status === 'NOT_VALIDATED' || status === 'MANUAL' ? status : null;
  }

  protected roleValidationLabel(row: ApplicationSecurityResourceOutput): string {
    const copy = APPLICATION_SECURITY_ROLE_VALIDATION;
    if (this.roleValidationStatus(row) !== 'MANUAL') return copy.notValidated;
    const { manualValidatedAt, manualValidatedBy } = row as ApplicationSecurityRoleOutput;
    return manualValidatedAt && manualValidatedBy?.trim()
      ? copy.manualBy(
          manualValidatedBy.trim(),
          formatDate(manualValidatedAt, 'dd/MM/yyyy HH:mm', this.locale),
        )
      : copy.manual;
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
