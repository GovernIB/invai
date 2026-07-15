import { TestBed } from '@angular/core/testing';
import { AdministrativeUnitsService } from '@features/administrative-units/services/administrative-units.service';
import { ApplicationsService } from '@features/applications/services/applications.service';
import { CategoriesService } from '@features/categories/services/categories.service';
import { CommissionsService } from '@features/commissions/services/commissions.service';
import { FieldsService } from '@features/fields/services/fields.service';
import { SystemTypesService } from '@features/system-types/services/system-types.service';
import { SpringPage } from '@models/page.model';
import { OAuthService } from '@core/services/auth.service';
import { of, throwError } from 'rxjs';
import { initApp } from './app.initializer';

describe('initApp', () => {
  let applicationsService: { getPage: ReturnType<typeof vi.fn> };
  let categoriesService: { getAll: ReturnType<typeof vi.fn> };
  let commissionsService: { getAll: ReturnType<typeof vi.fn> };
  let fieldsService: { getAll: ReturnType<typeof vi.fn> };
  let administrativeUnitsService: { getAll: ReturnType<typeof vi.fn> };
  let systemTypesService: { getAll: ReturnType<typeof vi.fn> };
  let oAuthService: {
    checkSession: ReturnType<typeof vi.fn>;
    login: ReturnType<typeof vi.fn>;
  };

  beforeEach(() => {
    applicationsService = { getPage: vi.fn(() => of(page([]))) };
    categoriesService = { getAll: vi.fn(() => of(page([]))) };
    commissionsService = { getAll: vi.fn(() => of(page([]))) };
    fieldsService = { getAll: vi.fn(() => of(page([]))) };
    administrativeUnitsService = { getAll: vi.fn(() => of(page([]))) };
    systemTypesService = { getAll: vi.fn(() => of(page([]))) };
    oAuthService = {
      checkSession: vi.fn(() =>
        of({
          authenticated: true,
          user: {
            id: 'mgarcia',
            username: 'mgarcia',
          },
        }),
      ),
      login: vi.fn(() => of(undefined)),
    };

    TestBed.configureTestingModule({
      providers: [
        { provide: OAuthService, useValue: oAuthService },
        { provide: ApplicationsService, useValue: applicationsService },
        { provide: CategoriesService, useValue: categoriesService },
        { provide: CommissionsService, useValue: commissionsService },
        { provide: FieldsService, useValue: fieldsService },
        { provide: AdministrativeUnitsService, useValue: administrativeUnitsService },
        { provide: SystemTypesService, useValue: systemTypesService },
      ],
    });
  });

  it('preloads applications and catalog first pages during app initialization', async () => {
    await TestBed.runInInjectionContext(() => initApp());

    expect(applicationsService.getPage).toHaveBeenCalledWith({ page: 0, size: 10 });
    expect(categoriesService.getAll).toHaveBeenCalledOnce();
    expect(commissionsService.getAll).toHaveBeenCalledOnce();
    expect(fieldsService.getAll).toHaveBeenCalledOnce();
    expect(administrativeUnitsService.getAll).toHaveBeenCalledOnce();
    expect(systemTypesService.getAll).toHaveBeenCalledOnce();
  });

  it('redirects without preloading protected data when there is no session', async () => {
    oAuthService.checkSession.mockReturnValueOnce(
      of({
        authenticated: false,
        user: null,
      }),
    );

    const initialization = TestBed.runInInjectionContext(() => initApp());
    let settled = false;
    void initialization.finally(() => {
      settled = true;
    });

    await flushMicrotasks();

    expect(oAuthService.login).toHaveBeenCalledOnce();
    expect(applicationsService.getPage).not.toHaveBeenCalled();
    expect(categoriesService.getAll).not.toHaveBeenCalled();
    expect(commissionsService.getAll).not.toHaveBeenCalled();
    expect(fieldsService.getAll).not.toHaveBeenCalled();
    expect(administrativeUnitsService.getAll).not.toHaveBeenCalled();
    expect(systemTypesService.getAll).not.toHaveBeenCalled();
    expect(settled).toBe(false);
  });

  it('rejects without preloading protected data when the session check fails', async () => {
    oAuthService.checkSession.mockReturnValueOnce(
      throwError(() => new Error('Session unavailable')),
    );

    await expect(TestBed.runInInjectionContext(() => initApp())).rejects.toThrow(
      'Session unavailable',
    );

    expect(oAuthService.login).not.toHaveBeenCalled();
    expect(applicationsService.getPage).not.toHaveBeenCalled();
    expect(categoriesService.getAll).not.toHaveBeenCalled();
    expect(commissionsService.getAll).not.toHaveBeenCalled();
    expect(fieldsService.getAll).not.toHaveBeenCalled();
    expect(administrativeUnitsService.getAll).not.toHaveBeenCalled();
    expect(systemTypesService.getAll).not.toHaveBeenCalled();
  });

  it('rejects without preloading protected data when login configuration fails', async () => {
    oAuthService.checkSession.mockReturnValueOnce(
      of({
        authenticated: false,
        user: null,
      }),
    );
    oAuthService.login.mockReturnValueOnce(
      throwError(() => new Error('Login configuration unavailable')),
    );

    await expect(TestBed.runInInjectionContext(() => initApp())).rejects.toThrow(
      'Login configuration unavailable',
    );

    expect(oAuthService.login).toHaveBeenCalledOnce();
    expect(applicationsService.getPage).not.toHaveBeenCalled();
    expect(categoriesService.getAll).not.toHaveBeenCalled();
    expect(commissionsService.getAll).not.toHaveBeenCalled();
    expect(fieldsService.getAll).not.toHaveBeenCalled();
    expect(administrativeUnitsService.getAll).not.toHaveBeenCalled();
    expect(systemTypesService.getAll).not.toHaveBeenCalled();
  });

  it('keeps the initial loader visible for at least 600ms', async () => {
    vi.useFakeTimers();

    try {
      const initPromise = TestBed.runInInjectionContext(() => initApp());
      let resolved = false;
      initPromise.then(() => {
        resolved = true;
      });

      await vi.advanceTimersByTimeAsync(599);
      expect(resolved).toBe(false);

      await vi.advanceTimersByTimeAsync(1);
      await expect(initPromise).resolves.toBeUndefined();
      expect(resolved).toBe(true);
    } finally {
      vi.useRealTimers();
    }
  });

  it('resolves initialization even when a preload request fails', async () => {
    categoriesService.getAll.mockReturnValueOnce(
      throwError(() => new Error('Categories unavailable')),
    );

    await expect(TestBed.runInInjectionContext(() => initApp())).resolves.toBeUndefined();
    expect(categoriesService.getAll).toHaveBeenCalledOnce();
    expect(systemTypesService.getAll).toHaveBeenCalledOnce();
  });
});

function page<TItem>(content: TItem[]): SpringPage<TItem> {
  return {
    content,
    empty: content.length === 0,
    first: true,
    last: true,
    number: 0,
    numberOfElements: content.length,
    pageable: {
      offset: 0,
      pageNumber: 0,
      pageSize: 10,
      paged: true,
      sort: { empty: true, sorted: false, unsorted: true },
      unpaged: false,
    },
    size: 10,
    sort: { empty: true, sorted: false, unsorted: true },
    totalElements: content.length,
    totalPages: 1,
  };
}

async function flushMicrotasks(): Promise<void> {
  await Promise.resolve();
  await Promise.resolve();
}
