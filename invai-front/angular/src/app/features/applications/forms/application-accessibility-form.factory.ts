import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';

export type ApplicationAccessibilityClassificationSegment = number | null;

export type ApplicationAccessibilityComplianceStatus = number | null;

export type ApplicationAccessibilityMobileApplication = string;

export interface ApplicationAccessibilityFormControls {
  classificationSegment: FormControl<ApplicationAccessibilityClassificationSegment>;
  complianceStatus: FormControl<ApplicationAccessibilityComplianceStatus>;
  reportExpirationDate: FormControl<Date | null>;
  mobileApplication: FormControl<boolean | null>;
  mobileApplicationName: FormControl<ApplicationAccessibilityMobileApplication | null>;
  publicUrl: FormControl<string>;
  inaccessibleContent: FormControl<string>;
  observations: FormControl<string>;
}

export type ApplicationAccessibilityFormGroup = FormGroup<ApplicationAccessibilityFormControls>;

export type ApplicationAccessibilityFormValue = ReturnType<
  ApplicationAccessibilityFormGroup['getRawValue']
>;

export const EMPTY_APPLICATION_ACCESSIBILITY_VALUE: ApplicationAccessibilityFormValue = {
  classificationSegment: null,
  complianceStatus: null,
  reportExpirationDate: null,
  mobileApplication: null,
  mobileApplicationName: null,
  publicUrl: '',
  inaccessibleContent: '',
  observations: '',
};

export function createApplicationAccessibilityForm(
  formBuilder: FormBuilder,
): ApplicationAccessibilityFormGroup {
  return formBuilder.group({
    classificationSegment: formBuilder.control<ApplicationAccessibilityClassificationSegment>(
      EMPTY_APPLICATION_ACCESSIBILITY_VALUE.classificationSegment,
    ),
    complianceStatus: formBuilder.control<ApplicationAccessibilityComplianceStatus>(
      EMPTY_APPLICATION_ACCESSIBILITY_VALUE.complianceStatus,
    ),
    reportExpirationDate: formBuilder.control<Date | null>(null),
    mobileApplication: formBuilder.control<boolean | null>(
      EMPTY_APPLICATION_ACCESSIBILITY_VALUE.mobileApplication,
    ),
    mobileApplicationName: formBuilder.control<ApplicationAccessibilityMobileApplication | null>(
      EMPTY_APPLICATION_ACCESSIBILITY_VALUE.mobileApplicationName,
      [Validators.maxLength(255)],
    ),
    publicUrl: formBuilder.nonNullable.control(EMPTY_APPLICATION_ACCESSIBILITY_VALUE.publicUrl, [
      Validators.maxLength(255),
      Validators.pattern(/^\s*$|^\s*https?:\/\/\S+\s*$/i),
    ]),
    inaccessibleContent: formBuilder.nonNullable.control(
      EMPTY_APPLICATION_ACCESSIBILITY_VALUE.inaccessibleContent,
    ),
    observations: formBuilder.nonNullable.control(
      EMPTY_APPLICATION_ACCESSIBILITY_VALUE.observations,
    ),
  });
}
