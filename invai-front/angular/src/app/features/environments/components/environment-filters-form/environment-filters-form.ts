import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { SearchFilterGridDirective } from '@components/search-filters/search-filter-grid.directive';
import { FloatLabel } from 'primeng/floatlabel';
import { InputText } from 'primeng/inputtext';
import { Select } from 'primeng/select';

import { ENVIRONMENT_STATUS_OPTIONS } from '../../environments.constants';
import { EnvironmentFiltersFormGroup } from '../../forms/environment-filters-form.factory';

export interface EnvironmentFilterLabels {
  code: string;
  name: string;
  nameEs: string;
  status: string;
}

@Component({
  selector: 'app-environment-filters-form',
  standalone: true,
  imports: [
    FloatLabel,
    InputText,
    ReactiveFormsModule,
    SearchFilterGridDirective,
    Select,
  ],
  templateUrl: './environment-filters-form.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EnvironmentFiltersForm {
  form = input.required<EnvironmentFiltersFormGroup>();
  labels = input.required<EnvironmentFilterLabels>();

  protected readonly statusOptions = ENVIRONMENT_STATUS_OPTIONS;
}
