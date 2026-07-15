import { Routes } from '@angular/router';
import { DOCUMENTATION_ROUTES_LOC } from '@features/documentation/documentation.routes.i18n';
import {
  APPLICATIONS_ROUTES_LABELS,
  APPLICATIONS_ROUTES_LOC,
} from './features/applications/applications.routes.i18n';

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
    path: '**',
    redirectTo: APPLICATIONS_ROUTES_LOC.BASE, // opcional: fallback para rutas no encontradas
  },
];
