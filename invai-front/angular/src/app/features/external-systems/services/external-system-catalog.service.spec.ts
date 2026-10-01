import { TestBed } from '@angular/core/testing';
import { SpringPage } from '@models/page.model';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';
import { firstValueFrom, of } from 'rxjs';

import { ExternalSystem } from '../external-systems.model';
import { ExternalSystemCatalogService } from './external-system-catalog.service';
import { ExternalSystemsService } from './external-systems.service';

describe('ExternalSystemCatalogService', () => {
  let service: ExternalSystemCatalogService;
  let getAll: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    getAll = vi.fn(({ page: pageNumber }: { page: number }) =>
      of(
        pageNumber === 0
          ? page([system(2, 'Soffid'), system(1, 'DIR3CAIB')], 0, false)
          : page([system(3, ' '), system(4, 'Portal Salut')], 1, true),
      ),
    );
    TestBed.configureTestingModule({
      providers: [{ provide: ExternalSystemsService, useValue: { getAll } }],
    });
    service = TestBed.inject(ExternalSystemCatalogService);
  });

  it('loads every active page into sorted options', async () => {
    await expect(firstValueFrom(service.getActiveOptions())).resolves.toEqual([
      { id: 3, label: '#3' },
      { id: 1, label: 'DIR3CAIB' },
      { id: 4, label: 'Portal Salut' },
      { id: 2, label: 'Soffid' },
    ]);
    expect(getAll).toHaveBeenNthCalledWith(1, {
      page: 0,
      size: 100,
      sort: 'name,asc',
      statusId: SoftDeleteStatus.ACTIVE,
    });
    expect(getAll).toHaveBeenNthCalledWith(2, expect.objectContaining({ page: 1 }));
  });
});

function system(id: number, name: string): ExternalSystem {
  return { id, name, company: null, deletedAt: null };
}

function page<TItem>(content: TItem[], number: number, last: boolean): SpringPage<TItem> {
  return { content, number, last, totalElements: content.length } as SpringPage<TItem>;
}
