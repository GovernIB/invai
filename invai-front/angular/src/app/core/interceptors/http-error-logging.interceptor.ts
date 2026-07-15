import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';

export const httpErrorLoggingInterceptor: HttpInterceptorFn = (request, next) =>
  next(request).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse) {
        console.error('HTTP request failed', {
          method: request.method,
          url: request.urlWithParams,
          status: error.status,
          statusText: error.statusText,
          error: error.error,
        });
      }

      return throwError(() => error);
    }),
  );
