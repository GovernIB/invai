import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import {
  ApplicationOutput,
  ApplicationSystemDatabaseOutput,
} from '../applications.model';
import { ApplicationSystemDatabaseService } from './application-system-database.service';

const BASE_URL = '/invaiapi/interna/application/system-database';
const RECORD: ApplicationSystemDatabaseOutput = {
  id: 70,
  application: {
    id: 7,
    appInformationSystemDbId: 70,
    appDevelopmentId: 90,
  } as ApplicationOutput,
  observation: '<p>Observacions</p>',
  deletedAt: null,
};

describe('ApplicationSystemDatabaseService', () => {
  let service: ApplicationSystemDatabaseService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ApplicationSystemDatabaseService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTesting.verify());

  it('loads the information-system/database aggregate by its own id and caches it', () => {
    const first = vi.fn();
    const cached = vi.fn();

    service.getById(70).subscribe(first);
    const request = httpTesting.expectOne(
      (req) =>
        req.method === 'GET' &&
        req.url === `${BASE_URL}/70` &&
        req.params.keys().length === 0,
    );
    request.flush(RECORD);
    service.getById(70).subscribe(cached);
    httpTesting.expectNone(`${BASE_URL}/70`);

    expect(first).toHaveBeenCalledWith(RECORD);
    expect(cached).toHaveBeenCalledWith(RECORD);
  });

  it('treats a missing aggregate as a successful null response', () => {
    const result = vi.fn();

    service.getById(70).subscribe(result);
    httpTesting.expectOne(`${BASE_URL}/70`).flush(null);

    expect(result).toHaveBeenCalledWith(null);
  });

  it('separates aggregates and retries after a failed cached request', () => {
    service.getById(70).subscribe({ error: vi.fn() });
    httpTesting
      .expectOne(`${BASE_URL}/70`)
      .flush('Failed', { status: 500, statusText: 'Error' });

    service.getById(70).subscribe();
    httpTesting.expectOne(`${BASE_URL}/70`).flush(RECORD);

    service.getById(71).subscribe();
    httpTesting.expectOne(`${BASE_URL}/71`).flush({ ...RECORD, id: 71 });
  });

  it('creates and updates observations and invalidates cached aggregates', () => {
    const payload = {
      applicationId: 7,
      observation: '<p>Actualitzades</p>',
    };

    service.getById(70).subscribe();
    httpTesting.expectOne(`${BASE_URL}/70`).flush(RECORD);

    service.create(payload).subscribe();
    const createRequest = httpTesting.expectOne(BASE_URL);
    expect(createRequest.request.method).toBe('POST');
    expect(createRequest.request.body).toEqual(payload);
    createRequest.flush(RECORD);

    service.getById(70).subscribe();
    httpTesting.expectOne(`${BASE_URL}/70`).flush(RECORD);

    service.update(70, payload).subscribe();
    const updateRequest = httpTesting.expectOne(`${BASE_URL}/70`);
    expect(updateRequest.request.method).toBe('PUT');
    expect(updateRequest.request.body).toEqual(payload);
    updateRequest.flush({ ...RECORD, observation: payload.observation });

    service.getById(70).subscribe();
    httpTesting.expectOne(`${BASE_URL}/70`).flush(RECORD);
  });
});
