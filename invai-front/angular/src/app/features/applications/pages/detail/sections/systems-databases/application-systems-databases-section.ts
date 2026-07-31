import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  LOCALE_ID,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AbstractControl, FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { ConfirmationDialogComponent } from '@components/confirmation-dialog/confirmation-dialog.component';
import { CrudEntityDialogMode } from '@components/crud-entity-dialog/crud-entity-dialog';
import { isStructuredBadRequest } from '@core/models/api-error.model';
import { DatabasesService } from '@features/systems/services/databases.service';
import { SystemsService } from '@features/systems/services/systems.service';
import { InfrastructureStatus } from '@features/systems/systems.model';
import { ActionParams, PaginatedList } from '@models/table.model';
import { fnCountSelectedFilters } from '@shared/utils/table.utils';
import { MessageService, PrimeIcons } from 'primeng/api';
import { Button } from 'primeng/button';
import { Editor } from 'primeng/editor';
import { TableLazyLoadEvent } from 'primeng/table';
import {
  EMPTY,
  Observable,
  Subject,
  catchError,
  debounceTime,
  distinctUntilChanged,
  filter,
  finalize,
  map,
  merge,
  switchMap,
} from 'rxjs';

import {
  ApplicationDatabaseFiltersForm,
  ApplicationDatabaseRelationDialog,
  ApplicationInfrastructureList,
  ApplicationInfrastructureTableAction,
  ApplicationServerFiltersForm,
  ApplicationSystemRelationDialog,
} from '../../../../components';
import {
  toApplicationDatabaseCatalogRow,
  toApplicationSystemCatalogRow,
} from '../../../../application-infrastructure.utils';
import {
  APPLICATION_DATABASES_TABLE_COLUMNS,
  APPLICATION_SERVERS_TABLE_COLUMNS,
} from '../../../../applications.constants';
import {
  ApplicationDatabase,
  ApplicationDatabaseCatalogRow,
  ApplicationDatabaseFilters,
  ApplicationDatabasesPageParams,
  ApplicationInfrastructureStatus,
  ApplicationServer,
  ApplicationSystemCatalogRow,
  ApplicationServerFilters,
  ApplicationSystemsPageParams,
  SelectOption,
} from '../../../../applications.model';
import {
  ApplicationDatabaseFiltersFormGroup,
  ApplicationServerFiltersFormGroup,
  createApplicationDatabaseFiltersForm,
  createApplicationServerFiltersForm,
} from '../../../../forms/application-infrastructure-filter-form.factory';
import {
  createApplicationDatabaseRelationForm,
  createApplicationSystemRelationForm,
} from '../../../../forms/application-infrastructure-relation-forms.factory';
import { ApplicationDatabasesService } from '../../../../services/application-databases.service';
import { ApplicationSystemsService } from '../../../../services/application-systems.service';
import {
  APPLICATION_DETAIL_SAVE_ERROR_MESSAGE,
  APPLICATION_DETAIL_SAVE_ERROR_TITLE,
  APPLICATION_DETAIL_SECTION_SAVE_SUCCESS_MESSAGE,
  APPLICATION_DETAIL_SECTION_SAVE_SUCCESS_TITLE,
} from '../../application-detail.i18n';
import { ApplicationDetailState } from '../../application-detail-state';
import { ApplicationDetailSectionActions } from '../../components/application-detail-section-actions/application-detail-section-actions';
import { ApplicationDetailSectionLayout } from '../../components/application-detail-section-layout/application-detail-section-layout';
import {
  APPLICATION_SYSTEMS_DATABASES_DATABASE_FILTER_LABELS,
  APPLICATION_SYSTEMS_DATABASES_DATABASE_CATALOG_LOAD_ERROR,
  APPLICATION_SYSTEMS_DATABASES_DATABASE_CREATE_SUCCESS,
  APPLICATION_SYSTEMS_DATABASES_DATABASE_DELETE_ERROR,
  APPLICATION_SYSTEMS_DATABASES_DATABASE_DELETE_MESSAGE,
  APPLICATION_SYSTEMS_DATABASES_DATABASE_DELETE_SUCCESS,
  APPLICATION_SYSTEMS_DATABASES_DATABASE_DELETE_TITLE,
  APPLICATION_SYSTEMS_DATABASES_DATABASE_SAVE_ERROR,
  APPLICATION_SYSTEMS_DATABASES_DATABASE_UPDATE_SUCCESS,
  APPLICATION_SYSTEMS_DATABASES_DATABASE_NAME,
  APPLICATION_SYSTEMS_DATABASES_DATABASE_OPTIONS_LOAD_ERROR,
  APPLICATION_SYSTEMS_DATABASES_DATABASES_FILTERS_ARIA_LABEL,
  APPLICATION_SYSTEMS_DATABASES_DATABASES_LOAD_ERROR,
  APPLICATION_SYSTEMS_DATABASES_DATABASES_TITLE,
  APPLICATION_SYSTEMS_DATABASES_DOCUMENTATION_ARIA_LABEL,
  APPLICATION_SYSTEMS_DATABASES_DOCUMENTATION_LABEL,
  APPLICATION_SYSTEMS_DATABASES_DOCUMENTATION_PENDING_MESSAGE,
  APPLICATION_SYSTEMS_DATABASES_ENVIRONMENT_OPTIONS_LOAD_ERROR,
  APPLICATION_SYSTEMS_DATABASES_ERROR_TITLE,
  APPLICATION_SYSTEMS_DATABASES_INFO_TITLE,
  APPLICATION_SYSTEMS_DATABASES_OBSERVATIONS_LABEL,
  APPLICATION_SYSTEMS_DATABASES_RELATION_DELETE_CANCEL,
  APPLICATION_SYSTEMS_DATABASES_RELATION_DELETE_CONFIRM,
  APPLICATION_SYSTEMS_DATABASES_RELATION_ERROR,
  APPLICATION_SYSTEMS_DATABASES_SYSTEM_DATABASE_LOAD_ERROR,
  APPLICATION_SYSTEMS_DATABASES_SYSTEM_DATABASE_REQUIRED,
  APPLICATION_SYSTEMS_DATABASES_SERVER_CATALOG_LOAD_ERROR,
  APPLICATION_SYSTEMS_DATABASES_SERVER_CREATE_SUCCESS,
  APPLICATION_SYSTEMS_DATABASES_SERVER_DELETE_ERROR,
  APPLICATION_SYSTEMS_DATABASES_SERVER_DELETE_MESSAGE,
  APPLICATION_SYSTEMS_DATABASES_SERVER_DELETE_SUCCESS,
  APPLICATION_SYSTEMS_DATABASES_SERVER_DELETE_TITLE,
  APPLICATION_SYSTEMS_DATABASES_SERVER_SAVE_ERROR,
  APPLICATION_SYSTEMS_DATABASES_SERVER_UPDATE_SUCCESS,
  APPLICATION_SYSTEMS_DATABASES_SERVER_FILTER_LABELS,
  APPLICATION_SYSTEMS_DATABASES_SERVER_NAME,
  APPLICATION_SYSTEMS_DATABASES_SERVER_OPTIONS_LOAD_ERROR,
  APPLICATION_SYSTEMS_DATABASES_SECTION_TITLE,
  APPLICATION_SYSTEMS_DATABASES_SERVERS_FILTERS_ARIA_LABEL,
  APPLICATION_SYSTEMS_DATABASES_SERVERS_LOAD_ERROR,
  APPLICATION_SYSTEMS_DATABASES_SERVERS_TITLE,
  APPLICATION_SYSTEMS_DATABASES_UNSUPPORTED_FILTER,
  APPLICATION_SYSTEMS_DATABASES_UNSUPPORTED_FILTERS_ON_SEARCH,
  APPLICATION_SYSTEMS_DATABASES_WARNING_TITLE,
} from './application-systems-databases-section.i18n';
import {
  APPLICATION_SYSTEMS_DATABASES_RESOLVE_KEY,
  ApplicationSystemsDatabasesResolvedData,
} from './application-systems-databases-section.resolver';

const DEFAULT_PAGE_SIZE = 10;

interface UnsupportedFilterChange {
  label: string;
  value: unknown;
}

type InfrastructureRelationKind = 'system' | 'database';

interface PendingRelationDelete {
  kind: InfrastructureRelationKind;
  id: number;
}

@Component({
  selector: 'app-application-systems-databases-section',
  standalone: true,
  imports: [
    ApplicationDatabaseFiltersForm,
    ApplicationDatabaseRelationDialog,
    ApplicationDetailSectionActions,
    ApplicationDetailSectionLayout,
    ApplicationInfrastructureList,
    ApplicationServerFiltersForm,
    ApplicationSystemRelationDialog,
    Button,
    ConfirmationDialogComponent,
    Editor,
    ReactiveFormsModule,
  ],
  templateUrl: './application-systems-databases-section.html',
  styleUrl: './application-systems-databases-section.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationSystemsDatabasesSection implements OnInit {
  private readonly formBuilder = inject(FormBuilder);
  private readonly locale = inject(LOCALE_ID);
  private readonly messageService = inject(MessageService);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);
  private readonly applicationSystemsService = inject(ApplicationSystemsService);
  private readonly applicationDatabasesService = inject(ApplicationDatabasesService);
  private readonly systemsService = inject(SystemsService);
  private readonly databasesService = inject(DatabasesService);
  private readonly serverPageRequests = new Subject<ApplicationSystemsPageParams>();
  private readonly databasePageRequests = new Subject<ApplicationDatabasesPageParams>();
  private serverTableState: TableLazyLoadEvent = { first: 0, rows: DEFAULT_PAGE_SIZE };
  private databaseTableState: TableLazyLoadEvent = { first: 0, rows: DEFAULT_PAGE_SIZE };
  private systemCatalogTableState: TableLazyLoadEvent = {
    first: 0,
    rows: DEFAULT_PAGE_SIZE,
  };
  private databaseCatalogTableState: TableLazyLoadEvent = {
    first: 0,
    rows: DEFAULT_PAGE_SIZE,
  };
  private selectedSystemRelation: ApplicationServer | null = null;
  private selectedDatabaseRelation: ApplicationDatabase | null = null;
  private appliedServerFilters: Pick<ApplicationServerFilters, 'environment' | 'server'> = {
    environment: null,
    server: null,
  };
  private appliedDatabaseFilters: Pick<ApplicationDatabaseFilters, 'environment' | 'database'> = {
    environment: null,
    database: null,
  };

  protected readonly detailState = inject(ApplicationDetailState);
  protected readonly sectionTitle = APPLICATION_SYSTEMS_DATABASES_SECTION_TITLE;
  protected readonly isSaving = signal(false);
  protected readonly systemRelationForm =
    createApplicationSystemRelationForm(this.formBuilder);
  protected readonly databaseRelationForm =
    createApplicationDatabaseRelationForm(this.formBuilder);
  protected readonly systemDialogVisible = signal(false);
  protected readonly databaseDialogVisible = signal(false);
  protected readonly systemDialogMode = signal<CrudEntityDialogMode>('create');
  protected readonly databaseDialogMode = signal<CrudEntityDialogMode>('create');
  protected readonly systemCatalog = signal<PaginatedList<ApplicationSystemCatalogRow>>({
    items: [],
    total: 0,
  });
  protected readonly databaseCatalog = signal<PaginatedList<ApplicationDatabaseCatalogRow>>({
    items: [],
    total: 0,
  });
  protected readonly systemCatalogFirst = signal(0);
  protected readonly databaseCatalogFirst = signal(0);
  protected readonly isSystemCatalogLoading = signal(false);
  protected readonly isDatabaseCatalogLoading = signal(false);
  protected readonly systemCatalogLoadFailed = signal(false);
  protected readonly databaseCatalogLoadFailed = signal(false);
  protected readonly isSystemRelationSaving = signal(false);
  protected readonly isDatabaseRelationSaving = signal(false);
  protected readonly deleteDialogVisible = signal(false);
  protected readonly isDeletingRelation = signal(false);
  protected readonly pendingDelete = signal<PendingRelationDelete | null>(null);
  protected readonly deleteTitle = computed(() =>
    this.pendingDelete()?.kind === 'database'
      ? APPLICATION_SYSTEMS_DATABASES_DATABASE_DELETE_TITLE
      : APPLICATION_SYSTEMS_DATABASES_SERVER_DELETE_TITLE,
  );
  protected readonly deleteMessage = computed(() => {
    const pending = this.pendingDelete();
    if (!pending) return '';
    return pending.kind === 'database'
      ? APPLICATION_SYSTEMS_DATABASES_DATABASE_DELETE_MESSAGE(pending.id)
      : APPLICATION_SYSTEMS_DATABASES_SERVER_DELETE_MESSAGE(pending.id);
  });
  protected readonly deleteCancel = APPLICATION_SYSTEMS_DATABASES_RELATION_DELETE_CANCEL;
  protected readonly deleteConfirm = APPLICATION_SYSTEMS_DATABASES_RELATION_DELETE_CONFIRM;
  protected readonly serverFiltersForm: ApplicationServerFiltersFormGroup =
    createApplicationServerFiltersForm(this.formBuilder);
  protected readonly databaseFiltersForm: ApplicationDatabaseFiltersFormGroup =
    createApplicationDatabaseFiltersForm(this.formBuilder);
  protected readonly serversTitle = APPLICATION_SYSTEMS_DATABASES_SERVERS_TITLE;
  protected readonly serverName = APPLICATION_SYSTEMS_DATABASES_SERVER_NAME;
  protected readonly databasesTitle = APPLICATION_SYSTEMS_DATABASES_DATABASES_TITLE;
  protected readonly databaseName = APPLICATION_SYSTEMS_DATABASES_DATABASE_NAME;
  protected readonly serverFilterLabels = APPLICATION_SYSTEMS_DATABASES_SERVER_FILTER_LABELS;
  protected readonly databaseFilterLabels = APPLICATION_SYSTEMS_DATABASES_DATABASE_FILTER_LABELS;
  protected readonly serversFiltersAriaLabel =
    APPLICATION_SYSTEMS_DATABASES_SERVERS_FILTERS_ARIA_LABEL;
  protected readonly databasesFiltersAriaLabel =
    APPLICATION_SYSTEMS_DATABASES_DATABASES_FILTERS_ARIA_LABEL;
  protected readonly documentationLabel = APPLICATION_SYSTEMS_DATABASES_DOCUMENTATION_LABEL;
  protected readonly documentationAriaLabel =
    APPLICATION_SYSTEMS_DATABASES_DOCUMENTATION_ARIA_LABEL;
  protected readonly observationsLabel = APPLICATION_SYSTEMS_DATABASES_OBSERVATIONS_LABEL;
  protected readonly documentationIcon = PrimeIcons.EXTERNAL_LINK;
  protected readonly observationsId = 'application-systems-databases-observations';
  protected readonly observationsLabelId = `${this.observationsId}-label`;
  protected readonly servers = signal<PaginatedList<ApplicationServer>>({ items: [], total: 0 });
  protected readonly isServersLoading = signal(false);
  protected readonly serverColumns = APPLICATION_SERVERS_TABLE_COLUMNS;
  protected readonly serverFirst = signal(0);
  protected readonly selectedServerFilters = signal(1);
  protected readonly appliedServerStatus = signal<ApplicationInfrastructureStatus | null>(
    ApplicationInfrastructureStatus.ACTIVE,
  );
  protected readonly serverOptions = signal<SelectOption<number>[]>([]);
  protected readonly databases = signal<PaginatedList<ApplicationDatabase>>({
    items: [],
    total: 0,
  });
  protected readonly isDatabasesLoading = signal(false);
  protected readonly databaseColumns = APPLICATION_DATABASES_TABLE_COLUMNS;
  protected readonly databaseFirst = signal(0);
  protected readonly selectedDatabaseFilters = signal(1);
  protected readonly appliedDatabaseStatus = signal<ApplicationInfrastructureStatus | null>(
    ApplicationInfrastructureStatus.ACTIVE,
  );
  protected readonly databaseOptions = signal<SelectOption<number>[]>([]);
  protected readonly environmentOptions = signal<SelectOption<number>[]>([]);

  ngOnInit(): void {
    this.observeServerPageRequests();
    this.observeDatabasePageRequests();
    this.observeFilterCounts();
    this.observeUnsupportedFilters();
    this.initializeResolvedData();
  }

  protected onServersPageChange(event: TableLazyLoadEvent): void {
    this.serverTableState = event;
    this.serverFirst.set(event.first ?? 0);
    const params = this.toServerPageParams(event);
    if (params) this.serverPageRequests.next(params);
  }

  protected onDatabasesPageChange(event: TableLazyLoadEvent): void {
    this.databaseTableState = event;
    this.databaseFirst.set(event.first ?? 0);
    const params = this.toDatabasePageParams(event);
    if (params) this.databasePageRequests.next(params);
  }

  protected openSystemCreateDialog(): void {
    if (!this.canMutateRelations()) return;
    this.selectedSystemRelation = null;
    this.systemRelationForm.enable({ emitEvent: false });
    this.systemRelationForm.reset();
    this.systemDialogMode.set('create');
    this.systemDialogVisible.set(true);
    if (this.systemCatalogLoadFailed()) this.loadSystemCatalog();
  }

  protected openDatabaseCreateDialog(): void {
    if (!this.canMutateRelations()) return;
    this.selectedDatabaseRelation = null;
    this.databaseRelationForm.enable({ emitEvent: false });
    this.databaseRelationForm.reset();
    this.databaseDialogMode.set('create');
    this.databaseDialogVisible.set(true);
    if (this.databaseCatalogLoadFailed()) this.loadDatabaseCatalog();
  }

  protected onSystemTableAction(
    event: ActionParams<ApplicationServer | ApplicationDatabase>,
  ): void {
    const relation = event.params as ApplicationServer;
    switch (event.action) {
      case ApplicationInfrastructureTableAction.View:
        this.prepareSystemDialog(relation, 'view');
        break;
      case ApplicationInfrastructureTableAction.Edit:
        if (this.canMutateRelation(relation)) this.prepareSystemDialog(relation, 'edit');
        break;
      case ApplicationInfrastructureTableAction.Delete:
        if (this.canMutateRelation(relation)) {
          this.openDeleteDialog({ kind: 'system', id: relation.id });
        }
        break;
    }
  }

  protected onDatabaseTableAction(
    event: ActionParams<ApplicationServer | ApplicationDatabase>,
  ): void {
    const relation = event.params as ApplicationDatabase;
    switch (event.action) {
      case ApplicationInfrastructureTableAction.View:
        this.prepareDatabaseDialog(relation, 'view');
        break;
      case ApplicationInfrastructureTableAction.Edit:
        if (this.canMutateRelation(relation)) this.prepareDatabaseDialog(relation, 'edit');
        break;
      case ApplicationInfrastructureTableAction.Delete:
        if (this.canMutateRelation(relation)) {
          this.openDeleteDialog({ kind: 'database', id: relation.id });
        }
        break;
    }
  }

  protected startSystemEdit(): void {
    if (!this.selectedSystemRelation || !this.canEditRelation(this.selectedSystemRelation)) {
      return;
    }
    this.systemRelationForm.enable({ emitEvent: false });
    this.systemDialogMode.set('edit');
    if (this.systemCatalogLoadFailed()) this.loadSystemCatalog();
  }

  protected startDatabaseEdit(): void {
    if (
      !this.selectedDatabaseRelation ||
      !this.canEditRelation(this.selectedDatabaseRelation)
    ) {
      return;
    }
    this.databaseRelationForm.enable({ emitEvent: false });
    this.databaseDialogMode.set('edit');
    if (this.databaseCatalogLoadFailed()) this.loadDatabaseCatalog();
  }

  protected cancelSystemEdit(): void {
    if (!this.selectedSystemRelation || this.isSystemRelationSaving()) return;
    this.prepareSystemDialog(this.selectedSystemRelation, 'view');
  }

  protected cancelDatabaseEdit(): void {
    if (!this.selectedDatabaseRelation || this.isDatabaseRelationSaving()) return;
    this.prepareDatabaseDialog(this.selectedDatabaseRelation, 'view');
  }

  protected closeSystemDialog(): void {
    if (this.isSystemRelationSaving() || this.isDeletingRelation()) return;
    this.systemDialogVisible.set(false);
    this.selectedSystemRelation = null;
  }

  protected closeDatabaseDialog(): void {
    if (this.isDatabaseRelationSaving() || this.isDeletingRelation()) return;
    this.databaseDialogVisible.set(false);
    this.selectedDatabaseRelation = null;
  }

  protected submitSystemRelation(): void {
    if (this.isSystemRelationSaving() || this.isDeletingRelation()) return;
    const mode = this.systemDialogMode();
    if (
      mode === 'view' ||
      (mode === 'create' && !this.canMutateRelations()) ||
      (mode === 'edit' &&
        (!this.selectedSystemRelation || !this.canEditRelation(this.selectedSystemRelation)))
    ) {
      return;
    }
    if (this.systemRelationForm.invalid) {
      this.systemRelationForm.markAllAsTouched();
      return;
    }

    const informationSystemDbId = this.detailState.informationSystemDbId();
    if (informationSystemDbId == null) {
      this.showError(APPLICATION_SYSTEMS_DATABASES_SYSTEM_DATABASE_REQUIRED);
      return;
    }

    const payload = {
      informationSystemDbId,
      systemId: this.systemRelationForm.getRawValue().system!.id,
    };
    const request =
      mode === 'create'
        ? this.applicationSystemsService.create(payload)
        : this.applicationSystemsService.update(this.selectedSystemRelation!.id, payload);

    this.isSystemRelationSaving.set(true);
    request
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.isSystemRelationSaving.set(false)),
      )
      .subscribe({
        next: () => {
          this.showSuccess(
            mode === 'create'
              ? APPLICATION_SYSTEMS_DATABASES_SERVER_CREATE_SUCCESS
              : APPLICATION_SYSTEMS_DATABASES_SERVER_UPDATE_SUCCESS,
          );
          this.systemDialogVisible.set(false);
          this.selectedSystemRelation = null;
          this.refreshServersAfterMutation();
        },
        error: (error: unknown) =>
          this.handleMutationError(error, APPLICATION_SYSTEMS_DATABASES_SERVER_SAVE_ERROR),
      });
  }

  protected submitDatabaseRelation(): void {
    if (this.isDatabaseRelationSaving() || this.isDeletingRelation()) return;
    const mode = this.databaseDialogMode();
    if (
      mode === 'view' ||
      (mode === 'create' && !this.canMutateRelations()) ||
      (mode === 'edit' &&
        (!this.selectedDatabaseRelation || !this.canEditRelation(this.selectedDatabaseRelation)))
    ) {
      return;
    }
    if (this.databaseRelationForm.invalid) {
      this.databaseRelationForm.markAllAsTouched();
      return;
    }

    const informationSystemDbId = this.detailState.informationSystemDbId();
    if (informationSystemDbId == null) {
      this.showError(APPLICATION_SYSTEMS_DATABASES_SYSTEM_DATABASE_REQUIRED);
      return;
    }

    const payload = {
      informationSystemDbId,
      databaseId: this.databaseRelationForm.getRawValue().database!.id,
    };
    const request =
      mode === 'create'
        ? this.applicationDatabasesService.create(payload)
        : this.applicationDatabasesService.update(
            this.selectedDatabaseRelation!.id,
            payload,
          );

    this.isDatabaseRelationSaving.set(true);
    request
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.isDatabaseRelationSaving.set(false)),
      )
      .subscribe({
        next: () => {
          this.showSuccess(
            mode === 'create'
              ? APPLICATION_SYSTEMS_DATABASES_DATABASE_CREATE_SUCCESS
              : APPLICATION_SYSTEMS_DATABASES_DATABASE_UPDATE_SUCCESS,
          );
          this.databaseDialogVisible.set(false);
          this.selectedDatabaseRelation = null;
          this.refreshDatabasesAfterMutation();
        },
        error: (error: unknown) =>
          this.handleMutationError(error, APPLICATION_SYSTEMS_DATABASES_DATABASE_SAVE_ERROR),
      });
  }

  protected deleteSelectedSystem(): void {
    if (this.selectedSystemRelation) {
      this.openDeleteDialog({ kind: 'system', id: this.selectedSystemRelation.id });
    }
  }

  protected deleteSelectedDatabase(): void {
    if (this.selectedDatabaseRelation) {
      this.openDeleteDialog({ kind: 'database', id: this.selectedDatabaseRelation.id });
    }
  }

  protected onSystemCatalogPageChange(event: TableLazyLoadEvent): void {
    this.systemCatalogTableState = event;
    this.systemCatalogFirst.set(event.first ?? 0);
    this.loadSystemCatalog();
  }

  protected onDatabaseCatalogPageChange(event: TableLazyLoadEvent): void {
    this.databaseCatalogTableState = event;
    this.databaseCatalogFirst.set(event.first ?? 0);
    this.loadDatabaseCatalog();
  }

  protected closeDeleteDialog(): void {
    this.deleteDialogVisible.set(false);
    if (!this.isDeletingRelation()) this.pendingDelete.set(null);
  }

  protected confirmDelete(): void {
    const pending = this.pendingDelete();
    if (!pending || this.isDeletingRelation()) return;

    this.deleteDialogVisible.set(false);
    this.isDeletingRelation.set(true);
    const request =
      pending.kind === 'system'
        ? this.applicationSystemsService.delete(pending.id)
        : this.applicationDatabasesService.delete(pending.id);

    request
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.isDeletingRelation.set(false)),
      )
      .subscribe({
        next: () => {
          this.pendingDelete.set(null);
          if (pending.kind === 'system') {
            this.systemDialogVisible.set(false);
            this.selectedSystemRelation = null;
            this.showSuccess(APPLICATION_SYSTEMS_DATABASES_SERVER_DELETE_SUCCESS);
            this.refreshServersAfterMutation();
          } else {
            this.databaseDialogVisible.set(false);
            this.selectedDatabaseRelation = null;
            this.showSuccess(APPLICATION_SYSTEMS_DATABASES_DATABASE_DELETE_SUCCESS);
            this.refreshDatabasesAfterMutation();
          }
        },
        error: (error: unknown) => {
          this.pendingDelete.set(null);
          this.handleMutationError(
            error,
            pending.kind === 'system'
              ? APPLICATION_SYSTEMS_DATABASES_SERVER_DELETE_ERROR
              : APPLICATION_SYSTEMS_DATABASES_DATABASE_DELETE_ERROR,
          );
        },
      });
  }

  protected onServerFilterSearch(): void {
    if (this.hasUnsupportedServerFilters()) this.showUnsupportedSearchWarning();

    const filters = this.serverFiltersForm.getRawValue();
    this.appliedServerFilters = {
      environment: filters.environment,
      server: filters.server,
    };
    this.appliedServerStatus.set(filters.status);
    this.serverTableState = { ...this.serverTableState, first: 0 };
    this.serverFirst.set(0);
    const params = this.toServerPageParams(this.serverTableState);
    if (params) this.serverPageRequests.next(params);
  }

  protected onDatabaseFilterSearch(): void {
    if (this.hasUnsupportedDatabaseFilters()) this.showUnsupportedSearchWarning();

    const filters = this.databaseFiltersForm.getRawValue();
    this.appliedDatabaseFilters = {
      environment: filters.environment,
      database: filters.database,
    };
    this.appliedDatabaseStatus.set(filters.status);
    this.databaseTableState = { ...this.databaseTableState, first: 0 };
    this.databaseFirst.set(0);
    const params = this.toDatabasePageParams(this.databaseTableState);
    if (params) this.databasePageRequests.next(params);
  }

  protected onServerFilterReset(): void {
    this.serverFiltersForm.reset(undefined, { emitEvent: false });
    this.selectedServerFilters.set(fnCountSelectedFilters(this.serverFiltersForm));
    this.appliedServerFilters = {
      environment: null,
      server: null,
    };
    this.appliedServerStatus.set(ApplicationInfrastructureStatus.ACTIVE);
    this.serverTableState = { ...this.serverTableState, first: 0 };
    this.serverFirst.set(0);
    const params = this.toServerPageParams(this.serverTableState);
    if (params) this.serverPageRequests.next(params);
  }

  protected onDatabaseFilterReset(): void {
    this.databaseFiltersForm.reset(undefined, { emitEvent: false });
    this.selectedDatabaseFilters.set(fnCountSelectedFilters(this.databaseFiltersForm));
    this.appliedDatabaseFilters = {
      environment: null,
      database: null,
    };
    this.appliedDatabaseStatus.set(ApplicationInfrastructureStatus.ACTIVE);
    this.databaseTableState = { ...this.databaseTableState, first: 0 };
    this.databaseFirst.set(0);
    const params = this.toDatabasePageParams(this.databaseTableState);
    if (params) this.databasePageRequests.next(params);
  }

  protected onDocumentation(): void {
    this.messageService.add({
      severity: 'info',
      summary: APPLICATION_SYSTEMS_DATABASES_INFO_TITLE,
      detail: APPLICATION_SYSTEMS_DATABASES_DOCUMENTATION_PENDING_MESSAGE,
    });
  }

  protected startEditing(): void {
    this.detailState.startEditing('systems-databases');
  }

  protected cancelEditing(): void {
    this.detailState.cancelEditing('systems-databases');
  }

  protected save(): void {
    if (this.isSaving()) return;

    this.isSaving.set(true);
    this.detailState
      .save('systems-databases')
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (result) => {
          this.isSaving.set(false);
          if (result.status === 'invalid') {
            this.showError(APPLICATION_SYSTEMS_DATABASES_SYSTEM_DATABASE_REQUIRED);
            return;
          }
          if (result.status !== 'saved') return;
          this.messageService.add({
            severity: 'success',
            summary: APPLICATION_DETAIL_SECTION_SAVE_SUCCESS_TITLE,
            detail: APPLICATION_DETAIL_SECTION_SAVE_SUCCESS_MESSAGE(this.sectionTitle),
          });
        },
        error: (error: unknown) => {
          this.isSaving.set(false);
          if (isStructuredBadRequest(error)) return;
          this.messageService.add({
            severity: 'error',
            summary: APPLICATION_DETAIL_SAVE_ERROR_TITLE,
            detail: APPLICATION_DETAIL_SAVE_ERROR_MESSAGE,
          });
        },
      });
  }

  protected canEditSelectedSystemRelation(): boolean {
    return (
      this.selectedSystemRelation != null && this.canEditRelation(this.selectedSystemRelation)
    );
  }

  protected canEditSelectedDatabaseRelation(): boolean {
    return (
      this.selectedDatabaseRelation != null && this.canEditRelation(this.selectedDatabaseRelation)
    );
  }

  protected canMutateSelectedSystemRelation(): boolean {
    return (
      this.selectedSystemRelation != null && this.canMutateRelation(this.selectedSystemRelation)
    );
  }

  protected canMutateSelectedDatabaseRelation(): boolean {
    return (
      this.selectedDatabaseRelation != null &&
      this.canMutateRelation(this.selectedDatabaseRelation)
    );
  }

  private prepareSystemDialog(
    relation: ApplicationServer,
    mode: Exclude<CrudEntityDialogMode, 'create'>,
  ): void {
    this.selectedSystemRelation = relation;
    this.systemRelationForm.enable({ emitEvent: false });
    this.systemRelationForm.reset({ system: relation.catalogItem });
    if (mode === 'view') this.systemRelationForm.disable({ emitEvent: false });
    this.systemDialogMode.set(mode);
    this.systemDialogVisible.set(true);
  }

  private prepareDatabaseDialog(
    relation: ApplicationDatabase,
    mode: Exclude<CrudEntityDialogMode, 'create'>,
  ): void {
    this.selectedDatabaseRelation = relation;
    this.databaseRelationForm.enable({ emitEvent: false });
    this.databaseRelationForm.reset({ database: relation.catalogItem });
    if (mode === 'view') this.databaseRelationForm.disable({ emitEvent: false });
    this.databaseDialogMode.set(mode);
    this.databaseDialogVisible.set(true);
  }

  private openDeleteDialog(pending: PendingRelationDelete): void {
    if (!this.canMutateRelations() || this.isDeletingRelation()) return;
    this.pendingDelete.set(pending);
    this.deleteDialogVisible.set(true);
  }

  private loadSystemCatalog(): void {
    this.isSystemCatalogLoading.set(true);
    this.systemsService
      .getAll(this.toCatalogPageParams(this.systemCatalogTableState))
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.isSystemCatalogLoading.set(false)),
      )
      .subscribe({
        next: (page) => {
          this.systemCatalogLoadFailed.set(false);
          this.systemCatalog.set({
            items: page.content.map((system) =>
              toApplicationSystemCatalogRow(system, this.locale),
            ),
            total: page.totalElements,
          });
        },
        error: () => {
          this.systemCatalogLoadFailed.set(true);
          this.showLoadError(APPLICATION_SYSTEMS_DATABASES_SERVER_CATALOG_LOAD_ERROR);
        },
      });
  }

  private loadDatabaseCatalog(): void {
    this.isDatabaseCatalogLoading.set(true);
    this.databasesService
      .getAll(this.toCatalogPageParams(this.databaseCatalogTableState))
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.isDatabaseCatalogLoading.set(false)),
      )
      .subscribe({
        next: (page) => {
          this.databaseCatalogLoadFailed.set(false);
          this.databaseCatalog.set({
            items: page.content.map((database) =>
              toApplicationDatabaseCatalogRow(database, this.locale),
            ),
            total: page.totalElements,
          });
        },
        error: () => {
          this.databaseCatalogLoadFailed.set(true);
          this.showLoadError(APPLICATION_SYSTEMS_DATABASES_DATABASE_CATALOG_LOAD_ERROR);
        },
      });
  }

  private toCatalogPageParams(event: TableLazyLoadEvent) {
    return {
      ...this.toCommonPageParams(event),
      statusId: InfrastructureStatus.ACTIVE,
    };
  }

  private refreshServersAfterMutation(): void {
    this.serverTableState = { ...this.serverTableState, first: 0 };
    this.serverFirst.set(0);
    const params = this.toServerPageParams(this.serverTableState);
    if (params) this.serverPageRequests.next(params);
  }

  private refreshDatabasesAfterMutation(): void {
    this.databaseTableState = { ...this.databaseTableState, first: 0 };
    this.databaseFirst.set(0);
    const params = this.toDatabasePageParams(this.databaseTableState);
    if (params) this.databasePageRequests.next(params);
  }

  private canMutateRelations(): boolean {
    return (
      this.detailState.isEditing('systems-databases') &&
      this.detailState.canEdit() &&
      !this.isDeletingRelation()
    );
  }

  private canMutateRelation(
    relation: ApplicationServer | ApplicationDatabase,
  ): boolean {
    return this.canMutateRelations() && !relation.deletedAt;
  }

  private canEditRelation(relation: ApplicationServer | ApplicationDatabase): boolean {
    return this.detailState.canEdit() && !relation.deletedAt && !this.isDeletingRelation();
  }

  private handleMutationError(error: unknown, detail: string): void {
    if (isStructuredBadRequest(error)) return;
    this.showError(detail);
  }

  private showSuccess(detail: string): void {
    this.messageService.add({
      severity: 'success',
      summary: APPLICATION_DETAIL_SECTION_SAVE_SUCCESS_TITLE,
      detail,
    });
  }

  private showError(detail: string): void {
    this.messageService.add({
      severity: 'error',
      summary: APPLICATION_SYSTEMS_DATABASES_RELATION_ERROR,
      detail,
    });
  }

  private initializeResolvedData(): void {
    const resolvedData = this.route.snapshot.data[
      APPLICATION_SYSTEMS_DATABASES_RESOLVE_KEY
    ] as ApplicationSystemsDatabasesResolvedData;

    this.detailState.informationSystemDbId.set(resolvedData.informationSystemDbId);
    this.servers.set({
      items: resolvedData.serversPage?.content ?? [],
      total: resolvedData.serversPage?.totalElements ?? 0,
    });
    this.databases.set({
      items: resolvedData.databasesPage?.content ?? [],
      total: resolvedData.databasesPage?.totalElements ?? 0,
    });
    this.systemCatalog.set({
      items:
        resolvedData.systemCatalogPage?.content.map((system) =>
          toApplicationSystemCatalogRow(system, this.locale),
        ) ?? [],
      total: resolvedData.systemCatalogPage?.totalElements ?? 0,
    });
    this.databaseCatalog.set({
      items:
        resolvedData.databaseCatalogPage?.content.map((database) =>
          toApplicationDatabaseCatalogRow(database, this.locale),
        ) ?? [],
      total: resolvedData.databaseCatalogPage?.totalElements ?? 0,
    });
    this.systemCatalogLoadFailed.set(resolvedData.systemCatalogLoadFailed);
    this.databaseCatalogLoadFailed.set(resolvedData.databaseCatalogLoadFailed);
    this.detailState.initializeSystemsDatabases(resolvedData.systemDatabase);
    this.serverOptions.set(resolvedData.filterOptions.servers);
    this.databaseOptions.set(resolvedData.filterOptions.databases);
    this.environmentOptions.set(resolvedData.filterOptions.environments);

    if (resolvedData.serversLoadFailed) {
      this.showLoadError(APPLICATION_SYSTEMS_DATABASES_SERVERS_LOAD_ERROR);
    }
    if (resolvedData.databasesLoadFailed) {
      this.showLoadError(APPLICATION_SYSTEMS_DATABASES_DATABASES_LOAD_ERROR);
    }
    if (resolvedData.systemCatalogLoadFailed) {
      this.showLoadError(APPLICATION_SYSTEMS_DATABASES_SERVER_CATALOG_LOAD_ERROR);
    }
    if (resolvedData.databaseCatalogLoadFailed) {
      this.showLoadError(APPLICATION_SYSTEMS_DATABASES_DATABASE_CATALOG_LOAD_ERROR);
    }
    if (resolvedData.systemDatabaseLoadFailed) {
      this.showLoadError(APPLICATION_SYSTEMS_DATABASES_SYSTEM_DATABASE_LOAD_ERROR);
    }
    if (resolvedData.filterOptionsLoadFailed.servers) {
      this.showLoadError(APPLICATION_SYSTEMS_DATABASES_SERVER_OPTIONS_LOAD_ERROR);
    }
    if (resolvedData.filterOptionsLoadFailed.databases) {
      this.showLoadError(APPLICATION_SYSTEMS_DATABASES_DATABASE_OPTIONS_LOAD_ERROR);
    }
    if (resolvedData.filterOptionsLoadFailed.environments) {
      this.showLoadError(APPLICATION_SYSTEMS_DATABASES_ENVIRONMENT_OPTIONS_LOAD_ERROR);
    }
  }

  private observeServerPageRequests(): void {
    this.serverPageRequests
      .pipe(
        switchMap((params) => {
          this.isServersLoading.set(true);
          return this.applicationSystemsService.getPage(params).pipe(
            catchError(() => {
              this.servers.set({ items: [], total: 0 });
              this.showLoadError(APPLICATION_SYSTEMS_DATABASES_SERVERS_LOAD_ERROR);
              return EMPTY;
            }),
            finalize(() => this.isServersLoading.set(false)),
          );
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((page) => {
        this.servers.set({ items: page.content, total: page.totalElements });
      });
  }

  private observeDatabasePageRequests(): void {
    this.databasePageRequests
      .pipe(
        switchMap((params) => {
          this.isDatabasesLoading.set(true);
          return this.applicationDatabasesService.getPage(params).pipe(
            catchError(() => {
              this.databases.set({ items: [], total: 0 });
              this.showLoadError(APPLICATION_SYSTEMS_DATABASES_DATABASES_LOAD_ERROR);
              return EMPTY;
            }),
            finalize(() => this.isDatabasesLoading.set(false)),
          );
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((page) => {
        this.databases.set({ items: page.content, total: page.totalElements });
      });
  }

  private observeFilterCounts(): void {
    this.serverFiltersForm.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.selectedServerFilters.set(fnCountSelectedFilters(this.serverFiltersForm)));
    this.databaseFiltersForm.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() =>
        this.selectedDatabaseFilters.set(fnCountSelectedFilters(this.databaseFiltersForm)),
      );
  }

  private observeUnsupportedFilters(): void {
    this.observeUnsupportedFilterChanges([
      {
        control: this.serverFiltersForm.controls.environment,
        label: this.serverFilterLabels.environment,
      },
      { control: this.serverFiltersForm.controls.instance, label: this.serverFilterLabels.instance },
      { control: this.serverFiltersForm.controls.port, label: this.serverFilterLabels.port },
      { control: this.serverFiltersForm.controls.version, label: this.serverFilterLabels.version },
      {
        control: this.serverFiltersForm.controls.observations,
        label: this.serverFilterLabels.observations,
      },
      {
        control: this.databaseFiltersForm.controls.environment,
        label: this.databaseFilterLabels.environment,
      },
      { control: this.databaseFiltersForm.controls.server, label: this.databaseFilterLabels.server },
      {
        control: this.databaseFiltersForm.controls.version,
        label: this.databaseFilterLabels.version,
      },
      {
        control: this.databaseFiltersForm.controls.service,
        label: this.databaseFilterLabels.service,
      },
      { control: this.databaseFiltersForm.controls.port, label: this.databaseFilterLabels.port },
      { control: this.databaseFiltersForm.controls.type, label: this.databaseFilterLabels.type },
      {
        control: this.databaseFiltersForm.controls.observations,
        label: this.databaseFilterLabels.observations,
      },
    ]);
  }

  private observeUnsupportedFilterChanges(
    entries: { control: AbstractControl; label: string }[],
  ): void {
    const changes: Observable<UnsupportedFilterChange>[] = entries.map(({ control, label }) =>
      control.valueChanges.pipe(
        distinctUntilChanged(),
        map((value) => ({ label, value })),
      ),
    );

    merge(...changes)
      .pipe(
        debounceTime(400),
        filter(({ value }) => this.hasValue(value)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(({ label }) => {
        this.messageService.add({
          severity: 'warn',
          summary: APPLICATION_SYSTEMS_DATABASES_WARNING_TITLE,
          detail: APPLICATION_SYSTEMS_DATABASES_UNSUPPORTED_FILTER(label),
        });
      });
  }

  private toServerPageParams(event: TableLazyLoadEvent): ApplicationSystemsPageParams | null {
    const informationSystemDbId = this.detailState.informationSystemDbId();
    if (informationSystemDbId == null) return null;

    const params: ApplicationSystemsPageParams = {
      ...this.toCommonPageParams(event),
      informationSystemDbId,
    };
    const status = this.appliedServerStatus();
    if (status != null) params.statusId = status;
    if (this.appliedServerFilters.server != null) params.systemId = this.appliedServerFilters.server;
    return params;
  }

  private toDatabasePageParams(event: TableLazyLoadEvent): ApplicationDatabasesPageParams | null {
    const informationSystemDbId = this.detailState.informationSystemDbId();
    if (informationSystemDbId == null) return null;

    const params: ApplicationDatabasesPageParams = {
      ...this.toCommonPageParams(event),
      informationSystemDbId,
    };
    const status = this.appliedDatabaseStatus();
    if (status != null) params.statusId = status;
    if (this.appliedDatabaseFilters.database != null) {
      params.databaseId = this.appliedDatabaseFilters.database;
    }
    return params;
  }

  private toCommonPageParams(event: TableLazyLoadEvent) {
    const first = event.first ?? 0;
    const size = event.rows ?? DEFAULT_PAGE_SIZE;
    const sortField = Array.isArray(event.sortField) ? event.sortField[0] : event.sortField;
    const sortDirection = event.sortOrder === -1 ? 'desc' : event.sortOrder === 1 ? 'asc' : null;

    return {
      page: Math.floor(first / size),
      size,
      sort: sortField && sortDirection ? `${sortField},${sortDirection}` : undefined,
    };
  }

  private hasUnsupportedServerFilters(): boolean {
    const { environment, instance, port, version, observations } =
      this.serverFiltersForm.getRawValue();
    return [environment, instance, port, version, observations].some((value) =>
      this.hasValue(value),
    );
  }

  private hasUnsupportedDatabaseFilters(): boolean {
    const { environment, server, version, service, port, type, observations } =
      this.databaseFiltersForm.getRawValue();
    return [environment, server, version, service, port, type, observations].some((value) =>
      this.hasValue(value),
    );
  }

  private hasValue(value: unknown): boolean {
    return typeof value === 'string' ? value.trim().length > 0 : value !== null && value !== undefined;
  }

  private showUnsupportedSearchWarning(): void {
    this.messageService.add({
      severity: 'warn',
      summary: APPLICATION_SYSTEMS_DATABASES_WARNING_TITLE,
      detail: APPLICATION_SYSTEMS_DATABASES_UNSUPPORTED_FILTERS_ON_SEARCH,
    });
  }

  private showLoadError(detail: string): void {
    this.messageService.add({
      severity: 'error',
      summary: APPLICATION_SYSTEMS_DATABASES_ERROR_TITLE,
      detail,
    });
  }
}
