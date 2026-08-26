import { SoftDeleteStatus } from '@models/soft-delete-status.model';
import { TableLazyLoadEvent } from 'primeng/table';

import {
  ResponsibleNameFilters,
  ResponsibleNamePageParams,
  ResponsibleAuthorizationFilters,
  ResponsibleAuthorizationPageParams,
  ResponsiblePersonFilters,
  ResponsiblePerson,
  ResponsiblePersonOption,
  ResponsiblePersonPageParams,
} from './responsibles.model';

const DEFAULT_PAGE_SIZE = 10;

export function toResponsibleNamePageParams(
  filters: ResponsibleNameFilters,
  event: TableLazyLoadEvent | undefined,
  search: string,
): ResponsibleNamePageParams {
  return {
    ...pageParams(event),
    name: filters.name?.trim() || undefined,
    statusId: statusParam(filters.status),
    search: search.trim() || undefined,
  };
}

export function responsiblePersonFullName(
  person: Pick<ResponsiblePerson, 'firstName' | 'lastName'>,
): string {
  return `${person.firstName} ${person.lastName}`.replace(/\s+/g, ' ').trim();
}

export function toResponsiblePersonOption(person: ResponsiblePerson): ResponsiblePersonOption {
  return { id: person.id, label: responsiblePersonFullName(person) };
}

export function toResponsiblePersonPageParams(
  filters: ResponsiblePersonFilters,
  event: TableLazyLoadEvent | undefined,
  search: string,
): ResponsiblePersonPageParams {
  return {
    ...pageParams(event),
    companyId: filters.companyId ?? undefined,
    firstName: filters.firstName?.trim() || undefined,
    lastName: filters.lastName?.trim() || undefined,
    email: filters.email?.trim() || undefined,
    statusId: statusParam(filters.status),
    search: search.trim() || undefined,
  };
}

export function toResponsibleAuthorizationPageParams(
  filters: ResponsibleAuthorizationFilters,
  event: TableLazyLoadEvent | undefined,
  search: string,
): ResponsibleAuthorizationPageParams {
  return {
    ...toResponsibleNamePageParams(filters, event, search),
    nameEs: filters.nameEs?.trim() || undefined,
  };
}

function pageParams(event?: TableLazyLoadEvent) {
  const first = event?.first ?? 0;
  const size = event?.rows ?? DEFAULT_PAGE_SIZE;
  const sortField = Array.isArray(event?.sortField) ? event.sortField[0] : event?.sortField;
  const sortDirection = event?.sortOrder === -1 ? 'desc' : event?.sortOrder === 1 ? 'asc' : null;
  return {
    page: Math.floor(first / size),
    size,
    sort: sortField && sortDirection ? `${sortField},${sortDirection}` : undefined,
  };
}

function statusParam(status: SoftDeleteStatus | null): SoftDeleteStatus | undefined {
  return status ?? undefined;
}
