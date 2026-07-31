import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { isStructuredBadRequest } from '@core/models/api-error.model';
import { ServerErrorDialogService } from '@core/services/server-error-dialog.service';
import { environment } from '@environments/environment';
import { catchError, throwError } from 'rxjs';

export const httpErrorLoggingInterceptor: HttpInterceptorFn = (request, next) => {
  const errorDialog = inject(ServerErrorDialogService);

  return next(request).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse) {
        console.error('HTTP request failed', {
          method: request.method,
          url: request.urlWithParams,
          status: error.status,
          statusText: error.statusText,
          error: error.error,
        });

        if (isInternalApiRequest(request.url) && isStructuredBadRequest(error)) {
          errorDialog.open(error.error);
        }
      }

      return throwError(() => error);
    }),
  );
};

function isInternalApiRequest(url: string): boolean {
  return url === environment.apiBasePath || url.startsWith(`${environment.apiBasePath}/`);
}
