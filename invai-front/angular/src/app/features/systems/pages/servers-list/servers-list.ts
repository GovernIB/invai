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
import { finalize } from 'rxjs';

import {
  InfrastructureTable,
  InfrastructureTableAction,
} from '../../components/infrastructure-table/infrastructure-table';
import { ServerFiltersForm } from '../../components/server-filters-form/server-filters-form';
import { ServerMaintenanceDialog } from '../../components/server-maintenance-dialog/server-maintenance-dialog';
import { createSystemHostForm } from '../../forms/infrastructure-maintenance-forms.factory';
import { createServerFiltersForm } from '../../forms/server-filters-form.factory';
import { ServerCatalogService } from '../../services/server-catalog.service';
import { SystemsService } from '../../services/systems.service';
import {
  INFRASTRUCTURE_STATUS_LABELS,
  SERVERS_TABLE_COLUMNS,
} from '../../systems.constants';
import {
  InfrastructureDialogMode,
  InfrastructureSystem,
  InfrastructureSystemInput,
  InfrastructureTableRow,
  ServerCatalogOption,
  ServerTableRow,
  SystemPageParams,
} from '../../systems.model';
import {
  SERVERS_ADD_ARIA_LABEL,
  SERVERS_CATALOG_LOAD_ERROR_DETAIL,
  SERVERS_CREATE_SUCCESS_DETAIL,
  SERVERS_CREATE_SUCCESS_TITLE,
  SERVERS_DELETE_CANCEL,
  SERVERS_DELETE_CONFIRM,
  SERVERS_DELETE_ERROR_DETAIL,
  SERVERS_DELETE_MESSAGE,
  SERVERS_DELETE_SUCCESS_DETAIL,
  SERVERS_DELETE_SUCCESS_TITLE,
  SERVERS_DELETE_TITLE,
  SERVERS_FILTER_INSTANCE,
  SERVERS_FILTER_SERVER,
  SERVERS_FILTER_STATUS,
  SERVERS_FILTER_VERSION,
  SERVERS_HOSTS_ARIA_LABEL,
  SERVERS_LOAD_ERROR_DETAIL,
  SERVERS_LOAD_ERROR_SUMMARY,
  SERVERS_RESTORE_ERROR_DETAIL,
  SERVERS_RESTORE_SUCCESS_DETAIL,
  SERVERS_RESTORE_SUCCESS_TITLE,
  SERVERS_SAVE_ERROR_DETAIL,
  SERVERS_UPDATE_SUCCESS_DETAIL,
  SERVERS_UPDATE_SUCCESS_TITLE,
} from './servers-list.i18n';

const DEFAULT_PAGE_SIZE = 10;

@Component({
  standalone: true,
  selector: 'app-servers-list',
  imports: [
    ConfirmationDialogComponent,
    InfrastructureTable,
    SearchFiltersComponent,
    SectionActionsComponent,
    ServerFiltersForm,
    ServerMaintenanceDialog,
  ],
  templateUrl: './servers-list.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ServersList implements OnInit {
  private readonly formBuilder = inject(FormBuilder);
  private readonly destroyRef = inject(DestroyRef);
  private readonly locale = inject(LOCALE_ID);
  private readonly messageService = inject(MessageService);
  private readonly systemsService = inject(SystemsService);
  private readonly serverCatalogService = inject(ServerCatalogService);

  protected readonly filtersForm = createServerFiltersForm(this.formBuilder);
  protected readonly hostForm = createSystemHostForm(this.formBuilder);
  protected readonly itemsList = signal<PaginatedList<ServerTableRow>>({
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
  protected readonly isEntityLoading = signal(false);
  protected readonly isSaving = signal(false);
  protected readonly isDeleteDialogVisible = signal(false);
  protected readonly isDeleting = signal(false);
  protected readonly pendingDelete = signal<{ id: number } | null>(null);
  protected readonly columns = SERVERS_TABLE_COLUMNS;
  protected readonly selectedColumns = signal<KeyLabel[]>([]);
  protected readonly displayedColumns = computed(() =>
    fnGetVisibleColumns(this.columns, this.selectedColumns()),
  );
  protected readonly deleteMessage = computed(() =>
    SERVERS_DELETE_MESSAGE(this.pendingDelete()?.id ?? 0),
  );

  protected readonly filterLabels = {
    server: SERVERS_FILTER_SERVER,
    instance: SERVERS_FILTER_INSTANCE,
    version: SERVERS_FILTER_VERSION,
    status: SERVERS_FILTER_STATUS,
  };
  protected readonly hostsAriaLabel = SERVERS_HOSTS_ARIA_LABEL;
  protected readonly addAriaLabel = SERVERS_ADD_ARIA_LABEL;
  protected readonly deleteTitle = SERVERS_DELETE_TITLE;
  protected readonly deleteCancel = SERVERS_DELETE_CANCEL;
  protected readonly deleteConfirm = SERVERS_DELETE_CONFIRM;

  private selectedHost: InfrastructureSystem | null = null;
  private tableState: TableLazyLoadEvent = {
    first: 0,
    rows: DEFAULT_PAGE_SIZE,
  };
  private appliedFilters = this.filtersForm.getRawValue();
  private appliedStatusFilter = this.appliedFilters.status;

  ngOnInit(): void {
    this.selectedColumns.set([...this.columns]);
    this.synchronizeStatusColumnSelection(true);
    this.loadServerOptions();
    this.refresh();
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
    this.selectedFilters.set(1);
    this.resetPage();
    this.refresh();
  }

  protected openCreateDialog(): void {
    if (this.hasMutationInProgress()) return;

    this.selectedHost = null;
    this.selectedHostCanRestore.set(false);
    this.dialogMode.set('create');
    this.hostForm.enable({ emitEvent: false });
    this.hostForm.reset();
    this.isDialogVisible.set(true);
  }

  protected closeDialog(): void {
    if (this.isEntityLoading() || this.isSaving()) return;
    this.isDialogVisible.set(false);
    this.selectedHost = null;
    this.selectedHostCanRestore.set(false);
  }

  protected startEntityEdit(): void {
    if (
      this.dialogMode() !== 'view' ||
      this.selectedHostCanRestore() ||
      !this.selectedHost ||
      this.hasMutationInProgress()
    ) {
      return;
    }

    this.hostForm.enable({ emitEvent: false });
    this.dialogMode.set('edit');
  }

  protected cancelEntityEdit(): void {
    if (this.dialogMode() !== 'edit' || !this.selectedHost || this.isSaving()) {
      return;
    }
    this.prepareRecordDialog(this.selectedHost, 'view');
  }

  protected deactivateSelectedHost(): void {
    if (!this.selectedHost || this.hasMutationInProgress()) return;
    this.openDeleteDialog({ id: this.selectedHost.id });
  }

  protected submitEntity(): void {
    if (this.isSaving() || this.isEntityLoading()) return;
    if (this.hostForm.invalid) {
      this.hostForm.markAllAsTouched();
      return;
    }

    const value = this.hostForm.getRawValue();
    const payload: InfrastructureSystemInput = {
      name: value.server!.name,
      serverId: value.server!.id,
      instance: value.instance.trim(),
      port: value.port!,
      version: value.version.trim(),
      description: value.description.trim() || null,
    };
    const mode = this.dialogMode();
    const request =
      mode === 'create'
        ? this.systemsService.create(payload)
        : this.systemsService.update(this.selectedHost!.id, payload);

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
              ? SERVERS_CREATE_SUCCESS_TITLE
              : SERVERS_UPDATE_SUCCESS_TITLE,
            mode === 'create'
              ? SERVERS_CREATE_SUCCESS_DETAIL
              : SERVERS_UPDATE_SUCCESS_DETAIL,
          );
          this.isDialogVisible.set(false);
          this.selectedHost = null;
          this.resetAfterMutation();
        },
        error: (error: unknown) => this.handleError(error, SERVERS_SAVE_ERROR_DETAIL),
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

  protected restoreSelectedHost(id = this.selectedHost?.id): void {
    if (id == null || this.hasMutationInProgress()) return;

    this.isSaving.set(true);
    this.systemsService
      .reactivate(id)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.isSaving.set(false)),
      )
      .subscribe({
        next: () => {
          if (this.isDialogVisible()) this.isDialogVisible.set(false);
          this.selectedHost = null;
          this.showSuccess(
            SERVERS_RESTORE_SUCCESS_TITLE,
            SERVERS_RESTORE_SUCCESS_DETAIL,
          );
          this.resetAfterMutation();
        },
        error: (error: unknown) =>
          this.handleError(error, SERVERS_RESTORE_ERROR_DETAIL),
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
    this.systemsService
      .delete(record.id)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.isDeleting.set(false)),
      )
      .subscribe({
        next: () => {
          this.pendingDelete.set(null);
          if (this.selectedHost?.id === record.id) this.isDialogVisible.set(false);
          this.selectedHost = null;
          this.showSuccess(
            SERVERS_DELETE_SUCCESS_TITLE,
            SERVERS_DELETE_SUCCESS_DETAIL,
          );
          this.resetAfterMutation();
        },
        error: (error: unknown) => {
          this.pendingDelete.set(null);
          this.handleError(error, SERVERS_DELETE_ERROR_DETAIL);
        },
      });
  }

  private openRecordDialog(
    id: number,
    mode: Exclude<InfrastructureDialogMode, 'create'>,
  ): void {
    this.selectedHost = null;
    this.selectedHostCanRestore.set(false);
    this.hostForm.enable({ emitEvent: false });
    this.hostForm.reset();
    this.hostForm.disable({ emitEvent: false });
    this.dialogMode.set(mode);
    this.isDialogVisible.set(true);
    this.isEntityLoading.set(true);

    this.systemsService
      .getById(id)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.isEntityLoading.set(false)),
      )
      .subscribe({
        next: (record) => {
          this.selectedHost = record;
          this.prepareRecordDialog(record, mode);
        },
        error: (error: unknown) => {
          this.isDialogVisible.set(false);
          this.handleError(error, SERVERS_LOAD_ERROR_DETAIL);
        },
      });
  }

  private prepareRecordDialog(
    record: InfrastructureSystem,
    mode: Exclude<InfrastructureDialogMode, 'create'>,
  ): void {
    const server = this.serverOption(record);
    this.serverOptions.update((options) =>
      options.some(({ id }) => id === server.id) ? options : [server, ...options],
    );
    this.selectedHostCanRestore.set(mode === 'view' && Boolean(record.deletedAt));
    this.hostForm.enable({ emitEvent: false });
    this.hostForm.reset({
      server,
      instance: record.instance ?? '',
      port: record.port,
      version: record.version ?? '',
      description: record.description ?? '',
    });
    this.dialogMode.set(mode);
  }

  private openDeleteDialog(record: { id: number }): void {
    this.pendingDelete.set(record);
    this.isDeleteDialogVisible.set(true);
  }

  private refresh(): void {
    this.isLoading.set(true);
    this.systemsService
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
          this.showError(SERVERS_LOAD_ERROR_DETAIL);
        },
      });
  }

  private loadServerOptions(): void {
    this.serverCatalogService
      .getActiveOptions('APPLICATION')
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (options) => this.serverOptions.set(options),
        error: () => this.showError(SERVERS_CATALOG_LOAD_ERROR_DETAIL),
      });
  }

  private toPageParams(): SystemPageParams {
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
      instance: this.appliedFilters.instance?.trim() || undefined,
      version: this.appliedFilters.version?.trim() || undefined,
      statusId: this.appliedFilters.status ?? undefined,
    };
  }

  private toTableRow(record: InfrastructureSystem): ServerTableRow {
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
      instance: record.instance ?? '',
      port: record.port,
      version: record.version ?? '',
      status:
        INFRASTRUCTURE_STATUS_LABELS[record.deletedAt ? 2 : 1],
      description: record.description ?? '',
    };
  }

  private serverOption(record: InfrastructureSystem): ServerCatalogOption {
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
      summary: SERVERS_LOAD_ERROR_SUMMARY,
      detail,
    });
  }

  private showSuccess(summary: string, detail: string): void {
    this.messageService.add({ severity: 'success', summary, detail });
  }
}
