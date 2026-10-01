import { DestroyRef, Injectable, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder } from '@angular/forms';
import { CrudEntityDialogMode } from '@components/crud-entity-dialog/crud-entity-dialog';
import { isStructuredBadRequest, readApiErrorMessage } from '@core/models/api-error.model';
import { ExternalSystemCatalogOption } from '@features/external-systems/external-systems.model';
import {
  ExternalSystemCatalogService,
  toExternalSystemOption,
} from '@features/external-systems/services/external-system-catalog.service';
import {
  SoffidPersonCandidate,
  SoffidPersonOption,
} from '@features/maintenances/responsibles/responsibles.model';
import { ResponsiblePeopleService } from '@features/maintenances/responsibles/services/responsible-people.service';
import { TechnologyCatalogOption } from '@features/technologies/technologies.model';
import { TechnologyCatalogService } from '@features/technologies/services/technology-catalog.service';
import { ActionParams, PaginatedList } from '@models/table.model';
import { PAGINATOR_ROWS } from '@shared/constants/table.constants';
import { MessageService } from 'primeng/api';
import { TableLazyLoadEvent } from 'primeng/table';
import {
  EMPTY,
  Observable,
  Subject,
  catchError,
  finalize,
  forkJoin,
  map,
  switchMap,
  takeUntil,
} from 'rxjs';

import {
  ApplicationIntegrationConnectionInput,
  ApplicationIntegrationConnectionOutput,
  ApplicationSecurityRole,
  ApplicationStatus,
  SoffidRole,
} from '../../../../applications.model';
import {
  ApplicationIntegrationRoleOption,
  EMPTY_APPLICATION_INTEGRATION_CONNECTION_VALUE,
  configureApplicationIntegrationSystemKind,
  createApplicationIntegrationConnectionForm,
} from '../../../../forms/application-integration-connection-form.factory';
import { ApplicationIntegrationConnectionsService } from '../../../../services/application-integration-connections.service';
import { ApplicationsService } from '../../../../services/applications.service';
import { SoffidRolesService } from '../../../../services/soffid-roles.service';
import { ApplicationDetailState } from '../../application-detail-state';
import {
  APPLICATION_INTEGRATIONS_ERROR_TITLE,
  APPLICATION_INTEGRATIONS_LOAD_ERROR,
  APPLICATION_INTEGRATIONS_MESSAGES,
  APPLICATION_INTEGRATIONS_SUCCESS_TITLE,
} from './application-integrations-section.i18n';
import {
  APPLICATION_INTEGRATION_CONNECTIONS_INITIAL_PARAMS,
  ApplicationIntegrationsLoadResult,
} from './application-integrations-section.resolver';
import { ApplicationIntegrationsTableAction } from './application-integrations-table';

export interface ApplicationIntegrationApplicationOption {
  id: number;
  label: string;
}

interface SearchState<TOption> {
  options: TOption[];
  loading: boolean;
  searched: boolean;
  error: boolean;
  total: number;
}

const APPLICATION_SEARCH_SIZE = 20;
const SOFFID_MIN_QUERY_LENGTH = 3;

function emptySearch<TOption>(): SearchState<TOption> {
  return { options: [], loading: false, searched: false, error: false, total: 0 };
}

export function toIntegrationRoleOption(
  role: Pick<SoffidRole, 'id' | 'name' | 'system'>,
): ApplicationIntegrationRoleOption {
  const name = role.name?.trim() || `#${role.id}`;
  const system = role.system?.trim();
  return { roleId: role.id, label: system ? `${name} — ${system}` : name };
}

/** Scoped to Integracions: owns the connections workflow; table and dialog stay renderers. */
@Injectable()
export class ApplicationIntegrationConnectionsState {
  private readonly detail = inject(ApplicationDetailState);
  private readonly service = inject(ApplicationIntegrationConnectionsService);
  private readonly applicationsService = inject(ApplicationsService);
  private readonly technologyCatalog = inject(TechnologyCatalogService);
  private readonly externalSystemCatalog = inject(ExternalSystemCatalogService);
  private readonly peopleService = inject(ResponsiblePeopleService);
  private readonly soffidRoles = inject(SoffidRolesService);
  private readonly messages = inject(MessageService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly cancelPage = new Subject<void>();
  private readonly applicationQueries = new Subject<string>();
  private readonly userQueries = new Subject<string>();
  private readonly roleQueries = new Subject<string>();
  private pageRequestVersion = 0;
  private tableState: TableLazyLoadEvent = { first: 0, rows: PAGINATOR_ROWS };
  private catalogsLoaded = false;
  private readonly technologyCatalogOptions = signal<TechnologyCatalogOption[]>([]);
  private readonly externalSystemCatalogOptions = signal<ExternalSystemCatalogOption[]>([]);

  readonly items = signal<PaginatedList<ApplicationIntegrationConnectionOutput>>({
    items: [],
    total: 0,
  });
  readonly first = signal(0);
  readonly loading = signal(false);
  // Localized reason when the page could not be listed, e.g. Soffid is unavailable.
  readonly loadError = signal<string | null>(null);
  readonly saving = signal(false);
  readonly deleting = signal(false);
  readonly catalogsLoading = signal(false);
  readonly visible = signal(false);
  readonly mode = signal<CrudEntityDialogMode>('view');
  readonly selected = signal<ApplicationIntegrationConnectionOutput | null>(null);
  readonly pendingDelete = signal<ApplicationIntegrationConnectionOutput | null>(null);
  readonly deleteVisible = signal(false);
  readonly applicationSearch = signal(emptySearch<ApplicationIntegrationApplicationOption>());
  readonly userSearch = signal(emptySearch<SoffidPersonOption>());
  readonly roleSearch = signal(emptySearch<ApplicationIntegrationRoleOption>());
  readonly form = createApplicationIntegrationConnectionForm(inject(FormBuilder));

  readonly appIntegrationId = computed(() => this.detail.appIntegrationId());
  readonly showActions = computed(
    () => this.detail.canEdit() && this.detail.isEditing('integrations'),
  );
  readonly canManage = computed(
    () =>
      this.showActions() &&
      this.appIntegrationId() != null &&
      this.detail.integrationStatus() === 'loaded' &&
      !this.saving() &&
      !this.deleting(),
  );
  readonly canEdit = computed(() => this.canManage() && !this.selected()?.deletedAt);
  readonly technologyOptions = computed(() => {
    const selected = this.selected()?.technology;
    const options = this.technologyCatalogOptions().map(({ id, label }) => ({ id, label }));
    return selected && !options.some((option) => option.id === selected.id)
      ? [...options, { id: selected.id, label: selected.name?.trim() || `#${selected.id}` }]
      : options;
  });
  readonly externalSystemOptions = computed(() => {
    const selected = this.selected()?.externalSystem;
    const options = this.externalSystemCatalogOptions();
    return selected && !options.some((option) => option.id === selected.id)
      ? [...options, toExternalSystemOption(selected)]
      : options;
  });

  constructor() {
    this.applicationQueries
      .pipe(
        switchMap((query) => this.loadApplications(query)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe();
    this.userQueries
      .pipe(
        switchMap((query) => this.loadUsers(query)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe();
    this.roleQueries
      .pipe(
        switchMap((query) => this.loadRoles(query)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe();
    this.form.controls.systemKind.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => configureApplicationIntegrationSystemKind(this.form));
  }

  initialize(data: ApplicationIntegrationsLoadResult): void {
    this.cancelPage.next();
    this.pageRequestVersion++;
    this.loading.set(false);
    this.tableState = { first: 0, rows: PAGINATOR_ROWS };
    this.first.set(0);
    this.items.set({
      items: data.connectionsPage?.content ?? [],
      total: data.connectionsPage?.totalElements ?? 0,
    });
    this.loadError.set(
      data.connectionsLoadFailed
        ? (data.connectionsErrorMessage ?? APPLICATION_INTEGRATIONS_LOAD_ERROR)
        : null,
    );
  }

  onAction(event: ActionParams<ApplicationIntegrationConnectionOutput>): void {
    const row = event.params;
    if (event.action === ApplicationIntegrationsTableAction.View) this.open(row, 'view');
    if (event.action === ApplicationIntegrationsTableAction.Edit && this.canManage() && !row.deletedAt) {
      this.open(row, 'edit');
    }
    if (event.action === ApplicationIntegrationsTableAction.Delete) this.requestDelete(row);
  }

  create(): void {
    if (!this.canManage()) return;
    this.selected.set(null);
    this.resetSearches();
    this.form.reset({ ...EMPTY_APPLICATION_INTEGRATION_CONNECTION_VALUE }, { emitEvent: false });
    configureApplicationIntegrationSystemKind(this.form);
    this.mode.set('create');
    this.visible.set(true);
    this.ensureCatalogs();
  }

  edit(): void {
    if (!this.canEdit()) return;
    this.mode.set('edit');
    this.ensureCatalogs();
  }

  cancelEdit(): void {
    const selected = this.selected();
    if (selected) this.open(selected, 'view');
  }

  close(): void {
    this.visible.set(false);
    this.selected.set(null);
    this.resetSearches();
    this.form.reset({ ...EMPTY_APPLICATION_INTEGRATION_CONNECTION_VALUE }, { emitEvent: false });
    configureApplicationIntegrationSystemKind(this.form);
  }

  searchApplications(query: string): void {
    this.applicationQueries.next(query);
  }

  searchUsers(query: string): void {
    this.userQueries.next(query);
  }

  searchRoles(query: string): void {
    this.roleQueries.next(query);
  }

  save(): void {
    const appIntegrationId = this.appIntegrationId();
    if (!this.canEdit() || this.mode() === 'view' || appIntegrationId == null) return;
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const value = this.form.getRawValue();
    const user = value.user;
    if (!user || typeof user === 'string' || !user.userName?.trim()) return;
    const external = value.systemKind === 'external';
    const payload: ApplicationIntegrationConnectionInput = {
      appIntegrationId,
      applicationId: external ? null : value.applicationId,
      externalSystemId: external ? value.externalSystemId : null,
      technologyId: value.technologyId!,
      username: user.userName.trim(),
      requiredRoleIds: value.requiredRoles.map((role) => role.roleId),
    };
    const id = this.selected()?.id;
    const request = id == null ? this.service.create(payload) : this.service.update(id, payload);
    this.saving.set(true);
    request
      .pipe(
        finalize(() => this.saving.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.close();
          // Write responses omit the roles resolved against Soffid, so the page is reloaded.
          this.loadPage();
          this.detail.refreshCompletenessAfterMutation();
          this.success(APPLICATION_INTEGRATIONS_MESSAGES.saveSuccess);
        },
        error: (error: unknown) => this.mutationError(error, APPLICATION_INTEGRATIONS_MESSAGES.saveError),
      });
  }

  requestDelete(row = this.selected()): void {
    if (!this.canManage() || !row || row.deletedAt) return;
    this.pendingDelete.set(row);
    this.deleteVisible.set(true);
  }

  closeDelete(): void {
    this.deleteVisible.set(false);
    this.pendingDelete.set(null);
  }

  confirmDelete(): void {
    const row = this.pendingDelete();
    if (!this.canManage() || !row || row.deletedAt) return;
    this.deleting.set(true);
    this.service
      .delete(row.id)
      .pipe(
        finalize(() => this.deleting.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.closeDelete();
          this.close();
          this.loadPage();
          this.detail.refreshCompletenessAfterMutation();
          this.success(APPLICATION_INTEGRATIONS_MESSAGES.deleteSuccess);
        },
        error: (error: unknown) =>
          this.mutationError(error, APPLICATION_INTEGRATIONS_MESSAGES.deleteError),
      });
  }

  onPage(event: TableLazyLoadEvent): void {
    this.tableState = event;
    this.first.set(event.first ?? 0);
    this.loadPage();
  }

  reload(): void {
    if (!this.loading()) this.loadPage();
  }

  private loadPage(): void {
    const appIntegrationId = this.appIntegrationId();
    if (appIntegrationId == null) return;
    const requestVersion = ++this.pageRequestVersion;
    this.cancelPage.next();
    this.loading.set(true);
    const rows = this.tableState.rows ?? PAGINATOR_ROWS;
    const sortField =
      typeof this.tableState.sortField === 'string' ? this.tableState.sortField : 'id';
    this.service
      .getPage({
        ...APPLICATION_INTEGRATION_CONNECTIONS_INITIAL_PARAMS,
        appIntegrationId,
        page: Math.floor(this.first() / rows),
        size: rows,
        sort: `${sortField},${this.tableState.sortOrder === -1 ? 'desc' : 'asc'}`,
      })
      .pipe(
        takeUntil(this.cancelPage),
        finalize(() => {
          if (requestVersion === this.pageRequestVersion) this.loading.set(false);
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (page) => {
          if (page.content.length === 0 && page.number > 0) {
            this.onPage({ ...this.tableState, first: (page.number - 1) * rows });
            return;
          }
          this.loadError.set(null);
          this.items.set({ items: page.content, total: page.totalElements });
        },
        error: (error: unknown) =>
          this.loadError.set(readApiErrorMessage(error) ?? APPLICATION_INTEGRATIONS_LOAD_ERROR),
      });
  }

  private open(row: ApplicationIntegrationConnectionOutput, mode: CrudEntityDialogMode): void {
    this.selected.set(row);
    this.resetSearches();
    const userName = row.username?.trim() ?? '';
    this.form.reset(
      {
        systemKind: row.externalSystem && !row.application ? 'external' : 'application',
        applicationId: row.application?.id ?? null,
        externalSystemId: row.externalSystem?.id ?? null,
        technologyId: row.technology?.id ?? null,
        user: userName ? this.savedUser(userName) : null,
        requiredRoles: (row.requiredRoles ?? []).map(toIntegrationRoleOption),
      },
      { emitEvent: false },
    );
    configureApplicationIntegrationSystemKind(this.form);
    if (row.application) {
      this.applicationSearch.set({
        ...emptySearch(),
        options: [this.toApplicationOption(row.application)],
      });
    }
    this.roleSearch.set({ ...emptySearch(), options: this.form.controls.requiredRoles.value });
    this.mode.set(mode);
    this.visible.set(true);
    if (mode === 'edit') this.ensureCatalogs();
  }

  // An existing connection only stores the Soffid user code, not the person's name.
  private savedUser(userName: string): SoffidPersonOption {
    const candidate: SoffidPersonCandidate = {
      id: null,
      company: null,
      firstName: '',
      lastName: '',
      email: '',
      personalCaib: true,
      deletedAt: null,
      userName,
    };
    return { ...candidate, label: userName };
  }

  private ensureCatalogs(): void {
    if (this.catalogsLoaded || this.catalogsLoading()) return;
    this.catalogsLoading.set(true);
    forkJoin({
      technologies: this.technologyCatalog.getActiveOptions(),
      externalSystems: this.externalSystemCatalog.getActiveOptions(),
    })
      .pipe(
        finalize(() => this.catalogsLoading.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: ({ technologies, externalSystems }) => {
          this.catalogsLoaded = true;
          this.technologyCatalogOptions.set(technologies);
          this.externalSystemCatalogOptions.set(externalSystems);
        },
        error: () => this.error(APPLICATION_INTEGRATIONS_MESSAGES.catalogsError),
      });
  }

  private loadApplications(query: string): Observable<unknown> {
    const selectedId = this.form.controls.applicationId.value;
    const selected = this.applicationSearch().options.find((option) => option.id === selectedId);
    this.applicationSearch.set({ ...emptySearch(), options: selected ? [selected] : [], loading: true });
    const ownId = Number(this.detail.application()?.id);
    return this.applicationsService
      .getPage({
        page: 0,
        size: APPLICATION_SEARCH_SIZE,
        sort: 'name,asc',
        statusId: ApplicationStatus.ACTIVE,
        quickSearch: query.trim() || undefined,
      })
      .pipe(
        map((page) => {
          const options = page.content
            .map((application) => ({
              id: Number(application.id),
              label: this.applicationLabel(application.code, application.name),
            }))
            .filter((option) => option.id !== ownId);
          this.applicationSearch.set({
            options: this.withSelected(options, selected ? [selected] : [], (option) => option.id),
            loading: false,
            searched: true,
            error: false,
            total: page.totalElements,
          });
        }),
        catchError(() => {
          this.applicationSearch.update((state) => ({ ...state, loading: false, error: true }));
          return EMPTY;
        }),
      );
  }

  private loadUsers(query: string): Observable<unknown> {
    this.userSearch.set(emptySearch());
    if (query.trim().length < SOFFID_MIN_QUERY_LENGTH) return EMPTY;
    this.userSearch.set({ ...emptySearch(), loading: true });
    return this.peopleService.searchSoffid(query).pipe(
      map((page) =>
        this.userSearch.set({
          options: page.content
            .filter((person) => person.userName?.trim())
            .map((person) => ({
              ...person,
              label: `${person.firstName} ${person.lastName} (${person.userName})`.trim(),
            })),
          loading: false,
          searched: true,
          error: false,
          total: page.totalElements,
        }),
      ),
      catchError(() => {
        this.userSearch.set({ ...emptySearch(), searched: true, error: true });
        return EMPTY;
      }),
    );
  }

  private loadRoles(query: string): Observable<unknown> {
    const selected = this.form.controls.requiredRoles.value;
    this.roleSearch.set({ ...emptySearch(), options: selected, loading: true });
    return this.soffidRoles.search(query).pipe(
      map((page) => {
        const found = page.content
          .filter((role): role is ApplicationSecurityRole & { roleId: number } => role.roleId != null)
          .map((role) =>
            toIntegrationRoleOption({ id: role.roleId, name: role.name, system: role.system }),
          );
        this.roleSearch.set({
          options: this.withSelected(found, selected, (role) => role.roleId),
          loading: false,
          searched: true,
          error: false,
          total: page.totalElements,
        });
      }),
      catchError(() => {
        this.roleSearch.update((state) => ({ ...state, loading: false, error: true }));
        return EMPTY;
      }),
    );
  }

  // Keeps the current selection visible while the remote results change.
  private withSelected<TOption>(
    options: TOption[],
    selected: TOption[],
    key: (option: TOption) => number,
  ): TOption[] {
    const missing = selected.filter((item) => !options.some((option) => key(option) === key(item)));
    return [...missing, ...options];
  }

  private toApplicationOption(
    application: NonNullable<ApplicationIntegrationConnectionOutput['application']>,
  ): ApplicationIntegrationApplicationOption {
    return { id: application.id, label: this.applicationLabel(application.code, application.name) };
  }

  private applicationLabel(code: string | null, name: string | null): string {
    const parts = [code?.trim(), name?.trim()].filter(Boolean);
    return parts.length ? parts.join(' — ') : '-';
  }

  private resetSearches(): void {
    this.applicationSearch.set(emptySearch());
    this.userSearch.set(emptySearch());
    this.roleSearch.set(emptySearch());
  }

  // Structured validation errors are already shown by the global error dialog.
  private mutationError(error: unknown, fallback: string): void {
    if (isStructuredBadRequest(error)) return;
    this.error(fallback);
  }

  private error(detail: string): void {
    this.messages.add({ severity: 'error', summary: APPLICATION_INTEGRATIONS_ERROR_TITLE, detail });
  }

  private success(detail: string): void {
    this.messages.add({ severity: 'success', summary: APPLICATION_INTEGRATIONS_SUCCESS_TITLE, detail });
  }
}
