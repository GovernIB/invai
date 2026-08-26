import {
  ChangeDetectionStrategy,
  Component,
  computed,
  Directive,
  LOCALE_ID,
  inject,
  input,
  signal,
  viewChild,
  ViewEncapsulation,
} from '@angular/core';
import { KeyLabel } from '@models/table.model';
import { TableComponentBase } from '@shared/classes/table-component-base';
import { localizedName } from '@shared/utils/localized-name.utils';
import { MenuItem, PrimeIcons } from 'primeng/api';
import { Button } from 'primeng/button';
import { Menu } from 'primeng/menu';
import { Table, TableModule } from 'primeng/table';
import { Tooltip } from 'primeng/tooltip';

import { ResponsibleType } from '../../../../../maintenances/responsibles/responsibles.model';
import { responsiblePersonFullName } from '../../../../../maintenances/responsibles/responsibles.utils';
import {
  ApplicationAssignedResponsibleOutput,
  ApplicationAuthorizedOutput,
} from '../../../../applications.model';
import { APPLICATION_RESPONSIBLE_COPY } from './application-responsible-section.i18n';

export interface ApplicationResponsibleTableRow {
  rowKey: string;
  responsibleType: ResponsibleType;
  assignment: ApplicationAssignedResponsibleOutput | null;
}

type Assignment = ApplicationResponsibleTableRow | ApplicationAuthorizedOutput;

export enum ApplicationAssignmentTableAction {
  Add = 1,
  Edit,
  Deactivate,
}

@Directive()
abstract class ApplicationAssignmentTableBase<
  TItem extends Assignment,
> extends TableComponentBase<TItem> {
  first = input(0);
  isEditing = input(false);

  protected readonly copy = APPLICATION_RESPONSIBLE_COPY;
  protected readonly icons = PrimeIcons;
  protected readonly tableDataKey: string = 'id';
  private readonly locale = inject(LOCALE_ID);
  private readonly selectedRow = signal<TItem | null>(null);
  private readonly rowMenu = viewChild<Menu>('rowMenu');
  private readonly table = viewChild.required<Table>('table');
  protected readonly rowActions = computed<MenuItem[]>(() => {
    const row = this.selectedRow();
    if (!row) return [];
    const isVacant = 'assignment' in row && row.assignment === null;
    if (isVacant) return [];
    const primaryAction: MenuItem = {
      label: this.copy.rowEdit,
      icon: PrimeIcons.PENCIL,
      command: () => this.onSelectedAction(ApplicationAssignmentTableAction.Edit, row),
    };
    return [
      primaryAction,
      ...(!isVacant
        ? [
            {
              label: this.copy.rowDeactivate,
              icon: PrimeIcons.TRASH,
              command: () =>
                this.onSelectedAction(ApplicationAssignmentTableAction.Deactivate, row),
            },
          ]
        : []),
    ];
  });

  protected cellValue(row: TItem, key: string): string {
    return applicationAssignmentCellValue(row, key, this.locale, this.copy.caibRole);
  }

  protected isIncompleteResponsiblePersonCell(row: TItem, key: string): boolean {
    return key === 'person' && 'assignment' in row && row.assignment === null;
  }

  protected rowActionLabel(row: TItem): string {
    const label =
      'assignment' in row
        ? row.assignment
          ? responsiblePersonFullName(row.assignment.person)
          : localizedName(row.responsibleType, this.locale)
        : responsiblePersonFullName(row.person);
    return $localize`Accions de ${label}:itemName:`;
  }

  protected rowHasActions(row: TItem): boolean {
    return !('assignment' in row && row.assignment === null);
  }

  protected openActionsMenu(event: Event, row: TItem, menu = this.rowMenu()): void {
    this.selectedRow.set(row);
    menu?.toggle(event);
  }

  resetState(): void {
    this.table().reset();
  }
}

const TABLE_IMPORTS = [Button, Menu, TableModule, Tooltip];

@Component({
  selector: 'app-application-responsibles-table',
  standalone: true,
  imports: TABLE_IMPORTS,
  templateUrl: './application-assignment-tables.html',
  styleUrl: '../../../../../../shared/styles/development-maintenance-table.scss',
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationResponsiblesTable extends ApplicationAssignmentTableBase<ApplicationResponsibleTableRow> {
  protected override readonly tableDataKey = 'rowKey';
}

@Component({
  selector: 'app-application-authorized-table',
  standalone: true,
  imports: TABLE_IMPORTS,
  templateUrl: './application-assignment-tables.html',
  styleUrl: '../../../../../../shared/styles/development-maintenance-table.scss',
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationAuthorizedTable extends ApplicationAssignmentTableBase<ApplicationAuthorizedOutput> {}

export function applicationAssignmentCellValue(
  row: Assignment,
  key: string,
  locale: string,
  caibRole: string,
): string {
  if ('assignment' in row) {
    if (key === 'responsibility') {
      return localizedName(row.responsibleType, locale);
    }
    if (key === 'person')
      return row.assignment
        ? responsiblePersonFullName(row.assignment.person)
        : APPLICATION_RESPONSIBLE_COPY.incomplete;
    if (key === 'roleOrCompany') {
      if (!row.assignment) return '—';
      return row.assignment.person.personalCaib
        ? (row.assignment.jobTitle ?? caibRole)
        : (row.assignment.person.company?.name ?? '—');
    }
    return '';
  }
  if (key === 'person') return responsiblePersonFullName(row.person);
  if (key === 'authorization' && 'authorizationTypes' in row) {
    return row.authorizationTypes.map((item) => localizedName(item, locale)).join(', ');
  }
  return '';
}

export function assignmentRowsToCsv<TItem extends Assignment>(
  columns: KeyLabel[],
  rows: TItem[],
  value: (row: TItem, key: string) => string,
): string {
  return [
    columns.map(({ label }) => label),
    ...rows.map((row) => columns.map(({ key }) => value(row, key))),
  ]
    .map((cells) => cells.map(csvCell).join(','))
    .join('\r\n');
}

function csvCell(value: string): string {
  return /[",\r\n]/.test(value) ? `"${value.replaceAll('"', '""')}"` : value;
}
