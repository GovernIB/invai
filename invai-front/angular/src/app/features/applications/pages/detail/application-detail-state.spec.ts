import { TestBed } from '@angular/core/testing';
import { CommissionType } from '@features/commissions/commissions.model';
import { firstValueFrom, of } from 'rxjs';

import {
  Application,
  ApplicationDevelopmentOutput,
  ApplicationOutput,
  ApplicationStatus,
  ApplicationStatusCode,
  ApplicationSystemDatabaseOutput,
  DevelopmentModality,
  DevelopmentStandardAdaption,
} from '../../applications.model';
import { ApplicationDevelopmentService } from '../../services/application-development.service';
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
  admUnit: { id: 5, code: 'DGEDOT', name: 'Direcció General' },
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
  let updateApplication: ReturnType<typeof vi.fn>;
  let createDevelopment: ReturnType<typeof vi.fn>;
  let updateDevelopment: ReturnType<typeof vi.fn>;
  let createSystemDatabase: ReturnType<typeof vi.fn>;
  let updateSystemDatabase: ReturnType<typeof vi.fn>;
  let reactivate: ReturnType<typeof vi.fn>;
  let deleteApplication: ReturnType<typeof vi.fn>;

  beforeEach(() => {
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
    reactivate = vi.fn(() =>
      of({ ...APPLICATION_OUTPUT, status: ApplicationStatusCode.ACTIVE }),
    );
    deleteApplication = vi.fn(() => of(undefined));

    TestBed.configureTestingModule({
      providers: [
        ApplicationDetailState,
        {
          provide: ApplicationsService,
          useValue: {
            update: updateApplication,
            reactivate,
            delete: deleteApplication,
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
      ],
    });
    state = TestBed.inject(ApplicationDetailState);
  });

  it('initializes every section as read-only', () => {
    initializeAll();

    expect(state.informationSystemDbId()).toBe(70);
    expect(state.form.disabled).toBe(true);
    expect(state.systemsDatabasesForm.disabled).toBe(true);
    expect(state.developmentForm.disabled).toBe(true);
    expect(state.isEditing('general')).toBe(false);
    expect(state.isEditing('systems-databases')).toBe(false);
    expect(state.isEditing('development')).toBe(false);
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
    expect(state.form.disabled).toBe(true);
    expect(state.isEditing('general')).toBe(false);
    expect(state.developmentForm.controls.code.value).toBe(
      'https://git.caib.es/draft',
    );
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
      description: 'Updated',
    });
    state.form.markAsDirty();
    state.systemsDatabasesForm.controls.observations.setValue('<p>Local</p>');
    state.systemsDatabasesForm.markAsDirty();

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
      admUnitId: 5,
      commissionId: 4,
      description: 'Updated',
      statusId: ApplicationStatus.ACTIVE,
    });
    expect(state.application()?.name).toBe('Invai updated');
    expect(state.isEditing('general')).toBe(false);
    expect(state.isEditing('systems-databases')).toBe(true);
    expect(state.systemsDatabasesForm.dirty).toBe(true);
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

    await expect(
      firstValueFrom(state.save('systems-databases')),
    ).resolves.toEqual({
      status: 'saved',
      section: 'systems-databases',
    });

    expect(updateSystemDatabase).toHaveBeenCalledWith(70, {
      applicationId: 1,
      observation: '<p>Local</p>',
    });
    expect(createSystemDatabase).not.toHaveBeenCalled();
    expect(state.systemsDatabasesForm.disabled).toBe(true);
    expect(state.systemsDatabasesForm.pristine).toBe(true);
    expect(state.systemsDatabasesForm.controls.observations.value).toBe(
      '<p>Local</p>',
    );
    expect(updateApplication).not.toHaveBeenCalled();
  });

  it('creates Systems and Databases observations when no aggregate exists', async () => {
    state.initialize({ ...APPLICATION_OUTPUT, appInformationSystemDbId: null });
    state.initializeSystemsDatabases(null);
    state.startEditing('systems-databases');
    state.systemsDatabasesForm.controls.observations.setValue('<p>Inicials</p>');
    state.systemsDatabasesForm.markAsDirty();

    await expect(
      firstValueFrom(state.save('systems-databases')),
    ).resolves.toEqual({
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
    expect(state.systemsDatabasesForm.controls.observations.value).toBe(
      '<p>Inicials</p>',
    );
    expect(state.systemsDatabasesForm.disabled).toBe(true);
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

  it('updates Development with one request and no application request', async () => {
    initializeAll();
    state.startEditing('development');
    state.developmentForm.controls.code.setValue(
      'https://git.caib.es/invai-front',
    );
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
    expect(state.developmentForm.disabled).toBe(true);
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

    expect(state.dirtySections()).toEqual(['general', 'development']);
    expect(state.hasDirtySections()).toBe(true);
  });

  it('keeps inactive applications read-only and supports lifecycle endpoints', async () => {
    state.initialize({
      ...APPLICATION_OUTPUT,
      status: ApplicationStatusCode.INACTIVE,
    });

    state.startEditing('general');
    expect(state.isEditing('general')).toBe(false);
    expect(state.form.disabled).toBe(true);

    await expect(firstValueFrom(state.activate())).resolves.toBe(true);
    await expect(firstValueFrom(state.withdraw())).resolves.toBe(true);

    expect(reactivate).toHaveBeenCalledWith(1);
    expect(deleteApplication).toHaveBeenCalledWith(1);
  });

  function initializeAll(): void {
    state.initialize(APPLICATION_OUTPUT);
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
    administrativeUnitId: response.admUnit?.id,
    statusId: status ?? undefined,
    informationSystemDbId: response.appInformationSystemDbId,
    appDevelopmentId: response.appDevelopmentId,
  };
}
