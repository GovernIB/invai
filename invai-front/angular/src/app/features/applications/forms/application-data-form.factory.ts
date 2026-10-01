import {
  AbstractControl,
  FormBuilder,
  FormControl,
  FormGroup,
  ValidationErrors,
  ValidatorFn,
  Validators,
} from '@angular/forms';

export const APPLICATION_DATA_URL_MAX_LENGTH = 500;

export type ApplicationDataSource = 'openData' | 'reuse';

export interface ApplicationDataFormControls {
  useOpenDataUrl: FormControl<boolean>;
  openDataUrl: FormControl<string>;
  useReuseUrl: FormControl<boolean>;
  reuseUrl: FormControl<string>;
  observations: FormControl<string>;
}

export type ApplicationDataFormGroup = FormGroup<ApplicationDataFormControls>;

export type ApplicationDataFormValue = ReturnType<ApplicationDataFormGroup['getRawValue']>;

export const APPLICATION_DATA_SOURCE_CONTROLS = {
  openData: { useUrl: 'useOpenDataUrl', url: 'openDataUrl' },
  reuse: { useUrl: 'useReuseUrl', url: 'reuseUrl' },
} as const satisfies Record<
  ApplicationDataSource,
  { useUrl: keyof ApplicationDataFormControls; url: keyof ApplicationDataFormControls }
>;

export const EMPTY_APPLICATION_DATA_VALUE: ApplicationDataFormValue = {
  useOpenDataUrl: false,
  openDataUrl: '',
  useReuseUrl: false,
  reuseUrl: '',
  observations: '',
};

// The backend queries the explicit URL only when its flag is on, so only then is it required.
function requiredWhenUsed(flag: 'useOpenDataUrl' | 'useReuseUrl'): ValidatorFn {
  return (control: AbstractControl<string>): ValidationErrors | null =>
    control.parent?.get(flag)?.value === true && !control.value?.trim() ? { required: true } : null;
}

function urlValidators(flag: 'useOpenDataUrl' | 'useReuseUrl'): ValidatorFn[] {
  return [
    requiredWhenUsed(flag),
    Validators.maxLength(APPLICATION_DATA_URL_MAX_LENGTH),
    Validators.pattern(/^\s*$|^\s*https?:\/\/\S+\s*$/i),
  ];
}

export function createApplicationDataForm(formBuilder: FormBuilder): ApplicationDataFormGroup {
  return formBuilder.nonNullable.group({
    useOpenDataUrl: [EMPTY_APPLICATION_DATA_VALUE.useOpenDataUrl],
    openDataUrl: [EMPTY_APPLICATION_DATA_VALUE.openDataUrl, urlValidators('useOpenDataUrl')],
    useReuseUrl: [EMPTY_APPLICATION_DATA_VALUE.useReuseUrl],
    reuseUrl: [EMPTY_APPLICATION_DATA_VALUE.reuseUrl, urlValidators('useReuseUrl')],
    observations: [EMPTY_APPLICATION_DATA_VALUE.observations],
  });
}
