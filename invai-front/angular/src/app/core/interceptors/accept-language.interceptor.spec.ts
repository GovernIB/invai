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

  it.each([
    '/invaiback',
    '/invaiback/application',
    '/invaiback/application?page=0',
    '/invaiapi/externa/api/application',
  ])('adds the Catalan locale to backend request %s', (url) => {
    const { http, httpTesting } = setup('ca');

    http.get(url).subscribe();

    const request = httpTesting.expectOne(url);
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

  it.each([
    '/assets/help/index.json',
    '/invaiback-other/application',
    '/invaiapi/interna/soffid/users',
  ])('does not add the locale to unrelated request %s', (url) => {
    const { http, httpTesting } = setup('ca');

    http.get(url).subscribe();

    const request = httpTesting.expectOne(url);
    expect(request.request.headers.has('Accept-Language')).toBe(false);
    request.flush(null);
  });
});
