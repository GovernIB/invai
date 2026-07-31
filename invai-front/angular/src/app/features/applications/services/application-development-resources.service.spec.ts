import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { SpringPage } from '@models/page.model';

import {
  ApplicationProviderInput,
  ApplicationTechnologyInput,
} from '../applications.model';
import { ApplicationProvidersService } from './application-providers.service';
import { ApplicationTechnologiesService } from './application-technologies.service';

describe('application development resource services', () => {
  let providers: ApplicationProvidersService;
  let technologies: ApplicationTechnologiesService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    providers = TestBed.inject(ApplicationProvidersService);
    technologies = TestBed.inject(ApplicationTechnologiesService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTesting.verify());

  it('caches provider pages by development aggregate and pagination parameters', () => {
    const params = { appDevelopmentId: 90, page: 0, size: 10, sort: 'id,asc' };
    providers.getPage(params).subscribe();
    providers.getPage(params).subscribe();

    httpTesting
      .expectOne(
        (request) =>
          request.url === '/invaiapi/interna/application/development/provider/90' &&
          request.params.get('page') === '0' &&
          request.params.get('size') === '10' &&
          request.params.get('sort') === 'id,asc',
      )
      .flush(page([]));
  });

  it('retries provider pages after an error', () => {
    const params = { appDevelopmentId: 90, page: 0, size: 10 };
    providers.getPage(params).subscribe({ error: vi.fn() });
    httpTesting
      .expectOne((request) => request.url.endsWith('/provider/90'))
      .flush('Failed', { status: 500, statusText: 'Error' });

    providers.getPage(params).subscribe();
    httpTesting.expectOne((request) => request.url.endsWith('/provider/90')).flush(page([]));
  });

  it('creates, updates and deletes providers while invalidating cached pages', () => {
    const params = { appDevelopmentId: 90, page: 0, size: 10 };
    const payload: ApplicationProviderInput = {
      appDevelopmentId: 9,
      companyName: 'Plexus SL',
      roleId: 3,
      startDate: '2026-05-02T00:00:00',
      expireDate: null,
    };

    providers.getPage(params).subscribe();
    httpTesting.expectOne((request) => request.url.endsWith('/provider/90')).flush(page([]));

    providers.create(payload).subscribe();
    const creation = httpTesting.expectOne(
      '/invaiapi/interna/application/development/provider',
    );
    expect(creation.request.method).toBe('POST');
    expect(creation.request.body).toEqual(payload);
    creation.flush({ id: 4 });

    providers.getPage(params).subscribe();
    httpTesting.expectOne((request) => request.url.endsWith('/provider/90')).flush(page([]));

    providers.update(4, payload).subscribe();
    const update = httpTesting.expectOne(
      '/invaiapi/interna/application/development/provider/4',
    );
    expect(update.request.method).toBe('PUT');
    expect(update.request.body).toEqual(payload);
    update.flush({ id: 4 });

    providers.delete(4).subscribe();
    const deletion = httpTesting.expectOne(
      '/invaiapi/interna/application/development/provider/4',
    );
    expect(deletion.request.method).toBe('DELETE');
    deletion.flush(null);
  });

  it('creates, updates and deletes technologies while invalidating cached pages', () => {
    const params = { appDevelopmentId: 90, page: 0, size: 10 };
    technologies.getPage(params).subscribe();
    technologies.getPage(params).subscribe();
    httpTesting.expectOne((request) => request.url.endsWith('/technology/90')).flush(page([]));

    const payload: ApplicationTechnologyInput = {
      appDevelopmentId: 9,
      layerId: 1,
      technologyId: 2,
      version: '21',
      architecture: 'Monolítica',
    };

    technologies.create(payload).subscribe();
    const creation = httpTesting.expectOne(
      '/invaiapi/interna/application/development/technology',
    );
    expect(creation.request.method).toBe('POST');
    expect(creation.request.body).toEqual(payload);
    creation.flush({ id: 5 });

    technologies.getPage(params).subscribe();
    httpTesting
      .expectOne((request) => request.url.endsWith('/technology/90'))
      .flush(page([]));

    technologies.update(5, payload).subscribe();
    const update = httpTesting.expectOne(
      '/invaiapi/interna/application/development/technology/5',
    );
    expect(update.request.method).toBe('PUT');
    expect(update.request.body).toEqual(payload);
    update.flush({ id: 5 });

    technologies.delete(5).subscribe();
    const deletion = httpTesting.expectOne(
      '/invaiapi/interna/application/development/technology/5',
    );
    expect(deletion.request.method).toBe('DELETE');
    deletion.flush(null);
  });
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
