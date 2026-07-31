import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { DatabasesService } from '@features/systems/services/databases.service';
import { SystemsService } from '@features/systems/services/systems.service';
import { SpringPage } from '@models/page.model';
import { KeyLabel } from '@models/table.model';
import { By } from '@angular/platform-browser';
import { MessageService } from 'primeng/api';
import { Button } from 'primeng/button';
import { Editor } from 'primeng/editor';
import { TableLazyLoadEvent } from 'primeng/table';
import { Observable, Subject, of } from 'rxjs';

import {
  ApplicationDatabaseRelationDialog,
  ApplicationInfrastructureList,
  ApplicationInfrastructureTableAction,
  ApplicationSystemRelationDialog,
} from '../../../../components';
import {
  ApplicationDatabase,
  ApplicationDatabaseFilters,
  ApplicationDatabasesPageParams,
  ApplicationInfrastructureStatus,
  ApplicationOutput,
  ApplicationServer,
  ApplicationServerFilters,
  ApplicationSystemsPageParams,
  ApplicationStatus,
} from '../../../../applications.model';
import { ApplicationDatabasesService } from '../../../../services/application-databases.service';
import { ApplicationDevelopmentService } from '../../../../services/application-development.service';
import { ApplicationSystemsService } from '../../../../services/application-systems.service';
import { ApplicationSystemDatabaseService } from '../../../../services/application-system-database.service';
import { ApplicationsService } from '../../../../services/applications.service';
import {
  ApplicationDatabaseRelationFormGroup,
  ApplicationSystemRelationFormGroup,
} from '../../../../forms/application-infrastructure-relation-forms.factory';
import { ApplicationDetailState } from '../../application-detail-state';
import { ApplicationSystemsDatabasesSection } from './application-systems-databases-section';
import {
  APPLICATION_SYSTEMS_DATABASES_RESOLVE_KEY,
  ApplicationSystemsDatabasesResolvedData,
} from './application-systems-databases-section.resolver';

class ResizeObserverMock implements ResizeObserver {
  disconnect(): void {}
  observe(): void {}
  unobserve(): void {}
}

globalThis.ResizeObserver ??= ResizeObserverMock;

const SERVER: ApplicationServer = {
  id: 10,
  informationSystemDbId: 70,
  systemId: 5,
  deletedAt: null,
  environment: 'Producció',
  server: 'app01.caib.es',
  instance: 'jboss',
  port: 8080,
  version: '7.4',
  status: 'Actiu',
  observations: '',
  catalogItem: {
    id: 5,
    environment: 'Producció',
    server: 'app01.caib.es',
    instance: 'jboss',
    port: 8080,
    version: '7.4',
    description: '',
    source: null!,
  },
};
const DATABASE: ApplicationDatabase = {
  id: 20,
  informationSystemDbId: 70,
  databaseId: 8,
  deletedAt: null,
  environment: 'Producció',
  server: 'db01.caib.es',
  database: 'invai_svc',
  service: 'invai_svc',
  port: 5432,
  type: 'Relacional',
  status: 'Actiu',
  observations: '',
  catalogItem: {
    id: 8,
    environment: 'Producció',
    server: 'db01.caib.es',
    service: 'invai_svc',
    port: 5432,
    databaseType: 'Relacional',
    description: '',
    source: null!,
  },
};

interface SectionAccess {
  servers: () => { items: ApplicationServer[]; total: number };
  databases: () => { items: ApplicationDatabase[]; total: number };
  isServersLoading: () => boolean;
  isDatabasesLoading: () => boolean;
  onServersPageChange: (event: TableLazyLoadEvent) => void;
  onDatabasesPageChange: (event: TableLazyLoadEvent) => void;
  onServerFilterSearch: () => void;
  onDatabaseFilterSearch: () => void;
  onServerFilterReset: () => void;
  onDatabaseFilterReset: () => void;
  serverFiltersForm: {
    patchValue: (value: Partial<ApplicationServerFilters>) => void;
    getRawValue: () => ApplicationServerFilters;
  };
  databaseFiltersForm: {
    patchValue: (value: Partial<ApplicationDatabaseFilters>) => void;
    getRawValue: () => ApplicationDatabaseFilters;
  };
  serverFirst: () => number;
  selectedServerFilters: () => number;
  onDocumentation: () => void;
  openSystemCreateDialog: () => void;
  onSystemTableAction: (event: {
    action: ApplicationInfrastructureTableAction;
    params: ApplicationServer;
  }) => void;
  onDatabaseTableAction: (event: {
    action: ApplicationInfrastructureTableAction;
    params: ApplicationDatabase;
  }) => void;
  submitSystemRelation: () => void;
  submitDatabaseRelation: () => void;
  startSystemEdit: () => void;
  startDatabaseEdit: () => void;
  deleteSelectedDatabase: () => void;
  confirmDelete: () => void;
  systemRelationForm: ApplicationSystemRelationFormGroup;
  databaseRelationForm: ApplicationDatabaseRelationFormGroup;
  systemDialogVisible: () => boolean;
  databaseDialogVisible: () => boolean;
  systemDialogMode: () => 'create' | 'view' | 'edit';
  databaseDialogMode: () => 'create' | 'view' | 'edit';
  deleteDialogVisible: () => boolean;
}

interface InfrastructureListAccess {
  appliedStatus: () => ApplicationInfrastructureStatus | null;
  selectedColumns: {
    update(updateFn: (columns: KeyLabel[]) => KeyLabel[]): void;
  };
  visibleColumns: () => KeyLabel[];
}

describe('ApplicationSystemsDatabasesSection', () => {
  let fixture: ComponentFixture<ApplicationSystemsDatabasesSection>;
  let messageService: MessageService;
  let detailState: ApplicationDetailState;
  let getSystemsPage: ReturnType<typeof vi.fn>;
  let getDatabasesPage: ReturnType<typeof vi.fn>;
  let createSystemRelation: ReturnType<typeof vi.fn>;
  let updateSystemRelation: ReturnType<typeof vi.fn>;
  let deleteSystemRelation: ReturnType<typeof vi.fn>;
  let createDatabaseRelation: ReturnType<typeof vi.fn>;
  let updateDatabaseRelation: ReturnType<typeof vi.fn>;
  let deleteDatabaseRelation: ReturnType<typeof vi.fn>;
  let systemsRequests: Subject<SpringPage<ApplicationServer>>[];
  let databasesRequests: Subject<SpringPage<ApplicationDatabase>>[];
  let routeData: Record<string, ApplicationSystemsDatabasesResolvedData>;

  beforeEach(async () => {
    systemsRequests = [];
    databasesRequests = [];
    getSystemsPage = vi.fn(
      (_params: ApplicationSystemsPageParams): Observable<SpringPage<ApplicationServer>> => {
        const request = new Subject<SpringPage<ApplicationServer>>();
        systemsRequests.push(request);
        return request;
      },
    );
    getDatabasesPage = vi.fn(
      (
        _params: ApplicationDatabasesPageParams,
      ): Observable<SpringPage<ApplicationDatabase>> => {
        const request = new Subject<SpringPage<ApplicationDatabase>>();
        databasesRequests.push(request);
        return request;
      },
    );
    createSystemRelation = vi.fn(() => of({}));
    updateSystemRelation = vi.fn(() => of({}));
    deleteSystemRelation = vi.fn(() => of(undefined));
    createDatabaseRelation = vi.fn(() => of({}));
    updateDatabaseRelation = vi.fn(() => of({}));
    deleteDatabaseRelation = vi.fn(() => of(undefined));
    routeData = {
      [APPLICATION_SYSTEMS_DATABASES_RESOLVE_KEY]: resolvedData(),
    };

    await TestBed.configureTestingModule({
      imports: [ApplicationSystemsDatabasesSection],
      providers: [
        ApplicationDetailState,
        MessageService,
        {
          provide: ApplicationsService,
          useValue: {},
        },
        {
          provide: ApplicationDevelopmentService,
          useValue: {},
        },
        {
          provide: ApplicationSystemDatabaseService,
          useValue: { update: vi.fn() },
        },
        { provide: SystemsService, useValue: { getAll: vi.fn() } },
        { provide: DatabasesService, useValue: { getAll: vi.fn() } },
        { provide: ActivatedRoute, useValue: { snapshot: { data: routeData } } },
        {
          provide: ApplicationSystemsService,
          useValue: {
            getPage: getSystemsPage,
            create: createSystemRelation,
            update: updateSystemRelation,
            delete: deleteSystemRelation,
          },
        },
        {
          provide: ApplicationDatabasesService,
          useValue: {
            getPage: getDatabasesPage,
            create: createDatabaseRelation,
            update: updateDatabaseRelation,
            delete: deleteDatabaseRelation,
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ApplicationSystemsDatabasesSection);
    messageService = TestBed.inject(MessageService);
    detailState = TestBed.inject(ApplicationDetailState);
    detailState.application.set({
      id: '7',
      status: ApplicationStatus.ACTIVE,
    } as never);
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('renders both resolved pages without requesting them again', () => {
    const lists = fixture.debugElement.queryAll(By.directive(ApplicationInfrastructureList));

    expect(lists).toHaveLength(2);
    expect(lists[0].componentInstance.title()).toBe('Servidors');
    expect(lists[1].componentInstance.title()).toBe('Bases de dades');
    expect(lists[0].componentInstance.itemsList()).toEqual({ items: [SERVER], total: 17 });
    expect(lists[1].componentInstance.itemsList()).toEqual({ items: [DATABASE], total: 8 });
    expect(getSystemsPage).not.toHaveBeenCalled();
    expect(getDatabasesPage).not.toHaveBeenCalled();
  });

  it('renders the standardized descriptive section title', () => {
    const title = fixture.nativeElement.querySelector(
      '.application-detail-section-layout__title',
    ) as HTMLHeadingElement;

    expect(title.textContent?.trim()).toBe(
      'Infraestructura de sistemes i bases de dades',
    );
    expect(title.id).toBe('application-systems-databases-section-title');
  });

  it('enables infrastructure mutations only during this tab edit session', () => {
    const section = accessSection();
    let lists = fixture.debugElement.queryAll(By.directive(ApplicationInfrastructureList));

    expect(lists.every((list) => list.componentInstance.isReadOnly())).toBe(true);
    section.openSystemCreateDialog();
    expect(section.systemDialogVisible()).toBe(false);

    detailState.startEditing('systems-databases');
    fixture.detectChanges();
    lists = fixture.debugElement.queryAll(By.directive(ApplicationInfrastructureList));

    expect(lists.every((list) => !list.componentInstance.isReadOnly())).toBe(true);
  });

  it('opens relation rows in view mode even outside the section edit session', () => {
    const section = accessSection();

    section.onDatabaseTableAction({
      action: ApplicationInfrastructureTableAction.View,
      params: DATABASE,
    });

    expect(section.databaseDialogVisible()).toBe(true);
    expect(section.databaseDialogMode()).toBe('view');
    expect(section.databaseRelationForm.disabled).toBe(true);
    expect(section.databaseRelationForm.controls.database.value).toBe(DATABASE.catalogItem);
  });

  it('edits and saves existing relations from view dialogs outside the section edit session', () => {
    const section = accessSection();
    detailState.informationSystemDbId.set(70);

    section.onSystemTableAction({
      action: ApplicationInfrastructureTableAction.View,
      params: SERVER,
    });
    fixture.detectChanges();

    const systemDialog = fixture.debugElement.query(
      By.directive(ApplicationSystemRelationDialog),
    ).componentInstance as ApplicationSystemRelationDialog;
    expect(systemDialog.canEdit()).toBe(true);
    expect(systemDialog.canDeactivate()).toBe(false);

    section.startSystemEdit();
    expect(section.systemDialogMode()).toBe('edit');
    expect(section.systemRelationForm.enabled).toBe(true);

    section.submitSystemRelation();
    expect(updateSystemRelation).toHaveBeenCalledWith(10, {
      informationSystemDbId: 70,
      systemId: 5,
    });

    section.onDatabaseTableAction({
      action: ApplicationInfrastructureTableAction.View,
      params: DATABASE,
    });
    fixture.detectChanges();

    const databaseDialog = fixture.debugElement.query(
      By.directive(ApplicationDatabaseRelationDialog),
    ).componentInstance as ApplicationDatabaseRelationDialog;
    expect(databaseDialog.canEdit()).toBe(true);
    expect(databaseDialog.canDeactivate()).toBe(false);

    section.startDatabaseEdit();
    expect(section.databaseDialogMode()).toBe('edit');
    expect(section.databaseRelationForm.enabled).toBe(true);

    section.submitDatabaseRelation();
    expect(updateDatabaseRelation).toHaveBeenCalledWith(20, {
      informationSystemDbId: 70,
      databaseId: 8,
    });
  });

  it('keeps relation dialogs read-only for an inactive application', () => {
    const section = accessSection();
    detailState.application.set({
      id: '7',
      status: ApplicationStatus.INACTIVE,
    } as never);

    section.onSystemTableAction({
      action: ApplicationInfrastructureTableAction.View,
      params: SERVER,
    });
    fixture.detectChanges();

    const systemDialog = fixture.debugElement.query(
      By.directive(ApplicationSystemRelationDialog),
    ).componentInstance as ApplicationSystemRelationDialog;
    expect(systemDialog.canEdit()).toBe(false);
    expect(fixture.debugElement.query(By.directive(Editor)).componentInstance.readonly).toBe(true);

    section.startSystemEdit();
    expect(section.systemDialogMode()).toBe('view');
    expect(section.systemRelationForm.disabled).toBe(true);

    section.onDatabaseTableAction({
      action: ApplicationInfrastructureTableAction.View,
      params: DATABASE,
    });
    fixture.detectChanges();

    const databaseDialog = fixture.debugElement.query(
      By.directive(ApplicationDatabaseRelationDialog),
    ).componentInstance as ApplicationDatabaseRelationDialog;
    expect(databaseDialog.canEdit()).toBe(false);

    section.startDatabaseEdit();
    expect(section.databaseDialogMode()).toBe('view');
    expect(section.databaseRelationForm.disabled).toBe(true);

    section.submitSystemRelation();
    section.submitDatabaseRelation();
    expect(updateSystemRelation).not.toHaveBeenCalled();
    expect(updateDatabaseRelation).not.toHaveBeenCalled();
  });

  it('keeps relation dialog selection labels accessible without repeating the visible title', () => {
    const section = accessSection();
    detailState.startEditing('systems-databases');

    section.openSystemCreateDialog();
    section.onDatabaseTableAction({
      action: ApplicationInfrastructureTableAction.View,
      params: DATABASE,
    });
    fixture.detectChanges();

    for (const id of [
      'application-system-relation-selection-label',
      'application-database-relation-selection-label',
    ]) {
      const label = fixture.nativeElement.querySelector(`#${id}`) as HTMLElement | null;

      expect(label?.tagName).toBe('SPAN');
      expect(label?.classList.contains('sr-only')).toBe(true);
      expect(fixture.nativeElement.querySelector(`p#${id}`)).toBeNull();
    }
  });

  it('creates a system relation with informationSystemDbId and refreshes only servers', () => {
    const section = accessSection();
    detailState.informationSystemDbId.set(70);
    detailState.startEditing('systems-databases');

    section.openSystemCreateDialog();
    section.systemRelationForm.controls.system.setValue(SERVER.catalogItem);
    section.submitSystemRelation();

    expect(createSystemRelation).toHaveBeenCalledWith({
      informationSystemDbId: 70,
      systemId: 5,
    });
    expect(section.systemDialogVisible()).toBe(false);
    expect(getSystemsPage).toHaveBeenCalledWith({
      informationSystemDbId: 70,
      statusId: ApplicationInfrastructureStatus.ACTIVE,
      page: 0,
      size: 10,
      sort: undefined,
    });
    expect(getDatabasesPage).not.toHaveBeenCalled();
  });

  it('offers deletion from an edit dialog and refreshes only databases', () => {
    const section = accessSection();
    detailState.startEditing('systems-databases');

    section.onDatabaseTableAction({
      action: ApplicationInfrastructureTableAction.Edit,
      params: DATABASE,
    });
    expect(section.databaseDialogMode()).toBe('edit');

    section.deleteSelectedDatabase();
    expect(section.deleteDialogVisible()).toBe(true);

    section.confirmDelete();

    expect(deleteDatabaseRelation).toHaveBeenCalledWith(20);
    expect(section.databaseDialogVisible()).toBe(false);
    expect(getDatabasesPage).toHaveBeenCalledWith({
      informationSystemDbId: 70,
      statusId: ApplicationInfrastructureStatus.ACTIVE,
      page: 0,
      size: 10,
      sort: undefined,
    });
    expect(getSystemsPage).not.toHaveBeenCalled();
  });

  it('requests later server pages with the active application criteria and nested sorting', () => {
    const section = accessSection();

    section.onServersPageChange({
      first: 20,
      rows: 10,
      sortField: 'system.name',
      sortOrder: -1,
    });

    expect(getSystemsPage).toHaveBeenCalledWith({
      informationSystemDbId: 70,
      statusId: ApplicationInfrastructureStatus.ACTIVE,
      page: 2,
      size: 10,
      sort: 'system.name,desc',
    });
    expect(section.isServersLoading()).toBe(true);
    expect(section.isDatabasesLoading()).toBe(false);

    systemsRequests[0].next(page([{ ...SERVER, id: 11 }], 1));
    systemsRequests[0].complete();

    expect(section.servers()).toEqual({ items: [{ ...SERVER, id: 11 }], total: 1 });
    expect(section.isServersLoading()).toBe(false);
  });

  it('requests later database pages independently', () => {
    const section = accessSection();

    section.onDatabasesPageChange({ first: 10, rows: 10 });

    expect(getDatabasesPage).toHaveBeenCalledWith({
      informationSystemDbId: 70,
      statusId: ApplicationInfrastructureStatus.ACTIVE,
      page: 1,
      size: 10,
      sort: undefined,
    });
    expect(section.isDatabasesLoading()).toBe(true);

    databasesRequests[0].next(page([], 0));
    databasesRequests[0].complete();

    expect(section.databases()).toEqual({ items: [], total: 0 });
    expect(section.isDatabasesLoading()).toBe(false);
  });

  it('applies supported server filters and keeps them while paging', () => {
    const section = accessSection();
    section.serverFiltersForm.patchValue({
      environment: 3,
      server: 5,
      instance: 'ignored by backend',
      status: ApplicationInfrastructureStatus.INACTIVE,
    });

    section.onServerFilterSearch();
    section.onServersPageChange({ first: 10, rows: 10 });

    expect(getSystemsPage).toHaveBeenNthCalledWith(1, {
      informationSystemDbId: 70,
      statusId: ApplicationInfrastructureStatus.INACTIVE,
      systemId: 5,
      page: 0,
      size: 10,
      sort: undefined,
    });
    expect(getSystemsPage).toHaveBeenNthCalledWith(2, {
      informationSystemDbId: 70,
      statusId: ApplicationInfrastructureStatus.INACTIVE,
      systemId: 5,
      page: 1,
      size: 10,
      sort: undefined,
    });
    expect(section.serverFirst()).toBe(10);
  });

  it('applies only the supported database filters', () => {
    const section = accessSection();
    section.databaseFiltersForm.patchValue({
      environment: 3,
      database: 8,
      server: 'ignored by backend',
      status: ApplicationInfrastructureStatus.INACTIVE,
    });

    section.onDatabaseFilterSearch();

    expect(getDatabasesPage).toHaveBeenCalledWith({
      informationSystemDbId: 70,
      statusId: ApplicationInfrastructureStatus.INACTIVE,
      databaseId: 8,
      page: 0,
      size: 10,
      sort: undefined,
    });
  });

  it('omits server status for all and preserves manual columns while editing and paging', () => {
    const section = accessSection();
    const serverList = fixture.debugElement.queryAll(
      By.directive(ApplicationInfrastructureList),
    )[0].componentInstance as unknown as InfrastructureListAccess;

    section.serverFiltersForm.patchValue({ status: null });
    fixture.detectChanges();
    expect(serverList.appliedStatus()).toBe(ApplicationInfrastructureStatus.ACTIVE);
    expect(serverList.visibleColumns().map(({ key }) => key)).not.toContain('status');

    section.onServerFilterSearch();
    fixture.detectChanges();
    expect(getSystemsPage).toHaveBeenLastCalledWith({
      informationSystemDbId: 70,
      page: 0,
      size: 10,
      sort: undefined,
    });
    expect(serverList.appliedStatus()).toBeNull();
    expect(section.selectedServerFilters()).toBe(0);
    expect(serverList.visibleColumns().map(({ key }) => key)).toContain('status');

    serverList.selectedColumns.update((columns) =>
      columns.filter(({ key }) => key !== 'status'),
    );
    section.serverFiltersForm.patchValue({
      status: ApplicationInfrastructureStatus.INACTIVE,
    });
    section.onServersPageChange({ first: 10, rows: 10 });
    fixture.detectChanges();

    expect(getSystemsPage).toHaveBeenLastCalledWith({
      informationSystemDbId: 70,
      page: 1,
      size: 10,
      sort: undefined,
    });
    expect(serverList.appliedStatus()).toBeNull();
    expect(serverList.visibleColumns().map(({ key }) => key)).not.toContain('status');
  });

  it('omits database status for all and restores active on reset', () => {
    const section = accessSection();
    const databaseList = fixture.debugElement.queryAll(
      By.directive(ApplicationInfrastructureList),
    )[1].componentInstance as unknown as InfrastructureListAccess;

    section.databaseFiltersForm.patchValue({ status: null });
    section.onDatabaseFilterSearch();
    fixture.detectChanges();

    expect(getDatabasesPage).toHaveBeenLastCalledWith({
      informationSystemDbId: 70,
      page: 0,
      size: 10,
      sort: undefined,
    });
    expect(databaseList.appliedStatus()).toBeNull();
    expect(databaseList.visibleColumns().map(({ key }) => key)).toContain('status');

    section.onDatabasesPageChange({ first: 10, rows: 10 });
    expect(getDatabasesPage).toHaveBeenLastCalledWith({
      informationSystemDbId: 70,
      page: 1,
      size: 10,
      sort: undefined,
    });

    section.onDatabaseFilterReset();
    fixture.detectChanges();

    expect(section.databaseFiltersForm.getRawValue().status).toBe(
      ApplicationInfrastructureStatus.ACTIVE,
    );
    expect(databaseList.appliedStatus()).toBe(ApplicationInfrastructureStatus.ACTIVE);
    expect(databaseList.visibleColumns().map(({ key }) => key)).not.toContain('status');
    expect(getDatabasesPage).toHaveBeenLastCalledWith({
      informationSystemDbId: 70,
      statusId: ApplicationInfrastructureStatus.ACTIVE,
      page: 0,
      size: 10,
      sort: undefined,
    });
  });

  it('resets server filters to active and returns to the first page', () => {
    const section = accessSection();
    section.serverFiltersForm.patchValue({
      environment: 3,
      status: ApplicationInfrastructureStatus.INACTIVE,
    });

    section.onServerFilterReset();

    expect(section.serverFiltersForm.getRawValue()).toEqual({
      environment: null,
      server: null,
      instance: null,
      port: null,
      version: null,
      status: ApplicationInfrastructureStatus.ACTIVE,
      observations: null,
    });
    expect(section.selectedServerFilters()).toBe(1);
    expect(section.serverFirst()).toBe(0);
    expect(getSystemsPage).toHaveBeenCalledWith({
      informationSystemDbId: 70,
      statusId: ApplicationInfrastructureStatus.ACTIVE,
      page: 0,
      size: 10,
      sort: undefined,
    });
  });

  it('warns about unsupported filters on change and on search without sending them', async () => {
    vi.useFakeTimers();
    const addSpy = vi.spyOn(messageService, 'add');
    const section = accessSection();

    section.serverFiltersForm.patchValue({ instance: 'jboss' });
    await vi.advanceTimersByTimeAsync(400);

    expect(addSpy).toHaveBeenCalledWith({
      severity: 'warn',
      summary: 'Atenció',
      detail: 'El filtre «Instància» encara no està suportat pel servidor i no s\'aplicarà.',
    });

    section.onServerFilterSearch();
    expect(addSpy).toHaveBeenLastCalledWith({
      severity: 'warn',
      summary: 'Atenció',
      detail: 'Alguns filtres encara no estan suportats pel servidor i no s\'aplicaran.',
    });
    expect(getSystemsPage).toHaveBeenLastCalledWith({
      informationSystemDbId: 70,
      statusId: ApplicationInfrastructureStatus.ACTIVE,
      page: 0,
      size: 10,
      sort: undefined,
    });
    vi.useRealTimers();
  });

  it('clears and reports only the table whose later request fails', () => {
    const addSpy = vi.spyOn(messageService, 'add');
    const section = accessSection();

    section.onServersPageChange({ first: 10, rows: 10 });
    systemsRequests[0].error(new Error('Request failed'));

    expect(section.servers()).toEqual({ items: [], total: 0 });
    expect(section.databases()).toEqual({ items: [DATABASE], total: 8 });
    expect(section.isServersLoading()).toBe(false);
    expect(addSpy).toHaveBeenCalledWith({
      severity: 'error',
      summary: 'Error',
      detail: "No s'han pogut carregar els servidors de l'aplicació.",
    });
  });

  it('reports independent errors provided by the resolver', () => {
    const addSpy = vi.spyOn(messageService, 'add');

    recreateWithResolvedData({
      ...resolvedData(),
      serversPage: null,
      serversLoadFailed: true,
    });

    expect(accessSection().servers()).toEqual({ items: [], total: 0 });
    expect(accessSection().databases()).toEqual({ items: [DATABASE], total: 8 });
    expect(addSpy).toHaveBeenCalledOnce();
  });

  it('accepts a missing observations aggregate without reporting a load error', () => {
    const addSpy = vi.spyOn(messageService, 'add');

    recreateWithResolvedData({
      ...resolvedData(),
      systemDatabase: null,
      systemDatabaseLoadFailed: false,
    });

    expect(detailState.systemDatabase()).toBeNull();
    expect(addSpy).not.toHaveBeenCalled();
  });

  it('exposes the status column in the database list', () => {
    const lists = fixture.debugElement.queryAll(By.directive(ApplicationInfrastructureList));
    const databaseColumns = lists[1].componentInstance.columns();

    expect(databaseColumns).toContainEqual(
      expect.objectContaining({ key: 'status', sortBy: 'deletedAt' }),
    );
  });

  it('renders the documentation action and observations editor', () => {
    const documentationButton = fixture.debugElement
      .queryAll(By.directive(Button))
      .find((button) => button.componentInstance.label === 'Documentació');
    const editor = fixture.debugElement.query(By.directive(Editor));
    const observationsGroup = fixture.debugElement.query(
      By.css('.application-systems-databases__observations'),
    );
    const labelId = 'application-systems-databases-observations-label';

    expect(documentationButton?.componentInstance.ariaLabel).toBe(
      'Obrir la documentació de sistemes i bases de dades',
    );
    expect(editor).toBeTruthy();
    expect(editor.nativeElement.id).toBe('application-systems-databases-observations');
    expect(editor.componentInstance.readonly).toBe(true);
    expect(observationsGroup.attributes['aria-labelledby']).toBe(labelId);

    detailState.startEditing('systems-databases');
    fixture.detectChanges();
    expect(editor.componentInstance.readonly).toBe(false);

    detailState.cancelEditing('systems-databases');
    fixture.detectChanges();
    expect(editor.componentInstance.readonly).toBe(true);
  });

  it('binds observations to the page-scoped detail form', () => {
    expect(detailState.systemsDatabasesForm.controls.observations.value).toBe('');

    detailState.systemsDatabasesForm.controls.observations.setValue('<p>Text enriquit</p>');
    fixture.detectChanges();

    const editor = fixture.debugElement.query(By.directive(Editor));
    expect(editor.componentInstance.value).toBe('<p>Text enriquit</p>');
  });

  it('shows a pending message when opening documentation', () => {
    const addSpy = vi.spyOn(messageService, 'add');

    accessSection().onDocumentation();

    expect(addSpy).toHaveBeenCalledWith({
      severity: 'info',
      summary: 'Informació',
      detail: 'La documentació de sistemes i bases de dades encara no està implementada.',
    });
  });

  function accessSection(): SectionAccess {
    return fixture.componentInstance as unknown as SectionAccess;
  }

  function recreateWithResolvedData(data: ApplicationSystemsDatabasesResolvedData): void {
    fixture.destroy();
    routeData[APPLICATION_SYSTEMS_DATABASES_RESOLVE_KEY] = data;
    fixture = TestBed.createComponent(ApplicationSystemsDatabasesSection);
    fixture.detectChanges();
  }
});

function resolvedData(): ApplicationSystemsDatabasesResolvedData {
  return {
    applicationId: 7,
    informationSystemDbId: 70,
    serversPage: page([SERVER], 17),
    databasesPage: page([DATABASE], 8),
    systemCatalogPage: page([]),
    databaseCatalogPage: page([]),
    systemDatabase: {
      id: 70,
      application: {
        id: 7,
        appInformationSystemDbId: 70,
        appDevelopmentId: 90,
      } as ApplicationOutput,
      observation: '',
      deletedAt: null,
    },
    serversLoadFailed: false,
    databasesLoadFailed: false,
    systemCatalogLoadFailed: false,
    databaseCatalogLoadFailed: false,
    systemDatabaseLoadFailed: false,
    filterOptions: {
      servers: [{ label: 'app01.caib.es', value: 5 }],
      databases: [{ label: 'INVAI_PRD', value: 6 }],
      environments: [{ label: 'Producció', value: 3 }],
    },
    filterOptionsLoadFailed: { servers: false, databases: false, environments: false },
  };
}

function page<TItem>(content: TItem[], totalElements = content.length): SpringPage<TItem> {
  return {
    content,
    empty: content.length === 0,
    first: true,
    last: true,
    number: 0,
    numberOfElements: content.length,
    pageable: {
      offset: 0,
      pageNumber: 0,
      pageSize: 10,
      paged: true,
      sort: { empty: true, sorted: false, unsorted: true },
      unpaged: false,
    },
    size: 10,
    sort: { empty: true, sorted: false, unsorted: true },
    totalElements,
    totalPages: Math.ceil(totalElements / 10),
  };
}
