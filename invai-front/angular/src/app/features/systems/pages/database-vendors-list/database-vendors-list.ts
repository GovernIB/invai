import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  OnInit,
  computed,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder } from '@angular/forms';
import { ConfirmationDialogComponent } from '@components/confirmation-dialog/confirmation-dialog.component';
import { SearchFiltersComponent } from '@components/search-filters/search-filters.component';
import { SectionActionsComponent } from '@components/section-actions/section-actions.component';
import { isStructuredBadRequest } from '@core/models/api-error.model';
import { ActionParams, KeyLabel, PaginatedList } from '@models/table.model';
import {
  fnCountSelectedFilters,
  fnGetVisibleColumns,
  fnSetColumnVisibility,
} from '@shared/utils/table.utils';
import { MessageService } from 'primeng/api';
import { TableLazyLoadEvent } from 'primeng/table';
import {
  Subject,
  debounceTime,
  distinctUntilChanged,
  finalize,
  map,
} from 'rxjs';

import { DatabaseVendorDialog } from '../../components/database-vendor-dialog/database-vendor-dialog';
import { DatabaseVendorFiltersForm } from '../../components/database-vendor-filters-form/database-vendor-filters-form';
import {
  InfrastructureTable,
  InfrastructureTableAction,
} from '../../components/infrastructure-table/infrastructure-table';
import { createDatabaseVendorFiltersForm } from '../../forms/database-vendor-filters-form.factory';
import { createDatabaseVendorForm } from '../../forms/database-vendor-form.factory';
import { DatabaseVendorsService } from '../../services/database-vendors.service';
import {
  DATABASE_VENDORS_TABLE_COLUMNS,
  INFRASTRUCTURE_STATUS_LABELS,
} from '../../systems.constants';
import {
  DatabaseVendor,
  DatabaseVendorInput,
  DatabaseVendorPageParams,
  DatabaseVendorTableRow,
  InfrastructureDialogMode,
  InfrastructureTableRow,
} from '../../systems.model';
import {
  DATABASE_VENDORS_ADD_ARIA_LABEL,
  DATABASE_VENDORS_ARIA_LABEL,
  DATABASE_VENDORS_CREATE_SUCCESS_DETAIL,
  DATABASE_VENDORS_CREATE_SUCCESS_TITLE,
  DATABASE_VENDORS_DELETE_CANCEL,
  DATABASE_VENDORS_DELETE_CONFIRM,
  DATABASE_VENDORS_DELETE_ERROR_DETAIL,
  DATABASE_VENDORS_DELETE_MESSAGE,
  DATABASE_VENDORS_DELETE_SUCCESS_DETAIL,
  DATABASE_VENDORS_DELETE_SUCCESS_TITLE,
  DATABASE_VENDORS_DELETE_TITLE,
  DATABASE_VENDORS_LOAD_ERROR_DETAIL,
  DATABASE_VENDORS_LOAD_ERROR_SUMMARY,
  DATABASE_VENDORS_QUICK_SEARCH_ARIA_LABEL,
  DATABASE_VENDORS_QUICK_SEARCH_PLACEHOLDER,
  DATABASE_VENDORS_RESTORE_ERROR_DETAIL,
  DATABASE_VENDORS_RESTORE_SUCCESS_DETAIL,
  DATABASE_VENDORS_RESTORE_SUCCESS_TITLE,
  DATABASE_VENDORS_SAVE_ERROR_DETAIL,
  DATABASE_VENDORS_UPDATE_SUCCESS_DETAIL,
  DATABASE_VENDORS_UPDATE_SUCCESS_TITLE,
} from './database-vendors-list.i18n';

const DEFAULT_PAGE_SIZE = 10;
const QUICK_SEARCH_DEBOUNCE_MS = 400;

@Component({
  standalone: true,
  selector: 'app-database-vendors-list',
  imports: [
    ConfirmationDialogComponent,
    DatabaseVendorDialog,
    DatabaseVendorFiltersForm,
    InfrastructureTable,
    SearchFiltersComponent,
    SectionActionsComponent,
  ],
  templateUrl: './database-vendors-list.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DatabaseVendorsList implements OnInit {
  private readonly formBuilder = inject(FormBuilder);
  private readonly destroyRef = inject(DestroyRef);
  private readonly messageService = inject(MessageService);
  private readonly vendorsService = inject(DatabaseVendorsService);

  protected readonly filtersForm = createDatabaseVendorFiltersForm(this.formBuilder);
  protected readonly entityForm = createDatabaseVendorForm(this.formBuilder);
  protected readonly itemsList = signal<PaginatedList<DatabaseVendorTableRow>>({
    items: [],
    total: 0,
  });
  protected readonly isFiltersCollapsed = signal(true);
  protected readonly selectedFilters = signal(1);
  protected readonly tableFirst = signal(0);
  protected readonly isLoading = signal(false);
  protected readonly isInitialLoading = signal(true);
  protected readonly isDialogVisible = signal(false);
  protected readonly dialogMode = signal<InfrastructureDialogMode>('create');
  protected readonly canRestore = signal(false);
  protected readonly isEntityLoading = signal(false);
  protected readonly isSaving = signal(false);
  protected readonly isDeleteDialogVisible = signal(false);
  protected readonly isDeleting = signal(false);
  protected readonly pendingDelete = signal<DatabaseVendor | null>(null);
  protected readonly columns = DATABASE_VENDORS_TABLE_COLUMNS;
  protected readonly selectedColumns = signal<KeyLabel[]>([]);
  protected readonly displayedColumns = computed(() =>
    fnGetVisibleColumns(this.columns, this.selectedColumns()),
  );
  protected readonly deleteMessage = computed(() =>
    DATABASE_VENDORS_DELETE_MESSAGE(this.pendingDelete()?.name ?? ''),
  );
  protected readonly ariaLabel = DATABASE_VENDORS_ARIA_LABEL;
  protected readonly addAriaLabel = DATABASE_VENDORS_ADD_ARIA_LABEL;
  protected readonly quickSearchPlaceholder =
    DATABASE_VENDORS_QUICK_SEARCH_PLACEHOLDER;
  protected readonly quickSearchAriaLabel =
    DATABASE_VENDORS_QUICK_SEARCH_ARIA_LABEL;
  protected readonly deleteTitle = DATABASE_VENDORS_DELETE_TITLE;
  protected readonly deleteCancel = DATABASE_VENDORS_DELETE_CANCEL;
  protected readonly deleteConfirm = DATABASE_VENDORS_DELETE_CONFIRM;

  quickSearchTerm = '';
  private selectedVendor: DatabaseVendor | null = null;
  private tableState: TableLazyLoadEvent = {
    first: 0,
    rows: DEFAULT_PAGE_SIZE,
  };
  private appliedFilters = this.filtersForm.getRawValue();
  private appliedStatusFilter = this.appliedFilters.status;
  private readonly quickSearchChanges = new Subject<string>();

  ngOnInit(): void {
    this.selectedColumns.set([...this.columns]);
    this.synchronizeStatusColumnSelection(true);
    this.observeQuickSearch();
    this.refresh();
  }

  protected onQuickSearchChange(value: string): void {
    this.quickSearchTerm = value;
    this.quickSearchChanges.next(value);
  }

  protected onFilterSearch(): void {
    this.appliedFilters = this.filtersForm.getRawValue();
    this.synchronizeStatusColumnSelection();
    this.selectedFilters.set(fnCountSelectedFilters(this.filtersForm));
    this.resetPage();
    this.refresh();
  }

  protected onPageChange(event: TableLazyLoadEvent): void {
    this.tableState = event;
    this.tableFirst.set(event.first ?? 0);
    this.refresh();
  }

  protected reset(): void {
    this.filtersForm.reset();
    this.appliedFilters = this.filtersForm.getRawValue();
    this.synchronizeStatusColumnSelection(true);
    this.quickSearchTerm = '';
    this.selectedFilters.set(1);
    this.resetPage();
    this.refresh();
  }

  protected openCreateDialog(): void {
    if (this.hasMutationInProgress()) return;
    this.selectedVendor = null;
    this.canRestore.set(false);
    this.dialogMode.set('create');
    this.entityForm.enable({ emitEvent: false });
    this.entityForm.reset();
    this.isDialogVisible.set(true);
  }

  protected closeDialog(): void {
    if (this.isEntityLoading() || this.isSaving()) return;
    this.isDialogVisible.set(false);
    this.selectedVendor = null;
    this.canRestore.set(false);
  }

  protected startEntityEdit(): void {
    if (
      this.dialogMode() !== 'view' ||
      this.canRestore() ||
      !this.selectedVendor ||
      this.hasMutationInProgress()
    ) {
      return;
    }
    this.entityForm.enable({ emitEvent: false });
    this.dialogMode.set('edit');
  }

  protected cancelEntityEdit(): void {
    if (
      this.dialogMode() !== 'edit' ||
      !this.selectedVendor ||
      this.isSaving()
    ) {
      return;
    }
    this.prepareRecordDialog(this.selectedVendor, 'view');
  }

  protected deactivateSelected(): void {
    if (!this.selectedVendor || this.hasMutationInProgress()) return;
    this.openDeleteDialog(this.selectedVendor);
  }

  protected submitEntity(): void {
    if (this.isSaving() || this.isEntityLoading()) return;
    if (this.entityForm.invalid) {
      this.entityForm.markAllAsTouched();
      return;
    }
    const value = this.entityForm.getRawValue();
    const payload: DatabaseVendorInput = {
      name: value.name.trim(),
      defaultPort: value.defaultPort!,
    };
    const mode = this.dialogMode();
    const request =
      mode === 'create'
        ? this.vendorsService.create(payload)
        : this.vendorsService.update(this.selectedVendor!.id, payload);

    this.isSaving.set(true);
    request
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.isSaving.set(false)),
      )
      .subscribe({
        next: () => {
          this.showSuccess(
            mode === 'create'
              ? DATABASE_VENDORS_CREATE_SUCCESS_TITLE
              : DATABASE_VENDORS_UPDATE_SUCCESS_TITLE,
            mode === 'create'
              ? DATABASE_VENDORS_CREATE_SUCCESS_DETAIL
              : DATABASE_VENDORS_UPDATE_SUCCESS_DETAIL,
          );
          this.isDialogVisible.set(false);
          this.selectedVendor = null;
          this.resetAfterMutation();
        },
        error: (error: unknown) =>
          this.handleError(error, DATABASE_VENDORS_SAVE_ERROR_DETAIL),
      });
  }

  protected onTableAction(event: ActionParams<InfrastructureTableRow>): void {
    if (this.hasMutationInProgress()) return;
    const row = event.params;
    switch (event.action) {
      case InfrastructureTableAction.View:
        this.openRecordDialog(row.id, 'view');
        break;
      case InfrastructureTableAction.Edit:
        this.openRecordDialog(row.id, 'edit');
        break;
      case InfrastructureTableAction.Delete:
        this.openDeleteDialog({
          id: row.id,
          name: String(row['name'] ?? ''),
          defaultPort: Number(row['defaultPort'] ?? 0),
          deletedAt: row.deletedAt,
        });
        break;
      case InfrastructureTableAction.Restore:
        this.restore(row.id);
        break;
    }
  }

  protected restore(id = this.selectedVendor?.id): void {
    if (id == null || this.hasMutationInProgress()) return;
    this.isSaving.set(true);
    this.vendorsService
      .reactivate(id)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.isSaving.set(false)),
      )
      .subscribe({
        next: () => {
          this.isDialogVisible.set(false);
          this.selectedVendor = null;
          this.showSuccess(
            DATABASE_VENDORS_RESTORE_SUCCESS_TITLE,
            DATABASE_VENDORS_RESTORE_SUCCESS_DETAIL,
          );
          this.resetAfterMutation();
        },
        error: (error: unknown) =>
          this.handleError(error, DATABASE_VENDORS_RESTORE_ERROR_DETAIL),
      });
  }

  protected closeDeleteDialog(): void {
    this.isDeleteDialogVisible.set(false);
    if (!this.isDeleting()) this.pendingDelete.set(null);
  }

  protected confirmDelete(): void {
    const record = this.pendingDelete();
    if (!record || this.isDeleting()) return;
    this.isDeleteDialogVisible.set(false);
    this.isDeleting.set(true);
    this.vendorsService
      .delete(record.id)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.isDeleting.set(false)),
      )
      .subscribe({
        next: () => {
          this.pendingDelete.set(null);
          if (this.selectedVendor?.id === record.id) this.isDialogVisible.set(false);
          this.selectedVendor = null;
          this.showSuccess(
            DATABASE_VENDORS_DELETE_SUCCESS_TITLE,
            DATABASE_VENDORS_DELETE_SUCCESS_DETAIL,
          );
          this.resetAfterMutation();
        },
        error: (error: unknown) => {
          this.pendingDelete.set(null);
          this.handleError(error, DATABASE_VENDORS_DELETE_ERROR_DETAIL);
        },
      });
  }

  private observeQuickSearch(): void {
    this.quickSearchChanges
      .pipe(
        map((value) => value.trim()),
        debounceTime(QUICK_SEARCH_DEBOUNCE_MS),
        distinctUntilChanged(),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(() => {
        this.resetPage();
        this.refresh();
      });
  }

  private openRecordDialog(
    id: number,
    mode: Exclude<InfrastructureDialogMode, 'create'>,
  ): void {
    this.selectedVendor = null;
    this.canRestore.set(false);
    this.entityForm.enable({ emitEvent: false });
    this.entityForm.reset();
    this.entityForm.disable({ emitEvent: false });
    this.dialogMode.set(mode);
    this.isDialogVisible.set(true);
    this.isEntityLoading.set(true);
    this.vendorsService
      .getById(id)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.isEntityLoading.set(false)),
      )
      .subscribe({
        next: (record) => {
          this.selectedVendor = record;
          this.prepareRecordDialog(record, mode);
        },
        error: (error: unknown) => {
          this.isDialogVisible.set(false);
          this.handleError(error, DATABASE_VENDORS_LOAD_ERROR_DETAIL);
        },
      });
  }

  private prepareRecordDialog(
    record: DatabaseVendor,
    mode: Exclude<InfrastructureDialogMode, 'create'>,
  ): void {
    this.canRestore.set(mode === 'view' && Boolean(record.deletedAt));
    this.entityForm.enable({ emitEvent: false });
    this.entityForm.reset({
      name: record.name ?? '',
      defaultPort: record.defaultPort,
    });
    if (mode === 'view') this.entityForm.disable({ emitEvent: false });
    this.dialogMode.set(mode);
  }

  private openDeleteDialog(record: DatabaseVendor): void {
    this.pendingDelete.set(record);
    this.isDeleteDialogVisible.set(true);
  }

  private refresh(): void {
    this.isLoading.set(true);
    this.vendorsService
      .getAll(this.toPageParams())
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => {
          this.isLoading.set(false);
          this.isInitialLoading.set(false);
        }),
      )
      .subscribe({
        next: (page) =>
          this.itemsList.set({
            items: page.content.map((record) => this.toTableRow(record)),
            total: page.totalElements,
          }),
        error: () => {
          this.itemsList.set({ items: [], total: 0 });
          this.showError(DATABASE_VENDORS_LOAD_ERROR_DETAIL);
        },
      });
  }

  private toPageParams(): DatabaseVendorPageParams {
    const first = this.tableState.first ?? 0;
    const size = this.tableState.rows ?? DEFAULT_PAGE_SIZE;
    const sortField = Array.isArray(this.tableState.sortField)
      ? this.tableState.sortField[0]
      : this.tableState.sortField;
    const direction =
      this.tableState.sortOrder === -1
        ? 'desc'
        : this.tableState.sortOrder === 1
          ? 'asc'
          : null;
    return {
      page: Math.floor(first / size),
      size,
      sort: sortField && direction ? `${sortField},${direction}` : undefined,
      name: this.appliedFilters.name?.trim() || undefined,
      defaultPort: this.appliedFilters.defaultPort ?? undefined,
      statusId: this.appliedFilters.status ?? undefined,
      search: this.quickSearchTerm.trim() || undefined,
    };
  }

  private toTableRow(record: DatabaseVendor): DatabaseVendorTableRow {
    return {
      id: record.id,
      deletedAt: record.deletedAt,
      name: record.name ?? '',
      defaultPort: record.defaultPort,
      status: INFRASTRUCTURE_STATUS_LABELS[record.deletedAt ? 2 : 1],
    };
  }

  private synchronizeStatusColumnSelection(force = false): void {
    const status = this.appliedFilters.status;
    if (!force && Object.is(status, this.appliedStatusFilter)) return;
    this.selectedColumns.set(
      fnSetColumnVisibility(
        this.columns,
        this.selectedColumns(),
        'status',
        status == null,
      ),
    );
    this.appliedStatusFilter = status;
  }

  private resetAfterMutation(): void {
    this.filtersForm.reset();
    this.appliedFilters = this.filtersForm.getRawValue();
    this.synchronizeStatusColumnSelection(true);
    this.quickSearchTerm = '';
    this.selectedFilters.set(1);
    this.isFiltersCollapsed.set(true);
    this.resetPage();
    this.refresh();
  }

  private resetPage(): void {
    this.tableFirst.set(0);
    this.tableState = { ...this.tableState, first: 0 };
  }

  private hasMutationInProgress(): boolean {
    return this.isSaving() || this.isDeleting() || this.isEntityLoading();
  }

  private handleError(error: unknown, detail: string): void {
    if (isStructuredBadRequest(error)) return;
    this.showError(detail);
  }

  private showError(detail: string): void {
    this.messageService.add({
      severity: 'error',
      summary: DATABASE_VENDORS_LOAD_ERROR_SUMMARY,
      detail,
    });
  }

  private showSuccess(summary: string, detail: string): void {
    this.messageService.add({ severity: 'success', summary, detail });
  }
}
