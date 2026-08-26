import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  ElementRef,
  LOCALE_ID,
  OnInit,
  computed,
  effect,
  inject,
  input,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ConfirmationDialogComponent } from '@components/confirmation-dialog/confirmation-dialog.component';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';
import { localizedName } from '@shared/utils/localized-name.utils';
import { MessageService, PrimeIcons } from 'primeng/api';
import { Button } from 'primeng/button';
import { Select } from 'primeng/select';
import { ToggleSwitch } from 'primeng/toggleswitch';
import { Observable, Subject, catchError, forkJoin, map, of, switchMap } from 'rxjs';

import { createRoleTransferForm } from '../../forms/responsible-forms.factory';
import { RESPONSIBLE_COMMON_COPY, ROLE_TRANSFER_COPY } from '../../responsibles.i18n';
import {
  ResponsiblePerson,
  ResponsiblePersonOption,
  RoleAssignmentOutput,
  RoleAssignmentType,
  RoleTransferInput,
  RoleTransferSourceRequest,
} from '../../responsibles.model';
import { toResponsiblePersonOption } from '../../responsibles.utils';
import { ResponsiblePeopleService } from '../../services/responsible-people.service';
import { RoleTransferService } from '../../services/role-transfer.service';

interface AssignmentSubgroup {
  type: RoleAssignmentType;
  label: string;
  items: RoleAssignmentOutput[];
}

interface AssignmentApplicationGroup {
  applicationId: number;
  applicationName: string;
  items: RoleAssignmentOutput[];
  subgroups: AssignmentSubgroup[];
}

interface LoadResult<T> {
  value: T | null;
  failed: boolean;
}

type ConfirmationKind = 'apply' | 'discard-source' | null;

const PERSON_OPTIONS_PARAMS = {
  page: 0,
  size: 1000,
  sort: ['firstName,asc', 'lastName,asc'] as string[],
  statusId: SoftDeleteStatus.ACTIVE,
} as const;

@Component({
  selector: 'app-role-transfer',
  standalone: true,
  imports: [Button, ConfirmationDialogComponent, ReactiveFormsModule, Select, ToggleSwitch],
  templateUrl: './role-transfer.html',
  styleUrl: './role-transfer.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RoleTransfer implements OnInit {
  initialPeople = input<ResponsiblePerson[]>([]);
  initialPeopleLoadFailed = input(false);
  sourceRequest = input<RoleTransferSourceRequest | null>(null);

  protected readonly copy = ROLE_TRANSFER_COPY;
  protected readonly PrimeIcons = PrimeIcons;
  protected readonly RoleAssignmentType = RoleAssignmentType;
  protected readonly form = createRoleTransferForm(inject(FormBuilder));
  protected readonly sourceOptions = signal<ResponsiblePersonOption[]>([]);
  protected readonly destinationOptions = signal<ResponsiblePersonOption[]>([]);
  protected readonly sourceAssignments = signal<RoleAssignmentOutput[]>([]);
  protected readonly preparedAssignments = signal<RoleAssignmentOutput[]>([]);
  protected readonly sourceSelection = signal<Set<string>>(new Set());
  protected readonly preparedSelection = signal<Set<string>>(new Set());
  protected readonly sourceCatalogLoading = signal(false);
  protected readonly sourceCatalogLoadFailed = signal(false);
  protected readonly sourceRolesLoading = signal(false);
  protected readonly sourceRolesLoadFailed = signal(false);
  protected readonly destinationOptionsLoadFailed = signal(false);
  protected readonly destinationRolesLoading = signal(false);
  protected readonly destinationRolesLoadFailed = signal(false);
  protected readonly destinationRolesReady = signal(false);
  protected readonly submitting = signal(false);
  protected readonly confirmationVisible = signal(false);
  protected readonly statusMessage = signal('');

  private readonly peopleService = inject(ResponsiblePeopleService);
  private readonly roleTransferService = inject(RoleTransferService);
  private readonly messageService = inject(MessageService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef);
  private readonly locale = inject(LOCALE_ID);
  private readonly sourceRequests = new Subject<number>();
  private readonly destinationRequests = new Subject<number | null>();
  private readonly committedSourceId = signal<number | null>(null);
  private readonly destinationId = signal<number | null>(null);
  private readonly revokeMode = signal(false);
  private readonly forbiddenAuthorizationApplications = signal<Set<number>>(new Set());
  private readonly pendingSourceId = signal<number | null>(null);
  private readonly confirmationKind = signal<ConfirmationKind>(null);
  private initialized = false;
  private handledSourceRequestId: number | null = null;

  protected readonly sourceGroups = computed(() => this.groupAssignments(this.sourceAssignments()));
  protected readonly preparedGroups = computed(() =>
    this.groupAssignments(this.preparedAssignments()),
  );
  protected readonly preparedTitle = computed(() =>
    this.revokeMode() ? this.copy.rolesToRevoke : this.copy.rolesToTransfer,
  );
  protected readonly actionLabel = computed(() =>
    this.revokeMode() ? this.copy.revoke : this.copy.transfer,
  );
  protected readonly actionAriaLabel = computed(() =>
    this.revokeMode() ? this.copy.revokeAriaLabel : this.copy.transferAriaLabel,
  );
  protected readonly canApply = computed(() => {
    const destinationIsReady =
      this.revokeMode() ||
      (!!this.destinationId() &&
        this.destinationId() !== this.committedSourceId() &&
        this.destinationRolesReady() &&
        !this.destinationRolesLoading());
    return (
      !!this.committedSourceId() &&
      this.preparedAssignments().length > 0 &&
      destinationIsReady &&
      !this.sourceRolesLoading() &&
      !this.submitting()
    );
  });
  protected readonly confirmationTitle = computed(() => {
    if (this.confirmationKind() === 'discard-source') return this.copy.discardConfirmTitle;
    return this.revokeMode() ? this.copy.revokeConfirmTitle : this.copy.transferConfirmTitle;
  });
  protected readonly confirmationMessage = computed(() => {
    if (this.confirmationKind() === 'discard-source') {
      return this.pendingSourceId() === null
        ? this.copy.discardClearMessage
        : this.copy.discardConfirmMessage(this.personName(this.pendingSourceId()));
    }
    const count = this.preparedAssignments().length;
    const source = this.personName(this.committedSourceId());
    return this.revokeMode()
      ? this.copy.revokeConfirmMessage(count, source)
      : this.copy.transferConfirmMessage(count, source, this.personName(this.destinationId()));
  });
  protected readonly confirmationLabel = computed(() => {
    if (this.confirmationKind() === 'discard-source') return this.copy.confirmDiscard;
    return this.revokeMode() ? this.copy.confirmRevoke : this.copy.confirmTransfer;
  });
  protected readonly confirmationSeverity = computed(() =>
    this.confirmationKind() === 'discard-source' || this.revokeMode() ? 'danger' : 'primary',
  );

  constructor() {
    effect(() => {
      const request = this.sourceRequest();
      if (this.initialized) this.applySourceRequest(request);
    });

    this.form.controls.sourcePersonId.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((personId) => this.onSourceControlChange(personId));
    this.form.controls.destinationPersonId.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((personId) => this.onDestinationControlChange(personId));
    this.form.controls.revoke.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((revoke) => this.onRevokeChange(revoke));

    this.sourceRequests
      .pipe(
        switchMap((personId) => {
          this.sourceRolesLoading.set(true);
          this.sourceRolesLoadFailed.set(false);
          this.destinationOptionsLoadFailed.set(false);
          this.form.controls.destinationPersonId.disable({ emitEvent: false });
          return forkJoin({
            assignments: loadResult(this.roleTransferService.getAssignments(personId)),
            destinations: loadResult(
              this.peopleService.getPage({ ...PERSON_OPTIONS_PARAMS, excludeId: personId }),
            ),
          }).pipe(map((result) => ({ personId, ...result })));
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(({ personId, assignments, destinations }) => {
        if (personId !== this.committedSourceId()) return;
        this.sourceRolesLoading.set(false);
        this.sourceRolesLoadFailed.set(assignments.failed);
        this.destinationOptionsLoadFailed.set(destinations.failed);
        this.sourceAssignments.set(assignments.value ?? []);
        this.destinationOptions.set(
          (destinations.value?.content ?? [])
            .filter(({ id }) => id !== personId)
            .map(toResponsiblePersonOption),
        );
        this.updateDestinationAvailability();
      });

    this.destinationRequests
      .pipe(
        switchMap((personId) => {
          if (personId === null) {
            return of({
              personId,
              result: { value: [], failed: false } as LoadResult<RoleAssignmentOutput[]>,
            });
          }
          this.destinationRolesLoading.set(true);
          this.destinationRolesLoadFailed.set(false);
          this.destinationRolesReady.set(false);
          return loadResult(this.roleTransferService.getAssignments(personId)).pipe(
            map((result) => ({ personId, result })),
          );
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(({ personId, result }) => {
        if (personId !== this.destinationId()) return;
        this.destinationRolesLoading.set(false);
        this.destinationRolesLoadFailed.set(result.failed);
        this.destinationRolesReady.set(personId !== null && !result.failed);
        this.forbiddenAuthorizationApplications.set(
          new Set(
            (result.value ?? [])
              .filter(({ type }) => type === RoleAssignmentType.AUTHORIZED)
              .map(({ applicationId }) => applicationId),
          ),
        );
        if (!result.failed) this.removeDestinationConflicts();
      });
  }

  ngOnInit(): void {
    this.sourceOptions.set(this.initialPeople().map(toResponsiblePersonOption));
    this.sourceCatalogLoadFailed.set(this.initialPeopleLoadFailed());
    this.initialized = true;
    this.applySourceRequest(this.sourceRequest());
  }

  protected retrySourceCatalog(): void {
    if (this.sourceCatalogLoading()) return;
    this.sourceCatalogLoading.set(true);
    this.sourceCatalogLoadFailed.set(false);
    this.peopleService
      .getPage(PERSON_OPTIONS_PARAMS)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (page) => {
          this.sourceOptions.set(page.content.map(toResponsiblePersonOption));
          this.sourceCatalogLoading.set(false);
        },
        error: () => {
          this.sourceCatalogLoadFailed.set(true);
          this.sourceCatalogLoading.set(false);
        },
      });
  }

  protected retrySourceLoad(): void {
    const personId = this.committedSourceId();
    if (personId) this.sourceRequests.next(personId);
  }

  protected retryDestinationLoad(): void {
    if (this.destinationId()) this.destinationRequests.next(this.destinationId());
  }

  protected toggleAssignment(
    assignment: RoleAssignmentOutput,
    fromSource: boolean,
    selected: boolean,
  ): void {
    const selection = fromSource ? this.sourceSelection : this.preparedSelection;
    const next = new Set(selection());
    const key = this.assignmentKey(assignment);
    if (selected) next.add(key);
    else next.delete(key);
    selection.set(next);
  }

  protected isSelected(assignment: RoleAssignmentOutput, fromSource: boolean): boolean {
    return (fromSource ? this.sourceSelection() : this.preparedSelection()).has(
      this.assignmentKey(assignment),
    );
  }

  protected moveSelected(fromSource: boolean): void {
    const selection = fromSource ? this.sourceSelection() : this.preparedSelection();
    const items = (fromSource ? this.sourceAssignments() : this.preparedAssignments()).filter(
      (assignment) => selection.has(this.assignmentKey(assignment)),
    );
    this.moveAssignments(items, fromSource);
  }

  protected moveAll(fromSource: boolean): void {
    this.moveAssignments(
      fromSource ? this.sourceAssignments() : this.preparedAssignments(),
      fromSource,
    );
  }

  protected moveGroup(group: AssignmentApplicationGroup, fromSource: boolean): void {
    if (this.moveAssignments(group.items, fromSource)) this.focusStableMoveAction(fromSource);
  }

  protected moveSubgroup(subgroup: AssignmentSubgroup, fromSource: boolean): void {
    if (this.moveAssignments(subgroup.items, fromSource)) this.focusStableMoveAction(fromSource);
  }

  protected hasMovableSelection(fromSource: boolean): boolean {
    const selection = fromSource ? this.sourceSelection() : this.preparedSelection();
    return (fromSource ? this.sourceAssignments() : this.preparedAssignments()).some(
      (assignment) =>
        selection.has(this.assignmentKey(assignment)) &&
        (!fromSource || this.isEligibleForDestination(assignment)),
    );
  }

  protected hasMovableAssignments(fromSource: boolean): boolean {
    return (fromSource ? this.sourceAssignments() : this.preparedAssignments()).some(
      (assignment) => !fromSource || this.isEligibleForDestination(assignment),
    );
  }

  protected assignmentKey(assignment: RoleAssignmentOutput): string {
    return `${assignment.type}:${assignment.id}`;
  }

  protected assignmentTitle(assignment: RoleAssignmentOutput): string {
    if (assignment.type !== RoleAssignmentType.RESPONSIBLE) return this.copy.authorized;
    return assignment.responsibleType
      ? localizedName(assignment.responsibleType, this.locale)
      : this.copy.unnamedResponsibility;
  }

  protected assignmentDetail(assignment: RoleAssignmentOutput): string {
    if (assignment.type !== RoleAssignmentType.AUTHORIZED) return '';
    return (
      assignment.authorizationTypes?.map((type) => localizedName(type, this.locale)).join(', ') ||
      this.copy.noAuthorizationTypes
    );
  }

  protected groupActionLabel(group: AssignmentApplicationGroup, fromSource: boolean): string {
    return fromSource
      ? this.copy.moveApplication(group.applicationName)
      : this.copy.returnApplication(group.applicationName);
  }

  protected subgroupActionLabel(
    subgroup: AssignmentSubgroup,
    applicationName: string,
    fromSource: boolean,
  ): string {
    return fromSource
      ? this.copy.moveSubgroup(subgroup.label, applicationName)
      : this.copy.returnSubgroup(subgroup.label, applicationName);
  }

  protected openApplyConfirmation(): void {
    if (!this.canApply()) return;
    this.confirmationKind.set('apply');
    this.confirmationVisible.set(true);
  }

  protected confirmAction(): void {
    if (this.confirmationKind() === 'discard-source') {
      const personId = this.pendingSourceId();
      this.confirmationVisible.set(false);
      this.confirmationKind.set(null);
      this.pendingSourceId.set(null);
      this.form.controls.sourcePersonId.setValue(personId, { emitEvent: false });
      this.loadSource(personId);
      return;
    }
    this.applyPreparedAssignments();
  }

  protected cancelConfirmation(): void {
    this.confirmationVisible.set(false);
    this.confirmationKind.set(null);
    this.pendingSourceId.set(null);
  }

  private onSourceControlChange(personId: number | null): void {
    if (personId === this.committedSourceId()) return;
    if (this.preparedAssignments().length > 0) {
      this.pendingSourceId.set(personId);
      this.form.controls.sourcePersonId.setValue(this.committedSourceId(), { emitEvent: false });
      this.confirmationKind.set('discard-source');
      this.confirmationVisible.set(true);
      return;
    }
    this.loadSource(personId);
  }

  private applySourceRequest(request: RoleTransferSourceRequest | null): void {
    if (!request || request.requestId === this.handledSourceRequestId) return;
    this.handledSourceRequestId = request.requestId;
    const option = toResponsiblePersonOption(request.person);
    this.sourceOptions.update((current) => {
      const withoutRequested = current.filter(({ id }) => id !== option.id);
      return [...withoutRequested, option].sort((left, right) =>
        left.label.localeCompare(right.label, this.locale),
      );
    });
    this.confirmationVisible.set(false);
    this.confirmationKind.set(null);
    this.pendingSourceId.set(null);
    this.statusMessage.set('');
    this.form.controls.revoke.setValue(false, { emitEvent: false });
    this.revokeMode.set(false);
    this.form.controls.destinationPersonId.setValidators(Validators.required);
    this.form.controls.sourcePersonId.setValue(request.person.id, { emitEvent: false });
    this.loadSource(request.person.id);
    queueMicrotask(() =>
      this.host.nativeElement.querySelector<HTMLElement>('#role-transfer-source')?.focus(),
    );
  }

  private loadSource(personId: number | null): void {
    this.committedSourceId.set(personId);
    this.sourceAssignments.set([]);
    this.preparedAssignments.set([]);
    this.sourceSelection.set(new Set());
    this.preparedSelection.set(new Set());
    this.sourceRolesLoadFailed.set(false);
    this.resetDestination();
    if (personId !== null) this.sourceRequests.next(personId);
  }

  private onDestinationControlChange(personId: number | null): void {
    this.destinationId.set(personId);
    this.forbiddenAuthorizationApplications.set(new Set());
    this.destinationRolesLoadFailed.set(false);
    this.destinationRolesReady.set(false);
    this.destinationRequests.next(personId);
  }

  private onRevokeChange(revoke: boolean): void {
    this.revokeMode.set(revoke);
    const control = this.form.controls.destinationPersonId;
    if (revoke) {
      control.clearValidators();
      control.setValue(null, { emitEvent: false });
      control.disable({ emitEvent: false });
      this.destinationId.set(null);
      this.destinationRequests.next(null);
      this.forbiddenAuthorizationApplications.set(new Set());
      this.destinationRolesReady.set(false);
    } else {
      control.setValidators(Validators.required);
      this.updateDestinationAvailability();
    }
    control.updateValueAndValidity({ emitEvent: false });
  }

  private resetDestination(): void {
    this.form.controls.destinationPersonId.setValue(null, { emitEvent: false });
    this.destinationId.set(null);
    this.destinationOptions.set([]);
    this.destinationOptionsLoadFailed.set(false);
    this.destinationRolesLoading.set(false);
    this.destinationRolesLoadFailed.set(false);
    this.destinationRolesReady.set(false);
    this.forbiddenAuthorizationApplications.set(new Set());
    this.destinationRequests.next(null);
    this.form.controls.destinationPersonId.disable({ emitEvent: false });
  }

  private moveAssignments(items: RoleAssignmentOutput[], fromSource: boolean): boolean {
    if (items.length === 0) return false;
    const requestedKeys = new Set(items.map((item) => this.assignmentKey(item)));
    const movable = fromSource
      ? items.filter((assignment) => this.isEligibleForDestination(assignment))
      : items;
    const movableKeys = new Set(movable.map((item) => this.assignmentKey(item)));
    if (fromSource) {
      this.sourceAssignments.update((current) =>
        current.filter((item) => !movableKeys.has(this.assignmentKey(item))),
      );
      this.preparedAssignments.update((current) =>
        this.uniqueAssignments([...current, ...movable]),
      );
      this.sourceSelection.update(
        (current) => new Set([...current].filter((key) => !requestedKeys.has(key))),
      );
    } else {
      this.preparedAssignments.update((current) =>
        current.filter((item) => !movableKeys.has(this.assignmentKey(item))),
      );
      this.sourceAssignments.update((current) => this.uniqueAssignments([...current, ...movable]));
      this.preparedSelection.update(
        (current) => new Set([...current].filter((key) => !requestedKeys.has(key))),
      );
    }
    if (movable.length > 0) {
      this.statusMessage.set(this.copy.preparedCount(this.preparedAssignments().length));
      return true;
    }
    return false;
  }

  private isEligibleForDestination(assignment: RoleAssignmentOutput): boolean {
    return (
      this.revokeMode() ||
      assignment.type !== RoleAssignmentType.AUTHORIZED ||
      !this.destinationRolesReady() ||
      !this.forbiddenAuthorizationApplications().has(assignment.applicationId)
    );
  }

  private removeDestinationConflicts(): void {
    if (this.revokeMode()) return;
    const conflicting = this.preparedAssignments().filter(
      (assignment) => !this.isEligibleForDestination(assignment),
    );
    if (conflicting.length === 0) return;
    const conflictingKeys = new Set(conflicting.map((item) => this.assignmentKey(item)));
    this.preparedAssignments.update((current) =>
      current.filter((item) => !conflictingKeys.has(this.assignmentKey(item))),
    );
    this.sourceAssignments.update((current) =>
      this.uniqueAssignments([...current, ...conflicting]),
    );
    this.preparedSelection.update(
      (current) => new Set([...current].filter((key) => !conflictingKeys.has(key))),
    );
    this.statusMessage.set(this.copy.preparedCount(this.preparedAssignments().length));
  }

  private applyPreparedAssignments(): void {
    if (!this.canApply() || this.submitting()) return;
    const input: RoleTransferInput = {
      items: this.preparedAssignments().map(({ id, type }) => ({ id, type })),
      toPersonId: this.revokeMode() ? null : this.destinationId(),
      revoke: this.revokeMode(),
    };
    this.submitting.set(true);
    this.form.disable({ emitEvent: false });
    this.roleTransferService
      .apply(input)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          const success = this.revokeMode() ? this.copy.revoked : this.copy.transferred;
          this.submitting.set(false);
          this.restoreFormAvailability();
          this.confirmationVisible.set(false);
          this.confirmationKind.set(null);
          this.resetAfterSuccess();
          this.statusMessage.set(success);
          this.messageService.add({
            severity: 'success',
            summary: $localize`Correcte`,
            detail: success,
          });
        },
        error: () => {
          this.submitting.set(false);
          this.confirmationVisible.set(false);
          this.confirmationKind.set(null);
          this.showError(this.copy.applyError);
        },
      });
  }

  private resetAfterSuccess(): void {
    const destinationControl = this.form.controls.destinationPersonId;
    this.form.enable({ emitEvent: false });
    destinationControl.setValidators(Validators.required);
    this.form.reset(
      { sourcePersonId: null, destinationPersonId: null, revoke: false },
      { emitEvent: false },
    );
    this.committedSourceId.set(null);
    this.destinationId.set(null);
    this.revokeMode.set(false);
    this.sourceAssignments.set([]);
    this.preparedAssignments.set([]);
    this.sourceSelection.set(new Set());
    this.preparedSelection.set(new Set());
    this.destinationOptions.set([]);
    this.destinationRolesReady.set(false);
    this.forbiddenAuthorizationApplications.set(new Set());
    destinationControl.disable({ emitEvent: false });
  }

  private restoreFormAvailability(): void {
    this.form.controls.sourcePersonId.enable({ emitEvent: false });
    this.form.controls.revoke.enable({ emitEvent: false });
    this.updateDestinationAvailability();
  }

  private updateDestinationAvailability(): void {
    const control = this.form.controls.destinationPersonId;
    const available =
      !this.revokeMode() &&
      this.committedSourceId() !== null &&
      !this.destinationOptionsLoadFailed() &&
      !this.submitting();
    if (available) control.enable({ emitEvent: false });
    else control.disable({ emitEvent: false });
  }

  private groupAssignments(assignments: RoleAssignmentOutput[]): AssignmentApplicationGroup[] {
    const applications = new Map<number, RoleAssignmentOutput[]>();
    assignments.forEach((assignment) => {
      applications.set(assignment.applicationId, [
        ...(applications.get(assignment.applicationId) ?? []),
        assignment,
      ]);
    });
    return [...applications.entries()]
      .map(([applicationId, items]) => {
        const applicationName = items[0]?.applicationName || String(applicationId);
        const responsibilities = items.filter(
          ({ type }) => type === RoleAssignmentType.RESPONSIBLE,
        );
        const authorizations = items.filter(({ type }) => type === RoleAssignmentType.AUTHORIZED);
        const subgroups: AssignmentSubgroup[] = [];
        if (responsibilities.length) {
          subgroups.push({
            type: RoleAssignmentType.RESPONSIBLE,
            label: this.copy.responsibilities,
            items: this.sortAssignments(responsibilities),
          });
        }
        if (authorizations.length) {
          subgroups.push({
            type: RoleAssignmentType.AUTHORIZED,
            label: this.copy.authorizations,
            items: this.sortAssignments(authorizations),
          });
        }
        return {
          applicationId,
          applicationName,
          items: this.sortAssignments(items),
          subgroups,
        };
      })
      .sort((left, right) => left.applicationName.localeCompare(right.applicationName));
  }

  private sortAssignments(assignments: RoleAssignmentOutput[]): RoleAssignmentOutput[] {
    return [...assignments].sort((left, right) =>
      this.assignmentTitle(left).localeCompare(this.assignmentTitle(right)),
    );
  }

  private uniqueAssignments(assignments: RoleAssignmentOutput[]): RoleAssignmentOutput[] {
    return [...new Map(assignments.map((item) => [this.assignmentKey(item), item])).values()];
  }

  private personName(personId: number | null): string {
    if (personId === null) return '';
    return (
      [...this.sourceOptions(), ...this.destinationOptions()].find(({ id }) => id === personId)
        ?.label ?? String(personId)
    );
  }

  private focusStableMoveAction(fromSource: boolean): void {
    queueMicrotask(() => {
      this.host.nativeElement
        .querySelector<HTMLElement>(
          `[data-role-transfer-focus="${fromSource ? 'after-source' : 'after-prepared'}"] button`,
        )
        ?.focus();
    });
  }

  private showError(detail: string): void {
    this.messageService.add({
      severity: 'error',
      summary: RESPONSIBLE_COMMON_COPY.loadErrorSummary,
      detail,
    });
  }
}

function loadResult<T>(source: Observable<T>): Observable<LoadResult<T>> {
  return source.pipe(
    map((value) => ({ value, failed: false })),
    catchError(() => of({ value: null, failed: true })),
  );
}
