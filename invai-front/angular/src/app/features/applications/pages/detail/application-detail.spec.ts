import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, Navigation, provideRouter, Router } from '@angular/router';
import { BreadcrumbService } from '@core/components/breadcrumbs';
import { AdministrativeUnitsService } from '@features/administrative-units/services/administrative-units.service';
import { firstValueFrom, of } from 'rxjs';
import { APPLICATION_GENERAL_EDIT_NAVIGATION } from './application-detail-navigation';

import {
  Application,
  ApplicationOutput,
  ApplicationStatus,
  ApplicationStatusCode,
} from '../../applications.model';
import { ApplicationDevelopmentService } from '../../services/application-development.service';
import { ApplicationOptionsService } from '../../services/application-options.service';
import {
  ApplicationEnsClassificationsService,
  ApplicationSecurityService,
} from '../../services/application-security.service';
import { ApplicationsService } from '../../services/applications.service';
import { ApplicationDetail } from './application-detail';
import { ApplicationDetailState } from './application-detail-state';
import {
  APPLICATION_DETAIL_RESOLVE_KEY,
  ApplicationDetailResolvedData,
} from './application-detail.resolver';

const APPLICATION_OUTPUT: ApplicationOutput = {
  id: 7,
  code: 'INVAI',
  prefix: 'INV',
  name: 'Inventari',
  category: { id: 1, name: 'Categoria', deletedAt: null },
  systemType: { id: 2, name: 'Sistema', deletedAt: null },
  field: { id: 3, name: 'Àmbit', deletedAt: null },
  admUnit: { code: 'UT', name: 'Unitat', parentCode: 'GVA01', level: 2 },
  department: { code: 'GVA01', name: 'Conselleria', parentCode: null, level: 1 },
  csCommission: null,
  description: '',
  status: ApplicationStatusCode.ACTIVE,
  expirationDate: null,
  createdAt: null,
  createdBy: null,
  updatedAt: null,
  updatedBy: null,
  loadUser: null,
  loadDate: null,
  appInformationSystemDbId: 70,
  appDevelopmentId: 90,
  appResponsibleAuthorizedId: 91,
  incomplete: false,
  missingResponsibleTypes: false,
  missingAuthorized: false,
  missingDevelopmentFields: false,
  missingSystems: false,
  missingDatabases: false,
  missingAccessibilityFields: false,
  missingSecurityData: true,
};

describe('ApplicationDetail', () => {
  let fixture: ComponentFixture<ApplicationDetail>;
  let component: ApplicationDetail;
  let breadcrumbs: {
    setCustomBreadcrumbs: ReturnType<typeof vi.fn>;
    clear: ReturnType<typeof vi.fn>;
  };

  beforeEach(async () => {
    breadcrumbs = {
      setCustomBreadcrumbs: vi.fn(),
      clear: vi.fn(),
    };
    const resolved: ApplicationDetailResolvedData = {
      application: APPLICATION_OUTPUT,
      loadFailed: false,
      hasUnverifiedWebContexts: false,
      hasPendingResponsibleDir3: false,
    };
    const paramMap = convertToParamMap({ id: '7' });

    await TestBed.configureTestingModule({
      imports: [ApplicationDetail],
      providers: [{ provide: AdministrativeUnitsService, useValue: { getPage: vi.fn() } },
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: {
            paramMap: of(paramMap),
            data: of({ [APPLICATION_DETAIL_RESOLVE_KEY]: resolved }),
            snapshot: {
              paramMap,
              data: { [APPLICATION_DETAIL_RESOLVE_KEY]: resolved },
            },
          },
        },
        { provide: BreadcrumbService, useValue: breadcrumbs },
        {
          provide: ApplicationOptionsService,
          useValue: { getAdministrativeUnitOptions: vi.fn(() => of([])) },
        },
        {
          provide: ApplicationsService,
          useValue: {
            toApplication,
            update: vi.fn(),
            refreshById: vi.fn(() => of(APPLICATION_OUTPUT)),
            reactivate: vi.fn(),
            delete: vi.fn(),
          },
        },
        {
          provide: ApplicationDevelopmentService,
          useValue: { create: vi.fn(), update: vi.fn() },
        },
        {
          provide: ApplicationSecurityService,
          useValue: { create: vi.fn(), update: vi.fn() },
        },
        {
          provide: ApplicationEnsClassificationsService,
          useValue: { create: vi.fn(), update: vi.fn() },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ApplicationDetail);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  function recreateWithNavigation(info?: unknown): ApplicationDetailState {
    fixture.destroy();
    const navigation = vi.spyOn(TestBed.inject(Router), 'currentNavigation');
    navigation.mockReturnValue({ extras: { info } } as Navigation);
    fixture = TestBed.createComponent(ApplicationDetail);
    component = fixture.componentInstance;
    // NavigationEnd may have happened before the initialization effect executes.
    navigation.mockReturnValue(null);
    fixture.detectChanges();
    navigation.mockRestore();
    return fixture.debugElement.injector.get(ApplicationDetailState);
  }

  it('starts General editing once after loading and respects cancellation', () => {
    const state = recreateWithNavigation(APPLICATION_GENERAL_EDIT_NAVIGATION);
    expect(state.isEditing('general')).toBe(true);
    expect(state.isEditing('development')).toBe(false);
    expect(state.form.pristine).toBe(true);
    state.cancelEditing('general');
    state.application.update((application) => application ? { ...application, name: 'Refreshed' } : application);
    fixture.detectChanges();
    expect(state.isEditing('general')).toBe(false);
  });

  it('opens a fresh load in consultation after an edit navigation', () => {
    expect(recreateWithNavigation(APPLICATION_GENERAL_EDIT_NAVIGATION).isEditing('general')).toBe(true);
    expect(recreateWithNavigation().isEditing('general')).toBe(false);
  });

  it('returns to consultation after saving an initial edit without reopening it', async () => {
    const state = recreateWithNavigation(APPLICATION_GENERAL_EDIT_NAVIGATION);
    const response = { ...APPLICATION_OUTPUT, name: 'Saved name' };
    vi.mocked(TestBed.inject(ApplicationsService).update).mockReturnValue(of(response));
    vi.mocked(TestBed.inject(ApplicationsService).refreshById).mockReturnValue(of(response));
    state.form.patchValue({ application: response.name, commission: 1 });
    state.form.markAsDirty();
    expect(await firstValueFrom(state.save('general'))).toEqual({ status: 'saved', section: 'general' });
    fixture.detectChanges();
    expect(state.isEditing('general')).toBe(false);
    expect(state.form.pristine).toBe(true);
    expect(state.form.controls.application.value).toBe('Saved name');
  });

  it('does not edit an inactive application or a failed detail load', () => {
    const resolved = TestBed.inject(ActivatedRoute).snapshot.data[APPLICATION_DETAIL_RESOLVE_KEY] as ApplicationDetailResolvedData;
    resolved.application = { ...APPLICATION_OUTPUT, status: ApplicationStatusCode.INACTIVE };
    expect(recreateWithNavigation(APPLICATION_GENERAL_EDIT_NAVIGATION).isEditing('general')).toBe(false);
    resolved.application = null;
    resolved.loadFailed = true;
    expect(recreateWithNavigation(APPLICATION_GENERAL_EDIT_NAVIGATION).isEditing('general')).toBe(false);
  });

  it('ignores navigation intents for other sections', () => {
    expect(recreateWithNavigation({ mode: 'edit', section: 'security' }).isEditing('general')).toBe(false);
  });

  it('renders the six tabs without a global edit toolbar', () => {
    const tabs = [
      ...fixture.nativeElement.querySelectorAll('.application-detail-tab'),
    ] as HTMLAnchorElement[];

    expect(tabs.map((tab) => tab.textContent?.trim())).toEqual([
      'General',
      'Responsables',
      'Sistemes i BD',
      'Desenvolupament',
      'Accessibilitat',
      'SeguretatLa secció Seguretat està incompleta: falta almenys un context web, una classificació ENS o un risc actiu.',
    ]);
    expect(fixture.nativeElement.querySelector('.application-detail-toolbar')).toBeNull();
  });

  it('renders one shared page heading above the detail tabs', () => {
    const root = fixture.nativeElement as HTMLElement;
    const headings = root.querySelectorAll('h1');
    expect(headings).toHaveLength(1);
    expect(headings[0].closest('app-section-container')).not.toBeNull();
    expect(root.querySelector('.application-detail-header')).toBeNull();
    expect(root.querySelector('.section-container__body .application-detail-tabs')).not.toBeNull();
  });

  it('marks incomplete responsible and development tabs with the established accessible icon', () => {
    const access = component as unknown as {
      detailState: {
        application: {
          update(update: (value: Application | null) => Application | null): void;
        };
      };
    };
    access.detailState.application.update((application) =>
      application
        ? {
            ...application,
            missingResponsibleTypes: true,
            missingAuthorized: true,
            missingDevelopmentFields: true,
          }
        : application,
    );
    fixture.detectChanges();
    const tabs = [
      ...fixture.nativeElement.querySelectorAll('.application-detail-tab'),
    ] as HTMLAnchorElement[];

    expect(tabs[1].querySelector('.pi-exclamation-circle.text-red-500')).toBeTruthy();
    expect(tabs[1].textContent).toContain('no hi ha cap persona autoritzada');
    expect(tabs[3].querySelector('.pi-exclamation-circle.text-red-500')).toBeTruthy();
    expect(tabs[3].textContent).toContain('falten camps obligatoris');
    expect(tabs[5].querySelector('.pi-exclamation-circle.text-red-500')).toBeTruthy();
    expect(tabs[0].querySelector('.pi-exclamation-circle')).toBeNull();
  });

  it('marks Security when contexts are pending, preserves other warnings and clears only the pending message', () => {
    const state = fixture.debugElement.injector.get(ApplicationDetailState);
    state.application.update((app) => app ? { ...app, missingSecurityData: false } : app);
    state.updateWebContextCount(21);
    fixture.detectChanges();
    const tab = [...(fixture.nativeElement as HTMLElement).querySelectorAll<HTMLAnchorElement>('.application-detail-tab')]
      .find((link) => link.textContent?.trim().startsWith('Seguretat'))!;
    expect(tab.querySelector('.pi-exclamation-circle.text-red-500')).not.toBeNull();
    expect(tab.textContent).toContain('contextos web pendents de verificar');
    state.application.update((app) => app ? { ...app, missingSecurityData: true } : app);
    fixture.detectChanges();
    expect(tab.textContent).toContain('falta almenys un context web');
    expect(tab.textContent).toContain('contextos web pendents de verificar');
    expect(tab.querySelectorAll('.pi-exclamation-circle')).toHaveLength(1);
    state.updateWebContextCount(0);
    fixture.detectChanges();
    expect(tab.textContent).not.toContain('contextos web pendents de verificar');
    expect(tab.querySelector('.pi-exclamation-circle')).not.toBeNull();
    state.application.update((app) => app ? { ...app, missingSecurityData: false } : app);
    fixture.detectChanges();
    expect(tab.querySelector('.pi-exclamation-circle')).toBeNull();
  });

  it('adds a single red Responsible tab warning for pending DIR3 and preserves other missing information', () => {
    const state = fixture.debugElement.injector.get(ApplicationDetailState);
    const tab = (fixture.nativeElement as HTMLElement).querySelectorAll('.application-detail-tab')[1];
    expect(tab.querySelector('.pi-exclamation-circle')).toBeNull();
    state.hasPendingResponsibleDir3.set(true);
    fixture.detectChanges();
    expect(tab.querySelectorAll('.pi-exclamation-circle.text-red-500')).toHaveLength(1);
    expect(tab.querySelector('.sr-only')?.textContent).toContain('DIR3 pendent de validar');
    expect(tab.textContent).not.toContain('falten tipus');
    state.application.update((app) => app ? { ...app, missingResponsibleTypes: true, missingAuthorized: true } : app);
    fixture.detectChanges();
    expect(tab.querySelectorAll('.pi-exclamation-circle')).toHaveLength(1);
    expect(tab.textContent).toContain('DIR3 pendent de validar');
    expect(tab.textContent).toContain('no hi ha cap persona autoritzada');
    state.hasPendingResponsibleDir3.set(false);
    fixture.detectChanges();
    expect(tab.textContent).not.toContain('DIR3 pendent de validar');
    expect(tab.querySelector('.pi-exclamation-circle')).not.toBeNull();
    state.application.update((app) => app ? { ...app, missingResponsibleTypes: false, missingAuthorized: false } : app);
    fixture.detectChanges();
    expect(tab.querySelector('.pi-exclamation-circle')).toBeNull();
  });

  it('offers an accessible retry while preserving the section content', () => {
    const state = fixture.debugElement.injector.get(ApplicationDetailState);
    const retry = vi.spyOn(state, 'refreshCompletenessAfterMutation').mockImplementation(() => {});
    state.completenessRefreshFailed.set(true);
    fixture.detectChanges();

    const status = fixture.nativeElement.querySelector('[role="status"]') as HTMLElement;
    const button = status.parentElement!.querySelector('button') as HTMLButtonElement;
    expect(status.textContent).toContain("Es mantenen els últims valors");
    expect(button.textContent).toContain('Torna a carregar els indicadors');
    expect(button.disabled).toBe(false);
    button.focus();
    expect(document.activeElement).toBe(button);
    button.click();
    expect(retry).toHaveBeenCalledOnce();
    expect(document.activeElement?.closest('.application-detail-tabs')).toBeTruthy();

    state.completenessRefreshing.set(true);
    fixture.detectChanges();
    expect(button.disabled).toBe(true);
    expect(status.parentElement!.getAttribute('aria-busy')).toBe('true');
    expect(fixture.nativeElement.querySelector('router-outlet')).toBeTruthy();
  });

  it('initializes the application and breadcrumbs', () => {
    const access = component as unknown as {
      detailState: {
        application: () => Application | null;
      };
    };

    expect(access.detailState.application()?.name).toBe('Inventari');
    expect(breadcrumbs.setCustomBreadcrumbs).toHaveBeenCalled();
  });

  it('allows leaving when there are no dirty tabs', () => {
    expect(component.canDeactivate()).toBe(true);
  });

  it('blocks leaving and lists the dirty tabs', () => {
    const access = component as unknown as {
      detailState: {
        form: {
          controls: { description: { setValue: (value: string) => void } };
          markAsDirty: () => void;
        };
        isUnsavedChangesDialogVisible: () => boolean;
      };
    };
    access.detailState.form.controls.description.setValue('Changed');
    access.detailState.form.markAsDirty();

    expect(component.canDeactivate()).toBe(false);
    expect(access.detailState.isUnsavedChangesDialogVisible()).toBe(true);

    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('General');
  });

  it('requests the native browser warning only for dirty tabs', () => {
    const event = {
      preventDefault: vi.fn(),
      returnValue: undefined,
    } as unknown as BeforeUnloadEvent;
    const access = component as unknown as {
      onBeforeUnload: (event: BeforeUnloadEvent) => void;
      detailState: {
        developmentForm: { markAsDirty: () => void };
      };
    };

    access.onBeforeUnload(event);
    expect(event.preventDefault).not.toHaveBeenCalled();

    access.detailState.developmentForm.markAsDirty();
    access.onBeforeUnload(event);
    expect(event.preventDefault).toHaveBeenCalled();
    expect(event.returnValue).toBe('');
  });

  it('clears breadcrumbs on destroy', () => {
    fixture.destroy();
    expect(breadcrumbs.clear).toHaveBeenCalled();
  });
});

function toApplication(response: ApplicationOutput): Application {
  return {
    id: String(response.id),
    code: response.code ?? '',
    prefix: response.prefix ?? '',
    name: response.name ?? '',
    category: response.category?.name ?? '',
    informationSystem: response.systemType?.name ?? '',
    scope: response.field?.name ?? '',
    commission: '',
    department: response.department?.name ?? '',
    administrativeUnit: response.admUnit?.name ?? '',
    status: response.status === ApplicationStatusCode.INACTIVE ? ApplicationStatus.INACTIVE : ApplicationStatus.ACTIVE,
    description: response.description ?? '',
    creationDate: response.createdAt ?? '',
    modificationDate: response.updatedAt ?? '',
    withdrawalDate: response.expirationDate ?? '',
    categoryId: response.category?.id,
    informationSystemId: response.systemType?.id,
    scopeId: response.field?.id,
    admUnitCode: response.admUnit?.code,
    departmentCode: response.department?.code,
    statusId: response.status === ApplicationStatusCode.INACTIVE ? ApplicationStatus.INACTIVE : ApplicationStatus.ACTIVE,
    appResponsibleAuthorizedId: response.appResponsibleAuthorizedId,
    appSecurityId: response.appSecurityId ?? null,
    appAccessibilityId: response.appAccessibilityId ?? null,
    incomplete: response.incomplete,
    missingResponsibleTypes: response.missingResponsibleTypes,
    missingAuthorized: response.missingAuthorized,
    missingDevelopmentFields: response.missingDevelopmentFields,
    missingSystems: response.missingSystems,
    missingDatabases: response.missingDatabases,
    missingAccessibilityFields: response.missingAccessibilityFields,
    missingSecurityData: response.missingSecurityData,
  };
}
