import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { SearchFilterGridDirective } from '@components/search-filters/search-filter-grid.directive';
import { SOFT_DELETE_STATUS_OPTIONS } from '@shared/constants/soft-delete-status.constants';
import { FloatLabel } from 'primeng/floatlabel';
import { InputText } from 'primeng/inputtext';
import { Select } from 'primeng/select';

import { DocumentCategoryFiltersFormGroup } from '../../forms/document-category-filters-form.factory';

export interface DocumentCategoryFilterLabels {
  name: string;
  status: string;
}

@Component({
  selector: 'app-document-category-filters-form',
  standalone: true,
  imports: [FloatLabel, InputText, ReactiveFormsModule, SearchFilterGridDirective, Select],
  templateUrl: './document-category-filters-form.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DocumentCategoryFiltersForm {
  form = input.required<DocumentCategoryFiltersFormGroup>();
  labels = input.required<DocumentCategoryFilterLabels>();
  protected readonly statusOptions = SOFT_DELETE_STATUS_OPTIONS;
}
