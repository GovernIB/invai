import { LOCALE_ID } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { SpringPage } from '@models/page.model';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';
import { firstValueFrom, of } from 'rxjs';

import { Role } from '../roles.model';
import { RoleCatalogService } from './role-catalog.service';
import { RolesService } from './roles.service';

describe('RoleCatalogService', () => {
  let service: RoleCatalogService;
  let getAll: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    getAll = vi.fn(({ page: pageNumber }: { page: number }) =>
      of(
        pageNumber === 0
          ? page(
              [
                role(2, 'Development', 'Desarrollo'),
                role(1, 'Analysis', 'Análisis'),
              ],
              0,
              false,
            )
          : page([role(3, 'Support', 'Soporte')], 1, true),
      ),
    );
    TestBed.configureTestingModule({
      providers: [
        { provide: LOCALE_ID, useValue: 'es' },
        { provide: RolesService, useValue: { getAll } },
      ],
    });
    service = TestBed.inject(RoleCatalogService);
  });

  it('loads every active page and returns localized sorted options', async () => {
    await expect(firstValueFrom(service.getActiveOptions())).resolves.toEqual([
      { id: 1, label: 'Análisis' },
      { id: 2, label: 'Desarrollo' },
      { id: 3, label: 'Soporte' },
    ]);
    expect(getAll).toHaveBeenNthCalledWith(1, {
      page: 0,
      size: 100,
      sort: 'name,asc',
      statusId: SoftDeleteStatus.ACTIVE,
    });
    expect(getAll).toHaveBeenNthCalledWith(2, {
      page: 1,
      size: 100,
      sort: 'name,asc',
      statusId: SoftDeleteStatus.ACTIVE,
    });
  });
});

function role(id: number, name: string, nameEs: string): Role {
  return { id, name, nameEs, deletedAt: null };
}

function page<TItem>(
  content: TItem[],
  number: number,
  last: boolean,
): SpringPage<TItem> {
  return {
    content,
    empty: content.length === 0,
    first: number === 0,
    last,
    number,
    numberOfElements: content.length,
    pageable: {
      offset: number * 100,
      pageNumber: number,
      pageSize: 100,
      paged: true,
      sort: { empty: false, sorted: true, unsorted: false },
      unpaged: false,
    },
    size: 100,
    sort: { empty: false, sorted: true, unsorted: false },
    totalElements: content.length,
    totalPages: last ? number + 1 : number + 2,
  };
}
