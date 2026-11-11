import { ActivatedRouteSnapshot } from '@angular/router';

/**
 * Help documents that screens and dialogs open, relative to `public/help/<locale>/`. Both
 * languages use the same paths, and a test checks that both indexes still list them.
 */
export const HELP_TOPICS = {
  applicationsList: 'applications/list.md',
  applicationCreate: 'applications/create.md',
  applicationDetail: 'application-detail/overview.md',
  applicationGeneral: 'application-detail/general.md',
  applicationResponsible: 'application-detail/responsible.md',
  applicationSystemsDatabases: 'application-detail/systems-databases.md',
  applicationDevelopment: 'application-detail/development.md',
  applicationAccessibility: 'application-detail/accessibility.md',
  applicationSecurity: 'application-detail/security.md',
  applicationData: 'application-detail/data.md',
  applicationIntegrations: 'application-detail/integrations.md',
  applicationDocumentation: 'application-detail/documentation.md',
  applicationDocumentDetail: 'application-detail/document-detail.md',
  maintenanceGeneral: 'maintenances/general.md',
  maintenanceCategories: 'maintenances/categories.md',
  maintenanceSystemTypes: 'maintenances/system-types.md',
  maintenanceFields: 'maintenances/fields.md',
  maintenanceCommissions: 'maintenances/commissions.md',
  maintenanceResponsibles: 'maintenances/responsibles.md',
  maintenancePeople: 'maintenances/people.md',
  maintenanceAuthorizations: 'maintenances/authorizations.md',
  maintenanceCompanies: 'maintenances/companies.md',
  maintenanceSystems: 'maintenances/systems.md',
  maintenanceDevelopment: 'maintenances/development.md',
  maintenanceAccessibility: 'maintenances/accessibility.md',
  maintenanceSecurity: 'maintenances/security.md',
  maintenanceIntegrations: 'maintenances/integrations.md',
  maintenanceDocumentation: 'maintenances/documentation.md',
  maintenanceDocumentCategory: 'maintenances/document-category.md',
} as const;

export type HelpTopicPath = (typeof HELP_TOPICS)[keyof typeof HELP_TOPICS];

const HELP_TOPIC_KEY = 'helpTopic';

/** Route `data` of a screen with its own help document: `data: { ...helpTopic(HELP_TOPICS.x) }`. */
export const helpTopic = (document: HelpTopicPath) => ({ [HELP_TOPIC_KEY]: document });

/**
 * The document of the deepest active route that declares one, so a child screen without its
 * own document falls back to the one of the screen that contains it.
 */
export function routeHelpTopic(root: ActivatedRouteSnapshot): string | undefined {
  let topic: string | undefined;
  for (let route: ActivatedRouteSnapshot | null = root; route; route = route.firstChild) {
    topic = (route.data[HELP_TOPIC_KEY] as string | undefined) ?? topic;
  }
  return topic;
}
