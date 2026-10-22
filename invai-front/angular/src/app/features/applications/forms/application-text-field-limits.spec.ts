import { AbstractControl, FormBuilder } from '@angular/forms';
import { TEXT_FIELD_MAX_BYTES } from '@shared/constants/text-field.constants';
import { MAX_BYTES_ERROR } from '@shared/utils/text-length.utils';
import { createApplicationAccessibilityForm } from './application-accessibility-form.factory';
import { createApplicationAssignmentDeactivateForm } from './application-assignment-deactivate-form.factory';
import { createApplicationAuthorizedForm } from './application-authorized-form.factory';
import { createApplicationDataForm } from './application-data-form.factory';
import { createApplicationDevelopmentForm } from './application-development-form.factory';
import { createApplicationDir3ManualForm } from './application-dir3-manual-form.factory';
import {
  createApplicationCreateForm,
  createApplicationDetailForm,
  createApplicationIntegrationsForm,
  createApplicationSystemsDatabasesForm,
} from './application-form.factory';
import { createApplicationResponsibleForm } from './application-responsible-form.factory';
import {
  createApplicationSecurityForm,
  createApplicationSecurityResourceForm,
  createApplicationWebContextVerificationForm,
} from './application-security-form.factory';

// Free-text columns are VARCHAR2(4000 CHAR) in the back end, with no validation of their own.
describe('application free-text limits', () => {
  const fb = new FormBuilder();
  const atLimit = 'à'.repeat(TEXT_FIELD_MAX_BYTES / 2);
  const overLimit = `${atLimit}a`;

  const richText: [string, () => AbstractControl<string>][] = [
    ['create description', () => createApplicationCreateForm(fb).controls.description],
    ['detail description', () => createApplicationDetailForm(fb).controls.description],
    [
      'accessibility inaccessible content',
      () => createApplicationAccessibilityForm(fb).controls.inaccessibleContent,
    ],
    ['accessibility observations', () => createApplicationAccessibilityForm(fb).controls.observations],
    ['data observations', () => createApplicationDataForm(fb).controls.observations],
    ['development observation', () => createApplicationDevelopmentForm(fb).controls.observation],
    ['integrations observations', () => createApplicationIntegrationsForm(fb).controls.observations],
    [
      'systems and databases observations',
      () => createApplicationSystemsDatabasesForm(fb).controls.observations,
    ],
    ['security observation', () => createApplicationSecurityForm(fb).controls.observation],
  ];

  const plainText: [string, () => AbstractControl<string>][] = [
    ['responsible observation', () => createApplicationResponsibleForm(fb).controls.observation],
    ['authorized observation', () => createApplicationAuthorizedForm(fb).controls.observation],
    [
      'assignment deactivation observation',
      () => createApplicationAssignmentDeactivateForm(fb).controls.observation,
    ],
    [
      'security resource observation',
      () => createApplicationSecurityResourceForm(fb).controls.observation,
    ],
    [
      'security resource description',
      () => createApplicationSecurityResourceForm(fb).controls.description,
    ],
    [
      'web context verification reason',
      () => createApplicationWebContextVerificationForm(fb).controls.reason,
    ],
  ];

  it.each(richText)('limits the persisted HTML of the %s', (_name, createControl) => {
    const control = createControl();

    control.setValue(`<p>${atLimit.slice(7)}</p>`);
    expect(control.hasError(MAX_BYTES_ERROR)).toBe(false);

    control.setValue(`<p>${overLimit}</p>`);
    expect(control.getError(MAX_BYTES_ERROR)).toEqual({
      max: TEXT_FIELD_MAX_BYTES,
      actual: TEXT_FIELD_MAX_BYTES + 8,
    });
  });

  it.each(plainText)('limits the trimmed %s', (_name, createControl) => {
    const control = createControl();

    control.setValue(` ${atLimit} `);
    expect(control.hasError(MAX_BYTES_ERROR)).toBe(false);

    control.setValue(overLimit);
    expect(control.getError(MAX_BYTES_ERROR)).toEqual({
      max: TEXT_FIELD_MAX_BYTES,
      actual: TEXT_FIELD_MAX_BYTES + 1,
    });
  });

  it('limits the manual DIR3 reason only while the manual validation is accepted', () => {
    const form = createApplicationDir3ManualForm(fb);

    form.controls.reason.setValue(overLimit);
    expect(form.valid).toBe(true);

    form.controls.validate.setValue(true);
    expect(form.hasError(MAX_BYTES_ERROR)).toBe(true);

    form.controls.reason.setValue(atLimit);
    expect(form.valid).toBe(true);
  });
});
