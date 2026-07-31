import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { SearchFilterGridDirective } from '@components/search-filters/search-filter-grid.directive';
import { FloatLabel } from 'primeng/floatlabel';
import { InputText } from 'primeng/inputtext';
import { Select } from 'primeng/select';
import { SOFT_DELETE_STATUS_OPTIONS } from '@shared/constants/soft-delete-status.constants';

import { SystemTypeFiltersFormGroup } from '../../forms/system-type-filters-form.factory';

export interface SystemTypeFilterLabels {
  name: string;
  nameEs: string;
  status: string;
}

@Component({
  selector: 'app-system-type-filters-form',
  standalone: true,
  imports: [FloatLabel, InputText, ReactiveFormsModule, SearchFilterGridDirective, Select],
  templateUrl: './system-type-filters-form.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SystemTypeFiltersForm {
  form = input.required<SystemTypeFiltersFormGroup>();
  labels = input.required<SystemTypeFilterLabels>();

  protected readonly statusOptions = SOFT_DELETE_STATUS_OPTIONS;
}
