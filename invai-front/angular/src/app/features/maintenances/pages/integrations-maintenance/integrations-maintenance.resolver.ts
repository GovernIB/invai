import { inject } from '@angular/core';
import { ResolveFn } from '@angular/router';
import { ResponsibleCompanyOption } from '@features/maintenances/responsibles/responsibles.model';
import { ResponsibleCompaniesService } from '@features/maintenances/responsibles/services/responsible-companies.service';
import { catchError, forkJoin, map, Observable, of } from 'rxjs';

export const INTEGRATIONS_MAINTENANCE_RESOLVE_KEY = 'integrationsMaintenance';

export interface IntegrationsMaintenanceResolvedData {
  // Active companies feed the dialog; every company feeds the filters.
  activeCompanyOptions: ResponsibleCompanyOption[];
  allCompanyOptions: ResponsibleCompanyOption[];
  companyOptionsLoadFailed: boolean;
}

function options(source: Observable<ResponsibleCompanyOption[]>) {
  return source.pipe(
    map((value) => ({ value, failed: false })),
    catchError(() => of({ value: [] as ResponsibleCompanyOption[], failed: true })),
  );
}

export const integrationsMaintenanceResolver: ResolveFn<IntegrationsMaintenanceResolvedData> = () => {
  const companies = inject(ResponsibleCompaniesService);
  return forkJoin({
    active: options(companies.getOptions(true)),
    all: options(companies.getOptions(false)),
  }).pipe(
    map(({ active, all }) => ({
      activeCompanyOptions: active.value,
      allCompanyOptions: all.value,
      companyOptionsLoadFailed: active.failed || all.failed,
    })),
  );
};
