import { Application } from './applications.model';

export type ApplicationCompleteness = Pick<
  Application,
  | 'incomplete'
  | 'missingResponsibleTypes'
  | 'missingAuthorized'
  | 'missingDevelopmentFields'
  | 'missingSystems'
  | 'missingDatabases'
  | 'missingAccessibilityFields'
  | 'missingSecurityData'
>;

/** Copies server flags without inferring completeness from missing values or form data. */
export function readApplicationCompleteness(
  response: ApplicationCompleteness,
): ApplicationCompleteness {
  return {
    incomplete: response.incomplete ?? null,
    missingResponsibleTypes: response.missingResponsibleTypes ?? null,
    missingAuthorized: response.missingAuthorized ?? null,
    missingDevelopmentFields: response.missingDevelopmentFields ?? null,
    missingSystems: response.missingSystems ?? null,
    missingDatabases: response.missingDatabases ?? null,
    missingAccessibilityFields: response.missingAccessibilityFields ?? null,
    missingSecurityData: response.missingSecurityData ?? null,
  };
}
