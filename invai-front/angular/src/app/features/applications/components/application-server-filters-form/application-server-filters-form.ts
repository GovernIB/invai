import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { SearchFilterGridDirective } from '@components/search-filters/search-filter-grid.directive';
import { FloatLabel } from 'primeng/floatlabel';
import { InputNumber } from 'primeng/inputnumber';
import { InputText } from 'primeng/inputtext';
import { Select } from 'primeng/select';

import { APPLICATION_INFRASTRUCTURE_STATUS_OPTIONS } from '../../applications.constants';
import { SelectOption } from '../../applications.model';
import { ApplicationServerFiltersFormGroup } from '../../forms/application-infrastructure-filter-form.factory';

export interface ApplicationServerFilterLabels {
  environment: string;
  server: string;
  instance: string;
  port: string;
  version: string;
  status: string;
  observations: string;
}

@Component({
  selector: 'app-application-server-filters-form',
  standalone: true,
  imports: [
    FloatLabel,
    InputNumber,
    InputText,
    ReactiveFormsModule,
    SearchFilterGridDirective,
    Select,
  ],
  templateUrl: './application-server-filters-form.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationServerFiltersForm {
  form = input.required<ApplicationServerFiltersFormGroup>();
  labels = input.required<ApplicationServerFilterLabels>();
  serverOptions = input.required<SelectOption<number>[]>();
  environmentOptions = input.required<SelectOption<number>[]>();

  protected readonly statusOptions = APPLICATION_INFRASTRUCTURE_STATUS_OPTIONS;
}
