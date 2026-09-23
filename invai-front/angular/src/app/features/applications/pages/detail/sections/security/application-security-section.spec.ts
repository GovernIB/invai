import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder, FormGroup } from '@angular/forms';
import { By } from '@angular/platform-browser';
import { ActivatedRoute } from '@angular/router';
import { SpringPage } from '@models/page.model';
import { MessageService } from 'primeng/api';
import { Select } from 'primeng/select';
import { of, Subject, throwError } from 'rxjs';
import { ApplicationSecurityRoleOutput, ApplicationWebContextOutput } from '../../../../applications.model';
import { ApplicationSecurityTableAction } from './application-security-resource-table';

import { createApplicationSecurityForm } from '../../../../forms/application-security-form.factory';
import {
  ApplicationSecurityMeasuresService,
  ApplicationSecurityRisksService,
  ApplicationSecurityRolesService,
  ApplicationWebContextsService,
} from '../../../../services/application-security.service';
import { ApplicationDetailState } from '../../application-detail-state';
import { ApplicationSecuritySection } from './application-security-section';
import {
  APPLICATION_SECURITY_RESOLVE_KEY,
  ApplicationSecurityResolvedData,
} from './application-security-section.resolver';

const ENS_SELECT_IDS = [
  'application-security-overall-grade',
  'application-security-identity-provider',
  'application-security-ens-subject',
  'application-security-personal-data',
  'application-security-confidentiality',
  'application-security-integrity',
  'application-security-traceability',
  'application-security-availability',
  'application-security-authenticity',
] as const;

describe('ApplicationSecuritySection', () => {
  let fixture: ComponentFixture<ApplicationSecuritySection>;
  let state: ReturnType<typeof createState>;

  beforeEach(async () => {
    state = createState();

    await TestBed.configureTestingModule({
      imports: [ApplicationSecuritySection],
      providers: [
        MessageService,
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { data: resolvedData() } },
        },
        { provide: ApplicationDetailState, useValue: state },
        { provide: ApplicationSecurityRolesService, useValue: resourceServiceStub() },
        { provide: ApplicationWebContextsService, useValue: resourceServiceStub() },
        { provide: ApplicationSecurityRisksService, useValue: resourceServiceStub() },
        { provide: ApplicationSecurityMeasuresService, useValue: resourceServiceStub() },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ApplicationSecuritySection);
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it(
    'keeps the ENS grade editable, omits small-catalog filters and preserves dialog filters',
    () => {
      state.editing.set(true);
      fixture.detectChanges();

      const overallGrade = selectById('application-security-overall-grade');
      const overallGradeTrigger = fixture.nativeElement.querySelector(
        '#application-security-overall-grade',
      ) as HTMLElement;
      expect(state.securityForm.controls.overallGradeId.enabled).toBe(true);
      expect(overallGradeTrigger.getAttribute('aria-disabled')).not.toBe('true');
      expect(overallGrade.ariaLabelledBy).toBe('application-security-overall-grade-label');

      for (const inputId of ENS_SELECT_IDS) {
        const select = selectById(inputId);
        expect(select.filter).toBeFalsy();
        expect(select.ariaFilterLabel).toBeFalsy();
      }

      harness().openCreateDialog('measure');
      fixture.detectChanges();

      for (const inputId of [
        'application-security-dialog-measure-type',
        'application-security-dialog-ens-requirement',
      ]) {
        const select = selectById(inputId);
        expect(select.filter).toBe(true);
        expect(select.ariaFilterLabel).toBeTruthy();
      }
    },
    15_000,
  );

  it('applies filters from page zero, retains them on paging and cancels obsolete responses', () => {
    const component = fixture.componentInstance as unknown as {
      roleFilters: FormGroup;
      applyRoleFilters(): void;
      onPageChange(kind: string, event: object): void;
      roles(): { items: ApplicationSecurityRoleOutput[] };
      rolesLoading(): boolean;
    };
    const service = TestBed.inject(ApplicationSecurityRolesService);
    const first = new Subject<SpringPage<ApplicationSecurityRoleOutput>>();
    const second = new Subject<SpringPage<ApplicationSecurityRoleOutput>>();
    vi.mocked(service.getPage).mockReturnValueOnce(first).mockReturnValueOnce(second);
    expect(service.getPage).not.toHaveBeenCalled();
    component.roleFilters.patchValue({ system: ' weblogic ' });
    component.applyRoleFilters();
    expect(service.getPage).toHaveBeenLastCalledWith(expect.objectContaining({ page: 0, system: 'weblogic' }));
    component.roleFilters.patchValue({ system: 'unapplied' });
    component.onPageChange('role', { first: 10, rows: 10, sortField: 'securityRole.system', sortOrder: -1 });
    expect(first.observed).toBe(false);
    expect(component.rolesLoading()).toBe(true);
    expect(service.getPage).toHaveBeenLastCalledWith(expect.objectContaining({ page: 1, system: 'weblogic', sort: 'securityRole.system,desc' }));
    const row = { id: 2, securityRole: { name: 'Current', system: 'weblogic' } } as ApplicationSecurityRoleOutput;
    second.next({ content: [row], totalElements: 1, number: 0 } as SpringPage<ApplicationSecurityRoleOutput>);
    second.complete();
    first.next(emptyPage<ApplicationSecurityRoleOutput>());
    expect(component.roles().items).toEqual([row]);
    expect(component.rolesLoading()).toBe(false);
    const showMessage = vi.spyOn(TestBed.inject(MessageService), 'add');
    vi.mocked(service.getPage).mockReturnValueOnce(throwError(() => new Error('Failed')));
    component.applyRoleFilters();
    expect(component.roles().items).toEqual([row]);
    expect(component.rolesLoading()).toBe(false);
    expect(showMessage).toHaveBeenCalledWith(expect.objectContaining({ severity: 'error' }));
    vi.mocked(service.getPage).mockReturnValue(of(emptyPage<ApplicationSecurityRoleOutput>()));
    component.roleFilters.patchValue({ system: '   ' });
    component.applyRoleFilters();
    expect(service.getPage).toHaveBeenLastCalledWith(expect.objectContaining({ page: 0, system: undefined, sort: 'securityRole.system,desc' }));
    expect(component.roles().items).toEqual([]);
  });

  it('renders only the system filter and submits via the form or its accessible icon button', () => {
    const service = TestBed.inject(ApplicationSecurityRolesService);
    vi.mocked(service.getPage).mockReturnValue(of(emptyPage<ApplicationSecurityRoleOutput>()));
    const input = fixture.nativeElement.querySelector('#application-security-role-system') as HTMLInputElement;
    const form = input.form!;
    const button = form.querySelector('button')!;
    expect(input.value).toBe('weblogic');
    expect(input.labels?.[0].textContent).toContain('Sistema');
    expect(button.type).toBe('submit');
    expect(button.getAttribute('aria-label')).toBe('Cerca rols per sistema');
    expect(form.querySelectorAll('input')).toHaveLength(1);
    expect(fixture.nativeElement.querySelector('app-search-filters')).toBeNull();
    input.value = ' other ';
    input.dispatchEvent(new Event('input'));
    expect(service.getPage).not.toHaveBeenCalled();
    form.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
    expect(service.getPage).toHaveBeenCalledTimes(1);
    expect(service.getPage).toHaveBeenLastCalledWith(expect.objectContaining({ system: 'other' }));
    input.value = '';
    input.dispatchEvent(new Event('input'));
    button.click();
    expect(service.getPage).toHaveBeenCalledTimes(2);
    expect(service.getPage).toHaveBeenLastCalledWith(expect.objectContaining({ system: undefined }));
  });

  it('paginates web contexts without search criteria', () => {
    const service = TestBed.inject(ApplicationWebContextsService);
    vi.mocked(service.getPage).mockReturnValue(of(emptyWebContextPage()));
    const component = fixture.componentInstance as unknown as { onPageChange(kind: string, event: object): void };
    component.onPageChange('web-context', { first: 10, rows: 10 });
    expect(service.getPage).toHaveBeenCalledWith({ appSecurityId: 9, page: 1, size: 10, sort: 'id,asc', statusId: 1 });
  });

  it('keeps web contexts consultable but blocks every mutation even in section edit mode', () => {
    state.editing.set(true);
    const component = fixture.componentInstance as unknown as {
      onTableAction(kind: string, event: object): void;
      openCreateDialog(kind: string): void;
      resourceDialogVisible(): boolean;
      resourceDialogMode(): string;
      resourceDialogCanEdit(): boolean;
      startResourceEdit(): void;
      submitResource(): void;
      requestDelete(): void;
      confirmDelete(): void;
      deleteDialogVisible(): boolean;
    };
    const row = { id: 3, url: 'https://original', webContext: { id: 1, name: 'Web' }, field: { id: 2, name: 'Intern' }, observation: '', deletedAt: null };
    const service = TestBed.inject(ApplicationWebContextsService);
    component.openCreateDialog('web-context');
    expect(component.resourceDialogVisible()).toBe(false);
    component.onTableAction('web-context', { action: ApplicationSecurityTableAction.Edit, params: row });
    expect(component.resourceDialogVisible()).toBe(false);
    component.onTableAction('web-context', { action: ApplicationSecurityTableAction.View, params: row });
    expect(component.resourceDialogVisible()).toBe(true);
    expect(component.resourceDialogCanEdit()).toBe(false);
    component.startResourceEdit();
    expect(component.resourceDialogMode()).toBe('view');
    component.submitResource();
    component.onTableAction('web-context', { action: ApplicationSecurityTableAction.Delete, params: row });
    component.requestDelete();
    component.confirmDelete();
    fixture.detectChanges();
    expect(component.deleteDialogVisible()).toBe(false);
    expect(service.create).not.toHaveBeenCalled();
    expect(service.update).not.toHaveBeenCalled();
    expect(service.delete).not.toHaveBeenCalled();
    expect(fixture.nativeElement.querySelector('button[aria-label="Afegeix un context web"]')).toBeNull();
    expect(fixture.nativeElement.querySelector('button[aria-label="Edita el registre"]')).toBeNull();
    expect(fixture.nativeElement.querySelector('#application-security-dialog-url').readOnly).toBe(true);
  });

  it('only exposes verification in edit mode and keeps it informational', () => {
    const messages = vi.spyOn(TestBed.inject(MessageService), 'add');
    const service = TestBed.inject(ApplicationWebContextsService);
    const component = fixture.componentInstance as unknown as { onTableAction(kind: string, event: object): void };
    const table = fixture.nativeElement.querySelector('app-application-security-resource-table[kind="web-context"]') as HTMLElement;
    expect(table.querySelector('.application-security-verification-column')).toBeNull();
    component.onTableAction('web-context', { action: ApplicationSecurityTableAction.Verify, params: { id: 3 } });
    expect(messages).not.toHaveBeenCalled();
    state.editing.set(true);
    fixture.detectChanges();
    expect(table.querySelector('.application-security-verification-column')).not.toBeNull();
    component.onTableAction('web-context', { action: ApplicationSecurityTableAction.Verify, params: { id: 3 } });
    expect(messages).toHaveBeenCalledExactlyOnceWith(expect.objectContaining({ severity: 'info', detail: 'La verificació de contextos web encara no està implementada.' }));
    expect(service.getPage).not.toHaveBeenCalled();
    expect(service.create).not.toHaveBeenCalled();
    expect(service.update).not.toHaveBeenCalled();
    expect(service.delete).not.toHaveBeenCalled();
    state.editing.set(false);
    fixture.detectChanges();
    expect(table.querySelector('.application-security-verification-column')).toBeNull();
  });

  function selectById(inputId: string): Select {
    const select = fixture.debugElement
      .queryAll(By.directive(Select))
      .map(({ componentInstance }) => componentInstance as Select)
      .find((candidate) => candidate.inputId === inputId);
    expect(select).toBeTruthy();
    return select!;
  }

  function harness(): ApplicationSecuritySectionHarness {
    return fixture.componentInstance as unknown as ApplicationSecuritySectionHarness;
  }
});

interface ApplicationSecuritySectionHarness {
  openCreateDialog(kind: 'web-context' | 'risk' | 'measure'): void;
}

function createState() {
  const securityForm = createApplicationSecurityForm(new FormBuilder());
  const editing = signal(false);

  return {
    appSecurityId: signal<number | null>(9),
    canEdit: signal(true),
    editing,
    ensClassification: signal(null),
    ensClassificationLoadState: signal('ready'),
    securityForm,
    isEditing: vi.fn(() => editing()),
    startEditing: vi.fn(() => editing.set(true)),
    cancelEditing: vi.fn(() => editing.set(false)),
    save: vi.fn(() => of({ status: 'saved', section: 'security' as const })),
    initializeSecurity: vi.fn(),
    refreshCompletenessAfterMutation: vi.fn(),
    updateWebContextCount: vi.fn(),
  };
}

function resolvedData(): Record<string, ApplicationSecurityResolvedData> {
  return {
    [APPLICATION_SECURITY_RESOLVE_KEY]: {
      applicationId: 7,
      appSecurityId: 9,
      security: null,
      rolesPage: null,
      webContextsPage: null,
      classifications: [],
      risksPage: null,
      measuresPage: null,
      options: {
        securityLevels: [],
        identityProviders: [],
        ensSubjects: [],
        personalDataProcessing: [],
        webContexts: [],
        fields: [],
        measureTypes: [],
        ensRequirements: [],
      },
      failures: {
        security: false,
        roles: false,
        webContexts: false,
        classifications: false,
        risks: false,
        measures: false,
        securityLevels: false,
        identityProviders: false,
        ensSubjects: false,
        personalDataProcessing: false,
        webContextOptions: false,
        fields: false,
        measureTypes: false,
        ensRequirements: false,
      },
    },
  };
}

function resourceServiceStub() {
  return {
    create: vi.fn(),
    update: vi.fn(),
    delete: vi.fn(),
    getPage: vi.fn(),
  };
}

function emptyWebContextPage(): SpringPage<ApplicationWebContextOutput> {
  return emptyPage<ApplicationWebContextOutput>();
}

function emptyPage<T>(): SpringPage<T> {
  const sort = { empty: true, sorted: false, unsorted: true };
  return {
    content: [], totalElements: 0, number: 0, empty: true, first: true, last: true,
    numberOfElements: 0, size: 10, totalPages: 0, sort,
    pageable: { offset: 0, pageNumber: 0, pageSize: 10, paged: true, unpaged: false, sort },
  };
}
