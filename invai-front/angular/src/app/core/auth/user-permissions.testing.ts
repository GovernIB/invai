import { Provider, signal } from '@angular/core';
import { OAuthService } from '@core/services/auth.service';

import { ALL_USER_PERMISSIONS, UserPermissions } from './user-permissions.model';

// Replaces OAuthService in a test with a logged-in user that has every permission except the ones
// overridden.
export function provideUserPermissions(overrides: Partial<UserPermissions> = {}): Provider {
  const permissions = signal<UserPermissions>({ ...ALL_USER_PERMISSIONS, ...overrides });
  return { provide: OAuthService, useValue: { permissions: permissions.asReadonly() } };
}
