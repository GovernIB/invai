import { KeyLabel } from '@models/table.model';
import {
  SOFT_DELETE_STATUS_LABELS,
  SOFT_DELETE_STATUS_OPTIONS,
} from '@shared/constants/soft-delete-status.constants';

import { SYSTEMS_PANEL_DESCRIPTIONS } from './systems.i18n';
import { SYSTEMS_ROUTES_LABELS } from './systems.routes.i18n';

export type SystemsPanelId =
  | 'environments'
  | 'physical-servers'
  | 'servers'
  | 'database-servers'
  | 'databases'
  | 'database-vendors';

export interface SystemsPanel {
  id: SystemsPanelId;
  title: string;
  description: string;
}

export const SYSTEMS_PANELS: SystemsPanel[] = [
  {
    id: 'environments',
    title: SYSTEMS_ROUTES_LABELS.ENVIRONMENTS,
    description: SYSTEMS_PANEL_DESCRIPTIONS.environments,
  },
  {
    id: 'physical-servers',
    title: SYSTEMS_ROUTES_LABELS.PHYSICAL_SERVERS,
    description: SYSTEMS_PANEL_DESCRIPTIONS.physicalServers,
  },
  {
    id: 'servers',
    title: SYSTEMS_ROUTES_LABELS.SERVERS,
    description: SYSTEMS_PANEL_DESCRIPTIONS.servers,
  },
  {
    id: 'database-servers',
    title: SYSTEMS_ROUTES_LABELS.DATABASE_SERVERS,
    description: SYSTEMS_PANEL_DESCRIPTIONS.databaseServers,
  },
  {
    id: 'databases',
    title: SYSTEMS_ROUTES_LABELS.DATABASES,
    description: SYSTEMS_PANEL_DESCRIPTIONS.databases,
  },
  {
    id: 'database-vendors',
    title: SYSTEMS_ROUTES_LABELS.DATABASE_VENDORS,
    description: SYSTEMS_PANEL_DESCRIPTIONS.databaseVendors,
  },
];

export const INFRASTRUCTURE_STATUS_LABELS = SOFT_DELETE_STATUS_LABELS;
export const INFRASTRUCTURE_STATUS_OPTIONS = SOFT_DELETE_STATUS_OPTIONS;

export const PHYSICAL_SERVERS_TABLE_COLUMNS: KeyLabel[] = [
  { key: 'name', label: $localize`Servidor`, sortBy: 'name', minWidth: '17rem' },
  {
    key: 'environment',
    label: $localize`Entorn`,
    sortBy: 'environment.name',
    minWidth: '11rem',
  },
  { key: 'status', label: $localize`Estat`, sortBy: 'deletedAt', minWidth: '8rem' },
];

export const SERVERS_TABLE_COLUMNS: KeyLabel[] = [
  {
    key: 'server',
    label: $localize`Servidor`,
    sortBy: 'server.name',
    minWidth: '17rem',
  },
  {
    key: 'environment',
    label: $localize`Entorn`,
    sortBy: 'server.environment.name',
    minWidth: '11rem',
  },
  { key: 'instance', label: $localize`Instància`, sortBy: 'instance', minWidth: '11rem' },
  { key: 'port', label: $localize`Port`, sortBy: 'port', minWidth: '6rem' },
  { key: 'version', label: $localize`Versió`, sortBy: 'version', minWidth: '10rem' },
  { key: 'status', label: $localize`Estat`, sortBy: 'deletedAt', minWidth: '8rem' },
  {
    key: 'description',
    label: $localize`Observacions`,
    sortBy: 'description',
    minWidth: '14rem',
  },
];

export const DATABASES_TABLE_COLUMNS: KeyLabel[] = [
  {
    key: 'server',
    label: $localize`Servidor de BD`,
    sortBy: 'server.name',
    minWidth: '17rem',
  },
  {
    key: 'environment',
    label: $localize`Entorn`,
    sortBy: 'server.environment.name',
    minWidth: '11rem',
  },
  { key: 'service', label: $localize`Servei / SID`, sortBy: 'service', minWidth: '11rem' },
  { key: 'port', label: $localize`Port`, sortBy: 'port', minWidth: '6rem' },
  {
    key: 'databaseType',
    label: $localize`Proveïdor`,
    sortBy: 'databaseType.name',
    minWidth: '10rem',
  },
  { key: 'status', label: $localize`Estat`, sortBy: 'deletedAt', minWidth: '8rem' },
  {
    key: 'description',
    label: $localize`Observacions`,
    sortBy: 'description',
    minWidth: '14rem',
  },
];

export const DATABASE_VENDORS_TABLE_COLUMNS: KeyLabel[] = [
  { key: 'name', label: $localize`Proveïdor`, sortBy: 'name', minWidth: '16rem' },
  {
    key: 'defaultPort',
    label: $localize`Port per defecte`,
    sortBy: 'defaultPort',
    minWidth: '10rem',
  },
  { key: 'status', label: $localize`Estat`, sortBy: 'deletedAt', minWidth: '8rem' },
];
