import { TestBed } from '@angular/core/testing';
import { Layer } from '@features/layers/layers.model';
import { SpringPage } from '@models/page.model';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';
import { firstValueFrom, of } from 'rxjs';

import { Technology } from '../technologies.model';
import { TechnologiesService } from './technologies.service';
import { TechnologyCatalogService } from './technology-catalog.service';

describe('TechnologyCatalogService', () => {
  let service: TechnologyCatalogService;
  let getAll: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    getAll = vi.fn(({ page: pageNumber }: { page: number }) =>
      of(
        pageNumber === 0
          ? page(
              [
                technology(2, 'Spring', 2, 'Backend'),
                technology(1, 'Angular', 1, 'Frontend'),
              ],
              0,
              false,
            )
          : page([technology(3, 'PostgreSQL', 3, 'Dades')], 1, true),
      ),
    );
    TestBed.configureTestingModule({
      providers: [{ provide: TechnologiesService, useValue: { getAll } }],
    });
    service = TestBed.inject(TechnologyCatalogService);
  });

  it('loads every active page and keeps the layer in each sorted option', async () => {
    await expect(firstValueFrom(service.getActiveOptions())).resolves.toEqual([
      { id: 1, label: 'Angular', layerId: 1, layerLabel: 'Frontend' },
      { id: 3, label: 'PostgreSQL', layerId: 3, layerLabel: 'Dades' },
      { id: 2, label: 'Spring', layerId: 2, layerLabel: 'Backend' },
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

function technology(
  id: number,
  name: string,
  layerId: number,
  layerName: string,
): Technology {
  const layer: Layer = {
    id: layerId,
    name: layerName,
    deletedAt: null,
  };
  return { id, name, layer, deletedAt: null };
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
