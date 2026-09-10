import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { SpringPage } from '@models/page.model';

import {
  InfrastructureServer,
  InfrastructureSystem,
  InfrastructureSystemInput,
} from '../systems.model';
import { SystemsService } from './systems.service';

const URL = '/invaiapi/interna/system';
const SERVER: InfrastructureServer = {
  id: 3,
  name: 'app01.caib.es',
  environment: {
    id: 1,
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
const SYSTEM: InfrastructureSystem = {
  id: 7,
  server: SERVER,
  instance: 'jboss',
  port: 8080,
  version: '7.1',
  description: null,
  deletedAt: null,
};
const INPUT: InfrastructureSystemInput = {
  name: SERVER.name,
  serverId: SERVER.id,
  instance: SYSTEM.instance,
  port: SYSTEM.port,
  version: SYSTEM.version,
  description: null,
};

describe('SystemsService', () => {
  let service: SystemsService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(SystemsService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('serializes supported System filters and keeps cache entries separated', () => {
    service
      .getAll({
        page: 1,
        size: 25,
        sort: 'instance,asc',
        serverId: 3,
        instance: ' jboss ',
        version: '7',
        statusId: 1,
        unassignedToInformationSystemDbId: 70,
      })
      .subscribe();
    const request = http.expectOne((candidate) => candidate.url === URL);
    expect(request.request.params.get('page')).toBe('1');
    expect(request.request.params.get('serverId')).toBe('3');
    expect(request.request.params.get('instance')).toBe('jboss');
    expect(request.request.params.get('version')).toBe('7');
    expect(request.request.params.get('statusId')).toBe('1');
    expect(request.request.params.get('unassignedToInformationSystemDbId')).toBe('70');
    request.flush(page([SYSTEM]));

    service.getAll({ page: 1, size: 25, serverId: 4 }).subscribe();
    http.expectOne((candidate) => candidate.params.get('serverId') === '4').flush(page([]));
  });

  it('separates assignable catalog cache entries by grouping id and omits an absent id', () => {
    const params = { page: 0, size: 10, statusId: 1 as const };

    service.getAll({ ...params, unassignedToInformationSystemDbId: 70 }).subscribe();
    http
      .expectOne((candidate) => candidate.params.get('unassignedToInformationSystemDbId') === '70')
      .flush(page([SYSTEM]));

    service.getAll({ ...params, unassignedToInformationSystemDbId: 71 }).subscribe();
    http
      .expectOne((candidate) => candidate.params.get('unassignedToInformationSystemDbId') === '71')
      .flush(page([]));

    service.getAll(params).subscribe();
    const request = http.expectOne(
      (candidate) => !candidate.params.has('unassignedToInformationSystemDbId'),
    );
    request.flush(page([SYSTEM]));
  });

  it('shares, reuses and retries cached reads', () => {
    const params = { page: 0, size: 10, serverId: 3 };
    service.getAll(params).subscribe();
    service.getAll(params).subscribe();
    http.expectOne((candidate) => candidate.url === URL).flush(page([SYSTEM]));
    service.getAll(params).subscribe();
    http.expectNone((candidate) => candidate.url === URL);

    service.getAll({ page: 2 }).subscribe({ error: vi.fn() });
    http
      .expectOne((candidate) => candidate.url === URL)
      .flush('fail', { status: 500, statusText: 'Error' });
    service.getAll({ page: 2 }).subscribe();
    http.expectOne((candidate) => candidate.url === URL).flush(page([]));
  });

  it('sends trimmed quick search with catalog restrictions and does not cache textual searches', () => {
    const params = {
      search: ' app ',
      serverId: 3,
      statusId: 1 as const,
      unassignedToInformationSystemDbId: 70,
    };
    for (let attempt = 0; attempt < 2; attempt++) {
      service.getAll(params).subscribe();
      const request = http.expectOne((candidate) => candidate.url === URL);
      expect(request.request.params.get('search')).toBe('app');
      expect(request.request.params.get('serverId')).toBe('3');
      expect(request.request.params.get('statusId')).toBe('1');
      expect(request.request.params.get('unassignedToInformationSystemDbId')).toBe('70');
      request.flush(page([SYSTEM]));
    }
    service.getAll({ search: '   ' }).subscribe();
    const empty = http.expectOne((candidate) => candidate.url === URL);
    expect(empty.request.params.has('search')).toBe(false);
    empty.flush(page([]));
    service.getAll({ search: '' }).subscribe();
    http.expectNone((candidate) => candidate.url === URL);
  });

  it('uses uncached detail and exact mutation contracts', () => {
    service.getById(7).subscribe();
    http.expectOne(`${URL}/7`).flush(SYSTEM);

    service.create(INPUT).subscribe();
    const create = http.expectOne(URL);
    expect(create.request.method).toBe('POST');
    expect(create.request.body).toEqual(INPUT);
    create.flush(SYSTEM);

    service.update(7, INPUT).subscribe();
    http.expectOne(`${URL}/7`).flush(SYSTEM);
    service.delete(7).subscribe();
    http.expectOne(`${URL}/7`).flush(null);
    service.reactivate(7).subscribe();
    const reactivate = http.expectOne(`${URL}/reactivate/7`);
    expect(reactivate.request.method).toBe('PUT');
    expect(reactivate.request.body).toBeNull();
    reactivate.flush(SYSTEM);
  });

  it('invalidates cached reads only after a successful mutation', () => {
    service.getAll().subscribe();
    http.expectOne(URL).flush(page([SYSTEM]));

    service.create(INPUT).subscribe({ error: vi.fn() });
    http.expectOne(URL).flush('fail', { status: 500, statusText: 'Error' });
    service.getAll().subscribe();
    http.expectNone(URL);

    service.create(INPUT).subscribe();
    http.expectOne(URL).flush(SYSTEM);
    service.getAll().subscribe();
    http.expectOne(URL).flush(page([SYSTEM]));
  });
});

function page<T>(content: T[]): SpringPage<T> {
  return {
    content,
    empty: !content.length,
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
