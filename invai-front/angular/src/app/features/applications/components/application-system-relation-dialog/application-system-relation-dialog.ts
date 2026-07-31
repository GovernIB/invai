import {
  ChangeDetectionStrategy,
  Component,
  computed,
  input,
  model,
  output,
} from '@angular/core';
import {
  CrudEntityDialog,
  CrudEntityDialogAriaLabels,
  CrudEntityDialogMode,
} from '@components/crud-entity-dialog/crud-entity-dialog';
import { PaginatedList } from '@models/table.model';
import { TableLazyLoadEvent } from 'primeng/table';

import { APPLICATION_SYSTEM_CATALOG_COLUMNS } from '../../applications.constants';
import {
  ApplicationInfrastructureCatalogRow,
  ApplicationSystemCatalogRow,
} from '../../applications.model';
import { ApplicationSystemRelationFormGroup } from '../../forms/application-infrastructure-relation-forms.factory';
import { ApplicationInfrastructureCatalogTable } from '../application-infrastructure-catalog-table/application-infrastructure-catalog-table';
import {
  APPLICATION_SYSTEM_RELATION_DIALOG_ARIA_LABELS,
  APPLICATION_SYSTEM_RELATION_DIALOG_REQUIRED,
  APPLICATION_SYSTEM_RELATION_DIALOG_TITLES,
} from './application-system-relation-dialog.i18n';

@Component({
  selector: 'app-application-system-relation-dialog',
  standalone: true,
  imports: [ApplicationInfrastructureCatalogTable, CrudEntityDialog],
  templateUrl: './application-system-relation-dialog.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationSystemRelationDialog {
  visible = model(false);
  mode = input.required<CrudEntityDialogMode>();
  form = input.required<ApplicationSystemRelationFormGroup>();
  catalog = input.required<PaginatedList<ApplicationSystemCatalogRow>>();
  first = input(0);
  isLoading = input(false);
  isSaving = input(false);
  canEdit = input(false);
  canDeactivate = input(true);

  submitForm = output<void>();
  closed = output<void>();
  edit = output<void>();
  cancelEdit = output<void>();
  deactivate = output<void>();
  pageChange = output<TableLazyLoadEvent>();

  protected readonly columns = APPLICATION_SYSTEM_CATALOG_COLUMNS;
  protected readonly title = computed(
    () => APPLICATION_SYSTEM_RELATION_DIALOG_TITLES[this.mode()],
  );
  protected readonly requiredError = APPLICATION_SYSTEM_RELATION_DIALOG_REQUIRED;
  protected readonly actionAriaLabels: CrudEntityDialogAriaLabels =
    APPLICATION_SYSTEM_RELATION_DIALOG_ARIA_LABELS;
  protected readonly displayedCatalog = computed<
    PaginatedList<ApplicationInfrastructureCatalogRow>
  >(() => {
    const selection = this.form().controls.system.value;
    return this.mode() === 'view' && selection
      ? { items: [selection], total: 1 }
      : this.catalog();
  });
  protected readonly hasUnsavedChanges = () =>
    this.mode() === 'edit' && this.form().dirty;
  protected readonly isInvalid = computed(() => {
    const control = this.form().controls.system;
    return control.invalid && (control.dirty || control.touched);
  });

  protected select(row: ApplicationInfrastructureCatalogRow): void {
    if (this.mode() === 'view') return;
    this.form().controls.system.setValue(row as ApplicationSystemCatalogRow);
    this.form().controls.system.markAsDirty();
  }
}
