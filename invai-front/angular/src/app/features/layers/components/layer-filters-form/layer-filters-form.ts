import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { SearchFilterGridDirective } from '@components/search-filters/search-filter-grid.directive';
import { SOFT_DELETE_STATUS_OPTIONS } from '@shared/constants/soft-delete-status.constants';
import { FloatLabel } from 'primeng/floatlabel';
import { InputText } from 'primeng/inputtext';
import { Select } from 'primeng/select';

import { LayerFiltersFormGroup } from '../../forms/layer-filters-form.factory';

export interface LayerFilterLabels {
  name: string;
  status: string;
}

@Component({
  selector: 'app-layer-filters-form',
  standalone: true,
  imports: [FloatLabel, InputText, ReactiveFormsModule, SearchFilterGridDirective, Select],
  templateUrl: './layer-filters-form.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LayerFiltersForm {
  form = input.required<LayerFiltersFormGroup>();
  labels = input.required<LayerFilterLabels>();
  protected readonly statusOptions = SOFT_DELETE_STATUS_OPTIONS;
}
