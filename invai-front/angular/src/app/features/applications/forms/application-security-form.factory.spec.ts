import { FormBuilder } from '@angular/forms';

import {
  configureApplicationSecurityResourceForm,
  createApplicationSecurityForm,
  createApplicationSecurityResourceForm,
  createApplicationSecurityRoleFiltersForm,
} from './application-security-form.factory';

describe('application security forms', () => {
  const formBuilder = new FormBuilder();

  it('defaults the role system to weblogic, allows empty filters and restores the default on reset', () => {
    const form = createApplicationSecurityRoleFiltersForm(formBuilder);
    expect(form.getRawValue()).toEqual({ securityRoleName: '', system: 'weblogic' });
    form.setValue({ securityRoleName: 'INV_', system: '' });
    expect(form.valid).toBe(true);
    form.reset();
    expect(form.getRawValue()).toEqual({ securityRoleName: '', system: 'weblogic' });
  });

  it('requires a Soffid role only for role assignments', () => {
    const form = createApplicationSecurityResourceForm(formBuilder);
    configureApplicationSecurityResourceForm(form, 'role');
    expect(form.controls.role.hasError('required')).toBe(true);
    form.controls.role.setValue({ roleId: 5, name: 'INV_ADMIN', system: 'weblogic', description: null });
    expect(form.valid).toBe(true);

    configureApplicationSecurityResourceForm(form, 'risk');
    expect(form.controls.role.value).toBeNull();
    expect(form.valid).toBe(true);
  });

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

  it('validates optional HTTP URLs up to 255 characters and clears validators for other resources', () => {
    const form = createApplicationSecurityResourceForm(formBuilder);
    configureApplicationSecurityResourceForm(form, 'web-context');
    for (const url of ['', '   ', 'http://intranet', 'https://' + 'a'.repeat(247)]) {
      form.controls.url.setValue(url);
      expect(form.controls.url.valid).toBe(true);
    }
    for (const url of ['ftp://host', 'https://', 'https://' + 'a'.repeat(248)]) {
      form.controls.url.setValue(url);
      expect(form.controls.url.invalid).toBe(true);
    }
    configureApplicationSecurityResourceForm(form, 'risk');
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
