import { HttpInterceptorFn } from '@angular/common/http';
import { inject, LOCALE_ID } from '@angular/core';
import { environment } from '@environments/environment';

const API_PATHS = [
  environment.apiBasePath,
  environment.apiExternalBasePath,
  environment.loginConfigUrl,
];

export const acceptLanguageInterceptor: HttpInterceptorFn = (request, next) => {
  const locale = inject(LOCALE_ID);

  const path = request.url.split(/[?#]/, 1)[0];
  if (!API_PATHS.some((base) => path === base || path.startsWith(`${base}/`))) {
    return next(request);
  }

  return next(
    request.clone({
      setHeaders: {
        'Accept-Language': locale,
      },
    }),
  );
};
