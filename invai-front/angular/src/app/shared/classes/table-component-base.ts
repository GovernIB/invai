import { computed, Directive, input, output } from '@angular/core';
import { ActionParams, KeyLabel, PaginatedList } from '@models/table.model';
import {
  ACTIONS,
  CURRENT_PAGE_REPORT_TEMPLATE,
  PAGINATOR_ROWS,
  PAGINATOR_STYLE_CLASS,
  RESULTS_NOT_FOUND,
  ROWS_PER_PAGE_OPTIONS,
} from '@shared/constants/table.constants';
import { TableLazyLoadEvent } from 'primeng/table';

@Directive({ standalone: true })
export abstract class TableComponentBase<TItem> {
  itemsList = input.required<PaginatedList<TItem>>();
  columns = input.required<Partial<KeyLabel>[]>();
  isLoading = input<boolean>(false);

  onPageChange = output<TableLazyLoadEvent>();
  onSelectAction = output<ActionParams<TItem>>();

  protected readonly ACTIONS = ACTIONS;
  protected readonly RESULTS_NOT_FOUND = RESULTS_NOT_FOUND;
  protected readonly PAGINATOR_ROWS = PAGINATOR_ROWS;
  protected readonly ROWS_PER_PAGE_OPTIONS = ROWS_PER_PAGE_OPTIONS;
  protected readonly CURRENT_PAGE_REPORT_TEMPLATE = CURRENT_PAGE_REPORT_TEMPLATE;
  protected readonly PAGINATOR_STYLE_CLASS = PAGINATOR_STYLE_CLASS;

  protected totalRecords = computed(() => this.itemsList().total);
  protected value = computed(() => this.itemsList().items);

  protected onPage($event: TableLazyLoadEvent) {
    this.onPageChange.emit($event);
  }

  protected onSelectedAction(action: number, params: TItem) {
    this.onSelectAction.emit({ action, params });
  }
}
