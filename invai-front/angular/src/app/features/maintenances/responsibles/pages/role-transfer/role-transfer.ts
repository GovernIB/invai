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
import { isStructuredBadRequest } from '@core/models/api-error.model';
import { localizedName } from '@shared/utils/localized-name.utils';
import { MessageService, PrimeIcons } from 'primeng/api';
import { Button } from 'primeng/button';
import { ToggleSwitch } from 'primeng/toggleswitch';
import { Observable, Subject, catchError, map, of, switchMap } from 'rxjs';

import {
  createRoleTransferForm,
  selectedRoleTransferPersonValidator,
} from '../../forms/responsible-forms.factory';
import { ROLE_TRANSFER_PERSON_SEARCH_PARAMS } from '../../responsibles.constants';
import { RESPONSIBLE_COMMON_COPY, ROLE_TRANSFER_COPY } from '../../responsibles.i18n';
import {
  ResponsiblePerson,
  ResponsiblePersonCombinedSearchOutput,
  RoleAssignmentOutput,
  RoleAssignmentType,
  RoleTransferDestinationOption,
  RoleTransferInput,
  RoleTransferPersonControlValue,
  RoleTransferPersonOption,
  RoleTransferSourceRequest,
  SoffidPersonCandidate,
} from '../../responsibles.model';
import {
  toRoleTransferDestinationOption,
  toRoleTransferSourceOption,
} from '../../responsibles.utils';
import { ResponsiblePeopleService } from '../../services/responsible-people.service';
import { RoleTransferService } from '../../services/role-transfer.service';
import { RoleTransferPersonField } from './role-transfer-person-field';

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

@Component({
  selector: 'app-role-transfer',
  standalone: true,
  imports: [
    Button,
    ConfirmationDialogComponent,
    ReactiveFormsModule,
    RoleTransferPersonField,
    ToggleSwitch,
  ],
  templateUrl: './role-transfer.html',
  styleUrl: './role-transfer.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RoleTransfer implements OnInit {
  initialPeopleSearch = input<ResponsiblePersonCombinedSearchOutput | null>(null);
  initialPeopleSearchFailed = input(false);
  sourceRequest = input<RoleTransferSourceRequest | null>(null);

  protected readonly copy = ROLE_TRANSFER_COPY;
  protected readonly PrimeIcons = PrimeIcons;
  protected readonly RoleAssignmentType = RoleAssignmentType;
  protected readonly form = createRoleTransferForm(inject(FormBuilder));
  protected readonly sourceOptions = signal<RoleTransferPersonOption[]>([]);
  protected readonly destinationOptions = signal<RoleTransferDestinationOption[]>([]);
  protected readonly sourceAssignments = signal<RoleAssignmentOutput[]>([]);
  protected readonly preparedAssignments = signal<RoleAssignmentOutput[]>([]);
  protected readonly sourceSelection = signal<Set<string>>(new Set());
  protected readonly preparedSelection = signal<Set<string>>(new Set());
  protected readonly sourceSearchLoading = signal(false);
  protected readonly sourceSearchFailed = signal(false);
  protected readonly sourceSearched = signal(false);
  protected readonly sourceRolesLoading = signal(false);
  protected readonly sourceRolesLoadFailed = signal(false);
  protected readonly destinationSearchLoading = signal(false);
  protected readonly destinationSearchFailed = signal(false);
  protected readonly destinationSearched = signal(false);
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
  private readonly sourceRequests = new Subject<number | null>();
  private readonly sourceSearchRequests = new Subject<string>();
  private readonly destinationSearchRequests = new Subject<string>();
  private readonly destinationRequests = new Subject<number | null>();
  protected readonly committedSourceId = signal<number | null>(null);
  private readonly sourceSearchQuery = signal('');
  private readonly destinationSearchQuery = signal('');
  private readonly selectedSource = signal<RoleTransferPersonOption | null>(null);
  private readonly selectedDestination = signal<RoleTransferDestinationOption | null>(null);
  private destinationSearchResult: ResponsiblePersonCombinedSearchOutput | null = null;
  private readonly revokeMode = signal(false);
  private readonly forbiddenAuthorizationApplications = signal<Set<number>>(new Set());
  private readonly pendingSource = signal<RoleTransferPersonOption | null>(null);
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
    const destination = this.selectedDestination();
    const source = this.selectedSource();
    const destinationIsReady =
      this.revokeMode() ||
      (!!destination &&
        destination.id !== this.committedSourceId() &&
        this.normalizeEmail(destination.email) !== this.normalizeEmail(source?.email ?? '') &&
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
      return this.pendingSource() === null
        ? this.copy.discardClearMessage
        : this.copy.discardConfirmMessage(this.pendingSource()?.label ?? '');
    }
    const count = this.preparedAssignments().length;
    const source = this.selectedSource()?.label ?? '';
    return this.revokeMode()
      ? this.copy.revokeConfirmMessage(count, source)
      : this.copy.transferConfirmMessage(
          count,
          source,
          this.selectedDestination()?.label ?? '',
        );
  });
  protected readonly confirmationLabel = computed(() => {
    if (this.confirmationKind() === 'discard-source') return this.copy.confirmDiscard;
    return this.revokeMode() ? this.copy.confirmRevoke : this.copy.confirmTransfer;
  });
  protected readonly confirmationSeverity = computed(() =>
    this.confirmationKind() === 'discard-source' || this.revokeMode() ? 'danger' : 'primary',
  );
  protected readonly confirmationIcon = computed(() => {
    if (this.confirmationKind() === 'discard-source') return PrimeIcons.TIMES;
    return this.revokeMode() ? PrimeIcons.TRASH : PrimeIcons.ARROW_RIGHT;
  });

  constructor() {
    effect(() => {
      const request = this.sourceRequest();
      if (this.initialized) this.applySourceRequest(request);
    });

    this.form.controls.sourcePerson.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((person) => this.onSourceControlChange(person));
    this.form.controls.destinationPerson.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((person) => this.onDestinationControlChange(person));
    this.form.controls.revoke.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((revoke) => this.onRevokeChange(revoke));

    this.sourceRequests
      .pipe(
        switchMap((personId) => {
          if (personId === null) {
            return of({
              personId,
              assignments: { value: [], failed: false } as LoadResult<RoleAssignmentOutput[]>,
            });
          }
          this.sourceRolesLoading.set(true);
          this.sourceRolesLoadFailed.set(false);
          this.form.controls.destinationPerson.disable({ emitEvent: false });
          return loadResult(this.roleTransferService.getAssignments(personId)).pipe(
            map((assignments) => ({ personId, assignments })),
          );
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(({ personId, assignments }) => {
        if (personId !== this.committedSourceId()) return;
        this.sourceRolesLoading.set(false);
        this.sourceRolesLoadFailed.set(assignments.failed);
        this.sourceAssignments.set(assignments.value ?? []);
        this.updateDestinationAvailability();
      });

    this.sourceSearchRequests
      .pipe(
        switchMap((query) => this.searchPeople(query, 'source')),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(({ result }) => {
        if (result === null) return;
        this.sourceSearchLoading.set(false);
        this.sourceSearchFailed.set(result.failed);
        this.sourceSearched.set(true);
        if (!result.value) return;
        this.setSourceOptions(result.value.database.content);
      });

    this.destinationSearchRequests
      .pipe(
        switchMap((query) => this.searchPeople(query, 'destination')),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(({ result }) => {
        if (result === null) return;
        this.destinationSearchLoading.set(false);
        this.destinationSearchFailed.set(result.failed);
        this.destinationSearched.set(true);
        if (!result.value) return;
        this.destinationSearchResult = result.value;
        this.refreshDestinationOptions();
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
        if (personId !== (this.selectedDestination()?.id ?? null)) return;
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
    const initialPeopleSearch = this.initialPeopleSearch();
    const initialPeopleSearchFailed = this.initialPeopleSearchFailed();
    this.sourceSearchFailed.set(initialPeopleSearchFailed);
    this.destinationSearchFailed.set(initialPeopleSearchFailed);
    this.sourceSearched.set(initialPeopleSearch !== null);
    this.destinationSearched.set(initialPeopleSearch !== null);
    if (initialPeopleSearch) {
      this.setSourceOptions(initialPeopleSearch.database.content);
      this.destinationSearchResult = initialPeopleSearch;
      this.refreshDestinationOptions();
    }
    this.initialized = true;
    this.applySourceRequest(this.sourceRequest());
  }

  protected retrySourceCatalog(): void {
    this.requestPeopleSearch(this.sourceSearchQuery(), 'source', true);
  }

  protected retrySourceLoad(): void {
    const personId = this.committedSourceId();
    if (personId) this.sourceRequests.next(personId);
  }

  protected retryDestinationLoad(): void {
    const personId = this.selectedDestination()?.id;
    if (personId) this.destinationRequests.next(personId);
  }

  protected searchDestinations(query: string): void {
    this.requestPeopleSearch(query, 'destination');
  }

  protected searchSources(query: string): void {
    this.requestPeopleSearch(query, 'source');
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
      const person = this.pendingSource();
      this.confirmationVisible.set(false);
      this.confirmationKind.set(null);
      this.pendingSource.set(null);
      this.form.controls.sourcePerson.setValue(person, { emitEvent: false });
      this.loadSource(person);
      return;
    }
    this.applyPreparedAssignments();
  }

  protected cancelConfirmation(): void {
    this.confirmationVisible.set(false);
    this.confirmationKind.set(null);
    this.pendingSource.set(null);
  }

  private onSourceControlChange(value: RoleTransferPersonControlValue): void {
    const person = value && typeof value !== 'string' && value.id !== null ? value : null;
    if ((person?.id ?? null) === this.committedSourceId()) return;
    if (this.preparedAssignments().length > 0) {
      this.pendingSource.set(person);
      this.form.controls.sourcePerson.setValue(this.selectedSource(), { emitEvent: false });
      this.confirmationKind.set('discard-source');
      this.confirmationVisible.set(true);
      return;
    }
    this.loadSource(person);
  }

  private applySourceRequest(request: RoleTransferSourceRequest | null): void {
    if (!request || request.requestId === this.handledSourceRequestId) return;
    this.handledSourceRequestId = request.requestId;
    const option = toRoleTransferSourceOption(request.person, 'database');
    this.sourceOptions.update((current) => {
      const withoutRequested = current.filter(({ id }) => id !== option.id);
      return [...withoutRequested, option].sort((left, right) =>
        left.label.localeCompare(right.label, this.locale),
      );
    });
    this.confirmationVisible.set(false);
    this.confirmationKind.set(null);
    this.pendingSource.set(null);
    this.statusMessage.set('');
    this.form.controls.revoke.setValue(false, { emitEvent: false });
    this.revokeMode.set(false);
    this.setDestinationValidators();
    this.form.controls.sourcePerson.setValue(option, { emitEvent: false });
    this.loadSource(option);
    queueMicrotask(() =>
      this.host.nativeElement.querySelector<HTMLElement>('#role-transfer-source')?.focus(),
    );
  }

  private loadSource(person: RoleTransferPersonOption | null): void {
    const personId = person?.id ?? null;
    this.selectedSource.set(person);
    this.committedSourceId.set(personId);
    this.sourceAssignments.set([]);
    this.preparedAssignments.set([]);
    this.sourceSelection.set(new Set());
    this.preparedSelection.set(new Set());
    this.sourceRolesLoadFailed.set(false);
    this.resetDestination();
    this.sourceRequests.next(personId);
  }

  private onDestinationControlChange(value: RoleTransferPersonControlValue): void {
    const destination = value && typeof value !== 'string' ? value : null;
    this.selectedDestination.set(destination);
    this.forbiddenAuthorizationApplications.set(new Set());
    this.destinationRolesLoadFailed.set(false);
    this.destinationRolesReady.set(false);
    if (!destination) {
      this.destinationRequests.next(null);
      return;
    }
    if (destination.id === null) {
      this.destinationRequests.next(null);
      this.destinationRolesLoading.set(false);
      this.destinationRolesReady.set(true);
      return;
    }
    this.destinationRequests.next(destination.id);
  }

  private onRevokeChange(revoke: boolean): void {
    this.revokeMode.set(revoke);
    const control = this.form.controls.destinationPerson;
    if (revoke) {
      control.clearValidators();
      control.setValue(null, { emitEvent: false });
      control.disable({ emitEvent: false });
      this.selectedDestination.set(null);
      this.destinationRequests.next(null);
      this.forbiddenAuthorizationApplications.set(new Set());
      this.destinationRolesReady.set(false);
    } else {
      this.setDestinationValidators();
      this.updateDestinationAvailability();
    }
    control.updateValueAndValidity({ emitEvent: false });
  }

  private resetDestination(): void {
    this.form.controls.destinationPerson.setValue(null, { emitEvent: false });
    this.selectedDestination.set(null);
    this.refreshDestinationOptions();
    this.destinationSearchLoading.set(false);
    this.destinationSearchFailed.set(false);
    this.destinationSearched.set(this.destinationSearchResult !== null);
    this.destinationRolesLoading.set(false);
    this.destinationRolesLoadFailed.set(false);
    this.destinationRolesReady.set(false);
    this.forbiddenAuthorizationApplications.set(new Set());
    this.destinationRequests.next(null);
    this.form.controls.destinationPerson.disable({ emitEvent: false });
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
      toPersonEmailAddress: this.revokeMode()
        ? null
        : (this.selectedDestination()?.email.trim() ?? null),
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
        error: (error) => {
          this.submitting.set(false);
          this.confirmationVisible.set(false);
          this.confirmationKind.set(null);
          if (isStructuredBadRequest(error)) return;
          this.showError(this.copy.applyError);
        },
      });
  }

  private resetAfterSuccess(): void {
    const destinationControl = this.form.controls.destinationPerson;
    this.form.enable({ emitEvent: false });
    this.setDestinationValidators();
    this.form.reset(
      { sourcePerson: null, destinationPerson: null, revoke: false },
      { emitEvent: false },
    );
    this.committedSourceId.set(null);
    this.selectedSource.set(null);
    this.selectedDestination.set(null);
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
    this.form.controls.sourcePerson.enable({ emitEvent: false });
    this.form.controls.revoke.enable({ emitEvent: false });
    this.updateDestinationAvailability();
  }

  private updateDestinationAvailability(): void {
    const control = this.form.controls.destinationPerson;
    const available =
      !this.revokeMode() &&
      this.committedSourceId() !== null &&
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

  private searchPeople(query: string, kind: 'source' | 'destination') {
    const search = query.trim();
    const loading = kind === 'source' ? this.sourceSearchLoading : this.destinationSearchLoading;
    const failed = kind === 'source' ? this.sourceSearchFailed : this.destinationSearchFailed;
    const searched = kind === 'source' ? this.sourceSearched : this.destinationSearched;
    failed.set(false);
    if (search.length > 0 && search.length < 3) {
      loading.set(false);
      searched.set(false);
      return of({ result: null as LoadResult<never> | null });
    }

    loading.set(true);
    searched.set(false);
    return loadResult(
      this.peopleService.searchCombined({
        ...ROLE_TRANSFER_PERSON_SEARCH_PARAMS,
        search: search || undefined,
      }),
    ).pipe(map((result) => ({ result })));
  }

  private setSourceOptions(database: ResponsiblePerson[]): void {
    const selected = this.selectedSource();
    this.sourceOptions.set(
      this.withSelectedOption(
        database
          .map((person) => toRoleTransferSourceOption(person, 'database'))
          .filter((person) => this.normalizeEmail(person.email).length > 0),
        selected,
      ).sort((left, right) => left.label.localeCompare(right.label, this.locale)),
    );
  }

  private setDestinationOptions(
    database: ResponsiblePerson[],
    soffid: SoffidPersonCandidate[],
  ): void {
    const source = this.selectedSource();
    const sourceEmail = this.normalizeEmail(source?.email ?? '');
    const selectedDestination = this.personOption(
      this.form.controls.destinationPerson.getRawValue(),
    );
    this.destinationOptions.set(
      this.withSelectedOption(
        this.mergePersonOptions(database, soffid, 'destination').filter(
          (person) => person.id !== source?.id && this.normalizeEmail(person.email) !== sourceEmail,
        ),
        selectedDestination,
      ).sort((left, right) => left.label.localeCompare(right.label, this.locale)),
    );
  }

  private refreshDestinationOptions(): void {
    const result = this.destinationSearchResult;
    if (!result) {
      this.destinationOptions.set([]);
      return;
    }
    this.setDestinationOptions(result.database.content, result.soffid.content);
  }

  private requestPeopleSearch(
    query: string,
    kind: 'source' | 'destination',
    force = false,
  ): void {
    const search = query.trim();
    const currentQuery =
      kind === 'source' ? this.sourceSearchQuery : this.destinationSearchQuery;
    const loading = kind === 'source' ? this.sourceSearchLoading : this.destinationSearchLoading;
    const failed = kind === 'source' ? this.sourceSearchFailed : this.destinationSearchFailed;
    const searched = kind === 'source' ? this.sourceSearched : this.destinationSearched;
    if (
      !force &&
      search === currentQuery() &&
      (loading() || (searched() && !failed()))
    ) {
      return;
    }

    currentQuery.set(search);
    if (kind === 'source') this.sourceSearchRequests.next(search);
    else this.destinationSearchRequests.next(search);
  }

  private withSelectedOption(
    options: RoleTransferPersonOption[],
    selected: RoleTransferPersonOption | null,
  ): RoleTransferPersonOption[] {
    if (
      !selected ||
      options.some(
        (option) => this.normalizeEmail(option.email) === this.normalizeEmail(selected.email),
      )
    ) {
      return options;
    }
    return [selected, ...options];
  }

  private personOption(value: RoleTransferPersonControlValue): RoleTransferPersonOption | null {
    return value !== null && typeof value !== 'string' ? value : null;
  }

  private mergePersonOptions(
    database: ResponsiblePerson[],
    soffid: SoffidPersonCandidate[],
    kind: 'source' | 'destination',
  ): RoleTransferPersonOption[] {
    const byEmail = new Map<string, RoleTransferPersonOption>();
    database.forEach((person) => {
      const email = this.normalizeEmail(person.email);
      if (!email) return;
      byEmail.set(
        email,
        kind === 'source'
          ? toRoleTransferSourceOption(person, 'database')
          : toRoleTransferDestinationOption(person, 'database'),
      );
    });
    soffid.forEach((person) => {
      const email = this.normalizeEmail(person.email);
      if (!email || byEmail.has(email)) return;
      byEmail.set(
        email,
        kind === 'source'
          ? toRoleTransferSourceOption(person, 'soffid')
          : toRoleTransferDestinationOption(person, 'soffid'),
      );
    });
    return [...byEmail.values()];
  }

  private setDestinationValidators(): void {
    this.form.controls.destinationPerson.setValidators([
      Validators.required,
      selectedRoleTransferPersonValidator,
    ]);
  }

  private normalizeEmail(email: string): string {
    return email.trim().toLowerCase();
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
