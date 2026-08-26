import { KeyLabel } from '@models/table.model';

export const RESPONSIBLE_COMPANY_COLUMNS: KeyLabel[] = [
  { key: 'name', label: $localize`Empresa`, sortBy: 'name', minWidth: '16rem' },
  { key: 'status', label: $localize`Estat`, sortBy: 'deletedAt', minWidth: '8rem' },
];

export const RESPONSIBLE_PERSON_COLUMNS: KeyLabel[] = [
  { key: 'company', label: $localize`Empresa`, sortBy: 'company.name', minWidth: '14rem' },
  { key: 'firstName', label: $localize`Nom`, sortBy: 'firstName', minWidth: '11rem' },
  { key: 'lastName', label: $localize`Llinatges`, sortBy: 'lastName', minWidth: '14rem' },
  { key: 'email', label: $localize`Correu electrònic`, sortBy: 'email', minWidth: '16rem' },
  { key: 'status', label: $localize`Estat`, sortBy: 'deletedAt', minWidth: '8rem' },
];

export const RESPONSIBLE_AUTHORIZATION_COLUMNS: KeyLabel[] = [
  { key: 'name', label: $localize`Autorització (català)`, sortBy: 'name', minWidth: '18rem' },
  { key: 'nameEs', label: $localize`Autorització (castellà)`, sortBy: 'nameEs', minWidth: '18rem' },
  { key: 'status', label: $localize`Estat`, sortBy: 'deletedAt', minWidth: '8rem' },
];
