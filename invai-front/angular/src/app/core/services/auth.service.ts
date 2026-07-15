import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, InjectionToken, computed, inject, signal } from '@angular/core';
import { environment } from '@environments/environment';
import { EMPTY, Observable, catchError, finalize, map, of, tap, throwError } from 'rxjs';

export interface AuthUser {
  id: string | number;
  username: string;
  email?: string;
  roles?: string[];
  permissions?: string[];
}

interface LoginConfiguration {
  url: string;
}

export interface AuthSession {
  authenticated: boolean;
  user: AuthUser | null;
}

interface UserAuthResponse {
  authenticated: boolean;
  username?: string | null;
}

class UnauthenticatedSessionError extends Error {}

export const AUTH_REDIRECT = new InjectionToken<(url: string) => void>('AUTH_REDIRECT', {
  providedIn: 'root',
  factory: () => (url: string) => window.location.assign(url),
});

@Injectable({
  providedIn: 'root',
})
export class OAuthService {
  private readonly http = inject(HttpClient);
  private readonly redirect = inject(AUTH_REDIRECT);

  private readonly authUrl = `${environment.apiBasePath}/auth`;
  private readonly logoutRedirectUrl = 'https://idp.caib.es/logout.jsp';

  private readonly _currentUser = signal<AuthUser | null>(null);
  private readonly _isLoading = signal<boolean>(false);

  readonly currentUser = this._currentUser.asReadonly();
  readonly isLoading = this._isLoading.asReadonly();

  readonly isAuthenticated = computed(() => this._currentUser() != null);

  //http://10.1.8.36:30066/invaiapi/api/auth/login
  login(): Observable<void> {
    if (this._isLoading()) {
      console.log('');
      return EMPTY;
    }

    this._isLoading.set(true);

    return this.http.get<LoginConfiguration>(environment.loginConfigUrl).pipe(
      map(({ url }) => this.validateLoginUrl(url)),
      tap((url) => {
        return this.redirect(url);
      }),
      map(() => undefined),
      finalize(() => this._isLoading.set(false)),
    );
  }

  logout(): void {
    this.clearSession();

    this.http
      .post<void>(`${this.authUrl}/logout`, null, {
        withCredentials: true,
      })
      .pipe(
        catchError(() => EMPTY),
        finalize(() => this.redirect(this.logoutRedirectUrl)),
      )
      .subscribe();
  }

  loadCurrentUser(): Observable<AuthUser> {
    return this.http
      .get<UserAuthResponse>(`${this.authUrl}/me`, {
        withCredentials: true,
      })
      .pipe(
        map((response) => this.toAuthUser(response)),
        tap({
          next: (user) => {
            this._currentUser.set(user);
          },
          error: () => {
            this.clearSession();
          },
        }),
      );
  }

  checkSession(): Observable<AuthSession> {
    return this.loadCurrentUser().pipe(
      map((user) => ({ authenticated: true, user }) satisfies AuthSession),
      catchError((error: unknown) => {
        if (
          (error instanceof HttpErrorResponse && error.status === 401) ||
          error instanceof UnauthenticatedSessionError
        ) {
          this.clearSession();
          return of({ authenticated: false, user: null } satisfies AuthSession);
        }

        return throwError(() => error);
      }),
    );
  }

  hasRole(role: string): boolean {
    const user = this.currentUser();

    return Boolean(user?.roles?.includes(role));
  }

  hasPermission(permission: string): boolean {
    const user = this.currentUser();

    return Boolean(user?.permissions?.includes(permission));
  }

  private clearSession(): void {
    this._currentUser.set(null);
  }

  private toAuthUser(response: UserAuthResponse): AuthUser {
    if (!response.authenticated) {
      throw new UnauthenticatedSessionError('The current user response is not authenticated');
    }

    if (!response.username) {
      throw new Error('The authenticated user response does not contain a username');
    }

    return {
      id: response.username,
      username: response.username,
    };
  }

  private validateLoginUrl(url: unknown): string {
    if (typeof url !== 'string' || !url.trim()) {
      throw new Error('The login configuration does not contain a URL');
    }

    const parsedUrl = new URL(url);
    if (!['http:', 'https:'].includes(parsedUrl.protocol)) {
      throw new Error('The login URL must use HTTP or HTTPS');
    }

    return url;
  }
}
