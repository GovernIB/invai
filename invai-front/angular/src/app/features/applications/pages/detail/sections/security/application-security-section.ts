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
import { ActionParams, PaginatedList } from '@models/table.model';
import { SpringPage } from '@models/page.model';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';
import { PAGINATOR_ROWS } from '@shared/constants/table.constants';
import { localizedName } from '@shared/utils/localized-name.utils';
import { MessageService, PrimeIcons } from 'primeng/api';
import { Button } from 'primeng/button';
import { DatePicker } from 'primeng/datepicker';
import { Editor } from 'primeng/editor';
import { Select } from 'primeng/select';
import { TableLazyLoadEvent } from 'primeng/table';
import { TooltipModule } from 'primeng/tooltip';
import { DatePickerPassThrough } from 'primeng/types/datepicker';
import { EMPTY, Observable, catchError, finalize } from 'rxjs';

import {
  ApplicationSecurityMeasureInput,
  ApplicationSecurityMeasureOutput,
  ApplicationSecurityResourceOutput,
  ApplicationSecurityRiskInput,
  ApplicationSecurityRiskOutput,
  ApplicationSecurityRoleOutput,
  ApplicationWebContextInput,
  ApplicationWebContextOutput,
  SecurityCatalogItem,
  SelectOption,
} from '../../../../applications.model';
import {
  ApplicationSecurityResourceFormGroup,
  ApplicationSecurityResourceKind,
  configureApplicationSecurityResourceForm,
  createApplicationSecurityResourceForm,
} from '../../../../forms/application-security-form.factory';
import {
  ApplicationSecurityMeasuresService,
  ApplicationSecurityRisksService,
  ApplicationSecurityRolesService,
  ApplicationWebContextsService,
} from '../../../../services/application-security.service';
import {
  APPLICATION_DETAIL_SAVE_ERROR_MESSAGE,
  APPLICATION_DETAIL_SAVE_ERROR_TITLE,
  APPLICATION_DETAIL_SECTION_SAVE_SUCCESS_MESSAGE,
  APPLICATION_DETAIL_SECTION_SAVE_SUCCESS_TITLE,
} from '../../application-detail.i18n';
import { ApplicationDetailState } from '../../application-detail-state';
import { ApplicationDetailSectionActions } from '../../components/application-detail-section-actions/application-detail-section-actions';
import { ApplicationDetailSectionLayout } from '../../components/application-detail-section-layout/application-detail-section-layout';
import { ApplicationSecurityResourceDialog } from './application-security-resource-dialog';
import {
  ApplicationSecurityResourceTable,
  ApplicationSecurityTableAction,
  ApplicationSecurityTableKind,
} from './application-security-resource-table';
import {
  APPLICATION_SECURITY_ADD_ARIA_LABELS,
  APPLICATION_SECURITY_ANCHOR_REQUIRED,
  APPLICATION_SECURITY_DATE_FORMAT,
  APPLICATION_SECURITY_DATE_PLACEHOLDER,
  APPLICATION_SECURITY_DELETE_DIALOG,
  APPLICATION_SECURITY_DOCUMENTATION_ARIA_LABEL,
  APPLICATION_SECURITY_DOCUMENTATION_LABEL,
  APPLICATION_SECURITY_DOCUMENTATION_PENDING,
  APPLICATION_SECURITY_ENS_TITLE,
  APPLICATION_SECURITY_ERROR_TITLE,
  APPLICATION_SECURITY_INCONSISTENT_MESSAGE,
  APPLICATION_SECURITY_INCONSISTENT_TITLE,
  APPLICATION_SECURITY_LABELS,
  APPLICATION_SECURITY_LOAD_ERROR,
  APPLICATION_SECURITY_MEASURE_COLUMNS,
  APPLICATION_SECURITY_MEASURES_TITLE,
  APPLICATION_SECURITY_PENDING_TITLE,
  APPLICATION_SECURITY_RESOURCE_DELETE_ERROR,
  APPLICATION_SECURITY_RESOURCE_DELETE_SUCCESS,
  APPLICATION_SECURITY_RESOURCE_SAVE_ERROR,
  APPLICATION_SECURITY_RESOURCE_SAVE_SUCCESS,
  APPLICATION_SECURITY_RISK_COLUMNS,
  APPLICATION_SECURITY_RISKS_TITLE,
  APPLICATION_SECURITY_ROLE_COLUMNS,
  APPLICATION_SECURITY_ROLES_TITLE,
  APPLICATION_SECURITY_ROLE_TEMPORARY_NOTE,
  APPLICATION_SECURITY_SECTION_TITLE,
  APPLICATION_SECURITY_SUCCESS_TITLE,
  APPLICATION_SECURITY_WEB_CONTEXT_COLUMNS,
  APPLICATION_SECURITY_WEB_CONTEXTS_TITLE,
  APPLICATION_SECURITY_DIMENSIONS_TITLE,
} from './application-security-section.i18n';
import {
  APPLICATION_SECURITY_RESOLVE_KEY,
  ApplicationSecurityResolvedData,
} from './application-security-section.resolver';

type MutableSecurityResource =
  ApplicationWebContextOutput | ApplicationSecurityRiskOutput | ApplicationSecurityMeasureOutput;

@Component({
  selector: 'app-application-security-section',
  standalone: true,
  imports: [
    ApplicationDetailSectionActions,
    ApplicationDetailSectionLayout,
    ApplicationSecurityResourceDialog,
    ApplicationSecurityResourceTable,
    Button,
    ConfirmationDialogComponent,
    DatePicker,
    Editor,
    ReactiveFormsModule,
    Select,
    TooltipModule,
  ],
  templateUrl: './application-security-section.html',
  styleUrl: './application-security-section.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationSecuritySection implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly formBuilder = inject(FormBuilder);
  private readonly locale = inject(LOCALE_ID);
  private readonly destroyRef = inject(DestroyRef);
  private readonly messageService = inject(MessageService);
  private readonly rolesService = inject(ApplicationSecurityRolesService);
  private readonly webContextsService = inject(ApplicationWebContextsService);
  private readonly risksService = inject(ApplicationSecurityRisksService);
  private readonly measuresService = inject(ApplicationSecurityMeasuresService);
  private readonly tableStates: Record<ApplicationSecurityTableKind, TableLazyLoadEvent> = {
    role: { first: 0, rows: PAGINATOR_ROWS },
    'web-context': { first: 0, rows: PAGINATOR_ROWS },
    risk: { first: 0, rows: PAGINATOR_ROWS },
    measure: { first: 0, rows: PAGINATOR_ROWS },
  };

  protected readonly detailState = inject(ApplicationDetailState);
  protected readonly icons = PrimeIcons;
  protected readonly sectionTitle = APPLICATION_SECURITY_SECTION_TITLE;
  protected readonly rolesTitle = APPLICATION_SECURITY_ROLES_TITLE;
  protected readonly webContextsTitle = APPLICATION_SECURITY_WEB_CONTEXTS_TITLE;
  protected readonly ensTitle = APPLICATION_SECURITY_ENS_TITLE;
  protected readonly dimensionsTitle = APPLICATION_SECURITY_DIMENSIONS_TITLE;
  protected readonly risksTitle = APPLICATION_SECURITY_RISKS_TITLE;
  protected readonly measuresTitle = APPLICATION_SECURITY_MEASURES_TITLE;
  protected readonly labels = APPLICATION_SECURITY_LABELS;
  protected readonly roleTemporaryNote = APPLICATION_SECURITY_ROLE_TEMPORARY_NOTE;
  protected readonly inconsistentTitle = APPLICATION_SECURITY_INCONSISTENT_TITLE;
  protected readonly inconsistentMessage = APPLICATION_SECURITY_INCONSISTENT_MESSAGE;
  protected readonly anchorRequired = APPLICATION_SECURITY_ANCHOR_REQUIRED;
  protected readonly documentationLabel = APPLICATION_SECURITY_DOCUMENTATION_LABEL;
  protected readonly documentationAriaLabel = APPLICATION_SECURITY_DOCUMENTATION_ARIA_LABEL;
  protected readonly dateFormat = APPLICATION_SECURITY_DATE_FORMAT;
  protected readonly datePlaceholder = APPLICATION_SECURITY_DATE_PLACEHOLDER;
  protected readonly roleColumns = APPLICATION_SECURITY_ROLE_COLUMNS;
  protected readonly webContextColumns = APPLICATION_SECURITY_WEB_CONTEXT_COLUMNS;
  protected readonly riskColumns = APPLICATION_SECURITY_RISK_COLUMNS;
  protected readonly measureColumns = APPLICATION_SECURITY_MEASURE_COLUMNS;
  protected readonly addAriaLabels = APPLICATION_SECURITY_ADD_ARIA_LABELS;
  protected readonly deleteDialog = APPLICATION_SECURITY_DELETE_DIALOG;

  protected readonly roles = signal<PaginatedList<ApplicationSecurityRoleOutput>>({
    items: [],
    total: 0,
  });
  protected readonly webContexts = signal<PaginatedList<ApplicationWebContextOutput>>({
    items: [],
    total: 0,
  });
  protected readonly risks = signal<PaginatedList<ApplicationSecurityRiskOutput>>({
    items: [],
    total: 0,
  });
  protected readonly measures = signal<PaginatedList<ApplicationSecurityMeasureOutput>>({
    items: [],
    total: 0,
  });
  protected readonly roleFirst = signal(0);
  protected readonly webContextFirst = signal(0);
  protected readonly riskFirst = signal(0);
  protected readonly measureFirst = signal(0);
  protected readonly rolesLoading = signal(false);
  protected readonly webContextsLoading = signal(false);
  protected readonly risksLoading = signal(false);
  protected readonly measuresLoading = signal(false);
  protected readonly isSaving = signal(false);
  protected readonly isResourceSaving = signal(false);
  protected readonly isResourceDeleting = signal(false);

  private readonly securityLevelCatalog = signal<SecurityCatalogItem[]>([]);
  private readonly identityProviderCatalog = signal<SecurityCatalogItem[]>([]);
  private readonly ensSubjectCatalog = signal<SecurityCatalogItem[]>([]);
  private readonly personalDataProcessingCatalog = signal<SecurityCatalogItem[]>([]);
  private readonly webContextCatalog = signal<SecurityCatalogItem[]>([]);
  private readonly fieldCatalog = signal<SecurityCatalogItem[]>([]);
  private readonly measureTypeCatalog = signal<SecurityCatalogItem[]>([]);
  private readonly ensRequirementCatalog = signal<SecurityCatalogItem[]>([]);

  protected readonly securityLevelOptions = computed(() =>
    this.toOptions(this.securityLevelCatalog()),
  );
  protected readonly identityProviderOptions = computed(() =>
    this.toOptions(this.identityProviderCatalog()),
  );
  protected readonly ensSubjectOptions = computed(() => this.toOptions(this.ensSubjectCatalog()));
  protected readonly personalDataProcessingOptions = computed(() =>
    this.toOptions(this.personalDataProcessingCatalog()),
  );
  protected readonly webContextOptions = computed(() => this.toOptions(this.webContextCatalog()));
  protected readonly fieldOptions = computed(() => this.toOptions(this.fieldCatalog()));
  protected readonly measureTypeOptions = computed(() => this.toOptions(this.measureTypeCatalog()));
  protected readonly ensRequirementOptions = computed(() =>
    this.toOptions(this.ensRequirementCatalog()),
  );

  protected readonly resourceForm: ApplicationSecurityResourceFormGroup =
    createApplicationSecurityResourceForm(this.formBuilder);
  protected readonly resourceDialogVisible = signal(false);
  protected readonly resourceDialogMode = signal<CrudEntityDialogMode>('create');
  protected readonly resourceKind = signal<ApplicationSecurityResourceKind>('web-context');
  protected readonly selectedResource = signal<MutableSecurityResource | null>(null);
  protected readonly pendingDelete = signal<MutableSecurityResource | null>(null);
  protected readonly deleteDialogVisible = signal(false);

  protected readonly canManageResources = computed(
    () =>
      this.detailState.canEdit() &&
      this.detailState.isEditing('security') &&
      this.detailState.appSecurityId() != null &&
      !this.isSaving() &&
      !this.isResourceSaving() &&
      !this.isResourceDeleting(),
  );
  protected readonly showResourceActions = computed(
    () => this.detailState.canEdit() && this.detailState.isEditing('security'),
  );
  protected readonly resourceDialogCanEdit = computed(
    () => this.canManageResources() && !this.selectedResource()?.deletedAt,
  );

  ngOnInit(): void {
    const resolved = this.route.snapshot.data[
      APPLICATION_SECURITY_RESOLVE_KEY
    ] as ApplicationSecurityResolvedData;
    this.detailState.initializeSecurity(
      resolved.applicationId,
      resolved.security,
      resolved.classifications,
      resolved.failures.classifications,
    );
    this.roles.set(this.toList(resolved.rolesPage));
    this.webContexts.set(this.toList(resolved.webContextsPage));
    this.risks.set(this.toList(resolved.risksPage));
    this.measures.set(this.toList(resolved.measuresPage));
    this.securityLevelCatalog.set(resolved.options.securityLevels);
    this.identityProviderCatalog.set(resolved.options.identityProviders);
    this.ensSubjectCatalog.set(resolved.options.ensSubjects);
    this.personalDataProcessingCatalog.set(resolved.options.personalDataProcessing);
    this.webContextCatalog.set(resolved.options.webContexts);
    this.fieldCatalog.set(resolved.options.fields);
    this.measureTypeCatalog.set(resolved.options.measureTypes);
    this.ensRequirementCatalog.set(resolved.options.ensRequirements);

    if (Object.values(resolved.failures).some(Boolean)) {
      this.showError(APPLICATION_SECURITY_LOAD_ERROR);
    }
  }

  protected startEditing(): void {
    this.detailState.startEditing('security');
  }

  protected cancelEditing(): void {
    this.detailState.cancelEditing('security');
  }

  protected save(): void {
    if (this.isSaving()) return;
    this.isSaving.set(true);
    this.detailState
      .save('security')
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

  protected selectLabel(
    options: SelectOption<number>[],
    value: number | null,
    fallback?: SecurityCatalogItem | null,
  ): string {
    return (
      options.find((option) => option.value === value)?.label ??
      (fallback ? localizedName(fallback, this.locale, `#${fallback.id}`) : '-')
    );
  }

  protected formattedApprovalDate(): string {
    const value = this.detailState.securityForm.controls.approvalDate.value;
    return value ? new Intl.DateTimeFormat(this.locale).format(value) : '-';
  }

  protected datePassThrough(): DatePickerPassThrough {
    return {
      pcInputText: {
        root: {
          'aria-invalid': 'false',
        },
      },
    };
  }

  protected onPageChange(kind: ApplicationSecurityTableKind, event: TableLazyLoadEvent): void {
    this.tableStates[kind] = event;
    this.setFirst(kind, event.first ?? 0);
    this.loadPage(kind);
  }

  protected onTableAction(
    kind: ApplicationSecurityResourceKind,
    event: ActionParams<ApplicationSecurityResourceOutput>,
  ): void {
    const resource = event.params as MutableSecurityResource;
    this.resourceKind.set(kind);
    switch (event.action) {
      case ApplicationSecurityTableAction.View:
        this.openExistingResource(kind, resource, 'view');
        break;
      case ApplicationSecurityTableAction.Edit:
        if (this.canManageResources()) this.openExistingResource(kind, resource, 'edit');
        break;
      case ApplicationSecurityTableAction.Delete:
        this.requestDelete(resource);
        break;
    }
  }

  protected openCreateDialog(kind: ApplicationSecurityResourceKind): void {
    if (!this.canManageResources()) return;
    configureApplicationSecurityResourceForm(this.resourceForm, kind);
    this.resourceKind.set(kind);
    this.selectedResource.set(null);
    this.resourceDialogMode.set('create');
    this.resourceDialogVisible.set(true);
  }

  protected startResourceEdit(): void {
    if (!this.resourceDialogCanEdit()) return;
    this.resourceDialogMode.set('edit');
  }

  protected cancelResourceEdit(): void {
    const selected = this.selectedResource();
    if (selected) this.populateResourceForm(this.resourceKind(), selected);
    this.resourceDialogMode.set('view');
  }

  protected closeResourceDialog(): void {
    this.resourceDialogVisible.set(false);
    this.selectedResource.set(null);
    this.resourceForm.reset();
  }

  protected submitResource(): void {
    if (this.isResourceSaving()) return;
    if (this.resourceForm.invalid) {
      this.resourceForm.markAllAsTouched();
      return;
    }
    const appSecurityId = this.detailState.appSecurityId();
    if (appSecurityId == null) return;

    const kind = this.resourceKind();
    const selected = this.selectedResource();
    const request = this.resourceSaveRequest(kind, appSecurityId, selected?.id ?? null);
    this.isResourceSaving.set(true);
    request
      .pipe(
        finalize(() => this.isResourceSaving.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.closeResourceDialog();
          this.loadPage(kind);
          this.detailState.refreshCompletenessAfterMutation();
          this.messageService.add({
            severity: 'success',
            summary: APPLICATION_SECURITY_SUCCESS_TITLE,
            detail: APPLICATION_SECURITY_RESOURCE_SAVE_SUCCESS,
          });
        },
        error: () => this.showError(APPLICATION_SECURITY_RESOURCE_SAVE_ERROR),
      });
  }

  protected requestDelete(resource = this.selectedResource()): void {
    if (!resource || !this.canManageResources()) return;
    this.pendingDelete.set(resource);
    this.deleteDialogVisible.set(true);
  }

  protected closeDeleteDialog(): void {
    this.deleteDialogVisible.set(false);
    this.pendingDelete.set(null);
  }

  protected confirmDelete(): void {
    const resource = this.pendingDelete();
    if (!resource || this.isResourceDeleting()) return;
    const kind = this.resourceKind();
    const request = this.resourceDeleteRequest(kind, resource.id);
    this.isResourceDeleting.set(true);
    request
      .pipe(
        finalize(() => this.isResourceDeleting.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.closeDeleteDialog();
          this.closeResourceDialog();
          this.loadPage(kind);
          this.detailState.refreshCompletenessAfterMutation();
          this.messageService.add({
            severity: 'success',
            summary: APPLICATION_SECURITY_SUCCESS_TITLE,
            detail: APPLICATION_SECURITY_RESOURCE_DELETE_SUCCESS,
          });
        },
        error: () => this.showError(APPLICATION_SECURITY_RESOURCE_DELETE_ERROR),
      });
  }

  protected onDocumentation(): void {
    this.messageService.add({
      severity: 'info',
      summary: APPLICATION_SECURITY_PENDING_TITLE,
      detail: APPLICATION_SECURITY_DOCUMENTATION_PENDING,
    });
  }

  private openExistingResource(
    kind: ApplicationSecurityResourceKind,
    resource: MutableSecurityResource,
    mode: CrudEntityDialogMode,
  ): void {
    this.resourceKind.set(kind);
    this.selectedResource.set(resource);
    configureApplicationSecurityResourceForm(this.resourceForm, kind);
    this.populateResourceForm(kind, resource);
    this.resourceDialogMode.set(mode);
    this.resourceDialogVisible.set(true);
  }

  private populateResourceForm(
    kind: ApplicationSecurityResourceKind,
    resource: MutableSecurityResource,
  ): void {
    configureApplicationSecurityResourceForm(this.resourceForm, kind);
    if (kind === 'web-context') {
      const item = resource as ApplicationWebContextOutput;
      this.resourceForm.patchValue({
        webContextId: item.webContext?.id ?? null,
        fieldId: item.field?.id ?? null,
        observation: item.observation ?? '',
      });
    } else if (kind === 'risk') {
      const item = resource as ApplicationSecurityRiskOutput;
      this.resourceForm.patchValue({
        levelId: item.level?.id ?? null,
        fieldId: item.field?.id ?? null,
        description: item.description ?? '',
      });
    } else {
      const item = resource as ApplicationSecurityMeasureOutput;
      this.resourceForm.patchValue({
        typeId: item.type?.id ?? null,
        ensRequirementId: item.ensRequirement?.id ?? null,
        description: item.description ?? '',
      });
    }
    this.resourceForm.markAsPristine();
  }

  private resourceSaveRequest(
    kind: ApplicationSecurityResourceKind,
    appSecurityId: number,
    id: number | null,
  ): Observable<ApplicationSecurityResourceOutput> {
    const value = this.resourceForm.getRawValue();
    if (kind === 'web-context') {
      const payload: ApplicationWebContextInput = {
        appSecurityId,
        webContextId: value.webContextId as number,
        fieldId: value.fieldId as number,
        observation: value.observation.trim() || null,
      };
      return id == null
        ? this.webContextsService.create(payload)
        : this.webContextsService.update(id, payload);
    }
    if (kind === 'risk') {
      const payload: ApplicationSecurityRiskInput = {
        appSecurityId,
        levelId: value.levelId,
        fieldId: value.fieldId,
        description: value.description.trim() || null,
      };
      return id == null ? this.risksService.create(payload) : this.risksService.update(id, payload);
    }
    const payload: ApplicationSecurityMeasureInput = {
      appSecurityId,
      typeId: value.typeId,
      ensRequirementId: value.ensRequirementId,
      description: value.description.trim() || null,
    };
    return id == null
      ? this.measuresService.create(payload)
      : this.measuresService.update(id, payload);
  }

  private resourceDeleteRequest(
    kind: ApplicationSecurityResourceKind,
    id: number,
  ): Observable<void> {
    switch (kind) {
      case 'web-context':
        return this.webContextsService.delete(id);
      case 'risk':
        return this.risksService.delete(id);
      case 'measure':
        return this.measuresService.delete(id);
    }
  }

  private loadPage(kind: ApplicationSecurityTableKind): void {
    const appSecurityId = this.detailState.appSecurityId();
    if (appSecurityId == null) return;
    const event = this.tableStates[kind];
    const rows = event.rows ?? PAGINATOR_ROWS;
    const page = Math.floor((event.first ?? 0) / rows);
    const sortField = typeof event.sortField === 'string' ? event.sortField : 'id';
    const sortDirection = event.sortOrder === -1 ? 'desc' : 'asc';
    const params = {
      appSecurityId,
      page,
      size: rows,
      sort: `${sortField},${sortDirection}`,
      statusId: SoftDeleteStatus.ACTIVE,
    };
    this.setLoading(kind, true);

    if (kind === 'role') {
      this.subscribePage(kind, this.rolesService.getPage(params), (page) => this.roles.set(page));
    } else if (kind === 'web-context') {
      this.subscribePage(kind, this.webContextsService.getPage(params), (page) =>
        this.webContexts.set(page),
      );
    } else if (kind === 'risk') {
      this.subscribePage(kind, this.risksService.getPage(params), (page) => this.risks.set(page));
    } else {
      this.subscribePage(kind, this.measuresService.getPage(params), (page) =>
        this.measures.set(page),
      );
    }
  }

  private subscribePage<T>(
    kind: ApplicationSecurityTableKind,
    request: Observable<SpringPage<T>>,
    update: (page: PaginatedList<T>) => void,
  ): void {
    request
      .pipe(
        catchError(() => {
          this.showError(APPLICATION_SECURITY_LOAD_ERROR);
          return EMPTY;
        }),
        finalize(() => this.setLoading(kind, false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((result) => {
        if (result.content.length === 0 && result.number > 0) {
          const rows = this.tableStates[kind].rows ?? PAGINATOR_ROWS;
          const first = (result.number - 1) * rows;
          this.tableStates[kind] = { ...this.tableStates[kind], first };
          this.setFirst(kind, first);
          this.loadPage(kind);
          return;
        }
        update(this.toList(result));
      });
  }

  private setFirst(kind: ApplicationSecurityTableKind, first: number): void {
    if (kind === 'role') this.roleFirst.set(first);
    else if (kind === 'web-context') this.webContextFirst.set(first);
    else if (kind === 'risk') this.riskFirst.set(first);
    else this.measureFirst.set(first);
  }

  private setLoading(kind: ApplicationSecurityTableKind, loading: boolean): void {
    if (kind === 'role') this.rolesLoading.set(loading);
    else if (kind === 'web-context') this.webContextsLoading.set(loading);
    else if (kind === 'risk') this.risksLoading.set(loading);
    else this.measuresLoading.set(loading);
  }

  private toList<T>(page: { content: T[]; totalElements: number } | null): PaginatedList<T> {
    return { items: page?.content ?? [], total: page?.totalElements ?? 0 };
  }

  private toOptions(items: SecurityCatalogItem[]): SelectOption<number>[] {
    return items.map((item) => ({
      value: item.id,
      label: localizedName(item, this.locale, `#${item.id}`),
    }));
  }

  private showError(detail: string): void {
    this.messageService.add({
      severity: 'error',
      summary: APPLICATION_SECURITY_ERROR_TITLE,
      detail,
    });
  }
}
