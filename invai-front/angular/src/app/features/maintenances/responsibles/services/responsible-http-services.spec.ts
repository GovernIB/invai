import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';

import { ResponsibleAuthorizationTypesService } from './responsible-authorization-types.service';
import { ResponsibleCompaniesService } from './responsible-companies.service';
import { ResponsiblePeopleService } from './responsible-people.service';

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
          request.url === '/invaiapi/interna/person' && request.params.get('companyId') === '7',
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
    http.expectOne((request) => request.url === '/invaiapi/interna/person').flush(page([]));
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
          request.url === '/invaiapi/interna/person' &&
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
          request.url === '/invaiapi/interna/person' && request.params.get('excludeId') === '7',
      )
      .flush(page([]));

    people.getPage({ ...params, excludeId: 8 }).subscribe();
    http
      .expectOne(
        (request) =>
          request.url === '/invaiapi/interna/person' && request.params.get('excludeId') === '8',
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
        request.url === '/invaiapi/interna/person' &&
        request.params.get('search') === 'maria' &&
        request.params.getAll('sort')?.join('|') === 'firstName,asc|lastName,asc',
    );
    expect(requests).toHaveLength(2);
    requests.forEach((request) => request.flush(page([])));
  });
});

function page<T>(content: T[]) {
  return { content, totalElements: content.length };
}
