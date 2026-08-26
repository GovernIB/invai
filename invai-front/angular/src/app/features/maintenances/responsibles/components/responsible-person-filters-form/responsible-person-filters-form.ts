import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { SearchFilterGridDirective } from '@components/search-filters/search-filter-grid.directive';
import { SOFT_DELETE_STATUS_OPTIONS } from '@shared/constants/soft-delete-status.constants';
import { FloatLabel } from 'primeng/floatlabel';
import { InputText } from 'primeng/inputtext';
import { Select } from 'primeng/select';

import { ResponsiblePersonFiltersFormGroup } from '../../forms/responsible-forms.factory';
import { ResponsibleCompanyOption } from '../../responsibles.model';

export interface ResponsiblePersonFilterLabels {
  company: string;
  firstName: string;
  lastName: string;
  email: string;
  status: string;
  companyFilterAriaLabel: string;
}

@Component({
  selector: 'app-responsible-person-filters-form',
  standalone: true,
  imports: [FloatLabel, InputText, ReactiveFormsModule, SearchFilterGridDirective, Select],
  templateUrl: './responsible-person-filters-form.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ResponsiblePersonFiltersForm {
  form = input.required<ResponsiblePersonFiltersFormGroup>();
  labels = input.required<ResponsiblePersonFilterLabels>();
  companyOptions = input.required<ResponsibleCompanyOption[]>();
  protected readonly statusOptions = SOFT_DELETE_STATUS_OPTIONS;
}
