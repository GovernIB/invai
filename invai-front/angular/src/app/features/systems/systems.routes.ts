import { inject } from '@angular/core';
import { Router, Routes } from '@angular/router';
import {
  ENVIRONMENTS_LIST_RESOLVE_KEY,
  environmentsListResolver,
} from '@features/environments/pages/list/environments-list.resolver';

import { SYSTEMS_ROUTES_LOC } from './systems.routes.i18n';

export const SYSTEMS_ROUTES: Routes = [
  {
    path: '',
    pathMatch: 'full',
    loadComponent: () =>
      import('./pages/systems-shell/systems-shell').then((m) => m.SystemsShell),
    resolve: {
      [ENVIRONMENTS_LIST_RESOLVE_KEY]: environmentsListResolver,
    },
  },
  legacySystemRoute(SYSTEMS_ROUTES_LOC.DATABASES, 'databases'),
  legacySystemRoute(SYSTEMS_ROUTES_LOC.SERVERS, 'servers'),
];

function legacySystemRoute(path: string, panelId: string) {
  return {
    path,
    redirectTo: () =>
      inject(Router).createUrlTree(
        ['/', SYSTEMS_ROUTES_LOC.BASE],
        { fragment: panelId },
      ),
  };
}
