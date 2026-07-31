import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { SpringPage } from '@models/page.model';

import {
  InfrastructureServer,
  InfrastructureServerInput,
} from '../systems.model';
import { ServersService } from './servers.service';

const URL = '/invaiapi/interna/server';
const SERVER: InfrastructureServer = {
  id: 1,
  name: 'app01.caib.es',
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
const INPUT: InfrastructureServerInput = {
  name: SERVER.name,
  environmentId: SERVER.environment.id,
  serverTypeId: SERVER.serverType.id,
};

describe('ServersService', () => {
  let service: ServersService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ServersService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('always sends the server type code and keys cache by filters', () => {
    service
      .getAll({
        page: 0,
        serverTypeCode: 'APPLICATION',
        environmentId: 3,
        statusId: 1,
      })
      .subscribe();
    const request = http.expectOne((candidate) => candidate.url === URL);
    expect(request.request.params.get('serverTypeCode')).toBe('APPLICATION');
    expect(request.request.params.get('environmentId')).toBe('3');
    request.flush(page([SERVER]));

    service.getAll({ page: 0, serverTypeCode: 'DATABASE' }).subscribe();
    http.expectOne((candidate) => candidate.params.get('serverTypeCode') === 'DATABASE')
      .flush(page([]));
  });

  it('shares successful reads, retries failures and invalidates after mutations', () => {
    const params = { page: 0, serverTypeCode: 'APPLICATION' as const };
    service.getAll(params).subscribe();
    service.getAll(params).subscribe();
    http.expectOne((candidate) => candidate.url === URL).flush(page([SERVER]));
    service.getAll(params).subscribe();
    http.expectNone((candidate) => candidate.url === URL);

    service.create(INPUT).subscribe();
    const create = http.expectOne(URL);
    expect(create.request.body).toEqual(INPUT);
    create.flush(SERVER);
    service.getAll(params).subscribe();
    http.expectOne((candidate) => candidate.url === URL).flush(page([SERVER]));

    service.getAll({ page: 2, serverTypeCode: 'APPLICATION' }).subscribe({
      error: vi.fn(),
    });
    http.expectOne((candidate) => candidate.url === URL).flush('fail', {
      status: 500,
      statusText: 'Error',
    });
    service.getAll({ page: 2, serverTypeCode: 'APPLICATION' }).subscribe();
    http.expectOne((candidate) => candidate.url === URL).flush(page([]));
  });

  it('supports detail, update, delete and reactivate', () => {
    service.getById(1).subscribe();
    http.expectOne(`${URL}/1`).flush(SERVER);
    service.update(1, INPUT).subscribe();
    http.expectOne(`${URL}/1`).flush(SERVER);
    service.delete(1).subscribe();
    http.expectOne(`${URL}/1`).flush(null);
    service.reactivate(1).subscribe();
    const reactivate = http.expectOne(`${URL}/reactivate/1`);
    expect(reactivate.request.method).toBe('PUT');
    reactivate.flush(SERVER);
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
