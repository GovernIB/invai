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

import { ENVIRONMENTS_TABLE_COLUMNS } from '../../environments.constants';
import {
  Environment,
  EnvironmentFilters,
  EnvironmentInput,
  EnvironmentPageParams,
} from '../../environments.model';
import {
  EnvironmentDialog,
  EnvironmentDialogMode,
} from '../../components/environment-dialog/environment-dialog';
import { EnvironmentFiltersForm } from '../../components/environment-filters-form/environment-filters-form';
import {
  EnvironmentsTable,
  EnvironmentTableAction,
} from '../../components/environments-table/environments-table';
import { createEnvironmentFiltersForm } from '../../forms/environment-filters-form.factory';
import { createEnvironmentForm } from '../../forms/environment-form.factory';
import { EnvironmentsService } from '../../services/environments.service';
import {
  ENVIRONMENTS_LIST_RESOLVE_KEY,
  EnvironmentsListResolvedData,
} from './environments-list.resolver';
import {
  ENVIRONMENTS_ADD_ARIA_LABEL,
  ENVIRONMENTS_FILTER_CODE,
  ENVIRONMENTS_FILTER_NAME,
  ENVIRONMENTS_FILTER_NAME_ES,
  ENVIRONMENTS_FILTER_STATUS,
  ENVIRONMENTS_LIST_TITLE,
  ENVIRONMENTS_LOAD_ERROR_DETAIL,
  ENVIRONMENTS_LOAD_ERROR_SUMMARY,
  ENVIRONMENTS_QUICK_SEARCH_ARIA_LABEL,
  ENVIRONMENTS_QUICK_SEARCH_PLACEHOLDER,
  ENVIRONMENT_CREATE_SUCCESS_DETAIL,
  ENVIRONMENT_CREATE_SUCCESS_TITLE,
  ENVIRONMENT_DELETE_DIALOG_CANCEL_ARIA_LABEL,
  ENVIRONMENT_DELETE_DIALOG_CANCEL_LABEL,
  ENVIRONMENT_DELETE_DIALOG_CONFIRM_ARIA_LABEL,
  ENVIRONMENT_DELETE_DIALOG_CONFIRM_LABEL,
  ENVIRONMENT_DELETE_DIALOG_MESSAGE,
  ENVIRONMENT_DELETE_DIALOG_TITLE,
  ENVIRONMENT_DELETE_ERROR_DETAIL,
  ENVIRONMENT_DELETE_SUCCESS_DETAIL,
  ENVIRONMENT_DELETE_SUCCESS_TITLE,
  ENVIRONMENT_LOAD_ERROR_DETAIL,
  ENVIRONMENT_RESTORE_ERROR_DETAIL,
  ENVIRONMENT_RESTORE_SUCCESS_DETAIL,
  ENVIRONMENT_RESTORE_SUCCESS_TITLE,
  ENVIRONMENT_SAVE_ERROR_DETAIL,
  ENVIRONMENT_UPDATE_SUCCESS_DETAIL,
  ENVIRONMENT_UPDATE_SUCCESS_TITLE,
} from './environments-list.i18n';

interface EnvironmentSearchRequest {
  params: EnvironmentPageParams;
}

const DEFAULT_PAGE_SIZE = 10;
const QUICK_SEARCH_DEBOUNCE_MS = 400;

@Component({
  standalone: true,
  selector: 'app-environments-list',
  imports: [
    ConfirmationDialogComponent,
    EnvironmentDialog,
    EnvironmentFiltersForm,
    EnvironmentsTable,
    SearchFiltersComponent,
    SectionActionsComponent,
  ],
  templateUrl: './environments-list.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EnvironmentsList
  extends SearchComponentBase<Environment, EnvironmentFilters>
  implements OnInit
{
  protected override readonly ALL_TABLE_COLUMNS = ENVIRONMENTS_TABLE_COLUMNS;
  protected readonly title = ENVIRONMENTS_LIST_TITLE;
  protected readonly filterLabels = {
    code: ENVIRONMENTS_FILTER_CODE,
    name: ENVIRONMENTS_FILTER_NAME,
    nameEs: ENVIRONMENTS_FILTER_NAME_ES,
    status: ENVIRONMENTS_FILTER_STATUS,
  };
  protected readonly quickSearchPlaceholder = ENVIRONMENTS_QUICK_SEARCH_PLACEHOLDER;
  protected readonly quickSearchAriaLabel = ENVIRONMENTS_QUICK_SEARCH_ARIA_LABEL;
  protected readonly addAriaLabel = ENVIRONMENTS_ADD_ARIA_LABEL;
  protected readonly deleteDialogTitle = ENVIRONMENT_DELETE_DIALOG_TITLE;
  protected readonly deleteDialogCancelLabel = ENVIRONMENT_DELETE_DIALOG_CANCEL_LABEL;
  protected readonly deleteDialogConfirmLabel = ENVIRONMENT_DELETE_DIALOG_CONFIRM_LABEL;
  protected readonly deleteDialogCancelAriaLabel = ENVIRONMENT_DELETE_DIALOG_CANCEL_ARIA_LABEL;
  protected readonly deleteDialogConfirmAriaLabel = ENVIRONMENT_DELETE_DIALOG_CONFIRM_ARIA_LABEL;

  quickSearchTerm = '';
  protected override filtersForm = createEnvironmentFiltersForm(this.fb);
  protected readonly entityForm = createEnvironmentForm(this.fb);
  protected readonly tableFirst = signal(0);
  protected readonly dialogMode = signal<EnvironmentDialogMode>('create');
  protected readonly isDialogVisible = signal(false);
  protected readonly isEntityLoading = signal(false);
  protected readonly isSaving = signal(false);
  protected readonly isDeleteDialogVisible = signal(false);
  protected readonly isDeleting = signal(false);
  protected readonly selectedEntityCanRestore = signal(false);
  protected readonly environmentPendingDelete = signal<Environment | null>(null);
  protected readonly deleteDialogMessage = computed(() =>
    ENVIRONMENT_DELETE_DIALOG_MESSAGE(this.environmentPendingDelete()?.code ?? ''),
  );
  private readonly selectedEntityId = signal<number | null>(null);
  private readonly selectedEntity = signal<Environment | null>(null);
  private readonly isQuickSearchPending = signal(false);
  private readonly hasLoadedResults = signal(false);
  protected readonly isSearchIndicatorLoading = computed(
    () => this.isLoading() || this.isQuickSearchPending(),
  );
  protected readonly isInitialTableLoading = computed(
    () => this.isLoading() && !this.hasLoadedResults(),
  );

  private readonly environmentsService = inject(EnvironmentsService);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);
  private readonly quickSearchChanges = new Subject<string>();
  private readonly searchRequests = new Subject<EnvironmentSearchRequest>();
  private readonly environmentsTable = viewChild(EnvironmentsTable);

  override ngOnInit(): void {
    this.observeSearchRequests();
    this.observeQuickSearch();
    super.ngOnInit();
  }

  protected override fetchFilteredList(
    filters: EnvironmentFilters,
    event?: TableLazyLoadEvent,
  ): void {
    this.searchRequests.next({ params: this.toPageParams(filters, event) });
  }

  protected override initializeResults(): void {
    const resolvedData = this.route.snapshot.data[
      ENVIRONMENTS_LIST_RESOLVE_KEY
    ] as EnvironmentsListResolvedData;

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
        summary: ENVIRONMENTS_LOAD_ERROR_SUMMARY,
        detail: ENVIRONMENTS_LOAD_ERROR_DETAIL,
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

  protected onTableAction(event: ActionParams<Environment>): void {
    if (this.hasMutationInProgress()) return;

    switch (event.action) {
      case EnvironmentTableAction.View:
        this.openExistingDialog(event.params, 'view');
        break;
      case EnvironmentTableAction.Edit:
        this.openExistingDialog(event.params, 'edit');
        break;
      case EnvironmentTableAction.Delete:
        this.environmentPendingDelete.set(event.params);
        this.isDeleteDialogVisible.set(true);
        break;
      case EnvironmentTableAction.Restore:
        this.restoreSelectedEntity(event.params);
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
    const environment = this.selectedEntity();
    if (this.dialogMode() !== 'edit' || !environment || this.isSaving()) return;

    this.prepareEntityForm(environment, 'view');
    this.dialogMode.set('view');
  }

  protected deactivateSelectedEntity(): void {
    const environment = this.selectedEntity();
    if (!environment || environment.deletedAt || this.hasMutationInProgress()) return;

    this.environmentPendingDelete.set(environment);
    this.isDeleteDialogVisible.set(true);
  }

  protected restoreSelectedEntity(
    environment: Environment | null = this.selectedEntity(),
  ): void {
    if (!environment || this.hasMutationInProgress()) return;

    this.isSaving.set(true);
    this.environmentsService
      .reactivate(environment.id)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.isSaving.set(false)),
      )
      .subscribe({
        next: () => {
          this.isDialogVisible.set(false);
          this.selectedEntityId.set(null);
          this.selectedEntity.set(null);
          this.selectedEntityCanRestore.set(false);
          this.messageService.add({
            severity: 'success',
            summary: ENVIRONMENT_RESTORE_SUCCESS_TITLE,
            detail: ENVIRONMENT_RESTORE_SUCCESS_DETAIL,
          });
          this.resetAfterMutation();
        },
        error: (error: unknown) => {
          if (isStructuredBadRequest(error)) return;
          this.showError(ENVIRONMENT_RESTORE_ERROR_DETAIL);
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
    const payload: EnvironmentInput = {
      code: value.code.trim(),
      name: value.name.trim(),
      nameEs: value.nameEs.trim(),
    };
    const mode = this.dialogMode();
    const selectedEntityId = this.selectedEntityId();
    if (mode !== 'create' && selectedEntityId === null) return;

    const request =
      mode === 'create'
        ? this.environmentsService.create(payload)
        : this.environmentsService.update(selectedEntityId!, payload);

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
              mode === 'create'
                ? ENVIRONMENT_CREATE_SUCCESS_TITLE
                : ENVIRONMENT_UPDATE_SUCCESS_TITLE,
            detail:
              mode === 'create'
                ? ENVIRONMENT_CREATE_SUCCESS_DETAIL
                : ENVIRONMENT_UPDATE_SUCCESS_DETAIL,
          });
          this.isDialogVisible.set(false);
          this.selectedEntityId.set(null);
          this.selectedEntity.set(null);
          this.selectedEntityCanRestore.set(false);
          this.resetAfterMutation();
        },
        error: (error: unknown) => {
          if (isStructuredBadRequest(error)) return;
          this.showError(ENVIRONMENT_SAVE_ERROR_DETAIL);
        },
      });
  }

  protected closeDeleteDialog(): void {
    this.isDeleteDialogVisible.set(false);
    if (!this.isDeleting()) this.environmentPendingDelete.set(null);
  }

  protected confirmDelete(): void {
    const environment = this.environmentPendingDelete();
    if (!environment || this.isDeleting()) return;

    this.isDeleteDialogVisible.set(false);
    this.isDeleting.set(true);
    this.environmentsService
      .delete(environment.id)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.isDeleting.set(false)),
      )
      .subscribe({
        next: () => {
          this.environmentPendingDelete.set(null);
          if (this.selectedEntityId() === environment.id) this.closeEntityDialog();
          this.messageService.add({
            severity: 'success',
            summary: ENVIRONMENT_DELETE_SUCCESS_TITLE,
            detail: ENVIRONMENT_DELETE_SUCCESS_DETAIL,
          });
          this.resetAfterMutation();
        },
        error: (error: unknown) => {
          this.environmentPendingDelete.set(null);
          if (isStructuredBadRequest(error)) return;
          this.showError(ENVIRONMENT_DELETE_ERROR_DETAIL);
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
    environment: Environment,
    mode: Exclude<EnvironmentDialogMode, 'create'>,
  ): void {
    this.selectedEntityId.set(environment.id);
    this.selectedEntity.set(environment);
    this.selectedEntityCanRestore.set(mode === 'view' && Boolean(environment.deletedAt));
    this.prepareEntityForm(null, mode);
    this.dialogMode.set(mode);
    this.isDialogVisible.set(true);
    this.isEntityLoading.set(true);

    this.environmentsService
      .getById(environment.id)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.isEntityLoading.set(false)),
      )
      .subscribe({
        next: (environment) => {
          this.selectedEntityId.set(environment.id);
          this.selectedEntity.set(environment);
          this.selectedEntityCanRestore.set(mode === 'view' && Boolean(environment.deletedAt));
          this.prepareEntityForm(environment, mode);
        },
        error: (error: unknown) => {
          this.isDialogVisible.set(false);
          this.selectedEntityId.set(null);
          this.selectedEntity.set(null);
          this.selectedEntityCanRestore.set(false);
          if (isStructuredBadRequest(error)) return;
          this.showError(ENVIRONMENT_LOAD_ERROR_DETAIL);
        },
      });
  }

  private prepareEntityForm(
    environment: Environment | null,
    mode: EnvironmentDialogMode,
  ): void {
    this.entityForm.enable({ emitEvent: false });
    this.entityForm.reset({
      code: environment?.code ?? '',
      name: environment?.name ?? '',
      nameEs: environment?.nameEs ?? '',
    });

  }

  private resetAfterMutation(): void {
    this.filtersForm.reset();
    this.synchronizeStatusColumnSelection(true);
    this.quickSearchTerm = '';
    this.updateSearchState();
    this.isFiltersCollapsed.set(true);
    this.tableFirst.set(0);

    const table = this.environmentsTable();
    if (table) table.resetState();
    else this.onSearch();
  }

  private hasMutationInProgress(): boolean {
    return this.isSaving() || this.isDeleting() || this.isEntityLoading();
  }

  private showError(detail: string): void {
    this.messageService.add({
      severity: 'error',
      summary: ENVIRONMENTS_LOAD_ERROR_SUMMARY,
      detail,
    });
  }

  private observeSearchRequests(): void {
    this.searchRequests
      .pipe(
        switchMap(({ params }) => {
          this.isLoading.set(true);
          return this.environmentsService.getAll(params).pipe(
            catchError(() => {
              this.itemsList.set({ items: [], total: 0 });
              this.messageService.add({
                severity: 'error',
                summary: ENVIRONMENTS_LOAD_ERROR_SUMMARY,
                detail: ENVIRONMENTS_LOAD_ERROR_DETAIL,
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

  protected override parseFormToFilters(): EnvironmentFilters {
    const value = this.filtersForm.getRawValue();

    return {
      code: value.code?.trim() ?? null,
      name: value.name?.trim() ?? null,
      nameEs: value.nameEs?.trim() ?? null,
      status: value.status,
    };
  }

  private toPageParams(
    filters: EnvironmentFilters,
    event?: TableLazyLoadEvent,
  ): EnvironmentPageParams {
    const first = event?.first ?? 0;
    const size = event?.rows ?? DEFAULT_PAGE_SIZE;
    const sortField = Array.isArray(event?.sortField) ? event.sortField[0] : event?.sortField;
    const sortDirection = event?.sortOrder === -1 ? 'desc' : event?.sortOrder === 1 ? 'asc' : null;
    const search = this.quickSearchTerm.trim();

    return {
      page: Math.floor(first / size),
      size,
      sort: sortField && sortDirection ? `${sortField},${sortDirection}` : undefined,
      code: filters.code || undefined,
      name: filters.name || undefined,
      nameEs: filters.nameEs || undefined,
      statusId: filters.status ?? undefined,
      search: search || undefined,
    };
  }
}
