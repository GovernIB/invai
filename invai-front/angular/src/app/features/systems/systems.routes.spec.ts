import { TestBed } from '@angular/core/testing';
import { Router, UrlTree } from '@angular/router';
import { MENU_ITEMS } from '@core/components/main-layout/menu-items';
import {
  ENVIRONMENTS_LIST_RESOLVE_KEY,
  environmentsListResolver,
} from '@features/environments/pages/list/environments-list.resolver';
import { SYSTEMS_ROUTES } from './systems.routes';
import { SYSTEMS_ROUTES_LABELS, SYSTEMS_ROUTES_LOC } from './systems.routes.i18n';
import { routes } from '../../app.routes';

describe('systems routes', () => {
  it('registers the top-level route and menu item', () => {
    const appRoute = routes.find(({ path }) => path === SYSTEMS_ROUTES_LOC.BASE);
    const menuItem = MENU_ITEMS.find(({ label }) => label === SYSTEMS_ROUTES_LABELS.BASE);

    expect(appRoute?.data?.['breadcrumb']).toBe(SYSTEMS_ROUTES_LABELS.BASE);
    expect(menuItem?.routerLink).toBe(SYSTEMS_ROUTES_LOC.BASE);
  });

  it('keeps only the backend-backed environments resolver on the grouped systems page', () => {
    const groupedRoute = SYSTEMS_ROUTES.find(({ path }) => path === '');

    expect(groupedRoute?.pathMatch).toBe('full');
    expect(groupedRoute?.resolve).toEqual({
      [ENVIRONMENTS_LIST_RESOLVE_KEY]: environmentsListResolver,
    });
  });

  it.each([
    [SYSTEMS_ROUTES_LOC.DATABASES, 'databases'],
    [SYSTEMS_ROUTES_LOC.SERVERS, 'servers'],
  ])('redirects the legacy %s page to its grouped panel', (legacyPath, fragment) => {
    const expectedTree = {} as UrlTree;
    const createUrlTree = vi.fn(() => expectedTree);
    TestBed.configureTestingModule({
      providers: [{ provide: Router, useValue: { createUrlTree } }],
    });
    const route = SYSTEMS_ROUTES.find(({ path }) => path === legacyPath);

    expect(typeof route?.redirectTo).toBe('function');
    const result = TestBed.runInInjectionContext(() =>
      (route!.redirectTo as (data: object) => UrlTree)({}),
    );

    expect(result).toBe(expectedTree);
    expect(createUrlTree).toHaveBeenCalledWith(
      ['/', SYSTEMS_ROUTES_LOC.BASE],
      { fragment },
    );
  });

});
