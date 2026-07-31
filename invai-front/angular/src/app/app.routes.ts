import { Routes } from '@angular/router';
import { DOCUMENTATION_ROUTES_LOC } from '@features/documentation/documentation.routes.i18n';
import {
  APPLICATIONS_ROUTES_LABELS,
  APPLICATIONS_ROUTES_LOC,
} from './features/applications/applications.routes.i18n';
import {
  MAINTENANCES_ROUTES_LABELS,
  MAINTENANCES_ROUTES_LOC,
} from './features/maintenances/maintenances.routes.i18n';
import {
  SYSTEMS_ROUTES_LABELS,
  SYSTEMS_ROUTES_LOC,
} from './features/systems/systems.routes.i18n';

export const routes: Routes = [
  {
    path: '',
    redirectTo: APPLICATIONS_ROUTES_LOC.BASE,
    pathMatch: 'full',
  },

  {
    path: APPLICATIONS_ROUTES_LOC.BASE,
    canActivate: [],
    loadChildren: () =>
      import('@features/applications/applications.routes').then((r) => r.APPLICATIONS_ROUTES),
    data: {
      breadcrumb: APPLICATIONS_ROUTES_LABELS.BASE,
    },
  },
  {
    path: DOCUMENTATION_ROUTES_LOC.BASE,
    canActivate: [],
    loadComponent: () =>
      import('@features/documentation/documentation').then((m) => m.Documentation),
    loadChildren: () =>
      import('@features/documentation/documentation.routes').then((r) => r.DOCUMENTATION_ROUTES),
    data: {
      breadcrumb: $localize`Documentació`,
    },
  },

  {
    path: MAINTENANCES_ROUTES_LOC.BASE,
    canActivate: [],
    loadChildren: () =>
      import('@features/maintenances/maintenances.routes').then((r) => r.MAINTENANCES_ROUTES),
    data: {
      breadcrumb: MAINTENANCES_ROUTES_LABELS.BASE,
    },
  },

  {
    path: SYSTEMS_ROUTES_LOC.BASE,
    canActivate: [],
    loadChildren: () => import('@features/systems/systems.routes').then((r) => r.SYSTEMS_ROUTES),
    data: {
      breadcrumb: SYSTEMS_ROUTES_LABELS.BASE,
    },
  },

  {
    path: '**',
    redirectTo: APPLICATIONS_ROUTES_LOC.BASE, // opcional: fallback para rutas no encontradas
  },
];
