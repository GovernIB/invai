import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  inject,
  input,
  LOCALE_ID,
  OnInit,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ReactiveFormsModule } from '@angular/forms';
import { ConfirmationDialogComponent } from '@components/confirmation-dialog/confirmation-dialog.component';
import { SearchFilterGridDirective } from '@components/search-filters/search-filter-grid.directive';
import { SearchFiltersComponent } from '@components/search-filters/search-filters.component';
import { SectionActionsComponent } from '@components/section-actions/section-actions.component';
import { isStructuredBadRequest } from '@core/models/api-error.model';
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
  map,
  Observable,
  Subject,
  switchMap,
  tap,
} from 'rxjs';

import {
  ApplicationDocumentSection,
  ApplicationLink,
  ApplicationLinkInput,
  ApplicationLinkPageParams,
} from '../../../../application-documentation.model';
import {
  createApplicationLinkFiltersForm,
  createApplicationLinkForm,
} from '../../../../forms/application-documentation-form.factory';
import { ApplicationDocumentSectionCatalogService } from '../../../../services/application-document-section-catalog.service';
import { ApplicationLinksService } from '../../../../services/application-links.service';
import { ApplicationDocumentationDialogMode } from './application-attachment-dialog';
import { APPLICATION_LINKS_COLUMNS } from './application-documentation.constants';
import { APPLICATION_LINKS_INITIAL_PARAMS } from './application-documentation-section.resolver';
import {
  APPLICATION_DOCUMENTATION_TEXTS,
  APPLICATION_LINKS_TEXTS,
} from './application-documentation.i18n';
import { ApplicationDocumentationAction } from './application-documentation-table-action';
import { ApplicationDocumentationOption, documentationLabel } from './application-documentation.utils';
import { ApplicationLinkDialog } from './application-link-dialog';
import { ApplicationLinksTable } from './application-links-table';

export interface ApplicationLinkFilters {
  sectionId: number | null;
}

const QUICK_SEARCH_DEBOUNCE_MS = 400;

// Links of an application: search, filter, paging, and the dialogs to add, consult, edit and
// delete them.
@Component({
  selector: 'app-application-links-list',
  standalone: true,
  imports: [
    ApplicationLinkDialog,
    ApplicationLinksTable,
    ConfirmationDialogComponent,
    FloatLabel,
    ReactiveFormsModule,
    SearchFilterGridDirective,
    SearchFiltersComponent,
    SectionActionsComponent,
    Select,
  ],
  templateUrl: './application-links-list.html',
  styleUrl: './application-documentation-list.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationLinksList
  extends SearchComponentBase<ApplicationLink, ApplicationLinkFilters>
  implements OnInit
{
  applicationId = input.required<number>();
  canManage = input(false);
  // First page read by the route resolver, with the default criteria.
  initialPage = input<SpringPage<ApplicationLink> | null>(null);

  protected override readonly ALL_TABLE_COLUMNS = APPLICATION_LINKS_COLUMNS;
  protected override filtersForm = createApplicationLinkFiltersForm(this.fb);
  protected readonly entityForm = createApplicationLinkForm(this.fb);
  protected readonly texts = APPLICATION_LINKS_TEXTS;
  protected readonly common = APPLICATION_DOCUMENTATION_TEXTS;
  protected readonly sections = signal<ApplicationDocumentSection[]>([]);
  protected readonly isSectionsLoading = signal(false);
  protected readonly sectionOptions = computed<ApplicationDocumentationOption[]>(() =>
    this.sections()
      .map((entry) => ({ value: entry.id, label: documentationLabel(entry, this.locale) }))
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
  protected readonly pendingDelete = signal<ApplicationLink | null>(null);
  protected readonly isInitialTableLoading = computed(
    () => this.isLoading() && !this.hasLoadedResults(),
  );
  protected readonly isSearchIndicatorLoading = computed(
    () => this.isLoading() || this.isQuickSearchPending(),
  );
  protected readonly deleteDialogMessage = computed(() =>
    this.texts.deleteDialogMessage(this.pendingDelete()?.title ?? ''),
  );

  private readonly selected = signal<ApplicationLink | null>(null);
  protected readonly selectedSection = computed(() => this.selected()?.section ?? null);
  private readonly locale = inject(LOCALE_ID);
  private readonly service = inject(ApplicationLinksService);
  private readonly sectionCatalog = inject(ApplicationDocumentSectionCatalogService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly quickSearchChanges = new Subject<string>();
  private readonly searchRequests = new Subject<ApplicationLinkPageParams>();
  private sectionsLoaded = false;
  private lastParams: ApplicationLinkPageParams = { ...APPLICATION_LINKS_INITIAL_PARAMS };

  override ngOnInit(): void {
    this.observeSearchRequests();
    this.observeQuickSearch();
    this.loadSections().subscribe();
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
    filters: ApplicationLinkFilters,
    event?: TableLazyLoadEvent,
  ): void {
    this.requestPage(this.toPageParams(filters, event));
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
    this.selected.set(null);
    this.entityForm.reset();
    this.dialogMode.set('create');
    this.isDialogVisible.set(true);
    if (!this.sectionsLoaded) this.loadSections().subscribe();
  }

  protected onTableAction(event: ActionParams<ApplicationLink>): void {
    const link = event.params;
    switch (event.action) {
      case ApplicationDocumentationAction.View:
        this.selected.set(link);
        this.resetEntityForm(link);
        this.dialogMode.set('view');
        this.isDialogVisible.set(true);
        break;
      case ApplicationDocumentationAction.Delete:
        this.requestDelete(link);
        break;
    }
  }

  protected startEdit(): void {
    if (this.dialogMode() === 'view' && this.canManage()) this.dialogMode.set('edit');
  }

  protected cancelEdit(): void {
    const link = this.selected();
    if (this.dialogMode() !== 'edit' || !link || this.isSaving()) return;
    this.resetEntityForm(link);
    this.dialogMode.set('view');
  }

  protected closeDialog(): void {
    if (this.isSaving()) return;
    this.selected.set(null);
    this.isDialogVisible.set(false);
  }

  protected deactivateSelected(): void {
    const link = this.selected();
    if (link) this.requestDelete(link);
  }

  protected submit(): void {
    if (this.isSaving()) return;
    if (this.entityForm.invalid) {
      this.entityForm.markAllAsTouched();
      return;
    }
    const value = this.entityForm.getRawValue();
    if (value.sectionId == null) return;
    const input: ApplicationLinkInput = {
      title: value.title.trim(),
      description: value.description.trim() || null,
      sectionId: value.sectionId,
      url: value.url.trim(),
    };
    const selected = this.selected();
    const isCreate = this.dialogMode() === 'create';
    if (!isCreate && !selected) return;
    this.isSaving.set(true);
    (isCreate
      ? this.service.createLink(this.applicationId(), input)
      : this.service.updateLink(selected!.id, input)
    )
      .pipe(
        finalize(() => this.isSaving.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.messageService.add({
            severity: 'success',
            summary: isCreate ? this.texts.createdTitle : this.texts.updatedTitle,
            detail: isCreate ? this.texts.createdDetail : this.texts.updatedDetail,
          });
          this.isDialogVisible.set(false);
          this.selected.set(null);
          this.requestPage({ ...this.lastParams });
        },
        error: (error: unknown) => {
          // The back-end messages, such as a repeated link, are shown by the error dialog.
          if (!isStructuredBadRequest(error)) this.showError(this.texts.saveError);
          // Another user may have deleted the link being edited.
          if (!isCreate) this.requestPage({ ...this.lastParams });
        },
      });
  }

  protected closeDeleteDialog(): void {
    this.isDeleteDialogVisible.set(false);
    if (!this.isDeleting()) this.pendingDelete.set(null);
  }

  protected confirmDelete(): void {
    const link = this.pendingDelete();
    if (!link || this.isDeleting()) return;
    this.isDeleteDialogVisible.set(false);
    this.isDeleting.set(true);
    this.service
      .deleteLink(link.id)
      .pipe(
        finalize(() => this.isDeleting.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.pendingDelete.set(null);
          if (this.selected()?.id === link.id) {
            this.isDialogVisible.set(false);
            this.selected.set(null);
          }
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
          // Another user may have deleted the link already.
          this.requestPage({ ...this.lastParams });
        },
      });
  }

  protected override parseFormToFilters(): ApplicationLinkFilters {
    return { sectionId: this.filtersForm.getRawValue().sectionId };
  }

  private requestDelete(link: ApplicationLink): void {
    if (!this.canManage() || this.isDeleting()) return;
    this.pendingDelete.set(link);
    this.isDeleteDialogVisible.set(true);
  }

  private resetEntityForm(link: ApplicationLink): void {
    this.entityForm.reset({
      title: link.title,
      description: link.description ?? '',
      sectionId: link.section?.id ?? null,
      url: link.url,
    });
  }

  // Sections a link can be filed under, for the filter and the dialog.
  private loadSections(): Observable<void> {
    this.isSectionsLoading.set(true);
    return this.sectionCatalog.getAll().pipe(
      map((sections) => {
        this.sectionsLoaded = true;
        this.sections.set(sections);
      }),
      catchError(() => {
        this.showError(this.texts.sectionsLoadError);
        return EMPTY;
      }),
      finalize(() => this.isSectionsLoading.set(false)),
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
          return this.service.getLinks(this.applicationId(), params).pipe(
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

  private showPage(page: SpringPage<ApplicationLink>): void {
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

  private requestPage(params: ApplicationLinkPageParams): void {
    this.lastParams = params;
    this.searchRequests.next(params);
  }

  private toPageParams(
    filters: ApplicationLinkFilters,
    event?: TableLazyLoadEvent,
  ): ApplicationLinkPageParams {
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
      quickSearch: search || undefined,
      deleted: false,
    };
  }

  private showError(detail: string): void {
    this.messageService.add({ severity: 'error', summary: this.common.errorSummary, detail });
  }
}
