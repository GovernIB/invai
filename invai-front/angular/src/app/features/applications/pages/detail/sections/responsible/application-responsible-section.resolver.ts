import { inject } from '@angular/core';
import { ResolveFn } from '@angular/router';
import { SpringPage } from '@models/page.model';
import { SoftDeleteStatus } from '@models/soft-delete-status.model';
import { Observable, catchError, forkJoin, map, of } from 'rxjs';

import {
  ResponsibleAuthorization,
  ResponsibleCompanyOption,
  ResponsiblePerson,
  ResponsibleType,
} from '../../../../../maintenances/responsibles/responsibles.model';
import { ResponsibleAuthorizationTypesService } from '../../../../../maintenances/responsibles/services/responsible-authorization-types.service';
import { ResponsibleCompaniesService } from '../../../../../maintenances/responsibles/services/responsible-companies.service';
import { ResponsiblePeopleService } from '../../../../../maintenances/responsibles/services/responsible-people.service';
import { ResponsibleTypesService } from '../../../../../maintenances/responsibles/services/responsible-types.service';

import {
  ApplicationAuthorizedOutput,
  ApplicationResponsibleOutput,
} from '../../../../applications.model';
import { ApplicationAuthorizedService } from '../../../../services/application-authorized.service';
import { ApplicationResponsiblesService } from '../../../../services/application-responsibles.service';
import {
  APPLICATION_DETAIL_RESOLVE_KEY,
  ApplicationDetailResolvedData,
} from '../../application-detail.resolver';

export const APPLICATION_RESPONSIBLE_RESOLVE_KEY = 'applicationResponsible';

export interface ApplicationResponsibleResolvedData {
  anchorId: number | null;
  available: boolean;
  responsiblesPage: SpringPage<ApplicationResponsibleOutput> | null;
  responsiblesLoadFailed: boolean;
  authorizedPage: SpringPage<ApplicationAuthorizedOutput> | null;
  authorizedLoadFailed: boolean;
  responsibleTypes: ResponsibleType[];
  responsibleTypesLoadFailed: boolean;
  companyOptions: ResponsibleCompanyOption[];
  companyOptionsLoadFailed: boolean;
  people: ResponsiblePerson[];
  peopleLoadFailed: boolean;
  authorizationTypes: ResponsibleAuthorization[];
  authorizationTypesLoadFailed: boolean;
}

const result = <T>(source: Observable<T>) =>
  source.pipe(
    map((value) => ({ value, failed: false })),
    catchError(() => of({ value: null, failed: true })),
  );

export const applicationResponsibleResolver: ResolveFn<ApplicationResponsibleResolvedData> = (
  route,
) => {
  const detail = route.parent?.data[APPLICATION_DETAIL_RESOLVE_KEY] as
    ApplicationDetailResolvedData | undefined;
  const value = detail?.application?.appResponsibleAuthorizedId;
  const anchorId = Number.isInteger(value) && Number(value) > 0 ? Number(value) : null;
  if (anchorId === null) return of(emptyResolvedData());

  const initialPage = {
    appResponsibleAuthorizedId: anchorId,
    page: 0,
    size: 10,
    sort: 'id,asc',
    statusId: SoftDeleteStatus.ACTIVE,
  } as const;
  const allResponsiblesPage = { ...initialPage, size: 1000 } as const;
  return forkJoin({
    responsibles: result(inject(ApplicationResponsiblesService).getPage(allResponsiblesPage)),
    authorized: result(inject(ApplicationAuthorizedService).getPage(initialPage)),
    responsibleTypes: result(inject(ResponsibleTypesService).getAll()),
    companyOptions: result(inject(ResponsibleCompaniesService).getOptions(true)),
    people: result(
      inject(ResponsiblePeopleService).getPage({
        page: 0,
        size: 1000,
        sort: ['firstName,asc', 'lastName,asc'],
        statusId: SoftDeleteStatus.ACTIVE,
      }),
    ),
    authorizationTypes: result(
      inject(ResponsibleAuthorizationTypesService).getPage({
        page: 0,
        size: 1000,
        sort: 'name,asc',
        statusId: SoftDeleteStatus.ACTIVE,
      }),
    ),
  }).pipe(
    map(
      ({
        responsibles,
        authorized,
        responsibleTypes,
        companyOptions,
        people,
        authorizationTypes,
      }) => ({
        anchorId,
        available: true,
        responsiblesPage: responsibles.value,
        responsiblesLoadFailed: responsibles.failed,
        authorizedPage: authorized.value,
        authorizedLoadFailed: authorized.failed,
        responsibleTypes: responsibleTypes.value ?? [],
        responsibleTypesLoadFailed: responsibleTypes.failed,
        companyOptions: companyOptions.value ?? [],
        companyOptionsLoadFailed: companyOptions.failed,
        people: people.value?.content ?? [],
        peopleLoadFailed: people.failed,
        authorizationTypes: authorizationTypes.value?.content ?? [],
        authorizationTypesLoadFailed: authorizationTypes.failed,
      }),
    ),
  );
};

function emptyResolvedData(): ApplicationResponsibleResolvedData {
  return {
    anchorId: null,
    available: false,
    responsiblesPage: null,
    responsiblesLoadFailed: false,
    authorizedPage: null,
    authorizedLoadFailed: false,
    responsibleTypes: [],
    responsibleTypesLoadFailed: false,
    companyOptions: [],
    companyOptionsLoadFailed: false,
    people: [],
    peopleLoadFailed: false,
    authorizationTypes: [],
    authorizationTypesLoadFailed: false,
  };
}
