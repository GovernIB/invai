import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { SearchFilterGridDirective } from '@components/search-filters/search-filter-grid.directive';
import { SOFT_DELETE_STATUS_OPTIONS } from '@shared/constants/soft-delete-status.constants';
import { FloatLabel } from 'primeng/floatlabel';
import { InputText } from 'primeng/inputtext';
import { Select } from 'primeng/select';

import { ResponsibleNameFiltersFormGroup } from '../../forms/responsible-forms.factory';

export interface ResponsibleNameFilterLabels {
  name: string;
  status: string;
}

@Component({
  selector: 'app-responsible-name-filters-form',
  standalone: true,
  imports: [FloatLabel, InputText, ReactiveFormsModule, SearchFilterGridDirective, Select],
  templateUrl: './responsible-name-filters-form.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ResponsibleNameFiltersForm {
  form = input.required<ResponsibleNameFiltersFormGroup>();
  labels = input.required<ResponsibleNameFilterLabels>();
  idPrefix = input.required<string>();
  protected readonly statusOptions = SOFT_DELETE_STATUS_OPTIONS;
}
