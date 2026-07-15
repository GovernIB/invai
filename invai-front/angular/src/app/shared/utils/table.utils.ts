import { FormGroup } from '@angular/forms';
import { TableLazyLoadEvent } from 'primeng/table';
import { KeyLabel, PaginatedBodyInfo } from '@models/table.model';

const DEFAULT_FIRST_RESULT = 0;
const DEFAULT_MAX_RESULT = 10;

export function fnGetVisibleColumns(
  allColumns: KeyLabel[],
  selectedColumns: Partial<KeyLabel>[],
): KeyLabel[] {
  const selectedKeys = new Set(selectedColumns.map((column) => column.key));
  return allColumns.filter((column) => selectedKeys.has(column.key));
}

export function fnCountSelectedFilters(form: FormGroup): number {
  return Object.values(form.controls).filter((control) => {
    const value = control.value;
    if (typeof value === 'string') return value.trim().length > 0;
    return value !== null && value !== undefined && value !== false && value !== '';
  }).length;
}

export function fnGetPaginatedBody(event?: TableLazyLoadEvent): PaginatedBodyInfo {
  return {
    firstResult: event?.first ?? DEFAULT_FIRST_RESULT,
    maxResult: event?.rows ?? DEFAULT_MAX_RESULT,
    orderBy: Array.isArray(event?.sortField) ? event?.sortField : event?.sortField ?? null,
    sortDirection: event?.sortOrder === 1 ? 'asc' : event?.sortOrder === -1 ? 'desc' : null,
  };
}

export function fnTrimObjectStrings<T extends Record<string, unknown>>(value: T): T {
  return Object.fromEntries(
    Object.entries(value).map(([key, item]) => [
      key,
      typeof item === 'string' ? item.trim() : item,
    ]),
  ) as T;
}
