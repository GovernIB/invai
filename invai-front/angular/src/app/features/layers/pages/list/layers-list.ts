import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  inject,
  OnInit,
  output,
  signal,
  viewChild,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { ConfirmationDialogComponent } from '@components/confirmation-dialog/confirmation-dialog.component';
import { SearchFiltersComponent } from '@components/search-filters/search-filters.component';
import { SectionActionsComponent } from '@components/section-actions/section-actions.component';
import { isStructuredBadRequest } from '@core/models/api-error.model';
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

import { LayerDialog, LayerDialogMode } from '../../components/layer-dialog/layer-dialog';
import { LayerFiltersForm } from '../../components/layer-filters-form/layer-filters-form';
import { LayersTable, LayerTableAction } from '../../components/layers-table/layers-table';
import { createLayerFiltersForm } from '../../forms/layer-filters-form.factory';
import { createLayerForm } from '../../forms/layer-form.factory';
import { LAYERS_TABLE_COLUMNS } from '../../layers.constants';
import { Layer, LayerFilters, LayerInput, LayerPageParams } from '../../layers.model';
import { LayersService } from '../../services/layers.service';
import {
  LAYER_CREATE_SUCCESS_DETAIL,
  LAYER_CREATE_SUCCESS_TITLE,
  LAYER_DELETE_DIALOG_CANCEL_ARIA_LABEL,
  LAYER_DELETE_DIALOG_CANCEL_LABEL,
  LAYER_DELETE_DIALOG_CONFIRM_ARIA_LABEL,
  LAYER_DELETE_DIALOG_CONFIRM_LABEL,
  LAYER_DELETE_DIALOG_MESSAGE,
  LAYER_DELETE_DIALOG_TITLE,
  LAYER_DELETE_ERROR_DETAIL,
  LAYER_DELETE_SUCCESS_DETAIL,
  LAYER_DELETE_SUCCESS_TITLE,
  LAYER_LOAD_ERROR_DETAIL,
  LAYER_RESTORE_ERROR_DETAIL,
  LAYER_RESTORE_SUCCESS_DETAIL,
  LAYER_RESTORE_SUCCESS_TITLE,
  LAYER_SAVE_ERROR_DETAIL,
  LAYER_UPDATE_SUCCESS_DETAIL,
  LAYER_UPDATE_SUCCESS_TITLE,
  LAYERS_ADD_ARIA_LABEL,
  LAYERS_FILTER_NAME,
  LAYERS_FILTER_STATUS,
  LAYERS_LIST_TITLE,
  LAYERS_LOAD_ERROR_DETAIL,
  LAYERS_LOAD_ERROR_SUMMARY,
  LAYERS_QUICK_SEARCH_ARIA_LABEL,
  LAYERS_QUICK_SEARCH_PLACEHOLDER,
} from './layers-list.i18n';
import { LAYERS_LIST_RESOLVE_KEY, LayersListResolvedData } from './layers-list.resolver';

const DEFAULT_PAGE_SIZE = 10;
const QUICK_SEARCH_DEBOUNCE_MS = 400;

@Component({
  selector: 'app-layers-list',
  standalone: true,
  imports: [
    ConfirmationDialogComponent,
    LayerDialog,
    LayerFiltersForm,
    LayersTable,
    SearchFiltersComponent,
    SectionActionsComponent,
  ],
  templateUrl: './layers-list.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LayersList extends SearchComponentBase<Layer, LayerFilters> implements OnInit {
  catalogChanged = output<void>();
  protected override readonly ALL_TABLE_COLUMNS = LAYERS_TABLE_COLUMNS;
  protected readonly title = LAYERS_LIST_TITLE;
  protected readonly filterLabels = { name: LAYERS_FILTER_NAME, status: LAYERS_FILTER_STATUS };
  protected readonly quickSearchPlaceholder = LAYERS_QUICK_SEARCH_PLACEHOLDER;
  protected readonly quickSearchAriaLabel = LAYERS_QUICK_SEARCH_ARIA_LABEL;
  protected readonly addAriaLabel = LAYERS_ADD_ARIA_LABEL;
  protected readonly deleteDialogTitle = LAYER_DELETE_DIALOG_TITLE;
  protected readonly deleteDialogCancelLabel = LAYER_DELETE_DIALOG_CANCEL_LABEL;
  protected readonly deleteDialogConfirmLabel = LAYER_DELETE_DIALOG_CONFIRM_LABEL;
  protected readonly deleteDialogCancelAriaLabel = LAYER_DELETE_DIALOG_CANCEL_ARIA_LABEL;
  protected readonly deleteDialogConfirmAriaLabel = LAYER_DELETE_DIALOG_CONFIRM_ARIA_LABEL;

  quickSearchTerm = '';
  protected override filtersForm = createLayerFiltersForm(this.fb);
  protected readonly entityForm = createLayerForm(this.fb);
  protected readonly tableFirst = signal(0);
  protected readonly dialogMode = signal<LayerDialogMode>('create');
  protected readonly isDialogVisible = signal(false);
  protected readonly isEntityLoading = signal(false);
  protected readonly isSaving = signal(false);
  protected readonly isDeleteDialogVisible = signal(false);
  protected readonly isDeleting = signal(false);
  protected readonly selectedEntityCanRestore = signal(false);
  protected readonly pendingDelete = signal<Layer | null>(null);
  protected readonly deleteDialogMessage = computed(() =>
    LAYER_DELETE_DIALOG_MESSAGE(this.pendingDelete()?.name ?? ''),
  );
  protected readonly isSearchIndicatorLoading = computed(
    () => this.isLoading() || this.isQuickSearchPending(),
  );
  protected readonly isInitialTableLoading = computed(
    () => this.isLoading() && !this.hasLoadedResults(),
  );

  private readonly selectedEntityId = signal<number | null>(null);
  private readonly selectedEntity = signal<Layer | null>(null);
  private readonly isQuickSearchPending = signal(false);
  private readonly hasLoadedResults = signal(false);
  private readonly layersService = inject(LayersService);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);
  private readonly quickSearchChanges = new Subject<string>();
  private readonly searchRequests = new Subject<LayerPageParams>();
  private readonly layersTable = viewChild(LayersTable);

  override ngOnInit(): void {
    this.observeSearchRequests();
    this.observeQuickSearch();
    super.ngOnInit();
  }

  protected override fetchFilteredList(filters: LayerFilters, event?: TableLazyLoadEvent): void {
    this.searchRequests.next(this.toPageParams(filters, event));
  }

  protected override initializeResults(): void {
    const resolved = this.route.snapshot.data[LAYERS_LIST_RESOLVE_KEY] as LayersListResolvedData;
    this.updateSearchState();
    this.hasLoadedResults.set(true);
    if (resolved?.page) {
      this.itemsList.set({ items: resolved.page.content, total: resolved.page.totalElements });
    } else {
      this.itemsList.set({ items: [], total: 0 });
      if (resolved?.pageLoadFailed) this.showError(LAYERS_LOAD_ERROR_DETAIL);
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

  protected onTableAction(event: ActionParams<Layer>): void {
    if (this.hasMutationInProgress()) return;
    switch (event.action) {
      case LayerTableAction.View:
        this.openExistingDialog(event.params, 'view');
        break;
      case LayerTableAction.Edit:
        this.openExistingDialog(event.params, 'edit');
        break;
      case LayerTableAction.Delete:
        this.pendingDelete.set(event.params);
        this.isDeleteDialogVisible.set(true);
        break;
      case LayerTableAction.Restore:
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
    const layer = this.selectedEntity();
    if (this.dialogMode() !== 'edit' || !layer || this.isSaving()) return;
    this.prepareEntityForm(layer, 'view');
    this.dialogMode.set('view');
  }

  protected deactivateSelectedEntity(): void {
    const layer = this.selectedEntity();
    if (!layer || layer.deletedAt || this.hasMutationInProgress()) return;
    this.pendingDelete.set(layer);
    this.isDeleteDialogVisible.set(true);
  }

  protected restoreSelectedEntity(layer: Layer | null = this.selectedEntity()): void {
    if (!layer || this.hasMutationInProgress()) return;
    this.isSaving.set(true);
    this.layersService
      .reactivate(layer.id)
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
            summary: LAYER_RESTORE_SUCCESS_TITLE,
            detail: LAYER_RESTORE_SUCCESS_DETAIL,
          });
          this.afterMutation();
        },
        error: (error: unknown) => {
          if (!isStructuredBadRequest(error)) this.showError(LAYER_RESTORE_ERROR_DETAIL);
        },
      });
  }

  protected submitEntity(): void {
    if (this.isSaving() || this.isEntityLoading()) return;
    if (this.entityForm.invalid) {
      this.entityForm.markAllAsTouched();
      return;
    }
    const payload: LayerInput = { name: this.entityForm.getRawValue().name.trim() };
    const mode = this.dialogMode();
    const id = this.selectedEntityId();
    if (mode !== 'create' && id === null) return;
    const request =
      mode === 'create'
        ? this.layersService.create(payload)
        : this.layersService.update(id!, payload);
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
            summary: mode === 'create' ? LAYER_CREATE_SUCCESS_TITLE : LAYER_UPDATE_SUCCESS_TITLE,
            detail: mode === 'create' ? LAYER_CREATE_SUCCESS_DETAIL : LAYER_UPDATE_SUCCESS_DETAIL,
          });
          this.isDialogVisible.set(false);
          this.clearSelection();
          this.afterMutation();
        },
        error: (error: unknown) => {
          if (!isStructuredBadRequest(error)) this.showError(LAYER_SAVE_ERROR_DETAIL);
        },
      });
  }

  protected closeDeleteDialog(): void {
    this.isDeleteDialogVisible.set(false);
    if (!this.isDeleting()) this.pendingDelete.set(null);
  }

  protected confirmDelete(): void {
    const layer = this.pendingDelete();
    if (!layer || this.isDeleting()) return;
    this.isDeleteDialogVisible.set(false);
    this.isDeleting.set(true);
    this.layersService
      .delete(layer.id)
      .pipe(
        finalize(() => this.isDeleting.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.pendingDelete.set(null);
          if (this.selectedEntityId() === layer.id) {
            this.isDialogVisible.set(false);
            this.clearSelection();
          }
          this.messageService.add({
            severity: 'success',
            summary: LAYER_DELETE_SUCCESS_TITLE,
            detail: LAYER_DELETE_SUCCESS_DETAIL,
          });
          this.afterMutation();
        },
        error: (error: unknown) => {
          this.pendingDelete.set(null);
          if (!isStructuredBadRequest(error)) this.showError(LAYER_DELETE_ERROR_DETAIL);
        },
      });
  }

  protected override parseFormToFilters(): LayerFilters {
    const value = this.filtersForm.getRawValue();
    return { name: value.name?.trim() || null, status: value.status };
  }

  private openExistingDialog(layer: Layer, mode: Exclude<LayerDialogMode, 'create'>): void {
    this.selectedEntityId.set(layer.id);
    this.selectedEntity.set(layer);
    this.selectedEntityCanRestore.set(mode === 'view' && Boolean(layer.deletedAt));
    this.prepareEntityForm(null, mode);
    this.dialogMode.set(mode);
    this.isDialogVisible.set(true);
    this.isEntityLoading.set(true);
    this.layersService
      .getById(layer.id)
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
          if (!isStructuredBadRequest(error)) this.showError(LAYER_LOAD_ERROR_DETAIL);
        },
      });
  }

  private prepareEntityForm(layer: Layer | null, mode: LayerDialogMode): void {
    this.entityForm.enable({ emitEvent: false });
    this.entityForm.reset({ name: layer?.name ?? '' });
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
          return this.layersService.getAll(params).pipe(
            catchError(() => {
              this.itemsList.set({ items: [], total: 0 });
              this.showError(LAYERS_LOAD_ERROR_DETAIL);
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

  private toPageParams(filters: LayerFilters, event?: TableLazyLoadEvent): LayerPageParams {
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
      statusId: filters.status ?? undefined,
      search: search || undefined,
    };
  }

  private afterMutation(): void {
    this.catalogChanged.emit();
    this.filtersForm.reset();
    this.synchronizeStatusColumnSelection(true);
    this.quickSearchTerm = '';
    this.updateSearchState();
    this.isFiltersCollapsed.set(true);
    this.tableFirst.set(0);
    const table = this.layersTable();
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
      summary: LAYERS_LOAD_ERROR_SUMMARY,
      detail,
    });
  }
}
