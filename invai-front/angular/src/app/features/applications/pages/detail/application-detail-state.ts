import { ApplicationAccessibilityService } from '../../services/application-accessibility.service';
import { ApplicationAccessibilityInput, ApplicationAccessibilityOutput } from '../../applications.model';
import type { ApplicationAccessibilityLoadResult, AccessibilityLoadStatus } from './sections/accessibility/application-accessibility-section.resolver';
import { DestroyRef, Injectable, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder } from '@angular/forms';
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

import { APPLICATION_STATUS_ACTIVE_ID } from '../../applications.constants';
import {
  EMPTY_APPLICATION_ACCESSIBILITY_VALUE,
  ApplicationAccessibilityFormValue,
  createApplicationAccessibilityForm,
} from '../../forms/application-accessibility-form.factory';
import {
  ApplicationDevelopmentFormGroup,
  createApplicationDevelopmentForm,
  formatLocalDateTime,
  parseLocalDateTime,
} from '../../forms/application-development-form.factory';
import {
  ApplicationSecurityFormGroup,
  createApplicationSecurityForm,
} from '../../forms/application-security-form.factory';
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
  ApplicationEnsClassificationInput,
  ApplicationEnsClassificationOutput,
  EnsClassificationLoadState,
  ApplicationSecurityInput,
  ApplicationSecurityOutput,
  ApplicationStatus,
  SelectOption,
  ApplicationSystemDatabaseOutput,
  ApplicationTechnologyOutput,
} from '../../applications.model';
import { ApplicationDevelopmentService } from '../../services/application-development.service';
import {
  ApplicationCommissionOption,
  ApplicationOptionsService,
} from '../../services/application-options.service';
import { ApplicationSystemDatabaseService } from '../../services/application-system-database.service';
import {
  ApplicationEnsClassificationsService,
  ApplicationSecurityService,
} from '../../services/application-security.service';
import { ApplicationsService } from '../../services/applications.service';

export type ApplicationGeneralFormValue = ApplicationDetailFormValue;
export type ApplicationDetailSection =
  'general' | 'responsible' | 'systems-databases' | 'development' | 'accessibility' | 'security';

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

const EMPTY_GENERAL_FORM_VALUE: ApplicationDetailFormValue = {
  application: '',
  category: null,
  informationSystem: null,
  scope: null,
  administrativeUnit: null,
  conselleria: null,
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
];

function createEditingState(): Record<ApplicationDetailSection, boolean> {
  return {
    general: false,
    responsible: false,
    'systems-databases': false,
    development: false,
    accessibility: false,
    security: false,
  };
}

@Injectable()
export class ApplicationDetailState {
  private readonly applicationsService = inject(ApplicationsService);
  private readonly applicationOptionsService = inject(ApplicationOptionsService);
  private readonly developmentService = inject(ApplicationDevelopmentService);
  private readonly accessibilityService = inject(ApplicationAccessibilityService);
  private readonly systemDatabaseService = inject(ApplicationSystemDatabaseService);
  private readonly securityService = inject(ApplicationSecurityService);
  private readonly ensClassificationsService = inject(ApplicationEnsClassificationsService);
  private readonly responsibleChanges = inject(ResponsibleDataChangesService);
  private readonly formBuilder = inject(FormBuilder);
  private readonly destroyRef = inject(DestroyRef);
  private initializedApplicationId: number | null = null;
  private completenessRequestVersion = 0;
  readonly completenessRefreshFailed = signal(false);
  readonly completenessRefreshing = signal(false);
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
  private readonly editingState = signal(createEditingState());
  private readonly administrativeUnitRequests = new Subject<string | null>();
  private loadedAdministrativeUnitsDepartmentCode: string | null = null;

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
  readonly appSecurityId = signal<number | null>(null);
  readonly security = signal<ApplicationSecurityOutput | null>(null);
  readonly ensClassification = signal<ApplicationEnsClassificationOutput | null>(null);
  readonly ensClassificationLoadState = signal<EnsClassificationLoadState>('unknown');
  readonly providers = signal<PaginatedList<ApplicationProviderOutput>>(EMPTY_RESOURCES_PAGE);
  readonly technologies = signal<PaginatedList<ApplicationTechnologyOutput>>(EMPTY_RESOURCES_PAGE);
  readonly isUnsavedChangesDialogVisible = signal(false);
  readonly administrativeUnitOptions = signal<SelectOption<string>[]>([]);
  readonly administrativeUnitsLoading = signal(false);
  readonly administrativeUnitsLoadFailed = signal(false);
  readonly canEdit = computed(() => {
    const application = this.application();
    return !!application && application.status !== ApplicationStatus.INACTIVE;
  });

  readonly form = createApplicationDetailForm(this.formBuilder);
  readonly developmentForm = createApplicationDevelopmentForm(this.formBuilder);
  readonly systemsDatabasesForm = createApplicationSystemsDatabasesForm(this.formBuilder);
  readonly accessibilityForm = createApplicationAccessibilityForm(this.formBuilder);
  readonly securityForm = createApplicationSecurityForm(this.formBuilder);

  constructor() {
    this.administrativeUnitRequests
      .pipe(
        tap((departmentCode) => {
          this.administrativeUnitsLoading.set(Boolean(departmentCode));
          this.administrativeUnitsLoadFailed.set(false);
          this.form.controls.administrativeUnit.disable({ emitEvent: false });
        }),
        switchMap((departmentCode) =>
          departmentCode
            ? this.applicationOptionsService.getAdministrativeUnitOptions(departmentCode).pipe(
                map((options) => ({ departmentCode, options, failed: false })),
                catchError(() =>
                  of({ departmentCode, options: [] as SelectOption<string>[], failed: true }),
                ),
              )
            : of({
                departmentCode: null,
                options: [] as SelectOption<string>[],
                failed: false,
              }),
        ),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(({ departmentCode, options, failed }) => {
        this.administrativeUnitsLoading.set(false);
        this.administrativeUnitsLoadFailed.set(failed);
        this.administrativeUnitOptions.set(options);
        this.loadedAdministrativeUnitsDepartmentCode = failed ? null : departmentCode;
        if (!failed && departmentCode === this.form.controls.conselleria.value) {
          this.form.controls.administrativeUnit.enable({ emitEvent: false });
        }
      });

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

  initializeAdministrativeUnitOptions(
    options: SelectOption<string>[],
    loadFailed: boolean,
  ): void {
    this.administrativeUnitOptions.set(options);
    this.administrativeUnitsLoadFailed.set(loadFailed);
    this.loadedAdministrativeUnitsDepartmentCode = loadFailed
      ? null
      : this.form.controls.conselleria.value;
    this.configureAdministrativeUnitControl();
  }

  selectConselleria(code: string | null): void {
    this.form.controls.administrativeUnit.reset(null, { emitEvent: false });
    this.administrativeUnitOptions.set([]);
    this.loadedAdministrativeUnitsDepartmentCode = null;
    this.administrativeUnitRequests.next(code);
  }

  retryAdministrativeUnits(): void {
    this.administrativeUnitRequests.next(this.form.controls.conselleria.value);
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
        this.configureAdministrativeUnitControl();
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
    }

    this.setEditing(section, true);
  }

  cancelEditing(section: ApplicationDetailSection): void {
    switch (section) {
      case 'general':
        this.form.reset(this.savedGeneralValue, { emitEvent: false });
        this.form.enable({ emitEvent: false });
        this.restoreAdministrativeUnitOptions();
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
    if (this.form.invalid || this.form.controls.administrativeUnit.disabled) {
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

  private finishUnchanged(section: ApplicationDetailSection): void {
    switch (section) {
      case 'general':
        this.form.enable({ emitEvent: false });
        this.configureAdministrativeUnitControl();
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
    }
    this.setEditing(section, false);
  }

  private resetState(): void {
    this.initializedApplicationId = null;
    this.application.set(null);
    this.informationSystemDbId.set(null);
    this.appSecurityId.set(null);
    this.systemDatabase.set(null);
    this.savedGeneralValue = { ...EMPTY_GENERAL_FORM_VALUE };
    this.administrativeUnitOptions.set([]);
    this.administrativeUnitsLoading.set(false);
    this.administrativeUnitsLoadFailed.set(false);
    this.loadedAdministrativeUnitsDepartmentCode = null;
    this.savedSystemsDatabasesValue = { ...EMPTY_SYSTEMS_DATABASES_VALUE };
    this.resetAccessibilityState();
    this.resetDevelopmentState();
    this.resetSecurityState();
    this.form.reset(this.savedGeneralValue, { emitEvent: false });
    this.systemsDatabasesForm.reset(this.savedSystemsDatabasesValue, {
      emitEvent: false,
    });
    this.enableViewForms();
    this.editingState.set(createEditingState());
  }

  private setApplication(response: ApplicationOutput): void {
    this.completenessRequestVersion++;
    this.completenessRefreshing.set(false);
    this.initializedApplicationId = response.id;
    this.application.set(this.applicationsService.toApplication(response));
    this.informationSystemDbId.set(response.appInformationSystemDbId);
    this.appSecurityId.set(response.appSecurityId ?? null);
    this.systemDatabase.set(null);
    this.administrativeUnitOptions.set([]);
    this.administrativeUnitsLoading.set(false);
    this.administrativeUnitsLoadFailed.set(false);
    this.loadedAdministrativeUnitsDepartmentCode = null;
    this.savedGeneralValue = this.toGeneralFormValue(response);
    this.savedSystemsDatabasesValue = { ...EMPTY_SYSTEMS_DATABASES_VALUE };
    this.resetAccessibilityState();
    this.resetDevelopmentState();
    this.resetSecurityState();
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
    this.form.reset(this.savedGeneralValue, { emitEvent: false });
    this.form.enable({ emitEvent: false });
    this.restoreAdministrativeUnitOptions();
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
              name: current?.administrativeUnit || value.administrativeUnit,
              parentCode: value.conselleria,
              level: null,
            }
          : null),
      department:
        enriched.department ??
        (value.conselleria
          ? {
              code: value.conselleria,
              name: current?.department || value.conselleria,
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
    this.configureEnsControls();
    this.configureMobileApplicationName(
      this.accessibilityForm.controls.mobileApplication.value,
      false,
    );
    this.configureAdministrativeUnitControl();
  }

  private configureAdministrativeUnitControl(): void {
    const departmentCode = this.form.controls.conselleria.value;
    const unavailable =
      !departmentCode ||
      this.administrativeUnitsLoading() ||
      this.administrativeUnitsLoadFailed() ||
      this.loadedAdministrativeUnitsDepartmentCode !== departmentCode;

    if (unavailable) {
      this.form.controls.administrativeUnit.disable({ emitEvent: false });
    } else {
      this.form.controls.administrativeUnit.enable({ emitEvent: false });
    }
  }

  private restoreAdministrativeUnitOptions(): void {
    const departmentCode = this.form.controls.conselleria.value;
    if (!departmentCode) {
      this.administrativeUnitOptions.set([]);
      this.loadedAdministrativeUnitsDepartmentCode = null;
      this.configureAdministrativeUnitControl();
      return;
    }

    if (
      this.loadedAdministrativeUnitsDepartmentCode === departmentCode &&
      !this.administrativeUnitsLoadFailed()
    ) {
      this.configureAdministrativeUnitControl();
      return;
    }

    this.administrativeUnitOptions.set([]);
    this.administrativeUnitRequests.next(departmentCode);
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
      conselleria: application.departmentCode ?? null,
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
