import {
  ChangeDetectionStrategy,
  Component,
  OnInit,
  computed,
  inject,
  input,
  output,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { DestroyRef } from '@angular/core';
import { ConfirmationDialogComponent } from '@components/confirmation-dialog/confirmation-dialog.component';
import { SearchFiltersComponent } from '@components/search-filters/search-filters.component';
import { SectionActionsComponent } from '@components/section-actions/section-actions.component';
import { SpringPage } from '@models/page.model';
import { ActionParams } from '@models/table.model';
import { TableLazyLoadEvent } from 'primeng/table';
import { Observable, finalize, switchMap, throwError } from 'rxjs';

import { ResponsiblePersonDialog } from '../../components/responsible-person-dialog/responsible-person-dialog';
import { ResponsiblePersonFiltersForm } from '../../components/responsible-person-filters-form/responsible-person-filters-form';
import {
  ResponsibleMaintenanceTableAction,
  ResponsiblePeopleTable,
} from '../../components/responsible-maintenance-tables/responsible-maintenance-tables';
import {
  ResponsiblePersonFormGroup,
  createResponsiblePersonFiltersForm,
  createResponsiblePersonForm,
} from '../../forms/responsible-forms.factory';
import { RESPONSIBLE_PERSON_COLUMNS } from '../../responsibles.constants';
import { RESPONSIBLE_COMMON_COPY, RESPONSIBLE_PERSON_COPY } from '../../responsibles.i18n';
import {
  ResponsibleCompanyOption,
  ResponsiblePerson,
  ResponsiblePersonFilters,
  ResponsiblePersonInput,
  ResponsiblePersonPageParams,
} from '../../responsibles.model';
import { ResponsibleCompaniesService } from '../../services/responsible-companies.service';
import { ResponsibleDataChangesService } from '../../services/responsible-data-changes.service';
import { ResponsiblePeopleService } from '../../services/responsible-people.service';
import { RoleTransferService } from '../../services/role-transfer.service';
import { toResponsiblePersonPageParams } from '../../responsibles.utils';
import { responsiblePersonFullName } from '../../responsibles.utils';
import { ResponsibleMaintenanceListBase } from '../responsible-maintenance-list.base';

@Component({
  selector: 'app-responsible-people-list',
  standalone: true,
  imports: [
    ConfirmationDialogComponent,
    ResponsiblePeopleTable,
    ResponsiblePersonDialog,
    ResponsiblePersonFiltersForm,
    SearchFiltersComponent,
    SectionActionsComponent,
  ],
  templateUrl: './responsible-people-list.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ResponsiblePeopleList
  extends ResponsibleMaintenanceListBase<
    ResponsiblePerson,
    ResponsiblePersonInput,
    ResponsiblePersonFilters,
    ResponsiblePersonPageParams,
    ResponsiblePersonFormGroup
  >
  implements OnInit
{
  protected override readonly ALL_TABLE_COLUMNS = RESPONSIBLE_PERSON_COLUMNS;
  protected override readonly filtersForm = createResponsiblePersonFiltersForm(this.fb);
  protected override readonly entityForm = createResponsiblePersonForm(this.fb);
  protected override readonly copy = RESPONSIBLE_PERSON_COPY;
  protected readonly activeCompanyOptions = signal<ResponsibleCompanyOption[]>([]);
  protected readonly allCompanyOptions = signal<ResponsibleCompanyOption[]>([]);
  protected readonly disableAdd = computed(() => this.activeCompanyOptions().length === 0);
  protected readonly isAssignmentsCheckPending = signal(false);
  protected readonly pendingPersonHasAssignments = signal(false);
  protected readonly selectedCompanyName = computed(() =>
    this.selectedEntity()?.personalCaib
      ? this.copy.personalCaib
      : (this.selectedEntity()?.company?.name ?? ''),
  );
  protected readonly canMutateSelected = computed(() => !this.selectedEntity()?.personalCaib);
  protected readonly filterLabels = {
    company: this.copy.company,
    firstName: this.copy.firstName,
    lastName: this.copy.lastName,
    email: this.copy.email,
    status: RESPONSIBLE_COMMON_COPY.status,
    companyFilterAriaLabel: this.copy.companyFilterAriaLabel,
  };
  initialActiveCompanyOptions = input<ResponsibleCompanyOption[]>([]);
  initialAllCompanyOptions = input<ResponsibleCompanyOption[]>([]);
  initialCompanyOptionsLoadFailed = input(false);
  transferRequested = output<ResponsiblePerson>();

  private readonly service = inject(ResponsiblePeopleService);
  private readonly companiesService = inject(ResponsibleCompaniesService);
  private readonly changes = inject(ResponsibleDataChangesService);
  private readonly roleTransferService = inject(RoleTransferService);
  private readonly localDestroyRef = inject(DestroyRef);

  override ngOnInit(): void {
    this.activeCompanyOptions.set(this.initialActiveCompanyOptions());
    this.allCompanyOptions.set(this.initialAllCompanyOptions());
    if (this.initialCompanyOptionsLoadFailed()) this.showError(this.copy.loadError);
    this.changes.companies
      .pipe(takeUntilDestroyed(this.localDestroyRef))
      .subscribe(() => {
        this.loadCompanyOptions();
        this.refreshCurrentPage();
      });
    super.ngOnInit();
  }

  protected override entityLabel(item: ResponsiblePerson | null): string {
    return item ? responsiblePersonFullName(item) : '';
  }

  protected override prepareEntityForm(item: ResponsiblePerson | null): void {
    this.entityForm.reset({
      companyId: item?.company?.id ?? null,
      firstName: item?.firstName ?? '',
      lastName: item?.lastName ?? '',
      email: item?.email ?? '',
    });
  }

  protected override toInput(): ResponsiblePersonInput {
    const value = this.entityForm.getRawValue();
    return {
      companyId: value.companyId!,
      firstName: value.firstName.trim(),
      lastName: value.lastName.trim(),
      email: value.email.trim(),
      personalCaib: false,
    };
  }

  protected override toPageParams(
    filters: ResponsiblePersonFilters,
    event: TableLazyLoadEvent | undefined,
    quickSearch: string,
  ): ResponsiblePersonPageParams {
    return toResponsiblePersonPageParams(filters, event, quickSearch);
  }

  protected override listRequest(
    params: ResponsiblePersonPageParams,
  ): Observable<SpringPage<ResponsiblePerson>> {
    return this.service.getPage(params);
  }

  protected override getByIdRequest(id: number): Observable<ResponsiblePerson> {
    return this.service.getById(id);
  }

  protected override createRequest(input: ResponsiblePersonInput): Observable<ResponsiblePerson> {
    return this.service.create(input);
  }

  protected override updateRequest(
    id: number,
    input: ResponsiblePersonInput,
  ): Observable<ResponsiblePerson> {
    return this.service.update(id, input);
  }

  protected override deactivateRequest(id: number): Observable<void> {
    return this.service.deactivate(id);
  }

  protected override deactivateSelectedEntity(): void {
    const selected = this.selectedEntity();
    if (!selected || selected.deletedAt) return;
    this.checkAssignmentsBeforeDeactivation(selected);
  }

  protected override restoreRequest(id: number): Observable<ResponsiblePerson> {
    return this.service.getById(id).pipe(
      switchMap((person) => {
        if (person.personalCaib) return throwError(() => new Error(this.copy.caibReadOnly));
        if (!person.company || person.company.deletedAt) {
          return throwError(() => new Error(this.copy.restoreBlocked));
        }
        return this.service
          .getPage({
            page: 0,
            size: 1000,
            statusId: 1,
            firstName: person.firstName,
            lastName: person.lastName,
            email: person.email,
          })
          .pipe(
            switchMap((page) =>
              page.content.some(
                (candidate) =>
                  candidate.id !== person.id &&
                  (responsiblePersonFullName(candidate).toLocaleLowerCase() ===
                    responsiblePersonFullName(person).toLocaleLowerCase() ||
                    candidate.email.trim().toLocaleLowerCase() ===
                      person.email.trim().toLocaleLowerCase()),
              )
                ? throwError(() => new Error(RESPONSIBLE_COMMON_COPY.duplicateRestore))
                : this.service.reactivate(id),
            ),
          );
      }),
    );
  }

  protected override mutationErrorDetail(error: unknown, fallback: string): string {
    return error instanceof Error && error.message ? error.message : fallback;
  }

  protected override onTableAction(event: ActionParams<ResponsiblePerson>): void {
    if (event.params.personalCaib && event.action !== ResponsibleMaintenanceTableAction.View) {
      this.showError(this.copy.caibReadOnly);
      return;
    }
    if (event.action === ResponsibleMaintenanceTableAction.Deactivate) {
      this.checkAssignmentsBeforeDeactivation(event.params);
      return;
    }
    super.onTableAction(event);
  }

  protected deleteMessage(): string {
    const person = this.pendingDelete();
    if (this.pendingPersonHasAssignments()) {
      return this.copy.deactivateWithAssignmentsMessage(this.entityLabel(person));
    }
    return this.copy.deactivateMessage(this.entityLabel(person));
  }

  protected transferAriaLabel(): string {
    return this.copy.transferAriaLabel(this.entityLabel(this.pendingDelete()));
  }

  protected transferPendingPerson(): void {
    const person = this.pendingDelete();
    if (!person || !this.pendingPersonHasAssignments()) return;
    this.closeDeleteDialog();
    this.closeEntityDialog();
    this.transferRequested.emit(person);
  }

  protected override closeDeleteDialog(): void {
    super.closeDeleteDialog();
    if (!this.isDeleting()) this.pendingPersonHasAssignments.set(false);
  }

  private loadCompanyOptions(): void {
    this.companiesService
      .getOptions(true)
      .subscribe((options) => this.activeCompanyOptions.set(options));
    this.companiesService
      .getOptions(false)
      .subscribe((options) => this.allCompanyOptions.set(options));
  }

  private checkAssignmentsBeforeDeactivation(person: ResponsiblePerson): void {
    if (this.isAssignmentsCheckPending() || this.isDeleting()) return;
    this.pendingPersonHasAssignments.set(false);
    this.isAssignmentsCheckPending.set(true);
    this.roleTransferService
      .getAssignments(person.id)
      .pipe(
        finalize(() => this.isAssignmentsCheckPending.set(false)),
        takeUntilDestroyed(this.localDestroyRef),
      )
      .subscribe({
        next: (assignments) => {
          this.pendingDelete.set(person);
          this.pendingPersonHasAssignments.set(assignments.length > 0);
          this.isDeleteDialogVisible.set(true);
        },
        error: () => this.showError(this.copy.assignmentsCheckError),
      });
  }
}
