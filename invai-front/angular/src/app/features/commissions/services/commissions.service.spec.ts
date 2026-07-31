import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';

import { CommissionInput, CommissionType } from '../commissions.model';
import { CommissionsService } from './commissions.service';

const COMMISSIONS_URL = '/invaiapi/interna/commission';

describe('CommissionsService', () => {
  let service: CommissionsService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(CommissionsService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('loads commissions with pagination params', () => {
    const result = vi.fn();

    service
      .getAll({
        page: 2,
        size: 25,
        sort: ['name,asc', 'id,desc'],
        statusId: SoftDeleteStatus.ACTIVE,
      })
      .subscribe(result);

    const request = httpTesting.expectOne(
      (req) =>
        req.method === 'GET' &&
        req.url === COMMISSIONS_URL &&
        req.params.get('page') === '2' &&
        req.params.get('size') === '25' &&
        req.params.getAll('sort')?.join('|') === 'name,asc|id,desc' &&
        req.params.get('statusId') === String(SoftDeleteStatus.ACTIVE),
    );

    request.flush(page([{ id: 1, name: 'Comissió tècnica' }]));

    expect(result).toHaveBeenCalledWith(
      expect.objectContaining({
        content: [{ id: 1, name: 'Comissió tècnica' }],
      }),
    );
  });

  it('shares and reuses cached commission pages by params', () => {
    const firstResult = vi.fn();
    const secondResult = vi.fn();
    const cachedResult = vi.fn();

    service.getAll({ page: 2, size: 25, sort: ['name,asc', 'id,desc'] }).subscribe(firstResult);
    service.getAll({ page: 2, size: 25, sort: ['name,asc', 'id,desc'] }).subscribe(secondResult);

    httpTesting
      .expectOne(
        (req) =>
          req.method === 'GET' &&
          req.url === COMMISSIONS_URL &&
          req.params.get('page') === '2',
      )
      .flush(page([{ id: 1, name: 'Comissió tècnica' }]));

    service.getAll({ page: 2, size: 25, sort: ['name,asc', 'id,desc'] }).subscribe(cachedResult);

    httpTesting.expectNone(COMMISSIONS_URL);
    expect(firstResult).toHaveBeenCalledWith(expect.objectContaining({ totalElements: 1 }));
    expect(secondResult).toHaveBeenCalledWith(expect.objectContaining({ totalElements: 1 }));
    expect(cachedResult).toHaveBeenCalledWith(expect.objectContaining({ totalElements: 1 }));
  });

  it('keeps commission cache entries separated by params', () => {
    const firstResult = vi.fn();
    const secondResult = vi.fn();

    service.getAll({ page: 0, size: 10, sort: 'name,asc' }).subscribe(firstResult);
    httpTesting
      .expectOne(
        (req) =>
          req.method === 'GET' &&
          req.url === COMMISSIONS_URL &&
          req.params.get('page') === '0',
      )
      .flush(page([{ id: 1, name: 'Comissió tècnica' }]));

    service.getAll({ page: 1, size: 10, sort: 'name,asc' }).subscribe(secondResult);
    httpTesting
      .expectOne(
        (req) =>
          req.method === 'GET' &&
          req.url === COMMISSIONS_URL &&
          req.params.get('page') === '1',
      )
      .flush(page([{ id: 2, name: 'Comissió informàtica' }]));

    expect(firstResult).toHaveBeenCalledWith(
      expect.objectContaining({ content: [{ id: 1, name: 'Comissió tècnica' }] }),
    );
    expect(secondResult).toHaveBeenCalledWith(
      expect.objectContaining({ content: [{ id: 2, name: 'Comissió informàtica' }] }),
    );
  });

  it('keeps commission cache entries separated for active, inactive and all statuses', () => {
    service.getAll({ page: 0, statusId: SoftDeleteStatus.ACTIVE }).subscribe();
    httpTesting
      .expectOne((req) => req.params.get('statusId') === String(SoftDeleteStatus.ACTIVE))
      .flush(page([]));

    service.getAll({ page: 0, statusId: SoftDeleteStatus.INACTIVE }).subscribe();
    httpTesting
      .expectOne((req) => req.params.get('statusId') === String(SoftDeleteStatus.INACTIVE))
      .flush(page([]));

    service.getAll({ page: 0 }).subscribe();
    httpTesting
      .expectOne((req) => !req.params.has('statusId'))
      .flush(page([]));
  });

  it('retries loading commissions after a failed cached request', () => {
    const errorResult = vi.fn();
    const retryResult = vi.fn();

    service.getAll({ page: 0 }).subscribe({ error: errorResult });
    httpTesting
      .expectOne(
        (req) =>
          req.method === 'GET' &&
          req.url === COMMISSIONS_URL &&
          req.params.get('page') === '0',
      )
      .flush('Request failed', {
        status: 500,
        statusText: 'Server Error',
      });

    service.getAll({ page: 0 }).subscribe(retryResult);
    httpTesting
      .expectOne(
        (req) =>
          req.method === 'GET' &&
          req.url === COMMISSIONS_URL &&
          req.params.get('page') === '0',
      )
      .flush(page([{ id: 1, name: 'Comissió tècnica' }]));

    expect(errorResult).toHaveBeenCalledOnce();
    expect(retryResult).toHaveBeenCalledWith(expect.objectContaining({ totalElements: 1 }));
  });

  it('loads a commission by id', () => {
    const result = vi.fn();

    service.getById(7).subscribe(result);

    const request = httpTesting.expectOne(`${COMMISSIONS_URL}/7`);
    expect(request.request.method).toBe('GET');
    request.flush({ id: 7, name: 'Comissió tècnica' });

    expect(result).toHaveBeenCalledWith({ id: 7, name: 'Comissió tècnica' });
  });

  it('creates a commission', () => {
    const result = vi.fn();

    service.create(commissionInput('Comissió tècnica')).subscribe(result);

    const request = httpTesting.expectOne(COMMISSIONS_URL);
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(commissionInput('Comissió tècnica'));
    request.flush({ id: 9, name: 'Comissió tècnica' });

    expect(result).toHaveBeenCalledWith({ id: 9, name: 'Comissió tècnica' });
  });

  it('clears cached commissions after creating one', () => {
    const refreshedResult = vi.fn();

    service.getAll().subscribe();
    httpTesting.expectOne(COMMISSIONS_URL).flush(page([{ id: 1, name: 'A' }]));

    service.create(commissionInput('B')).subscribe();
    httpTesting.expectOne(COMMISSIONS_URL).flush({ id: 2, name: 'B' });

    service.getAll().subscribe(refreshedResult);
    httpTesting.expectOne(COMMISSIONS_URL).flush(page([{ id: 2, name: 'B' }]));

    expect(refreshedResult).toHaveBeenCalledWith(
      expect.objectContaining({ content: [{ id: 2, name: 'B' }] }),
    );
  });

  it('updates a commission', () => {
    const result = vi.fn();

    service.update(9, commissionInput('Comissió informàtica')).subscribe(result);

    const request = httpTesting.expectOne(`${COMMISSIONS_URL}/9`);
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual(commissionInput('Comissió informàtica'));
    request.flush({ id: 9, name: 'Comissió informàtica' });

    expect(result).toHaveBeenCalledWith({ id: 9, name: 'Comissió informàtica' });
  });

  it('clears cached commissions after updating one', () => {
    const refreshedResult = vi.fn();

    service.getAll().subscribe();
    httpTesting.expectOne(COMMISSIONS_URL).flush(page([{ id: 1, name: 'A' }]));

    service.update(1, commissionInput('B')).subscribe();
    httpTesting.expectOne(`${COMMISSIONS_URL}/1`).flush({ id: 1, name: 'B' });

    service.getAll().subscribe(refreshedResult);
    httpTesting.expectOne(COMMISSIONS_URL).flush(page([{ id: 1, name: 'B' }]));

    expect(refreshedResult).toHaveBeenCalledWith(
      expect.objectContaining({ content: [{ id: 1, name: 'B' }] }),
    );
  });

  it('deletes a commission', () => {
    const result = vi.fn();

    service.delete(9).subscribe(result);

    const request = httpTesting.expectOne(`${COMMISSIONS_URL}/9`);
    expect(request.request.method).toBe('DELETE');
    request.flush(null);

    expect(result).toHaveBeenCalledWith(null);
  });

  it('clears cached commissions after deleting one', () => {
    const refreshedResult = vi.fn();

    service.getAll().subscribe();
    httpTesting.expectOne(COMMISSIONS_URL).flush(page([{ id: 1, name: 'A' }]));

    service.delete(1).subscribe();
    httpTesting.expectOne(`${COMMISSIONS_URL}/1`).flush(null);

    service.getAll().subscribe(refreshedResult);
    httpTesting.expectOne(COMMISSIONS_URL).flush(page([]));

    expect(refreshedResult).toHaveBeenCalledWith(expect.objectContaining({ content: [] }));
  });
});

function commissionInput(name: string): CommissionInput {
  return {
    name,
    nameEs: name,
    expedientNumber: `EXP-${name}`,
    approvalDate: '2026-07-14',
    commissionType: CommissionType.TECNICA,
  };
}

function page<T>(content: T[]): Record<string, unknown> {
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
      pageSize: 25,
      paged: true,
      sort: { empty: false, sorted: true, unsorted: false },
      unpaged: false,
    },
    size: 25,
    sort: { empty: false, sorted: true, unsorted: false },
    totalElements: content.length,
    totalPages: 1,
  };
}
