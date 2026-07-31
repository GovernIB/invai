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

import { RoleDialog, RoleDialogMode } from '../../components/role-dialog/role-dialog';
import { RoleFiltersForm } from '../../components/role-filters-form/role-filters-form';
import { RolesTable, RoleTableAction } from '../../components/roles-table/roles-table';
import { createRoleFiltersForm } from '../../forms/role-filters-form.factory';
import { createRoleForm } from '../../forms/role-form.factory';
import { ROLES_TABLE_COLUMNS } from '../../roles.constants';
import { Role, RoleFilters, RoleInput, RolePageParams } from '../../roles.model';
import { RolesService } from '../../services/roles.service';
import {
  ROLE_CREATE_SUCCESS_DETAIL,
  ROLE_CREATE_SUCCESS_TITLE,
  ROLE_DELETE_DIALOG_CANCEL_ARIA_LABEL,
  ROLE_DELETE_DIALOG_CANCEL_LABEL,
  ROLE_DELETE_DIALOG_CONFIRM_ARIA_LABEL,
  ROLE_DELETE_DIALOG_CONFIRM_LABEL,
  ROLE_DELETE_DIALOG_MESSAGE,
  ROLE_DELETE_DIALOG_TITLE,
  ROLE_DELETE_ERROR_DETAIL,
  ROLE_DELETE_SUCCESS_DETAIL,
  ROLE_DELETE_SUCCESS_TITLE,
  ROLE_LOAD_ERROR_DETAIL,
  ROLE_RESTORE_ERROR_DETAIL,
  ROLE_RESTORE_SUCCESS_DETAIL,
  ROLE_RESTORE_SUCCESS_TITLE,
  ROLE_SAVE_ERROR_DETAIL,
  ROLE_UPDATE_SUCCESS_DETAIL,
  ROLE_UPDATE_SUCCESS_TITLE,
  ROLES_ADD_ARIA_LABEL,
  ROLES_FILTER_NAME,
  ROLES_FILTER_NAME_ES,
  ROLES_FILTER_STATUS,
  ROLES_LIST_TITLE,
  ROLES_LOAD_ERROR_DETAIL,
  ROLES_LOAD_ERROR_SUMMARY,
  ROLES_QUICK_SEARCH_ARIA_LABEL,
  ROLES_QUICK_SEARCH_PLACEHOLDER,
} from './roles-list.i18n';
import { ROLES_LIST_RESOLVE_KEY, RolesListResolvedData } from './roles-list.resolver';

const DEFAULT_PAGE_SIZE = 10;
const QUICK_SEARCH_DEBOUNCE_MS = 400;

@Component({
  selector: 'app-roles-list',
  standalone: true,
  imports: [
    ConfirmationDialogComponent,
    RoleDialog,
    RoleFiltersForm,
    RolesTable,
    SearchFiltersComponent,
    SectionActionsComponent,
  ],
  templateUrl: './roles-list.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RolesList extends SearchComponentBase<Role, RoleFilters> implements OnInit {
  protected override readonly ALL_TABLE_COLUMNS = ROLES_TABLE_COLUMNS;
  protected readonly title = ROLES_LIST_TITLE;
  protected readonly filterLabels = {
    name: ROLES_FILTER_NAME,
    nameEs: ROLES_FILTER_NAME_ES,
    status: ROLES_FILTER_STATUS,
  };
  protected readonly quickSearchPlaceholder = ROLES_QUICK_SEARCH_PLACEHOLDER;
  protected readonly quickSearchAriaLabel = ROLES_QUICK_SEARCH_ARIA_LABEL;
  protected readonly addAriaLabel = ROLES_ADD_ARIA_LABEL;
  protected readonly deleteDialogTitle = ROLE_DELETE_DIALOG_TITLE;
  protected readonly deleteDialogCancelLabel = ROLE_DELETE_DIALOG_CANCEL_LABEL;
  protected readonly deleteDialogConfirmLabel = ROLE_DELETE_DIALOG_CONFIRM_LABEL;
  protected readonly deleteDialogCancelAriaLabel = ROLE_DELETE_DIALOG_CANCEL_ARIA_LABEL;
  protected readonly deleteDialogConfirmAriaLabel = ROLE_DELETE_DIALOG_CONFIRM_ARIA_LABEL;

  quickSearchTerm = '';
  protected override filtersForm = createRoleFiltersForm(this.fb);
  protected readonly entityForm = createRoleForm(this.fb);
  protected readonly tableFirst = signal(0);
  protected readonly dialogMode = signal<RoleDialogMode>('create');
  protected readonly isDialogVisible = signal(false);
  protected readonly isEntityLoading = signal(false);
  protected readonly isSaving = signal(false);
  protected readonly isDeleteDialogVisible = signal(false);
  protected readonly isDeleting = signal(false);
  protected readonly selectedEntityCanRestore = signal(false);
  protected readonly pendingDelete = signal<Role | null>(null);
  protected readonly deleteDialogMessage = computed(() =>
    ROLE_DELETE_DIALOG_MESSAGE(this.pendingDelete()?.name ?? ''),
  );
  protected readonly isSearchIndicatorLoading = computed(
    () => this.isLoading() || this.isQuickSearchPending(),
  );
  protected readonly isInitialTableLoading = computed(
    () => this.isLoading() && !this.hasLoadedResults(),
  );

  private readonly selectedEntityId = signal<number | null>(null);
  private readonly selectedEntity = signal<Role | null>(null);
  private readonly isQuickSearchPending = signal(false);
  private readonly hasLoadedResults = signal(false);
  private readonly rolesService = inject(RolesService);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);
  private readonly quickSearchChanges = new Subject<string>();
  private readonly searchRequests = new Subject<RolePageParams>();
  private readonly rolesTable = viewChild(RolesTable);

  override ngOnInit(): void {
    this.observeSearchRequests();
    this.observeQuickSearch();
    super.ngOnInit();
  }

  protected override fetchFilteredList(filters: RoleFilters, event?: TableLazyLoadEvent): void {
    this.searchRequests.next(this.toPageParams(filters, event));
  }

  protected override initializeResults(): void {
    const resolved = this.route.snapshot.data[ROLES_LIST_RESOLVE_KEY] as RolesListResolvedData;
    this.updateSearchState();
    this.hasLoadedResults.set(true);
    if (resolved?.page) {
      this.itemsList.set({ items: resolved.page.content, total: resolved.page.totalElements });
    } else {
      this.itemsList.set({ items: [], total: 0 });
      if (resolved?.pageLoadFailed) this.showError(ROLES_LOAD_ERROR_DETAIL);
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

  protected onTableAction(event: ActionParams<Role>): void {
    if (this.hasMutationInProgress()) return;
    switch (event.action) {
      case RoleTableAction.View:
        this.openExistingDialog(event.params, 'view');
        break;
      case RoleTableAction.Edit:
        this.openExistingDialog(event.params, 'edit');
        break;
      case RoleTableAction.Delete:
        this.pendingDelete.set(event.params);
        this.isDeleteDialogVisible.set(true);
        break;
      case RoleTableAction.Restore:
        this.restoreSelectedEntity(event.params);
        break;
    }
  }

  protected closeEntityDialog(): void {
    if (this.isEntityLoading() || this.isSaving()) return;
    this.clearSelection();
    this.isDialogVisible.set(false);
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
    const role = this.selectedEntity();
    if (this.dialogMode() !== 'edit' || !role || this.isSaving()) return;
    this.prepareEntityForm(role, 'view');
    this.dialogMode.set('view');
  }

  protected deactivateSelectedEntity(): void {
    const role = this.selectedEntity();
    if (!role || role.deletedAt || this.hasMutationInProgress()) return;
    this.pendingDelete.set(role);
    this.isDeleteDialogVisible.set(true);
  }

  protected restoreSelectedEntity(role: Role | null = this.selectedEntity()): void {
    if (!role || this.hasMutationInProgress()) return;
    this.isSaving.set(true);
    this.rolesService
      .reactivate(role.id)
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
            summary: ROLE_RESTORE_SUCCESS_TITLE,
            detail: ROLE_RESTORE_SUCCESS_DETAIL,
          });
          this.resetAfterMutation();
        },
        error: (error: unknown) => {
          if (!isStructuredBadRequest(error)) this.showError(ROLE_RESTORE_ERROR_DETAIL);
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
    const payload: RoleInput = {
      name: value.name.trim(),
      nameEs: value.nameEs.trim() || null,
    };
    const mode = this.dialogMode();
    const id = this.selectedEntityId();
    if (mode !== 'create' && id === null) return;
    const request =
      mode === 'create' ? this.rolesService.create(payload) : this.rolesService.update(id!, payload);
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
            summary: mode === 'create' ? ROLE_CREATE_SUCCESS_TITLE : ROLE_UPDATE_SUCCESS_TITLE,
            detail: mode === 'create' ? ROLE_CREATE_SUCCESS_DETAIL : ROLE_UPDATE_SUCCESS_DETAIL,
          });
          this.isDialogVisible.set(false);
          this.clearSelection();
          this.resetAfterMutation();
        },
        error: (error: unknown) => {
          if (!isStructuredBadRequest(error)) this.showError(ROLE_SAVE_ERROR_DETAIL);
        },
      });
  }

  protected closeDeleteDialog(): void {
    this.isDeleteDialogVisible.set(false);
    if (!this.isDeleting()) this.pendingDelete.set(null);
  }

  protected confirmDelete(): void {
    const role = this.pendingDelete();
    if (!role || this.isDeleting()) return;
    this.isDeleteDialogVisible.set(false);
    this.isDeleting.set(true);
    this.rolesService
      .delete(role.id)
      .pipe(
        finalize(() => this.isDeleting.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.pendingDelete.set(null);
          if (this.selectedEntityId() === role.id) {
            this.isDialogVisible.set(false);
            this.clearSelection();
          }
          this.messageService.add({
            severity: 'success',
            summary: ROLE_DELETE_SUCCESS_TITLE,
            detail: ROLE_DELETE_SUCCESS_DETAIL,
          });
          this.resetAfterMutation();
        },
        error: (error: unknown) => {
          this.pendingDelete.set(null);
          if (!isStructuredBadRequest(error)) this.showError(ROLE_DELETE_ERROR_DETAIL);
        },
      });
  }

  protected override parseFormToFilters(): RoleFilters {
    const value = this.filtersForm.getRawValue();
    return {
      name: value.name?.trim() || null,
      nameEs: value.nameEs?.trim() || null,
      status: value.status,
    };
  }

  private openExistingDialog(role: Role, mode: Exclude<RoleDialogMode, 'create'>): void {
    this.selectedEntityId.set(role.id);
    this.selectedEntity.set(role);
    this.selectedEntityCanRestore.set(mode === 'view' && Boolean(role.deletedAt));
    this.prepareEntityForm(null, mode);
    this.dialogMode.set(mode);
    this.isDialogVisible.set(true);
    this.isEntityLoading.set(true);
    this.rolesService
      .getById(role.id)
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
          if (!isStructuredBadRequest(error)) this.showError(ROLE_LOAD_ERROR_DETAIL);
        },
      });
  }

  private prepareEntityForm(role: Role | null, mode: RoleDialogMode): void {
    this.entityForm.enable({ emitEvent: false });
    this.entityForm.reset({ name: role?.name ?? '', nameEs: role?.nameEs ?? '' });
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
          return this.rolesService.getAll(params).pipe(
            catchError(() => {
              this.itemsList.set({ items: [], total: 0 });
              this.showError(ROLES_LOAD_ERROR_DETAIL);
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

  private toPageParams(filters: RoleFilters, event?: TableLazyLoadEvent): RolePageParams {
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
      nameEs: filters.nameEs || undefined,
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
    const table = this.rolesTable();
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
      summary: ROLES_LOAD_ERROR_SUMMARY,
      detail,
    });
  }
}
