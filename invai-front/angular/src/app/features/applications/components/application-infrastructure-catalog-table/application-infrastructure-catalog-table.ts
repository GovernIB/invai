import {
  ChangeDetectionStrategy,
  Component,
  input,
  output,
  ViewEncapsulation,
} from '@angular/core';
import { KeyLabel, PaginatedList } from '@models/table.model';
import {
  CURRENT_PAGE_REPORT_TEMPLATE,
  PAGINATOR_ROWS,
  PAGINATOR_STYLE_CLASS,
  RESULTS_NOT_FOUND,
  ROWS_PER_PAGE_OPTIONS,
} from '@shared/constants/table.constants';
import { TableLazyLoadEvent, TableModule } from 'primeng/table';

import { ApplicationInfrastructureCatalogRow } from '../../applications.model';
import { APPLICATION_INFRASTRUCTURE_CATALOG_SELECT_ARIA_LABEL } from './application-infrastructure-catalog-table.i18n';

@Component({
  selector: 'app-application-infrastructure-catalog-table',
  standalone: true,
  imports: [TableModule],
  templateUrl: './application-infrastructure-catalog-table.html',
  styleUrls: [
    './application-infrastructure-catalog-table.scss',
    '../../../../shared/styles/development-maintenance-table.scss',
  ],
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationInfrastructureCatalogTable {
  itemsList = input.required<PaginatedList<ApplicationInfrastructureCatalogRow>>();
  columns = input.required<KeyLabel[]>();
  selection = input<ApplicationInfrastructureCatalogRow | null>(null);
  first = input(0);
  isLoading = input(false);
  isReadOnly = input(false);

  selectionChange = output<ApplicationInfrastructureCatalogRow>();
  pageChange = output<TableLazyLoadEvent>();

  protected readonly selectAriaLabel =
    APPLICATION_INFRASTRUCTURE_CATALOG_SELECT_ARIA_LABEL;
  protected readonly resultsNotFound = RESULTS_NOT_FOUND;
  protected readonly paginatorRows = PAGINATOR_ROWS;
  protected readonly rowsPerPageOptions = ROWS_PER_PAGE_OPTIONS;
  protected readonly currentPageReportTemplate = CURRENT_PAGE_REPORT_TEMPLATE;
  protected readonly paginatorStyleClass = PAGINATOR_STYLE_CLASS;

  protected select(row: ApplicationInfrastructureCatalogRow): void {
    if (!this.isReadOnly()) this.selectionChange.emit(row);
  }
}
