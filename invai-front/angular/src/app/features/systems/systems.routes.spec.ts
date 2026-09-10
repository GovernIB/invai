import { TestBed } from '@angular/core/testing';
import { Router, UrlTree } from '@angular/router';
import { MENU_ITEMS } from '@core/components/main-layout/menu-items';
import {
  MAINTENANCES_ROUTES_LABELS,
  MAINTENANCES_ROUTES_LOC,
} from '@features/maintenances/maintenances.routes.i18n';

import { routes } from '../../app.routes';
import { SYSTEMS_ROUTES } from './systems.routes';
import { SYSTEMS_ROUTES_LABELS, SYSTEMS_ROUTES_LOC } from './systems.routes.i18n';

describe('systems routes', () => {
  it('keeps the old top-level route only for compatibility', () => {
    const appRoute = routes.find(({ path }) => path === SYSTEMS_ROUTES_LOC.BASE);
    const topLevelMenuItem = MENU_ITEMS.find(({ label }) => label === SYSTEMS_ROUTES_LABELS.BASE);
    const maintenanceMenu = MENU_ITEMS.find(
      ({ label }) => label === MAINTENANCES_ROUTES_LABELS.BASE,
    );

    expect(appRoute?.loadChildren).toBeDefined();
    expect(topLevelMenuItem).toBeUndefined();
    expect(
      maintenanceMenu?.items?.find(({ label }) => label === SYSTEMS_ROUTES_LABELS.BASE),
    ).toMatchObject({
      routerLink: `/${MAINTENANCES_ROUTES_LOC.BASE}/${SYSTEMS_ROUTES_LOC.BASE}`,
    });
  });

  it('redirects the old systems root while preserving query parameters and fragment', () => {
    const expectedTree = {} as UrlTree;
    const createUrlTree = vi.fn(() => expectedTree);
    TestBed.configureTestingModule({
      providers: [{ provide: Router, useValue: { createUrlTree } }],
    });
    const route = SYSTEMS_ROUTES.find(({ path }) => path === '');

    const result = TestBed.runInInjectionContext(() =>
      (
        route!.redirectTo as (data: {
          queryParams: Record<string, string>;
          fragment: string | null;
        }) => UrlTree
      )({ queryParams: { view: 'compact' }, fragment: 'databases' }),
    );

    expect(result).toBe(expectedTree);
    expect(createUrlTree).toHaveBeenCalledWith(
      ['/', MAINTENANCES_ROUTES_LOC.BASE, SYSTEMS_ROUTES_LOC.BASE],
      { queryParams: { view: 'compact' }, fragment: 'databases' },
    );
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

    const result = TestBed.runInInjectionContext(() =>
      (route!.redirectTo as (data: { queryParams: Record<string, string> }) => UrlTree)({
        queryParams: { view: 'compact' },
      }),
    );

    expect(result).toBe(expectedTree);
    expect(createUrlTree).toHaveBeenCalledWith(
      ['/', MAINTENANCES_ROUTES_LOC.BASE, SYSTEMS_ROUTES_LOC.BASE],
      { queryParams: { view: 'compact' }, fragment },
    );
  });
});
