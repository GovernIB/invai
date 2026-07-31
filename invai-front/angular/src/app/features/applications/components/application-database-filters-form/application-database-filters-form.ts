import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { SearchFilterGridDirective } from '@components/search-filters/search-filter-grid.directive';
import { FloatLabel } from 'primeng/floatlabel';
import { InputNumber } from 'primeng/inputnumber';
import { InputText } from 'primeng/inputtext';
import { Select } from 'primeng/select';

import { APPLICATION_INFRASTRUCTURE_STATUS_OPTIONS } from '../../applications.constants';
import { SelectOption } from '../../applications.model';
import { ApplicationDatabaseFiltersFormGroup } from '../../forms/application-infrastructure-filter-form.factory';

export interface ApplicationDatabaseFilterLabels {
  environment: string;
  server: string;
  version: string;
  database: string;
  service: string;
  port: string;
  type: string;
  status: string;
  observations: string;
}

@Component({
  selector: 'app-application-database-filters-form',
  standalone: true,
  imports: [
    FloatLabel,
    InputNumber,
    InputText,
    ReactiveFormsModule,
    SearchFilterGridDirective,
    Select,
  ],
  templateUrl: './application-database-filters-form.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationDatabaseFiltersForm {
  form = input.required<ApplicationDatabaseFiltersFormGroup>();
  labels = input.required<ApplicationDatabaseFilterLabels>();
  databaseOptions = input.required<SelectOption<number>[]>();
  environmentOptions = input.required<SelectOption<number>[]>();

  protected readonly statusOptions = APPLICATION_INFRASTRUCTURE_STATUS_OPTIONS;
}
