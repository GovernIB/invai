import { HttpErrorResponse } from '@angular/common/http';
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
import { FormBuilder } from '@angular/forms';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { ConfirmationDialogComponent } from '@components/confirmation-dialog/confirmation-dialog.component';
import { isStructuredBadRequest } from '@core/models/api-error.model';
import { CrudEntityDialogMode } from '@components/crud-entity-dialog/crud-entity-dialog';
import { ActionParams, KeyLabel, PaginatedList } from '@models/table.model';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';
import { localizedName } from '@shared/utils/localized-name.utils';
import { MessageService, PrimeIcons } from 'primeng/api';
import { Button } from 'primeng/button';
import { ButtonPassThrough } from 'primeng/types/button';
import { TableLazyLoadEvent } from 'primeng/table';
import { Textarea } from 'primeng/textarea';
import { catchError, EMPTY, finalize, map, Observable, of, Subject, switchMap } from 'rxjs';

import {
  ApplicationAuthorizedInput,
  ApplicationAuthorizedOutput,
  ApplicationAssignedResponsibleOutput,
  ApplicationAssignmentDeactivateInput,
  ApplicationPersonReferenceInput,
  ApplicationResponsibleInput,
  ApplicationResponsibleOutput,
} from '../../../../applications.model';
import {
  ApplicationAssignmentDeactivateFormGroup,
  createApplicationAssignmentDeactivateForm,
} from '../../../../forms/application-assignment-deactivate-form.factory';
import {
  ApplicationAuthorizedFormGroup,
  createApplicationAuthorizedForm,
  setApplicationAuthorizedCompanyRequired,
  setApplicationAuthorizedPersonSource,
} from '../../../../forms/application-authorized-form.factory';
import {
  ApplicationResponsibleFormGroup,
  createApplicationResponsibleForm,
  setApplicationResponsibleCompanyRequired,
  setApplicationResponsiblePersonSource,
} from '../../../../forms/application-responsible-form.factory';
import { ApplicationAuthorizedService } from '../../../../services/application-authorized.service';
import { ApplicationResponsiblesService } from '../../../../services/application-responsibles.service';
import {
  ResponsibleAuthorization,
  ResponsibleCompanyOption,
  ResponsiblePerson,
  ResponsibleType,
  SoffidPersonControlValue,
  SoffidPersonOption,
} from '../../../../../maintenances/responsibles/responsibles.model';
import { ResponsiblePeopleService } from '../../../../../maintenances/responsibles/services/responsible-people.service';
import { responsiblePersonFullName } from '../../../../../maintenances/responsibles/responsibles.utils';
import {
  APPLICATION_DETAIL_SAVE_ERROR_MESSAGE,
  APPLICATION_DETAIL_SAVE_ERROR_TITLE,
} from '../../application-detail.i18n';
import { ApplicationDetailState } from '../../application-detail-state';
import { ApplicationDetailSectionActions } from '../../components/application-detail-section-actions/application-detail-section-actions';
import { ApplicationDetailSectionLayout } from '../../components/application-detail-section-layout/application-detail-section-layout';
import { ApplicationAssignmentClipboardService } from './application-assignment-clipboard.service';
import { ApplicationAuthorizedDialog } from './application-authorized-dialog';
import {
  ApplicationAssignmentTableAction,
  ApplicationAuthorizedTable,
  ApplicationResponsibleTableRow,
  ApplicationResponsiblesTable,
  applicationAssignmentCellValue,
  assignmentRowsToCsv,
} from './application-assignment-tables';
import {
  ApplicationResponsibleDialog,
  ApplicationResponsibleSelectOption,
} from './application-responsible-dialog';
import {
  APPLICATION_RESPONSIBLE_COPY,
  APPLICATION_RESPONSIBLE_SECTION_TITLE,
} from './application-responsible-section.i18n';
import {
  APPLICATION_RESPONSIBLE_RESOLVE_KEY,
  ApplicationResponsibleResolvedData,
} from './application-responsible-section.resolver';

type AssignmentKind = 'responsible' | 'authorized';
type AssignmentDialogMode = Extract<CrudEntityDialogMode, 'create' | 'edit'>;
type PendingDeactivate =
  | { kind: 'responsible'; assignment: ApplicationAssignedResponsibleOutput }
  | { kind: 'authorized'; assignment: ApplicationAuthorizedOutput };

@Component({
  standalone: true,
  selector: 'app-application-responsible-section',
  imports: [
    ApplicationAuthorizedDialog,
    ApplicationAuthorizedTable,
    ApplicationDetailSectionActions,
    ApplicationDetailSectionLayout,
    ApplicationResponsiblesTable,
    ApplicationResponsibleDialog,
    Button,
    ConfirmationDialogComponent,
    ReactiveFormsModule,
    Textarea,
  ],
  templateUrl: './application-responsible-section.html',
  styleUrl: './application-responsible-section.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationResponsibleSection implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);
  private readonly formBuilder = inject(FormBuilder);
  private readonly messageService = inject(MessageService);
  private readonly locale = inject(LOCALE_ID);
  private readonly clipboard = inject(ApplicationAssignmentClipboardService);
  private readonly responsiblesService = inject(ApplicationResponsiblesService);
  private readonly authorizedService = inject(ApplicationAuthorizedService);
  private readonly peopleService = inject(ResponsiblePeopleService);

  protected readonly detailState = inject(ApplicationDetailState);
  protected readonly sectionTitle = APPLICATION_RESPONSIBLE_SECTION_TITLE;
  protected readonly copy = APPLICATION_RESPONSIBLE_COPY;
  protected readonly icons = PrimeIcons;
  protected readonly responsibleForm: ApplicationResponsibleFormGroup =
    createApplicationResponsibleForm(this.formBuilder);
  protected readonly authorizedForm: ApplicationAuthorizedFormGroup =
    createApplicationAuthorizedForm(this.formBuilder);
  protected readonly deactivateForm: ApplicationAssignmentDeactivateFormGroup =
    createApplicationAssignmentDeactivateForm(this.formBuilder);
  protected readonly responsibleColumns: KeyLabel[] = [
    {
      key: 'responsibility',
      width: '25%',
      wrap: true,
      maxWidth: '24rem',
      label: this.copy.responsibility,
      sortBy: 'responsibleType.name',
      minWidth: '12rem',
    },
    {
      key: 'person',
      width: '40%',
      wrap: true,
      maxWidth: '24rem',
      label: this.copy.person,
      sortBy: 'person.firstName',
      minWidth: '11rem',
    },
    {
      key: 'roleOrCompany',
      width: '35%',
      wrap: true,
      maxWidth: '24rem',
      label: this.copy.roleOrCompany,
      sortBy: 'person.company.name',
      minWidth: '11rem',
    },
  ];
  protected readonly authorizedColumns: KeyLabel[] = [
    {
      key: 'person',
      width: '35%',
      wrap: true,
      maxWidth: '24rem',
      label: this.copy.person,
      sortBy: 'person.firstName',
      minWidth: '12rem',
    },
    {
      key: 'authorization',
      width: '65%',
      wrap: true,
      maxWidth: '24rem',
      label: this.copy.authorization,
      minWidth: '22rem',
    },
  ];

  protected readonly anchorId = signal<number | null>(null);
  protected readonly available = signal(false);
  private readonly responsibleAssignments = signal<ApplicationResponsibleOutput[]>([]);
  protected readonly responsibles = signal<PaginatedList<ApplicationResponsibleTableRow>>({
    items: [],
    total: 0,
  });
  protected readonly authorized = signal<PaginatedList<ApplicationAuthorizedOutput>>({
    items: [],
    total: 0,
  });
  protected readonly responsiblesLoading = signal(false);
  protected readonly authorizedLoading = signal(false);
  protected readonly responsibleFirst = signal(0);
  protected readonly authorizedFirst = signal(0);
  protected readonly isSaving = signal(false);

  protected readonly responsibleDialogVisible = signal(false);
  protected readonly responsibleDialogSaving = signal(false);
  protected readonly responsibleDialogMode = signal<AssignmentDialogMode>('create');
  private readonly selectedResponsible = signal<ApplicationAssignedResponsibleOutput | null>(null);
  protected readonly authorizedDialogVisible = signal(false);
  protected readonly authorizedDialogSaving = signal(false);
  protected readonly authorizedDialogMode = signal<AssignmentDialogMode>('create');
  private readonly selectedAuthorized = signal<ApplicationAuthorizedOutput | null>(null);
  protected readonly deactivateDialogVisible = signal(false);
  protected readonly deactivatePending = signal(false);
  private readonly pendingDeactivate = signal<PendingDeactivate | null>(null);

  protected readonly responsibleTypes = signal<ResponsibleType[]>([]);
  protected readonly companyOptions = signal<ResponsibleCompanyOption[]>([]);
  protected readonly people = signal<ResponsiblePerson[]>([]);
  protected readonly authorizationTypes = signal<ResponsibleAuthorization[]>([]);
  protected readonly responsibleTypesLoadFailed = signal(false);
  protected readonly companyOptionsLoadFailed = signal(false);
  protected readonly peopleLoadFailed = signal(false);
  protected readonly authorizationTypesLoadFailed = signal(false);
  protected readonly soffidOptions = signal<SoffidPersonOption[]>([]);
  protected readonly soffidLoading = signal(false);
  protected readonly soffidSearched = signal(false);
  protected readonly soffidSearchError = signal(false);
  protected readonly soffidTotal = signal(0);
  protected readonly responsiblePersonalCaibLocked = signal(false);
  private readonly soffidSearchRequests = new Subject<string>();

  protected readonly responsiblePersonalCaib = signal(false);
  protected readonly responsibleSelectedCompanyId = signal<number | null>(null);
  protected readonly authorizedPersonalCaib = signal(false);
  protected readonly authorizedSelectedCompanyId = signal<number | null>(null);

  private readonly assignedResponsibleTypeIds = computed(
    () =>
      new Set(
        this.responsibleAssignments()
          .filter(isAssignedResponsible)
          .map(({ responsibleType }) => responsibleType.id),
      ),
  );
  private readonly vacantResponsibleTypes = computed(() =>
    this.responsibleTypes().filter((type) => !this.assignedResponsibleTypeIds().has(type.id)),
  );
  protected readonly assignableVacantResponsibleTypes = this.vacantResponsibleTypes;
  protected readonly responsibilitiesComplete = computed(
    () => this.responsibleTypes().length > 0 && this.vacantResponsibleTypes().length === 0,
  );
  protected readonly responsibleAddButtonPassThrough = computed<ButtonPassThrough>(() => ({
    root: {
      'aria-disabled': this.responsibilitiesComplete() ? 'true' : undefined,
      class: this.responsibilitiesComplete() ? 'application-responsible__add-complete' : undefined,
    },
  }));
  protected readonly responsibleTypeOptions = computed<ApplicationResponsibleSelectOption[]>(() => {
    const selectedTypeId = this.selectedResponsible()?.responsibleType.id;
    return this.responsibleTypes()
      .filter(
        (type) => type.id === selectedTypeId || !this.assignedResponsibleTypeIds().has(type.id),
      )
      .map((type) => ({
        id: type.id,
        label: localizedName(type, this.locale),
      }));
  });
  protected readonly authorizationTypeOptions = computed<ApplicationResponsibleSelectOption[]>(
    () => {
      const byId = new Map<number, ResponsibleAuthorization>();
      for (const type of this.authorizationTypes()) byId.set(type.id, type);
      for (const type of this.selectedAuthorized()?.authorizationTypes ?? []) {
        byId.set(type.id, type);
      }
      return [...byId.values()].map((type) => ({
        id: type.id,
        label: localizedName(type, this.locale),
      }));
    },
  );
  protected readonly responsibleDialogCompanyOptions = computed(() =>
    this.optionsWithSelectedCompany(this.selectedResponsible()?.person ?? null),
  );
  protected readonly authorizedDialogCompanyOptions = computed(() =>
    this.optionsWithSelectedCompany(this.selectedAuthorized()?.person ?? null),
  );
  protected readonly responsibleSelectablePeople = computed(() =>
    this.peopleForDialog(
      this.selectedResponsible()?.person ?? null,
      this.responsiblePersonalCaib(),
      this.responsibleSelectedCompanyId(),
    ),
  );
  protected readonly authorizedSelectablePeople = computed(() =>
    this.peopleForDialog(
      this.selectedAuthorized()?.person ?? null,
      this.authorizedPersonalCaib(),
      this.authorizedSelectedCompanyId(),
    ),
  );
  protected readonly copying = signal<AssignmentKind | null>(null);
  protected readonly canUseRowActions = computed(
    () => this.detailState.canEdit() && this.detailState.isEditing('responsible'),
  );
  protected readonly deactivateMessage = computed(() => {
    const pending = this.pendingDeactivate();
    if (!pending) return '';
    if (pending.kind === 'responsible') {
      return this.copy.responsibleDeactivateMessage(
        responsiblePersonFullName(pending.assignment.person),
        localizedName(pending.assignment.responsibleType, this.locale),
      );
    }
    return this.copy.authorizedDeactivateMessage(
      responsiblePersonFullName(pending.assignment.person),
      pending.assignment.authorizationTypes
        .map((type) => localizedName(type, this.locale))
        .join(', '),
    );
  });
  protected readonly deactivateConfirmAriaLabel = computed(() =>
    this.pendingDeactivate()?.kind === 'authorized'
      ? this.copy.authorizedDeactivateConfirmAriaLabel
      : this.copy.responsibleDeactivateConfirmAriaLabel,
  );

  private responsibleEvent: TableLazyLoadEvent | undefined;
  private authorizedEvent: TableLazyLoadEvent | undefined;

  constructor() {
    this.responsibleForm.controls.personalCaib.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((personalCaib) => this.onResponsiblePersonalCaibChanged(personalCaib));
    this.responsibleForm.controls.companyId.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((companyId) => this.onResponsibleCompanyChanged(companyId));
    this.responsibleForm.controls.personId.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((personId) => this.onResponsiblePersonChanged(personId));
    this.responsibleForm.controls.responsibleTypeId.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((typeId) => this.onResponsibleTypeChanged(typeId));
    this.responsibleForm.controls.soffidPerson.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((person) => this.onSoffidPersonChanged(person, this.responsibleForm));

    this.authorizedForm.controls.personalCaib.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((personalCaib) => this.onAuthorizedPersonalCaibChanged(personalCaib));
    this.authorizedForm.controls.companyId.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((companyId) => this.onAuthorizedCompanyChanged(companyId));
    this.authorizedForm.controls.personId.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((personId) => this.onAuthorizedPersonChanged(personId));
    this.authorizedForm.controls.soffidPerson.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((person) => this.onSoffidPersonChanged(person, this.authorizedForm));

    this.soffidSearchRequests
      .pipe(
        switchMap((query) => this.loadSoffid(query)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((result) => {
        if (result.error) {
          this.soffidSearchError.set(true);
          return;
        }
        const page = result.page!;
        this.soffidOptions.set(
          page.content.map((person) => ({
            ...person,
            label: `${person.firstName} ${person.lastName} — ${person.email}`,
          })),
        );
        this.soffidTotal.set(page.totalElements);
        this.soffidSearched.set(true);
      });
  }

  ngOnInit(): void {
    const resolved = this.route.snapshot.data[
      APPLICATION_RESPONSIBLE_RESOLVE_KEY
    ] as ApplicationResponsibleResolvedData;
    this.anchorId.set(resolved.anchorId);
    this.available.set(resolved.available);
    this.responsibleAssignments.set(resolved.responsiblesPage?.content ?? []);
    this.authorized.set({
      items: resolved.authorizedPage?.content ?? [],
      total: resolved.authorizedPage?.totalElements ?? 0,
    });
    this.responsibleTypes.set(resolved.responsibleTypes ?? []);
    this.companyOptions.set(resolved.companyOptions ?? []);
    this.people.set(resolved.people ?? []);
    this.authorizationTypes.set(resolved.authorizationTypes ?? []);
    this.responsibleTypesLoadFailed.set(resolved.responsibleTypesLoadFailed ?? false);
    this.companyOptionsLoadFailed.set(resolved.companyOptionsLoadFailed ?? false);
    this.peopleLoadFailed.set(resolved.peopleLoadFailed ?? false);
    this.authorizationTypesLoadFailed.set(resolved.authorizationTypesLoadFailed ?? false);
    this.rebuildResponsibleRows();

    if (resolved.responsiblesLoadFailed) this.showError(this.copy.responsiblesLoadError);
    if (resolved.authorizedLoadFailed) this.showError(this.copy.authorizedLoadError);
    if (resolved.responsibleTypesLoadFailed) this.showError(this.copy.responsibleTypesLoadError);
    if (resolved.companyOptionsLoadFailed) this.showError(this.copy.companyOptionsLoadError);
    if (resolved.peopleLoadFailed) this.showError(this.copy.peopleLoadError);
    if (resolved.authorizationTypesLoadFailed) {
      this.showError(this.copy.authorizationTypesLoadError);
    }
  }

  protected startEditing(): void {
    this.detailState.startEditing('responsible');
  }

  protected cancelEditing(): void {
    this.detailState.cancelEditing('responsible');
  }

  protected save(): void {
    if (this.isSaving()) return;

    this.isSaving.set(true);
    this.detailState
      .save('responsible')
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => this.isSaving.set(false),
        error: () => {
          this.isSaving.set(false);
          this.messageService.add({
            severity: 'error',
            summary: APPLICATION_DETAIL_SAVE_ERROR_TITLE,
            detail: APPLICATION_DETAIL_SAVE_ERROR_MESSAGE,
          });
        },
      });
  }

  protected canAttemptResponsibleAdd(): boolean {
    return this.canPreparePersonDialog() && !this.responsibleTypesLoadFailed();
  }

  protected searchSoffidPeople(query: string): void {
    const caibDialogActive =
      (this.responsibleDialogVisible() && this.responsibleForm.controls.personalCaib.value) ||
      (this.authorizedDialogVisible() && this.authorizedForm.controls.personalCaib.value);
    if (!caibDialogActive) {
      this.resetSoffidSearch();
      return;
    }
    this.soffidSearchRequests.next(query.trim());
  }

  protected openResponsibleDialog(): void {
    if (this.responsibilitiesComplete() && this.canAttemptResponsibleAdd()) {
      this.messageService.add({
        severity: 'info',
        summary: this.copy.allResponsibilitiesAssignedTitle,
        detail: this.copy.allResponsibilitiesAssignedDetail,
      });
      return;
    }
    if (!this.canAttemptResponsibleAdd() || this.assignableVacantResponsibleTypes().length === 0) {
      return;
    }
    const vacantTypes = this.assignableVacantResponsibleTypes();
    const fixedCaibType =
      vacantTypes.length === 1 && vacantTypes[0].requiresPersonalCaib ? vacantTypes[0] : null;
    this.prepareResponsibleDialog(null, 'create', fixedCaibType);
  }

  protected closeResponsibleDialog(): void {
    if (this.responsibleDialogSaving()) return;
    this.responsibleDialogVisible.set(false);
    this.selectedResponsible.set(null);
    this.resetSoffidSearch();
  }

  protected submitResponsible(): void {
    const mode = this.responsibleDialogMode();
    const selected = this.selectedResponsible();
    if (
      this.responsibleDialogSaving() ||
      !this.detailState.canEdit() ||
      (mode === 'edit' && !selected)
    ) {
      return;
    }
    if (this.responsibleForm.invalid) {
      this.responsibleForm.markAllAsTouched();
      return;
    }

    const anchorId = this.anchorId();
    if (anchorId === null) return;
    const value = this.responsibleForm.getRawValue();
    const personReference = this.personReference(
      mode === 'edit' ? selected!.person.id : value.personId,
      mode === 'edit' ? selected!.person.personalCaib : value.personalCaib,
      value.soffidPerson,
    );
    if (!personReference) return;
    const input: ApplicationResponsibleInput = {
      ...personReference,
      appResponsibleAuthorizedId: anchorId,
      responsibleTypeId: mode === 'edit' ? selected!.responsibleType.id : value.responsibleTypeId!,
      jobTitle:
        mode === 'edit'
          ? selected!.person.personalCaib
            ? value.cargo.trim() || null
            : selected!.jobTitle
          : value.personalCaib
            ? value.cargo.trim() || null
            : null,
      observation: value.observation.trim() || null,
    };
    const request =
      mode === 'create'
        ? this.responsiblesService.create(input)
        : this.responsiblesService.update(selected!.id, input);

    this.responsibleDialogSaving.set(true);
    request
      .pipe(
        finalize(() => this.responsibleDialogSaving.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.responsibleDialogVisible.set(false);
          this.selectedResponsible.set(null);
          this.resetSoffidSearch();
          this.showSuccess(
            mode === 'create' ? this.copy.responsibleCreated : this.copy.responsibleUpdated,
          );
          this.refreshResponsibles();
        },
        error: (error) =>
          this.handleError(
            error,
            mode === 'create' ? this.copy.responsibleCreateError : this.copy.responsibleUpdateError,
          ),
      });
  }

  protected openAuthorizedDialog(): void {
    if (!this.canOpenAuthorizedDialog()) return;
    this.prepareAuthorizedDialog(null, 'create');
  }

  protected closeAuthorizedDialog(): void {
    if (this.authorizedDialogSaving()) return;
    this.authorizedDialogVisible.set(false);
    this.selectedAuthorized.set(null);
    this.resetSoffidSearch();
  }

  protected submitAuthorized(): void {
    const mode = this.authorizedDialogMode();
    const selected = this.selectedAuthorized();
    if (
      this.authorizedDialogSaving() ||
      !this.detailState.canEdit() ||
      (mode === 'edit' && !selected)
    ) {
      return;
    }
    if (this.authorizedForm.invalid) {
      this.authorizedForm.markAllAsTouched();
      return;
    }

    const anchorId = this.anchorId();
    if (anchorId === null) return;
    const value = this.authorizedForm.getRawValue();
    const personReference = this.personReference(
      mode === 'edit' ? selected!.person.id : value.personId,
      mode === 'edit' ? selected!.person.personalCaib : value.personalCaib,
      value.soffidPerson,
    );
    if (!personReference) return;
    const input: ApplicationAuthorizedInput = {
      ...personReference,
      appResponsibleAuthorizedId: anchorId,
      authorizationTypeIds: value.authorizationTypeIds,
      observation: value.observation.trim() || null,
    };
    const request =
      mode === 'create'
        ? this.authorizedService.create(input)
        : this.authorizedService.update(selected!.id, input);

    this.authorizedDialogSaving.set(true);
    request
      .pipe(
        finalize(() => this.authorizedDialogSaving.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.authorizedDialogVisible.set(false);
          this.selectedAuthorized.set(null);
          this.resetSoffidSearch();
          this.showSuccess(
            mode === 'create' ? this.copy.authorizedCreated : this.copy.authorizedUpdated,
          );
          this.refreshAuthorized();
        },
        error: (error) =>
          this.handleError(
            error,
            mode === 'create' ? this.copy.authorizedCreateError : this.copy.authorizedUpdateError,
          ),
      });
  }

  protected onResponsibleTableAction(event: ActionParams<ApplicationResponsibleTableRow>): void {
    if (!this.canUseRowActions()) return;
    if (event.action === ApplicationAssignmentTableAction.Add && !event.params.assignment) {
      this.prepareResponsibleDialog(null, 'create', event.params.responsibleType);
    } else if (event.action === ApplicationAssignmentTableAction.Edit && event.params.assignment) {
      this.prepareResponsibleDialog(event.params.assignment, 'edit');
    } else if (
      event.action === ApplicationAssignmentTableAction.Deactivate &&
      event.params.assignment
    ) {
      this.openDeactivateDialog({
        kind: 'responsible',
        assignment: event.params.assignment,
      });
    }
  }

  protected onAuthorizedTableAction(event: ActionParams<ApplicationAuthorizedOutput>): void {
    if (!this.canUseRowActions()) return;
    if (event.action === ApplicationAssignmentTableAction.Edit) {
      if (this.canOpenAuthorizedDialog(event.params)) {
        this.prepareAuthorizedDialog(event.params, 'edit');
      }
    } else if (event.action === ApplicationAssignmentTableAction.Deactivate) {
      this.openDeactivateDialog({ kind: 'authorized', assignment: event.params });
    }
  }

  protected closeDeactivateDialog(): void {
    if (this.deactivatePending()) return;
    this.deactivateDialogVisible.set(false);
    this.pendingDeactivate.set(null);
    this.deactivateForm.reset();
  }

  protected confirmDeactivate(): void {
    const pending = this.pendingDeactivate();
    if (this.deactivatePending() || !this.canUseRowActions() || !pending) return;
    if (this.deactivateForm.invalid) {
      this.deactivateForm.markAllAsTouched();
      return;
    }

    const observation = this.deactivateForm.controls.observation.value.trim();
    const input: ApplicationAssignmentDeactivateInput = {
      observation,
    };
    const request: Observable<unknown> =
      pending.kind === 'responsible'
        ? this.responsiblesService.deactivate(pending.assignment.id, input)
        : this.authorizedService.deactivate(pending.assignment.id, input);

    this.deactivatePending.set(true);
    request
      .pipe(
        finalize(() => this.deactivatePending.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => {
          this.deactivateDialogVisible.set(false);
          this.pendingDeactivate.set(null);
          this.deactivateForm.reset();
          this.showSuccess(
            pending.kind === 'responsible'
              ? this.copy.responsibleDeactivated
              : this.copy.authorizedDeactivated,
          );
          if (pending.kind === 'responsible') {
            this.refreshResponsibles();
          } else {
            this.refreshAuthorized();
          }
        },
        error: (error) =>
          this.handleError(
            error,
            pending.kind === 'responsible'
              ? this.copy.responsibleDeactivateError
              : this.copy.authorizedDeactivateError,
          ),
      });
  }

  protected copyResponsibles(): void {
    this.copyPage('responsible', this.responsibleColumns, this.responsibles().items, (row, key) =>
      applicationAssignmentCellValue(row, key, this.locale, this.copy.caibRole),
    );
  }

  protected copyAuthorized(): void {
    this.copyPage('authorized', this.authorizedColumns, this.authorized().items, (row, key) =>
      applicationAssignmentCellValue(row, key, this.locale, this.copy.caibRole),
    );
  }

  protected onResponsiblePage(event: TableLazyLoadEvent): void {
    this.responsibleEvent = event;
    this.responsibleFirst.set(event.first ?? 0);
    this.rebuildResponsibleRows();
  }

  protected onAuthorizedPage(event: TableLazyLoadEvent): void {
    this.authorizedEvent = event;
    this.authorizedFirst.set(event.first ?? 0);
    this.refreshAuthorized();
  }

  private prepareResponsibleDialog(
    assignment: ApplicationAssignedResponsibleOutput | null,
    mode: AssignmentDialogMode,
    fixedType: ResponsibleType | null = null,
  ): void {
    if (
      this.responsibleTypesLoadFailed() ||
      !this.canPreparePersonDialog(assignment?.person ?? null) ||
      (mode === 'edit' && !this.canUseRowActions())
    ) {
      return;
    }
    const person = assignment?.person ?? null;
    const personalCaib = person?.personalCaib ?? Boolean(fixedType?.requiresPersonalCaib);
    const companyId = person?.company?.id ?? null;
    this.selectedResponsible.set(assignment);
    this.responsibleDialogMode.set(mode);
    this.responsibleForm.enable({ emitEvent: false });
    this.responsibleForm.reset(
      {
        personalCaib,
        responsibleTypeId: fixedType?.id ?? assignment?.responsibleType.id ?? null,
        companyId,
        personId: person?.id ?? null,
        soffidPerson: null,
        email: person?.email ?? '',
        cargo: assignment?.jobTitle ?? '',
        observation: assignment?.observation ?? '',
      },
      { emitEvent: false },
    );
    setApplicationResponsiblePersonSource(this.responsibleForm, personalCaib);
    setApplicationResponsibleCompanyRequired(this.responsibleForm, !personalCaib);
    this.responsiblePersonalCaib.set(personalCaib);
    this.responsibleSelectedCompanyId.set(companyId);
    this.responsiblePersonalCaibLocked.set(Boolean(fixedType?.requiresPersonalCaib));
    this.resetSoffidSearch();
    this.configureResponsibleFormAvailability(
      mode,
      personalCaib,
      fixedType !== null,
      Boolean(fixedType?.requiresPersonalCaib),
    );
    this.responsibleForm.markAsPristine();
    this.responsibleForm.markAsUntouched();
    this.responsibleDialogVisible.set(true);
  }

  private openDeactivateDialog(pending: PendingDeactivate): void {
    this.pendingDeactivate.set(pending);
    this.deactivateForm.reset({ observation: '' });
    this.deactivateForm.markAsPristine();
    this.deactivateForm.markAsUntouched();
    this.deactivateDialogVisible.set(true);
  }

  private prepareAuthorizedDialog(
    assignment: ApplicationAuthorizedOutput | null,
    mode: AssignmentDialogMode,
  ): void {
    if (
      !this.canOpenAuthorizedDialog(assignment) ||
      (mode === 'edit' && !this.canUseRowActions())
    ) {
      return;
    }
    const person = assignment?.person ?? null;
    const personalCaib = person?.personalCaib ?? false;
    const companyId = person?.company?.id ?? null;
    this.selectedAuthorized.set(assignment);
    this.authorizedDialogMode.set(mode);
    this.authorizedForm.enable({ emitEvent: false });
    this.authorizedForm.reset(
      {
        personalCaib,
        companyId,
        personId: person?.id ?? null,
        soffidPerson: null,
        email: person?.email ?? '',
        observation: assignment?.observation ?? '',
        authorizationTypeIds: assignment?.authorizationTypes.map(({ id }) => id) ?? [],
      },
      { emitEvent: false },
    );
    setApplicationAuthorizedPersonSource(this.authorizedForm, personalCaib);
    setApplicationAuthorizedCompanyRequired(this.authorizedForm, !personalCaib);
    this.authorizedPersonalCaib.set(personalCaib);
    this.authorizedSelectedCompanyId.set(companyId);
    this.resetSoffidSearch();
    this.configureAuthorizedFormAvailability(mode, personalCaib);
    this.authorizedForm.markAsPristine();
    this.authorizedForm.markAsUntouched();
    this.authorizedDialogVisible.set(true);
  }

  protected canOpenAuthorizedDialog(existing: ApplicationAuthorizedOutput | null = null): boolean {
    const authorizationOptionsAvailable =
      !this.authorizationTypesLoadFailed() &&
      (this.authorizationTypes().length > 0 || Boolean(existing?.authorizationTypes.length));
    return this.canPreparePersonDialog(existing?.person ?? null) && authorizationOptionsAvailable;
  }

  private canPreparePersonDialog(selectedPerson: ResponsiblePerson | null = null): boolean {
    return this.detailState.canEdit() && (selectedPerson === null || selectedPerson.id > 0);
  }

  private onResponsiblePersonalCaibChanged(personalCaib: boolean): void {
    if (this.responsibleDialogMode() === 'edit') return;
    this.responsiblePersonalCaib.set(personalCaib);
    this.responsibleSelectedCompanyId.set(null);
    setApplicationResponsiblePersonSource(this.responsibleForm, personalCaib);
    setApplicationResponsibleCompanyRequired(this.responsibleForm, !personalCaib);
    this.responsibleForm.controls.companyId.setValue(null, { emitEvent: false });
    this.responsibleForm.controls.personId.setValue(null, { emitEvent: false });
    this.responsibleForm.controls.soffidPerson.setValue(null, { emitEvent: false });
    this.responsibleForm.controls.email.setValue('', { emitEvent: false });
    if (!personalCaib) this.responsibleForm.controls.cargo.setValue('', { emitEvent: false });
    this.updateResponsibleConditionalAvailability(personalCaib);
    this.resetSoffidSearch();
  }

  private onResponsibleCompanyChanged(companyId: number | null): void {
    if (this.responsibleDialogMode() === 'edit') return;
    if (this.responsiblePersonalCaib()) return;
    this.responsibleSelectedCompanyId.set(companyId);
    this.responsibleForm.controls.personId.setValue(null, { emitEvent: false });
    this.responsibleForm.controls.email.setValue('', { emitEvent: false });
  }

  private onResponsiblePersonChanged(personId: number | null): void {
    if (this.responsibleDialogMode() === 'edit') return;
    const person = this.responsibleSelectablePeople().find(
      (candidate) => candidate.id === personId,
    );
    this.responsibleForm.controls.email.setValue(person?.email ?? '', { emitEvent: false });
  }

  private onResponsibleTypeChanged(typeId: number | null): void {
    if (this.responsibleDialogMode() === 'edit') return;
    const requiresPersonalCaib = Boolean(
      this.responsibleTypes().find((type) => type.id === typeId)?.requiresPersonalCaib,
    );
    this.responsiblePersonalCaibLocked.set(requiresPersonalCaib);
    if (requiresPersonalCaib) {
      this.responsibleForm.controls.personalCaib.setValue(true);
      this.responsibleForm.controls.personalCaib.disable({ emitEvent: false });
    } else {
      this.responsibleForm.controls.personalCaib.enable({ emitEvent: false });
    }
  }

  private onAuthorizedPersonalCaibChanged(personalCaib: boolean): void {
    if (this.authorizedDialogMode() === 'edit') return;
    this.authorizedPersonalCaib.set(personalCaib);
    this.authorizedSelectedCompanyId.set(null);
    setApplicationAuthorizedPersonSource(this.authorizedForm, personalCaib);
    setApplicationAuthorizedCompanyRequired(this.authorizedForm, !personalCaib);
    this.authorizedForm.controls.companyId.setValue(null, { emitEvent: false });
    this.authorizedForm.controls.personId.setValue(null, { emitEvent: false });
    this.authorizedForm.controls.soffidPerson.setValue(null, { emitEvent: false });
    this.authorizedForm.controls.email.setValue('', { emitEvent: false });
    this.updateAuthorizedConditionalAvailability(personalCaib);
    this.resetSoffidSearch();
  }

  private onAuthorizedCompanyChanged(companyId: number | null): void {
    if (this.authorizedDialogMode() === 'edit') return;
    if (this.authorizedPersonalCaib()) return;
    this.authorizedSelectedCompanyId.set(companyId);
    this.authorizedForm.controls.personId.setValue(null, { emitEvent: false });
    this.authorizedForm.controls.email.setValue('', { emitEvent: false });
  }

  private onAuthorizedPersonChanged(personId: number | null): void {
    if (this.authorizedDialogMode() === 'edit') return;
    const person = this.authorizedSelectablePeople().find((candidate) => candidate.id === personId);
    this.authorizedForm.controls.email.setValue(person?.email ?? '', { emitEvent: false });
  }

  private onSoffidPersonChanged(
    person: SoffidPersonControlValue,
    form: ApplicationResponsibleFormGroup | ApplicationAuthorizedFormGroup,
  ): void {
    if (person && typeof person !== 'string') {
      form.controls.email.setValue(person.email, { emitEvent: false });
      return;
    }
    form.controls.email.setValue('', { emitEvent: false });
    this.resetSoffidSearch();
  }

  private loadSoffid(query: string) {
    this.clearSoffidSearchState();
    if (query.length < 3) return EMPTY;

    this.soffidLoading.set(true);
    return this.peopleService.searchSoffid(query).pipe(
      map((page) => ({ page, error: false as const })),
      catchError(() => of({ page: null, error: true as const })),
      finalize(() => this.soffidLoading.set(false)),
    );
  }

  private resetSoffidSearch(): void {
    this.clearSoffidSearchState();
    this.soffidSearchRequests.next('');
  }

  private clearSoffidSearchState(): void {
    this.soffidOptions.set([]);
    this.soffidLoading.set(false);
    this.soffidSearched.set(false);
    this.soffidSearchError.set(false);
    this.soffidTotal.set(0);
  }

  private personReference(
    personId: number | null,
    personalCaib: boolean,
    soffidPerson: SoffidPersonControlValue,
  ): ApplicationPersonReferenceInput | null {
    if (personId !== null) return { personId, personalCaib };
    if (!personalCaib || !soffidPerson || typeof soffidPerson === 'string') return null;
    const firstName = soffidPerson.firstName.trim();
    const lastName = soffidPerson.lastName.trim();
    const email = soffidPerson.email.trim();
    if (!firstName || !lastName || !email) return null;
    return {
      personId: null,
      personFirstName: firstName,
      personLastName: lastName,
      personEmail: email,
      companyId: null,
      personalCaib: true,
    };
  }

  private configureResponsibleFormAvailability(
    mode: AssignmentDialogMode,
    personalCaib: boolean,
    hasFixedType: boolean,
    personalCaibLocked: boolean,
  ): void {
    const controls = this.responsibleForm.controls;
    controls.email.enable({ emitEvent: false });
    controls.observation.enable({ emitEvent: false });

    if (mode === 'edit') {
      controls.personalCaib.disable({ emitEvent: false });
      controls.responsibleTypeId.disable({ emitEvent: false });
      controls.companyId.disable({ emitEvent: false });
      controls.personId.disable({ emitEvent: false });
      controls.soffidPerson.disable({ emitEvent: false });
    } else {
      if (personalCaibLocked) controls.personalCaib.disable({ emitEvent: false });
      else controls.personalCaib.enable({ emitEvent: false });
      if (hasFixedType) controls.responsibleTypeId.disable({ emitEvent: false });
      else controls.responsibleTypeId.enable({ emitEvent: false });
      this.updateResponsibleConditionalAvailability(personalCaib);
    }

    if (personalCaib) controls.cargo.enable({ emitEvent: false });
    else controls.cargo.disable({ emitEvent: false });
  }

  private updateResponsibleConditionalAvailability(personalCaib: boolean): void {
    const controls = this.responsibleForm.controls;
    if (personalCaib) {
      controls.companyId.disable({ emitEvent: false });
      controls.personId.disable({ emitEvent: false });
      controls.soffidPerson.enable({ emitEvent: false });
    } else {
      controls.companyId.enable({ emitEvent: false });
      controls.personId.enable({ emitEvent: false });
      controls.soffidPerson.disable({ emitEvent: false });
    }
    if (personalCaib) controls.cargo.enable({ emitEvent: false });
    else controls.cargo.disable({ emitEvent: false });
  }

  private configureAuthorizedFormAvailability(
    mode: AssignmentDialogMode,
    personalCaib: boolean,
  ): void {
    const controls = this.authorizedForm.controls;
    controls.email.enable({ emitEvent: false });
    controls.observation.enable({ emitEvent: false });
    controls.authorizationTypeIds.enable({ emitEvent: false });

    if (mode === 'edit') {
      controls.personalCaib.disable({ emitEvent: false });
      controls.companyId.disable({ emitEvent: false });
      controls.personId.disable({ emitEvent: false });
      controls.soffidPerson.disable({ emitEvent: false });
    } else {
      controls.personalCaib.enable({ emitEvent: false });
      this.updateAuthorizedConditionalAvailability(personalCaib);
    }
  }

  private updateAuthorizedConditionalAvailability(personalCaib: boolean): void {
    const controls = this.authorizedForm.controls;
    if (personalCaib) {
      controls.companyId.disable({ emitEvent: false });
      controls.personId.disable({ emitEvent: false });
      controls.soffidPerson.enable({ emitEvent: false });
    } else {
      controls.companyId.enable({ emitEvent: false });
      controls.personId.enable({ emitEvent: false });
      controls.soffidPerson.disable({ emitEvent: false });
    }
  }

  private optionsWithSelectedCompany(person: ResponsiblePerson | null): ResponsibleCompanyOption[] {
    const options = [...this.companyOptions()];
    if (person?.company && !options.some(({ id }) => id === person.company!.id)) {
      options.push({ id: person.company.id, label: person.company.name });
    }
    return options;
  }

  private peopleForDialog(
    selectedPerson: ResponsiblePerson | null,
    personalCaib: boolean,
    companyId: number | null,
  ): ResponsiblePerson[] {
    const people = selectedPerson
      ? this.uniquePeople([...this.people(), selectedPerson])
      : this.people();
    return people.filter((person) => {
      if (person.personalCaib !== personalCaib) return false;
      return personalCaib || person.company?.id === companyId;
    });
  }

  private uniquePeople(people: ResponsiblePerson[]): ResponsiblePerson[] {
    return [...new Map(people.map((person) => [person.id, person])).values()];
  }

  private rebuildResponsibleRows(): void {
    const assignments = this.responsibleAssignments();
    const types = this.responsibleTypes().length
      ? this.responsibleTypes()
      : [
          ...new Map(
            assignments.map((item) => [item.responsibleType.id, item.responsibleType]),
          ).values(),
        ];
    const byType = new Map(
      assignments
        .filter(isAssignedResponsible)
        .map((item) => [item.responsibleType.id, item] as const),
    );
    const rows: ApplicationResponsibleTableRow[] = types.map((responsibleType) => ({
      rowKey: `responsible-type-${responsibleType.id}`,
      responsibleType,
      assignment: byType.get(responsibleType.id) ?? null,
    }));
    this.sortResponsibleRows(rows);
    const size = this.responsibleEvent?.rows ?? 10;
    const first = this.responsibleEvent?.first ?? 0;
    this.responsibles.set({ items: rows.slice(first, first + size), total: rows.length });
  }

  private sortResponsibleRows(rows: ApplicationResponsibleTableRow[]): void {
    const field = Array.isArray(this.responsibleEvent?.sortField)
      ? this.responsibleEvent?.sortField[0]
      : this.responsibleEvent?.sortField;
    if (!field) return;
    const direction = this.responsibleEvent?.sortOrder === -1 ? -1 : 1;
    rows.sort(
      (left, right) =>
        this.responsibleSortValue(left, field).localeCompare(
          this.responsibleSortValue(right, field),
          this.locale,
          { sensitivity: 'base' },
        ) * direction,
    );
  }

  private responsibleSortValue(row: ApplicationResponsibleTableRow, field: string): string {
    if (field === 'responsibleType.name') return localizedName(row.responsibleType, this.locale);
    if (field === 'person.firstName') {
      return row.assignment ? responsiblePersonFullName(row.assignment.person) : '';
    }
    if (field === 'person.company.name') {
      return row.assignment?.person.personalCaib
        ? this.copy.caibRole
        : (row.assignment?.person.company?.name ?? '');
    }
    return '';
  }

  private copyPage<TItem extends ApplicationResponsibleTableRow | ApplicationAuthorizedOutput>(
    kind: AssignmentKind,
    columns: KeyLabel[],
    rows: TItem[],
    value: (row: TItem, key: string) => string,
  ): void {
    if (!rows.length || this.copying()) return;

    this.copying.set(kind);
    const csv = assignmentRowsToCsv(columns, rows, value);
    void this.clipboard
      .copy(csv)
      .then(() => {
        this.messageService.add({
          severity: 'success',
          summary: this.copy.copySuccessTitle,
          detail: this.copy.copySuccessDetail,
        });
      })
      .catch(() => {
        this.messageService.add({
          severity: 'error',
          summary: this.copy.copyErrorTitle,
          detail: this.copy.copyErrorDetail,
        });
      })
      .finally(() => this.copying.set(null));
  }

  private refreshResponsibles(): void {
    const anchorId = this.anchorId();
    if (anchorId === null) return;

    this.responsiblesLoading.set(true);
    this.responsiblesService
      .getPage({
        appResponsibleAuthorizedId: anchorId,
        page: 0,
        size: 1000,
        sort: 'id,asc',
        statusId: SoftDeleteStatus.ACTIVE,
      })
      .pipe(
        finalize(() => this.responsiblesLoading.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (page) => {
          this.responsibleAssignments.set(page.content);
          this.rebuildResponsibleRows();
        },
        error: (error) => this.handleError(error, this.copy.responsiblesLoadError),
      });
  }

  private refreshAuthorized(): void {
    const anchorId = this.anchorId();
    if (anchorId === null) return;

    this.authorizedLoading.set(true);
    this.authorizedService
      .getPage(this.authorizedPageParams(anchorId, this.authorizedEvent))
      .pipe(
        finalize(() => this.authorizedLoading.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (page) => this.authorized.set({ items: page.content, total: page.totalElements }),
        error: (error) => this.handleError(error, this.copy.authorizedLoadError),
      });
  }

  private authorizedPageParams(anchorId: number, event: TableLazyLoadEvent | undefined) {
    const size = event?.rows ?? 10;
    const sortField = Array.isArray(event?.sortField) ? event.sortField[0] : event?.sortField;
    return {
      appResponsibleAuthorizedId: anchorId,
      page: Math.floor((event?.first ?? 0) / size),
      size,
      sort: sortField ? `${sortField},${event?.sortOrder === -1 ? 'desc' : 'asc'}` : 'id,asc',
      statusId: SoftDeleteStatus.ACTIVE,
    };
  }

  private showSuccess(detail: string): void {
    this.messageService.add({ severity: 'success', summary: $localize`Correcte`, detail });
  }

  private handleError(error: unknown, fallback: string): void {
    if (isStructuredBadRequest(error)) return;
    if (error instanceof HttpErrorResponse && error.status === 403) {
      return this.showError(this.copy.forbidden);
    }
    this.showError(fallback);
  }

  private showError(detail: string): void {
    this.messageService.add({ severity: 'error', summary: $localize`Error`, detail });
  }
}

function isAssignedResponsible(
  value: ApplicationResponsibleOutput,
): value is ApplicationAssignedResponsibleOutput {
  return value.id !== null && value.person !== null;
}
