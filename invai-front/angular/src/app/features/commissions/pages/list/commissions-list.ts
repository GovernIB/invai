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

import { COMMISSIONS_TABLE_COLUMNS } from '../../commissions.constants';
import {
  Commission,
  CommissionFilters,
  CommissionInput,
  CommissionPageParams,
} from '../../commissions.model';
import {
  CommissionDialog,
  CommissionDialogMode,
} from '../../components/commission-dialog/commission-dialog';
import { CommissionFiltersForm } from '../../components/commission-filters-form/commission-filters-form';
import {
  CommissionsTable,
  CommissionTableAction,
} from '../../components/commissions-table/commissions-table';
import { formatCommissionDate, parseCommissionDate } from '../../forms/commission-date.utils';
import { createCommissionForm } from '../../forms/commission-form.factory';
import { createCommissionFiltersForm } from '../../forms/commission-filters-form.factory';
import { CommissionsService } from '../../services/commissions.service';
import {
  COMMISSIONS_LIST_RESOLVE_KEY,
  CommissionsListResolvedData,
} from './commissions-list.resolver';
import {
  COMMISSIONS_ADD_ARIA_LABEL,
  COMMISSIONS_FILTER_APPROVAL_DATE_FROM,
  COMMISSIONS_FILTER_APPROVAL_DATE_TO,
  COMMISSIONS_FILTER_EXPEDIENT_NUMBER,
  COMMISSIONS_FILTER_INVALID_DATE_RANGE,
  COMMISSIONS_FILTER_NAME,
  COMMISSIONS_FILTER_NAME_ES,
  COMMISSIONS_FILTER_TYPE,
  COMMISSIONS_LIST_TITLE,
  COMMISSIONS_LOAD_ERROR_DETAIL,
  COMMISSIONS_LOAD_ERROR_SUMMARY,
  COMMISSIONS_QUICK_SEARCH_ARIA_LABEL,
  COMMISSIONS_QUICK_SEARCH_PLACEHOLDER,
  COMMISSION_CREATE_SUCCESS_DETAIL,
  COMMISSION_CREATE_SUCCESS_TITLE,
  COMMISSION_DELETE_DIALOG_CANCEL_ARIA_LABEL,
  COMMISSION_DELETE_DIALOG_CANCEL_LABEL,
  COMMISSION_DELETE_DIALOG_CONFIRM_ARIA_LABEL,
  COMMISSION_DELETE_DIALOG_CONFIRM_LABEL,
  COMMISSION_DELETE_DIALOG_MESSAGE,
  COMMISSION_DELETE_DIALOG_TITLE,
  COMMISSION_DELETE_ERROR_DETAIL,
  COMMISSION_DELETE_SUCCESS_DETAIL,
  COMMISSION_DELETE_SUCCESS_TITLE,
  COMMISSION_LOAD_ERROR_DETAIL,
  COMMISSION_SAVE_ERROR_DETAIL,
  COMMISSION_UPDATE_SUCCESS_DETAIL,
  COMMISSION_UPDATE_SUCCESS_TITLE,
} from './commissions-list.i18n';

interface CommissionSearchRequest {
  params: CommissionPageParams;
}

const DEFAULT_PAGE_SIZE = 10;
const QUICK_SEARCH_DEBOUNCE_MS = 400;

@Component({
  standalone: true,
  selector: 'app-commissions-list',
  imports: [
    CommissionDialog,
    CommissionFiltersForm,
    CommissionsTable,
    ConfirmationDialogComponent,
    SearchFiltersComponent,
    SectionActionsComponent,
  ],
  templateUrl: './commissions-list.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CommissionsList
  extends SearchComponentBase<Commission, CommissionFilters>
  implements OnInit
{
  protected override readonly ALL_TABLE_COLUMNS = COMMISSIONS_TABLE_COLUMNS;
  protected readonly title = COMMISSIONS_LIST_TITLE;
  protected readonly filterLabels = {
    name: COMMISSIONS_FILTER_NAME,
    nameEs: COMMISSIONS_FILTER_NAME_ES,
    expedientNumber: COMMISSIONS_FILTER_EXPEDIENT_NUMBER,
    approvalDateFrom: COMMISSIONS_FILTER_APPROVAL_DATE_FROM,
    approvalDateTo: COMMISSIONS_FILTER_APPROVAL_DATE_TO,
    commissionType: COMMISSIONS_FILTER_TYPE,
    status: $localize`Estat`,
    invalidDateRange: COMMISSIONS_FILTER_INVALID_DATE_RANGE,
  };
  protected readonly quickSearchPlaceholder = COMMISSIONS_QUICK_SEARCH_PLACEHOLDER;
  protected readonly quickSearchAriaLabel = COMMISSIONS_QUICK_SEARCH_ARIA_LABEL;
  protected readonly addAriaLabel = COMMISSIONS_ADD_ARIA_LABEL;
  protected readonly deleteDialogTitle = COMMISSION_DELETE_DIALOG_TITLE;
  protected readonly deleteDialogCancelLabel = COMMISSION_DELETE_DIALOG_CANCEL_LABEL;
  protected readonly deleteDialogConfirmLabel = COMMISSION_DELETE_DIALOG_CONFIRM_LABEL;
  protected readonly deleteDialogCancelAriaLabel = COMMISSION_DELETE_DIALOG_CANCEL_ARIA_LABEL;
  protected readonly deleteDialogConfirmAriaLabel = COMMISSION_DELETE_DIALOG_CONFIRM_ARIA_LABEL;

  quickSearchTerm = '';
  protected override filtersForm = createCommissionFiltersForm(this.fb);
  protected readonly entityForm = createCommissionForm(this.fb);
  protected readonly tableFirst = signal(0);
  protected readonly dialogMode = signal<CommissionDialogMode>('create');
  protected readonly isDialogVisible = signal(false);
  protected readonly isEntityLoading = signal(false);
  protected readonly isSaving = signal(false);
  protected readonly isDeleteDialogVisible = signal(false);
  protected readonly isDeleting = signal(false);
  protected readonly selectedEntityCanRestore = signal(false);
  protected readonly commissionPendingDelete = signal<Commission | null>(null);
  protected readonly deleteDialogMessage = computed(() =>
    COMMISSION_DELETE_DIALOG_MESSAGE(this.commissionPendingDelete()?.name ?? ''),
  );
  private readonly selectedEntityId = signal<number | null>(null);
  private readonly selectedEntity = signal<Commission | null>(null);
  private readonly isQuickSearchPending = signal(false);
  private readonly hasLoadedResults = signal(false);
  protected readonly isSearchIndicatorLoading = computed(
    () => this.isLoading() || this.isQuickSearchPending(),
  );
  protected readonly isInitialTableLoading = computed(
    () => this.isLoading() && !this.hasLoadedResults(),
  );

  private readonly commissionsService = inject(CommissionsService);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);
  private readonly quickSearchChanges = new Subject<string>();
  private readonly searchRequests = new Subject<CommissionSearchRequest>();
  private readonly commissionsTable = viewChild(CommissionsTable);

  override ngOnInit(): void {
    this.observeSearchRequests();
    this.observeQuickSearch();
    super.ngOnInit();
  }

  protected override fetchFilteredList(
    filters: CommissionFilters,
    event?: TableLazyLoadEvent,
  ): void {
    if (this.filtersForm.invalid) {
      this.filtersForm.markAllAsTouched();
      return;
    }

    this.searchRequests.next({ params: this.toPageParams(filters, event) });
  }

  protected override initializeResults(): void {
    const resolvedData = this.route.snapshot.data[
      COMMISSIONS_LIST_RESOLVE_KEY
    ] as CommissionsListResolvedData;

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
        summary: COMMISSIONS_LOAD_ERROR_SUMMARY,
        detail: COMMISSIONS_LOAD_ERROR_DETAIL,
      });
    }
  }

  protected onQuickSearchChange(value: string): void {
    this.quickSearchTerm = value;
    this.isQuickSearchPending.set(true);
    this.quickSearchChanges.next(value);
  }

  protected onFilterSearch(): void {
    if (this.filtersForm.invalid) {
      this.filtersForm.markAllAsTouched();
      return;
    }

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

  protected onTableAction(event: ActionParams<Commission>): void {
    if (this.hasMutationInProgress()) return;

    switch (event.action) {
      case CommissionTableAction.View:
        this.openExistingDialog(event.params, 'view');
        break;
      case CommissionTableAction.Edit:
        this.openExistingDialog(event.params, 'edit');
        break;
      case CommissionTableAction.Delete:
        this.commissionPendingDelete.set(event.params);
        this.isDeleteDialogVisible.set(true);
        break;
      case CommissionTableAction.Restore:
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
    const commission = this.selectedEntity();
    if (this.dialogMode() !== 'edit' || !commission || this.isSaving()) return;

    this.prepareEntityForm(commission, 'view');
    this.dialogMode.set('view');
  }

  protected deactivateSelectedEntity(): void {
    const commission = this.selectedEntity();
    if (!commission || commission.deletedAt || this.hasMutationInProgress()) return;

    this.commissionPendingDelete.set(commission);
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
    const approvalDate = formatCommissionDate(value.approvalDate);
    if (!approvalDate) {
      this.entityForm.controls.approvalDate.setErrors({ required: true });
      this.entityForm.controls.approvalDate.markAsTouched();
      return;
    }

    const payload: CommissionInput = {
      name: value.name.trim(),
      nameEs: value.nameEs.trim(),
      expedientNumber: value.expedientNumber.trim(),
      approvalDate,
      commissionType: value.commissionType!,
    };
    const mode = this.dialogMode();
    const selectedEntityId = this.selectedEntityId();
    if (mode !== 'create' && selectedEntityId === null) return;

    const request =
      mode === 'create'
        ? this.commissionsService.create(payload)
        : this.commissionsService.update(selectedEntityId!, payload);

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
              mode === 'create' ? COMMISSION_CREATE_SUCCESS_TITLE : COMMISSION_UPDATE_SUCCESS_TITLE,
            detail:
              mode === 'create'
                ? COMMISSION_CREATE_SUCCESS_DETAIL
                : COMMISSION_UPDATE_SUCCESS_DETAIL,
          });
          this.isDialogVisible.set(false);
          this.selectedEntityId.set(null);
          this.selectedEntity.set(null);
          this.selectedEntityCanRestore.set(false);
          this.resetAfterMutation();
        },
        error: (error: unknown) => {
          if (isStructuredBadRequest(error)) return;
          this.showError(COMMISSION_SAVE_ERROR_DETAIL);
        },
      });
  }

  protected closeDeleteDialog(): void {
    this.isDeleteDialogVisible.set(false);
    if (!this.isDeleting()) this.commissionPendingDelete.set(null);
  }

  protected confirmDelete(): void {
    const commission = this.commissionPendingDelete();
    if (!commission || this.isDeleting()) return;

    this.isDeleteDialogVisible.set(false);
    this.isDeleting.set(true);
    this.commissionsService
      .delete(commission.id)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.isDeleting.set(false)),
      )
      .subscribe({
        next: () => {
          this.commissionPendingDelete.set(null);
          if (this.selectedEntityId() === commission.id) this.closeEntityDialog();
          this.messageService.add({
            severity: 'success',
            summary: COMMISSION_DELETE_SUCCESS_TITLE,
            detail: COMMISSION_DELETE_SUCCESS_DETAIL,
          });
          this.resetAfterMutation();
        },
        error: (error: unknown) => {
          this.commissionPendingDelete.set(null);
          if (isStructuredBadRequest(error)) return;
          this.showError(COMMISSION_DELETE_ERROR_DETAIL);
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
    commission: Commission,
    mode: Exclude<CommissionDialogMode, 'create'>,
  ): void {
    this.selectedEntityId.set(commission.id);
    this.selectedEntity.set(commission);
    this.selectedEntityCanRestore.set(mode === 'view' && Boolean(commission.deletedAt));
    this.prepareEntityForm(null, mode);
    this.dialogMode.set(mode);
    this.isDialogVisible.set(true);
    this.isEntityLoading.set(true);

    this.commissionsService
      .getById(commission.id)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.isEntityLoading.set(false)),
      )
      .subscribe({
        next: (commission) => {
          this.selectedEntityId.set(commission.id);
          this.selectedEntity.set(commission);
          this.selectedEntityCanRestore.set(mode === 'view' && Boolean(commission.deletedAt));
          this.prepareEntityForm(commission, mode);
        },
        error: (error: unknown) => {
          this.isDialogVisible.set(false);
          this.selectedEntityId.set(null);
          this.selectedEntity.set(null);
          this.selectedEntityCanRestore.set(false);
          if (isStructuredBadRequest(error)) return;
          this.showError(COMMISSION_LOAD_ERROR_DETAIL);
        },
      });
  }

  private prepareEntityForm(commission: Commission | null, mode: CommissionDialogMode): void {
    this.entityForm.enable({ emitEvent: false });
    this.entityForm.reset({
      name: commission?.name ?? '',
      nameEs: commission?.nameEs ?? '',
      expedientNumber: commission?.expedientNumber ?? '',
      approvalDate: parseCommissionDate(commission?.approvalDate),
      commissionType: commission?.commissionType ?? null,
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

    const table = this.commissionsTable();
    if (table) table.resetState();
    else this.onSearch();
  }

  private hasMutationInProgress(): boolean {
    return this.isSaving() || this.isDeleting() || this.isEntityLoading();
  }

  private showError(detail: string): void {
    this.messageService.add({
      severity: 'error',
      summary: COMMISSIONS_LOAD_ERROR_SUMMARY,
      detail,
    });
  }

  private observeSearchRequests(): void {
    this.searchRequests
      .pipe(
        switchMap(({ params }) => {
          this.isLoading.set(true);
          return this.commissionsService.getAll(params).pipe(
            catchError(() => {
              this.itemsList.set({ items: [], total: 0 });
              this.messageService.add({
                severity: 'error',
                summary: COMMISSIONS_LOAD_ERROR_SUMMARY,
                detail: COMMISSIONS_LOAD_ERROR_DETAIL,
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

  protected override parseFormToFilters(): CommissionFilters {
    const value = this.filtersForm.getRawValue();

    return {
      name: value.name?.trim() ?? null,
      nameEs: value.nameEs?.trim() ?? null,
      expedientNumber: value.expedientNumber?.trim() ?? null,
      approvalDateFrom: formatCommissionDate(value.approvalDateFrom),
      approvalDateTo: formatCommissionDate(value.approvalDateTo),
      commissionType: value.commissionType,
      status: value.status,
    };
  }

  private toPageParams(
    filters: CommissionFilters,
    event?: TableLazyLoadEvent,
  ): CommissionPageParams {
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
      expedientNumber: filters.expedientNumber || undefined,
      approvalDateFrom: filters.approvalDateFrom || undefined,
      approvalDateTo: filters.approvalDateTo || undefined,
      commissionType: filters.commissionType ?? undefined,
      statusId: filters.status ?? undefined,
      quickSearch: quickSearch || undefined,
    };
  }
}
