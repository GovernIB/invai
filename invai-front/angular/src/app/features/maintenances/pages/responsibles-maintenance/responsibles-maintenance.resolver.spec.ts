import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { SpringPage } from '@models/page.model';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';
import { Observable, firstValueFrom, of, throwError } from 'rxjs';

import { ResponsibleAuthorizationTypesService } from '../../responsibles/services/responsible-authorization-types.service';
import { ResponsibleCompaniesService } from '../../responsibles/services/responsible-companies.service';
import { ResponsiblePeopleService } from '../../responsibles/services/responsible-people.service';
import { ResponsiblePersonPageParams } from '../../responsibles/responsibles.model';
import { ROLE_TRANSFER_PERSON_SEARCH_PARAMS } from '../../responsibles/responsibles.constants';
import {
  ResponsiblesMaintenanceResolvedData,
  responsiblesMaintenanceResolver,
} from './responsibles-maintenance.resolver';

describe('responsiblesMaintenanceResolver', () => {
  const companies = {
    getPage: vi.fn(() => of(page([]))),
    getOptions: vi.fn(() => of([])),
  };
  const people = {
    getPage: vi.fn((_params: ResponsiblePersonPageParams) => of(page([]))),
    searchCombined: vi.fn(() => of({ database: page([]), soffid: page([]) })),
  };
  const authorizations = { getPage: vi.fn(() => of(page([]))) };

  beforeEach(() => {
    vi.clearAllMocks();
    companies.getPage.mockReturnValue(of(page([])));
    companies.getOptions.mockReturnValue(of([]));
    people.getPage.mockReturnValue(of(page([])));
    people.searchCombined.mockReturnValue(of({ database: page([]), soffid: page([]) }));
    authorizations.getPage.mockReturnValue(of(page([])));
    TestBed.configureTestingModule({
      providers: [
        { provide: ResponsibleCompaniesService, useValue: companies },
        { provide: ResponsiblePeopleService, useValue: people },
        { provide: ResponsibleAuthorizationTypesService, useValue: authorizations },
      ],
    });
  });

  it('loads the first page for every initial surface, including role transfer', async () => {
    const result = await resolve();

    expect(people.getPage).toHaveBeenCalledTimes(1);
    expect(people.getPage).toHaveBeenCalledWith({
      page: 0,
      size: 10,
      sort: 'id,asc',
      statusId: SoftDeleteStatus.ACTIVE,
    });
    expect(people.searchCombined).toHaveBeenCalledTimes(1);
    expect(people.searchCombined).toHaveBeenCalledWith(ROLE_TRANSFER_PERSON_SEARCH_PARAMS);
    expect(result.transferPeopleSearch).toEqual({ database: page([]), soffid: page([]) });
    expect(result.transferPeopleSearchFailed).toBe(false);
  });

  it('degrades the people page independently when its initial request fails', async () => {
    people.getPage.mockReturnValue(throwError(() => new Error('Unavailable')));

    const result = await resolve();

    expect(result.peoplePage).toBeNull();
    expect(result.peopleLoadFailed).toBe(true);
    expect(result.companiesLoadFailed).toBe(false);
    expect(result.transferPeopleSearchFailed).toBe(false);
  });

  it('degrades the transfer catalog independently when its initial request fails', async () => {
    people.searchCombined.mockReturnValue(throwError(() => new Error('Unavailable')));

    const result = await resolve();

    expect(result.transferPeopleSearch).toBeNull();
    expect(result.transferPeopleSearchFailed).toBe(true);
    expect(result.peopleLoadFailed).toBe(false);
  });

  function resolve(): Promise<ResponsiblesMaintenanceResolvedData> {
    return firstValueFrom(
      TestBed.runInInjectionContext(
        () =>
          responsiblesMaintenanceResolver(
            {} as ActivatedRouteSnapshot,
            {} as RouterStateSnapshot,
          ) as Observable<ResponsiblesMaintenanceResolvedData>,
      ),
    );
  }
});

describe('responsiblesMaintenanceResolver HTTP cache', () => {
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('reuses the initial combined catalog on subsequent route entries', async () => {
    const resolve = () =>
      firstValueFrom(
        TestBed.runInInjectionContext(
          () =>
            responsiblesMaintenanceResolver(
              {} as ActivatedRouteSnapshot,
              {} as RouterStateSnapshot,
            ) as Observable<ResponsiblesMaintenanceResolvedData>,
        ),
      );
    const firstEntry = resolve();
    const combined = { database: page([]), soffid: page([]) };
    const request = http.expectOne((request) => request.url === '/invaiapi/interna/person/all');
    expect(request.request.params.get('page')).toBe('0');
    expect(request.request.params.get('size')).toBe('20');
    expect(request.request.params.getAll('sort')).toEqual(['firstName,asc', 'lastName,asc']);
    expect(request.request.params.has('search')).toBe(false);
    request.flush(combined);
    const otherRequests = http.match((request) => request.method === 'GET');
    expect(otherRequests).toHaveLength(5);
    otherRequests.forEach((request) => request.flush(page([])));
    const initial = await firstEntry;

    const nextEntry = resolve();
    http.expectNone((request) => request.method === 'GET');
    const next = await nextEntry;
    expect(initial.transferPeopleSearch).toEqual(combined);
    expect(next.transferPeopleSearch).toEqual(combined);
    expect(next.transferPeopleSearchFailed).toBe(false);
  });
});

function page<T>(content: T[]): SpringPage<T> {
  return { content, totalElements: content.length } as SpringPage<T>;
}
