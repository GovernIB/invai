import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { LOCALE_ID } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { SpringPage } from '@models/page.model';

import {
  ApplicationDatabaseRelationOutput,
  ApplicationInfrastructureStatus,
} from '../applications.model';
import { ApplicationDatabasesService } from './application-databases.service';

const DATABASES_URL = '/invaiapi/interna/application/database';
const ACTIVE_RELATION = relation(20, null);

describe('ApplicationDatabasesService', () => {
  let service: ApplicationDatabasesService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: LOCALE_ID, useValue: 'es' },
      ],
    });

    service = TestBed.inject(ApplicationDatabasesService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTesting.verify());

  it('loads and maps an application database page with all criteria', () => {
    const result = vi.fn();

    service
      .getPage({
        informationSystemDbId: 70,
        statusId: ApplicationInfrastructureStatus.ACTIVE,
        page: 1,
        size: 20,
        sort: 'database.serverName,desc',
        databaseId: 8,
        environmentId: 3,
      })
      .subscribe(result);

    const request = httpTesting.expectOne(
      (req) =>
        req.method === 'GET' &&
        req.url === `${DATABASES_URL}/70` &&
        !req.params.has('applicationId') &&
        req.params.get('statusId') === '1' &&
        req.params.get('page') === '1' &&
        req.params.get('size') === '20' &&
        req.params.get('databaseId') === '8' &&
        !req.params.has('environmentId') &&
        req.params.get('sort') === 'database.serverName,desc',
    );
    request.flush(page([ACTIVE_RELATION, relation(21, '2026-07-22T10:00:00')]));

    expect(result).toHaveBeenCalledWith(
      expect.objectContaining({
        content: [
          expect.objectContaining({
            id: 20,
            informationSystemDbId: 70,
            databaseId: 8,
            deletedAt: null,
            environment: 'Producción',
            server: 'db01.caib.es',
            service: 'invai_svc',
            database: 'invai_svc',
            port: 5432,
            type: 'Relacional',
            status: 'Actiu',
            observations: 'Base de dades principal',
          }),
          expect.objectContaining({ id: 21, status: 'Inactiu' }),
        ],
      }),
    );
  });

  it('shares and reuses a cached page', () => {
    const params = activeParams(70);
    const first = vi.fn();
    const second = vi.fn();
    const cached = vi.fn();

    service.getPage(params).subscribe(first);
    service.getPage(params).subscribe(second);
    httpTesting
      .expectOne((req) => req.url === `${DATABASES_URL}/70`)
      .flush(page([ACTIVE_RELATION]));
    service.getPage(params).subscribe(cached);
    httpTesting.expectNone((req) => req.url === `${DATABASES_URL}/70`);

    expect(first).toHaveBeenCalledOnce();
    expect(second).toHaveBeenCalledOnce();
    expect(cached).toHaveBeenCalledOnce();
  });

  it('separates cached pages by application and pagination criteria', () => {
    service.getPage(activeParams(70)).subscribe();
    httpTesting.expectOne((req) => req.url === `${DATABASES_URL}/70`).flush(page([]));

    service.getPage({ ...activeParams(71), page: 1 }).subscribe();
    httpTesting
      .expectOne(
        (req) => req.url === `${DATABASES_URL}/71` && req.params.get('page') === '1',
      )
      .flush(page([]));
  });

  it('separates cached pages for active, inactive and all statuses', () => {
    service.getPage(activeParams(70)).subscribe();
    httpTesting
      .expectOne(
        (req) =>
          req.params.get('statusId') === String(ApplicationInfrastructureStatus.ACTIVE),
      )
      .flush(page([]));

    service
      .getPage({
        ...activeParams(70),
        statusId: ApplicationInfrastructureStatus.INACTIVE,
      })
      .subscribe();
    httpTesting.expectOne((req) => req.params.get('statusId') === '2').flush(page([]));

    service.getPage({ informationSystemDbId: 70, page: 0, size: 10 }).subscribe();
    httpTesting
      .expectOne((req) => !req.params.has('statusId'))
      .flush(page([]));
  });

  it('separates cached pages by database filters and ignores unsupported environment criteria', () => {
    service.getPage({ ...activeParams(70), databaseId: 8, environmentId: 3 }).subscribe();
    httpTesting
      .expectOne(
        (req) => req.params.get('databaseId') === '8' && !req.params.has('environmentId'),
      )
      .flush(page([]));

    service.getPage({ ...activeParams(70), databaseId: 9, environmentId: 3 }).subscribe();
    httpTesting.expectOne((req) => req.params.get('databaseId') === '9').flush(page([]));
  });

  it('retries after a failed cached request', () => {
    const params = activeParams(70);
    const error = vi.fn();

    service.getPage(params).subscribe({ error });
    httpTesting
      .expectOne((req) => req.url === `${DATABASES_URL}/70`)
      .flush('Failed', { status: 500, statusText: 'Error' });

    service.getPage(params).subscribe();
    httpTesting
      .expectOne((req) => req.url === `${DATABASES_URL}/70`)
      .flush(page([ACTIVE_RELATION]));

    expect(error).toHaveBeenCalledOnce();
  });
});

function activeParams(informationSystemDbId: number) {
  return {
    informationSystemDbId,
    statusId: ApplicationInfrastructureStatus.ACTIVE,
    page: 0,
    size: 10,
  } as const;
}

function relation(id: number, deletedAt: string | null): ApplicationDatabaseRelationOutput {
  return {
    id,
    informationSystemDb: {
      id: 70,
    } as ApplicationDatabaseRelationOutput['informationSystemDb'],
    database: {
      id: 8,
      server: {
        id: 5,
        name: 'db01.caib.es',
        environment: {
          id: 3,
          code: 'PRO',
          name: 'Producció',
          nameEs: 'Producción',
          deletedAt: null,
        },
        serverType: {
          id: 2,
          code: 'DATABASE',
          name: 'Bases de dades',
          nameEs: 'Bases de datos',
        },
        deletedAt: null,
      },
      service: 'invai_svc',
      port: 5432,
      databaseType: {
        id: 1,
        name: 'Relacional',
        defaultPort: 5432,
        deletedAt: null,
      },
      description: 'Base de dades principal',
      deletedAt: null,
    },
    deletedAt,
  };
}

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
