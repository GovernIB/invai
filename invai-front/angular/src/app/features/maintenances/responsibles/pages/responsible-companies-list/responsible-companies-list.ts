import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { ConfirmationDialogComponent } from '@components/confirmation-dialog/confirmation-dialog.component';
import { SearchFiltersComponent } from '@components/search-filters/search-filters.component';
import { SectionActionsComponent } from '@components/section-actions/section-actions.component';
import { SpringPage } from '@models/page.model';
import { TableLazyLoadEvent } from 'primeng/table';
import { Observable, switchMap, throwError } from 'rxjs';

import { ResponsibleNameDialog } from '../../components/responsible-name-dialog/responsible-name-dialog';
import { ResponsibleNameFiltersForm } from '../../components/responsible-name-filters-form/responsible-name-filters-form';
import { ResponsibleCompaniesTable } from '../../components/responsible-maintenance-tables/responsible-maintenance-tables';
import {
  ResponsibleNameFormGroup,
  createResponsibleNameFiltersForm,
  createResponsibleNameForm,
} from '../../forms/responsible-forms.factory';
import { RESPONSIBLE_COMPANY_COLUMNS } from '../../responsibles.constants';
import { RESPONSIBLE_COMMON_COPY, RESPONSIBLE_COMPANY_COPY } from '../../responsibles.i18n';
import {
  ResponsibleCompany,
  ResponsibleNameFilters,
  ResponsibleNameInput,
  ResponsibleNamePageParams,
} from '../../responsibles.model';
import { ResponsibleCompaniesService } from '../../services/responsible-companies.service';
import { toResponsibleNamePageParams } from '../../responsibles.utils';
import { ResponsibleMaintenanceListBase } from '../responsible-maintenance-list.base';

@Component({
  selector: 'app-responsible-companies-list',
  standalone: true,
  imports: [
    ConfirmationDialogComponent,
    ResponsibleCompaniesTable,
    ResponsibleNameDialog,
    ResponsibleNameFiltersForm,
    SearchFiltersComponent,
    SectionActionsComponent,
  ],
  templateUrl: './responsible-companies-list.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ResponsibleCompaniesList extends ResponsibleMaintenanceListBase<
  ResponsibleCompany,
  ResponsibleNameInput,
  ResponsibleNameFilters,
  ResponsibleNamePageParams,
  ResponsibleNameFormGroup
> {
  protected override readonly ALL_TABLE_COLUMNS = RESPONSIBLE_COMPANY_COLUMNS;
  protected override readonly filtersForm = createResponsibleNameFiltersForm(this.fb);
  protected override readonly entityForm = createResponsibleNameForm(this.fb);
  protected override readonly copy = RESPONSIBLE_COMPANY_COPY;
  protected readonly commonCopy = RESPONSIBLE_COMMON_COPY;
  protected readonly filterLabels = {
    name: this.copy.name,
    status: this.commonCopy.status,
  };

  private readonly service = inject(ResponsibleCompaniesService);

  protected override entityLabel(item: ResponsibleCompany | null): string {
    return item?.name ?? '';
  }

  protected override prepareEntityForm(item: ResponsibleCompany | null): void {
    this.entityForm.reset({ name: item?.name ?? '' });
  }

  protected override toInput(): ResponsibleNameInput {
    return { name: this.entityForm.controls.name.value.trim() };
  }

  protected override toPageParams(
    filters: ResponsibleNameFilters,
    event: TableLazyLoadEvent | undefined,
    quickSearch: string,
  ): ResponsibleNamePageParams {
    return toResponsibleNamePageParams(filters, event, quickSearch);
  }

  protected override listRequest(
    params: ResponsibleNamePageParams,
  ): Observable<SpringPage<ResponsibleCompany>> {
    return this.service.getPage(params);
  }

  protected override getByIdRequest(id: number): Observable<ResponsibleCompany> {
    return this.service.getById(id);
  }

  protected override createRequest(input: ResponsibleNameInput): Observable<ResponsibleCompany> {
    return this.service.create(input);
  }

  protected override updateRequest(
    id: number,
    input: ResponsibleNameInput,
  ): Observable<ResponsibleCompany> {
    return this.service.update(id, input);
  }

  protected override deactivateRequest(id: number): Observable<void> {
    return this.service.deactivate(id);
  }

  protected override restoreRequest(id: number): Observable<ResponsibleCompany> {
    return this.service
      .getById(id)
      .pipe(
        switchMap((company) =>
          this.service
            .getPage({ page: 0, size: 1000, name: company.name, statusId: 1 })
            .pipe(
              switchMap((page) =>
                page.content.some(
                  (candidate) =>
                    candidate.id !== id &&
                    candidate.name.trim().toLocaleLowerCase() ===
                      company.name.trim().toLocaleLowerCase(),
                )
                  ? throwError(() => new Error(RESPONSIBLE_COMMON_COPY.duplicateRestore))
                  : this.service.reactivate(id),
              ),
            ),
        ),
      );
  }

  protected override mutationErrorDetail(error: unknown, fallback: string): string {
    return error instanceof Error && error.message ? error.message : fallback;
  }
}
