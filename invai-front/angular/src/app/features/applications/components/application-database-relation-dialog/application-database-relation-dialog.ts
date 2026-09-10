import { SectionActionsComponent } from '@components/section-actions/section-actions.component';
import { SearchFiltersComponent } from '@components/search-filters/search-filters.component';
import { DatabaseFiltersForm } from '@features/systems/components/database-filters-form/database-filters-form';
import { DatabaseFiltersFormGroup } from '@features/systems/forms/database-filters-form.factory';
import { ServerCatalogOption, DatabaseVendorCatalogOption } from '@features/systems/systems.model';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  input,
  model,
  output,
  signal,
  ViewEncapsulation,
} from '@angular/core';
import {
  CrudEntityDialog,
  CrudEntityDialogAriaLabels,
  CrudEntityDialogMode,
} from '@components/crud-entity-dialog/crud-entity-dialog';
import { PaginatedList } from '@models/table.model';
import { TableLazyLoadEvent } from 'primeng/table';

import { APPLICATION_DATABASE_CATALOG_COLUMNS } from '../../applications.constants';
import {
  ApplicationDatabaseCatalogRow,
  ApplicationInfrastructureCatalogRow,
} from '../../applications.model';
import { ApplicationDatabaseRelationFormGroup } from '../../forms/application-infrastructure-relation-forms.factory';
import { ApplicationInfrastructureCatalogTable } from '../application-infrastructure-catalog-table/application-infrastructure-catalog-table';
import {
  APPLICATION_DATABASE_CATALOG_SEARCH_LABEL,
  APPLICATION_DATABASE_CATALOG_FILTER_LABELS,
  APPLICATION_DATABASE_RELATION_DIALOG_ARIA_LABELS,
  APPLICATION_DATABASE_RELATION_DIALOG_REQUIRED,
  APPLICATION_DATABASE_RELATION_DIALOG_TITLES,
} from './application-database-relation-dialog.i18n';

@Component({
  selector: 'app-application-database-relation-dialog',
  standalone: true,
  imports: [
    ApplicationInfrastructureCatalogTable,
    CrudEntityDialog,
    SectionActionsComponent,
    SearchFiltersComponent,
    DatabaseFiltersForm,
  ],
  templateUrl: './application-database-relation-dialog.html',
  styleUrl: '../application-infrastructure-catalog-dialog.scss',
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationDatabaseRelationDialog {
  visible = model(false);
  mode = input.required<Exclude<CrudEntityDialogMode, 'edit'>>();
  form = input.required<ApplicationDatabaseRelationFormGroup>();
  catalog = input.required<PaginatedList<ApplicationDatabaseCatalogRow>>();
  filtersForm = input.required<DatabaseFiltersFormGroup>();
  serverOptions = input<ServerCatalogOption[]>([]);
  databaseTypeOptions = input<DatabaseVendorCatalogOption[]>([]);
  quickSearch = input('');
  filtersSelected = input(0);
  quickSearchChange = output<string>();
  filterSearch = output<void>();
  filterReset = output<void>();
  protected readonly isFiltersCollapsed = signal(true);
  protected readonly searchLabel = APPLICATION_DATABASE_CATALOG_SEARCH_LABEL;
  protected readonly filterLabels = APPLICATION_DATABASE_CATALOG_FILTER_LABELS;
  first = input(0);
  isLoading = input(false);
  isSaving = input(false);

  submitForm = output<void>();
  closed = output<void>();
  pageChange = output<TableLazyLoadEvent>();

  protected readonly columns = APPLICATION_DATABASE_CATALOG_COLUMNS;
  protected readonly title = computed(
    () => APPLICATION_DATABASE_RELATION_DIALOG_TITLES[this.mode()],
  );
  protected readonly requiredError = APPLICATION_DATABASE_RELATION_DIALOG_REQUIRED;
  protected readonly actionAriaLabels: CrudEntityDialogAriaLabels =
    APPLICATION_DATABASE_RELATION_DIALOG_ARIA_LABELS;
  protected readonly displayedCatalog = computed<
    PaginatedList<ApplicationInfrastructureCatalogRow>
  >(() => {
    const selection = this.form().controls.database.value;
    return this.mode() === 'view' && selection ? { items: [selection], total: 1 } : this.catalog();
  });
  protected readonly isInvalid = computed(() => {
    const control = this.form().controls.database;
    return control.invalid && (control.dirty || control.touched);
  });

  protected select(row: ApplicationInfrastructureCatalogRow): void {
    if (this.mode() === 'view') return;
    this.form().controls.database.setValue(row as ApplicationDatabaseCatalogRow);
    this.form().controls.database.markAsDirty();
  }
}
