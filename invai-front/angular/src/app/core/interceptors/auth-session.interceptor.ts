import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { OAuthService } from '@core/services/auth.service';
import { environment } from '@environments/environment';
import { catchError, switchMap, throwError } from 'rxjs';

const authUrl = `${environment.apiBasePath}/auth`;

export const authSessionInterceptor: HttpInterceptorFn = (request, next) => {
  if (!isProtectedApiRequest(request.url)) {
    return next(request);
  }

  const oAuthService = inject(OAuthService);

  return next(request).pipe(
    catchError((error: unknown) => {
      if (!(error instanceof HttpErrorResponse) || error.status !== 401) {
        return throwError(() => error);
      }

      return oAuthService
        .recoverFromUnauthorized()
        .pipe(switchMap(() => throwError(() => error)));
    }),
  );
};

function isProtectedApiRequest(url: string): boolean {
  const isInternalApi =
    url === environment.apiBasePath || url.startsWith(`${environment.apiBasePath}/`);
  const isAuthEndpoint = url === authUrl || url.startsWith(`${authUrl}/`);

  return isInternalApi && !isAuthEndpoint;
}
