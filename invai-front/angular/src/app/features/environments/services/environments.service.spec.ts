import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { SpringPage } from '@models/page.model';
import { Observable } from 'rxjs';

import {
  Environment,
  EnvironmentInput,
  EnvironmentStatus,
} from '../environments.model';
import { EnvironmentsService } from './environments.service';

const ENVIRONMENTS_URL = '/invaiapi/interna/environment';
const ENVIRONMENT: Environment = {
  id: 3,
  code: 'PRO',
  name: 'Producció',
  nameEs: 'Producción',
  deletedAt: null,
};
const ENVIRONMENT_INPUT: EnvironmentInput = {
  code: 'PRO',
  name: 'Producció',
  nameEs: 'Producción',
};

describe('EnvironmentsService', () => {
  let service: EnvironmentsService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(EnvironmentsService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('shares and reuses the active catalog page', () => {
    const params = {
      page: 0,
      size: 100,
      sort: 'name,asc',
      statusId: EnvironmentStatus.ACTIVE,
    };
    const first = vi.fn();
    const second = vi.fn();
    const cached = vi.fn();

    service.getAll(params).subscribe(first);
    service.getAll(params).subscribe(second);
    httpTesting
      .expectOne(
        (request) =>
          request.url === ENVIRONMENTS_URL &&
          request.params.get('statusId') === String(EnvironmentStatus.ACTIVE),
      )
      .flush(page([ENVIRONMENT]));

    service.getAll(params).subscribe(cached);
    httpTesting.expectNone(ENVIRONMENTS_URL);

    expect(first).toHaveBeenCalledOnce();
    expect(second).toHaveBeenCalledOnce();
    expect(cached).toHaveBeenCalledOnce();
  });

  it('separates cached environment pages for active, inactive and all statuses', () => {
    service.getAll({ page: 0, statusId: EnvironmentStatus.ACTIVE }).subscribe();
    httpTesting
      .expectOne(
        (request) =>
          request.url === ENVIRONMENTS_URL &&
          request.params.get('statusId') === String(EnvironmentStatus.ACTIVE),
      )
      .flush(page([ENVIRONMENT]));

    service.getAll({ page: 0, statusId: EnvironmentStatus.INACTIVE }).subscribe();
    httpTesting
      .expectOne(
        (request) =>
          request.url === ENVIRONMENTS_URL &&
          request.params.get('statusId') === String(EnvironmentStatus.INACTIVE),
      )
      .flush(page([{ ...ENVIRONMENT, deletedAt: '2026-07-24T10:00:00.000Z' }]));

    service.getAll({ page: 0 }).subscribe();
    httpTesting
      .expectOne(
        (request) =>
          request.url === ENVIRONMENTS_URL && !request.params.has('statusId'),
      )
      .flush(page([ENVIRONMENT]));
  });

  it('retries a failed cached catalog page', () => {
    service.getAll({ page: 0, statusId: EnvironmentStatus.ACTIVE }).subscribe({
      error: vi.fn(),
    });
    httpTesting
      .expectOne((request) => request.url === ENVIRONMENTS_URL)
      .flush('Failed', { status: 500, statusText: 'Error' });

    service.getAll({ page: 0, statusId: EnvironmentStatus.ACTIVE }).subscribe();
    httpTesting
      .expectOne((request) => request.url === ENVIRONMENTS_URL)
      .flush(page([ENVIRONMENT]));
  });

  it.each([
    ['create', () => service.create(ENVIRONMENT_INPUT), ENVIRONMENTS_URL, 'POST'],
    [
      'update',
      () => service.update(ENVIRONMENT.id, ENVIRONMENT_INPUT),
      `${ENVIRONMENTS_URL}/${ENVIRONMENT.id}`,
      'PUT',
    ],
    [
      'delete',
      () => service.delete(ENVIRONMENT.id),
      `${ENVIRONMENTS_URL}/${ENVIRONMENT.id}`,
      'DELETE',
    ],
    [
      'reactivate',
      () => service.reactivate(ENVIRONMENT.id),
      `${ENVIRONMENTS_URL}/reactivate/${ENVIRONMENT.id}`,
      'PUT',
    ],
  ])('invalidates the catalog cache after %s succeeds', (_name, mutate, url, method) => {
    primeCache();

    (mutate as () => Observable<unknown>)().subscribe();
    const mutation = httpTesting.expectOne(url);
    expect(mutation.request.method).toBe(method);
    mutation.flush(method === 'DELETE' ? null : ENVIRONMENT);

    service.getAll().subscribe();
    httpTesting.expectOne(ENVIRONMENTS_URL).flush(page([ENVIRONMENT]));
  });

  function primeCache(): void {
    service.getAll().subscribe();
    httpTesting.expectOne(ENVIRONMENTS_URL).flush(page([ENVIRONMENT]));
  }
});

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
      pageSize: 100,
      paged: true,
      sort: { empty: false, sorted: true, unsorted: false },
      unpaged: false,
    },
    size: 100,
    sort: { empty: false, sorted: true, unsorted: false },
    totalElements: content.length,
    totalPages: 1,
  };
}
