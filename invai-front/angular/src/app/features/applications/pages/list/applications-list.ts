import { Component, computed, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';
import { SearchFiltersComponent } from '@components/search-filters/search-filters.component';
import { SectionActionsComponent } from '@components/section-actions/section-actions.component';
import { SectionContainerComponent } from '@components/section-container/section-container.component';
import { ResponsiblePersonOption } from '@features/maintenances/responsibles/responsibles.model';
import { toResponsiblePersonOption } from '@features/maintenances/responsibles/responsibles.utils';
import { ResponsiblePeopleService } from '@features/maintenances/responsibles/services/responsible-people.service';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';
import { SearchComponentBase } from '@shared/classes/search-component-base';
import { TableLazyLoadEvent } from 'primeng/table';
import {
  catchError,
  debounce,
  debounceTime,
  distinctUntilChanged,
  EMPTY,
  finalize,
  map,
  of,
  Subject,
  switchMap,
  tap,
  timer,
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
import {
  ApplicationOptionsService,
  ApplicationSelectOptions,
} from '../../services/application-options.service';
import { ApplicationsService } from '../../services/applications.service';
import {
  APPLICATIONS_LIST_RESOLVE_KEY,
  ApplicationsListResolvedData,
} from './applications-list.resolver';
import {
  APPLICATIONS_ADD_ARIA_LABEL,
  APPLICATIONS_EXPORT_ARIA_LABEL,
  APPLICATIONS_FILTER_ADMINISTRATIVE_UNIT,
  APPLICATIONS_FILTER_ADMINISTRATIVE_UNIT_EMPTY,
  APPLICATIONS_FILTER_ADMINISTRATIVE_UNIT_ERROR,
  APPLICATIONS_FILTER_ADMINISTRATIVE_UNIT_LOADING,
  APPLICATIONS_FILTER_APPLICATION,
  APPLICATIONS_FILTER_CATEGORY,
  APPLICATIONS_FILTER_COMMISSION,
  APPLICATIONS_FILTER_CONSELLERIA,
  APPLICATIONS_FILTER_CONSELLERIA_ERROR,
  APPLICATIONS_FILTER_CONSELLERIA_LOADING,
  APPLICATIONS_FILTER_DATABASE,
  APPLICATIONS_FILTER_ENVIRONMENT,
  APPLICATIONS_FILTER_INCOMPLETE,
  APPLICATIONS_FILTER_INFORMATION_SYSTEM,
  APPLICATIONS_FILTER_PREFIX,
  APPLICATIONS_FILTER_RETRY,
  APPLICATIONS_FILTER_RESPONSIBLE,
  APPLICATIONS_FILTER_RESPONSIBLE_EMPTY,
  APPLICATIONS_FILTER_RESPONSIBLE_LOADING,
  APPLICATIONS_FILTER_SCOPE,
  APPLICATIONS_FILTER_SELECT_CONSELLERIA_FIRST,
  APPLICATIONS_FILTER_SERVER,
  APPLICATIONS_FILTER_STATUS,
  APPLICATIONS_LOAD_ERROR_DETAIL,
  APPLICATIONS_LOAD_ERROR_SUMMARY,
  APPLICATIONS_QUICK_SEARCH_ARIA_LABEL,
  APPLICATIONS_RESPONSIBLE_SEARCH_ERROR_DETAIL,
  APPLICATIONS_TITLE,
} from './applications-list.i18n';

interface ApplicationSearchRequest {
  params: ApplicationPageParams;
}

const DEFAULT_PAGE_SIZE = 10;
const QUICK_SEARCH_DEBOUNCE_MS = 400;
const RESPONSIBLE_FILTER_DEBOUNCE_MS = 400;
const RESPONSIBLE_FILTER_PAGE_SIZE = 20;

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
    conselleria: APPLICATIONS_FILTER_CONSELLERIA,
    administrativeUnit: APPLICATIONS_FILTER_ADMINISTRATIVE_UNIT,
    departmentsLoading: APPLICATIONS_FILTER_CONSELLERIA_LOADING,
    departmentsLoadError: APPLICATIONS_FILTER_CONSELLERIA_ERROR,
    administrativeUnitsLoading: APPLICATIONS_FILTER_ADMINISTRATIVE_UNIT_LOADING,
    administrativeUnitsLoadError: APPLICATIONS_FILTER_ADMINISTRATIVE_UNIT_ERROR,
    administrativeUnitsEmpty: APPLICATIONS_FILTER_ADMINISTRATIVE_UNIT_EMPTY,
    selectConselleriaFirst: APPLICATIONS_FILTER_SELECT_CONSELLERIA_FIRST,
    retry: APPLICATIONS_FILTER_RETRY,
    status: APPLICATIONS_FILTER_STATUS,
    responsible: APPLICATIONS_FILTER_RESPONSIBLE,
    responsibleEmpty: APPLICATIONS_FILTER_RESPONSIBLE_EMPTY,
    responsibleLoading: APPLICATIONS_FILTER_RESPONSIBLE_LOADING,
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
  private readonly applicationOptionsService = inject(ApplicationOptionsService);
  private readonly responsiblePeopleService = inject(ResponsiblePeopleService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly quickSearchChanges = new Subject<string>();
  private readonly responsibleSearchChanges = new Subject<string>();
  private readonly searchRequests = new Subject<ApplicationSearchRequest>();
  private readonly administrativeUnitRequests = new Subject<string | null>();
  private responsibleSearchTerm = '';

  protected override filtersForm = createApplicationFiltersForm(this.fb);
  protected readonly filterOptions = signal<ApplicationSelectOptions | null>(null);
  protected readonly infrastructureFilterOptions =
    signal<ApplicationInfrastructureFilterOptions | null>(null);
  protected readonly responsibleOptions = signal<ResponsiblePersonOption[]>([]);
  protected readonly isResponsibleSearchLoading = signal(false);
  protected readonly isDepartmentsLoading = signal(false);
  protected readonly departmentsLoadFailed = signal(false);
  protected readonly isAdministrativeUnitsLoading = signal(false);
  protected readonly administrativeUnitsLoadFailed = signal(false);

  override ngOnInit(): void {
    this.observeSearchRequests();
    this.observeQuickSearch();
    this.observeResponsibleSearch();
    this.observeAdministrativeUnitRequests();
    super.ngOnInit();
  }

  protected override fetchFilteredList(
    filters: ApplicationFilters,
    $event?: TableLazyLoadEvent,
  ): void {
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
    this.departmentsLoadFailed.set(resolvedData.departmentsLoadFailed);
    this.administrativeUnitsLoadFailed.set(resolvedData.administrativeUnitsLoadFailed);
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

  protected onResponsibleSearch(value: string): void {
    const query = value.trim();
    if (query === this.responsibleSearchTerm) return;

    this.responsibleSearchTerm = query;
    if (!query) {
      this.responsibleOptions.set([]);
      this.isResponsibleSearchLoading.set(false);
    } else {
      this.isResponsibleSearchLoading.set(true);
    }
    this.responsibleSearchChanges.next(query);
  }

  protected selectConselleria(code: string | null): void {
    this.filtersForm.controls.administrativeUnit.reset(null, { emitEvent: false });
    this.filterOptions.update((options) =>
      options ? { ...options, administrativeUnits: [] } : options,
    );
    this.administrativeUnitRequests.next(code);
  }

  protected retryAdministrativeUnits(): void {
    this.administrativeUnitRequests.next(this.filtersForm.controls.conselleria.value);
  }

  protected retryDepartments(): void {
    if (this.isDepartmentsLoading()) return;

    this.isDepartmentsLoading.set(true);
    this.applicationOptionsService
      .getDepartmentOptions()
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.isDepartmentsLoading.set(false)),
      )
      .subscribe({
        next: (departments) => {
          this.filterOptions.update((options) =>
            options ? { ...options, departments } : options,
          );
          this.departmentsLoadFailed.set(false);
        },
        error: () => this.departmentsLoadFailed.set(true),
      });
  }

  protected onPageChange(event: TableLazyLoadEvent): void {
    this.tableFirst.set(event.first ?? 0);
    this.onSearch(event);
  }

  override reset(): void {
    this.tableFirst.set(0);
    this.responsibleSearchTerm = '';
    this.responsibleOptions.set([]);
    this.isResponsibleSearchLoading.set(false);
    this.responsibleSearchChanges.next('');
    super.reset();
    this.filterOptions.update((options) =>
      options ? { ...options, administrativeUnits: [] } : options,
    );
    this.administrativeUnitRequests.next(null);
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

  private observeResponsibleSearch(): void {
    this.responsibleSearchChanges
      .pipe(
        debounce((query) => timer(query ? RESPONSIBLE_FILTER_DEBOUNCE_MS : 0)),
        distinctUntilChanged(),
        switchMap((query) => {
          if (!query) return of([] as ResponsiblePersonOption[]);

          this.isResponsibleSearchLoading.set(true);
          return this.responsiblePeopleService
            .getPage({
              page: 0,
              size: RESPONSIBLE_FILTER_PAGE_SIZE,
              sort: ['firstName,asc', 'lastName,asc'],
              statusId: SoftDeleteStatus.ACTIVE,
              search: query,
            })
            .pipe(
              map((page) => page.content.map(toResponsiblePersonOption)),
              catchError(() => {
                this.messageService.add({
                  severity: 'error',
                  summary: APPLICATIONS_LOAD_ERROR_SUMMARY,
                  detail: APPLICATIONS_RESPONSIBLE_SEARCH_ERROR_DETAIL,
                });
                return of([] as ResponsiblePersonOption[]);
              }),
              finalize(() => this.isResponsibleSearchLoading.set(false)),
            );
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((options) => this.responsibleOptions.set(options));
  }

  private observeAdministrativeUnitRequests(): void {
    this.administrativeUnitRequests
      .pipe(
        tap((departmentCode) => {
          this.isAdministrativeUnitsLoading.set(Boolean(departmentCode));
          this.administrativeUnitsLoadFailed.set(false);
          this.filtersForm.controls.administrativeUnit.disable({ emitEvent: false });
        }),
        switchMap((departmentCode) =>
          departmentCode
            ? this.applicationOptionsService.getAdministrativeUnitOptions(departmentCode).pipe(
                map((options) => ({ departmentCode, options, failed: false })),
                catchError(() => of({ departmentCode, options: [], failed: true })),
              )
            : of({ departmentCode: null, options: [], failed: false }),
        ),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(({ departmentCode, options, failed }) => {
        this.isAdministrativeUnitsLoading.set(false);
        this.administrativeUnitsLoadFailed.set(failed);
        this.filterOptions.update((current) =>
          current ? { ...current, administrativeUnits: options } : current,
        );
        if (!failed && departmentCode === this.filtersForm.controls.conselleria.value) {
          this.filtersForm.controls.administrativeUnit.enable({ emitEvent: false });
        }
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
      incomplete: filters.incomplete ? true : undefined,
      size,
      sort: sort?.length === 1 ? sort[0] : sort,
      prefix: filters.prefix || undefined,
      applicationName: filters.application || undefined,
      categoryId: filters.category ?? undefined,
      systemTypeId: filters.informationSystem ?? undefined,
      fieldId: filters.scope ?? undefined,
      commissionId: filters.commission ?? undefined,
      admUnitCode: filters.administrativeUnit ?? undefined,
      statusId: filters.status ?? undefined,
      quickSearch: quickSearch || undefined,
      responsibleId: filters.responsible?.id,
      databaseId: filters.database ?? undefined,
      serverId: filters.server ?? undefined,
      environmentId: filters.environment ?? undefined,
    };
  }
}
