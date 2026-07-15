import { computed, Directive, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { MessageService } from 'primeng/api';
import { TableLazyLoadEvent } from 'primeng/table';
import { EMPTY_LIST } from '@shared/constants/table.constants';
import { KeyLabel, PaginatedBodyInfo, PaginatedList } from '@models/table.model';
import {
  fnCountSelectedFilters,
  fnGetPaginatedBody,
  fnGetVisibleColumns,
  fnTrimObjectStrings,
} from '@shared/utils/table.utils';

@Directive({ standalone: true })
export abstract class SearchComponentBase<TItem, TFilters extends object> implements OnInit {
  protected abstract readonly ALL_TABLE_COLUMNS: KeyLabel[];
  protected readonly DEFAULT_TABLE_COLUMNS: KeyLabel[] | null = null;
  protected readonly REQUIRED_TABLE_COLUMN_KEYS: string[] = [];

  protected readonly fb = inject(FormBuilder);
  protected readonly messageService = inject(MessageService);

  protected abstract filtersForm: FormGroup;

  selectedColumns = signal<KeyLabel[]>([]);
  isLoading = signal(false);
  isSubmitted = signal(true);
  isFiltersCollapsed = signal(true);
  selectedFilters = signal(0);
  itemsList = signal<PaginatedList<TItem>>(EMPTY_LIST as PaginatedList<TItem>);
  filterExport: TFilters | null = null;
  paginatedBodyInfo: PaginatedBodyInfo | null = null;

  selectableColumns = computed(() =>
    this.ALL_TABLE_COLUMNS.filter((column) => !this.REQUIRED_TABLE_COLUMN_KEYS.includes(column.key)),
  );

  sortedSelectedColumns = computed(() =>
    fnGetVisibleColumns(this.ALL_TABLE_COLUMNS, [
      ...this._requiredTableColumns(),
      ...this.selectedColumns(),
    ]),
  );

  ngOnInit() {
    const initialColumns = this.DEFAULT_TABLE_COLUMNS ?? this.ALL_TABLE_COLUMNS;
    this.selectedColumns.set(
      initialColumns.filter((column) => !this.REQUIRED_TABLE_COLUMN_KEYS.includes(column.key)),
    );
    this.onSearch();
  }

  onSearch($event?: TableLazyLoadEvent) {
    const filters = this.parseFormToFilters();
    this.filterExport = filters;
    this.paginatedBodyInfo = fnGetPaginatedBody($event);
    this.fetchFilteredList(filters, $event);
    this.selectedFilters.set(fnCountSelectedFilters(this.filtersForm));
    this.isSubmitted.set(true);
  }

  reset(): void {
    this.filtersForm.reset();
    this.selectedFilters.set(0);
    this.onSearch();
  }

  protected onExport(): void {
    if (this.filterExport && this.itemsList().total > 0) {
      this.exportExcel();
      return;
    }

    this.messageService.add({
      severity: 'info',
      summary: $localize`Informació`,
      detail: $localize`Has de realitzar la cerca abans d'exportar.`,
    });
  }

  protected abstract fetchFilteredList(filters: TFilters, $event?: TableLazyLoadEvent): void;

  protected parseFormToFilters(): TFilters {
    return fnTrimObjectStrings(this.filtersForm.getRawValue() as Record<string, unknown>) as TFilters;
  }

  protected exportExcel(): void {}

  private _requiredTableColumns(): KeyLabel[] {
    return this.ALL_TABLE_COLUMNS.filter((column) =>
      this.REQUIRED_TABLE_COLUMN_KEYS.includes(column.key),
    );
  }
}
