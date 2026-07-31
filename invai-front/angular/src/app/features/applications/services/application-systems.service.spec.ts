import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { LOCALE_ID } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { SpringPage } from '@models/page.model';

import {
  ApplicationInfrastructureStatus,
  ApplicationSystemRelationOutput,
} from '../applications.model';
import { ApplicationSystemsService } from './application-systems.service';

const SYSTEMS_URL = '/invaiapi/interna/application/system';
const ACTIVE_RELATION = relation(10, null);

describe('ApplicationSystemsService', () => {
  let service: ApplicationSystemsService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: LOCALE_ID, useValue: 'es' },
      ],
    });

    service = TestBed.inject(ApplicationSystemsService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTesting.verify());

  it('loads and maps an application system page with all criteria', () => {
    const result = vi.fn();

    service
      .getPage({
        informationSystemDbId: 70,
        statusId: ApplicationInfrastructureStatus.ACTIVE,
        page: 2,
        size: 25,
        sort: ['system.name,asc', 'id,desc'],
        systemId: 5,
        environmentId: 3,
      })
      .subscribe(result);

    const request = httpTesting.expectOne(
      (req) =>
        req.method === 'GET' &&
        req.url === `${SYSTEMS_URL}/70` &&
        !req.params.has('applicationId') &&
        req.params.get('statusId') === '1' &&
        req.params.get('page') === '2' &&
        req.params.get('size') === '25' &&
        req.params.get('systemId') === '5' &&
        !req.params.has('environmentId') &&
        req.params.getAll('sort')?.join('|') === 'system.name,asc|id,desc',
    );
    request.flush(page([ACTIVE_RELATION, relation(11, '2026-07-22T10:00:00')]));

    expect(result).toHaveBeenCalledWith(
      expect.objectContaining({
        content: [
          expect.objectContaining({
            id: 10,
            informationSystemDbId: 70,
            systemId: 5,
            deletedAt: null,
            environment: 'Producción',
            server: 'app01.caib.es',
            instance: 'jboss',
            port: 8080,
            version: '7.4',
            status: 'Actiu',
            observations: 'Servidor principal',
          }),
          expect.objectContaining({ id: 11, status: 'Inactiu' }),
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
      .expectOne((req) => req.url === `${SYSTEMS_URL}/70`)
      .flush(page([ACTIVE_RELATION]));
    service.getPage(params).subscribe(cached);
    httpTesting.expectNone((req) => req.url === `${SYSTEMS_URL}/70`);

    expect(first).toHaveBeenCalledOnce();
    expect(second).toHaveBeenCalledOnce();
    expect(cached).toHaveBeenCalledOnce();
  });

  it('separates cached pages for active, inactive and all statuses', () => {
    service.getPage(activeParams(70)).subscribe();
    httpTesting
      .expectOne(
        (req) =>
          req.url === `${SYSTEMS_URL}/70` &&
          req.params.get('statusId') === String(ApplicationInfrastructureStatus.ACTIVE),
      )
      .flush(page([]));

    service
      .getPage({ ...activeParams(70), statusId: ApplicationInfrastructureStatus.INACTIVE })
      .subscribe();
    httpTesting
      .expectOne(
        (req) => req.url === `${SYSTEMS_URL}/70` && req.params.get('statusId') === '2',
      )
      .flush(page([]));

    service.getPage({ informationSystemDbId: 70, page: 0, size: 10 }).subscribe();
    httpTesting
      .expectOne(
        (req) =>
          req.url === `${SYSTEMS_URL}/70` && !req.params.has('statusId'),
      )
      .flush(page([]));
  });

  it('separates cached pages by system filters and ignores unsupported environment criteria', () => {
    service.getPage({ ...activeParams(70), systemId: 5, environmentId: 3 }).subscribe();
    httpTesting
      .expectOne(
        (req) => req.params.get('systemId') === '5' && !req.params.has('environmentId'),
      )
      .flush(page([]));

    service.getPage({ ...activeParams(70), systemId: 6, environmentId: 3 }).subscribe();
    httpTesting.expectOne((req) => req.params.get('systemId') === '6').flush(page([]));
  });

  it('retries after a failed cached request', () => {
    const params = activeParams(70);
    const error = vi.fn();

    service.getPage(params).subscribe({ error });
    httpTesting
      .expectOne((req) => req.url === `${SYSTEMS_URL}/70`)
      .flush('Failed', { status: 500, statusText: 'Error' });

    service.getPage(params).subscribe();
    httpTesting
      .expectOne((req) => req.url === `${SYSTEMS_URL}/70`)
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

function relation(id: number, deletedAt: string | null): ApplicationSystemRelationOutput {
  return {
    id,
    informationSystemDb: {
      id: 70,
    } as ApplicationSystemRelationOutput['informationSystemDb'],
    system: {
      id: 5,
      server: {
        id: 4,
        name: 'app01.caib.es',
        environment: {
          id: 3,
          code: 'PRO',
          name: 'Producció',
          nameEs: 'Producción',
          deletedAt: null,
        },
        serverType: {
          id: 1,
          code: 'APPLICATION',
          name: 'Aplicacions',
          nameEs: 'Aplicaciones',
        },
        deletedAt: null,
      },
      instance: 'jboss',
      port: 8080,
      version: '7.4',
      description: 'Servidor principal',
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
