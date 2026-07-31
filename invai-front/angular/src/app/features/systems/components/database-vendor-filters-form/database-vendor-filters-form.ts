import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { SearchFilterGridDirective } from '@components/search-filters/search-filter-grid.directive';
import { FloatLabel } from 'primeng/floatlabel';
import { InputNumber } from 'primeng/inputnumber';
import { InputText } from 'primeng/inputtext';
import { Select } from 'primeng/select';

import { DatabaseVendorFiltersFormGroup } from '../../forms/database-vendor-filters-form.factory';
import { INFRASTRUCTURE_STATUS_OPTIONS } from '../../systems.constants';

@Component({
  selector: 'app-database-vendor-filters-form',
  standalone: true,
  imports: [
    FloatLabel,
    InputNumber,
    InputText,
    ReactiveFormsModule,
    SearchFilterGridDirective,
    Select,
  ],
  templateUrl: './database-vendor-filters-form.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DatabaseVendorFiltersForm {
  form = input.required<DatabaseVendorFiltersFormGroup>();

  protected readonly statusOptions = INFRASTRUCTURE_STATUS_OPTIONS;
  protected readonly labels = {
    name: $localize`Proveïdor`,
    defaultPort: $localize`Port per defecte`,
    status: $localize`Estat`,
  };
}
