import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { SearchFilterGridDirective } from '@components/search-filters/search-filter-grid.directive';
import { FloatLabel } from 'primeng/floatlabel';
import { InputText } from 'primeng/inputtext';
import { Select } from 'primeng/select';
import { SOFT_DELETE_STATUS_OPTIONS } from '@shared/constants/soft-delete-status.constants';

import { CategoryFiltersFormGroup } from '../../forms/category-filters-form.factory';

export interface CategoryFilterLabels {
  name: string;
  nameEs: string;
  status: string;
}

@Component({
  selector: 'app-category-filters-form',
  standalone: true,
  imports: [FloatLabel, InputText, ReactiveFormsModule, SearchFilterGridDirective, Select],
  templateUrl: './category-filters-form.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CategoryFiltersForm {
  form = input.required<CategoryFiltersFormGroup>();
  labels = input.required<CategoryFilterLabels>();

  protected readonly statusOptions = SOFT_DELETE_STATUS_OPTIONS;
}
