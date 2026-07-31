import { FormBuilder } from '@angular/forms';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';

import { createLayerFiltersForm } from '@features/layers/forms/layer-filters-form.factory';
import { createLayerForm } from '@features/layers/forms/layer-form.factory';
import { createRoleFiltersForm } from '@features/roles/forms/role-filters-form.factory';
import { createRoleForm } from '@features/roles/forms/role-form.factory';
import { createTechnologyFiltersForm } from '@features/technologies/forms/technology-filters-form.factory';
import { createTechnologyForm } from '@features/technologies/forms/technology-form.factory';

describe('development maintenance forms', () => {
  const formBuilder = new FormBuilder();

  it('requires role and layer names while keeping the Spanish role name optional', () => {
    const role = createRoleForm(formBuilder);
    const layer = createLayerForm(formBuilder);

    expect(role.invalid).toBe(true);
    expect(layer.invalid).toBe(true);

    role.setValue({ name: 'Desenvolupament', nameEs: '' });
    layer.setValue({ name: 'Frontend' });
    expect(role.valid).toBe(true);
    expect(layer.valid).toBe(true);

    role.controls.nameEs.setValue('x'.repeat(101));
    layer.controls.name.setValue('x'.repeat(101));
    expect(role.controls.nameEs.hasError('maxlength')).toBe(true);
    expect(layer.controls.name.hasError('maxlength')).toBe(true);
  });

  it('requires a technology name and layer identifier', () => {
    const technology = createTechnologyForm(formBuilder);

    expect(technology.getRawValue()).toEqual({ name: '', layerId: null });
    expect(technology.invalid).toBe(true);

    technology.setValue({ name: 'Angular', layerId: 4 });
    expect(technology.valid).toBe(true);
  });

  it('keeps nullable filters and resets every status to active', () => {
    const forms = [
      createRoleFiltersForm(formBuilder),
      createLayerFiltersForm(formBuilder),
      createTechnologyFiltersForm(formBuilder),
    ];

    for (const form of forms) {
      form.controls.status.setValue(null);
      expect(form.controls.status.value).toBeNull();
      form.reset();
      expect(form.controls.status.value).toBe(SoftDeleteStatus.ACTIVE);
    }
  });
});
