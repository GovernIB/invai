import { DestroyRef, Injectable, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder } from '@angular/forms';
import { readApiErrorMessage } from '@core/models/api-error.model';
import { AdministrativeUnitSearchState } from '@features/administrative-units/administrative-unit-search.state';
import { Commission } from '@features/commissions/commissions.model';
import { ResponsibleDataChangesService } from '@features/maintenances/responsibles/services/responsible-data-changes.service';
import { PaginatedList } from '@models/table.model';
import { normalizeQuillHtml } from '@shared/utils/rich-text.utils';
import {
  EMPTY,
  Observable,
  Subject,
  catchError,
  defaultIfEmpty,
  defer,
  filter,
  finalize,
  map,
  of,
  switchMap,
  tap,
  throwError,
} from 'rxjs';
import { readApplicationCompleteness } from '../../application-completeness';
import { hasPendingResponsibleDir3 } from '../../application-dir3.utils';
import { ApplicationResponsibleOutput } from '../../applications.model';
import { ApplicationAccessibilityInput, ApplicationAccessibilityOutput } from '../../applications.model';
import { ApplicationAccessibilityService } from '../../services/application-accessibility.service';
import type { AccessibilityLoadStatus, ApplicationAccessibilityLoadResult } from './sections/accessibility/application-accessibility-section.resolver';
import { ApplicationDataInput, ApplicationDataOutput } from '../../applications.model';
import { ApplicationDataService } from '../../services/application-data.service';
import {
  ApplicationIntegrationInput,
  ApplicationIntegrationOutput,
} from '../../applications.model';
import { ApplicationIntegrationService } from '../../services/application-integration.service';
import type {
  ApplicationIntegrationLoadStatus,
  ApplicationIntegrationsLoadResult,
} from './sections/integrations/application-integrations-section.resolver';
import type {
  ApplicationDataLoadResult,
  ApplicationDataLoadStatus,
} from './sections/data/application-data-section.resolver';
import {
  APPLICATION_DATA_SOURCE_CONTROLS,
  ApplicationDataFormValue,
  ApplicationDataSource,
  EMPTY_APPLICATION_DATA_VALUE,
  createApplicationDataForm,
} from '../../forms/application-data-form.factory';

import { APPLICATION_STATUS_ACTIVE_ID } from '../../applications.constants';
import {
  Application,
  ApplicationDevelopmentInput,
  ApplicationDevelopmentOutput,
  ApplicationEnsClassificationInput,
  ApplicationEnsClassificationOutput,
  ApplicationInput,
  ApplicationOutput,
  ApplicationProviderOutput,
  ApplicationSecurityInput,
  ApplicationSecurityOutput,
  ApplicationStatus,
  ApplicationSystemDatabaseOutput,
  ApplicationTechnologyOutput,
  EnsClassificationLoadState
} from '../../applications.model';
import {
  ApplicationAccessibilityFormValue,
  EMPTY_APPLICATION_ACCESSIBILITY_VALUE,
  createApplicationAccessibilityForm,
} from '../../forms/application-accessibility-form.factory';
import {
  ApplicationDevelopmentFormGroup,
  createApplicationDevelopmentForm,
  formatLocalDateTime,
  parseLocalDateTime,
} from '../../forms/application-development-form.factory';
import {
  ApplicationDetailFormValue,
  createApplicationDetailForm,
  createApplicationIntegrationsForm,
  createApplicationSystemsDatabasesForm,
} from '../../forms/application-form.factory';
import {
  ApplicationSecurityFormGroup,
  createApplicationSecurityForm,
} from '../../forms/application-security-form.factory';
import { ApplicationDevelopmentService } from '../../services/application-development.service';
import { ApplicationCommissionOption } from '../../services/application-options.service';
import {
  ApplicationEnsClassificationsService,
  ApplicationSecurityService,
  ApplicationWebContextsService,
} from '../../services/application-security.service';
import { ApplicationSystemDatabaseService } from '../../services/application-system-database.service';
import { ApplicationsService } from '../../services/applications.service';

export type ApplicationGeneralFormValue = ApplicationDetailFormValue;
export type ApplicationDetailSection =
  | 'general'
  | 'responsible'
  | 'systems-databases'
  | 'development'
  | 'accessibility'
  | 'security'
  | 'data'
  | 'integrations';

export type ApplicationDetailSaveResult =
  | { status: 'saved'; section: ApplicationDetailSection }
  | { status: 'mocked'; section: ApplicationDetailSection }
  | { status: 'unchanged'; section: ApplicationDetailSection }
  | { status: 'invalid'; section: ApplicationDetailSection };

type ApplicationDevelopmentFormValue = ReturnType<ApplicationDevelopmentFormGroup['getRawValue']>;
type ApplicationSystemsDatabasesFormValue = ReturnType<
  ReturnType<typeof createApplicationSystemsDatabasesForm>['getRawValue']
>;
type ApplicationSecurityFormValue = ReturnType<ApplicationSecurityFormGroup['getRawValue']>;
type ApplicationIntegrationsFormValue = ReturnType<
  ReturnType<typeof createApplicationIntegrationsForm>['getRawValue']
>;

const EMPTY_GENERAL_FORM_VALUE: ApplicationDetailFormValue = {
  application: '',
  category: null,
  informationSystem: null,
  scope: null,
  administrativeUnit: null,
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
const EMPTY_INTEGRATIONS_VALUE: ApplicationIntegrationsFormValue = {
  observations: '',
};
const EMPTY_SECURITY_FORM_VALUE: ApplicationSecurityFormValue = {
  overallGradeId: null,
  identityProviderId: null,
  ensSubjectId: null,
  personalDataProcessingId: null,
  approvalDate: null,
  confidentialityId: null,
  integrityId: null,
  traceabilityId: null,
  availabilityId: null,
  authenticityId: null,
  observation: '',
};
const EMPTY_RESOURCES_PAGE = { items: [], total: 0 };
const DETAIL_SECTIONS: ApplicationDetailSection[] = [
  'general',
  'responsible',
  'systems-databases',
  'development',
  'accessibility',
  'security',
  'data',
  'integrations',
];

function createEditingState(): Record<ApplicationDetailSection, boolean> {
  return {
    general: false,
    responsible: false,
    'systems-databases': false,
    development: false,
    accessibility: false,
    security: false,
    data: false,
    integrations: false,
  };
}

@Injectable()
export class ApplicationDetailState {
  private readonly applicationsService = inject(ApplicationsService);
  private readonly developmentService = inject(ApplicationDevelopmentService);
  private readonly accessibilityService = inject(ApplicationAccessibilityService);
  private readonly dataService = inject(ApplicationDataService);
  private readonly integrationService = inject(ApplicationIntegrationService);
  private readonly systemDatabaseService = inject(ApplicationSystemDatabaseService);
  private readonly securityService = inject(ApplicationSecurityService);
  private readonly webContextsService = inject(ApplicationWebContextsService);
  private readonly ensClassificationsService = inject(ApplicationEnsClassificationsService);
  private readonly responsibleChanges = inject(ResponsibleDataChangesService);
  private readonly formBuilder = inject(FormBuilder);
  private readonly destroyRef = inject(DestroyRef);
  private initializedApplicationId: number | null = null;
  private completenessRequestVersion = 0;
  private webContextVerificationRequestVersion = 0;
  readonly completenessRefreshFailed = signal(false);
  readonly completenessRefreshing = signal(false);
  // Derived from the validation state of active web contexts.
  readonly hasUnverifiedWebContexts = signal<boolean | null>(null);
  readonly hasPendingResponsibleDir3 = signal<boolean | null>(null);

  private developmentApplicationId: number | null = null;
  private developmentInitialized = false;
  private securityApplicationId: number | null = null;
  private securityInitialized = false;
  private savedGeneralValue: ApplicationDetailFormValue = EMPTY_GENERAL_FORM_VALUE;
  private savedSystemsDatabasesValue: ApplicationSystemsDatabasesFormValue =
    EMPTY_SYSTEMS_DATABASES_VALUE;
  private savedDevelopmentValue: ApplicationDevelopmentFormValue = EMPTY_DEVELOPMENT_FORM_VALUE;
  private savedAccessibilityValue = this.cloneAccessibilityValue(
    EMPTY_APPLICATION_ACCESSIBILITY_VALUE,
  );
  private savedSecurityValue = this.cloneSecurityValue(EMPTY_SECURITY_FORM_VALUE);
  private savedIntegrationsValue: ApplicationIntegrationsFormValue = EMPTY_INTEGRATIONS_VALUE;
  private savedDataValue: ApplicationDataFormValue = { ...EMPTY_APPLICATION_DATA_VALUE };
  private readonly editingState = signal(createEditingState());

  readonly application = signal<Application | null>(null);
  readonly informationSystemDbId = signal<number | null>(null);
  readonly systemDatabase = signal<ApplicationSystemDatabaseOutput | null>(null);
  readonly development = signal<ApplicationDevelopmentOutput | null>(null);
  readonly appAccessibilityId = signal<number | null>(null);
  readonly accessibility = signal<ApplicationAccessibilityOutput | null>(null);
  readonly accessibilityStatus = signal<AccessibilityLoadStatus>('unavailable');
  readonly accessibilityClearBlocked = new Subject<void>();
  readonly canEditAccessibility = computed(() => this.canEdit() &&
    (this.accessibilityStatus() === 'loaded' || this.accessibilityStatus() === 'absent'));
  readonly appDataId = signal<number | null>(null);
  readonly data = signal<ApplicationDataOutput | null>(null);
  readonly dataStatus = signal<ApplicationDataLoadStatus>('unavailable');
  readonly dataErrorMessage = signal<string | null>(null);
  readonly dataActiveSource = signal<ApplicationDataSource>('openData');
  readonly canEditData = computed(() => this.canEdit() &&
    (this.dataStatus() === 'loaded' || this.dataStatus() === 'absent'));
  readonly appIntegrationId = signal<number | null>(null);
  readonly integration = signal<ApplicationIntegrationOutput | null>(null);
  readonly integrationStatus = signal<ApplicationIntegrationLoadStatus>('unavailable');
  readonly integrationErrorMessage = signal<string | null>(null);
  readonly canEditIntegrations = computed(() => this.canEdit() &&
    (this.integrationStatus() === 'loaded' || this.integrationStatus() === 'absent'));
  readonly appSecurityId = signal<number | null>(null);
  readonly security = signal<ApplicationSecurityOutput | null>(null);
  readonly ensClassification = signal<ApplicationEnsClassificationOutput | null>(null);
  readonly ensClassificationLoadState = signal<EnsClassificationLoadState>('unknown');
  readonly providers = signal<PaginatedList<ApplicationProviderOutput>>(EMPTY_RESOURCES_PAGE);
  readonly technologies = signal<PaginatedList<ApplicationTechnologyOutput>>(EMPTY_RESOURCES_PAGE);
  readonly isUnsavedChangesDialogVisible = signal(false);
  readonly canEdit = computed(() => {
    const application = this.application();
    return !!application && application.status !== ApplicationStatus.INACTIVE;
  });

  readonly form = createApplicationDetailForm(this.formBuilder);
  readonly dir3 = new AdministrativeUnitSearchState(this.form.controls.administrativeUnit);
  readonly developmentForm = createApplicationDevelopmentForm(this.formBuilder);
  readonly systemsDatabasesForm = createApplicationSystemsDatabasesForm(this.formBuilder);
  readonly accessibilityForm = createApplicationAccessibilityForm(this.formBuilder);
  readonly securityForm = createApplicationSecurityForm(this.formBuilder);
  readonly integrationsForm = createApplicationIntegrationsForm(this.formBuilder);
  readonly dataForm = createApplicationDataForm(this.formBuilder);

  constructor() {
    for (const source of ['openData', 'reuse'] as const) {
      const { useUrl } = APPLICATION_DATA_SOURCE_CONTROLS[source];
      this.dataForm.controls[useUrl].valueChanges
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe(() => this.configureDataUrl(source));
    }

    this.accessibilityForm.controls.mobileApplication.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((hasMobileApp) => {
        this.configureMobileApplicationName(hasMobileApp, true);
      });

    for (const key of ['classificationSegment', 'complianceStatus'] as const) {
      this.accessibilityForm.controls[key].valueChanges
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe(() => {
          if (this.isEditing('accessibility')) this.restoreAccessibilityReferences();
        });
    }

    this.responsibleChanges.assignments
      .pipe(
        switchMap(() => this.refreshCompleteness().pipe(catchError(() => of(undefined)))),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe();
  }

  initialize(response: ApplicationOutput | null): void {
    if (!response) {
      this.completenessRequestVersion++;
      this.completenessRefreshing.set(false);
      this.completenessRefreshFailed.set(false);
      this.resetState();
      return;
    }

    if (this.initializedApplicationId === response.id) return;

    this.completenessRefreshFailed.set(false);
    this.setApplication(response);
  }

  refreshWebContextVerification(): void {
    const securityId = this.appSecurityId();
    const applicationId = this.application()?.id;
    const version = ++this.webContextVerificationRequestVersion;
    if (securityId == null) {
      this.hasUnverifiedWebContexts.set(false);
      return;
    }
    this.hasUnverifiedWebContexts.set(null);
    this.webContextsService
      .hasUnverified(securityId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (pending) => {
          if (
            version === this.webContextVerificationRequestVersion &&
            this.application()?.id === applicationId
          ) {
            this.hasUnverifiedWebContexts.set(pending);
          }
        },
        error: () => {
          if (
            version === this.webContextVerificationRequestVersion &&
            this.application()?.id === applicationId
          ) {
            this.hasUnverifiedWebContexts.set(null);
          }
        },
      });
  }

  updateResponsibleDir3(assignments: readonly ApplicationResponsibleOutput[]): void {
    this.hasPendingResponsibleDir3.set(hasPendingResponsibleDir3(assignments));
  }

  initializeAccessibility(result: ApplicationAccessibilityLoadResult): void {
    if (result.applicationId !== Number(this.application()?.id) || this.isEditing('accessibility')) return;
    this.appAccessibilityId.set(result.appAccessibilityId);
    this.accessibilityStatus.set(result.status);
    this.accessibility.set(result.record);
    this.application.update((current) => current ? { ...current, appAccessibilityId: result.appAccessibilityId } : current);
    this.savedAccessibilityValue = result.record ? {
      classificationSegment: result.record.classificationSegment?.id ?? null,
      complianceStatus: result.record.compliance?.id ?? null,
      reportExpirationDate: parseLocalDateTime(result.record.expireDate),
      mobileApplication: result.record.mobileApplication,
      mobileApplicationName: result.record.mobileApplicationName,
      publicUrl: result.record.publicUrl ?? '',
      inaccessibleContent: result.record.nonAccessibleContent ?? '',
      observations: result.record.observations ?? '',
    } : { ...EMPTY_APPLICATION_ACCESSIBILITY_VALUE };
    this.accessibilityForm.reset(this.cloneAccessibilityValue(this.savedAccessibilityValue), { emitEvent: false });
    this.configureMobileApplicationName(this.savedAccessibilityValue.mobileApplication, false);
  }

  initializeData(result: ApplicationDataLoadResult): void {
    if (result.applicationId !== Number(this.application()?.id) || this.isEditing('data')) return;
    this.appDataId.set(result.appDataId);
    this.dataStatus.set(result.status);
    this.dataErrorMessage.set(result.errorMessage);
    this.data.set(result.record);
    this.application.update((current) =>
      current ? { ...current, appDataId: result.appDataId } : current,
    );
    this.savedDataValue = result.record
      ? {
          useOpenDataUrl: result.record.useOpenDataUrl,
          openDataUrl: result.record.openDataUrl ?? '',
          useReuseUrl: result.record.useReuseUrl,
          reuseUrl: result.record.reuseUrl ?? '',
          observations: result.record.observation ?? '',
        }
      : { ...EMPTY_APPLICATION_DATA_VALUE };
    this.dataForm.reset({ ...this.savedDataValue }, { emitEvent: false });
  }

  initializeIntegrations(result: ApplicationIntegrationsLoadResult): void {
    if (result.applicationId !== Number(this.application()?.id) || this.isEditing('integrations')) {
      return;
    }
    this.applyIntegration(result.appIntegrationId, result.record, result.status);
    this.integrationErrorMessage.set(result.errorMessage);
  }

  private applyIntegration(
    appIntegrationId: number | null,
    record: ApplicationIntegrationOutput | null,
    status: ApplicationIntegrationLoadStatus,
  ): void {
    this.appIntegrationId.set(appIntegrationId);
    this.integration.set(record);
    this.integrationStatus.set(status);
    this.application.update((current) =>
      current ? { ...current, appIntegrationId } : current,
    );
    this.savedIntegrationsValue = { observations: record?.observation ?? '' };
    this.integrationsForm.reset({ ...this.savedIntegrationsValue }, { emitEvent: false });
  }

  private restoreAccessibilityReferences(): boolean {
    let restored = false;
    for (const key of ['classificationSegment', 'complianceStatus'] as const) {
      const saved = this.savedAccessibilityValue[key];
      const control = this.accessibilityForm.controls[key];
      if (saved != null && control.value == null) {
        control.setValue(saved, { emitEvent: false });
        restored = true;
      }
    }
    if (restored) this.accessibilityClearBlocked.next();
    return restored;
  }

  initializeSecurity(
    applicationId: number | null,
    response: ApplicationSecurityOutput | null,
    classifications: ApplicationEnsClassificationOutput[],
    classificationLoadFailed = false,
  ): void {
    if (
      this.securityInitialized &&
      applicationId != null &&
      applicationId === this.securityApplicationId
    ) {
      return;
    }

    this.securityApplicationId = applicationId;
    this.securityInitialized = applicationId != null;
    this.security.set(response);
    this.appSecurityId.set(response?.id ?? this.appSecurityId());
    this.ensClassification.set(classifications.length === 1 ? classifications[0] : null);
    if (classificationLoadFailed) this.ensClassificationLoadState.set('unknown');
    else if (classifications.length > 1) this.ensClassificationLoadState.set('inconsistent');
    else this.ensClassificationLoadState.set('ready');
    this.savedSecurityValue = this.toSecurityFormValue(response, this.ensClassification());
    this.securityForm.reset(this.cloneSecurityValue(this.savedSecurityValue), {
      emitEvent: false,
    });
    this.configureEnsControls();
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
    this.systemsDatabasesForm.enable({ emitEvent: false });
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
      case 'accessibility':
        if (!this.canEditAccessibility()) return;
        this.accessibilityForm.enable({ emitEvent: false });
        this.configureMobileApplicationName(
          this.accessibilityForm.controls.mobileApplication.value,
          false,
        );
        break;
      case 'security':
        this.securityForm.enable({ emitEvent: false });
        this.configureEnsControls();
        break;
      case 'data':
        if (!this.canEditData()) return;
        this.dataForm.enable({ emitEvent: false });
        break;
      case 'integrations':
        if (!this.canEditIntegrations()) return;
        this.integrationsForm.enable({ emitEvent: false });
        break;
    }

    this.setEditing(section, true);
  }

  cancelEditing(section: ApplicationDetailSection): void {
    switch (section) {
      case 'general':
        this.form.reset(this.savedGeneralValue, { emitEvent: false });
        this.form.enable({ emitEvent: false });
        this.restoreDir3Snapshot();
        break;
      case 'systems-databases':
        this.systemsDatabasesForm.reset(this.savedSystemsDatabasesValue, {
          emitEvent: false,
        });
        this.systemsDatabasesForm.enable({ emitEvent: false });
        break;
      case 'development':
        this.developmentForm.reset(this.cloneDevelopmentValue(this.savedDevelopmentValue), {
          emitEvent: false,
        });
        this.developmentForm.enable({ emitEvent: false });
        break;
      case 'responsible':
        break;
      case 'accessibility':
        this.accessibilityForm.reset(this.cloneAccessibilityValue(this.savedAccessibilityValue), {
          emitEvent: false,
        });
        this.configureMobileApplicationName(this.savedAccessibilityValue.mobileApplication, false);
        break;
      case 'security':
        this.securityForm.reset(this.cloneSecurityValue(this.savedSecurityValue), {
          emitEvent: false,
        });
        this.configureEnsControls();
        break;
      case 'data':
        this.dataForm.reset({ ...this.savedDataValue }, { emitEvent: false });
        this.dataForm.enable({ emitEvent: false });
        break;
      case 'integrations':
        this.integrationsForm.reset(this.savedIntegrationsValue, { emitEvent: false });
        this.integrationsForm.enable({ emitEvent: false });
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
      case 'accessibility':
        return this.saveAccessibility();
      case 'security':
        return this.saveSecurity();
      case 'data':
        return this.saveData();
      case 'integrations':
        return this.saveIntegrations();
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
      case 'accessibility':
        return this.accessibilityForm.dirty;
      case 'security':
        return this.securityForm.dirty;
      case 'data':
        return this.dataForm.dirty;
      case 'integrations':
        return this.integrationsForm.dirty;
    }
  }

  activate(): Observable<boolean> {
    const current = this.application();
    const id = Number(current?.id);
    if (!current || Number.isNaN(id)) return of(false);

    return this.applicationsService.reactivate(id).pipe(
      switchMap((response) => this.refreshAfterApplicationMutation(id, response)),
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

  refreshCompletenessAfterMutation(): void {
    this.refreshCompleteness()
      .pipe(
        catchError(() => of(undefined)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe();
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
      switchMap((response) => this.refreshAfterApplicationMutation(id, response)),
      tap((response) => {
        this.applyGeneralResponse(response);
        if (current.admUnitCode !== this.application()?.admUnitCode) {
          this.responsibleChanges.assignmentsChanged();
        }
      }),
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
      observation: normalizeQuillHtml(observations),
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
        this.systemsDatabasesForm.enable({ emitEvent: false });
        this.setEditing(section, false);
      }),
      switchMap(() => this.refreshCompleteness().pipe(catchError(() => of(undefined)))),
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
      switchMap(() => this.refreshCompleteness().pipe(catchError(() => of(undefined)))),
      map(() => ({ status: 'saved', section }) as const),
    );
  }

  private saveAccessibility(): Observable<ApplicationDetailSaveResult> {
    const section: ApplicationDetailSection = 'accessibility';
    if (!this.canEditAccessibility() || this.restoreAccessibilityReferences()) {
      return of({ status: 'invalid', section });
    }
    if (this.accessibilityForm.invalid) {
      this.accessibilityForm.markAllAsTouched();
      return of({ status: 'invalid', section });
    }
    if (this.accessibilityForm.pristine) {
      this.finishUnchanged(section);
      return of({ status: 'unchanged', section });
    }
    const applicationId = Number(this.application()?.id);
    if (!Number.isInteger(applicationId) || applicationId <= 0) return of({ status: 'invalid', section });
    const value = this.accessibilityForm.getRawValue();
    const payload: ApplicationAccessibilityInput = {
      applicationId,
      complianceId: value.complianceStatus,
      classificationSegmentId: value.classificationSegment,
      publicUrl: value.publicUrl.trim() || null,
      mobileApplication: value.mobileApplication,
      mobileApplicationName: value.mobileApplicationName?.trim() || null,
      nonAccessibleContent: normalizeQuillHtml(value.inaccessibleContent) || null,
      observations: normalizeQuillHtml(value.observations) || null,
      expireDate: formatLocalDateTime(value.reportExpirationDate),
    };
    const id = this.appAccessibilityId();
    const request = id == null
      ? this.accessibilityService.create(payload)
      : this.accessibilityService.update(id, payload);
    return defer(() => {
      this.accessibilityForm.disable({ emitEvent: false });
      return request;
    }).pipe(
      tap((record) => {
        if (!record || record.application?.id !== applicationId || !Number.isInteger(record.id) ||
          record.id <= 0 || (id != null && record.id !== id) || record.deletedAt) {
          throw new Error('Invalid accessibility response');
        }
        this.setEditing(section, false);
        this.initializeAccessibility({ applicationId, appAccessibilityId: record.id, record, status: 'loaded' });
      }),
      switchMap(() => this.refreshCompleteness().pipe(catchError(() => of(undefined)))),
      map(() => ({ status: 'saved', section }) as const),
      finalize(() => {
        this.accessibilityForm.enable({ emitEvent: false });
        this.configureMobileApplicationName(this.accessibilityForm.controls.mobileApplication.value, false);
      }),
    );
  }

  private saveSecurity(): Observable<ApplicationDetailSaveResult> {
    const section: ApplicationDetailSection = 'security';
    if (this.securityForm.invalid) {
      this.securityForm.markAllAsTouched();
      return of({ status: 'invalid', section });
    }
    if (this.securityForm.pristine) {
      this.finishUnchanged(section);
      return of({ status: 'unchanged', section });
    }

    const applicationId = Number(this.application()?.id);
    if (!Number.isInteger(applicationId) || applicationId <= 0) {
      return of({ status: 'invalid', section });
    }

    const observationDirty = this.securityForm.controls.observation.dirty;
    const ensDirty = this.ensControls().some((control) => control.dirty);
    const currentSecurity = this.security();
    const corePayload: ApplicationSecurityInput = {
      applicationId,
      observation: normalizeQuillHtml(this.securityForm.controls.observation.value),
    };
    const coreRequest = currentSecurity
      ? observationDirty
        ? this.securityService.update(currentSecurity.id, corePayload)
        : of(currentSecurity)
      : this.securityService.create(corePayload);

    return coreRequest.pipe(
      tap((response) => this.applySecurityCoreResponse(response)),
      switchMap((response) => {
        if (
          !ensDirty
          || ['inconsistent', 'unknown'].includes(this.ensClassificationLoadState())
        ) {
          return of(this.ensClassification());
        }

        const payload = this.toEnsClassificationInput(response.id);
        const currentClassification = this.ensClassification();
        if (!currentClassification && !this.hasEnsData(payload)) return of(null);

        return currentClassification
          ? this.ensClassificationsService.update(currentClassification.id, payload)
          : this.ensClassificationsService.create(payload);
      }),
      tap((classification) => this.finishSecuritySave(classification)),
      switchMap(() => this.refreshCompleteness().pipe(catchError(() => of(undefined)))),
      map(() => ({ status: 'saved', section }) as const),
    );
  }

  private saveData(): Observable<ApplicationDetailSaveResult> {
    const section: ApplicationDetailSection = 'data';
    if (!this.canEditData()) return of({ status: 'invalid', section });
    if (this.dataForm.invalid) {
      this.dataForm.markAllAsTouched();
      this.dataActiveSource.set(this.firstInvalidDataSource());
      return of({ status: 'invalid', section });
    }
    if (this.dataForm.pristine) {
      this.finishUnchanged(section);
      return of({ status: 'unchanged', section });
    }
    const applicationId = Number(this.application()?.id);
    if (!Number.isInteger(applicationId) || applicationId <= 0) {
      return of({ status: 'invalid', section });
    }
    const value = this.dataForm.getRawValue();
    const payload: ApplicationDataInput = {
      applicationId,
      observation: normalizeQuillHtml(value.observations) || null,
      openDataUrl: value.openDataUrl.trim() || null,
      useOpenDataUrl: value.useOpenDataUrl,
      reuseUrl: value.reuseUrl.trim() || null,
      useReuseUrl: value.useReuseUrl,
    };
    const id = this.appDataId();
    const request = id == null
      ? this.dataService.create(payload)
      : this.dataService.update(id, payload);
    return defer(() => {
      this.dataForm.disable({ emitEvent: false });
      return request;
    }).pipe(
      tap((record) => {
        if (!record || record.application?.id !== applicationId || !Number.isInteger(record.id) ||
          record.id <= 0 || (id != null && record.id !== id) || record.deletedAt) {
          throw new Error('Invalid data response');
        }
        this.setEditing(section, false);
      }),
      // Write responses omit the published endpoints and the URLs may have changed.
      switchMap((record) => this.reloadData(applicationId, record.id)),
      tap((result) => this.initializeData(result)),
      map(() => ({ status: 'saved', section }) as const),
      finalize(() => this.dataForm.enable({ emitEvent: false })),
    );
  }

  private reloadData(applicationId: number, appDataId: number): Observable<ApplicationDataLoadResult> {
    const failed = (errorMessage: string | null): ApplicationDataLoadResult => ({
      applicationId,
      appDataId,
      record: null,
      status: 'failed',
      errorMessage,
    });
    return this.dataService.refreshById(appDataId).pipe(
      map((record): ApplicationDataLoadResult =>
        record && record.id === appDataId && record.application?.id === applicationId
          ? { applicationId, appDataId, record, status: record.deletedAt ? 'deleted' : 'loaded', errorMessage: null }
          : failed(null),
      ),
      defaultIfEmpty(failed(null)),
      catchError((error: unknown) =>
        of(failed(readApiErrorMessage(error))),
      ),
    );
  }

  private firstInvalidDataSource(): ApplicationDataSource {
    const invalid = (['openData', 'reuse'] as const).find(
      (source) => this.dataForm.controls[APPLICATION_DATA_SOURCE_CONTROLS[source].url].invalid,
    );
    return invalid ?? this.dataActiveSource();
  }

  // With its flag off an URL only shows the backend-detected value, so edits are discarded.
  private configureDataUrl(source: ApplicationDataSource): void {
    const { useUrl, url } = APPLICATION_DATA_SOURCE_CONTROLS[source];
    const control = this.dataForm.controls[url];
    if (!this.dataForm.controls[useUrl].value) {
      control.setValue(this.savedDataValue[url], { emitEvent: false });
    }
    control.updateValueAndValidity({ emitEvent: false });
  }

  // The anchor response carries every persisted field, so it replaces the snapshot directly.
  private saveIntegrations(): Observable<ApplicationDetailSaveResult> {
    const section: ApplicationDetailSection = 'integrations';
    if (!this.canEditIntegrations()) return of({ status: 'invalid', section });
    if (this.integrationsForm.pristine) {
      this.finishUnchanged(section);
      return of({ status: 'unchanged', section });
    }
    const applicationId = Number(this.application()?.id);
    if (!Number.isInteger(applicationId) || applicationId <= 0) {
      return of({ status: 'invalid', section });
    }
    const payload: ApplicationIntegrationInput = {
      applicationId,
      observation:
        normalizeQuillHtml(this.integrationsForm.getRawValue().observations) || null,
    };
    const id = this.appIntegrationId();
    const request = id == null
      ? this.integrationService.create(payload)
      : this.integrationService.update(id, payload);
    return defer(() => {
      this.integrationsForm.disable({ emitEvent: false });
      return request;
    }).pipe(
      tap((record) => {
        if (!record || record.applicationId !== applicationId || !Number.isInteger(record.id) ||
          record.id <= 0 || (id != null && record.id !== id) || record.deletedAt) {
          throw new Error('Invalid integration response');
        }
        this.setEditing(section, false);
        this.applyIntegration(record.id, record, 'loaded');
        this.integrationErrorMessage.set(null);
      }),
      map(() => ({ status: 'saved', section }) as const),
      finalize(() => this.integrationsForm.enable({ emitEvent: false })),
    );
  }

  private finishUnchanged(section: ApplicationDetailSection): void {
    switch (section) {
      case 'general':
        this.form.enable({ emitEvent: false });
        break;
      case 'systems-databases':
        this.systemsDatabasesForm.enable({ emitEvent: false });
        break;
      case 'development':
        this.developmentForm.enable({ emitEvent: false });
        break;
      case 'responsible':
        break;
      case 'accessibility':
        this.configureMobileApplicationName(
          this.accessibilityForm.controls.mobileApplication.value,
          false,
        );
        break;
      case 'security':
        this.securityForm.enable({ emitEvent: false });
        this.configureEnsControls();
        break;
      case 'data':
        this.dataForm.enable({ emitEvent: false });
        break;
      case 'integrations':
        this.integrationsForm.enable({ emitEvent: false });
        break;
    }
    this.setEditing(section, false);
  }

  private resetState(): void {
    this.hasPendingResponsibleDir3.set(null);
    this.hasUnverifiedWebContexts.set(null);
    this.webContextVerificationRequestVersion++;
    this.initializedApplicationId = null;
    this.application.set(null);
    this.dir3.selectSnapshot(null);
    this.informationSystemDbId.set(null);
    this.appSecurityId.set(null);
    this.systemDatabase.set(null);
    this.savedGeneralValue = { ...EMPTY_GENERAL_FORM_VALUE };
    this.savedSystemsDatabasesValue = { ...EMPTY_SYSTEMS_DATABASES_VALUE };
    this.resetAccessibilityState();
    this.resetDevelopmentState();
    this.resetSecurityState();
    this.resetDataState();
    this.resetIntegrationsState();
    this.form.reset(this.savedGeneralValue, { emitEvent: false });
    this.systemsDatabasesForm.reset(this.savedSystemsDatabasesValue, {
      emitEvent: false,
    });
    this.enableViewForms();
    this.editingState.set(createEditingState());
  }

  private setApplication(response: ApplicationOutput): void {
    this.hasPendingResponsibleDir3.set(null);
    this.hasUnverifiedWebContexts.set(null);
    this.webContextVerificationRequestVersion++;
    this.completenessRequestVersion++;
    this.completenessRefreshing.set(false);
    this.initializedApplicationId = response.id;
    this.application.set(this.applicationsService.toApplication(response));
    this.informationSystemDbId.set(response.appInformationSystemDbId);
    this.appSecurityId.set(response.appSecurityId ?? null);
    this.systemDatabase.set(null);
    this.savedGeneralValue = this.toGeneralFormValue(response);
    this.dir3.selectSnapshot(response.admUnit);
    this.savedSystemsDatabasesValue = { ...EMPTY_SYSTEMS_DATABASES_VALUE };
    this.resetAccessibilityState();
    this.resetDevelopmentState();
    this.resetSecurityState();
    this.resetDataState();
    this.resetIntegrationsState();
    this.form.reset(this.savedGeneralValue, { emitEvent: false });
    this.systemsDatabasesForm.reset(this.savedSystemsDatabasesValue, {
      emitEvent: false,
    });
    this.enableViewForms();
    this.editingState.set(createEditingState());
  }

  private applyGeneralResponse(response: ApplicationOutput): void {
    this.initializedApplicationId = response.id;
    this.application.set(this.applicationsService.toApplication(response));
    this.informationSystemDbId.set(
      response.appInformationSystemDbId ?? this.informationSystemDbId(),
    );
    this.savedGeneralValue = this.toGeneralFormValue(response);
    this.dir3.selectSnapshot(response.admUnit);
    this.form.reset(this.savedGeneralValue, { emitEvent: false });
    this.form.enable({ emitEvent: false });
    this.restoreDir3Snapshot();
    this.setEditing('general', false);
  }

  private refreshCompleteness(): Observable<void> {
    const id = Number(this.application()?.id);
    if (!Number.isInteger(id) || id <= 0) return of(undefined);
    return this.readCurrentApplication(id).pipe(map(() => undefined));
  }

  private refreshAfterApplicationMutation(
    id: number,
    response: ApplicationOutput,
  ): Observable<ApplicationOutput> {
    return this.readCurrentApplication(id).pipe(
      catchError(() => of(null)),
      defaultIfEmpty(null),
      filter(() => Number(this.application()?.id) === id),
      map((fresh) => fresh ?? this.withCurrentDetailData(response)),
    );
  }

  private readCurrentApplication(id: number): Observable<ApplicationOutput> {
    return defer(() => {
      if (Number(this.application()?.id) !== id) return EMPTY;
      const version = ++this.completenessRequestVersion;
      const isCurrent = () =>
        version === this.completenessRequestVersion && Number(this.application()?.id) === id;
      this.completenessRefreshing.set(true);
      return this.applicationsService.refreshById(id).pipe(
        filter(() => isCurrent()),
        tap((response) => {
          this.completenessRefreshFailed.set(false);
          this.appSecurityId.set(response.appSecurityId ?? this.appSecurityId());
          this.application.update((current) =>
            current ? { ...current, ...readApplicationCompleteness(response) } : current,
          );
        }),
        catchError((error: unknown) => {
          if (!isCurrent()) return EMPTY;
          this.completenessRefreshFailed.set(true);
          return throwError(() => error);
        }),
        finalize(() => {
          if (isCurrent()) this.completenessRefreshing.set(false);
        }),
        takeUntilDestroyed(this.destroyRef),
      );
    });
  }

  private withCurrentCompleteness(response: ApplicationOutput): ApplicationOutput {
    const current = this.application();
    if (!current) return response;
    return { ...response, ...readApplicationCompleteness(current) };
  }

  private withCurrentDetailData(response: ApplicationOutput): ApplicationOutput {
    const current = this.application();
    const value = this.form.getRawValue();
    const enriched = this.withCurrentCompleteness(response);

    return {
      ...enriched,
      admUnit:
        enriched.admUnit ??
        (value.administrativeUnit
          ? {
              code: value.administrativeUnit,
              name: this.dir3.options().find(unit => unit.code === value.administrativeUnit)?.name || current?.administrativeUnit || value.administrativeUnit,
              parentCode: null,
              level: null,
            }
          : null),

    };
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

    this.developmentForm.enable({ emitEvent: false });
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

  private resetAccessibilityState(): void {
    this.appAccessibilityId.set(this.application()?.appAccessibilityId ?? null);
    this.accessibility.set(null);
    this.accessibilityStatus.set('unavailable');
    this.savedAccessibilityValue = this.cloneAccessibilityValue(
      EMPTY_APPLICATION_ACCESSIBILITY_VALUE,
    );
    this.accessibilityForm.reset(this.cloneAccessibilityValue(this.savedAccessibilityValue), {
      emitEvent: false,
    });
    this.configureMobileApplicationName(this.savedAccessibilityValue.mobileApplication, false);
  }

  private enableViewForms(): void {
    this.form.enable({ emitEvent: false });
    this.systemsDatabasesForm.enable({ emitEvent: false });
    this.developmentForm.enable({ emitEvent: false });
    this.accessibilityForm.enable({ emitEvent: false });
    this.securityForm.enable({ emitEvent: false });
    this.dataForm.enable({ emitEvent: false });
    this.integrationsForm.enable({ emitEvent: false });
    this.configureEnsControls();
    this.configureMobileApplicationName(
      this.accessibilityForm.controls.mobileApplication.value,
      false,
    );
  }

  private restoreDir3Snapshot(): void {
    const application = this.application();
    this.dir3.selectSnapshot(application?.admUnitCode ? {
      code: application.admUnitCode, name: application.administrativeUnit,
      parentCode: null, level: null,
    } : null);
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
      administrativeUnit: application.admUnitCode ?? null,
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
      admUnitCode: value.administrativeUnit as string,
      commissionId: value.commission as number,
      description: normalizeQuillHtml(value.description),
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
      observation: normalizeQuillHtml(value.observation),
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

  private applySecurityCoreResponse(response: ApplicationSecurityOutput): void {
    this.security.set(response);
    this.appSecurityId.set(response.id);
    this.application.update((current) =>
      current ? { ...current, appSecurityId: response.id } : current,
    );
    this.savedSecurityValue = {
      ...this.savedSecurityValue,
      observation: response.observation ?? '',
    };
    this.securityForm.controls.observation.reset(this.savedSecurityValue.observation, {
      emitEvent: false,
    });
  }

  private finishSecuritySave(classification: ApplicationEnsClassificationOutput | null): void {
    this.ensClassification.set(classification);
    this.savedSecurityValue = this.toSecurityFormValue(this.security(), classification);
    this.securityForm.reset(this.cloneSecurityValue(this.savedSecurityValue), {
      emitEvent: false,
    });
    this.configureEnsControls();
    this.setEditing('security', false);
  }

  private resetSecurityState(): void {
    this.security.set(null);
    this.ensClassification.set(null);
    this.securityApplicationId = null;
    this.securityInitialized = false;
    this.ensClassificationLoadState.set('unknown');
    this.savedSecurityValue = this.cloneSecurityValue(EMPTY_SECURITY_FORM_VALUE);
    this.securityForm.reset(this.cloneSecurityValue(this.savedSecurityValue), {
      emitEvent: false,
    });
    this.configureEnsControls();
  }

  private resetDataState(): void {
    this.appDataId.set(this.application()?.appDataId ?? null);
    this.data.set(null);
    this.dataStatus.set('unavailable');
    this.dataErrorMessage.set(null);
    this.dataActiveSource.set('openData');
    this.savedDataValue = { ...EMPTY_APPLICATION_DATA_VALUE };
    this.dataForm.reset({ ...this.savedDataValue }, { emitEvent: false });
  }

  private resetIntegrationsState(): void {
    this.appIntegrationId.set(this.application()?.appIntegrationId ?? null);
    this.integration.set(null);
    this.integrationStatus.set('unavailable');
    this.integrationErrorMessage.set(null);
    this.savedIntegrationsValue = { ...EMPTY_INTEGRATIONS_VALUE };
    this.integrationsForm.reset(this.savedIntegrationsValue, { emitEvent: false });
  }

  private toSecurityFormValue(
    security: ApplicationSecurityOutput | null,
    classification: ApplicationEnsClassificationOutput | null,
  ): ApplicationSecurityFormValue {
    return {
      overallGradeId: classification?.overallGrade?.id ?? null,
      identityProviderId: classification?.identityProvider?.id ?? null,
      ensSubjectId: classification?.ensSubject?.id ?? null,
      personalDataProcessingId: classification?.personalDataProcessing?.id ?? null,
      approvalDate: parseLocalDateTime(classification?.approvalDate),
      confidentialityId: classification?.confidentiality?.id ?? null,
      integrityId: classification?.integrity?.id ?? null,
      traceabilityId: classification?.traceability?.id ?? null,
      availabilityId: classification?.availability?.id ?? null,
      authenticityId: classification?.authenticity?.id ?? null,
      observation: security?.observation ?? '',
    };
  }

  private toEnsClassificationInput(appSecurityId: number): ApplicationEnsClassificationInput {
    const value = this.securityForm.getRawValue();
    return {
      appSecurityId,
      identityProviderId: value.identityProviderId,
      ensSubjectId: value.ensSubjectId,
      personalDataProcessingId: value.personalDataProcessingId,
      approvalDate: formatLocalDateTime(value.approvalDate),
      confidentialityId: value.confidentialityId,
      integrityId: value.integrityId,
      traceabilityId: value.traceabilityId,
      availabilityId: value.availabilityId,
      authenticityId: value.authenticityId,
      overallGradeId: value.overallGradeId,
    };
  }

  private hasEnsData(payload: ApplicationEnsClassificationInput): boolean {
    return Object.entries(payload).some(([key, value]) => key !== 'appSecurityId' && value != null);
  }

  private ensControls() {
    const controls = this.securityForm.controls;
    return [
      controls.overallGradeId,
      controls.identityProviderId,
      controls.ensSubjectId,
      controls.personalDataProcessingId,
      controls.approvalDate,
      controls.confidentialityId,
      controls.integrityId,
      controls.traceabilityId,
      controls.availabilityId,
      controls.authenticityId,
    ];
  }

  private configureEnsControls(): void {
    const ensUnavailable = ['inconsistent', 'unknown'].includes(
      this.ensClassificationLoadState(),
    );
    this.ensControls().forEach((control) =>
      ensUnavailable
        ? control.disable({ emitEvent: false })
        : control.enable({ emitEvent: false }),
    );
  }

  private cloneSecurityValue(value: ApplicationSecurityFormValue): ApplicationSecurityFormValue {
    return {
      ...value,
      approvalDate: value.approvalDate ? new Date(value.approvalDate) : null,
    };
  }

  private cloneAccessibilityValue(
    value: ApplicationAccessibilityFormValue,
  ): ApplicationAccessibilityFormValue {
    return {
      ...value,
      reportExpirationDate: value.reportExpirationDate
        ? new Date(value.reportExpirationDate)
        : null,
    };
  }

  private configureMobileApplicationName(hasMobileApp: boolean | null, clearValue: boolean): void {
    const control = this.accessibilityForm.controls.mobileApplicationName;
    if (hasMobileApp) {
      control.enable({ emitEvent: false });
      return;
    }

    if (clearValue) control.setValue(null, { emitEvent: false });
    control.disable({ emitEvent: false });
  }

  private clonePage<T>(page: PaginatedList<T>): PaginatedList<T> {
    return { items: [...page.items], total: page.total };
  }
}
