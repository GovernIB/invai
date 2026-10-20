import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  inject,
  input,
  OnInit,
  signal,
  viewChild,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { ConfirmationDialogComponent } from '@components/confirmation-dialog/confirmation-dialog.component';
import { SearchFiltersComponent } from '@components/search-filters/search-filters.component';
import { SectionActionsComponent } from '@components/section-actions/section-actions.component';
import { isStructuredBadRequest } from '@core/models/api-error.model';
import { ResponsibleCompanyOption } from '@features/maintenances/responsibles/responsibles.model';
import { ActionParams } from '@models/table.model';
import { SearchComponentBase } from '@shared/classes/search-component-base';
import { TableLazyLoadEvent } from 'primeng/table';
import {
  catchError,
  debounceTime,
  distinctUntilChanged,
  EMPTY,
  finalize,
  map,
  Subject,
  switchMap,
  tap,
} from 'rxjs';

import {
  ExternalSystemsTable,
  ExternalSystemTableAction,
} from '../../components/external-systems-table/external-systems-table';
import {
  ExternalSystemDialog,
  ExternalSystemDialogMode,
} from '../../components/external-system-dialog/external-system-dialog';
import { ExternalSystemFiltersForm } from '../../components/external-system-filters-form/external-system-filters-form';
import { createExternalSystemFiltersForm } from '../../forms/external-system-filters-form.factory';
import { createExternalSystemForm } from '../../forms/external-system-form.factory';
import { EXTERNAL_SYSTEMS_TABLE_COLUMNS } from '../../external-systems.constants';
import {
  ExternalSystem,
  ExternalSystemFilters,
  ExternalSystemInput,
  ExternalSystemPageParams,
} from '../../external-systems.model';
import { ExternalSystemsService } from '../../services/external-systems.service';
import {
  EXTERNAL_SYSTEMS_ADD_ARIA_LABEL,
  EXTERNAL_SYSTEMS_FILTER_COMPANY,
  EXTERNAL_SYSTEMS_FILTER_NAME,
  EXTERNAL_SYSTEMS_FILTER_STATUS,
  EXTERNAL_SYSTEMS_LIST_TITLE,
  EXTERNAL_SYSTEMS_LOAD_ERROR_DETAIL,
  EXTERNAL_SYSTEMS_LOAD_ERROR_SUMMARY,
  EXTERNAL_SYSTEMS_QUICK_SEARCH_ARIA_LABEL,
  EXTERNAL_SYSTEMS_QUICK_SEARCH_PLACEHOLDER,
  EXTERNAL_SYSTEM_CREATE_SUCCESS_DETAIL,
  EXTERNAL_SYSTEM_CREATE_SUCCESS_TITLE,
  EXTERNAL_SYSTEM_DELETE_DIALOG_CANCEL_ARIA_LABEL,
  EXTERNAL_SYSTEM_DELETE_DIALOG_CANCEL_LABEL,
  EXTERNAL_SYSTEM_DELETE_DIALOG_CONFIRM_ARIA_LABEL,
  EXTERNAL_SYSTEM_DELETE_DIALOG_CONFIRM_LABEL,
  EXTERNAL_SYSTEM_DELETE_DIALOG_MESSAGE,
  EXTERNAL_SYSTEM_DELETE_DIALOG_TITLE,
  EXTERNAL_SYSTEM_DELETE_ERROR_DETAIL,
  EXTERNAL_SYSTEM_DELETE_SUCCESS_DETAIL,
  EXTERNAL_SYSTEM_DELETE_SUCCESS_TITLE,
  EXTERNAL_SYSTEM_LOAD_ERROR_DETAIL,
  EXTERNAL_SYSTEM_RESTORE_ERROR_DETAIL,
  EXTERNAL_SYSTEM_RESTORE_SUCCESS_DETAIL,
  EXTERNAL_SYSTEM_RESTORE_SUCCESS_TITLE,
  EXTERNAL_SYSTEM_SAVE_ERROR_DETAIL,
  EXTERNAL_SYSTEM_UPDATE_SUCCESS_DETAIL,
  EXTERNAL_SYSTEM_UPDATE_SUCCESS_TITLE,
} from './external-systems-list.i18n';
import {
  EXTERNAL_SYSTEMS_LIST_RESOLVE_KEY,
  ExternalSystemsListResolvedData,
} from './external-systems-list.resolver';

const DEFAULT_PAGE_SIZE = 10;
const QUICK_SEARCH_DEBOUNCE_MS = 400;

@Component({
  selector: 'app-external-systems-list',
  standalone: true,
  imports: [
    ConfirmationDialogComponent,
    SearchFiltersComponent,
    SectionActionsComponent,
    ExternalSystemsTable,
    ExternalSystemDialog,
    ExternalSystemFiltersForm,
  ],
  templateUrl: './external-systems-list.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ExternalSystemsList
  extends SearchComponentBase<ExternalSystem, ExternalSystemFilters>
  implements OnInit
{
  // Active companies for the dialog; every company for the filters.
  companyOptions = input.required<ResponsibleCompanyOption[]>();
  filterCompanyOptions = input.required<ResponsibleCompanyOption[]>();
  protected override readonly ALL_TABLE_COLUMNS = EXTERNAL_SYSTEMS_TABLE_COLUMNS;
  protected readonly title = EXTERNAL_SYSTEMS_LIST_TITLE;
  protected readonly filterLabels = {
    name: EXTERNAL_SYSTEMS_FILTER_NAME,
    company: EXTERNAL_SYSTEMS_FILTER_COMPANY,
    status: EXTERNAL_SYSTEMS_FILTER_STATUS,
  };
  protected readonly quickSearchPlaceholder = EXTERNAL_SYSTEMS_QUICK_SEARCH_PLACEHOLDER;
  protected readonly quickSearchAriaLabel = EXTERNAL_SYSTEMS_QUICK_SEARCH_ARIA_LABEL;
  protected readonly addAriaLabel = EXTERNAL_SYSTEMS_ADD_ARIA_LABEL;
  protected readonly deleteDialogTitle = EXTERNAL_SYSTEM_DELETE_DIALOG_TITLE;
  protected readonly deleteDialogCancelLabel = EXTERNAL_SYSTEM_DELETE_DIALOG_CANCEL_LABEL;
  protected readonly deleteDialogConfirmLabel = EXTERNAL_SYSTEM_DELETE_DIALOG_CONFIRM_LABEL;
  protected readonly deleteDialogCancelAriaLabel = EXTERNAL_SYSTEM_DELETE_DIALOG_CANCEL_ARIA_LABEL;
  protected readonly deleteDialogConfirmAriaLabel = EXTERNAL_SYSTEM_DELETE_DIALOG_CONFIRM_ARIA_LABEL;

  quickSearchTerm = '';
  protected override filtersForm = createExternalSystemFiltersForm(this.fb);
  protected readonly entityForm = createExternalSystemForm(this.fb);
  protected readonly tableFirst = signal(0);
  protected readonly dialogMode = signal<ExternalSystemDialogMode>('create');
  protected readonly isDialogVisible = signal(false);
  protected readonly isEntityLoading = signal(false);
  protected readonly isSaving = signal(false);
  protected readonly isDeleteDialogVisible = signal(false);
  protected readonly isDeleting = signal(false);
  protected readonly selectedEntityCanRestore = signal(false);
  protected readonly pendingDelete = signal<ExternalSystem | null>(null);
  protected readonly deleteDialogMessage = computed(() =>
    EXTERNAL_SYSTEM_DELETE_DIALOG_MESSAGE(this.pendingDelete()?.name ?? ''),
  );
  protected readonly isSearchIndicatorLoading = computed(
    () => this.isLoading() || this.isQuickSearchPending(),
  );
  protected readonly isInitialTableLoading = computed(
    () => this.isLoading() && !this.hasLoadedResults(),
  );
  protected readonly dialogCompanyOptions = computed(() => {
    const options = this.companyOptions();
    const currentCompany = this.selectedEntity()?.company;
    if (!currentCompany || options.some(({ id }) => id === currentCompany.id)) return options;
    return [
      ...options,
      { id: currentCompany.id, label: currentCompany.name?.trim() || `#${currentCompany.id}` },
    ];
  });

  private readonly selectedEntityId = signal<number | null>(null);
  private readonly selectedEntity = signal<ExternalSystem | null>(null);
  private readonly isQuickSearchPending = signal(false);
  private readonly hasLoadedResults = signal(false);
  private readonly externalSystemsService = inject(ExternalSystemsService);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);
  private readonly quickSearchChanges = new Subject<string>();
  private readonly searchRequests = new Subject<ExternalSystemPageParams>();
  private readonly externalSystemsTable = viewChild(ExternalSystemsTable);

  override ngOnInit(): void {
    this.observeSearchRequests();
    this.observeQuickSearch();
    super.ngOnInit();
  }

  protected override fetchFilteredList(
    filters: ExternalSystemFilters,
    event?: TableLazyLoadEvent,
  ): void {
    this.searchRequests.next(this.toPageParams(filters, event));
  }

  protected override initializeResults(): void {
    const resolved = this.route.snapshot.data[
      EXTERNAL_SYSTEMS_LIST_RESOLVE_KEY
    ] as ExternalSystemsListResolvedData;
    this.updateSearchState();
    this.hasLoadedResults.set(true);
    if (resolved?.page) {
      this.itemsList.set({ items: resolved.page.content, total: resolved.page.totalElements });
    } else {
      this.itemsList.set({ items: [], total: 0 });
      if (resolved?.pageLoadFailed) this.showError(EXTERNAL_SYSTEMS_LOAD_ERROR_DETAIL);
    }
  }

  protected onQuickSearchChange(value: string): void {
    this.quickSearchTerm = value;
    this.isQuickSearchPending.set(true);
    this.quickSearchChanges.next(value);
  }

  protected onFilterSearch(): void {
    this.tableFirst.set(0);
    this.applyFiltersAndSearch();
  }

  protected onPageChange(event: TableLazyLoadEvent): void {
    this.tableFirst.set(event.first ?? 0);
    this.onSearch(event);
  }

  override reset(): void {
    this.tableFirst.set(0);
    super.reset();
  }

  protected openCreateDialog(): void {
    if (this.hasMutationInProgress()) return;
    this.clearSelection();
    this.prepareEntityForm(null, 'create');
    this.dialogMode.set('create');
    this.isDialogVisible.set(true);
  }

  protected onTableAction(event: ActionParams<ExternalSystem>): void {
    if (this.hasMutationInProgress()) return;
    switch (event.action) {
      case ExternalSystemTableAction.View:
        this.openExistingDialog(event.params, 'view');
        break;
      case ExternalSystemTableAction.Edit:
        this.openExistingDialog(event.params, 'edit');
        break;
      case ExternalSystemTableAction.Delete:
        this.pendingDelete.set(event.params);
        this.isDeleteDialogVisible.set(true);
        break;
      case ExternalSystemTableAction.Restore:
        this.restoreSelectedEntity(event.params);
        break;
    }
  }

  protected closeEntityDialog(): void {
    if (this.isEntityLoading() || this.isSaving()) return;
    this.isDialogVisible.set(false);
    this.clearSelection();
  }

  protected startEntityEdit(): void {
    if (
      this.dialogMode() !== 'view' ||
      this.selectedEntityCanRestore() ||
      this.hasMutationInProgress()
    ) {
      return;
    }
    this.entityForm.enable({ emitEvent: false });
    this.dialogMode.set('edit');
  }

  protected cancelEntityEdit(): void {
    const externalSystem = this.selectedEntity();
    if (this.dialogMode() !== 'edit' || !externalSystem || this.isSaving()) return;
    this.prepareEntityForm(externalSystem, 'view');
    this.dialogMode.set('view');
  }

  protected deactivateSelectedEntity(): void {
    const externalSystem = this.selectedEntity();
    if (!externalSystem || externalSystem.deletedAt || this.hasMutationInProgress()) return;
    this.pendingDelete.set(externalSystem);
    this.isDeleteDialogVisible.set(true);
  }

  protected restoreSelectedEntity(
    externalSystem: ExternalSystem | null = this.selectedEntity(),
  ): void {
    if (!externalSystem || this.hasMutationInProgress()) return;
    this.isSaving.set(true);
    this.externalSystemsService
      .reactivate(externalSystem.id)
      .pipe(
        finalize(() => this.isSaving.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.isDialogVisible.set(false);
          this.clearSelection();
          this.messageService.add({
            severity: 'success',
            summary: EXTERNAL_SYSTEM_RESTORE_SUCCESS_TITLE,
            detail: EXTERNAL_SYSTEM_RESTORE_SUCCESS_DETAIL,
          });
          this.resetAfterMutation();
        },
        error: (error: unknown) => {
          if (!isStructuredBadRequest(error)) this.showError(EXTERNAL_SYSTEM_RESTORE_ERROR_DETAIL);
        },
      });
  }

  protected submitEntity(): void {
    if (this.isSaving() || this.isEntityLoading()) return;
    if (this.entityForm.invalid) {
      this.entityForm.markAllAsTouched();
      return;
    }
    const value = this.entityForm.getRawValue();
    if (value.companyId === null) return;
    const payload: ExternalSystemInput = { name: value.name.trim(), companyId: value.companyId };
    const mode = this.dialogMode();
    const id = this.selectedEntityId();
    if (mode !== 'create' && id === null) return;
    const request =
      mode === 'create'
        ? this.externalSystemsService.create(payload)
        : this.externalSystemsService.update(id!, payload);
    this.isSaving.set(true);
    request
      .pipe(
        finalize(() => this.isSaving.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary:
              mode === 'create'
                ? EXTERNAL_SYSTEM_CREATE_SUCCESS_TITLE
                : EXTERNAL_SYSTEM_UPDATE_SUCCESS_TITLE,
            detail:
              mode === 'create'
                ? EXTERNAL_SYSTEM_CREATE_SUCCESS_DETAIL
                : EXTERNAL_SYSTEM_UPDATE_SUCCESS_DETAIL,
          });
          this.isDialogVisible.set(false);
          this.clearSelection();
          this.resetAfterMutation();
        },
        error: (error: unknown) => {
          if (!isStructuredBadRequest(error)) this.showError(EXTERNAL_SYSTEM_SAVE_ERROR_DETAIL);
        },
      });
  }

  protected closeDeleteDialog(): void {
    this.isDeleteDialogVisible.set(false);
    if (!this.isDeleting()) this.pendingDelete.set(null);
  }

  protected confirmDelete(): void {
    const externalSystem = this.pendingDelete();
    if (!externalSystem || this.isDeleting()) return;
    this.isDeleteDialogVisible.set(false);
    this.isDeleting.set(true);
    this.externalSystemsService
      .delete(externalSystem.id)
      .pipe(
        finalize(() => this.isDeleting.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.pendingDelete.set(null);
          if (this.selectedEntityId() === externalSystem.id) {
            this.isDialogVisible.set(false);
            this.clearSelection();
          }
          this.messageService.add({
            severity: 'success',
            summary: EXTERNAL_SYSTEM_DELETE_SUCCESS_TITLE,
            detail: EXTERNAL_SYSTEM_DELETE_SUCCESS_DETAIL,
          });
          this.resetAfterMutation();
        },
        error: (error: unknown) => {
          this.pendingDelete.set(null);
          if (!isStructuredBadRequest(error)) this.showError(EXTERNAL_SYSTEM_DELETE_ERROR_DETAIL);
        },
      });
  }

  protected override parseFormToFilters(): ExternalSystemFilters {
    const value = this.filtersForm.getRawValue();
    return {
      name: value.name?.trim() || null,
      companyId: value.companyId,
      status: value.status,
    };
  }

  private openExistingDialog(
    externalSystem: ExternalSystem,
    mode: Exclude<ExternalSystemDialogMode, 'create'>,
  ): void {
    this.selectedEntityId.set(externalSystem.id);
    this.selectedEntity.set(externalSystem);
    this.selectedEntityCanRestore.set(mode === 'view' && Boolean(externalSystem.deletedAt));
    this.prepareEntityForm(null, mode);
    this.dialogMode.set(mode);
    this.isDialogVisible.set(true);
    this.isEntityLoading.set(true);
    this.externalSystemsService
      .getById(externalSystem.id)
      .pipe(
        finalize(() => this.isEntityLoading.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (loaded) => {
          this.selectedEntityId.set(loaded.id);
          this.selectedEntity.set(loaded);
          this.selectedEntityCanRestore.set(mode === 'view' && Boolean(loaded.deletedAt));
          this.prepareEntityForm(loaded, mode);
        },
        error: (error: unknown) => {
          this.isDialogVisible.set(false);
          this.clearSelection();
          if (!isStructuredBadRequest(error)) this.showError(EXTERNAL_SYSTEM_LOAD_ERROR_DETAIL);
        },
      });
  }

  private prepareEntityForm(
    externalSystem: ExternalSystem | null,
    mode: ExternalSystemDialogMode,
  ): void {
    this.entityForm.enable({ emitEvent: false });
    this.entityForm.reset({
      name: externalSystem?.name ?? '',
      companyId: externalSystem?.company?.id ?? null,
    });
  }

  private observeQuickSearch(): void {
    this.quickSearchChanges
      .pipe(
        map((value) => value.trim()),
        debounceTime(QUICK_SEARCH_DEBOUNCE_MS),
        tap(() => this.isQuickSearchPending.set(false)),
        distinctUntilChanged(),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(() => {
        this.tableFirst.set(0);
        this.onSearch();
      });
  }

  private observeSearchRequests(): void {
    this.searchRequests
      .pipe(
        switchMap((params) => {
          this.isLoading.set(true);
          return this.externalSystemsService.getAll(params).pipe(
            catchError(() => {
              this.itemsList.set({ items: [], total: 0 });
              this.showError(EXTERNAL_SYSTEMS_LOAD_ERROR_DETAIL);
              return EMPTY;
            }),
            finalize(() => this.isLoading.set(false)),
          );
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((page) => {
        this.hasLoadedResults.set(true);
        this.itemsList.set({ items: page.content, total: page.totalElements });
      });
  }

  private toPageParams(
    filters: ExternalSystemFilters,
    event?: TableLazyLoadEvent,
  ): ExternalSystemPageParams {
    const first = event?.first ?? 0;
    const size = event?.rows ?? DEFAULT_PAGE_SIZE;
    const sortField = Array.isArray(event?.sortField) ? event.sortField[0] : event?.sortField;
    const sortDirection = event?.sortOrder === -1 ? 'desc' : event?.sortOrder === 1 ? 'asc' : null;
    const search = this.quickSearchTerm.trim();
    return {
      page: Math.floor(first / size),
      size,
      sort: sortField && sortDirection ? `${sortField},${sortDirection}` : undefined,
      name: filters.name || undefined,
      companyId: filters.companyId ?? undefined,
      statusId: filters.status ?? undefined,
      search: search || undefined,
    };
  }

  private resetAfterMutation(): void {
    this.filtersForm.reset();
    this.synchronizeStatusColumnSelection(true);
    this.quickSearchTerm = '';
    this.updateSearchState();
    this.isFiltersCollapsed.set(true);
    this.tableFirst.set(0);
    const table = this.externalSystemsTable();
    if (table) table.resetState();
    else this.onSearch();
  }

  private clearSelection(): void {
    this.selectedEntityId.set(null);
    this.selectedEntity.set(null);
    this.selectedEntityCanRestore.set(false);
  }

  private hasMutationInProgress(): boolean {
    return this.isSaving() || this.isDeleting() || this.isEntityLoading();
  }

  private showError(detail: string): void {
    this.messageService.add({
      severity: 'error',
      summary: EXTERNAL_SYSTEMS_LOAD_ERROR_SUMMARY,
      detail,
    });
  }
}
