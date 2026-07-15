import { TestBed } from '@angular/core/testing';
import { OAuthService } from '@core/services/auth.service';
import { of, throwError } from 'rxjs';

import { initAuth } from './auth.initializer';

describe('initAuth', () => {
  let checkSession: ReturnType<typeof vi.fn>;
  let login: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    checkSession = vi.fn(() =>
      of({
        authenticated: true,
        user: {
          id: 'mgarcia',
          username: 'mgarcia',
        },
      }),
    );
    login = vi.fn(() => of(undefined));

    TestBed.configureTestingModule({
      providers: [
        {
          provide: OAuthService,
          useValue: {
            checkSession,
            login,
          },
        },
      ],
    });
  });

  it('should check the current session during app initialization', async () => {
    await TestBed.runInInjectionContext(() => initAuth());

    expect(checkSession).toHaveBeenCalledOnce();
  });

  it('should not request login when the user is already authenticated', async () => {
    await TestBed.runInInjectionContext(() => initAuth());

    expect(checkSession).toHaveBeenCalledOnce();
    expect(login).not.toHaveBeenCalled();
  });

  it('should request login and keep initialization pending when there is no session', async () => {
    checkSession.mockReturnValueOnce(
      of({
        authenticated: false,
        user: null,
      }),
    );

    const initialization = TestBed.runInInjectionContext(() => initAuth());
    let settled = false;
    void initialization.finally(() => {
      settled = true;
    });

    await flushMicrotasks();

    expect(checkSession).toHaveBeenCalledOnce();
    expect(login).toHaveBeenCalledOnce();
    expect(settled).toBe(false);
  });

  it('should reject app initialization when login fails', async () => {
    checkSession.mockReturnValueOnce(
      of({
        authenticated: false,
        user: null,
      }),
    );
    login.mockReturnValue(throwError(() => new Error('Configuration error')));

    await expect(TestBed.runInInjectionContext(() => initAuth())).rejects.toThrow(
      'Configuration error',
    );
    expect(checkSession).toHaveBeenCalledOnce();
    expect(login).toHaveBeenCalledOnce();
  });

  it('should reject app initialization when session checking fails', async () => {
    checkSession.mockReturnValue(throwError(() => new Error('Session error')));

    await expect(TestBed.runInInjectionContext(() => initAuth())).rejects.toThrow('Session error');
    expect(checkSession).toHaveBeenCalledOnce();
    expect(login).not.toHaveBeenCalled();
  });
});

async function flushMicrotasks(): Promise<void> {
  await Promise.resolve();
  await Promise.resolve();
}
