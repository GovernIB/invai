import { FormBuilder } from '@angular/forms';

import {
  configureApplicationSecurityResourceForm,
  createApplicationSecurityForm,
  createApplicationSecurityResourceForm,
} from './application-security-form.factory';

describe('application security forms', () => {
  const formBuilder = new FormBuilder();

  it('creates the optional ENS form with an empty non-null observation', () => {
    const form = createApplicationSecurityForm(formBuilder);

    expect(form.getRawValue()).toEqual({
      overallGradeId: null,
      identityProviderId: null,
      ensSubjectId: null,
      personalDataProcessingId: null,
      approvalDate: null,
      confidentialityId: null,
      integrityId: null,
      traceabilityId: null,
      availabilityId: null,
      authenticityId: null,
      observation: '',
    });
    expect(form.valid).toBe(true);
  });

  it('requires context and field only for web-context resources', () => {
    const form = createApplicationSecurityResourceForm(formBuilder);

    configureApplicationSecurityResourceForm(form, 'web-context');
    expect(form.controls.webContextId.hasError('required')).toBe(true);
    expect(form.controls.fieldId.hasError('required')).toBe(true);

    configureApplicationSecurityResourceForm(form, 'risk');
    expect(form.valid).toBe(true);
    expect(form.controls.webContextId.hasError('required')).toBe(false);
    expect(form.controls.fieldId.hasError('required')).toBe(false);
  });
});
