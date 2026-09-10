import { ACCESSIBILITY_MAINTENANCE_RESOLVE_KEY, accessibilityMaintenanceResolver } from './accessibility/pages/accessibility-maintenance/accessibility-maintenance.resolver';
import { inject } from '@angular/core';
import { Route, Router, Routes } from '@angular/router';
import {
  ENVIRONMENTS_LIST_RESOLVE_KEY,
  environmentsListResolver,
} from '@features/environments/pages/list/environments-list.resolver';
import { SYSTEMS_ROUTES_LABELS, SYSTEMS_ROUTES_LOC } from '@features/systems/systems.routes.i18n';

import { MAINTENANCES_ROUTES_LABELS, MAINTENANCES_ROUTES_LOC } from './maintenances.routes.i18n';
import {
  CATEGORIES_LIST_RESOLVE_KEY,
  categoriesListResolver,
} from '@features/categories/pages/list/categories-list.resolver';
import {
  COMMISSIONS_LIST_RESOLVE_KEY,
  commissionsListResolver,
} from '@features/commissions/pages/list/commissions-list.resolver';
import {
  FIELDS_LIST_RESOLVE_KEY,
  fieldsListResolver,
} from '@features/fields/pages/list/fields-list.resolver';
import {
  SYSTEM_TYPES_LIST_RESOLVE_KEY,
  systemTypesListResolver,
} from '@features/system-types/pages/list/system-types-list.resolver';
import {
  LAYERS_LIST_RESOLVE_KEY,
  layersListResolver,
} from '@features/layers/pages/list/layers-list.resolver';
import {
  LAYER_CATALOG_RESOLVE_KEY,
  layerCatalogResolver,
} from '@features/layers/services/layer-catalog.resolver';
import {
  ROLES_LIST_RESOLVE_KEY,
  rolesListResolver,
} from '@features/roles/pages/list/roles-list.resolver';
import {
  TECHNOLOGIES_LIST_RESOLVE_KEY,
  technologiesListResolver,
} from '@features/technologies/pages/list/technologies-list.resolver';
import {
  RESPONSIBLES_MAINTENANCE_RESOLVE_KEY,
  responsiblesMaintenanceResolver,
} from './pages/responsibles-maintenance/responsibles-maintenance.resolver';
import {
  SECURITY_MAINTENANCE_RESOLVE_KEY,
  securityMaintenanceResolver,
} from './security/pages/security-maintenance/security-maintenance.resolver';

export const MAINTENANCES_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/maintenances-shell/maintenances-shell').then((m) => m.MaintenancesShell),
    children: [
      {
        path: '',
        redirectTo: MAINTENANCES_ROUTES_LOC.GENERAL,
        pathMatch: 'full',
      },
      {
        path: MAINTENANCES_ROUTES_LOC.GENERAL,
        loadComponent: () =>
          import('./pages/general-maintenance/general-maintenance').then(
            (m) => m.GeneralMaintenance,
          ),
        data: {
          breadcrumb: MAINTENANCES_ROUTES_LABELS.GENERAL,
        },
        resolve: {
          [CATEGORIES_LIST_RESOLVE_KEY]: categoriesListResolver,
          [SYSTEM_TYPES_LIST_RESOLVE_KEY]: systemTypesListResolver,
          [FIELDS_LIST_RESOLVE_KEY]: fieldsListResolver,
          [COMMISSIONS_LIST_RESOLVE_KEY]: commissionsListResolver,
        },
      },
      {
        path: MAINTENANCES_ROUTES_LOC.RESPONSIBLES,
        loadComponent: () =>
          import('./pages/responsibles-maintenance/responsibles-maintenance').then(
            (m) => m.ResponsiblesMaintenance,
          ),
        data: {
          breadcrumb: MAINTENANCES_ROUTES_LABELS.RESPONSIBLES,
        },
        resolve: {
          [RESPONSIBLES_MAINTENANCE_RESOLVE_KEY]: responsiblesMaintenanceResolver,
        },
      },
      {
        path: SYSTEMS_ROUTES_LOC.BASE,
        loadComponent: () =>
          import('@features/systems/pages/systems-shell/systems-shell').then((m) => m.SystemsShell),
        data: {
          breadcrumb: SYSTEMS_ROUTES_LABELS.BASE,
        },
        resolve: {
          [ENVIRONMENTS_LIST_RESOLVE_KEY]: environmentsListResolver,
        },
      },
      {
        path: MAINTENANCES_ROUTES_LOC.DEVELOPMENT,
        loadComponent: () =>
          import('./pages/development-maintenance/development-maintenance').then(
            (m) => m.DevelopmentMaintenance,
          ),
        data: {
          breadcrumb: MAINTENANCES_ROUTES_LABELS.DEVELOPMENT,
        },
        resolve: {
          [ROLES_LIST_RESOLVE_KEY]: rolesListResolver,
          [LAYERS_LIST_RESOLVE_KEY]: layersListResolver,
          [TECHNOLOGIES_LIST_RESOLVE_KEY]: technologiesListResolver,
          [LAYER_CATALOG_RESOLVE_KEY]: layerCatalogResolver,
        },
      },
      {
        path: MAINTENANCES_ROUTES_LOC.ACCESSIBILITY,
        loadComponent: () =>
          import('./accessibility/pages/accessibility-maintenance/accessibility-maintenance').then(
            (m) => m.AccessibilityMaintenance,
          ),
        data: { breadcrumb: MAINTENANCES_ROUTES_LABELS.ACCESSIBILITY },
        resolve: { [ACCESSIBILITY_MAINTENANCE_RESOLVE_KEY]: accessibilityMaintenanceResolver },
      },
      {
        path: MAINTENANCES_ROUTES_LOC.SECURITY,
        loadComponent: () =>
          import('./security/pages/security-maintenance/security-maintenance').then(
            (m) => m.SecurityMaintenance,
          ),
        data: {
          breadcrumb: MAINTENANCES_ROUTES_LABELS.SECURITY,
        },
        resolve: {
          [SECURITY_MAINTENANCE_RESOLVE_KEY]: securityMaintenanceResolver,
        },
      },
      legacyMaintenanceRoute(
        MAINTENANCES_ROUTES_LOC.CATEGORIES,
        MAINTENANCES_ROUTES_LOC.GENERAL,
        'categories',
      ),
      legacyMaintenanceRoute(
        MAINTENANCES_ROUTES_LOC.SYSTEM_TYPES,
        MAINTENANCES_ROUTES_LOC.GENERAL,
        'system-types',
      ),
      legacyMaintenanceRoute(
        MAINTENANCES_ROUTES_LOC.FIELDS,
        MAINTENANCES_ROUTES_LOC.GENERAL,
        'fields',
      ),
      legacyMaintenanceRoute(
        MAINTENANCES_ROUTES_LOC.COMMISSIONS,
        MAINTENANCES_ROUTES_LOC.GENERAL,
        'commissions',
      ),
      legacyEnvironmentRoute(MAINTENANCES_ROUTES_LOC.SYSTEMS_DATABASES),
      legacyEnvironmentRoute(MAINTENANCES_ROUTES_LOC.ENVIRONMENTS),
    ],
  },
];

function legacyMaintenanceRoute(path: string, sectionPath: string, panelId: string) {
  return {
    path,
    redirectTo: () =>
      inject(Router).createUrlTree(['/', MAINTENANCES_ROUTES_LOC.BASE, sectionPath], {
        fragment: panelId,
      }),
  };
}

function legacyEnvironmentRoute(path: string): Route {
  return {
    path,
    redirectTo: ({ queryParams }) =>
      inject(Router).createUrlTree(['/', MAINTENANCES_ROUTES_LOC.BASE, SYSTEMS_ROUTES_LOC.BASE], {
        queryParams,
        fragment: 'environments',
      }),
  };
}
