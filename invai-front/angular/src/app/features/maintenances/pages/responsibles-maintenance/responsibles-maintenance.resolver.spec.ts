import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { WritableSignal, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import {
  ALL_USER_PERMISSIONS,
  NO_USER_PERMISSIONS,
  UserPermissions,
} from '@core/auth/user-permissions.model';
import { provideUserPermissions } from '@core/auth/user-permissions.testing';
import { OAuthService } from '@core/services/auth.service';
import { SpringPage } from '@models/page.model';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';
import { Observable, firstValueFrom, of, throwError } from 'rxjs';

import { ROLE_TRANSFER_PERSON_SEARCH_PARAMS } from '../../responsibles/responsibles.constants';
import { ResponsiblePersonPageParams } from '../../responsibles/responsibles.model';
import { ResponsibleAuthorizationTypesService } from '../../responsibles/services/responsible-authorization-types.service';
import { ResponsibleCompaniesService } from '../../responsibles/services/responsible-companies.service';
import { ResponsiblePeopleService } from '../../responsibles/services/responsible-people.service';
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
  let permissions: WritableSignal<UserPermissions>;

  beforeEach(() => {
    vi.clearAllMocks();
    permissions = signal<UserPermissions>(ALL_USER_PERMISSIONS);
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
        { provide: OAuthService, useValue: { permissions } },
      ],
    });
  });

  it('loads the first page for every initial surface, including role transfer', async () => {
    const result = await resolve();

    expect(people.getPage).toHaveBeenCalledTimes(1);
    expect(people.getPage).toHaveBeenCalledWith({
      personalCaib: false,
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

  it('does not load the transfer catalog for a user who is not INV_SUPER', async () => {
    permissions.set({ ...NO_USER_PERMISSIONS, responsibleRead: true, responsibleWrite: true });

    const result = await resolve();

    expect(people.searchCombined).not.toHaveBeenCalled();
    expect(result.transferPeopleSearch).toBeNull();
    expect(result.transferPeopleSearchFailed).toBe(false);
    expect(people.getPage).toHaveBeenCalledTimes(1);
  });

  it('loads neither the people nor the transfer catalog for a user who cannot change the responsibles', async () => {
    permissions.set({ ...NO_USER_PERMISSIONS, responsibleRead: true });

    const result = await resolve();

    expect(people.getPage).not.toHaveBeenCalled();
    expect(people.searchCombined).not.toHaveBeenCalled();
    expect(result.peoplePage).toBeNull();
    expect(result.peopleLoadFailed).toBe(false);
    expect(result.transferPeopleSearchFailed).toBe(false);
    expect(companies.getPage).toHaveBeenCalledTimes(1);
    expect(authorizations.getPage).toHaveBeenCalledTimes(1);
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
      providers: [provideHttpClient(), provideHttpClientTesting(), provideUserPermissions()],
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
    const request = http.expectOne((request) => request.url === '/invaiback/person/all');
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
