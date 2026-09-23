import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { MessageService } from 'primeng/api';
import { Subject, of, throwError } from 'rxjs';
import { SpringPage } from '@models/page.model';
import { ApplicationWebContextOutput } from '../../../../applications.model';
import { ApplicationWebContextsService } from '../../../../services/application-security.service';
import { ApplicationDetailState } from '../../application-detail-state';
import { ApplicationSecurityTableAction as Action } from '../security/application-security-resource-table';
import { ApplicationDevelopmentWebContextsState } from './application-development-web-contexts-state';

const row = {
  id: 3,
  appSecurity: { id: 9 },
  webContext: { id: 1, name: 'Web' },
  field: { id: 2, name: 'Intern' },
  url: 'https://original',
  observation: '',
  deletedAt: null,
} as ApplicationWebContextOutput;
const page = (content: ApplicationWebContextOutput[], number = 0) =>
  ({ content, totalElements: content.length, number }) as SpringPage<ApplicationWebContextOutput>;

describe('ApplicationDevelopmentWebContextsState', () => {
  let state: ApplicationDevelopmentWebContextsState;
  let detail: {
    canEdit: ReturnType<typeof signal<boolean>>;
    isEditing: ReturnType<typeof signal<boolean>>;
    appSecurityId: ReturnType<typeof signal<number | null>>;
    refreshCompletenessAfterMutation: ReturnType<typeof vi.fn>;
    updateWebContextCount: ReturnType<typeof vi.fn>;
  };
  let api: {
    getPage: ReturnType<typeof vi.fn>;
    create: ReturnType<typeof vi.fn>;
    update: ReturnType<typeof vi.fn>;
    delete: ReturnType<typeof vi.fn>;
  };

  beforeEach(() => {
    detail = {
      canEdit: signal(true),
      isEditing: signal(true),
      appSecurityId: signal<number | null>(9),
      refreshCompletenessAfterMutation: vi.fn(),
      updateWebContextCount: vi.fn(),
    };
    api = {
      getPage: vi.fn(() => of(page([row]))),
      create: vi.fn(() => of(row)),
      update: vi.fn(() => of(row)),
      delete: vi.fn(() => of(void 0)),
    };
    TestBed.configureTestingModule({
      providers: [
        ApplicationDevelopmentWebContextsState,
        MessageService,
        { provide: ApplicationDetailState, useValue: detail },
        { provide: ApplicationWebContextsService, useValue: api },
      ],
    });
    state = TestBed.inject(ApplicationDevelopmentWebContextsState);
    state.initialize({
      appSecurityId: 9,
      page: page([row]),
      webContextOptions: [],
      fieldOptions: [],
      loadFailed: false,
    });
  });

  it('consumes resolved rows without HTTP and uses selected labels absent from active catalogs', () => {
    expect(api.getPage).not.toHaveBeenCalled();
    state.onAction({ action: Action.View, params: row });
    expect(state.items().items).toEqual([row]);
    expect(detail.updateWebContextCount).toHaveBeenCalledWith(1);
    expect(state.webContextOptions()).toEqual([{ value: 1, label: 'Web' }]);
    expect(state.fieldOptions()).toEqual([{ value: 2, label: 'Intern' }]);
  });

  it('requires permission and section edit mode for all mutations, but permits consultation', () => {
    for (const permission of [detail.canEdit, detail.isEditing]) {
      permission.set(false);
      state.create();
      expect(state.visible()).toBe(false);
      state.onAction({ action: Action.Edit, params: row });
      expect(state.visible()).toBe(false);
      state.onAction({ action: Action.View, params: row });
      expect(state.mode()).toBe('view');
      state.edit();
      state.save();
      state.requestDelete(row);
      state.pendingDelete.set(row);
      state.confirmDelete();
      expect(state.mode()).toBe('view');
      expect(state.deleteVisible()).toBe(false);
      state.close();
      permission.set(true);
    }
    expect(api.create).not.toHaveBeenCalled();
    expect(api.update).not.toHaveBeenCalled();
    expect(api.delete).not.toHaveBeenCalled();
  });

  it('prevents mutations without the security anchor and for inactive rows', () => {
    state.initialize({
      appSecurityId: null,
      page: null,
      webContextOptions: [],
      fieldOptions: [],
      loadFailed: false,
    });
    detail.appSecurityId.set(null);
    state.create();
    expect(state.visible()).toBe(false);
    detail.appSecurityId.set(9);
    const inactive = { ...row, deletedAt: '2026-01-01' };
    state.onAction({ action: Action.Edit, params: inactive });
    state.requestDelete(inactive);
    expect(state.visible()).toBe(false);
    expect(state.deleteVisible()).toBe(false);
  });

  it('validates before creating and saves immediately with the security identifier', () => {
    state.create();
    state.save();
    expect(state.form.controls.webContextId.touched).toBe(true);
    expect(api.create).not.toHaveBeenCalled();
    state.form.patchValue({
      webContextId: 1,
      fieldId: 2,
      url: ' https://example.test ',
      observation: ' Note ',
    });
    state.save();
    expect(api.create).toHaveBeenCalledExactlyOnceWith({
      appSecurityId: 9,
      webContextId: 1,
      fieldId: 2,
      url: 'https://example.test',
      observation: 'Note',
    });
    expect(state.visible()).toBe(false);
    expect(api.getPage).toHaveBeenCalledOnce();
    expect(detail.refreshCompletenessAfterMutation).toHaveBeenCalledOnce();
  });

  it('restores the snapshot on cancel and normalizes blank optional values on update', () => {
    state.onAction({ action: Action.Edit, params: row });
    state.form.patchValue({ url: 'https://temporary' });
    state.form.markAsDirty();
    state.cancelEdit();
    expect(state.form.controls.url.value).toBe('https://original');
    expect(state.mode()).toBe('view');
    expect(state.form.pristine).toBe(true);
    state.edit();
    state.form.patchValue({ url: ' ', observation: ' ' });
    state.save();
    expect(api.update).toHaveBeenCalledWith(3, {
      appSecurityId: 9,
      webContextId: 1,
      fieldId: 2,
      url: null,
      observation: null,
    });
  });

  it('keeps an unsuccessful edit open and blocks duplicate pending submissions', () => {
    const pending = new Subject<ApplicationWebContextOutput>();
    api.update.mockReturnValueOnce(pending);
    state.onAction({ action: Action.Edit, params: row });
    state.save();
    state.save();
    expect(api.update).toHaveBeenCalledOnce();
    expect(state.saving()).toBe(true);
    pending.error(new Error('Unavailable'));
    expect(state.saving()).toBe(false);
    expect(state.visible()).toBe(true);
    expect(state.mode()).toBe('edit');
    expect(state.form.controls.url.value).toBe(row.url);
  });

  it('confirms deletion, refreshes and falls back to the previous page when it becomes empty', () => {
    state.onPage({ first: 10, rows: 10 });
    state.requestDelete(row);
    state.closeDelete();
    state.confirmDelete();
    expect(api.delete).not.toHaveBeenCalled();
    state.requestDelete(row);
    api.getPage.mockReturnValueOnce(of(page([], 1))).mockReturnValueOnce(of(page([row])));
    state.confirmDelete();
    expect(api.delete).toHaveBeenCalledExactlyOnceWith(3);
    expect(state.deleteVisible()).toBe(false);
    expect(state.first()).toBe(0);
    expect(api.getPage).toHaveBeenLastCalledWith({
      appSecurityId: 9,
      page: 0,
      size: 10,
      sort: 'id,asc',
      statusId: 1,
    });
  });

  it('keeps the delete confirmation open when the backend rejects deletion', () => {
    api.delete.mockReturnValueOnce(throwError(() => new Error('Unavailable')));
    state.requestDelete(row);
    state.confirmDelete();
    expect(state.deleteVisible()).toBe(true);
    expect(state.deleting()).toBe(false);
    expect(state.items().items).toEqual([row]);
  });

  it('preserves rows on refresh failure and cancels obsolete page requests', () => {
    const old = new Subject<SpringPage<ApplicationWebContextOutput>>();
    const current = new Subject<SpringPage<ApplicationWebContextOutput>>();
    api.getPage.mockReturnValueOnce(old).mockReturnValueOnce(current);
    state.onPage({ first: 10, rows: 10 });
    state.onPage({ first: 20, rows: 10, sortField: 'webContext.name', sortOrder: -1 });
    expect(old.observed).toBe(false);
    expect(state.loading()).toBe(true);
    expect(state.items().items).toEqual([row]);
    expect(api.getPage).toHaveBeenLastCalledWith({
      appSecurityId: 9,
      page: 2,
      size: 10,
      sort: 'webContext.name,desc',
      statusId: 1,
    });
    current.error(new Error('Unavailable'));
    expect(state.loading()).toBe(false);
    expect(state.items().items).toEqual([row]);
  });
});
