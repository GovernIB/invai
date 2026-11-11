import { inject } from '@angular/core';
import { CanActivateChildFn, Router } from '@angular/router';
import { UserPermission, UserPermissions } from '@core/auth/user-permissions.model';
import { OAuthService } from '@core/services/auth.service';

import { ApplicationDetailSection } from './application-detail-state';

export type ApplicationDetailTab = ApplicationDetailSection | 'documentation';

// Permission each tab of an application needs: without it the back-end answers HTTP 403 to the
// requests of the tab. The general tab and the documentation, which the back-end filters by
// category, are open to every user.
const APPLICATION_TAB_PERMISSIONS: Partial<Record<ApplicationDetailTab, UserPermission>> = {
  responsible: 'responsibleRead',
  'systems-databases': 'systems',
  development: 'development',
  accessibility: 'accessibility',
  security: 'security',
  data: 'data',
  integrations: 'integration',
};

export function canOpenApplicationTab(
  permissions: UserPermissions,
  tab: string | undefined,
): boolean {
  const permission = APPLICATION_TAB_PERMISSIONS[tab as ApplicationDetailTab];
  return permission === undefined || permissions[permission];
}

// Sends to the general tab of the application whoever opens, by its URL, a tab they can't use.
export const applicationTabGuard: CanActivateChildFn = (childRoute) => {
  if (canOpenApplicationTab(inject(OAuthService).permissions(), childRoute.routeConfig?.path)) {
    return true;
  }

  const detailPath = (childRoute.parent?.pathFromRoot ?? []).flatMap(({ url }) =>
    url.map(({ path }) => path),
  );
  return inject(Router).createUrlTree(['/', ...detailPath, 'general']);
};
