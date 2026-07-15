import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { SearchFilterGridDirective } from '@components/search-filters/search-filter-grid.directive';
import { FloatLabel } from 'primeng/floatlabel';
import { InputText } from 'primeng/inputtext';
import { Select } from 'primeng/select';
import { ToggleSwitch } from 'primeng/toggleswitch';

import { APPLICATION_STATUS_OPTIONS } from '../../applications.constants';
import { ApplicationFiltersFormGroup } from '../../forms/application-form.factory';
import { ApplicationSelectOptions } from '../../services/application-options.service';

export interface ApplicationFilterLabels {
  prefix: string;
  application: string;
  category: string;
  informationSystem: string;
  scope: string;
  commission: string;
  administrativeUnit: string;
  status: string;
  description: string;
  incomplete: string;
}

@Component({
  selector: 'app-application-filters-form',
  standalone: true,
  imports: [
    FloatLabel,
    InputText,
    ReactiveFormsModule,
    SearchFilterGridDirective,
    Select,
    ToggleSwitch,
  ],
  templateUrl: './application-filters-form.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationFiltersForm {
  form = input.required<ApplicationFiltersFormGroup>();
  labels = input.required<ApplicationFilterLabels>();
  options = input<Partial<ApplicationSelectOptions> | null>(null);

  protected readonly statusOptions = APPLICATION_STATUS_OPTIONS;
}
