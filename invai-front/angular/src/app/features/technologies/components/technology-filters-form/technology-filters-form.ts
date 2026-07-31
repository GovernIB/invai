import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { SearchFilterGridDirective } from '@components/search-filters/search-filter-grid.directive';
import { LayerOption } from '@features/layers/layers.model';
import { SOFT_DELETE_STATUS_OPTIONS } from '@shared/constants/soft-delete-status.constants';
import { FloatLabel } from 'primeng/floatlabel';
import { InputText } from 'primeng/inputtext';
import { Select } from 'primeng/select';

import { TechnologyFiltersFormGroup } from '../../forms/technology-filters-form.factory';

export interface TechnologyFilterLabels {
  name: string;
  layer: string;
  status: string;
}

@Component({
  selector: 'app-technology-filters-form',
  standalone: true,
  imports: [FloatLabel, InputText, ReactiveFormsModule, SearchFilterGridDirective, Select],
  templateUrl: './technology-filters-form.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TechnologyFiltersForm {
  form = input.required<TechnologyFiltersFormGroup>();
  labels = input.required<TechnologyFilterLabels>();
  layerOptions = input.required<LayerOption[]>();
  protected readonly statusOptions = SOFT_DELETE_STATUS_OPTIONS;
}
