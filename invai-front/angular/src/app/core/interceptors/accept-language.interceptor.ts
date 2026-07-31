import { HttpInterceptorFn } from '@angular/common/http';
import { inject, LOCALE_ID } from '@angular/core';

const API_PATH_PREFIX = '/invaiapi/';

export const acceptLanguageInterceptor: HttpInterceptorFn = (request, next) => {
  const locale = inject(LOCALE_ID);

  if (!request.url.startsWith(API_PATH_PREFIX)) {
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
