import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '@environments/environment';

import { AUTH_REDIRECT, OAuthService } from './auth.service';

describe('OAuthService', () => {
  let service: OAuthService;
  let httpTesting: HttpTestingController;
  let redirect: ReturnType<typeof vi.fn>;

  const authMeUrl = `${environment.apiBasePath}/auth/me`;

  beforeEach(() => {
    redirect = vi.fn();

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: AUTH_REDIRECT, useValue: redirect },
      ],
    });

    service = TestBed.inject(OAuthService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should obtain the login configuration and redirect to its URL', () => {
    const loginUrl = 'https://invai.plexus.services/invaiapi/api/auth/login';

    service.login().subscribe();

    expect(service.isLoading()).toBe(true);

    const request = httpTesting.expectOne(environment.loginConfigUrl);
    expect(request.request.method).toBe('GET');
    request.flush({ url: loginUrl });

    expect(redirect).toHaveBeenCalledOnce();
    expect(redirect).toHaveBeenCalledWith(loginUrl);
    expect(service.isLoading()).toBe(false);
  });

  it.each([undefined, '', 'javascript:alert(1)'])(
    'should reject an invalid login URL (%s)',
    (url) => {
      const error = vi.fn();

      service.login().subscribe({ error });
      httpTesting.expectOne(environment.loginConfigUrl).flush({ url });

      expect(error).toHaveBeenCalledOnce();
      expect(redirect).not.toHaveBeenCalled();
      expect(service.isLoading()).toBe(false);
    },
  );

  it('should restore the loading state after an HTTP error', () => {
    const error = vi.fn();

    service.login().subscribe({ error });
    httpTesting.expectOne(environment.loginConfigUrl).flush('Configuration error', {
      status: 500,
      statusText: 'Internal Server Error',
    });

    expect(error).toHaveBeenCalledOnce();
    expect(redirect).not.toHaveBeenCalled();
    expect(service.isLoading()).toBe(false);
  });

  it('should ignore duplicate login attempts while loading', () => {
    service.login().subscribe();

    const duplicateCompleted = vi.fn();
    service.login().subscribe({ complete: duplicateCompleted });

    expect(duplicateCompleted).toHaveBeenCalledOnce();

    httpTesting.expectOne(environment.loginConfigUrl).flush({
      url: 'http://localhost:8080/invaiapi/api/auth/login',
    });
  });

  it('should clear the current user, close the backend session, and then redirect to the IDP', () => {
    service.loadCurrentUser().subscribe();
    httpTesting.expectOne(authMeUrl).flush({ authenticated: true, username: 'mgarcia' });

    service.logout();

    expect(service.currentUser()).toBeNull();
    expect(service.isAuthenticated()).toBe(false);
    expect(redirect).not.toHaveBeenCalled();

    const request = httpTesting.expectOne(`${environment.apiBasePath}/auth/logout`);
    expect(request.request.method).toBe('POST');
    expect(request.request.withCredentials).toBe(true);
    expect(request.request.body).toBeNull();
    request.flush(null, { status: 204, statusText: 'No Content' });

    expect(redirect).toHaveBeenCalledWith('https://idp.caib.es/logout.jsp');
  });

  it('should redirect to the IDP when closing the backend session fails', () => {
    service.logout();

    expect(redirect).not.toHaveBeenCalled();

    httpTesting.expectOne(`${environment.apiBasePath}/auth/logout`).flush('Service unavailable', {
      status: 503,
      statusText: 'Service Unavailable',
    });

    expect(service.currentUser()).toBeNull();
    expect(service.isAuthenticated()).toBe(false);
    expect(redirect).toHaveBeenCalledWith('https://idp.caib.es/logout.jsp');
  });

  it('should load the current user from the backend session endpoint', () => {
    const result = vi.fn();

    service.loadCurrentUser().subscribe(result);

    const request = httpTesting.expectOne(authMeUrl);
    expect(request.request.method).toBe('GET');
    expect(request.request.withCredentials).toBe(true);
    request.flush({ authenticated: true, username: 'mgarcia' });

    expect(result).toHaveBeenCalledWith({
      id: 'mgarcia',
      username: 'mgarcia',
    });
    expect(service.currentUser()).toEqual({
      id: 'mgarcia',
      username: 'mgarcia',
    });
    expect(service.isAuthenticated()).toBe(true);
  });

  it('should report an authenticated session when /auth/me returns a user', () => {
    const result = vi.fn();

    service.checkSession().subscribe(result);

    httpTesting.expectOne(authMeUrl).flush({ authenticated: true, username: 'mgarcia' });

    expect(result).toHaveBeenCalledWith({
      authenticated: true,
      user: {
        id: 'mgarcia',
        username: 'mgarcia',
      },
    });
    expect(service.isAuthenticated()).toBe(true);
  });

  it('should report an anonymous session when /auth/me returns unauthorized', () => {
    const result = vi.fn();

    service.checkSession().subscribe(result);

    httpTesting.expectOne(authMeUrl).flush(null, {
      status: 401,
      statusText: 'Unauthorized',
    });

    expect(result).toHaveBeenCalledWith({
      authenticated: false,
      user: null,
    });
    expect(service.currentUser()).toBeNull();
    expect(service.isAuthenticated()).toBe(false);
  });

  it('should propagate technical errors from /auth/me', () => {
    const result = vi.fn();
    const error = vi.fn();

    service.checkSession().subscribe({ next: result, error });

    httpTesting.expectOne(authMeUrl).flush('Service unavailable', {
      status: 503,
      statusText: 'Service Unavailable',
    });

    expect(result).not.toHaveBeenCalled();
    expect(error).toHaveBeenCalledOnce();
    expect(service.currentUser()).toBeNull();
    expect(service.isAuthenticated()).toBe(false);
  });

  it('should clear the current user when /auth/me returns a non-authenticated response', () => {
    const result = vi.fn();

    service.checkSession().subscribe(result);

    httpTesting.expectOne(authMeUrl).flush({ authenticated: false });

    expect(result).toHaveBeenCalledWith({
      authenticated: false,
      user: null,
    });
    expect(service.currentUser()).toBeNull();
    expect(service.isAuthenticated()).toBe(false);
  });

  it('should propagate an invalid authenticated response without a username', () => {
    const result = vi.fn();
    const error = vi.fn();

    service.checkSession().subscribe({ next: result, error });

    httpTesting.expectOne(authMeUrl).flush({ authenticated: true });

    expect(result).not.toHaveBeenCalled();
    expect(error).toHaveBeenCalledOnce();
    expect(service.currentUser()).toBeNull();
    expect(service.isAuthenticated()).toBe(false);
  });
});
