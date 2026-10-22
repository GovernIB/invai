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
import { ActivatedRoute, Router } from '@angular/router';
import { ConfirmationDialogComponent } from '@components/confirmation-dialog/confirmation-dialog.component';
import { SearchFiltersComponent } from '@components/search-filters/search-filters.component';
import { SectionActionsComponent } from '@components/section-actions/section-actions.component';
import { isStructuredBadRequest } from '@core/models/api-error.model';
import { SpringPage } from '@models/page.model';
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
  of,
  Subject,
  switchMap,
  tap,
} from 'rxjs';

import {
  DocumentCategoriesTable,
  DocumentCategoryTableAction,
} from '../../components/document-categories-table/document-categories-table';
import {
  DocumentCategoryDialog,
  DocumentCategoryDialogMode,
  DocumentCategoryKind,
} from '../../components/document-category-dialog/document-category-dialog';
import { DocumentCategoryFiltersForm } from '../../components/document-category-filters-form/document-category-filters-form';
import {
  DOCUMENT_CATEGORIES_TABLE_COLUMNS,
  DOCUMENT_CATEGORY_EDIT_NAVIGATION,
  documentCategoryDetailLink,
} from '../../document-categories.constants';
import {
  DocumentCategory,
  DocumentCategoryFilters,
  DocumentCategoryInput,
  DocumentCategoryOption,
  DocumentCategoryPageParams,
} from '../../document-categories.model';
import { createDocumentCategoryFiltersForm } from '../../forms/document-category-filters-form.factory';
import { createDocumentCategoryForm } from '../../forms/document-category-form.factory';
import { DocumentCategoriesService } from '../../services/document-categories.service';
import { DocumentCategoryCatalogService } from '../../services/document-category-catalog.service';
import {
  DOCUMENT_CATEGORIES_ADD_ARIA_LABEL,
  DOCUMENT_CATEGORIES_ERROR_SUMMARY,
  DOCUMENT_CATEGORIES_FILTER_NAME,
  DOCUMENT_CATEGORIES_FILTER_STATUS,
  DOCUMENT_CATEGORIES_LIST_TITLE,
  DOCUMENT_CATEGORIES_LOAD_ERROR,
  DOCUMENT_CATEGORIES_QUICK_SEARCH_ARIA_LABEL,
  DOCUMENT_CATEGORIES_QUICK_SEARCH_PLACEHOLDER,
  DOCUMENT_CATEGORY_DELETE_DIALOG_CANCEL_LABEL,
  DOCUMENT_CATEGORY_DELETE_DIALOG_CONFIRM_LABEL,
  DOCUMENT_CATEGORY_OPTIONS_LOAD_ERROR,
  DOCUMENT_CATEGORY_TEXTS,
} from './document-categories-list.i18n';
import {
  DOCUMENT_CATEGORIES_LIST_RESOLVE_KEY,
  DocumentCategoriesListResolvedData,
} from './document-categories-list.resolver';

const DEFAULT_PAGE_SIZE = 10;
const QUICK_SEARCH_DEBOUNCE_MS = 400;

const kindOf = (category: DocumentCategory): DocumentCategoryKind =>
  category.parentId == null ? 'category' : 'subcategory';

// Categories and their subcategories share the dialog, the confirmations and the endpoints, except
// that a category is consulted and edited in its own detail page, where its subcategories are also
// added. After a change the current page is read again, keeping the filters and the categories
// expanded.
@Component({
  selector: 'app-document-categories-list',
  standalone: true,
  imports: [
    ConfirmationDialogComponent,
    DocumentCategoriesTable,
    DocumentCategoryDialog,
    DocumentCategoryFiltersForm,
    SearchFiltersComponent,
    SectionActionsComponent,
  ],
  templateUrl: './document-categories-list.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DocumentCategoriesList
  extends SearchComponentBase<DocumentCategory, DocumentCategoryFilters>
  implements OnInit
{
  protected override readonly ALL_TABLE_COLUMNS = DOCUMENT_CATEGORIES_TABLE_COLUMNS;
  protected readonly title = DOCUMENT_CATEGORIES_LIST_TITLE;
  protected readonly filterLabels = {
    name: DOCUMENT_CATEGORIES_FILTER_NAME,
    status: DOCUMENT_CATEGORIES_FILTER_STATUS,
  };
  protected readonly quickSearchPlaceholder = DOCUMENT_CATEGORIES_QUICK_SEARCH_PLACEHOLDER;
  protected readonly quickSearchAriaLabel = DOCUMENT_CATEGORIES_QUICK_SEARCH_ARIA_LABEL;
  protected readonly addAriaLabel = DOCUMENT_CATEGORIES_ADD_ARIA_LABEL;
  protected readonly deleteDialogCancelLabel = DOCUMENT_CATEGORY_DELETE_DIALOG_CANCEL_LABEL;
  protected readonly deleteDialogConfirmLabel = DOCUMENT_CATEGORY_DELETE_DIALOG_CONFIRM_LABEL;

  quickSearchTerm = '';
  protected override filtersForm = createDocumentCategoryFiltersForm(this.fb);
  protected readonly entityForm = createDocumentCategoryForm(this.fb);
  protected readonly tableFirst = signal(0);
  protected readonly dialogMode = signal<DocumentCategoryDialogMode>('create');
  protected readonly dialogKind = signal<DocumentCategoryKind>('category');
  protected readonly dialogCategoryLabel = signal('');
  protected readonly categoryOptions = signal<DocumentCategoryOption[]>([]);
  protected readonly isDialogVisible = signal(false);
  protected readonly isEntityLoading = signal(false);
  protected readonly isSaving = signal(false);
  protected readonly isDeleteDialogVisible = signal(false);
  protected readonly isDeleting = signal(false);
  protected readonly selectedEntityCanRestore = signal(false);
  protected readonly selectedEntityCanEdit = signal(true);
  protected readonly pendingDelete = signal<DocumentCategory | null>(null);
  protected readonly hasTextSearch = signal(false);
  protected readonly deleteTexts = computed(() => {
    const pending = this.pendingDelete();
    return DOCUMENT_CATEGORY_TEXTS[pending ? kindOf(pending) : 'category'];
  });
  protected readonly deleteDialogMessage = computed(() =>
    this.deleteTexts().deleteDialogMessage(this.pendingDelete()?.name ?? ''),
  );
  protected readonly isSearchIndicatorLoading = computed(
    () => this.isLoading() || this.isQuickSearchPending(),
  );
  protected readonly isInitialTableLoading = computed(
    () => this.isLoading() && !this.hasLoadedResults(),
  );

  private readonly selectedEntity = signal<DocumentCategory | null>(null);
  private readonly isQuickSearchPending = signal(false);
  private readonly hasLoadedResults = signal(false);
  private readonly categoriesService = inject(DocumentCategoriesService);
  private readonly catalogService = inject(DocumentCategoryCatalogService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private readonly quickSearchChanges = new Subject<string>();
  private readonly searchRequests = new Subject<DocumentCategoryPageParams>();
  private readonly table = viewChild(DocumentCategoriesTable);
  private lastPageParams: DocumentCategoryPageParams = {
    page: 0,
    size: DEFAULT_PAGE_SIZE,
    statusId: this.filtersForm.controls.status.value ?? undefined,
  };
  // Category to expand once the page read after a change of one of its subcategories arrives.
  private categoryToExpand: number | null = null;

  override ngOnInit(): void {
    this.observeSearchRequests();
    this.observeQuickSearch();
    super.ngOnInit();
  }

  protected override fetchFilteredList(
    filters: DocumentCategoryFilters,
    event?: TableLazyLoadEvent,
  ): void {
    this.requestPage(this.toPageParams(filters, event));
  }

  protected override initializeResults(): void {
    const resolved = this.route.snapshot.data[
      DOCUMENT_CATEGORIES_LIST_RESOLVE_KEY
    ] as DocumentCategoriesListResolvedData;
    this.updateSearchState();
    this.hasLoadedResults.set(true);
    if (resolved?.page) {
      this.itemsList.set({ items: resolved.page.content, total: resolved.page.totalElements });
    } else {
      this.itemsList.set({ items: [], total: 0 });
      if (resolved?.pageLoadFailed) this.showError(DOCUMENT_CATEGORIES_LOAD_ERROR);
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
    this.openNewCategoryDialog();
  }

  protected onTableAction(event: ActionParams<DocumentCategory>): void {
    if (this.hasMutationInProgress()) return;
    const row = event.params;
    switch (event.action) {
      case DocumentCategoryTableAction.View:
      case DocumentCategoryTableAction.Edit:
        if (kindOf(row) === 'category') {
          void this.router.navigate(
            documentCategoryDetailLink(row.id),
            event.action === DocumentCategoryTableAction.Edit
              ? { info: DOCUMENT_CATEGORY_EDIT_NAVIGATION }
              : undefined,
          );
        } else {
          this.openExistingDialog(row, event.action === DocumentCategoryTableAction.View ? 'view' : 'edit');
        }
        break;
      case DocumentCategoryTableAction.Delete:
        this.pendingDelete.set(row);
        this.isDeleteDialogVisible.set(true);
        break;
      case DocumentCategoryTableAction.Restore:
        this.restoreEntity(row);
        break;
    }
  }

  protected closeEntityDialog(): void {
    if (this.isEntityLoading() || this.isSaving()) return;
    this.selectedEntity.set(null);
    this.isDialogVisible.set(false);
  }

  protected startEntityEdit(): void {
    if (
      this.dialogMode() !== 'view' ||
      !this.selectedEntityCanEdit() ||
      this.hasMutationInProgress()
    ) {
      return;
    }
    this.dialogMode.set('edit');
  }

  protected cancelEntityEdit(): void {
    const entity = this.selectedEntity();
    if (this.dialogMode() !== 'edit' || !entity || this.isSaving()) return;
    this.prepareEntityForm(entity);
    this.dialogMode.set('view');
  }

  protected deactivateSelectedEntity(): void {
    const entity = this.selectedEntity();
    if (!entity || entity.deletedAt || this.hasMutationInProgress()) return;
    this.pendingDelete.set(entity);
    this.isDeleteDialogVisible.set(true);
  }

  protected restoreSelectedEntity(): void {
    const entity = this.selectedEntity();
    if (entity) this.restoreEntity(entity);
  }

  protected submitEntity(): void {
    if (this.isSaving() || this.isEntityLoading()) return;
    if (this.entityForm.invalid) {
      this.entityForm.markAllAsTouched();
      return;
    }
    const kind = this.dialogKind();
    const value = this.entityForm.getRawValue();
    const payload: DocumentCategoryInput = {
      name: value.name.trim(),
      nameEs: value.nameEs.trim(),
      parentId: kind === 'subcategory' ? value.parentId : null,
    };
    if (kind === 'subcategory' && payload.parentId == null) return;
    const mode = this.dialogMode();
    const id = this.selectedEntity()?.id;
    if (mode !== 'create' && id == null) return;
    const request =
      mode === 'create'
        ? this.categoriesService.create(payload)
        : this.categoriesService.update(id!, payload);
    const texts = DOCUMENT_CATEGORY_TEXTS[kind];
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
            summary: mode === 'create' ? texts.createdTitle : texts.updatedTitle,
            detail: mode === 'create' ? texts.createdDetail : texts.updatedDetail,
          });
          this.isDialogVisible.set(false);
          this.selectedEntity.set(null);
          this.refreshAfterMutation(payload.parentId);
        },
        error: (error: unknown) => {
          if (!isStructuredBadRequest(error)) this.showError(texts.saveError);
        },
      });
  }

  protected closeDeleteDialog(): void {
    this.isDeleteDialogVisible.set(false);
    if (!this.isDeleting()) this.pendingDelete.set(null);
  }

  protected confirmDelete(): void {
    const entity = this.pendingDelete();
    if (!entity || this.isDeleting()) return;
    const texts = DOCUMENT_CATEGORY_TEXTS[kindOf(entity)];
    this.isDeleteDialogVisible.set(false);
    this.isDeleting.set(true);
    this.categoriesService
      .delete(entity.id)
      .pipe(
        finalize(() => this.isDeleting.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.pendingDelete.set(null);
          if (this.selectedEntity()?.id === entity.id) {
            this.isDialogVisible.set(false);
            this.selectedEntity.set(null);
          }
          this.messageService.add({
            severity: 'success',
            summary: texts.deletedTitle,
            detail: texts.deletedDetail,
          });
          this.refreshAfterMutation(entity.parentId);
        },
        error: (error: unknown) => {
          this.pendingDelete.set(null);
          if (!isStructuredBadRequest(error)) this.showError(texts.deleteError);
        },
      });
  }

  protected override parseFormToFilters(): DocumentCategoryFilters {
    const value = this.filtersForm.getRawValue();
    return { name: value.name?.trim() || null, status: value.status };
  }

  private restoreEntity(entity: DocumentCategory): void {
    if (this.hasMutationInProgress()) return;
    const texts = DOCUMENT_CATEGORY_TEXTS[kindOf(entity)];
    this.isSaving.set(true);
    this.categoriesService
      .reactivate(entity.id)
      .pipe(
        finalize(() => this.isSaving.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.isDialogVisible.set(false);
          this.selectedEntity.set(null);
          this.messageService.add({
            severity: 'success',
            summary: texts.restoredTitle,
            detail: texts.restoredDetail,
          });
          this.refreshAfterMutation(entity.parentId);
        },
        error: (error: unknown) => {
          if (!isStructuredBadRequest(error)) this.showError(texts.restoreError);
        },
      });
  }

  private openNewCategoryDialog(): void {
    this.selectedEntity.set(null);
    this.selectedEntityCanRestore.set(false);
    this.selectedEntityCanEdit.set(true);
    this.dialogKind.set('category');
    this.dialogCategoryLabel.set('');
    this.entityForm.reset({ name: '', nameEs: '', parentId: null });
    this.dialogMode.set('create');
    this.isDialogVisible.set(true);
  }

  private openExistingDialog(
    row: DocumentCategory,
    mode: Exclude<DocumentCategoryDialogMode, 'create'>,
  ): void {
    const kind = kindOf(row);
    this.selectedEntity.set(row);
    this.applyEntityState(row);
    this.dialogKind.set(kind);
    this.dialogCategoryLabel.set(this.categoryName(row.parentId));
    this.entityForm.reset({ name: '', nameEs: '', parentId: row.parentId });
    this.dialogMode.set(mode);
    this.isDialogVisible.set(true);
    this.isEntityLoading.set(true);
    this.categoriesService
      .getById(row.id)
      .pipe(
        switchMap((loaded) =>
          kindOf(loaded) === 'subcategory' && !loaded.deletedAt
            ? this.loadCategoryOptions(loaded).pipe(map(() => loaded))
            : of(loaded),
        ),
        finalize(() => this.isEntityLoading.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (loaded) => {
          this.selectedEntity.set(loaded);
          this.applyEntityState(loaded);
          this.dialogCategoryLabel.set(this.categoryName(loaded.parentId));
          this.prepareEntityForm(loaded);
        },
        error: (error: unknown) => {
          this.isDialogVisible.set(false);
          this.selectedEntity.set(null);
          if (!isStructuredBadRequest(error)) this.showError(DOCUMENT_CATEGORY_TEXTS[kind].loadError);
        },
      });
  }

  // An active subcategory can move to another active category; its own one is always offered.
  private loadCategoryOptions(subcategory: DocumentCategory) {
    const current: DocumentCategoryOption = {
      id: subcategory.parentId!,
      label: this.categoryName(subcategory.parentId),
    };
    return this.catalogService.getActiveCategoryOptions().pipe(
      catchError(() => {
        this.showError(DOCUMENT_CATEGORY_OPTIONS_LOAD_ERROR);
        return of([]);
      }),
      tap((options) =>
        this.categoryOptions.set(
          options.some(({ id }) => id === current.id) ? options : [current, ...options],
        ),
      ),
    );
  }

  // An inactive record can only be restored; a subcategory, only while its category is active.
  private applyEntityState(entity: DocumentCategory): void {
    const parentInactive =
      entity.parentId != null && Boolean(this.categoryById(entity.parentId)?.deletedAt);
    this.selectedEntityCanEdit.set(!entity.deletedAt);
    this.selectedEntityCanRestore.set(Boolean(entity.deletedAt) && !parentInactive);
  }

  private prepareEntityForm(entity: DocumentCategory): void {
    this.entityForm.reset({
      name: entity.name ?? '',
      nameEs: entity.nameEs ?? '',
      parentId: entity.parentId,
    });
  }

  private categoryById(id: number): DocumentCategory | undefined {
    return this.itemsList().items.find((category) => category.id === id);
  }

  private categoryName(id: number | null): string {
    if (id == null) return '';
    return this.categoryById(id)?.name?.trim() || `#${id}`;
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
          return this.categoriesService.getAll(params).pipe(
            catchError(() => {
              this.itemsList.set({ items: [], total: 0 });
              this.showError(DOCUMENT_CATEGORIES_LOAD_ERROR);
              return EMPTY;
            }),
            finalize(() => this.isLoading.set(false)),
          );
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((page) => this.showPage(page));
  }

  private showPage(page: SpringPage<DocumentCategory>): void {
    // A change may empty the last page read; go back to the previous one.
    if (!page.content.length && page.number > 0 && page.totalElements > 0) {
      const size = this.lastPageParams.size ?? DEFAULT_PAGE_SIZE;
      this.tableFirst.set((page.number - 1) * size);
      this.requestPage({ ...this.lastPageParams, page: page.number - 1 });
      return;
    }
    this.hasLoadedResults.set(true);
    this.itemsList.set({ items: page.content, total: page.totalElements });
    if (this.categoryToExpand != null) this.table()?.expand(this.categoryToExpand);
    this.categoryToExpand = null;
  }

  private requestPage(params: DocumentCategoryPageParams): void {
    this.lastPageParams = params;
    this.hasTextSearch.set(Boolean(params.search || params.name));
    this.searchRequests.next(params);
  }

  private toPageParams(
    filters: DocumentCategoryFilters,
    event?: TableLazyLoadEvent,
  ): DocumentCategoryPageParams {
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

  // Reads the current page again, with the same filters, and shows the subcategories of the
  // category whose subcategory changed.
  private refreshAfterMutation(categoryId: number | null): void {
    this.categoryToExpand = categoryId;
    this.requestPage({ ...this.lastPageParams });
  }

  private hasMutationInProgress(): boolean {
    return this.isSaving() || this.isDeleting() || this.isEntityLoading();
  }

  private showError(detail: string): void {
    this.messageService.add({
      severity: 'error',
      summary: DOCUMENT_CATEGORIES_ERROR_SUMMARY,
      detail,
    });
  }
}
