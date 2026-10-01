import {
  AbstractControl,
  FormBuilder,
  FormControl,
  FormGroup,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { SoffidPersonControlValue } from '@features/maintenances/responsibles/responsibles.model';

// The other system of a connection is either an inventory application or an external system.
export type ApplicationIntegrationSystemKind = 'application' | 'external';

export interface ApplicationIntegrationRoleOption {
  // Soffid's own role identifier, sent back as a required role id.
  roleId: number;
  label: string;
}

export interface ApplicationIntegrationConnectionFormControls {
  systemKind: FormControl<ApplicationIntegrationSystemKind>;
  applicationId: FormControl<number | null>;
  externalSystemId: FormControl<number | null>;
  technologyId: FormControl<number | null>;
  user: FormControl<SoffidPersonControlValue>;
  requiredRoles: FormControl<ApplicationIntegrationRoleOption[]>;
}

export type ApplicationIntegrationConnectionFormGroup =
  FormGroup<ApplicationIntegrationConnectionFormControls>;

export type ApplicationIntegrationConnectionFormValue = ReturnType<
  ApplicationIntegrationConnectionFormGroup['getRawValue']
>;

export const EMPTY_APPLICATION_INTEGRATION_CONNECTION_VALUE: ApplicationIntegrationConnectionFormValue =
  {
    systemKind: 'application',
    applicationId: null,
    externalSystemId: null,
    technologyId: null,
    user: null,
    requiredRoles: [],
  };

export function createApplicationIntegrationConnectionForm(
  formBuilder: FormBuilder,
): ApplicationIntegrationConnectionFormGroup {
  const form = formBuilder.group({
    systemKind: formBuilder.nonNullable.control<ApplicationIntegrationSystemKind>(
      'application',
      Validators.required,
    ),
    applicationId: formBuilder.control<number | null>(null),
    externalSystemId: formBuilder.control<number | null>(null),
    technologyId: formBuilder.control<number | null>(null, Validators.required),
    user: formBuilder.control<SoffidPersonControlValue>(null, [
      Validators.required,
      selectedIntegrationUserValidator,
    ]),
    requiredRoles: formBuilder.nonNullable.control<ApplicationIntegrationRoleOption[]>(
      [],
      Validators.required,
    ),
  });
  configureApplicationIntegrationSystemKind(form);
  return form;
}

/** Requires the selector of the chosen system kind and clears the other one. */
export function configureApplicationIntegrationSystemKind(
  form: ApplicationIntegrationConnectionFormGroup,
): void {
  const { applicationId, externalSystemId, systemKind } = form.controls;
  const [required, unused] =
    systemKind.value === 'external'
      ? [externalSystemId, applicationId]
      : [applicationId, externalSystemId];
  required.setValidators(Validators.required);
  unused.clearValidators();
  if (unused.value !== null) unused.setValue(null, { emitEvent: false });
  required.updateValueAndValidity({ emitEvent: false });
  unused.updateValueAndValidity({ emitEvent: false });
}

/** Accepts only a Soffid candidate, or a saved user, that carries its user code. */
export function selectedIntegrationUserValidator(control: AbstractControl): ValidationErrors | null {
  const value = control.value as SoffidPersonControlValue;
  if (value == null || value === '') return null;
  if (typeof value === 'string') return { soffidSelection: true };
  return value.userName?.trim() ? null : { soffidSelection: true };
}
