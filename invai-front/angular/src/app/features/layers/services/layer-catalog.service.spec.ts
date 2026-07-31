import { TestBed } from '@angular/core/testing';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';
import { of } from 'rxjs';

import { LayersService } from './layers.service';
import { LayerCatalogService } from './layer-catalog.service';

describe('LayerCatalogService', () => {
  it('loads every active page and returns sorted options with identifier fallbacks', () => {
    const getAll = vi
      .fn()
      .mockReturnValueOnce(
        of({
          content: [{ id: 3, name: null, deletedAt: null }],
          last: false,
          number: 0,
        }),
      )
      .mockReturnValueOnce(
        of({
          content: [{ id: 2, name: 'Backend', deletedAt: null }],
          last: true,
          number: 1,
        }),
      );
    TestBed.configureTestingModule({
      providers: [{ provide: LayersService, useValue: { getAll } }],
    });

    let result: unknown;
    TestBed.inject(LayerCatalogService)
      .getActiveOptions()
      .subscribe((options) => (result = options));

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
    expect(result).toEqual([
      { id: 3, label: '#3' },
      { id: 2, label: 'Backend' },
    ]);
  });
});
