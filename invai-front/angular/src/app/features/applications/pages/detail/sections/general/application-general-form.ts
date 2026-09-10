import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';

import { ApplicationFormFields } from '../../../../components';
import { ApplicationDetailFormGroup } from '../../../../forms/application-form.factory';
import {
  ApplicationCommissionOption,
  ApplicationSelectOptions,
} from '../../../../services/application-options.service';
import {
  APPLICATION_GENERAL_LABELS,
  APPLICATION_GENERAL_PREFIX_MAX_LENGTH_ERROR,
  APPLICATION_GENERAL_REQUIRED_ERROR,
  APPLICATION_GENERAL_SELECT_PLACEHOLDER,
} from './application-general.i18n';

@Component({
  standalone: true,
  selector: 'app-application-general-form',
  imports: [ApplicationFormFields, ReactiveFormsModule],
  templateUrl: './application-general-form.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ApplicationGeneralForm {
  form = input.required<ApplicationDetailFormGroup>();
  isReadOnly = input.required<boolean>();
  options = input.required<ApplicationSelectOptions>();
  departmentsLoading = input(false);
  departmentsLoadFailed = input(false);
  administrativeUnitsLoading = input(false);
  administrativeUnitsLoadFailed = input(false);
  commissionSelected = output<ApplicationCommissionOption | null>();
  conselleriaSelected = output<string | null>();
  departmentsRetry = output<void>();
  administrativeUnitsRetry = output<void>();

  protected readonly labels = APPLICATION_GENERAL_LABELS;
  protected readonly requiredError = APPLICATION_GENERAL_REQUIRED_ERROR;
  protected readonly prefixMaxLengthError = APPLICATION_GENERAL_PREFIX_MAX_LENGTH_ERROR;
  protected readonly selectPlaceholder = APPLICATION_GENERAL_SELECT_PLACEHOLDER;
  protected selectCommission(commission: ApplicationCommissionOption | null): void {
    this.commissionSelected.emit(commission);
  }

  protected selectConselleria(code: string | null): void {
    this.conselleriaSelected.emit(code);
  }
}
