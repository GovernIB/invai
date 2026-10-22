import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { APPLICATION_DOCUMENTATION_TITLE_MAX_LENGTH } from '../pages/detail/sections/documentation/application-documentation.constants';

export interface ApplicationDocumentDetailControls {
  title: FormControl<string>;
  categoryId: FormControl<number | null>;
  subcategoryId: FormControl<number | null>;
  statusId: FormControl<number | null>;
}

export type ApplicationDocumentDetailForm = FormGroup<ApplicationDocumentDetailControls>;

export function createApplicationDocumentDetailForm(fb: FormBuilder): ApplicationDocumentDetailForm {
  return fb.group({
    title: fb.nonNullable.control('', [
      Validators.required,
      Validators.pattern(/\S/),
      Validators.maxLength(APPLICATION_DOCUMENTATION_TITLE_MAX_LENGTH),
    ]),
    categoryId: fb.control<number | null>(null, Validators.required),
    subcategoryId: fb.control<number | null>(null),
    statusId: fb.control<number | null>(null, Validators.required),
  });
}
