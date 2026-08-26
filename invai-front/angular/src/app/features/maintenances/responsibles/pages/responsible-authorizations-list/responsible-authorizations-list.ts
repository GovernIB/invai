import { ChangeDetectionStrategy, Component, LOCALE_ID, inject } from '@angular/core';
import { ConfirmationDialogComponent } from '@components/confirmation-dialog/confirmation-dialog.component';
import { SearchFiltersComponent } from '@components/search-filters/search-filters.component';
import { SectionActionsComponent } from '@components/section-actions/section-actions.component';
import { SpringPage } from '@models/page.model';
import { TableLazyLoadEvent } from 'primeng/table';
import { Observable, switchMap, throwError } from 'rxjs';

import { ResponsibleAuthorizationDialog } from '../../components/responsible-authorization-dialog/responsible-authorization-dialog';
import { ResponsibleAuthorizationFiltersForm } from '../../components/responsible-authorization-filters-form/responsible-authorization-filters-form';
import { ResponsibleAuthorizationsTable } from '../../components/responsible-maintenance-tables/responsible-maintenance-tables';
import {
  ResponsibleAuthorizationFormGroup,
  createResponsibleAuthorizationFiltersForm,
  createResponsibleAuthorizationForm,
} from '../../forms/responsible-forms.factory';
import { RESPONSIBLE_AUTHORIZATION_COLUMNS } from '../../responsibles.constants';
import { RESPONSIBLE_AUTHORIZATION_COPY, RESPONSIBLE_COMMON_COPY } from '../../responsibles.i18n';
import {
  ResponsibleAuthorization,
  ResponsibleAuthorizationFilters,
  ResponsibleAuthorizationInput,
  ResponsibleAuthorizationPageParams,
} from '../../responsibles.model';
import { ResponsibleAuthorizationTypesService } from '../../services/responsible-authorization-types.service';
import { toResponsibleAuthorizationPageParams } from '../../responsibles.utils';
import { localizedName } from '@shared/utils/localized-name.utils';
import { ResponsibleMaintenanceListBase } from '../responsible-maintenance-list.base';

@Component({
  selector: 'app-responsible-authorizations-list',
  standalone: true,
  imports: [
    ConfirmationDialogComponent,
    ResponsibleAuthorizationsTable,
    ResponsibleAuthorizationDialog,
    ResponsibleAuthorizationFiltersForm,
    SearchFiltersComponent,
    SectionActionsComponent,
  ],
  templateUrl: './responsible-authorizations-list.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ResponsibleAuthorizationsList extends ResponsibleMaintenanceListBase<
  ResponsibleAuthorization,
  ResponsibleAuthorizationInput,
  ResponsibleAuthorizationFilters,
  ResponsibleAuthorizationPageParams,
  ResponsibleAuthorizationFormGroup
> {
  protected override readonly ALL_TABLE_COLUMNS = RESPONSIBLE_AUTHORIZATION_COLUMNS;
  protected override readonly filtersForm = createResponsibleAuthorizationFiltersForm(this.fb);
  protected override readonly entityForm = createResponsibleAuthorizationForm(this.fb);
  protected override readonly copy = RESPONSIBLE_AUTHORIZATION_COPY;
  protected readonly filterLabels = {
    name: this.copy.name,
    nameEs: this.copy.nameEs,
    status: RESPONSIBLE_COMMON_COPY.status,
  };

  private readonly service = inject(ResponsibleAuthorizationTypesService);
  private readonly locale = inject(LOCALE_ID);

  protected override entityLabel(item: ResponsibleAuthorization | null): string {
    return item ? localizedName(item, this.locale) : '';
  }

  protected override prepareEntityForm(item: ResponsibleAuthorization | null): void {
    this.entityForm.reset({ name: item?.name ?? '', nameEs: item?.nameEs ?? '' });
  }

  protected override toInput(): ResponsibleAuthorizationInput {
    return {
      name: this.entityForm.controls.name.value.trim(),
      nameEs: this.entityForm.controls.nameEs.value.trim(),
    };
  }

  protected override toPageParams(
    filters: ResponsibleAuthorizationFilters,
    event: TableLazyLoadEvent | undefined,
    quickSearch: string,
  ): ResponsibleAuthorizationPageParams {
    return toResponsibleAuthorizationPageParams(filters, event, quickSearch);
  }

  protected override listRequest(
    params: ResponsibleAuthorizationPageParams,
  ): Observable<SpringPage<ResponsibleAuthorization>> {
    return this.service.getPage(params);
  }

  protected override getByIdRequest(id: number): Observable<ResponsibleAuthorization> {
    return this.service.getById(id);
  }

  protected override createRequest(
    input: ResponsibleAuthorizationInput,
  ): Observable<ResponsibleAuthorization> {
    return this.service.create(input);
  }

  protected override updateRequest(
    id: number,
    input: ResponsibleAuthorizationInput,
  ): Observable<ResponsibleAuthorization> {
    return this.service.update(id, input);
  }

  protected override deactivateRequest(id: number): Observable<void> {
    return this.service.deactivate(id);
  }

  protected override restoreRequest(id: number): Observable<ResponsibleAuthorization> {
    return this.service
      .getById(id)
      .pipe(
        switchMap((authorization) =>
          this.service
            .getPage({
              page: 0,
              size: 1000,
              statusId: 1,
              name: authorization.name,
              nameEs: authorization.nameEs,
            })
            .pipe(
              switchMap((page) =>
                page.content.some(
                  (candidate) =>
                    candidate.id !== id &&
                    (candidate.name.trim().toLocaleLowerCase() ===
                      authorization.name.trim().toLocaleLowerCase() ||
                      candidate.nameEs.trim().toLocaleLowerCase() ===
                        authorization.nameEs.trim().toLocaleLowerCase()),
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
