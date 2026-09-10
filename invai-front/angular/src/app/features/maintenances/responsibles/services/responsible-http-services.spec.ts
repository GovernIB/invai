import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';
import { Observable } from 'rxjs';

import { ROLE_TRANSFER_PERSON_SEARCH_PARAMS } from '../responsibles.constants';
import { ResponsiblePersonInput, RoleAssignmentType } from '../responsibles.model';
import { ResponsibleAuthorizationTypesService } from './responsible-authorization-types.service';
import { ResponsibleCompaniesService } from './responsible-companies.service';
import { ResponsiblePeopleService } from './responsible-people.service';
import { RoleTransferService } from './role-transfer.service';

describe('responsible HTTP maintenance services', () => {
  let http: HttpTestingController;
  let companies: ResponsibleCompaniesService;
  let people: ResponsiblePeopleService;
  let authorizationTypes: ResponsibleAuthorizationTypesService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpTestingController);
    companies = TestBed.inject(ResponsibleCompaniesService);
    people = TestBed.inject(ResponsiblePeopleService);
    authorizationTypes = TestBed.inject(ResponsibleAuthorizationTypesService);
  });

  afterEach(() => http.verify());

  it('uses exact company filters, shares cache entries and retries a failed entry', () => {
    const params = {
      page: 1,
      size: 25,
      sort: 'name,desc',
      name: 'Plexus',
      statusId: SoftDeleteStatus.ACTIVE,
      search: 'plex',
    };
    const error = vi.fn();
    companies.getPage(params).subscribe({ error });
    companies.getPage(params).subscribe({ error });
    http
      .expectOne(
        (request) =>
          request.url === '/invaiapi/interna/company' &&
          request.params.get('page') === '1' &&
          request.params.get('size') === '25' &&
          request.params.get('sort') === 'name,desc' &&
          request.params.get('name') === 'Plexus' &&
          request.params.get('statusId') === '1' &&
          request.params.get('search') === 'plex',
      )
      .flush('failed', { status: 500, statusText: 'Error' });
    expect(error).toHaveBeenCalledTimes(2);

    companies.getPage(params).subscribe();
    http
      .expectOne(
        '/invaiapi/interna/company?page=1&size=25&sort=name,desc&name=Plexus&statusId=1&search=plex',
      )
      .flush(page([]));
  });

  it('invalidates company pages and dependent person pages only after a successful mutation', () => {
    const companyParams = { page: 0, size: 10, statusId: SoftDeleteStatus.ACTIVE };
    const personParams = { page: 0, size: 10, companyId: 7, statusId: SoftDeleteStatus.ACTIVE };
    companies.getPage(companyParams).subscribe();
    http.expectOne((request) => request.url === '/invaiapi/interna/company').flush(page([]));
    people.getPage(personParams).subscribe();
    http
      .expectOne(
        (request) =>
          request.url === '/invaiapi/interna/person/database-search' &&
          request.params.get('companyId') === '7',
      )
      .flush(page([]));

    companies.create({ name: 'Nova' }).subscribe();
    http
      .expectOne(
        (request) => request.method === 'POST' && request.url === '/invaiapi/interna/company',
      )
      .flush({ id: 8, name: 'Nova', deletedAt: null });
    companies.getPage(companyParams).subscribe();
    http.expectOne((request) => request.url === '/invaiapi/interna/company').flush(page([]));
    people.getPage(personParams).subscribe();
    http
      .expectOne((request) => request.url === '/invaiapi/interna/person/database-search')
      .flush(page([]));
  });

  it('sends the person contract and authorization bilingual criteria', () => {
    const person = {
      companyId: 4,
      firstName: 'Maria',
      lastName: 'Tur',
      email: 'maria@example.org',
      personalCaib: false as const,
    };
    people.create(person).subscribe();
    const personRequest = http.expectOne('/invaiapi/interna/person');
    expect(personRequest.request.body).toEqual(person);
    personRequest.flush({
      id: 1,
      company: { id: 4, name: 'Plexus', deletedAt: null },
      ...person,
      deletedAt: null,
    });

    authorizationTypes
      .getPage({
        page: 0,
        size: 10,
        name: 'Signar',
        nameEs: 'Firmar',
        statusId: SoftDeleteStatus.INACTIVE,
      })
      .subscribe();
    http
      .expectOne(
        (request) =>
          request.url === '/invaiapi/interna/authorization-type' &&
          request.params.get('name') === 'Signar' &&
          request.params.get('nameEs') === 'Firmar' &&
          request.params.get('statusId') === '2',
      )
      .flush(page([]));
  });

  it('keys and sends first-name and last-name filters independently', () => {
    people.getPage({ page: 0, size: 10, firstName: 'Maria', lastName: 'Tur' }).subscribe();
    http
      .expectOne(
        (request) =>
          request.url === '/invaiapi/interna/person/database-search' &&
          request.params.get('firstName') === 'Maria' &&
          request.params.get('lastName') === 'Tur' &&
          !request.params.has('name'),
      )
      .flush(page([]));
  });

  it('keys and sends the excluded person independently', () => {
    const params = { page: 0, size: 1000, statusId: SoftDeleteStatus.ACTIVE, excludeId: 7 };
    people.getPage(params).subscribe();
    people.getPage(params).subscribe();
    http
      .expectOne(
        (request) =>
          request.url === '/invaiapi/interna/person/database-search' &&
          request.params.get('excludeId') === '7',
      )
      .flush(page([]));

    people.getPage({ ...params, excludeId: 8 }).subscribe();
    http
      .expectOne(
        (request) =>
          request.url === '/invaiapi/interna/person/database-search' &&
          request.params.get('excludeId') === '8',
      )
      .flush(page([]));
  });

  it('normalizes and does not cache remote person searches', () => {
    const params = {
      page: 0,
      size: 20,
      sort: ['firstName,asc', 'lastName,asc'],
      statusId: SoftDeleteStatus.ACTIVE,
      search: ' maria ',
    };

    people.getPage(params).subscribe();
    people.getPage(params).subscribe();

    const requests = http.match(
      (request) =>
        request.url === '/invaiapi/interna/person/database-search' &&
        request.params.get('search') === 'maria' &&
        request.params.getAll('sort')?.join('|') === 'firstName,asc|lastName,asc',
    );
    expect(requests).toHaveLength(2);
    requests.forEach((request) => request.flush(page([])));
  });

  it('searches Soffid with the fixed first page, does not cache and retries errors', () => {
    const error = vi.fn();
    people.searchSoffid('  Maria Tur  ').subscribe();
    people.searchSoffid('Maria Tur').subscribe({ error: () => undefined });

    const requests = http.match(
      (request) =>
        request.url === '/invaiapi/interna/person/soffid-search' &&
        request.params.get('fullName') === 'Maria Tur' &&
        request.params.get('page') === '0' &&
        request.params.get('size') === '20' &&
        !request.params.has('sort'),
    );
    expect(requests).toHaveLength(2);
    requests[0].flush(page([]));
    requests[1].flush('failed', { status: 500, statusText: 'Error' });

    people.searchSoffid('Maria Tur').subscribe({ error });
    http
      .expectOne('/invaiapi/interna/person/soffid-search?fullName=Maria%20Tur&page=0&size=20')
      .flush('failed again', { status: 500, statusText: 'Error' });
    expect(error).toHaveBeenCalledOnce();
  });

  it('searches database and Soffid together without caching the typeahead response', () => {
    const params = {
      page: 0,
      size: 20,
      sort: ['firstName,asc', 'lastName,asc'],
      search: '  Maria  ',
    };

    people.searchCombined(params).subscribe();
    people.searchCombined(params).subscribe();

    const requests = http.match(
      (request) =>
        request.url === '/invaiapi/interna/person/all' &&
        request.params.get('search') === 'Maria' &&
        request.params.get('page') === '0' &&
        request.params.get('size') === '20' &&
        request.params.getAll('sort')?.join('|') === 'firstName,asc|lastName,asc',
    );
    expect(requests).toHaveLength(2);
    requests.forEach((request) =>
      request.flush({ database: page([]), soffid: page([]) }),
    );
  });

  it('shares and reuses the initial combined catalog, including empty search variants', () => {
    const params = ROLE_TRANSFER_PERSON_SEARCH_PARAMS;
    const received = vi.fn();
    const response = { database: page([{ id: 7 }]), soffid: page([]) };
    const request$ = people.searchCombined(params);
    request$.subscribe(received);
    request$.subscribe(received);
    people.searchCombined({ ...params, search: '' }).subscribe(received);
    people.searchCombined({ ...params, search: '   ' }).subscribe(received);

    const request = http.expectOne((request) => request.url === '/invaiapi/interna/person/all');
    expect(request.request.params.get('page')).toBe('0');
    expect(request.request.params.get('size')).toBe('20');
    expect(request.request.params.getAll('sort')).toEqual(['firstName,asc', 'lastName,asc']);
    expect(request.request.params.has('search')).toBe(false);
    request.flush(response);

    people.searchCombined({ ...params, sort: [...params.sort] }).subscribe(received);
    http.expectNone((request) => request.url === '/invaiapi/interna/person/all');
    expect(received).toHaveBeenCalledTimes(5);
    expect(received.mock.calls).toEqual(Array.from({ length: 5 }, () => [response]));
  });

  it.each([
    { page: 1 },
    { size: 10 },
    { sort: ['firstName,desc', 'lastName,asc'] },
    { sort: ['lastName,asc', 'firstName,asc'] },
  ])('separates combined catalog entries for %j', (overrides) => {
    const initialParams = ROLE_TRANSFER_PERSON_SEARCH_PARAMS;
    const otherParams = { ...initialParams, ...overrides };
    const initialResponse = { database: page([{ id: 7 }]), soffid: page([]) };
    const otherResponse = { database: page([{ id: 8 }]), soffid: page([]) };

    people.searchCombined(initialParams).subscribe();
    http.expectOne((request) => request.url === '/invaiapi/interna/person/all').flush(initialResponse);
    people.searchCombined(otherParams).subscribe();
    http.expectOne((request) => request.url === '/invaiapi/interna/person/all').flush(otherResponse);

    const initial = vi.fn();
    const other = vi.fn();
    people.searchCombined(initialParams).subscribe(initial);
    people.searchCombined(otherParams).subscribe(other);
    http.expectNone((request) => request.url === '/invaiapi/interna/person/all');
    expect(initial).toHaveBeenCalledWith(initialResponse);
    expect(other).toHaveBeenCalledWith(otherResponse);
  });

  it('retries a failed combined catalog without evicting another page', () => {
    const params = ROLE_TRANSFER_PERSON_SEARCH_PARAMS;
    people.searchCombined({ ...params, page: 1 }).subscribe();
    http.expectOne((request) => request.url === '/invaiapi/interna/person/all')
      .flush({ database: page([]), soffid: page([]) });

    const error = vi.fn();
    people.searchCombined(params).subscribe({ error });
    people.searchCombined(params).subscribe({ error });
    http.expectOne((request) => request.url === '/invaiapi/interna/person/all')
      .flush('failed', { status: 500, statusText: 'Error' });
    expect(error).toHaveBeenCalledTimes(2);

    const cached = vi.fn();
    people.searchCombined({ ...params, page: 1 }).subscribe(cached);
    http.expectNone((request) => request.url === '/invaiapi/interna/person/all');
    expect(cached).toHaveBeenCalledOnce();

    const retried = vi.fn();
    people.searchCombined(params).subscribe(retried);
    http.expectOne((request) => request.url === '/invaiapi/interna/person/all')
      .flush({ database: page([]), soffid: page([]) });
    expect(retried).toHaveBeenCalledOnce();
  });

  describe('person catalog invalidation', () => {
    const input: ResponsiblePersonInput = {
      companyId: 4,
      firstName: 'Maria',
      lastName: 'Tur',
      email: 'maria@example.org',
      personalCaib: false,
    };
    const params = ROLE_TRANSFER_PERSON_SEARCH_PARAMS;

    function loadCatalogs(): void {
      people.getPage(params).subscribe();
      people.searchCombined(params).subscribe();
    }

    function flushCatalogs(): void {
      http.expectOne((request) => request.url === '/invaiapi/interna/person/database-search')
        .flush(page([]));
      http.expectOne((request) => request.url === '/invaiapi/interna/person/all')
        .flush({ database: page([]), soffid: page([]) });
    }

    const mutations: { name: string; run: () => Observable<unknown>; url: string }[] = [
      { name: 'create', run: () => people.create(input), url: '/invaiapi/interna/person' },
      { name: 'update', run: () => people.update(7, input), url: '/invaiapi/interna/person/7' },
      { name: 'deactivate', run: () => people.deactivate(7), url: '/invaiapi/interna/person/7' },
      {
        name: 'reactivate',
        run: () => people.reactivate(7),
        url: '/invaiapi/interna/person/reactivate/7',
      },
      {
        name: 'company update',
        run: () => companies.update(4, { name: 'Nova' }),
        url: '/invaiapi/interna/company/4',
      },
      {
        name: 'role transfer',
        run: () =>
          TestBed.inject(RoleTransferService).apply({
            items: [{ id: 4, type: RoleAssignmentType.RESPONSIBLE }],
            toPersonEmailAddress: 'maria@example.org',
            revoke: false,
          }),
        url: '/invaiapi/interna/role-transfer',
      },
    ];

    it.each(mutations)('invalidates both catalogs only after successful $name', ({ run, url }) => {
      loadCatalogs();
      flushCatalogs();

      run().subscribe();
      const mutation = http.expectOne(url);
      loadCatalogs();
      http.expectNone((request) => request.method === 'GET');
      mutation.flush(null);

      loadCatalogs();
      flushCatalogs();
    });

    it.each(mutations)('preserves both catalogs after failed $name', ({ run, url }) => {
      loadCatalogs();
      flushCatalogs();

      const error = vi.fn();
      run().subscribe({ error });
      http.expectOne(url).flush('failed', { status: 500, statusText: 'Error' });
      expect(error).toHaveBeenCalledOnce();

      loadCatalogs();
      http.expectNone((request) => request.method === 'GET');
    });

    it('clears both catalogs explicitly', () => {
      loadCatalogs();
      flushCatalogs();
      people.clearCache();
      loadCatalogs();
      flushCatalogs();
    });
  });
});

function page<T>(content: T[]) {
  return { content, totalElements: content.length };
}
