import { FormBuilder } from '@angular/forms';

import {
  APPLICATION_DEVELOPMENT_DATE_RANGE_ERROR,
  createApplicationDevelopmentForm,
  createApplicationProviderForm,
  createApplicationTechnologyForm,
  formatLocalDateTime,
  parseLocalDateTime,
} from './application-development-form.factory';

describe('application development forms', () => {
  const formBuilder = new FormBuilder();

  it('creates an empty required development form', () => {
    const form = createApplicationDevelopmentForm(formBuilder);

    expect(form.getRawValue()).toEqual({
      environment: null,
      modality: null,
      code: '',
      standardAdaption: null,
      revisionDate: null,
      observation: '',
    });
    expect(form.invalid).toBe(true);
  });

  it('rejects invalid source URLs', () => {
    const form = createApplicationDevelopmentForm(formBuilder);

    form.controls.code.setValue('git.caib.es/invai');

    expect(form.controls.code.hasError('pattern')).toBe(true);

    form.controls.code.setValue('https://git.caib.es/invai');

    expect(form.controls.code.valid).toBe(true);
  });

  it('allows visually empty optional development observations', () => {
    const form = createApplicationDevelopmentForm(formBuilder);

    form.setValue({
      environment: 3,
      modality: 1,
      code: 'https://git.caib.es/invai',
      standardAdaption: 1,
      revisionDate: new Date(2026, 4, 2),
      observation: '<p><br></p><p>&nbsp;</p>',
    });

    expect(form.controls.observation.valid).toBe(true);
    expect(form.valid).toBe(true);
  });

  it('requires a provider company and role while allowing optional dates', () => {
    const form = createApplicationProviderForm(formBuilder);

    expect(form.invalid).toBe(true);

    form.controls.companyName.setValue('Plexus SL');

    expect(form.controls.roleId.hasError('required')).toBe(true);

    form.controls.roleId.setValue(3);

    expect(form.valid).toBe(true);

    form.patchValue({
      startDate: new Date(2026, 4, 3),
      expireDate: new Date(2026, 4, 2),
    });

    expect(form.hasError(APPLICATION_DEVELOPMENT_DATE_RANGE_ERROR)).toBe(true);
  });

  it('requires every technology field', () => {
    const form = createApplicationTechnologyForm(formBuilder);

    expect(form.invalid).toBe(true);

    form.setValue({
      layerId: 1,
      technologyId: 2,
      version: '21',
      architecture: 'Monolítica',
    });

    expect(form.valid).toBe(true);
  });

  it('converts LocalDateTime values without applying UTC offsets', () => {
    const parsed = parseLocalDateTime('2026-05-02T23:45:00');

    expect(parsed).toEqual(new Date(2026, 4, 2));
    expect(formatLocalDateTime(parsed)).toBe('2026-05-02T00:00:00');
    expect(parseLocalDateTime(null)).toBeNull();
    expect(formatLocalDateTime(null)).toBeNull();
  });
});
