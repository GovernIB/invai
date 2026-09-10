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
  applicationOptionsResolver,
  resolveApplicationOptions,
} from './application-options.resolver';

const OPTIONS: ApplicationSelectOptions = {
  categories: [{ label: 'DRASSANA', value: 1 }],
  informationSystems: [],
  scopes: [],
  commissions: [],
  departments: [{ label: 'Conselleria', value: 'GVA01' }],
  administrativeUnits: [],
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
    getDepartmentOptions = vi.fn(() => of(OPTIONS.departments));
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

  it('preloads the units for the department resolved by the detail route', async () => {
    const service = TestBed.inject(ApplicationOptionsService);
    const result = await firstValueFrom(resolveApplicationOptions(service, 'GVA01'));

    expect(getAdministrativeUnitOptions).toHaveBeenCalledWith('GVA01');
    expect(result.options.administrativeUnits).toEqual([
      { label: 'Direcció General', value: 'UA01' },
    ]);
    expect(result.administrativeUnitsLoadFailed).toBe(false);
  });

  it('should resolve application form options', async () => {
    await expect(resolveOptions()).resolves.toEqual({
      options: OPTIONS,
      loadFailed: false,
      departmentsLoadFailed: false,
      administrativeUnitsLoadFailed: false,
    });
    expect(getStaticOptions).toHaveBeenCalledOnce();
    expect(getDepartmentOptions).toHaveBeenCalledOnce();
  });

  it('should complete with empty options when orchestration fails', async () => {
    getStaticOptions.mockReturnValueOnce(throwError(() => new Error('Options unavailable')));

    await expect(resolveOptions()).resolves.toEqual({
      options: {
        categories: [],
        informationSystems: [],
        scopes: [],
        commissions: [],
        departments: OPTIONS.departments,
        administrativeUnits: [],
      },
      loadFailed: true,
      departmentsLoadFailed: false,
      administrativeUnitsLoadFailed: false,
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
