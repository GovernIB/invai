import { KeyLabel } from '@models/table.model';
import {
  SOFT_DELETE_STATUS_LABELS,
  SOFT_DELETE_STATUS_OPTIONS,
} from '@shared/constants/soft-delete-status.constants';

export const ENVIRONMENT_STATUS_LABELS = SOFT_DELETE_STATUS_LABELS;
export const ENVIRONMENT_STATUS_OPTIONS = SOFT_DELETE_STATUS_OPTIONS;

export const ENVIRONMENTS_TABLE_COLUMNS: KeyLabel[] = [
  { key: 'code', label: $localize`Codi`, sortBy: 'code', minWidth: '8rem' },
  {
    key: 'name',
    wrap: true,
    maxWidth: '24rem',
    label: $localize`Nom`,
    sortBy: 'name',
    minWidth: '14rem',
  },
  {
    key: 'nameEs',
    wrap: true,
    maxWidth: '24rem',
    label: $localize`Nom en castellà`,
    sortBy: 'nameEs',
    minWidth: '14rem',
  },
  { key: 'status', label: $localize`Estat`, sortBy: 'deletedAt', minWidth: '8rem' },
];
