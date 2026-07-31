import { Component, computed, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AbstractControl } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { SearchFiltersComponent } from '@components/search-filters/search-filters.component';
import { SectionActionsComponent } from '@components/section-actions/section-actions.component';
import { SectionContainerComponent } from '@components/section-container/section-container.component';
import { SearchComponentBase } from '@shared/classes/search-component-base';
import { TableLazyLoadEvent } from 'primeng/table';
import {
  catchError,
  debounceTime,
  distinctUntilChanged,
  EMPTY,
  filter,
  finalize,
  map,
  merge,
  Observable,
  Subject,
  switchMap,
  tap,
} from 'rxjs';

import { APPLICATIONS_TABLE_COLUMNS } from '../../applications.constants';
import {
  Application,
  ApplicationFilters,
  ApplicationInfrastructureFilterOptions,
  ApplicationPageParams,
} from '../../applications.model';
import { ApplicationFiltersForm, ApplicationsTable } from '../../components';
import { createApplicationFiltersForm } from '../../forms/application-form.factory';
import { ApplicationSelectOptions } from '../../services/application-options.service';
import { ApplicationsService } from '../../services/applications.service';
import {
  APPLICATIONS_LIST_RESOLVE_KEY,
  ApplicationsListResolvedData,
} from './applications-list.resolver';
import {
  APPLICATIONS_ADD_ARIA_LABEL,
  APPLICATIONS_EXPORT_ARIA_LABEL,
  APPLICATIONS_FILTER_ADMINISTRATIVE_UNIT,
  APPLICATIONS_FILTER_APPLICATION,
  APPLICATIONS_FILTER_CATEGORY,
  APPLICATIONS_FILTER_COMMISSION,
  APPLICATIONS_FILTER_DATABASE,
  APPLICATIONS_FILTER_DESCRIPTION,
  APPLICATIONS_FILTER_ENVIRONMENT,
  APPLICATIONS_FILTER_INCOMPLETE,
  APPLICATIONS_FILTER_INFORMATION_SYSTEM,
  APPLICATIONS_FILTER_PREFIX,
  APPLICATIONS_FILTER_RESPONSIBLE,
  APPLICATIONS_FILTER_SCOPE,
  APPLICATIONS_FILTER_SERVER,
  APPLICATIONS_FILTER_STATUS,
  APPLICATIONS_LOAD_ERROR_DETAIL,
  APPLICATIONS_LOAD_ERROR_SUMMARY,
  APPLICATIONS_QUICK_SEARCH_ARIA_LABEL,
  APPLICATIONS_TITLE,
  APPLICATIONS_UNSUPPORTED_FILTER_WARNING,
  APPLICATIONS_UNSUPPORTED_FILTER_WARNING_TITLE,
} from './applications-list.i18n';

interface ApplicationSearchRequest {
  params: ApplicationPageParams;
}

interface UnsupportedFilterChange {
  label: string;
  value: unknown;
}

interface UnsupportedFilterEntry {
  control: AbstractControl;
  label: string;
  debounceMs?: number;
}

const DEFAULT_PAGE_SIZE = 10;
const QUICK_SEARCH_DEBOUNCE_MS = 400;
const RESPONSIBLE_FILTER_DEBOUNCE_MS = 400;
const INCOMPLETE_FILTER_WARNING =
  "El filtre d'aplicacions incompletes encara no està suportat pel backend i s'omet de la petició.";

@Component({
  standalone: true,
  selector: 'app-applications-list',
  imports: [
    ApplicationsTable,
    ApplicationFiltersForm,
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
  protected override readonly DEFAULT_HIDDEN_TABLE_COLUMN_KEYS = [
    'commission',
    'status',
    'environment',
    'database',
    'server',
    'responsible',
  ];
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
    responsible: APPLICATIONS_FILTER_RESPONSIBLE,
    database: APPLICATIONS_FILTER_DATABASE,
    server: APPLICATIONS_FILTER_SERVER,
    environment: APPLICATIONS_FILTER_ENVIRONMENT,
    incomplete: APPLICATIONS_FILTER_INCOMPLETE,
  };
  protected readonly quickSearchAriaLabel = APPLICATIONS_QUICK_SEARCH_ARIA_LABEL;
  protected readonly exportAriaLabel = APPLICATIONS_EXPORT_ARIA_LABEL;
  protected readonly addAriaLabel = APPLICATIONS_ADD_ARIA_LABEL;

  quickSearchTerm = '';
  private readonly isQuickSearchPending = signal(false);
  protected readonly isSearchIndicatorLoading = computed(
    () => this.isLoading() || this.isQuickSearchPending(),
  );
  private readonly hasLoadedResults = signal(false);
  protected readonly isInitialTableLoading = computed(
    () => this.isLoading() && !this.hasLoadedResults(),
  );
  protected readonly tableFirst = signal(0);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly applicationsService = inject(ApplicationsService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly quickSearchChanges = new Subject<string>();
  private readonly searchRequests = new Subject<ApplicationSearchRequest>();

  protected override filtersForm = createApplicationFiltersForm(this.fb);
  protected readonly filterOptions = signal<ApplicationSelectOptions | null>(null);
  protected readonly infrastructureFilterOptions =
    signal<ApplicationInfrastructureFilterOptions | null>(null);

  override ngOnInit(): void {
    this.observeSearchRequests();
    this.observeQuickSearch();
    this.observeUnsupportedFilters();
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

  protected override initializeResults(): void {
    const resolvedData = this.route.snapshot.data[
      APPLICATIONS_LIST_RESOLVE_KEY
    ] as ApplicationsListResolvedData;

    this.updateSearchState();
    this.filterOptions.set(resolvedData.options);
    this.infrastructureFilterOptions.set(resolvedData.infrastructureOptions);
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
        summary: APPLICATIONS_LOAD_ERROR_SUMMARY,
        detail: APPLICATIONS_LOAD_ERROR_DETAIL,
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

  protected onViewApplication(application: Application): void {
    void this.router.navigate([application.id], { relativeTo: this.route });
  }

  protected onNewApplication(): void {
    void this.router.navigate(['new'], { relativeTo: this.route });
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
        this.hasLoadedResults.set(true);
        this.itemsList.set({ items: page.content, total: page.totalElements });
      });
  }

  private observeUnsupportedFilters(): void {
    const entries: UnsupportedFilterEntry[] = [
      {
        control: this.filtersForm.controls.responsible,
        label: this.filterLabels.responsible,
        debounceMs: RESPONSIBLE_FILTER_DEBOUNCE_MS,
      },
      {
        control: this.filtersForm.controls.database,
        label: this.filterLabels.database,
      },
      {
        control: this.filtersForm.controls.server,
        label: this.filterLabels.server,
      },
      {
        control: this.filtersForm.controls.environment,
        label: this.filterLabels.environment,
      },
    ];
    const changes: Observable<UnsupportedFilterChange>[] = entries.map(
      ({ control, label, debounceMs }) => {
        const change$ = control.valueChanges.pipe(
          distinctUntilChanged(),
          map((value) => ({ label, value })),
        );

        return debounceMs ? change$.pipe(debounceTime(debounceMs)) : change$;
      },
    );

    merge(...changes)
      .pipe(
        filter(({ value }) => this.hasValue(value)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(({ label }) => {
        this.messageService.add({
          severity: 'warn',
          summary: APPLICATIONS_UNSUPPORTED_FILTER_WARNING_TITLE,
          detail: APPLICATIONS_UNSUPPORTED_FILTER_WARNING(label),
        });
      });
  }

  private hasValue(value: unknown): boolean {
    return typeof value === 'string'
      ? value.trim().length > 0
      : value !== null && value !== undefined;
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
