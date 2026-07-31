import { MAINTENANCE_TABS } from '@features/maintenances/maintenances.constants';
import { MAINTENANCES_ROUTES_LABELS } from '@features/maintenances/maintenances.routes.i18n';

import { MENU_ITEMS } from './menu-items';

describe('MENU_ITEMS', () => {
  it('groups maintenances by the same sections as application detail', () => {
    const maintenanceMenu = MENU_ITEMS.find(
      ({ label }) => label === MAINTENANCES_ROUTES_LABELS.BASE,
    );

    expect(maintenanceMenu?.items).toEqual(
      MAINTENANCE_TABS.map((tab) => ({
        id: tab.id,
        label: tab.label,
        icon: tab.icon,
        routerLink: tab.routerLink,
      })),
    );
    expect(maintenanceMenu?.items?.map(({ label }) => label)).toEqual([
      'General',
      'Responsables',
      'Desenvolupament',
    ]);
  });
});
