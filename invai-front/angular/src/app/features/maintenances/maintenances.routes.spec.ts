import { TestBed } from '@angular/core/testing';
import { Router, UrlTree } from '@angular/router';
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
import { SYSTEMS_ROUTES_LOC } from '@features/systems/systems.routes.i18n';
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

import { MAINTENANCES_ROUTES } from './maintenances.routes';
import { MAINTENANCES_ROUTES_LOC } from './maintenances.routes.i18n';

describe('MAINTENANCES_ROUTES', () => {
  const children = MAINTENANCES_ROUTES[0].children!;

  it('redirects the maintenance root to general', () => {
    expect(children.find(({ path }) => path === '')?.redirectTo).toBe(
      MAINTENANCES_ROUTES_LOC.GENERAL,
    );
  });

  it('preloads all general maintenance lists on the grouped route', () => {
    const route = children.find(({ path }) => path === MAINTENANCES_ROUTES_LOC.GENERAL);

    expect(route?.resolve).toEqual({
      [CATEGORIES_LIST_RESOLVE_KEY]: categoriesListResolver,
      [SYSTEM_TYPES_LIST_RESOLVE_KEY]: systemTypesListResolver,
      [FIELDS_LIST_RESOLVE_KEY]: fieldsListResolver,
      [COMMISSIONS_LIST_RESOLVE_KEY]: commissionsListResolver,
    });
  });

  it('preloads the development catalogs and active layer options', () => {
    const route = children.find(
      ({ path }) => path === MAINTENANCES_ROUTES_LOC.DEVELOPMENT,
    );

    expect(route?.resolve).toEqual({
      [ROLES_LIST_RESOLVE_KEY]: rolesListResolver,
      [LAYERS_LIST_RESOLVE_KEY]: layersListResolver,
      [TECHNOLOGIES_LIST_RESOLVE_KEY]: technologiesListResolver,
      [LAYER_CATALOG_RESOLVE_KEY]: layerCatalogResolver,
    });
  });

  it.each([
    [MAINTENANCES_ROUTES_LOC.CATEGORIES, MAINTENANCES_ROUTES_LOC.GENERAL, 'categories'],
    [MAINTENANCES_ROUTES_LOC.SYSTEM_TYPES, MAINTENANCES_ROUTES_LOC.GENERAL, 'system-types'],
    [MAINTENANCES_ROUTES_LOC.FIELDS, MAINTENANCES_ROUTES_LOC.GENERAL, 'fields'],
    [MAINTENANCES_ROUTES_LOC.COMMISSIONS, MAINTENANCES_ROUTES_LOC.GENERAL, 'commissions'],
  ])('redirects %s to its grouped panel', (legacyPath, sectionPath, fragment) => {
    const expectedTree = {} as UrlTree;
    const createUrlTree = vi.fn(() => expectedTree);
    TestBed.configureTestingModule({
      providers: [{ provide: Router, useValue: { createUrlTree } }],
    });
    const route = children.find(({ path }) => path === legacyPath);

    expect(typeof route?.redirectTo).toBe('function');
    const result = TestBed.runInInjectionContext(() =>
      (route!.redirectTo as (data: object) => UrlTree)({}),
    );

    expect(result).toBe(expectedTree);
    expect(createUrlTree).toHaveBeenCalledWith(
      ['/', MAINTENANCES_ROUTES_LOC.BASE, sectionPath],
      { fragment },
    );
  });

  it.each([
    MAINTENANCES_ROUTES_LOC.SYSTEMS_DATABASES,
    MAINTENANCES_ROUTES_LOC.ENVIRONMENTS,
  ])('redirects the moved %s page to the environments panel in systems', (legacyPath) => {
    const expectedTree = {} as UrlTree;
    const createUrlTree = vi.fn(() => expectedTree);
    TestBed.configureTestingModule({
      providers: [{ provide: Router, useValue: { createUrlTree } }],
    });
    const route = children.find(({ path }) => path === legacyPath);

    expect(typeof route?.redirectTo).toBe('function');
    const result = TestBed.runInInjectionContext(() =>
      (route!.redirectTo as (data: object) => UrlTree)({}),
    );

    expect(result).toBe(expectedTree);
    expect(createUrlTree).toHaveBeenCalledWith(
      ['/', SYSTEMS_ROUTES_LOC.BASE],
      { fragment: 'environments' },
    );
  });
});
