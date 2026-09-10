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

import { FieldDialog, FieldDialogMode } from '../../components/field-dialog/field-dialog';
import { FieldFiltersForm } from '../../components/field-filters-form/field-filters-form';
import { FieldsTable, FieldTableAction } from '../../components/fields-table/fields-table';
import { FIELDS_TABLE_COLUMNS } from '../../fields.constants';
import { Field, FieldFilters, FieldInput, FieldPageParams } from '../../fields.model';
import { createFieldFiltersForm } from '../../forms/field-filters-form.factory';
import { createFieldForm } from '../../forms/field-form.factory';
import { FieldsService } from '../../services/fields.service';
import { FIELDS_LIST_RESOLVE_KEY, FieldsListResolvedData } from './fields-list.resolver';
import {
  FIELD_CREATE_SUCCESS_DETAIL,
  FIELD_CREATE_SUCCESS_TITLE,
  FIELD_DELETE_DIALOG_CANCEL_ARIA_LABEL,
  FIELD_DELETE_DIALOG_CANCEL_LABEL,
  FIELD_DELETE_DIALOG_CONFIRM_ARIA_LABEL,
  FIELD_DELETE_DIALOG_CONFIRM_LABEL,
  FIELD_DELETE_DIALOG_MESSAGE,
  FIELD_DELETE_DIALOG_TITLE,
  FIELD_DELETE_ERROR_DETAIL,
  FIELD_DELETE_SUCCESS_DETAIL,
  FIELD_DELETE_SUCCESS_TITLE,
  FIELD_LOAD_ERROR_DETAIL,
  FIELD_SAVE_ERROR_DETAIL,
  FIELD_UPDATE_SUCCESS_DETAIL,
  FIELD_UPDATE_SUCCESS_TITLE,
  FIELDS_ADD_ARIA_LABEL,
  FIELDS_FILTER_NAME,
  FIELDS_FILTER_NAME_ES,
  FIELDS_LIST_TITLE,
  FIELDS_LOAD_ERROR_DETAIL,
  FIELDS_LOAD_ERROR_SUMMARY,
  FIELDS_QUICK_SEARCH_ARIA_LABEL,
  FIELDS_QUICK_SEARCH_PLACEHOLDER,
  FIELDS_UNSUPPORTED_SEARCH_DETAIL,
  FIELDS_UNSUPPORTED_SEARCH_SUMMARY,
} from './fields-list.i18n';

interface FieldSearchRequest {
  params: FieldPageParams;
}

const DEFAULT_PAGE_SIZE = 10;
const QUICK_SEARCH_DEBOUNCE_MS = 400;

@Component({
  standalone: true,
  selector: 'app-fields-list',
  imports: [
    ConfirmationDialogComponent,
    FieldDialog,
    FieldFiltersForm,
    FieldsTable,
    SearchFiltersComponent,
    SectionActionsComponent,
  ],
  templateUrl: './fields-list.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FieldsList extends SearchComponentBase<Field, FieldFilters> implements OnInit {
  protected override readonly ALL_TABLE_COLUMNS = FIELDS_TABLE_COLUMNS;
  protected readonly title = FIELDS_LIST_TITLE;
  protected readonly filterLabels = {
    name: FIELDS_FILTER_NAME,
    nameEs: FIELDS_FILTER_NAME_ES,
    status: $localize`Estat`,
  };
  protected readonly quickSearchPlaceholder = FIELDS_QUICK_SEARCH_PLACEHOLDER;
  protected readonly quickSearchAriaLabel = FIELDS_QUICK_SEARCH_ARIA_LABEL;
  protected readonly addAriaLabel = FIELDS_ADD_ARIA_LABEL;
  protected readonly deleteDialogTitle = FIELD_DELETE_DIALOG_TITLE;
  protected readonly deleteDialogCancelLabel = FIELD_DELETE_DIALOG_CANCEL_LABEL;
  protected readonly deleteDialogConfirmLabel = FIELD_DELETE_DIALOG_CONFIRM_LABEL;
  protected readonly deleteDialogCancelAriaLabel = FIELD_DELETE_DIALOG_CANCEL_ARIA_LABEL;
  protected readonly deleteDialogConfirmAriaLabel = FIELD_DELETE_DIALOG_CONFIRM_ARIA_LABEL;

  quickSearchTerm = '';
  protected override filtersForm = createFieldFiltersForm(this.fb);
  protected readonly entityForm = createFieldForm(this.fb);
  protected readonly tableFirst = signal(0);
  protected readonly dialogMode = signal<FieldDialogMode>('create');
  protected readonly isDialogVisible = signal(false);
  protected readonly isEntityLoading = signal(false);
  protected readonly isSaving = signal(false);
  protected readonly isDeleteDialogVisible = signal(false);
  protected readonly isDeleting = signal(false);
  protected readonly selectedEntityCanRestore = signal(false);
  protected readonly fieldPendingDelete = signal<Field | null>(null);
  protected readonly deleteDialogMessage = computed(() =>
    FIELD_DELETE_DIALOG_MESSAGE(this.fieldPendingDelete()?.name ?? ''),
  );
  private readonly selectedEntityId = signal<number | null>(null);
  private readonly selectedEntity = signal<Field | null>(null);
  private readonly isQuickSearchPending = signal(false);
  private readonly hasLoadedResults = signal(false);
  protected readonly isSearchIndicatorLoading = computed(
    () => this.isLoading() || this.isQuickSearchPending(),
  );
  protected readonly isInitialTableLoading = computed(
    () => this.isLoading() && !this.hasLoadedResults(),
  );

  private readonly fieldsService = inject(FieldsService);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);
  private readonly quickSearchChanges = new Subject<string>();
  private readonly searchRequests = new Subject<FieldSearchRequest>();
  private readonly fieldsTable = viewChild(FieldsTable);

  override ngOnInit(): void {
    this.observeSearchRequests();
    this.observeQuickSearch();
    this.observeUnsupportedStatus();
    super.ngOnInit();
  }

  protected override fetchFilteredList(_filters: FieldFilters, event?: TableLazyLoadEvent): void {
    this.searchRequests.next({ params: this.toPageParams(event) });
  }

  protected override initializeResults(): void {
    const resolvedData = this.route.snapshot.data[
      FIELDS_LIST_RESOLVE_KEY
    ] as FieldsListResolvedData;

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
    if (resolvedData.pageLoadFailed) this.showError(FIELDS_LOAD_ERROR_DETAIL);
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

  protected onTableAction(event: ActionParams<Field>): void {
    if (this.hasMutationInProgress()) return;

    switch (event.action) {
      case FieldTableAction.View:
        this.openExistingDialog(event.params, 'view');
        break;
      case FieldTableAction.Edit:
        this.openExistingDialog(event.params, 'edit');
        break;
      case FieldTableAction.Delete:
        this.fieldPendingDelete.set(event.params);
        this.isDeleteDialogVisible.set(true);
        break;
      case FieldTableAction.Restore:
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
    const field = this.selectedEntity();
    if (this.dialogMode() !== 'edit' || !field || this.isSaving()) return;

    this.prepareEntityForm(field, 'view');
    this.dialogMode.set('view');
  }

  protected deactivateSelectedEntity(): void {
    const field = this.selectedEntity();
    if (!field || field.deletedAt || this.hasMutationInProgress()) return;

    this.fieldPendingDelete.set(field);
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
    const payload: FieldInput = {
      name: value.name.trim(),
      nameEs: value.nameEs.trim(),
    };
    const mode = this.dialogMode();
    const selectedEntityId = this.selectedEntityId();
    if (mode !== 'create' && selectedEntityId === null) return;

    const request =
      mode === 'create'
        ? this.fieldsService.create(payload)
        : this.fieldsService.update(selectedEntityId!, payload);

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
              mode === 'create' ? FIELD_CREATE_SUCCESS_TITLE : FIELD_UPDATE_SUCCESS_TITLE,
            detail:
              mode === 'create' ? FIELD_CREATE_SUCCESS_DETAIL : FIELD_UPDATE_SUCCESS_DETAIL,
          });
          this.isDialogVisible.set(false);
          this.selectedEntityId.set(null);
          this.selectedEntity.set(null);
          this.selectedEntityCanRestore.set(false);
          this.resetAfterMutation();
        },
        error: (error: unknown) => {
          if (isStructuredBadRequest(error)) return;
          this.showError(FIELD_SAVE_ERROR_DETAIL);
        },
      });
  }

  protected closeDeleteDialog(): void {
    this.isDeleteDialogVisible.set(false);
    if (!this.isDeleting()) this.fieldPendingDelete.set(null);
  }

  protected confirmDelete(): void {
    const field = this.fieldPendingDelete();
    if (!field || this.isDeleting()) return;

    this.isDeleteDialogVisible.set(false);
    this.isDeleting.set(true);
    this.fieldsService
      .delete(field.id)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.isDeleting.set(false)),
      )
      .subscribe({
        next: () => {
          this.fieldPendingDelete.set(null);
          if (this.selectedEntityId() === field.id) this.closeEntityDialog();
          this.messageService.add({
            severity: 'success',
            summary: FIELD_DELETE_SUCCESS_TITLE,
            detail: FIELD_DELETE_SUCCESS_DETAIL,
          });
          this.resetAfterMutation();
        },
        error: (error: unknown) => {
          this.fieldPendingDelete.set(null);
          if (isStructuredBadRequest(error)) return;
          this.showError(FIELD_DELETE_ERROR_DETAIL);
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

  private openExistingDialog(field: Field, mode: Exclude<FieldDialogMode, 'create'>): void {
    this.selectedEntityId.set(field.id);
    this.selectedEntity.set(field);
    this.selectedEntityCanRestore.set(mode === 'view' && Boolean(field.deletedAt));
    this.prepareEntityForm(null, mode);
    this.dialogMode.set(mode);
    this.isDialogVisible.set(true);
    this.isEntityLoading.set(true);

    this.fieldsService
      .getById(field.id)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.isEntityLoading.set(false)),
      )
      .subscribe({
        next: (field) => {
          this.selectedEntityId.set(field.id);
          this.selectedEntity.set(field);
          this.selectedEntityCanRestore.set(mode === 'view' && Boolean(field.deletedAt));
          this.prepareEntityForm(field, mode);
        },
        error: (error: unknown) => {
          this.isDialogVisible.set(false);
          this.selectedEntityId.set(null);
          this.selectedEntity.set(null);
          this.selectedEntityCanRestore.set(false);
          if (isStructuredBadRequest(error)) return;
          this.showError(FIELD_LOAD_ERROR_DETAIL);
        },
      });
  }

  private prepareEntityForm(field: Field | null, mode: FieldDialogMode): void {
    this.entityForm.enable({ emitEvent: false });
    this.entityForm.reset({
      name: field?.name ?? '',
      nameEs: field?.nameEs ?? '',
    });

  }

  private resetAfterMutation(): void {
    this.filtersForm.reset();
    this.synchronizeStatusColumnSelection(true);
    this.quickSearchTerm = '';
    this.updateSearchState();
    this.isFiltersCollapsed.set(true);
    this.tableFirst.set(0);

    const table = this.fieldsTable();
    if (table) table.resetState();
    else this.onSearch();
  }

  private hasMutationInProgress(): boolean {
    return this.isSaving() || this.isDeleting() || this.isEntityLoading();
  }

  private showError(detail: string): void {
    this.messageService.add({
      severity: 'error',
      summary: FIELDS_LOAD_ERROR_SUMMARY,
      detail,
    });
  }

  private showUnsupportedSearch(): void {
    this.messageService.add({
      severity: 'info',
      summary: FIELDS_UNSUPPORTED_SEARCH_SUMMARY,
      detail: FIELDS_UNSUPPORTED_SEARCH_DETAIL,
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
          return this.fieldsService.getAll(params).pipe(
            catchError(() => {
              this.itemsList.set({ items: [], total: 0 });
              this.showError(FIELDS_LOAD_ERROR_DETAIL);
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

  private toPageParams(event?: TableLazyLoadEvent): FieldPageParams {
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
