import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { SearchFilterGridDirective } from '@components/search-filters/search-filter-grid.directive';
import { ResponsibleCompanyOption } from '@features/maintenances/responsibles/responsibles.model';
import { SOFT_DELETE_STATUS_OPTIONS } from '@shared/constants/soft-delete-status.constants';
import { FloatLabel } from 'primeng/floatlabel';
import { InputText } from 'primeng/inputtext';
import { Select } from 'primeng/select';

import { ExternalSystemFiltersFormGroup } from '../../forms/external-system-filters-form.factory';

export interface ExternalSystemFilterLabels {
  name: string;
  company: string;
  status: string;
}

@Component({
  selector: 'app-external-system-filters-form',
  standalone: true,
  imports: [FloatLabel, InputText, ReactiveFormsModule, SearchFilterGridDirective, Select],
  templateUrl: './external-system-filters-form.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ExternalSystemFiltersForm {
  form = input.required<ExternalSystemFiltersFormGroup>();
  labels = input.required<ExternalSystemFilterLabels>();
  companyOptions = input.required<ResponsibleCompanyOption[]>();
  protected readonly statusOptions = SOFT_DELETE_STATUS_OPTIONS;
}
