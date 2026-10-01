import { HttpErrorResponse } from '@angular/common/http';
import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ExternalSystemCatalogService } from '@features/external-systems/services/external-system-catalog.service';
import { ResponsiblePeopleService } from '@features/maintenances/responsibles/services/responsible-people.service';
import { TechnologyCatalogService } from '@features/technologies/services/technology-catalog.service';
import { MessageService } from 'primeng/api';
import { of, throwError } from 'rxjs';

import { ApplicationIntegrationConnectionOutput } from '../../../../applications.model';
import { ApplicationIntegrationConnectionsService } from '../../../../services/application-integration-connections.service';
import { ApplicationsService } from '../../../../services/applications.service';
import { SoffidRolesService } from '../../../../services/soffid-roles.service';
import { ApplicationDetailState } from '../../application-detail-state';
import { ApplicationIntegrationConnectionsState } from './application-integration-connections-state';
import { ApplicationIntegrationsLoadResult } from './application-integrations-section.resolver';
import { ApplicationIntegrationsTableAction } from './application-integrations-table';

const ROW: ApplicationIntegrationConnectionOutput = {
  id: 1,
  appIntegrationId: 13,
  application: { id: 8, code: 'APP-002', name: 'Portal Salut' },
  externalSystem: null,
  technology: { id: 9, name: 'Struts', layer: { id: 1, name: 'Backend', deletedAt: null }, deletedAt: null },
  username: 'u00004',
  requiredRoles: [{ id: 26, name: 'INV_ADMIN', description: null, system: 'SEYCON' }],
  grantedRoles: [],
  rolesMismatch: true,
  deletedAt: null,
};
const PAGE = { content: [ROW], totalElements: 1, number: 0 };
const USER = {
  id: null,
  company: null,
  firstName: 'Maria',
  lastName: 'Tur',
  email: 'maria@caib.es',
  personalCaib: true as const,
  deletedAt: null,
  userName: 'u00009',
  label: 'Maria Tur (u00009)',
};

function loadResult(overrides: Partial<ApplicationIntegrationsLoadResult> = {}): ApplicationIntegrationsLoadResult {
  return {
    applicationId: 7,
    appIntegrationId: 13,
    record: { id: 13, applicationId: 7, observation: null, deletedAt: null },
    status: 'loaded',
    errorMessage: null,
    connectionsPage: PAGE as never,
    connectionsLoadFailed: false,
    connectionsErrorMessage: null,
    ...overrides,
  };
}

describe('ApplicationIntegrationConnectionsState', () => {
  let state: ApplicationIntegrationConnectionsState;
  let messages: MessageService;
  const editing = signal(true);
  const detail = {
    appIntegrationId: signal<number | null>(13),
    integrationStatus: signal<string>('loaded'),
    canEdit: signal(true),
    application: signal({ id: '7' }),
    isEditing: (section: string) => section === 'integrations' && editing(),
    refreshCompletenessAfterMutation: vi.fn(),
  };
  const service = { getPage: vi.fn(), create: vi.fn(), update: vi.fn(), delete: vi.fn() };
  const applications = { getPage: vi.fn() };
  const technologies = { getActiveOptions: vi.fn() };
  const externalSystems = { getActiveOptions: vi.fn() };
  const people = { searchSoffid: vi.fn() };
  const roles = { search: vi.fn() };

  beforeEach(() => {
    editing.set(true);
    detail.appIntegrationId.set(13);
    detail.integrationStatus.set('loaded');
    detail.refreshCompletenessAfterMutation.mockReset();
    service.getPage.mockReset().mockReturnValue(of(PAGE));
    service.create.mockReset().mockReturnValue(of({ ...ROW, id: 2 }));
    service.update.mockReset().mockReturnValue(of(ROW));
    service.delete.mockReset().mockReturnValue(of(undefined));
    applications.getPage.mockReset().mockReturnValue(
      of({
        content: [
          { id: '7', code: 'APP-001', name: 'Invai' },
          { id: '8', code: 'APP-002', name: 'Portal Salut' },
          { id: '9', code: 'APP-003', name: 'Registre' },
        ],
        totalElements: 3,
      }),
    );
    technologies.getActiveOptions.mockReset().mockReturnValue(
      of([{ id: 4, label: 'Angular', layerId: 2, layerLabel: 'Frontend' }]),
    );
    externalSystems.getActiveOptions.mockReset().mockReturnValue(of([{ id: 2, label: 'Soffid' }]));
    people.searchSoffid.mockReset().mockReturnValue(
      of({ content: [USER, { ...USER, userName: null, email: 'sense@caib.es' }], totalElements: 2 }),
    );
    roles.search.mockReset().mockReturnValue(
      of({
        content: [
          { id: null, roleId: 33, name: 'INV_READ', system: 'SEYCON', description: null },
          { id: null, roleId: null, name: 'SENSE_ID', system: null, description: null },
        ],
        totalElements: 2,
      }),
    );

    TestBed.configureTestingModule({
      providers: [
        ApplicationIntegrationConnectionsState,
        MessageService,
        { provide: ApplicationDetailState, useValue: detail },
        { provide: ApplicationIntegrationConnectionsService, useValue: service },
        { provide: ApplicationsService, useValue: applications },
        { provide: TechnologyCatalogService, useValue: technologies },
        { provide: ExternalSystemCatalogService, useValue: externalSystems },
        { provide: ResponsiblePeopleService, useValue: people },
        { provide: SoffidRolesService, useValue: roles },
      ],
    });
    state = TestBed.inject(ApplicationIntegrationConnectionsState);
    messages = TestBed.inject(MessageService);
    vi.spyOn(messages, 'add');
    state.initialize(loadResult());
  });

  it('initializes the resolved page and reports a failed listing inline', () => {
    expect(state.items()).toEqual({ items: [ROW], total: 1 });
    expect(state.loadError()).toBeNull();

    state.initialize(
      loadResult({
        connectionsPage: null,
        connectionsLoadFailed: true,
        connectionsErrorMessage: 'Soffid no disponible',
      }),
    );
    expect(state.items()).toEqual({ items: [], total: 0 });
    expect(state.loadError()).toBe('Soffid no disponible');
  });

  it('allows management only while the section is edited and the anchor is loaded', () => {
    expect(state.canManage()).toBe(true);
    editing.set(false);
    expect(state.canManage()).toBe(false);
    state.create();
    expect(state.visible()).toBe(false);

    editing.set(true);
    detail.integrationStatus.set('absent');
    detail.appIntegrationId.set(null);
    expect(state.canManage()).toBe(false);
  });

  it('opens a consultation without loading the dialog catalogs', () => {
    state.onAction({ action: ApplicationIntegrationsTableAction.View, params: ROW });

    expect(state.visible()).toBe(true);
    expect(state.mode()).toBe('view');
    expect(state.selected()).toBe(ROW);
    expect(technologies.getActiveOptions).not.toHaveBeenCalled();
  });

  it('creates a connection with an external system and reloads the page', () => {
    state.create();
    expect(state.mode()).toBe('create');
    expect(technologies.getActiveOptions).toHaveBeenCalledOnce();
    expect(state.technologyOptions()).toEqual([{ id: 4, label: 'Angular' }]);
    expect(state.externalSystemOptions()).toEqual([{ id: 2, label: 'Soffid' }]);

    state.form.controls.systemKind.setValue('external');
    state.form.patchValue({
      externalSystemId: 2,
      technologyId: 4,
      user: USER,
      requiredRoles: [{ roleId: 26, label: 'INV_ADMIN' }, { roleId: 33, label: 'INV_READ' }],
    });
    service.getPage.mockClear();
    state.save();

    expect(service.create).toHaveBeenCalledWith({
      appIntegrationId: 13,
      applicationId: null,
      externalSystemId: 2,
      technologyId: 4,
      username: 'u00009',
      requiredRoleIds: [26, 33],
    });
    expect(state.visible()).toBe(false);
    expect(service.getPage).toHaveBeenCalledWith(
      expect.objectContaining({ appIntegrationId: 13, page: 0, statusId: 1, sort: 'id,asc' }),
    );
    expect(detail.refreshCompletenessAfterMutation).toHaveBeenCalledOnce();
    expect(messages.add).toHaveBeenCalledWith(expect.objectContaining({ severity: 'success' }));

    state.create();
    expect(technologies.getActiveOptions).toHaveBeenCalledOnce();
  });

  it('marks an invalid form as touched without sending it', () => {
    state.create();
    state.save();

    expect(service.create).not.toHaveBeenCalled();
    expect(state.form.controls.applicationId.touched).toBe(true);
    expect(state.visible()).toBe(true);
  });

  it('opens an edit with the stored snapshot and keeps the selected values as options', () => {
    state.onAction({ action: ApplicationIntegrationsTableAction.Edit, params: ROW });

    expect(state.mode()).toBe('edit');
    expect(state.form.getRawValue()).toEqual({
      systemKind: 'application',
      applicationId: 8,
      externalSystemId: null,
      technologyId: 9,
      user: expect.objectContaining({ userName: 'u00004', label: 'u00004' }),
      requiredRoles: [{ roleId: 26, label: 'INV_ADMIN — SEYCON' }],
    });
    expect(state.form.valid).toBe(true);
    expect(state.form.pristine).toBe(true);
    expect(state.applicationSearch().options).toEqual([{ id: 8, label: 'APP-002 — Portal Salut' }]);
    expect(state.technologyOptions()).toContainEqual({ id: 9, label: 'Struts' });

    state.save();
    expect(service.update).toHaveBeenCalledWith(1, {
      appIntegrationId: 13,
      applicationId: 8,
      externalSystemId: null,
      technologyId: 9,
      username: 'u00004',
      requiredRoleIds: [26],
    });
  });

  it('restores the snapshot when an edit is cancelled', () => {
    state.onAction({ action: ApplicationIntegrationsTableAction.View, params: ROW });
    state.edit();
    state.form.controls.technologyId.setValue(4);
    state.cancelEdit();

    expect(state.mode()).toBe('view');
    expect(state.form.controls.technologyId.value).toBe(9);
  });

  it('keeps the dialog open and leaves structured errors to the global dialog', () => {
    state.onAction({ action: ApplicationIntegrationsTableAction.Edit, params: ROW });
    service.update.mockReturnValueOnce(
      throwError(
        () => new HttpErrorResponse({ status: 400, error: { error: 'Error', message: 'Ambigu' } }),
      ),
    );
    state.save();
    expect(state.visible()).toBe(true);
    expect(messages.add).not.toHaveBeenCalled();

    service.update.mockReturnValueOnce(throwError(() => new HttpErrorResponse({ status: 500 })));
    state.save();
    expect(state.visible()).toBe(true);
    expect(messages.add).toHaveBeenCalledWith(
      expect.objectContaining({ severity: 'error', detail: "No s'ha pogut desar la connexió." }),
    );
  });

  it('confirms a deletion before soft-deleting and refreshing', () => {
    state.onAction({ action: ApplicationIntegrationsTableAction.Delete, params: ROW });
    expect(state.deleteVisible()).toBe(true);
    expect(service.delete).not.toHaveBeenCalled();

    state.confirmDelete();

    expect(service.delete).toHaveBeenCalledWith(1);
    expect(state.deleteVisible()).toBe(false);
    expect(detail.refreshCompletenessAfterMutation).toHaveBeenCalledOnce();
    expect(messages.add).toHaveBeenCalledWith(expect.objectContaining({ severity: 'success' }));
  });

  it('reports a failed page inline and clears it once a reload succeeds', () => {
    service.getPage.mockReturnValueOnce(
      throwError(
        () =>
          new HttpErrorResponse({
            status: 400,
            error: { error: 'Error', message: "No s'ha pogut consultar els rols atorgats." },
          }),
      ),
    );
    state.onPage({ first: 10, rows: 10, sortField: 'username', sortOrder: -1 });

    expect(service.getPage).toHaveBeenLastCalledWith(
      expect.objectContaining({ page: 1, size: 10, sort: 'username,desc' }),
    );
    expect(state.loadError()).toBe("No s'ha pogut consultar els rols atorgats.");
    expect(state.loading()).toBe(false);

    state.reload();
    expect(state.loadError()).toBeNull();
    expect(state.items().items).toEqual([ROW]);
  });

  it('steps back a page when the current one is left empty', () => {
    service.getPage
      .mockReturnValueOnce(of({ content: [], totalElements: 10, number: 1 }))
      .mockReturnValueOnce(of(PAGE));
    state.onPage({ first: 10, rows: 10 });

    expect(service.getPage).toHaveBeenLastCalledWith(expect.objectContaining({ page: 0 }));
    expect(state.first()).toBe(0);
  });

  it('searches other active applications while keeping the current selection', () => {
    state.onAction({ action: ApplicationIntegrationsTableAction.Edit, params: ROW });
    applications.getPage.mockReturnValueOnce(
      of({ content: [{ id: '9', code: 'APP-003', name: 'Registre' }], totalElements: 1 }),
    );
    state.searchApplications(' reg ');

    expect(applications.getPage).toHaveBeenCalledWith({
      page: 0,
      size: 20,
      sort: 'name,asc',
      statusId: 1,
      quickSearch: 'reg',
    });
    expect(state.applicationSearch().options).toEqual([
      { id: 8, label: 'APP-002 — Portal Salut' },
      { id: 9, label: 'APP-003 — Registre' },
    ]);

    state.searchApplications('');
    expect(state.applicationSearch().options.map((option) => option.id)).toEqual([8, 9]);
    expect(state.applicationSearch().options.some((option) => option.id === 7)).toBe(false);
  });

  it('searches Soffid users with a user code only from three characters', () => {
    state.searchUsers('ma');
    expect(people.searchSoffid).not.toHaveBeenCalled();

    state.searchUsers('mar');
    expect(people.searchSoffid).toHaveBeenCalledWith('mar');
    expect(state.userSearch().options).toEqual([
      expect.objectContaining({ userName: 'u00009', label: 'Maria Tur (u00009)' }),
    ]);

    people.searchSoffid.mockReturnValueOnce(throwError(() => new Error('offline')));
    state.searchUsers('mari');
    expect(state.userSearch()).toEqual(expect.objectContaining({ error: true, options: [] }));
  });

  it('searches Soffid roles by their Soffid identifier and keeps the selected ones', () => {
    state.onAction({ action: ApplicationIntegrationsTableAction.Edit, params: ROW });
    state.searchRoles('INV');

    expect(roles.search).toHaveBeenCalledWith('INV');
    expect(state.roleSearch().options).toEqual([
      { roleId: 26, label: 'INV_ADMIN — SEYCON' },
      { roleId: 33, label: 'INV_READ — SEYCON' },
    ]);

    roles.search.mockReturnValueOnce(throwError(() => new Error('offline')));
    state.searchRoles('INV_X');
    expect(state.roleSearch()).toEqual(expect.objectContaining({ error: true, loading: false }));
    expect(state.roleSearch().options).toEqual([{ roleId: 26, label: 'INV_ADMIN — SEYCON' }]);
  });

  it('reports catalog failures and retries them on the next opening', () => {
    technologies.getActiveOptions.mockReturnValueOnce(throwError(() => new Error('offline')));
    state.create();
    expect(messages.add).toHaveBeenCalledWith(expect.objectContaining({ severity: 'error' }));

    state.close();
    state.create();
    expect(technologies.getActiveOptions).toHaveBeenCalledTimes(2);
    expect(state.technologyOptions()).toEqual([{ id: 4, label: 'Angular' }]);
  });
});
