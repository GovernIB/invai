import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { SearchFilterGridDirective } from '@components/search-filters/search-filter-grid.directive';
import { SOFT_DELETE_STATUS_OPTIONS } from '@shared/constants/soft-delete-status.constants';
import { FloatLabel } from 'primeng/floatlabel';
import { InputText } from 'primeng/inputtext';
import { Select } from 'primeng/select';

import { AccessibilityResourceFiltersFormGroup } from '../../forms/accessibility-resource-forms.factory';

export interface AccessibilityResourceFilterLabels {
  name: string;
  nameEs: string;
  status: string;
}

@Component({
  selector: 'app-accessibility-resource-filters',
  standalone: true,
  imports: [FloatLabel, InputText, ReactiveFormsModule, SearchFilterGridDirective, Select],
  templateUrl: './accessibility-resource-filters.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AccessibilityResourceFilters {
  form = input.required<AccessibilityResourceFiltersFormGroup>();
  labels = input.required<AccessibilityResourceFilterLabels>();
  bilingual = input.required<boolean>();
  idPrefix = input.required<string>();

  protected readonly statusOptions = SOFT_DELETE_STATUS_OPTIONS;
}
