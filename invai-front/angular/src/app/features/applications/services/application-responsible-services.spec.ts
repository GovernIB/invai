import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ResponsibleDataChangesService } from '@features/maintenances/responsibles/services/responsible-data-changes.service';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';

import { ApplicationAuthorizedService } from './application-authorized.service';
import { ApplicationResponsiblesService } from './application-responsibles.service';

describe('application responsible and authorized services', () => {
  let http: HttpTestingController;
  let responsibles: ApplicationResponsiblesService;
  let authorized: ApplicationAuthorizedService;
  let changes: ResponsibleDataChangesService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpTestingController);
    responsibles = TestBed.inject(ApplicationResponsiblesService);
    authorized = TestBed.inject(ApplicationAuthorizedService);
    changes = TestBed.inject(ResponsibleDataChangesService);
  });

  afterEach(() => http.verify());

  it('loads independently by anchor with exact page, sort and filter parameters', () => {
    responsibles
      .getPage({
        appResponsibleAuthorizedId: 90,
        page: 2,
        size: 25,
        sort: 'person.firstName,asc',
        statusId: SoftDeleteStatus.ACTIVE,
        personId: 4,
        responsibleTypeId: 3,
        search: 'mar',
      })
      .subscribe();
    http
      .expectOne(
        (request) =>
          request.url === '/invaiapi/interna/application/responsible/90' &&
          request.params.get('page') === '2' &&
          request.params.get('size') === '25' &&
          request.params.get('sort') === 'person.firstName,asc' &&
          request.params.get('statusId') === '1' &&
          request.params.get('personId') === '4' &&
          request.params.get('responsibleTypeId') === '3' &&
          request.params.get('search') === 'mar',
      )
      .flush(page([]));
  });

  it('keeps responsible and authorized pages separate for different application anchors', () => {
    const responsibleResults: Array<number | null> = [];
    const authorizedResults: number[] = [];
    const params = { page: 0, size: 10, sort: 'id,asc', statusId: SoftDeleteStatus.ACTIVE };

    responsibles
      .getPage({ ...params, appResponsibleAuthorizedId: 90 })
      .subscribe((result) => responsibleResults.push(result.content[0].id));
    responsibles
      .getPage({ ...params, appResponsibleAuthorizedId: 91 })
      .subscribe((result) => responsibleResults.push(result.content[0].id));
    authorized
      .getPage({ ...params, appResponsibleAuthorizedId: 90 })
      .subscribe((result) => authorizedResults.push(result.content[0].id));
    authorized
      .getPage({ ...params, appResponsibleAuthorizedId: 91 })
      .subscribe((result) => authorizedResults.push(result.content[0].id));

    http
      .expectOne(
        '/invaiapi/interna/application/responsible/90?page=0&size=10&sort=id,asc&statusId=1',
      )
      .flush(page([{ id: 900 } as never]));
    http
      .expectOne(
        '/invaiapi/interna/application/responsible/91?page=0&size=10&sort=id,asc&statusId=1',
      )
      .flush(page([{ id: 910 } as never]));
    http
      .expectOne(
        '/invaiapi/interna/application/authorized/90?page=0&size=10&sort=id,asc&statusId=1',
      )
      .flush(page([{ id: 901 } as never]));
    http
      .expectOne(
        '/invaiapi/interna/application/authorized/91?page=0&size=10&sort=id,asc&statusId=1',
      )
      .flush(page([{ id: 911 } as never]));

    expect(responsibleResults).toEqual([900, 910]);
    expect(authorizedResults).toEqual([901, 911]);
  });

  it('shares and reuses identical responsible and authorized page requests', () => {
    const params = { appResponsibleAuthorizedId: 90, page: 0, size: 10 };
    const responsibleFirst = vi.fn();
    const responsibleSecond = vi.fn();
    const authorizedFirst = vi.fn();
    const authorizedSecond = vi.fn();

    responsibles.getPage(params).subscribe(responsibleFirst);
    responsibles.getPage(params).subscribe(responsibleSecond);
    http.expectOne('/invaiapi/interna/application/responsible/90?page=0&size=10').flush(page([]));

    authorized.getPage(params).subscribe(authorizedFirst);
    authorized.getPage(params).subscribe(authorizedSecond);
    http.expectOne('/invaiapi/interna/application/authorized/90?page=0&size=10').flush(page([]));

    responsibles.getPage(params).subscribe(responsibleFirst);
    authorized.getPage(params).subscribe(authorizedFirst);
    http.expectNone('/invaiapi/interna/application/responsible/90?page=0&size=10');
    http.expectNone('/invaiapi/interna/application/authorized/90?page=0&size=10');
    expect(responsibleFirst).toHaveBeenCalledTimes(2);
    expect(responsibleSecond).toHaveBeenCalledOnce();
    expect(authorizedFirst).toHaveBeenCalledTimes(2);
    expect(authorizedSecond).toHaveBeenCalledOnce();
  });

  it('retries failed responsible and authorized page requests', () => {
    const params = { appResponsibleAuthorizedId: 90, page: 0, size: 10 };

    responsibles.getPage(params).subscribe({ error: vi.fn() });
    http
      .expectOne('/invaiapi/interna/application/responsible/90?page=0&size=10')
      .flush(null, { status: 500, statusText: 'Server Error' });
    responsibles.getPage(params).subscribe();
    http.expectOne('/invaiapi/interna/application/responsible/90?page=0&size=10').flush(page([]));

    authorized.getPage(params).subscribe({ error: vi.fn() });
    http
      .expectOne('/invaiapi/interna/application/authorized/90?page=0&size=10')
      .flush(null, { status: 500, statusText: 'Server Error' });
    authorized.getPage(params).subscribe();
    http.expectOne('/invaiapi/interna/application/authorized/90?page=0&size=10').flush(page([]));
  });

  it('invalidates each page cache only after a successful write', () => {
    const params = { appResponsibleAuthorizedId: 90, page: 0, size: 10 };
    const responsibleClearCache = vi.spyOn(responsibles, 'clearCache');
    const authorizedClearCache = vi.spyOn(authorized, 'clearCache');
    const responsibleInput = {
      appResponsibleAuthorizedId: 90,
      personId: 4,
      responsibleTypeId: 3,
      jobTitle: null,
      observation: null,
      personalCaib: true,
    };
    const authorizedInput = {
      appResponsibleAuthorizedId: 90,
      personId: 4,
      authorizationTypeIds: [2],
      observation: null,
      personalCaib: true,
    };

    responsibles.getPage(params).subscribe();
    http.expectOne('/invaiapi/interna/application/responsible/90?page=0&size=10').flush(page([]));
    authorized.getPage(params).subscribe();
    http.expectOne('/invaiapi/interna/application/authorized/90?page=0&size=10').flush(page([]));

    responsibles.create(responsibleInput).subscribe({ error: vi.fn() });
    http
      .expectOne('/invaiapi/interna/application/responsible')
      .flush(null, { status: 500, statusText: 'Server Error' });
    expect(responsibleClearCache).not.toHaveBeenCalled();
    responsibles.getPage(params).subscribe();
    http.expectNone('/invaiapi/interna/application/responsible/90?page=0&size=10');

    responsibles.create(responsibleInput).subscribe();
    http.expectOne('/invaiapi/interna/application/responsible').flush({ id: 1 });
    expect(responsibleClearCache).toHaveBeenCalledOnce();
    expect(authorizedClearCache).toHaveBeenCalledOnce();
    responsibles.getPage(params).subscribe();
    http.expectOne('/invaiapi/interna/application/responsible/90?page=0&size=10').flush(page([]));
    authorized.getPage(params).subscribe();
    http.expectOne('/invaiapi/interna/application/authorized/90?page=0&size=10').flush(page([]));

    responsibleClearCache.mockClear();
    authorizedClearCache.mockClear();

    authorized.create(authorizedInput).subscribe({ error: vi.fn() });
    http
      .expectOne('/invaiapi/interna/application/authorized')
      .flush(null, { status: 500, statusText: 'Server Error' });
    expect(authorizedClearCache).not.toHaveBeenCalled();
    authorized.getPage(params).subscribe();
    http.expectNone('/invaiapi/interna/application/authorized/90?page=0&size=10');

    authorized.create(authorizedInput).subscribe();
    http.expectOne('/invaiapi/interna/application/authorized').flush({ id: 1 });
    expect(authorizedClearCache).toHaveBeenCalledOnce();
    expect(responsibleClearCache).toHaveBeenCalledOnce();
    authorized.getPage(params).subscribe();
    http.expectOne('/invaiapi/interna/application/authorized/90?page=0&size=10').flush(page([]));

    responsibleClearCache.mockClear();
    authorizedClearCache.mockClear();

    responsibles.update(1, responsibleInput).subscribe();
    http.expectOne('/invaiapi/interna/application/responsible/1').flush({ id: 1 });
    responsibles.deactivate(1, { observation: null }).subscribe();
    http.expectOne('/invaiapi/interna/application/responsible/deactivate/1').flush({ id: 1 });
    responsibles.reactivate(1).subscribe();
    http.expectOne('/invaiapi/interna/application/responsible/reactivate/1').flush({ id: 1 });
    expect(responsibleClearCache).toHaveBeenCalledTimes(3);
    expect(authorizedClearCache).toHaveBeenCalledTimes(3);

    responsibleClearCache.mockClear();
    authorizedClearCache.mockClear();

    authorized.update(1, authorizedInput).subscribe();
    http.expectOne('/invaiapi/interna/application/authorized/1').flush({ id: 1 });
    authorized.deactivate(1, { observation: null }).subscribe();
    http
      .expectOne('/invaiapi/interna/application/authorized/deactivate/1')
      .flush(null, { status: 204, statusText: 'No Content' });
    authorized.reactivate(1).subscribe();
    http.expectOne('/invaiapi/interna/application/authorized/reactivate/1').flush({ id: 1 });
    expect(authorizedClearCache).toHaveBeenCalledTimes(3);
    expect(responsibleClearCache).toHaveBeenCalledTimes(3);
  });

  it('models responsible deactivation as 200 and authorized deactivation as 204', () => {
    responsibles.deactivate(7, { observation: 'Baixa' }).subscribe();
    const responsible = http.expectOne('/invaiapi/interna/application/responsible/deactivate/7');
    expect(responsible.request.method).toBe('PUT');
    expect(responsible.request.body).toEqual({ observation: 'Baixa' });
    responsible.flush({ id: 7, observation: 'Baixa', deletedAt: '2026-08-14' });

    authorized.deactivate(8, { observation: null }).subscribe();
    const authorizedRequest = http.expectOne(
      '/invaiapi/interna/application/authorized/deactivate/8',
    );
    expect(authorizedRequest.request.body).toEqual({ observation: null });
    authorizedRequest.flush(null, { status: 204, statusText: 'No Content' });
  });

  it('invalidates assignment caches when nested catalogs or assignments change', () => {
    const responsibleParams = { appResponsibleAuthorizedId: 90, page: 0, size: 10 };
    const authorizedParams = { appResponsibleAuthorizedId: 90, page: 0, size: 10 };
    responsibles.getPage(responsibleParams).subscribe();
    http.expectOne('/invaiapi/interna/application/responsible/90?page=0&size=10').flush(page([]));
    authorized.getPage(authorizedParams).subscribe();
    http.expectOne('/invaiapi/interna/application/authorized/90?page=0&size=10').flush(page([]));

    changes.peopleChanged();
    changes.authorizationTypesChanged();
    changes.assignmentsChanged();
    responsibles.getPage(responsibleParams).subscribe();
    http.expectOne('/invaiapi/interna/application/responsible/90?page=0&size=10').flush(page([]));
    authorized.getPage(authorizedParams).subscribe();
    http.expectOne('/invaiapi/interna/application/authorized/90?page=0&size=10').flush(page([]));
  });
});

function page<T>(content: T[]) {
  return { content, totalElements: content.length };
}
