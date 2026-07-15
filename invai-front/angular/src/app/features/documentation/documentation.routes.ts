import { Routes } from '@angular/router';
import { DOCUMENTATION_ROUTES_LOC } from './documentation.routes.i18n';

export const DOCUMENTATION_ROUTES: Routes = [
  {
    path: DOCUMENTATION_ROUTES_LOC.BASE,
    children: [
      /*  {
        path: DOCUMENTATION_ROUTES_LOC.APPLICATION.ELECTRONIC_PROCEDURES,
        loadComponent: () => import('./core/components/route-tester/route-tester'),
        data: {
          breadcrumb: $localize`Procediments electrònics`,
        },
      }, */
    ],
  },
];
