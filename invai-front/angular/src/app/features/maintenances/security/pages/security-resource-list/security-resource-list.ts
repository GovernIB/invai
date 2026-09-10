import { ChangeDetectionStrategy, Component, inject, input, OnInit } from '@angular/core';
import { ConfirmationDialogComponent } from '@components/confirmation-dialog/confirmation-dialog.component';
import { CrudEntityDialogMode } from '@components/crud-entity-dialog/crud-entity-dialog';
import { SearchFiltersComponent } from '@components/search-filters/search-filters.component';
import { SectionActionsComponent } from '@components/section-actions/section-actions.component';
import { SpringPage } from '@models/page.model';
import { KeyLabel } from '@models/table.model';
import { TableLazyLoadEvent } from 'primeng/table';
import { Observable } from 'rxjs';

import { SecurityResourceDialog } from '../../components/security-resource-dialog/security-resource-dialog';
import { SecurityResourceFilters } from '../../components/security-resource-filters/security-resource-filters';
import { SecurityResourceTable } from '../../components/security-resource-table/security-resource-table';
import {
  createSecurityResourceFiltersForm,
  createSecurityResourceForm,
  SecurityResourceFormGroup,
} from '../../forms/security-resource-forms.factory';
import { SECURITY_RESOURCE_DEFINITIONS, SecurityResourceDefinition } from '../../security.constants';
import {
  SECURITY_ADD_ARIA_LABEL,
  SECURITY_COLUMNS_BUTTON_ARIA_LABEL,
  SECURITY_COLUMNS_INPUT_ID,
  SECURITY_CREATED,
  SECURITY_DIALOG_ACTIONS,
  SECURITY_DEACTIVATE_DIALOG_MESSAGE,
  SECURITY_DEACTIVATE_DIALOG_CANCEL_ARIA_LABEL,
  SECURITY_DEACTIVATE_DIALOG_CANCEL_LABEL,
  SECURITY_DEACTIVATE_DIALOG_CONFIRM_ARIA_LABEL,
  SECURITY_DEACTIVATE_DIALOG_CONFIRM_LABEL,
  SECURITY_DEACTIVATE_DIALOG_TITLE,
  SECURITY_DEACTIVATED,
  SECURITY_DEACTIVATE_ERROR,
  SECURITY_ENTITY_LOAD_ERROR,
  SECURITY_FILTER_LABELS,
  SECURITY_FILTERS_BUTTON_ARIA_LABEL,
  SECURITY_LIST_LOAD_ERROR,
  SECURITY_QUICK_SEARCH_ARIA_LABEL,
  SECURITY_QUICK_SEARCH_PLACEHOLDER,
  SECURITY_RESTORED,
  SECURITY_RESTORE_ERROR,
  SECURITY_SAVE_ERROR,
  SECURITY_FORBIDDEN,
  SECURITY_UPDATED,
} from '../../security.i18n';
import {
  SecurityResource,
  SecurityResourceFilters as SecurityFilters,
  SecurityResourceInput,
  SecurityResourceKey,
  SecurityResourcePageParams,
} from '../../security.model';
import {
  EnsRequirementsService,
  IdentityProvidersService,
  PersonalDataProcessingService,
  SecurityMeasureTypesService,
  SecurityResourceService,
  WebContextsService,
} from '../../services/security-resource.services';
import {
  ResponsibleMaintenanceListBase,
  ResponsibleMaintenanceListCopy,
} from '../../../responsibles/pages/responsible-maintenance-list.base';

const DEFAULT_PAGE_SIZE = 10;

@Component({
  selector: 'app-security-resource-list',
  standalone: true,
  imports: [
    ConfirmationDialogComponent,
    SearchFiltersComponent,
    SectionActionsComponent,
    SecurityResourceDialog,
    SecurityResourceFilters,
    SecurityResourceTable,
  ],
  templateUrl: './security-resource-list.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SecurityResourceList
  extends ResponsibleMaintenanceListBase<
    SecurityResource,
    SecurityResourceInput,
    SecurityFilters,
    SecurityResourcePageParams,
    SecurityResourceFormGroup
  >
  implements OnInit
{
  resource = input.required<SecurityResourceKey>();

  protected override ALL_TABLE_COLUMNS!: KeyLabel[];
  protected override filtersForm!: ReturnType<typeof createSecurityResourceFiltersForm>;
  protected override entityForm!: SecurityResourceFormGroup;
  protected override copy!: ResponsibleMaintenanceListCopy;
  protected definition!: SecurityResourceDefinition;
  protected readonly filterLabels = SECURITY_FILTER_LABELS;
  protected readonly deactivateDialogCancelLabel = SECURITY_DEACTIVATE_DIALOG_CANCEL_LABEL;
  protected readonly deactivateDialogConfirmLabel = SECURITY_DEACTIVATE_DIALOG_CONFIRM_LABEL;
  protected readonly deactivateDialogCancelAriaLabel =
    SECURITY_DEACTIVATE_DIALOG_CANCEL_ARIA_LABEL;
  protected readonly deactivateDialogConfirmAriaLabel =
    SECURITY_DEACTIVATE_DIALOG_CONFIRM_ARIA_LABEL;

  private service!: SecurityResourceService;
  private readonly ensRequirementsService = inject(EnsRequirementsService);
  private readonly identityProvidersService = inject(IdentityProvidersService);
  private readonly personalDataProcessingService = inject(PersonalDataProcessingService);
  private readonly securityMeasureTypesService = inject(SecurityMeasureTypesService);
  private readonly webContextsService = inject(WebContextsService);

  override ngOnInit(): void {
    this.definition = SECURITY_RESOURCE_DEFINITIONS[this.resource()];
    this.ALL_TABLE_COLUMNS = this.definition.columns;
    this.filtersForm = createSecurityResourceFiltersForm(this.fb);
    this.entityForm = createSecurityResourceForm(this.fb, this.definition.bilingual);
    this.service = this.resolveService(this.definition.key);
    this.copy = this.createCopy(this.definition);
    super.ngOnInit();
  }

  protected override entityLabel(item: SecurityResource | null): string {
    return item?.name ?? '';
  }

  protected override prepareEntityForm(item: SecurityResource | null): void {
    this.entityForm.reset({ name: item?.name ?? '', nameEs: item?.nameEs ?? '' });
  }

  protected override toInput(): SecurityResourceInput {
    const value = this.entityForm.getRawValue();
    return this.definition.bilingual
      ? { name: value.name.trim(), nameEs: value.nameEs.trim() }
      : { name: value.name.trim() };
  }

  protected override parseFormToFilters(): SecurityFilters {
    const value = this.filtersForm.getRawValue();
    return {
      name: value.name?.trim() || null,
      nameEs: this.definition.bilingual ? value.nameEs?.trim() || null : null,
      status: value.status,
    };
  }

  protected override toPageParams(
    filters: SecurityFilters,
    event: TableLazyLoadEvent | undefined,
    quickSearch: string,
  ): SecurityResourcePageParams {
    const first = event?.first ?? 0;
    const size = event?.rows ?? DEFAULT_PAGE_SIZE;
    const sortField = Array.isArray(event?.sortField) ? event.sortField[0] : event?.sortField;
    const sortDirection = event?.sortOrder === -1 ? 'desc' : event?.sortOrder === 1 ? 'asc' : null;
    return {
      page: Math.floor(first / size),
      size,
      sort: sortField && sortDirection ? `${sortField},${sortDirection}` : undefined,
      name: filters.name || undefined,
      nameEs: this.definition.bilingual ? filters.nameEs || undefined : undefined,
      statusId: filters.status ?? undefined,
      search: quickSearch.trim() || undefined,
    };
  }

  protected override listRequest(
    params: SecurityResourcePageParams,
  ): Observable<SpringPage<SecurityResource>> {
    return this.service.getAll(params);
  }

  protected override getByIdRequest(id: number): Observable<SecurityResource> {
    return this.service.getById(id);
  }

  protected override createRequest(input: SecurityResourceInput): Observable<SecurityResource> {
    return this.service.create(input);
  }

  protected override updateRequest(
    id: number,
    input: SecurityResourceInput,
  ): Observable<SecurityResource> {
    return this.service.update(id, input);
  }

  protected override deactivateRequest(id: number): Observable<void> {
    return this.service.deactivate(id);
  }

  protected override restoreRequest(id: number): Observable<SecurityResource> {
    return this.service.reactivate(id);
  }

  private resolveService(key: SecurityResourceKey): SecurityResourceService {
    switch (key) {
      case 'ens-requirement':
        return this.ensRequirementsService;
      case 'identity-provider':
        return this.identityProvidersService;
      case 'personal-data-processing':
        return this.personalDataProcessingService;
      case 'security-measure-type':
        return this.securityMeasureTypesService;
      case 'web-context':
        return this.webContextsService;
    }
  }

  private createCopy(definition: SecurityResourceDefinition): ResponsibleMaintenanceListCopy {
    return {
      title: definition.plural,
      quickSearchPlaceholder: SECURITY_QUICK_SEARCH_PLACEHOLDER,
      quickSearchAriaLabel: SECURITY_QUICK_SEARCH_ARIA_LABEL(definition.plural),
      addAriaLabel: SECURITY_ADD_ARIA_LABEL(definition.singular),
      columnsInputId: SECURITY_COLUMNS_INPUT_ID(definition.key),
      filtersButtonAriaLabel: SECURITY_FILTERS_BUTTON_ARIA_LABEL(definition.plural),
      columnsButtonAriaLabel: SECURITY_COLUMNS_BUTTON_ARIA_LABEL(definition.plural),
      actions: SECURITY_DIALOG_ACTIONS(definition.singular),
      deactivateTitle: SECURITY_DEACTIVATE_DIALOG_TITLE,
      deactivateMessage: SECURITY_DEACTIVATE_DIALOG_MESSAGE,
      loadError: SECURITY_LIST_LOAD_ERROR(definition.plural),
      entityLoadError: SECURITY_ENTITY_LOAD_ERROR,
      created: SECURITY_CREATED,
      updated: SECURITY_UPDATED,
      deactivated: SECURITY_DEACTIVATED,
      restored: SECURITY_RESTORED,
      saveError: SECURITY_SAVE_ERROR,
      deactivateError: SECURITY_DEACTIVATE_ERROR,
      restoreError: SECURITY_RESTORE_ERROR,
      forbidden: SECURITY_FORBIDDEN,
    };
  }
}
