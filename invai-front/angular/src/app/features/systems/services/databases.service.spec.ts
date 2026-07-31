import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { SpringPage } from '@models/page.model';

import {
  DatabaseInput,
  DatabaseRecord,
  InfrastructureServer,
} from '../systems.model';
import { DatabasesService } from './databases.service';

const URL = '/invaiapi/interna/database';
const SERVER: InfrastructureServer = {
  id: 4,
  name: 'db01.caib.es',
  environment: {
    id: 1,
    code: 'PRO',
    name: 'Producció',
    nameEs: 'Producción',
    deletedAt: null,
  },
  serverType: {
    id: 202,
    code: 'DATABASE',
    name: 'Base de dades',
    nameEs: 'Base de datos',
  },
  deletedAt: null,
};
const DATABASE: DatabaseRecord = {
  id: 9,
  server: SERVER,
  service: 'INVAI',
  port: 5432,
  databaseType: { id: 2, name: 'PostgreSQL', defaultPort: 5432, deletedAt: null },
  description: null,
  deletedAt: null,
};
const INPUT: DatabaseInput = {
  serverId: SERVER.id,
  service: DATABASE.service,
  port: DATABASE.port,
  databaseTypeId: DATABASE.databaseType.id,
  description: null,
};

describe('DatabasesService', () => {
  let service: DatabasesService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(DatabasesService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('serializes database criteria and bypasses cache for quick search', () => {
    const params = {
      page: 0,
      serverId: 4,
      service: ' INVAI ',
      databaseTypeId: 2,
      statusId: 1,
      search: 'prod',
    };
    service.getAll(params).subscribe();
    service.getAll(params).subscribe();
    const requests = http.match((candidate) => candidate.url === URL);
    expect(requests).toHaveLength(2);
    expect(requests[0].request.params.get('serverId')).toBe('4');
    expect(requests[0].request.params.get('service')).toBe('INVAI');
    expect(requests[0].request.params.get('databaseTypeId')).toBe('2');
    requests.forEach((request) => request.flush(page([DATABASE])));
  });

  it('shares cached reads and invalidates after create and reactivate', () => {
    service.getAll({ page: 0 }).subscribe();
    service.getAll({ page: 0 }).subscribe();
    http.expectOne((candidate) => candidate.url === URL).flush(page([DATABASE]));

    service.create(INPUT).subscribe();
    const create = http.expectOne(URL);
    expect(create.request.body).toEqual(INPUT);
    create.flush(DATABASE);
    service.getAll({ page: 0 }).subscribe();
    http.expectOne((candidate) => candidate.url === URL).flush(page([DATABASE]));

    service.reactivate(9).subscribe();
    http.expectOne(`${URL}/reactivate/9`).flush(DATABASE);
  });

  it('retries a cached page after failure and supports detail/update/delete', () => {
    service.getAll({ page: 2 }).subscribe({ error: vi.fn() });
    http.expectOne((candidate) => candidate.url === URL)
      .flush('fail', { status: 500, statusText: 'Error' });
    service.getAll({ page: 2 }).subscribe();
    http.expectOne((candidate) => candidate.url === URL).flush(page([]));

    service.getById(9).subscribe();
    http.expectOne(`${URL}/9`).flush(DATABASE);
    service.update(9, INPUT).subscribe();
    http.expectOne(`${URL}/9`).flush(DATABASE);
    service.delete(9).subscribe();
    http.expectOne(`${URL}/9`).flush(null);
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
