import { KeyLabel } from '@models/table.model';
import { CommissionType } from '@features/commissions/commissions.model';
import {
  Application,
  ApplicationInfrastructureStatus,
  ApplicationStatus,
  SelectOption,
} from './applications.model';

export const APPLICATION_STATUS_LABELS: Record<ApplicationStatus, string> = {
  [ApplicationStatus.ACTIVE]: $localize`Actiu`,
  [ApplicationStatus.INACTIVE]: $localize`Inactiu`,
};

export const APPLICATION_STATUS_ACTIVE_ID = ApplicationStatus.ACTIVE;

export const APPLICATION_COMMISSION_TYPE_LABELS: Record<CommissionType, string> = {
  [CommissionType.TECNICA]: $localize`Tècnica`,
  [CommissionType.SUPERIOR]: $localize`Superior`,
};

export const APPLICATION_STATUS_OPTIONS: SelectOption<ApplicationStatus>[] = [
  {
    label: APPLICATION_STATUS_LABELS[ApplicationStatus.ACTIVE],
    value: ApplicationStatus.ACTIVE,
  },
  {
    label: APPLICATION_STATUS_LABELS[ApplicationStatus.INACTIVE],
    value: ApplicationStatus.INACTIVE,
  },
];

export const APPLICATIONS_TABLE_COLUMNS: KeyLabel[] = [
  { key: 'code', label: $localize`Codi`, sortBy: 'code', minWidth: '5.5rem' },
  { key: 'prefix', label: $localize`Prefix`, sortBy: 'prefix', minWidth: '6rem' },
  { key: 'name', label: $localize`Aplicació`, sortBy: 'name', minWidth: '7rem' },
  {
    key: 'category',
    label: $localize`Categoria`,
    sortBy: 'category.name',
    minWidth: '8rem',
  },
  {
    key: 'informationSystem',
    label: $localize`Sistema d'informació`,
    sortBy: 'systemType.name',
    minWidth: '10.5rem',
  },
  { key: 'scope', label: $localize`Àmbit`, sortBy: 'field.name', minWidth: '8.5rem' },
  {
    key: 'administrativeUnit',
    label: $localize`Ut. Administrativa`,
    sortBy: 'admUnit.name',
    minWidth: '10.5rem',
  },
  {
    key: 'commission',
    label: $localize`Comissió informàtica`,
    sortBy: 'csCommission.name',
    minWidth: '10.5rem',
  },
  { key: 'status', label: $localize`Estat`, sortBy: 'status.name', minWidth: '7rem' },
  { key: 'environment', label: $localize`Entorn`, minWidth: '8rem' },
  { key: 'database', label: $localize`Bases de dades`, minWidth: '11rem' },
  { key: 'server', label: $localize`Servidor`, minWidth: '11rem' },
  { key: 'responsible', label: $localize`Responsables`, minWidth: '11rem' },
];

export const APPLICATION_SERVERS_TABLE_COLUMNS: KeyLabel[] = [
  {
    key: 'environment',
    label: $localize`Entorn`,
    sortBy: 'system.server.environment.name',
    minWidth: '8rem',
  },
  {
    key: 'server',
    label: $localize`Servidor`,
    sortBy: 'system.server.name',
    minWidth: '17rem',
  },
  {
    key: 'instance',
    label: $localize`Instància`,
    sortBy: 'system.instance',
    minWidth: '10rem',
  },
  { key: 'port', label: $localize`Port`, sortBy: 'system.port', minWidth: '6rem' },
  { key: 'version', label: $localize`Versió`, sortBy: 'system.version', minWidth: '10rem' },
  { key: 'status', label: $localize`Estat`, sortBy: 'deletedAt', minWidth: '8rem' },
  {
    key: 'observations',
    label: $localize`Observacions`,
    sortBy: 'system.description',
    minWidth: '10rem',
  },
];

export const APPLICATION_DATABASES_TABLE_COLUMNS: KeyLabel[] = [
  {
    key: 'environment',
    label: $localize`Entorn`,
    sortBy: 'database.server.environment.name',
    minWidth: '8rem',
  },
  {
    key: 'server',
    label: $localize`Servidor`,
    sortBy: 'database.server.name',
    minWidth: '17rem',
  },
  {
    key: 'database',
    label: $localize`Base de dades`,
    sortBy: 'database.service',
    minWidth: '11rem',
  },
  { key: 'port', label: $localize`Port`, sortBy: 'database.port', minWidth: '6rem' },
  {
    key: 'type',
    label: $localize`Tipus`,
    sortBy: 'database.databaseType.name',
    minWidth: '7rem',
  },
  { key: 'status', label: $localize`Estat`, sortBy: 'deletedAt', minWidth: '8rem' },
  {
    key: 'observations',
    label: $localize`Observacions`,
    sortBy: 'database.description',
    minWidth: '10rem',
  },
];

export const APPLICATION_SYSTEM_CATALOG_COLUMNS: KeyLabel[] = [
  { key: 'server', label: $localize`Servidor`, sortBy: 'server.name', minWidth: '15rem' },
  {
    key: 'environment',
    label: $localize`Entorn`,
    sortBy: 'server.environment.name',
    minWidth: '10rem',
  },
  { key: 'instance', label: $localize`Instància`, sortBy: 'instance', minWidth: '9rem' },
  { key: 'port', label: $localize`Port`, sortBy: 'port', minWidth: '6rem' },
  { key: 'version', label: $localize`Versió`, sortBy: 'version', minWidth: '8rem' },
  {
    key: 'description',
    label: $localize`Observacions`,
    sortBy: 'description',
    minWidth: '12rem',
  },
];

export const APPLICATION_DATABASE_CATALOG_COLUMNS: KeyLabel[] = [
  {
    key: 'server',
    label: $localize`Servidor de BD`,
    sortBy: 'server.name',
    minWidth: '15rem',
  },
  {
    key: 'environment',
    label: $localize`Entorn`,
    sortBy: 'server.environment.name',
    minWidth: '10rem',
  },
  { key: 'service', label: $localize`Servei / SID`, sortBy: 'service', minWidth: '10rem' },
  { key: 'port', label: $localize`Port`, sortBy: 'port', minWidth: '6rem' },
  {
    key: 'databaseType',
    label: $localize`Proveïdor`,
    sortBy: 'databaseType.name',
    minWidth: '9rem',
  },
  {
    key: 'description',
    label: $localize`Observacions`,
    sortBy: 'description',
    minWidth: '12rem',
  },
];

export const APPLICATION_INFRASTRUCTURE_STATUS_LABELS: Record<
  ApplicationInfrastructureStatus,
  string
> = {
  [ApplicationInfrastructureStatus.ACTIVE]: $localize`Actiu`,
  [ApplicationInfrastructureStatus.INACTIVE]: $localize`Inactiu`,
};

export const APPLICATION_INFRASTRUCTURE_STATUS_OPTIONS: SelectOption<ApplicationInfrastructureStatus>[] =
  [
    {
      label: APPLICATION_INFRASTRUCTURE_STATUS_LABELS[ApplicationInfrastructureStatus.ACTIVE],
      value: ApplicationInfrastructureStatus.ACTIVE,
    },
    {
      label: APPLICATION_INFRASTRUCTURE_STATUS_LABELS[ApplicationInfrastructureStatus.INACTIVE],
      value: ApplicationInfrastructureStatus.INACTIVE,
    },
  ];

export const APPLICATIONS_SEED_DATA: Application[] = [
  {
    id: '1',
    code: '0000',
    prefix: 'CVF',
    name: 'Invai',
    category: 'DRASSANA',
    informationSystem: 'Instrumental',
    scope: 'Departamental',
    commission: 'Equip directiu',
    administrativeUnit: 'Direcció General',
    status: ApplicationStatus.ACTIVE,
    description: 'Aplicació interna',
    creationDate: '19/03/2024',
    modificationDate: '19/03/2025',
    withdrawalDate: '19/03/2026',
    appResponsibleAuthorizedId: null,
  },
  {
    id: '2',
    code: '0000',
    prefix: 'CVF',
    name: 'Invai',
    category: 'DRASSANA',
    informationSystem: 'Instrumental',
    scope: 'Departamental',
    commission: 'Equip directiu',
    administrativeUnit: 'Direcció General',
    status: ApplicationStatus.ACTIVE,
    description: 'Gestió funcional',
    creationDate: '19/03/2024',
    modificationDate: '19/03/2025',
    withdrawalDate: '',
    appResponsibleAuthorizedId: null,
  },
  {
    id: '3',
    code: '0000',
    prefix: 'CVF',
    name: 'Invai',
    category: 'DRASSANA',
    informationSystem: 'Instrumental',
    scope: 'Departamental',
    commission: 'Equip directiu',
    administrativeUnit: 'Direcció General',
    status: ApplicationStatus.ACTIVE,
    description: 'Aplicació corporativa',
    creationDate: '19/03/2024',
    modificationDate: '19/03/2025',
    withdrawalDate: '',
    appResponsibleAuthorizedId: null,
  },
];

export const APPLICATION_CATEGORY_OPTIONS: SelectOption[] = [
  { label: $localize`DRASSANA`, value: 'DRASSANA' },
];

export const APPLICATION_INFORMATION_SYSTEM_OPTIONS: SelectOption[] = [
  { label: $localize`Instrumental`, value: 'Instrumental' },
];

export const APPLICATION_SCOPE_OPTIONS: SelectOption[] = [
  { label: $localize`Departamental`, value: 'Departamental' },
];

export const APPLICATION_ADMINISTRATIVE_UNIT_OPTIONS: SelectOption[] = [
  { label: $localize`DGEDOT`, value: 'DGEDOT' },
  { label: $localize`Direcció General`, value: 'Direcció General' },
];

export const APPLICATION_CONSELLERIA_MOCK_VALUE = $localize`Conselleria d'Educació, Universitats i Ocupació`;
