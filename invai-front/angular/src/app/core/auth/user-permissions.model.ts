// Areas of INVAI the logged-in user can use, as GET /auth/me reports them in `permissions`. The
// back-end computes each one with the same expression that secures the endpoints of the area, so
// what is false here answers with HTTP 403 there.
export interface UserPermissions {
  // Systems and databases tab of an application, and its maintenances.
  systems: boolean;
  // Development tab of an application, and its maintenances.
  development: boolean;
  // Security tab of an application, including the assignment of the Soffid roles.
  security: boolean;
  // Open data and reuse tab of an application.
  data: boolean;
  // Accessibility tab of an application, and its maintenances.
  accessibility: boolean;
  // Integration tab of an application, and its maintenances.
  integration: boolean;
  // Documentation tab of an application, and its maintenance.
  documentation: boolean;
  // DIR3 audit of a single application.
  dir3: boolean;
  // Responsibles tab of an application, read-only (also true for who can change it).
  responsibleRead: boolean;
  // Responsibles tab of an application, editable, the manual DIR3 validation and the people.
  responsibleWrite: boolean;
  // Everything: application management, general and responsible maintenances, the role transfer
  // and the DIR3 audit of every application.
  superUser: boolean;
}

export type UserPermission = keyof UserPermissions;

function permissionsWith(granted: (permission: UserPermission) => boolean): UserPermissions {
  return {
    systems: granted('systems'),
    development: granted('development'),
    security: granted('security'),
    data: granted('data'),
    accessibility: granted('accessibility'),
    integration: granted('integration'),
    documentation: granted('documentation'),
    dir3: granted('dir3'),
    responsibleRead: granted('responsibleRead'),
    responsibleWrite: granted('responsibleWrite'),
    superUser: granted('superUser'),
  };
}

// Nobody is logged in.
export const NO_USER_PERMISSIONS: Readonly<UserPermissions> = Object.freeze(
  permissionsWith(() => false),
);

// What a back-end older than 1.0.7, which doesn't send `permissions`, lets its users do: it only
// admits INV_SUPER, so the user can use everything.
export const ALL_USER_PERMISSIONS: Readonly<UserPermissions> = Object.freeze(
  permissionsWith(() => true),
);

// Reads the `permissions` of GET /auth/me. A permission that is missing or isn't `true` is denied;
// without the whole object the back-end predates them (see ALL_USER_PERMISSIONS).
export function toUserPermissions(response: unknown): UserPermissions {
  if (response === undefined || response === null) return { ...ALL_USER_PERMISSIONS };
  if (typeof response !== 'object') return { ...NO_USER_PERMISSIONS };

  const values = response as Partial<Record<UserPermission, unknown>>;
  return permissionsWith((permission) => values[permission] === true);
}
