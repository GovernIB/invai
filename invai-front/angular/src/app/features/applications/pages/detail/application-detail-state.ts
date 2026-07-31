import { Injectable, computed, inject, signal } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { Commission } from '@features/commissions/commissions.model';
import { PaginatedList } from '@models/table.model';
import { Observable, map, of, tap } from 'rxjs';

import {
  APPLICATION_CONSELLERIA_MOCK_VALUE,
  APPLICATION_STATUS_ACTIVE_ID,
} from '../../applications.constants';
import {
  ApplicationDevelopmentFormGroup,
  createApplicationDevelopmentForm,
  formatLocalDateTime,
  parseLocalDateTime,
} from '../../forms/application-development-form.factory';
import {
  ApplicationDetailFormValue,
  createApplicationDetailForm,
  createApplicationSystemsDatabasesForm,
} from '../../forms/application-form.factory';
import {
  Application,
  ApplicationDevelopmentInput,
  ApplicationDevelopmentOutput,
  ApplicationInput,
  ApplicationOutput,
  ApplicationProviderOutput,
  ApplicationStatus,
  ApplicationSystemDatabaseOutput,
  ApplicationTechnologyOutput,
} from '../../applications.model';
import { ApplicationDevelopmentService } from '../../services/application-development.service';
import { ApplicationCommissionOption } from '../../services/application-options.service';
import { ApplicationSystemDatabaseService } from '../../services/application-system-database.service';
import { ApplicationsService } from '../../services/applications.service';

export type ApplicationGeneralFormValue = ApplicationDetailFormValue;
export type ApplicationDetailSection =
  | 'general'
  | 'responsible'
  | 'systems-databases'
  | 'development';

export type ApplicationDetailSaveResult =
  | { status: 'saved'; section: ApplicationDetailSection }
  | { status: 'mocked'; section: ApplicationDetailSection }
  | { status: 'unchanged'; section: ApplicationDetailSection }
  | { status: 'invalid'; section: ApplicationDetailSection };

type ApplicationDevelopmentFormValue = ReturnType<ApplicationDevelopmentFormGroup['getRawValue']>;
type ApplicationSystemsDatabasesFormValue = ReturnType<
  ReturnType<typeof createApplicationSystemsDatabasesForm>['getRawValue']
>;

const EMPTY_GENERAL_FORM_VALUE: ApplicationDetailFormValue = {
  application: '',
  category: null,
  informationSystem: null,
  scope: null,
  administrativeUnit: null,
  conselleria: APPLICATION_CONSELLERIA_MOCK_VALUE,
  creationDate: '',
  modificationDate: '',
  withdrawalDate: '',
  commission: null,
  commissionExpedientNumber: '',
  commissionApprovalDate: '',
  commissionType: null,
  prefix: '',
  description: '',
};

const EMPTY_DEVELOPMENT_FORM_VALUE: ApplicationDevelopmentFormValue = {
  environment: null,
  modality: null,
  code: '',
  standardAdaption: null,
  revisionDate: null,
  observation: '',
};

const EMPTY_SYSTEMS_DATABASES_VALUE: ApplicationSystemsDatabasesFormValue = {
  observations: '',
};
const EMPTY_RESOURCES_PAGE = { items: [], total: 0 };
const DETAIL_SECTIONS: ApplicationDetailSection[] = [
  'general',
  'responsible',
  'systems-databases',
  'development',
];

function createEditingState(): Record<ApplicationDetailSection, boolean> {
  return {
    general: false,
    responsible: false,
    'systems-databases': false,
    development: false,
  };
}

@Injectable()
export class ApplicationDetailState {
  private readonly applicationsService = inject(ApplicationsService);
  private readonly developmentService = inject(ApplicationDevelopmentService);
  private readonly systemDatabaseService = inject(ApplicationSystemDatabaseService);
  private readonly formBuilder = inject(FormBuilder);
  private initializedApplicationId: number | null = null;
  private developmentApplicationId: number | null = null;
  private developmentInitialized = false;
  private savedGeneralValue: ApplicationDetailFormValue = EMPTY_GENERAL_FORM_VALUE;
  private savedSystemsDatabasesValue: ApplicationSystemsDatabasesFormValue =
    EMPTY_SYSTEMS_DATABASES_VALUE;
  private savedDevelopmentValue: ApplicationDevelopmentFormValue =
    EMPTY_DEVELOPMENT_FORM_VALUE;
  private readonly editingState = signal(createEditingState());

  readonly application = signal<Application | null>(null);
  readonly informationSystemDbId = signal<number | null>(null);
  readonly systemDatabase = signal<ApplicationSystemDatabaseOutput | null>(null);
  readonly development = signal<ApplicationDevelopmentOutput | null>(null);
  readonly providers = signal<PaginatedList<ApplicationProviderOutput>>(EMPTY_RESOURCES_PAGE);
  readonly technologies =
    signal<PaginatedList<ApplicationTechnologyOutput>>(EMPTY_RESOURCES_PAGE);
  readonly isUnsavedChangesDialogVisible = signal(false);
  readonly canEdit = computed(() => {
    const application = this.application();
    return !!application && application.status !== ApplicationStatus.INACTIVE;
  });

  readonly form = createApplicationDetailForm(this.formBuilder);
  readonly developmentForm = createApplicationDevelopmentForm(this.formBuilder);
  readonly systemsDatabasesForm = createApplicationSystemsDatabasesForm(this.formBuilder);

  constructor() {
    this.disableAllForms();
  }

  initialize(response: ApplicationOutput | null): void {
    if (!response) {
      this.resetState();
      return;
    }

    if (this.initializedApplicationId === response.id) return;

    this.setApplication(response);
  }

  initializeDevelopment(
    applicationId: number | null,
    response: ApplicationDevelopmentOutput | null,
  ): void {
    if (
      this.developmentInitialized &&
      applicationId != null &&
      applicationId === this.developmentApplicationId
    ) {
      return;
    }

    this.developmentApplicationId = applicationId;
    this.developmentInitialized = applicationId != null;
    this.setDevelopment(response);
  }

  initializeDevelopmentResources(
    providers: PaginatedList<ApplicationProviderOutput>,
    technologies: PaginatedList<ApplicationTechnologyOutput>,
  ): void {
    this.providers.set(this.clonePage(providers));
    this.technologies.set(this.clonePage(technologies));
  }

  initializeSystemsDatabases(response: ApplicationSystemDatabaseOutput | null): void {
    const expectedId = this.informationSystemDbId();
    const matchingResponse =
      response && (expectedId == null || response.id === expectedId) ? response : null;

    this.systemDatabase.set(matchingResponse);
    this.savedSystemsDatabasesValue = {
      observations: matchingResponse?.observation ?? '',
    };
    this.systemsDatabasesForm.reset(this.savedSystemsDatabasesValue, {
      emitEvent: false,
    });
    if (this.isEditing('systems-databases') && this.canEdit()) {
      this.systemsDatabasesForm.enable({ emitEvent: false });
    } else {
      this.systemsDatabasesForm.disable({ emitEvent: false });
    }
  }

  setProviderPage(page: PaginatedList<ApplicationProviderOutput>, _first?: number): void {
    this.providers.set(this.clonePage(page));
  }

  setTechnologyPage(page: PaginatedList<ApplicationTechnologyOutput>, _first?: number): void {
    this.technologies.set(this.clonePage(page));
  }

  isEditing(section: ApplicationDetailSection): boolean {
    return this.editingState()[section];
  }

  startEditing(section: ApplicationDetailSection): void {
    if (!this.canEdit()) return;

    switch (section) {
      case 'general':
        this.form.enable({ emitEvent: false });
        this.disableGeneralReadOnlyControls();
        break;
      case 'systems-databases':
        this.systemsDatabasesForm.enable({ emitEvent: false });
        break;
      case 'development':
        if (this.developmentInitialized) {
          this.developmentForm.enable({ emitEvent: false });
        }
        break;
      case 'responsible':
        break;
    }

    this.setEditing(section, true);
  }

  cancelEditing(section: ApplicationDetailSection): void {
    switch (section) {
      case 'general':
        this.form.reset(this.savedGeneralValue, { emitEvent: false });
        this.form.disable({ emitEvent: false });
        break;
      case 'systems-databases':
        this.systemsDatabasesForm.reset(this.savedSystemsDatabasesValue, {
          emitEvent: false,
        });
        this.systemsDatabasesForm.disable({ emitEvent: false });
        break;
      case 'development':
        this.developmentForm.reset(this.cloneDevelopmentValue(this.savedDevelopmentValue), {
          emitEvent: false,
        });
        this.developmentForm.disable({ emitEvent: false });
        break;
      case 'responsible':
        break;
    }

    this.setEditing(section, false);
  }

  save(section: ApplicationDetailSection): Observable<ApplicationDetailSaveResult> {
    switch (section) {
      case 'general':
        return this.saveGeneral();
      case 'responsible':
        return this.saveResponsible();
      case 'systems-databases':
        return this.saveSystemsDatabases();
      case 'development':
        return this.saveDevelopment();
    }
  }

  selectCommission(commission: ApplicationCommissionOption | null): void {
    this.form.patchValue(
      {
        commissionExpedientNumber: commission?.expedientNumber ?? '',
        commissionApprovalDate: commission?.approvalDate ?? '',
        commissionType: commission?.commissionType ?? null,
      },
      { emitEvent: false },
    );
  }

  dirtySections(): ApplicationDetailSection[] {
    return DETAIL_SECTIONS.filter((section) => this.isDirty(section));
  }

  hasDirtySections(): boolean {
    return this.dirtySections().length > 0;
  }

  showUnsavedChangesDialog(): void {
    this.isUnsavedChangesDialogVisible.set(true);
  }

  hideUnsavedChangesDialog(): void {
    this.isUnsavedChangesDialogVisible.set(false);
  }

  isDirty(section: ApplicationDetailSection): boolean {
    switch (section) {
      case 'general':
        return this.form.dirty;
      case 'systems-databases':
        return this.systemsDatabasesForm.dirty;
      case 'development':
        return this.developmentForm.dirty;
      case 'responsible':
        return false;
    }
  }

  activate(): Observable<boolean> {
    const current = this.application();
    const id = Number(current?.id);
    if (!current || Number.isNaN(id)) return of(false);

    return this.applicationsService.reactivate(id).pipe(
      tap((response) => this.setApplication(response)),
      map(() => true),
    );
  }

  withdraw(): Observable<boolean> {
    const current = this.application();
    const id = Number(current?.id);
    if (!current || Number.isNaN(id)) return of(false);

    return this.applicationsService.delete(id).pipe(map(() => true));
  }

  private saveGeneral(): Observable<ApplicationDetailSaveResult> {
    const section: ApplicationDetailSection = 'general';
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return of({ status: 'invalid', section });
    }
    if (this.form.pristine) {
      this.finishUnchanged(section);
      return of({ status: 'unchanged', section });
    }

    const current = this.application();
    const id = Number(current?.id);
    if (!current || Number.isNaN(id)) return of({ status: 'invalid', section });

    return this.applicationsService.update(id, this.toApplicationInput(current)).pipe(
      tap((response) => this.applyGeneralResponse(response)),
      map(() => ({ status: 'saved', section }) as const),
    );
  }

  private saveResponsible(): Observable<ApplicationDetailSaveResult> {
    const section: ApplicationDetailSection = 'responsible';
    this.setEditing(section, false);
    return of({ status: 'mocked', section });
  }

  private saveSystemsDatabases(): Observable<ApplicationDetailSaveResult> {
    const section: ApplicationDetailSection = 'systems-databases';
    if (this.systemsDatabasesForm.pristine) {
      this.finishUnchanged(section);
      return of({ status: 'unchanged', section });
    }

    const applicationId = Number(this.application()?.id);
    const informationSystemDbId = this.informationSystemDbId();
    if (!Number.isInteger(applicationId) || applicationId <= 0) {
      return of({ status: 'invalid', section });
    }

    const observations = this.systemsDatabasesForm.getRawValue().observations;
    const payload = {
      applicationId,
      observation: observations || null,
    };
    const request =
      informationSystemDbId == null
        ? this.systemDatabaseService.create(payload)
        : this.systemDatabaseService.update(informationSystemDbId, payload);

    return request.pipe(
      tap((response) => {
        this.informationSystemDbId.set(response.id);
        this.systemDatabase.set(response);
        this.savedSystemsDatabasesValue = {
          observations: response.observation ?? '',
        };
        this.systemsDatabasesForm.reset(this.savedSystemsDatabasesValue, {
          emitEvent: false,
        });
        this.systemsDatabasesForm.disable({ emitEvent: false });
        this.setEditing(section, false);
      }),
      map(() => ({ status: 'saved', section }) as const),
    );
  }

  private saveDevelopment(): Observable<ApplicationDetailSaveResult> {
    const section: ApplicationDetailSection = 'development';
    if (this.developmentForm.invalid) {
      this.developmentForm.markAllAsTouched();
      return of({ status: 'invalid', section });
    }
    if (this.developmentForm.pristine) {
      this.finishUnchanged(section);
      return of({ status: 'unchanged', section });
    }

    const applicationId = Number(this.application()?.id);
    if (!Number.isInteger(applicationId) || applicationId <= 0) {
      return of({ status: 'invalid', section });
    }

    const payload = this.toDevelopmentInput(applicationId);
    const current = this.development();
    const request = current
      ? this.developmentService.update(current.id, payload)
      : this.developmentService.create(payload);

    return request.pipe(
      tap((response) => {
        this.setEditing(section, false);
        this.setDevelopment(response);
      }),
      map(() => ({ status: 'saved', section }) as const),
    );
  }

  private finishUnchanged(section: ApplicationDetailSection): void {
    switch (section) {
      case 'general':
        this.form.disable({ emitEvent: false });
        break;
      case 'systems-databases':
        this.systemsDatabasesForm.disable({ emitEvent: false });
        break;
      case 'development':
        this.developmentForm.disable({ emitEvent: false });
        break;
      case 'responsible':
        break;
    }
    this.setEditing(section, false);
  }

  private resetState(): void {
    this.initializedApplicationId = null;
    this.application.set(null);
    this.informationSystemDbId.set(null);
    this.systemDatabase.set(null);
    this.savedGeneralValue = { ...EMPTY_GENERAL_FORM_VALUE };
    this.savedSystemsDatabasesValue = { ...EMPTY_SYSTEMS_DATABASES_VALUE };
    this.resetDevelopmentState();
    this.form.reset(this.savedGeneralValue, { emitEvent: false });
    this.systemsDatabasesForm.reset(this.savedSystemsDatabasesValue, {
      emitEvent: false,
    });
    this.disableAllForms();
    this.editingState.set(createEditingState());
  }

  private setApplication(response: ApplicationOutput): void {
    this.initializedApplicationId = response.id;
    this.application.set(this.applicationsService.toApplication(response));
    this.informationSystemDbId.set(response.appInformationSystemDbId);
    this.systemDatabase.set(null);
    this.savedGeneralValue = this.toGeneralFormValue(response);
    this.savedSystemsDatabasesValue = { ...EMPTY_SYSTEMS_DATABASES_VALUE };
    this.resetDevelopmentState();
    this.form.reset(this.savedGeneralValue, { emitEvent: false });
    this.systemsDatabasesForm.reset(this.savedSystemsDatabasesValue, {
      emitEvent: false,
    });
    this.disableAllForms();
    this.editingState.set(createEditingState());
  }

  private applyGeneralResponse(response: ApplicationOutput): void {
    this.initializedApplicationId = response.id;
    this.application.set(this.applicationsService.toApplication(response));
    this.informationSystemDbId.set(
      response.appInformationSystemDbId ?? this.informationSystemDbId(),
    );
    this.savedGeneralValue = this.toGeneralFormValue(response);
    this.form.reset(this.savedGeneralValue, { emitEvent: false });
    this.form.disable({ emitEvent: false });
    this.setEditing('general', false);
  }

  private setDevelopment(response: ApplicationDevelopmentOutput | null): void {
    this.development.set(response);
    this.savedDevelopmentValue = response
      ? {
          environment: response.environment?.id ?? null,
          modality: response.modality?.id ?? null,
          code: response.code ?? '',
          standardAdaption: response.standardAdaption?.id ?? null,
          revisionDate: parseLocalDateTime(response.revisionDate),
          observation: response.observation ?? '',
        }
      : this.cloneDevelopmentValue(EMPTY_DEVELOPMENT_FORM_VALUE);
    this.developmentForm.reset(this.cloneDevelopmentValue(this.savedDevelopmentValue), {
      emitEvent: false,
    });

    if (this.isEditing('development') && this.canEdit()) {
      this.developmentForm.enable({ emitEvent: false });
    } else {
      this.developmentForm.disable({ emitEvent: false });
    }
  }

  private resetDevelopmentState(): void {
    this.development.set(null);
    this.developmentApplicationId = null;
    this.developmentInitialized = false;
    this.savedDevelopmentValue = this.cloneDevelopmentValue(EMPTY_DEVELOPMENT_FORM_VALUE);
    this.developmentForm.reset(this.cloneDevelopmentValue(EMPTY_DEVELOPMENT_FORM_VALUE), {
      emitEvent: false,
    });
    this.providers.set(EMPTY_RESOURCES_PAGE);
    this.technologies.set(EMPTY_RESOURCES_PAGE);
  }

  private disableAllForms(): void {
    this.form.disable({ emitEvent: false });
    this.systemsDatabasesForm.disable({ emitEvent: false });
    this.developmentForm.disable({ emitEvent: false });
  }

  private disableGeneralReadOnlyControls(): void {
    this.form.controls.commissionExpedientNumber.disable({ emitEvent: false });
    this.form.controls.commissionApprovalDate.disable({ emitEvent: false });
    this.form.controls.commissionType.disable({ emitEvent: false });
    this.form.controls.creationDate.disable({ emitEvent: false });
    this.form.controls.modificationDate.disable({ emitEvent: false });
    this.form.controls.withdrawalDate.disable({ emitEvent: false });
  }

  private setEditing(section: ApplicationDetailSection, isEditing: boolean): void {
    this.editingState.update((state) => ({ ...state, [section]: isEditing }));
  }

  private toGeneralFormValue(response: ApplicationOutput): ApplicationDetailFormValue {
    const application = this.applicationsService.toApplication(response);
    return this.toFormValue(application, response.csCommission);
  }

  private toFormValue(
    application: Application,
    commission: Commission | null,
  ): ApplicationDetailFormValue {
    return {
      application: application.name,
      category: application.categoryId ?? null,
      informationSystem: application.informationSystemId ?? null,
      scope: application.scopeId ?? null,
      administrativeUnit: application.administrativeUnitId ?? null,
      conselleria: APPLICATION_CONSELLERIA_MOCK_VALUE,
      creationDate: application.creationDate,
      modificationDate: application.modificationDate,
      withdrawalDate: application.withdrawalDate,
      commission: application.commissionId ?? null,
      commissionExpedientNumber: commission?.expedientNumber ?? '',
      commissionApprovalDate: commission?.approvalDate ?? '',
      commissionType: commission?.commissionType ?? null,
      prefix: application.prefix,
      description: application.description,
    };
  }

  private toApplicationInput(current: Application): ApplicationInput {
    const value = this.form.getRawValue();
    return {
      name: value.application,
      prefix: value.prefix,
      code: current.code,
      categoryId: value.category as number,
      systemTypeId: value.informationSystem as number,
      fieldId: value.scope as number,
      admUnitId: value.administrativeUnit as number,
      commissionId: value.commission as number,
      description: value.description || null,
      statusId: current.statusId ?? current.status ?? APPLICATION_STATUS_ACTIVE_ID,
    };
  }

  private toDevelopmentInput(applicationId: number): ApplicationDevelopmentInput {
    const value = this.developmentForm.getRawValue();
    return {
      applicationId,
      environmentId: value.environment as number,
      modalityId: value.modality!,
      code: value.code.trim(),
      standardAdaptionId: value.standardAdaption!,
      revisionDate: formatLocalDateTime(value.revisionDate) as string,
      observation: value.observation,
    };
  }

  private cloneDevelopmentValue(
    value: ApplicationDevelopmentFormValue,
  ): ApplicationDevelopmentFormValue {
    return {
      ...value,
      revisionDate: value.revisionDate ? new Date(value.revisionDate) : null,
    };
  }

  private clonePage<T>(page: PaginatedList<T>): PaginatedList<T> {
    return { items: [...page.items], total: page.total };
  }
}
