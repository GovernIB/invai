import { HttpErrorResponse } from '@angular/common/http';
import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder, FormGroup } from '@angular/forms';
import { By } from '@angular/platform-browser';
import { ActivatedRoute } from '@angular/router';
import { SpringPage } from '@models/page.model';
import { MessageService } from 'primeng/api';
import { Select } from 'primeng/select';
import { of, Subject, throwError } from 'rxjs';
import {
  ApplicationSecurityRole,
  ApplicationSecurityRoleOutput,
  ApplicationWebContextOutput,
} from '../../../../applications.model';
import { ApplicationSecurityTableAction } from './application-security-resource-table';

import { createApplicationSecurityForm } from '../../../../forms/application-security-form.factory';
import { ApplicationDevelopmentWebContextsService } from '../../../../services/application-development-web-contexts.service';
import { SoffidRolesService } from '../../../../services/soffid-roles.service';
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
        { provide: ApplicationDevelopmentWebContextsService, useValue: { clearCache: vi.fn() } },
        { provide: ApplicationSecurityRisksService, useValue: resourceServiceStub() },
        { provide: ApplicationSecurityMeasuresService, useValue: resourceServiceStub() },
        {
          provide: SoffidRolesService,
          useValue: { search: vi.fn(() => of(emptyPage<ApplicationSecurityRole>())) },
        },
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
    component.roleFilters.patchValue({ securityRoleName: ' INV_ ', system: ' weblogic ' });
    component.applyRoleFilters();
    expect(service.getPage).toHaveBeenLastCalledWith(
      expect.objectContaining({ page: 0, securityRoleName: 'INV_', system: 'weblogic', search: undefined }),
    );
    component.roleFilters.patchValue({ securityRoleName: 'unapplied', system: 'unapplied' });
    component.onPageChange('role', { first: 10, rows: 10, sortField: 'securityRole.system', sortOrder: -1 });
    expect(first.observed).toBe(false);
    expect(component.rolesLoading()).toBe(true);
    expect(service.getPage).toHaveBeenLastCalledWith(
      expect.objectContaining({
        page: 1,
        securityRoleName: 'INV_',
        system: 'weblogic',
        sort: 'securityRole.system,desc',
      }),
    );
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
    component.roleFilters.patchValue({ securityRoleName: '', system: '   ' });
    component.applyRoleFilters();
    expect(service.getPage).toHaveBeenLastCalledWith(
      expect.objectContaining({
        page: 0,
        securityRoleName: undefined,
        system: undefined,
        sort: 'securityRole.system,desc',
      }),
    );
    expect(component.roles().items).toEqual([]);
  });

  it('searches roles from the toolbar and reveals the code and system filters on demand', async () => {
    vi.useFakeTimers();
    try {
      const service = TestBed.inject(ApplicationSecurityRolesService);
      vi.mocked(service.getPage).mockReturnValue(of(emptyPage<ApplicationSecurityRoleOutput>()));
      const header = fixture.nativeElement.querySelector(
        '.application-security__resource-header--toolbar',
      ) as HTMLElement;
      const search = header.querySelector('input[type="text"]') as HTMLInputElement;
      const filtersButton = header.querySelector('button[aria-expanded]') as HTMLButtonElement;
      expect(search.getAttribute('aria-label')).toBe("Cerca rols de l'aplicació per codi o descripció");
      expect(search.placeholder).toBe('Cerca per codi o descripció');
      expect(filtersButton.getAttribute('aria-label')).toBe(
        'Mostra o oculta els filtres de rols (1 filtre aplicat)',
      );
      expect(filtersButton.getAttribute('aria-expanded')).toBe('false');
      expect(header.querySelector('.p-badge')?.textContent?.trim()).toBe('1');
      expect(header.querySelector('button[aria-label="Afegeix un rol de l\'aplicació"]')).toBeNull();
      expect(fixture.nativeElement.querySelector('app-search-filters')).toBeNull();

      search.value = ' admin ';
      search.dispatchEvent(new Event('input'));
      vi.advanceTimersByTime(399);
      expect(service.getPage).not.toHaveBeenCalled();
      vi.advanceTimersByTime(1);
      expect(service.getPage).toHaveBeenCalledExactlyOnceWith(
        expect.objectContaining({ page: 0, search: 'admin', system: 'weblogic' }),
      );
      search.value = 'admin';
      search.dispatchEvent(new Event('input'));
      fixture.detectChanges();
      expect(search.getAttribute('aria-busy')).toBe('true');
      vi.advanceTimersByTime(400);
      fixture.detectChanges();
      expect(service.getPage).toHaveBeenCalledOnce();
      expect(search.getAttribute('aria-busy')).toBe('false');

      filtersButton.click();
      fixture.detectChanges();
      expect(filtersButton.getAttribute('aria-expanded')).toBe('true');
      const code = fixture.nativeElement.querySelector(
        '#application-security-role-filter-code',
      ) as HTMLInputElement;
      const system = fixture.nativeElement.querySelector(
        '#application-security-role-filter-system',
      ) as HTMLInputElement;
      expect(code.labels?.[0].textContent).toContain('Codi');
      expect(system.labels?.[0].textContent).toContain('Sistema');
      expect(system.value).toBe('weblogic');
      code.value = 'INV_';
      code.dispatchEvent(new Event('input'));
      fixture.detectChanges();
      expect(header.querySelector('.p-badge')?.textContent?.trim()).toBe('2');
      expect(filtersButton.getAttribute('aria-label')).toBe(
        'Mostra o oculta els filtres de rols (2 filtres aplicats)',
      );
      expect(service.getPage).toHaveBeenCalledOnce();

      const [searchButton, resetButton] = Array.from(
        fixture.nativeElement.querySelectorAll('app-search-filters footer button'),
      ) as HTMLButtonElement[];
      searchButton.click();
      expect(service.getPage).toHaveBeenLastCalledWith(
        expect.objectContaining({ page: 0, search: 'admin', securityRoleName: 'INV_', system: 'weblogic' }),
      );
      system.value = '';
      system.dispatchEvent(new Event('input'));
      resetButton.click();
      fixture.detectChanges();
      expect(code.value).toBe('');
      expect(system.value).toBe('weblogic');
      expect(header.querySelector('.p-badge')?.textContent?.trim()).toBe('1');
      expect(service.getPage).toHaveBeenLastCalledWith(
        expect.objectContaining({ page: 0, search: 'admin', securityRoleName: undefined, system: 'weblogic' }),
      );
    } finally {
      vi.useRealTimers();
    }
  });

  it('assigns, consults, changes and removes application roles with Soffid candidates', () => {
    const service = TestBed.inject(ApplicationSecurityRolesService);
    const soffid = TestBed.inject(SoffidRolesService);
    vi.mocked(service.getPage).mockReturnValue(of(emptyPage<ApplicationSecurityRoleOutput>()));
    vi.mocked(service.create).mockReturnValue(of({} as ApplicationSecurityRoleOutput));
    vi.mocked(service.update).mockReturnValue(of({} as ApplicationSecurityRoleOutput));
    vi.mocked(service.delete).mockReturnValue(of(void 0));
    const component = fixture.componentInstance as unknown as RoleHarness;

    component.openCreateDialog('role');
    expect(component.resourceDialogVisible()).toBe(false);
    state.editing.set(true);
    fixture.detectChanges();
    const add = fixture.nativeElement.querySelector(
      'button[aria-label="Afegeix un rol de l\'aplicació"]',
    ) as HTMLButtonElement;
    expect(add.disabled).toBe(false);
    add.click();
    fixture.detectChanges();
    expect(component.resourceDialogVisible()).toBe(true);
    expect(component.resourceDialogMode()).toBe('create');
    const roleSelect = selectById('application-security-dialog-role');
    expect(roleSelect.filter).toBe(true);
    expect(roleSelect.ariaFilterLabel).toBe('Codi');
    component.submitResource();
    fixture.detectChanges();
    expect(service.create).not.toHaveBeenCalled();
    expect(document.querySelector('#application-security-dialog-role-error')?.textContent).toContain(
      'Selecciona un rol de Soffid.',
    );

    vi.mocked(soffid.search).mockReturnValueOnce(
      of(pageOf<ApplicationSecurityRole>([
        { id: null, roleId: 501, name: 'INV_ADMIN', system: 'weblogic', description: 'Administració' },
        { id: null, roleId: null, name: 'Without id', system: null, description: null },
      ], 35)),
    );
    component.searchSoffidRoles('INV');
    expect(soffid.search).toHaveBeenCalledExactlyOnceWith('INV');
    expect(component.roleSearch()).toEqual({
      options: [{ roleId: 501, name: 'INV_ADMIN', system: 'weblogic', description: 'Administració' }],
      loading: false,
      searched: true,
      error: false,
      total: 35,
    });
    component.resourceForm.controls['role'].setValue(component.roleSearch().options[0]);
    component.submitResource();
    expect(service.create).toHaveBeenCalledExactlyOnceWith({ appSecurityId: 9, securityRoleId: 501 });
    expect(component.resourceDialogVisible()).toBe(false);
    expect(service.getPage).toHaveBeenLastCalledWith(expect.objectContaining({ page: 0, system: 'weblogic' }));
    expect(state.refreshCompletenessAfterMutation).toHaveBeenCalledOnce();

    const row = {
      id: 12,
      securityRole: { id: 3, roleId: 501, name: 'INV_ADMIN', system: 'weblogic', description: 'Administració' },
      deletedAt: null,
    } as ApplicationSecurityRoleOutput;
    component.onTableAction('role', { action: ApplicationSecurityTableAction.View, params: row });
    fixture.detectChanges();
    expect(component.resourceDialogMode()).toBe('view');
    expect(fixture.nativeElement.querySelector('#application-security-dialog-role')).toBeNull();
    const dialogText = document.querySelector('app-application-security-resource-dialog')?.textContent;
    expect(dialogText).toContain('INV_ADMIN');
    expect(dialogText).toContain('weblogic');
    expect(dialogText).toContain('Administració');
    component.startResourceEdit();
    fixture.detectChanges();
    expect(component.roleSearch().options).toEqual([
      { roleId: 501, name: 'INV_ADMIN', system: 'weblogic', description: 'Administració' },
    ]);
    vi.mocked(soffid.search).mockReturnValueOnce(throwError(() => new Error('Soffid')));
    component.searchSoffidRoles('');
    expect(component.roleSearch()).toMatchObject({ loading: false, error: true });
    expect(component.roleSearch().options).toHaveLength(1);
    component.resourceForm.controls['role'].setValue({
      roleId: 502, name: 'INV_USER', system: 'weblogic', description: null,
    });
    component.submitResource();
    expect(service.update).toHaveBeenCalledExactlyOnceWith(12, { appSecurityId: 9, securityRoleId: 502 });

    component.onTableAction('role', { action: ApplicationSecurityTableAction.Delete, params: row });
    expect(component.deleteDialogVisible()).toBe(true);
    component.confirmDelete();
    expect(service.delete).toHaveBeenCalledExactlyOnceWith(12);
    expect(component.deleteDialogVisible()).toBe(false);
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
    expect(fixture.nativeElement.querySelector('button[aria-label="Afegeix un context web"]')).toBeNull();
    expect(fixture.nativeElement.querySelector('button[aria-label="Edita el registre"]')).toBeNull();
    expect(fixture.nativeElement.querySelector('#application-security-dialog-url').readOnly).toBe(true);
  });

  it('requires edit mode and a nonblank reason before validating a web context', () => {
    const service = TestBed.inject(ApplicationWebContextsService);
    const developmentCache = TestBed.inject(ApplicationDevelopmentWebContextsService);
    const refresh = new Subject<SpringPage<ApplicationWebContextOutput>>();
    vi.mocked(service.getPage).mockReturnValueOnce(refresh);
    const component = fixture.componentInstance as unknown as {
      onTableAction(kind: string, event: object): void;
      verifyWebContext(): void;
      verificationForm: FormGroup;
      webContexts: {
        set(value: { items: ApplicationWebContextOutput[]; total: number }): void;
        (): { items: ApplicationWebContextOutput[]; total: number };
      };
    };
    const table = fixture.nativeElement.querySelector('app-application-security-resource-table[kind="web-context"]') as HTMLElement;
    expect(table.querySelector('.application-security-verification-column')).toBeNull();
    const row = { id: 3, validated: false, deletedAt: null, webContext: { id: 1, name: 'Web' } } as ApplicationWebContextOutput;
    component.webContexts.set({ items: [row], total: 1 });
    component.onTableAction('web-context', { action: ApplicationSecurityTableAction.Verify, params: row });
    expect(fixture.nativeElement.querySelector('app-application-web-context-verification-dialog')).toBeNull();
    state.editing.set(true);
    fixture.detectChanges();
    expect(table.querySelector('.application-security-verification-column')).not.toBeNull();
    component.onTableAction('web-context', { action: ApplicationSecurityTableAction.Verify, params: row });
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('app-application-web-context-verification-dialog')).not.toBeNull();
    component.verifyWebContext();
    expect(service.validate).not.toHaveBeenCalled();
    fixture.detectChanges();
    expect(document.querySelector('#application-web-context-verification-reason-error')?.textContent)
      .toContain('Indica el motiu');
    expect(document.querySelector('#application-web-context-verification-reason')?.getAttribute('aria-invalid'))
      .toBe('true');
    component.verificationForm.controls['reason'].setValue('  Motiu  ');
    component.verifyWebContext();
    expect(service.validate).toHaveBeenCalledExactlyOnceWith(3, 'Motiu');
    expect(component.webContexts().items[0]).toMatchObject({ validated: true, validatedReason: 'Motiu' });
    expect(developmentCache.clearCache).toHaveBeenCalledOnce();
    expect(state.refreshWebContextVerification).toHaveBeenCalledOnce();
    expect(service.getPage).toHaveBeenCalledOnce();
    state.editing.set(false);
    fixture.detectChanges();
    expect(table.querySelector('.application-security-verification-column')).toBeNull();
  });

  it('keeps the verification dialog open on failure and prevents duplicate requests', () => {
    const service = TestBed.inject(ApplicationWebContextsService);
    const developmentCache = TestBed.inject(ApplicationDevelopmentWebContextsService);
    const request = new Subject<void>();
    vi.mocked(service.validate).mockReturnValueOnce(request);
    const component = fixture.componentInstance as unknown as {
      onTableAction(kind: string, event: object): void;
      verifyWebContext(): void;
      verificationForm: FormGroup;
      verificationDialogVisible: () => boolean;
    };
    state.editing.set(true);
    fixture.detectChanges();
    component.onTableAction('web-context', {
      action: ApplicationSecurityTableAction.Verify,
      params: { id: 3, validated: false, deletedAt: null, webContext: { id: 1, name: 'Web' } },
    });
    component.verificationForm.controls['reason'].setValue('Motiu');
    component.verifyWebContext();
    component.verifyWebContext();
    expect(service.validate).toHaveBeenCalledOnce();
    request.error(new Error('Unavailable'));
    expect(component.verificationDialogVisible()).toBe(true);
    expect(state.refreshWebContextVerification).not.toHaveBeenCalled();
    expect(developmentCache.clearCache).not.toHaveBeenCalled();
    component.verifyWebContext();
    expect(service.validate).toHaveBeenCalledTimes(2);
    expect(component.verificationDialogVisible()).toBe(false);
    expect(developmentCache.clearCache).toHaveBeenCalledOnce();
  });

  it('moves focus into the reason field and returns it to the action on cancel', async () => {
    const row = { id: 3, validated: false, deletedAt: null, webContext: { id: 1, name: 'Web' } } as ApplicationWebContextOutput;
    const component = fixture.componentInstance as unknown as {
      webContexts: { set(value: { items: ApplicationWebContextOutput[]; total: number }): void };
      closeVerificationDialog(): void;
    };
    component.webContexts.set({ items: [row], total: 1 });
    state.editing.set(true);
    fixture.detectChanges();
    const button = fixture.nativeElement.querySelector('app-application-security-resource-table[kind="web-context"] tbody button') as HTMLButtonElement;
    button.focus();
    button.click();
    fixture.detectChanges();
    await new Promise((resolve) => setTimeout(resolve, 0));
    expect(document.activeElement?.id).toBe('application-web-context-verification-reason');
    component.closeVerificationDialog();
    fixture.detectChanges();
    await new Promise((resolve) => setTimeout(resolve, 0));
    expect(document.activeElement).toBe(button);
  });

  describe('manual role validation', () => {
    const pendingRole = {
      id: 12,
      securityRole: { id: 3, roleId: 501, name: 'ADM_ROOT', system: 'weblogic', description: null },
      deletedAt: null,
      validationStatus: 'NOT_VALIDATED',
      manualValidatedAt: null,
      manualValidatedBy: null,
    } as ApplicationSecurityRoleOutput;

    function roleValidation() {
      return fixture.componentInstance as unknown as {
        roles: { set(value: { items: ApplicationSecurityRoleOutput[]; total: number }): void };
        onTableAction(kind: string, event: object): void;
        validateRole(): void;
        closeRoleValidationDialog(): void;
        roleValidationDialogVisible(): boolean;
      };
    }

    function requestValidation(role: ApplicationSecurityRoleOutput = pendingRole): void {
      roleValidation().onTableAction('role', {
        action: ApplicationSecurityTableAction.Verify,
        params: role,
      });
      fixture.detectChanges();
    }

    beforeEach(() => {
      roleValidation().roles.set({ items: [pendingRole], total: 1 });
      state.editing.set(true);
      fixture.detectChanges();
    });

    it('confirms the validation, then reloads the roles, reports it and refocuses the row', async () => {
      const service = TestBed.inject(ApplicationSecurityRolesService);
      const add = vi.spyOn(TestBed.inject(MessageService), 'add');
      vi.mocked(service.getPage).mockReturnValue(
        of(pageOf([{ ...pendingRole, validationStatus: 'MANUAL' } as ApplicationSecurityRoleOutput], 1)),
      );

      requestValidation();
      expect(roleValidation().roleValidationDialogVisible()).toBe(true);
      const dialogText = document.querySelector('app-confirmation-dialog')?.textContent;
      expect(dialogText).toContain('Validar manualment el rol?');
      expect(dialogText).toContain(
        "El rol «ADM_ROOT» no comença pel prefix de l'aplicació. Si el valides, quedarà registrat al teu nom.",
      );
      expect(service.validate).not.toHaveBeenCalled();

      roleValidation().validateRole();
      fixture.detectChanges();
      await new Promise((resolve) => setTimeout(resolve, 0));

      expect(service.validate).toHaveBeenCalledExactlyOnceWith(12);
      expect(roleValidation().roleValidationDialogVisible()).toBe(false);
      expect(service.getPage).toHaveBeenCalledOnce();
      expect(add).toHaveBeenCalledExactlyOnceWith(
        expect.objectContaining({ severity: 'success', detail: "El rol s'ha validat manualment." }),
      );
      expect(document.activeElement?.getAttribute('data-role-id')).toBe('12');
    });

    it('returns focus to the row without validating when the confirmation is cancelled', async () => {
      const service = TestBed.inject(ApplicationSecurityRolesService);

      requestValidation();
      roleValidation().closeRoleValidationDialog();
      fixture.detectChanges();
      await new Promise((resolve) => setTimeout(resolve, 0));

      expect(roleValidation().roleValidationDialogVisible()).toBe(false);
      expect(service.validate).not.toHaveBeenCalled();
      expect(document.activeElement?.getAttribute('data-role-id')).toBe('12');
    });

    it('ignores the request outside edit mode and for roles not pending validation', () => {
      state.editing.set(false);
      requestValidation();
      expect(roleValidation().roleValidationDialogVisible()).toBe(false);

      state.editing.set(true);
      requestValidation({ ...pendingRole, validationStatus: 'MANUAL' });
      requestValidation({ ...pendingRole, deletedAt: '2026-10-01T10:00:00' });
      expect(roleValidation().roleValidationDialogVisible()).toBe(false);
    });

    it('sends a single request while the validation is pending', () => {
      const service = TestBed.inject(ApplicationSecurityRolesService);
      const request = new Subject<void>();
      vi.mocked(service.validate).mockReturnValueOnce(request);

      requestValidation();
      roleValidation().validateRole();
      roleValidation().validateRole();
      roleValidation().closeRoleValidationDialog();

      expect(service.validate).toHaveBeenCalledOnce();
      expect(roleValidation().roleValidationDialogVisible()).toBe(true);
      request.next();
      request.complete();
      expect(roleValidation().roleValidationDialogVisible()).toBe(false);
    });

    it('leaves a structured 400 to the server error dialog and reloads the current roles', () => {
      const service = TestBed.inject(ApplicationSecurityRolesService);
      const add = vi.spyOn(TestBed.inject(MessageService), 'add');
      vi.mocked(service.validate).mockReturnValueOnce(
        throwError(
          () =>
            new HttpErrorResponse({
              status: 400,
              error: { error: 'Bad Request', message: 'Només es pot validar un rol no validat.' },
            }),
        ),
      );

      requestValidation();
      roleValidation().validateRole();

      expect(add).not.toHaveBeenCalled();
      expect(roleValidation().roleValidationDialogVisible()).toBe(false);
      expect(service.getPage).toHaveBeenCalledOnce();
    });

    it('reports any other validation failure', () => {
      const service = TestBed.inject(ApplicationSecurityRolesService);
      const add = vi.spyOn(TestBed.inject(MessageService), 'add');
      vi.mocked(service.validate).mockReturnValueOnce(
        throwError(() => new HttpErrorResponse({ status: 500 })),
      );

      requestValidation();
      roleValidation().validateRole();

      expect(add).toHaveBeenCalledExactlyOnceWith(
        expect.objectContaining({
          severity: 'error',
          detail: "No s'ha pogut validar manualment el rol. Revisa el seu estat i torna-ho a provar.",
        }),
      );
    });

    it('warns in the role dialog that a role outside the prefix is saved as validated manually', () => {
      const component = fixture.componentInstance as unknown as RoleHarness;
      const note = () =>
        document.querySelector('#application-security-dialog-role-prefix-note') as HTMLElement;

      component.openCreateDialog('role');
      fixture.detectChanges();
      expect(note().getAttribute('role')).toBe('status');
      expect(note().textContent?.trim()).toBe('');
      expect(
        document
          .querySelector('[aria-describedby*="application-security-dialog-role-prefix-note"]'),
      ).not.toBeNull();

      component.resourceForm.controls['role'].setValue({
        roleId: 7, name: 'ADM_ROOT', system: 'weblogic', description: null,
      });
      fixture.detectChanges();
      expect(note().textContent?.trim()).toBe(
        'Aquest rol no comença per «INV_». Es desarà com a validat manualment al teu nom.',
      );

      for (const name of ['INV_ADMIN', 'inv_admin']) {
        component.resourceForm.controls['role'].setValue({
          roleId: 8, name, system: 'weblogic', description: null,
        });
        fixture.detectChanges();
        expect(note().textContent?.trim()).toBe(
          name === 'INV_ADMIN'
            ? ''
            : 'Aquest rol no comença per «INV_». Es desarà com a validat manualment al teu nom.',
        );
      }
    });
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
  openCreateDialog(kind: 'role' | 'web-context' | 'risk' | 'measure'): void;
}

interface RoleHarness {
  resourceForm: FormGroup;
  roleSearch(): {
    options: { roleId: number; name: string; system: string | null; description: string | null }[];
    loading: boolean;
    searched: boolean;
    error: boolean;
    total: number;
  };
  openCreateDialog(kind: string): void;
  onTableAction(kind: string, event: object): void;
  searchSoffidRoles(query: string): void;
  startResourceEdit(): void;
  submitResource(): void;
  confirmDelete(): void;
  resourceDialogVisible(): boolean;
  resourceDialogMode(): string;
  deleteDialogVisible(): boolean;
}

function createState() {
  const securityForm = createApplicationSecurityForm(new FormBuilder());
  const editing = signal(false);

  return {
    application: signal({ prefix: 'INV' }),
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
    refreshWebContextVerification: vi.fn(),
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
    validate: vi.fn(() => of(void 0)),
    getPage: vi.fn(() => of(emptyWebContextPage())),
  };
}

function emptyWebContextPage(): SpringPage<ApplicationWebContextOutput> {
  return emptyPage<ApplicationWebContextOutput>();
}

function emptyPage<T>(): SpringPage<T> {
  return pageOf<T>([], 0);
}

function pageOf<T>(content: T[], totalElements: number): SpringPage<T> {
  const sort = { empty: true, sorted: false, unsorted: true };
  return {
    content, totalElements, number: 0, empty: content.length === 0, first: true, last: true,
    numberOfElements: content.length, size: 10, totalPages: totalElements ? 1 : 0, sort,
    pageable: { offset: 0, pageNumber: 0, pageSize: 10, paged: true, unpaged: false, sort },
  };
}
