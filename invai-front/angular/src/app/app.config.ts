import { provideHttpClient, withInterceptors, withInterceptorsFromDi } from '@angular/common/http';
import {
  ApplicationConfig,
  provideAppInitializer,
  provideBrowserGlobalErrorListeners,
} from '@angular/core';
import { provideRouter } from '@angular/router';
import { initApp } from '@core/auth/app.initializer';
import { credentialsInterceptor } from '@core/interceptors/credentials.interceptor';
import { httpErrorLoggingInterceptor } from '@core/interceptors/http-error-logging.interceptor';
import { provideMarkdown } from 'ngx-markdown';
import { MessageService } from 'primeng/api';
import { providePrimeNG } from 'primeng/config';
import { routes } from './app.routes';
import { primengLocale } from './primeng-locale.config';
import { InvaiPreset } from './theme/invai-preset';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(
      withInterceptors([credentialsInterceptor, httpErrorLoggingInterceptor]),
      withInterceptorsFromDi(),
    ),
    provideAppInitializer(initApp),
    providePrimeNG({
      theme: {
        preset: InvaiPreset,
        options: {
          darkModeSelector: false,
          cssLayer: {
            name: 'primeng',
            order: 'theme, base, primeng, utilities',
          },
        },
      },
      overlayAppendTo: 'body',
      translation: primengLocale,
    }),
    provideMarkdown(),
    MessageService,
  ],
};
