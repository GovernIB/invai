import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { LOCALE_ID } from '@angular/core';
import { TestBed } from '@angular/core/testing';

import { acceptLanguageInterceptor } from './accept-language.interceptor';

describe('acceptLanguageInterceptor', () => {
  const setup = (locale: string) => {
    TestBed.configureTestingModule({
      providers: [
        { provide: LOCALE_ID, useValue: locale },
        provideHttpClient(withInterceptors([acceptLanguageInterceptor])),
        provideHttpClientTesting(),
      ],
    });

    return {
      http: TestBed.inject(HttpClient),
      httpTesting: TestBed.inject(HttpTestingController),
    };
  };

  afterEach(() => {
    TestBed.inject(HttpTestingController).verify();
  });

  it('adds the Catalan locale to backend requests', () => {
    const { http, httpTesting } = setup('ca');

    http.get('/invaiapi/interna/application').subscribe();

    const request = httpTesting.expectOne('/invaiapi/interna/application');
    expect(request.request.headers.get('Accept-Language')).toBe('ca');
    request.flush(null);
  });

  it('adds the Spanish locale to backend requests', () => {
    const { http, httpTesting } = setup('es');

    http.get('/invaiapi/externa/config/url').subscribe();

    const request = httpTesting.expectOne('/invaiapi/externa/config/url');
    expect(request.request.headers.get('Accept-Language')).toBe('es');
    request.flush(null);
  });

  it('does not add the locale to requests outside the backend API', () => {
    const { http, httpTesting } = setup('ca');

    http.get('/assets/help/index.json').subscribe();

    const request = httpTesting.expectOne('/assets/help/index.json');
    expect(request.request.headers.has('Accept-Language')).toBe(false);
    request.flush(null);
  });
});
