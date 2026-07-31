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
import { LayerOption } from '@features/layers/layers.model';
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
  TechnologiesTable,
  TechnologyTableAction,
} from '../../components/technologies-table/technologies-table';
import {
  TechnologyDialog,
  TechnologyDialogMode,
} from '../../components/technology-dialog/technology-dialog';
import { TechnologyFiltersForm } from '../../components/technology-filters-form/technology-filters-form';
import { createTechnologyFiltersForm } from '../../forms/technology-filters-form.factory';
import { createTechnologyForm } from '../../forms/technology-form.factory';
import { TECHNOLOGIES_TABLE_COLUMNS } from '../../technologies.constants';
import {
  Technology,
  TechnologyFilters,
  TechnologyInput,
  TechnologyPageParams,
} from '../../technologies.model';
import { TechnologiesService } from '../../services/technologies.service';
import {
  TECHNOLOGIES_ADD_ARIA_LABEL,
  TECHNOLOGIES_FILTER_LAYER,
  TECHNOLOGIES_FILTER_NAME,
  TECHNOLOGIES_FILTER_STATUS,
  TECHNOLOGIES_LIST_TITLE,
  TECHNOLOGIES_LOAD_ERROR_DETAIL,
  TECHNOLOGIES_LOAD_ERROR_SUMMARY,
  TECHNOLOGIES_QUICK_SEARCH_ARIA_LABEL,
  TECHNOLOGIES_QUICK_SEARCH_PLACEHOLDER,
  TECHNOLOGY_CREATE_SUCCESS_DETAIL,
  TECHNOLOGY_CREATE_SUCCESS_TITLE,
  TECHNOLOGY_DELETE_DIALOG_CANCEL_ARIA_LABEL,
  TECHNOLOGY_DELETE_DIALOG_CANCEL_LABEL,
  TECHNOLOGY_DELETE_DIALOG_CONFIRM_ARIA_LABEL,
  TECHNOLOGY_DELETE_DIALOG_CONFIRM_LABEL,
  TECHNOLOGY_DELETE_DIALOG_MESSAGE,
  TECHNOLOGY_DELETE_DIALOG_TITLE,
  TECHNOLOGY_DELETE_ERROR_DETAIL,
  TECHNOLOGY_DELETE_SUCCESS_DETAIL,
  TECHNOLOGY_DELETE_SUCCESS_TITLE,
  TECHNOLOGY_LOAD_ERROR_DETAIL,
  TECHNOLOGY_RESTORE_ERROR_DETAIL,
  TECHNOLOGY_RESTORE_SUCCESS_DETAIL,
  TECHNOLOGY_RESTORE_SUCCESS_TITLE,
  TECHNOLOGY_SAVE_ERROR_DETAIL,
  TECHNOLOGY_UPDATE_SUCCESS_DETAIL,
  TECHNOLOGY_UPDATE_SUCCESS_TITLE,
} from './technologies-list.i18n';
import {
  TECHNOLOGIES_LIST_RESOLVE_KEY,
  TechnologiesListResolvedData,
} from './technologies-list.resolver';

const DEFAULT_PAGE_SIZE = 10;
const QUICK_SEARCH_DEBOUNCE_MS = 400;

@Component({
  selector: 'app-technologies-list',
  standalone: true,
  imports: [
    ConfirmationDialogComponent,
    SearchFiltersComponent,
    SectionActionsComponent,
    TechnologiesTable,
    TechnologyDialog,
    TechnologyFiltersForm,
  ],
  templateUrl: './technologies-list.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TechnologiesList
  extends SearchComponentBase<Technology, TechnologyFilters>
  implements OnInit
{
  layerOptions = input.required<LayerOption[]>();
  protected override readonly ALL_TABLE_COLUMNS = TECHNOLOGIES_TABLE_COLUMNS;
  protected readonly title = TECHNOLOGIES_LIST_TITLE;
  protected readonly filterLabels = {
    name: TECHNOLOGIES_FILTER_NAME,
    layer: TECHNOLOGIES_FILTER_LAYER,
    status: TECHNOLOGIES_FILTER_STATUS,
  };
  protected readonly quickSearchPlaceholder = TECHNOLOGIES_QUICK_SEARCH_PLACEHOLDER;
  protected readonly quickSearchAriaLabel = TECHNOLOGIES_QUICK_SEARCH_ARIA_LABEL;
  protected readonly addAriaLabel = TECHNOLOGIES_ADD_ARIA_LABEL;
  protected readonly deleteDialogTitle = TECHNOLOGY_DELETE_DIALOG_TITLE;
  protected readonly deleteDialogCancelLabel = TECHNOLOGY_DELETE_DIALOG_CANCEL_LABEL;
  protected readonly deleteDialogConfirmLabel = TECHNOLOGY_DELETE_DIALOG_CONFIRM_LABEL;
  protected readonly deleteDialogCancelAriaLabel = TECHNOLOGY_DELETE_DIALOG_CANCEL_ARIA_LABEL;
  protected readonly deleteDialogConfirmAriaLabel = TECHNOLOGY_DELETE_DIALOG_CONFIRM_ARIA_LABEL;

  quickSearchTerm = '';
  protected override filtersForm = createTechnologyFiltersForm(this.fb);
  protected readonly entityForm = createTechnologyForm(this.fb);
  protected readonly tableFirst = signal(0);
  protected readonly dialogMode = signal<TechnologyDialogMode>('create');
  protected readonly isDialogVisible = signal(false);
  protected readonly isEntityLoading = signal(false);
  protected readonly isSaving = signal(false);
  protected readonly isDeleteDialogVisible = signal(false);
  protected readonly isDeleting = signal(false);
  protected readonly selectedEntityCanRestore = signal(false);
  protected readonly pendingDelete = signal<Technology | null>(null);
  protected readonly deleteDialogMessage = computed(() =>
    TECHNOLOGY_DELETE_DIALOG_MESSAGE(this.pendingDelete()?.name ?? ''),
  );
  protected readonly isSearchIndicatorLoading = computed(
    () => this.isLoading() || this.isQuickSearchPending(),
  );
  protected readonly isInitialTableLoading = computed(
    () => this.isLoading() && !this.hasLoadedResults(),
  );
  protected readonly dialogLayerOptions = computed(() => {
    const options = this.layerOptions();
    const currentLayer = this.selectedEntity()?.layer;
    if (!currentLayer || options.some(({ id }) => id === currentLayer.id)) return options;
    return [
      ...options,
      { id: currentLayer.id, label: currentLayer.name?.trim() || `#${currentLayer.id}` },
    ];
  });

  private readonly selectedEntityId = signal<number | null>(null);
  private readonly selectedEntity = signal<Technology | null>(null);
  private readonly isQuickSearchPending = signal(false);
  private readonly hasLoadedResults = signal(false);
  private readonly technologiesService = inject(TechnologiesService);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);
  private readonly quickSearchChanges = new Subject<string>();
  private readonly searchRequests = new Subject<TechnologyPageParams>();
  private readonly technologiesTable = viewChild(TechnologiesTable);

  override ngOnInit(): void {
    this.observeSearchRequests();
    this.observeQuickSearch();
    super.ngOnInit();
  }

  protected override fetchFilteredList(
    filters: TechnologyFilters,
    event?: TableLazyLoadEvent,
  ): void {
    this.searchRequests.next(this.toPageParams(filters, event));
  }

  protected override initializeResults(): void {
    const resolved = this.route.snapshot.data[
      TECHNOLOGIES_LIST_RESOLVE_KEY
    ] as TechnologiesListResolvedData;
    this.updateSearchState();
    this.hasLoadedResults.set(true);
    if (resolved?.page) {
      this.itemsList.set({ items: resolved.page.content, total: resolved.page.totalElements });
    } else {
      this.itemsList.set({ items: [], total: 0 });
      if (resolved?.pageLoadFailed) this.showError(TECHNOLOGIES_LOAD_ERROR_DETAIL);
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

  protected onTableAction(event: ActionParams<Technology>): void {
    if (this.hasMutationInProgress()) return;
    switch (event.action) {
      case TechnologyTableAction.View:
        this.openExistingDialog(event.params, 'view');
        break;
      case TechnologyTableAction.Edit:
        this.openExistingDialog(event.params, 'edit');
        break;
      case TechnologyTableAction.Delete:
        this.pendingDelete.set(event.params);
        this.isDeleteDialogVisible.set(true);
        break;
      case TechnologyTableAction.Restore:
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
    const technology = this.selectedEntity();
    if (this.dialogMode() !== 'edit' || !technology || this.isSaving()) return;
    this.prepareEntityForm(technology, 'view');
    this.dialogMode.set('view');
  }

  protected deactivateSelectedEntity(): void {
    const technology = this.selectedEntity();
    if (!technology || technology.deletedAt || this.hasMutationInProgress()) return;
    this.pendingDelete.set(technology);
    this.isDeleteDialogVisible.set(true);
  }

  protected restoreSelectedEntity(
    technology: Technology | null = this.selectedEntity(),
  ): void {
    if (!technology || this.hasMutationInProgress()) return;
    this.isSaving.set(true);
    this.technologiesService
      .reactivate(technology.id)
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
            summary: TECHNOLOGY_RESTORE_SUCCESS_TITLE,
            detail: TECHNOLOGY_RESTORE_SUCCESS_DETAIL,
          });
          this.resetAfterMutation();
        },
        error: (error: unknown) => {
          if (!isStructuredBadRequest(error)) this.showError(TECHNOLOGY_RESTORE_ERROR_DETAIL);
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
    if (value.layerId === null) return;
    const payload: TechnologyInput = { name: value.name.trim(), layerId: value.layerId };
    const mode = this.dialogMode();
    const id = this.selectedEntityId();
    if (mode !== 'create' && id === null) return;
    const request =
      mode === 'create'
        ? this.technologiesService.create(payload)
        : this.technologiesService.update(id!, payload);
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
                ? TECHNOLOGY_CREATE_SUCCESS_TITLE
                : TECHNOLOGY_UPDATE_SUCCESS_TITLE,
            detail:
              mode === 'create'
                ? TECHNOLOGY_CREATE_SUCCESS_DETAIL
                : TECHNOLOGY_UPDATE_SUCCESS_DETAIL,
          });
          this.isDialogVisible.set(false);
          this.clearSelection();
          this.resetAfterMutation();
        },
        error: (error: unknown) => {
          if (!isStructuredBadRequest(error)) this.showError(TECHNOLOGY_SAVE_ERROR_DETAIL);
        },
      });
  }

  protected closeDeleteDialog(): void {
    this.isDeleteDialogVisible.set(false);
    if (!this.isDeleting()) this.pendingDelete.set(null);
  }

  protected confirmDelete(): void {
    const technology = this.pendingDelete();
    if (!technology || this.isDeleting()) return;
    this.isDeleteDialogVisible.set(false);
    this.isDeleting.set(true);
    this.technologiesService
      .delete(technology.id)
      .pipe(
        finalize(() => this.isDeleting.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.pendingDelete.set(null);
          if (this.selectedEntityId() === technology.id) {
            this.isDialogVisible.set(false);
            this.clearSelection();
          }
          this.messageService.add({
            severity: 'success',
            summary: TECHNOLOGY_DELETE_SUCCESS_TITLE,
            detail: TECHNOLOGY_DELETE_SUCCESS_DETAIL,
          });
          this.resetAfterMutation();
        },
        error: (error: unknown) => {
          this.pendingDelete.set(null);
          if (!isStructuredBadRequest(error)) this.showError(TECHNOLOGY_DELETE_ERROR_DETAIL);
        },
      });
  }

  protected override parseFormToFilters(): TechnologyFilters {
    const value = this.filtersForm.getRawValue();
    return {
      name: value.name?.trim() || null,
      layerId: value.layerId,
      status: value.status,
    };
  }

  private openExistingDialog(
    technology: Technology,
    mode: Exclude<TechnologyDialogMode, 'create'>,
  ): void {
    this.selectedEntityId.set(technology.id);
    this.selectedEntity.set(technology);
    this.selectedEntityCanRestore.set(mode === 'view' && Boolean(technology.deletedAt));
    this.prepareEntityForm(null, mode);
    this.dialogMode.set(mode);
    this.isDialogVisible.set(true);
    this.isEntityLoading.set(true);
    this.technologiesService
      .getById(technology.id)
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
          if (!isStructuredBadRequest(error)) this.showError(TECHNOLOGY_LOAD_ERROR_DETAIL);
        },
      });
  }

  private prepareEntityForm(
    technology: Technology | null,
    mode: TechnologyDialogMode,
  ): void {
    this.entityForm.enable({ emitEvent: false });
    this.entityForm.reset({
      name: technology?.name ?? '',
      layerId: technology?.layer?.id ?? null,
    });
    if (mode === 'view') this.entityForm.disable({ emitEvent: false });
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
          return this.technologiesService.getAll(params).pipe(
            catchError(() => {
              this.itemsList.set({ items: [], total: 0 });
              this.showError(TECHNOLOGIES_LOAD_ERROR_DETAIL);
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
    filters: TechnologyFilters,
    event?: TableLazyLoadEvent,
  ): TechnologyPageParams {
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
      layerId: filters.layerId ?? undefined,
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
    const table = this.technologiesTable();
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
      summary: TECHNOLOGIES_LOAD_ERROR_SUMMARY,
      detail,
    });
  }
}
