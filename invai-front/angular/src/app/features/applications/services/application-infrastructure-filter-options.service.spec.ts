import { LOCALE_ID } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { EnvironmentsService } from '@features/environments/services/environments.service';
import { Environment } from '@features/environments/environments.model';
import { DatabasesService } from '@features/systems/services/databases.service';
import { SystemsService } from '@features/systems/services/systems.service';
import {
  DatabaseRecord,
  InfrastructureServer,
  InfrastructureSystem,
} from '@features/systems/systems.model';
import { SpringPage } from '@models/page.model';
import { firstValueFrom, of } from 'rxjs';

import { ApplicationInfrastructureFilterOptionsService } from './application-infrastructure-filter-options.service';

describe('ApplicationInfrastructureFilterOptionsService', () => {
  let service: ApplicationInfrastructureFilterOptionsService;
  let getSystems: ReturnType<typeof vi.fn>;
  let getDatabases: ReturnType<typeof vi.fn>;
  let getEnvironments: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    getSystems = vi.fn(({ page: pageNumber }: { page: number }) =>
      of(
        pageNumber === 0
          ? page([system(2, 'zeta')], 0, false)
          : page([system(1, 'Alpha')], 1, true),
      ),
    );
    getDatabases = vi.fn(() =>
      of(
        page([
          database(8, 'INVAI', 'db02.caib.es'),
          database(7, 'INVAI', 'db01.caib.es'),
          database(9, 'Auditoria', 'db03.caib.es'),
        ]),
      ),
    );
    getEnvironments = vi.fn(() =>
      of(
        page<Environment>([
          { id: 3, code: 'PRO', name: 'Producció', nameEs: 'Producción', deletedAt: null },
        ]),
      ),
    );

    TestBed.configureTestingModule({
      providers: [
        { provide: LOCALE_ID, useValue: 'es' },
        { provide: SystemsService, useValue: { getAll: getSystems } },
        { provide: DatabasesService, useValue: { getAll: getDatabases } },
        { provide: EnvironmentsService, useValue: { getAll: getEnvironments } },
      ],
    });
    service = TestBed.inject(ApplicationInfrastructureFilterOptionsService);
  });

  it('loads every server catalog page and sorts the resulting options', async () => {
    await expect(firstValueFrom(service.getServerOptions())).resolves.toEqual([
      { label: 'Alpha', value: 1 },
      { label: 'zeta', value: 2 },
    ]);
    expect(getSystems).toHaveBeenNthCalledWith(1, {
      page: 0,
      size: 100,
      sort: 'server.name,asc',
    });
    expect(getSystems).toHaveBeenNthCalledWith(2, {
      page: 1,
      size: 100,
      sort: 'server.name,asc',
    });
  });

  it('localizes environments and disambiguates duplicate database names', async () => {
    await expect(firstValueFrom(service.getEnvironmentOptions())).resolves.toEqual([
      { label: 'Producción', value: 3 },
    ]);
    await expect(firstValueFrom(service.getDatabaseOptions())).resolves.toEqual([
      { label: 'Auditoria', value: 9 },
      { label: 'INVAI — db01.caib.es', value: 7 },
      { label: 'INVAI — db02.caib.es', value: 8 },
    ]);
  });
});

function system(id: number, name: string): InfrastructureSystem {
  return {
    id,
    server: server(id, name),
    instance: '',
    port: 0,
    version: '',
    description: null,
    deletedAt: null,
  };
}

function database(id: number, serverName: string, systemName: string): DatabaseRecord {
  return {
    id,
    server: server(id, systemName),
    service: serverName,
    port: 0,
    databaseType: {
      id: 1,
      name: 'Oracle',
      defaultPort: 1521,
      deletedAt: null,
    },
    description: null,
    deletedAt: null,
  };
}

function server(id: number, name: string): InfrastructureServer {
  return {
    id,
    name,
    environment: {
      id: 3,
      code: 'PRO',
      name: 'Producció',
      nameEs: 'Producción',
      deletedAt: null,
    },
    serverType: {
      id: 101,
      code: 'APPLICATION',
      name: 'Aplicació',
      nameEs: 'Aplicación',
    },
    deletedAt: null,
  };
}

function page<TItem>(content: TItem[], number = 0, last = true): SpringPage<TItem> {
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
      sort: { empty: true, sorted: false, unsorted: true },
      unpaged: false,
    },
    size: 100,
    sort: { empty: true, sorted: false, unsorted: true },
    totalElements: content.length,
    totalPages: last ? number + 1 : number + 2,
  };
}
