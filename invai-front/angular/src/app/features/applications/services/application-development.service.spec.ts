import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import {
  ApplicationDevelopmentInput,
  ApplicationDevelopmentOutput,
  DevelopmentModality,
  DevelopmentStandardAdaption,
} from '../applications.model';
import { ApplicationDevelopmentService } from './application-development.service';

const BASE_URL = '/invaiapi/interna/application/development';

describe('ApplicationDevelopmentService', () => {
  let service: ApplicationDevelopmentService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ApplicationDevelopmentService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTesting.verify());

  it('loads the development aggregate without pagination and caches it', () => {
    const result = vi.fn();
    const cached = vi.fn();

    service.getById(90).subscribe(result);
    httpTesting
      .expectOne(
        (request) =>
          request.url === `${BASE_URL}/90` &&
          request.params.keys().length === 0,
      )
      .flush(development(3, null));
    service.getById(90).subscribe(cached);
    httpTesting.expectNone(`${BASE_URL}/90`);

    expect(result).toHaveBeenCalledWith(development(3, null));
    expect(cached).toHaveBeenCalledWith(development(3, null));
  });

  it('treats a missing development as a successful null response', () => {
    const result = vi.fn();

    service.getById(90).subscribe(result);
    httpTesting.expectOne(`${BASE_URL}/90`).flush(null);

    expect(result).toHaveBeenCalledWith(null);
  });

  it('shares requests, separates development aggregates and retries after errors', () => {
    const error = vi.fn();
    service.getById(90).subscribe({ error });
    service.getById(90).subscribe({ error });

    httpTesting
      .expectOne((request) => request.url === `${BASE_URL}/90`)
      .flush('Failed', { status: 500, statusText: 'Error' });
    expect(error).toHaveBeenCalledTimes(2);

    service.getById(90).subscribe();
    httpTesting
      .expectOne((request) => request.url === `${BASE_URL}/90`)
      .flush(development(3, null));

    service.getById(91).subscribe();
    httpTesting
      .expectOne((request) => request.url === `${BASE_URL}/91`)
      .flush(development(4, null));
  });

  it('creates and updates with the backend input contract and invalidates reads', () => {
    const payload = input();

    service.getById(90).subscribe();
    httpTesting
      .expectOne((request) => request.url === `${BASE_URL}/90`)
      .flush(null);

    service.create(payload).subscribe();
    const createRequest = httpTesting.expectOne(BASE_URL);
    expect(createRequest.request.method).toBe('POST');
    expect(createRequest.request.body).toEqual(payload);
    createRequest.flush(development(4, null));

    service.getById(90).subscribe();
    httpTesting
      .expectOne((request) => request.url === `${BASE_URL}/90`)
      .flush(development(4, null));

    service.update(4, payload).subscribe();
    const updateRequest = httpTesting.expectOne(`${BASE_URL}/4`);
    expect(updateRequest.request.method).toBe('PUT');
    expect(updateRequest.request.body).toEqual(payload);
    updateRequest.flush(development(4, null));

    service.getById(90).subscribe();
    httpTesting
      .expectOne((request) => request.url === `${BASE_URL}/90`)
      .flush(development(4, null));
  });
});

function input(): ApplicationDevelopmentInput {
  return {
    applicationId: 7,
    environmentId: 3,
    modalityId: DevelopmentModality.INTERNAL,
    code: 'https://git.caib.es/invai',
    standardAdaptionId: DevelopmentStandardAdaption.CONFORMING,
    revisionDate: '2026-05-02T00:00:00',
    observation: '<p>Observació</p>',
  };
}

function development(id: number, deletedAt: string | null): ApplicationDevelopmentOutput {
  return {
    id,
    application: { id: 7 } as ApplicationDevelopmentOutput['application'],
    environment: { id: 3, code: 'PRO', name: 'Producció', nameEs: 'Producción' },
    modality: { id: DevelopmentModality.INTERNAL, name: 'Intern', nameEs: 'Interno' },
    code: 'https://git.caib.es/invai',
    standardAdaption: {
      id: DevelopmentStandardAdaption.CONFORMING,
      name: 'Conforme',
      nameEs: 'Conforme',
    },
    revisionDate: '2026-05-02T00:00:00',
    observation: '<p>Observació</p>',
    deletedAt,
  };
}
