import { Routes } from '@angular/router';
import {
  APPLICATIONS_LIST_RESOLVE_KEY,
  applicationsListResolver,
} from './pages/list/applications-list.resolver';
import {
  APPLICATION_OPTIONS_RESOLVE_KEY,
  applicationOptionsResolver,
} from './resolvers/application-options.resolver';
import {
  APPLICATION_DETAIL_RESOLVE_KEY,
  applicationDetailResolver,
} from './pages/detail/application-detail.resolver';
import { applicationDetailCanDeactivate } from './pages/detail/application-detail.guard';
import {
  APPLICATION_SYSTEMS_DATABASES_RESOLVE_KEY,
  applicationSystemsDatabasesResolver,
} from './pages/detail/sections/systems-databases/application-systems-databases-section.resolver';
import {
  APPLICATION_DEVELOPMENT_RESOLVE_KEY,
  applicationDevelopmentResolver,
} from './pages/detail/sections/development/application-development-section.resolver';
import {
  APPLICATION_RESPONSIBLE_RESOLVE_KEY,
  applicationResponsibleResolver,
} from './pages/detail/sections/responsible/application-responsible-section.resolver';

export const APPLICATIONS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/list/applications-list').then((m) => m.ApplicationsList),
    resolve: {
      [APPLICATIONS_LIST_RESOLVE_KEY]: applicationsListResolver,
    },
  },
  {
    path: 'new',
    loadComponent: () =>
      import('./pages/create/application-create').then((m) => m.ApplicationCreate),
    data: {
      breadcrumb: $localize`Afegir aplicació`,
    },
    resolve: {
      [APPLICATION_OPTIONS_RESOLVE_KEY]: applicationOptionsResolver,
    },
  },
  {
    path: ':id',
    canDeactivate: [applicationDetailCanDeactivate],
    loadComponent: () =>
      import('./pages/detail/application-detail').then((m) => m.ApplicationDetail),
    data: {
      breadcrumb: $localize`Detall`,
    },
    resolve: {
      [APPLICATION_DETAIL_RESOLVE_KEY]: applicationDetailResolver,
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
        resolve: {
          [APPLICATION_OPTIONS_RESOLVE_KEY]: applicationOptionsResolver,
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
        resolve: {
          [APPLICATION_RESPONSIBLE_RESOLVE_KEY]: applicationResponsibleResolver,
        },
      },
      {
        path: 'systems-databases',
        loadComponent: () =>
          import('./pages/detail/sections/systems-databases/application-systems-databases-section').then(
            (m) => m.ApplicationSystemsDatabasesSection,
          ),
        data: {
          breadcrumb: $localize`Sistemes i BD`,
        },
        resolve: {
          [APPLICATION_SYSTEMS_DATABASES_RESOLVE_KEY]: applicationSystemsDatabasesResolver,
        },
      },
      {
        path: 'development',
        loadComponent: () =>
          import('./pages/detail/sections/development/application-development-section').then(
            (m) => m.ApplicationDevelopmentSection,
          ),
        data: {
          breadcrumb: $localize`Desenvolupament`,
        },
        resolve: {
          [APPLICATION_DEVELOPMENT_RESOLVE_KEY]: applicationDevelopmentResolver,
        },
      },
    ],
  },
];
