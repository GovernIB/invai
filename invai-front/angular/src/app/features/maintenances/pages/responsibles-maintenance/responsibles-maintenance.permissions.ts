import { UserPermission, UserPermissions } from '@core/auth/user-permissions.model';

import { MaintenancePanel, RESPONSIBLES_MAINTENANCE_PANELS } from '../../maintenances.constants';

// Permission each panel of the responsibles maintenance needs to load: the back-end only lets
// INV_SUPER transfer roles, and only who can change the responsibles of an application read the
// people. Every user can read the authorizations and the companies.
const RESPONSIBLES_PANEL_PERMISSIONS: Partial<Record<string, UserPermission>> = {
  'role-transfer': 'superUser',
  people: 'responsibleWrite',
};

export function canOpenResponsiblesPanel(permissions: UserPermissions, panelId: string): boolean {
  const permission = RESPONSIBLES_PANEL_PERMISSIONS[panelId];
  return permission === undefined || permissions[permission];
}

export function responsiblesMaintenancePanels(permissions: UserPermissions): MaintenancePanel[] {
  return RESPONSIBLES_MAINTENANCE_PANELS.filter(({ id }) =>
    canOpenResponsiblesPanel(permissions, id),
  );
}
