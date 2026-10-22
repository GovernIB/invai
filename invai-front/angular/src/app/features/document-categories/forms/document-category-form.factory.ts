import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';

import { DOCUMENT_CATEGORY_NAME_MAX_LENGTH } from '../document-categories.constants';

export interface DocumentCategoryFormControls {
  name: FormControl<string>;
  nameEs: FormControl<string>;
  // Set by the owner: null for a category, the category of a subcategory otherwise.
  parentId: FormControl<number | null>;
}

export type DocumentCategoryFormGroup = FormGroup<DocumentCategoryFormControls>;

export type DocumentCategoryFormValue = ReturnType<DocumentCategoryFormGroup['getRawValue']>;

const NAME_VALIDATORS = [
  Validators.required,
  Validators.pattern(/\S/),
  Validators.maxLength(DOCUMENT_CATEGORY_NAME_MAX_LENGTH),
];

export function createDocumentCategoryForm(formBuilder: FormBuilder): DocumentCategoryFormGroup {
  return formBuilder.group({
    name: formBuilder.nonNullable.control('', NAME_VALIDATORS),
    nameEs: formBuilder.nonNullable.control('', NAME_VALIDATORS),
    parentId: formBuilder.control<number | null>(null),
  });
}
