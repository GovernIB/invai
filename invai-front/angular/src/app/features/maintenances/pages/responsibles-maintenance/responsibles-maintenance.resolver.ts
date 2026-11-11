import { inject } from '@angular/core';
import { ResolveFn } from '@angular/router';
import { OAuthService } from '@core/services/auth.service';
import { SpringPage } from '@models/page.model';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';
import { Observable, catchError, forkJoin, map, of } from 'rxjs';

import { ROLE_TRANSFER_PERSON_SEARCH_PARAMS } from '../../responsibles/responsibles.constants';
import {
  ResponsibleAuthorization,
  ResponsibleCompany,
  ResponsibleCompanyOption,
  ResponsiblePerson,
  ResponsiblePersonCombinedSearchOutput,
} from '../../responsibles/responsibles.model';
import { ResponsibleAuthorizationTypesService } from '../../responsibles/services/responsible-authorization-types.service';
import { ResponsibleCompaniesService } from '../../responsibles/services/responsible-companies.service';
import { ResponsiblePeopleService } from '../../responsibles/services/responsible-people.service';
import { canOpenResponsiblesPanel } from './responsibles-maintenance.permissions';

export const RESPONSIBLES_MAINTENANCE_RESOLVE_KEY = 'responsiblesMaintenance';

export interface ResponsiblesMaintenanceResolvedData {
  companiesPage: SpringPage<ResponsibleCompany> | null;
  companiesLoadFailed: boolean;
  peoplePage: SpringPage<ResponsiblePerson> | null;
  peopleLoadFailed: boolean;
  transferPeopleSearch: ResponsiblePersonCombinedSearchOutput | null;
  transferPeopleSearchFailed: boolean;
  authorizationsPage: SpringPage<ResponsibleAuthorization> | null;
  authorizationsLoadFailed: boolean;
  activeCompanyOptions: ResponsibleCompanyOption[];
  activeCompanyOptionsLoadFailed: boolean;
  allCompanyOptions: ResponsibleCompanyOption[];
  allCompanyOptionsLoadFailed: boolean;
}

const INITIAL_PARAMS = {
  page: 0,
  size: 10,
  sort: 'id,asc',
  statusId: SoftDeleteStatus.ACTIVE,
} as const;

interface LoadResult<T> {
  value: T | null;
  failed: boolean;
}

function result<T>(source: Observable<T>): Observable<LoadResult<T>> {
  return source.pipe(
    map((value) => ({ value, failed: false })),
    catchError(() => of({ value: null, failed: true })),
  );
}

// Loads the data of a panel only if the user can open it; otherwise the back-end would answer
// with HTTP 403 for a panel that isn't shown.
function panelResult<T>(open: boolean, load: () => Observable<T>): Observable<LoadResult<T>> {
  return open ? result(load()) : of({ value: null, failed: false });
}

export const responsiblesMaintenanceResolver: ResolveFn<
  ResponsiblesMaintenanceResolvedData
> = () => {
  const companiesService = inject(ResponsibleCompaniesService);
  const peopleService = inject(ResponsiblePeopleService);
  const permissions = inject(OAuthService).permissions();
  return forkJoin({
    companies: result(companiesService.getPage(INITIAL_PARAMS)),
    people: panelResult(canOpenResponsiblesPanel(permissions, 'people'), () =>
      peopleService.getPage({ ...INITIAL_PARAMS, personalCaib: false }),
    ),
    transferPeople: panelResult(canOpenResponsiblesPanel(permissions, 'role-transfer'), () =>
      peopleService.searchCombined(ROLE_TRANSFER_PERSON_SEARCH_PARAMS),
    ),
    authorizations: result(inject(ResponsibleAuthorizationTypesService).getPage(INITIAL_PARAMS)),
    activeCompanies: result(companiesService.getOptions(true)),
    allCompanies: result(companiesService.getOptions(false)),
  }).pipe(
    map(({ companies, people, transferPeople, authorizations, activeCompanies, allCompanies }) => ({
      companiesPage: companies.value,
      companiesLoadFailed: companies.failed,
      peoplePage: people.value,
      peopleLoadFailed: people.failed,
      transferPeopleSearch: transferPeople.value,
      transferPeopleSearchFailed: transferPeople.failed,
      authorizationsPage: authorizations.value,
      authorizationsLoadFailed: authorizations.failed,
      activeCompanyOptions: activeCompanies.value ?? [],
      activeCompanyOptionsLoadFailed: activeCompanies.failed,
      allCompanyOptions: allCompanies.value ?? [],
      allCompanyOptionsLoadFailed: allCompanies.failed,
    })),
  );
};
