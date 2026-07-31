import { LOCALE_ID } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { EnvironmentStatus } from '@features/environments/environments.model';
import { SpringPage } from '@models/page.model';
import { firstValueFrom, of } from 'rxjs';

import { Environment } from '../environments.model';
import { EnvironmentCatalogService } from './environment-catalog.service';
import { EnvironmentsService } from './environments.service';

describe('EnvironmentCatalogService', () => {
  let service: EnvironmentCatalogService;
  let getAll: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    getAll = vi.fn(({ page: pageNumber }: { page: number }) =>
      of(
        pageNumber === 0
          ? page(
              [
                environment(2, 'PRO', 'Producció', 'Producción'),
                environment(1, 'DEV', 'Desenvolupament', 'Desarrollo'),
              ],
              0,
              false,
            )
          : page([environment(3, 'PRE', 'Preproducció', 'Preproducción')], 1, true),
      ),
    );

    TestBed.configureTestingModule({
      providers: [
        { provide: LOCALE_ID, useValue: 'es' },
        { provide: EnvironmentsService, useValue: { getAll } },
      ],
    });
    service = TestBed.inject(EnvironmentCatalogService);
  });

  it('loads every active page and returns localized sorted options', async () => {
    await expect(firstValueFrom(service.getActiveOptions())).resolves.toEqual([
      { id: 1, code: 'DEV', label: 'Desarrollo' },
      { id: 3, code: 'PRE', label: 'Preproducción' },
      { id: 2, code: 'PRO', label: 'Producción' },
    ]);
    expect(getAll).toHaveBeenNthCalledWith(1, {
      page: 0,
      size: 100,
      sort: 'name,asc',
      statusId: EnvironmentStatus.ACTIVE,
    });
    expect(getAll).toHaveBeenNthCalledWith(2, {
      page: 1,
      size: 100,
      sort: 'name,asc',
      statusId: EnvironmentStatus.ACTIVE,
    });
  });
});

function environment(
  id: number,
  code: string,
  name: string,
  nameEs: string,
): Environment {
  return { id, code, name, nameEs, deletedAt: null };
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
