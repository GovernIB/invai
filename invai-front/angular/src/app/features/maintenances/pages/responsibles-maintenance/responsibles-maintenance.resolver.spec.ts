import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { SpringPage } from '@models/page.model';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';
import { Observable, firstValueFrom, of, throwError } from 'rxjs';

import { ResponsibleAuthorizationTypesService } from '../../responsibles/services/responsible-authorization-types.service';
import { ResponsibleCompaniesService } from '../../responsibles/services/responsible-companies.service';
import { ResponsiblePeopleService } from '../../responsibles/services/responsible-people.service';
import { ResponsiblePersonPageParams } from '../../responsibles/responsibles.model';
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
  };
  const authorizations = { getPage: vi.fn(() => of(page([]))) };

  beforeEach(() => {
    vi.clearAllMocks();
    companies.getPage.mockReturnValue(of(page([])));
    companies.getOptions.mockReturnValue(of([]));
    people.getPage.mockReturnValue(of(page([])));
    authorizations.getPage.mockReturnValue(of(page([])));
    TestBed.configureTestingModule({
      providers: [
        { provide: ResponsibleCompaniesService, useValue: companies },
        { provide: ResponsiblePeopleService, useValue: people },
        { provide: ResponsibleAuthorizationTypesService, useValue: authorizations },
      ],
    });
  });

  it('loads the maintenance pages and the complete active source catalog', async () => {
    const result = await resolve();

    expect(result.transferPeopleLoadFailed).toBe(false);
    expect(people.getPage).toHaveBeenCalledWith({
      page: 0,
      size: 1000,
      sort: ['firstName,asc', 'lastName,asc'],
      statusId: SoftDeleteStatus.ACTIVE,
    });
    expect(people.getPage).toHaveBeenCalledWith({
      page: 0,
      size: 10,
      sort: 'id,asc',
      statusId: SoftDeleteStatus.ACTIVE,
    });
  });

  it('degrades only the transfer catalog when that request fails', async () => {
    people.getPage.mockImplementation((params: ResponsiblePersonPageParams) =>
      params.size === 1000 ? throwError(() => new Error('Unavailable')) : of(page([])),
    );

    const result = await resolve();

    expect(result.peopleLoadFailed).toBe(false);
    expect(result.transferPeoplePage).toBeNull();
    expect(result.transferPeopleLoadFailed).toBe(true);
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

function page<T>(content: T[]): SpringPage<T> {
  return { content, totalElements: content.length } as SpringPage<T>;
}
