import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { SearchFilterGridDirective } from '@components/search-filters/search-filter-grid.directive';
import { FloatLabel } from 'primeng/floatlabel';
import { InputText } from 'primeng/inputtext';
import { Select } from 'primeng/select';
import { SOFT_DELETE_STATUS_OPTIONS } from '@shared/constants/soft-delete-status.constants';

import { FieldFiltersFormGroup } from '../../forms/field-filters-form.factory';

export interface FieldFilterLabels {
  name: string;
  nameEs: string;
  status: string;
}

@Component({
  selector: 'app-field-filters-form',
  standalone: true,
  imports: [FloatLabel, InputText, ReactiveFormsModule, SearchFilterGridDirective, Select],
  templateUrl: './field-filters-form.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FieldFiltersForm {
  form = input.required<FieldFiltersFormGroup>();
  labels = input.required<FieldFilterLabels>();

  protected readonly statusOptions = SOFT_DELETE_STATUS_OPTIONS;
}
