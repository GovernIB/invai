import { FormBuilder, Validators } from '@angular/forms';
import { createApplicationDir3ManualForm } from './application-dir3-manual-form.factory';

describe('manual DIR3 form', () => {
  it('allows a pending assignment but requires a nonblank reason for manual validation', () => {
    const form = createApplicationDir3ManualForm(new FormBuilder());
    expect(form.valid).toBe(true);
    form.controls.validate.setValue(true);
    expect(form.invalid).toBe(true);
    form.controls.reason.setValue('   ');
    expect(form.invalid).toBe(true);
    form.controls.reason.setValue('Responsabilitat transversal');
    expect(form.valid).toBe(true);
    form.reset();
    expect(form.getRawValue()).toEqual({ validate: false, reason: '' });
  });

  it('requires the checkbox in manual-only mode', () => {
    const form = createApplicationDir3ManualForm(new FormBuilder());
    form.controls.validate.setValidators(Validators.requiredTrue);
    form.patchValue({ reason: 'Motiu', validate: false });
    expect(form.invalid).toBe(true);
    form.controls.validate.setValue(true);
    expect(form.valid).toBe(true);
  });
});
