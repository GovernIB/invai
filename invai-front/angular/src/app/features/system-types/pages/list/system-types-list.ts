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
} from 'rxjs';

import { SYSTEM_TYPES_TABLE_COLUMNS } from '../../system-types.constants';
import {
  SystemType,
  SystemTypeFilters,
  SystemTypeInput,
  SystemTypePageParams,
} from '../../system-types.model';
import {
  SystemTypeDialog,
  SystemTypeDialogMode,
} from '../../components/system-type-dialog/system-type-dialog';
import { SystemTypeFiltersForm } from '../../components/system-type-filters-form/system-type-filters-form';
import {
  SystemTypesTable,
  SystemTypeTableAction,
} from '../../components/system-types-table/system-types-table';
import { createSystemTypeFiltersForm } from '../../forms/system-type-filters-form.factory';
import { createSystemTypeForm } from '../../forms/system-type-form.factory';
import { SystemTypesService } from '../../services/system-types.service';
import {
  SYSTEM_TYPES_LIST_RESOLVE_KEY,
  SystemTypesListResolvedData,
} from './system-types-list.resolver';
import {
  SYSTEM_TYPE_CREATE_SUCCESS_DETAIL,
  SYSTEM_TYPE_CREATE_SUCCESS_TITLE,
  SYSTEM_TYPE_DELETE_DIALOG_CANCEL_ARIA_LABEL,
  SYSTEM_TYPE_DELETE_DIALOG_CANCEL_LABEL,
  SYSTEM_TYPE_DELETE_DIALOG_CONFIRM_ARIA_LABEL,
  SYSTEM_TYPE_DELETE_DIALOG_CONFIRM_LABEL,
  SYSTEM_TYPE_DELETE_DIALOG_MESSAGE,
  SYSTEM_TYPE_DELETE_DIALOG_TITLE,
  SYSTEM_TYPE_DELETE_ERROR_DETAIL,
  SYSTEM_TYPE_DELETE_SUCCESS_DETAIL,
  SYSTEM_TYPE_DELETE_SUCCESS_TITLE,
  SYSTEM_TYPE_LOAD_ERROR_DETAIL,
  SYSTEM_TYPE_SAVE_ERROR_DETAIL,
  SYSTEM_TYPE_UPDATE_SUCCESS_DETAIL,
  SYSTEM_TYPE_UPDATE_SUCCESS_TITLE,
  SYSTEM_TYPES_ADD_ARIA_LABEL,
  SYSTEM_TYPES_FILTER_NAME,
  SYSTEM_TYPES_FILTER_NAME_ES,
  SYSTEM_TYPES_LIST_TITLE,
  SYSTEM_TYPES_LOAD_ERROR_DETAIL,
  SYSTEM_TYPES_LOAD_ERROR_SUMMARY,
  SYSTEM_TYPES_QUICK_SEARCH_ARIA_LABEL,
  SYSTEM_TYPES_QUICK_SEARCH_PLACEHOLDER,
  SYSTEM_TYPES_UNSUPPORTED_SEARCH_DETAIL,
  SYSTEM_TYPES_UNSUPPORTED_SEARCH_SUMMARY,
} from './system-types-list.i18n';

interface SystemTypeSearchRequest {
  params: SystemTypePageParams;
}

const DEFAULT_PAGE_SIZE = 10;
const QUICK_SEARCH_DEBOUNCE_MS = 400;

@Component({
  standalone: true,
  selector: 'app-system-types-list',
  imports: [
    ConfirmationDialogComponent,
    SearchFiltersComponent,
    SectionActionsComponent,
    SystemTypeDialog,
    SystemTypeFiltersForm,
    SystemTypesTable,
  ],
  templateUrl: './system-types-list.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SystemTypesList
  extends SearchComponentBase<SystemType, SystemTypeFilters>
  implements OnInit
{
  protected override readonly ALL_TABLE_COLUMNS = SYSTEM_TYPES_TABLE_COLUMNS;
  protected readonly title = SYSTEM_TYPES_LIST_TITLE;
  protected readonly filterLabels = {
    name: SYSTEM_TYPES_FILTER_NAME,
    nameEs: SYSTEM_TYPES_FILTER_NAME_ES,
    status: $localize`Estat`,
  };
  protected readonly quickSearchPlaceholder = SYSTEM_TYPES_QUICK_SEARCH_PLACEHOLDER;
  protected readonly quickSearchAriaLabel = SYSTEM_TYPES_QUICK_SEARCH_ARIA_LABEL;
  protected readonly addAriaLabel = SYSTEM_TYPES_ADD_ARIA_LABEL;
  protected readonly deleteDialogTitle = SYSTEM_TYPE_DELETE_DIALOG_TITLE;
  protected readonly deleteDialogCancelLabel = SYSTEM_TYPE_DELETE_DIALOG_CANCEL_LABEL;
  protected readonly deleteDialogConfirmLabel = SYSTEM_TYPE_DELETE_DIALOG_CONFIRM_LABEL;
  protected readonly deleteDialogCancelAriaLabel =
    SYSTEM_TYPE_DELETE_DIALOG_CANCEL_ARIA_LABEL;
  protected readonly deleteDialogConfirmAriaLabel =
    SYSTEM_TYPE_DELETE_DIALOG_CONFIRM_ARIA_LABEL;

  quickSearchTerm = '';
  protected override filtersForm = createSystemTypeFiltersForm(this.fb);
  protected readonly entityForm = createSystemTypeForm(this.fb);
  protected readonly tableFirst = signal(0);
  protected readonly dialogMode = signal<SystemTypeDialogMode>('create');
  protected readonly isDialogVisible = signal(false);
  protected readonly isEntityLoading = signal(false);
  protected readonly isSaving = signal(false);
  protected readonly isDeleteDialogVisible = signal(false);
  protected readonly isDeleting = signal(false);
  protected readonly selectedEntityCanRestore = signal(false);
  protected readonly systemTypePendingDelete = signal<SystemType | null>(null);
  protected readonly deleteDialogMessage = computed(() =>
    SYSTEM_TYPE_DELETE_DIALOG_MESSAGE(this.systemTypePendingDelete()?.name ?? ''),
  );
  private readonly selectedEntityId = signal<number | null>(null);
  private readonly selectedEntity = signal<SystemType | null>(null);
  private readonly isQuickSearchPending = signal(false);
  private readonly hasLoadedResults = signal(false);
  protected readonly isSearchIndicatorLoading = computed(
    () => this.isLoading() || this.isQuickSearchPending(),
  );
  protected readonly isInitialTableLoading = computed(
    () => this.isLoading() && !this.hasLoadedResults(),
  );

  private readonly systemTypesService = inject(SystemTypesService);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);
  private readonly quickSearchChanges = new Subject<string>();
  private readonly searchRequests = new Subject<SystemTypeSearchRequest>();
  private readonly systemTypesTable = viewChild(SystemTypesTable);

  override ngOnInit(): void {
    this.observeSearchRequests();
    this.observeQuickSearch();
    this.observeUnsupportedStatus();
    super.ngOnInit();
  }

  protected override fetchFilteredList(
    _filters: SystemTypeFilters,
    event?: TableLazyLoadEvent,
  ): void {
    this.searchRequests.next({ params: this.toPageParams(event) });
  }

  protected override initializeResults(): void {
    const resolvedData = this.route.snapshot.data[
      SYSTEM_TYPES_LIST_RESOLVE_KEY
    ] as SystemTypesListResolvedData;

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
    if (resolvedData.pageLoadFailed) this.showError(SYSTEM_TYPES_LOAD_ERROR_DETAIL);
  }

  protected onQuickSearchChange(value: string): void {
    this.quickSearchTerm = value;
    this.isQuickSearchPending.set(Boolean(value.trim()));
    this.quickSearchChanges.next(value);
  }

  protected onFilterSearch(): void {
    this.updateSearchState();
    this.showUnsupportedSearch();
  }

  protected onPageChange(event: TableLazyLoadEvent): void {
    this.tableFirst.set(event.first ?? 0);
    this.onSearch(event);
  }

  override reset(): void {
    this.filtersForm.reset();
    this.synchronizeStatusColumnSelection(true);
    this.selectedFilters.set(1);
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

  protected onTableAction(event: ActionParams<SystemType>): void {
    if (this.hasMutationInProgress()) return;

    switch (event.action) {
      case SystemTypeTableAction.View:
        this.openExistingDialog(event.params, 'view');
        break;
      case SystemTypeTableAction.Edit:
        this.openExistingDialog(event.params, 'edit');
        break;
      case SystemTypeTableAction.Delete:
        this.systemTypePendingDelete.set(event.params);
        this.isDeleteDialogVisible.set(true);
        break;
      case SystemTypeTableAction.Restore:
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
    const systemType = this.selectedEntity();
    if (this.dialogMode() !== 'edit' || !systemType || this.isSaving()) return;

    this.prepareEntityForm(systemType, 'view');
    this.dialogMode.set('view');
  }

  protected deactivateSelectedEntity(): void {
    const systemType = this.selectedEntity();
    if (!systemType || systemType.deletedAt || this.hasMutationInProgress()) return;

    this.systemTypePendingDelete.set(systemType);
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
    const payload: SystemTypeInput = {
      name: value.name.trim(),
      nameEs: value.nameEs.trim(),
    };
    const mode = this.dialogMode();
    const selectedEntityId = this.selectedEntityId();
    if (mode !== 'create' && selectedEntityId === null) return;

    const request =
      mode === 'create'
        ? this.systemTypesService.create(payload)
        : this.systemTypesService.update(selectedEntityId!, payload);

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
                ? SYSTEM_TYPE_CREATE_SUCCESS_TITLE
                : SYSTEM_TYPE_UPDATE_SUCCESS_TITLE,
            detail:
              mode === 'create'
                ? SYSTEM_TYPE_CREATE_SUCCESS_DETAIL
                : SYSTEM_TYPE_UPDATE_SUCCESS_DETAIL,
          });
          this.isDialogVisible.set(false);
          this.selectedEntityId.set(null);
          this.selectedEntity.set(null);
          this.selectedEntityCanRestore.set(false);
          this.resetAfterMutation();
        },
        error: (error: unknown) => {
          if (isStructuredBadRequest(error)) return;
          this.showError(SYSTEM_TYPE_SAVE_ERROR_DETAIL);
        },
      });
  }

  protected closeDeleteDialog(): void {
    this.isDeleteDialogVisible.set(false);
    if (!this.isDeleting()) this.systemTypePendingDelete.set(null);
  }

  protected confirmDelete(): void {
    const systemType = this.systemTypePendingDelete();
    if (!systemType || this.isDeleting()) return;

    this.isDeleteDialogVisible.set(false);
    this.isDeleting.set(true);
    this.systemTypesService
      .delete(systemType.id)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.isDeleting.set(false)),
      )
      .subscribe({
        next: () => {
          this.systemTypePendingDelete.set(null);
          if (this.selectedEntityId() === systemType.id) this.closeEntityDialog();
          this.messageService.add({
            severity: 'success',
            summary: SYSTEM_TYPE_DELETE_SUCCESS_TITLE,
            detail: SYSTEM_TYPE_DELETE_SUCCESS_DETAIL,
          });
          this.resetAfterMutation();
        },
        error: (error: unknown) => {
          this.systemTypePendingDelete.set(null);
          if (isStructuredBadRequest(error)) return;
          this.showError(SYSTEM_TYPE_DELETE_ERROR_DETAIL);
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
      .subscribe((value) => {
        this.isQuickSearchPending.set(false);
        if (value) this.showUnsupportedSearch();
      });
  }

  private openExistingDialog(
    systemType: SystemType,
    mode: Exclude<SystemTypeDialogMode, 'create'>,
  ): void {
    this.selectedEntityId.set(systemType.id);
    this.selectedEntity.set(systemType);
    this.selectedEntityCanRestore.set(mode === 'view' && Boolean(systemType.deletedAt));
    this.prepareEntityForm(null, mode);
    this.dialogMode.set(mode);
    this.isDialogVisible.set(true);
    this.isEntityLoading.set(true);

    this.systemTypesService
      .getById(systemType.id)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.isEntityLoading.set(false)),
      )
      .subscribe({
        next: (systemType) => {
          this.selectedEntityId.set(systemType.id);
          this.selectedEntity.set(systemType);
          this.selectedEntityCanRestore.set(mode === 'view' && Boolean(systemType.deletedAt));
          this.prepareEntityForm(systemType, mode);
        },
        error: (error: unknown) => {
          this.isDialogVisible.set(false);
          this.selectedEntityId.set(null);
          this.selectedEntity.set(null);
          this.selectedEntityCanRestore.set(false);
          if (isStructuredBadRequest(error)) return;
          this.showError(SYSTEM_TYPE_LOAD_ERROR_DETAIL);
        },
      });
  }

  private prepareEntityForm(
    systemType: SystemType | null,
    mode: SystemTypeDialogMode,
  ): void {
    this.entityForm.enable({ emitEvent: false });
    this.entityForm.reset({
      name: systemType?.name ?? '',
      nameEs: systemType?.nameEs ?? '',
    });

    if (mode === 'view') this.entityForm.disable({ emitEvent: false });
  }

  private resetAfterMutation(): void {
    this.filtersForm.reset();
    this.synchronizeStatusColumnSelection(true);
    this.quickSearchTerm = '';
    this.updateSearchState();
    this.isFiltersCollapsed.set(true);
    this.tableFirst.set(0);

    const table = this.systemTypesTable();
    if (table) table.resetState();
    else this.onSearch();
  }

  private hasMutationInProgress(): boolean {
    return this.isSaving() || this.isDeleting() || this.isEntityLoading();
  }

  private showError(detail: string): void {
    this.messageService.add({
      severity: 'error',
      summary: SYSTEM_TYPES_LOAD_ERROR_SUMMARY,
      detail,
    });
  }

  private showUnsupportedSearch(): void {
    this.messageService.add({
      severity: 'info',
      summary: SYSTEM_TYPES_UNSUPPORTED_SEARCH_SUMMARY,
      detail: SYSTEM_TYPES_UNSUPPORTED_SEARCH_DETAIL,
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
          return this.systemTypesService.getAll(params).pipe(
            catchError(() => {
              this.itemsList.set({ items: [], total: 0 });
              this.showError(SYSTEM_TYPES_LOAD_ERROR_DETAIL);
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

  private toPageParams(event?: TableLazyLoadEvent): SystemTypePageParams {
    const first = event?.first ?? 0;
    const size = event?.rows ?? DEFAULT_PAGE_SIZE;
    const sortField = Array.isArray(event?.sortField) ? event.sortField[0] : event?.sortField;
    const sortDirection = event?.sortOrder === -1 ? 'desc' : event?.sortOrder === 1 ? 'asc' : null;

    return {
      page: Math.floor(first / size),
      size,
      sort: sortField && sortDirection ? `${sortField},${sortDirection}` : undefined,
    };
  }
}
