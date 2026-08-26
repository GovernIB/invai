import { inject } from '@angular/core';
import { ResolveFn } from '@angular/router';
import { SpringPage } from '@models/page.model';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';
import { Observable, catchError, forkJoin, map, of } from 'rxjs';

import {
  ResponsibleAuthorization,
  ResponsibleCompany,
  ResponsibleCompanyOption,
  ResponsiblePerson,
} from '../../responsibles/responsibles.model';
import { ResponsibleAuthorizationTypesService } from '../../responsibles/services/responsible-authorization-types.service';
import { ResponsibleCompaniesService } from '../../responsibles/services/responsible-companies.service';
import { ResponsiblePeopleService } from '../../responsibles/services/responsible-people.service';

export const RESPONSIBLES_MAINTENANCE_RESOLVE_KEY = 'responsiblesMaintenance';

export interface ResponsiblesMaintenanceResolvedData {
  companiesPage: SpringPage<ResponsibleCompany> | null;
  companiesLoadFailed: boolean;
  peoplePage: SpringPage<ResponsiblePerson> | null;
  peopleLoadFailed: boolean;
  transferPeoplePage: SpringPage<ResponsiblePerson> | null;
  transferPeopleLoadFailed: boolean;
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

const TRANSFER_PEOPLE_PARAMS = {
  page: 0,
  size: 1000,
  sort: ['firstName,asc', 'lastName,asc'] as string[],
  statusId: SoftDeleteStatus.ACTIVE,
} as const;

function result<T>(source: Observable<T>) {
  return source.pipe(
    map((value) => ({ value, failed: false })),
    catchError(() => of({ value: null, failed: true })),
  );
}

export const responsiblesMaintenanceResolver: ResolveFn<
  ResponsiblesMaintenanceResolvedData
> = () => {
  const companiesService = inject(ResponsibleCompaniesService);
  const peopleService = inject(ResponsiblePeopleService);
  return forkJoin({
    companies: result(companiesService.getPage(INITIAL_PARAMS)),
    people: result(peopleService.getPage(INITIAL_PARAMS)),
    transferPeople: result(peopleService.getPage(TRANSFER_PEOPLE_PARAMS)),
    authorizations: result(inject(ResponsibleAuthorizationTypesService).getPage(INITIAL_PARAMS)),
    activeCompanies: result(companiesService.getOptions(true)),
    allCompanies: result(companiesService.getOptions(false)),
  }).pipe(
    map(({ companies, people, transferPeople, authorizations, activeCompanies, allCompanies }) => ({
      companiesPage: companies.value,
      companiesLoadFailed: companies.failed,
      peoplePage: people.value,
      peopleLoadFailed: people.failed,
      transferPeoplePage: transferPeople.value,
      transferPeopleLoadFailed: transferPeople.failed,
      authorizationsPage: authorizations.value,
      authorizationsLoadFailed: authorizations.failed,
      activeCompanyOptions: activeCompanies.value ?? [],
      activeCompanyOptionsLoadFailed: activeCompanies.failed,
      allCompanyOptions: allCompanies.value ?? [],
      allCompanyOptionsLoadFailed: allCompanies.failed,
    })),
  );
};
