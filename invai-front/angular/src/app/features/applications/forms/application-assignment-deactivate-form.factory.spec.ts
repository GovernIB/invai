import { FormBuilder } from '@angular/forms';

import { createApplicationAssignmentDeactivateForm } from './application-assignment-deactivate-form.factory';

describe('createApplicationAssignmentDeactivateForm', () => {
  it('requires a non-blank observation and accepts meaningful text', () => {
    const form = createApplicationAssignmentDeactivateForm(new FormBuilder());

    expect(form.getRawValue()).toEqual({ observation: '' });
    expect(form.invalid).toBe(true);

    form.controls.observation.setValue('   ');
    expect(form.invalid).toBe(true);

    form.controls.observation.setValue('Motiu de la baixa');
    expect(form.valid).toBe(true);
  });

  it('resets the observation to its invalid empty default', () => {
    const form = createApplicationAssignmentDeactivateForm(new FormBuilder());

    form.controls.observation.setValue('Motiu de la baixa');
    form.reset();

    expect(form.controls.observation.value).toBe('');
    expect(form.invalid).toBe(true);
  });
});
