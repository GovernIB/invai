import { ChangeDetectionStrategy, Component, input, ViewEncapsulation } from '@angular/core';
import { TableComponentBase } from '@shared/classes/table-component-base';
import { Application } from '../../applications.model';
import { PrimeIcons } from 'primeng/api';
import { Button } from 'primeng/button';
import { TableModule } from 'primeng/table';
import { TooltipModule } from 'primeng/tooltip';
import {
  APPLICATIONS_TABLE_ACTIONS_HEADER,
  APPLICATIONS_TABLE_DELETE_ARIA_LABEL,
  APPLICATIONS_TABLE_DETAIL_ARIA_LABEL,
  APPLICATIONS_TABLE_UPDATE_ARIA_LABEL,
} from './applications-table.i18n';

export enum ApplicationTableAction {
  Delete = 1,
  Detail = 2,
  Update = 3,
}

@Component({
  selector: 'app-applications-table',
  standalone: true,
  imports: [Button, TableModule, TooltipModule],
  templateUrl: './applications-table.html',
  styleUrl: './applications-table.scss',
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationsTable extends TableComponentBase<Application> {
  first = input(0);

  protected readonly PrimeIcons = PrimeIcons;
  protected readonly ApplicationTableAction = ApplicationTableAction;
  protected readonly actionsHeader = APPLICATIONS_TABLE_ACTIONS_HEADER;
  protected readonly deleteAriaLabel = APPLICATIONS_TABLE_DELETE_ARIA_LABEL;
  protected readonly detailAriaLabel = APPLICATIONS_TABLE_DETAIL_ARIA_LABEL;
  protected readonly updateAriaLabel = APPLICATIONS_TABLE_UPDATE_ARIA_LABEL;
}
