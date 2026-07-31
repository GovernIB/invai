import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { SearchFilterGridDirective } from '@components/search-filters/search-filter-grid.directive';
import { DatePicker } from 'primeng/datepicker';
import { FloatLabel } from 'primeng/floatlabel';
import { InputText } from 'primeng/inputtext';
import { Select } from 'primeng/select';
import { DatePickerPassThrough } from 'primeng/types/datepicker';
import { SOFT_DELETE_STATUS_OPTIONS } from '@shared/constants/soft-delete-status.constants';

import {
  COMMISSION_DATE_FORMAT,
  COMMISSION_DATE_PLACEHOLDER,
  COMMISSION_TYPE_OPTIONS,
} from '../../commissions.constants';
import {
  COMMISSION_DATE_RANGE_ERROR,
  CommissionFiltersFormGroup,
} from '../../forms/commission-filters-form.factory';

export interface CommissionFilterLabels {
  name: string;
  nameEs: string;
  expedientNumber: string;
  approvalDateFrom: string;
  approvalDateTo: string;
  commissionType: string;
  status: string;
  invalidDateRange: string;
}

@Component({
  selector: 'app-commission-filters-form',
  standalone: true,
  imports: [
    DatePicker,
    FloatLabel,
    InputText,
    ReactiveFormsModule,
    SearchFilterGridDirective,
    Select,
  ],
  templateUrl: './commission-filters-form.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CommissionFiltersForm {
  form = input.required<CommissionFiltersFormGroup>();
  labels = input.required<CommissionFilterLabels>();

  protected readonly commissionTypeOptions = COMMISSION_TYPE_OPTIONS;
  protected readonly statusOptions = SOFT_DELETE_STATUS_OPTIONS;
  protected readonly dateFormat = COMMISSION_DATE_FORMAT;
  protected readonly datePlaceholder = COMMISSION_DATE_PLACEHOLDER;
  protected readonly dateRangeErrorId = 'commissions-filter-date-range-error';

  protected isDateRangeErrorVisible(): boolean {
    const form = this.form();
    const from = form.controls.approvalDateFrom;
    const to = form.controls.approvalDateTo;
    return (
      form.hasError(COMMISSION_DATE_RANGE_ERROR) &&
      (from.touched || from.dirty || to.touched || to.dirty)
    );
  }

  protected datePassThrough(): DatePickerPassThrough {
    const invalid = this.isDateRangeErrorVisible();

    return {
      pcInputText: {
        root: {
          'aria-describedby': invalid ? this.dateRangeErrorId : null,
          'aria-invalid': String(invalid),
        },
      },
    };
  }
}
