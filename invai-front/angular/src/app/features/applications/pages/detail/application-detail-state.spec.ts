import { TestBed } from '@angular/core/testing';
import { AdministrativeUnitsService } from '@features/administrative-units/services/administrative-units.service';
import { CommissionType } from '@features/commissions/commissions.model';
import { ResponsibleDataChangesService } from '@features/maintenances/responsibles/services/responsible-data-changes.service';
import { firstValueFrom, of, Subject, throwError } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import {
  ApplicationAccessibilityInput,
  ApplicationAccessibilityOutput,
  ApplicationDataInput,
  ApplicationDataOutput,
} from '../../applications.model';
import { ApplicationAccessibilityService } from '../../services/application-accessibility.service';
import { ApplicationDataService } from '../../services/application-data.service';
import { ApplicationIntegrationService } from '../../services/application-integration.service';

import {
  Application,
  ApplicationDevelopmentOutput,
  ApplicationEnsClassificationInput,
  ApplicationEnsClassificationOutput,
  ApplicationOutput,
  ApplicationSecurityOutput,
  ApplicationStatus,
  ApplicationStatusCode,
  ApplicationSystemDatabaseOutput,
  DevelopmentModality,
  DevelopmentStandardAdaption,
} from '../../applications.model';
import { ApplicationDevelopmentService } from '../../services/application-development.service';
import { ApplicationOptionsService } from '../../services/application-options.service';
import {
  ApplicationEnsClassificationsService,
  ApplicationSecurityService,
  ApplicationSoffidRolesAssignmentService,
  ApplicationWebContextsService,
} from '../../services/application-security.service';
import { ApplicationSystemDatabaseService } from '../../services/application-system-database.service';
import { ApplicationsService } from '../../services/applications.service';
import { ApplicationDetailState } from './application-detail-state';

const APPLICATION_OUTPUT: ApplicationOutput = {
  id: 1,
  code: '0001',
  prefix: 'CVF',
  name: 'Invai',
  category: { id: 1, name: 'DRASSANA', deletedAt: null },
  systemType: { id: 2, name: 'Instrumental', deletedAt: null },
  field: { id: 3, name: 'Departamental', deletedAt: null },
  admUnit: { code: 'DGEDOT', name: 'Direcció General', parentCode: 'GVA01', level: 2 },
  department: { code: 'GVA01', name: 'Conselleria', parentCode: null, level: 1 },
  csCommission: {
    id: 4,
    name: 'Equip directiu',
    nameEs: 'Equipo directivo',
    expedientNumber: 'EXP-4',
    approvalDate: '2026-07-14',
    commissionType: CommissionType.SUPERIOR,
    deletedAt: null,
  },
  description: 'Aplicació interna',
  status: ApplicationStatusCode.ACTIVE,
  expirationDate: null,
  createdAt: '2026-01-01T10:00:00',
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
  missingSecurityData: false,
  missingIntegrationData: false,
};

const SYSTEM_DATABASE_OUTPUT: ApplicationSystemDatabaseOutput = {
  id: 70,
  application: APPLICATION_OUTPUT,
  observation: '<p>Observació inicial</p>',
  deletedAt: null,
};

const DEVELOPMENT_OUTPUT: ApplicationDevelopmentOutput = {
  id: 9,
  application: APPLICATION_OUTPUT,
  environment: {
    id: 3,
    code: 'PRO',
    name: 'Producció',
    nameEs: 'Producción',
  },
  modality: {
    id: DevelopmentModality.INTERNAL,
    name: 'Desenvolupament Intern',
    nameEs: 'Desarrollo Interno',
  },
  code: 'https://git.caib.es/invai',
  standardAdaption: {
    id: DevelopmentStandardAdaption.CONFORMING,
    name: 'Conforme',
    nameEs: 'Conforme',
  },
  revisionDate: '2026-05-02T00:00:00',
  observation: '<p>Observació</p>',
  deletedAt: null,
};

describe('ApplicationDetailState', () => {
  let state: ApplicationDetailState;
  const accessibility = { create: vi.fn(), update: vi.fn() };
  const dataService = {
    create: vi.fn(),
    update: vi.fn(),
    refreshById: vi.fn(),
    updateUrl: vi.fn(),
    checkUrl: vi.fn(),
  };
  const integrationService = { create: vi.fn(), update: vi.fn() };
  let updateApplication: ReturnType<typeof vi.fn>;
  let createDevelopment: ReturnType<typeof vi.fn>;
  let updateDevelopment: ReturnType<typeof vi.fn>;
  let createSystemDatabase: ReturnType<typeof vi.fn>;
  let updateSystemDatabase: ReturnType<typeof vi.fn>;
  let reactivate: ReturnType<typeof vi.fn>;
  let deleteApplication: ReturnType<typeof vi.fn>;
  let refreshById: ReturnType<typeof vi.fn>;
  let createSecurity: ReturnType<typeof vi.fn>;
  let updateSecurity: ReturnType<typeof vi.fn>;
  let hasUnverified: ReturnType<typeof vi.fn>;
  let createClassification: ReturnType<typeof vi.fn>;
  let updateClassification: ReturnType<typeof vi.fn>;
  let assignRoles: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    assignRoles = vi.fn(() => of([]));
    hasUnverified = vi.fn(() => of(false));
    accessibility.create.mockReset().mockImplementation((input) => of(accessibilityFromInput(input)));
    accessibility.update.mockReset().mockImplementation((_id, input) => of(accessibilityFromInput(input)));
    dataService.create.mockReset().mockImplementation((input) => of(dataFromInput(12, input)));
    dataService.update.mockReset().mockImplementation((id, input) => of(dataFromInput(id, input)));
    dataService.refreshById.mockReset().mockImplementation((id) => of({ ...DATA_RECORD, id }));
    dataService.updateUrl
      .mockReset()
      .mockImplementation((_id, _source, url) => of({ url, validUrl: true }));
    // The open data URL of DATA_RECORD answers; its reuse URL does not.
    dataService.checkUrl
      .mockReset()
      .mockImplementation((url: string) => of({ url, validUrl: url === DATA_RECORD.openDataUrl }));
    integrationService.create
      .mockReset()
      .mockImplementation((input) => of({ id: 14, ...input, deletedAt: null }));
    integrationService.update
      .mockReset()
      .mockImplementation((id, input) => of({ id, ...input, deletedAt: null }));

    updateApplication = vi.fn((_id, payload) =>
      of({
        ...APPLICATION_OUTPUT,
        name: payload.name,
        prefix: payload.prefix,
        description: payload.description,
      }),
    );
    createDevelopment = vi.fn(() => of(DEVELOPMENT_OUTPUT));
    updateDevelopment = vi.fn(() =>
      of({ ...DEVELOPMENT_OUTPUT, code: 'https://git.caib.es/invai-front' }),
    );
    createSystemDatabase = vi.fn((payload) =>
      of({
        ...SYSTEM_DATABASE_OUTPUT,
        id: 71,
        observation: payload.observation,
      }),
    );
    updateSystemDatabase = vi.fn((_id, payload) =>
      of({
        ...SYSTEM_DATABASE_OUTPUT,
        observation: payload.observation,
      }),
    );
    reactivate = vi.fn(() => of({ ...APPLICATION_OUTPUT, status: ApplicationStatusCode.ACTIVE }));
    deleteApplication = vi.fn(() => of(undefined));
    refreshById = vi.fn(() => of({ ...APPLICATION_OUTPUT, missingDevelopmentFields: false }));
    createSecurity = vi.fn((payload) =>
      of({
        id: 100,
        application: APPLICATION_OUTPUT,
        observation: payload.observation,
        deletedAt: null,
      } satisfies ApplicationSecurityOutput),
    );
    updateSecurity = vi.fn((_id, payload) =>
      of({
        id: 100,
        application: APPLICATION_OUTPUT,
        observation: payload.observation,
        deletedAt: null,
      } satisfies ApplicationSecurityOutput),
    );
    createClassification = vi.fn((payload) => of(classificationFromInput(payload)));
    updateClassification = vi.fn((id, payload) => of({ ...classificationFromInput(payload), id }));

    TestBed.configureTestingModule({
      providers: [{ provide: AdministrativeUnitsService, useValue: { getPage: vi.fn() } },
        ApplicationDetailState,
        { provide: ApplicationAccessibilityService, useValue: accessibility },
        { provide: ApplicationDataService, useValue: dataService },
        { provide: ApplicationIntegrationService, useValue: integrationService },
        {
          provide: ApplicationOptionsService,
          useValue: {
            getAdministrativeUnitOptions: vi.fn(() =>
              of([{ label: 'Direcció General', value: 'DGEDOT' }]),
            ),
          },
        },
        {
          provide: ApplicationsService,
          useValue: {
            update: updateApplication,
            reactivate,
            delete: deleteApplication,
            refreshById,
            toApplication,
          },
        },
        {
          provide: ApplicationDevelopmentService,
          useValue: {
            create: createDevelopment,
            update: updateDevelopment,
          },
        },
        {
          provide: ApplicationSystemDatabaseService,
          useValue: {
            create: createSystemDatabase,
            update: updateSystemDatabase,
          },
        },
        {
          provide: ApplicationSecurityService,
          useValue: { create: createSecurity, update: updateSecurity },
        },
        { provide: ApplicationWebContextsService, useValue: { hasUnverified } },
        {
          provide: ApplicationEnsClassificationsService,
          useValue: { create: createClassification, update: updateClassification },
        },
        { provide: ApplicationSoffidRolesAssignmentService, useValue: { assign: assignRoles } },
      ],
    });
    state = TestBed.inject(ApplicationDetailState);
  });

  it('initializes every section as read-only', () => {
    initializeAll();

    expect(state.informationSystemDbId()).toBe(70);
    expect(state.form.enabled).toBe(true);
    expect(state.systemsDatabasesForm.enabled).toBe(true);
    expect(state.developmentForm.enabled).toBe(true);
    expect(state.accessibilityForm.enabled).toBe(true);
    expect(state.isEditing('general')).toBe(false);
    expect(state.isEditing('systems-databases')).toBe(false);
    expect(state.isEditing('development')).toBe(false);
    expect(state.isEditing('accessibility')).toBe(false);
  });

  it('clears the pending DIR3 tab state on a different application or a failed detail load', () => {
    state.initialize(APPLICATION_OUTPUT);
    state.hasPendingResponsibleDir3.set(true);
    state.initialize({ ...APPLICATION_OUTPUT, id: 72 });
    expect(state.hasPendingResponsibleDir3()).toBeNull();
    state.hasPendingResponsibleDir3.set(true);
    state.initialize(null);
    expect(state.hasPendingResponsibleDir3()).toBeNull();
  });

  it('tracks pending contexts separately from backend completeness and resets on application change', () => {
    state.initialize(APPLICATION_OUTPUT);
    state.appSecurityId.set(8);
    const original = state.application()?.incomplete;
    hasUnverified.mockReturnValueOnce(of(true)).mockReturnValueOnce(of(false));
    state.refreshWebContextVerification();
    expect(state.hasUnverifiedWebContexts()).toBe(true);
    expect(state.application()?.incomplete).toBe(original);
    state.refreshWebContextVerification();
    expect(state.hasUnverifiedWebContexts()).toBe(false);
    state.initialize({ ...APPLICATION_OUTPUT, id: 72 });
    expect(state.hasUnverifiedWebContexts()).toBeNull();
    state.initialize(null);
    expect(state.hasUnverifiedWebContexts()).toBeNull();
  });

  it('ignores an older verification scan after a newer result', () => {
    state.initialize(APPLICATION_OUTPUT);
    state.appSecurityId.set(8);
    const older = new Subject<boolean>();
    hasUnverified.mockReturnValueOnce(older).mockReturnValueOnce(of(false));
    state.refreshWebContextVerification();
    state.refreshWebContextVerification();
    older.next(true);
    expect(state.hasUnverifiedWebContexts()).toBe(false);
  });

  it('initializes Accessibility empty until its data is resolved', () => {
    state.initialize(APPLICATION_OUTPUT);

    expect(state.accessibilityForm.getRawValue()).toEqual({
      classificationSegment: null,
      complianceStatus: null,
      reportExpirationDate: null,
      mobileApplication: null,
      mobileApplicationName: null,
      publicUrl: '',
      inaccessibleContent: '',
      observations: '',
    });
  });

  it('creates the security anchor on first save and then persists one ENS record', async () => {
    state.initialize({ ...APPLICATION_OUTPUT, appSecurityId: null });
    state.initializeSecurity(APPLICATION_OUTPUT.id, null, []);
    state.startEditing('security');
    state.securityForm.patchValue({
      confidentialityId: 1,
      integrityId: 1,
      traceabilityId: 1,
      availabilityId: 1,
      authenticityId: 1,
      observation: 'Observació de seguretat',
    });
    state.securityForm.markAsDirty();
    [
      state.securityForm.controls.confidentialityId,
      state.securityForm.controls.integrityId,
      state.securityForm.controls.traceabilityId,
      state.securityForm.controls.availabilityId,
      state.securityForm.controls.authenticityId,
      state.securityForm.controls.observation,
    ].forEach((control) => control.markAsDirty());

    await expect(firstValueFrom(state.save('security'))).resolves.toEqual({
      status: 'saved',
      section: 'security',
    });
    expect(createSecurity).toHaveBeenCalledWith({
      applicationId: APPLICATION_OUTPUT.id,
      observation: 'Observació de seguretat',
    });
    expect(createClassification).toHaveBeenCalledWith(
      expect.objectContaining({
        appSecurityId: 100,
        confidentialityId: 1,
        integrityId: 1,
        traceabilityId: 1,
        availabilityId: 1,
        authenticityId: 1,
      }),
    );
    expect(state.appSecurityId()).toBe(100);
    expect(state.ensClassificationLoadState()).toBe('ready');
    expect(state.securityForm.pristine).toBe(true);
    expect(state.isEditing('security')).toBe(false);
  });

  it('keeps the manually selected ENS grade independent from the security dimensions', async () => {
    const security: ApplicationSecurityOutput = {
      id: 100,
      application: APPLICATION_OUTPUT,
      observation: 'Visible',
      deletedAt: null,
    };
    const classification = classificationFromInput({
      appSecurityId: 100,
      identityProviderId: null,
      ensSubjectId: null,
      personalDataProcessingId: null,
      approvalDate: null,
      confidentialityId: 1,
      integrityId: 1,
      traceabilityId: 1,
      availabilityId: 1,
      authenticityId: 1,
      overallGradeId: 3,
    });

    state.initialize({ ...APPLICATION_OUTPUT, appSecurityId: 100 });
    state.initializeSecurity(APPLICATION_OUTPUT.id, security, [classification]);
    state.startEditing('security');

    const overallGrade = state.securityForm.controls.overallGradeId;
    const availability = state.securityForm.controls.availabilityId;
    expect(overallGrade.enabled).toBe(true);

    overallGrade.setValue(2);
    overallGrade.markAsDirty();
    availability.setValue(3);
    availability.markAsDirty();

    expect(overallGrade.value).toBe(2);

    await expect(firstValueFrom(state.save('security'))).resolves.toEqual({
      status: 'saved',
      section: 'security',
    });
    expect(updateClassification).toHaveBeenCalledWith(
      classification.id,
      expect.objectContaining({
        appSecurityId: 100,
        availabilityId: 3,
        overallGradeId: 2,
      }),
    );
    expect(updateSecurity).not.toHaveBeenCalled();
    expect(state.securityForm.controls.overallGradeId.value).toBe(2);
  });

  it('blocks only the ENS controls when duplicate classifications are resolved', () => {
    const security: ApplicationSecurityOutput = {
      id: 100,
      application: APPLICATION_OUTPUT,
      observation: 'Visible',
      deletedAt: null,
    };
    const classification = classificationFromInput({
      appSecurityId: 100,
      identityProviderId: null,
      ensSubjectId: null,
      personalDataProcessingId: null,
      approvalDate: null,
      confidentialityId: 1,
      integrityId: 1,
      traceabilityId: 1,
      availabilityId: 1,
      authenticityId: 1,
      overallGradeId: null,
    });

    state.initialize({ ...APPLICATION_OUTPUT, appSecurityId: 100 });
    state.initializeSecurity(APPLICATION_OUTPUT.id, security, [
      classification,
      { ...classification, id: 2 },
    ]);
    state.startEditing('security');

    expect(state.ensClassificationLoadState()).toBe('inconsistent');
    expect(state.securityForm.controls.confidentialityId.disabled).toBe(true);
    expect(state.securityForm.controls.observation.enabled).toBe(true);
  });

  it('blocks ENS while its load is unknown and reenables it after a successful reload', () => {
    state.initialize({ ...APPLICATION_OUTPUT, appSecurityId: 100 });
    state.startEditing('security');

    expect(state.securityForm.controls.confidentialityId.disabled).toBe(true);
    expect(state.securityForm.controls.observation.enabled).toBe(true);

    state.initializeSecurity(APPLICATION_OUTPUT.id, null, []);

    expect(state.ensClassificationLoadState()).toBe('ready');
    expect(state.securityForm.controls.confidentialityId.enabled).toBe(true);
  });

  it('disables and clears the mobile application name only when it is inapplicable', () => {
    state.initialize(APPLICATION_OUTPUT);
    state.initializeAccessibility({ applicationId: 1, appAccessibilityId: null, record: null, status: 'absent' });
    state.startEditing('accessibility');

    state.accessibilityForm.controls.mobileApplication.setValue(false);

    expect(state.accessibilityForm.controls.mobileApplicationName.value).toBeNull();
    expect(state.accessibilityForm.controls.mobileApplicationName.disabled).toBe(true);

    state.accessibilityForm.controls.mobileApplication.setValue(true);

    expect(state.accessibilityForm.controls.mobileApplicationName.enabled).toBe(true);
  });

  it('cancels Accessibility changes back to its saved local snapshot', () => {
    state.initialize(APPLICATION_OUTPUT);
    state.initializeAccessibility({ applicationId: 1, appAccessibilityId: null, record: null, status: 'absent' });
    state.startEditing('accessibility');
    state.accessibilityForm.controls.complianceStatus.setValue(3);
    state.accessibilityForm.controls.complianceStatus.markAsDirty();

    state.cancelEditing('accessibility');

    expect(state.accessibilityForm.controls.complianceStatus.value).toBeNull();
    expect(state.accessibilityForm.pristine).toBe(true);
    expect(state.isEditing('accessibility')).toBe(false);
  });

  it('creates Accessibility after confirmed absence and normalizes rich text values', async () => {
    state.initialize(APPLICATION_OUTPUT);
    state.initializeAccessibility({ applicationId: 1, appAccessibilityId: null, record: null, status: 'absent' });
    state.startEditing('accessibility');
    state.accessibilityForm.patchValue({
      publicUrl: '  https://git.caib.es/invai-accessible  ',
      inaccessibleContent: '<p><br></p>',
      observations: '<p>Revisat</p>',
    });
    state.accessibilityForm.markAsDirty();

    await expect(firstValueFrom(state.save('accessibility'))).resolves.toEqual({
      status: 'saved',
      section: 'accessibility',
    });

    expect(state.accessibilityForm.getRawValue()).toEqual(
      expect.objectContaining({
        publicUrl: 'https://git.caib.es/invai-accessible',
        inaccessibleContent: '',
        observations: '<p>Revisat</p>',
      }),
    );
    expect(state.accessibilityForm.pristine).toBe(true);
    expect(state.isEditing('accessibility')).toBe(false);
    expect(updateApplication).not.toHaveBeenCalled();
  });

  it('keeps Accessibility editing active when its optional URL has an invalid format', async () => {
    state.initialize(APPLICATION_OUTPUT);
    state.initializeAccessibility({ applicationId: 1, appAccessibilityId: null, record: null, status: 'absent' });
    state.startEditing('accessibility');
    state.accessibilityForm.controls.publicUrl.setValue('git.caib.es/invai');
    state.accessibilityForm.controls.publicUrl.markAsDirty();

    await expect(firstValueFrom(state.save('accessibility'))).resolves.toEqual({
      status: 'invalid',
      section: 'accessibility',
    });

    expect(state.accessibilityForm.controls.publicUrl.touched).toBe(true);
    expect(state.isEditing('accessibility')).toBe(true);
  });

  it('updates the loaded accessibility relation and keeps the snapshot on error', async () => {
    state.initialize(APPLICATION_OUTPUT);
    const record = accessibilityFromInput({ applicationId: 1, complianceId: 29, classificationSegmentId: 17,
      mobileApplication: null, mobileApplicationName: null, publicUrl: null, expireDate: '2026-02-12T09:30:00',
      observations: null, nonAccessibleContent: null });
    state.initializeAccessibility({ applicationId: 1, appAccessibilityId: 99, record, status: 'loaded' });
    state.startEditing('accessibility');
    state.accessibilityForm.controls.observations.setValue('Draft');
    state.accessibilityForm.markAsDirty();
    accessibility.update.mockReturnValueOnce(throwError(() => new Error('Unavailable')));
    await expect(firstValueFrom(state.save('accessibility'))).rejects.toThrow('Unavailable');
    expect(state.accessibilityForm.controls.observations.value).toBe('Draft');
    expect(state.isEditing('accessibility')).toBe(true);
    await firstValueFrom(state.save('accessibility'));
    expect(accessibility.create).not.toHaveBeenCalled();
    expect(accessibility.update).toHaveBeenLastCalledWith(99, expect.objectContaining({
      applicationId: 1, complianceId: 29, classificationSegmentId: 17, expireDate: '2026-02-12T00:00:00', mobileApplication: null,
    }));
    expect(state.appAccessibilityId()).toBe(99);
    expect(refreshById).toHaveBeenCalledWith(1);
    state.startEditing('accessibility');
    state.accessibilityForm.controls.observations.setValue('Discard');
    state.cancelEditing('accessibility');
    expect(state.accessibilityForm.controls.observations.value).toBe('Draft');
  });

  it.each(['failed', 'forbidden', 'unavailable', 'deleted'] as const)('prevents writes for %s accessibility', async (status) => {
    state.initialize(APPLICATION_OUTPUT);
    state.initializeAccessibility({ applicationId: 1, appAccessibilityId: null, record: null, status });
    state.startEditing('accessibility');
    state.accessibilityForm.markAsDirty();
    expect(state.isEditing('accessibility')).toBe(false);
    expect((await firstValueFrom(state.save('accessibility'))).status).toBe('invalid');
    expect(accessibility.create).not.toHaveBeenCalled();
    expect(accessibility.update).not.toHaveBeenCalled();
  });

  describe('data section', () => {
    function loadData(record: ApplicationDataOutput = DATA_RECORD): void {
      state.initialize(APPLICATION_OUTPUT);
      state.initializeData({
        applicationId: 1,
        appDataId: record.id,
        record,
        status: 'loaded',
        errorMessage: null,
      });
    }

    function loadAbsentData(): void {
      state.initialize(APPLICATION_OUTPUT);
      state.initializeData({
        applicationId: 1,
        appDataId: null,
        record: null,
        status: 'absent',
        errorMessage: null,
      });
    }

    it('starts every edit with no URL typed in by hand and the saved observation', () => {
      loadData();

      expect(state.dataForm.getRawValue()).toEqual({
        manualOpenDataUrl: false,
        openDataUrl: '',
        manualReuseUrl: false,
        reuseUrl: '',
        observations: '<p>Notes</p>',
      });
      expect(state.dataForm.pristine).toBe(true);
      expect(state.data()).toEqual(DATA_RECORD);
      expect(state.appDataId()).toBe(5);
      expect(state.application()?.appDataId).toBe(5);
      expect(state.canEditData()).toBe(true);
    });

    it('checks the URL of each source on its own once the anchor is read', () => {
      loadData();

      expect(dataService.checkUrl.mock.calls).toEqual([[DATA_RECORD.openDataUrl], [DATA_RECORD.reuseUrl]]);
      expect(state.dataUrls()).toEqual({
        openData: { url: DATA_RECORD.openDataUrl, check: 'valid' },
        reuse: { url: DATA_RECORD.reuseUrl, check: 'invalid' },
      });
    });

    it('shows each URL while it is checked and keeps one check failing apart from the other', () => {
      const openData = new Subject<{ url: string; validUrl: boolean }>();
      dataService.checkUrl.mockImplementation((url: string) =>
        url === DATA_RECORD.openDataUrl ? openData : throwError(() => new Error('Unavailable')),
      );

      loadData();
      expect(state.dataUrls()).toEqual({
        openData: { url: DATA_RECORD.openDataUrl, check: 'checking' },
        reuse: { url: DATA_RECORD.reuseUrl, check: 'failed' },
      });

      openData.next({ url: DATA_RECORD.openDataUrl!, validUrl: true });
      expect(state.dataUrls().openData).toEqual({ url: DATA_RECORD.openDataUrl, check: 'valid' });
    });

    it('ignores a check that ends after the URL of its source was saved again', async () => {
      const reuse = new Subject<{ url: string; validUrl: boolean }>();
      dataService.checkUrl.mockImplementation((url: string) =>
        url === DATA_RECORD.reuseUrl ? reuse : of({ url, validUrl: true }),
      );
      loadData();
      state.startEditing('data');
      state.dataForm.patchValue({ manualReuseUrl: true, reuseUrl: 'https://reuse.example/api' });
      state.dataForm.markAsDirty();

      await firstValueFrom(state.save('data'));
      reuse.next({ url: DATA_RECORD.reuseUrl!, validUrl: false });

      expect(state.dataUrls().reuse).toEqual({ url: 'https://reuse.example/api', check: 'valid' });
    });

    it('checks no URL while the application has no anchor', () => {
      loadAbsentData();

      expect(dataService.checkUrl).not.toHaveBeenCalled();
      expect(state.dataUrls()).toEqual({ openData: null, reuse: null });
    });

    it('requires a URL while it is typed in by hand and discards it when unchecked', () => {
      loadData();
      state.startEditing('data');
      const { manualOpenDataUrl, openDataUrl } = state.dataForm.controls;

      manualOpenDataUrl.setValue(true);
      expect(openDataUrl.hasError('required')).toBe(true);

      openDataUrl.setValue('https://manual.example/api');
      openDataUrl.markAsTouched();
      manualOpenDataUrl.setValue(false);

      expect(openDataUrl.value).toBe('');
      expect(openDataUrl.valid).toBe(true);
      expect(openDataUrl.touched).toBe(false);
    });

    it('stays in edit mode and sends nothing while a URL typed in by hand is missing', async () => {
      loadData();
      state.startEditing('data');
      state.dataForm.controls.manualReuseUrl.setValue(true);

      await expect(firstValueFrom(state.save('data'))).resolves.toEqual({
        status: 'invalid',
        section: 'data',
      });

      expect(state.dataForm.controls.reuseUrl.touched).toBe(true);
      expect(state.isEditing('data')).toBe(true);
      expect(dataService.update).not.toHaveBeenCalled();
      expect(dataService.updateUrl).not.toHaveBeenCalled();
    });

    it('finishes without requests when a checkbox was checked and unchecked again', async () => {
      loadData();
      state.startEditing('data');
      state.dataForm.controls.manualOpenDataUrl.setValue(true);
      state.dataForm.controls.manualOpenDataUrl.markAsDirty();
      state.dataForm.controls.manualOpenDataUrl.setValue(false);

      await expect(firstValueFrom(state.save('data'))).resolves.toEqual({
        status: 'unchanged',
        section: 'data',
      });

      expect(dataService.update).not.toHaveBeenCalled();
      expect(dataService.updateUrl).not.toHaveBeenCalled();
      expect(state.dataForm.pristine).toBe(true);
      expect(state.isEditing('data')).toBe(false);
    });

    it('saves only the observation and keeps the URLs already checked', async () => {
      loadData();
      state.startEditing('data');
      state.dataForm.controls.observations.setValue('<p><br></p>');
      state.dataForm.markAsDirty();

      await expect(firstValueFrom(state.save('data'))).resolves.toEqual({
        status: 'saved',
        section: 'data',
        invalidDataUrls: [],
      });

      expect(dataService.update).toHaveBeenCalledWith(5, {
        applicationId: 1,
        observation: null,
      } satisfies ApplicationDataInput);
      expect(dataService.updateUrl).not.toHaveBeenCalled();
      expect(dataService.refreshById).not.toHaveBeenCalled();
      expect(state.data()).toEqual({ ...DATA_RECORD, observation: null });
      expect(state.dataForm.controls.observations.value).toBe('');
      expect(state.isEditing('data')).toBe(false);
      expect(state.dataForm.pristine).toBe(true);
      expect(state.dataForm.enabled).toBe(true);
    });

    it('saves the URLs typed in by hand after the observation and reports those that do not answer', async () => {
      loadData();
      dataService.updateUrl.mockImplementation((_id, source, url) =>
        of({ url, validUrl: source === 'openData' }),
      );
      state.startEditing('data');
      state.dataForm.patchValue({
        manualOpenDataUrl: true,
        openDataUrl: '  https://open.example/api  ',
        manualReuseUrl: true,
        reuseUrl: 'https://reuse.example/api',
        observations: '<p>Noves</p>',
      });
      state.dataForm.markAsDirty();

      await expect(firstValueFrom(state.save('data'))).resolves.toEqual({
        status: 'saved',
        section: 'data',
        invalidDataUrls: ['reuse'],
      });

      expect(dataService.update).toHaveBeenCalledWith(5, { applicationId: 1, observation: '<p>Noves</p>' });
      expect(dataService.updateUrl.mock.calls).toEqual([
        [5, 'openData', 'https://open.example/api'],
        [5, 'reuse', 'https://reuse.example/api'],
      ]);
      expect(dataService.update.mock.invocationCallOrder[0]).toBeLessThan(
        dataService.updateUrl.mock.invocationCallOrder[0],
      );
      expect(dataService.refreshById).not.toHaveBeenCalled();
      expect(state.data()).toEqual({
        ...DATA_RECORD,
        observation: '<p>Noves</p>',
        openDataUrl: 'https://open.example/api',
        reuseUrl: 'https://reuse.example/api',
      });
      expect(state.dataUrls()).toEqual({
        openData: { url: 'https://open.example/api', check: 'valid' },
        reuse: { url: 'https://reuse.example/api', check: 'invalid' },
      });
      expect(state.dataForm.getRawValue()).toEqual({
        manualOpenDataUrl: false,
        openDataUrl: '',
        manualReuseUrl: false,
        reuseUrl: '',
        observations: '<p>Noves</p>',
      });
      expect(state.isEditing('data')).toBe(false);
    });

    it('sets a URL without touching an unchanged observation', async () => {
      loadData();
      state.startEditing('data');
      state.dataForm.patchValue({ manualReuseUrl: true, reuseUrl: 'https://reuse.example/api' });
      state.dataForm.markAsDirty();

      await firstValueFrom(state.save('data'));

      expect(dataService.update).not.toHaveBeenCalled();
      expect(dataService.updateUrl).toHaveBeenCalledExactlyOnceWith(5, 'reuse', 'https://reuse.example/api');
    });

    it('creates the anchor first and then reads the URLs it uses by default', async () => {
      loadAbsentData();
      state.startEditing('data');
      state.dataForm.patchValue({
        manualOpenDataUrl: true,
        openDataUrl: 'https://open.example/api',
        observations: '<p>Nova</p>',
      });
      state.dataForm.markAsDirty();

      await firstValueFrom(state.save('data'));

      expect(dataService.create).toHaveBeenCalledWith({ applicationId: 1, observation: '<p>Nova</p>' });
      expect(dataService.updateUrl).toHaveBeenCalledWith(12, 'openData', 'https://open.example/api');
      expect(dataService.refreshById).toHaveBeenCalledWith(12);
      expect(state.appDataId()).toBe(12);
      expect(state.application()?.appDataId).toBe(12);
      expect(state.dataStatus()).toBe('loaded');
      expect(state.data()).toEqual({ ...DATA_RECORD, id: 12 });
    });

    it.each([400, 504])('reports the save but keeps the tab in error when reading a new anchor fails with %s', async (status) => {
      loadAbsentData();
      const message = 'No se ha encontrado ningún anclaje de datos con el identificador especificado.';
      dataService.refreshById.mockReturnValueOnce(
        throwError(
          () => new HttpErrorResponse({ status, error: { error: 'Error', message } }),
        ),
      );
      state.startEditing('data');
      state.dataForm.controls.observations.setValue('<p>Nova</p>');
      state.dataForm.markAsDirty();

      await expect(firstValueFrom(state.save('data'))).resolves.toEqual({
        status: 'saved',
        section: 'data',
        invalidDataUrls: [],
      });

      expect(state.dataStatus()).toBe('failed');
      expect(state.dataErrorMessage()).toBe(message);
      expect(state.canEditData()).toBe(false);
    });

    it('keeps only the pending changes in edit mode when a later write fails', async () => {
      loadData();
      dataService.updateUrl.mockImplementation((_id, source, url) =>
        source === 'reuse'
          ? throwError(() => new Error('Unavailable'))
          : of({ url, validUrl: true }),
      );
      state.startEditing('data');
      state.dataForm.patchValue({
        manualOpenDataUrl: true,
        openDataUrl: 'https://open.example/api',
        manualReuseUrl: true,
        reuseUrl: 'https://reuse.example/api',
        observations: '<p>Noves</p>',
      });
      for (const control of Object.values(state.dataForm.controls)) control.markAsDirty();

      await expect(firstValueFrom(state.save('data'))).rejects.toThrow('Unavailable');

      expect(state.data()).toEqual({
        ...DATA_RECORD,
        observation: '<p>Noves</p>',
        openDataUrl: 'https://open.example/api',
      });
      const { observations, manualOpenDataUrl, openDataUrl, manualReuseUrl, reuseUrl } =
        state.dataForm.controls;
      expect([observations.pristine, manualOpenDataUrl.value, openDataUrl.value]).toEqual([
        true,
        false,
        '',
      ]);
      expect([manualReuseUrl.value, reuseUrl.value]).toEqual([true, 'https://reuse.example/api']);
      expect(state.dirtySections()).toContain('data');
      expect(state.isEditing('data')).toBe(true);
      expect(state.dataForm.enabled).toBe(true);

      dataService.updateUrl.mockImplementation((_id, _source, url) => of({ url, validUrl: true }));
      await firstValueFrom(state.save('data'));
      expect(dataService.update).toHaveBeenCalledOnce();
      expect(dataService.updateUrl).toHaveBeenLastCalledWith(5, 'reuse', 'https://reuse.example/api');
    });

    it('keeps the draft in edit mode when the observation update fails', async () => {
      loadData();
      dataService.update.mockReturnValueOnce(throwError(() => new Error('Unavailable')));
      state.startEditing('data');
      state.dataForm.controls.observations.setValue('Draft');
      state.dataForm.markAsDirty();

      await expect(firstValueFrom(state.save('data'))).rejects.toThrow('Unavailable');

      expect(state.dataForm.controls.observations.value).toBe('Draft');
      expect(state.dataForm.enabled).toBe(true);
      expect(state.isEditing('data')).toBe(true);
      expect(dataService.updateUrl).not.toHaveBeenCalled();
    });

    it('cancels data changes back to the saved snapshot and tracks them as dirty', () => {
      loadData();
      state.startEditing('data');
      state.dataForm.patchValue({
        manualOpenDataUrl: true,
        openDataUrl: 'https://open.example/api',
        observations: 'Draft',
      });
      state.dataForm.markAsDirty();

      expect(state.dirtySections()).toContain('data');
      state.cancelEditing('data');

      expect(state.dataForm.getRawValue()).toEqual({
        manualOpenDataUrl: false,
        openDataUrl: '',
        manualReuseUrl: false,
        reuseUrl: '',
        observations: '<p>Notes</p>',
      });
      expect(state.dataForm.pristine).toBe(true);
      expect(state.isEditing('data')).toBe(false);
    });

    it.each(['failed', 'forbidden', 'unavailable', 'deleted'] as const)(
      'prevents writes for %s data',
      async (status) => {
        state.initialize(APPLICATION_OUTPUT);
        state.initializeData({ applicationId: 1, appDataId: 5, record: null, status, errorMessage: null });
        state.startEditing('data');
        state.dataForm.markAsDirty();

        expect(state.isEditing('data')).toBe(false);
        expect((await firstValueFrom(state.save('data'))).status).toBe('invalid');
        expect(dataService.update).not.toHaveBeenCalled();
        expect(dataService.create).not.toHaveBeenCalled();
        expect(dataService.updateUrl).not.toHaveBeenCalled();
      },
    );

    it('ignores results of another application', () => {
      state.initialize(APPLICATION_OUTPUT);
      state.initializeData({
        applicationId: 2,
        appDataId: 5,
        record: DATA_RECORD,
        status: 'loaded',
        errorMessage: null,
      });

      expect(state.dataStatus()).toBe('unavailable');
      expect(state.appDataId()).toBeNull();
    });
  });

  it('enables and cancels sections independently', () => {
    initializeAll();
    state.startEditing('general');
    state.startEditing('development');
    state.form.controls.application.setValue('General draft');
    state.form.controls.application.markAsDirty();
    state.developmentForm.controls.code.setValue('https://git.caib.es/draft');
    state.developmentForm.controls.code.markAsDirty();

    state.cancelEditing('general');

    expect(state.form.controls.application.value).toBe('Invai');
    expect(state.form.enabled).toBe(true);
    expect(state.isEditing('general')).toBe(false);
    expect(state.developmentForm.controls.code.value).toBe('https://git.caib.es/draft');
    expect(state.developmentForm.enabled).toBe(true);
    expect(state.isEditing('development')).toBe(true);
  });

  it('saves General with exactly one application update and keeps other drafts', async () => {
    initializeAll();
    state.startEditing('general');
    state.startEditing('systems-databases');
    state.form.patchValue({
      application: 'Invai updated',
      prefix: 'INV',
      description: '<p>Updated <strong>description</strong></p>',
    });
    state.form.markAsDirty();
    state.systemsDatabasesForm.controls.observations.setValue('<p>Local</p>');
    state.systemsDatabasesForm.markAsDirty();
    refreshById.mockReturnValueOnce(
      of({
        ...APPLICATION_OUTPUT,
        name: 'Invai updated',
        description: '<p>Updated <strong>description</strong></p>',
      }),
    );

    await expect(firstValueFrom(state.save('general'))).resolves.toEqual({
      status: 'saved',
      section: 'general',
      assignedSoffidRoles: 0,
    });

    expect(updateApplication).toHaveBeenCalledTimes(1);
    expect(updateApplication).toHaveBeenCalledWith(1, {
      name: 'Invai updated',
      prefix: 'INV',
      code: '0001',
      categoryId: 1,
      systemTypeId: 2,
      fieldId: 3,
      admUnitCode: 'DGEDOT',
      commissionId: 4,
      description: '<p>Updated <strong>description</strong></p>',
      statusId: ApplicationStatus.ACTIVE,
    });
    expect(state.application()?.name).toBe('Invai updated');
    expect(state.isEditing('general')).toBe(false);
    expect(state.isEditing('systems-databases')).toBe(true);
    expect(state.systemsDatabasesForm.dirty).toBe(true);
  });

  it('saves visually empty General descriptions as an empty string', async () => {
    initializeAll();
    state.startEditing('general');
    state.form.controls.description.setValue('<p><br></p><p>&nbsp;</p>');
    state.form.controls.description.markAsDirty();

    await firstValueFrom(state.save('general'));

    expect(updateApplication).toHaveBeenCalledWith(1, expect.objectContaining({ description: '' }));
  });

  it('assigns the Soffid roles of a new prefix before re-reading the application', async () => {
    initializeAll();
    state.startEditing('general');
    state.form.controls.prefix.setValue('INV');
    state.form.controls.prefix.markAsDirty();
    assignRoles.mockReturnValueOnce(of([{ id: 5 }, { id: 6 }]));

    await expect(firstValueFrom(state.save('general'))).resolves.toEqual({
      status: 'saved',
      section: 'general',
      assignedSoffidRoles: 2,
    });

    expect(assignRoles).toHaveBeenCalledExactlyOnceWith(1);
    expect(updateApplication.mock.invocationCallOrder[0]).toBeLessThan(
      assignRoles.mock.invocationCallOrder[0],
    );
    expect(assignRoles.mock.invocationCallOrder[0]).toBeLessThan(
      refreshById.mock.invocationCallOrder[0],
    );
    expect(state.isEditing('general')).toBe(false);
  });

  it('does not assign Soffid roles when General keeps the same prefix', async () => {
    initializeAll();
    state.startEditing('general');
    state.form.controls.application.setValue('Invai updated');
    state.form.controls.application.markAsDirty();

    await expect(firstValueFrom(state.save('general'))).resolves.toEqual({
      status: 'saved',
      section: 'general',
      assignedSoffidRoles: undefined,
    });

    expect(assignRoles).not.toHaveBeenCalled();
  });

  it('still saves General with a new prefix when its Soffid roles cannot be assigned', async () => {
    initializeAll();
    state.startEditing('general');
    state.form.controls.prefix.setValue('INV');
    state.form.controls.prefix.markAsDirty();
    assignRoles.mockReturnValueOnce(
      throwError(() => new HttpErrorResponse({ status: 504, statusText: 'Gateway Timeout' })),
    );

    await expect(firstValueFrom(state.save('general'))).resolves.toEqual({
      status: 'saved',
      section: 'general',
      assignedSoffidRoles: null,
    });

    expect(refreshById).toHaveBeenCalledWith(1);
    expect(state.isEditing('general')).toBe(false);
    expect(state.form.pristine).toBe(true);
  });

  it('preserves authoritative completeness if the GET after a general update fails', async () => {
    refreshById.mockReturnValueOnce(throwError(() => new Error('Offline')));
    initializeAll({
      ...APPLICATION_OUTPUT,
      missingResponsibleTypes: true,
      missingAuthorized: true,
      missingDevelopmentFields: true,
    });
    state.startEditing('general');
    state.form.controls.description.setValue('Canvi');
    state.form.controls.description.markAsDirty();

    await expect(firstValueFrom(state.save('general'))).resolves.toEqual({
      status: 'saved', section: 'general',
    });
    expect(state.completenessRefreshFailed()).toBe(true);

    expect(state.application()).toEqual(
      expect.objectContaining({
        missingResponsibleTypes: true,
        missingAuthorized: true,
        missingDevelopmentFields: true,
      }),
    );
  });

  it('keeps backend security indicators independent of the editable ENS fields', () => {
    state.initialize({ ...APPLICATION_OUTPUT, incomplete: false, missingSecurityData: false });
    state.initializeSecurity(APPLICATION_OUTPUT.id, null, []);

    expect(state.ensClassificationLoadState()).toBe('ready');
    expect(state.application()?.missingSecurityData).toBe(false);
    expect(state.application()?.incomplete).toBe(false);

    state.securityForm.patchValue({
      confidentialityId: 1, integrityId: 1, traceabilityId: 1,
      availabilityId: 1, authenticityId: 1,
    });
    state.application.update((current) => current
      ? { ...current, incomplete: true, missingSecurityData: true } : current);

    expect(state.application()?.missingSecurityData).toBe(true);
    expect(state.application()?.incomplete).toBe(true);
  });

  it('keeps ENS unavailable after saving observations when classifications failed to load', async () => {
    state.initialize(APPLICATION_OUTPUT);
    state.initializeSecurity(APPLICATION_OUTPUT.id, null, [], true);
    state.startEditing('security');
    state.securityForm.controls.observation.setValue('Saved observation');
    state.securityForm.controls.observation.markAsDirty();

    await firstValueFrom(state.save('security'));

    expect(state.ensClassificationLoadState()).toBe('unknown');
    expect(state.securityForm.controls.confidentialityId.disabled).toBe(true);
    expect(createClassification).not.toHaveBeenCalled();
  });

  it('uses the authoritative GET after general save instead of mutation defaults or previous flags', async () => {
    initializeAll({ ...APPLICATION_OUTPUT, incomplete: true, missingDevelopmentFields: true });
    state.startEditing('general');
    state.form.controls.description.setValue('Saved description');
    state.form.controls.description.markAsDirty();
    refreshById.mockReturnValueOnce(of({
      ...APPLICATION_OUTPUT, description: 'Saved description', incomplete: true,
      missingSecurityData: true, missingDevelopmentFields: false,
    }));

    await firstValueFrom(state.save('general'));

    expect(refreshById).toHaveBeenCalledWith(APPLICATION_OUTPUT.id);
    expect(state.application()).toEqual(expect.objectContaining({
      incomplete: true, missingSecurityData: true, missingDevelopmentFields: false,
    }));
    expect(state.completenessRefreshFailed()).toBe(false);
  });

  it('preserves the last snapshot and dirty forms after a failed refresh, then retries', () => {
    state.initialize({ ...APPLICATION_OUTPUT, incomplete: true, missingSystems: true });
    state.startEditing('general');
    state.form.controls.description.setValue('Unsaved draft');
    state.form.controls.description.markAsDirty();
    refreshById.mockReturnValueOnce(throwError(() => new Error('Offline')));

    state.refreshCompletenessAfterMutation();

    expect(state.completenessRefreshFailed()).toBe(true);
    expect(state.completenessRefreshing()).toBe(false);
    expect(state.application()?.missingSystems).toBe(true);
    expect(state.application()?.incomplete).toBe(true);

    state.refreshCompletenessAfterMutation();

    expect(state.application()?.missingSystems).toBe(false);
    expect(state.application()?.incomplete).toBe(false);
    expect(state.completenessRefreshFailed()).toBe(false);
    expect(state.form.controls.description.value).toBe('Unsaved draft');
    expect(state.form.dirty).toBe(true);
    expect(state.isEditing('general')).toBe(true);
  });

  it.each(['success', 'error'] as const)('ignores an older refresh %s when a newer request finishes first', (result) => {
    const older = new Subject<ApplicationOutput>();
    const newer = new Subject<ApplicationOutput>();
    refreshById.mockReturnValueOnce(older).mockReturnValueOnce(newer);
    state.initialize(APPLICATION_OUTPUT);
    state.refreshCompletenessAfterMutation();
    state.refreshCompletenessAfterMutation();
    expect(state.completenessRefreshing()).toBe(true);

    newer.next({ ...APPLICATION_OUTPUT, incomplete: true, missingSecurityData: true });
    newer.complete();
    if (result === 'success') {
      older.next(APPLICATION_OUTPUT);
      older.complete();
    } else {
      older.error(new Error('Old failure'));
    }

    expect(state.application()?.incomplete).toBe(true);
    expect(state.application()?.missingSecurityData).toBe(true);
    expect(state.completenessRefreshFailed()).toBe(false);
    expect(state.completenessRefreshing()).toBe(false);
  });

  it('finishes general saving without overwriting newer indicators from a child mutation', async () => {
    const pending = new Subject<ApplicationOutput>();
    refreshById.mockReturnValueOnce(pending);
    initializeAll(APPLICATION_OUTPUT);
    state.startEditing('general');
    state.form.controls.description.setValue('Saved description');
    state.form.controls.description.markAsDirty();
    const saved = firstValueFrom(state.save('general'));

    refreshById.mockReturnValueOnce(of({
      ...APPLICATION_OUTPUT, incomplete: true, missingSecurityData: true,
    }));
    state.refreshCompletenessAfterMutation();
    pending.next(APPLICATION_OUTPUT);
    pending.complete();

    await expect(saved).resolves.toEqual({ status: 'saved', section: 'general' });
    expect(state.application()?.incomplete).toBe(true);
    expect(state.application()?.missingSecurityData).toBe(true);
    expect(state.form.controls.description.value).toBe('Saved description');
    expect(state.form.pristine).toBe(true);
    expect(state.completenessRefreshFailed()).toBe(false);
  });

  it('ignores a pending completeness refresh after changing applications', () => {
    const pending = new Subject<ApplicationOutput>();
    refreshById.mockReturnValueOnce(pending);
    state.initialize(APPLICATION_OUTPUT);
    state.refreshCompletenessAfterMutation();
    state.initialize({ ...APPLICATION_OUTPUT, id: 2, incomplete: true });

    pending.next(APPLICATION_OUTPUT);
    pending.complete();

    expect(state.application()?.id).toBe('2');
    expect(state.application()?.incomplete).toBe(true);
    expect(state.completenessRefreshing()).toBe(false);
  });

  it('marks an invalid General form and does not call the endpoint', async () => {
    state.initialize(APPLICATION_OUTPUT);
    state.startEditing('general');
    state.form.controls.application.setValue('');
    state.form.controls.application.markAsDirty();

    await expect(firstValueFrom(state.save('general'))).resolves.toEqual({
      status: 'invalid',
      section: 'general',
    });

    expect(updateApplication).not.toHaveBeenCalled();
    expect(state.form.controls.application.touched).toBe(true);
    expect(state.isEditing('general')).toBe(true);
  });

  it('persists Systems and Databases observations through its backend aggregate', async () => {
    state.initialize(APPLICATION_OUTPUT);
    state.initializeSystemsDatabases(SYSTEM_DATABASE_OUTPUT);
    state.startEditing('systems-databases');
    state.systemsDatabasesForm.controls.observations.setValue('<p>Local</p>');
    state.systemsDatabasesForm.markAsDirty();

    await expect(firstValueFrom(state.save('systems-databases'))).resolves.toEqual({
      status: 'saved',
      section: 'systems-databases',
    });

    expect(updateSystemDatabase).toHaveBeenCalledWith(70, {
      applicationId: 1,
      observation: '<p>Local</p>',
    });
    expect(createSystemDatabase).not.toHaveBeenCalled();
    expect(state.systemsDatabasesForm.enabled).toBe(true);
    expect(state.systemsDatabasesForm.pristine).toBe(true);
    expect(state.systemsDatabasesForm.controls.observations.value).toBe('<p>Local</p>');
    expect(updateApplication).not.toHaveBeenCalled();
  });

  it('saves visually empty Systems and Databases observations as an empty string', async () => {
    state.initialize(APPLICATION_OUTPUT);
    state.initializeSystemsDatabases(SYSTEM_DATABASE_OUTPUT);
    state.startEditing('systems-databases');
    state.systemsDatabasesForm.controls.observations.setValue('<p><br></p><p>&nbsp;</p>');
    state.systemsDatabasesForm.controls.observations.markAsDirty();

    await firstValueFrom(state.save('systems-databases'));

    expect(updateSystemDatabase).toHaveBeenCalledWith(70, {
      applicationId: 1,
      observation: '',
    });
  });

  it('keeps over-limit Systems and Databases observations unsaved in edit mode', async () => {
    state.initialize(APPLICATION_OUTPUT);
    state.initializeSystemsDatabases(SYSTEM_DATABASE_OUTPUT);
    state.startEditing('systems-databases');
    state.systemsDatabasesForm.controls.observations.setValue(`<p>${'a'.repeat(4000)}</p>`);
    state.systemsDatabasesForm.markAsDirty();

    await expect(firstValueFrom(state.save('systems-databases'))).resolves.toEqual({
      status: 'invalid',
      section: 'systems-databases',
    });

    expect(updateSystemDatabase).not.toHaveBeenCalled();
    expect(state.systemsDatabasesForm.controls.observations.touched).toBe(true);
    expect(state.isEditing('systems-databases')).toBe(true);
  });

  it('saves Systems and Databases observations with compacted Quill spaces', async () => {
    state.initialize(APPLICATION_OUTPUT);
    state.initializeSystemsDatabases(SYSTEM_DATABASE_OUTPUT);
    state.startEditing('systems-databases');
    state.systemsDatabasesForm.controls.observations.setValue(
      '<p>Servidor&nbsp;de&nbsp;proves</p>',
    );
    state.systemsDatabasesForm.markAsDirty();

    await firstValueFrom(state.save('systems-databases'));

    expect(updateSystemDatabase).toHaveBeenCalledWith(70, {
      applicationId: 1,
      observation: '<p>Servidor de proves</p>',
    });
  });

  it('creates Systems and Databases observations when no aggregate exists', async () => {
    state.initialize({ ...APPLICATION_OUTPUT, appInformationSystemDbId: null });
    state.initializeSystemsDatabases(null);
    state.startEditing('systems-databases');
    state.systemsDatabasesForm.controls.observations.setValue('<p>Inicials</p>');
    state.systemsDatabasesForm.markAsDirty();

    await expect(firstValueFrom(state.save('systems-databases'))).resolves.toEqual({
      status: 'saved',
      section: 'systems-databases',
    });

    expect(createSystemDatabase).toHaveBeenCalledWith({
      applicationId: 1,
      observation: '<p>Inicials</p>',
    });
    expect(updateSystemDatabase).not.toHaveBeenCalled();
    expect(state.informationSystemDbId()).toBe(71);
    expect(state.systemDatabase()?.id).toBe(71);
    expect(state.systemsDatabasesForm.controls.observations.value).toBe('<p>Inicials</p>');
    expect(state.systemsDatabasesForm.enabled).toBe(true);
    expect(state.systemsDatabasesForm.pristine).toBe(true);
  });

  it('completes the mocked Responsible flow independently', async () => {
    state.initialize(APPLICATION_OUTPUT);
    state.startEditing('responsible');

    await expect(firstValueFrom(state.save('responsible'))).resolves.toEqual({
      status: 'mocked',
      section: 'responsible',
    });

    expect(state.isEditing('responsible')).toBe(false);
  });

  describe('integrations section', () => {
    const RECORD = { id: 13, applicationId: 1, observation: '<p>Nota</p>', deletedAt: null };
    type IntegrationStatus = 'loaded' | 'absent' | 'failed' | 'forbidden' | 'deleted' | 'unavailable';

    function loadIntegration(
      status: IntegrationStatus = 'loaded',
      record: typeof RECORD | { deletedAt: string } | null = status === 'absent' ? null : RECORD,
    ): void {
      state.initialize(APPLICATION_OUTPUT);
      state.initializeIntegrations({
        applicationId: 1,
        appIntegrationId: status === 'absent' ? null : 13,
        record: record as typeof RECORD | null,
        status,
        errorMessage: null,
        connectionsPage: null,
        connectionsLoadFailed: false,
        connectionsErrorMessage: null,
      });
    }

    it('initializes the saved observations and the anchor ID from the loaded record', () => {
      loadIntegration();

      expect(state.integrationsForm.controls.observations.value).toBe('<p>Nota</p>');
      expect(state.integrationsForm.pristine).toBe(true);
      expect(state.appIntegrationId()).toBe(13);
      expect(state.application()?.appIntegrationId).toBe(13);
      expect(state.canEditIntegrations()).toBe(true);
    });

    it('updates the observations of an existing anchor and keeps the response as snapshot', async () => {
      loadIntegration();
      state.startEditing('integrations');
      state.integrationsForm.controls.observations.setValue('<p>Nova nota</p>');
      state.integrationsForm.markAsDirty();

      expect(state.dirtySections()).toEqual(['integrations']);
      await expect(firstValueFrom(state.save('integrations'))).resolves.toEqual({
        status: 'saved',
        section: 'integrations',
      });

      expect(integrationService.update).toHaveBeenCalledWith(13, {
        applicationId: 1,
        observation: '<p>Nova nota</p>',
      });
      expect(integrationService.create).not.toHaveBeenCalled();
      expect(state.isEditing('integrations')).toBe(false);
      expect(state.integrationsForm.pristine).toBe(true);
      expect(state.integrationsForm.enabled).toBe(true);

      state.startEditing('integrations');
      state.integrationsForm.controls.observations.setValue('<p>Esborrany</p>');
      state.cancelEditing('integrations');
      expect(state.integrationsForm.controls.observations.value).toBe('<p>Nova nota</p>');
    });

    it('creates the anchor when the application has none and records its ID', async () => {
      loadIntegration('absent');
      state.startEditing('integrations');
      state.integrationsForm.controls.observations.setValue('<p>Primera</p>');
      state.integrationsForm.markAsDirty();

      await expect(firstValueFrom(state.save('integrations'))).resolves.toEqual({
        status: 'saved',
        section: 'integrations',
      });

      expect(integrationService.create).toHaveBeenCalledWith({
        applicationId: 1,
        observation: '<p>Primera</p>',
      });
      expect(state.appIntegrationId()).toBe(14);
      expect(state.application()?.appIntegrationId).toBe(14);
      expect(state.integrationStatus()).toBe('loaded');
    });

    it('sends null observations when the editor is emptied', async () => {
      loadIntegration();
      state.startEditing('integrations');
      state.integrationsForm.controls.observations.setValue('<p><br></p>');
      state.integrationsForm.markAsDirty();

      await firstValueFrom(state.save('integrations'));

      expect(integrationService.update).toHaveBeenCalledWith(13, {
        applicationId: 1,
        observation: null,
      });
    });

    it('keeps the draft in edit mode when the update fails', async () => {
      loadIntegration();
      integrationService.update.mockReturnValueOnce(throwError(() => new Error('Unavailable')));
      state.startEditing('integrations');
      state.integrationsForm.controls.observations.setValue('<p>Esborrany</p>');
      state.integrationsForm.markAsDirty();

      await expect(firstValueFrom(state.save('integrations'))).rejects.toThrow('Unavailable');

      expect(state.isEditing('integrations')).toBe(true);
      expect(state.integrationsForm.controls.observations.value).toBe('<p>Esborrany</p>');
      expect(state.integrationsForm.enabled).toBe(true);
    });

    it('keeps over-limit observations in edit mode without saving them', async () => {
      loadIntegration();
      state.startEditing('integrations');
      state.integrationsForm.controls.observations.setValue(`<p>${'a'.repeat(4000)}</p>`);
      state.integrationsForm.markAsDirty();

      await expect(firstValueFrom(state.save('integrations'))).resolves.toEqual({
        status: 'invalid',
        section: 'integrations',
      });

      expect(integrationService.update).not.toHaveBeenCalled();
      expect(state.integrationsForm.controls.observations.touched).toBe(true);
      expect(state.isEditing('integrations')).toBe(true);
    });

    it('finishes an unchanged edit without saving', async () => {
      loadIntegration();
      state.startEditing('integrations');

      await expect(firstValueFrom(state.save('integrations'))).resolves.toEqual({
        status: 'unchanged',
        section: 'integrations',
      });
      expect(state.isEditing('integrations')).toBe(false);
      expect(integrationService.update).not.toHaveBeenCalled();
    });

    it.each(['failed', 'forbidden', 'unavailable', 'deleted'] as const)(
      'blocks edits while the anchor is %s',
      async (status) => {
        loadIntegration(status, status === 'deleted' ? { ...RECORD, deletedAt: '2026-09-01' } : null);

        state.startEditing('integrations');
        expect(state.isEditing('integrations')).toBe(false);
        expect((await firstValueFrom(state.save('integrations'))).status).toBe('invalid');
        expect(integrationService.update).not.toHaveBeenCalled();
      },
    );

    it('ignores results for another application and resets when it changes', () => {
      loadIntegration();
      state.initializeIntegrations({
        applicationId: 2,
        appIntegrationId: 99,
        record: { ...RECORD, id: 99, applicationId: 2 },
        status: 'loaded',
        errorMessage: null,
        connectionsPage: null,
        connectionsLoadFailed: false,
        connectionsErrorMessage: null,
      });
      expect(state.appIntegrationId()).toBe(13);

      state.startEditing('integrations');
      state.integrationsForm.controls.observations.setValue('<p>Esborrany</p>');
      state.integrationsForm.markAsDirty();
      state.initialize({ ...APPLICATION_OUTPUT, id: 2 });

      expect(state.integrationsForm.controls.observations.value).toBe('');
      expect(state.isEditing('integrations')).toBe(false);
      expect(state.integrationStatus()).toBe('unavailable');
    });
  });

  it('updates Development and refreshes application completeness', async () => {
    initializeAll();
    state.startEditing('development');
    state.developmentForm.controls.code.setValue('https://git.caib.es/invai-front');
    state.developmentForm.controls.code.markAsDirty();

    await expect(firstValueFrom(state.save('development'))).resolves.toEqual({
      status: 'saved',
      section: 'development',
    });

    expect(updateDevelopment).toHaveBeenCalledTimes(1);
    expect(updateDevelopment).toHaveBeenCalledWith(9, {
      applicationId: 1,
      environmentId: 3,
      modalityId: DevelopmentModality.INTERNAL,
      code: 'https://git.caib.es/invai-front',
      standardAdaptionId: DevelopmentStandardAdaption.CONFORMING,
      revisionDate: '2026-05-02T00:00:00',
      observation: '<p>Observació</p>',
    });
    expect(createDevelopment).not.toHaveBeenCalled();
    expect(updateApplication).not.toHaveBeenCalled();
    expect(state.developmentForm.enabled).toBe(true);
    expect(refreshById).toHaveBeenCalledWith(1);
  });

  it('refreshes completeness after assignment changes without resetting drafts', () => {
    refreshById.mockReturnValue(
      of({ ...APPLICATION_OUTPUT, missingAuthorized: false, missingResponsibleTypes: false }),
    );
    state.initialize({
      ...APPLICATION_OUTPUT,
      missingAuthorized: true,
      missingResponsibleTypes: true,
    });
    state.form.controls.description.setValue('Esborrany');
    state.form.controls.description.markAsDirty();

    TestBed.inject(ResponsibleDataChangesService).assignmentsChanged();

    expect(refreshById).toHaveBeenCalledWith(1);
    expect(state.application()?.missingAuthorized).toBe(false);
    expect(state.application()?.missingResponsibleTypes).toBe(false);
    expect(state.form.controls.description.value).toBe('Esborrany');
    expect(state.form.dirty).toBe(true);
  });

  it('saves visually empty Development observations as an empty string', async () => {
    initializeAll();
    state.startEditing('development');
    state.developmentForm.controls.observation.setValue('<p><br></p><p>&nbsp;</p>');
    state.developmentForm.controls.observation.markAsDirty();

    await expect(firstValueFrom(state.save('development'))).resolves.toEqual({
      status: 'saved',
      section: 'development',
    });

    expect(updateDevelopment).toHaveBeenCalledWith(9, {
      applicationId: 1,
      environmentId: 3,
      modalityId: DevelopmentModality.INTERNAL,
      code: 'https://git.caib.es/invai',
      standardAdaptionId: DevelopmentStandardAdaption.CONFORMING,
      revisionDate: '2026-05-02T00:00:00',
      observation: '',
    });
  });

  it('loads nullable Development observations as an empty editor value', () => {
    state.initialize(APPLICATION_OUTPUT);
    state.initializeDevelopment(1, { ...DEVELOPMENT_OUTPUT, observation: null });

    expect(state.developmentForm.controls.observation.value).toBe('');
  });

  it('creates Development when no active record exists', async () => {
    state.initialize(APPLICATION_OUTPUT);
    state.initializeDevelopment(1, null);
    state.startEditing('development');
    state.developmentForm.setValue({
      environment: 3,
      modality: DevelopmentModality.INTERNAL,
      code: 'https://git.caib.es/invai',
      standardAdaption: DevelopmentStandardAdaption.CONFORMING,
      revisionDate: new Date(2026, 4, 2),
      observation: '<p>Observació</p>',
    });
    state.developmentForm.markAsDirty();

    await firstValueFrom(state.save('development'));

    expect(createDevelopment).toHaveBeenCalledTimes(1);
    expect(updateDevelopment).not.toHaveBeenCalled();
  });

  it('does not call Development when its form is unchanged', async () => {
    initializeAll();
    state.startEditing('development');

    await expect(firstValueFrom(state.save('development'))).resolves.toEqual({
      status: 'unchanged',
      section: 'development',
    });

    expect(createDevelopment).not.toHaveBeenCalled();
    expect(updateDevelopment).not.toHaveBeenCalled();
    expect(state.isEditing('development')).toBe(false);
  });

  it('reports only tabs with actual unsaved changes', () => {
    initializeAll();
    state.startEditing('responsible');
    state.startEditing('general');
    state.startEditing('development');
    state.form.controls.description.setValue('Changed');
    state.form.controls.description.markAsDirty();
    state.developmentForm.controls.observation.setValue('<p>Changed</p>');
    state.developmentForm.controls.observation.markAsDirty();
    state.accessibilityForm.controls.observations.setValue('<p>Changed</p>');
    state.accessibilityForm.controls.observations.markAsDirty();

    expect(state.dirtySections()).toEqual(['general', 'development', 'accessibility']);
    expect(state.hasDirtySections()).toBe(true);
  });

  it('keeps inactive applications read-only and supports lifecycle endpoints', async () => {
    state.initialize({
      ...APPLICATION_OUTPUT,
      status: ApplicationStatusCode.INACTIVE,
    });

    state.startEditing('general');
    expect(state.isEditing('general')).toBe(false);
    expect(state.form.enabled).toBe(true);

    await expect(firstValueFrom(state.activate())).resolves.toEqual({
      activated: true,
      assignedSoffidRoles: 0,
    });
    await expect(firstValueFrom(state.withdraw())).resolves.toBe(true);

    expect(reactivate).toHaveBeenCalledWith(1);
    expect(deleteApplication).toHaveBeenCalledWith(1);
  });

  it('assigns the Soffid roles of a restored application before re-reading it', async () => {
    state.initialize({ ...APPLICATION_OUTPUT, status: ApplicationStatusCode.INACTIVE });
    assignRoles.mockReturnValueOnce(of([{ id: 5 }]));

    await expect(firstValueFrom(state.activate())).resolves.toEqual({
      activated: true,
      assignedSoffidRoles: 1,
    });

    expect(assignRoles).toHaveBeenCalledExactlyOnceWith(1);
    expect(reactivate.mock.invocationCallOrder[0]).toBeLessThan(
      assignRoles.mock.invocationCallOrder[0],
    );
    expect(assignRoles.mock.invocationCallOrder[0]).toBeLessThan(
      refreshById.mock.invocationCallOrder[0],
    );
    expect(state.application()?.status).toBe(ApplicationStatus.ACTIVE);
  });

  it('still restores the application when its Soffid roles cannot be assigned', async () => {
    state.initialize({ ...APPLICATION_OUTPUT, status: ApplicationStatusCode.INACTIVE });
    assignRoles.mockReturnValueOnce(
      throwError(() => new HttpErrorResponse({ status: 504, statusText: 'Gateway Timeout' })),
    );

    await expect(firstValueFrom(state.activate())).resolves.toEqual({
      activated: true,
      assignedSoffidRoles: null,
    });

    expect(state.application()?.status).toBe(ApplicationStatus.ACTIVE);
  });

  it('does not reactivate nor assign Soffid roles without a loaded application', async () => {
    await expect(firstValueFrom(state.activate())).resolves.toEqual({ activated: false });

    expect(reactivate).not.toHaveBeenCalled();
    expect(assignRoles).not.toHaveBeenCalled();
  });

  it('restores the saved DIR3 code and label when cancelling general edits', () => {
    initializeAll();
    state.startEditing('general');
    state.form.controls.administrativeUnit.setValue('NEW');
    state.dir3.selectSnapshot({ code: 'NEW', name: 'New department', parentCode: null, level: 1 });
    state.form.markAsDirty();
    state.cancelEditing('general');
    expect(state.form.controls.administrativeUnit.value).toBe('DGEDOT');
    expect(state.dir3.selectedLabel()).toBe('Direcció General (DGEDOT)');
    expect(state.form.controls.administrativeUnit.enabled).toBe(true);
  });

  function initializeAll(response = APPLICATION_OUTPUT): void {
    state.initialize(response);
    state.initializeDevelopment(1, DEVELOPMENT_OUTPUT);
  }
});

function toApplication(response: ApplicationOutput): Application {
  const status = response.status ? ApplicationStatus[response.status] : null;
  return {
    id: String(response.id),
    code: response.code ?? '',
    prefix: response.prefix ?? '',
    name: response.name ?? '',
    category: response.category?.name ?? '',
    informationSystem: response.systemType?.name ?? '',
    scope: response.field?.name ?? '',
    commission: response.csCommission?.name ?? '',
    department: response.department?.name ?? '',
    administrativeUnit: response.admUnit?.name ?? '',
    status,
    description: response.description ?? '',
    creationDate: response.createdAt ?? '',
    modificationDate: response.updatedAt ?? '',
    withdrawalDate: response.expirationDate ?? '',
    categoryId: response.category?.id,
    informationSystemId: response.systemType?.id,
    scopeId: response.field?.id,
    commissionId: response.csCommission?.id,
    admUnitCode: response.admUnit?.code,
    departmentCode: response.department?.code,
    statusId: status ?? undefined,
    informationSystemDbId: response.appInformationSystemDbId,
    appDevelopmentId: response.appDevelopmentId,
    appResponsibleAuthorizedId: response.appResponsibleAuthorizedId,
    dir3Mismatch: response.dir3Mismatch ?? false,
    incomplete: response.incomplete,
    missingResponsibleTypes: response.missingResponsibleTypes,
    missingAuthorized: response.missingAuthorized,
    missingDevelopmentFields: response.missingDevelopmentFields,
    missingSystems: response.missingSystems,
    missingDatabases: response.missingDatabases,
    missingAccessibilityFields: response.missingAccessibilityFields,
    missingSecurityData: response.missingSecurityData,
    missingIntegrationData: response.missingIntegrationData,
  };
}

function classificationFromInput(
  input: ApplicationEnsClassificationInput,
): ApplicationEnsClassificationOutput {
  const catalog = (id: number | null) => (id == null ? null : { id, name: `Nivell ${id}` });
  return {
    id: 1,
    appSecurity: { id: input.appSecurityId } as ApplicationEnsClassificationOutput['appSecurity'],
    identityProvider: catalog(input.identityProviderId),
    ensSubject: catalog(input.ensSubjectId),
    personalDataProcessing: catalog(input.personalDataProcessingId),
    approvalDate: input.approvalDate,
    confidentiality: catalog(input.confidentialityId),
    integrity: catalog(input.integrityId),
    traceability: catalog(input.traceabilityId),
    availability: catalog(input.availabilityId),
    authenticity: catalog(input.authenticityId),
    overallGrade: catalog(input.overallGradeId),
    deletedAt: null,
  };
}

const DATA_RECORD: ApplicationDataOutput = {
  id: 5,
  application: { id: 1 },
  observation: '<p>Notes</p>',
  openDataUrl: 'https://intranet.caib.es/0001api/externa',
  reuseUrl: 'https://intranet.caib.es/0001api/interna',
  deletedAt: null,
};

function dataFromInput(id: number, input: ApplicationDataInput): ApplicationDataOutput {
  return {
    id,
    application: { id: input.applicationId },
    observation: input.observation,
    openDataUrl: null,
    reuseUrl: null,
    deletedAt: null,
  };
}

function accessibilityFromInput(input: ApplicationAccessibilityInput): ApplicationAccessibilityOutput {
  const resource = (id: number | null) => id == null ? null : { id, name: 'Catàleg', nameEs: 'Catálogo', deletedAt: null };
  return { id: 99, application: { id: input.applicationId }, compliance: resource(input.complianceId),
    classificationSegment: resource(input.classificationSegmentId), publicUrl: input.publicUrl,
    mobileApplication: input.mobileApplication, mobileApplicationName: input.mobileApplicationName,
    expireDate: input.expireDate, observations: input.observations, nonAccessibleContent: input.nonAccessibleContent, deletedAt: null };
}
