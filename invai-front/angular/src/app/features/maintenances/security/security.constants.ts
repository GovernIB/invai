import { KeyLabel } from '@models/table.model';

import { SecurityResourceKey } from './security.model';
import { SECURITY_PANEL_COPY } from './security.i18n';

export interface SecurityResourceDefinition {
  key: SecurityResourceKey;
  singular: string;
  plural: string;
  bilingual: boolean;
  columns: KeyLabel[];
}

const NAME_COLUMN: KeyLabel = {
  key: 'name',
  wrap: true,
  maxWidth: '24rem',
  label: $localize`:@@securityNameColumn:Nom`,
  sortBy: 'name',
  minWidth: '14rem',
};
const NAME_ES_COLUMN: KeyLabel = {
  key: 'nameEs',
  wrap: true,
  maxWidth: '24rem',
  label: $localize`:@@securityNameEsColumn:Nom en castellà`,
  sortBy: 'nameEs',
  minWidth: '14rem',
};
const STATUS_COLUMN: KeyLabel = {
  key: 'status',
  label: $localize`:@@securityStatusColumn:Estat`,
  sortBy: 'deletedAt',
  minWidth: '8rem',
};

const BILINGUAL_COLUMNS = [NAME_COLUMN, NAME_ES_COLUMN, STATUS_COLUMN];
const NAME_ONLY_COLUMNS = [NAME_COLUMN, STATUS_COLUMN];

export const SECURITY_RESOURCE_DEFINITIONS: Record<
  SecurityResourceKey,
  SecurityResourceDefinition
> = {
  'ens-requirement': {
    key: 'ens-requirement',
    ...SECURITY_PANEL_COPY.ensRequirement,
    bilingual: true,
    columns: BILINGUAL_COLUMNS,
  },
  'identity-provider': {
    key: 'identity-provider',
    ...SECURITY_PANEL_COPY.identityProvider,
    bilingual: false,
    columns: NAME_ONLY_COLUMNS,
  },
  'personal-data-processing': {
    key: 'personal-data-processing',
    ...SECURITY_PANEL_COPY.personalDataProcessing,
    bilingual: true,
    columns: BILINGUAL_COLUMNS,
  },
  'security-measure-type': {
    key: 'security-measure-type',
    ...SECURITY_PANEL_COPY.securityMeasureType,
    bilingual: true,
    columns: BILINGUAL_COLUMNS,
  },
  'web-context': {
    key: 'web-context',
    ...SECURITY_PANEL_COPY.webContext,
    bilingual: true,
    columns: BILINGUAL_COLUMNS,
  },
};

export interface SecurityMaintenancePanel {
  id: string;
  title: string;
  description: string;
  resource: SecurityResourceKey;
}

export const SECURITY_MAINTENANCE_PANELS: SecurityMaintenancePanel[] = [
  {
    id: 'web-contexts',
    title: SECURITY_PANEL_COPY.webContext.title,
    description: SECURITY_PANEL_COPY.webContext.description,
    resource: 'web-context',
  },
  {
    id: 'identity-providers',
    title: SECURITY_PANEL_COPY.identityProvider.title,
    description: SECURITY_PANEL_COPY.identityProvider.description,
    resource: 'identity-provider',
  },
  {
    id: 'personal-data-processing',
    title: SECURITY_PANEL_COPY.personalDataProcessing.title,
    description: SECURITY_PANEL_COPY.personalDataProcessing.description,
    resource: 'personal-data-processing',
  },
  {
    id: 'security-measure-types',
    title: SECURITY_PANEL_COPY.securityMeasureType.title,
    description: SECURITY_PANEL_COPY.securityMeasureType.description,
    resource: 'security-measure-type',
  },
  {
    id: 'ens-requirements',
    title: SECURITY_PANEL_COPY.ensRequirement.title,
    description: SECURITY_PANEL_COPY.ensRequirement.description,
    resource: 'ens-requirement',
  },
];
