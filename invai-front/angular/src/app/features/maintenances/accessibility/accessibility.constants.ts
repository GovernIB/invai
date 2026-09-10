import { KeyLabel } from '@models/table.model';

import { AccessibilityResourceKey } from './accessibility.model';
import { ACCESSIBILITY_PANEL_COPY } from './accessibility.i18n';

export interface AccessibilityResourceDefinition {
  key: AccessibilityResourceKey;
  singular: string;
  plural: string;
  bilingual: boolean;
  columns: KeyLabel[];
}

const NAME_COLUMN: KeyLabel = {
  key: 'name',
  wrap: true,
  maxWidth: '24rem',
  label: $localize`:@@accessibilityNameColumn:Nom`,
  sortBy: 'name',
  minWidth: '14rem',
};
const NAME_ES_COLUMN: KeyLabel = {
  key: 'nameEs',
  wrap: true,
  maxWidth: '24rem',
  label: $localize`:@@accessibilityNameEsColumn:Nom en castellà`,
  sortBy: 'nameEs',
  minWidth: '14rem',
};
const STATUS_COLUMN: KeyLabel = {
  key: 'status',
  label: $localize`:@@accessibilityStatusColumn:Estat`,
  sortBy: 'deletedAt',
  minWidth: '8rem',
};

const BILINGUAL_COLUMNS: KeyLabel[] = [
  { key: 'id', label: $localize`:@@accessibilityIdColumn:ID`, sortBy: 'id', minWidth: '5rem' },
  NAME_COLUMN,
  NAME_ES_COLUMN,
  STATUS_COLUMN,
];

export const ACCESSIBILITY_RESOURCE_DEFINITIONS: Record<
  AccessibilityResourceKey,
  AccessibilityResourceDefinition
> = {
  'classification-segment': {
    key: 'classification-segment',
    ...ACCESSIBILITY_PANEL_COPY.classificationSegment,
    bilingual: true,
    columns: BILINGUAL_COLUMNS,
  },
  'compliance-situation': {
    key: 'compliance-situation',
    ...ACCESSIBILITY_PANEL_COPY.complianceSituation,
    bilingual: true,
    columns: BILINGUAL_COLUMNS,
  },
};

export const ACCESSIBILITY_MAINTENANCE_PANELS = [
  {
    id: 'classification-segments',
    resource: 'classification-segment',
    ...ACCESSIBILITY_PANEL_COPY.classificationSegment,
  },
  {
    id: 'compliance-situations',
    resource: 'compliance-situation',
    ...ACCESSIBILITY_PANEL_COPY.complianceSituation,
  },
] satisfies {
  id: string;
  resource: AccessibilityResourceKey;
  title: string;
  description: string;
}[];
