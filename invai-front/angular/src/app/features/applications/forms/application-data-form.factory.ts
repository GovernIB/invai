import {
  AbstractControl,
  FormBuilder,
  FormControl,
  FormGroup,
  ValidationErrors,
  ValidatorFn,
  Validators,
} from '@angular/forms';
import { TEXT_FIELD_MAX_BYTES } from '@shared/constants/text-field.constants';
import { richTextMaxBytesValidator } from '@shared/utils/text-length.utils';

import { ApplicationDataSource } from '../applications.model';

export const APPLICATION_DATA_URL_MAX_LENGTH = 500;

export const APPLICATION_DATA_SOURCES: readonly ApplicationDataSource[] = ['openData', 'reuse'];

export interface ApplicationDataFormControls {
  manualOpenDataUrl: FormControl<boolean>;
  openDataUrl: FormControl<string>;
  manualReuseUrl: FormControl<boolean>;
  reuseUrl: FormControl<string>;
  observations: FormControl<string>;
}

export type ApplicationDataFormGroup = FormGroup<ApplicationDataFormControls>;

export type ApplicationDataFormValue = ReturnType<ApplicationDataFormGroup['getRawValue']>;

export const APPLICATION_DATA_SOURCE_CONTROLS = {
  openData: { manual: 'manualOpenDataUrl', url: 'openDataUrl' },
  reuse: { manual: 'manualReuseUrl', url: 'reuseUrl' },
} as const satisfies Record<
  ApplicationDataSource,
  { manual: keyof ApplicationDataFormControls; url: keyof ApplicationDataFormControls }
>;

// The back-end does not say whether a source uses a URL typed in by hand, so every edit starts
// with the checkbox unchecked and the URL field empty.
export const EMPTY_APPLICATION_DATA_VALUE: ApplicationDataFormValue = {
  manualOpenDataUrl: false,
  openDataUrl: '',
  manualReuseUrl: false,
  reuseUrl: '',
  observations: '',
};

// A URL typed in by hand replaces the one in use, so it is only required while it is requested.
function requiredWhenManual(flag: 'manualOpenDataUrl' | 'manualReuseUrl'): ValidatorFn {
  return (control: AbstractControl<string>): ValidationErrors | null =>
    control.parent?.get(flag)?.value === true && !control.value?.trim() ? { required: true } : null;
}

// Same rule as the back-end: an absolute http:// or https:// URL, with the scheme in lower case.
function urlValidators(flag: 'manualOpenDataUrl' | 'manualReuseUrl'): ValidatorFn[] {
  return [
    requiredWhenManual(flag),
    Validators.maxLength(APPLICATION_DATA_URL_MAX_LENGTH),
    Validators.pattern(/^\s*$|^\s*https?:\/\/\S+\s*$/),
  ];
}

export function createApplicationDataForm(formBuilder: FormBuilder): ApplicationDataFormGroup {
  return formBuilder.nonNullable.group({
    manualOpenDataUrl: [EMPTY_APPLICATION_DATA_VALUE.manualOpenDataUrl],
    openDataUrl: [EMPTY_APPLICATION_DATA_VALUE.openDataUrl, urlValidators('manualOpenDataUrl')],
    manualReuseUrl: [EMPTY_APPLICATION_DATA_VALUE.manualReuseUrl],
    reuseUrl: [EMPTY_APPLICATION_DATA_VALUE.reuseUrl, urlValidators('manualReuseUrl')],
    observations: [
      EMPTY_APPLICATION_DATA_VALUE.observations,
      richTextMaxBytesValidator(TEXT_FIELD_MAX_BYTES),
    ],
  });
}
