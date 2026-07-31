import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { SearchFilterGridDirective } from '@components/search-filters/search-filter-grid.directive';
import { FloatLabel } from 'primeng/floatlabel';
import { InputText } from 'primeng/inputtext';
import { Select } from 'primeng/select';

import { DatabaseFiltersFormGroup } from '../../forms/database-filters-form.factory';
import { INFRASTRUCTURE_STATUS_OPTIONS } from '../../systems.constants';
import {
  DatabaseVendorCatalogOption,
  ServerCatalogOption,
} from '../../systems.model';

export interface DatabaseFilterLabels {
  server: string;
  service: string;
  databaseType: string;
  status: string;
}

@Component({
  selector: 'app-database-filters-form',
  standalone: true,
  imports: [
    FloatLabel,
    InputText,
    ReactiveFormsModule,
    SearchFilterGridDirective,
    Select,
  ],
  templateUrl: './database-filters-form.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DatabaseFiltersForm {
  form = input.required<DatabaseFiltersFormGroup>();
  labels = input.required<DatabaseFilterLabels>();
  serverOptions = input.required<ServerCatalogOption[]>();
  databaseTypeOptions = input.required<DatabaseVendorCatalogOption[]>();

  protected readonly statusOptions = INFRASTRUCTURE_STATUS_OPTIONS;
}
