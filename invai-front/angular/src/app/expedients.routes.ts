import { Routes } from '@angular/router';
import { FILE_MANAGEMENT_ROUTES_LOC } from '@core/components/main-layout/routes/file-management.routes';

export const MANAGEMENT_FILE_ROUTES: Routes = [
  {
    path: FILE_MANAGEMENT_ROUTES_LOC.APPLICATION.BASE,
    children: [
      {
        path: FILE_MANAGEMENT_ROUTES_LOC.APPLICATION.ELECTRONIC_PROCEDURES,
        loadComponent: () => import('./core/components/route-tester/route-tester'),
        data: {
          breadcrumb: $localize`Procediments electrònics`,
        },
      },
      {
        path: FILE_MANAGEMENT_ROUTES_LOC.APPLICATION.INBOX,
        loadComponent: () => import('./core/components/route-tester/route-tester'),
        data: {
          breadcrumb: $localize`Safata d'entrada`,
        },
      },
      {
        path: FILE_MANAGEMENT_ROUTES_LOC.APPLICATION.NEW,
        loadComponent: () => import('./core/components/route-tester/route-tester'),
        data: {
          breadcrumb: $localize`Nou`,
        },
      },
    ],
  },
  {
    path: FILE_MANAGEMENT_ROUTES_LOC.INITIAL_PROCEDURES.RECORDING,
    loadComponent: () => import('./core/components/route-tester/route-tester'),
    data: {
      breadcrumb: $localize`Validació`,
    },
  },
];
