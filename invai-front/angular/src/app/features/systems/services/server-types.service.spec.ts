import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { firstValueFrom } from 'rxjs';

import { ServerTypeOutput } from '../systems.model';
import { ServerTypesService } from './server-types.service';

const URL = '/invaiapi/interna/server-type';
const SERVER_TYPES: ServerTypeOutput[] = [
  {
    id: 101,
    code: 'APPLICATION',
    name: 'Aplicació',
    nameEs: 'Aplicación',
  },
  {
    id: 202,
    code: 'DATABASE',
    name: 'Base de dades',
    nameEs: 'Base de datos',
  },
];

describe('ServerTypesService', () => {
  let service: ServerTypesService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ServerTypesService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('shares and reuses the server type catalog request', () => {
    service.getAll().subscribe();
    service.getAll().subscribe();

    const request = http.expectOne(URL);
    expect(request.request.method).toBe('GET');
    request.flush(SERVER_TYPES);

    service.getAll().subscribe();
    http.expectNone(URL);
  });

  it('resolves supported server types by their technical code', async () => {
    const application = firstValueFrom(service.getByCode('APPLICATION'));
    const database = firstValueFrom(service.getByCode('DATABASE'));
    http.expectOne(URL).flush(SERVER_TYPES);

    await expect(application).resolves.toEqual(SERVER_TYPES[0]);
    await expect(database).resolves.toEqual(SERVER_TYPES[1]);
  });

  it('fails when the required technical code is absent', async () => {
    const result = firstValueFrom(service.getByCode('DATABASE'));
    http.expectOne(URL).flush([SERVER_TYPES[0]]);

    await expect(result).rejects.toThrow('Server type DATABASE is not available');
  });

  it('removes a failed request from cache so a later call retries', () => {
    service.getAll().subscribe({ error: vi.fn() });
    http.expectOne(URL).flush('fail', {
      status: 500,
      statusText: 'Error',
    });

    service.getAll().subscribe();
    http.expectOne(URL).flush(SERVER_TYPES);
  });
});
