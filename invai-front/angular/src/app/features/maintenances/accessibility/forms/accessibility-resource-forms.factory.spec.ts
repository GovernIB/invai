import { FormBuilder } from '@angular/forms';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';

import {
  createAccessibilityResourceFiltersForm,
  createAccessibilityResourceForm,
} from './accessibility-resource-forms.factory';

describe('accessibility resource forms', () => {
  const formBuilder = new FormBuilder();

  it('requires both localized names for bilingual resources with the backend limits', () => {
    const form = createAccessibilityResourceForm(formBuilder, true);

    expect(form.invalid).toBe(true);
    form.setValue({ name: 'Requisit', nameEs: 'Requisito' });
    expect(form.valid).toBe(true);

    form.controls.name.setValue('x'.repeat(151));
    form.controls.nameEs.setValue('x'.repeat(101));
    expect(form.controls.name.hasError('maxlength')).toBe(true);
    expect(form.controls.nameEs.hasError('maxlength')).toBe(true);
  });

  it('rejects whitespace-only names', () => {
    const form = createAccessibilityResourceForm(formBuilder, true);
    form.setValue({ name: '   ', nameEs: '   ' });
    expect(form.invalid).toBe(true);
  });

  it('resets the soft-delete filter to active', () => {
    const form = createAccessibilityResourceFiltersForm(formBuilder);

    form.controls.status.setValue(SoftDeleteStatus.INACTIVE);
    form.reset();

    expect(form.controls.status.value).toBe(SoftDeleteStatus.ACTIVE);
  });
});
