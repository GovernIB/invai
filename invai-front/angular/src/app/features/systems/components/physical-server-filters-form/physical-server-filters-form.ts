import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { SearchFilterGridDirective } from '@components/search-filters/search-filter-grid.directive';
import { EnvironmentCatalogOption } from '@features/environments/environments.model';
import { FloatLabel } from 'primeng/floatlabel';
import { InputText } from 'primeng/inputtext';
import { Select } from 'primeng/select';

import { PhysicalServerFiltersFormGroup } from '../../forms/physical-server-filters-form.factory';
import { INFRASTRUCTURE_STATUS_OPTIONS } from '../../systems.constants';

@Component({
  selector: 'app-physical-server-filters-form',
  standalone: true,
  imports: [
    FloatLabel,
    InputText,
    ReactiveFormsModule,
    SearchFilterGridDirective,
    Select,
  ],
  templateUrl: './physical-server-filters-form.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PhysicalServerFiltersForm {
  form = input.required<PhysicalServerFiltersFormGroup>();
  environmentOptions = input.required<EnvironmentCatalogOption[]>();

  protected readonly statusOptions = INFRASTRUCTURE_STATUS_OPTIONS;
  protected readonly labels = {
    name: $localize`Servidor`,
    environment: $localize`Entorn`,
    status: $localize`Estat`,
  };
}
