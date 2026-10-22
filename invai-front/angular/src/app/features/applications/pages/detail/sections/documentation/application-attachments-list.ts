import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  effect,
  inject,
  input,
  LOCALE_ID,
  OnInit,
  signal,
  untracked,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { APPLICATIONS_ROUTES_LOC } from '../../../../applications.routes.i18n';
import { isStructuredBadRequest } from '@core/models/api-error.model';
import { ConfirmationDialogComponent } from '@components/confirmation-dialog/confirmation-dialog.component';
import { SearchFilterGridDirective } from '@components/search-filters/search-filter-grid.directive';
import { SearchFiltersComponent } from '@components/search-filters/search-filters.component';
import { SectionActionsComponent } from '@components/section-actions/section-actions.component';
import { DocumentCategoryCatalogService } from '@features/document-categories/services/document-category-catalog.service';
import { SpringPage } from '@models/page.model';
import { ActionParams } from '@models/table.model';
import { SearchComponentBase } from '@shared/classes/search-component-base';
import { PAGINATOR_ROWS } from '@shared/constants/table.constants';
import { FloatLabel } from 'primeng/floatlabel';
import { Select } from 'primeng/select';
import { TableLazyLoadEvent } from 'primeng/table';
import {
  catchError,
  debounceTime,
  distinctUntilChanged,
  EMPTY,
  finalize,
  forkJoin,
  map,
  Observable,
  of,
  Subject,
  switchMap,
  tap,
} from 'rxjs';

import {
  APPLICATION_DOCUMENT_STATUS_CODES,
  ApplicationAttachment,
  ApplicationAttachmentPageParams,
  ApplicationDocumentCategoryOption,
  ApplicationDocumentInput,
  ApplicationDocumentMimeType,
  ApplicationDocumentSection,
  ApplicationDocumentStatus,
} from '../../../../application-documentation.model';
import {
  createApplicationAttachmentFiltersForm,
  createApplicationAttachmentForm,
} from '../../../../forms/application-documentation-form.factory';
import { ApplicationDocumentMimeTypeCatalogService } from '../../../../services/application-document-mime-type-catalog.service';
import { ApplicationDocumentSectionCatalogService } from '../../../../services/application-document-section-catalog.service';
import { ApplicationDocumentStatusCatalogService } from '../../../../services/application-document-status-catalog.service';
import { ApplicationDocumentationService } from '../../../../services/application-documentation.service';
import {
  ApplicationAttachmentDialog,
  ApplicationDocumentationDialogMode,
} from './application-attachment-dialog';
import { ApplicationAttachmentsTable } from './application-attachments-table';
import { APPLICATION_ATTACHMENTS_COLUMNS } from './application-documentation.constants';
import {
  APPLICATION_ATTACHMENTS_TEXTS,
  APPLICATION_DOCUMENTATION_TEXTS,
} from './application-documentation.i18n';
import { ApplicationDocumentationAction } from './application-documentation-table-action';
import {
  ApplicationDocumentationOption,
  documentationLabel,
  saveDocumentFile,
} from './application-documentation.utils';

// `categoryId` is the most specific category chosen: the back-end also lists the documents of the
// subcategories of a category.
export interface ApplicationAttachmentFilters {
  sectionId: number | null;
  mimeTypeId: number | null;
  categoryId: number | null;
  statusId: number | null;
}


const QUICK_SEARCH_DEBOUNCE_MS = 400;

// Attachments of an application: search, filters, paging, downloading the current version, and
// the dialogs to add and delete them; a document is consulted on its own page. The listing only
// shows the documents that are not deleted. Exporting is not available yet.
@Component({
  selector: 'app-application-attachments-list',
  standalone: true,
  imports: [
    ApplicationAttachmentDialog,
    ApplicationAttachmentsTable,
    ConfirmationDialogComponent,
    FloatLabel,
    ReactiveFormsModule,
    SearchFilterGridDirective,
    SearchFiltersComponent,
    SectionActionsComponent,
    Select,
  ],
  templateUrl: './application-attachments-list.html',
  styleUrl: './application-documentation-list.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationAttachmentsList
  extends SearchComponentBase<ApplicationAttachment, ApplicationAttachmentFilters>
  implements OnInit
{
  applicationId = input.required<number>();
  canManage = input(false);
  // First page read by the route resolver, with the default criteria.
  initialPage = input<SpringPage<ApplicationAttachment> | null>(null);

  protected override readonly ALL_TABLE_COLUMNS = APPLICATION_ATTACHMENTS_COLUMNS;
  protected override filtersForm = createApplicationAttachmentFiltersForm(this.fb);
  protected readonly texts = APPLICATION_ATTACHMENTS_TEXTS;
  protected readonly common = APPLICATION_DOCUMENTATION_TEXTS;
  protected readonly sections = signal<ApplicationDocumentSection[]>([]);
  protected readonly statuses = signal<ApplicationDocumentStatus[]>([]);
  protected readonly mimeTypes = signal<ApplicationDocumentMimeType[]>([]);
  protected readonly isCatalogsLoading = signal(false);
  protected readonly entityForm = createApplicationAttachmentForm(this.fb, () =>
    this.mimeTypes().map(({ extension }) => extension),
  );
  protected readonly sectionOptions = computed(() => this.toOptions(this.sections()));
  protected readonly stateOptions = computed(() => this.toOptions(this.statuses()));
  // Each format is named with its extension: several share a name (Word for .doc and .docx) and
  // the back-end filters by one of them.
  protected readonly mimeTypeOptions = computed<ApplicationDocumentationOption[]>(() =>
    this.mimeTypes()
      .map(({ id, name, extension }) => ({ value: id, label: `${name} (.${extension})` }))
      .sort((left, right) => left.label.localeCompare(right.label, this.locale)),
  );

  quickSearchTerm = '';
  protected readonly tableFirst = signal(0);
  protected readonly hasLoadedResults = signal(false);
  protected readonly isQuickSearchPending = signal(false);
  protected readonly dialogMode = signal<ApplicationDocumentationDialogMode>('create');
  protected readonly isDialogVisible = signal(false);
  protected readonly isSaving = signal(false);
  protected readonly isDeleting = signal(false);
  protected readonly isDeleteDialogVisible = signal(false);
  protected readonly pendingDelete = signal<ApplicationAttachment | null>(null);
  protected readonly categoryOptions = signal<ApplicationDocumentCategoryOption[]>([]);
  protected readonly isCategoriesLoading = signal(false);
  private readonly filterCategoryId = signal<number | null>(null);
  protected readonly subcategoryFilterOptions = computed(
    () => this.categoryOptions().find(({ id }) => id === this.filterCategoryId())?.subcategories ?? [],
  );
  protected readonly isInitialTableLoading = computed(
    () => this.isLoading() && !this.hasLoadedResults(),
  );
  protected readonly isSearchIndicatorLoading = computed(
    () => this.isLoading() || this.isQuickSearchPending(),
  );
  protected readonly deleteDialogMessage = computed(() =>
    this.texts.deleteDialogMessage(this.pendingDelete()?.title ?? ''),
  );

  private readonly router = inject(Router);
  private readonly locale = inject(LOCALE_ID);
  private readonly service = inject(ApplicationDocumentationService);
  private readonly catalog = inject(DocumentCategoryCatalogService);
  private readonly sectionCatalog = inject(ApplicationDocumentSectionCatalogService);
  private readonly statusCatalog = inject(ApplicationDocumentStatusCatalogService);
  private readonly mimeTypeCatalog = inject(ApplicationDocumentMimeTypeCatalogService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly quickSearchChanges = new Subject<string>();
  private readonly searchRequests = new Subject<ApplicationAttachmentPageParams>();
  private categoriesLoaded = false;
  private catalogsLoaded = false;
  private lastParams: ApplicationAttachmentPageParams = { page: 0, size: PAGINATOR_ROWS, deleted: false };

  constructor() {
    super();
    // The categories are read the first time the filters are shown, as for the add dialog.
    effect(() => {
      if (!this.isFiltersCollapsed()) untracked(() => this.loadCategories().subscribe());
    });
  }

  override ngOnInit(): void {
    this.observeSearchRequests();
    this.observeQuickSearch();
    this.observeFilterCategory();
    this.loadCatalogs().subscribe();
    super.ngOnInit();
  }

  protected override initializeResults(): void {
    const page = this.initialPage();
    if (!page) {
      this.applyFiltersAndSearch();
      return;
    }
    this.updateSearchState();
    this.showPage(page);
  }

  protected override fetchFilteredList(
    filters: ApplicationAttachmentFilters,
    event?: TableLazyLoadEvent,
  ): void {
    this.requestPage(this.toPageParams(filters, event));
  }

  protected override exportExcel(): void {
    this.messageService.add({
      severity: 'info',
      summary: this.common.infoSummary,
      detail: this.common.exportUnavailable,
    });
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
    if (!this.canManage() || this.isSaving()) return;
    this.entityForm.reset();
    this.dialogMode.set('create');
    this.isDialogVisible.set(true);
    this.loadCategories().subscribe();
    if (!this.catalogsLoaded) this.loadCatalogs().subscribe();
  }

  protected onTableAction(event: ActionParams<ApplicationAttachment>): void {
    const attachment = event.params;
    switch (event.action) {
      case ApplicationDocumentationAction.View:
        void this.router.navigate(['/', APPLICATIONS_ROUTES_LOC.BASE, this.applicationId(), 'documentation', attachment.id]);
        break;
      case ApplicationDocumentationAction.Download:
        this.download(attachment);
        break;
      case ApplicationDocumentationAction.Delete:
        this.requestDelete(attachment);
        break;
    }
  }

  protected closeDialog(): void {
    if (this.isSaving()) return;
    this.isDialogVisible.set(false);
  }

  protected submit(): void {
    if (this.isSaving()) return;
    if (this.entityForm.invalid) {
      this.entityForm.markAllAsTouched();
      return;
    }
    const value = this.entityForm.getRawValue();
    const active = this.statuses().find(({ code }) => code === APPLICATION_DOCUMENT_STATUS_CODES.active);
    if (!active) {
      this.showError(this.texts.catalogsLoadError);
      return;
    }
    if (value.sectionId == null || value.categoryId == null || !value.file) return;
    const input: ApplicationDocumentInput = {
      applicationId: this.applicationId(),
      title: value.title.trim(),
      sectionId: value.sectionId,
      categoryId: value.subcategoryId ?? value.categoryId,
      statusId: active.id,
      versionLabel: value.versionLabel.trim(),
    };
    this.isSaving.set(true);
    this.service.createDocument(input, value.file)
      .pipe(
        finalize(() => this.isSaving.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: this.texts.createdTitle,
            detail: this.texts.createdDetail,
          });
          this.isDialogVisible.set(false);
          this.requestPage({ ...this.lastParams });
        },
        error: (error: unknown) => {
          if (!isStructuredBadRequest(error)) this.showError(this.texts.saveError);
        },
      });
  }

  protected closeDeleteDialog(): void {
    this.isDeleteDialogVisible.set(false);
    if (!this.isDeleting()) this.pendingDelete.set(null);
  }

  protected confirmDelete(): void {
    const attachment = this.pendingDelete();
    if (!attachment || this.isDeleting()) return;
    this.isDeleteDialogVisible.set(false);
    this.isDeleting.set(true);
    this.service
      .deleteDocument(attachment.id)
      .pipe(
        finalize(() => this.isDeleting.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.pendingDelete.set(null);
          this.messageService.add({
            severity: 'success',
            summary: this.texts.deletedTitle,
            detail: this.texts.deletedDetail,
          });
          this.requestPage({ ...this.lastParams });
        },
        error: (error: unknown) => {
          this.pendingDelete.set(null);
          if (!isStructuredBadRequest(error)) this.showError(this.texts.deleteError);
        },
      });
  }

  protected override parseFormToFilters(): ApplicationAttachmentFilters {
    const value = this.filtersForm.getRawValue();
    return {
      sectionId: value.sectionId,
      mimeTypeId: value.mimeTypeId,
      categoryId: value.subcategoryId ?? value.categoryId,
      statusId: value.statusId,
    };
  }

  private download(attachment: ApplicationAttachment): void {
    if (attachment.currentAttachmentId == null) return;
    this.service
      .getDocumentVersionFile(attachment.id, attachment.currentAttachmentId, attachment.title)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (file) => saveDocumentFile(file),
        error: () => this.showError(this.common.downloadError),
      });
  }

  private toOptions(
    entries: ApplicationDocumentSection[] | ApplicationDocumentStatus[],
  ): ApplicationDocumentationOption[] {
    return entries
      .map((entry) => ({ value: entry.id, label: documentationLabel(entry, this.locale) }))
      .sort((left, right) => left.label.localeCompare(right.label, this.locale));
  }

  // Sections, states and file formats of the documents, for the filters and the add dialog.
  private loadCatalogs(): Observable<void> {
    this.isCatalogsLoading.set(true);
    return forkJoin({
      sections: this.sectionCatalog.getAll(),
      statuses: this.statusCatalog.getAll(),
      mimeTypes: this.mimeTypeCatalog.getAll(),
    }).pipe(
      map(({ sections, statuses, mimeTypes }) => {
        this.catalogsLoaded = true;
        this.sections.set(sections);
        this.statuses.set(statuses);
        this.mimeTypes.set(mimeTypes);
        this.entityForm.controls.file.updateValueAndValidity();
      }),
      catchError(() => {
        this.showError(this.texts.catalogsLoadError);
        return EMPTY;
      }),
      finalize(() => this.isCatalogsLoading.set(false)),
      takeUntilDestroyed(this.destroyRef),
    );
  }

  private requestDelete(attachment: ApplicationAttachment): void {
    if (!this.canManage() || this.isDeleting()) return;
    this.pendingDelete.set(attachment);
    this.isDeleteDialogVisible.set(true);
  }

  // Creation loads the active category catalog once; document consultation has its own route.
  private loadCategories(): Observable<void> {
    const categories$ = this.categoriesLoaded
      ? of(this.categoryOptions())
      : this.catalog.getActiveCategoryTree().pipe(
          tap(() => (this.categoriesLoaded = true)),
          catchError(() => {
            this.showError(this.texts.categoriesLoadError);
            return of([] as ApplicationDocumentCategoryOption[]);
          }),
        );
    this.isCategoriesLoading.set(!this.categoriesLoaded);
    return categories$.pipe(
      map((options) => {
        this.categoryOptions.set(options);
      }),
      finalize(() => this.isCategoriesLoading.set(false)),
      takeUntilDestroyed(this.destroyRef),
    );
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
          return this.service.getAttachments(this.applicationId(), params).pipe(
            catchError(() => {
              this.itemsList.set({ items: [], total: 0 });
              this.showError(this.texts.loadError);
              return EMPTY;
            }),
            finalize(() => this.isLoading.set(false)),
          );
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((page) => this.showPage(page));
  }

  private showPage(page: SpringPage<ApplicationAttachment>): void {
    // A deletion may empty the last page read; go back to the previous one.
    if (!page.content.length && page.number > 0 && page.totalElements > 0) {
      const size = this.lastParams.size ?? PAGINATOR_ROWS;
      this.tableFirst.set((page.number - 1) * size);
      this.requestPage({ ...this.lastParams, page: page.number - 1 });
      return;
    }
    this.hasLoadedResults.set(true);
    this.itemsList.set({ items: page.content, total: page.totalElements });
  }

  private requestPage(params: ApplicationAttachmentPageParams): void {
    this.lastParams = params;
    this.searchRequests.next(params);
  }

  private toPageParams(
    filters: ApplicationAttachmentFilters,
    event?: TableLazyLoadEvent,
  ): ApplicationAttachmentPageParams {
    const first = event?.first ?? 0;
    const size = event?.rows ?? PAGINATOR_ROWS;
    const sortField = Array.isArray(event?.sortField) ? event.sortField[0] : event?.sortField;
    const sortDirection = event?.sortOrder === -1 ? 'desc' : event?.sortOrder === 1 ? 'asc' : null;
    const search = this.quickSearchTerm.trim();
    return {
      page: Math.floor(first / size),
      size,
      sort: sortField && sortDirection ? `${sortField},${sortDirection}` : undefined,
      sectionId: filters.sectionId ?? undefined,
      mimeTypeId: filters.mimeTypeId ?? undefined,
      categoryId: filters.categoryId ?? undefined,
      statusId: filters.statusId ?? undefined,
      search: search || undefined,
      deleted: false,
    };
  }

  // A subcategory can only be chosen among those of the chosen category; changing the category,
  // or clearing the filters, discards it.
  private observeFilterCategory(): void {
    const { categoryId, subcategoryId } = this.filtersForm.controls;
    categoryId.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((id) => {
      this.filterCategoryId.set(id);
      subcategoryId.reset(null, { emitEvent: false });
      if (this.subcategoryFilterOptions().length) subcategoryId.enable({ emitEvent: false });
      else subcategoryId.disable({ emitEvent: false });
    });
  }

  private showError(detail: string): void {
    this.messageService.add({ severity: 'error', summary: this.common.errorSummary, detail });
  }
}
