import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { SearchFilterGridDirective } from '@components/search-filters/search-filter-grid.directive';
import { ResponsiblePersonOption } from '@features/maintenances/responsibles/responsibles.model';
import { AutoComplete } from 'primeng/autocomplete';
import { FloatLabel } from 'primeng/floatlabel';
import { InputText } from 'primeng/inputtext';
import { Select } from 'primeng/select';
import { ToggleSwitch } from 'primeng/toggleswitch';

import { APPLICATION_STATUS_OPTIONS } from '../../applications.constants';
import { ApplicationInfrastructureFilterOptions } from '../../applications.model';
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
  responsible: string;
  database: string;
  server: string;
  environment: string;
  incomplete: string;
  responsibleEmpty: string;
  responsibleLoading: string;
}

@Component({
  selector: 'app-application-filters-form',
  standalone: true,
  imports: [
    AutoComplete,
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
  infrastructureOptions = input<Partial<ApplicationInfrastructureFilterOptions> | null>(null);
  responsibleOptions = input<ResponsiblePersonOption[]>([]);
  responsibleLoading = input(false);
  responsibleSearch = output<string>();

  protected readonly statusOptions = APPLICATION_STATUS_OPTIONS;

  protected onResponsibleSearch(event: { query: string }): void {
    this.responsibleSearch.emit(event.query);
  }

  protected onResponsibleClear(): void {
    this.responsibleSearch.emit('');
  }
}
