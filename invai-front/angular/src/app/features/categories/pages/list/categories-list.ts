import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  inject,
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
import { ActionParams } from '@models/table.model';
import { SearchComponentBase } from '@shared/classes/search-component-base';
import {
  SOFT_DELETE_STATUS_UNSUPPORTED_DETAIL,
  SOFT_DELETE_STATUS_UNSUPPORTED_SUMMARY,
} from '@shared/constants/soft-delete-status.constants';
import { observeUnsupportedSoftDeleteStatus } from '@shared/utils/unsupported-soft-delete-status.utils';
import { showRestorePending } from '@shared/utils/soft-delete-actions.utils';
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

import { CATEGORIES_TABLE_COLUMNS } from '../../categories.constants';
import {
  Category,
  CategoryFilters,
  CategoryInput,
  CategoryPageParams,
} from '../../categories.model';
import {
  CategoriesTable,
  CategoryTableAction,
} from '../../components/categories-table/categories-table';
import {
  CategoryDialog,
  CategoryDialogMode,
} from '../../components/category-dialog/category-dialog';
import { CategoryFiltersForm } from '../../components/category-filters-form/category-filters-form';
import { createCategoryForm } from '../../forms/category-form.factory';
import { createCategoryFiltersForm } from '../../forms/category-filters-form.factory';
import { CategoriesService } from '../../services/categories.service';
import {
  CATEGORIES_LIST_RESOLVE_KEY,
  CategoriesListResolvedData,
} from './categories-list.resolver';
import {
  CATEGORIES_ADD_ARIA_LABEL,
  CATEGORIES_FILTER_NAME,
  CATEGORIES_FILTER_NAME_ES,
  CATEGORIES_LIST_TITLE,
  CATEGORIES_LOAD_ERROR_DETAIL,
  CATEGORIES_LOAD_ERROR_SUMMARY,
  CATEGORIES_QUICK_SEARCH_ARIA_LABEL,
  CATEGORIES_QUICK_SEARCH_PLACEHOLDER,
  CATEGORY_CREATE_SUCCESS_DETAIL,
  CATEGORY_CREATE_SUCCESS_TITLE,
  CATEGORY_DELETE_DIALOG_CANCEL_ARIA_LABEL,
  CATEGORY_DELETE_DIALOG_CANCEL_LABEL,
  CATEGORY_DELETE_DIALOG_CONFIRM_ARIA_LABEL,
  CATEGORY_DELETE_DIALOG_CONFIRM_LABEL,
  CATEGORY_DELETE_DIALOG_MESSAGE,
  CATEGORY_DELETE_DIALOG_TITLE,
  CATEGORY_DELETE_ERROR_DETAIL,
  CATEGORY_DELETE_SUCCESS_DETAIL,
  CATEGORY_DELETE_SUCCESS_TITLE,
  CATEGORY_LOAD_ERROR_DETAIL,
  CATEGORY_SAVE_ERROR_DETAIL,
  CATEGORY_UPDATE_SUCCESS_DETAIL,
  CATEGORY_UPDATE_SUCCESS_TITLE,
} from './categories-list.i18n';

interface CategorySearchRequest {
  params: CategoryPageParams;
}

const DEFAULT_PAGE_SIZE = 10;
const QUICK_SEARCH_DEBOUNCE_MS = 400;

@Component({
  standalone: true,
  selector: 'app-categories-list',
  imports: [
    CategoriesTable,
    CategoryDialog,
    CategoryFiltersForm,
    ConfirmationDialogComponent,
    SearchFiltersComponent,
    SectionActionsComponent,
  ],
  templateUrl: './categories-list.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CategoriesList
  extends SearchComponentBase<Category, CategoryFilters>
  implements OnInit
{
  protected override readonly ALL_TABLE_COLUMNS = CATEGORIES_TABLE_COLUMNS;
  protected readonly title = CATEGORIES_LIST_TITLE;
  protected readonly filterLabels = {
    name: CATEGORIES_FILTER_NAME,
    nameEs: CATEGORIES_FILTER_NAME_ES,
    status: $localize`Estat`,
  };
  protected readonly quickSearchPlaceholder = CATEGORIES_QUICK_SEARCH_PLACEHOLDER;
  protected readonly quickSearchAriaLabel = CATEGORIES_QUICK_SEARCH_ARIA_LABEL;
  protected readonly addAriaLabel = CATEGORIES_ADD_ARIA_LABEL;
  protected readonly deleteDialogTitle = CATEGORY_DELETE_DIALOG_TITLE;
  protected readonly deleteDialogCancelLabel = CATEGORY_DELETE_DIALOG_CANCEL_LABEL;
  protected readonly deleteDialogConfirmLabel = CATEGORY_DELETE_DIALOG_CONFIRM_LABEL;
  protected readonly deleteDialogCancelAriaLabel = CATEGORY_DELETE_DIALOG_CANCEL_ARIA_LABEL;
  protected readonly deleteDialogConfirmAriaLabel = CATEGORY_DELETE_DIALOG_CONFIRM_ARIA_LABEL;

  quickSearchTerm = '';
  protected override filtersForm = createCategoryFiltersForm(this.fb);
  protected readonly entityForm = createCategoryForm(this.fb);
  protected readonly tableFirst = signal(0);
  protected readonly dialogMode = signal<CategoryDialogMode>('create');
  protected readonly isDialogVisible = signal(false);
  protected readonly isEntityLoading = signal(false);
  protected readonly isSaving = signal(false);
  protected readonly isDeleteDialogVisible = signal(false);
  protected readonly isDeleting = signal(false);
  protected readonly selectedEntityCanRestore = signal(false);
  protected readonly categoryPendingDelete = signal<Category | null>(null);
  protected readonly deleteDialogMessage = computed(() =>
    CATEGORY_DELETE_DIALOG_MESSAGE(this.categoryPendingDelete()?.name ?? ''),
  );
  private readonly selectedEntityId = signal<number | null>(null);
  private readonly selectedEntity = signal<Category | null>(null);
  private readonly isQuickSearchPending = signal(false);
  private readonly hasLoadedResults = signal(false);
  protected readonly isSearchIndicatorLoading = computed(
    () => this.isLoading() || this.isQuickSearchPending(),
  );
  protected readonly isInitialTableLoading = computed(
    () => this.isLoading() && !this.hasLoadedResults(),
  );

  private readonly categoriesService = inject(CategoriesService);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);
  private readonly quickSearchChanges = new Subject<string>();
  private readonly searchRequests = new Subject<CategorySearchRequest>();
  private readonly categoriesTable = viewChild(CategoriesTable);

  override ngOnInit(): void {
    this.observeSearchRequests();
    this.observeQuickSearch();
    this.observeUnsupportedStatus();
    super.ngOnInit();
  }

  protected override fetchFilteredList(filters: CategoryFilters, event?: TableLazyLoadEvent): void {
    this.searchRequests.next({ params: this.toPageParams(filters, event) });
  }

  protected override initializeResults(): void {
    const resolvedData = this.route.snapshot.data[
      CATEGORIES_LIST_RESOLVE_KEY
    ] as CategoriesListResolvedData;

    this.updateSearchState();
    this.hasLoadedResults.set(true);

    if (resolvedData.page) {
      this.itemsList.set({
        items: resolvedData.page.content,
        total: resolvedData.page.totalElements,
      });
      return;
    }

    this.itemsList.set({ items: [], total: 0 });
    if (resolvedData.pageLoadFailed) {
      this.messageService.add({
        severity: 'error',
        summary: CATEGORIES_LOAD_ERROR_SUMMARY,
        detail: CATEGORIES_LOAD_ERROR_DETAIL,
      });
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

    this.selectedEntityId.set(null);
    this.selectedEntity.set(null);
    this.selectedEntityCanRestore.set(false);
    this.prepareEntityForm(null, 'create');
    this.dialogMode.set('create');
    this.isDialogVisible.set(true);
  }

  protected onTableAction(event: ActionParams<Category>): void {
    if (this.hasMutationInProgress()) return;

    switch (event.action) {
      case CategoryTableAction.View:
        this.openExistingDialog(event.params, 'view');
        break;
      case CategoryTableAction.Edit:
        this.openExistingDialog(event.params, 'edit');
        break;
      case CategoryTableAction.Delete:
        this.categoryPendingDelete.set(event.params);
        this.isDeleteDialogVisible.set(true);
        break;
      case CategoryTableAction.Restore:
        this.restoreSelectedEntity();
        break;
    }
  }

  protected closeEntityDialog(): void {
    if (this.isEntityLoading() || this.isSaving()) return;

    this.isDialogVisible.set(false);
    this.selectedEntityId.set(null);
    this.selectedEntity.set(null);
    this.selectedEntityCanRestore.set(false);
  }

  protected startEntityEdit(): void {
    if (
      this.dialogMode() !== 'view' ||
      this.selectedEntityCanRestore() ||
      this.isEntityLoading() ||
      this.isSaving()
    ) {
      return;
    }

    this.entityForm.enable({ emitEvent: false });
    this.dialogMode.set('edit');
  }

  protected cancelEntityEdit(): void {
    const category = this.selectedEntity();
    if (this.dialogMode() !== 'edit' || !category || this.isSaving()) return;

    this.prepareEntityForm(category, 'view');
    this.dialogMode.set('view');
  }

  protected deactivateSelectedEntity(): void {
    const category = this.selectedEntity();
    if (!category || category.deletedAt || this.hasMutationInProgress()) return;

    this.categoryPendingDelete.set(category);
    this.isDeleteDialogVisible.set(true);
  }

  protected restoreSelectedEntity(): void {
    showRestorePending(this.messageService);
  }

  protected submitEntity(): void {
    if (this.isSaving() || this.isEntityLoading()) return;

    if (this.entityForm.invalid) {
      this.entityForm.markAllAsTouched();
      return;
    }

    const value = this.entityForm.getRawValue();
    const payload: CategoryInput = { name: value.name.trim(), nameEs: value.nameEs.trim() };
    const mode = this.dialogMode();
    const selectedEntityId = this.selectedEntityId();
    if (mode !== 'create' && selectedEntityId === null) return;

    const request =
      mode === 'create'
        ? this.categoriesService.create(payload)
        : this.categoriesService.update(selectedEntityId!, payload);

    this.isSaving.set(true);
    request
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.isSaving.set(false)),
      )
      .subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary:
              mode === 'create' ? CATEGORY_CREATE_SUCCESS_TITLE : CATEGORY_UPDATE_SUCCESS_TITLE,
            detail:
              mode === 'create' ? CATEGORY_CREATE_SUCCESS_DETAIL : CATEGORY_UPDATE_SUCCESS_DETAIL,
          });
          this.isDialogVisible.set(false);
          this.selectedEntityId.set(null);
          this.selectedEntity.set(null);
          this.selectedEntityCanRestore.set(false);
          this.resetAfterMutation();
        },
        error: (error: unknown) => {
          if (isStructuredBadRequest(error)) return;
          this.showError(CATEGORY_SAVE_ERROR_DETAIL);
        },
      });
  }

  protected closeDeleteDialog(): void {
    this.isDeleteDialogVisible.set(false);
    if (!this.isDeleting()) this.categoryPendingDelete.set(null);
  }

  protected confirmDelete(): void {
    const category = this.categoryPendingDelete();
    if (!category || this.isDeleting()) return;

    this.isDeleteDialogVisible.set(false);
    this.isDeleting.set(true);
    this.categoriesService
      .delete(category.id)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.isDeleting.set(false)),
      )
      .subscribe({
        next: () => {
          this.categoryPendingDelete.set(null);
          if (this.selectedEntityId() === category.id) this.closeEntityDialog();
          this.messageService.add({
            severity: 'success',
            summary: CATEGORY_DELETE_SUCCESS_TITLE,
            detail: CATEGORY_DELETE_SUCCESS_DETAIL,
          });
          this.resetAfterMutation();
        },
        error: (error: unknown) => {
          this.categoryPendingDelete.set(null);
          if (isStructuredBadRequest(error)) return;
          this.showError(CATEGORY_DELETE_ERROR_DETAIL);
        },
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

  private openExistingDialog(
    category: Category,
    mode: Exclude<CategoryDialogMode, 'create'>,
  ): void {
    this.selectedEntityId.set(category.id);
    this.selectedEntity.set(category);
    this.selectedEntityCanRestore.set(mode === 'view' && Boolean(category.deletedAt));
    this.prepareEntityForm(null, mode);
    this.dialogMode.set(mode);
    this.isDialogVisible.set(true);
    this.isEntityLoading.set(true);

    this.categoriesService
      .getById(category.id)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.isEntityLoading.set(false)),
      )
      .subscribe({
        next: (category) => {
          this.selectedEntityId.set(category.id);
          this.selectedEntity.set(category);
          this.selectedEntityCanRestore.set(mode === 'view' && Boolean(category.deletedAt));
          this.prepareEntityForm(category, mode);
        },
        error: (error: unknown) => {
          this.isDialogVisible.set(false);
          this.selectedEntityId.set(null);
          this.selectedEntity.set(null);
          this.selectedEntityCanRestore.set(false);
          if (isStructuredBadRequest(error)) return;
          this.showError(CATEGORY_LOAD_ERROR_DETAIL);
        },
      });
  }

  private prepareEntityForm(category: Category | null, mode: CategoryDialogMode): void {
    this.entityForm.enable({ emitEvent: false });
    this.entityForm.reset({
      name: category?.name ?? '',
      nameEs: category?.nameEs ?? '',
    });

  }

  private resetAfterMutation(): void {
    this.filtersForm.reset();
    this.synchronizeStatusColumnSelection(true);
    this.quickSearchTerm = '';
    this.updateSearchState();
    this.isFiltersCollapsed.set(true);
    this.tableFirst.set(0);

    const table = this.categoriesTable();
    if (table) table.resetState();
    else this.onSearch();
  }

  private hasMutationInProgress(): boolean {
    return this.isSaving() || this.isDeleting() || this.isEntityLoading();
  }

  private showError(detail: string): void {
    this.messageService.add({
      severity: 'error',
      summary: CATEGORIES_LOAD_ERROR_SUMMARY,
      detail,
    });
  }

  private observeUnsupportedStatus(): void {
    observeUnsupportedSoftDeleteStatus(
      this.filtersForm.controls.status,
      this.destroyRef,
      () =>
        this.messageService.add({
          severity: 'info',
          summary: SOFT_DELETE_STATUS_UNSUPPORTED_SUMMARY,
          detail: SOFT_DELETE_STATUS_UNSUPPORTED_DETAIL,
        }),
    );
  }

  private observeSearchRequests(): void {
    this.searchRequests
      .pipe(
        switchMap(({ params }) => {
          this.isLoading.set(true);
          return this.categoriesService.getAll(params).pipe(
            catchError(() => {
              this.itemsList.set({ items: [], total: 0 });
              this.messageService.add({
                severity: 'error',
                summary: CATEGORIES_LOAD_ERROR_SUMMARY,
                detail: CATEGORIES_LOAD_ERROR_DETAIL,
              });
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

  private toPageParams(filters: CategoryFilters, event?: TableLazyLoadEvent): CategoryPageParams {
    const first = event?.first ?? 0;
    const size = event?.rows ?? DEFAULT_PAGE_SIZE;
    const sortField = Array.isArray(event?.sortField) ? event.sortField[0] : event?.sortField;
    const sortDirection = event?.sortOrder === -1 ? 'desc' : event?.sortOrder === 1 ? 'asc' : null;
    const quickSearch = this.quickSearchTerm.trim();

    return {
      page: Math.floor(first / size),
      size,
      sort: sortField && sortDirection ? `${sortField},${sortDirection}` : undefined,
      name: filters.name || undefined,
      nameEs: filters.nameEs || undefined,
      quickSearch: quickSearch || undefined,
    };
  }
}
