import { HttpErrorResponse } from '@angular/common/http';
import { DestroyRef, Directive, OnInit, computed, inject, input, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormGroup } from '@angular/forms';
import { isStructuredBadRequest } from '@core/models/api-error.model';
import {
  CrudEntityDialogAriaLabels,
  CrudEntityDialogMode,
} from '@components/crud-entity-dialog/crud-entity-dialog';
import { ActionParams, KeyLabel } from '@models/table.model';
import { SearchComponentBase } from '@shared/classes/search-component-base';
import { TableLazyLoadEvent } from 'primeng/table';
import {
  EMPTY,
  Observable,
  Subject,
  catchError,
  debounceTime,
  distinctUntilChanged,
  finalize,
  map,
  switchMap,
  tap,
} from 'rxjs';

import { ResponsibleMaintenanceEntity, ResponsibleNamePageParams } from '../responsibles.model';
import { ResponsibleMaintenanceTableAction } from '../components/responsible-maintenance-tables/responsible-maintenance-tables';
import { RESPONSIBLE_COMMON_COPY } from '../responsibles.i18n';
import { SpringPage } from '@models/page.model';

const QUICK_SEARCH_DEBOUNCE_MS = 400;

export interface ResponsibleMaintenanceListCopy {
  title: string;
  quickSearchPlaceholder: string;
  quickSearchAriaLabel: string;
  addAriaLabel: string;
  columnsInputId: string;
  filtersButtonAriaLabel: string;
  columnsButtonAriaLabel: string;
  actions: CrudEntityDialogAriaLabels;
  deactivateTitle: string;
  deactivateMessage: (name: string) => string;
  loadError: string;
  entityLoadError: string;
  created: string;
  updated: string;
  deactivated: string;
  restored: string;
}

@Directive()
export abstract class ResponsibleMaintenanceListBase<
  TItem extends ResponsibleMaintenanceEntity,
  TInput,
  TFilters extends object,
  TPageParams extends ResponsibleNamePageParams,
  TForm extends FormGroup,
>
  extends SearchComponentBase<TItem, TFilters>
  implements OnInit
{
  protected abstract override readonly ALL_TABLE_COLUMNS: KeyLabel[];
  protected abstract override filtersForm: FormGroup;
  protected abstract readonly entityForm: TForm;
  protected abstract readonly copy: ResponsibleMaintenanceListCopy;
  initialPage = input<SpringPage<TItem> | null>(null);
  initialLoadFailed = input(false);

  protected readonly tableFirst = signal(0);
  protected readonly dialogMode = signal<CrudEntityDialogMode>('create');
  protected readonly isDialogVisible = signal(false);
  protected readonly isEntityLoading = signal(false);
  protected readonly isSaving = signal(false);
  protected readonly isDeleteDialogVisible = signal(false);
  protected readonly isDeleting = signal(false);
  protected readonly selectedEntityCanRestore = signal(false);
  protected readonly pendingDelete = signal<TItem | null>(null);
  protected readonly deleteDialogMessage = computed(() =>
    this.copy.deactivateMessage(this.entityLabel(this.pendingDelete())),
  );
  protected readonly isSearchIndicatorLoading = computed(
    () => this.isLoading() || this.isQuickSearchPending(),
  );
  protected readonly isInitialTableLoading = computed(
    () => this.isLoading() && !this.hasLoadedResults(),
  );

  protected quickSearchTerm = '';

  private readonly selectedEntityId = signal<number | null>(null);
  protected readonly selectedEntity = signal<TItem | null>(null);
  private readonly isQuickSearchPending = signal(false);
  private readonly hasLoadedResults = signal(false);
  private readonly destroyRef = inject(DestroyRef);
  private readonly quickSearchChanges = new Subject<string>();
  private readonly searchRequests = new Subject<TPageParams>();
  private lastPageEvent: TableLazyLoadEvent | undefined;

  override ngOnInit(): void {
    this.observeSearchRequests();
    this.observeQuickSearch();
    super.ngOnInit();
  }

  protected override initializeResults(): void {
    this.updateSearchState();
    const page = this.initialPage();
    if (page || this.initialLoadFailed()) {
      this.hasLoadedResults.set(true);
      this.itemsList.set({ items: page?.content ?? [], total: page?.totalElements ?? 0 });
      if (this.initialLoadFailed()) this.showError(this.copy.loadError);
      return;
    }
    this.fetchFilteredList(this.filterExport!, undefined);
  }

  protected override fetchFilteredList(filters: TFilters, event?: TableLazyLoadEvent): void {
    this.lastPageEvent = event;
    this.searchRequests.next(this.toPageParams(filters, event, this.quickSearchTerm));
  }

  protected onQuickSearchChange(value: string): void {
    this.quickSearchTerm = value;
    this.isQuickSearchPending.set(true);
    this.quickSearchChanges.next(value);
  }

  protected onFilterSearch(): void {
    this.tableFirst.set(0);
    this.lastPageEvent = undefined;
    this.applyFiltersAndSearch();
  }

  protected onPageChange(event: TableLazyLoadEvent): void {
    this.tableFirst.set(event.first ?? 0);
    this.onSearch(event);
  }

  override reset(): void {
    this.tableFirst.set(0);
    this.lastPageEvent = undefined;
    super.reset();
  }

  protected openCreateDialog(): void {
    if (this.hasMutationInProgress()) return;
    this.clearSelection();
    this.prepareEntityForm(null);
    this.dialogMode.set('create');
    this.isDialogVisible.set(true);
  }

  protected onTableAction(event: ActionParams<TItem>): void {
    if (this.hasMutationInProgress()) return;
    switch (event.action) {
      case ResponsibleMaintenanceTableAction.View:
        this.openExistingDialog(event.params, 'view');
        break;
      case ResponsibleMaintenanceTableAction.Edit:
        this.openExistingDialog(event.params, 'edit');
        break;
      case ResponsibleMaintenanceTableAction.Deactivate:
        this.pendingDelete.set(event.params);
        this.isDeleteDialogVisible.set(true);
        break;
      case ResponsibleMaintenanceTableAction.Restore:
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
    this.dialogMode.set('edit');
  }

  protected cancelEntityEdit(): void {
    const selected = this.selectedEntity();
    if (this.dialogMode() !== 'edit' || !selected || this.isSaving()) return;
    this.prepareEntityForm(selected);
    this.dialogMode.set('view');
  }

  protected deactivateSelectedEntity(): void {
    const selected = this.selectedEntity();
    if (!selected || selected.deletedAt || this.hasMutationInProgress()) return;
    this.pendingDelete.set(selected);
    this.isDeleteDialogVisible.set(true);
  }

  protected restoreSelectedEntity(item: TItem | null = this.selectedEntity()): void {
    if (!item || this.hasMutationInProgress()) return;
    this.isSaving.set(true);
    this.restoreRequest(item.id)
      .pipe(
        finalize(() => this.isSaving.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.isDialogVisible.set(false);
          this.clearSelection();
          this.showSuccess(this.copy.restored);
          this.refreshCurrentPage();
        },
        error: (error: unknown) =>
          this.showMutationError(error, RESPONSIBLE_COMMON_COPY.restoreError),
      });
  }

  protected submitEntity(): void {
    if (this.isSaving() || this.isEntityLoading()) return;
    if (this.entityForm.invalid) {
      this.entityForm.markAllAsTouched();
      return;
    }
    const mode = this.dialogMode();
    const id = this.selectedEntityId();
    if (mode !== 'create' && id === null) return;
    this.isSaving.set(true);
    const request =
      mode === 'create'
        ? this.createRequest(this.toInput())
        : this.updateRequest(id!, this.toInput());
    request
      .pipe(
        finalize(() => this.isSaving.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.isDialogVisible.set(false);
          this.clearSelection();
          this.showSuccess(mode === 'create' ? this.copy.created : this.copy.updated);
          this.refreshCurrentPage();
        },
        error: (error: unknown) => this.showMutationError(error, RESPONSIBLE_COMMON_COPY.saveError),
      });
  }

  protected closeDeleteDialog(): void {
    this.isDeleteDialogVisible.set(false);
    if (!this.isDeleting()) this.pendingDelete.set(null);
  }

  protected confirmDelete(): void {
    const item = this.pendingDelete();
    if (!item || this.isDeleting()) return;
    this.isDeleteDialogVisible.set(false);
    this.isDeleting.set(true);
    this.deactivateRequest(item.id)
      .pipe(
        finalize(() => this.isDeleting.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.pendingDelete.set(null);
          if (this.selectedEntityId() === item.id) {
            this.isDialogVisible.set(false);
            this.clearSelection();
          }
          this.showSuccess(this.copy.deactivated);
          this.refreshCurrentPage();
        },
        error: (error: unknown) => {
          this.pendingDelete.set(null);
          this.showMutationError(error, RESPONSIBLE_COMMON_COPY.deactivateError);
        },
      });
  }

  protected mutationErrorDetail(_error: unknown, fallback: string): string {
    return fallback;
  }

  protected abstract entityLabel(item: TItem | null): string;
  protected abstract prepareEntityForm(item: TItem | null): void;
  protected abstract toInput(): TInput;
  protected abstract toPageParams(
    filters: TFilters,
    event: TableLazyLoadEvent | undefined,
    quickSearch: string,
  ): TPageParams;
  protected abstract listRequest(params: TPageParams): Observable<SpringPage<TItem>>;
  protected abstract getByIdRequest(id: number): Observable<TItem>;
  protected abstract createRequest(input: TInput): Observable<TItem>;
  protected abstract updateRequest(id: number, input: TInput): Observable<TItem>;
  protected abstract deactivateRequest(id: number): Observable<void>;
  protected abstract restoreRequest(id: number): Observable<TItem>;

  private openExistingDialog(item: TItem, mode: Exclude<CrudEntityDialogMode, 'create'>): void {
    this.selectedEntityId.set(item.id);
    this.selectedEntity.set(item);
    this.selectedEntityCanRestore.set(mode === 'view' && Boolean(item.deletedAt));
    this.prepareEntityForm(item);
    this.dialogMode.set(mode);
    this.isDialogVisible.set(true);
    this.isEntityLoading.set(true);
    this.getByIdRequest(item.id)
      .pipe(
        finalize(() => this.isEntityLoading.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (loaded) => {
          this.selectedEntityId.set(loaded.id);
          this.selectedEntity.set(loaded);
          this.selectedEntityCanRestore.set(mode === 'view' && Boolean(loaded.deletedAt));
          this.prepareEntityForm(loaded);
        },
        error: () => {
          this.isDialogVisible.set(false);
          this.clearSelection();
          this.showError(this.copy.entityLoadError);
        },
      });
  }

  protected refreshCurrentPage(): void {
    const filters = this.filterExport ?? this.parseFormToFilters();
    this.fetchFilteredList(filters, this.lastPageEvent);
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
        this.lastPageEvent = undefined;
        this.onSearch();
      });
  }

  private observeSearchRequests(): void {
    this.searchRequests
      .pipe(
        switchMap((params) => {
          this.isLoading.set(true);
          return this.listRequest(params).pipe(
            catchError(() => {
              this.itemsList.set({ items: [], total: 0 });
              this.showError(this.copy.loadError);
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

  private clearSelection(): void {
    this.selectedEntityId.set(null);
    this.selectedEntity.set(null);
    this.selectedEntityCanRestore.set(false);
  }

  private hasMutationInProgress(): boolean {
    return this.isSaving() || this.isDeleting() || this.isEntityLoading();
  }

  private showSuccess(detail: string): void {
    this.messageService.add({ severity: 'success', summary: $localize`Correcte`, detail });
  }

  protected showError(detail: string): void {
    this.messageService.add({
      severity: 'error',
      summary: RESPONSIBLE_COMMON_COPY.loadErrorSummary,
      detail,
    });
  }

  private showMutationError(error: unknown, fallback: string): void {
    if (isStructuredBadRequest(error)) return;
    this.showError(
      error instanceof HttpErrorResponse && error.status === 403
        ? RESPONSIBLE_COMMON_COPY.forbidden
        : this.mutationErrorDetail(error, fallback),
    );
  }
}
