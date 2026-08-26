import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { SearchFilterGridDirective } from '@components/search-filters/search-filter-grid.directive';
import { SOFT_DELETE_STATUS_OPTIONS } from '@shared/constants/soft-delete-status.constants';
import { FloatLabel } from 'primeng/floatlabel';
import { InputText } from 'primeng/inputtext';
import { Select } from 'primeng/select';

import { ResponsibleAuthorizationFiltersFormGroup } from '../../forms/responsible-forms.factory';

@Component({
  selector: 'app-responsible-authorization-filters-form',
  standalone: true,
  imports: [FloatLabel, InputText, ReactiveFormsModule, SearchFilterGridDirective, Select],
  templateUrl: './responsible-authorization-filters-form.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ResponsibleAuthorizationFiltersForm {
  form = input.required<ResponsibleAuthorizationFiltersFormGroup>();
  labels = input.required<{ name: string; nameEs: string; status: string }>();
  protected readonly statusOptions = SOFT_DELETE_STATUS_OPTIONS;
}
