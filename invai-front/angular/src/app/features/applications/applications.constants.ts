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
  {
    key: 'name',
    wrap: true,
    maxWidth: '24rem',
    label: $localize`Aplicació`,
    sortBy: 'name',
    minWidth: '7rem',
  },
  {
    key: 'category',
    wrap: true,
    maxWidth: '24rem',
    label: $localize`Categoria`,
    sortBy: 'category.name',
    minWidth: '8rem',
  },
  {
    key: 'informationSystem',
    wrap: true,
    maxWidth: '24rem',
    label: $localize`Sistema d'informació`,
    sortBy: 'systemType.name',
    minWidth: '10.5rem',
  },
  {
    key: 'scope',
    wrap: true,
    maxWidth: '24rem',
    label: $localize`Àmbit`,
    sortBy: 'field.name',
    minWidth: '8.5rem',
  },
  {
    key: 'administrativeUnit',
    wrap: true,
    maxWidth: '24rem',
    label: $localize`Ut. Administrativa`,
    sortBy: 'admUnitCode',
    minWidth: '10.5rem',
  },
  {
    key: 'commission',
    wrap: true,
    maxWidth: '24rem',
    label: $localize`Comissió informàtica`,
    sortBy: 'csCommission.name',
    minWidth: '10.5rem',
  },
  { key: 'status', label: $localize`Estat`, sortBy: 'status.name', minWidth: '7rem' },
  {
    key: 'environment',
    wrap: true,
    maxWidth: '24rem',
    label: $localize`Entorn`,
    minWidth: '8rem',
  },
  {
    key: 'database',
    wrap: true,
    maxWidth: '24rem',
    label: $localize`Bases de dades`,
    minWidth: '11rem',
  },
  {
    key: 'server',
    wrap: true,
    maxWidth: '24rem',
    label: $localize`Servidor`,
    minWidth: '11rem',
  },
  {
    key: 'responsible',
    wrap: true,
    maxWidth: '24rem',
    label: $localize`Responsables`,
    minWidth: '11rem',
  },
];

export const APPLICATION_SERVERS_TABLE_COLUMNS: KeyLabel[] = [
  {
    key: 'environment',
    width: '10%',
    wrap: true,
    maxWidth: '24rem',
    label: $localize`Entorn`,
    sortBy: 'system.server.environment.name',
    minWidth: '8rem',
  },
  {
    key: 'server',
    width: '22%',
    wrap: true,
    maxWidth: '24rem',
    label: $localize`Servidor`,
    sortBy: 'system.server.name',
    minWidth: '17rem',
  },
  {
    key: 'instance',
    width: '12%',
    label: $localize`Instància`,
    sortBy: 'system.instance',
    minWidth: '10rem',
  },
  { key: 'port', width: '6%', label: $localize`Port`, sortBy: 'system.port', minWidth: '6rem' },
  {
    key: 'version',
    width: '10%',
    label: $localize`Versió`,
    sortBy: 'system.version',
    minWidth: '10rem',
  },
  { key: 'status', width: '8%', label: $localize`Estat`, sortBy: 'deletedAt', minWidth: '8rem' },
  {
    key: 'observations',
    width: '32%',
    wrap: true,
    maxWidth: '32rem',
    label: $localize`Observacions`,
    sortBy: 'system.description',
    minWidth: '10rem',
  },
];

export const APPLICATION_DATABASES_TABLE_COLUMNS: KeyLabel[] = [
  {
    key: 'environment',
    width: '10%',
    wrap: true,
    maxWidth: '24rem',
    label: $localize`Entorn`,
    sortBy: 'database.server.environment.name',
    minWidth: '8rem',
  },
  {
    key: 'server',
    width: '22%',
    wrap: true,
    maxWidth: '24rem',
    label: $localize`Servidor`,
    sortBy: 'database.server.name',
    minWidth: '17rem',
  },
  {
    key: 'database',
    width: '14%',
    wrap: true,
    maxWidth: '24rem',
    label: $localize`Base de dades`,
    sortBy: 'database.service',
    minWidth: '11rem',
  },
  { key: 'port', width: '6%', label: $localize`Port`, sortBy: 'database.port', minWidth: '6rem' },
  {
    key: 'type',
    width: '8%',
    label: $localize`Tipus`,
    sortBy: 'database.databaseType.name',
    minWidth: '7rem',
  },
  { key: 'status', width: '8%', label: $localize`Estat`, sortBy: 'deletedAt', minWidth: '8rem' },
  {
    key: 'observations',
    width: '32%',
    wrap: true,
    maxWidth: '32rem',
    label: $localize`Observacions`,
    sortBy: 'database.description',
    minWidth: '10rem',
  },
];

export const APPLICATION_SYSTEM_CATALOG_COLUMNS: KeyLabel[] = [
  {
    key: 'server',
    width: '24%',
    wrap: true,
    maxWidth: '24rem',
    label: $localize`Servidor`,
    sortBy: 'server.name',
    minWidth: '15rem',
  },
  {
    key: 'environment',
    width: '12%',
    wrap: true,
    maxWidth: '24rem',
    label: $localize`Entorn`,
    sortBy: 'server.environment.name',
    minWidth: '10rem',
  },
  {
    key: 'instance',
    width: '14%',
    label: $localize`Instància`,
    sortBy: 'instance',
    minWidth: '9rem',
  },
  { key: 'port', width: '7%', label: $localize`Port`, sortBy: 'port', minWidth: '6rem' },
  { key: 'version', width: '10%', label: $localize`Versió`, sortBy: 'version', minWidth: '8rem' },
  {
    key: 'description',
    width: '33%',
    wrap: true,
    maxWidth: '32rem',
    label: $localize`Observacions`,
    sortBy: 'description',
    minWidth: '12rem',
  },
];

export const APPLICATION_DATABASE_CATALOG_COLUMNS: KeyLabel[] = [
  {
    key: 'server',
    width: '24%',
    wrap: true,
    maxWidth: '24rem',
    label: $localize`Servidor de BD`,
    sortBy: 'server.name',
    minWidth: '15rem',
  },
  {
    key: 'environment',
    width: '12%',
    wrap: true,
    maxWidth: '24rem',
    label: $localize`Entorn`,
    sortBy: 'server.environment.name',
    minWidth: '10rem',
  },
  {
    key: 'service',
    width: '15%',
    wrap: true,
    maxWidth: '24rem',
    label: $localize`Servei / SID`,
    sortBy: 'service',
    minWidth: '10rem',
  },
  { key: 'port', width: '7%', label: $localize`Port`, sortBy: 'port', minWidth: '6rem' },
  {
    key: 'databaseType',
    width: '10%',
    label: $localize`Proveïdor`,
    sortBy: 'databaseType.name',
    minWidth: '9rem',
  },
  {
    key: 'description',
    width: '32%',
    wrap: true,
    maxWidth: '32rem',
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
    department: 'Conselleria',
    status: ApplicationStatus.ACTIVE,
    description: 'Aplicació interna',
    creationDate: '19/03/2024',
    modificationDate: '19/03/2025',
    withdrawalDate: '19/03/2026',
    appResponsibleAuthorizedId: null,
    incomplete: false,
    missingResponsibleTypes: false,
    missingAuthorized: false,
    missingDevelopmentFields: false,
    missingSystems: false,
    missingDatabases: false,
    missingAccessibilityFields: false,
    missingSecurityData: false,
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
    department: 'Conselleria',
    status: ApplicationStatus.ACTIVE,
    description: 'Gestió funcional',
    creationDate: '19/03/2024',
    modificationDate: '19/03/2025',
    withdrawalDate: '',
    appResponsibleAuthorizedId: null,
    incomplete: false,
    missingResponsibleTypes: false,
    missingAuthorized: false,
    missingDevelopmentFields: false,
    missingSystems: false,
    missingDatabases: false,
    missingAccessibilityFields: false,
    missingSecurityData: false,
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
    department: 'Conselleria',
    status: ApplicationStatus.ACTIVE,
    description: 'Aplicació corporativa',
    creationDate: '19/03/2024',
    modificationDate: '19/03/2025',
    withdrawalDate: '',
    appResponsibleAuthorizedId: null,
    incomplete: false,
    missingResponsibleTypes: false,
    missingAuthorized: false,
    missingDevelopmentFields: false,
    missingSystems: false,
    missingDatabases: false,
    missingAccessibilityFields: false,
    missingSecurityData: false,
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
