import {
  AbstractControl,
  FormBuilder,
  FormControl,
  FormGroup,
  ValidationErrors,
  ValidatorFn,
  Validators,
} from '@angular/forms';

import {
  APPLICATION_ATTACHMENT_MAX_FILE_BYTES,
  APPLICATION_ATTACHMENT_VERSION_MAX_LENGTH,
  APPLICATION_DOCUMENTATION_TITLE_MAX_LENGTH,
  APPLICATION_LINK_DESCRIPTION_MAX_LENGTH,
  APPLICATION_LINK_TITLE_MAX_LENGTH,
  APPLICATION_LINK_URL_MAX_LENGTH,
} from '../pages/detail/sections/documentation/application-documentation.constants';

const TITLE_VALIDATORS = [
  Validators.required,
  Validators.pattern(/\S/),
  Validators.maxLength(APPLICATION_DOCUMENTATION_TITLE_MAX_LENGTH),
];

export const APPLICATION_ATTACHMENT_VERSION_VALIDATORS = [
  Validators.required,
  Validators.pattern(/\S/),
  Validators.maxLength(APPLICATION_ATTACHMENT_VERSION_MAX_LENGTH),
];

// The back-end only accepts the scheme in lower case.
export const APPLICATION_LINK_URL_PATTERN = /^https?:\/\/\S+$/;

// Lower-case extension of a file name, without the dot; empty when it has none.
export function fileExtensionOf(fileName: string): string {
  const dot = fileName.lastIndexOf('.');
  return dot > 0 ? fileName.slice(dot + 1).toLowerCase() : '';
}

export type ApplicationAttachmentFileRejection = 'fileTooLarge' | 'fileType';

// Why the back-end would refuse a file: larger than 5 MB (`fileTooLarge`) or with an extension
// outside the accepted formats (`fileType`). The formats are only checked once known.
export function applicationAttachmentFileRejection(
  file: File,
  allowedExtensions: readonly string[],
): ApplicationAttachmentFileRejection | null {
  if (file.size > APPLICATION_ATTACHMENT_MAX_FILE_BYTES) return 'fileTooLarge';
  return allowedExtensions.length && !allowedExtensions.includes(fileExtensionOf(file.name))
    ? 'fileType'
    : null;
}

// The pickers refuse those files before they reach the form; this keeps any that gets through.
export function applicationAttachmentFileValidator(
  allowedExtensions: () => readonly string[],
): ValidatorFn {
  return (control: AbstractControl<File | null>): ValidationErrors | null => {
    const rejection = control.value
      ? applicationAttachmentFileRejection(control.value, allowedExtensions())
      : null;
    return rejection ? { [rejection]: true } : null;
  };
}

// The subcategory is chosen among those of the category, so it starts disabled.
export interface ApplicationAttachmentFiltersFormControls {
  sectionId: FormControl<number | null>;
  mimeTypeId: FormControl<number | null>;
  categoryId: FormControl<number | null>;
  subcategoryId: FormControl<number | null>;
  statusId: FormControl<number | null>;
}

export type ApplicationAttachmentFiltersFormGroup =
  FormGroup<ApplicationAttachmentFiltersFormControls>;

export function createApplicationAttachmentFiltersForm(
  formBuilder: FormBuilder,
): ApplicationAttachmentFiltersFormGroup {
  return formBuilder.group({
    sectionId: formBuilder.control<number | null>(null),
    mimeTypeId: formBuilder.control<number | null>(null),
    categoryId: formBuilder.control<number | null>(null),
    subcategoryId: formBuilder.control<number | null>({ value: null, disabled: true }),
    statusId: formBuilder.control<number | null>(null),
  });
}

// A new document is created active; its state changes from the document detail.
export interface ApplicationAttachmentFormControls {
  title: FormControl<string>;
  sectionId: FormControl<number | null>;
  file: FormControl<File | null>;
  categoryId: FormControl<number | null>;
  subcategoryId: FormControl<number | null>;
  versionLabel: FormControl<string>;
}

export type ApplicationAttachmentFormGroup = FormGroup<ApplicationAttachmentFormControls>;

export function createApplicationAttachmentForm(
  formBuilder: FormBuilder,
  allowedExtensions: () => readonly string[] = () => [],
): ApplicationAttachmentFormGroup {
  return formBuilder.group({
    title: formBuilder.nonNullable.control('', TITLE_VALIDATORS),
    sectionId: formBuilder.control<number | null>(null, Validators.required),
    file: formBuilder.control<File | null>(null, [
      Validators.required,
      applicationAttachmentFileValidator(allowedExtensions),
    ]),
    categoryId: formBuilder.control<number | null>(null, Validators.required),
    subcategoryId: formBuilder.control<number | null>(null),
    versionLabel: formBuilder.nonNullable.control('', APPLICATION_ATTACHMENT_VERSION_VALIDATORS),
  });
}

export interface ApplicationLinkFiltersFormControls {
  sectionId: FormControl<number | null>;
}

export type ApplicationLinkFiltersFormGroup = FormGroup<ApplicationLinkFiltersFormControls>;

export function createApplicationLinkFiltersForm(
  formBuilder: FormBuilder,
): ApplicationLinkFiltersFormGroup {
  return formBuilder.group({
    sectionId: formBuilder.control<number | null>(null),
  });
}

export interface ApplicationLinkFormControls {
  title: FormControl<string>;
  description: FormControl<string>;
  sectionId: FormControl<number | null>;
  url: FormControl<string>;
}

export type ApplicationLinkFormGroup = FormGroup<ApplicationLinkFormControls>;

export function createApplicationLinkForm(formBuilder: FormBuilder): ApplicationLinkFormGroup {
  return formBuilder.group({
    title: formBuilder.nonNullable.control('', [
      Validators.required,
      Validators.pattern(/\S/),
      Validators.maxLength(APPLICATION_LINK_TITLE_MAX_LENGTH),
    ]),
    description: formBuilder.nonNullable.control(
      '',
      Validators.maxLength(APPLICATION_LINK_DESCRIPTION_MAX_LENGTH),
    ),
    sectionId: formBuilder.control<number | null>(null, Validators.required),
    url: formBuilder.nonNullable.control('', [
      Validators.required,
      Validators.pattern(APPLICATION_LINK_URL_PATTERN),
      Validators.maxLength(APPLICATION_LINK_URL_MAX_LENGTH),
    ]),
  });
}
