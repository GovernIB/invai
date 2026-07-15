import { KeyLabel } from '@models/table.model';
import {
  Application,
  ApplicationDatabase,
  ApplicationServer,
  SelectOption,
} from './applications.model';

export const APPLICATION_STATUS_ACTIVE_ID = 1;

export const APPLICATION_STATUS_OPTIONS: SelectOption<number>[] = [
  { label: $localize`Actiu`, value: APPLICATION_STATUS_ACTIVE_ID },
  { label: $localize`Manteniment`, value: 2 },
  { label: $localize`Deprecat`, value: 3 },
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
];

export const APPLICATION_SERVERS_TABLE_COLUMNS: KeyLabel[] = [
  { key: 'environment', label: $localize`Entorn`, sortBy: 'environment', minWidth: '8rem' },
  { key: 'server', label: $localize`Servidor`, sortBy: 'server', minWidth: '17rem' },
  { key: 'instance', label: $localize`Instància`, sortBy: 'instance', minWidth: '10rem' },
  { key: 'port', label: $localize`Port`, sortBy: 'port', minWidth: '6rem' },
  { key: 'version', label: $localize`Versió`, sortBy: 'version', minWidth: '10rem' },
  { key: 'status', label: $localize`Estat`, sortBy: 'status', minWidth: '8rem' },
  {
    key: 'observations',
    label: $localize`Observacions`,
    sortBy: 'observations',
    minWidth: '10rem',
  },
];

export const APPLICATION_DATABASES_TABLE_COLUMNS: KeyLabel[] = [
  { key: 'environment', label: $localize`Entorn`, sortBy: 'environment', minWidth: '8rem' },
  { key: 'server', label: $localize`Servidor`, sortBy: 'server', minWidth: '17rem' },
  { key: 'version', label: $localize`Versió`, sortBy: 'version', minWidth: '10rem' },
  {
    key: 'database',
    label: $localize`Base de dades`,
    sortBy: 'database',
    minWidth: '11rem',
  },
  { key: 'service', label: $localize`Servei`, sortBy: 'service', minWidth: '9rem' },
  { key: 'port', label: $localize`Port`, sortBy: 'port', minWidth: '6rem' },
  { key: 'type', label: $localize`Tipus`, sortBy: 'type', minWidth: '7rem' },
  {
    key: 'observations',
    label: $localize`Observacions`,
    sortBy: 'observations',
    minWidth: '10rem',
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
    status: 'Activa',
    description: 'Aplicació interna',
    creationDate: '19/03/2024',
    modificationDate: '19/03/2025',
    withdrawalDate: '19/03/2026',
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
    status: 'Activa',
    description: 'Gestió funcional',
    creationDate: '19/03/2024',
    modificationDate: '19/03/2025',
    withdrawalDate: '',
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
    status: 'Activa',
    description: 'Aplicació corporativa',
    creationDate: '19/03/2024',
    modificationDate: '19/03/2025',
    withdrawalDate: '',
  },
];

export const APPLICATION_SERVERS_SEED_DATA: ApplicationServer[] = [
  {
    id: 'server-1',
    environment: 'Producció',
    server: 'exappdb01.caib.es/bbdd02.caib.es',
    instance: 'PostgreSQL 15',
    port: '5432',
    version: 'invai_svc',
    status: 'Actiu',
    observations: '5432',
  },
  {
    id: 'server-2',
    environment: 'Preproducció',
    server: 'exappdb02.caib.es/bbdd02.caib.es',
    instance: 'PostgreSQL 15',
    port: '5432',
    version: 'invai_svc',
    status: 'Actiu',
    observations: '',
  },
  {
    id: 'server-3',
    environment: 'Desenvolupament',
    server: 'exappdb03.caib.es/bbdd02.caib.es',
    instance: 'PostgreSQL 15',
    port: '5432',
    version: 'invai_svc',
    status: 'Actiu',
    observations: '',
  },
];

export const APPLICATION_DATABASES_SEED_DATA: ApplicationDatabase[] = [
  {
    id: 'database-1',
    environment: 'Producció',
    server: 'exappdb01.caib.es/bbdd02.caib.es',
    version: 'PostgreSQL 15',
    database: 'INVAI_PRD',
    service: 'invai_svc',
    port: '5432',
    type: 'noSQL',
    observations: 'Relacional',
  },
  {
    id: 'database-2',
    environment: 'Preproducció',
    server: 'exappdb02.caib.es/bbdd02.caib.es',
    version: 'PostgreSQL 15',
    database: 'INVAI_PRE',
    service: 'invai_svc',
    port: '5432',
    type: 'noSQL',
    observations: 'Relacional',
  },
  {
    id: 'database-3',
    environment: 'Desenvolupament',
    server: 'exappdb03.caib.es/bbdd02.caib.es',
    version: 'PostgreSQL 15',
    database: 'INVAI_DEV',
    service: 'invai_svc',
    port: '5432',
    type: 'noSQL',
    observations: 'Relacional',
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

export const APPLICATION_COMMISSION_OPTIONS: SelectOption[] = [
  { label: $localize`A04026930`, value: 'A04026930' },
];

export const APPLICATION_ADMINISTRATIVE_UNIT_OPTIONS: SelectOption[] = [
  { label: $localize`DGEDOT`, value: 'DGEDOT' },
  { label: $localize`Direcció General`, value: 'Direcció General' },
];
