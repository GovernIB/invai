import { inject } from '@angular/core';
import { OAuthService } from '@core/services/auth.service';
import { LanguagePreferenceService } from '@core/services/language-preference.service';
import { firstValueFrom } from 'rxjs';

export async function initAuth(): Promise<void> {
  const oAuthService = inject(OAuthService);
  if (inject(LanguagePreferenceService).restore()) {
    // Load the preferred translated bundle before starting authentication.
    return new Promise<void>(() => undefined);
  }

  const session = await firstValueFrom(oAuthService.checkSession());

  if (session.authenticated) {
    return;
  }

  await firstValueFrom(oAuthService.login());

  // Keep Angular's bootstrap blocked until the browser leaves for the login page.
  return new Promise<void>(() => undefined);
}
