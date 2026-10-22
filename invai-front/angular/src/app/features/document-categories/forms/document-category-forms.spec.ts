import { FormBuilder } from '@angular/forms';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';

import { createDocumentCategoryFiltersForm } from './document-category-filters-form.factory';
import { createDocumentCategoryForm } from './document-category-form.factory';

describe('document category forms', () => {
  const formBuilder = new FormBuilder();

  it('requires both names, with text and up to 150 characters', () => {
    const form = createDocumentCategoryForm(formBuilder);

    expect(form.getRawValue()).toEqual({ name: '', nameEs: '', parentId: null });
    expect(form.controls.name.hasError('required')).toBe(true);
    expect(form.controls.nameEs.hasError('required')).toBe(true);

    form.patchValue({ name: '   ', nameEs: 'x'.repeat(151) });
    expect(form.controls.name.hasError('pattern')).toBe(true);
    expect(form.controls.nameEs.hasError('maxlength')).toBe(true);

    form.patchValue({ name: 'Manuals', nameEs: 'x'.repeat(150), parentId: 3 });
    expect(form.valid).toBe(true);
  });

  it('resets its name and parent to empty values', () => {
    const form = createDocumentCategoryForm(formBuilder);
    form.patchValue({ name: 'Manuals', nameEs: 'Manuales', parentId: 3 });

    form.reset();

    expect(form.getRawValue()).toEqual({ name: '', nameEs: '', parentId: null });
  });

  it('filters active categories by default and goes back to them on reset', () => {
    const form = createDocumentCategoryFiltersForm(formBuilder);
    expect(form.getRawValue()).toEqual({ name: null, status: SoftDeleteStatus.ACTIVE });

    form.patchValue({ name: 'API', status: null });
    form.reset();

    expect(form.getRawValue()).toEqual({ name: null, status: SoftDeleteStatus.ACTIVE });
  });
});
