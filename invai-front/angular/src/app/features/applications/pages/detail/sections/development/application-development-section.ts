import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  LOCALE_ID,
  OnInit,
  computed,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { ConfirmationDialogComponent } from '@components/confirmation-dialog/confirmation-dialog.component';
import { CrudEntityDialogMode } from '@components/crud-entity-dialog/crud-entity-dialog';
import { isStructuredBadRequest } from '@core/models/api-error.model';
import { EnvironmentCatalogOption } from '@features/environments/environments.model';
import { RoleCatalogOption } from '@features/roles/roles.model';
import { TechnologyCatalogOption } from '@features/technologies/technologies.model';
import { ActionParams } from '@models/table.model';
import { PAGINATOR_ROWS } from '@shared/constants/table.constants';
import { localizedName } from '@shared/utils/localized-name.utils';
import { MessageService, PrimeIcons } from 'primeng/api';
import { Button } from 'primeng/button';
import { DatePicker } from 'primeng/datepicker';
import { Editor } from 'primeng/editor';
import { InputText } from 'primeng/inputtext';
import { Select } from 'primeng/select';
import { TableLazyLoadEvent } from 'primeng/table';
import { TooltipModule } from 'primeng/tooltip';
import { DatePickerPassThrough } from 'primeng/types/datepicker';
import { EMPTY, catchError, finalize } from 'rxjs';

import {
  ApplicationProviderDialog,
  ApplicationProvidersTable,
  ApplicationProviderTableAction,
  ApplicationTechnologyDialog,
  ApplicationTechnologiesTable,
  ApplicationTechnologyTableAction,
} from '../../../../components';
import {
  ApplicationDevelopmentResourcePageParams,
  DevelopmentLookupOutput,
  ApplicationProviderInput,
  ApplicationProviderOutput,
  ApplicationTechnologyInput,
  ApplicationTechnologyOutput,
  DevelopmentModality,
  DevelopmentStandardAdaption,
  SelectOption,
} from '../../../../applications.model';
import {
  createApplicationProviderForm,
  createApplicationTechnologyForm,
  formatLocalDateTime,
  parseLocalDateTime,
} from '../../../../forms/application-development-form.factory';
import { ApplicationProvidersService } from '../../../../services/application-providers.service';
import { ApplicationTechnologiesService } from '../../../../services/application-technologies.service';
import {
  APPLICATION_DETAIL_SAVE_ERROR_MESSAGE,
  APPLICATION_DETAIL_SAVE_ERROR_TITLE,
  APPLICATION_DETAIL_SECTION_SAVE_SUCCESS_MESSAGE,
  APPLICATION_DETAIL_SECTION_SAVE_SUCCESS_TITLE,
} from '../../application-detail.i18n';
import { ApplicationDetailState } from '../../application-detail-state';
import { ApplicationDetailSectionActions } from '../../components/application-detail-section-actions/application-detail-section-actions';
import { ApplicationDetailSectionLayout } from '../../components/application-detail-section-layout/application-detail-section-layout';
import {
  APPLICATION_DEVELOPMENT_ADD_PROVIDER_ARIA_LABEL,
  APPLICATION_DEVELOPMENT_ADD_TECHNOLOGY_ARIA_LABEL,
  APPLICATION_DEVELOPMENT_CODE_MAX_LENGTH_ERROR,
  APPLICATION_DEVELOPMENT_DATE_FORMAT,
  APPLICATION_DEVELOPMENT_DATE_PLACEHOLDER,
  APPLICATION_DEVELOPMENT_DOCUMENTATION_ARIA_LABEL,
  APPLICATION_DEVELOPMENT_DOCUMENTATION_LABEL,
  APPLICATION_DEVELOPMENT_DOCUMENTATION_PENDING,
  APPLICATION_DEVELOPMENT_DELETE_DIALOG_CANCEL_LABEL,
  APPLICATION_DEVELOPMENT_DELETE_DIALOG_CONFIRM_LABEL,
  APPLICATION_DEVELOPMENT_ENVIRONMENTS_LOAD_ERROR,
  APPLICATION_DEVELOPMENT_ERROR_TITLE,
  APPLICATION_DEVELOPMENT_LABELS,
  APPLICATION_DEVELOPMENT_LOAD_ERROR,
  APPLICATION_DEVELOPMENT_MODALITIES_LOAD_ERROR,
  APPLICATION_DEVELOPMENT_PENDING_TITLE,
  APPLICATION_DEVELOPMENT_PROVIDER_COLUMNS,
  APPLICATION_DEVELOPMENT_PROVIDER_CREATE_ERROR,
  APPLICATION_DEVELOPMENT_PROVIDER_CREATE_SUCCESS,
  APPLICATION_DEVELOPMENT_PROVIDER_DELETE_DIALOG_CANCEL_ARIA_LABEL,
  APPLICATION_DEVELOPMENT_PROVIDER_DELETE_DIALOG_CONFIRM_ARIA_LABEL,
  APPLICATION_DEVELOPMENT_PROVIDER_DELETE_DIALOG_MESSAGE,
  APPLICATION_DEVELOPMENT_PROVIDER_DELETE_DIALOG_TITLE,
  APPLICATION_DEVELOPMENT_PROVIDER_DELETE_ERROR,
  APPLICATION_DEVELOPMENT_PROVIDER_DELETE_SUCCESS,
  APPLICATION_DEVELOPMENT_PROVIDER_TITLE,
  APPLICATION_DEVELOPMENT_PROVIDER_UPDATE_ERROR,
  APPLICATION_DEVELOPMENT_PROVIDER_UPDATE_SUCCESS,
  APPLICATION_DEVELOPMENT_PROVIDERS_LOAD_ERROR,
  APPLICATION_DEVELOPMENT_REQUIRED_ERROR,
  APPLICATION_DEVELOPMENT_ROLES_LOAD_ERROR,
  APPLICATION_DEVELOPMENT_SECTION_TITLE,
  APPLICATION_DEVELOPMENT_SOURCE_CODE_ARIA_LABEL,
  APPLICATION_DEVELOPMENT_TECHNOLOGIES_LOAD_ERROR,
  APPLICATION_DEVELOPMENT_TECHNOLOGY_CREATE_ERROR,
  APPLICATION_DEVELOPMENT_TECHNOLOGY_CREATE_SUCCESS,
  APPLICATION_DEVELOPMENT_TECHNOLOGY_COLUMNS,
  APPLICATION_DEVELOPMENT_TECHNOLOGY_DELETE_DIALOG_CANCEL_ARIA_LABEL,
  APPLICATION_DEVELOPMENT_TECHNOLOGY_DELETE_DIALOG_CONFIRM_ARIA_LABEL,
  APPLICATION_DEVELOPMENT_TECHNOLOGY_DELETE_DIALOG_MESSAGE,
  APPLICATION_DEVELOPMENT_TECHNOLOGY_DELETE_DIALOG_TITLE,
  APPLICATION_DEVELOPMENT_TECHNOLOGY_DELETE_ERROR,
  APPLICATION_DEVELOPMENT_TECHNOLOGY_DELETE_SUCCESS,
  APPLICATION_DEVELOPMENT_TECHNOLOGY_TITLE,
  APPLICATION_DEVELOPMENT_TECHNOLOGY_UPDATE_ERROR,
  APPLICATION_DEVELOPMENT_TECHNOLOGY_UPDATE_SUCCESS,
  APPLICATION_DEVELOPMENT_TECHNOLOGY_OPTIONS_LOAD_ERROR,
  APPLICATION_DEVELOPMENT_STANDARD_ADAPTIONS_LOAD_ERROR,
  APPLICATION_DEVELOPMENT_URL_ERROR,
} from './application-development-section.i18n';
import {
  APPLICATION_DEVELOPMENT_RESOLVE_KEY,
  ApplicationDevelopmentResolvedData,
} from './application-development-section.resolver';

@Component({
  selector: 'app-application-development-section',
  standalone: true,
  imports: [
    ApplicationDetailSectionActions,
    ApplicationDetailSectionLayout,
    ApplicationProviderDialog,
    ApplicationProvidersTable,
    ApplicationTechnologyDialog,
    ApplicationTechnologiesTable,
    Button,
    ConfirmationDialogComponent,
    DatePicker,
    Editor,
    InputText,
    ReactiveFormsModule,
    Select,
    TooltipModule,
  ],
  templateUrl: './application-development-section.html',
  styleUrl: './application-development-section.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationDevelopmentSection implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly formBuilder = inject(FormBuilder);
  private readonly locale = inject(LOCALE_ID);
  private readonly destroyRef = inject(DestroyRef);
  private readonly messageService = inject(MessageService);
  private readonly providersService = inject(ApplicationProvidersService);
  private readonly technologiesService = inject(ApplicationTechnologiesService);
  private providerTableState: TableLazyLoadEvent = { first: 0, rows: PAGINATOR_ROWS };
  private technologyTableState: TableLazyLoadEvent = { first: 0, rows: PAGINATOR_ROWS };

  protected readonly detailState = inject(ApplicationDetailState);
  protected readonly sectionTitle = APPLICATION_DEVELOPMENT_SECTION_TITLE;
  protected readonly labels = APPLICATION_DEVELOPMENT_LABELS;
  protected readonly providerTitle = APPLICATION_DEVELOPMENT_PROVIDER_TITLE;
  protected readonly technologyTitle = APPLICATION_DEVELOPMENT_TECHNOLOGY_TITLE;
  protected readonly addProviderAriaLabel = APPLICATION_DEVELOPMENT_ADD_PROVIDER_ARIA_LABEL;
  protected readonly addTechnologyAriaLabel = APPLICATION_DEVELOPMENT_ADD_TECHNOLOGY_ARIA_LABEL;
  protected readonly providerColumns = APPLICATION_DEVELOPMENT_PROVIDER_COLUMNS;
  protected readonly technologyColumns = APPLICATION_DEVELOPMENT_TECHNOLOGY_COLUMNS;
  protected readonly documentationLabel = APPLICATION_DEVELOPMENT_DOCUMENTATION_LABEL;
  protected readonly documentationAriaLabel = APPLICATION_DEVELOPMENT_DOCUMENTATION_ARIA_LABEL;
  protected readonly sourceCodeAriaLabel = APPLICATION_DEVELOPMENT_SOURCE_CODE_ARIA_LABEL;
  protected readonly requiredError = APPLICATION_DEVELOPMENT_REQUIRED_ERROR;
  protected readonly urlError = APPLICATION_DEVELOPMENT_URL_ERROR;
  protected readonly codeMaxLengthError = APPLICATION_DEVELOPMENT_CODE_MAX_LENGTH_ERROR;
  protected readonly dateFormat = APPLICATION_DEVELOPMENT_DATE_FORMAT;
  protected readonly datePlaceholder = APPLICATION_DEVELOPMENT_DATE_PLACEHOLDER;
  protected readonly icons = PrimeIcons;
  protected readonly deleteDialogCancelLabel = APPLICATION_DEVELOPMENT_DELETE_DIALOG_CANCEL_LABEL;
  protected readonly deleteDialogConfirmLabel = APPLICATION_DEVELOPMENT_DELETE_DIALOG_CONFIRM_LABEL;
  protected readonly providerDeleteDialogTitle =
    APPLICATION_DEVELOPMENT_PROVIDER_DELETE_DIALOG_TITLE;
  protected readonly providerDeleteDialogCancelAriaLabel =
    APPLICATION_DEVELOPMENT_PROVIDER_DELETE_DIALOG_CANCEL_ARIA_LABEL;
  protected readonly providerDeleteDialogConfirmAriaLabel =
    APPLICATION_DEVELOPMENT_PROVIDER_DELETE_DIALOG_CONFIRM_ARIA_LABEL;
  protected readonly technologyDeleteDialogTitle =
    APPLICATION_DEVELOPMENT_TECHNOLOGY_DELETE_DIALOG_TITLE;
  protected readonly technologyDeleteDialogCancelAriaLabel =
    APPLICATION_DEVELOPMENT_TECHNOLOGY_DELETE_DIALOG_CANCEL_ARIA_LABEL;
  protected readonly technologyDeleteDialogConfirmAriaLabel =
    APPLICATION_DEVELOPMENT_TECHNOLOGY_DELETE_DIALOG_CONFIRM_ARIA_LABEL;
  protected readonly providers = this.detailState.providers;
  protected readonly technologies = this.detailState.technologies;
  protected readonly environmentOptions = signal<EnvironmentCatalogOption[]>([]);
  protected readonly roleOptions = signal<RoleCatalogOption[]>([]);
  protected readonly technologyOptions = signal<TechnologyCatalogOption[]>([]);
  protected readonly providerForm = createApplicationProviderForm(this.formBuilder);
  protected readonly technologyForm = createApplicationTechnologyForm(this.formBuilder);
  protected readonly providerDialogVisible = signal(false);
  protected readonly technologyDialogVisible = signal(false);
  protected readonly providerDialogMode = signal<CrudEntityDialogMode>('create');
  protected readonly technologyDialogMode = signal<CrudEntityDialogMode>('create');
  protected readonly selectedProvider = signal<ApplicationProviderOutput | null>(null);
  protected readonly selectedTechnology = signal<ApplicationTechnologyOutput | null>(null);
  protected readonly pendingProviderDelete = signal<ApplicationProviderOutput | null>(null);
  protected readonly pendingTechnologyDelete = signal<ApplicationTechnologyOutput | null>(null);
  protected readonly providerDeleteDialogVisible = signal(false);
  protected readonly technologyDeleteDialogVisible = signal(false);
  protected readonly providerFirst = signal(0);
  protected readonly technologyFirst = signal(0);
  protected readonly isProvidersLoading = signal(false);
  protected readonly isTechnologiesLoading = signal(false);
  protected readonly isSaving = signal(false);
  protected readonly isProviderSaving = signal(false);
  protected readonly isTechnologySaving = signal(false);
  protected readonly isProviderDeleting = signal(false);
  protected readonly isTechnologyDeleting = signal(false);
  protected readonly canEditResources = computed(() => {
    const appDevelopmentId = Number(this.detailState.development()?.id);
    return (
      this.detailState.canEdit() &&
      Number.isInteger(appDevelopmentId) &&
      appDevelopmentId > 0 &&
      !this.isSaving() &&
      !this.isProviderSaving() &&
      !this.isTechnologySaving() &&
      !this.isProviderDeleting() &&
      !this.isTechnologyDeleting()
    );
  });
  protected readonly canManageResources = computed(
    () => this.canEditResources() && this.detailState.isEditing('development'),
  );
  protected readonly showResourceActions = computed(
    () => this.detailState.canEdit() && this.detailState.isEditing('development'),
  );
  protected readonly providerDialogCanEdit = computed(
    () => this.canEditResources() && !this.selectedProvider()?.deletedAt,
  );
  protected readonly technologyDialogCanEdit = computed(
    () => this.canEditResources() && !this.selectedTechnology()?.deletedAt,
  );
  protected readonly providerDeleteDialogMessage = computed(() =>
    APPLICATION_DEVELOPMENT_PROVIDER_DELETE_DIALOG_MESSAGE(
      this.pendingProviderDelete()?.companyName ?? '',
    ),
  );
  protected readonly technologyDeleteDialogMessage = computed(() =>
    APPLICATION_DEVELOPMENT_TECHNOLOGY_DELETE_DIALOG_MESSAGE(
      this.pendingTechnologyDelete()?.technology?.name ?? '',
    ),
  );
  protected readonly displayedRoleOptions = computed(() =>
    this.withSelectedRole(this.roleOptions()),
  );
  protected readonly displayedTechnologyOptions = computed(() =>
    this.withSelectedTechnology(this.technologyOptions()),
  );
  private readonly modalityCatalog = signal<DevelopmentLookupOutput<DevelopmentModality>[]>([]);
  private readonly standardAdaptionCatalog = signal<
    DevelopmentLookupOutput<DevelopmentStandardAdaption>[]
  >([]);
  protected readonly modalityOptionsLoadFailed = signal(false);
  protected readonly standardAdaptionOptionsLoadFailed = signal(false);
  protected readonly modalityOptions = computed(() =>
    this.withCurrentLookupOption(
      this.modalityCatalog().map((item) => ({
        value: item.id,
        label: localizedName(item, this.locale),
      })),
      this.detailState.development()?.modality,
    ),
  );
  protected readonly standardAdaptionOptions = computed(() =>
    this.withCurrentLookupOption(
      this.standardAdaptionCatalog().map((item) => ({
        value: item.id,
        label: localizedName(item, this.locale),
      })),
      this.detailState.development()?.standardAdaption,
    ),
  );
  protected readonly displayedEnvironmentOptions = computed(() =>
    this.withCurrentEnvironment(this.environmentOptions()),
  );

  ngOnInit(): void {
    const resolvedData = this.route.snapshot.data[
      APPLICATION_DEVELOPMENT_RESOLVE_KEY
    ] as ApplicationDevelopmentResolvedData;
    this.detailState.initializeDevelopment(resolvedData.applicationId, resolvedData.development);
    this.detailState.initializeDevelopmentResources(
      {
        items: resolvedData.providersPage?.content ?? [],
        total: resolvedData.providersPage?.totalElements ?? 0,
      },
      {
        items: resolvedData.technologiesPage?.content ?? [],
        total: resolvedData.technologiesPage?.totalElements ?? 0,
      },
    );
    this.environmentOptions.set(resolvedData.environmentOptions);
    this.roleOptions.set(resolvedData.roleOptions);
    this.technologyOptions.set(resolvedData.technologyOptions);
    this.modalityCatalog.set(resolvedData.modalityOptions);
    this.standardAdaptionCatalog.set(resolvedData.standardAdaptionOptions);
    this.modalityOptionsLoadFailed.set(resolvedData.modalityOptionsLoadFailed);
    this.standardAdaptionOptionsLoadFailed.set(resolvedData.standardAdaptionOptionsLoadFailed);
    this.enforceUnavailableCatalogControls();

    if (resolvedData.developmentLoadFailed) {
      this.showError(APPLICATION_DEVELOPMENT_LOAD_ERROR);
    }
    if (resolvedData.providersLoadFailed) {
      this.showError(APPLICATION_DEVELOPMENT_PROVIDERS_LOAD_ERROR);
    }
    if (resolvedData.technologiesLoadFailed) {
      this.showError(APPLICATION_DEVELOPMENT_TECHNOLOGIES_LOAD_ERROR);
    }
    if (resolvedData.environmentOptionsLoadFailed) {
      this.showError(APPLICATION_DEVELOPMENT_ENVIRONMENTS_LOAD_ERROR);
    }
    if (resolvedData.roleOptionsLoadFailed) {
      this.showError(APPLICATION_DEVELOPMENT_ROLES_LOAD_ERROR);
    }
    if (resolvedData.technologyOptionsLoadFailed) {
      this.showError(APPLICATION_DEVELOPMENT_TECHNOLOGY_OPTIONS_LOAD_ERROR);
    }
    if (resolvedData.modalityOptionsLoadFailed) {
      this.showError(APPLICATION_DEVELOPMENT_MODALITIES_LOAD_ERROR);
    }
    if (resolvedData.standardAdaptionOptionsLoadFailed) {
      this.showError(APPLICATION_DEVELOPMENT_STANDARD_ADAPTIONS_LOAD_ERROR);
    }
  }

  protected startEditing(): void {
    this.detailState.startEditing('development');
    this.enforceUnavailableCatalogControls();
  }

  private enforceUnavailableCatalogControls(): void {
    if (this.modalityOptionsLoadFailed()) {
      this.detailState.developmentForm.controls.modality.disable({ emitEvent: false });
    }
    if (this.standardAdaptionOptionsLoadFailed()) {
      this.detailState.developmentForm.controls.standardAdaption.disable({ emitEvent: false });
    }
  }

  protected cancelEditing(): void {
    this.detailState.cancelEditing('development');
  }

  protected save(): void {
    if (this.isSaving()) return;

    this.isSaving.set(true);
    this.detailState
      .save('development')
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (result) => {
          this.isSaving.set(false);
          if (result.status !== 'saved') return;

          this.messageService.add({
            severity: 'success',
            summary: APPLICATION_DETAIL_SECTION_SAVE_SUCCESS_TITLE,
            detail: APPLICATION_DETAIL_SECTION_SAVE_SUCCESS_MESSAGE(this.sectionTitle),
          });
        },
        error: (error: unknown) => {
          this.isSaving.set(false);
          if (isStructuredBadRequest(error)) return;

          this.messageService.add({
            severity: 'error',
            summary: APPLICATION_DETAIL_SAVE_ERROR_TITLE,
            detail: APPLICATION_DETAIL_SAVE_ERROR_MESSAGE,
          });
        },
      });
  }

  protected isInvalid(control: { invalid: boolean; dirty: boolean; touched: boolean }): boolean {
    return control.invalid && (control.dirty || control.touched);
  }

  protected sourceCodeUrl(): string | null {
    const control = this.detailState.developmentForm.controls.code;
    const validationErrors = control.validator?.(control) ?? null;
    return validationErrors === null ? control.value.trim() : null;
  }

  protected environmentLabel(): string {
    const value = this.detailState.developmentForm.controls.environment.value;
    return this.displayedEnvironmentOptions().find((option) => option.id === value)?.label ?? '-';
  }

  protected selectOptionLabel<T>(options: SelectOption<T>[], value: T | null): string {
    return options.find((option) => option.value === value)?.label ?? '-';
  }

  protected formattedRevisionDate(): string {
    const value = this.detailState.developmentForm.controls.revisionDate.value;
    return value ? new Intl.DateTimeFormat(this.locale).format(value) : '-';
  }

  protected datePassThrough(errorId: string, invalid: boolean): DatePickerPassThrough {
    return {
      pcInputText: {
        root: {
          'aria-describedby': invalid ? errorId : null,
          'aria-invalid': String(invalid),
        },
      },
    };
  }

  protected onDocumentation(): void {
    this.messageService.add({
      severity: 'info',
      summary: APPLICATION_DEVELOPMENT_PENDING_TITLE,
      detail: APPLICATION_DEVELOPMENT_DOCUMENTATION_PENDING,
    });
  }

  protected onProvidersPageChange(event: TableLazyLoadEvent): void {
    this.providerTableState = event;
    this.providerFirst.set(event.first ?? 0);
    this.loadProviders();
  }

  protected onTechnologiesPageChange(event: TableLazyLoadEvent): void {
    this.technologyTableState = event;
    this.technologyFirst.set(event.first ?? 0);
    this.loadTechnologies();
  }

  protected onProviderTableAction(event: ActionParams<ApplicationProviderOutput>): void {
    switch (event.action) {
      case ApplicationProviderTableAction.View:
        this.openExistingProvider(
          event.params,
          this.canManageResources() && !event.params.deletedAt ? 'edit' : 'view',
        );
        break;
      case ApplicationProviderTableAction.Edit:
        if (this.canManageResources()) {
          this.openExistingProvider(event.params, 'edit');
        }
        break;
      case ApplicationProviderTableAction.Delete:
        this.requestProviderDelete(event.params);
        break;
    }
  }

  protected onTechnologyTableAction(event: ActionParams<ApplicationTechnologyOutput>): void {
    switch (event.action) {
      case ApplicationTechnologyTableAction.View:
        this.openExistingTechnology(
          event.params,
          this.canManageResources() && !event.params.deletedAt ? 'edit' : 'view',
        );
        break;
      case ApplicationTechnologyTableAction.Edit:
        if (this.canManageResources()) {
          this.openExistingTechnology(event.params, 'edit');
        }
        break;
      case ApplicationTechnologyTableAction.Delete:
        this.requestTechnologyDelete(event.params);
        break;
    }
  }

  protected openProviderDialog(): void {
    if (!this.canManageResources()) return;
    this.selectedProvider.set(null);
    this.prepareProviderForm(null, 'create');
    this.providerDialogMode.set('create');
    this.providerDialogVisible.set(true);
  }

  protected closeProviderDialog(): void {
    if (this.isProviderSaving() || this.isProviderDeleting()) return;
    this.providerDialogVisible.set(false);
    this.selectedProvider.set(null);
  }

  protected startProviderEdit(): void {
    const provider = this.selectedProvider();
    if (
      this.providerDialogMode() !== 'view' ||
      !provider ||
      provider.deletedAt ||
      !this.ensureDevelopmentEditing()
    ) {
      return;
    }
    this.providerForm.enable({ emitEvent: false });
    this.providerDialogMode.set('edit');
  }

  protected cancelProviderEdit(): void {
    const provider = this.selectedProvider();
    if (this.providerDialogMode() !== 'edit' || !provider || this.isProviderSaving()) {
      return;
    }
    this.prepareProviderForm(provider, 'view');
    this.providerDialogMode.set('view');
  }

  protected requestProviderDelete(
    provider: ApplicationProviderOutput | null = this.selectedProvider(),
  ): void {
    if (
      !provider ||
      provider.deletedAt ||
      !this.canManageResources() ||
      this.isProviderDeleting()
    ) {
      return;
    }
    this.pendingProviderDelete.set(provider);
    this.providerDeleteDialogVisible.set(true);
  }

  protected closeProviderDeleteDialog(): void {
    this.providerDeleteDialogVisible.set(false);
    if (!this.isProviderDeleting()) this.pendingProviderDelete.set(null);
  }

  protected confirmProviderDelete(): void {
    const provider = this.pendingProviderDelete();
    if (!provider || !this.canManageResources() || this.isProviderDeleting()) {
      return;
    }

    this.providerDeleteDialogVisible.set(false);
    this.isProviderDeleting.set(true);
    this.providersService
      .delete(provider.id)
      .pipe(
        finalize(() => this.isProviderDeleting.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.pendingProviderDelete.set(null);
          if (this.selectedProvider()?.id === provider.id) {
            this.providerDialogVisible.set(false);
            this.selectedProvider.set(null);
          }
          this.showSuccess(APPLICATION_DEVELOPMENT_PROVIDER_DELETE_SUCCESS);
          this.refreshProvidersAfterMutation();
        },
        error: (error: unknown) => {
          this.pendingProviderDelete.set(null);
          this.handleMutationError(error, APPLICATION_DEVELOPMENT_PROVIDER_DELETE_ERROR);
        },
      });
  }

  protected submitProvider(): void {
    const mode = this.providerDialogMode();
    const selected = this.selectedProvider();
    if (
      mode === 'view' ||
      !this.canManageResources() ||
      this.isProviderSaving() ||
      (mode === 'edit' && !selected)
    ) {
      return;
    }
    if (this.providerForm.invalid) {
      this.providerForm.markAllAsTouched();
      return;
    }

    const appDevelopmentId = Number(this.detailState.development()?.id);
    const value = this.providerForm.getRawValue();
    const payload: ApplicationProviderInput = {
      appDevelopmentId,
      companyName: value.companyName.trim(),
      roleId: value.roleId,
      startDate: formatLocalDateTime(value.startDate),
      expireDate: formatLocalDateTime(value.expireDate),
    };
    const request =
      mode === 'create'
        ? this.providersService.create(payload)
        : this.providersService.update(selected!.id, payload);

    this.isProviderSaving.set(true);
    request
      .pipe(
        finalize(() => this.isProviderSaving.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.providerDialogVisible.set(false);
          this.selectedProvider.set(null);
          this.showSuccess(
            mode === 'create'
              ? APPLICATION_DEVELOPMENT_PROVIDER_CREATE_SUCCESS
              : APPLICATION_DEVELOPMENT_PROVIDER_UPDATE_SUCCESS,
          );
          this.refreshProvidersAfterMutation();
        },
        error: (error: unknown) =>
          this.handleMutationError(
            error,
            mode === 'create'
              ? APPLICATION_DEVELOPMENT_PROVIDER_CREATE_ERROR
              : APPLICATION_DEVELOPMENT_PROVIDER_UPDATE_ERROR,
          ),
      });
  }

  protected openTechnologyDialog(): void {
    if (!this.canManageResources()) return;
    this.selectedTechnology.set(null);
    this.prepareTechnologyForm(null);
    this.technologyDialogMode.set('create');
    this.technologyDialogVisible.set(true);
  }

  protected closeTechnologyDialog(): void {
    if (this.isTechnologySaving() || this.isTechnologyDeleting()) return;
    this.technologyDialogVisible.set(false);
    this.selectedTechnology.set(null);
  }

  protected startTechnologyEdit(): void {
    const technology = this.selectedTechnology();
    if (
      this.technologyDialogMode() !== 'view' ||
      !technology ||
      technology.deletedAt ||
      !this.ensureDevelopmentEditing()
    ) {
      return;
    }
    this.technologyDialogMode.set('edit');
  }

  protected cancelTechnologyEdit(): void {
    const technology = this.selectedTechnology();
    if (this.technologyDialogMode() !== 'edit' || !technology || this.isTechnologySaving()) {
      return;
    }
    this.prepareTechnologyForm(technology);
    this.technologyDialogMode.set('view');
  }

  protected requestTechnologyDelete(
    technology: ApplicationTechnologyOutput | null = this.selectedTechnology(),
  ): void {
    if (
      !technology ||
      technology.deletedAt ||
      !this.canManageResources() ||
      this.isTechnologyDeleting()
    ) {
      return;
    }
    this.pendingTechnologyDelete.set(technology);
    this.technologyDeleteDialogVisible.set(true);
  }

  protected closeTechnologyDeleteDialog(): void {
    this.technologyDeleteDialogVisible.set(false);
    if (!this.isTechnologyDeleting()) this.pendingTechnologyDelete.set(null);
  }

  protected confirmTechnologyDelete(): void {
    const technology = this.pendingTechnologyDelete();
    if (!technology || !this.canManageResources() || this.isTechnologyDeleting()) {
      return;
    }

    this.technologyDeleteDialogVisible.set(false);
    this.isTechnologyDeleting.set(true);
    this.technologiesService
      .delete(technology.id)
      .pipe(
        finalize(() => this.isTechnologyDeleting.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.pendingTechnologyDelete.set(null);
          if (this.selectedTechnology()?.id === technology.id) {
            this.technologyDialogVisible.set(false);
            this.selectedTechnology.set(null);
          }
          this.showSuccess(APPLICATION_DEVELOPMENT_TECHNOLOGY_DELETE_SUCCESS);
          this.refreshTechnologiesAfterMutation();
        },
        error: (error: unknown) => {
          this.pendingTechnologyDelete.set(null);
          this.handleMutationError(error, APPLICATION_DEVELOPMENT_TECHNOLOGY_DELETE_ERROR);
        },
      });
  }

  protected submitTechnology(): void {
    const mode = this.technologyDialogMode();
    const selected = this.selectedTechnology();
    if (
      mode === 'view' ||
      !this.canManageResources() ||
      this.isTechnologySaving() ||
      (mode === 'edit' && !selected)
    ) {
      return;
    }
    if (this.technologyForm.invalid) {
      this.technologyForm.markAllAsTouched();
      return;
    }

    const appDevelopmentId = Number(this.detailState.development()?.id);
    const value = this.technologyForm.getRawValue();
    const payload: ApplicationTechnologyInput = {
      appDevelopmentId,
      layerId: value.layerId as number,
      technologyId: value.technologyId as number,
      version: value.version.trim(),
      architecture: value.architecture.trim(),
    };
    const request =
      mode === 'create'
        ? this.technologiesService.create(payload)
        : this.technologiesService.update(selected!.id, payload);

    this.isTechnologySaving.set(true);
    request
      .pipe(
        finalize(() => this.isTechnologySaving.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.technologyDialogVisible.set(false);
          this.selectedTechnology.set(null);
          this.showSuccess(
            mode === 'create'
              ? APPLICATION_DEVELOPMENT_TECHNOLOGY_CREATE_SUCCESS
              : APPLICATION_DEVELOPMENT_TECHNOLOGY_UPDATE_SUCCESS,
          );
          this.refreshTechnologiesAfterMutation();
        },
        error: (error: unknown) =>
          this.handleMutationError(
            error,
            mode === 'create'
              ? APPLICATION_DEVELOPMENT_TECHNOLOGY_CREATE_ERROR
              : APPLICATION_DEVELOPMENT_TECHNOLOGY_UPDATE_ERROR,
          ),
      });
  }

  private openExistingProvider(
    provider: ApplicationProviderOutput,
    mode: Exclude<CrudEntityDialogMode, 'create'>,
  ): void {
    if (mode === 'edit' && (provider.deletedAt || !this.canManageResources())) {
      return;
    }
    this.selectedProvider.set(provider);
    this.prepareProviderForm(provider, mode);
    this.providerDialogMode.set(mode);
    this.providerDialogVisible.set(true);
  }

  private ensureDevelopmentEditing(): boolean {
    if (!this.canEditResources()) return false;

    if (!this.detailState.isEditing('development')) {
      this.detailState.startEditing('development');
    }

    return this.canManageResources();
  }

  private openExistingTechnology(
    technology: ApplicationTechnologyOutput,
    mode: Exclude<CrudEntityDialogMode, 'create'>,
  ): void {
    if (mode === 'edit' && (technology.deletedAt || !this.canManageResources())) {
      return;
    }
    this.selectedTechnology.set(technology);
    this.prepareTechnologyForm(technology);
    this.technologyDialogMode.set(mode);
    this.technologyDialogVisible.set(true);
  }

  private prepareProviderForm(
    provider: ApplicationProviderOutput | null,
    mode: CrudEntityDialogMode,
  ): void {
    this.providerForm.enable({ emitEvent: false });
    this.providerForm.reset(
      {
        companyName: provider?.companyName ?? '',
        roleId: provider?.role?.id ?? null,
        startDate: parseLocalDateTime(provider?.startDate),
        expireDate: parseLocalDateTime(provider?.expireDate),
      },
      { emitEvent: false },
    );
  }

  private prepareTechnologyForm(technology: ApplicationTechnologyOutput | null): void {
    this.technologyForm.reset(
      {
        layerId: technology?.layer?.id ?? null,
        technologyId: technology?.technology?.id ?? null,
        version: technology?.version ?? '',
        architecture: technology?.architecture ?? '',
      },
      { emitEvent: false },
    );
  }

  private loadProviders(): void {
    const params = this.toPageParams(this.providerTableState);
    if (!params) return;

    this.isProvidersLoading.set(true);
    this.providersService
      .getPage(params)
      .pipe(
        catchError(() => {
          this.detailState.setProviderPage({ items: [], total: 0 });
          this.showError(APPLICATION_DEVELOPMENT_PROVIDERS_LOAD_ERROR);
          return EMPTY;
        }),
        finalize(() => this.isProvidersLoading.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((page) => {
        this.detailState.setProviderPage({
          items: page.content,
          total: page.totalElements,
        });
      });
  }

  private loadTechnologies(): void {
    const params = this.toPageParams(this.technologyTableState);
    if (!params) return;

    this.isTechnologiesLoading.set(true);
    this.technologiesService
      .getPage(params)
      .pipe(
        catchError(() => {
          this.detailState.setTechnologyPage({ items: [], total: 0 });
          this.showError(APPLICATION_DEVELOPMENT_TECHNOLOGIES_LOAD_ERROR);
          return EMPTY;
        }),
        finalize(() => this.isTechnologiesLoading.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((page) => {
        this.detailState.setTechnologyPage({
          items: page.content,
          total: page.totalElements,
        });
      });
  }

  private refreshProvidersAfterMutation(): void {
    this.providerTableState = { ...this.providerTableState, first: 0 };
    this.providerFirst.set(0);
    this.loadProviders();
  }

  private refreshTechnologiesAfterMutation(): void {
    this.technologyTableState = { ...this.technologyTableState, first: 0 };
    this.technologyFirst.set(0);
    this.loadTechnologies();
  }

  private toPageParams(event: TableLazyLoadEvent): ApplicationDevelopmentResourcePageParams | null {
    const appDevelopmentId = Number(this.detailState.development()?.id);
    if (!Number.isInteger(appDevelopmentId) || appDevelopmentId <= 0) return null;

    const first = event.first ?? 0;
    const size = event.rows ?? PAGINATOR_ROWS;
    const sortField = Array.isArray(event.sortField) ? event.sortField[0] : event.sortField;
    const direction = event.sortOrder === -1 ? 'desc' : event.sortOrder === 1 ? 'asc' : null;

    return {
      appDevelopmentId,
      page: Math.floor(first / size),
      size,
      sort: sortField && direction ? `${sortField},${direction}` : 'id,asc',
    };
  }

  private withCurrentEnvironment(options: EnvironmentCatalogOption[]): EnvironmentCatalogOption[] {
    const current = this.detailState.development()?.environment;
    if (!current || options.some((option) => option.id === current.id)) return options;

    return [
      ...options,
      {
        id: current.id,
        code: current.code?.trim() || `#${current.id}`,
        label: localizedName(current, this.locale, current.code || `#${current.id}`),
      },
    ];
  }

  private withCurrentLookupOption<TId extends DevelopmentModality | DevelopmentStandardAdaption>(
    options: SelectOption<TId>[],
    current: { id: TId; name: string | null; nameEs: string | null } | undefined,
  ): SelectOption<TId>[] {
    if (!current || options.some((option) => option.value === current.id)) {
      return options;
    }

    return [
      ...options,
      {
        value: current.id,
        label: localizedName(current, this.locale, `#${current.id}`),
      },
    ];
  }

  private showError(detail: string): void {
    this.messageService.add({
      severity: 'error',
      summary: APPLICATION_DEVELOPMENT_ERROR_TITLE,
      detail,
    });
  }

  private withSelectedRole(options: RoleCatalogOption[]): RoleCatalogOption[] {
    const current = this.selectedProvider()?.role;
    if (!current || options.some((option) => option.id === current.id)) {
      return options;
    }

    return [
      ...options,
      {
        id: current.id,
        label: localizedName(current, this.locale, current.name?.trim() || `#${current.id}`),
      },
    ];
  }

  private withSelectedTechnology(options: TechnologyCatalogOption[]): TechnologyCatalogOption[] {
    const current = this.selectedTechnology();
    const technology = current?.technology;
    if (!current || !technology || options.some((option) => option.id === technology.id)) {
      return options;
    }

    return [
      ...options,
      {
        id: technology.id,
        label: technology.name?.trim() || `#${technology.id}`,
        layerId: current.layer.id,
        layerLabel: current.layer.name?.trim() || `#${current.layer.id}`,
      },
    ];
  }

  private showSuccess(detail: string): void {
    this.messageService.add({
      severity: 'success',
      summary: APPLICATION_DETAIL_SECTION_SAVE_SUCCESS_TITLE,
      detail,
    });
  }

  private handleMutationError(error: unknown, detail: string): void {
    if (!isStructuredBadRequest(error)) this.showError(detail);
  }
}
