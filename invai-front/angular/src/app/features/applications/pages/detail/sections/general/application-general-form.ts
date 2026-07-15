import { ChangeDetectionStrategy, Component, inject, input } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ReactiveFormsModule } from '@angular/forms';

import { ApplicationFormFields } from '../../../../components';
import { ApplicationDetailFormGroup } from '../../../../forms/application-form.factory';
import { ApplicationOptionsService } from '../../../../services/application-options.service';
import {
  APPLICATION_GENERAL_LABELS,
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
  private readonly optionsService = inject(ApplicationOptionsService);

  form = input.required<ApplicationDetailFormGroup>();

  protected readonly labels = APPLICATION_GENERAL_LABELS;
  protected readonly requiredError = APPLICATION_GENERAL_REQUIRED_ERROR;
  protected readonly selectPlaceholder = APPLICATION_GENERAL_SELECT_PLACEHOLDER;
  protected readonly options = toSignal(this.optionsService.getOptions(), { initialValue: null });
}
