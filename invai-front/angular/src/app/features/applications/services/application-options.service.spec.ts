import { LOCALE_ID } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { AdministrativeUnitsService } from '@features/administrative-units/services/administrative-units.service';
import { CategoriesService } from '@features/categories/services/categories.service';
import { CommissionsService } from '@features/commissions/services/commissions.service';
import { CommissionType } from '@features/commissions/commissions.model';
import { FieldsService } from '@features/fields/services/fields.service';
import { SystemTypesService } from '@features/system-types/services/system-types.service';
import { SpringPage } from '@models/page.model';
import { firstValueFrom, of, throwError } from 'rxjs';
import { ApplicationOptionsService } from './application-options.service';

describe('ApplicationOptionsService', () => {
  let categoriesService: { getAll: ReturnType<typeof vi.fn> };
  let commissionsService: { getAll: ReturnType<typeof vi.fn> };
  let fieldsService: { getAll: ReturnType<typeof vi.fn> };
  let administrativeUnitsService: { getAll: ReturnType<typeof vi.fn> };
  let systemTypesService: { getAll: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    categoriesService = {
      getAll: vi.fn(() => of(page([{ id: 1, name: 'DRASSANA', nameEs: 'ASTILLERO' }]))),
    };
    systemTypesService = {
      getAll: vi.fn(() =>
        of(page([{ id: 2, name: 'Instrumental', nameEs: 'Instrumental ES' }])),
      ),
    };
    fieldsService = {
      getAll: vi.fn(() =>
        of(page([{ id: 3, name: 'Departamental', nameEs: 'Departamental ES' }])),
      ),
    };
    commissionsService = {
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
    };
    administrativeUnitsService = {
      getAll: vi.fn(() =>
        of(
          page([
            {
              id: 5,
              code: 'DGEDOT',
              name: 'Direcció General',
              nameEs: 'Dirección General',
            },
          ]),
        ),
      ),
    };

    TestBed.configureTestingModule({
      providers: [
        ApplicationOptionsService,
        { provide: LOCALE_ID, useValue: 'ca' },
        { provide: CategoriesService, useValue: categoriesService },
        { provide: CommissionsService, useValue: commissionsService },
        { provide: FieldsService, useValue: fieldsService },
        { provide: AdministrativeUnitsService, useValue: administrativeUnitsService },
        { provide: SystemTypesService, useValue: systemTypesService },
      ],
    });
  });

  it('maps cached catalog pages to application selector options', async () => {
    const service = TestBed.inject(ApplicationOptionsService);

    const result = await firstValueFrom(service.getOptions());

    expect(result.categories).toEqual([{ label: 'DRASSANA', value: 1 }]);
    expect(result.informationSystems).toEqual([
      { label: 'Instrumental', value: 2 },
    ]);
    expect(result.scopes).toEqual([{ label: 'Departamental', value: 3 }]);
    expect(result.commissions).toEqual([
      {
        label: 'Comissió tècnica',
        value: 4,
        expedientNumber: 'EXP-4',
        approvalDate: '2026-07-14',
        commissionType: CommissionType.TECNICA,
      },
    ]);
    expect(result.administrativeUnits).toEqual([
      { label: 'Direcció General', value: 5 },
    ]);
  });

  it('uses Spanish names for every application catalog when the locale is Spanish', async () => {
    TestBed.overrideProvider(LOCALE_ID, { useValue: 'es' });
    const service = TestBed.inject(ApplicationOptionsService);

    const result = await firstValueFrom(service.getOptions());

    expect(result.categories[0]?.label).toBe('ASTILLERO');
    expect(result.informationSystems[0]?.label).toBe('Instrumental ES');
    expect(result.scopes[0]?.label).toBe('Departamental ES');
    expect(result.commissions[0]?.label).toBe('Comisión técnica');
    expect(result.administrativeUnits[0]?.label).toBe('Dirección General');
  });

  it('returns empty options for a catalog that fails to load', async () => {
    categoriesService.getAll.mockReturnValueOnce(
      throwError(() => new Error('Categories unavailable')),
    );
    const service = TestBed.inject(ApplicationOptionsService);

    const result = await firstValueFrom(service.getOptions());

    expect(result.categories).toEqual([]);
    expect(result.informationSystems).toEqual([
      { label: 'Instrumental', value: 2 },
    ]);
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
