import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { Observable, firstValueFrom, of, throwError } from 'rxjs';

import { APPLICATIONS_ROUTES } from '../applications.routes';
import {
  ApplicationOptionsService,
  ApplicationSelectOptions,
} from '../services/application-options.service';
import {
  APPLICATION_OPTIONS_RESOLVE_KEY,
  ApplicationOptionsResolvedData,
  applicationOptionsResolver
} from './application-options.resolver';

const OPTIONS: ApplicationSelectOptions = {
  categories: [{ label: 'DRASSANA', value: 1 }],
  informationSystems: [],
  scopes: [],
  commissions: [],

};

describe('applicationOptionsResolver', () => {
  let getStaticOptions: ReturnType<typeof vi.fn>;
  let getDepartmentOptions: ReturnType<typeof vi.fn>;
  let getAdministrativeUnitOptions: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    getStaticOptions = vi.fn(() =>
      of({
        categories: OPTIONS.categories,
        informationSystems: OPTIONS.informationSystems,
        scopes: OPTIONS.scopes,
        commissions: OPTIONS.commissions,
      }),
    );
    getDepartmentOptions = vi.fn(() => of([]));
    getAdministrativeUnitOptions = vi.fn(() =>
      of([{ label: 'Direcció General', value: 'UA01' }]),
    );
    TestBed.configureTestingModule({
      providers: [
        {
          provide: ApplicationOptionsService,
          useValue: {
            getStaticOptions,
            getDepartmentOptions,
            getAdministrativeUnitOptions,
          },
        },
      ],
    });
  });

  it('does not preload the interaction-driven DIR3 catalog', async () => {
    await resolveOptions();
    expect(getDepartmentOptions).not.toHaveBeenCalled();
    expect(getAdministrativeUnitOptions).not.toHaveBeenCalled();
  });

  it('should resolve application form options', async () => {
    await expect(resolveOptions()).resolves.toEqual({
      options: OPTIONS,
      loadFailed: false,

    });
    expect(getStaticOptions).toHaveBeenCalledOnce();
    expect(getDepartmentOptions).not.toHaveBeenCalled();
  });

  it('should complete with empty options when orchestration fails', async () => {
    getStaticOptions.mockReturnValueOnce(throwError(() => new Error('Options unavailable')));

    await expect(resolveOptions()).resolves.toEqual({
      options: {
        categories: [],
        informationSystems: [],
        scopes: [],
        commissions: [],

      },
      loadFailed: true,

    });
  });

  it('should be registered on create and general detail routes', () => {
    const createRoute = APPLICATIONS_ROUTES.find(({ path }) => path === 'new');
    const detailRoute = APPLICATIONS_ROUTES.find(({ path }) => path === ':id');
    const generalRoute = detailRoute?.children?.find(({ path }) => path === 'general');

    expect(createRoute?.resolve?.[APPLICATION_OPTIONS_RESOLVE_KEY]).toBe(
      applicationOptionsResolver,
    );
    expect(generalRoute?.resolve?.[APPLICATION_OPTIONS_RESOLVE_KEY]).toBe(
      applicationOptionsResolver,
    );
  });

  function resolveOptions(): Promise<ApplicationOptionsResolvedData> {
    const result = TestBed.runInInjectionContext(() =>
      applicationOptionsResolver({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot),
    );

    return firstValueFrom(result as Observable<ApplicationOptionsResolvedData>);
  }
});
