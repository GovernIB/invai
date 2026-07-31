import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  LOCALE_ID,
  OnInit,
  computed,
  inject,
  input,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder } from '@angular/forms';
import { ConfirmationDialogComponent } from '@components/confirmation-dialog/confirmation-dialog.component';
import { SearchFiltersComponent } from '@components/search-filters/search-filters.component';
import { SectionActionsComponent } from '@components/section-actions/section-actions.component';
import { isStructuredBadRequest } from '@core/models/api-error.model';
import { EnvironmentCatalogOption } from '@features/environments/environments.model';
import { EnvironmentCatalogService } from '@features/environments/services/environment-catalog.service';
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

import { PhysicalServerDialog } from '../../components/physical-server-dialog/physical-server-dialog';
import { PhysicalServerFiltersForm } from '../../components/physical-server-filters-form/physical-server-filters-form';
import {
  InfrastructureTable,
  InfrastructureTableAction,
} from '../../components/infrastructure-table/infrastructure-table';
import { createPhysicalServerFiltersForm } from '../../forms/physical-server-filters-form.factory';
import { createPhysicalServerForm } from '../../forms/physical-server-form.factory';
import { ServerTypesService } from '../../services/server-types.service';
import { ServersService } from '../../services/servers.service';
import {
  INFRASTRUCTURE_STATUS_LABELS,
  PHYSICAL_SERVERS_TABLE_COLUMNS,
} from '../../systems.constants';
import {
  InfrastructureDialogMode,
  InfrastructureServer,
  InfrastructureServerInput,
  InfrastructureTableRow,
  PhysicalServerTableRow,
  ServerPageParams,
  ServerTypeCode,
} from '../../systems.model';
import {
  PHYSICAL_SERVERS_CATALOG_LOAD_ERROR_DETAIL,
  PHYSICAL_SERVERS_COPY,
  PHYSICAL_SERVERS_DELETE_CANCEL,
  PHYSICAL_SERVERS_DELETE_CONFIRM,
  PHYSICAL_SERVERS_DELETE_ERROR_DETAIL,
  PHYSICAL_SERVERS_LOAD_ERROR_DETAIL,
  PHYSICAL_SERVERS_LOAD_ERROR_SUMMARY,
  PHYSICAL_SERVERS_QUICK_SEARCH_ARIA_LABEL,
  PHYSICAL_SERVERS_RESTORE_ERROR_DETAIL,
  PHYSICAL_SERVERS_SAVE_ERROR_DETAIL,
  PHYSICAL_SERVERS_SERVER_TYPE_LOAD_ERROR_DETAIL,
} from './physical-servers-list.i18n';

const DEFAULT_PAGE_SIZE = 10;
const QUICK_SEARCH_DEBOUNCE_MS = 400;

@Component({
  standalone: true,
  selector: 'app-physical-servers-list',
  imports: [
    ConfirmationDialogComponent,
    InfrastructureTable,
    PhysicalServerDialog,
    PhysicalServerFiltersForm,
    SearchFiltersComponent,
    SectionActionsComponent,
  ],
  templateUrl: './physical-servers-list.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PhysicalServersList implements OnInit {
  serverTypeCode = input.required<ServerTypeCode>();

  private readonly formBuilder = inject(FormBuilder);
  private readonly destroyRef = inject(DestroyRef);
  private readonly locale = inject(LOCALE_ID);
  private readonly messageService = inject(MessageService);
  private readonly environmentCatalogService = inject(EnvironmentCatalogService);
  private readonly serversService = inject(ServersService);
  private readonly serverTypesService = inject(ServerTypesService);

  protected readonly filtersForm = createPhysicalServerFiltersForm(this.formBuilder);
  protected readonly entityForm = createPhysicalServerForm(this.formBuilder);
  protected readonly itemsList = signal<PaginatedList<PhysicalServerTableRow>>({
    items: [],
    total: 0,
  });
  protected readonly environmentOptions = signal<EnvironmentCatalogOption[]>([]);
  protected readonly isFiltersCollapsed = signal(true);
  protected readonly selectedFilters = signal(1);
  protected readonly tableFirst = signal(0);
  protected readonly isLoading = signal(false);
  protected readonly isInitialLoading = signal(true);
  protected readonly isDialogVisible = signal(false);
  protected readonly dialogMode = signal<InfrastructureDialogMode>('create');
  protected readonly canRestore = signal(false);
  protected readonly isEntityLoading = signal(false);
  protected readonly isServerTypeLoading = signal(false);
  protected readonly isSaving = signal(false);
  protected readonly isDeleteDialogVisible = signal(false);
  protected readonly isDeleting = signal(false);
  protected readonly pendingDelete = signal<{ id: number } | null>(null);
  protected readonly columns = PHYSICAL_SERVERS_TABLE_COLUMNS;
  protected readonly selectedColumns = signal<KeyLabel[]>([]);
  protected readonly displayedColumns = computed(() =>
    fnGetVisibleColumns(this.columns, this.selectedColumns()),
  );
  protected readonly copy = computed(
    () => PHYSICAL_SERVERS_COPY[this.serverTypeCode()],
  );
  protected readonly deleteMessage = computed(() =>
    this.copy().deleteMessage(this.pendingDelete()?.id ?? 0),
  );
  protected readonly deleteCancel = PHYSICAL_SERVERS_DELETE_CANCEL;
  protected readonly deleteConfirm = PHYSICAL_SERVERS_DELETE_CONFIRM;
  protected readonly quickSearchAriaLabel =
    PHYSICAL_SERVERS_QUICK_SEARCH_ARIA_LABEL;

  quickSearchTerm = '';
  private selectedServer: InfrastructureServer | null = null;
  private selectedServerTypeId: number | null = null;
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
    this.loadEnvironmentOptions();
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
    this.selectedServerTypeId = null;
    this.isServerTypeLoading.set(true);
    this.serverTypesService
      .getByCode(this.serverTypeCode())
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.isServerTypeLoading.set(false)),
      )
      .subscribe({
        next: (serverType) => {
          this.selectedServerTypeId = serverType.id;
          this.selectedServer = null;
          this.canRestore.set(false);
          this.dialogMode.set('create');
          this.entityForm.enable({ emitEvent: false });
          this.entityForm.reset();
          this.isDialogVisible.set(true);
        },
        error: () =>
          this.showError(PHYSICAL_SERVERS_SERVER_TYPE_LOAD_ERROR_DETAIL),
      });
  }

  protected closeDialog(): void {
    if (this.isEntityLoading() || this.isSaving()) return;
    this.isDialogVisible.set(false);
    this.selectedServer = null;
    this.selectedServerTypeId = null;
    this.canRestore.set(false);
  }

  protected startEntityEdit(): void {
    if (
      this.dialogMode() !== 'view' ||
      this.canRestore() ||
      !this.selectedServer ||
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
      !this.selectedServer ||
      this.isSaving()
    ) {
      return;
    }
    this.prepareRecordDialog(this.selectedServer, 'view');
  }

  protected deactivateSelected(): void {
    if (!this.selectedServer || this.hasMutationInProgress()) return;
    this.openDeleteDialog({ id: this.selectedServer.id });
  }

  protected submitEntity(): void {
    if (this.isSaving() || this.isEntityLoading()) return;
    if (this.entityForm.invalid) {
      this.entityForm.markAllAsTouched();
      return;
    }

    const value = this.entityForm.getRawValue();
    const mode = this.dialogMode();
    const serverTypeId =
      mode === 'create'
        ? this.selectedServerTypeId
        : this.selectedServer?.serverType.id;
    if (serverTypeId == null) {
      this.showError(PHYSICAL_SERVERS_SERVER_TYPE_LOAD_ERROR_DETAIL);
      return;
    }
    const payload: InfrastructureServerInput = {
      name: value.name.trim(),
      environmentId: value.environment!.id,
      serverTypeId,
    };
    const request =
      mode === 'create'
        ? this.serversService.create(payload)
        : this.serversService.update(this.selectedServer!.id, payload);

    this.isSaving.set(true);
    request
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.isSaving.set(false)),
      )
      .subscribe({
        next: () => {
          this.showSuccess(
            mode === 'create' ? this.copy().createTitle : this.copy().updateTitle,
            mode === 'create' ? this.copy().createDetail : this.copy().updateDetail,
          );
          this.isDialogVisible.set(false);
          this.selectedServer = null;
          this.selectedServerTypeId = null;
          this.resetAfterMutation();
        },
        error: (error: unknown) =>
          this.handleError(error, PHYSICAL_SERVERS_SAVE_ERROR_DETAIL),
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
        this.restore(event.params.id);
        break;
    }
  }

  protected restore(id = this.selectedServer?.id): void {
    if (id == null || this.hasMutationInProgress()) return;
    this.isSaving.set(true);
    this.serversService
      .reactivate(id)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.isSaving.set(false)),
      )
      .subscribe({
        next: () => {
          this.isDialogVisible.set(false);
          this.selectedServer = null;
          this.showSuccess(this.copy().restoreTitle, this.copy().restoreDetail);
          this.resetAfterMutation();
        },
        error: (error: unknown) =>
          this.handleError(error, PHYSICAL_SERVERS_RESTORE_ERROR_DETAIL),
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
    this.serversService
      .delete(record.id)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.isDeleting.set(false)),
      )
      .subscribe({
        next: () => {
          this.pendingDelete.set(null);
          if (this.selectedServer?.id === record.id) {
            this.isDialogVisible.set(false);
          }
          this.selectedServer = null;
          this.showSuccess(
            this.copy().deleteSuccessTitle,
            this.copy().deleteSuccessDetail,
          );
          this.resetAfterMutation();
        },
        error: (error: unknown) => {
          this.pendingDelete.set(null);
          this.handleError(error, PHYSICAL_SERVERS_DELETE_ERROR_DETAIL);
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
    this.selectedServer = null;
    this.selectedServerTypeId = null;
    this.canRestore.set(false);
    this.entityForm.enable({ emitEvent: false });
    this.entityForm.reset();
    this.entityForm.disable({ emitEvent: false });
    this.dialogMode.set(mode);
    this.isDialogVisible.set(true);
    this.isEntityLoading.set(true);
    this.serversService
      .getById(id)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.isEntityLoading.set(false)),
      )
      .subscribe({
        next: (record) => {
          this.selectedServer = record;
          this.prepareRecordDialog(record, mode);
        },
        error: (error: unknown) => {
          this.isDialogVisible.set(false);
          this.handleError(error, PHYSICAL_SERVERS_LOAD_ERROR_DETAIL);
        },
      });
  }

  private prepareRecordDialog(
    record: InfrastructureServer,
    mode: Exclude<InfrastructureDialogMode, 'create'>,
  ): void {
    const environment = this.environmentOption(record);
    this.environmentOptions.update((options) =>
      options.some(({ id }) => id === environment.id)
        ? options
        : [environment, ...options],
    );
    this.canRestore.set(mode === 'view' && Boolean(record.deletedAt));
    this.entityForm.enable({ emitEvent: false });
    this.entityForm.reset({ name: record.name ?? '', environment });
    if (mode === 'view') this.entityForm.disable({ emitEvent: false });
    this.dialogMode.set(mode);
  }

  private openDeleteDialog(record: { id: number }): void {
    this.pendingDelete.set(record);
    this.isDeleteDialogVisible.set(true);
  }

  private refresh(): void {
    this.isLoading.set(true);
    this.serversService
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
          this.showError(PHYSICAL_SERVERS_LOAD_ERROR_DETAIL);
        },
      });
  }

  private loadEnvironmentOptions(): void {
    this.environmentCatalogService
      .getActiveOptions()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (options) => this.environmentOptions.set(options),
        error: () => this.showError(PHYSICAL_SERVERS_CATALOG_LOAD_ERROR_DETAIL),
      });
  }

  private toPageParams(): ServerPageParams {
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
      environmentId: this.appliedFilters.environmentId ?? undefined,
      serverTypeCode: this.serverTypeCode(),
      statusId: this.appliedFilters.status ?? undefined,
      search: this.quickSearchTerm.trim() || undefined,
    };
  }

  private toTableRow(record: InfrastructureServer): PhysicalServerTableRow {
    return {
      id: record.id,
      deletedAt: record.deletedAt,
      name: record.name ?? '',
      environment: localizedName(
        record.environment,
        this.locale,
        record.environment.code || `#${record.environment.id}`,
      ),
      status: INFRASTRUCTURE_STATUS_LABELS[record.deletedAt ? 2 : 1],
    };
  }

  private environmentOption(
    record: InfrastructureServer,
  ): EnvironmentCatalogOption {
    return {
      id: record.environment.id,
      code: record.environment.code || `#${record.environment.id}`,
      label: localizedName(
        record.environment,
        this.locale,
        record.environment.code || `#${record.environment.id}`,
      ),
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
    return (
      this.isSaving() ||
      this.isDeleting() ||
      this.isEntityLoading() ||
      this.isServerTypeLoading()
    );
  }

  private handleError(error: unknown, detail: string): void {
    if (isStructuredBadRequest(error)) return;
    this.showError(detail);
  }

  private showError(detail: string): void {
    this.messageService.add({
      severity: 'error',
      summary: PHYSICAL_SERVERS_LOAD_ERROR_SUMMARY,
      detail,
    });
  }

  private showSuccess(summary: string, detail: string): void {
    this.messageService.add({ severity: 'success', summary, detail });
  }
}
