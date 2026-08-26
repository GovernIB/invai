import {
  ChangeDetectionStrategy,
  Component,
  Directive,
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

import {
  ResponsibleAuthorization,
  ResponsibleCompany,
  ResponsibleMaintenanceEntity,
  ResponsiblePerson,
} from '../../responsibles.model';
import { RESPONSIBLE_TABLE_COPY } from '../../responsibles.i18n';

export enum ResponsibleMaintenanceTableAction {
  View = 1,
  Edit,
  Deactivate,
  Restore,
}

@Directive()
abstract class ResponsibleMaintenanceTableBase<
  TItem extends ResponsibleMaintenanceEntity,
> extends TableComponentBase<TItem> {
  first = input(0);
  isInitialLoading = input(false);

  private readonly selectedRow = signal<TItem | null>(null);
  private readonly table = viewChild.required<Table>('table');

  protected readonly icons = PrimeIcons;
  protected readonly TableAction = ResponsibleMaintenanceTableAction;
  protected readonly actionsAriaLabel = RESPONSIBLE_TABLE_COPY.actions;
  protected readonly statusLabel = softDeleteStatusLabel;
  protected readonly skeletonRows = Array.from({ length: this.PAGINATOR_ROWS });
  protected readonly isRefreshing = computed(() => this.isLoading() && !this.isInitialLoading());
  protected readonly rowActions = computed<MenuItem[]>(() => [
    {
      label: RESPONSIBLE_TABLE_COPY.view,
      icon: PrimeIcons.EYE,
      command: () => this.emitRowAction(ResponsibleMaintenanceTableAction.View),
    },
    ...(!this.isImmutable(this.selectedRow())
      ? [
          {
            label: RESPONSIBLE_TABLE_COPY.edit,
            icon: PrimeIcons.PENCIL,
            command: () => this.emitRowAction(ResponsibleMaintenanceTableAction.Edit),
          },
          {
            label: RESPONSIBLE_TABLE_COPY.deactivate,
            icon: PrimeIcons.TRASH,
            command: () => this.emitRowAction(ResponsibleMaintenanceTableAction.Deactivate),
          },
        ]
      : []),
  ]);

  protected openActionsMenu(event: Event, row: TItem, menu: Menu): void {
    this.selectedRow.set(row);
    menu.toggle(event);
  }

  protected cellValue(row: TItem, key: string): string {
    if (key === 'company' && 'personalCaib' in row) {
      const person = row as unknown as ResponsiblePerson;
      return person.personalCaib ? RESPONSIBLE_TABLE_COPY.caib : (person.company?.name ?? '');
    }
    return String((row as unknown as Record<string, unknown>)[key] ?? '');
  }

  protected isImmutable(row: TItem | null): boolean {
    return Boolean(
      row && 'personalCaib' in row && (row as unknown as ResponsiblePerson).personalCaib,
    );
  }

  resetState(): void {
    this.table().reset();
  }

  private emitRowAction(action: ResponsibleMaintenanceTableAction): void {
    const row = this.selectedRow();
    if (row) this.onSelectedAction(action, row);
  }
}

const TABLE_IMPORTS = [Button, Menu, RestoreRecordMenu, Skeleton, TableModule];

@Component({
  selector: 'app-responsible-companies-table',
  standalone: true,
  imports: TABLE_IMPORTS,
  host: { class: 'responsible-maintenance-table' },
  templateUrl: './responsible-maintenance-tables.html',
  styleUrl: './responsible-maintenance-tables.scss',
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ResponsibleCompaniesTable extends ResponsibleMaintenanceTableBase<ResponsibleCompany> {}

@Component({
  selector: 'app-responsible-people-table',
  standalone: true,
  imports: TABLE_IMPORTS,
  host: { class: 'responsible-maintenance-table' },
  templateUrl: './responsible-maintenance-tables.html',
  styleUrl: './responsible-maintenance-tables.scss',
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ResponsiblePeopleTable extends ResponsibleMaintenanceTableBase<ResponsiblePerson> {}

@Component({
  selector: 'app-responsible-authorizations-table',
  standalone: true,
  imports: TABLE_IMPORTS,
  host: { class: 'responsible-maintenance-table' },
  templateUrl: './responsible-maintenance-tables.html',
  styleUrl: './responsible-maintenance-tables.scss',
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ResponsibleAuthorizationsTable extends ResponsibleMaintenanceTableBase<ResponsibleAuthorization> {}
