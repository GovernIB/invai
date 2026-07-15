import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  TestRequest,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { Application, ApplicationInput, ApplicationOutput } from '../applications.model';
import { ApplicationsService } from './applications.service';

const APPLICATION_URL = '/invaiapi/interna/application';

const EXPECTED_APPLICATION: Application = {
  id: '7',
  code: 'APP-7',
  prefix: 'INV',
  name: 'Invai',
  category: 'DRASSANA',
  informationSystem: 'Instrumental',
  scope: 'Departamental',
  commission: 'Equip directiu',
  administrativeUnit: 'Direcció General',
  status: 'Activa',
  description: 'Aplicació interna',
  creationDate: '2026-01-02T10:30:00',
  modificationDate: '',
  withdrawalDate: '',
  categoryId: 1,
  informationSystemId: 2,
  scopeId: 3,
  commissionId: 5,
  administrativeUnitId: 4,
  statusId: 6,
};

const APPLICATION_INPUT: ApplicationInput = {
  name: 'Invai',
  prefix: 'INV',
  code: 'APP-7',
  categoryId: 1,
  systemTypeId: 2,
  fieldId: 3,
  admUnitId: 4,
  commissionId: 5,
  description: 'Aplicació interna',
  statusId: 6,
};

const APPLICATION_OUTPUT: ApplicationOutput = {
  id: 7,
  code: 'APP-7',
  prefix: 'INV',
  name: 'Invai',
  category: { id: 1, name: 'DRASSANA' },
  systemType: { id: 2, name: 'Instrumental' },
  field: { id: 3, name: 'Departamental' },
  admUnit: { id: 4, code: 'DG', name: 'Direcció General' },
  csCommission: { id: 5, name: 'Equip directiu' },
  description: 'Aplicació interna',
  status: { id: 6, name: 'Activa' },
  expirationDate: null,
  createdAt: '2026-01-02T10:30:00',
  createdBy: null,
  updatedAt: null,
  updatedBy: null,
  loadUser: null,
  loadDate: null,
};

const OTHER_APPLICATION_OUTPUT: ApplicationOutput = {
  ...APPLICATION_OUTPUT,
  id: 8,
  code: 'APP-8',
  name: 'Invai admin',
};

describe('ApplicationsService', () => {
  let service: ApplicationsService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(ApplicationsService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('loads and adapts applications for the table', () => {
    const result = vi.fn();

    service.getAll().subscribe(result);

    const request = httpTesting.expectOne(APPLICATION_URL);
    expect(request.request.method).toBe('GET');
    flushApplications(request);

    expect(result).toHaveBeenCalledWith([EXPECTED_APPLICATION]);
  });

  it('shares and reuses the cached applications request', () => {
    const firstResult = vi.fn();
    const secondResult = vi.fn();
    const cachedResult = vi.fn();

    service.getAll().subscribe(firstResult);
    service.getAll().subscribe(secondResult);

    const request = httpTesting.expectOne(APPLICATION_URL);
    flushApplications(request);

    expect(firstResult).toHaveBeenCalledWith([EXPECTED_APPLICATION]);
    expect(secondResult).toHaveBeenCalledWith([EXPECTED_APPLICATION]);

    service.getAll().subscribe(cachedResult);

    httpTesting.expectNone(APPLICATION_URL);
    expect(cachedResult).toHaveBeenCalledWith([EXPECTED_APPLICATION]);
  });

  it('keeps cached application pages separated by criteria', () => {
    const activeResult = vi.fn();
    const maintenanceResult = vi.fn();

    service.getPage({ page: 0, size: 10, statusId: 1 }).subscribe(activeResult);
    service.getPage({ page: 0, size: 10, statusId: 2 }).subscribe(maintenanceResult);

    const requests = httpTesting.match(
      (request) => request.method === 'GET' && request.url === APPLICATION_URL,
    );
    expect(requests).toHaveLength(2);
    expect(requests.map((request) => request.request.params.get('statusId')).sort()).toEqual([
      '1',
      '2',
    ]);
    requests.forEach(flushApplications);

    expect(activeResult).toHaveBeenCalledOnce();
    expect(maintenanceResult).toHaveBeenCalledOnce();
  });

  it('does not cache quick searches', () => {
    service.getPage({ page: 0, size: 10, quickSearch: ' inv ' }).subscribe();
    service.getPage({ page: 0, size: 10, quickSearch: ' inv ' }).subscribe();

    const requests = httpTesting.match(
      (request) =>
        request.method === 'GET' &&
        request.url === APPLICATION_URL &&
        request.params.get('quickSearch') === 'inv',
    );
    expect(requests).toHaveLength(2);
    requests.forEach(flushApplications);
  });

  it('retries the applications request after a failed load', () => {
    const errorResult = vi.fn();
    const retryResult = vi.fn();

    service.getAll().subscribe({ error: errorResult });

    const failedRequest = httpTesting.expectOne(APPLICATION_URL);
    failedRequest.flush('Request failed', {
      status: 500,
      statusText: 'Server Error',
    });

    expect(errorResult).toHaveBeenCalledOnce();

    service.getAll().subscribe(retryResult);

    const retryRequest = httpTesting.expectOne(APPLICATION_URL);
    flushApplications(retryRequest);

    expect(retryResult).toHaveBeenCalledWith([EXPECTED_APPLICATION]);
  });

  it('clears the cached applications and details', () => {
    const firstApplicationsResult = vi.fn();
    const refreshedApplicationsResult = vi.fn();
    const firstDetailResult = vi.fn();
    const refreshedDetailResult = vi.fn();

    service.getAll().subscribe(firstApplicationsResult);
    flushApplications(httpTesting.expectOne(APPLICATION_URL));
    service.getById(7).subscribe(firstDetailResult);
    httpTesting.expectOne(`${APPLICATION_URL}/7`).flush(APPLICATION_OUTPUT);

    service.clearCache();

    service.getAll().subscribe(refreshedApplicationsResult);
    flushApplications(httpTesting.expectOne(APPLICATION_URL));
    service.getById(7).subscribe(refreshedDetailResult);
    httpTesting.expectOne(`${APPLICATION_URL}/7`).flush(APPLICATION_OUTPUT);

    expect(firstApplicationsResult).toHaveBeenCalledWith([EXPECTED_APPLICATION]);
    expect(refreshedApplicationsResult).toHaveBeenCalledWith([EXPECTED_APPLICATION]);
    expect(firstDetailResult).toHaveBeenCalledWith(APPLICATION_OUTPUT);
    expect(refreshedDetailResult).toHaveBeenCalledWith(APPLICATION_OUTPUT);
  });

  it('loads paged application DTOs with filter and pagination params', () => {
    const result = vi.fn();

    service
      .getPage({
        page: 1,
        size: 20,
        sort: ['name,asc', 'id,desc'],
        prefix: 'INV',
        applicationName: 'Invai',
        categoryId: 1,
        systemTypeId: 2,
        fieldId: 3,
        commissionId: 5,
        admUnitId: 4,
        statusId: 6,
        description: 'interna',
        quickSearch: 'inv',
      })
      .subscribe(result);

    const request = httpTesting.expectOne(
      (req) =>
        req.method === 'GET' &&
        req.url === APPLICATION_URL &&
        req.params.get('page') === '1' &&
        req.params.get('size') === '20' &&
        req.params.getAll('sort')?.join('|') === 'name,asc|id,desc' &&
        req.params.get('prefix') === 'INV' &&
        req.params.get('applicationName') === 'Invai' &&
        req.params.get('categoryId') === '1' &&
        req.params.get('systemTypeId') === '2' &&
        req.params.get('fieldId') === '3' &&
        req.params.get('commissionId') === '5' &&
        req.params.get('admUnitId') === '4' &&
        req.params.get('statusId') === '6' &&
        req.params.get('description') === 'interna' &&
        req.params.get('quickSearch') === 'inv',
    );

    request.flush(page([APPLICATION_OUTPUT]));

    expect(result).toHaveBeenCalledWith(
      expect.objectContaining({
        content: [EXPECTED_APPLICATION],
      }),
    );
  });

  it('loads an application DTO by id', () => {
    const result = vi.fn();

    service.getById(7).subscribe(result);

    const request = httpTesting.expectOne(`${APPLICATION_URL}/7`);
    expect(request.request.method).toBe('GET');
    request.flush(APPLICATION_OUTPUT);

    expect(result).toHaveBeenCalledWith(APPLICATION_OUTPUT);
  });

  it('shares and reuses the cached application detail request', () => {
    const firstResult = vi.fn();
    const secondResult = vi.fn();
    const cachedResult = vi.fn();

    service.getById(7).subscribe(firstResult);
    service.getById(7).subscribe(secondResult);

    const request = httpTesting.expectOne(`${APPLICATION_URL}/7`);
    expect(request.request.method).toBe('GET');
    request.flush(APPLICATION_OUTPUT);

    expect(firstResult).toHaveBeenCalledWith(APPLICATION_OUTPUT);
    expect(secondResult).toHaveBeenCalledWith(APPLICATION_OUTPUT);

    service.getById(7).subscribe(cachedResult);

    httpTesting.expectNone(`${APPLICATION_URL}/7`);
    expect(cachedResult).toHaveBeenCalledWith(APPLICATION_OUTPUT);
  });

  it('keeps cached application details separated by id', () => {
    const firstResult = vi.fn();
    const secondResult = vi.fn();

    service.getById(7).subscribe(firstResult);
    service.getById(8).subscribe(secondResult);

    httpTesting.expectOne(`${APPLICATION_URL}/7`).flush(APPLICATION_OUTPUT);
    httpTesting.expectOne(`${APPLICATION_URL}/8`).flush(OTHER_APPLICATION_OUTPUT);

    expect(firstResult).toHaveBeenCalledWith(APPLICATION_OUTPUT);
    expect(secondResult).toHaveBeenCalledWith(OTHER_APPLICATION_OUTPUT);
  });

  it('retries the application detail request after a failed load', () => {
    const errorResult = vi.fn();
    const retryResult = vi.fn();

    service.getById(7).subscribe({ error: errorResult });

    const failedRequest = httpTesting.expectOne(`${APPLICATION_URL}/7`);
    failedRequest.flush('Request failed', {
      status: 500,
      statusText: 'Server Error',
    });

    expect(errorResult).toHaveBeenCalledOnce();

    service.getById(7).subscribe(retryResult);

    const retryRequest = httpTesting.expectOne(`${APPLICATION_URL}/7`);
    retryRequest.flush(APPLICATION_OUTPUT);

    expect(retryResult).toHaveBeenCalledWith(APPLICATION_OUTPUT);
  });

  it('creates an application with reference ids and clears the table cache', () => {
    const cachedResult = vi.fn();
    const createResult = vi.fn();
    const refreshedResult = vi.fn();

    service.getAll().subscribe(cachedResult);
    flushApplications(httpTesting.expectOne(APPLICATION_URL));

    service.create(APPLICATION_INPUT).subscribe(createResult);

    const createRequest = httpTesting.expectOne(APPLICATION_URL);
    expect(createRequest.request.method).toBe('POST');
    expect(createRequest.request.body).toEqual(APPLICATION_INPUT);
    createRequest.flush(APPLICATION_OUTPUT);

    service.getAll().subscribe(refreshedResult);
    flushApplications(httpTesting.expectOne(APPLICATION_URL));

    expect(createResult).toHaveBeenCalledWith(APPLICATION_OUTPUT);
    expect(refreshedResult).toHaveBeenCalledWith([EXPECTED_APPLICATION]);
  });

  it('updates an application with reference ids and clears the table cache', () => {
    const cachedResult = vi.fn();
    const updateResult = vi.fn();
    const refreshedResult = vi.fn();

    service.getAll().subscribe(cachedResult);
    flushApplications(httpTesting.expectOne(APPLICATION_URL));

    service.update(7, APPLICATION_INPUT).subscribe(updateResult);

    const updateRequest = httpTesting.expectOne(`${APPLICATION_URL}/7`);
    expect(updateRequest.request.method).toBe('PUT');
    expect(updateRequest.request.body).toEqual(APPLICATION_INPUT);
    updateRequest.flush(APPLICATION_OUTPUT);

    service.getAll().subscribe(refreshedResult);
    flushApplications(httpTesting.expectOne(APPLICATION_URL));

    expect(updateResult).toHaveBeenCalledWith(APPLICATION_OUTPUT);
    expect(refreshedResult).toHaveBeenCalledWith([EXPECTED_APPLICATION]);
  });

  it('deletes an application and clears the table cache', () => {
    const cachedResult = vi.fn();
    const deleteResult = vi.fn();
    const refreshedResult = vi.fn();

    service.getAll().subscribe(cachedResult);
    flushApplications(httpTesting.expectOne(APPLICATION_URL));

    service.delete(7).subscribe(deleteResult);

    const deleteRequest = httpTesting.expectOne(`${APPLICATION_URL}/7`);
    expect(deleteRequest.request.method).toBe('DELETE');
    deleteRequest.flush(null);

    service.getAll().subscribe(refreshedResult);
    flushApplications(httpTesting.expectOne(APPLICATION_URL));

    expect(deleteResult).toHaveBeenCalledWith(null);
    expect(refreshedResult).toHaveBeenCalledWith([EXPECTED_APPLICATION]);
  });

  it('clears cached application details after successful mutations', () => {
    const cachedResult = vi.fn();
    const createResult = vi.fn();
    const afterCreateResult = vi.fn();
    const updateResult = vi.fn();
    const afterUpdateResult = vi.fn();
    const deleteResult = vi.fn();
    const afterDeleteResult = vi.fn();

    service.getById(7).subscribe(cachedResult);
    httpTesting.expectOne(`${APPLICATION_URL}/7`).flush(APPLICATION_OUTPUT);

    service.create(APPLICATION_INPUT).subscribe(createResult);
    httpTesting.expectOne(APPLICATION_URL).flush(APPLICATION_OUTPUT);

    service.getById(7).subscribe(afterCreateResult);
    httpTesting.expectOne(`${APPLICATION_URL}/7`).flush(APPLICATION_OUTPUT);

    service.update(7, APPLICATION_INPUT).subscribe(updateResult);
    httpTesting
      .expectOne((request) => request.method === 'PUT' && request.url === `${APPLICATION_URL}/7`)
      .flush(APPLICATION_OUTPUT);

    service.getById(7).subscribe(afterUpdateResult);
    httpTesting.expectOne(`${APPLICATION_URL}/7`).flush(APPLICATION_OUTPUT);

    service.delete(7).subscribe(deleteResult);
    httpTesting
      .expectOne((request) => request.method === 'DELETE' && request.url === `${APPLICATION_URL}/7`)
      .flush(null);

    service.getById(7).subscribe(afterDeleteResult);
    httpTesting.expectOne(`${APPLICATION_URL}/7`).flush(APPLICATION_OUTPUT);

    expect(cachedResult).toHaveBeenCalledWith(APPLICATION_OUTPUT);
    expect(createResult).toHaveBeenCalledWith(APPLICATION_OUTPUT);
    expect(afterCreateResult).toHaveBeenCalledWith(APPLICATION_OUTPUT);
    expect(updateResult).toHaveBeenCalledWith(APPLICATION_OUTPUT);
    expect(afterUpdateResult).toHaveBeenCalledWith(APPLICATION_OUTPUT);
    expect(deleteResult).toHaveBeenCalledWith(null);
    expect(afterDeleteResult).toHaveBeenCalledWith(APPLICATION_OUTPUT);
  });
});

function flushApplications(request: TestRequest): void {
  request.flush(page([APPLICATION_OUTPUT]));
}

function page<T>(content: T[]): Record<string, unknown> {
  return {
    content,
    empty: false,
    first: true,
    last: true,
    number: 0,
    numberOfElements: content.length,
    pageable: {
      offset: 0,
      pageNumber: 0,
      pageSize: 10,
      paged: true,
      sort: { empty: false, sorted: true, unsorted: false },
      unpaged: false,
    },
    size: 10,
    sort: { empty: false, sorted: true, unsorted: false },
    totalElements: content.length,
    totalPages: 1,
  };
}
