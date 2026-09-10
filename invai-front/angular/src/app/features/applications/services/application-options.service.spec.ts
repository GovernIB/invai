import { LOCALE_ID } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { AdministrativeUnitsService } from '@features/administrative-units/services/administrative-units.service';
import { CategoriesService } from '@features/categories/services/categories.service';
import { CommissionType } from '@features/commissions/commissions.model';
import { CommissionsService } from '@features/commissions/services/commissions.service';
import { FieldsService } from '@features/fields/services/fields.service';
import { SystemTypesService } from '@features/system-types/services/system-types.service';
import { SpringPage } from '@models/page.model';
import { firstValueFrom, of, throwError } from 'rxjs';

import { ApplicationOptionsService } from './application-options.service';

describe('ApplicationOptionsService', () => {
  let categoriesService: { getAll: ReturnType<typeof vi.fn> };
  let administrativeUnitsService: {
    getDepartments: ReturnType<typeof vi.fn>;
    getAdmUnitsByDepartment: ReturnType<typeof vi.fn>;
  };

  beforeEach(() => {
    categoriesService = {
      getAll: vi.fn(() => of(page([{ id: 1, name: 'DRASSANA', nameEs: 'ASTILLERO' }]))),
    };
    administrativeUnitsService = {
      getDepartments: vi.fn(() =>
        of(page([unit('GVA02', 'Educació'), unit('GVA01', 'Agricultura')], 1)),
      ),
      getAdmUnitsByDepartment: vi.fn(() =>
        of(page([unit('UA01', 'Direcció General', 'GVA01')], 1)),
      ),
    };

    TestBed.configureTestingModule({
      providers: [
        ApplicationOptionsService,
        { provide: LOCALE_ID, useValue: 'ca' },
        { provide: CategoriesService, useValue: categoriesService },
        {
          provide: SystemTypesService,
          useValue: {
            getAll: vi.fn(() => of(page([{ id: 2, name: 'Instrumental', nameEs: 'Instrumental ES' }]))),
          },
        },
        {
          provide: FieldsService,
          useValue: {
            getAll: vi.fn(() => of(page([{ id: 3, name: 'Departamental', nameEs: 'Departamental ES' }]))),
          },
        },
        {
          provide: CommissionsService,
          useValue: {
            getAll: vi.fn(() =>
              of(
                page([
                  {
                    id: 4,
                    name: 'Comissió tècnica',
                    nameEs: 'Comisión técnica',
                    expedientNumber: 'EXP-4',
                    approvalDate: '2026-07-14',
                    commissionType: CommissionType.TECNICA,
                  },
                ]),
              ),
            ),
          },
        },
        { provide: AdministrativeUnitsService, useValue: administrativeUnitsService },
      ],
    });
  });

  it('maps the static maintenance catalogs', async () => {
    const result = await firstValueFrom(
      TestBed.inject(ApplicationOptionsService).getStaticOptions(),
    );

    expect(result.categories).toEqual([{ label: 'DRASSANA', value: 1 }]);
    expect(result.informationSystems).toEqual([{ label: 'Instrumental', value: 2 }]);
    expect(result.scopes).toEqual([{ label: 'Departamental', value: 3 }]);
    expect(result.commissions[0]).toEqual(
      expect.objectContaining({ label: 'Comissió tècnica', value: 4 }),
    );
  });

  it('isolates failures in a static catalog', async () => {
    categoriesService.getAll.mockReturnValueOnce(throwError(() => new Error('Unavailable')));

    const result = await firstValueFrom(
      TestBed.inject(ApplicationOptionsService).getStaticOptions(),
    );

    expect(result.categories).toEqual([]);
    expect(result.informationSystems).toHaveLength(1);
  });

  it('maps and sorts departments by label using their DIR3 code as value', async () => {
    const result = await firstValueFrom(
      TestBed.inject(ApplicationOptionsService).getDepartmentOptions(),
    );

    expect(result).toEqual([
      { label: 'Agricultura', value: 'GVA01' },
      { label: 'Educació', value: 'GVA02' },
    ]);
    expect(administrativeUnitsService.getDepartments).toHaveBeenCalledWith({ page: 0, size: 100 });
  });

  it('loads every page of units for the selected department', async () => {
    administrativeUnitsService.getAdmUnitsByDepartment
      .mockReturnValueOnce(of(page([unit('UA01', 'Unitat A', 'GVA01')], 2)))
      .mockReturnValueOnce(of(page([unit('UA02', 'Unitat B', 'GVA01')], 2)));

    const result = await firstValueFrom(
      TestBed.inject(ApplicationOptionsService).getAdministrativeUnitOptions('GVA01'),
    );

    expect(result).toEqual([
      { label: 'Unitat A', value: 'UA01' },
      { label: 'Unitat B', value: 'UA02' },
    ]);
    expect(administrativeUnitsService.getAdmUnitsByDepartment).toHaveBeenNthCalledWith(
      2,
      'GVA01',
      { page: 1, size: 100 },
    );
  });
});

function unit(code: string, name: string, parentCode: string | null = null) {
  return { code, name, parentCode, level: parentCode ? 2 : 1 };
}

function page<TItem>(content: TItem[], totalPages = 1): SpringPage<TItem> {
  return {
    content,
    empty: content.length === 0,
    first: true,
    last: totalPages === 1,
    number: 0,
    numberOfElements: content.length,
    pageable: {
      offset: 0,
      pageNumber: 0,
      pageSize: 100,
      paged: true,
      sort: { empty: true, sorted: false, unsorted: true },
      unpaged: false,
    },
    size: 100,
    sort: { empty: true, sorted: false, unsorted: true },
    totalElements: content.length * totalPages,
    totalPages,
  };
}
