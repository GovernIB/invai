import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  LOCALE_ID,
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
import { localizedName } from '@shared/utils/localized-name.utils';
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

import { DatabaseFiltersForm } from '../../components/database-filters-form/database-filters-form';
import { DatabaseMaintenanceDialog } from '../../components/database-maintenance-dialog/database-maintenance-dialog';
import {
  InfrastructureTable,
  InfrastructureTableAction,
} from '../../components/infrastructure-table/infrastructure-table';
import { createDatabaseFiltersForm } from '../../forms/database-filters-form.factory';
import { createDatabaseHostForm } from '../../forms/infrastructure-maintenance-forms.factory';
import { DatabaseVendorCatalogService } from '../../services/database-vendor-catalog.service';
import { DatabasesService } from '../../services/databases.service';
import { ServerCatalogService } from '../../services/server-catalog.service';
import {
  DATABASES_TABLE_COLUMNS,
  INFRASTRUCTURE_STATUS_LABELS,
} from '../../systems.constants';
import {
  DatabaseInput,
  DatabasePageParams,
  DatabaseRecord,
  DatabaseTableRow,
  DatabaseVendorCatalogOption,
  InfrastructureDialogMode,
  InfrastructureTableRow,
  ServerCatalogOption,
} from '../../systems.model';
import {
  DATABASES_ADD_ARIA_LABEL,
  DATABASES_CATALOG_LOAD_ERROR_DETAIL,
  DATABASES_CREATE_SUCCESS_DETAIL,
  DATABASES_CREATE_SUCCESS_TITLE,
  DATABASES_DELETE_CANCEL,
  DATABASES_DELETE_CONFIRM,
  DATABASES_DELETE_ERROR_DETAIL,
  DATABASES_DELETE_MESSAGE,
  DATABASES_DELETE_SUCCESS_DETAIL,
  DATABASES_DELETE_SUCCESS_TITLE,
  DATABASES_DELETE_TITLE,
  DATABASES_FILTER_SERVER,
  DATABASES_FILTER_SERVICE,
  DATABASES_FILTER_STATUS,
  DATABASES_FILTER_TYPE,
  DATABASES_HOSTS_ARIA_LABEL,
  DATABASES_LOAD_ERROR_DETAIL,
  DATABASES_LOAD_ERROR_SUMMARY,
  DATABASES_QUICK_SEARCH_ARIA_LABEL,
  DATABASES_QUICK_SEARCH_PLACEHOLDER,
  DATABASES_RESTORE_ERROR_DETAIL,
  DATABASES_RESTORE_SUCCESS_DETAIL,
  DATABASES_RESTORE_SUCCESS_TITLE,
  DATABASES_SAVE_ERROR_DETAIL,
  DATABASES_UPDATE_SUCCESS_DETAIL,
  DATABASES_UPDATE_SUCCESS_TITLE,
} from './databases-list.i18n';

const DEFAULT_PAGE_SIZE = 10;
const QUICK_SEARCH_DEBOUNCE_MS = 400;

@Component({
  standalone: true,
  selector: 'app-databases-list',
  imports: [
    ConfirmationDialogComponent,
    DatabaseFiltersForm,
    DatabaseMaintenanceDialog,
    InfrastructureTable,
    SearchFiltersComponent,
    SectionActionsComponent,
  ],
  templateUrl: './databases-list.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DatabasesList implements OnInit {
  private readonly formBuilder = inject(FormBuilder);
  private readonly destroyRef = inject(DestroyRef);
  private readonly locale = inject(LOCALE_ID);
  private readonly messageService = inject(MessageService);
  private readonly databasesService = inject(DatabasesService);
  private readonly serverCatalogService = inject(ServerCatalogService);
  private readonly vendorCatalogService = inject(DatabaseVendorCatalogService);

  protected readonly filtersForm = createDatabaseFiltersForm(this.formBuilder);
  protected readonly hostForm = createDatabaseHostForm(this.formBuilder);
  protected readonly itemsList = signal<PaginatedList<DatabaseTableRow>>({
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
  protected readonly selectedHostCanRestore = signal(false);
  protected readonly serverOptions = signal<ServerCatalogOption[]>([]);
  protected readonly databaseTypeOptions = signal<DatabaseVendorCatalogOption[]>([]);
  protected readonly isEntityLoading = signal(false);
  protected readonly isSaving = signal(false);
  protected readonly isDeleteDialogVisible = signal(false);
  protected readonly isDeleting = signal(false);
  protected readonly pendingDelete = signal<{ id: number } | null>(null);
  protected readonly columns = DATABASES_TABLE_COLUMNS;
  protected readonly selectedColumns = signal<KeyLabel[]>([]);
  protected readonly displayedColumns = computed(() =>
    fnGetVisibleColumns(this.columns, this.selectedColumns()),
  );
  protected readonly deleteMessage = computed(() =>
    DATABASES_DELETE_MESSAGE(this.pendingDelete()?.id ?? 0),
  );

  protected readonly filterLabels = {
    server: DATABASES_FILTER_SERVER,
    service: DATABASES_FILTER_SERVICE,
    databaseType: DATABASES_FILTER_TYPE,
    status: DATABASES_FILTER_STATUS,
  };
  protected readonly hostsAriaLabel = DATABASES_HOSTS_ARIA_LABEL;
  protected readonly quickSearchPlaceholder = DATABASES_QUICK_SEARCH_PLACEHOLDER;
  protected readonly quickSearchAriaLabel = DATABASES_QUICK_SEARCH_ARIA_LABEL;
  protected readonly addAriaLabel = DATABASES_ADD_ARIA_LABEL;
  protected readonly deleteTitle = DATABASES_DELETE_TITLE;
  protected readonly deleteCancel = DATABASES_DELETE_CANCEL;
  protected readonly deleteConfirm = DATABASES_DELETE_CONFIRM;

  quickSearchTerm = '';
  private selectedDatabase: DatabaseRecord | null = null;
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
    this.loadCatalogs();
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

    this.selectedDatabase = null;
    this.selectedHostCanRestore.set(false);
    this.dialogMode.set('create');
    this.hostForm.enable({ emitEvent: false });
    this.hostForm.reset();
    this.isDialogVisible.set(true);
  }

  protected closeDialog(): void {
    if (this.isEntityLoading() || this.isSaving()) return;
    this.isDialogVisible.set(false);
    this.selectedDatabase = null;
    this.selectedHostCanRestore.set(false);
  }

  protected startEntityEdit(): void {
    if (
      this.dialogMode() !== 'view' ||
      this.selectedHostCanRestore() ||
      !this.selectedDatabase ||
      this.hasMutationInProgress()
    ) {
      return;
    }
    this.hostForm.enable({ emitEvent: false });
    this.dialogMode.set('edit');
  }

  protected cancelEntityEdit(): void {
    if (
      this.dialogMode() !== 'edit' ||
      !this.selectedDatabase ||
      this.isSaving()
    ) {
      return;
    }
    this.prepareRecordDialog(this.selectedDatabase, 'view');
  }

  protected deactivateSelectedHost(): void {
    if (!this.selectedDatabase || this.hasMutationInProgress()) return;
    this.openDeleteDialog({ id: this.selectedDatabase.id });
  }

  protected submitEntity(): void {
    if (this.isSaving() || this.isEntityLoading()) return;
    if (this.hostForm.invalid) {
      this.hostForm.markAllAsTouched();
      return;
    }

    const value = this.hostForm.getRawValue();
    const payload: DatabaseInput = {
      serverId: value.server!.id,
      service: value.service.trim(),
      port: value.port!,
      databaseTypeId: value.databaseType!.id,
      description: value.description.trim() || null,
    };
    const mode = this.dialogMode();
    const request =
      mode === 'create'
        ? this.databasesService.create(payload)
        : this.databasesService.update(this.selectedDatabase!.id, payload);

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
              ? DATABASES_CREATE_SUCCESS_TITLE
              : DATABASES_UPDATE_SUCCESS_TITLE,
            mode === 'create'
              ? DATABASES_CREATE_SUCCESS_DETAIL
              : DATABASES_UPDATE_SUCCESS_DETAIL,
          );
          this.isDialogVisible.set(false);
          this.selectedDatabase = null;
          this.resetAfterMutation();
        },
        error: (error: unknown) =>
          this.handleError(error, DATABASES_SAVE_ERROR_DETAIL),
      });
  }

  protected onTableAction(event: ActionParams<InfrastructureTableRow>): void {
    if (this.hasMutationInProgress()) return;
    switch (event.action) {
      case InfrastructureTableAction.View:
        this.openRecordDialog(event.params.id, 'view');
        break;
      case InfrastructureTableAction.Edit:
        this.openRecordDialog(event.params.id, 'edit');
        break;
      case InfrastructureTableAction.Delete:
        this.openDeleteDialog(event.params);
        break;
      case InfrastructureTableAction.Restore:
        this.restoreSelectedHost(event.params.id);
        break;
    }
  }

  protected restoreSelectedHost(id = this.selectedDatabase?.id): void {
    if (id == null || this.hasMutationInProgress()) return;

    this.isSaving.set(true);
    this.databasesService
      .reactivate(id)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.isSaving.set(false)),
      )
      .subscribe({
        next: () => {
          if (this.isDialogVisible()) this.isDialogVisible.set(false);
          this.selectedDatabase = null;
          this.showSuccess(
            DATABASES_RESTORE_SUCCESS_TITLE,
            DATABASES_RESTORE_SUCCESS_DETAIL,
          );
          this.resetAfterMutation();
        },
        error: (error: unknown) =>
          this.handleError(error, DATABASES_RESTORE_ERROR_DETAIL),
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
    this.databasesService
      .delete(record.id)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.isDeleting.set(false)),
      )
      .subscribe({
        next: () => {
          this.pendingDelete.set(null);
          if (this.selectedDatabase?.id === record.id) {
            this.isDialogVisible.set(false);
          }
          this.selectedDatabase = null;
          this.showSuccess(
            DATABASES_DELETE_SUCCESS_TITLE,
            DATABASES_DELETE_SUCCESS_DETAIL,
          );
          this.resetAfterMutation();
        },
        error: (error: unknown) => {
          this.pendingDelete.set(null);
          this.handleError(error, DATABASES_DELETE_ERROR_DETAIL);
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
    this.selectedDatabase = null;
    this.selectedHostCanRestore.set(false);
    this.hostForm.enable({ emitEvent: false });
    this.hostForm.reset();
    this.hostForm.disable({ emitEvent: false });
    this.dialogMode.set(mode);
    this.isDialogVisible.set(true);
    this.isEntityLoading.set(true);

    this.databasesService
      .getById(id)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.isEntityLoading.set(false)),
      )
      .subscribe({
        next: (record) => {
          this.selectedDatabase = record;
          this.prepareRecordDialog(record, mode);
        },
        error: (error: unknown) => {
          this.isDialogVisible.set(false);
          this.handleError(error, DATABASES_LOAD_ERROR_DETAIL);
        },
      });
  }

  private prepareRecordDialog(
    record: DatabaseRecord,
    mode: Exclude<InfrastructureDialogMode, 'create'>,
  ): void {
    const server = this.serverOption(record);
    const vendor = {
      id: record.databaseType.id,
      name: record.databaseType.name,
      defaultPort: record.databaseType.defaultPort,
    };
    this.serverOptions.update((options) =>
      options.some(({ id }) => id === server.id) ? options : [server, ...options],
    );
    this.databaseTypeOptions.update((options) =>
      options.some(({ id }) => id === vendor.id) ? options : [vendor, ...options],
    );
    this.selectedHostCanRestore.set(mode === 'view' && Boolean(record.deletedAt));
    this.hostForm.enable({ emitEvent: false });
    this.hostForm.reset({
      server,
      service: record.service ?? '',
      port: record.port,
      databaseType: vendor,
      description: record.description ?? '',
    });
    if (mode === 'view') this.hostForm.disable({ emitEvent: false });
    this.dialogMode.set(mode);
  }

  private openDeleteDialog(record: { id: number }): void {
    this.pendingDelete.set(record);
    this.isDeleteDialogVisible.set(true);
  }

  private refresh(): void {
    this.isLoading.set(true);
    this.databasesService
      .getAll(this.toPageParams())
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => {
          this.isLoading.set(false);
          this.isInitialLoading.set(false);
        }),
      )
      .subscribe({
        next: (page) => {
          this.itemsList.set({
            items: page.content.map((record) => this.toTableRow(record)),
            total: page.totalElements,
          });
        },
        error: () => {
          this.itemsList.set({ items: [], total: 0 });
          this.showError(DATABASES_LOAD_ERROR_DETAIL);
        },
      });
  }

  private loadCatalogs(): void {
    this.serverCatalogService
      .getActiveOptions('DATABASE')
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (options) => this.serverOptions.set(options),
        error: () => this.showError(DATABASES_CATALOG_LOAD_ERROR_DETAIL),
      });
    this.vendorCatalogService
      .getActiveOptions()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (options) => this.databaseTypeOptions.set(options),
        error: () => this.showError(DATABASES_CATALOG_LOAD_ERROR_DETAIL),
      });
  }

  private toPageParams(): DatabasePageParams {
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
      serverId: this.appliedFilters.serverId ?? undefined,
      service: this.appliedFilters.service?.trim() || undefined,
      databaseTypeId: this.appliedFilters.databaseTypeId ?? undefined,
      statusId: this.appliedFilters.status ?? undefined,
      search: this.quickSearchTerm.trim() || undefined,
    };
  }

  private toTableRow(record: DatabaseRecord): DatabaseTableRow {
    return {
      id: record.id,
      deletedAt: record.deletedAt,
      server: record.server?.name ?? '',
      environment: record.server?.environment
        ? localizedName(
            record.server.environment,
            this.locale,
            record.server.environment.code || `#${record.server.environment.id}`,
          )
        : '',
      service: record.service ?? '',
      port: record.port,
      databaseType: record.databaseType?.name ?? '',
      status: INFRASTRUCTURE_STATUS_LABELS[record.deletedAt ? 2 : 1],
      description: record.description ?? '',
    };
  }

  private serverOption(record: DatabaseRecord): ServerCatalogOption {
    const server = record.server;
    const environmentLabel = localizedName(
      server.environment,
      this.locale,
      server.environment.code || `#${server.environment.id}`,
    );
    return {
      id: server.id,
      name: server.name,
      environment: server.environment,
      label: `${server.name} · ${environmentLabel}`,
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
      summary: DATABASES_LOAD_ERROR_SUMMARY,
      detail,
    });
  }

  private showSuccess(summary: string, detail: string): void {
    this.messageService.add({ severity: 'success', summary, detail });
  }
}
