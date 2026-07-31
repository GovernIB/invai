import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { SearchFilterGridDirective } from '@components/search-filters/search-filter-grid.directive';
import { SOFT_DELETE_STATUS_OPTIONS } from '@shared/constants/soft-delete-status.constants';
import { FloatLabel } from 'primeng/floatlabel';
import { InputText } from 'primeng/inputtext';
import { Select } from 'primeng/select';

import { RoleFiltersFormGroup } from '../../forms/role-filters-form.factory';

export interface RoleFilterLabels {
  name: string;
  nameEs: string;
  status: string;
}

@Component({
  selector: 'app-role-filters-form',
  standalone: true,
  imports: [FloatLabel, InputText, ReactiveFormsModule, SearchFilterGridDirective, Select],
  templateUrl: './role-filters-form.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RoleFiltersForm {
  form = input.required<RoleFiltersFormGroup>();
  labels = input.required<RoleFilterLabels>();
  protected readonly statusOptions = SOFT_DELETE_STATUS_OPTIONS;
}
