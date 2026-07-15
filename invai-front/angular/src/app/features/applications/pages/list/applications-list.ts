import { Component, computed, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';
import { ConfirmationDialogComponent } from '@components/confirmation-dialog/confirmation-dialog.component';
import { SearchFiltersComponent } from '@components/search-filters/search-filters.component';
import { SectionActionsComponent } from '@components/section-actions/section-actions.component';
import { SectionContainerComponent } from '@components/section-container/section-container.component';
import { ActionParams } from '@models/table.model';
import { SearchComponentBase } from '@shared/classes/search-component-base';
import { PrimeIcons } from 'primeng/api';
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

import { APPLICATIONS_TABLE_COLUMNS } from '../../applications.constants';
import { Application, ApplicationFilters, ApplicationPageParams } from '../../applications.model';
import {
  ApplicationFiltersForm,
  ApplicationsTable,
  ApplicationTableAction,
} from '../../components';
import { createApplicationFiltersForm } from '../../forms/application-form.factory';
import { ApplicationOptionsService } from '../../services/application-options.service';
import { ApplicationsService } from '../../services/applications.service';
import {
  APPLICATIONS_ADD_ARIA_LABEL,
  APPLICATIONS_EXPORT_ARIA_LABEL,
  APPLICATIONS_FILTER_ADMINISTRATIVE_UNIT,
  APPLICATIONS_FILTER_APPLICATION,
  APPLICATIONS_FILTER_CATEGORY,
  APPLICATIONS_FILTER_COMMISSION,
  APPLICATIONS_FILTER_DESCRIPTION,
  APPLICATIONS_FILTER_INCOMPLETE,
  APPLICATIONS_FILTER_INFORMATION_SYSTEM,
  APPLICATIONS_FILTER_PREFIX,
  APPLICATIONS_FILTER_SCOPE,
  APPLICATIONS_FILTER_STATUS,
  APPLICATIONS_LOAD_ERROR_DETAIL,
  APPLICATIONS_LOAD_ERROR_SUMMARY,
  APPLICATIONS_QUICK_SEARCH_ARIA_LABEL,
  APPLICATIONS_TITLE,
  APPLICATIONS_WITHDRAWAL_DIALOG_CANCEL_ARIA_LABEL,
  APPLICATIONS_WITHDRAWAL_DIALOG_CANCEL_LABEL,
  APPLICATIONS_WITHDRAWAL_DIALOG_CONFIRM_ARIA_LABEL,
  APPLICATIONS_WITHDRAWAL_DIALOG_CONFIRM_LABEL,
  APPLICATIONS_WITHDRAWAL_DIALOG_MESSAGE,
  APPLICATIONS_WITHDRAWAL_DIALOG_TITLE,
  APPLICATIONS_WITHDRAWAL_ERROR_DETAIL,
  APPLICATIONS_WITHDRAWAL_ERROR_SUMMARY,
  APPLICATIONS_WITHDRAWAL_SUCCESS_DETAIL,
  APPLICATIONS_WITHDRAWAL_SUCCESS_SUMMARY,
} from './applications-list.i18n';

interface ApplicationSearchRequest {
  params: ApplicationPageParams;
}

const DEFAULT_PAGE_SIZE = 10;
const QUICK_SEARCH_DEBOUNCE_MS = 400;
const INCOMPLETE_FILTER_WARNING =
  "El filtre d'aplicacions incompletes encara no està suportat pel backend i s'omet de la petició.";

@Component({
  standalone: true,
  selector: 'app-applications-list',
  imports: [
    ApplicationsTable,
    ApplicationFiltersForm,
    ConfirmationDialogComponent,
    SearchFiltersComponent,
    SectionActionsComponent,
    SectionContainerComponent,
  ],
  templateUrl: './applications-list.html',
  styleUrl: './applications-list.scss',
})
export class ApplicationsList
  extends SearchComponentBase<Application, ApplicationFilters>
  implements OnInit
{
  readonly header = APPLICATIONS_TITLE;
  protected override readonly ALL_TABLE_COLUMNS = APPLICATIONS_TABLE_COLUMNS;
  protected readonly filterLabels = {
    prefix: APPLICATIONS_FILTER_PREFIX,
    application: APPLICATIONS_FILTER_APPLICATION,
    category: APPLICATIONS_FILTER_CATEGORY,
    informationSystem: APPLICATIONS_FILTER_INFORMATION_SYSTEM,
    scope: APPLICATIONS_FILTER_SCOPE,
    commission: APPLICATIONS_FILTER_COMMISSION,
    administrativeUnit: APPLICATIONS_FILTER_ADMINISTRATIVE_UNIT,
    status: APPLICATIONS_FILTER_STATUS,
    description: APPLICATIONS_FILTER_DESCRIPTION,
    incomplete: APPLICATIONS_FILTER_INCOMPLETE,
  };
  protected readonly quickSearchAriaLabel = APPLICATIONS_QUICK_SEARCH_ARIA_LABEL;
  protected readonly exportAriaLabel = APPLICATIONS_EXPORT_ARIA_LABEL;
  protected readonly addAriaLabel = APPLICATIONS_ADD_ARIA_LABEL;
  protected readonly withdrawalDialogTitle = APPLICATIONS_WITHDRAWAL_DIALOG_TITLE;
  protected readonly withdrawalDialogCancelLabel = APPLICATIONS_WITHDRAWAL_DIALOG_CANCEL_LABEL;
  protected readonly withdrawalDialogConfirmLabel = APPLICATIONS_WITHDRAWAL_DIALOG_CONFIRM_LABEL;
  protected readonly withdrawalDialogCancelAriaLabel =
    APPLICATIONS_WITHDRAWAL_DIALOG_CANCEL_ARIA_LABEL;
  protected readonly withdrawalDialogConfirmAriaLabel =
    APPLICATIONS_WITHDRAWAL_DIALOG_CONFIRM_ARIA_LABEL;
  protected readonly withdrawalDialogConfirmIcon = PrimeIcons.TRASH;

  quickSearchTerm = '';
  private readonly isQuickSearchPending = signal(false);
  protected readonly isSearchIndicatorLoading = computed(
    () => this.isLoading() || this.isQuickSearchPending(),
  );
  protected readonly tableFirst = signal(0);
  protected readonly selectedApplicationForWithdrawal = signal<Application | null>(null);
  protected readonly isWithdrawalDialogVisible = signal(false);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly applicationsService = inject(ApplicationsService);
  private readonly applicationOptionsService = inject(ApplicationOptionsService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly quickSearchChanges = new Subject<string>();
  private readonly searchRequests = new Subject<ApplicationSearchRequest>();

  protected override filtersForm = createApplicationFiltersForm(this.fb);
  protected readonly filterOptions = toSignal(this.applicationOptionsService.getOptions(), {
    initialValue: null,
  });

  override ngOnInit(): void {
    this.observeSearchRequests();
    this.observeQuickSearch();
    super.ngOnInit();
  }

  protected override fetchFilteredList(
    filters: ApplicationFilters,
    $event?: TableLazyLoadEvent,
  ): void {
    if (filters.incomplete) {
      console.warn(INCOMPLETE_FILTER_WARNING);
    }

    this.searchRequests.next({
      params: this.toPageParams(filters, $event),
    });
  }

  protected onQuickSearchChange(value: string): void {
    this.quickSearchTerm = value;
    this.isQuickSearchPending.set(true);
    this.quickSearchChanges.next(value);
  }

  protected onFilterSearch(): void {
    this.tableFirst.set(0);
    this.onSearch();
  }

  protected onPageChange(event: TableLazyLoadEvent): void {
    this.tableFirst.set(event.first ?? 0);
    this.onSearch(event);
  }

  override reset(): void {
    this.tableFirst.set(0);
    super.reset();
  }

  protected onTableActions(event: ActionParams<Application>): void {
    if (event.action === ApplicationTableAction.Delete) {
      this.selectedApplicationForWithdrawal.set(event.params);
      this.isWithdrawalDialogVisible.set(true);
      return;
    }

    if (
      event.action === ApplicationTableAction.Detail ||
      event.action === ApplicationTableAction.Update
    ) {
      void this.router.navigate([event.params.id], { relativeTo: this.route });
      return;
    }
  }

  protected onNewApplication(): void {
    void this.router.navigate(['new'], { relativeTo: this.route });
  }

  protected getWithdrawalDialogMessage(): string {
    return APPLICATIONS_WITHDRAWAL_DIALOG_MESSAGE(
      this.selectedApplicationForWithdrawal()?.name ?? '',
    );
  }

  protected onWithdrawalDialogClose(): void {
    this.isWithdrawalDialogVisible.set(false);
    this.selectedApplicationForWithdrawal.set(null);
  }

  protected onWithdrawalDialogConfirm(): void {
    const application = this.selectedApplicationForWithdrawal();
    const id = Number(application?.id);

    if (!application || Number.isNaN(id)) {
      this.onWithdrawalDialogClose();
      return;
    }

    this.applicationsService
      .delete(id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.onWithdrawalDialogClose();
          this.messageService.add({
            severity: 'success',
            summary: APPLICATIONS_WITHDRAWAL_SUCCESS_SUMMARY,
            detail: APPLICATIONS_WITHDRAWAL_SUCCESS_DETAIL,
          });
          this.tableFirst.set(0);
          this.onSearch();
        },
        error: () => {
          this.messageService.add({
            severity: 'error',
            summary: APPLICATIONS_WITHDRAWAL_ERROR_SUMMARY,
            detail: APPLICATIONS_WITHDRAWAL_ERROR_DETAIL,
          });
        },
      });
  }

  protected override exportExcel(): void {
    this.messageService.add({
      severity: 'info',
      summary: $localize`Informació`,
      detail: $localize`L'exportació encara no està implementada.`,
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

  private observeSearchRequests(): void {
    this.searchRequests
      .pipe(
        switchMap((request) => {
          this.isLoading.set(true);

          return this.applicationsService.getPage(request.params).pipe(
            catchError(() => {
              this.itemsList.set({ items: [], total: 0 });
              this.messageService.add({
                severity: 'error',
                summary: APPLICATIONS_LOAD_ERROR_SUMMARY,
                detail: APPLICATIONS_LOAD_ERROR_DETAIL,
              });
              return EMPTY;
            }),
            finalize(() => {
              this.isLoading.set(false);
            }),
          );
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((page) => {
        this.itemsList.set({ items: page.content, total: page.totalElements });
      });
  }

  private toPageParams(
    filters: ApplicationFilters,
    event?: TableLazyLoadEvent,
  ): ApplicationPageParams {
    const first = event?.first ?? 0;
    const size = event?.rows ?? DEFAULT_PAGE_SIZE;
    const sortFields = Array.isArray(event?.sortField)
      ? event.sortField
      : event?.sortField
        ? [event.sortField]
        : [];
    const sortDirection = event?.sortOrder === -1 ? 'desc' : event?.sortOrder === 1 ? 'asc' : null;
    const sort = sortDirection ? sortFields.map((field) => `${field},${sortDirection}`) : undefined;
    const quickSearch = this.quickSearchTerm.trim();

    return {
      page: Math.floor(first / size),
      size,
      sort: sort?.length === 1 ? sort[0] : sort,
      prefix: filters.prefix || undefined,
      applicationName: filters.application || undefined,
      categoryId: filters.category ?? undefined,
      systemTypeId: filters.informationSystem ?? undefined,
      fieldId: filters.scope ?? undefined,
      commissionId: filters.commission ?? undefined,
      admUnitId: filters.administrativeUnit ?? undefined,
      statusId: filters.status ?? undefined,
      description: filters.description || undefined,
      quickSearch: quickSearch || undefined,
    };
  }
}
