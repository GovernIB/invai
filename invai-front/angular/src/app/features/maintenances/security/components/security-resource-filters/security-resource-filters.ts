import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { SearchFilterGridDirective } from '@components/search-filters/search-filter-grid.directive';
import { SOFT_DELETE_STATUS_OPTIONS } from '@shared/constants/soft-delete-status.constants';
import { FloatLabel } from 'primeng/floatlabel';
import { InputText } from 'primeng/inputtext';
import { Select } from 'primeng/select';

import { SecurityResourceFiltersFormGroup } from '../../forms/security-resource-forms.factory';

export interface SecurityResourceFilterLabels {
  name: string;
  nameEs: string;
  status: string;
}

@Component({
  selector: 'app-security-resource-filters',
  standalone: true,
  imports: [FloatLabel, InputText, ReactiveFormsModule, SearchFilterGridDirective, Select],
  templateUrl: './security-resource-filters.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SecurityResourceFilters {
  form = input.required<SecurityResourceFiltersFormGroup>();
  labels = input.required<SecurityResourceFilterLabels>();
  bilingual = input.required<boolean>();
  idPrefix = input.required<string>();

  protected readonly statusOptions = SOFT_DELETE_STATUS_OPTIONS;
}
