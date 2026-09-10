import { ChangeDetectionStrategy, Component, inject, input, OnInit } from '@angular/core';
import { ConfirmationDialogComponent } from '@components/confirmation-dialog/confirmation-dialog.component';
import { CrudEntityDialogMode } from '@components/crud-entity-dialog/crud-entity-dialog';
import { SearchFiltersComponent } from '@components/search-filters/search-filters.component';
import { SectionActionsComponent } from '@components/section-actions/section-actions.component';
import { SpringPage } from '@models/page.model';
import { KeyLabel } from '@models/table.model';
import { TableLazyLoadEvent } from 'primeng/table';
import { Observable } from 'rxjs';

import { AccessibilityResourceDialog } from '../../components/accessibility-resource-dialog/accessibility-resource-dialog';
import { AccessibilityResourceFilters } from '../../components/accessibility-resource-filters/accessibility-resource-filters';
import { AccessibilityResourceTable } from '../../components/accessibility-resource-table/accessibility-resource-table';
import {
  createAccessibilityResourceFiltersForm,
  createAccessibilityResourceForm,
  AccessibilityResourceFormGroup,
} from '../../forms/accessibility-resource-forms.factory';
import {
  ACCESSIBILITY_RESOURCE_DEFINITIONS,
  AccessibilityResourceDefinition,
} from '../../accessibility.constants';
import {
  ACCESSIBILITY_ADD_ARIA_LABEL,
  ACCESSIBILITY_COLUMNS_BUTTON_ARIA_LABEL,
  ACCESSIBILITY_COLUMNS_INPUT_ID,
  ACCESSIBILITY_CREATED,
  ACCESSIBILITY_DIALOG_ACTIONS,
  ACCESSIBILITY_DEACTIVATE_DIALOG_MESSAGE,
  ACCESSIBILITY_DEACTIVATE_DIALOG_CANCEL_ARIA_LABEL,
  ACCESSIBILITY_DEACTIVATE_DIALOG_CANCEL_LABEL,
  ACCESSIBILITY_DEACTIVATE_DIALOG_CONFIRM_ARIA_LABEL,
  ACCESSIBILITY_DEACTIVATE_DIALOG_CONFIRM_LABEL,
  ACCESSIBILITY_DEACTIVATE_DIALOG_TITLE,
  ACCESSIBILITY_DEACTIVATED,
  ACCESSIBILITY_DEACTIVATE_ERROR,
  ACCESSIBILITY_ENTITY_LOAD_ERROR,
  ACCESSIBILITY_FILTER_LABELS,
  ACCESSIBILITY_FILTERS_BUTTON_ARIA_LABEL,
  ACCESSIBILITY_LIST_LOAD_ERROR,
  ACCESSIBILITY_QUICK_SEARCH_ARIA_LABEL,
  ACCESSIBILITY_QUICK_SEARCH_PLACEHOLDER,
  ACCESSIBILITY_RESTORED,
  ACCESSIBILITY_RESTORE_ERROR,
  ACCESSIBILITY_SAVE_ERROR,
  ACCESSIBILITY_FORBIDDEN,
  ACCESSIBILITY_UPDATED,
} from '../../accessibility.i18n';
import {
  AccessibilityResource,
  AccessibilityResourceFilters as AccessibilityFilters,
  AccessibilityResourceInput,
  AccessibilityResourceKey,
  AccessibilityResourcePageParams,
} from '../../accessibility.model';
import {
  ClassificationSegmentsService,
  ComplianceSituationsService,
  AccessibilityResourceService,
} from '../../services/accessibility-resource.services';
import {
  ResponsibleMaintenanceListBase,
  ResponsibleMaintenanceListCopy,
} from '../../../responsibles/pages/responsible-maintenance-list.base';

const DEFAULT_PAGE_SIZE = 10;

@Component({
  selector: 'app-accessibility-resource-list',
  standalone: true,
  imports: [
    ConfirmationDialogComponent,
    SearchFiltersComponent,
    SectionActionsComponent,
    AccessibilityResourceDialog,
    AccessibilityResourceFilters,
    AccessibilityResourceTable,
  ],
  templateUrl: './accessibility-resource-list.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AccessibilityResourceList
  extends ResponsibleMaintenanceListBase<
    AccessibilityResource,
    AccessibilityResourceInput,
    AccessibilityFilters,
    AccessibilityResourcePageParams,
    AccessibilityResourceFormGroup
  >
  implements OnInit
{
  resource = input.required<AccessibilityResourceKey>();
  initialForbidden = input(false);
  protected override initializeResults(): void {
    if (this.initialForbidden()) {
      this.updateSearchState();
      return;
    }
    super.initializeResults();
  }

  protected override ALL_TABLE_COLUMNS!: KeyLabel[];
  protected override filtersForm!: ReturnType<typeof createAccessibilityResourceFiltersForm>;
  protected override entityForm!: AccessibilityResourceFormGroup;
  protected override copy!: ResponsibleMaintenanceListCopy;
  protected definition!: AccessibilityResourceDefinition;
  protected readonly filterLabels = ACCESSIBILITY_FILTER_LABELS;
  protected readonly deactivateDialogCancelLabel = ACCESSIBILITY_DEACTIVATE_DIALOG_CANCEL_LABEL;
  protected readonly deactivateDialogConfirmLabel = ACCESSIBILITY_DEACTIVATE_DIALOG_CONFIRM_LABEL;
  protected readonly deactivateDialogCancelAriaLabel =
    ACCESSIBILITY_DEACTIVATE_DIALOG_CANCEL_ARIA_LABEL;
  protected readonly deactivateDialogConfirmAriaLabel =
    ACCESSIBILITY_DEACTIVATE_DIALOG_CONFIRM_ARIA_LABEL;

  private service!: AccessibilityResourceService;
  private readonly classificationSegmentsService = inject(ClassificationSegmentsService);
  private readonly complianceSituationsService = inject(ComplianceSituationsService);

  override ngOnInit(): void {
    this.definition = ACCESSIBILITY_RESOURCE_DEFINITIONS[this.resource()];
    this.ALL_TABLE_COLUMNS = this.definition.columns;
    this.filtersForm = createAccessibilityResourceFiltersForm(this.fb);
    this.entityForm = createAccessibilityResourceForm(this.fb, this.definition.bilingual);
    this.service = this.resolveService(this.definition.key);
    this.copy = this.createCopy(this.definition);
    super.ngOnInit();
  }

  protected override entityLabel(item: AccessibilityResource | null): string {
    return item?.name ?? '';
  }

  protected override prepareEntityForm(item: AccessibilityResource | null): void {
    this.entityForm.reset({ name: item?.name ?? '', nameEs: item?.nameEs ?? '' });
  }

  protected override toInput(): AccessibilityResourceInput {
    const value = this.entityForm.getRawValue();
    return { name: value.name.trim(), nameEs: value.nameEs.trim() };
  }

  protected override parseFormToFilters(): AccessibilityFilters {
    const value = this.filtersForm.getRawValue();
    return {
      name: value.name?.trim() || null,
      nameEs: this.definition.bilingual ? value.nameEs?.trim() || null : null,
      status: value.status,
    };
  }

  protected override toPageParams(
    filters: AccessibilityFilters,
    event: TableLazyLoadEvent | undefined,
    quickSearch: string,
  ): AccessibilityResourcePageParams {
    const first = event?.first ?? 0;
    const size = event?.rows ?? DEFAULT_PAGE_SIZE;
    const sortField = Array.isArray(event?.sortField) ? event.sortField[0] : event?.sortField;
    const sortDirection = event?.sortOrder === -1 ? 'desc' : event?.sortOrder === 1 ? 'asc' : null;
    return {
      page: Math.floor(first / size),
      size,
      sort: sortField && sortDirection ? `${sortField},${sortDirection}` : 'id,asc',
      name: filters.name || undefined,
      nameEs: this.definition.bilingual ? filters.nameEs || undefined : undefined,
      statusId: filters.status ?? undefined,
      search: quickSearch.trim() || undefined,
    };
  }

  protected override listRequest(
    params: AccessibilityResourcePageParams,
  ): Observable<SpringPage<AccessibilityResource>> {
    return this.service.getAll(params);
  }

  protected override getByIdRequest(id: number): Observable<AccessibilityResource> {
    return this.service.getById(id);
  }

  protected override createRequest(
    input: AccessibilityResourceInput,
  ): Observable<AccessibilityResource> {
    return this.service.create(input);
  }

  protected override updateRequest(
    id: number,
    input: AccessibilityResourceInput,
  ): Observable<AccessibilityResource> {
    return this.service.update(id, input);
  }

  protected override deactivateRequest(id: number): Observable<void> {
    return this.service.deactivate(id);
  }

  protected override restoreRequest(id: number): Observable<AccessibilityResource> {
    return this.service.reactivate(id);
  }

  private resolveService(key: AccessibilityResourceKey): AccessibilityResourceService {
    return key === 'classification-segment'
      ? this.classificationSegmentsService
      : this.complianceSituationsService;
  }

  private createCopy(definition: AccessibilityResourceDefinition): ResponsibleMaintenanceListCopy {
    return {
      title: definition.plural,
      quickSearchPlaceholder: ACCESSIBILITY_QUICK_SEARCH_PLACEHOLDER,
      quickSearchAriaLabel: ACCESSIBILITY_QUICK_SEARCH_ARIA_LABEL(definition.plural),
      addAriaLabel: ACCESSIBILITY_ADD_ARIA_LABEL(definition.singular),
      columnsInputId: ACCESSIBILITY_COLUMNS_INPUT_ID(definition.key),
      filtersButtonAriaLabel: ACCESSIBILITY_FILTERS_BUTTON_ARIA_LABEL(definition.plural),
      columnsButtonAriaLabel: ACCESSIBILITY_COLUMNS_BUTTON_ARIA_LABEL(definition.plural),
      actions: ACCESSIBILITY_DIALOG_ACTIONS(definition.singular),
      deactivateTitle: ACCESSIBILITY_DEACTIVATE_DIALOG_TITLE,
      deactivateMessage: ACCESSIBILITY_DEACTIVATE_DIALOG_MESSAGE,
      loadError: ACCESSIBILITY_LIST_LOAD_ERROR(definition.plural),
      entityLoadError: ACCESSIBILITY_ENTITY_LOAD_ERROR,
      created: ACCESSIBILITY_CREATED,
      updated: ACCESSIBILITY_UPDATED,
      deactivated: ACCESSIBILITY_DEACTIVATED,
      restored: ACCESSIBILITY_RESTORED,
      saveError: ACCESSIBILITY_SAVE_ERROR,
      deactivateError: ACCESSIBILITY_DEACTIVATE_ERROR,
      restoreError: ACCESSIBILITY_RESTORE_ERROR,
      forbidden: ACCESSIBILITY_FORBIDDEN,
    };
  }
}
