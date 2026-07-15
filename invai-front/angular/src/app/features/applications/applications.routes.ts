import { Routes } from '@angular/router';

export const APPLICATIONS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/list/applications-list').then((m) => m.ApplicationsList),
  },
  {
    path: 'new',
    loadComponent: () =>
      import('./pages/create/application-create').then((m) => m.ApplicationCreate),
    data: {
      breadcrumb: $localize`Afegir aplicació`,
    },
  },
  {
    path: ':id',
    loadComponent: () =>
      import('./pages/detail/application-detail').then((m) => m.ApplicationDetail),
    data: {
      breadcrumb: $localize`Detall`,
    },
    children: [
      { path: '', redirectTo: 'general', pathMatch: 'full' },
      {
        path: 'general',
        loadComponent: () =>
          import('./pages/detail/sections/general/application-general-section').then(
            (m) => m.ApplicationGeneralSection,
          ),
        data: {
          breadcrumb: $localize`General`,
        },
      },
      {
        path: 'responsible',
        loadComponent: () =>
          import('./pages/detail/sections/responsible/application-responsible-section').then(
            (m) => m.ApplicationResponsibleSection,
          ),
        data: {
          breadcrumb: $localize`Responsable`,
        },
      },
      {
        path: 'systems-databases',
        loadComponent: () =>
          import('./pages/detail/sections/systems-databases/application-systems-databases-section').then(
            (m) => m.ApplicationSystemsDatabasesSection,
          ),
        data: {
          breadcrumb: $localize`Sistemes i BBDD`,
        },
      },
    ],
  },
];
