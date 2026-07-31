import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { Type } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { SpringPage } from '@models/page.model';
import { Observable } from 'rxjs';

import { LayersService } from '@features/layers/services/layers.service';
import { RolesService } from '@features/roles/services/roles.service';
import { TechnologiesService } from '@features/technologies/services/technologies.service';

interface CatalogService {
  getAll(params: {
    page: number;
    size?: number;
    name?: string;
    layerId?: number;
    statusId?: number;
    search?: string;
  }): Observable<SpringPage<unknown>>;
  create(payload: never): Observable<unknown>;
  update(id: number, payload: never): Observable<unknown>;
  delete(id: number): Observable<void>;
  reactivate(id: number): Observable<unknown>;
}

interface Scenario {
  name: string;
  type: Type<unknown>;
  uri: string;
  payload: Record<string, unknown>;
  filteredParams: Record<string, string>;
}

const SCENARIOS: Scenario[] = [
  {
    name: 'roles',
    type: RolesService,
    uri: 'role',
    payload: { name: 'Desenvolupament', nameEs: null },
    filteredParams: { name: 'Des', statusId: '1' },
  },
  {
    name: 'layers',
    type: LayersService,
    uri: 'layer',
    payload: { name: 'Frontend' },
    filteredParams: { name: 'Front', statusId: '1' },
  },
  {
    name: 'technologies',
    type: TechnologiesService,
    uri: 'technology',
    payload: { name: 'Angular', layerId: 2 },
    filteredParams: { name: 'Angular', layerId: '2', statusId: '1' },
  },
];

describe('development catalog services', () => {
  for (const scenario of SCENARIOS) {
    describe(scenario.name, () => {
      let service: CatalogService;
      let http: HttpTestingController;
      const url = `/invaiapi/interna/${scenario.uri}`;

      beforeEach(() => {
        TestBed.configureTestingModule({
          providers: [provideHttpClient(), provideHttpClientTesting()],
        });
        service = TestBed.inject(scenario.type) as unknown as CatalogService;
        http = TestBed.inject(HttpTestingController);
      });

      afterEach(() => http.verify());

      it('shares equal list requests and separates filter parameters', () => {
        const params = { page: 0, size: 10, ...numericParams(scenario.filteredParams) };
        service.getAll(params).subscribe();
        service.getAll(params).subscribe();

        const request = http.expectOne(
          (candidate) =>
            candidate.url === url &&
            Object.entries(scenario.filteredParams).every(
              ([key, value]) => candidate.params.get(key) === value,
            ),
        );
        request.flush(page([]));

        service.getAll({ ...params, page: 1 }).subscribe();
        http.expectOne((candidate) => candidate.url === url && candidate.params.get('page') === '1')
          .flush(page([]));
      });

      it('retries a failed cached request', () => {
        service.getAll({ page: 0, statusId: 1 }).subscribe({ error: vi.fn() });
        http
          .expectOne((candidate) => candidate.url === url)
          .flush('Failed', { status: 500, statusText: 'Error' });

        service.getAll({ page: 0, statusId: 1 }).subscribe();
        http.expectOne((candidate) => candidate.url === url).flush(page([]));
      });

      it('invalidates list caches after every successful mutation', () => {
        const params = { page: 0, statusId: 1 };
        service.getAll(params).subscribe();
        http.expectOne((candidate) => candidate.url === url).flush(page([]));

        const mutations: Array<{
          run: () => Observable<unknown>;
          method: string;
          mutationUrl: string;
        }> = [
          {
            run: () => service.create(scenario.payload as never),
            method: 'POST',
            mutationUrl: url,
          },
          {
            run: () => service.update(7, scenario.payload as never),
            method: 'PUT',
            mutationUrl: `${url}/7`,
          },
          { run: () => service.delete(7), method: 'DELETE', mutationUrl: `${url}/7` },
          {
            run: () => service.reactivate(7),
            method: 'PUT',
            mutationUrl: `${url}/reactivate/7`,
          },
        ];

        for (const mutation of mutations) {
          mutation.run().subscribe();
          const request = http.expectOne(mutation.mutationUrl);
          expect(request.request.method).toBe(mutation.method);
          request.flush(mutation.method === 'DELETE' ? null : { id: 7 });

          service.getAll(params).subscribe();
          http.expectOne((candidate) => candidate.url === url).flush(page([]));
        }
      });
    });
  }
});

function numericParams(values: Record<string, string>): Record<string, number | string> {
  return Object.fromEntries(
    Object.entries(values).map(([key, value]) => [
      key,
      key.endsWith('Id') ? Number(value) : value,
    ]),
  );
}

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
