import { FormBuilder } from '@angular/forms';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';

import {
  createSecurityResourceFiltersForm,
  createSecurityResourceForm,
} from './security-resource-forms.factory';

describe('security resource forms', () => {
  const formBuilder = new FormBuilder();

  it('requires both localized names for bilingual resources with the backend limits', () => {
    const form = createSecurityResourceForm(formBuilder, true);

    expect(form.invalid).toBe(true);
    form.setValue({ name: 'Requisit', nameEs: 'Requisito' });
    expect(form.valid).toBe(true);

    form.controls.name.setValue('x'.repeat(151));
    form.controls.nameEs.setValue('x'.repeat(101));
    expect(form.controls.name.hasError('maxlength')).toBe(true);
    expect(form.controls.nameEs.hasError('maxlength')).toBe(true);
  });

  it('does not require a Spanish name for identity providers', () => {
    const form = createSecurityResourceForm(formBuilder, false);

    form.setValue({ name: 'Cl@ve', nameEs: '' });

    expect(form.valid).toBe(true);
  });

  it('resets the soft-delete filter to active', () => {
    const form = createSecurityResourceFiltersForm(formBuilder);

    form.controls.status.setValue(SoftDeleteStatus.INACTIVE);
    form.reset();

    expect(form.controls.status.value).toBe(SoftDeleteStatus.ACTIVE);
  });
});
