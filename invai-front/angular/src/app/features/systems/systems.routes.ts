import { inject } from '@angular/core';
import { Route, Router, Routes } from '@angular/router';
import { MAINTENANCES_ROUTES_LOC } from '@features/maintenances/maintenances.routes.i18n';

import { SYSTEMS_ROUTES_LOC } from './systems.routes.i18n';

export const SYSTEMS_ROUTES: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: ({ queryParams, fragment }) =>
      inject(Router).createUrlTree(['/', MAINTENANCES_ROUTES_LOC.BASE, SYSTEMS_ROUTES_LOC.BASE], {
        queryParams,
        fragment: fragment ?? undefined,
      }),
  },
  legacySystemRoute(SYSTEMS_ROUTES_LOC.DATABASES, 'databases'),
  legacySystemRoute(SYSTEMS_ROUTES_LOC.SERVERS, 'servers'),
];

function legacySystemRoute(path: string, panelId: string): Route {
  return {
    path,
    redirectTo: ({ queryParams }) =>
      inject(Router).createUrlTree(['/', MAINTENANCES_ROUTES_LOC.BASE, SYSTEMS_ROUTES_LOC.BASE], {
        queryParams,
        fragment: panelId,
      }),
  };
}
