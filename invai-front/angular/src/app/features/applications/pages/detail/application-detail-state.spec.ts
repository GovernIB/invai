import { ApplicationAccessibilityService } from '../../services/application-accessibility.service';
import { ApplicationAccessibilityInput, ApplicationAccessibilityOutput } from '../../applications.model';
import { TestBed } from '@angular/core/testing';
import { CommissionType } from '@features/commissions/commissions.model';
import { ResponsibleDataChangesService } from '@features/maintenances/responsibles/services/responsible-data-changes.service';
import { firstValueFrom, of, Subject, throwError } from 'rxjs';

import {
  Application,
  ApplicationEnsClassificationOutput,
  ApplicationEnsClassificationInput,
  ApplicationDevelopmentOutput,
  ApplicationOutput,
  ApplicationStatus,
  ApplicationStatusCode,
  ApplicationSecurityOutput,
  ApplicationSystemDatabaseOutput,
  DevelopmentModality,
  DevelopmentStandardAdaption,
} from '../../applications.model';
import { ApplicationDevelopmentService } from '../../services/application-development.service';
import { ApplicationSystemDatabaseService } from '../../services/application-system-database.service';
import {
  ApplicationEnsClassificationsService,
  ApplicationSecurityService,
} from '../../services/application-security.service';
import { ApplicationsService } from '../../services/applications.service';
import { ApplicationOptionsService } from '../../services/application-options.service';
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
  let createClassification: ReturnType<typeof vi.fn>;
  let updateClassification: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    accessibility.create.mockReset().mockImplementation((input) => of(accessibilityFromInput(input)));
    accessibility.update.mockReset().mockImplementation((_id, input) => of(accessibilityFromInput(input)));

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
      providers: [
        ApplicationDetailState,
        { provide: ApplicationAccessibilityService, useValue: accessibility },
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
        {
          provide: ApplicationEnsClassificationsService,
          useValue: { create: createClassification, update: updateClassification },
        },
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

    await expect(firstValueFrom(state.activate())).resolves.toBe(true);
    await expect(firstValueFrom(state.withdraw())).resolves.toBe(true);

    expect(reactivate).toHaveBeenCalledWith(1);
    expect(deleteApplication).toHaveBeenCalledWith(1);
  });

  function initializeAll(response = APPLICATION_OUTPUT): void {
    state.initialize(response);
    state.initializeAdministrativeUnitOptions(
      [{ label: 'Direcció General', value: 'DGEDOT' }],
      false,
    );
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

function accessibilityFromInput(input: ApplicationAccessibilityInput): ApplicationAccessibilityOutput {
  const resource = (id: number | null) => id == null ? null : { id, name: 'Catàleg', nameEs: 'Catálogo', deletedAt: null };
  return { id: 99, application: { id: input.applicationId }, compliance: resource(input.complianceId),
    classificationSegment: resource(input.classificationSegmentId), publicUrl: input.publicUrl,
    mobileApplication: input.mobileApplication, mobileApplicationName: input.mobileApplicationName,
    expireDate: input.expireDate, observations: input.observations, nonAccessibleContent: input.nonAccessibleContent, deletedAt: null };
}
