import {
  HttpClient,
  HttpErrorResponse,
  provideHttpClient,
  withInterceptors,
} from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { AUTH_REDIRECT } from '@core/services/auth.service';
import { environment } from '@environments/environment';

import { authSessionInterceptor } from './auth-session.interceptor';

describe('authSessionInterceptor', () => {
  let http: HttpClient;
  let httpTesting: HttpTestingController;
  let redirect: ReturnType<typeof vi.fn>;

  const authMeUrl = `${environment.apiBasePath}/auth/me`;
  const protectedUrl = `${environment.apiBasePath}/application`;

  beforeEach(() => {
    redirect = vi.fn();

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authSessionInterceptor])),
        provideHttpClientTesting(),
        { provide: AUTH_REDIRECT, useValue: redirect },
      ],
    });

    http = TestBed.inject(HttpClient);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('checks the session and propagates the original 401 when it is still authenticated', () => {
    const errorHandler = vi.fn();

    http.get(protectedUrl).subscribe({ error: errorHandler });
    flushUnauthorized(protectedUrl);

    httpTesting
      .expectOne(authMeUrl)
      .flush({ authenticated: true, username: 'mgarcia' });

    expectUnauthorized(errorHandler, protectedUrl);
    httpTesting.expectNone(environment.loginConfigUrl);
    expect(redirect).not.toHaveBeenCalled();
  });

  it('redirects to login and propagates the original 401 when the session has expired', () => {
    const errorHandler = vi.fn();
    const loginUrl = 'https://invai.plexus.services/invaiapi/api/auth/login';

    http.get(protectedUrl).subscribe({ error: errorHandler });
    flushUnauthorized(protectedUrl);

    httpTesting.expectOne(authMeUrl).flush(null, {
      status: 401,
      statusText: 'Unauthorized',
    });
    httpTesting.expectOne(environment.loginConfigUrl).flush({ url: loginUrl });

    expect(redirect).toHaveBeenCalledOnce();
    expect(redirect).toHaveBeenCalledWith(loginUrl);
    expectUnauthorized(errorHandler, protectedUrl);
  });

  it('shares session recovery between concurrent 401 responses', () => {
    const firstErrorHandler = vi.fn();
    const secondErrorHandler = vi.fn();
    const secondProtectedUrl = `${environment.apiBasePath}/category`;
    const loginUrl = 'https://invai.plexus.services/invaiapi/api/auth/login';

    http.get(protectedUrl).subscribe({ error: firstErrorHandler });
    http.get(secondProtectedUrl).subscribe({ error: secondErrorHandler });

    flushUnauthorized(protectedUrl);
    flushUnauthorized(secondProtectedUrl);

    httpTesting.expectOne(authMeUrl).flush({ authenticated: false });
    httpTesting.expectOne(environment.loginConfigUrl).flush({ url: loginUrl });

    expect(redirect).toHaveBeenCalledOnce();
    expectUnauthorized(firstErrorHandler, protectedUrl);
    expectUnauthorized(secondErrorHandler, secondProtectedUrl);
  });

  it('does not redirect when checking the session fails for a technical reason', () => {
    const errorHandler = vi.fn();

    http.get(protectedUrl).subscribe({ error: errorHandler });
    flushUnauthorized(protectedUrl);

    httpTesting.expectOne(authMeUrl).flush('Service unavailable', {
      status: 503,
      statusText: 'Service Unavailable',
    });

    expectUnauthorized(errorHandler, protectedUrl);
    httpTesting.expectNone(environment.loginConfigUrl);
    expect(redirect).not.toHaveBeenCalled();
  });

  it('preserves the original 401 when loading the login configuration fails', () => {
    const errorHandler = vi.fn();

    http.get(protectedUrl).subscribe({ error: errorHandler });
    flushUnauthorized(protectedUrl);

    httpTesting.expectOne(authMeUrl).flush(null, {
      status: 401,
      statusText: 'Unauthorized',
    });
    httpTesting.expectOne(environment.loginConfigUrl).flush('Configuration unavailable', {
      status: 503,
      statusText: 'Service Unavailable',
    });

    expectUnauthorized(errorHandler, protectedUrl);
    expect(redirect).not.toHaveBeenCalled();
  });

  it.each([
    `${environment.apiBasePath}/auth/me`,
    `${environment.apiBasePath}/auth/logout`,
    environment.loginConfigUrl,
    '/assets/help/index.json',
  ])('does not recover the session for an excluded URL (%s)', (url) => {
    const errorHandler = vi.fn();

    http.get(url).subscribe({ error: errorHandler });
    flushUnauthorized(url);

    expectUnauthorized(errorHandler, url);
    httpTesting.expectNone(environment.loginConfigUrl);
    expect(redirect).not.toHaveBeenCalled();
  });

  it('propagates non-401 errors without checking the session', () => {
    const errorHandler = vi.fn();

    http.get(protectedUrl).subscribe({ error: errorHandler });
    httpTesting.expectOne(protectedUrl).flush('Forbidden', {
      status: 403,
      statusText: 'Forbidden',
    });

    expect(errorHandler).toHaveBeenCalledOnce();
    expect(errorHandler.mock.calls[0][0]).toBeInstanceOf(HttpErrorResponse);
    expect(errorHandler.mock.calls[0][0].status).toBe(403);
    httpTesting.expectNone(authMeUrl);
    expect(redirect).not.toHaveBeenCalled();
  });

  function flushUnauthorized(url: string): void {
    httpTesting.expectOne(url).flush('Unauthorized', {
      status: 401,
      statusText: 'Unauthorized',
    });
  }

  function expectUnauthorized(errorHandler: ReturnType<typeof vi.fn>, url: string): void {
    expect(errorHandler).toHaveBeenCalledOnce();

    const error = errorHandler.mock.calls[0][0];
    expect(error).toBeInstanceOf(HttpErrorResponse);
    expect(error.status).toBe(401);
    expect(error.url).toBe(url);
  }
});
